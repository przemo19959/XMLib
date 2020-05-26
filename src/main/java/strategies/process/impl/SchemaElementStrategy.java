package strategies.process.impl;

import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.lang.model.element.Element;

import annotations.SchemaElement;
import processors.ServiceDTO;
import services.XMLElement;
import strategies.process.ProcessStrategy;

public class SchemaElementStrategy extends ProcessStrategy {
	public SchemaElementStrategy(ServiceDTO serviceDTO, Annotation annotation, Element annotatedElement) {
		super(serviceDTO, annotation, annotatedElement);
	}

	@Override
	public void processAnnotation() {
		boolean isCollectionSubType = getServiceDTO().getTypes().isSubtype(getAnnotatedElement().asType(), //
			getServiceDTO().getElements().getTypeElement(Collection.class.getCanonicalName()).asType());
		getServiceDTO().getXmlService().addSchemaElement(XMLElement.builder()//
			.schemaElement((SchemaElement) getAnnotation())//
			.isCollectionSubType(isCollectionSubType)//
			.annotatedElement(getAnnotatedElement())//
			.build());
	}
}
