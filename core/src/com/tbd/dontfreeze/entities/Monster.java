package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.WorldScreen;
import com.tbd.dontfreeze.entities.player.Player;

import java.util.Random;

/**
 * Base class for all Monsters.
 *
 * Created by Quasar on 16/06/2015.
 */
public class Monster implements LiveEntity {

	private static final String PATH = "assets/snowbaby.atlas";
	private static final float FRAME_RATE = 0.18F;
	private static final int SPEED = 30;
	private static final int SPRITE_WIDTH = 55;
	private static final int SPRITE_HEIGHT = 50;

	// timing values for monster random movement
	private static final float MIN_RAND_TIME = 1.0F;
	private static final float MAX_RAND_TIME = 4.0F;

	private static final int BASE_HEALTH = 10;
	private static final float ATTACK_COOLDOWN = 1F;

	/** Connection to world */
	private WorldScreen world;
	private float x;
	private float y;
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

		this.aggressive = false;
		this.lastMeleeTime = 0;
		this.meleeTime = 0;
		this.meleeHit = false;

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
		// @TODO make it so the monster can't get hit-stunned repeatedly forever (do this for player too)
		// @TODO make it so that monsters continue facing the hit-from direction for a while after getting hit
		// although it will probably be done as a side-effect of monster aggro anyway
		if (health > 0) { // can survive this blow
			if (action == Action.IDLE_MOVE) {
				// set action to knocked back
				setAction(Action.KNOCKBACK);
			} else if (action == Action.MELEE) {
				// if monster is already attacking, we don't put it into knockback
			}
		} else { // dead!
			setAction(Action.EXPIRING);
		}
		// otherwise the monster is expiring (we do nothing) or already getting hit (we do nothing)
		// OR the monster is already being knocked back (this shouldn't happen though, the player attack animations are
		// both quite long in comparison to the monster's knockback animation
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
		int rh = height / 10 * 7;
		return new Rectangle(rx, ry, rw, rh);
	}

	@Override
	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		// @TODO remove this if-condition after implementing different speeds for different animation types per manager
		if (action == Action.EXPIRING) { animations.update(delta / 5); } else if (action == Action.KNOCKBACK) { animations.update(delta / 1.5F); } else

		// update animation
		animations.update(delta);

		// add to lastMeleeTime counter
		lastMeleeTime += delta;

		// calculate distance we can move, if we choose to move
		float dist = delta * SPEED;

		if (action == Action.KNOCKBACK) { // update the knockback
			// if knockback is complete, set mode back to IDLE_MOVE
			if (animations.isComplete()) {
				setAction(Action.IDLE_MOVE);
				timeRemaining = 0; // prompt new random movement
				moving = false;
			}
		} else if (action == Action.MELEE) { // this monster is currently attacking
			// @TODO attack player
			if (animations.isComplete()) { // we just finished the attack animation
				lastMeleeTime = 0; // time since last melee attack finished: 0 ms
				setAction(Action.IDLE_MOVE);
			}
		} else if (action == Action.IDLE_MOVE) { // update movement
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
						meleeHit = false;
						meleeTime = 0;
					}
					// otherwise we just stay in IDLE_MOVE until attack comes off cooldown
				} else if (!inRangeX) { // move on horizontal plane
					if (diffX > 0) { // player on the right
						dir = Direction.RIGHT;
						x += dist;
					} else if (diffX < 0) { // player on the left
						dir = Direction.LEFT;
						x -= dist;
					}
				} else { // !inRangeY, move on vertical plane
					if (diffY > 0) { // player is above
						dir = Direction.UP;
						y += dist;
					} else { // player is below
						dir = Direction.DOWN;
						y -= dist;
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
		}
		// otherwise, action is probably expiring - we do nothing
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animations.getCurrentFrame(dir);
		spriteBatch.draw(frame, x, y);
	}
}
