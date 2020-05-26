package strategies.process.impl;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

import annotations.SchemaElement;
import processors.ServiceDTO;
import services.elements.XMLElement;
import strategies.process.ProcessStrategy;

public class SchemaElementStrategy extends ProcessStrategy {
	public SchemaElementStrategy(ServiceDTO serviceDTO, Annotation annotation, Element annotatedElement) {
		super(serviceDTO, annotation, annotatedElement);
	}

	@Override
	public void processAnnotation() {
		getServiceDTO().getXmlService()//
			.addSchemaElement(XMLElement.builder()//
				.schemaElement((SchemaElement) getAnnotation())//
				.isCollectionSubType(isCollectionType(getAnnotatedElement()))//
				.annotatedElement(getAnnotatedElement())//
				.build());
	}
}
