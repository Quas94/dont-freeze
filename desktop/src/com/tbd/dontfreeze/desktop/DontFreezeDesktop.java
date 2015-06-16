package com.tbd.dontfreeze.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tbd.dontfreeze.GameMain;

public class DontFreezeDesktop {

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 480;
		config.width = 640;
		config.title = "pls don't freeze bby";
		new LwjglApplication(new GameMain(), config);
	}

}
