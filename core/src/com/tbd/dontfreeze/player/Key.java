package com.tbd.dontfreeze.player;

import com.badlogic.gdx.Input.Keys;

/**
 * An enum expressing Keys that are relevant to the game. Keycode values are copied from LibGDX's Keys class.
 *
 * Created by Quasar on 17/06/2015.
 */
public enum Key {

	UP(Keys.UP),
	DOWN(Keys.DOWN),
	LEFT(Keys.LEFT),
	RIGHT(Keys.RIGHT),
	NO_KEY(-999999)
	;

	private int code;

	private Key(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static Key getByCode(int code) {
		for (Key k : values()) {
			if (k.code == code) {
				return k;
			}
		}
		return null;
	}
}
