package eu.epicpvp.language;

import java.io.File;
import java.util.HashMap;

import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;

public class LanguageManager {
	public static final File languageDir = new File("languages/");
	private static HashMap<LanguageType, LanguageFile> languages = new HashMap<>();

	public static void init(){
		languageDir.mkdirs();
		languages.clear();
		for(LanguageType t : LanguageType.values())
			languages.put(t, new LanguageFile(t));
	}

	public static LanguageFile getLanguage(LanguageType type){
		if(!languages.containsKey(type)){
			languages.put(type, new LanguageFile(type));
		}
		return languages.get(type);
	}
}
