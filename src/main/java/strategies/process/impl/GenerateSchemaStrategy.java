package strategies.process.impl;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilderFactory;

import annotations.GenerateSchema;
import lombok.SneakyThrows;
import processors.ServiceDTO;
import services.XMLRoot;
import strategies.process.ProcessStrategy;

public class GenerateSchemaStrategy extends ProcessStrategy {
	public GenerateSchemaStrategy(ServiceDTO serviceDTO, Annotation annotation, Element annotatedElement) {
		super(serviceDTO, annotation, annotatedElement);
	}

	@Override
	@SneakyThrows
	public void processAnnotation() {
		GenerateSchema a=(GenerateSchema) getAnnotation();
		
		getServiceDTO().getXmlService().addRoot(XMLRoot.builder()//
			.document(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument())//
			.generateSchema(a)//
			.resourceFolderPath(a.path())//
			.fileName(getAnnotatedElement().getSimpleName().toString())//
			.annotatedElement(getAnnotatedElement())//
			.build());
	}
}
