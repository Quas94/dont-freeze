package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.util.ResourceInfo;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.arctite.dontfreeze.WorldScreen;

import java.util.List;

/**
 * Class that represents Entities in the game world that can be interacted with and picked up.
 * eg. Fires
 *
 * Created by Quasar on 19/06/2015.
 */
public class Collectable implements Entity {

	/** Fade speed */
	private static final float FADE_IN_SPEED = 1.0F; // 1 second to completely fade in

	/** Technical fields */
	private WorldScreen world;
	private int id;
	private float x;
	private float y;
	private int width;
	private int height;
	private AnimationManager animation;

	/** Whether this collectable is spawning in */
	private boolean spawning;
	/** When spawning, alpha value of the collectable */
	private float alpha;

	public Collectable(WorldScreen world, int id, float x, float y) {
		this.world = world;
		this.id = id;
		ResourceInfo info = ResourceInfo.getByTypeAndId(ResourceInfo.Type.COLLECTABLE, id);

		this.x = x;
		this.y = y;
		this.width = info.getWidth();
		this.height = info.getHeight();

		// getDirection() always returns down
		this.animation = new AnimationManager(AnimationManager.UNI_DIR, this, info);

		this.spawning = false;
		this.alpha = 0;
	}

	/**
	 * Sets this collectable to start spawning.
	 */
	public void setSpawning() {
		this.spawning = true;
		alpha = 0;
	}

	@Override
	public int getId() {
		return id;
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
	public void update(float delta, boolean paused, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
		animation.update(delta);

		if (spawning) { // update spawning alpha and status
			alpha += delta * FADE_IN_SPEED;
			if (alpha >= 1.0F) {
				alpha = 1.0F;
				spawning = false; // finished spawning
			}
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		TextureRegion frame = animation.getCurrentFrame(getDirection());
		if (spawning) { // if spawning, tint
			spriteBatch.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
		}
		spriteBatch.draw(frame, x, y); // actually draw collectable
		if (spawning) { // untint
			spriteBatch.setColor(Color.WHITE);
		}
	}
}
