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
	 * Checks whether the given Entity collides with any of the given PolygonMapObjects and RectangleMapObjects.
	 *
	 * @param e the Entity that is being checked for collision against the map objects
	 * @param rects array of RectangleMapObjects to be checked for collision
	 * @param polys array of PolygonMapObjects to be checked for collision
	 * @return whether any collision is detected
	 */
	public static boolean collidesTerrain(Entity e, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		Rectangle bounds = e.getCollisionBounds();

		Polygon polyBounds = new Polygon(new float[] { bounds.x, bounds.y, bounds.x + bounds.width, bounds.y,
				bounds.x + bounds.width, bounds.y + bounds.height, bounds.x, bounds.y + bounds.height });
		for (PolygonMapObject polyObj : polys) {
			Polygon poly = polyObj.getPolygon();
			if (Intersector.overlapConvexPolygons(polyBounds, poly)) {
				return true;
			}
		}
		for (RectangleMapObject rectObj : rects) {
			Rectangle rect = rectObj.getRectangle();
			if (Intersector.overlaps(bounds, rect)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the two given Entities have collision bounds that are overlapping.
	 *
	 * @param e1 the first Entity
	 * @param e2 the second Entity
	 * @return whether their collision bounds overlap
	 */
	public static boolean collidesEntity(Entity e1, Entity e2) {
		return Intersector.overlaps(e1.getCollisionBounds(), e2.getCollisionBounds());
	}

	private EntityUtil() {
	}
}
