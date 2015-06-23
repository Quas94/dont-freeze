package com.tbd.dontfreeze.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tbd.dontfreeze.GameMain;

public class DontFreezeDesktop {

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = GameMain.GAME_WINDOW_HEIGHT;
		config.width = GameMain.GAME_WINDOW_WIDTH;
		config.title = GameMain.GAME_WINDOW_TITLE;
		// config.addIcon(); @TODO add icon
		config.resizable = false; // don't allow resizing
		new LwjglApplication(new GameMain(), config);
	}

}
