package processors;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SchemaProcessor extends AbstractProcessor {
	// Processor API
	private Messager messager;
	private Types types;
	private Elements elements;

	// own API
	private XMLService xmlService;
	private String projectPath;

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
					// messager.printMessage(Kind.ERROR, ":: " + annotatedElement.getSimpleName());
					if(annotatedElement.getAnnotation(GenerateSchema.class) != null) {
						// messager.printMessage(Kind.ERROR, "GenerateSchema: " + annotatedElement.getSimpleName());
						GenerateSchema a = annotatedElement.getAnnotation(GenerateSchema.class);
						xmlService.addRoot(
							XMLRoot.builder()//
								.messager(messager)//
								.document(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument())//
								.generateSchema(a)//
								.resourceFolderPath(a.path())//
								.fileName(annotatedElement.getSimpleName().toString())//
								.annotatedElement(annotatedElement)//
								.build());
					} else if(annotatedElement.getAnnotation(SchemaElement.class) != null) {

						boolean isCollectionSubType = types.isSubtype(
							annotatedElement.asType(), //
							elements.getTypeElement(Collection.class.getCanonicalName()).asType());
//						messager.printMessage(Kind.ERROR, "SchemaElement: " + annotatedElement.asType().toString() + ", isCollection: " + isCollectionSubType);
						xmlService.addSchemaElement(
							XMLElement.builder()//
								.schemaElement(annotatedElement.getAnnotation(SchemaElement.class))//
								.isCollectionSubType(isCollectionSubType)//
								.annotatedElement(annotatedElement)//
								.build());
					} else if(annotatedElement.getAnnotation(SchemaAttribute.class) != null) {

						// messager.printMessage(Kind.ERROR, "SchemaAttribute: " + annotatedElement.asType());
						// messager.printMessage(Kind.ERROR, "SchemaAttribute: " + types.asElement(annotatedElement.asType()));
						List<Element> enumConstants = null;
						if(types.asElement(annotatedElement.asType()) != null && ElementKind.ENUM.equals(types.asElement(annotatedElement.asType()).getKind())) {
							enumConstants = types.asElement(annotatedElement.asType()).getEnclosedElements().stream()//
								.filter(e -> e.getKind().equals(ElementKind.ENUM_CONSTANT))//
								.collect(Collectors.toList());
						}
						xmlService.addSchemaAttribute(
							XMLAttribute.builder()//
								.schemaAttribute(annotatedElement.getAnnotation(SchemaAttribute.class))//
								.enumConstants(enumConstants)//
								.annotatedElement(annotatedElement)//
								.build());
					}
				}
			}
			messager.printMessage(Kind.ERROR, "Size: " + xmlService.getSchemaRoots().size());
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

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new LinkedHashSet<String>();
		annotations.add(GenerateSchema.class.getCanonicalName());
		annotations.add(SchemaElement.class.getCanonicalName());
		annotations.add(SchemaAttribute.class.getCanonicalName());
		return annotations;
	}

}
