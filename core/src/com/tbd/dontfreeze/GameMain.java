package com.tbd.dontfreeze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Main class of Don't Freeze!
 *
 * Contains the screens of the game (WorldScreen, and in the future, MenuScreen etc) and controls the state of the game.
 *
 * Created by Quasar on 14/06/2015.
 */
public class GameMain extends Game {

	private WorldScreen world;

	/**
	 * Creates a new instance of GameMain.
	 */
	public GameMain() {
	}

	@Override
	public void create () {
		world = new WorldScreen(this);

		setScreen(world);
	}
}
