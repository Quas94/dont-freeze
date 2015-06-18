package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

/**
 * Created by Quasar on 18/06/2015.
 */
public class AnimationSequence {

	private float stateTime;
	private Animation[] animations;

	private Entity entity;

	/**
	 * Creates an AnimationSequence, to be used by entities in the game world.
	 *
	 * Loads all the frames and does everything related to the animation.
	 *
	 * @param entity The Entity that this Animation is rendering for
	 * @param file The file to pull this Animation's sprites from
	 * @param frameRate How long to hold each frame for before moving onto next
	 */
	public AnimationSequence(Entity entity, String file, float frameRate) {
		this.entity = entity;
		this.stateTime = 0;
		this.animations = new Animation[Direction.values().length];

		ArrayList<Array<TextureAtlas.AtlasRegion>> framesList = new ArrayList<Array<TextureAtlas.AtlasRegion>>();
		for (Direction d : Direction.values()) {
			framesList.add(new Array<TextureAtlas.AtlasRegion>());
		}
		// load animation
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(file));
		for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
			// find corresponding direction of this region/frame
			Direction frameDir = Direction.getByChar(region.name.charAt(0));
			// add this region to the list
			framesList.get(frameDir.getIdx()).add(region);
		}
		for (Direction d : Direction.values()) {
			int i = d.getIdx();
			animations[i] = new Animation(frameRate, framesList.get(i));
			animations[i].setPlayMode(Animation.PlayMode.LOOP);
		}
	}

	public TextureRegion getCurrentFrame() {
		return animations[entity.getDirection().getIdx()].getKeyFrame(stateTime);
	}

	public void update(float delta) {
		stateTime += delta;
	}
}
