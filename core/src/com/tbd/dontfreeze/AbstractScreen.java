package com.tbd.dontfreeze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

/**
 * Abstract screen class that the WorldScreen and MenuScreen classes will extend.
 *
 * Created by Quasar on 14/06/2015.
 */
public abstract class AbstractScreen implements Screen {

	/** If delta exceeds this limit in milliseconds, update is skipped */
	protected static final int DELTA_LIMIT = 1000;

	/** protected because subclasses will require reference to the Game */
	protected Game game;

	public AbstractScreen(Game game) {
		this.game = game;
	}

	/**
	 * Method in which all the game logic and transitions will be applied.
	 *
	 * This method should be called by render() in all subclasses.
	 *
	 * @param delta The amount of time that has passed since the last time this method was called
	 */
	public abstract void update(float delta);

	/**
	 * This method should firstly call update() first and pass along delta.
	 *
	 * Is responsible for rendering all screen elements (after calling update()).
	 *
	 * @param delta The amount of time that has passed since the last time this method was called
	 */
	@Override
	public abstract void render(float delta);

	@Override
	public void resize(int width, int height) {
	}

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
