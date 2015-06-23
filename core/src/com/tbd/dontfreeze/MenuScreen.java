package com.tbd.dontfreeze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.tbd.dontfreeze.GameMain.BUTTON_WIDTH;
import static com.tbd.dontfreeze.GameMain.BUTTON_HEIGHT;

import java.util.HashMap;

/**
 * Handles all operations to do with starting/saving/loading games, credits, pausing, and a bunch of other stuff.
 *
 * Created by Quasar on 23/06/2015.
 */
public class MenuScreen extends AbstractScreen {

	/** Menu background image file */
	private static final String MENU_BACKGROUND = "assets/menubg.png";

	/** Credits text */
	public static final String CREDITS_TEXT = "Developer: Quasar\n\nArt: Axelsior\n\nThe Gimp Master: Rethiqlor";

	/** Button names */
	private static final String LOAD_GAME = "Load Game";
	private static final String NEW_GAME = "New Game";
	private static final String CREDITS = "Credits";
	private static final String QUIT_GAME = "Quit Game";
	private static final String[] MAIN_BUTTON_NAMES = { LOAD_GAME, NEW_GAME, CREDITS, QUIT_GAME };
	private static final String CREDITS_BACK = "Back to Menu";

	/** Background image for the menu */
	private Sprite background;

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

		this.background = new Sprite(new Texture(Gdx.files.internal(MENU_BACKGROUND)));

		// create the stage
		this.stage = new Stage();

		// get the skin
		Skin skin = GameMain.getDefaultSkin();

		int winWidth = Gdx.graphics.getWidth();
		int winHeight = Gdx.graphics.getHeight();
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
		stage.act(delta);
	}

	@Override
	public void render(float delta) {
		// unlike World, we don't care if the delta is huge in the menu due to lag
		update(delta);
		// prepare for new frame
		clearScreen();

		// draw background
		spriteBatch.begin();
		background.draw(spriteBatch);
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
