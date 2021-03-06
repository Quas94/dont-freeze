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

		MONSTER_MELEE(21, "monster_melee"),
		MONSTER_SPECIAL(22, "monster_special"),
		MONSTER_SPECIAL_HIT(23, "monster_special_hit"),
		MONSTER_DEATH(23, "monster_death"),

		PLAYER_MELEE(94, "player_melee"),
		PLAYER_SPECIAL(95, "player_special"),
		PLAYER_SPECIAL_HIT(96, "player_special_hit"),
		PLAYER_DEATH(97, "player_death"),
		PLAYER_PICKUP_FIRE(98, "player_pickup_fire"),
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

	/** Volume level */
	private static float volume;

	/**
	 * If enabling sound and music, if there is music loaded and ready to be streamed, this method starts playing it.
	 *
	 * If disabling sound and music, if there is music playing, this method stops playing it.
	 */
	public static void setEnabled(boolean enabled) {
		if (enabled) {
			volume = 1.0F; // set volume to max
		} else {
			volume = 0.0F;
		}
		if (currentMusic != null) {
			currentMusic.setVolume(volume);
		}
	}

	/**
	 * Checks whether or not sound and music are currently enabled.
	 * @return whether sound/music are enabled
	 */
	public static boolean isEnabled() {
		return volume > 0F;
	}

	/**
	 * Gets the current volume level.
	 * @return the current volume level
	 */
	public static float getVolume() {
		return volume;
	}

	/**
	 * Sets the volume of the current music. Used in very short time periods (transitions).
	 *
	 * @param v the volume to set to
	 */
	public static void setVolume(float v) {
		volume = v;
		if (currentMusic != null) {
			currentMusic.setVolume(volume);
		}
	}

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
		currentMusic.setVolume(volume);
		currentMusic.play();
	}

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
			currentMusic.setVolume(volume);
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
	 * Sounds are played at (volume / 2) as opposed to music which is played at full volume.
	 *
	 * @param si the SoundInfo enum instance representing the sound to be played
	 */
	public static void playSound(SoundInfo si) {
		sounds.get(si.id).play(volume / 2);
	}

	/**
	 * Shorthand for playing the MENU_CLICK sound.
	 */
	public static void playClick() {
		playSound(SoundInfo.MENU_CLICK);
	}

	private SoundManager() {
	}
}
