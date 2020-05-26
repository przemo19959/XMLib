package strategies.process.impl;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilderFactory;

import annotations.GenerateSchema;
import lombok.SneakyThrows;
import processors.ServiceDTO;
import services.elements.XMLRoot;
import strategies.process.ProcessStrategy;

public class GenerateSchemaStrategy extends ProcessStrategy {
	public GenerateSchemaStrategy(ServiceDTO serviceDTO, Annotation annotation, Element annotatedElement) {
		super(serviceDTO, annotation, annotatedElement);
	}

	@Override
	@SneakyThrows
	public void processAnnotation() {
		getServiceDTO().getXmlService()//
			.addRoot(new XMLRoot(	DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(), //
									getAnnotatedElement(), //
									(GenerateSchema) getAnnotation()));
	}
}
