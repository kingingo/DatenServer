package dev.wolveringer.dataserver.gamestats;

import lombok.Getter;

public enum Game {
	GUNGAME(true, "GunGame-Server", "GunGame", ServerType.GUNGAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.LEVEL }),
	SurvivalGames1vs1(false, "SurvivalGames1vs1", "SG1vs1", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE }),
	BedWars1vs1(false, "BedWars1vs1", "BW1vs1", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.BEDWARS_ZERSTOERTE_BEDs }),
	SkyWars1vs1(false, "SkyWars1vs1", "SkyWars1vs1", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.LOSE, StatsKey.WIN }),
	Versus(false, "VERSUS", "VS", ServerType.GAME, new StatsKey[] { StatsKey.KIT_RANDOM, StatsKey.KIT_ID, StatsKey.TEAM_MAX, StatsKey.TEAM_MIN, StatsKey.ELO, StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE }),
	SkyWars(true, "SkyWars", "SkyWars", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.LOSE, StatsKey.WIN }),
	QuickSurvivalGames(true, "QuickSurvivalGames", "QSG", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE }),
	SurvivalGames(true, "SurvivalGames", "SG", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE }),
	OneInTheChamber(true, "OneInTheChamber", "OITC", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE }),
	SkyPvP(true, "SkyPvP", "SK", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE }),
	Falldown(true, "Falldown", "FD", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.POWER, StatsKey.WIN, StatsKey.LOSE }),
	TroubleInMinecraft(true, "TroubleInMinecraft", "TTT", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.TTT_KARMA, StatsKey.TTT_PAESSE, StatsKey.TTT_TESTS, StatsKey.TTT_TRAITOR_PUNKTE, StatsKey.TTT_DETECTIVE_PUNKTE }),
	DeathGames(true, "DeathGames", "DG", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE }),
	BedWars(true, "BedWars", "BW", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.BEDWARS_ZERSTOERTE_BEDs }),
	SheepWars(true, "SheepWars", "SW", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.SHEEPWARS_KILLED_SHEEPS }),
	PVP(true, "PvP-Server", "PvP", ServerType.PVP, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.MONEY, StatsKey.ELO, StatsKey.TIME_ELO, StatsKey.TIME }),
	SKYBLOCK(true, "SkyBlock", "Sky", ServerType.SKYBLOCK, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.MONEY }),
	WARZ(true, "WarZ-Server", "WarZ", ServerType.WARZ, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.ANIMAL_KILLS, StatsKey.ANIMAL_DEATHS, StatsKey.MONSTER_KILLS, StatsKey.MONSTER_DEATHS }),
	CaveWars(true, "CaveWars", "CW", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.SHEEPWARS_KILLED_SHEEPS }),
	Masterbuilders(true, "Master Builders", "MB", ServerType.GAME, new StatsKey[] { StatsKey.LOSE, StatsKey.WIN }),
	NONE(true, "NONE", "FAIL", ServerType.GAME, null);

	@Getter
	private String typ;
	@Getter
	private String Kuerzel;
	@Getter
	private StatsKey[] stats;
	@Getter
	private ServerType serverType;
	@Getter
	private boolean solo = true;

	private Game(boolean solo, String Typ, String Kuerzel, ServerType serverType, StatsKey[] stats) {
		this.typ = Typ;
		this.solo = solo;
		this.stats = stats;
		this.Kuerzel = Kuerzel;
		this.serverType = serverType;
	}

	public static Game get(String g) {
		g = g.replaceAll("-", "");
		g = g.replaceAll(" ", "");
		for (Game t : Game.values()) {
			if (t.getTyp().replaceAll(" ", "").replaceAll("-", "").equalsIgnoreCase(g))
				return t;
		}

		for (Game t : Game.values()) {
			if (t.getKuerzel().replaceAll(" ", "").replaceAll("-", "").equalsIgnoreCase(g))
				return t;
		}
		return null;
	}

}