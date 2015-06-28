package com.arctite.dontfreeze.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.logging.FileHandler;

/**
 * Static class that is used to store and playback sound effects and music.
 *
 * Created by Quasar on 27/06/2015.
 */
public class SoundManager {


	/**
	 * Contains list of sound ids coupled with their filename.
	 */
	public static enum SoundInfo {

		MENU_CLICK(1, "menu_click"),

		MONSTER_MELEE(2, "monster_melee"),
		MONSTER_DEATH(3, "monster_death"),

		PLAYER_MELEE(4, "player_melee"),
		PLAYER_SPECIAL(5, "player_special"),
		PLAYER_SPECIAL_EXPLOSION(6, "player_special_hit"),
		PLAYER_DEATH(7, "player_death"),

		// pickup fire
		;

		private int id;
		private String file;

		private SoundInfo(int id, String file) {
			this.id = id;
			this.file = file;
		}
	}

	private static HashMap<Integer, Sound> sounds;

	private static final String FOLDER = "assets/sound/";
	private static final String SOUND_EXT = ".wav";
	private static final String MUSIC_EXT = ".mp3";

	/** List of music files */
	public static final String MENU_BG_MUSIC = "menu";

	private static Music currentMusic;

	/**
	 * Stops the current music which is playing and disposes of the resources.
	 */
	public static void stopCurrentMusic() {
		if (currentMusic != null) {
			currentMusic.stop();
			currentMusic.dispose();
			currentMusic = null;
		}
	}

	/**
	 * Plays the music file with the given name.
	 *
	 * @param file the name of the music file
	 */
	public static void playMusic(String file) {
		currentMusic = Gdx.audio.newMusic(Gdx.files.internal(FOLDER + file + MUSIC_EXT));
		currentMusic.setLooping(true);
		currentMusic.play();}

	/**
	 * Plays the music file for the given chunk, or outputs a message if no music has been assigned yet
	 * @param chunkX the x coordinate of the chunk to play music for
	 * @param chunkY the y coordinate of the chunk to play music for
	 */
	public static void playMusic(int chunkX, int chunkY) {
		String file = chunkX + "_" + chunkY;
		FileHandle musicFile = Gdx.files.internal(FOLDER + file + MUSIC_EXT);
		if (musicFile.exists()) {
			currentMusic = Gdx.audio.newMusic(musicFile);
			currentMusic.setLooping(true);
			currentMusic.play();
		} else {
			System.out.printf("chunk (%d, %d) has no assigned music\n", chunkX, chunkY);
		}
	}

	/**
	 * Loads all sounds into memory.
	 */
	public static void loadSounds() {
		sounds = new HashMap<Integer, Sound>();
		for (SoundInfo s : SoundInfo.values()) {
			sounds.put(s.id, Gdx.audio.newSound(Gdx.files.internal(FOLDER + s.file + SOUND_EXT)));
		}
	}

	/**
	 * Plays the given sound.
	 *
	 * @param si the SoundInfo enum instance representing the sound to be played
	 */
	public static void playSound(SoundInfo si) {
		sounds.get(si.id).play(1.0F);
	}

	/**
	 * Shorthand for playing the MENU_CLICK sound.
	 */
	public static void playClick() {
		sounds.get(SoundInfo.MENU_CLICK.id).play(1.0F);
	}

	private SoundManager() {
	}
}
