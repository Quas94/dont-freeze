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
	private static final int DELTA_LIMIT = 1000;

	/** protected because subclasses will require reference to the Game */
	protected Game game;

	public AbstractScreen(Game game) {
		this.game = game;
	}

	/** protected because the only place this will be called from will be from render() in subclasses */
	protected abstract void update(float delta);

	@Override
	public void render(float delta) {
		if (delta <= DELTA_LIMIT) {
			update(delta);
		}
	}

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
