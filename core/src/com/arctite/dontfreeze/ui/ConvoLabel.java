package com.arctite.dontfreeze.ui;

import com.arctite.dontfreeze.GameMain;
import com.arctite.dontfreeze.util.GameMessages;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The UI element which holds text events/conversation in the game.
 *
 * Created by Quasar on 29/06/2015.
 */
public class ConvoLabel {

	/** Convo Label attributes */
	public static final int OUTER_PADDING = 10; // gap on both left/right sides
	public static final int CONVO_WIDTH = GameMain.GAME_WINDOW_WIDTH - (OUTER_PADDING * 2); // width of outer label
	public static final int CONVO_HEIGHT = GameMain.GAME_WINDOW_HEIGHT / 6; // height of outer label
	/** Inner label (text) attributes (wrt. to outer label) */
	private static final int INNER_PADDING = 10; // gap between outer and inner labels
	private static final int INNER_TOTAL_PADDING = OUTER_PADDING + INNER_PADDING; // absolute gap of inner (with window)
	private static final int INNER_WIDTH = CONVO_WIDTH - (INNER_PADDING * 2);
	private static final int INNER_HEIGHT = CONVO_HEIGHT - (INNER_PADDING * 2);
	/** Convo label colours (r, g, b, a) */
	public static final float[] CLC = new float[] { 192, 192, 192, 0.45F };

	/** The label which contains the background and the inner label */
	private Label outerLabel;

	/** The label which contains the text */
	private Label innerLabel;

	/** Queued messages (for very long messages, or messages that need to be broken up */
	private Queue<String> queue;

	/**
	 * Creates a new ConvoLabel, which is composed of an outer Label (providing the semi-transparent background) and an
	 * inner Label (container for the text).
	 */
	public ConvoLabel() {
		this.queue = new LinkedList<String>();

		Skin skin = SkinManager.getSkin(SkinManager.CONVO_LABEL_SKIN);

		// load outer label
		this.outerLabel = new Label(null, skin);
		outerLabel.setSize(CONVO_WIDTH, CONVO_HEIGHT);
		outerLabel.setPosition(OUTER_PADDING, OUTER_PADDING);

		// load inner label
		Label.LabelStyle innerStyle = new Label.LabelStyle();
		innerStyle.font = SkinManager.getFont();
		innerStyle.fontColor = Color.BLACK;
		this.innerLabel = new Label(null, innerStyle);
		innerLabel.setAlignment(Align.topLeft);
		innerLabel.setWrap(true);
		innerLabel.setSize(INNER_WIDTH, INNER_HEIGHT);
		innerLabel.setPosition(INNER_TOTAL_PADDING, INNER_TOTAL_PADDING);

		// not active, need to be triggered by events
		setActive(false);
	}

	/**
	 * Sets the text for this ConvoLabel.
	 *
	 * Very long messages must be broken up by the SEPARATOR symbol (currently tilda ~) otherwise they will overflow
	 * the label and off the bottom of the screen.
	 *
	 * If a message is partitioned, they are stored in the queue.
	 *
	 * This method sets the ConvoLabel to visible.
	 *
	 * @param text the text to set to
	 */
	public void setMessages(String text) {
		// new messages can only be set when there is nothing currently going on with this convo label:
		// ie. either still displaying a message, or having messages remaining in the queue
		if (isActive() || !queue.isEmpty()) {
			throw new RuntimeException("attempting to set new message(s) where there is still stuff to be processed!");
		}
		if (text.contains(GameMessages.SEPARATOR)) { // split and queue
			String[] messages = text.split(GameMessages.SEPARATOR);
			innerLabel.setText(messages[0]); // push first message to the label
			for (int i = 1; i < messages.length; i++) { // queue the rest
				queue.add(messages[i]);
			}
		} else { // single message
			innerLabel.setText(text);
		}
		setActive(true);
	}

	/**
	 * If the queue is empty, false is returned.
	 * If the queue is not empty, the next message is pulled out and set on the screen and true is returned.
	 *
	 * @return true if the queue was empty, false otherwise
	 */
	public boolean nextMessage() {
		if (!isActive()) {
			throw new RuntimeException("ConvoLabel.nextMessage() called when inactive");
		}
		boolean notEmpty = !queue.isEmpty();
		if (notEmpty) {
			// serve up the next queued item
			String message = queue.poll();
			innerLabel.setText(message);
		}
		setActive(notEmpty);
		return notEmpty;
	}

	/**
	 * Sets this entire ConvoLabel's visibility.
	 */
	public void setActive(boolean active) {
		outerLabel.setVisible(active);
		innerLabel.setVisible(active);
	}

	/**
	 * Gets this ConvoLabel's visibility.
	 * @return whether this ConvoLabel is visible
	 */
	public boolean isActive() {
		return outerLabel.isVisible();
	}

	/**
	 * Adds all components of this ConvoLabel to the given Stage.
	 *
	 * @param stage the Stage to add to
	 */
	public void addToStage(Stage stage) {
		stage.addActor(outerLabel);
		stage.addActor(innerLabel);
	}
}
