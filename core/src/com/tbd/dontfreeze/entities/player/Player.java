package com.tbd.dontfreeze.entities.player;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.WorldScreen;
import com.tbd.dontfreeze.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Player class for all things related to the fire elemental.
 *
 * Created by Quasar on 14/06/2015.
 */
public class Player implements LiveEntity {

	/** Sprite constants and other constants. @TODO: don't hardcode this stuff */
	private static final int SPRITE_WIDTH = 80;
	private static final int SPRITE_HEIGHT = 95;
	private static final int COLLISION_WIDTH = 20;
	private static final int COLLISION_HEIGHT = 10;
	private static final String PATH = "assets/player.atlas";
	private static final int SPEED = 150;
	private static final float DIAGONAL_MOVE_RATIO = 0.765F;
	// private static final float SCALE = 1F;
	private static final float FRAME_RATE = 0.12F;
	private static final int FIREBALL_RANGE = 250; // how far this Player's fireball can travel before dissipating
	private static final int BASE_HEALTH = 2;

	/** Link to the World this player is currently in */
	private WorldScreen world;
	private WorldInputHandler inputHandler;

	/** Animation related variables */
	private AnimationManager animations;
	private Action action;

	/** Position and dimensions of the player */
	private Direction dir;
	private float x;
	private float y;
	private int width;
	private int height;
	/** The furthest this player can go whilst staying in bounds (leftmost and downmost are zero) */
	private float rightmost;
	private float upmost;

	/** Combat */
	/** The amount of time that has elapsed since the start of the melee attack animation */
	private float meleeTime;
	/** Whether the current melee attack has hit something yet (limits to 1 target hit per melee swing) */
	private boolean meleeHit;
	private Rectangle meleeCollisionBounds;

	/** Gameplay fields */
	private int maxHealth;
	private int health;
	private int fires;

	public Player(WorldScreen world, WorldInputHandler inputHandler, float x, float y) {
		this.world = world;
		this.inputHandler = inputHandler;

		this.x = x;
		this.y = y;
		this.width = SPRITE_WIDTH;
		this.height = SPRITE_HEIGHT;
		this.upmost = world.getHeight() - height;
		this.rightmost = world.getWidth() - width;

		this.dir = Direction.DOWN;
		this.action = Action.IDLE_MOVE; // only exception where we don't use setAction()
		this.animations = new AnimationManager(AnimationManager.MULTI_DIR, this, PATH, FRAME_RATE);

		this.meleeTime = 0;
		this.meleeCollisionBounds = null;

		this.maxHealth = BASE_HEALTH;
		this.health = maxHealth;
		this.fires = 0;
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
		health--; // take damage
		dir = from; // change direction to from
		if (health > 0) {
			if (action == Action.IDLE_MOVE) { // start knockback, from idle/move state only
				setAction(Action.KNOCKBACK);
			}
			// don't do any changes otherwise
		} else { // start expire frames no matter what action we're currently doing
			setAction(Action.EXPIRING);
			dir = Direction.DOWN; // player death frames are down only
			world.notifyPlayerDeath();
		}
	}

	public int getFireCount() {
		return fires;
	}

	public void collectFire() {
		fires++;
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

	@Override
	public Rectangle getCollisionBounds() {
		float collisionX = x + ((width - COLLISION_WIDTH) / 2);
		float collisionY = y + 10;
		return new Rectangle(collisionX, collisionY, COLLISION_WIDTH, COLLISION_HEIGHT);
	}

	@Override
	public Rectangle getAttackCollisionBounds() {
		return meleeCollisionBounds;
	}

	@Override
	public Rectangle getDefenseCollisionBounds() {
		// vertically: middle 60%. horizontally: middle 50%
		float rx = x + (width / 4);
		float ry = y + (height / 5);
		float rw = width / 2;
		float rh = height / 5 * 3;
		Rectangle rect = new Rectangle(rx, ry, rw, rh);
		return rect;
	}

	@Override
	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		if (action == Action.EXPIRING) {
			if (!animations.isComplete()) {
				animations.update(delta / 2);
			} else {
				world.notifyPlayerDeathComplete();
			}
			return; // we died... don't update anything else
		}

		// animation
		animations.update(delta);

		// key presses
		boolean leftPressed = inputHandler.isKeyDown(Key.LEFT);
		boolean rightPressed = inputHandler.isKeyDown(Key.RIGHT);
		boolean upPressed = inputHandler.isKeyDown(Key.UP);
		boolean downPressed = inputHandler.isKeyDown(Key.DOWN);
		boolean attackPressed = inputHandler.isKeyDown(Key.ATTACK);
		boolean specialAttackPressed = inputHandler.isKeyDown(Key.SPECIAL);

		// states
		boolean meleeAttacking = (action == Action.MELEE);
		boolean specialAttacking = (action == Action.SPECIAL);
		boolean recoiling = (action == Action.KNOCKBACK); // being knocked back
		boolean attacking = meleeAttacking || specialAttacking; // either type of attacking

		// set direction
		if (!attacking && !recoiling) { // can only change dir if not attacking or recoiling (getting knocked back)
			Direction newDir = Direction.getByKey(inputHandler.getNewKey()); // newKey is highest priority for direction
			if (newDir != null) {
				dir = newDir;
			}
		}

		// attacking
		if (attacking) {
			// if melee attack, update time
			if (meleeAttacking) meleeTime += delta;

			// check if animation complete
			if (animations.isComplete()) {
				// change action back to idle/move
				setAction(Action.IDLE_MOVE);
			}
		} else if (attackPressed || specialAttackPressed) { // if not already attacking, consider starting to attack
			if (attackPressed) {
				// change action field
				setAction(Action.MELEE);
				meleeAttack();
			} else if (specialAttackPressed) {
				// launch special attack
				// @TODO delay fireball generation by a bit
				setAction(Action.SPECIAL);
				specialAttack();
			}
		} else if (recoiling) {
			// we are stunned and cannot do anything until animation ends
			if (animations.isComplete()) {
				setAction(Action.IDLE_MOVE); // finish the knockback and revert to idle/move
			}
		} else {
			// lastly, if we are not dealing with anything else, we update movement instead
			updateMovement(delta, leftPressed, rightPressed, upPressed, downPressed, polys, rects);
		}
	}

	/**
	 * Changes the action and notifies the AnimationManager of this change.
	 *
	 * All modifications to this class' action should be done via this method.
	 *
	 * @param action the action to change to
	 */
	private void setAction(Action action) {
		this.action = action;
		animations.updateAction(action);
	}

	/**
	 * Starts off a melee attack.
	 */
	private void meleeAttack() {
		meleeTime = 0; // reset melee time counter
		meleeHit = false; // reset melee hit counter
		// player can't move during attacks so we'll save this attack Rectangle bound as a field
		float mx = x;
		float my = y;
		int mw = width / 2;
		int mh = height / 2;
		switch (dir) {
			case LEFT:
				my += height / 4;
				break;
			case RIGHT:
				mx += width / 2;
				my += height / 4;
				break;
			case UP:
				mx += width / 4;
				my += height / 2;
				break;
			case DOWN:
				mx += width / 4;
				break;
		}
		meleeCollisionBounds = new Rectangle(mx, my, mw, mh);
	}

	/**
	 * Checks whether or not the current melee swing has hit a target yet.
	 *
	 * @return whether the current melee swing has hit a target
	 */
	public boolean getMeleeHit() {
		return meleeHit;
	}

	/**
	 * Sets the Player's current melee hit as having hit a target.
	 */
	public void setMeleeHit() {
		this.meleeHit = true;
	}

	/**
	 * Checks whether or not enough time has elapsed for this melee attack to actually cause damage and hit something.
	 * This is so that an enemy isn't hit as soon as the Player's melee animation starts, which would look strange
	 * because the enemy is recoiling before the Player's arms have even started coming down.
	 *
	 * @return whether this melee attack can actually hit something yet
	 */
	public boolean getMeleeCanHit() {
		return meleeTime >= LiveEntity.MELEE_ATTACK_DELAY;
	}

	/**
	 * Launches the Player's special attack (a fireball) and adds it to the game world.
	 *
	 * @TODO tidy this up
	 */
	private void specialAttack() {
		// create fireball object first with incorrect x, y values - just so we can pull the width and height
		Projectile fireball = new Projectile(x, y, dir, FIREBALL_RANGE);
		float fx = x;
		float fy = y;
		// float fw = fireball.getWidth();
		// float fh = fireball.getHeight();
		if (dir == Direction.RIGHT || dir == Direction.LEFT) {
			fx -= 5; // shift towards player a bit more
			fy += height / 3;
			// don't need x modifier for LEFT because TextureRegion.flip seems to handle that
		} else if (dir == Direction.UP) {
			fx += 12;
			fy += 5;
		} else if (dir == Direction.DOWN) {
			fx += 12;
			fy += 35;
		}
		fireball.setPosition(fx, fy);
		world.addProjectile(fireball);
	}

	/**
	 * Updates the movement of this Player.
	 */
	private void updateMovement(float delta, boolean leftPressed, boolean rightPressed, boolean upPressed,
								boolean downPressed, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {

		float dist = delta * SPEED;

		float oldX = x;
		float oldY = y;
		float oldDist = dist;

		// first, try making both x and y changes at once. if it doesn't collide, everything is fine
		if ((leftPressed || rightPressed) && (upPressed || downPressed)) {
			// going diagonally
			dist *= DIAGONAL_MOVE_RATIO;
		}
		if (leftPressed) x -= dist;
		if (rightPressed) x += dist;
		if (upPressed) y += dist;
		if (downPressed) y -= dist;

		// if it does collide, we need to separate x and y changes and test individually
		if (EntityUtil.collidesTerrain(this, polys, rects)) {
			// undo changes
			x = oldX;
			y = oldY;
			dist = oldDist;

			// tentatively update x pos first
			if (leftPressed) x -= dist;
			if (rightPressed) x += dist;
			// check for collisions after updating x
			if (EntityUtil.collidesTerrain(this, polys, rects)) {
				leftPressed = false;
				rightPressed = false;
			}
			// undo change
			x = oldX;
			// update y pos next
			if (upPressed) y += dist;
			if (downPressed) y -= dist;
			// check for collisions after updating y
			if (EntityUtil.collidesTerrain(this, polys, rects)) {
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
		}

		// keep within bounds of map
		if (x < 0) x = 0;
		if (x >= rightmost) x = rightmost;
		if (y < 0) y = 0;
		if (y >= upmost) y = upmost;
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animations.getCurrentFrame(dir);
		spriteBatch.draw(frame, x, y);
	}
}
