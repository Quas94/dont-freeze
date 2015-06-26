package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.entities.player.Key;

/**
 * Enumeration of Directions that the Player can be facing. Linked to the corresponding Keys responsible for the
 * directions.
 *
 * Created by Quasar on 16/06/2015.
 */
public enum Direction {

	LEFT('l', 0, Key.LEFT),
	RIGHT('r', 1, Key.RIGHT),
	UP('u', 2, Key.UP),
	DOWN('d', 3, Key.DOWN),
	;

	private char chr;
	private int idx;
	private Key key;

	private Direction(char chr, int idx, Key key) {
		this.chr = chr;
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

	public char getChar() {
		return chr;
	}

	/**
	 * Finds the direction, given the corresponding character.
	 *
	 * @param c The corresponding character for the direction
	 * @return The corresponding direction, or null if none were found
	 */
	public static Direction getByChar(char c) {
		for (Direction d : values()) {
			if (d.chr == c) return d;
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
		for (Direction dir : values()) {
			if (dir.key == k) {
				return dir;
			}
		}
		return null;
	}

	/**
	 * Finds the direction, given the index.
	 *
	 * @param i The corresponding index for the direction
	 * @return The corresponding direction, or null if none were found
	 */
	public static Direction getByIndex(int i) {
		for (Direction dir : values()) {
			if (dir.idx == i) {
				return dir;
			}
		}
		return null;
	}

	/**
	 * Given a direction, returns the opposite direction
	 *
	 * @param d the direction to find the opposite of
	 * @return the opposite direction to the one given
	 */
	public static Direction getOpposite(Direction d) {
		switch (d) {
			case LEFT: return RIGHT;
			case RIGHT: return LEFT;
			case UP: return DOWN;
			case DOWN: return UP;
			default: throw new IllegalArgumentException("direction " + d.name() + " has no opposite");
		}
	}
}
