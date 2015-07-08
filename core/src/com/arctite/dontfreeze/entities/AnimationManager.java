package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.util.ResourceInfo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	 * Represents a frame within the texture atlas. Effectively a pair structure, linking the frame's order number
	 * (within the particular animation) to the actual texture region object.
	 */
	private static class Frame {

		private int number;
		private TextureRegion region;

		/**
		 * Creates a Frame with the given frame number and texture region object.
		 *
		 * @param number the frame number
		 * @param region the texture region
		 */
		private Frame(int number, TextureRegion region) {
			this.number = number;
			this.region = region;
		}
	}

	/**
	 * Creates an AnimationSequence, to be used by entities in the game world. Loads all the frames and does everything
	 * related to the animation.
	 *
	 * NOTE: Entity's direction and action MUST be set BEFORE calling this constructor.
	 *
	 * @TODO make this entire constructor cleaner - really messy right now
	 * @TODO stop loading from file every time a new AnimationManager is constructed (ie. when an entity is constructed)
	 *
	 * @param type The type of animation sequence it is (see this class' constants)
	 * @param entity The Entity that this Animation is rendering for
	 * @param info The ResourceInfo containing relevant information on file locations and framerate
	 */
	public AnimationManager(int type, Entity entity, ResourceInfo info) {
		this.entity = entity;
		this.lastAction = entity.getAction();
		this.prefix = lastAction.getPrefix() + entity.getDirection().getChar();

		this.stateTime = 0;
		this.animations = new HashMap<String, Animation>();

		// initialise initial HashMap
		HashMap<String, ArrayList<Frame>> loadedFrames = new HashMap<String, ArrayList<Frame>>();

		// load animation
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(info.getLocation()));
		for (AtlasRegion region : atlas.getRegions()) {
			// initialise the Array within the HashMap with given key, if it hasn't already been initialised
			int number = Integer.parseInt(region.name.replaceAll("[a-zA-Z]", "")); // number of frame (removed prefix)
			Frame frame = new Frame(number, region);
			String prefix = region.name.replaceAll("[0-9]", ""); // prefix = type of frame this is, 1 or 2 letters
			if (!loadedFrames.containsKey(prefix)) {
				loadedFrames.put(prefix, new ArrayList<Frame>());
			}
			ArrayList<Frame> array = loadedFrames.get(prefix);

			if (type == UNI_DIR) { // only one direction for uni dir type animations
				// UNI_DIR animations must have region prefixes of either 'd' or 'a' ('a' only for animated obstacles)
				loadedFrames.get(prefix).add(frame);
			} else if (type == MULTI_DIR) {
				// add this region to the array with appropriate prefix
				array.add(frame);
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
						loadedFrames.put(s, new ArrayList<Frame>());
					}
				}
				ArrayList<Frame> arrayLeft = loadedFrames.get(prefixLeft);
				ArrayList<Frame> arrayRight = loadedFrames.get(prefixRight);
				ArrayList<Frame> arrayUp = loadedFrames.get(prefixUp);
				ArrayList<Frame> arrayDown = loadedFrames.get(prefixDown);

				// copy and put into array
				arrayRight.add(frame);
				arrayDown.add(frame); // rotation is done in Projectile.render() by the spritebatch
				// left is flip
				TextureRegion leftTex = new TextureRegion(region);
				leftTex.flip(true, false); // flip x but not y
				Frame left = new Frame(number, leftTex);
				arrayLeft.add(left);
				arrayUp.add(left); // rotation is done in Projectile.render() by the spritebatch
			}
		}
		for (String prefix : loadedFrames.keySet()) {
			String actionPrefix = new String(prefix);
			Action action;
			actionPrefix = actionPrefix.substring(0, 1); // get first letter if prefix is 2 letters (2nd letter is dir)
			action = Action.getByPrefix(actionPrefix);
			if (action == null) action = Action.IDLE_MOVE; // u, d, l, r are IDLE_MOVE
			HashMap<Action, Float> frameRates = info.getFrameRates();
			if (!frameRates.containsKey(action)) {
				throw new RuntimeException("frameRates for " + info.toString() + " doesn't contain action " + action);
			}
			ArrayList<Frame> unsortedFrames = loadedFrames.get(prefix);
			// sort the frames and then copy into libgdx array object that can be used by the Animation constructor
			Collections.sort(unsortedFrames, new Comparator<Frame>() {
				@Override
				public int compare(Frame f1, Frame f2) {
					if (f1.number < f2.number) {
						return -1;
					} else if (f1.number > f2.number) {
						return 1;
					}
					return 0;
				}
			});
			// copy into libgdx array now that we've sorted
			Array<TextureRegion> sortedArray = new Array<TextureRegion>();
			for (Frame frame : unsortedFrames) {
				sortedArray.add(frame.region);
			}

			Animation anim = new Animation(frameRates.get(action), sortedArray);
			// @TODO support differing framemode types for differing actions
			anim.setPlayMode(Animation.PlayMode.LOOP); // loop for majority of animations
			if (prefix.startsWith(Action.EXPIRING.getPrefix()) || prefix.equals(Action.INITIALISING.getPrefix())) {
				anim.setPlayMode(Animation.PlayMode.NORMAL); // expire/init do not loop
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
		try {
			return animations.get(prefix).getKeyFrame(stateTime);
		} catch (NullPointerException npe) {
			System.err.println("entity = " + entity.toString() + "prefix = " + prefix + ", animations.get(prefix) = " +
					animations.get(prefix));
			throw new RuntimeException("missing frame - see above line");
		}
	}

	public void update(float delta) {
		stateTime += delta;
	}
}
