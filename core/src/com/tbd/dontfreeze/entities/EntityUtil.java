package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Collection of static utility methods for use by Entities.
 *
 * Includes collision detection methods and more.
 *
 * Created by Quasar on 19/06/2015.
 */
public class EntityUtil {

	/**
	 * Checks whether the Rectangle/Polygon(Rectangle) formed by the four given points collides with any of the given
	 * PolygonMapObjects and RectangleMapObjects.
	 *
	 * @param x x-coordinate to form the Rectangle/Polygon
	 * @param y y-coordinate to form the Rectangle/Polygon
	 * @param width width of the Rectangle/Polygon
	 * @param height height of the Rectangle/Polygon
	 * @param rects array of RectangleMapObjects to be checked for collision
	 * @param polys array of PolygonMapObjects to be checked for collision
	 * @return whether any collision is detected
	 */
	public static boolean collides(float x, float y, int width, int height, Array<PolygonMapObject> polys,
							Array<RectangleMapObject> rects) {

		Polygon polyBounds = new Polygon(new float[] { x, y, x + width, y, x + width, y + height, x, y + height });
		Rectangle rectBounds =  new Rectangle(x, y, width, height);
		for (PolygonMapObject polyObj : polys) {
			Polygon poly = polyObj.getPolygon();
			if (Intersector.overlapConvexPolygons(polyBounds, poly)) {
				return true;
			}
		}
		for (RectangleMapObject rectObj : rects) {
			Rectangle rect = rectObj.getRectangle();
			if (Intersector.overlaps(rectBounds, rect)) {
				return true;
			}
		}

		return false;
	}

	private EntityUtil() {
	}
}
