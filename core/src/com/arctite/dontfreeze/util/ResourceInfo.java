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
	FIRE_CORE(Type.COLLECTABLE, 10, 0, 70, 70),

	// animated obstacles
	GEYSER(Type.ANIMATED_OBSTACLE, 1, 0, 130, 200),

	// live entities (player and monsters)
	// NOTE: melee attack ranges are hardcoded in the Monster constructor
	PLAYER(Type.ENTITY, 0, 200, 80, 95),
	SNOW_BABY(Type.ENTITY, 1, 30, 55, 50), // 55 x 50
	SNOW_BRO(Type.ENTITY, 2, 60, 120, 100),
	SNOW_RABBIT(Type.ENTITY, 3, 90, 100, 100),
	SNOW_MONSTER(Type.ENTITY, 10, 120, 200, 200), // 200 x 200
	ICE_ELEMENTAL(Type.ENTITY, 20, 160, 150, 140), // final boss

	// projectiles
	PLAYER_PROJECTILE(Type.PROJECTILE, PLAYER.getId(), 180, 70, 60),
	SNOW_MONSTER_PROJECTILE(Type.PROJECTILE, SNOW_MONSTER.getId(), 180, 70, 100),
	ICE_ELEMENTAL_PROJECTILE(Type.PROJECTILE, ICE_ELEMENTAL.getId(), 180, 140, 100),
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
		FIRE_CORE.frameRates.put(Action.IDLE_MOVE, 0.1F);

		// animated obstacles
		GEYSER.frameRates.put(Action.IDLE_MOVE, 0.2F);
		GEYSER.frameRates.put(Action.ANIMATING, 0.15F);

		// player
		PLAYER.frameRates.put(Action.IDLE_MOVE, 0.12F);
		PLAYER.frameRates.put(Action.MELEE, 0.12F);
		PLAYER.frameRates.put(Action.SPECIAL, 0.12F);
		PLAYER.frameRates.put(Action.KNOCKBACK, 0.12F);
		PLAYER.frameRates.put(Action.EXPIRING, 0.2F);
		// PLAYER ATTACK STUFFS
		PLAYER.meleeDamage = 1;
		PLAYER.specialDamage = 1;
		// player projectile
		PLAYER_PROJECTILE.frameRates.put(Action.INITIALISING, 0.1F);
		PLAYER_PROJECTILE.frameRates.put(Action.LOOPING, 0.1F);
		PLAYER_PROJECTILE.frameRates.put(Action.EXPIRING, 0.1F);

		// SNOW BABY
		SNOW_BABY.frameRates.put(Action.IDLE_MOVE, 0.1F);
		SNOW_BABY.frameRates.put(Action.MELEE, 0.1F);
		// SNOW_BABY.frameRates.put(Action.SPECIAL, 0.1F); // snow baby has no special attack
		SNOW_BABY.frameRates.put(Action.KNOCKBACK, 0.15F);
		SNOW_BABY.frameRates.put(Action.EXPIRING, 0.3F);
		// snow baby
		SNOW_BABY.meleeDamage = 3;
		SNOW_BABY.meleeRangeX = SNOW_BABY.width / 2F;
		SNOW_BABY.meleeRangeY = SNOW_BABY.height / 2F;

		// SNOW BRO
		SNOW_BRO.frameRates.put(Action.IDLE_MOVE, 0.15F);
		SNOW_BRO.frameRates.put(Action.MELEE, 0.15F);
		SNOW_BRO.frameRates.put(Action.KNOCKBACK, 0.15F);
		SNOW_BRO.frameRates.put(Action.EXPIRING, 0.15F);
		SNOW_BRO.meleeDamage = 5;
		SNOW_BRO.meleeRangeX = SNOW_BRO.width / 2F;
		SNOW_BRO.meleeRangeY = SNOW_BRO.height / 2F;

		// SNOW RABBIT
		SNOW_RABBIT.frameRates.put(Action.IDLE_MOVE, 0.12F);
		SNOW_RABBIT.frameRates.put(Action.MELEE, 0.12F);
		SNOW_RABBIT.frameRates.put(Action.KNOCKBACK, 0.12F);
		SNOW_RABBIT.frameRates.put(Action.EXPIRING, 0.12F);
		SNOW_RABBIT.meleeDamage = 10;
		SNOW_RABBIT.meleeRangeX = SNOW_RABBIT.width / 2F;
		SNOW_RABBIT.meleeRangeY = SNOW_RABBIT.height / 2F;

		// SNOW MONSTER
		SNOW_MONSTER.frameRates.put(Action.IDLE_MOVE, 0.15F);
		SNOW_MONSTER.frameRates.put(Action.MELEE, 0.1F);
		SNOW_MONSTER.frameRates.put(Action.SPECIAL, 0.1F);
		SNOW_MONSTER.frameRates.put(Action.KNOCKBACK, 0.15F);
		SNOW_MONSTER.frameRates.put(Action.EXPIRING, 0.2F);
		// snow monster damage
		SNOW_MONSTER.meleeDamage = 15;
		SNOW_MONSTER.specialDamage = 15;
		SNOW_MONSTER.meleeRangeX = SNOW_MONSTER.width / 3F;
		SNOW_MONSTER.meleeRangeY = SNOW_MONSTER.height / 3F;
		SNOW_MONSTER.specialRange = 200;
		// snow monster collision offset
		SNOW_MONSTER.specialOriginOffset = new Rectangle(15, 30, -30, 20);
		// monster projectile
		SNOW_MONSTER_PROJECTILE.frameRates.put(Action.INITIALISING, 0.1F);
		SNOW_MONSTER_PROJECTILE.frameRates.put(Action.LOOPING, 0.1F);
		SNOW_MONSTER_PROJECTILE.frameRates.put(Action.EXPIRING, 0.1F);

		// ICE ELEMENTAL (final boss)
		ICE_ELEMENTAL.frameRates.put(Action.IDLE_MOVE, 0.15F);
		ICE_ELEMENTAL.frameRates.put(Action.MELEE, 0.15F);
		ICE_ELEMENTAL.frameRates.put(Action.SPECIAL, 0.15F);
		ICE_ELEMENTAL.frameRates.put(Action.KNOCKBACK, 0.15F);
		ICE_ELEMENTAL.frameRates.put(Action.EXPIRING, 0.15F);
		ICE_ELEMENTAL.meleeDamage = 20;
		ICE_ELEMENTAL.specialDamage = 20;
		ICE_ELEMENTAL.meleeRangeX = SNOW_MONSTER.width / 3F;
		ICE_ELEMENTAL.meleeRangeY = SNOW_MONSTER.height / 3F;
		ICE_ELEMENTAL.specialRange = 250;
		// ice elemental collision offset
		ICE_ELEMENTAL.specialOriginOffset = new Rectangle(10, 20, -20, 60);
		// ice elemental projectile
		ICE_ELEMENTAL_PROJECTILE.frameRates.put(Action.INITIALISING, 0.1F);
		ICE_ELEMENTAL_PROJECTILE.frameRates.put(Action.LOOPING, 0.1F);
		ICE_ELEMENTAL_PROJECTILE.frameRates.put(Action.EXPIRING, 0.1F);
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
	private int meleeDamage;
	private int specialDamage;
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
		this.specialDamage = 0;
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

	public int getMeleeDamage() {
		return meleeDamage;
	}

	/**
	 * Gets the amount of damage that can be done via special attack (projectile). Zero if unable to special attack.
	 *
	 * @return amount of damage done with special attack, or 0 if can't special
	 */
	public int getSpecialDamage() {
		return specialDamage;
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
		ANIMATED_OBSTACLE("animated"), // geyser
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
