package services.elements;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
@RequiredArgsConstructor
@Getter
public class XMLException extends Exception {
	private final String message;
	private final Element annotatedElement;
	private final Class<? extends Annotation> annotationClass;
}
