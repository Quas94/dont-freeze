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
public class Monster implements LiveEntity {

	private static final String PATH = "assets/snowbaby.atlas";
	private static final float FRAME_RATE = 0.15F;
	private static final int SPEED = 30;
	private static final int SPRITE_WIDTH = 55;
	private static final int SPRITE_HEIGHT = 50;

	// timing values for monster random movement
	private static final float MIN_RAND_TIME = 1.0F;
	private static final float MAX_RAND_TIME = 4.0F;

	private static final int BASE_HEALTH = 10;

	/** Connection to world */
	private WorldScreen world;
	private float x;
	private float y;
	private int width;
	private int height;
	private Direction dir;
	private Action action;
	private AnimationManager animations;

	/** Randomised movement "AI" stuff */
	private Random random;
	private boolean moving;
	private float timeRemaining; // can be for idle time left, or move time left

	/** Game mechanic related fields */
	private int maxHealth;
	private int health;

	public Monster(WorldScreen world, float x, float y) {
		// initialise technical fields
		this.world = world;

		this.x = x;
		this.y = y;
		this.width = SPRITE_WIDTH;
		this.height = SPRITE_HEIGHT;

		this.dir = Direction.LEFT;
		this.action = Action.IDLE_MOVE;
		this.animations = new AnimationManager(AnimationManager.MULTI_DIR, this, PATH, FRAME_RATE);

		this.random = new Random();
		this.moving = false;
		this.timeRemaining = 0;

		// initialise game mechanic related fields
		this.maxHealth = BASE_HEALTH;
		this.health = maxHealth;
	}

	@Override
	public int getMaxHealth() {
		return maxHealth;
	}

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public void hit(Direction from) {
		// deal 1 point of damage
		health--;
		// change direction to 'from' direction, to make recoil animation make sense
		dir = from;
		// @TODO make it so the monster can't get hit-stunned repeatedly forever
		if (action == Action.IDLE_MOVE) {
			if (health > 0) {
				// set action to knocked back
				setAction(Action.KNOCKBACK);
			} else {
				// health <= 0 - dead!
				setAction(Action.EXPIRING);
			}
		}
		// otherwise the monster is expiring (we do nothing) or already getting hit (we do nothing)
	}

	/**
	 * Checks whether this Monster has completed its expire animation and is ready to be removed from the world
	 *
	 * @return if we can remove this monster from the world
	 */
	public boolean expireComplete() {
		return (action == Action.EXPIRING) && (animations.isComplete());
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
	public Action getAction() {
		return action;
	}

	/**
	 * Updates this Monster's action. Updates the animation manager too.
	 *
	 * @param action the new action
	 */
	private void setAction(Action action) {
		this.action = action;
		animations.updateAction(action);
	}

	@Override
	public Rectangle getCollisionBounds() {
		return new Rectangle(x, y, width, height);
	}

	@Override
	public Rectangle getAttackCollisionBounds() {
		return null;
	}

	@Override
	public Rectangle getDefenseCollisionBounds() {
		float rx = x + width / 4;
		float ry = y;
		int rw = width / 2;
		int rh = height / 5 * 4;
		return new Rectangle(rx, ry, rw, rh);
	}

	@Override
	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		// @TODO remove this if-condition after implementing different speeds for different animation types per manager
		if (action == Action.EXPIRING) { animations.update(delta / 5); } else

		// update animation
		animations.update(delta);

		if (action == Action.KNOCKBACK) { // update the knockback
			// if knockback is complete, set mode back to IDLE_MOVE
			if (animations.isComplete()) {
				setAction(Action.IDLE_MOVE);
				timeRemaining = 0; // prompt new random movement
				moving = false;
			}
		} else if (action == Action.IDLE_MOVE) { // update randomised movement
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
		// otherwise, action is probably expiring - we do nothing
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animations.getCurrentFrame(dir);
		spriteBatch.draw(frame, x, y);
	}
}
