package com.arctite.dontfreeze.util;

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
	FIRE(Type.COLLECTABLE, 1, 0, 0.1F, 50, 65),

	// live entities (player and monsters)
	PLAYER(Type.ENTITY, 0, 150, 0.12F, 80, 95),
	SNOW_BABY(Type.ENTITY, 1, 30, 0.1F, 55, 50), // 55 x 50
	SNOW_MONSTER(Type.ENTITY, 10, 60, 0.15F, 200, 200), // 200 x 200

	// projectiles
	PLAYER_PROJECTILE(Type.PROJECTILE, PLAYER.getId(), 180, 0.1F, 70, 60),
	SNOW_MONSTER_PROJECTILE(Type.PROJECTILE, SNOW_MONSTER.getId(), 180,  0.1F, 70, 60); // @TODO fix snow monster values
	;

	private static final String EXT = ".atlas";

	/** Resource type */
	private Type type;

	/** ID number */
	private int id;

	/** Travel speed */
	private int speed;

	/** Framerate */
	private float frameRate;

	/** Height/width for monsters only */
	private int height;
	private int width;

	private ResourceInfo(Type type, int id, int speed, float frameRate, int width, int height) {
		this.type = type;
		this.id = id;
		this.speed = speed;
		this.frameRate = frameRate;
		this.width = width;
		this.height = height;
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

	public float getFrameRate() {
		return frameRate;
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
