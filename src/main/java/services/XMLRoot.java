package services;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import annotations.GenerateSchema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class XMLRoot {
	
	//TODO - 30 sty 2020:ogólnie działa, ale do poprawki. Najlepiej wykorzystać jakiś algorytm tworzenia drzewa, bo straszne zamieszanie jest
	private final Messager messager;

	private final Document document;
	private final javax.lang.model.element.Element annotatedElement;
	private final GenerateSchema generateSchema;

	// pola nie ustawiane przez buildera
	private final String resourceFolderPath;
	private final String fileName;

	public void createXML(List<XMLElement> schemaElements, List<XMLAttribute> schemaAttributes) throws XMLException {
		Element body = document.createElement("xs:schema");
		body.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		document.appendChild(body);

		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		// String str = serializer.writeToString(node);

		XMLElement rootElement = XMLElement.builder()//
			.document(document)//
			.schemaElement(generateSchema.rootElement())//
			.annotatedElement(annotatedElement)//
			.isSequenceNeeded(true)//
			.build();

		rootElement.createXMLElement();
		body.appendChild(rootElement.getElement());

		// process schema elements
		messager.printMessage(Kind.ERROR, "rozmiar: " + schemaElements.size());
		for(XMLElement e:schemaElements) {
			e.setDocument(document);
			e.createXMLElement();
			// messager.printMessage(Kind.ERROR, "element: "+serializer.writeToString(e.getElement()));
		}

		for(XMLElement schemaElement:schemaElements) {
			// messager.printMessage(Kind.ERROR, "enclosingElement: "+schemaElement.getAnnotatedElement()+", annotatedElement: "+schemaElement.getAnnotatedElement());
			List<XMLElement> tmp = schemaElements.stream()//
				.filter(e -> {
					// messager.printMessage(Kind.ERROR, "e1: "+e.getAnnotatedElement().asType()+", e2: "+schemaElement.getAnnotatedElement());

					String parameterType = e.getAnnotatedElement().asType().toString(); // typ pola oznaczonego @SchemaElement
					if(e.isCollectionSubType()) // jeśli Collection<?> pobierz ?
						parameterType = parameterType.substring(parameterType.indexOf('<') + 1, parameterType.lastIndexOf('>'));
					return parameterType.equals(schemaElement.getAnnotatedElement().getEnclosingElement().asType().toString());

					// if(e.equals(schemaElement)==false)
					// return e.getAnnotatedElement().getEnclosingElement().equals(schemaElement.getAnnotatedElement());
					// return false;
				})//
				// .sorted(
				// (e1, e2) -> Integer.compare(
				// schemaElement.getAnnotatedElement().getEnclosedElements().indexOf(e1.getAnnotatedElement()), //
				// schemaElement.getAnnotatedElement().getEnclosedElements().indexOf(e2.getAnnotatedElement())))
				.collect(Collectors.toList());
			// messager.printMessage(Kind.ERROR, "tmp: "+tmp.size()+", dla: "+schemaElement.getAnnotatedElement());
			for(XMLElement element:tmp) {
				// messager.printMessage(Kind.ERROR, "element: "+element.getElement().toString());
				element.getSequence().appendChild(schemaElement.getElement());
			}
		}

		// for(XMLElement e:schemaElements) {
		// messager.printMessage(Kind.ERROR, "element: "+serializer.writeToString(e.getElement()));
		// }
		// schemaElements = schemaElements.stream()//
		List<XMLElement> firstRootChildren = schemaElements.stream()//
			.filter(e -> e.getAnnotatedElement().getEnclosingElement().equals(annotatedElement))//
			.sorted(
				(e1, e2) -> Integer.compare(
					annotatedElement.getEnclosedElements().indexOf(e1.getAnnotatedElement()), //
					annotatedElement.getEnclosedElements().indexOf(e2.getAnnotatedElement())))
			.collect(Collectors.toList());
		for(XMLElement schemaElement:firstRootChildren) {

			// schemaElement.setDocument(document); // ważne
			if(annotatedElement != null && document != null) {
				// schemaElement.createXMLElement();
				messager.printMessage(Kind.ERROR, "11" + schemaElement.getElement());
				rootElement.getSequence().appendChild(schemaElement.getElement());
			}
		}

		// // process schema attributes
		for(XMLAttribute schemaAttribute:schemaAttributes) {
			schemaAttribute.setDocument(document);
			XMLElement ele = schemaElements.stream()//
				.filter(e -> {
					String parameterType = e.getAnnotatedElement().asType().toString(); // typ pola oznaczonego @SchemaElement
					if(e.isCollectionSubType()) // jeśli Collection<?> pobierz ?
						parameterType = parameterType.substring(parameterType.indexOf('<') + 1, parameterType.lastIndexOf('>'));
					return parameterType.equals(schemaAttribute.getAnnotatedElement().getEnclosingElement().asType().toString());
				}).findFirst().orElse(null); // wybierz ten, których typy się zgadzają
			if(ele != null) {
				schemaAttribute.setParentElement(ele); // ważne
//				if(firstRootChildren.contains(ele))
//					schemaAttribute.setRootElement(rootElement); // w tym elemencie umieszczone zostaną ograniczenia xs:unique
//				else
					XMLElement root=getRootElement(schemaElements, ele);
					schemaAttribute.setRootElement((root==null)?rootElement:root);
				schemaAttribute.createXMLAttribute();
				ele.getComplexType().appendChild(schemaAttribute.getAttribute());
			}
		}
	}

	private XMLElement getRootElement(List<XMLElement> elements, XMLElement enclosingElement) {
		messager.printMessage(Kind.ERROR, enclosingElement.getAnnotatedElement().asType()+", "+enclosingElement.getAnnotatedElement().getEnclosingElement().asType());
		XMLElement ele = elements.stream()//
			.filter(e -> {
				String parameterType = e.getAnnotatedElement().asType().toString(); // typ pola oznaczonego @SchemaElement
				if(e.isCollectionSubType()) // jeśli Collection<?> pobierz ?
					parameterType = parameterType.substring(parameterType.indexOf('<') + 1, parameterType.lastIndexOf('>'));
				return parameterType.equals(enclosingElement.getAnnotatedElement().getEnclosingElement().asType().toString()); 
			}).findFirst().orElse(null); // wybierz ten, których typy się zgadzają
		if(ele!=null)
			messager.printMessage(Kind.ERROR, "result: "+ele.getAnnotatedElement());
//		messager.printMessage(Kind.ERROR, "root: "+ele.getAnnotatedElement().asType()+" for: "+enclosingElement.getAnnotatedElement().asType());
		return ele;
	}
}
