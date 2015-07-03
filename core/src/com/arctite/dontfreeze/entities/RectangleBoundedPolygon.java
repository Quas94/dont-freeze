package com.arctite.dontfreeze.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import net.dermetfan.gdx.math.GeometryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * As the name would indicate, this class contains a Polygon wrapped within a Rectangle. The external Rectangle is
 * checked for collision first, if that rough check detects potential collision, the internal Polygon is then checked.
 * This should greatly improve performance on maps with large numbers of Polygons as rectangular bound collision
 * detection is much cheaper than complex polygon collision detection.
 *
 * The Polygon is represented by a List of polygons, since concave polygons are broken up into two or more concave
 * polygons within this class' constructor.
 *
 * Created by Quasar on 26/06/2015.
 */
public class RectangleBoundedPolygon implements Shape2D {

	/** List of sub-polygons that this RectangleBoundedPolygon contains. If convex, this list will only have size 1 */
	private List<Polygon> subPolygons;

	/** The bounding Rectangle around the original Polygon */
	private Rectangle boundingRect;

	/**
	 * Creates a new RectangleBoundedPolygon. The constructor determines whether or not the given Polygon is convex or
	 * concave, and subdivides into two or more convex polygons if concave. The constructor also determines and creates
	 * the bounding Rectangle for the original polygon and saves it as a field.
	 *
	 * @param polygon The initial Polygon object to wrap (and decompose if concave)
	 */
	public RectangleBoundedPolygon(Polygon polygon) {
		// determine sub-polygons
		ArrayList<Polygon> initial = new ArrayList<Polygon>();
		if (polygon.getVertices().length == 6 || GeometryUtils.isConvex(polygon.getVertices())) { // triangle or convex
			initial.add(polygon);
		} else { // concave - need to decompose into two or more convexes
			Polygon[] polygons = GeometryUtils.decompose(polygon);
			for (Polygon decomposed : polygons) {
				initial.add(decomposed);
			}
		}
		// set subPolygons to unmodifiable version of created list (initial)
		this.subPolygons = Collections.unmodifiableList(initial);

		// determine bounding Rectangle of original polygon
		TreeSet<Float> xs = new TreeSet<Float>();
		TreeSet<Float> ys = new TreeSet<Float>();
		float[] vertices = polygon.getTransformedVertices(); // use transformed vertices of original polygon
		for (int i = 0; i < vertices.length; i++) {
			boolean even = (i % 2 == 0); // zero remainder when divided by 2 means index is even
			if (even) xs.add(vertices[i]); // even indices are x coords
			else ys.add(vertices[i]); // odd indices are y coords
		}
		float minX = xs.first();
		float maxX = xs.last();
		float minY = ys.first();
		float maxY = ys.last();
		float width = maxX - minX;
		float height = maxY - minY;
		this.boundingRect = new Rectangle(minX, minY, width, height);
	}

	/**
	 * Gets the underlying list of sub-polygons representing the original polygon. If the original polygon was convex,
	 * the list will contain just the original polygon itself. If the original polygon was concave, the list will
	 * contain two or more convex polygons which combine to form the original polygon.
	 *
	 * @return the list of sub-polygons (which is unmodifiable)
	 */
	public List<Polygon> getSubPolygons() {
		return subPolygons;
	}

	/**
	 * Gets the bounding Rectangle of the original Polygon. This Rectangle will be used for rough collision detection.
	 * If the rough detections finds possible collision, then the accurate (and more expensive) Polygon collision
	 * will be undertaken.
	 *
	 * @return the bounding Rectangle
	 */
	public Rectangle getBoundingRectangle() {
		return boundingRect;
	}
}
