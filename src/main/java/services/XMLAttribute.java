package services;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annotations.SchemaAttribute;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class XMLAttribute {
	private static final String TWO_RESTRICTION_ERROR = "Field is of enum type, you can't add pattern restriction. There is already enumeration restriction!";

	@Setter
	private Document document;
	@Setter
	private XMLElement parentElement;
	@Setter
	private XMLElement rootElement;

	private Element attribute;
	private javax.lang.model.element.Element annotatedElement;
	private SchemaAttribute schemaAttribute;
	private List<javax.lang.model.element.Element> enumConstants;
	
	public void createXMLAttribute() throws XMLException {
		attribute = document.createElement("xs:attribute");
		attribute.setAttribute("name", ("".equals(schemaAttribute.name())) ? annotatedElement.getSimpleName().toString() : schemaAttribute.name());
		attribute.setAttribute("use", (schemaAttribute.required()) ? "required" : "optional");
		addDocumentationToAttribute(attribute);
		addEnumRestrictionIfEnum(attribute);
		addPatternRestriction(attribute);
		addUniqueConstraintToElement(rootElement.getElement());
	}

	private void addDocumentationToAttribute(Element element) {
		if("".equals(schemaAttribute.documentation()))
			return;
		Element annotation = document.createElement("xs:annotation");
		Element doc = document.createElement("xs:documentation");
		doc.setTextContent(schemaAttribute.documentation());
		annotation.appendChild(doc);
		element.appendChild(annotation);
	}

	private void addEnumRestrictionIfEnum(Element element) {
		if(enumConstants != null) {
			Element simpleType = document.createElement("xs:simpleType");
			Element restriction = document.createElement("xs:restriction");
			restriction.setAttribute("base", "xs:string");
			for(javax.lang.model.element.Element enumContant:enumConstants) {
				Element enumeration = document.createElement("xs:enumeration");
				enumeration.setAttribute("value", enumContant.getSimpleName().toString());
				restriction.appendChild(enumeration);
			}
			simpleType.appendChild(restriction);
			element.appendChild(simpleType);
		}
	}

	private void addPatternRestriction(Element element) throws XMLException {
		if(enumConstants != null && !schemaAttribute.pattern().equals(""))
			throw new XMLException(TWO_RESTRICTION_ERROR, annotatedElement, SchemaAttribute.class);
		if(schemaAttribute.pattern().equals(""))
			return;
		Element simpleType = document.createElement("xs:simpleType");
		Element restriction = document.createElement("xs:restriction");
		restriction.setAttribute("base", "xs:string");
		Element pattern = document.createElement("xs:pattern");
		pattern.setAttribute("value", schemaAttribute.pattern());
		restriction.appendChild(pattern);
		simpleType.appendChild(restriction);
		element.appendChild(simpleType);
	}

	private void addUniqueConstraintToElement(Element element) {
		if(schemaAttribute.unique()) {
			Element unique = document.createElement("xs:unique");
			unique.setAttribute(
				"name",
				"unique" + attribute.getAttribute("name")//
						+ "Across" + annotatedElement.getEnclosingElement().getSimpleName());
			Element selector=document.createElement("xs:selector");
			selector.setAttribute("xpath", parentElement.getElement().getAttribute("name"));
			Element field=document.createElement("xs:field");
			field.setAttribute("xpath", "@"+attribute.getAttribute("name"));
			unique.appendChild(selector);
			unique.appendChild(field);
			element.appendChild(unique);
		}
	}
}
