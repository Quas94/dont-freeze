package com.arctite.dontfreeze.entities;

/**
 * Enumeration of possible actions an Entity can currently be undertaking, some of which can only be applicable to
 * certain Entity implementations (eg. special attacks are player only)
 *
 * Created by Quasar on 20/06/2015.
 */
public enum Action {

	/** Idle or moving */
	IDLE_MOVE(""), // no prefix
	/** Melee attacking */
	MELEE("m"),
	/** Special attacking */
	SPECIAL("s"),
	/** Being knocked back (and facing the direction named */
	KNOCKBACK("k"),
	/** Dying or fading */
	EXPIRING("e"),
	;

	private String prefix;

	private Action(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}
}
