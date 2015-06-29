package com.arctite.dontfreeze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Abstract screen class that the WorldScreen and MenuScreen classes will extend.
 *
 * Created by Quasar on 14/06/2015.
 */
public abstract class AbstractScreen implements Screen {

	/** If delta exceeds this limit in seconds, update is skipped */
	protected static final float DELTA_LIMIT = 0.25F; // quarter of a second

	/** The SpriteBatch which renders this Screen (and every other Screen in the game) */
	protected final SpriteBatch spriteBatch;

	/** The Game object this Screen is a part of */
	private final GameMain game;

	public AbstractScreen(GameMain game, SpriteBatch spriteBatch) {
		this.game = game;

		this.spriteBatch = spriteBatch;
	}

	public GameMain getGame() {
		return game;
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
		update(delta);
		render();
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
