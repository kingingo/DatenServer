package dev.wolveringer.dataserver.uuid;

import java.util.UUID;

import org.apache.commons.codec.Charsets;

import dev.wolveringer.dataserver.skin.UUIDFetcher;

public class UUIDManager {
	public static UUID getOfflineUUID(String player) {
		return UUID.nameUUIDFromBytes(new StringBuilder().append("OfflinePlayer:").append(player.toLowerCase()).toString().getBytes(Charsets.UTF_8));
	}
	
	public static UUID getOnlineUUID(String player) throws Exception{
		return UUIDFetcher.getUUIDOf(player);
	}
}
