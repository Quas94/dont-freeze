package com.arctite.dontfreeze.entities.player;

import com.arctite.dontfreeze.GameMain;
import com.arctite.dontfreeze.util.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.arctite.dontfreeze.WorldScreen;
import com.arctite.dontfreeze.entities.*;
import com.arctite.dontfreeze.entities.Collisions;

import java.util.ArrayList;
import java.util.List;

import static com.arctite.dontfreeze.util.SaveManager.*;

/**
 * Player class for all things related to the fire elemental.
 *
 * Created by Quasar on 14/06/2015.
 */
public class Player implements LiveEntity {

	/** Sprite constants and other constants. @TODO: don't hardcode this stuff */
	private static final int COLLISION_WIDTH = 20;
	private static final int COLLISION_HEIGHT = 10;
	private static final float DIAGONAL_MOVE_RATIO = 0.765F;
	private static final int FIREBALL_RANGE = 200; // how far this Player's fireball can travel before dissipating
	private static final int BASE_HEALTH = 5;

	/** Limits */
	public static final int RIGHTMOST_X = WorldScreen.CHUNK_WIDTH - ResourceInfo.PLAYER.getWidth();
	public static final int HIGHEST_Y = WorldScreen.CHUNK_HEIGHT - ResourceInfo.PLAYER.getHeight();

	/** Link to the World this player is currently in */
	private WorldScreen world;
	private WorldInputHandler inputHandler;

	/** Animation related variables */
	private AnimationManager animations;
	private Action action;

	/** Position and dimensions of the player */
	private Direction dir;
	private Direction lastRenderedDir; // flag for map changing, so map will only change if player facing correct dir
	private int chunkX;
	private int chunkY;
	private float x;
	private float y;
	private int speed;
	private int width;
	private int height;

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
		this.speed = ResourceInfo.PLAYER.getSpeed();
		this.width = ResourceInfo.PLAYER.getWidth();
		this.height = ResourceInfo.PLAYER.getHeight();

		this.dir = Direction.DOWN;
		this.lastRenderedDir = null;
		this.action = Action.IDLE_MOVE; // only exception where we don't use setAction()
		this.animations = new AnimationManager(AnimationManager.MULTI_DIR, this, ResourceInfo.PLAYER);

		this.meleeTime = 0;
		this.meleeCollisionBounds = null;

		this.maxHealth = BASE_HEALTH;
		this.health = maxHealth;
		this.fires = 0;
	}

	/**
	 * Sets this Player's WorldScreen reference as well as WorldInputHandler reference
	 *
	 * @param world WorldScreen
	 * @param inputHandler WorldInputHandler
	 */
	public void setWorld(WorldScreen world, WorldInputHandler inputHandler) {
		this.world = world;
		this.inputHandler = inputHandler;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getId() {
		return ResourceInfo.PLAYER.getId();
	}

	/**
	 * Loads the relevant saved fields into this Player object, given the save manager.
	 */
	public void load() {
		SaveManager saver = SaveManager.getSaveManager();

		// load chunk data
		chunkX = saver.getDataValue(PLAYER_CHUNK_X, Integer.class);
		chunkY = saver.getDataValue(PLAYER_CHUNK_Y, Integer.class);

		x = saver.getDataValue(PLAYER + POSITION_X, Float.class);
		y = saver.getDataValue(PLAYER + POSITION_Y, Float.class);
		health = saver.getDataValue(PLAYER + HEALTH, Integer.class);
		int di = saver.getDataValue(PLAYER + DIR_IDX, Integer.class);
		dir = Direction.getByIndex(di);

		fires = saver.getDataValue(PLAYER + FIRES, Integer.class);
	}

	/**
	 * Saves relevant fields from this Player object into the given save manager.
	 */
	public void save() {
		SaveManager saver = SaveManager.getSaveManager();

		saver.setDataValue(PLAYER_CHUNK_X, chunkX);
		saver.setDataValue(PLAYER_CHUNK_Y, chunkY);

		saver.setDataValue(PLAYER + POSITION_X, x);
		saver.setDataValue(PLAYER + POSITION_Y, y);
		saver.setDataValue(PLAYER + HEALTH, health);
		saver.setDataValue(PLAYER + DIR_IDX, dir.getIdx());
		saver.setDataValue(PLAYER + FIRES, fires);
	}

	public int getChunkX() {
		return chunkX;
	}

	public int getChunkY() {
		return chunkY;
	}

	public void setChunk(int chunkX, int chunkY) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
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
		// check for death upon finishing knockback animation, in update method
		if (action == Action.IDLE_MOVE) { // start knockback, from idle/move state only
			setAction(Action.KNOCKBACK);
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
	public void update(float delta, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
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

		if (health <= 0 && action!= Action.KNOCKBACK && action != Action.EXPIRING) {
			setAction(Action.EXPIRING);
			dir = Direction.DOWN; // player death frames are down only
			world.deaggroMonsters();
			SoundManager.playSound(SoundManager.SoundInfo.PLAYER_DEATH);
		} else if (attacking) {
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
				SoundManager.playSound(SoundManager.SoundInfo.PLAYER_MELEE);
			} else if (specialAttackPressed) {
				// launch special attack
				// @TODO delay fireball generation by a bit
				setAction(Action.SPECIAL);
				specialAttack();
				SoundManager.playSound(SoundManager.SoundInfo.PLAYER_SPECIAL);
			}
		} else if (recoiling) {
			// we are stunned and cannot do anything until animation ends
			if (animations.isComplete()) {
				setAction(Action.IDLE_MOVE); // finish the knockback and revert to idle/move
			}
		} else {
			// lastly, if we are not dealing with anything else, we update movement instead
			updateMovement(delta, leftPressed, rightPressed, upPressed, downPressed, rects, polys);
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
		Projectile fireball = new Projectile(this, x, y, dir, FIREBALL_RANGE);
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
								boolean downPressed, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {

		float dist = delta * speed;

		float oldX = x;
		float oldY = y;
		float oldDist = dist;

		// first, try making both x and y changes at once. if it doesn't collide, everything is fine
		if ((leftPressed || rightPressed) && (upPressed || downPressed)) {
			// going diagonally
			dist *= DIAGONAL_MOVE_RATIO;
		}
		int dirsPressed = 0; // number of directions pressed
		if (leftPressed) {
			x -= dist;
			dirsPressed++;
		}
		if (rightPressed) {
			x += dist;
			dirsPressed++;
		}
		if (upPressed) {
			y += dist;
			dirsPressed++;
		}
		if (downPressed) {
			y -= dist;
			dirsPressed++;
		}

		// check collisions and stuff only if directions were pressed (since otherwise we wouldn't have moved at all)
		if (dirsPressed > 0) {
			// check if we collide with stuff after
			ArrayList<Rectangle> collideRects = Collisions.collidesWithRects(getCollisionBounds(), rects);
			ArrayList<RectangleBoundedPolygon> collidePolys = Collisions.collidesWithPolys(getCollisionBounds(), polys);
			int collisions = collideRects.size() + collidePolys.size();

			if (collisions > 0 && dirsPressed == 1) {
				// try possible sliding over very slight slopes if one dir was pressed and one or more collisions
				// limiting sliding guesses to only one collision isn't 100% correct but close enough, and faster
				// another possible problem is we only test slide against the one shape we previously collided against,
				// w/o checking if we newly collide with shapes after slide. @TODO if we get sticking issues, refer here
				float slideDist = dist * (float) Math.sqrt(1D / 3D); // allow slides of up to 30 degrees
				// dist = dist / 3F * 2F; // so dist = 2/3 of original dist, and slideDist = 1/3 of original dist.
				Shape2D collideShape; // refraining from using ? notation in this project
				// get the shape we collided with
				if (collideRects.size() == 1) {
					collideShape = collideRects.get(0);
				} else { // means collidePolys.size() == 1 for sure
					collideShape = collidePolys.get(0);
				}
				boolean slideSuccess;
				if (leftPressed || rightPressed) { // try sliding up or down
					if (leftPressed) x += slideDist; // undo left covered by slideDist
					else x -= slideDist; // rightPressed - undo right covered by slideDist
					// slide up attempt
					y += slideDist;
					slideSuccess = !Collisions.collidesShapes(getCollisionBounds(), collideShape); // test with the shape
					if (!slideSuccess) { // try sliding down instead
						y = oldY - slideDist;
						slideSuccess = !Collisions.collidesShapes(getCollisionBounds(), collideShape); // test again
					}
				} else { // if (upPressed || downPressed) - try sliding left or right
					if (upPressed) y -= slideDist; // undo up covered by slideDist
					else y += slideDist; // undo down covered by slideDist
					// slide left attempt
					x -= slideDist; // slide left
					slideSuccess = !Collisions.collidesShapes(getCollisionBounds(), collideShape); // test with the shape
					if (!slideSuccess) { // try sliding right instead
						x = oldX + slideDist;
						slideSuccess = !Collisions.collidesShapes(getCollisionBounds(), collideShape); // test again
					}
				}
				if (!slideSuccess) { // if slide was not successful, we undo changes
					x = oldX;
					y = oldY;
				}
			} else if (collisions > 0 && dirsPressed == 2) {
				// if collisions isn't none, and we pressed 2 directions, separate dirs and test them individually
				// undo changes
				x = oldX;
				y = oldY;
				dist = oldDist;

				// tentatively update x pos first
				if (leftPressed) x -= dist;
				if (rightPressed) x += dist;
				// check for collisions after updating x
				collideRects = Collisions.collidesWithRects(getCollisionBounds(), rects);
				collidePolys = Collisions.collidesWithPolys(getCollisionBounds(), polys);
				collisions = collideRects.size() + collidePolys.size();
				if (collisions > 0) { // if there are collisions after updating x only, under x changes
					leftPressed = false;
					rightPressed = false;
				}
				// undo change
				x = oldX;
				// update y pos next
				if (upPressed) y += dist;
				if (downPressed) y -= dist;
				// check for collisions after updating y
				collideRects = Collisions.collidesWithRects(getCollisionBounds(), rects);
				collidePolys = Collisions.collidesWithPolys(getCollisionBounds(), polys);
				collisions = collideRects.size() + collidePolys.size();
				if (collisions > 0) { // if there are collisions after updating x only, under x changes
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
			} else if (collisions > 0) { // note: it's wholly possible to have dirsPressed = 3 or 4
				// last case: just immediately undo, full stop.
				x = oldX;
				y = oldY;
			}

			// check for map change or in bounds
			int chunkX = world.getChunkX();
			int chunkY = world.getChunkY();
			GameMain game = world.getGame();
			// lastRenderedDir is used so that the screen displays the player facing the correct dir before map change
			if (x < 0) {
				if (chunkX == WorldScreen.LEFTMOST_CHUNK_X || lastRenderedDir != dir) x = 0;
				else game.setWorldChangeMap(Direction.LEFT);
			}
			if (x >= RIGHTMOST_X) {
				if (chunkX == WorldScreen.RIGHTMOST_CHUNK_X || lastRenderedDir != dir) x = RIGHTMOST_X;
				else game.setWorldChangeMap(Direction.RIGHT);
			}
			if (y < 0) {
				if (chunkY == WorldScreen.LOWEST_CHUNK_Y || lastRenderedDir != dir) y = 0;
				else game.setWorldChangeMap(Direction.DOWN);
			}
			if (y >= HIGHEST_Y) {
				if (chunkY == WorldScreen.HIGHEST_CHUNK_Y || lastRenderedDir != dir) y = HIGHEST_Y;
				else game.setWorldChangeMap(Direction.UP);
			}
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		lastRenderedDir = dir; // update flag
		TextureRegion frame = animations.getCurrentFrame(dir);
		spriteBatch.draw(frame, x, y);
	}
}
