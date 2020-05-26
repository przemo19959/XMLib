package strategies.process;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import processors.ServiceDTO;

@Getter
@RequiredArgsConstructor
public abstract class ProcessStrategy{
	private final ServiceDTO serviceDTO;
	private final Annotation annotation;
	private final Element annotatedElement;
	
	public abstract void processAnnotation();
}
