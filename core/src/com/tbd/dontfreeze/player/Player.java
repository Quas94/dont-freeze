package com.tbd.dontfreeze.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tbd.dontfreeze.Entity;
import com.tbd.dontfreeze.WorldScreen;

/**
 * Player class for all things related to the fire elemental.
 *
 * Created by Quasar on 14/06/2015.
 */
public class Player implements Entity {

	/** Sprite constants and other constants. @TODO: don't hardcode this stuff */
	private static final int SPRITE_WIDTH = 64;
	private static final int SPRITE_HEIGHT = 87;
	private static final String PATH = "assets/player.png";
	private static final int SPEED = 160;
	private static final float DIAGONAL_MOVE_RATIO = 0.765F;
	private static final float SCALE = 1F;

	/** Link to the World this player is currently in */
	private WorldScreen world;

	/** Animation related variables */
	private float stateTime;
	private Sprite sprite;

	/** Heat (effective health) of the player */
	private int heat;

	/** Position and dimensions of the player */
	private float x;
	private float y;
	private int width;
	private int height;
	/** The furthest this player can go whilst staying in bounds (leftmost and upmost are zero) */
	private float rightmost;
	private float upmost;

	public Player(WorldScreen world, float x, float y) {
		this.world = world;
		setLocation(x, y);
		this.width = SPRITE_WIDTH;
		this.height = SPRITE_HEIGHT;
		this.upmost = world.getHeight() - height;
		this.rightmost = world.getWidth() - width;

		this.stateTime = 0;

		// load sprite
		Texture tex = new Texture(Gdx.files.internal(PATH));
		this.sprite = new Sprite(tex);
	}

	@Override
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void update(float delta) {
		boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
		boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
		boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
		float dist = delta * SPEED;
		if ((leftPressed || rightPressed) && (upPressed || downPressed)) {
			// going diagonally
			dist *= DIAGONAL_MOVE_RATIO;
		}
		if (leftPressed) x -= dist;
		if (rightPressed) x += dist;
		if (upPressed) y += dist;
		if (downPressed) y -= dist;
		// keep within bounds of map
		if (x < 0) x = 0;
		if (x >= rightmost) x = rightmost;
		if (y < 0) y = 0;
		if (y >= upmost) y = upmost;
	}

	public void render(SpriteBatch batch) {
		batch.draw(sprite, x, y, width * SCALE, height * SCALE);
	}
}
