package com.arctite.dontfreeze;

import com.arctite.dontfreeze.entities.LiveEntity;
import com.arctite.dontfreeze.ui.SkinManager;
import com.arctite.dontfreeze.util.ResourceInfo;
import com.arctite.dontfreeze.util.SaveManager;
import com.arctite.dontfreeze.util.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.arctite.dontfreeze.entities.Monster;

import java.util.*;

/**
 * Handles all operations to do with starting/saving/loading games, credits, pausing, and a bunch of other stuff.
 *
 * Created by Quasar on 23/06/2015.
 */
public class MenuScreen extends AbstractScreen {

	private static final String MENU_FOLDER = "assets/menu/";

	/** Menu background image file */
	private static final String MENU_BACKGROUND = MENU_FOLDER + "menubg.png";
	/** Logo */
	private static final String LOGO_FIRE_PATH = MENU_FOLDER + "logofire.atlas";
	private static final String LOGO_MAIN_PATH = MENU_FOLDER + "logomain.png";
	private static final float LOGO_FRAME_RATE = 0.12F;

	/** Number of snow babies to have jumping around to server as decorations */
	private static final int NUM_DECORATIONS = 2;
	/** Upper limit on Y for decoration monsters (so they don't go into the sky, without world collisions to bound */
	private static final int DECORATION_Y_BOUND = 310;

	/** Credits text */
	public static final String CREDITS_TEXT = "Developer: Quasar\n\nArt: Axelsior\n\nContent Creator: Reth";

	/** Button names */
	private static final String LOAD_GAME = "Load Game";
	private static final String NEW_GAME = "New Game";
	private static final String CREDITS = "Credits";
	private static final String QUIT_GAME = "Quit Game";
	private static final String[] MAIN_BUTTON_NAMES = { LOAD_GAME, NEW_GAME, CREDITS, QUIT_GAME };
	private static final String CREDITS_BACK = "Back to Menu";

	/** Main banner of logo */
	private Sprite logoMain;
	/** Logo animation */
	private Animation logoFire;
	private float logoFireStateTime;
	/** Background image for the menu */
	private Sprite background;
	/** Logo render position */
	private float logoX;
	private float logoY;

	/** Have some snow babies jumping around */
	private ArrayList<Monster> decorations;

	/** The Scene2D Stage for our menu UI */
	private Stage stage;
	/** Whether or not this menu is in the display credits state */
	private boolean creditsMode;
	/** HashMap containing all buttons in this MenuScreen */
	private HashMap<String, TextButton> buttons;
	/** Credits label */
	private Label creditsLabel;

	public MenuScreen(GameMain game, SpriteBatch spriteBatch) {
		super(game, spriteBatch);

		// window dimensions
		int winWidth = Gdx.graphics.getWidth();
		int winHeight = Gdx.graphics.getHeight();

		// load logo fire
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(LOGO_FIRE_PATH));
		Array<TextureRegion> regions = new Array<TextureRegion>();
		for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
			regions.add(region);
		}
		this.logoFire = new Animation(LOGO_FRAME_RATE, regions);
		logoFire.setPlayMode(Animation.PlayMode.LOOP);
		// load logo main
		this.logoMain = new Sprite(new Texture(Gdx.files.internal(LOGO_MAIN_PATH)));
		// load background
		this.background = new Sprite(new Texture(Gdx.files.internal(MENU_BACKGROUND)));
		// work out logo render position
		this.logoX = (winWidth / 2) - (logoMain.getWidth() / 2);
		this.logoY = (winHeight / 2) + 50;
		logoMain.setPosition(logoX, logoY);
		// for logoFire, position is given to spriteBatch when drawing

		// create the stage
		this.stage = new Stage();

		// get the skin
		Skin skin = SkinManager.getSkin(SkinManager.MENU_BUTTON_SKIN);

		// initialise main buttons
		int buttonWidth = SkinManager.MENU_BUTTON_WIDTH;
		int buttonHeight = SkinManager.MENU_BUTTON_HEIGHT;
		this.buttons = new HashMap<String, TextButton>();
		for (int i = 0; i < MAIN_BUTTON_NAMES.length; i++) {
			TextButton button = new TextButton(MAIN_BUTTON_NAMES[i], skin);
			button.setPosition((winWidth / 2) - (buttonWidth / 2),
					(winHeight / 2) - buttonHeight -  (i * (buttonHeight + 10)));

			// add to stage and map
			stage.addActor(button);
			buttons.put(MAIN_BUTTON_NAMES[i], button);
		}
		// return to menu from credits button
		TextButton creditsBackButton = new TextButton(CREDITS_BACK, skin);
		creditsBackButton.setPosition((winWidth / 2) - (buttonWidth / 2),
				(winHeight / 2) - buttonHeight - ((MAIN_BUTTON_NAMES.length - 1) * (buttonHeight + 10)));
		creditsBackButton.setVisible(false);
		stage.addActor(creditsBackButton);
		buttons.put(CREDITS_BACK, creditsBackButton);

		// set functionalities for the buttons
		TextButton loadGameButton = buttons.get(LOAD_GAME);
		loadGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SoundManager.playClick();
				getGame().setWorldLoadGame();
			}
		});
		TextButton newGameButton = buttons.get(NEW_GAME);
		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SoundManager.playClick();
				getGame().setWorldNewGame();
			}
		});
		TextButton quitGameButton = buttons.get(QUIT_GAME);
		quitGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SoundManager.playClick();
				// save settings
				SaveManager.getSettings().setDataValue(SaveManager.VOLUME, SoundManager.getVolume());
				SaveManager.getSettings().saveToJson();
				Gdx.app.exit();
			}
		});
		ClickListener flipCreditsModeListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SoundManager.playClick();
				flipCreditsMode();
			}
		};
		TextButton creditsButton = buttons.get(CREDITS);
		creditsButton.addListener(flipCreditsModeListener);
		creditsBackButton.addListener(flipCreditsModeListener);

		// credits label
		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = new BitmapFont();
		labelStyle.fontColor = Color.BLACK;
		this.creditsLabel = new Label(CREDITS_TEXT, labelStyle);
		creditsLabel.setPosition(winWidth / 2 - 70, winHeight / 2 - 80);
		creditsLabel.setVisible(false);
		stage.addActor(creditsLabel);

		// initialise decorations
		this.decorations = new ArrayList<Monster>();
		Random random = new Random();
		ResourceInfo info = ResourceInfo.SNOW_BABY;
		for (int i = 0; i < NUM_DECORATIONS; i++) {
			float randX = random.nextFloat() * (winWidth - info.getWidth());
			float randY = random.nextFloat() * (DECORATION_Y_BOUND - info.getHeight());
			Monster dec = new Monster(null, info.getId(), randX, randY);
			decorations.add(dec);
		}
	}

	/**
	 * Flips this menu's state into and out of credit mode and does the associated enabling/disabling of relevant
	 * menu actors accordingly.
	 */
	private void flipCreditsMode() {
		creditsMode = !creditsMode;
		for (String s : MAIN_BUTTON_NAMES) {
			buttons.get(s).setVisible(!creditsMode);
		}
		// loadgame button is special
		boolean saveExists = SaveManager.saveFileExists();
		buttons.get(LOAD_GAME).setVisible(saveExists && !creditsMode);

		buttons.get(CREDITS_BACK).setVisible(creditsMode);
		creditsLabel.setVisible(creditsMode);
	}

	@Override
	public void update(float delta) {
		// check toggle sound
		getGame().checkToggleSound();

		// update UI
		stage.act(delta);
		// update logo fire
		logoFireStateTime += delta;
		// update decoration monsters
		for (Monster dec : decorations) {
			dec.updateAsDecoration(delta, DECORATION_Y_BOUND);
		}
	}

	@Override
	public void render() {
		// prepare for new frame
		clearScreen();

		spriteBatch.begin();
		// draw background
		background.draw(spriteBatch);
		// sort monsters
		Collections.sort(decorations, new Comparator<LiveEntity>() {
			@Override
			public int compare(LiveEntity e1, LiveEntity e2) {
				float y1 = e1.getY();
				float y2 = e2.getY();
				if (y1 < y2) {
					return 1;
				} else if (y1 > y2) {
					return -1;
				}
				return 0; // equal
			}
		});
		// draw decoration monsters
		for (Monster dec : decorations) {
			dec.render(spriteBatch);
		}
		// render the logo
		logoMain.draw(spriteBatch);
		spriteBatch.draw(logoFire.getKeyFrame(logoFireStateTime), logoX, logoY);
		spriteBatch.end();
		// render the scene
		stage.draw();
	}

	@Override
	public void show() {
		// set input processor to menu's one
		Gdx.input.setInputProcessor(stage);

		// check if a save file exists and enable/disable the load game button accordingly
		boolean saveExists = SaveManager.saveFileExists();
		buttons.get(LOAD_GAME).setVisible(saveExists);
	}
}
