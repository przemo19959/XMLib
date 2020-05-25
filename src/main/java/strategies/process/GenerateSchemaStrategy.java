package strategies.process;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilderFactory;

import annotations.GenerateSchema;
import lombok.SneakyThrows;
import services.XMLRoot;
import services.XMLService;

public class GenerateSchemaStrategy extends ProcessStrategy<GenerateSchema> {

	public GenerateSchemaStrategy(XMLService xmlService, Class<GenerateSchema> annotationCls) {
		super(xmlService, annotationCls);
	}

	@Override
	@SneakyThrows
	public void processAnnotation(Element annotatedElement) {
		super.processAnnotation(annotatedElement);
		getXmlService().addRoot(XMLRoot.builder()//
			//			.messager(messager)//
			.document(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument())//
			.generateSchema(getAnnotation())//
			.resourceFolderPath(getAnnotation().path())//
			.fileName(annotatedElement.getSimpleName().toString())//
			.annotatedElement(annotatedElement)//
			.build());
	}
}
