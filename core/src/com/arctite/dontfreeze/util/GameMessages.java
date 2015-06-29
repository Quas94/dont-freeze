package com.arctite.dontfreeze.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * Static singleton class which loads and holds all game messages (that are triggered by Events).
 *
 * Created by Quasar on 29/06/2015.
 */
public class GameMessages {

	/** Symbol which partitions a multi-message String */
	public static final String SEPARATOR = "~";
	/** Symbol which splits a property key into event-name and part number */
	private static final String NAME_SEPARATOR = ".";

	/** .dfd is dont-freeze-data file ext */
	private static final String MESSAGES_FILE_LOCATION = "assets/data/messages.dfd";

	private static HashMap<String, String> messages;

	/**
	 * Loads all messages from file
	 */
	public static void loadMessages() {
		messages = new HashMap<String, String>();

		FileHandle messagesFile = Gdx.files.internal(MESSAGES_FILE_LOCATION);
		Properties props = new Properties();
		try {
			props.load(messagesFile.read());
		} catch (IOException ioe) {
			System.err.println("there was a problem loading message file props: " + ioe.toString());
			ioe.printStackTrace(System.err);
		}
		HashSet<String> processed = new HashSet<String>(); // contains all events that have been processed already
		for (String prop : props.stringPropertyNames()) {
			String eventName = prop.split("\\" + NAME_SEPARATOR)[0]; // escape period for regex
			if (!processed.contains(eventName)) { // this event hasn't had its parts processed yet
				processed.add(eventName); // set as processed
				int i = 1;
				String message = new String();
				while (props.containsKey(eventName + NAME_SEPARATOR + i)) {
					String messagePart = props.getProperty(eventName + NAME_SEPARATOR + i);
					message += messagePart + SEPARATOR;
					i++;
				}
				message = message.substring(0, message.length() - 1); // chop off the last SEPARATOR
				messages.put(eventName, message); // add to hashmap
			}
		}
	}

	/**
	 * Gets a message by the event name.
	 *
	 * @param name the event name
	 * @return the event message
	 */
	public static String getMessage(String name) {
		if (!messages.containsKey(name)) {
			throw new RuntimeException("no event message with given name: " + name);
		}
		return messages.get(name);
	}

	private GameMessages() {
	}
}
