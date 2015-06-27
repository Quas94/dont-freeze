package com.arctite.dontfreeze;

import com.arctite.dontfreeze.entities.Direction;
import com.arctite.dontfreeze.entities.player.Player;
import com.arctite.dontfreeze.util.SaveManager;
import com.arctite.dontfreeze.util.SoundManager;
import com.badlogic.gdx.Game;
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

	/** Scene2d button dimensions */
	public static final int BUTTON_WIDTH = 130;
	public static final int BUTTON_HEIGHT = 30;
	/** Scene2d UI stuff */
	private static final String DEFAULT = "default";
	private static final String BACKGROUND = "background";
	/** Default skin, for the getDefaultSkin() method */
	private static Skin defaultSkin;

	/** The single SpriteBatch which renders the entire game, all the screens */
	private SpriteBatch spriteBatch;

	/** The menu screen, handling new games, saving/loading, credits, pausing, etc */
	private MenuScreen menu;
	/** Screen representing the game world, where the majority of the game will be played */
	private WorldScreen world;

	/**
	 * Creates a new instance of GameMain.
	 */
	public GameMain() {
	}

	/**
	 * Initialises a new WorldScreen and transitions to that screen.
	 */
	public void setWorldNewGame() {
		world = new WorldScreen(this, null, WorldScreen.NEW_GAME_MAP_X, WorldScreen.NEW_GAME_MAP_Y, spriteBatch);
		setScreen(world);
	}

	/**
	 * Initialises a new WorldScreen with the direction to go from that map
	 *
	 * @param dir the direction to move from the current WorldScreen
	 */
	public void setWorldNewMap(Direction dir) {
		// de-aggro monsters
		world.deaggroMonsters();
		// save this map but not player details
		world.saveGame(false);

		Player player = world.getPlayer();
		float playerX = player.getX();
		float playerY = player.getY();
		int mapX = world.getMapX();
		int mapY = world.getMapY();
		if (dir == Direction.LEFT) {
			playerX = Player.RIGHTMOST_X - 1;
			mapX--;
		} else if (dir == Direction.RIGHT) {
			playerX = 0;
			mapX++;
		} else if (dir == Direction.DOWN) {
			playerY = Player.HIGHEST_Y - 1;
			mapY--;
		} else if (dir == Direction.UP) {
			playerY = 0;
			mapY++;
		}

		this.world = new WorldScreen(this, player, mapX, mapY, spriteBatch);
		SaveManager saver = new SaveManager(true); // true = load
		world.loadGame(saver, false); // don't load player details
		player.setWorldAndPosition(world, playerX, playerY);
		setScreen(world);
	}

	/**
	 * Loads a WorldScreen from the save file and transitions to that screen.
	 */
	public void setWorldLoadGame() {
		// load save manager and get the map chunk to load
		SaveManager saver = new SaveManager(true); // true = load
		int chunkX = saver.getDataValue(SaveManager.PLAYER_MAP_X, Integer.class);
		int chunkY = saver.getDataValue(SaveManager.PLAYER_MAP_Y, Integer.class);
		world = new WorldScreen(this, null, chunkX, chunkY, spriteBatch);
		// load the world
		world.loadGame(saver, true); // loading a game so load player too
		// finally, swap into it
		setScreen(world);
	}

	/**
	 * Transitions to the menu screen.
	 */
	public void setMenu() {
		setScreen(menu);
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
			// @TODO world music
		}
	}

	@Override
	public void create() {
		this.spriteBatch = new SpriteBatch();

		// load sounds first
		SoundManager.loadSounds();
		// start playing music
		SoundManager.playMusic(SoundManager.MENU_BG_MUSIC);

		menu = new MenuScreen(this, spriteBatch);
		setScreen(menu);
	}

	/**
	 * Creates the default Skin that all screens in the game can use.
	 *
	 * @return the default Skin
	 */
	public static Skin getDefaultSkin() {
		if (defaultSkin == null) {
			BitmapFont font = new BitmapFont();
			defaultSkin = new Skin();
			defaultSkin.add(DEFAULT, font);

			// button background pixmap
			Pixmap pixmap = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGB888);
			pixmap.setColor(Color.WHITE);
			pixmap.fill();
			defaultSkin.add(BACKGROUND, new Texture(pixmap));
			// create button style
			TextButton.TextButtonStyle style = new TextButtonStyle();
			style.up = defaultSkin.newDrawable(BACKGROUND, Color.GRAY);
			style.down = defaultSkin.newDrawable(BACKGROUND, Color.DARK_GRAY);
			// style.checked = skin.newDrawable(BACKGROUND, Color.DARK_GRAY);
			style.over = defaultSkin.newDrawable(BACKGROUND, Color.LIGHT_GRAY);
			style.font = defaultSkin.getFont(DEFAULT);
			defaultSkin.add(DEFAULT, style);
		}
		return defaultSkin;
	}
}
