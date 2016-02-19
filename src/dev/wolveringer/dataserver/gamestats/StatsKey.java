package dev.wolveringer.dataserver.gamestats;

import lombok.Getter;

public enum StatsKey {
	INVENTORY("inventory varchar(1000)", "inventory", "Inventory", true, String.class),
	TIME("time varchar(50)", "time", "Time", true, String.class),
	TIME_ELO("time_elo double", "time_elo", "Elo", true, double.class),
	ELO("elo double", "elo", "Elo", true, double.class),
	KIT_ID("kit_id int", "kit_id", "Kit-ID", true, int.class),
	KIT_RANDOM("kit_random varchar(30)", "kit_random", "Kit-Egal", true, String.class),
	KIT("kit varchar(1000)", "kit", "Kit", true, String.class),
	TEAM_MIN("team_min int", "team_min", "TeamMin", true, int.class),
	TEAM_MAX("team_max int", "team_max", "TeamMax", true, int.class),
	WORLD("world varchar(30)", "world", "World", true, String.class),
	LOC_X("x double", "x", "X", true, double.class),
	LOC_Y("y double", "y", "Y", true, double.class),
	LOC_Z("z double", "z", "Z", true, double.class),
	MONEY("money double", "money", "Money", true, double.class),
	FAME_KILLS("fkills int", "fkills", "Fame-Kills", true, int.class),
	FAME_DEATHS("fdeaths int", "fdeaths", "Fame-Tode", true, int.class),
	MONSTER_KILLS("monsterkills int", "monsterkills", "MonsterKills", true, int.class),
	MONSTER_DEATHS("monsterdeaths int", "monsterdeaths", "MonsterDeaths", true, int.class),
	ANIMAL_KILLS("animalkills int", "animalkills", "AnimalKills", true, int.class),
	ANIMAL_DEATHS("animaldeaths int", "animaldeaths", "AnimalDeaths", true, int.class),
	LEVEL("level int", "level", "Level", true, int.class),
	KILLS("kills int", "kills", "Kills", true, int.class),
	DEATHS("deaths int", "deaths", "Tode", true, int.class),
	WIN("win int", "win", "Wins", true, int.class),
	LOSE("lose int", "lose", "Lose", true, int.class),
	RANKING("rank int", "rank", "Rang", false, int.class),
	BEDWARS_ZERSTOERTE_BEDs("beds int", "beds", "Beds", true, int.class),
	SHEEPWARS_KILLED_SHEEPS("sheeps int", "sheeps", "Sheeps", true, int.class),
	POWER("power int", "power", "Power", true, int.class),
	TTT_TESTS("tests int", "tests", "Tests", true, int.class),
	TTT_PAESSE("paesse int", "paesse", "Paesse", true, int.class),
	TTT_KARMA("karma int", "karma", "Karma", true, int.class),
	TTT_DETECTIVE_PUNKTE("detectivepunkte int", "detectivepunkte", "Detective-Punkte", true, int.class),
	TTT_TRAITOR_PUNKTE("traitorpunkte int", "traitorpunkte", "Traitor-Punkte", true, int.class);

	@Getter
	private String mySQLSyntax;
	@Getter
	private String mySQLName;
	@Getter
	private String contraction;
	@Getter
	private boolean mysql;
	@Getter
	private Class<?> type;

	private StatsKey(String mySQLSyntax, String TYP, String contraction, boolean mysql, Class<?> type) {
		this.mySQLSyntax = mySQLSyntax;
		this.mySQLName = TYP;
		this.contraction = contraction;
		this.mysql = mysql;
		this.type = type;
	}

	public static StatsKey get(String s) {
		for (StatsKey stats : values()) {
			if (stats.getMySQLName().equalsIgnoreCase(s))
				return stats;
		}
		return null;
	}

}