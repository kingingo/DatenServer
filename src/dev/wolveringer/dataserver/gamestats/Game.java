package dev.wolveringer.dataserver.gamestats;

import dev.wolveringer.serverbalancer.AcardeManager;
import lombok.Getter;

public enum Game {
	GUNGAME(true, "GunGame-Server", "GunGame", ServerType.GUNGAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.LEVEL },true),
	SurvivalGames1vs1(false, "SurvivalGames1vs1", "SG1vs1", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE },true),
	BedWars1vs1(false, "BedWars1vs1", "BW1vs1", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.BEDWARS_ZERSTOERTE_BEDs },true),
	SkyWars1vs1(false, "SkyWars1vs1", "SkyWars1vs1", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.LOSE, StatsKey.WIN },true),
	Versus(false, "VERSUS", "VS", ServerType.GAME, new StatsKey[] { StatsKey.KIT_RANDOM, StatsKey.KIT_ID, StatsKey.TEAM_MAX, StatsKey.TEAM_MIN, StatsKey.ELO, StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE },true),
	SkyWars(true, "SkyWars", "SkyWars", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.LOSE, StatsKey.WIN },true),
	QuickSurvivalGames(true, "QuickSurvivalGames", "QSG", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE },true),
	SurvivalGames(true, "SurvivalGames", "SG", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE },true),
	OneInTheChamber(true, "OneInTheChamber", "OITC", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE },true),
	SkyPvP(true, "SkyPvP", "SK", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE },true),
	Falldown(true, "Falldown", "FD", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.POWER, StatsKey.WIN, StatsKey.LOSE },true),
	TroubleInMinecraft(true, "TroubleInMinecraft", "TTT", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.TTT_KARMA, StatsKey.TTT_PAESSE, StatsKey.TTT_TESTS, StatsKey.TTT_TRAITOR_PUNKTE, StatsKey.TTT_DETECTIVE_PUNKTE },true),
	DeathGames(true, "DeathGames", "DG", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE },true),
	BedWars(true, "BedWars", "BW", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.BEDWARS_ZERSTOERTE_BEDs },true),
	SheepWars(true, "SheepWars", "SW", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.SHEEPWARS_KILLED_SHEEPS },true),
	PVP(true, "PvP-Server", "PvP", ServerType.PVP, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.MONEY, StatsKey.ELO, StatsKey.TIME_ELO, StatsKey.TIME },true),
	SKYBLOCK(true, "SkyBlock", "Sky", ServerType.SKYBLOCK, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.MONEY },true),
	WARZ(true, "WarZ-Server", "WarZ", ServerType.WARZ, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.ANIMAL_KILLS, StatsKey.ANIMAL_DEATHS, StatsKey.MONSTER_KILLS, StatsKey.MONSTER_DEATHS },true),
	CaveWars(true, "CaveWars", "CW", ServerType.GAME, new StatsKey[] { StatsKey.KILLS, StatsKey.DEATHS, StatsKey.WIN, StatsKey.LOSE, StatsKey.SHEEPWARS_KILLED_SHEEPS },true),
	Masterbuilders(true, "Master Builders", "MB", ServerType.GAME, new StatsKey[] { StatsKey.LOSE, StatsKey.WIN},true),
	Money(true, "Money", "MONEY", ServerType.BUNGEECORD, new StatsKey[] { StatsKey.COINS, StatsKey.GEMS },false),
	NONE(true, "NONE", "FAIL", ServerType.GAME, null,false);

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
	@Getter
	private boolean arcade;
	private Game(boolean solo, String Typ, String Kuerzel, ServerType serverType, StatsKey[] stats,boolean arcadeGame) {
		this.typ = Typ;
		this.solo = solo;
		this.stats = stats;
		this.Kuerzel = Kuerzel;
		this.serverType = serverType;
		this.arcade = arcadeGame;
	}

	public static GameType get(String g) {
		g = g.replaceAll("-", "");
		g = g.replaceAll(" ", "");
		for (GameType t : GameType.values()) {
			if (t.getTyp().replaceAll(" ", "").replaceAll("-", "").equalsIgnoreCase(g))
				return t;
		}

		for (GameType t : GameType.values()) {
			if (t.getKuerzel().replaceAll(" ", "").replaceAll("-", "").equalsIgnoreCase(g))
				return t;
		}
		return null;
	}
	
	public boolean isMySQL(){
		return stats != null;
	}

}