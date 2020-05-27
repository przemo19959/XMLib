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
	
	/**
	 * This method compares kind of element in context of surounding class. For example "private String name;"
	 * is of ElementKind.FIELD kind. Contructor is ElementKind.CONSTRUCTOR, and so on. 
	 */
	public boolean equalsKind(Element element, ElementKind kind) {
		return element.getKind().equals(kind);
	}
	
	/**
	 * This method compares kind of element type. For example for field "private WorkerType type;" it will
	 * compare element type. WorkerType is enum, and so method yields true only for ElementKind.ENUM. 
	 */
	public boolean equalsTypeKind(Element element, ElementKind kind) {
		Element e=null;
		if((e=getServiceDTO().getTypes().asElement(element.asType()))!=null)
			return equalsKind(e, kind);
		return false;
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
