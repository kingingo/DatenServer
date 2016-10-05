package eu.epicpvp.language;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import lombok.Getter;

public class LanguageFile {
	private static final DocumentBuilder xmlBuilder;
	private static final Transformer xmlTransformer;

	static {
		DocumentBuilder builder = null;
		Transformer transformer = null;

		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		xmlBuilder = builder;

		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		xmlTransformer = transformer;
	}

	@Getter
	private File file;
	@Getter
	private LanguageType language;
	@Getter
	private boolean exsist;
	@Getter
	private Document xmlFile;
	@Getter
	private Double version;

	public LanguageFile(LanguageType language) {
		this.language = language;
		this.file = new File(LanguageManager.languageDir, language.getShortName() + "/EpicPvPMC Text.xml");
		this.exsist = file.exists();
		if (!exsist)
			System.err.println("Cant find translation for " + language + " (" + file + ")");
		if (exsist) {
			try {
				xmlFile = xmlBuilder.parse(file);
				String version = null;
				if (xmlFile.getDocumentElement().getElementsByTagName("version").getLength() != 0)
					version = xmlFile.getDocumentElement().getElementsByTagName("version").item(0).getTextContent();
				if (version == null || version.equalsIgnoreCase(""))
					System.out.println("Missing version for: " + language);
				if(version == null)
					version = "unknown";
				try {
					this.version = Double.parseDouble(version);
				} catch (Exception e) {
					this.version = 1D;
					System.out.println("Cant paradise version for: " + language + " (" + version + "). Using version 1D");
					Element nversion = xmlFile.createElement("version");
					xmlFile.getDocumentElement().appendChild(nversion);
					nversion.setTextContent("1D");
					xmlFile.getDocumentElement().getElementsByTagName("version").item(0).setTextContent("1D");
					saveDocument();
				}
			} catch (SAXException | IOException e) {
				e.printStackTrace();
			}
		}
		if(version == null)
			version = 1D;
	}

	public void saveDocument(){
		try {
			FileOutputStream os = new FileOutputStream(file);
			xmlTransformer.transform(new DOMSource(xmlFile), new StreamResult(os));
			os.flush();
			os.close();
		} catch (TransformerException | IOException e) {
			e.printStackTrace();
		}
	}

	public String getFileAsString() {
		try {
			return FileUtils.readFileToString(file,Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
