package pl.dabrowski.XMLib.services;

import java.io.File;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import lombok.Getter;
import lombok.SneakyThrows;
import pl.dabrowski.XMLib.services.elements.XMLAttribute;
import pl.dabrowski.XMLib.services.elements.XMLElement;
import pl.dabrowski.XMLib.services.elements.XMLException;
import pl.dabrowski.XMLib.services.elements.XMLRoot;

@Getter
public class XMLService {
	private static final String PATH_PATTERN = "{0}/{1}";
	private static final String SCHEMA_PATTERN = "{0}/{1}.xsd";
	
	private List<XMLRoot> schemaRoots;
	private List<XMLElement> schemaElements;
	private List<XMLAttribute> schemaAttributes;

	public XMLService() {
		schemaRoots = new ArrayList<>();
		schemaElements = new ArrayList<>();
		schemaAttributes = new ArrayList<>();
	}

	public void addRoot(XMLRoot xmlRoot) {
		if(schemaRoots.stream()//
			.filter(sr -> sr.getAnnotatedElement().getSimpleName().equals(xmlRoot.getAnnotatedElement().getSimpleName()))//
			.count() == 0) {
			schemaRoots.add(xmlRoot);
		}
	}

	//@formatter:off
	public void addSchemaElement(XMLElement xmlElement) {schemaElements.add(xmlElement);}
	public void addSchemaAttribute(XMLAttribute xmlAttribute) {schemaAttributes.add(xmlAttribute);}
	//@formatter:on

	public void createSchemaFile(String projectDirPath) throws XMLException {
		for(XMLRoot xmlRoot:schemaRoots) {
			// appendAllElements();
			xmlRoot.createXML(schemaElements, schemaAttributes);
			
			String tmp=MessageFormat.format(PATH_PATTERN, projectDirPath, xmlRoot.getGenerateSchema().path());
			createDirectoryIfNotExists(tmp);
			
			String schemaFilePath = MessageFormat.format(SCHEMA_PATTERN, tmp, xmlRoot.getFileName());
			createFileIfNotExists(schemaFilePath);
			writeDocumentToFile(schemaFilePath, xmlRoot.getDocument());
		}
	}

	@SneakyThrows
	private void writeDocumentToFile(String schemaFilePath, Document document) {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result output = new StreamResult(new File(schemaFilePath));
		Source input = new DOMSource(document);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(input, output);
	}

	@SneakyThrows
	private void createFileIfNotExists(String filePath) {
		File file = new File(filePath);
		if(Files.exists(file.getAbsoluteFile().toPath())==false)
			Files.createFile(file.getAbsoluteFile().toPath());
	}

	@SneakyThrows
	private void createDirectoryIfNotExists(String dirPath) {
		File file = new File(dirPath);
		if(Files.exists(file.getAbsoluteFile().toPath())==false)
			Files.createDirectory(file.getAbsoluteFile().toPath());
	}
}
