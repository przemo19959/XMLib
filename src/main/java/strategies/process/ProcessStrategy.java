package strategies.process;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic.Kind;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import processors.ServiceDTO;

@Getter
@RequiredArgsConstructor
public abstract class ProcessStrategy {
	private final ServiceDTO serviceDTO;
	private final Annotation annotation;
	private final Element annotatedElement;

	public abstract void processAnnotation();

	public boolean isCollectionType(Element element) {
		return getServiceDTO().getTypes()//
			.directSupertypes(element.asType()).toString()//
			.contains(Collection.class.getCanonicalName());
	}

	public boolean equalsKind(Element element, ElementKind kind) {
		return element.getKind().equals(kind);
	}

	public List<? extends Element> getEnclosedElements(Element element) {
		return getServiceDTO().getTypes()//
			.asElement(element.asType())//
			.getEnclosedElements();
	}
	
	public void log(String msg) {
		getServiceDTO().getMessager().printMessage(Kind.NOTE, msg);
	}
}
