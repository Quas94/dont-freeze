package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.ui.HealthBar;
import com.arctite.dontfreeze.util.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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

	/** Timing values for monster random movement */
	private static final float MIN_RAND_TIME = 1.0F;
	private static final float MAX_RAND_TIME = 4.0F;

	/** Drop aggressiveness at this distance (between player and the monster) */
	private static final float AGGRO_DROP_DISTANCE = 500;
	/** Fade speed */
	private static final float FADE_IN_SPEED = 1.0F; // 1 second to completely fade in
	private static final float FADE_OUT_SPEED = 0.5F; // 2 seconds to completely fade out

	/** Color to tint monster when aggressive */
	private static final float[] TINT = new float[] { 250 / 255F, 200 / 255F, 200 / 255F };

	private static final int BASE_HEALTH = 10;
	private static final float MELEE_COOLDOWN = 1.0F;
	private static final float SPECIAL_COOLDOWN = 3.0F; // 3 second cooldown

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
	private boolean spawning; // whether this monster is spawning (fading in)
	private float alpha; // for fading in (spawning) and out (expiring)

	/** Movement "AI" stuff */
	/** Fields related to randomised movement */
	private Random random;
	private boolean moving;
	private float timeRemaining; // can be for idle time left, or move time left

	/** Attacking / aggression related stuff */
	/** Whether or not this Monster is currently aggressive toward the player */
	private boolean aggressive; // @TODO implement de-aggro if not touched for a long time?
	/** How long since the last melee attack started. Used to give monsters a bit of cooldown between attacks */
	private float lastMeleeTime;
	/** How long since the last special attack launched */
	private float lastSpecialTime;
	/** How long since the last melee OR special attack started/launched */
	private float lastAttackTime;
	/** How long since the start of the latest MELEE Action. Used to delay damage after animation start */
	private float meleeTime;
	/** Marks whether or not this melee hit has connected with the Player yet */
	private boolean meleeHit;
	/** Melee attack collision Rectangle */
	private Rectangle meleeCollisionBounds;
	/** Melee attack range */
	private float meleeRangeX;
	private float meleeRangeY;
	/** Whether or not this monster has special attack capabilities */
	private boolean special;
	/** How long this monster's special attacks can travel */
	private int specialRange;
	/** Special origin box's offset as defined in ResourceInfo */
	private Rectangle specialOriginOffset;

	/** Game mechanic related fields */
	private HealthBar healthBar;

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
		this.alpha = 1.0F;
		this.spawning = false;

		// create animation manager
		this.animations = new AnimationManager(AnimationManager.MULTI_DIR, this, info);
		// generate random state time so monsters are not all in sync animation-frame wise
		float animationTime = animations.getAnimationTime(); // find time it takes for one loop
		float randomStateTime = random.nextFloat() * animationTime;
		animations.setRandomStateTime(randomStateTime);

		this.moving = false;
		this.timeRemaining = 0;

		this.aggressive = false;
		this.lastMeleeTime = MELEE_COOLDOWN;
		this.lastSpecialTime = SPECIAL_COOLDOWN;
		this.lastAttackTime = Math.max(lastMeleeTime, lastSpecialTime);
		this.meleeTime = 0;
		this.meleeHit = false;
		this.meleeCollisionBounds = null;
		this.meleeRangeX = info.getMeleeRangeX();
		this.meleeRangeY = info.getMeleeRangeY();
		this.special = info.canSpecial();
		this.specialRange = info.getSpecialRange();

		this.specialOriginOffset = info.getSpecialOriginOffset();

		// add small arbitrary offset
		meleeRangeX += 5;
		meleeRangeY += 5;

		// initialise health bar
		this.healthBar = new HealthBar(this, x, y, width, 5, BASE_HEALTH, BASE_HEALTH);
		healthBar.setVisible(false); // invisible to begin with
	}

	@Override
	public int getId() {
		return id;
	}

	public WorldScreen getWorld() {
		return world;
	}

	/**
	 * Loads the relevant saved fields into this Monster object, given the save manager.
	 * Assumes that this monster's active flag in the save file has already been checked and is true.
	 *
	 * @param chunkId the chunk id of the map this monster is in
	 * @param name the unique identifier of this monster
	 */
	public void load(String chunkId, String name) {
		SaveManager saver = SaveManager.getSaveManager();
		x = saver.getDataValue(chunkId + MONSTER + name + POSITION_X, Float.class);
		y = saver.getDataValue(chunkId + MONSTER + name + POSITION_Y, Float.class);
		int di = saver.getDataValue(chunkId + MONSTER + name + DIR_IDX, Integer.class);
		dir = Direction.getByIndex(di);
		int health = saver.getDataValue(chunkId + MONSTER + name + HEALTH, Integer.class);
		healthBar.setHealth(health);

		boolean aggro = saver.getDataValue(chunkId + MONSTER + name + AGGRO, Boolean.class);
		setAggressive(aggro);
	}

	/**
	 * Saves relevant fields from this Player object into the given save manager.
	 *
	 * @param chunkId the chunk id of the map this monster is in
	 * @param name the unique identifier of this monster
	 */
	public void save(String chunkId, String name) {
		SaveManager saver = SaveManager.getSaveManager();

		saver.setDataValue(chunkId + MONSTER + name + POSITION_X, x);
		saver.setDataValue(chunkId + MONSTER + name + POSITION_Y, y);
		saver.setDataValue(chunkId + MONSTER + name + HEALTH, healthBar.getHealth());
		saver.setDataValue(chunkId + MONSTER + name + DIR_IDX, dir.getIdx());

		saver.setDataValue(chunkId + MONSTER+ name + AGGRO, aggressive);
	}

	/**
	 * Gets the health bar UI object of this monster. The health bar is also linked to the World's stage.
	 *
	 * @return the monster's health bar object
	 */
	public HealthBar getHealthBar() {
		return healthBar;
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
		healthBar.changeHealth(-1);
		// change direction to 'from' direction, to make recoil animation make sense
		dir = from;
		// set aggressive flag to true since this monster's gonna want payback
		setAggressive(true);
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

	/**
	 * Sets the aggressiveness of this monster.
	 *
	 * @param aggressive whether or not this monster is being set to aggressive
	 */
	public void setAggressive(boolean aggressive) {
		this.aggressive = aggressive;
	}

	/**
	 * Checks the position of the player relative to this monster. If the player is sufficiently far away, this monster
	 * will drop aggro (only if the monster has aggro).
	 *
	 * @param player the player object
	 */
	public void updateAggressive(Player player) {
		if (aggressive) {
			float px = player.getX();
			float py = player.getY();
			double dist = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
			if (dist >= AGGRO_DROP_DISTANCE) {
				setAggressive(false);
			}
		}
	}

	public boolean isAggressive() {
		return aggressive;
	}

	public void setSpawning(boolean spawning) {
		this.spawning = spawning;
		alpha = 0;
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

		lastMeleeTime = 0; // time since last melee attack finished: 0 ms
		lastAttackTime = 0;
	}

	/**
	 * Performs a special attack at the Player, given the player's centre x and centre y coordinates.
	 *
	 * @param pcx player centre x
	 * @param pcy player centre y
	 */
	private void specialAttack(float pcx, float pcy) {
		Projectile snowball = new Projectile(this, x, y, dir, specialRange);
		float sbw = snowball.getWidth();
		float sbh = snowball.getHeight();
		float sx = x - sbw;
		float sy = y - sbh;
		if (dir == Direction.LEFT || dir == Direction.RIGHT) {
			sy = pcy - (sbh / 2); // align snowball y with player y appropriately
			sx += 150;
		} else { // dir == UP/DOWN
			sx = pcx - (sbw / 2);
			sy += 150;
		}
		snowball.setPosition(sx, sy);
		world.addProjectile(snowball);

		lastSpecialTime = 0; // reset flag
		lastAttackTime = 0;
		SoundManager.playSound(SoundManager.SoundInfo.MONSTER_SPECIAL);
	}

	/**
	 * Checks whether this Monster has completed its expire animation and is ready to be removed from the world
	 *
	 * @return if we can remove this monster from the world
	 */
	public boolean isFadeOutComplete() {
		return (action == Action.EXPIRING) && animations.isComplete() && (alpha <= 0);
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

	/**
	 * Changes this monster's direction to face at the player.
	 *
	 * @param player the player object
	 */
	public void face(Player player) {
		float diffX = x - player.getX();
		float diffY = y - player.getY();
		float absDiffX = Math.abs(diffX);
		float absDiffY = Math.abs(diffY);
		if (absDiffX >= absDiffY) { // difference is bigger horizontally, face horizontally
			if (diffX >= 0) dir = Direction.LEFT;
			else dir = Direction.RIGHT;
		} else { // difference is bigger vertically, face vertically
			if (diffY >= 0) dir = Direction.DOWN;
			else dir = Direction.UP;
		}
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

	public boolean canSpecialAttack() {
		return special;
	}

	@Override
	public Rectangle getCollisionBounds() {
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

	/**
	 * Gets the bounds of the Rectangle where the Monster can shoot its projectile. (Works by checking that the
	 * player is at least either within x-range or y-range before shooting)
	 *
	 * @return the bounds of the special projectile origins
	 */
	public Rectangle getSpecialOriginBounds() {
		Rectangle origin = getCollisionBounds();
		// check if we should offset
		if (specialOriginOffset != null) {
			origin.x += specialOriginOffset.x;
			origin.y += specialOriginOffset.y;
			origin.width += specialOriginOffset.width;
			origin.height += specialOriginOffset.height;
		}
		return origin;
	}

	@Override
	public void update(float delta, boolean paused, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
		// update fade
		if (action == Action.EXPIRING && animations.isComplete()) {
			alpha -= delta * FADE_OUT_SPEED;
			return; // don't do anything else
		} else if (spawning) {
			alpha += delta * FADE_IN_SPEED;
			if (alpha >= 1) {
				alpha = 1;
				spawning = false; // finished spawning
			}
		}

		// update animation
		animations.update(delta);

		// add to meleeTime even if paused, since when pausing we allow any attacks already started to be carried out
		meleeTime += delta;

		if (!paused) { // update last melee/special/either attack flags
			lastMeleeTime += delta;
			lastSpecialTime += delta;
			lastAttackTime += delta;
		}

		// calculate distance we can move, if we choose to move
		float dist = delta * speed;

		if (healthBar.getHealth() <= 0 && action!= Action.KNOCKBACK && action != Action.EXPIRING) {
			setAction(Action.EXPIRING);
			setAggressive(false); // de-aggro upon death
			SoundManager.playSound(SoundManager.SoundInfo.MONSTER_DEATH);
		} else if (action == Action.KNOCKBACK) { // update the knockback
			if (animations.isComplete()) {
				setAction(Action.IDLE_MOVE);
				timeRemaining = 0; // prompt new random movement
				moving = false;
			}
		} else if (action == Action.MELEE) { // this monster is currently attacking
			if (animations.isComplete()) { // we just finished the attack animation
				setAction(Action.IDLE_MOVE);
			}
		} else if (action == Action.SPECIAL) { // this monster is currently firing off special attack
			if (animations.isComplete()) {
				setAction(Action.IDLE_MOVE);
			}
		} else if (action == Action.IDLE_MOVE && !paused) { // update movement if not paused
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
				boolean inRangeX = absDiffX < meleeRangeX; // in range x for melee
				boolean inRangeY = absDiffY < meleeRangeY; // in range y for melee

				Rectangle thisCollision = getSpecialOriginBounds();
				float tx = thisCollision.x;
				float ty = thisCollision.y;
				float txp = tx + thisCollision.width;
				float typ = ty + thisCollision.height;

				// first check if we can special attack
				if (special && (lastSpecialTime >= SPECIAL_COOLDOWN) && (lastAttackTime >= MELEE_COOLDOWN) &&
						((pcx >= tx && pcx <= txp) || (pcy >= ty && pcy <= typ))) {
					// if player x within monster's x extremities, OR player y within monster's y extremities

					// turn to face correct direction
					if (pcx >= tx && pcx <= txp) { // face up/down
						if (diffY < 0) dir = Direction.DOWN;
						else dir = Direction.UP;
					} else { // face left/right
						if (diffX < 0) dir = Direction.LEFT;
						else dir = Direction.RIGHT;
					}
					// start new special attack
					setAction(Action.SPECIAL);
					specialAttack(pcx, pcy);

				} else if (inRangeX && inRangeY) { // within range to start melee attack
					if (lastMeleeTime >= MELEE_COOLDOWN && lastAttackTime >= MELEE_COOLDOWN) { // if attack is off cooldown
						// actually attack
						setAction(Action.MELEE);
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
		// otherwise, action is probably expiring, or game is paused, in which case we do nothing
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
		if (aggressive) { // tint red if aggro
			spriteBatch.setColor(TINT[0], TINT[1], TINT[2], alpha);
		} else {
			spriteBatch.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
		}
		spriteBatch.draw(frame, x, y);
		spriteBatch.setColor(Color.WHITE); // untint
	}
}
