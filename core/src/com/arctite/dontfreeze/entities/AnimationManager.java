package com.arctite.dontfreeze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

/**
 * Contains and processes the animation sequences for Players and all other Entities that need to be animated.
 *
 * Created by Quasar on 18/06/2015.
 */
public class AnimationManager {

	/** Only one direction animation sequence */
	public static final int UNI_DIR = 0;
	/** Multi-directional animation sequence - loads all directions from spritesheet */
	public static final int MULTI_DIR = 1;
	/** Multi-directional animation sequence - loads only one direction from spritesheet, rotates to make other dirs */
	public static final int MULTI_DIR_CLONE = 2;

	private float stateTime;
	private HashMap<String, Animation> animations;

	/** The Entity that this animation manager is linked to */
	private Entity entity;
	/** The last action that this entity was in the process of completing, when getCurrentFrame() was last called */
	private Action lastAction;
	/** Prefix denoting the current animation type */
	private String prefix;
	/** Animation loop time @TODO this is only for 1 type of frame, need to do diff types */
	private float animationTime;

	/**
	 * Creates an AnimationSequence, to be used by entities in the game world. Loads all the frames and does everything
	 * related to the animation.
	 *
	 * NOTE: Entity's direction and action MUST be set BEFORE calling this constructor.
	 *
	 * @TODO make this entire constructor cleaner - really messy right now
	 *
	 * @param type The type of animation sequence it is (see this class' constants)
	 * @param entity The Entity that this Animation is rendering for
	 * @param file The file to pull this Animation's sprites from
	 * @param frameRate How long to hold each frame for before moving onto next
	 */
	public AnimationManager(int type, Entity entity, String file, float frameRate) {
		this.entity = entity;
		this.lastAction = entity.getAction();
		this.prefix = lastAction.getPrefix() + entity.getDirection().getChar();

		this.stateTime = 0;
		this.animations = new HashMap<String, Animation>();

		// initialise initial HashMap
		HashMap<String, Array<TextureRegion>> loadedFrames = new HashMap<String, Array<TextureRegion>>();

		// load animation
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(file));
		for (AtlasRegion region : atlas.getRegions()) {
			// initialise the Array within the HashMap with given key, if it hasn't already been initialised
			String prefix = region.name.replaceAll("[0-9]", ""); // prefix = type of frame this is, can be 1 or 2 letters
			if (!loadedFrames.containsKey(prefix)) {
				loadedFrames.put(prefix, new Array<TextureRegion>());
			}
			Array<TextureRegion> array = loadedFrames.get(prefix);

			if (type == UNI_DIR) { // only one direction for uni dir type animations
				// all UNI_DIR typed animations should have region prefixes of only 'd' (DOWN)
				loadedFrames.get(prefix).add(region);
			} else if (type == MULTI_DIR) {
				// add this region to the array with appropriate prefix
				array.add(region);
			} else if (type == MULTI_DIR_CLONE) {
				// only one direction type in sprite sheets, but we copy and rotate
				// direction type in sprite sheets will all be facing RIGHT
				boolean loop = prefix.equals(Action.LOOPING.getPrefix());
				boolean expiring = prefix.equals(Action.EXPIRING.getPrefix());
				boolean initialising = prefix.equals(Action.INITIALISING.getPrefix());
				if (!loop && !expiring && !initialising) {
					throw new IllegalStateException("non-proj texture in MULTI_DIR_CLONE AnimationManager constructor");
				}

				String prefixRight = prefix + Direction.RIGHT.getChar();
				String prefixLeft = prefix + Direction.LEFT.getChar();
				String prefixUp = prefix + Direction.UP.getChar();
				String prefixDown = prefix + Direction.DOWN.getChar();
				String[] prefixes = new String[] { prefixRight, prefixLeft, prefixUp, prefixDown };
				// create Array objects if not already done, in the map
				for (String s : prefixes) {
					if (!loadedFrames.containsKey(s)) {
						loadedFrames.put(s, new Array<TextureRegion>());
					}
				}
				Array<TextureRegion> arrayLeft = loadedFrames.get(prefixLeft);
				Array<TextureRegion> arrayRight = loadedFrames.get(prefixRight);
				Array<TextureRegion> arrayUp = loadedFrames.get(prefixUp);
				Array<TextureRegion> arrayDown = loadedFrames.get(prefixDown);

				// copy and put into array
				arrayRight.add(region);
				arrayDown.add(region); // rotation is done in Projectile.render() by the spritebatch
				// left is flip
				TextureRegion left = new TextureRegion(region);
				left.flip(true, false); // flip x but not y
				arrayLeft.add(left);
				arrayUp.add(left); // rotation is done in Projectile.render() by the spritebatch
			}
		}
		for (String prefix : loadedFrames.keySet()) {
			// @TODO support differing framerates for differing actions
			Animation anim = new Animation(frameRate, loadedFrames.get(prefix));
			// @TODO support differing framemode types for differing actions
			anim.setPlayMode(Animation.PlayMode.LOOP); // loop for majority of animations
			if (type == MULTI_DIR_CLONE) { // check for non-looping ones
				if (prefix.equals(Action.EXPIRING) || prefix.equals(Action.INITIALISING)) {
					anim.setPlayMode(Animation.PlayMode.NORMAL); // projectile expire/init do not loop
				}
			}
			animations.put(prefix, anim);
		}

		// set animation time - how long one cycle takes
		// get any animation at from the anims list
		Animation randAnim = animations.values().iterator().next(); // will throw exception here if empty animation,
		this.animationTime = randAnim.getAnimationDuration();
	}

	/**
	 * Finds how long this Animation takes to do one complete cycle.
	 *
	 * @return animation time
	 */
	public float getAnimationTime() {
		return animationTime;
	}

	/**
	 * Sets the stateTime to a given random value.
	 *
	 * @param randomStateTime random state time
	 */
	public void setRandomStateTime(float randomStateTime) {
		stateTime = randomStateTime;
	}

	/**
	 * Returns whether the current non-looped animation has completed.
	 *
	 * @return completion status
	 */
	public boolean isComplete() {
		if (prefix != null) {
			return stateTime >= animations.get(prefix).getAnimationDuration();
		}
		return false;
	}

	/**
	 * Updates the action of the entity. This method is required because Player.update() has an internal loop which
	 * can iterate multiple times at WorldScreen.DELTA_STEP ms per step, which means the Player can go from Attack to
	 * Idle to Attack all within one update() call, which can leave this animation manager completely oblivious to
	 * the action changes, leading it to believe that it's just permanently attacking. This will mess up the
	 * isComplete() method which will in turn mess up the Player.update() method.
	 *
	 * Thus, this method must be called whenever the Player's action is altered.
	 *
	 * If the action is different to the previous action stored by this animation manager, the state time will be reset.
	 *
	 * @param action
	 */
	public void updateAction(Action action) {
		if (lastAction != action) {
			lastAction = action;
			stateTime = 0;
		}
	}

	/**
	 * Gets the current frame of the animation.
	 *
	 * Checks the corresponding Entity's direction and action, and if there were changes, start animating from the
	 * start of the new animation (ie. resets stateTime to 0).
	 *
	 * @TODO use entity.getDirection() instead of accepting dir as parameter (need to make getDirection() and the
	 * direction supplied in render->getCurrentFrame consistent in a few classes, first)
	 *
	 * @param dir the direction to get from the animations array
	 * @return the relevant TextureRegion frame
	 */
	public TextureRegion getCurrentFrame(Direction dir) {
		prefix = lastAction.getPrefix() + dir.getChar();
		//if(entity instanceof Player)prefix = prefix.replaceAll("s", "z").replaceAll("k", "s").replaceAll("z", "k");
		try {
			return animations.get(prefix).getKeyFrame(stateTime);
		} catch (NullPointerException npe) {
			System.out.println("prefix = " + prefix + ", animations.get(prefix) = " + animations.get(prefix));
			throw new RuntimeException("missing frame - see above line");
		}
	}

	public void update(float delta) {
		stateTime += delta;
	}
}
