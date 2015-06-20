package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

/**
 * Created by Quasar on 18/06/2015.
 */
public class AnimationSequence {

	/** Only one direction animation sequence */
	public static final int UNI_DIR = 0;
	/** Multidirectional animation sequence - loads all directions from spritesheet */
	public static final int MULTI_DIR = 1;
	/** Multidirectional animation sequence - loads only one direction from spritesheet, rotates to make other dirs */
	public static final int MULTI_DIR_CLONE = 2;

	private float stateTime;
	private Animation[] animations;

	private Entity entity;

	/** The type of animation this is */
	private int type;

	/**
	 * Creates an AnimationSequence, to be used by entities in the game world.
	 *
	 * Loads all the frames and does everything related to the animation.
	 *
	 * @param type The type of animation sequence it is (see this class' constants)
	 * @param entity The Entity that this Animation is rendering for
	 * @param file The file to pull this Animation's sprites from
	 * @param frameRate How long to hold each frame for before moving onto next
	 */
	public AnimationSequence(int type, Entity entity, String file, float frameRate) {
		int dirs;
		if (type == UNI_DIR) dirs = 1;
		else dirs = Direction.values().length; // number of directions this animation will face

		this.entity = entity;
		this.stateTime = 0;
		this.animations = new Animation[dirs];

		ArrayList<Array<TextureRegion>> framesList = new ArrayList<Array<TextureRegion>>();
		for (int i = 0; i < dirs; i++) {
			framesList.add(new Array<TextureRegion>());
		}
		// load animation
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(file));
		for (AtlasRegion region : atlas.getRegions()) {
			if (type == UNI_DIR) { // only one direction for uni dir type animations
				framesList.get(0).add(region); // add to index 0
			} else if (type == MULTI_DIR) {
				Direction frameDir = Direction.getByChar(region.name.charAt(0));
				// add this region to the list using direction index
				framesList.get(frameDir.getIdx()).add(region);
			} else if (type == MULTI_DIR_CLONE) {
				// only one direction type in sprite sheets, but we copy and rotate
				// direction type in sprite sheets will be facing RIGHT
				framesList.get(Direction.RIGHT.getIdx()).add(region);
				// left is flip
				TextureRegion left = new TextureRegion(region);
				left.flip(true, false); // flip x but not y
				framesList.get(Direction.LEFT.getIdx()).add(left);
			}
		}
		for (int i = 0; i < dirs; i++) {
			animations[i] = new Animation(frameRate, framesList.get(i));
			animations[i].setPlayMode(Animation.PlayMode.LOOP);
		}
	}

	public void setAnimations(Animation[] animations) {
		this.animations = animations;
	}

	public TextureRegion getCurrentFrame(Direction dir) {
		return animations[dir.getIdx()].getKeyFrame(stateTime);
	}

	public void update(float delta) {
		stateTime += delta;
	}
}
