package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.WorldScreen;

import java.util.Random;

/**
 * Base class for all Monsters.
 *
 * Created by Quasar on 16/06/2015.
 */
public class Monster implements Entity {

	private static final String PATH = "assets/snowbaby.atlas";
	private static final float FRAME_RATE = 0.1F;
	private static final int SPEED = 60;
	private static final int SPRITE_WIDTH = 45;
	private static final int SPRITE_HEIGHT = 40;

	// timing values for monster random movement
	private static final float MIN_RAND_TIME = 1.0F;
	private static final float MAX_RAND_TIME = 4.0F;

	/** Connection to world */
	private WorldScreen world;

	private float x;
	private float y;
	private int width;
	private int height;
	private Direction dir;
	private AnimationSequence animations;

	/** Randomised movement "AI" stuff */
	private Random random;
	private boolean moving;
	private float timeRemaining; // can be for idle time left, or move time left

	public Monster(WorldScreen world, float x, float y) {
		this.world = world;

		this.x = x;
		this.y = y;
		this.width = SPRITE_WIDTH;
		this.height = SPRITE_HEIGHT;

		this.dir = Direction.LEFT;
		// monsters can only face LEFT or RIGHT for now
		Direction[] dirs = new Direction[] { Direction.LEFT, Direction.RIGHT };
		this.animations = new AnimationSequence(this, PATH, FRAME_RATE, dirs);

		this.random = new Random();
		this.moving = false;
		this.timeRemaining = 0;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Direction getDirection() {
		return dir;
	}

	@Override
	public Rectangle getCollisionBounds() {
		return new Rectangle(x, y, width, height);
	}

	@Override
	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		// update animation
		animations.update(delta);

		// update randomised movement
		if (timeRemaining <= 0) { // take further action if timeRemaining runs out
			timeRemaining = random.nextFloat() * (MAX_RAND_TIME - MIN_RAND_TIME) + MIN_RAND_TIME;
			moving = !moving; // flip moving
			if (moving) {
				// if we're now moving we need to generate a new direction
				int dirIndex = random.nextInt(Direction.values().length);
				dir = Direction.getByIndex(dirIndex);
			}
		}
		// update time remaining
		timeRemaining -= delta;
		// now actually move
		if (moving) {
			float dist = delta * SPEED;
			if (dir == Direction.LEFT) x -= dist;
			else if (dir == Direction.RIGHT) x += dist;
			else if (dir == Direction.UP) y += dist;
			else if (dir == Direction.DOWN) y -= dist;

			// test collision
			boolean collision = EntityUtil.collidesTerrain(this, polys, rects);
			boolean inBounds = (x >= 0) && (y >= 0) && (x + SPRITE_WIDTH <= world.getWidth()) && (y + SPRITE_HEIGHT <= world.getHeight());
			if (collision || !inBounds) {
				// if there is a collision, we undo coordinate change and set moving to false
				// the leftover seconds on timeRemaining will be spent idling
				moving = false;
				// the following changes are opposite signs to above, to undo
				if (dir == Direction.LEFT) x += dist;
				else if (dir == Direction.RIGHT) x -= dist;
				else if (dir == Direction.UP) y -= dist;
				else if (dir == Direction.DOWN) y += dist;
			}
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		// @TODO: implement up/down directions for monsters (need alex's sprites first, though) so that the following
		// 3 lines of hacky code is no longer required
		Direction tempDir = dir;
		if (tempDir == Direction.UP) tempDir = Direction.LEFT;
		else if (tempDir == Direction.DOWN) tempDir = Direction.RIGHT;

		TextureRegion frame = animations.getCurrentFrame(tempDir);
		spriteBatch.draw(frame, x, y);
	}
}
