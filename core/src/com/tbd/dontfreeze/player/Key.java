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

	/** Enum representing no key. Currently only used in InputHandler's newKey and oldKey fields */
	NO_KEY(-999999)
	;

	private int code;

	private Key(int code) {
		this.code = code;
	}

	/**
	 * Gets the keycode for this Key. The keycode values are copied over from the constant values in
	 * com.badlogic.gdx.Input.Keys
	 *
	 * @return The keycode for this key
	 */
	public int getCode() {
		return code;
	}
}
