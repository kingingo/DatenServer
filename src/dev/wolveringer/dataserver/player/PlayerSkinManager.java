package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.dataserver.skin.OperationCallback;
import dev.wolveringer.dataserver.skin.SkinCash;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.skin.Skin;
import dev.wolveringer.skin.SteveSkin;

public class PlayerSkinManager {
	private static final long SKIN_CASH_TIME = TimeUnit.HOURS.toMillis(12); //12H
	private static final int SKIN_GET_TIMEOUT = 1500; //12H
	
	public static void init(){
		MySQL.getInstance().commandSync("CREATE TABLE IF NOT EXISTS `PlayerSkins`(`playerId` INT, `skinName` VARCHAR (1000), `skinUUID` VARCHAR (1000), `skinValue` VARCHAR (1000), `signature` VARCHAR (1000),`timestamp` INT);");
	}
	
	private OnlinePlayer player;
	private Skin skin; //textures

	public PlayerSkinManager(OnlinePlayer player) {
		this.player = player;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		ArrayList<String[]> out = MySQL.getInstance().querySync("SELECT `skinName`,`skinUUID`,`skinValue`,`signature`,`timestamp` FROM `PlayerSkins` WHERE `playerId`='"+player.getPlayerId()+"'",1);
		if(out.size() == 0){
			MySQL.getInstance().commandSync("INSERT INTO `PlayerSkins`(`playerId`, `skinName`, `skinUUID`, `skinValue`, `signature`, `timestamp`) VALUES ('"+player.getPlayerId()+"','nan','undefined','undefined','undefined','-1')");
			out.add(new String[]{"nan","undefined","undefined","undefined","-1"});
		}
		String skinName = out.get(0)[0];
		UUID skinUUID = null;
		if(!out.get(0)[1].equalsIgnoreCase("undefined"))
			skinUUID = UUID.fromString(out.get(0)[1]);
		boolean skinCached = !out.get(0)[2].equalsIgnoreCase("undefined") && Long.parseLong(out.get(0)[4])-System.currentTimeMillis() >= SKIN_CASH_TIME;
		if(!skinCached && (!skinName.equalsIgnoreCase("nan") || skinUUID != null)){ //If nan than no skin set
			if(skinUUID != null){
				SkinCash.getSkin(skinUUID, new OperationCallback<Skin>() {
					@Override
					public void done(Skin obj) {
						PlayerSkinManager.this.skin = skin;
					}
				});
			}
			else
			{
				SkinCash.getSkin(skinName, new OperationCallback<Skin>() {
					@Override
					public void done(Skin obj) {
						PlayerSkinManager.this.skin = skin;
					}
				});
			}
		}
		if(!out.get(0)[2].equalsIgnoreCase("undefined") && !out.get(0)[3].equalsIgnoreCase("undefined"))
		{
			skin = new Skin(out.get(0)[2],out.get(0)[3]);
		}
		if(skin == null)
			if(player.isPremiumPlayer())
				skin = SkinCash.getSkin(player.getName());
			else
				skin = new SteveSkin();
	}
	
	@SuppressWarnings("unchecked")
	public void disableSkin(){
		skin = new SteveSkin();
		MySQL.getInstance().command("UPDATE `PlayerSkins` SET `skinName`='nan',`skinUUID`='undefined',`skinValue`='undefined', `signature`='undefined',`timestamp`='-1' WHERE playerId='"+player.getPlayerId()+"'");
	}
	
	@SuppressWarnings("unchecked")
	public void setSkin(String name){
		SkinCash.getSkin(name, new OperationCallback<Skin>() {
			@Override
			public void done(Skin obj) {
				setSkin(obj);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void setSkin(UUID uuid){
		SkinCash.getSkin(uuid, new OperationCallback<Skin>() {
			@Override
			public void done(Skin obj) {
				setSkin(obj);
			}
		});
	}
	
	public void setSkin(String raw,String signature){
		setSkin(new Skin(raw,signature));
	}
	
	@SuppressWarnings("unchecked")
	public void setSkin(Skin skin){
		PlayerSkinManager.this.skin = skin;
		MySQL.getInstance().command("UPDATE `PlayerSkins` SET `skinName`='"+(skin.hasProfileName() ? skin.getProfileName() : "nan")+"',`skinUUID`='"+(skin.hasUUID() ? skin.getUUID() : "undefined")+"',`skinValue`='"+(skin.getRawData() != null ? skin.getRawData() : "undefined")+"', `signature`='"+(skin.getSignature())+"',`timestamp`='"+System.currentTimeMillis()+"' WHERE playerId='"+player.getPlayerId()+"'");
	}
	
	public Skin getSkin() {
		return getSkin(SKIN_GET_TIMEOUT);
	}
	public Skin getSkin(int timeout) {
		int loopCount = 0;
		int waitTime = 50;
		while (skin == null && loopCount*waitTime<timeout) {
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
			}
		}
		return skin;
	}
}
