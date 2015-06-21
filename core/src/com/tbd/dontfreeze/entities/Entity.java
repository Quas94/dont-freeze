package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * The Entity interface which is to be implemented by most game objects including the Player and Monster(s).
 *
 * Created by Quasar on 16/06/2015.
 */
public interface Entity {

	public Direction getDirection();

	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects);

	public void render(SpriteBatch spriteBatch);

	public float getX();
	public float getY();

	public int getWidth();
	public int getHeight();

	public Action getAction();

	/**
	 * Gets this Entity's collision bounds.
	 *
	 * The Rectangle object that this method returns is customized, and in most cases is NOT equivalent to a Rectangle
	 * created by Rectangle( getX() , getY() , getWidth() , getHeight() )
	 *
	 * For example, the Player's collision object is only around the feet of the player, whilst its getWidth() and
	 * getHeight() return values equivalent to the width and height of the entire sprite.
	 *
	 * @return the Entity's collision bounds within a Rectangle object
	 */
	public Rectangle getCollisionBounds();
}
