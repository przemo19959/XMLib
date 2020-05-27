package pl.dabrowski.XMLib.strategies.process.impl;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

import pl.dabrowski.XMLib.annotations.SchemaElement;
import pl.dabrowski.XMLib.services.elements.XMLElement;
import pl.dabrowski.XMLib.strategies.process.ProcessStrategy;
import pl.dabrowski.XMLib.utils.ServiceDTO;

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
