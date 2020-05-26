package strategies.process.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import annotations.SchemaAttribute;
import processors.ServiceDTO;
import services.XMLAttribute;
import strategies.process.ProcessStrategy;

public class SchemaAttributeStrategy extends ProcessStrategy {
	public SchemaAttributeStrategy(ServiceDTO serviceDTO, Annotation annotation, Element annotatedElement) {
		super(serviceDTO, annotation, annotatedElement);
	}

	@Override
	public void processAnnotation() {
		List<Element> enumConstants = null;
		if(getServiceDTO().getTypes().asElement(getAnnotatedElement().asType()) != null && ElementKind.ENUM.equals(getServiceDTO().getTypes().asElement(getAnnotatedElement().asType()).getKind())) {
			enumConstants = getServiceDTO().getTypes().asElement(getAnnotatedElement().asType()).getEnclosedElements().stream()//
				.filter(e -> e.getKind().equals(ElementKind.ENUM_CONSTANT))//
				.collect(Collectors.toList());
		}
		getServiceDTO().getXmlService().addSchemaAttribute(XMLAttribute.builder()//
			.schemaAttribute((SchemaAttribute) getAnnotation())//
			.enumConstants(enumConstants)//
			.annotatedElement(getAnnotatedElement())//
			.build());
	}
}
