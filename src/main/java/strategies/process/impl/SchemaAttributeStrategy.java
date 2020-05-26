package strategies.process.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import annotations.SchemaAttribute;
import processors.ServiceDTO;
import services.elements.XMLAttribute;
import strategies.process.ProcessStrategy;

public class SchemaAttributeStrategy extends ProcessStrategy {
	public SchemaAttributeStrategy(ServiceDTO serviceDTO, Annotation annotation, Element annotatedElement) {
		super(serviceDTO, annotation, annotatedElement);
	}

	@Override
	public void processAnnotation() {
		List<Element> enumConstants = null;
		log("b");
		if(equalsKind(getAnnotatedElement(), ElementKind.ENUM)) {
			log("a");
			enumConstants = getEnclosedElements(getAnnotatedElement()).stream()//
				.filter(e -> equalsKind(e, ElementKind.ENUM_CONSTANT))//
				.collect(Collectors.toList());
		}

		getServiceDTO().getXmlService()//
			.addSchemaAttribute(XMLAttribute.builder()//
				.schemaAttribute((SchemaAttribute) getAnnotation())//
				.enumConstants(enumConstants)//
				.annotatedElement(getAnnotatedElement())//
				.build());
	}
}
