package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.util.RectangleBoundedPolygon;

import java.util.ArrayList;
import java.util.List;

/**
 * The Entity interface which is to be implemented by most game objects including the Player and Monster(s).
 *
 * Created by Quasar on 16/06/2015.
 */
public interface Entity {

	public Direction getDirection();

	public void update(float delta, List<Rectangle> rects, List<RectangleBoundedPolygon> polys);

	public void render(SpriteBatch spriteBatch);

	public float getX();
	public float getY();

	public int getWidth();
	public int getHeight();

	public Action getAction();

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
