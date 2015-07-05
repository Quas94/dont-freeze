package com.arctite.dontfreeze;

import com.arctite.dontfreeze.entities.Direction;
import com.arctite.dontfreeze.entities.player.Player;
import com.arctite.dontfreeze.entities.player.WorldInputHandler;
import com.arctite.dontfreeze.ui.SkinManager;
import com.arctite.dontfreeze.util.GameMessages;
import com.arctite.dontfreeze.util.SaveManager;
import com.arctite.dontfreeze.util.SoundManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * Main class of Don't Freeze!
 *
 * Contains the screens of the game (WorldScreen, and in the future, MenuScreen etc) and controls the state of the game.
 *
 * Created by Quasar on 14/06/2015.
 */
public class GameMain extends Game {

	/** Game window title */
	public static final String GAME_WINDOW_TITLE = "Don't Freeze! dev version";
	/** Height of the game window across all platforms */
	public static final int GAME_WINDOW_WIDTH = 640;
	/** Width of the game window across all platforms */
	public static final int GAME_WINDOW_HEIGHT = 480;

	/** The single SpriteBatch which renders the entire game, all the screens */
	private SpriteBatch spriteBatch;

	/** The menu screen, handling new games, saving/loading, credits, pausing, etc */
	private MenuScreen menu;
	/** Screen representing the game world, where the majority of the game will be played */
	private WorldScreen world;
	/** WorldInputHandler which should stay the same throughout the entire game application */
	private WorldInputHandler worldInputHandler;

	/** Direction flag for use by the setWorldChangeMap method */
	private Direction changeMapDir;

	/**
	 * Enum used to decide which method to call after a fade out of an AbstractScreen.
	 */
	public static enum ChangeType {

		WORLD_NEW_GAME,
		WORLD_CHANGE_MAP,
		WORLD_LOAD_GAME,
		MENU,
		;
	}

	/**
	 * Creates a new instance of GameMain.
	 */
	public GameMain() {
	}

	/**
	 * Checks for the pressing of keyboard key M and toggles sound if pressed.
	 */
	public void checkToggleSound() {
		// toggle sound if M key pressed
		if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
			SoundManager.setEnabled(!SoundManager.isEnabled());
		}
	}

	/**
	 * Initialises a new WorldScreen and transitions to that screen.
	 */
	private void setWorldNewGame() {
		world = new WorldScreen(this, worldInputHandler, null, spriteBatch);
		// new game, so clear save manager
		SaveManager.getSaveManager().clearAll();
		setScreen(world);
	}

	/**
	 * Sets the direction for use by the setWorldChangeMap() method.
	 *
	 * @param dir the direction to change maps to
	 */
	public void setWorldChangeMapDirection(Direction dir) {
		changeMapDir = dir;
	}

	/**
	 * Initialises a new WorldScreen and changes over to it.
	 *
	 * The direction pre-load method setWorldChangeMapDirection() MUST be called before invoking setTransitioning() to
	 * this method.
	 */
	private void setWorldChangeMap() {
		if (changeMapDir == null) {
			throw new IllegalStateException("setWorldChangeMapDirection() must be called before setWorldChangeMap()");
		}

		// de-aggro monsters in from-chunk
		world.deaggroMonsters();
		// save from-chunk's values
		world.saveValues();

		// calculate player's new chunk and coordinates within that new chunk
		Player player = world.getPlayer();
		float playerX = player.getX();
		float playerY = player.getY();
		int mapX = world.getChunkX();
		int mapY = world.getChunkY();
		if (changeMapDir == Direction.LEFT) {
			playerX = Player.RIGHTMOST_X - 1;
			mapX--;
		} else if (changeMapDir == Direction.RIGHT) {
			playerX = 0;
			mapX++;
		} else if (changeMapDir == Direction.DOWN) {
			playerY = Player.HIGHEST_Y - 1;
			mapY--;
		} else if (changeMapDir == Direction.UP) {
			playerY = 0;
			mapY++;
		}

		// set which chunk the player is in now
		player.setChunk(mapX, mapY);
		player.setPosition(playerX, playerY);
		// player.save(); // don't need to save player when changing maps
		// don't save to file when changing maps

		// load the new chunk into a new WorldScreen (dropping the old WorldScreen instance completely)
		this.world = new WorldScreen(this, worldInputHandler, player, spriteBatch);
		world.loadValues();

		// finally, new screen
		setScreen(world);
	}

	/**
	 * Loads a WorldScreen from the save file and transitions to that screen.
	 */
	private void setWorldLoadGame() {
		// load save manager and get the map chunk to load
		SaveManager.getSaveManager().load();
		// load player info first
		Player player = new Player(null, null, 0, 0);
		player.load();
		world = new WorldScreen(this, worldInputHandler, player, spriteBatch);
		// load the world
		world.loadValues();
		// finally, swap into it
		setScreen(world);
	}

	/**
	 * Transitions to the menu screen.
	 */
	private void setMenu() {
		setScreen(menu);
	}

	/**
	 * Calls the corresponding change method, given the type enum.
	 *
	 * @param type the type which defines which method to call
	 */
	public void setScreen(ChangeType type) {
		switch (type) {
			case WORLD_CHANGE_MAP:
				setWorldChangeMap();
				break;
			case WORLD_LOAD_GAME:
				setWorldLoadGame();
				break;
			case WORLD_NEW_GAME:
				setWorldNewGame();
				break;
			case MENU:
				setMenu();
				break;
		}
	}

	/**
	 * Adds extra functionality: changes music to new screen's.
	 *
	 * @param screen the screen we're changing to
	 */
	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);

		// change music
		SoundManager.stopCurrentMusic();
		// play new music
		if (screen instanceof MenuScreen) {
			SoundManager.playMusic(SoundManager.MENU_BG_MUSIC);
		} else if (screen instanceof WorldScreen) {
			WorldScreen world = (WorldScreen) screen;
			// play music for this chunk
			SoundManager.playMusic(world.getChunkX(), world.getChunkY());
		}
	}

	@Override
	public void create() {
		// initialise sprite batch
		this.spriteBatch = new SpriteBatch();

		// load input handler
		this.worldInputHandler = new WorldInputHandler();

		// load settings
		SaveManager settings = SaveManager.getSettings();
		float volume = settings.getDataValue(SaveManager.VOLUME, Float.class);
		SoundManager.setEnabled(volume > 0);

		// load sounds
		SoundManager.loadSounds();

		// initialise skins
		SkinManager.loadSkins();

		// load event-triggered messages from file
		GameMessages.loadMessages();

		// lastly, create screen and change over
		menu = new MenuScreen(this, spriteBatch);
		setScreen(menu);
	}
}
