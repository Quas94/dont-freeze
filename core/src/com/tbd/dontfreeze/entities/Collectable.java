package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.WorldScreen;

/**
 * Class that represents Entities in the game world that can be interacted with and picked up.
 * eg. Fires
 *
 * Created by Quasar on 19/06/2015.
 */
public class Collectable implements Entity {

	private static final float FRAME_RATE = 0.2F;
	private static final String FILE = "assets/fire.atlas";
	private static final int SPRITE_WIDTH = 50;
	private static final int SPRITE_HEIGHT = 65;

	private WorldScreen world;

	private float x;
	private float y;
	private int width;
	private int height;

	private AnimationManager animation;

	public Collectable(WorldScreen world, float x, float y) {
		this.world = world;

		this.x = x;
		this.y = y;
		this.width = SPRITE_WIDTH;
		this.height = SPRITE_HEIGHT;

		// getDirection() always returns down
		this.animation = new AnimationManager(AnimationManager.UNI_DIR, this, FILE, FRAME_RATE);
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

	/**
	 * Collectables always point down.
	 */
	@Override
	public Direction getDirection() {
		return Direction.DOWN;
	}

	@Override
	public Action getAction() {
		// for now, collectables can only be stationary
		// @TODO implement collectable spawning animations / getting picked up (expiry) animations later
		return Action.IDLE_MOVE;
	}

	@Override
	public Rectangle getCollisionBounds() {
		// middle 50% of width, bottom 25% of height
		return new Rectangle(x + (width / 4), y, width / 2, height / 4);
	}

	@Override
	public void update(float delta, Array<PolygonMapObject> polys, Array<RectangleMapObject> rects) {
		animation.update(delta);
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animation.getCurrentFrame(getDirection());
		spriteBatch.draw(frame, x, y);
	}
}
