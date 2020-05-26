package factories.process;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

import annotations.GenerateSchema;
import annotations.SchemaAttribute;
import annotations.SchemaElement;
import lombok.RequiredArgsConstructor;
import processors.ServiceDTO;
import strategies.process.ProcessStrategy;
import strategies.process.impl.GenerateSchemaStrategy;
import strategies.process.impl.SchemaAttributeStrategy;
import strategies.process.impl.SchemaElementStrategy;

@RequiredArgsConstructor
public class ProcessFactory {
	private final ServiceDTO serviceDTO;

	public ProcessStrategy getInstance(Element annotatedElement) {
		Annotation a=null;
		if((a = annotatedElement.getAnnotation(GenerateSchema.class)) != null)
			return new GenerateSchemaStrategy(serviceDTO, a, annotatedElement);
		else if((a = annotatedElement.getAnnotation(SchemaElement.class)) != null)
			return new SchemaElementStrategy(serviceDTO, a, annotatedElement);
		else if((a = annotatedElement.getAnnotation(SchemaAttribute.class)) != null)
			return new SchemaAttributeStrategy(serviceDTO, a, annotatedElement);
		throw new IllegalArgumentException("Annotation doesn't have strategy!");
	}
}
