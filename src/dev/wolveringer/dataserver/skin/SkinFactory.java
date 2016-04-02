package dev.wolveringer.dataserver.skin;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import dev.wolveringer.skin.Skin;
import dev.wolveringer.skin.SteveSkin;

public class SkinFactory {
	private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

	private static LoadingCache<UUID, Skin> profileCache = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(4, TimeUnit.HOURS).build(new CacheLoader<UUID, Skin>() {
		public Skin load(UUID name) throws Exception {
			Skin skin = null;
			try{
				skin = loadSkin(name);
			}catch(Exception e){
				e.printStackTrace();
			}
			return skin;
		};
	});

	@Deprecated
	public static Skin getSkin(UUID uuid) {
		if(uuid == null)
			throw new IllegalArgumentException("UUID cant be null");
		try{
			Skin s = profileCache.get(uuid);
			if(s == null){
				profileCache.refresh(uuid);
				s = profileCache.get(uuid);
			}
			return s.clone();
		}catch (Exception e){
			System.err.println("Cant loading Skin for " + uuid + " (Reson: " + e.getMessage() + ")");
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
	
	public static Skin createEmptySkin(){
		return Skin.createEmptySkin();
	}
	
	@Deprecated
	public static Skin getSkin(String name) {
		try{
			return getSkin(UUIDFetcher.getUUIDOf(name));
		}catch (Exception e){
			e.printStackTrace();
		}
		return Skin.createEmptySkin();
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
		String s = SkinRequestFactory.performGetRequest(new URL(PROFILE_URL + uuid.toString().replace("-", "") + "?unsigned=false"));
		if("".equalsIgnoreCase(s) || s == null)
			throw new IOException("Player skin not found (" + uuid + ")");
		return new Skin(new JSONObject(s));
	}
	
	public static void main(String[] args) {
		Skin s = Skin.createEmptySkin();
		s.setRawData("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjdiYmQwYjI5MTFjOTZiNWQ4N2IyZGY3NjY5MWE1MWI4YjEyYzZmZWZkNTIzMTQ2ZDhhYzVlZjFiOGVlIn19fQ==");
		s.setSkin(SkinFactory.getSkin("WolverinDE").getSkinUrl());
		s.setUUID(UUID.randomUUID());
		
		System.out.print(s.getRawData());
	}
}