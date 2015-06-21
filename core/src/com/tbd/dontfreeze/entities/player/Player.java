package com.tbd.dontfreeze.entities.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.WorldScreen;
import com.tbd.dontfreeze.entities.*;

import java.util.ArrayList;


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
	private static final int SPEED = 120;
	private static final float DIAGONAL_MOVE_RATIO = 0.765F;
	// private static final float SCALE = 1F;
	private static final float FRAME_RATE = 0.12F;
	private static final int FIREBALL_RANGE = 300; // how far this Player's fireball can travel before dissipating
	private static final int BASE_HEALTH = 100;

	/** Link to the World this player is currently in */
	private WorldScreen world;
	private InputHandler inputHandler;

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

	public Player(WorldScreen world, InputHandler inputHandler, float x, float y) {
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
		// player gets hit
	}

	public int getFireCount() {
		return fires;
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
		boolean attacking = meleeAttacking || specialAttacking; // either type of attacking

		// set direction
		if (!attacking) { // can only change dir if not in the middle of an attack
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
				// @TODO special attack frames
				setAction(Action.MELEE);
				specialAttack();
			}
		} else {
			// lastly, if we are not dealing with any sort of attacking, we update movement instead
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
			// no changes needed
		} else if (dir == Direction.DOWN) {
			fy += 30;
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

	/**
	 * Checks the player and sees if it is colliding with any Collectables (pick them up).
	 *
	 * Will also check for collision of player and enemy projectiles, when they are implemented later.
	 *
	 * @param collectables list of Collectables
	 * @param projectiles list of Projectiles
	 */
	public void updateCollision(ArrayList<Collectable> collectables, ArrayList<Projectile> projectiles) {
		// @TODO: projectile collision with player when boss monsters can use ranged attacks
		// @TODO move this to WorldScreen class for consistency
		// process collectables collision
		for (int i = 0; i < collectables.size(); i++) {
			Collectable c = collectables.get(i);
			if (EntityUtil.collides(getCollisionBounds(), c.getCollisionBounds())) {
				// remove from the map, because we picked it up
				collectables.remove(i);
				// increment our collected counter
				fires++;
			}
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animations.getCurrentFrame(dir);
		spriteBatch.draw(frame, x, y);
	}
}
