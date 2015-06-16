package com.tbd.dontfreeze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameMain extends Game {

	private WorldScreen world;

	public GameMain() {
	}

	@Override
	public void create () {
		world = new WorldScreen(this);

		setScreen(world);
	}
}
