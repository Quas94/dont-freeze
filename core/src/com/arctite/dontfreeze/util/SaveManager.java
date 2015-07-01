package com.arctite.dontfreeze.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages game saving and loading.
 *
 * Also manages game settings that aren't save-file-specific.
 *
 * Created by Quasar on 24/06/2015.
 */
public class SaveManager {

	/** Save key constants */
	public static final String PLAYER_CHUNK_X = "playerchunkx";
	public static final String PLAYER_CHUNK_Y = "playerchunky";
	public static final String VISITED_CHUNK = "visitedchunk";
	// camera location isn't saved anymore, just centred and adjusted accordingly
	//public static final String CAMERA_X = "camx";
	//public static final String CAMERA_Y = "camy";
	/** Event settings */
	public static final String EVENT_PROPERTY_SETTING = "eventpropset";
	/** Entity-related save key constants */
	public static final String ACTIVE = "active"; // whether this monster/collectable is still alive/not-picked-up
	public static final String TRIGGERED = "trig"; // whether this event has been triggered yet
	public static final String PLAYER = "player";
	public static final String FIRES = "f"; // player only, but still need PLAYER prefix
	public static final String MONSTER = "m";
	public static final String AGGRO = "a"; // monster only, but still need MONSTER prefix
	public static final String COLLECTABLE = "c";
	public static final String EVENT = "evt";
	public static final String POSITION_X = "x";
	public static final String POSITION_Y = "y";
	public static final String DIR_IDX = "d";
	public static final String HEALTH = "hp";

	/** Settings constants */
	// NOTE: when adding something here the default value must be specified in settings constructor (see below)
	public static final String VOLUME = "volume";
	/** New game stuff */
	public static final String NEW_GAME_CHUNK_X = "newgamechunkx";
	public static final String NEW_GAME_CHUNK_Y = "newgamechunky";
	public static final String NEW_GAME_PLAYER_X = "newgameplayerx";
	public static final String NEW_GAME_PLAYER_Y = "newgameplayery";

	/** Whether or not to encrypt the output file */
	private static final boolean ENCODE = false;

	/** Singletons */
	private static SaveManager singleton;
	private static SaveManager settings;

	public static SaveManager getSaveManager() {
		if (singleton == null) {
			singleton = new SaveManager();
		}
		return singleton;
	}

	public static SaveManager getSettings() {
		if (settings == null) {
			settings = new SaveManager(SETTINGS_FILE);
		}
		return settings;
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
	private static final String SETTINGS_FILE = "settings.json";

	/** Save file handle */
	private FileHandle file;
	/** Save object containing mappings */
	private Save save;

	private SaveManager() {
		this.save = new Save();
		this.file = Gdx.files.local(SAVE_FILE);
	}

	/**
	 * Used for constructing a save manager which actually handles settings.
	 *
	 * @param settings the settings file
	 */
	private SaveManager(String settings) {
		this.save = new Save();
		this.file = Gdx.files.local(settings);
		if (file.exists()) {
			load(); // load settings immediately upon object construction if we find a file
		} else {
			// DEFAULT SETTINGS HERE
			setDataValue(VOLUME, 1.0F);
			setDataValue(NEW_GAME_CHUNK_X, 0);
			setDataValue(NEW_GAME_CHUNK_Y, 5);
			setDataValue(NEW_GAME_PLAYER_X, 270);
			setDataValue(NEW_GAME_PLAYER_Y, 940);
		}
	}

	public void load() {
		if (!file.exists()) {
			throw new RuntimeException("Attempting to load file (" + file.name() + "), not found");
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

	/**
	 * Returns all keys in the data that are prefixed by the given String.
	 *
	 * @param prefix the prefix of wanted keys
	 * @return list of all keys with given prefix
	 */
	public ArrayList<String> getKeysByPrefix(String prefix) {
		ArrayList<String> list = new ArrayList<String>();
		for (String key : save.data.keys()) {
			if (key.startsWith(prefix)) list.add(key);
		}
		return list;
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
