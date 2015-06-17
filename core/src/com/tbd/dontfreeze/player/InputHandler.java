package com.tbd.dontfreeze.player;

import com.badlogic.gdx.InputAdapter;

import java.util.HashMap;

/**
 * Handles peripheral input that is relevant to the game.
 *
 * Manages order in which keys were pressed.
 *
 * Created by Quasar on 17/06/2015.
 */
public class InputHandler extends InputAdapter {

	/** <keycode, true for down/false for up> */
	private HashMap<Integer, Boolean> keyStatus;

	/** When two keys are down, this differentiates between which is the more recent keypress */
	private Key newKey;
	private Key oldKey;

	public InputHandler() {
		this.keyStatus = new HashMap<Integer, Boolean>();

		this.newKey = Key.NO_KEY;
		this.oldKey = Key.NO_KEY;
	}

	/**
	 * Gets the key which has highest priority on deciding which Direction the Player will face
	 *
	 * @return the highest priority Key
	 */
	public Key getNewKey() {
		return newKey;
	}

	/**
	 * Checks whether a Key is currently being held down.
	 *
	 * This method ONLY works for keys that are a part of the Key enumeration class.
	 *
	 * @param key the key to check
	 * @return whether the key is currently held down
	 */
	public boolean isKeyDown(Key key) {
		int code = key.getCode();
		if (keyStatus.containsKey(code)) {
			return keyStatus.get(code);
		}
		return false;
	}

	@Override
	public boolean keyDown(int code) {
		return setKeyDown(code, true);
	}

	@Override
	public boolean keyUp(int code) {
		return setKeyDown(code, false);
	}

	private boolean setKeyDown(int code, boolean down) {
		for (Key k : Key.values()) {
			if (k.getCode() == code) {
				// set order
				int newCode = newKey.getCode();
				int oldCode = oldKey.getCode();
				if (down) { // pressing down
					if (newCode != code && oldCode != code) { // completely new key
						oldKey = newKey;
						newKey = k;
					}
					// else do nothing because this shouldn't happen - due to release
				} else { // releasing
					if (newCode == code) {
						newKey = oldKey;
						oldKey = Key.NO_KEY;
					} else if (oldCode == code) {
						oldKey = Key.NO_KEY;
					}
				}

				// update status
				keyStatus.put(code, down);
				return true;
			}
		}
		return false;
	}
}
