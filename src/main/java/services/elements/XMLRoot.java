package services.elements;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annotations.GenerateSchema;
import lombok.Getter;

@Getter
public class XMLRoot {
	private final Document document;
	private final javax.lang.model.element.Element annotatedElement;
	private final GenerateSchema generateSchema;
	private String fileName;

	public XMLRoot(Document document, javax.lang.model.element.Element annotatedElement, GenerateSchema generateSchema) {
		this.document = document;
		this.annotatedElement = annotatedElement;
		this.generateSchema = generateSchema;
		fileName = annotatedElement.getSimpleName().toString();
	}

	public void createXML(List<XMLElement> schemaElements, List<XMLAttribute> schemaAttributes) throws XMLException {
		//create xs:schema element with namespace
		Element body = document.createElement("xs:schema");
		body.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		document.appendChild(body);

		XMLElement rootElement = createRootTagOfXSDFile(body);//create root xs:element
		processXMLElements(schemaElements, rootElement);
		processXMLAttributes(schemaAttributes, schemaElements, rootElement);
	}

	private XMLElement createRootTagOfXSDFile(Element body) {
		XMLElement rootElement = XMLElement.builder()//
			.document(document)//
			.schemaElement(generateSchema.rootElement())//
			.annotatedElement(annotatedElement)//
			.isSequenceNeeded(true)//
			.build();

		rootElement.createXMLElement();
		body.appendChild(rootElement.getElement());
		return rootElement;
	}

	//======================= XML Element ===========================
	private void processXMLElements(List<XMLElement> schemaElements, XMLElement rootElement) {
		// set document and create xs:element bodies
		for(XMLElement e:schemaElements) {
			e.setDocument(document);
			e.createXMLElement();
		}

		//find all parent elements of every element and add child element body to parent
		for(XMLElement schemaElement:schemaElements) {
			List<XMLElement> parentElements = schemaElements.stream()//
				.filter(e -> isElementInner(e, schemaElement))//
				.collect(Collectors.toList());
			for(XMLElement pe:parentElements)
				pe.getSequence().appendChild(schemaElement.getElement());
		}

		schemaElements.stream()//
			.filter(e -> e.getAnnotatedElement().getEnclosingElement().equals(annotatedElement))//child element to @GenerateSchema element
			.sorted((e1, e2) -> Integer.compare(annotatedElement.getEnclosedElements().indexOf(e1.getAnnotatedElement()), //
				annotatedElement.getEnclosedElements().indexOf(e2.getAnnotatedElement())))//so that order like in class body
			.forEach(se -> {
				if(annotatedElement != null && document != null)
					rootElement.getSequence().appendChild(se.getElement());
			});
	}
	//===============================================================

	//===================== XML Attribute ===========================
	private void processXMLAttributes(List<XMLAttribute> schemaAttributes, List<XMLElement> schemaElements, XMLElement rootElement) throws XMLException {
		for(XMLAttribute schemaAttribute:schemaAttributes) { //for every XMLAttribute
			schemaAttribute.setDocument(document);

			//find parent XMLElement
			XMLElement parentXMLElement = null;
			for(XMLElement se:schemaElements) {
				if(isAttributeInner(se, schemaAttribute)) {
					parentXMLElement = se;
					break;
				}
			}

			//if parent exists, find one-level up parent (for unique constraint use) XMLElement, create XSD attribute body
			if(parentXMLElement != null) {
				schemaAttribute.setParentElement(parentXMLElement); // ważne
				XMLElement root = getParentElement(schemaElements, parentXMLElement);
				schemaAttribute.setRootElement((root == null) ? rootElement : root);
				schemaAttribute.createXMLAttribute();
				parentXMLElement.getComplexType().appendChild(schemaAttribute.getAttribute());
			}
		}
	}

	private XMLElement getParentElement(List<XMLElement> schemaElements, XMLElement childElement) {
		XMLElement parentElement = null;
		for(XMLElement se:schemaElements) {
			if(isElementInner(se, childElement)) {
				parentElement = se;
				break;
			}
		}
		return parentElement;
	}
	//===============================================================

	/**
	 * This method check whether outer element annotated with @SchemaElement contains more element annotated with @SchemaElement. Example: In class A we have @SchemaElement private B field1; and in
	 * class B we have another @SchemaElement private int field2; Then this method returns true;
	 */
	private boolean isElementInner(XMLElement parentElement, XMLElement element) {
		String type = parentElement.getAnnotatedElement().asType().toString();
		if(parentElement.isCollectionSubType()) // jeśli Collection<?> pobierz ?
			type = type.substring(type.indexOf('<') + 1, type.lastIndexOf('>'));
		return type.equals(element.getAnnotatedElement().getEnclosingElement().asType().toString());
	}

	/**
	 * This method behaves same way like {@link XMLRoot#isElementInner(XMLElement, XMLElement)}, but for XMLAttribute class.
	 */
	private boolean isAttributeInner(XMLElement parentElement, XMLAttribute attribute) {
		String type = parentElement.getAnnotatedElement().asType().toString();
		if(parentElement.isCollectionSubType()) // jeśli Collection<?> pobierz ?
			type = type.substring(type.indexOf('<') + 1, type.lastIndexOf('>'));
		return type.equals(attribute.getAnnotatedElement().getEnclosingElement().asType().toString());
	}
}
