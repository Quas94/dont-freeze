package com.tbd.dontfreeze.entities.player;

import com.badlogic.gdx.InputAdapter;

import java.util.HashMap;

/**
 * Handles peripheral input that is relevant to the game.
 *
 * Manages order in which keys were pressed.
 *
 * Created by Quasar on 17/06/2015.
 */
public class WorldInputHandler extends InputAdapter {

	/** <keycode, true for down/false for up> */
	private HashMap<Integer, Boolean> keyStatus;

	/** When two direction keys are down simultaneously, this differentiates between which is the more recent keypress */
	private Key dirNewKey;
	private Key dirOldKey;

	public WorldInputHandler() {
		this.keyStatus = new HashMap<Integer, Boolean>();

		clear();
	}

	/**
	 * Completely clears and (re-)initialises all information stored by this handler.
	 */
	public void clear() {
		keyStatus.clear();
		dirNewKey = Key.NO_KEY;
		dirOldKey = Key.NO_KEY;
	}

	/**
	 * Gets the key which has highest priority on deciding which Direction the Player will face
	 *
	 * @return the highest priority Key
	 */
	public Key getNewKey() {
		return dirNewKey;
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
				// if movement key, relevant to dirOld/dirNew
				if (Key.isMovementKey(k)) {
					// set order
					int newCode = dirNewKey.getCode();
					int oldCode = dirOldKey.getCode();
					if (down) { // pressing down
						if (newCode != code && oldCode != code) { // completely new key
							dirOldKey = dirNewKey;
							dirNewKey = k;
						}
						// else do nothing because this shouldn't happen - due to release
					} else { // releasing
						if (newCode == code) {
							dirNewKey = dirOldKey;
							dirOldKey = Key.NO_KEY;
						} else if (oldCode == code) {
							dirOldKey = Key.NO_KEY;
						}
					}
				}

				// update status of the key
				keyStatus.put(code, down);
				return true;
			}
		}
		return false;
	}
}
