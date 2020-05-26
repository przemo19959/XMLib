package processors;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;

import factories.process.ProcessFactory;
import lombok.SneakyThrows;
import services.XMLException;
import services.XMLService;
import strategies.process.ProcessStrategy;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SchemaProcessor extends AbstractProcessor {
	private static final String PROCESSOR_ANNOTATIONS_PACKAGE_PATH = "annotations";

	// Processor API
	private Messager messager;
	private Types types;
	private Elements elements;

	// own API
	private XMLService xmlService;
	private String projectPath;

	private ProcessStrategy strategy;
	private ProcessFactory factory;
	private ServiceDTO serviceDTO;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		Filer filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();

		setProjectPath(filer);
	}

	@SneakyThrows
	private void setProjectPath(Filer filer) {
		URI uri = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "tmp", (Element[]) null).toUri();
		projectPath = Paths.get(uri).getParent().getParent().toString();
		projectPath = projectPath.substring(0, projectPath.lastIndexOf("\\")).replace("\\", "/");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		xmlService = new XMLService(projectPath);
		serviceDTO = new ServiceDTO(types, elements, xmlService);
		factory = new ProcessFactory(serviceDTO);
		
		try {
			for(TypeElement annotation:annotations) {
				for(Element annotatedElement:roundEnv.getElementsAnnotatedWith(annotation)) {
					log(annotatedElement.getSimpleName() + " => " + annotation.getSimpleName());
					strategy = factory.getInstance(annotatedElement);
					strategy.processAnnotation();
				}
			}
			log("schema roots size: " + xmlService.getSchemaRoots().size());
			xmlService.createSchemaFile();
		} catch (XMLException xe) {
			messager.printMessage(Kind.ERROR, xe.getMessage(), xe.getAnnotatedElement(), getAnnotationMirror(xe.getAnnotatedElement(), xe.getAnnotationClass()));
		} 
		return true;
	}

	private <A extends Annotation> AnnotationMirror getAnnotationMirror(Element annotatedElement, Class<A> annotationClass) {
		for(AnnotationMirror aMirror:annotatedElement.getAnnotationMirrors()) {
			if(aMirror.getAnnotationType().toString().equals(annotationClass.getCanonicalName()))
				return aMirror;
		}
		return null;
	}

	private void log(String msg) {
		messager.printMessage(Kind.NOTE, msg);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new LinkedHashSet<String>();
		doForEveryObjectInPackage(PROCESSOR_ANNOTATIONS_PACKAGE_PATH, //
			je -> annotations.add(je.getName().replace("/", ".").replace(".class", "")));
		//		annotations.forEach(i->log(i));
		return annotations;
	}

	//========================================
	private void doForEveryObjectInPackage(String packageName, Consumer<JarEntry> body) {
		String packagePath = getFullPathToPackage(packageName);
		if(packagePath.contains("jar!")) { //if JAR
			packagePath = fromFullPathToClassical(packagePath);
			try (JarFile jf = new JarFile(packagePath)) {
				final Enumeration<JarEntry> entries = jf.entries();
				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					if(isStringJavaLanguageObject(entry.getName())//
						&& entry.getName().contains("$") == false//
						&& entry.getName().startsWith(packageName.replace(".", "/"))) {
						body.accept(entry);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getFullPathToPackage(String packageName) {
		return getClass().getResource("").toString() + preparePackageString(packageName);
	}

	private String fromFullPathToClassical(String input) {
		return input//
			.substring(0, input.lastIndexOf(".jar!") + 4)//remove path after .jar!
			.substring("jar:file:/".length());//remove initial protocol
	}

	private static boolean isStringJavaLanguageObject(String input) {
		return input.endsWith(".class");
	}

	private static String preparePackageString(String input) { //@formatter:off
		input = input.replace(".", "/");
		if(input.startsWith("/")) input = input.substring(1); //lose first slash /
		if(input.endsWith("/") == false) input = input + "/"; //add last slash if not present
		return input;
	}//@formatter:on
	//===============================================
}
