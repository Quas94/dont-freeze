package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

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

	private static final int SPEED = 150;

	private float x;
	private float y;
	private int width;
	private int height;
	private Direction dir;
	private Action action;

	private AnimationManager animation;

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

	/**
	 * Whether or not this projectile has gone the entire length of its maximum range.
	 * @return whether this projectile has expired
	 */
	public boolean isExpired() {
		return range <= 0;
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

	@Override
	public Rectangle getCollisionBounds() {
		return new Rectangle(x, y, SPRITE_WIDTH, SPRITE_HEIGHT);
	}

	@Override
	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		// update animation
		animation.update(delta);

		// update range
		float dist = delta * SPEED;
		range -= dist;

		// move by dist
		if (dir == Direction.LEFT) x -= dist;
		else if (dir == Direction.RIGHT) x += dist;
		else if (dir == Direction.UP) y += dist;
		else if (dir == Direction.DOWN) y -= dist;

		// @TODO collision detection - if there is collision with terrain, set range to 0 instantly
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
