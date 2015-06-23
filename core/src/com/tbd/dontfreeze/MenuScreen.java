package com.tbd.dontfreeze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.HashMap;

/**
 * Handles all operations to do with starting/saving/loading games, credits, pausing, and a bunch of other stuff.
 *
 * Created by Quasar on 23/06/2015.
 */
public class MenuScreen extends AbstractScreen {

	/** Button names */
	private static final String LOAD_GAME = "Load Game";
	private static final String NEW_GAME = "New Game";
	private static final String CREDITS = "Credits";
	private static final String QUIT_GAME = "Quit Game";
	private static final String[] BUTTON_NAMES = { LOAD_GAME, NEW_GAME, CREDITS, QUIT_GAME };

	/** The Scene2D Stage for our menu UI */
	private Stage stage;

	private HashMap<String, TextButton> buttons;

	public MenuScreen(GameMain game) {
		super(game);

		// create the stage
		this.stage = new Stage();

		// get the skin
		Skin skin = GameMain.getDefaultSkin();

		// initialise buttons
		this.buttons = new HashMap<String, TextButton>();
		for (int i = 0; i < BUTTON_NAMES.length; i++) {
			TextButton button = new TextButton(BUTTON_NAMES[i], skin);
			int winWidth = Gdx.graphics.getWidth();
			int winHeight = Gdx.graphics.getHeight();
			button.setPosition((winWidth / 2) - (GameMain.BUTTON_WIDTH / 2),
					(winHeight / 2) - GameMain.BUTTON_HEIGHT -  (i * (GameMain.BUTTON_HEIGHT + 10)));

			// add to stage and map
			stage.addActor(button);
			buttons.put(BUTTON_NAMES[i], button);
		}
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

		// render the scene
		stage.draw();
	}

	@Override
	public void show() {
		// set input processor to menu's one
		Gdx.input.setInputProcessor(stage);
	}
}
