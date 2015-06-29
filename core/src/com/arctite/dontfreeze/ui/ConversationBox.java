package com.arctite.dontfreeze.ui;

import com.arctite.dontfreeze.GameMain;
import com.arctite.dontfreeze.util.GameMessages;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
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
public class ConversationBox extends Group {

	/** Convo Label attributes */
	public static final int OUTER_PADDING = 10; // gap on both left/right sides
	public static final int CONVO_WIDTH = GameMain.GAME_WINDOW_WIDTH - (OUTER_PADDING * 2); // width of outer label
	public static final int CONVO_HEIGHT = GameMain.GAME_WINDOW_HEIGHT / 6; // height of outer label
	/** Convo label colours (r, g, b, a) */
	public static final float[] CLC = new float[] { 192, 192, 192, 0.45F };
	/** Inner label (text) attributes (wrt. to outer label) */
	private static final int INNER_PADDING = 10; // gap between outer and inner labels
	private static final int INNER_TOTAL_PADDING = OUTER_PADDING + INNER_PADDING; // absolute gap of inner (with window)
	private static final int INNER_WIDTH = CONVO_WIDTH - (INNER_PADDING * 2);
	private static final int INNER_HEIGHT = CONVO_HEIGHT - (INNER_PADDING * 2);
	/** Speed at which the convo box pops up */
	private static final int SHIFT_SPEED = 500;
	/** Speed at which new messages are rendered, in letters per second */
	private static final int TEXT_SPEED = 60;
	private static final int TEXT_SPEED_FAST = TEXT_SPEED * 6;

	/** The label which contains the background and the inner label */
	private Label outerLabel;

	/** The label which contains the text */
	private Label innerLabel;

	/** Queued messages (for very long messages, or messages that need to be broken up */
	private Queue<String> queue;

	/** Transitioning flag: is this box currently moving in/out? */
	private boolean shifting;
	/** If shifting, is this box moving in or out? */
	private boolean in;
	/** The current message String, which may or may not be entired showing */
	private String currentMessage;
	/** Number of characters in the current message that are currently showing */
	private float charsShowing;
	/** The speed at which the current message is being uncovered at */
	private int textSpeed;

	/**
	 * Creates a new ConvoLabel, which is composed of an outer Label (providing the semi-transparent background) and an
	 * inner Label (container for the text).
	 */
	public ConversationBox() {
		this.queue = new LinkedList<String>();

		// load outer label
		Skin skin = SkinManager.getSkin(SkinManager.CONVO_LABEL_SKIN);
		this.outerLabel = new Label(null, skin);
		outerLabel.setSize(CONVO_WIDTH, CONVO_HEIGHT);
		outerLabel.setPosition(OUTER_PADDING, OUTER_PADDING);
		addActor(outerLabel);

		// load inner label
		Label.LabelStyle innerStyle = new Label.LabelStyle();
		innerStyle.font = SkinManager.getFont();
		innerStyle.fontColor = Color.BLACK;
		this.innerLabel = new Label(null, innerStyle);
		innerLabel.setAlignment(Align.topLeft);
		innerLabel.setWrap(true);
		innerLabel.setSize(INNER_WIDTH, INNER_HEIGHT);
		innerLabel.setPosition(INNER_TOTAL_PADDING, INNER_TOTAL_PADDING);
		addActor(innerLabel);

		setPosition(0, -CONVO_HEIGHT); // set off screen to start off with, so upon first setVisible(true), slides in
		super.setVisible(false);
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
		if (isVisible() || !queue.isEmpty()) {
			throw new RuntimeException("attempting to set new message(s) where there is still stuff to be processed!");
		}
		if (text.contains(GameMessages.SEPARATOR)) { // split and queue
			String[] messages = text.split(GameMessages.SEPARATOR);
			currentMessage = messages[0]; // push first message to the label
			for (int i = 1; i < messages.length; i++) { // queue the rest
				queue.add(messages[i]);
			}
		} else { // single message
			currentMessage = text;
		}
		charsShowing = 0; // set to 0 chars showing
		textSpeed = TEXT_SPEED; // default (slowish) text speed
		setVisible(true);
	}

	/**
	 * If the current message has finished rendering, the next message in the queue (if any) is pulled up and begins
	 * rendering. If there were no messages to pull from the head of the queue, this convo box starts shifting off.
	 *
	 * If the current message has not finished rendering, the rendering speed is increased to (almost) instantaneously
	 * complete the rendering.
	 */
	public void next() {
		if (!isVisible()) {
			throw new RuntimeException("ConvoLabel.nextMessage() called when inactive");
		}
		if (charsShowing < currentMessage.length()) { // haven't finished showing current message, speed it up
			textSpeed = TEXT_SPEED_FAST;
		} else { // have finished showing current message, try to start next one, if any
			boolean notEmpty = !queue.isEmpty();
			if (notEmpty) {
				// serve up the next queued item
				currentMessage = queue.poll();
				charsShowing = 0;
				textSpeed = TEXT_SPEED;
			}
			setVisible(notEmpty);
		}
	}

	/**
	 * Updates this ConversationBox with the given delta
	 *
	 * @param delta time passed since last frame, in seconds
	 */
	@Override
	public void act(float delta) {
		// don't need to update actors
		// super.act(delta); // update all actors of this group

		int curMessageLen = 0;
		if (shifting) { // alter y according to in flag, if moving
			float dist = delta * SHIFT_SPEED;
			float y = getY();
			if (in) {
				y += dist;
				if (y >= 0) { // finished shifting in
					y = 0;
					shifting = false;
				}
			} else {
				y -= dist;
				if (y <= -CONVO_HEIGHT) { // finished shifting out
					y = -CONVO_HEIGHT;
					shifting = false;
					super.setVisible(false); // now set to invis
				}
			}
			setY(y);
		} else {
			if (currentMessage != null) {
				curMessageLen = currentMessage.length();
				if (charsShowing < curMessageLen) {
					// not shifting, and characters left to show
					charsShowing += delta * textSpeed;
					if (charsShowing > curMessageLen) charsShowing = curMessageLen;
					innerLabel.setText(currentMessage.substring(0, (int) charsShowing)); // show charsShowing characters
				}
			}
		}
	}

	/**
	 * Sets this ConversationBox's visibility. When becoming visible, the box slides in, and when turning off
	 * visibility, the box slides out.
	 *
	 * @param visible whether this box is being set to visible or not
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) super.setVisible(visible); // set visible true immediately, but not for false.
		shifting = true;
		in = visible; // visible = move in, invisible = move out
	}
}
