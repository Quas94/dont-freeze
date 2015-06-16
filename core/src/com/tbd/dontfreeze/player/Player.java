package com.tbd.dontfreeze.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.Entity;
import com.tbd.dontfreeze.WorldScreen;

import java.awt.*;

/**
 * Player class for all things related to the fire elemental.
 *
 * Created by Quasar on 14/06/2015.
 */
public class Player implements Entity {

	/** Sprite constants and other constants. @TODO: don't hardcode this stuff */
	private static final int SPRITE_WIDTH = 56;
	private static final int SPRITE_HEIGHT = 82;
	private static final int COLLISION_WIDTH = 20;
	private static final int COLLISION_HEIGHT = 10;
	private static final String PATH = "assets/player.png";
	private static final int SPEED = 160;
	private static final float DIAGONAL_MOVE_RATIO = 0.765F;
	private static final float SCALE = 1F;

	/** Link to the World this player is currently in */
	private WorldScreen world;

	/** Animation related variables */
	private float stateTime;
	private Sprite sprite;

	/** Heat (effective health) of the player */
	private int heat;

	/** Position and dimensions of the player */
	private float x;
	private float y;
	private int width;
	private int height;
	/** The furthest this player can go whilst staying in bounds (leftmost and downmost are zero) */
	private float rightmost;
	private float upmost;

	public Player(WorldScreen world, float x, float y) {
		this.world = world;
		setLocation(x, y);
		this.width = SPRITE_WIDTH;
		this.height = SPRITE_HEIGHT;
		this.upmost = world.getHeight() - height;
		this.rightmost = world.getWidth() - width;

		this.stateTime = 0;

		// load sprite
		Texture tex = new Texture(Gdx.files.internal(PATH));
		this.sprite = new Sprite(tex);
	}

	@Override
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
		boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
		boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
		float dist = delta * SPEED;

		float oldX = x;
		float oldY = y;

		// tentatively update x pos first
		if (leftPressed) x -= dist;
		if (rightPressed) x += dist;
		// check for collisions after updating x
		if (collides(polys, rects)) {
			leftPressed = false;
			rightPressed = false;
		}
		// undo change
		x = oldX;
		// update y pos next
		if (upPressed) y += dist;
		if (downPressed) y -= dist;
		// check for collisions after updating y
		if (collides(polys, rects)) {
			upPressed = false;
			downPressed = false;
		}
		// undo change
		y = oldY;

		// now make actual changes
		if ((leftPressed || rightPressed) && (upPressed || downPressed)) {
			// going diagonally
			dist *= DIAGONAL_MOVE_RATIO;
		}
		if (leftPressed) x -= dist;
		if (rightPressed) x += dist;
		if (upPressed) y += dist;
		if (downPressed) y -= dist;

		// keep within bounds of map
		if (x < 0) x = 0;
		if (x >= rightmost) x = rightmost;
		if (y < 0) y = 0;
		if (y >= upmost) y = upmost;
	}

	public boolean collides(Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		float collisionX = x + ((width - COLLISION_WIDTH) / 2);
		Polygon polyBounds = new Polygon(new float[] { collisionX, y, collisionX + COLLISION_WIDTH, y, collisionX + COLLISION_WIDTH,
				y + COLLISION_HEIGHT, collisionX, y + COLLISION_HEIGHT });
		for (PolygonMapObject polyObj : polys) {
			Polygon poly = polyObj.getPolygon();

			if (Intersector.overlapConvexPolygons(polyBounds, poly)) {
				return true;
			}
		}

		Rectangle rectBounds =  new Rectangle(collisionX, y, COLLISION_WIDTH, COLLISION_HEIGHT);
		for (RectangleMapObject rectObj : rects) {
			Rectangle rect = rectObj.getRectangle();
			if (Intersector.overlaps(rectBounds, rect)) {
				// intersects!
				return true;
			}
		}

		return false;
	}

	public void render(SpriteBatch batch) {
		batch.draw(sprite, x, y, width * SCALE, height * SCALE);
	}

	// @TODO figure out if this is a good value
	private static final float EPSILON = 0.00001F;
	private boolean floatEquals(float a, float b) {
		return Math.abs(a - b) <= EPSILON;
	}
}
