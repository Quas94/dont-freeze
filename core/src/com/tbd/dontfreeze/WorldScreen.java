package com.tbd.dontfreeze;

import com.badlogic.gdx.Game;

/**
 * In-game screen where the actual gameplaying will take place.
 *
 * Map rendering will be done by rendering the current 640x480 chunk the player is standing on, and then rendering the
 * neighbouring chunks as necessary.
 *
 * Created by Quasar on 14/06/2015.
 */
public class WorldScreen extends AbstractScreen {

	/** Map dimensions */
	private int width;
	private int height;

	public WorldScreen(Game game) {
		super(game);

		// test values
		this.width = 2560;
		this.height = 1960;
	}

	public void update(float delta) {

	}
}
