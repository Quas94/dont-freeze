package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

/**
 * Collection of static utility methods for use by Entities.
 *
 * Includes collision detection methods and more.
 *
 * Created by Quasar on 19/06/2015.
 */
public class EntityUtil {

	/**
	 * Checks if the given Entity's collision bounds collide with the given RectangleMapObjects. If a collision is
	 * detected, the underlying Rectangle is returned immediately.
	 *
	 * @param bounds the Entity's collision bounds
	 * @param rects array of RectangleMapObjects to be checked for collision
	 * @return all the Rectangles the entity's collision bounds collide with
	 */
	public static ArrayList<Rectangle> collidesWithRects(Rectangle bounds, Array<RectangleMapObject> rects) {
		ArrayList<Rectangle> collides = new ArrayList<Rectangle>();
		for (RectangleMapObject rectObj : rects) {
			Rectangle rect = rectObj.getRectangle();
			if (Intersector.overlaps(bounds, rect)) {
				collides.add(rect);
			}
		}

		return collides;
	}

	/**
	 * Checks if the given Entity's collision bounds collide with the given PolygonMapObjects. If a collision is
	 * detected, the underlying Polygon is returned immediately.
	 *
	 * @param bounds the Entity's collison bounds
	 * @param polys array of PolygonMapObjects to be checked for collison
	 * @return all the Polygons the entity's collision bounds collide with
	 */
	public static ArrayList<Polygon> collidesWithPolys(Rectangle bounds, Array<PolygonMapObject> polys) {
		ArrayList<Polygon> collides = new ArrayList<Polygon>();
		Polygon polyBounds = new Polygon(new float[] { bounds.x, bounds.y, bounds.x + bounds.width, bounds.y,
				bounds.x + bounds.width, bounds.y + bounds.height, bounds.x, bounds.y + bounds.height });

		for (PolygonMapObject polyObj : polys) {
			Polygon poly = polyObj.getPolygon();
			if (Intersector.overlapConvexPolygons(polyBounds, poly)) {
				collides.add(poly);
			}
		}
		return collides;
	}

	/**
	 * Checks whether the given Entity bounds collides with the given Shape2D (Rectangle or Polygon).
	 * The Shape2D can ONLY be a Rectangle or Polygon, otherwise IllegalArgumentException is thrown.
	 *
	 * @param rect the Entity bound
	 * @param shape either a Rectangle or Polygon to check for collision against
	 * @return whether the two given shapes collide
	 */
	public static boolean collidesShapes(Rectangle rect, Shape2D shape) {
		if (shape instanceof Rectangle) {
			Rectangle r = (Rectangle) shape;
			return Intersector.overlaps(rect, r);
		} else if (shape instanceof Polygon) {
			Polygon p = (Polygon) shape;
			Polygon polyBounds = new Polygon(new float[] { rect.x, rect.y, rect.x + rect.width, rect.y,
					rect.x + rect.width, rect.y + rect.height, rect.x, rect.y + rect.height });
			return Intersector.overlapConvexPolygons(p, polyBounds);
		} else {
			throw new IllegalArgumentException("shape can only be Rectangle of Polygon, but is " + shape.getClass().toString());
		}
	}

	/**
	 * Checks whether the point given by the x and y coordinates is within the bounds given by the Rectangle.
	 *
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param bounds the Rectangle defining the bounds
	 * @return whether the point is inside the bounds
	 */
	public static boolean isPointInBounds(float x, float y, Rectangle bounds) {
		return x >= bounds.x && y >= bounds.y && x <= bounds.x + bounds.width && y <= bounds.y + bounds.height;
	}

	private EntityUtil() {
	}
}
