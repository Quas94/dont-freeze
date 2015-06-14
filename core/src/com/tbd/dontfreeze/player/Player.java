package com.tbd.dontfreeze.player;

/**
 * Player class for all things related to the fire elemental.
 *
 * Created by Quasar on 14/06/2015.
 */
public class Player {

	/** Heat (effective health) of the player */
	private int heat;

	/** Position of the player */
	private float x;
	private float y;

	public Player() {
		// middle of the screen
		this.x = 320;
		this.y = 240;
	}
}
