package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.util.ResourceInfo;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * Representation of a projectile, which could be fired by a player or an enemy.
 * (Projectile sender identification has not yet been implemented)
 *
 * Created by Quasar on 19/06/2015.
 */
public class Projectile implements Entity {

	/** How deep projectiles can drive into obstacles/monsters, used as param for setCollided() method */
	public static final int COLLISION_PENETRATION = 20;

	private Entity owner;
	private float x;
	private float y;
	private int speed;
	private int width;
	private int height;
	private Direction dir;
	private Action action;

	private AnimationManager animation;

	/** How much longer this projectile can travel before it expires */
	private float range;
	/** Whether this projectile has collided with something */
	private boolean collided;
	/** Whether or not the update() method has been called on this projectile yet (flag for just created) */
	private boolean updated;
	/** Whether or not this projectile was spawned in a colliding obstacle AND has continued to be blocked since */
	private boolean spawnBlocked;
	/** Distance travelled since spawning */
	private float travelled;

	/**
	 * Creates a new Projectile with the given location, direction and maximum range.
	 *
	 * @param owner the Entity that created this Projectile
	 * @param x starting x coordinate
	 * @param y starting y coordinate
	 * @param dir direction this projectile is travelling in
	 * @param range the range this projectile may travel before expiring
	 */
	public Projectile(LiveEntity owner, float x, float y, Direction dir, int range) {
		this.owner = owner;
		ResourceInfo info = ResourceInfo.getByTypeAndId(ResourceInfo.Type.PROJECTILE, owner.getId());

		this.x = x;
		this.y = y;
		this.speed = info.getSpeed();
		this.width = info.getWidth();
		this.height = info.getHeight();

		this.dir = dir;
		// note: projectiles can only be  initialising, looping, or expiring
		this.action = Action.INITIALISING;
		// only 1 direction for projectiles in the Animation sequence
		this.animation = new AnimationManager(AnimationManager.MULTI_DIR_CLONE, this, info);

		this.range = range;
		this.collided = false;
		this.updated = false;
		this.spawnBlocked = false;

		// travelled starting value varies with direction (since collision box has coordinates at bottom left corner)
		this.travelled = 0;
		float offset = getCollisionBounds().getWidth();
		if (dir == Direction.RIGHT || dir == Direction.UP) {
			travelled = -offset; // flip offset for right/up, since they have more distance to cover than the other 2 dirs
		}
	}

	/**
	 * Gets the id of this Projectile, which is equivalent to this Projectile's owner Entity's id
	 *
	 * @return the id of this projectile
	 */
	@Override
	public int getId() {
		return owner.getId();
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
		animation.updateAction(action);
	}

	/**
	 * Notifies this projectile that it has collided with an obstacle or monster. This method just sets range to the
	 * COLLISION_PENETRATION constant, and the update() method will set action to expiring when that range runs out.
	 */
	public void setCollided() {
		this.collided = true;
		if (range > COLLISION_PENETRATION) {
			range = COLLISION_PENETRATION; // don't change if remaining range before the collision was already lower
		}
	}

	/**
	 * Checks whether or not this projectile has collided into something yet.
	 *
	 * @return whether this projectile has collided
	 */
	public boolean hasCollided() {
		return collided;
	}

	/**
	 * Checks whether or not this Projectile has had enough time to complete its expiry animation. This method returning
	 * true would indicate that this Projectile should be removed from the world.
	 *
	 * @return whether this Projectile has completed its expiry animation
	 */
	public boolean expireComplete() {
		return (action == Action.EXPIRING) && animation.isComplete();
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
	public void update(float delta, boolean paused, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
		// update animation
		animation.update(delta);

		if (action == Action.LOOPING || action == Action.INITIALISING) { // move only while looping or init, not expire
			if (!paused) { // movement: only do this if game not paused
				// update range
				float dist = delta * speed;
				range -= dist;
				travelled += dist; // update travelled flag as well as range
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
				List<Rectangle> collideRects = Collisions.collidesWithRects(collisionBounds, rects);
				List<RectangleBoundedPolygon> collidePolys = Collisions.collidesWithPolys(collisionBounds, polys);
				boolean collisionDetected = (collideRects.size() + collidePolys.size() > 0);
				if (dir == Direction.LEFT || dir == Direction.RIGHT) { // only horizontal has the spawnblock checking
					if (collisionDetected) {
						if (!updated) {
							// first update, just set spawnBlocked and updated flags and don't do anything else
							spawnBlocked = true;
							updated = true;
						} else if (spawnBlocked && travelled >= COLLISION_PENETRATION) {
							// spawned in block and has been blocked fulltime until now, and travelled enough
							setAction(Action.EXPIRING);
						} else if (!spawnBlocked) { // if not spawnblocked, set collided (this is a "normal" projectile)
							setCollided();
						}
					} else if (spawnBlocked) { // no collision detected and spawnBlocked
						spawnBlocked = false; // set spawnBlocked flag to false because we haven't been non-stop blocked
					}
				} else { // vertical projectiles is exactly the same as the original algorithm
					if (collisionDetected) {
						setAction(Action.EXPIRING); // set expiring immediately - no penetration
					}
				}
			}
			if (action == Action.INITIALISING && animation.isComplete()) { // check if we should change init to loop
				setAction(Action.LOOPING);
			}
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
