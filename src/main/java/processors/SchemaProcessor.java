package processors;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;

import annotations.GenerateSchema;
import annotations.SchemaAttribute;
import annotations.SchemaElement;
import lombok.SneakyThrows;
import services.XMLAttribute;
import services.XMLElement;
import services.XMLException;
import services.XMLRoot;
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
	
	private ProcessStrategy<Annotation> strategy;

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
		try {
			for(TypeElement annotation:annotations) {
				for(Element annotatedElement:roundEnv.getElementsAnnotatedWith(annotation)) {
					messager.printMessage(Kind.NOTE, annotatedElement.getSimpleName() + " => " + annotation.getSimpleName());
					// messager.printMessage(Kind.ERROR, ":: " + annotatedElement.getSimpleName());
					//TODO - 25 maj 2020:tutaj jeszcze dać fabrykę, która zwróci odpowiednią implementację na podstawie
					//czyli strategy=Factory.getInstance(annotation);
					strategy.processAnnotation(annotatedElement);
					
					
					if(annotatedElement.getAnnotation(GenerateSchema.class) != null) {
						// messager.printMessage(Kind.ERROR, "GenerateSchema: " + annotatedElement.getSimpleName());
						GenerateSchema a = annotatedElement.getAnnotation(GenerateSchema.class);
						xmlService.addRoot(XMLRoot.builder()//
							.messager(messager)//
							.document(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument())//
							.generateSchema(a)//
							.resourceFolderPath(a.path())//
							.fileName(annotatedElement.getSimpleName().toString())//
							.annotatedElement(annotatedElement)//
							.build());
					} else if(annotatedElement.getAnnotation(SchemaElement.class) != null) {
						boolean isCollectionSubType = types.isSubtype(annotatedElement.asType(), //
							elements.getTypeElement(Collection.class.getCanonicalName()).asType());
						xmlService.addSchemaElement(XMLElement.builder()//
							.schemaElement(annotatedElement.getAnnotation(SchemaElement.class))//
							.isCollectionSubType(isCollectionSubType)//
							.annotatedElement(annotatedElement)//
							.build());
					} else if(annotatedElement.getAnnotation(SchemaAttribute.class) != null) {
						List<Element> enumConstants = null;
						if(types.asElement(annotatedElement.asType()) != null && ElementKind.ENUM.equals(types.asElement(annotatedElement.asType()).getKind())) {
							enumConstants = types.asElement(annotatedElement.asType()).getEnclosedElements().stream()//
								.filter(e -> e.getKind().equals(ElementKind.ENUM_CONSTANT))//
								.collect(Collectors.toList());
						}
						xmlService.addSchemaAttribute(XMLAttribute.builder()//
							.schemaAttribute(annotatedElement.getAnnotation(SchemaAttribute.class))//
							.enumConstants(enumConstants)//
							.annotatedElement(annotatedElement)//
							.build());
					}
				}
			}
			messager.printMessage(Kind.NOTE, "schema roots size: " + xmlService.getSchemaRoots().size());
			xmlService.createSchemaFile();
		} catch (XMLException xe) {
			messager.printMessage(Kind.ERROR, xe.getMessage(), xe.getAnnotatedElement(), getAnnotationMirror(xe.getAnnotatedElement(), xe.getAnnotationClass()));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
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
