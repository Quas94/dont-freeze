package com.tbd.dontfreeze.entities.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.entities.*;
import com.tbd.dontfreeze.WorldScreen;

import java.util.ArrayList;


/**
 * Player class for all things related to the fire elemental.
 *
 * Created by Quasar on 14/06/2015.
 */
public class Player implements Entity {

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

	/** Link to the World this player is currently in */
	private WorldScreen world;
	private InputHandler inputHandler;

	/** Animation related variables */
	private AnimationManager animations;
	private Action action;

	/** Player stats */
	private int heat;
	private int fires;

	/** Position and dimensions of the player */
	private Direction dir;
	private float x;
	private float y;
	private int width;
	private int height;
	/** The furthest this player can go whilst staying in bounds (leftmost and downmost are zero) */
	private float rightmost;
	private float upmost;

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

		this.heat = 0;
		this.fires = 0;
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
		return new Rectangle(collisionX, y, COLLISION_WIDTH, COLLISION_HEIGHT);
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
		boolean attacking = (action == Action.MELEE) || (action == Action.SPECIAL); // either type of attacking

		// set direction
		Direction newDir = Direction.getByKey(inputHandler.getNewKey()); // newKey is highest priority for direction
		if (newDir != null && !attacking) dir = newDir; // only can change dir if not in the middle of an attack

		// attacking
		if (attacking) {
			// check if animation complete
			if (animations.isComplete()) {
				// change action back to idle/move
				setAction(Action.IDLE_MOVE);
			}
		} else if (attackPressed || specialAttackPressed) { // if not already attacking, consider starting to attack
			if (attackPressed) {
				// change action field
				setAction(Action.MELEE);

			} else if (specialAttackPressed) {
				// launch special attack
				// @TODO special attack frames
				setAction(Action.MELEE);
				specialAttack();
				System.out.println("launched a special attack at " + System.currentTimeMillis());
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

	public void updateCollision(ArrayList<Monster> monsters, ArrayList<Collectable> collectables, ArrayList<Projectile> projectiles) {
		// @TODO: collision with monsters, as part of the combat system
		// @TODO: projectile collision with player

		// process collectables collision
		for (int i = 0; i < collectables.size(); i++) {
			Collectable c = collectables.get(i);
			if (EntityUtil.collidesEntity(this, c)) {
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
