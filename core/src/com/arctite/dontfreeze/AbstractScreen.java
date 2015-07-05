package com.arctite.dontfreeze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Abstract screen class that the WorldScreen and MenuScreen classes will extend.
 *
 * Created by Quasar on 14/06/2015.
 */
public abstract class AbstractScreen implements Screen {

	/** Fade image */
	private static final String TRANSITION_TEXTURE_LOCATION = "assets/menu/fade.png";

	/** Transition speed */
	private static final float TRANSITION_SPEED = 1.5F;

	/** If delta exceeds this limit in seconds, update is skipped */
	protected static final float DELTA_LIMIT = 0.25F; // quarter of a second

	/** The SpriteBatch which renders this Screen (and every other Screen in the game) */
	protected final SpriteBatch spriteBatch;

	/** The Game object this Screen is a part of */
	private final GameMain game;

	/** Transition texture */
	private Sprite transitionImage;
	/** Whether or not this Screen is currently transitioning */
	private boolean transitioning;
	/** If transitioning, whether it is transitioning in (fading in) or out (fading out) */
	private boolean transitionIn;
	/** Alpha value of the transition image, 1 is screen faded out completely, 0 is screen faded in completely */
	private float transitionAlpha;
	/** Whether our transition out has completed */
	private boolean transitionedOut;
	/** The change type we're making, after we fade out (only if we're transitioning out, obviously) */
	private GameMain.ChangeType transitionOutType;

	public AbstractScreen(GameMain game, SpriteBatch spriteBatch) {
		this.game = game;

		this.spriteBatch = spriteBatch;
	}

	public GameMain getGame() {
		return game;
	}

	/**
	 * Sets this screen to currently transitioning, whether we are transitioning in or out, and if out, which screen
	 * to swap to after we've faded out completely.
	 *
	 * @param transIn true if fading in, false if fading out
	 * @param type if fading in, null. if fading out, the enum that methods which method to call, to change screens
	 */
	public void setTransitioning(boolean transIn, GameMain.ChangeType type) {
		transitioning = true;
		transitionIn = transIn;
		if (transIn) {
			transitionAlpha = 1.0F;
		} else {
			transitionAlpha = 0.0F;
			transitionOutType = type;
		}

		// load transition texture if not already loaded
		if (transitionImage == null) {
			transitionImage = new Sprite(new Texture(Gdx.files.internal(TRANSITION_TEXTURE_LOCATION)));
		}
	}

	public boolean isTransitioning() {
		return transitioning;
	}

	/**
	 * Calls the OpenGL methods to clear the screen before a new frame is rendered.
	 */
	public final void clearScreen() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	/**
	 * Method in which all the animation updates, game logic changes and transitions will be applied.
	 *
	 * This method should be called by render() in all subclasses.
	 *
	 * @param delta The amount of time that has passed since the last time this method was called
	 */
	public abstract void update(float delta);

	/**
	 * Actually renders the screen elements.
	 */
	public abstract void render();

	/**
	 * This method calls the updateAnimations, updateLogic, and renderVisuals methods, in that order.
	 *
	 * @param delta The amount of time that has passed since the last time this method was called
	 */
	@Override
	public final void render(float delta) {
		if (delta > DELTA_LIMIT) {
			// delta too high, probably recovered from long freeze, skip it
			return;
		}

		// check the transition
		if (transitioning) {
			if (transitionIn && transitionAlpha <= 0.0F) { // faded in: stop transitioning
				transitioning = false;
				transitionAlpha = 0.0F;
			} else if (!transitionIn && transitionAlpha >= 1.0F) { // faded out: change screens
				transitioning = false;
				transitionedOut = true;
				transitionAlpha = 1.0F;
				game.setScreen(transitionOutType);
			}
		}

		update(delta);
		render();

		transition(delta);
	}

	/**
	 * If transitioning, updates and draws the tint over the screen.
	 *
	 * @param delta time passed (in seconds) since last frame was rendered
	 */
	private final void transition(float delta) {
		if (transitioning) {
			float modifier = TRANSITION_SPEED * delta;
			if (transitionIn) modifier = -modifier; // flip to negative if fading out
			transitionAlpha += modifier; // update the alpha accordingly
			if (transitionIn && transitionAlpha <= 0) transitionAlpha = 0;
			else if (!transitionIn && transitionAlpha >= 1) transitionAlpha = 1;
		}
		if (transitioning || transitionedOut) {
			// draw tint
			spriteBatch.begin();
			transitionImage.draw(spriteBatch, transitionAlpha);
			spriteBatch.end();
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	/**
	 * This method is called when its particular Screen is set to currently displaying. Should update Gdx's input
	 * processor within this method.
	 */
	@Override
	public void show() {
		// start transition
		setTransitioning(true, null);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}
}
