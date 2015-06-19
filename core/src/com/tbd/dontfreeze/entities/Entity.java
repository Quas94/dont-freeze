package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Array;

/**
 * The Entity interface which is to be implemented by most game objects including the Player and Monster(s).
 *
 * Created by Quasar on 16/06/2015.
 */
public interface Entity {

	public void setLocation(float x, float y);

	public Direction getDirection();

	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects);

	public void render(SpriteBatch spriteBatch);
}
