package com.tbd.dontfreeze.player;

/**
 * Enumeration of Directions that the Player can be facing. Linked to the corresponding Keys responsible for the
 * directions.
 *
 * Created by Quasar on 16/06/2015.
 */
public enum Direction {

	UP('u', 0, Key.UP),
	DOWN('d', 1, Key.DOWN),
	LEFT('l', 2, Key.LEFT),
	RIGHT('r', 3, Key.RIGHT);

	private char ext;
	private int idx;
	private Key key;

	private Direction(char ext, int idx, Key key) {
		this.ext = ext;
		this.idx = idx;
		this.key = key;
	}

	/**
	 * Gets the index of this direction. Responsible for ordering of indices of the Animations array in the Player
	 * class.
	 *
	 * @return index of this direction
	 */
	public int getIdx() {
		return idx;
	}

	/**
	 * Finds the direction, given the corresponding character.
	 *
	 * @param c The corresponding character for the direction
	 * @return The corresponding direction, or null if none were found
	 */
	public static Direction getByChar(char c) {
		for (Direction d : values()) {
			if (d.ext == c) return d;
		}
		return null;
	}

	/**
	 * Finds the direction, given the corresponding Key.
	 *
	 * @param k The corresponding key for the direction
	 * @return The corresponding direction, or null if none were found
	 */
	public static Direction getByKey(Key k) {
		if (k == Key.LEFT) return LEFT;
		if (k == Key.RIGHT) return RIGHT;
		if (k == Key.UP) return UP;
		if (k == Key.DOWN) return DOWN;
		return null;
	}
}
