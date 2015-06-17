package com.tbd.dontfreeze.player;

/**
 * Created by Quasar on 16/06/2015.
 */
public enum Direction {

	UP('u', 0),
	DOWN('d', 1),
	LEFT('l', 2),
	RIGHT('r', 3);

	private char ext;
	private int idx;

	private Direction(char ext, int idx) {
		this.ext = ext;
		this.idx = idx;
	}

	public char getAbbrv() {
		return ext;
	}

	public int getIdx() {
		return idx;
	}

	public static Direction getByChar(char c) {
		for (Direction d : values()) {
			if (d.ext == c) return d;
		}
		return null;
	}

	public static Direction getByKey(Key k) {
		if (k == Key.LEFT) return LEFT;
		if (k == Key.RIGHT) return RIGHT;
		if (k == Key.UP) return UP;
		if (k == Key.DOWN) return DOWN;
		return null;
	}
}
