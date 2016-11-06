package eu.epicpvp.dataserver.skin;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import org.json.JSONObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import eu.epicpvp.datenserver.definitions.skin.Skin;
import eu.epicpvp.datenserver.definitions.skin.SteveSkin;

public class SkinCache {

	private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

	private static Cache<UUID, Skin> profileCache = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(4, TimeUnit.HOURS).build(new CacheLoader<UUID, Skin>() {
		@Override
		public Skin load(@NonNull UUID uuid) throws Exception {
			Skin out = null;
			try{
				out = loadSkin(uuid);
			}catch(Exception e){
				e.printStackTrace();
				out = new SteveSkin();
			}
			return out;
		};
	});

	@Deprecated
	public static Skin getSkin(UUID uuid) {
		if(uuid == null)
			throw new IllegalArgumentException("UUID cant be null");
		try{
			return profileCache.get(uuid);
		}catch (Exception e){
			System.out.println("Cant loading Skin for " + uuid + " (Reason: " + e.getMessage() + ")");
			return new SteveSkin();
		}
	}

	@SuppressWarnings("unchecked")
	public static void getSkin(final UUID uuid, final OperationCallback<Skin>... c) {
		SkinEventLoop.ThreadLoop.join(new Runnable() {
			@SuppressWarnings({ "rawtypes" })
			@Override
			public void run() {
				Skin s = getSkin(uuid);
				for(OperationCallback t : c)
					t.done(s);
			}
		});
	}

	@Deprecated
	public static Skin getSkin(String name) {
		try{
			return getSkin(UUIDFetcher.getUUIDOf(name));
		}catch (Exception e){
			e.printStackTrace();
		}
		return new SteveSkin();
	}

	@SuppressWarnings("unchecked")
	public static void getSkin(final String name, final OperationCallback<Skin>... c) {
		SkinEventLoop.ThreadLoop.join(new Runnable() {
			@SuppressWarnings({ "rawtypes" })
			@Override
			public void run() {
				Skin s = getSkin(name);
				for(OperationCallback t : c)
					t.done(s);
			}
		});
	}

	private static Skin loadSkin(UUID uuid) throws IOException {
		if (uuid.version() == 3) {
//			throw new IOException("The UUID ("+uuid+") is an Offline-UUID!");
			return new SteveSkin();
		}
		String s = SkinRequestFactory.performGetRequest(new URL(PROFILE_URL + uuid.toString().replace("-", "") + "?unsigned=false"));
		if("".equalsIgnoreCase(s) || s == null)
			throw new IOException("Player skin not found (" + uuid + ")");
		return new Skin(new JSONObject(s));
	}
}
