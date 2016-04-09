package dev.wolveringer.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import dev.wolveringer.dataserver.player.LanguageType;
import lombok.Getter;

public class LanguageFile {
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
		if(exsist){
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				xmlFile = builder.parse(file);
				String version = xmlFile.getElementById("version").getNodeValue();
				if(version == null || version.equalsIgnoreCase(""))
					System.out.println("Missing version for: "+language);
				version = "1.0";
				try{
					this.version = Double.parseDouble(version);
				}catch(Exception e){
					this.version = 1D;
					System.out.println("Cant paradise version for: "+language+" ("+version+")");
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getFileAsString() {
		try {
			return FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
