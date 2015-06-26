package com.arctite.dontfreeze;

import com.badlogic.gdx.Game;
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
	public void setNewWorld() {
		world = new WorldScreen(this, spriteBatch);
		setScreen(world);
	}

	/**
	 * Loads a WorldScreen from the save file and transitions to that screen.
	 */
	public void setLoadWorld() {
		world = new WorldScreen(this, spriteBatch);
		// load the world
		world.loadGame();
		// finally, swap into it
		setScreen(world);
	}

	/**
	 * Transitions to the menu screen.
	 */
	public void setMenu() {
		setScreen(menu);
	}

	@Override
	public void create() {
		this.spriteBatch = new SpriteBatch();

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
