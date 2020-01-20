package services;

import java.io.File;
import java.nio.file.Files;
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

@Getter
public class XMLService {
	private String projectDirPath;

	private List<XMLRoot> schemaRoots;
	private List<XMLElement> schemaElements;
	private List<XMLAttribute> schemaAttributes;

	public XMLService(String projectDirPath) {
		this.projectDirPath = projectDirPath;
		schemaRoots = new ArrayList<>();
		schemaElements = new ArrayList<>();
		schemaAttributes = new ArrayList<>();
	}

	public void addRoot(XMLRoot xmlRoot) {
		//ewentualne ostrzeżenie dodać o tym, że istniej już taka adnoacja GenerateSchema z taką ścieżką
		if(schemaRoots.stream().filter(sRoot->sRoot.getAnnotatedElement().getSimpleName().equals(xmlRoot.getAnnotatedElement().getSimpleName())).count()>0)
			return; //potrzebne aby nie dublowały się obiekty
		schemaRoots.add(xmlRoot);
	}

	//@formatter:off
	public void addSchemaElement(XMLElement xmlElement) {schemaElements.add(xmlElement);}
	public void addSchemaAttribute(XMLAttribute xmlAttribute) {schemaAttributes.add(xmlAttribute);}
	//@formatter:on

	public void createSchemaFile() throws XMLException {
		for(XMLRoot xmlRoot:schemaRoots) {
			// appendAllElements();
			xmlRoot.createXML(schemaElements, schemaAttributes);
			String schemaFilePath = projectDirPath + "/" + xmlRoot.getResourceFolderPath() + "/" + xmlRoot.getFileName() + ".xsd";
			createDirectoryIfNotExists(projectDirPath + "/" + xmlRoot.getResourceFolderPath());
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
		if(!Files.exists(file.getAbsoluteFile().toPath()))
			Files.createFile(file.getAbsoluteFile().toPath());
	}

	@SneakyThrows
	private void createDirectoryIfNotExists(String dirPath) {
		File file = new File(dirPath);
		if(!Files.exists(file.getAbsoluteFile().toPath()))
			Files.createDirectory(file.getAbsoluteFile().toPath());
	}
}
