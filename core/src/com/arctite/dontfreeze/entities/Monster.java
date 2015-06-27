package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.util.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.arctite.dontfreeze.WorldScreen;
import com.arctite.dontfreeze.entities.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.arctite.dontfreeze.util.SaveManager.*;

/**
 * Base class for all Monsters.
 *
 * Created by Quasar on 16/06/2015.
 */
public class Monster implements LiveEntity {

	// timing values for monster random movement
	private static final float MIN_RAND_TIME = 1.0F;
	private static final float MAX_RAND_TIME = 4.0F;

	private static final int BASE_HEALTH = 3;
	private static final float ATTACK_COOLDOWN = 1F;

	/** Connection to world */
	private WorldScreen world;
	private int id;
	private float x;
	private float y;
	private int speed;
	private int width;
	private int height;
	private Direction dir;
	private Action action;
	private AnimationManager animations;

	/** Movement "AI" stuff */
	/** Fields related to randomised movement */
	private Random random;
	private boolean moving;
	private float timeRemaining; // can be for idle time left, or move time left

	/** Attacking / aggression related stuff */
	/** Whether or not this Monster is currently aggressive toward the player */
	private boolean aggressive; // @TODO implement de-aggro if not touched for a long time?
	/** How long since the last melee attack finished. Used to give monsters a bit of cooldown between attacks */
	private float lastMeleeTime;
	/** How long since the start of the latest MELEE Action. Used to delay damage after animation start */
	private float meleeTime;
	/** Marks whether or not this melee hit has connected with the Player yet */
	private boolean meleeHit;
	/** Melee attack collision Rectangle */
	private Rectangle meleeCollisionBounds;

	/** Game mechanic related fields */
	private int maxHealth;
	private int health;

	/**
	 * Constructs a new Monster with the given position. The animation state of the m
	 *
	 * @param world the World this monster resides within
	 * @param x starting coordinate x value
	 * @param y starting coordinate y value
	 */
	public Monster(WorldScreen world, int id, float x, float y) {
		// initialise technical fields
		this.world = world;
		// random seed
		this.random = new Random();

		// id info
		this.id = id;
		ResourceInfo info = ResourceInfo.getByTypeAndId(ResourceInfo.Type.ENTITY, id);

		this.x = x;
		this.y = y;
		this.speed = info.getSpeed();
		this.width = info.getWidth();
		this.height = info.getHeight();

		this.dir = Direction.LEFT;
		this.action = Action.IDLE_MOVE;

		// create animation manager
		this.animations = new AnimationManager(AnimationManager.MULTI_DIR, this, info);
		// generate random state time so monsters are not all in sync animation-frame wise
		float animationTime = animations.getAnimationTime(); // find time it takes for one loop
		float randomStateTime = random.nextFloat() * animationTime;
		animations.setRandomStateTime(randomStateTime);

		this.moving = false;
		this.timeRemaining = 0;

		this.aggressive = false;
		this.lastMeleeTime = 0;
		this.meleeTime = 0;
		this.meleeHit = false;
		this.meleeCollisionBounds = null;

		// initialise game mechanic related fields
		this.maxHealth = BASE_HEALTH;
		this.health = maxHealth;
	}

	@Override
	public int getId() {
		return id;
	}

	/**
	 * Loads the relevant saved fields into this Monster object, given the save manager.
	 * Assumes that this monster's active flag in the save file has already been checked and is true.
	 *
	 * @param chunkId the chunk id of the map this monster is in
	 * @param saver the save manager
	 * @param name the unique identifier of this monster
	 */
	public void load(String chunkId, SaveManager saver, String name) {
		x = saver.getDataValue(chunkId + MONSTER + name + POSITION_X, Float.class);
		y = saver.getDataValue(chunkId + MONSTER + name + POSITION_Y, Float.class);
		int di = saver.getDataValue(chunkId + MONSTER + name + DIR_IDX, Integer.class);
		health = saver.getDataValue(chunkId + MONSTER + name + HEALTH, Integer.class);
		dir = Direction.getByIndex(di);

		aggressive = saver.getDataValue(chunkId + MONSTER + name + AGGRO, Boolean.class);
	}

	/**
	 * Saves relevant fields from this Player object into the given save manager.
	 *
	 * @param chunkId the chunk id of the map this monster is in
	 * @param saver the save manager
	 * @param name the unique identifier of this monster
	 */
	public void save(String chunkId, SaveManager saver, String name) {
		saver.setDataValue(chunkId + MONSTER + name + POSITION_X, x);
		saver.setDataValue(chunkId + MONSTER + name + POSITION_Y, y);
		saver.setDataValue(chunkId + MONSTER + name + HEALTH, health);
		saver.setDataValue(chunkId + MONSTER + name + DIR_IDX, dir.getIdx());

		saver.setDataValue(chunkId + MONSTER+ name + AGGRO, aggressive);
	}

	@Override
	public int getMaxHealth() {
		return maxHealth;
	}

	@Override
	public int getHealth() {
		return health;
	}

	/**
	 * Inflicts a hit on this monster, dealing damage and possibly putting it into recoil (knockback) mode. Whether the
	 * recoil occurs depends on what action the monster is currently performing: idle - yes, melee attacking - no
	 *
	 * @param from The direction that the hit is coming from
	 */
	@Override
	public void hit(Direction from) {
		// deal 1 point of damage
		health--;
		// change direction to 'from' direction, to make recoil animation make sense
		dir = from;
		// set aggressive flag to true since this monster's gonna want payback
		aggressive = true;
		// check for monster expire when recovering from knockback state, in update method
		if (action == Action.IDLE_MOVE) {
			// set action to knocked back
			setAction(Action.KNOCKBACK);
		} else if (action == Action.MELEE) {
			// if monster is already attacking, we don't put it into knockback
		}
		// otherwise the monster is expiring (we do nothing) or already getting hit (we do nothing)
		// OR the monster is already being knocked back (this shouldn't happen though, the player attack animations are
		// both quite long in comparison to the monster's knockback animation
	}

	public void setAggressive(boolean aggressive) {
		this.aggressive = aggressive;
	}

	public boolean getMeleeHit() {
		return meleeHit;
	}

	public void setMeleeHit() {
		this.meleeHit = true;
	}

	public boolean getMeleeCanHit() {
		return meleeTime >= LiveEntity.MELEE_ATTACK_DELAY;
	}

	/**
	 * Starts a melee attack.
	 */
	private void meleeAttack() {
		meleeHit = false;
		meleeTime = 0;
		float rx = x;
		float ry = y;
		int rw = width / 2;
		int rh = height / 2;
		switch (dir) {
			case LEFT:
				rx -= width / 4;
				ry += height / 4;
				break;
			case RIGHT:
				rx += width / 4 * 3;
				ry += height / 4;
				break;
			case UP:
				rx += width / 8;
				ry += height / 8 * 5;
				rw = width / 4 * 3;
				rh = height / 4 * 3;
				break;
			case DOWN:
				rx += width / 8;
				ry -= height / 8 * 3;
				rw = width / 4 * 3;
				rh = height / 4 * 3;
				break;
		}
		meleeCollisionBounds = new Rectangle(rx, ry, rw, rh);
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
		// collision of snow baby against terrain - use small bounds just like player, near base of snow baby
		return new Rectangle(x + (width / 4), y, width / 2, height / 4);
	}

	@Override
	public Rectangle getAttackCollisionBounds() {
		return meleeCollisionBounds;
	}

	@Override
	public Rectangle getDefenseCollisionBounds() {
		float rx = x + width / 4;
		float ry = y;
		int rw = width / 2;
		int rh = height / 10 * 7;
		return new Rectangle(rx, ry, rw, rh);
	}

	@Override
	public void update(float delta, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
		// @TODO remove this if-condition after implementing different speeds for different animation types per manager
		if (action == Action.EXPIRING) { animations.update(delta / 5); } else if (action == Action.KNOCKBACK) { animations.update(delta / 1.5F); } else

		// update animation
		animations.update(delta);

		// add to attack counters
		meleeTime += delta;
		lastMeleeTime += delta;

		// calculate distance we can move, if we choose to move
		float dist = delta * speed;

		if (health <= 0 && action!= Action.KNOCKBACK && action != Action.EXPIRING) {
			setAction(Action.EXPIRING);
			SoundManager.playSound(SoundManager.SoundInfo.MONSTER_DEATH);
		} else if (action == Action.KNOCKBACK) { // update the knockback
			if (animations.isComplete()) {
				setAction(Action.IDLE_MOVE);
				timeRemaining = 0; // prompt new random movement
				moving = false;
			}
		} else if (action == Action.MELEE) { // this monster is currently attacking
			if (animations.isComplete()) { // we just finished the attack animation
				lastMeleeTime = 0; // time since last melee attack finished: 0 ms
				setAction(Action.IDLE_MOVE);
			}
		} else if (action == Action.IDLE_MOVE) { // update movement
			// @TODO make the monster less stupid and not get stuck behind obstacles - pathfinding algorithm
			if (aggressive) { // aggressive and can attack
				// calculate player and this monster's centre positions
				Player player = world.getPlayer();
				int pw = player.getWidth();
				int ph = player.getHeight();
				float pcx = player.getX() + (pw / 2);
				float pcy = player.getY() + (ph / 2);
				float cx = x + (width / 2);
				float cy = y + (height / 2);
				// calculate differences
				float diffX = pcx - cx; // positive value = player on right, negative value = player on left
				float diffY = pcy - cy; // positive value = player above, negative value = player below
				float absDiffX = Math.abs(diffX);
				float absDiffY = Math.abs(diffY);
				float rangeX = (width / 4) + (pw / 4) + 5; // 5 is just some arbitrary extra distance
				float rangeY = ph / 3.5F;
				boolean inRangeX = absDiffX < rangeX;
				boolean inRangeY = absDiffY < rangeY;
				// @TODO think of a way to make the monster-chase-player algorithm less rigid (ie. not always prioritize
				// one orientation over the other)
				if (inRangeX && inRangeY) { // within range to start attacking
					if (lastMeleeTime >= ATTACK_COOLDOWN) { // if attack is off cooldown
						setAction(Action.MELEE); // assume facing correct direction due to latest moves
						meleeAttack();
					}
					// otherwise we just stay in IDLE_MOVE until attack comes off cooldown
				} else { // we need to move a bit more
					boolean moved = false; // marks if horizontal move was successful (if attempted), false (if not attempted)
					if (!inRangeX) { // move on horizontal plane
						if (diffX > 0) { // player on the right
							dir = Direction.RIGHT;
							moved = tryMove(dist, rects, polys);
						} else if (diffX < 0) { // player on the left
							dir = Direction.LEFT;
							moved = tryMove(dist, rects, polys);
						}
					}
					if (!inRangeY && !moved) { // if we haven't moved already (or we tried to horizontally but failed)
						if (diffY > 0) { // player is above
							dir = Direction.UP;
							tryMove(dist, rects, polys); // we don't care about result of tryMove anymore here
						} else { // player is below
							dir = Direction.DOWN;
							tryMove(dist, rects, polys); // nor here
						}
					}
				}
			} else { // while not aggressive, move randomly
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
					tryMove(dist, rects, polys); // we don't care about the result here
				}
			}
		}
		// otherwise, action is probably expiring - we do nothing
	}

	/**
	 * Starts or continues a random movement. This method is called (instead of update()) when this Monster is merely a
	 * decoration on the MenuScreen, as opposed to an active monster within the game world.
	 *
	 * @param delta amount of time that's passed
	 * @param upperBoundY the upper bound on Y (to prevent the monster from travelling onto thin air at the top)
	 */
	public void updateAsDecoration(float delta, float upperBoundY) {
		// update animation
		animations.update(delta);

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
			float dist = delta * speed;

			if (dir == Direction.LEFT) x -= dist;
			else if (dir == Direction.RIGHT) x += dist;
			else if (dir == Direction.UP) y += dist;
			else if (dir == Direction.DOWN) y -= dist;

			boolean inBounds = (x >= 0) && (x <= Gdx.graphics.getWidth() - width) && (y >= 0) && (y <= upperBoundY);

			if (!inBounds) {
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

	/**
	 * Attempts to move in the current direction. After moving the given distance, tests for collision. If collision is
	 * detected, the move is reverted. Returns true if no collision, and false if there was collision and move was
	 * reverted.
	 *
	 * @param dist The distance to move
	 * @param polys The collection of polygons to test for collision against
	 * @param rects The collection of rectangles to test for collision against
	 * @return true if the move was successful, false if the move collided and had to be reverted
	 */
	private boolean tryMove(float dist, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
		if (dir == Direction.LEFT) x -= dist;
		else if (dir == Direction.RIGHT) x += dist;
		else if (dir == Direction.UP) y += dist;
		else if (dir == Direction.DOWN) y -= dist;

		// test collision
		Rectangle collisionBounds = getCollisionBounds();
		ArrayList<Rectangle> collideRects = Collisions.collidesWithRects(collisionBounds, rects);
		ArrayList<RectangleBoundedPolygon> collidePolys = Collisions.collidesWithPolys(collisionBounds, polys);
		boolean collision = (collideRects.size() + collidePolys.size()) > 0;
		boolean inBounds = (x >= 0) && (y >= 0) && (x + width <= world.getWidth()) && (y + height <= world.getHeight());
		if (collision || !inBounds) {
			// if there is a collision, we undo coordinate change and set moving to false
			// the leftover seconds on timeRemaining will be spent idling
			moving = false;
			// the following changes are opposite signs to above, to undo
			if (dir == Direction.LEFT) x += dist;
			else if (dir == Direction.RIGHT) x -= dist;
			else if (dir == Direction.UP) y -= dist;
			else if (dir == Direction.DOWN) y += dist;
			return false;
		}
		return true;
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animations.getCurrentFrame(dir);
		spriteBatch.draw(frame, x, y);
	}
}
