package com.arctite.dontfreeze;

import com.arctite.dontfreeze.util.GameMessages;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents objects on the Events layer on the maps.
 *
 * Created by Quasar on 29/06/2015.
 */
public class Event {

	/** Types */
	public static final String TYPE_MESSAGE = "message";
	public static final String TYPE_SPAWN = "spawn";
	public static final String TYPE_SET = "set"; // sets event properties

	/** Splitter for set-type event names, and required prop/value pairs */
	public static final String EQUALS = "=";
	/** Splitter for requirements for prop NOT EQUAL to value */
	public static final String DIFFERS = "&lt;&gt;"; // which is <>
	/** Splitter for multipler names/etc */
	public static final String COMMA = ",";

	/** Unique ID of this event, set by Tiled and unmodifiable */
	private int id;
	/** Name of this event, as per Tiled object name property */
	private String[] names;
	/** Type of this event, as per Tiled object type property */
	private String[] types;
	/** Bounds of this Event on the map - player colliding with these bounds triggers this Event */
	private Rectangle bounds;
	/** Triggered flag */
	private boolean triggered;
	/** Requirements for this event to trigger */
	private ArrayList<Requirement> requirements;

	/**
	 * Creates a new Event trigger object with the given name, type, and bounds, as read from the Tiled map file. The
	 * requirements flag is set to false by default. If this event needs requirements set, that must be done through
	 * calling setRequirements()
	 *
	 * @param names names of this event, separated by commas
	 * @param types types of events, separated by commas
	 * @param x bounds x coordinate (bottom left corner)
	 * @param y bounds y coordinate (bottom left corner)
	 * @param width width of the bounds
	 * @param height height of the bounds
	 */
	public Event(int id, String names, String types, int x, int y, float width, float height) {
		this.id = id;
		this.names = names.split(COMMA);
		this.types = types.split(COMMA);
		this.bounds = new Rectangle(x, y, width, height);
		this.triggered = false; // @TODO load/save for events

		this.requirements = new ArrayList<Requirement>();
	}

	/**
	 * Gets the unique identifier number for this particular event.
	 *
	 * @return id of this event
	 */
	public int getId() {
		return id;
	}

	/**
	 * Checks whether or not the given world's event-set properties map contains values that satisfy this event's
	 * requirements, for this event to be triggered. Returns true if this event has no requirements.
	 *
	 * @param props the world's event-set properties map
	 *
	 * @return whether or not this event's requirements (if any) are satisfied, and this event can be triggered
	 */
	public boolean satisfiesRequirements(HashMap<String, String> props) {
		for (Requirement req : requirements) {
			// LHS and RHS equivalent means requirement satisfied
			if (req.getEquals() != req.getValue().equals(props.get(req.getName()))) {
				return false; // this req not satisfied, event can't trigger
			}
		}
		return true;
	}

	/**
	 * Adds a requirement to this event.
	 *
	 * @param equals true for '=', false for '<>'
	 * @param name name of requirement
	 * @param value value of requirement
	 */
	public void addRequirement(boolean equals, String name, String value) {
		Requirement req = new Requirement(equals, name, value);
		requirements.add(req);
	}

	/**
	 * Fires off this event, does NOT check requirements.
	 */
	public void trigger(WorldScreen world) {
		triggered = true;

		for (int i = 0; i < types.length; i++) {
			String name = names[i];
			String type = types[i];
			if (type.equals(TYPE_MESSAGE)) {
				// get the game message corresponding to this event's name
				world.getConvoBox().setMessages(GameMessages.getMessage(name));
			} else if (type.equals(TYPE_SPAWN)) {
				world.spawn(name);
			} else if (type.equals(TYPE_SET)) {
				String[] split = name.split(EQUALS);
				String propName = split[0];
				String propValue = split[1];
				world.setEventProperty(propName, propValue);
			} else {
				// we don't have a handler for this type of event yet
				throw new RuntimeException("unsupported event type: " + type);
			}
		}
	}

	/**
	 * Sets the triggered flag of this event to the given boolean value. Used when loading from save files.
	 *
	 * @param triggered value to set the triggered flag to
	 */
	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
	}

	/**
	 * Checks whether or not this event has been triggered yet.
	 *
	 * @return whether this event has been triggered
	 */
	public boolean hasTriggered() {
		return triggered;
	}

	/**
	 * Gets the bounds of this event. If the player collides with these bounds, this Event is triggered.
	 *
	 * @return the bounds of this event
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * Pair of Strings representing a requirement.
	 */
	public class Requirement {

		private boolean equals;
		private String name;
		private String value;

		private Requirement(boolean equals, String name, String value) {
			this.equals = equals;
			this.name = name;
			this.value = value;
		}

		public boolean getEquals() {
			return equals;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}
}
