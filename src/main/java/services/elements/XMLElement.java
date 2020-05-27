package services.elements;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annotations.SchemaElement;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class XMLElement {
	@Setter
	private Document document;

	private Element element;
	private javax.lang.model.element.Element annotatedElement;
	private SchemaElement schemaElement;
	private boolean isCollectionSubType;
	private boolean isSequenceNeeded; // włączyć tylko dla głównego elementu

	//pola nie ustawiane przez budowniczego
	private Element sequence;
	private Element complexType;

	public void createXMLElement(){
		element = document.createElement("xs:element");
		Map<String, String> attributes = getAttributesMapForElement();
		attributes.keySet().stream()//
			.forEach(key -> element.setAttribute(key, attributes.get(key)));
		addDocumentationToElement(element);
		addSequenceTemplate(element);
	}

	private void addDocumentationToElement(Element element) {
		if(schemaElement.documentation().length() > 0) {
			Element annotation = document.createElement("xs:annotation");
			Element doc = document.createElement("xs:documentation");
			doc.setTextContent(schemaElement.documentation());
			annotation.appendChild(doc);
			element.appendChild(annotation);
		}
	}

	private Map<String, String> getAttributesMapForElement(){
		Map<String, String> map = new HashMap<>();
		map.put("name", schemaElement.name().length()>0?schemaElement.name():annotatedElement.getSimpleName().toString());
		if(annotatedElement.getKind().isField()) {
			map.put("minOccurs", schemaElement.required() ? "1" : "0");
			map.put("maxOccurs", isCollectionSubType ? "unbounded" : "1");
		}
		return map;
	}

	private void addSequenceTemplate(Element element) {
		complexType = document.createElement("xs:complexType");
		if(isCollectionSubType || isSequenceNeeded) { //było isSequenceNeeded
			sequence = document.createElement("xs:sequence");
			complexType.appendChild(sequence);
		}
		element.appendChild(complexType);
	}
}
