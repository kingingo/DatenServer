package dev.wolveringer.dataserver.connection;

import lombok.Getter;

public enum LanguageType {
ENGLISH("ENGLISH"),
GERMAN("GERMAN");

@Getter
private String def;
private LanguageType(String def){
	this.def=def;
}

public static LanguageType get(String def){
	for(LanguageType type : LanguageType.values())if(type.getDef().equalsIgnoreCase(def))return type;
	return LanguageType.GERMAN;
}

}
