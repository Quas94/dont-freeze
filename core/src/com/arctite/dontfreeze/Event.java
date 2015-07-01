package com.arctite.dontfreeze;

import com.arctite.dontfreeze.util.GameMessages;
import com.badlogic.gdx.math.Rectangle;

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
	/** Custom properties */
	public static final String REQUIREMENT = "req";

	/** Splitter for set-type event names */
	public static final String EQUALS = "=";

	/** Name of this event, as per Tiled object name property */
	private String name;
	/** Type of this event, as per Tiled object type property */
	private String type;
	/** Bounds of this Event on the map - player colliding with these bounds triggers this Event */
	private Rectangle bounds;
	/** Triggered flag */
	private boolean triggered;
	/** Requirements for this event to trigger */
	private boolean hasReq;
	private String reqName;
	private String reqValue;

	/**
	 * Creates a new Event trigger object with the given name, type, and bounds, as read from the Tiled map file. The
	 * requirements flag is set to false by default. If this event needs a requirement set, that must be done through
	 * calling setRequirement()
	 *
	 * @param name name of this event
	 * @param type type of event
	 * @param x bounds x coordinate (bottom left corner)
	 * @param y bounds y coordinate (bottom left corner)
	 * @param width width of the bounds
	 * @param height height of the bounds
	 */
	public Event(String name, String type, int x, int y, float width, float height) {
		this.name = name;
		this.type = type;
		this.bounds = new Rectangle(x, y, width, height);
		this.triggered = false; // @TODO load/save for events

		this.hasReq = false;
	}

	/**
	 * Checks whether or not this event has a property requirement to be triggered.
	 *
	 * @return whether this event has a property requirement
	 */
	public boolean hasRequirement() {
		return hasReq;
	}

	/**
	 * Gets this event's requirement name.
	 *
	 * @return requirement name
	 */
	public String getRequirementName() {
		return reqName;
	}

	/**
	 * Gets this event's requirement value.
	 *
	 * @return
	 */
	public String getRequirementValue() {
		return reqValue;
	}

	/**
	 * Sets this event to have a requirement, with the given property name and value to satisfy.
	 *
	 * @param name name of property
	 * @param value value of property
	 */
	public void setRequirement(String name, String value) {
		hasReq = true;
		reqName = name;
		reqValue = value;
	}

	/**
	 * Fires off this event, does NOT check requirements.
	 */
	public void trigger(WorldScreen world) {
		triggered = true;

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
	 * Gets the name of this event, as per defined in the object name property, on the Tiled map.
	 *
	 * @return the name of this event
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type of this event, as per defined in the object type property, on the Tiled map.
	 *
	 * @return the type of this event
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the bounds of this event. If the player collides with these bounds, this Event is triggered.
	 *
	 * @return the bounds of this event
	 */
	public Rectangle getBounds() {
		return bounds;
	}
}
