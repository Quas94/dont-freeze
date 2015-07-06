package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.util.ResourceInfo;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * An animated obstacle entity (eg. geyser) on the map. Has two animation states: idling and actually activating.
 *
 * Created by Quasar on 6/07/2015.
 */
public class AnimatedObstacle implements Entity {

	/** General entity fields */
	private int id;
	private float x;
	private float y;
	private int width;
	private int height;

	/** Bounds, for drawing by the debug renderer */
	private Rectangle bounds;

	/** Action that this obstacle is taking. Can either be IDLE_MOVE or ANIMATING */
	private Action action;

	/** Animation manager for this animated obstacle */
	private AnimationManager animations;

	/**
	 * Creates a new AnimatedObstacle entity with the given details.
	 *
	 * @param id id number of the obstacle
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public AnimatedObstacle(int id, float x, float y) {
		this.id = id;
		this.x = x;
		this.y = y;
		ResourceInfo info = ResourceInfo.getByTypeAndId(ResourceInfo.Type.ANIMATED_OBSTACLE, id);
		this.width = info.getWidth();
		this.height = info.getHeight();
		this.bounds = new Rectangle(x, y, width, height);
		// start off not animating
		this.action = Action.IDLE_MOVE;

		this.animations = new AnimationManager(AnimationManager.UNI_DIR, this, info);
	}

	@Override
	public void update(float delta, boolean paused, List<Rectangle> rects, List<RectangleBoundedPolygon> polys) {
		animations.update(delta);
		if (action == Action.ANIMATING && animations.isComplete()) { // only animate one cycle
			setAction(Action.IDLE_MOVE);
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		spriteBatch.draw(animations.getCurrentFrame(getDirection()), x, y);
	}

	/**
	 * Animated obstacles are permanently facing "down"
	 *
	 * @return Direction.DOWN
	 */
	@Override
	public Direction getDirection() {
		return Direction.DOWN;
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
	 * Updates this AnimatedObstacle's current action. All changes made to the action field must utilise this method,
	 * in order for the animation manager to be updated correctly.
	 *
	 * @param action the new Action to change to
	 */
	public void setAction(Action action) {
		this.action = action;
		animations.updateAction(action);
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Rectangle getCollisionBounds() {
		return bounds;
	}
}
