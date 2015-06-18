package com.tbd.dontfreeze.entities;

/**
 * The Entity interface which is to be implemented by most game objects including the Player and Monster(s).
 *
 * Created by Quasar on 16/06/2015.
 */
public interface Entity {

	public void setLocation(float x, float y);

	public Direction getDirection();
}
