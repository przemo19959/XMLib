package strategies.process.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
		getServiceDTO().getXmlService()//
			.addSchemaAttribute(XMLAttribute.builder()//
				.schemaAttribute((SchemaAttribute) getAnnotation())//
				.enumConstants(getEnumConstantsIfElementIsEnum())//
				.annotatedElement(getAnnotatedElement())//
				.build());
	}

	private List<Element> getEnumConstantsIfElementIsEnum() {
		List<Element> enumConstants = new ArrayList<>();
		if(equalsTypeKind(getAnnotatedElement(), ElementKind.ENUM)) {
			enumConstants.addAll(getEnclosedElements(getAnnotatedElement()).stream()//
				.filter(e -> equalsKind(e, ElementKind.ENUM_CONSTANT))//
				.collect(Collectors.toList()));
		}
		return enumConstants;
	}
}
