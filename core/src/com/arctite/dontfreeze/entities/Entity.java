package com.arctite.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * The Entity interface which is to be implemented by most game objects including the Player and Monster(s).
 *
 * Created by Quasar on 16/06/2015.
 */
public interface Entity {

	public Direction getDirection();

	/**
	 * Updates this given Entity.
	 *
	 * @param delta time in seconds passed since last frame render
	 * @param paused whether or not the game is currently paused
	 * @param rects list of Rectangle objects to check for collision against
	 * @param polys list of RectangleBoundedPolygon objects to check for collision against
	 */
	public void update(float delta, boolean paused, List<Rectangle> rects, List<RectangleBoundedPolygon> polys);

	public void render(SpriteBatch spriteBatch);

	public float getX();
	public float getY();

	public int getWidth();
	public int getHeight();

	public Action getAction();

	/**
	 * Gets the id of this Entity. (Player = 0, Monsters have ids ranging from 1 upwards)
	 *
	 * @return the id of this Entity
	 */
	public int getId();

	/**
	 * Gets this Entity's main collision bounds.
	 * The main collision bounds are representative of this Entity's non-combat interactions, eg. any entity's
	 * interaction with the terrain obstacles, or a Collectable's interaction with a Player picking it up.
	 *
	 * The exception to this is with Projectile, since the projectile's only purpose is to collide. It is the only
	 * Entity whose main collision bounds are combat-related.
	 *
	 * The Rectangle object that this method returns is customized, and in most cases is NOT equivalent to a Rectangle
	 * created by Rectangle(getX(), getY(), getWidth(), getHeight())
	 *
	 * @return the Entity's main collision bounds within a Rectangle object
	 */
	public Rectangle getCollisionBounds();
}
