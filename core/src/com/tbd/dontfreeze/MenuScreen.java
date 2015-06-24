package com.tbd.dontfreeze;

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
import com.tbd.dontfreeze.entities.Monster;

import static com.tbd.dontfreeze.GameMain.BUTTON_WIDTH;
import static com.tbd.dontfreeze.GameMain.BUTTON_HEIGHT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Handles all operations to do with starting/saving/loading games, credits, pausing, and a bunch of other stuff.
 *
 * Created by Quasar on 23/06/2015.
 */
public class MenuScreen extends AbstractScreen {

	/** Menu background image file */
	private static final String MENU_BACKGROUND = "assets/menubg.png";
	/** Logo */
	private static final String LOGO_FIRE_PATH = "assets/logofire.atlas";
	private static final String LOGO_MAIN_PATH = "assets/logomain.png";
	private static final float LOGO_FRAME_RATE = 0.12F;

	/** Number of snow babies to have jumping around to server as decorations */
	private static final int NUM_DECORATIONS = 2;
	/** Upper limit on Y for decoration monsters (so they don't go into the sky, without world collisions to bound */
	private static final int DECORATION_Y_BOUND = 310;

	/** Credits text */
	public static final String CREDITS_TEXT = "Developer: Quasar\n\nArt: Axelsior\n\nThe Gimp Master: Rethiqlor";

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
		Skin skin = GameMain.getDefaultSkin();

		// initialise main buttons
		this.buttons = new HashMap<String, TextButton>();
		for (int i = 0; i < MAIN_BUTTON_NAMES.length; i++) {
			TextButton button = new TextButton(MAIN_BUTTON_NAMES[i], skin);
			button.setPosition((winWidth / 2) - (BUTTON_WIDTH / 2),
					(winHeight / 2) - BUTTON_HEIGHT -  (i * (BUTTON_HEIGHT + 10)));

			// add to stage and map
			stage.addActor(button);
			buttons.put(MAIN_BUTTON_NAMES[i], button);
		}
		// return to menu from credits button
		TextButton creditsBackButton = new TextButton(CREDITS_BACK, skin);
		creditsBackButton.setPosition((winWidth / 2) - (BUTTON_WIDTH / 2),
				(winHeight / 2) - BUTTON_HEIGHT - ((MAIN_BUTTON_NAMES.length - 1) * (BUTTON_HEIGHT + 10)));
		creditsBackButton.setVisible(false);
		stage.addActor(creditsBackButton);
		buttons.put(CREDITS_BACK, creditsBackButton);

		// set functionalities for the buttons
		TextButton newGameButton = buttons.get(NEW_GAME);
		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getGame().setNewWorld();
			}
		});
		TextButton quitGameButton = buttons.get(QUIT_GAME);
		quitGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});
		ClickListener flipCreditsModeListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
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
		creditsLabel.setPosition(winWidth / 2 - 85, winHeight / 2 - 80);
		creditsLabel.setVisible(false);
		stage.addActor(creditsLabel);

		// initialise decorations
		this.decorations = new ArrayList<Monster>();
		Random random = new Random();
		for (int i = 0; i < NUM_DECORATIONS; i++) {
			float randX = random.nextFloat() * winWidth;
			float randY = random.nextFloat() * DECORATION_Y_BOUND;
			Monster dec = new Monster(null, randX, randY);
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
		buttons.get(CREDITS_BACK).setVisible(creditsMode);
		creditsLabel.setVisible(creditsMode);
	}

	@Override
	public void update(float delta) {
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
	public void render(float delta) {
		// unlike World, we don't care if the delta is huge in the menu due to lag
		update(delta);
		// prepare for new frame
		clearScreen();

		spriteBatch.begin();
		// draw background
		background.draw(spriteBatch);
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
	}
}
