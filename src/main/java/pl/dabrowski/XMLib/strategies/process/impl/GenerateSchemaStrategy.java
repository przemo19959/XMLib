package pl.dabrowski.XMLib.strategies.process.impl;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.SneakyThrows;
import pl.dabrowski.XMLib.annotations.GenerateSchema;
import pl.dabrowski.XMLib.services.elements.XMLRoot;
import pl.dabrowski.XMLib.strategies.process.ProcessStrategy;
import pl.dabrowski.XMLib.utils.ServiceDTO;

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
