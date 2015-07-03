package com.arctite.dontfreeze.util;

import com.arctite.dontfreeze.entities.Action;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;

/**
 * Details each Collectable, Entity (Monster), and Projectile in the game, containing all relevant information such as
 * file locations, sprite width/height, id numbers, etc.
 *
 * @TODO: load these values from file instead of hardcoding into this enum class
 * @TODO: collision boundary definitions for each type
 *
 * Created by Quasar on 27/06/2015.
 */
public enum ResourceInfo {

	// collectables
	FIRE(Type.COLLECTABLE, 1, 0, 50, 65),

	// live entities (player and monsters)
	// NOTE: melee attack ranges are hardcoded in the Monster constructor
	PLAYER(Type.ENTITY, 0, 150, 80, 95),
	SNOW_BABY(Type.ENTITY, 1, 30, 55, 50), // 55 x 50
	SNOW_MONSTER(Type.ENTITY, 10, 60, 200, 200), // 200 x 200

	// projectiles
	PLAYER_PROJECTILE(Type.PROJECTILE, PLAYER.getId(), 180, 70, 60),
	SNOW_MONSTER_PROJECTILE(Type.PROJECTILE, SNOW_MONSTER.getId(), 180, 70, 100); // @TODO fix snow monster values
	;

	/**
	 * Here, set:
	 * - animation framerates for each relevant Action type
	 * - melee attack ranges for monsters
	 */
	static {
		// FRAMERATES
		// collectables
		// fire
		FIRE.frameRates.put(Action.IDLE_MOVE, 0.1F);

		// player
		PLAYER.frameRates.put(Action.IDLE_MOVE, 0.12F);
		PLAYER.frameRates.put(Action.MELEE, 0.12F);
		PLAYER.frameRates.put(Action.SPECIAL, 0.12F);
		PLAYER.frameRates.put(Action.KNOCKBACK, 0.12F);
		PLAYER.frameRates.put(Action.EXPIRING, 0.2F);

		// monsters
		// snow baby
		SNOW_BABY.frameRates.put(Action.IDLE_MOVE, 0.1F);
		SNOW_BABY.frameRates.put(Action.MELEE, 0.1F);
		// SNOW_BABY.frameRates.put(Action.SPECIAL, 0.1F); // snow baby has no special attack
		SNOW_BABY.frameRates.put(Action.KNOCKBACK, 0.15F);
		SNOW_BABY.frameRates.put(Action.EXPIRING, 0.3F);
		// snow monster
		SNOW_MONSTER.frameRates.put(Action.IDLE_MOVE, 0.15F);
		SNOW_MONSTER.frameRates.put(Action.MELEE, 0.1F);
		SNOW_MONSTER.frameRates.put(Action.SPECIAL, 0.1F);
		SNOW_MONSTER.frameRates.put(Action.KNOCKBACK, 0.15F);
		SNOW_MONSTER.frameRates.put(Action.EXPIRING, 0.2F);

		// projectiles
		// player projectile
		PLAYER_PROJECTILE.frameRates.put(Action.INITIALISING, 0.1F);
		PLAYER_PROJECTILE.frameRates.put(Action.LOOPING, 0.1F);
		PLAYER_PROJECTILE.frameRates.put(Action.EXPIRING, 0.1F);
		// monster projectile
		SNOW_MONSTER_PROJECTILE.frameRates.put(Action.INITIALISING, 0.1F);
		SNOW_MONSTER_PROJECTILE.frameRates.put(Action.LOOPING, 0.1F);
		SNOW_MONSTER_PROJECTILE.frameRates.put(Action.EXPIRING, 0.1F);


		// MONSTER ATTACK STUFFS (MELEE RANGE, SPECIAL CAPABILITY
		// snow baby
		SNOW_BABY.meleeRangeX = SNOW_BABY.width / 2F;
		SNOW_BABY.meleeRangeY = SNOW_BABY.height / 2F;
		// snow monster melee range
		SNOW_MONSTER.meleeRangeX = SNOW_MONSTER.width / 3F;
		SNOW_MONSTER.meleeRangeY = SNOW_MONSTER.height / 3F;
		// snow monster special attack
		SNOW_MONSTER.special = true;
		SNOW_MONSTER.specialRange = 200;
		// snow monster collision offset
		SNOW_MONSTER.specialOriginOffset = new Rectangle(0, 30, 0, 20);
	}

	private static final String EXT = ".atlas";

	/** Resource type */
	private Type type;

	/** ID number */
	private int id;

	/** Travel speed */
	private int speed;

	/** Framerates mapped by Actions */
	private HashMap<Action, Float> frameRates;

	/** Height/width/melee-range for monsters only */
	private int height;
	private int width;
	private float meleeRangeX;
	private float meleeRangeY;
	private boolean special;
	private int specialRange; // range that this entity's projectile can travel
	/** Collision offset */
	private Rectangle specialOriginOffset;

	private ResourceInfo(Type type, int id, int speed, int width, int height) {
		this.type = type;
		this.id = id;
		this.speed = speed;
		this.width = width;
		this.height = height;

		this.frameRates = new HashMap<Action, Float>();
		this.special = false;
	}

	public Type getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public int getSpeed() {
		return speed;
	}

	/**
	 * Gets the framerates map
	 *
	 * @return the framerates map
	 */
	public HashMap<Action, Float> getFrameRates() {
		return frameRates;
	}

	public float getMeleeRangeX() {
		return meleeRangeX;
	}

	public float getMeleeRangeY() {
		return meleeRangeY;
	}

	public boolean canSpecial() {
		return special;
	}

	public int getSpecialRange() {
		return specialRange;
	}

	public String getLocation() {
		return type.getLocation() + id + EXT;
	}

	public int getWidth() {
		if (width == -1) throw new UnsupportedOperationException("id = " + id + ", doesn't have width/height info");
		return width;
	}

	public int getHeight() {
		if (height == -1) throw new UnsupportedOperationException("id = " + id + ", doesn't have width/height info");
		return height;
	}

	public Rectangle getSpecialOriginOffset() {
		return specialOriginOffset;
	}

	/**
	 * Gets the ResourceInfo enum object with given type and given id.
	 * @param type the type of the info to get
	 * @param id the id of the info to get
	 * @return the info, or null if there is no info with the given type and id combination
	 */
	public static ResourceInfo getByTypeAndId(Type type, int id) {
		for (ResourceInfo info : values()) {
			if (info.getType() == type && info.getId() == id) {
				return info;
			}
		}
		return null;
	}

	public static enum Type {

		COLLECTABLE("collectables"),
		ENTITY("entities"), // refers to both player entity and monster entities
		PROJECTILE("projectiles"),
		;

		private static final String ASSETS = "assets/";
		private static final String SLASH = "/";

		private String location;

		private Type(String location) {
			this.location = location;
		}

		public String getLocation() {
			return ASSETS + location + SLASH;
		}
	}
}
