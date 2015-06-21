package com.tbd.dontfreeze.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * An extension of the generic Entity interface, to be implemented by Monsters and Players.
 *
 * Contains additional methods involving combat and other game-mechanic related stuff.
 *
 * Created by Quasar on 21/06/2015.
 */
public interface LiveEntity extends Entity {

	/** Melee attacks are delayed by this many seconds before actually affecting targets */
	public static final float MELEE_ATTACK_DELAY = 0.2F;

	/**
	 * Gets this LiveEntity's maximum health.
	 */
	public int getMaxHealth();

	/**
	 * Gets this LiveEntity's current health.
	 */
	public int getHealth();

	/**
	 * Strikes this monster, dealing damage and potentially putting it into an Action = KNOCKBACK state.
	 *
	 * @param from The direction that the hit is coming from
	 */
	public void hit(Direction from);

	/**
	 * Gets this Entity's melee attack collision bounds.
	 *
	 * The Rectangle object that this method returns is customized, and in most cases is NOT equivalent to a Rectangle
	 * created by Rectangle(getX(), getY(), getWidth(), getHeight())
	 *
	 * @return the Entity's melee attack collision bounds within a Rectangle object
	 */
	public Rectangle getAttackCollisionBounds();

	/**
	 * Gets this Entity's defending collision bounds (whole body).
	 *
	 * The Rectangle object that this method returns is customized, and in most cases is NOT equivalent to a Rectangle
	 * created by Rectangle(getX(), getY(), getWidth(), getHeight())
	 *
	 * @return the Entity's defending collision bounds
	 */
	public Rectangle getDefenseCollisionBounds();
}
