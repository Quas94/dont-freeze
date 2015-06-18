package com.tbd.dontfreeze.entities;

/**
 * Base class for all Monsters.
 *
 * Created by Quasar on 16/06/2015.
 */
public class Monster implements Entity {

	private float x;
	private float y;
	private Direction dir;

	public Monster() {
		this.dir = Direction.LEFT;
	}

	@Override
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Direction getDirection() {
		return dir;
	}
}
