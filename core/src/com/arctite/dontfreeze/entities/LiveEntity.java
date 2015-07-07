package com.arctite.dontfreeze.entities;

import com.arctite.dontfreeze.WorldScreen;
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
	 * Gets the WorldScreen object that this live entity resides within.
	 *
	 * @return the WorldScreen object
	 */
	public WorldScreen getWorld();

	/**
	 * Strikes this monster, dealing damage and potentially putting it into an Action = KNOCKBACK state.
	 *
	 * @param damage the amount of damage the projectile deals
	 * @param from The direction that the hit is coming from
	 */
	public void hit(int damage, Direction from);

	/**
	 * Gets the amount of damage this live entity's special attacks (projectiles) deal upon impact. Returns 0 if the
	 * monster can't deal special attacks.
	 *
	 * @return amount of damage this entity's special attacks do
	 */
	public int getSpecialDamage();

	/**
	 * Gets the amount of damage this live entity's melee attacks deal upon contact.
	 *
	 * @return amount of damage this entity's melee attacks do
	 */
	public int getMeleeDamage();

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
