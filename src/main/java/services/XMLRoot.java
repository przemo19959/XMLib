package services;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annotations.GenerateSchema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class XMLRoot {
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

		XMLElement rootElement = XMLElement.builder()//
			.document(document)//
			.schemaElement(generateSchema.rootElement())//
			.annotatedElement(annotatedElement)//
			.isSequenceNeeded(true)//
			.build();

		rootElement.createXMLElement();
		body.appendChild(rootElement.getElement());

		// process schema elements
		schemaElements = schemaElements.stream()//
			.filter(e -> e.getAnnotatedElement().getEnclosingElement().equals(annotatedElement))//
			.sorted(
				(e1, e2) -> Integer.compare(
					annotatedElement.getEnclosedElements().indexOf(e1.getAnnotatedElement()), //
					annotatedElement.getEnclosedElements().indexOf(e2.getAnnotatedElement())))
			.collect(Collectors.toList());
		for(XMLElement schemaElement:schemaElements) {
			schemaElement.setDocument(document); // ważne
			if(annotatedElement != null && document != null) {
				schemaElement.createXMLElement();
				rootElement.getSequence().appendChild(schemaElement.getElement());
			}
		}

		// process schema attributes
		for(XMLAttribute schemaAttribute:schemaAttributes) {
			schemaAttribute.setDocument(document);
			XMLElement ele = schemaElements.stream()//
				.filter(e -> {
					String parameterType = e.getAnnotatedElement().asType().toString(); //typ pola oznaczonego @SchemaElement
					if(e.isCollectionSubType()) //jeśli Collection<?>  pobierz ?
						parameterType = parameterType.substring(parameterType.indexOf('<') + 1, parameterType.lastIndexOf('>'));
					return parameterType.equals(schemaAttribute.getAnnotatedElement().getEnclosingElement().asType().toString());
				}).findFirst().orElse(null); //wybierz ten, których typy się zgadzają
			if(ele != null) {
				schemaAttribute.setRootElement(rootElement);
				schemaAttribute.setParentElement(ele); //ważne
				schemaAttribute.createXMLAttribute();
				ele.getComplexType().appendChild(schemaAttribute.getAttribute());
			}
		}
	}
}
