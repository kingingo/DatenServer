package eu.epicpvp.dataserver.player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.epicpvp.dataserver.skin.OperationCallback;
import eu.epicpvp.dataserver.skin.SkinCache;
import eu.epicpvp.datenserver.definitions.skin.Skin;
import eu.epicpvp.datenserver.definitions.skin.SteveSkin;
import eu.epicpvp.mysql.MySQL;

public class PlayerSkinManager {
	private static final long SKIN_CASH_TIME = TimeUnit.HOURS.toMillis(12L);
	private static final int SKIN_GET_TIMEOUT = 1500;
	private OnlinePlayer player;
	private Skin skin;

	public static void init() {
		MySQL.getInstance().commandSync("CREATE TABLE IF NOT EXISTS `PlayerSkins`(`playerId` INT, `skinName` VARCHAR (1000), `skinUUID` VARCHAR (1000), `skinValue` VARCHAR (1000), `signature` VARCHAR (1000),`timestamp` INT);");
	}

	public PlayerSkinManager(OnlinePlayer player) {
		this.player = player;
	}

	public void load() {
		new Thread() {
			public void run() {
				ArrayList<String[]> out = MySQL.getInstance().querySync("SELECT `skinName`,`skinUUID`,`skinValue`,`signature`,`timestamp` FROM `PlayerSkins` WHERE `playerId`='" + PlayerSkinManager.this.player.getPlayerId() + "'", 1);
				if (out.size() == 0) {
					MySQL.getInstance().commandSync("INSERT INTO `PlayerSkins`(`playerId`, `skinName`, `skinUUID`, `skinValue`, `signature`, `timestamp`) VALUES ('" + PlayerSkinManager.this.player.getPlayerId() + "','nan','undefined','undefined','undefined','-1')");
					out.add(new String[] { "nan", "undefined", "undefined", "undefined", "-1" });
				}
				String skinName = ((String[]) out.get(0))[0];
				UUID skinUUID = null;
				if (!((String[]) out.get(0))[1].equalsIgnoreCase("undefined")) {
					skinUUID = UUID.fromString(((String[]) out.get(0))[1]);
				}
				boolean skinCached = (!((String[]) out.get(0))[2].equalsIgnoreCase("undefined")) && (Long.parseLong(((String[]) out.get(0))[4]) - System.currentTimeMillis() >= PlayerSkinManager.SKIN_CASH_TIME);
				if ((!skinCached) && ((!skinName.equalsIgnoreCase("nan")) || (skinUUID != null))) {
					if (skinUUID != null) {
						SkinCache.getSkin(skinUUID, new OperationCallback[] { new OperationCallback<Skin>() {
							public void done(Skin obj) {
								PlayerSkinManager.this.skin = PlayerSkinManager.this.skin;
							}
						} });
					} else {
						SkinCache.getSkin(skinName, new OperationCallback[] { new OperationCallback<Skin>() {
							public void done(Skin obj) {
								PlayerSkinManager.this.skin = PlayerSkinManager.this.skin;
							}
						} });
					}
				}
				if ((!((String[]) out.get(0))[2].equalsIgnoreCase("undefined")) && (!((String[]) out.get(0))[3].equalsIgnoreCase("undefined"))) {
					PlayerSkinManager.this.skin = new Skin(((String[]) out.get(0))[2], ((String[]) out.get(0))[3]);
				}
				if (PlayerSkinManager.this.skin == null) {
					try {
						if (PlayerSkinManager.this.player.isPremiumPlayer()) {
							PlayerSkinManager.this.skin = SkinCache.getSkin(PlayerSkinManager.this.player.getName());
						} else {
							PlayerSkinManager.this.skin = new SteveSkin();
						}
					} catch (Exception e) {
						PlayerSkinManager.this.skin = new SteveSkin();
					}
				}
			}
		}.start();
	}

	public void disableSkin() {
		this.skin = new SteveSkin();
		MySQL.getInstance().command("UPDATE `PlayerSkins` SET `skinName`='nan',`skinUUID`='undefined',`skinValue`='undefined', `signature`='undefined',`timestamp`='-1' WHERE playerId='" + this.player.getPlayerId() + "'", new MySQL.Callback[0]);
	}

	public void setSkin(String name) {
		SkinCache.getSkin(name, new OperationCallback[] { new OperationCallback<Skin>() {
			public void done(Skin obj) {
				PlayerSkinManager.this.setSkin(obj);
			}
		} });
	}

	public void setSkin(UUID uuid) {
		SkinCache.getSkin(uuid, new OperationCallback[] { new OperationCallback<Skin>() {
			public void done(Skin obj) {
				PlayerSkinManager.this.setSkin(obj);
			}
		} });
	}

	public void setSkin(String raw, String signature) {
		setSkin(new Skin(raw, signature));
	}

	public void setSkin(Skin skin) {
		this.skin = skin;
		MySQL.getInstance().command("UPDATE `PlayerSkins` SET `skinName`='" + (skin.hasProfileName() ? skin.getProfileName() : "nan") + "',`skinUUID`='" + (skin.hasUUID() ? skin.getUUID() : "undefined") + "',`skinValue`='" + (skin.getRawData() != null ? skin.getRawData() : "undefined") + "', `signature`='" + skin.getSignature() + "',`timestamp`='" + System.currentTimeMillis() + "' WHERE playerId='" + this.player.getPlayerId() + "'", new MySQL.Callback[0]);
	}

	public Skin getSkin() {
		return getSkin(1500);
	}

	public Skin getSkin(int timeout) {
		int loopCount = 0;
		int waitTime = 50;
		while ((this.skin == null) && (loopCount * waitTime < timeout)) {
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException localInterruptedException) {
			}
		}
		return this.skin;
	}
}
