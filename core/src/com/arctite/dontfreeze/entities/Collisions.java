package com.arctite.dontfreeze.entities;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of static utility methods for use by Entities.
 *
 * Includes collision detection methods and more.
 *
 * Created by Quasar on 19/06/2015.
 */
public class Collisions {

	/**
	 * Checks if the given Entity's collision bounds collide with the given RectangleMapObjects. If a collision is
	 * detected, the underlying Rectangle is returned immediately.
	 *
	 * @param bounds the Entity's collision bounds
	 * @param rects array of RectangleMapObjects to be checked for collision
	 * @return all the Rectangles the entity's collision bounds collide with
	 */
	public static ArrayList<Rectangle> collidesWithRects(Rectangle bounds, List<Rectangle> rects) {
		ArrayList<Rectangle> collides = new ArrayList<Rectangle>();
		for (Rectangle rect : rects) {
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
	 * @param polys list of RectangleBoundedPolygons to be checked for collision
	 * @return all the Polygons the entity's collision bounds collide with
	 */
	public static ArrayList<RectangleBoundedPolygon> collidesWithPolys(Rectangle bounds, List<RectangleBoundedPolygon> polys) {
		ArrayList<RectangleBoundedPolygon> collides = new ArrayList<RectangleBoundedPolygon>();
		Polygon polyBounds = new Polygon(new float[] { bounds.x, bounds.y, bounds.x + bounds.width, bounds.y,
				bounds.x + bounds.width, bounds.y + bounds.height, bounds.x, bounds.y + bounds.height });

		for (RectangleBoundedPolygon rbp : polys) {
			Rectangle boundRect = rbp.getBoundingRectangle();
			if (Intersector.overlaps(bounds, boundRect)) {
				List<Polygon> subPolys = rbp.getSubPolygons();
				for (Polygon sub : subPolys) {
					if (Intersector.overlapConvexPolygons(polyBounds, sub)) {
						collides.add(rbp);
						break; // add original polygon that this sub was a part of, and break
					}
				}
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
		} else if (shape instanceof RectangleBoundedPolygon) {
			RectangleBoundedPolygon rbp = (RectangleBoundedPolygon) shape;
			Polygon polyBounds = new Polygon(new float[] { rect.x, rect.y, rect.x + rect.width, rect.y,
					rect.x + rect.width, rect.y + rect.height, rect.x, rect.y + rect.height });
			List<Polygon> subPolys = rbp.getSubPolygons();
			for (Polygon sub : subPolys) {
				if (Intersector.overlapConvexPolygons(sub, polyBounds)) {
					return true;
				}
			}
			return false;
		} else {
			throw new IllegalArgumentException("shape can only be Rectangle or RectangleBoundedPolygon, but is "
					+ shape.getClass().toString());
		}
	}

	private Collisions() {
	}
}
