package strategies.process;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import services.XMLService;

@Getter
@RequiredArgsConstructor
public abstract class ProcessStrategy<T extends Annotation> {
	private final XMLService xmlService;
	private final Class<T> annotationCls;
	private T annotation;
	
	public void processAnnotation(Element annotatedElement) {
		annotation = annotatedElement.getAnnotation(annotationCls);
	};
}
