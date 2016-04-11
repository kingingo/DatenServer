package dev.wolveringer.language;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.google.common.io.Files;

import dev.wolveringer.dataserver.player.LanguageType;

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
