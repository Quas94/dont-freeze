package com.tbd.dontfreeze;

/**
 * Base class for all Monsters.
 *
 * Created by Quasar on 16/06/2015.
 */
public class Monster implements Entity {

	private float x;
	private float y;

	public Monster() {
	}

	@Override
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
