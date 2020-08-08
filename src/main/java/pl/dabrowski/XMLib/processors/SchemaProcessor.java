package pl.dabrowski.XMLib.processors;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Set;

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

import lombok.SneakyThrows;
import pl.dabrowski.XMLib.factories.process.ProcessFactory;
import pl.dabrowski.XMLib.services.XMLService;
import pl.dabrowski.XMLib.services.elements.XMLException;
import pl.dabrowski.XMLib.strategies.process.ProcessStrategy;
import pl.dabrowski.XMLib.utils.PackageTool;
import pl.dabrowski.XMLib.utils.ServiceDTO;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SchemaProcessor extends AbstractProcessor {
	private static final String STRATEGY_CANON_PATTERN = "{0}.{1}Strategy";
	//paths can't start or end with . sign
	private static final String PROCESSOR_STRATEGIES_IMPL_PATH = "pl.dabrowski.XMLib.strategies.process.impl";
	private static final String PROCESSOR_ANNOTATIONS_PACKAGE_PATH = "pl.dabrowski.XMLib.annotations";

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
		log("Processor XMLib started...");
	}

	@SneakyThrows
	private void setProjectPath(Filer filer) {
		URI uri = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "tmp", (Element[]) null).toUri();
		projectPath = Paths.get(uri).getParent().getParent().toString();
		projectPath = projectPath.substring(0, projectPath.lastIndexOf("\\")).replace("\\", "/");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		xmlService = new XMLService();
		serviceDTO = new ServiceDTO(messager, types, elements, xmlService);
		factory = new ProcessFactory(serviceDTO);
		try {
			for(TypeElement annotation:annotations) {
				for(Element annotatedElement:roundEnv.getElementsAnnotatedWith(annotation)) {
					strategy = factory.getInstance(annotatedElement);
					strategy.processAnnotation();
				}
			}
			xmlService.createSchemaFile(projectPath);
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
		PackageTool.doForEveryObjectInPackage(PROCESSOR_ANNOTATIONS_PACKAGE_PATH, je -> {
			String annotationCanonicalName = je.getName().replace("/", ".").replace(".class", "");
			annotations.add(annotationCanonicalName);
			try {
				Class<? extends ProcessStrategy> strategy = Class.forName(MessageFormat.format(STRATEGY_CANON_PATTERN, //
					PROCESSOR_STRATEGIES_IMPL_PATH, annotationCanonicalName.substring(annotationCanonicalName.lastIndexOf(".") + 1)))//
					.asSubclass(ProcessStrategy.class);
				Class<? extends Annotation> annotationCls = Class.forName(annotationCanonicalName).asSubclass(Annotation.class);
				ProcessFactory.register(annotationCls, strategy);
			} catch (ClassNotFoundException e) {
				//do nothing
			}
		});
		//		annotations.forEach(i->log(i));
		return annotations;
	}
}
