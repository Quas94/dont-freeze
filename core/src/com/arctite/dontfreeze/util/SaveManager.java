package com.arctite.dontfreeze.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Manages game saving and loading.
 *
 * Created by Quasar on 24/06/2015.
 */
public class SaveManager {

	/** Save key constants */
	public static final String PLAYER_CHUNK_X = "playerchunkx";
	public static final String PLAYER_CHUNK_Y = "playerchunky";
	public static final String VISITED_CHUNK = "visitedchunk";
	public static final String CAMERA_X = "camx";
	public static final String CAMERA_Y = "camy";
	/** Entity-related save key constants */
	public static final String ACTIVE = "active"; // whether this monster/collectable is still alive/not-picked-up
	public static final String PLAYER = "player";
	public static final String FIRES = "f"; // player only, but still need PLAYER prefix
	public static final String MONSTER = "m";
	public static final String AGGRO = "a"; // monster only, but still need MONSTER prefix
	public static final String COLLECTABLE = "c";
	public static final String POSITION_X = "x";
	public static final String POSITION_Y = "y";
	public static final String DIR_IDX = "d";
	public static final String HEALTH = "hp";

	/** Whether or not to encrypt the save file */
	private static final boolean ENCODE = false;

	/** Singleton */
	private static SaveManager singleton;

	public static SaveManager getSaveManager() {
		if (singleton == null) {
			singleton = new SaveManager();
		}
		return singleton;
	}

	/** Location of save file */
	private static class Save {

		/** ObjectMap to store all the save data */
		private ObjectMap<String, Object> data;

		private Save() {
			this.data = new ObjectMap<String, Object>();
		}
	}

	private static final String SAVE_FILE = "save.json";

	/** Save file handle */
	private FileHandle file;
	/** Save object containing mappings */
	private Save save;

	private SaveManager() {
		this.save = new Save();
		this.file = Gdx.files.local(SAVE_FILE);
	}

	public void load() {
		if (!file.exists()) {
			throw new RuntimeException("Attempting to load save file: none found");
		}
		Json json = new Json();
		String fileString = file.readString();
		if (ENCODE) {
			fileString = Base64Coder.decodeString(fileString);
		}
		save = json.fromJson(Save.class, fileString);
	}

	public static boolean saveFileExists() {
		return Gdx.files.local(SAVE_FILE).exists();
	}

	@SuppressWarnings("unchecked")
	public <T> T getDataValue(String key, Class type) {
		if (save.data.containsKey(key)) {
			return (T) save.data.get(key);
		}
		return null;
	}

	public boolean hasDataValue(String key) {
		return save.data.containsKey(key);
	}

	/**
	 * Removes all mappings from the Save object which have a key with a prefix of the given chunkId
	 *
	 * @param chunkId the chunk prefix
	 */
	public void removeChunkDataValues(String chunkId) {
		for (String key :save.data.keys()) {
			if (key.startsWith(chunkId)) {
				save.data.remove(key);
			}
		}
	}

	/**
	 * Saves to the external Json file.
	 */
	public void saveToJson() {
		Json json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);
		String outputString = json.prettyPrint(save);
		if (ENCODE) {
			outputString = Base64Coder.encodeString(outputString);
		}
		file.writeString(outputString, false);
	}

	/**
	 * Places the key-object pair into the data storage to be saved upon next call of saveToJson()
	 *
	 * @param key the key of the object to be saved
	 * @param object the object to be saved
	 */
	public void setDataValue(String key, Object object) {
		save.data.put(key, object);
	}

	/**
	 * Clears all data in this SaveManager. Done when starting a new game.
	 */
	public void clearAll() {
		save.data.clear();
	}
}
