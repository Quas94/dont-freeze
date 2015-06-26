package com.arctite.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.arctite.dontfreeze.util.GameUtil;
import com.arctite.dontfreeze.util.RectangleBoundedPolygon;

import java.util.List;

/**
 * Representation of a projectile, which could be fired by a player or an enemy.
 * (Projectile sender identification has not yet been implemented)
 *
 * Created by Quasar on 19/06/2015.
 */
public class Projectile implements Entity {

	public static final String PREFIX = "p";
	public static final String PREFIX_EXPIRING = "pe";

	private static final int SPRITE_WIDTH = 70;
	private static final int SPRITE_HEIGHT = 60;
	private static final String FILE = "assets/fireball.atlas";
	private static final float FRAME_RATE = 0.1F;
	/** How long it takes for a fireball to expire and completely dissipate */
	private static final float EXPIRE_TIME = 0.5F;

	private static final int SPEED = 200;

	private float x;
	private float y;
	private int width;
	private int height;
	private Direction dir;
	private Action action;

	private AnimationManager animation;
	/** How long this Projectile has been in Action.EXPIRING state for */
	private float expireTime;

	/** How much longer this projectile can travel before it expires */
	private float range;

	/**
	 * Creates a new Projectile with the given location, direction and maximum range.
	 *
	 * @param x starting x coordinate
	 * @param y starting y coordinate
	 * @param dir direction this projectile is travelling in
	 * @param range the range this projectile may travel before expiring
	 */
	public Projectile(float x, float y, Direction dir, int range) {
		this.x = x;
		this.y = y;
		this.width = SPRITE_WIDTH;
		this.height = SPRITE_HEIGHT;

		this.dir = dir;
		// note: projectiles can only be moving or expiring (hitting)
		// @TODO handle projectile action states and frame repeating
		this.action = Action.IDLE_MOVE;
		// only 1 direction for projectiles in the Animation sequence
		this.animation = new AnimationManager(AnimationManager.MULTI_DIR_CLONE, this, FILE, FRAME_RATE);

		this.range = range;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
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
	public Action getAction() {
		return action;
	}

	/**
	 * Updates this Projectile's action.
	 *
	 * @param action the new action to set to
	 */
	public void setAction(Action action) {
		this.action = action;
		if (action == Action.EXPIRING) {
			// @TODO update animation manager with separate framesets for normal travel and explosion
			expireTime = 0;
		}
	}

	/**
	 * Checks whether or not this Projectile has had enough time to complete its expiry animation. This method returning
	 * true would indicate that this Projectile should be removed from the world.
	 *
	 * @return whether this Projectile has completed its expiry animation
	 */
	public boolean expireComplete() {
		// @TODO change this to animation.isComplete() once segmenting of Projectile animation frames is complete
		return expireTime >= EXPIRE_TIME;
	}

	/**
	 * Gets the Rectangle bounding the collision area of this Projectile.
	 *
	 * @return the Rectangle bounding the collision area
	 */
	@Override
	public Rectangle getCollisionBounds() {
		float rx = x;
		float ry = y;
		int rw = width / 3;
		int rh = height / 4;
		switch (dir) {
			case RIGHT:
				rx += width / 2;
				ry += height / 4;
				break;
			case LEFT:
				rx += width / 6;
				ry += height / 4;
				break;
			case UP:
				rx += 20;
				ry += 35;
				rw = height / 4;
				rh = width / 3;
				break;
			case DOWN:
				rx += 20;
				rw = height / 4;
				rh = width / 3;
				break;
		}
		return new Rectangle(rx, ry, rw, rh);
	}

	@Override
	public void update(float delta, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
		// update animation
		animation.update(delta);

		if (action == Action.IDLE_MOVE) {
			// update range
			float dist = delta * SPEED;
			range -= dist;
			if (range <= 0) {
				// no range left, start expiring
				setAction(Action.EXPIRING);
			}

			// move by dist
			if (dir == Direction.LEFT) x -= dist;
			else if (dir == Direction.RIGHT) x += dist;
			else if (dir == Direction.UP) y += dist;
			else if (dir == Direction.DOWN) y -= dist;

			Rectangle collisionBounds = getCollisionBounds();
			List<Rectangle> collideRects = GameUtil.collidesWithRects(collisionBounds, rects);
			List<RectangleBoundedPolygon> collidePolys = GameUtil.collidesWithPolys(collisionBounds, polys);
			if (collideRects.size() + collidePolys.size() > 0) { // collision detected
				setAction(Action.EXPIRING); // end the action
			}

		} else if (action == Action.EXPIRING) {
			// finishing up after hitting terrain or a monster
			expireTime += delta;
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animation.getCurrentFrame(dir);
		if (dir == Direction.LEFT || dir == Direction.RIGHT) {
			// no rotation required for left/right
			spriteBatch.draw(frame, x, y);
		} else {
			// up/down : need sprite rotation
			spriteBatch.draw(frame, x, y, width / 2, height / 2, width, height, 1, 1, -90);
		}
	}
}
