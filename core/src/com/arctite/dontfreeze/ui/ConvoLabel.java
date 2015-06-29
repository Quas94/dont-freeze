package com.arctite.dontfreeze.ui;

import com.arctite.dontfreeze.GameMain;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

/**
 * The UI element which holds text events/conversation in the game.
 *
 * Created by Quasar on 29/06/2015.
 */
public class ConvoLabel {

	/** Convo Label attributes */
	public static final int OUTER_PADDING = 10; // gap on both left/right sides
	public static final int CONVO_WIDTH = GameMain.GAME_WINDOW_WIDTH - (OUTER_PADDING * 2); // width of outer label
	public static final int CONVO_HEIGHT = GameMain.GAME_WINDOW_HEIGHT / 5; // height of outer label
	/** Inner label (text) attributes (wrt. to outer label) */
	private static final int INNER_PADDING = 10; // gap between outer and inner labels
	private static final int INNER_TOTAL_PADDING = OUTER_PADDING + INNER_PADDING; // absolute gap of inner (with window)
	private static final int INNER_WIDTH = CONVO_WIDTH - (INNER_PADDING * 2);
	private static final int INNER_HEIGHT = CONVO_HEIGHT - (INNER_PADDING * 2);
	/** Convo label colours (r, g, b, a) */
	public static final float[] CLC = new float[] { 192, 192, 192, 0.5F };

	/** The label which contains the background and the inner label */
	private Label outerLabel;

	/** The label which contains the text */
	private Label innerLabel;

	/**
	 * Creates a new ConvoLabel, which is composed of an outer Label (providing the semi-transparent background) and an
	 * inner Label (container for the text).
	 */
	public ConvoLabel() {
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

		System.out.printf("fontscaleX = %f, fontscaleY = %f\n", innerLabel.getFontScaleX(), innerLabel.getFontScaleY());

		setVisible(true);
		setText("TTTT test text blah blahTTTT test text blah blahTTTT test text blah bTTTT test text blah blahTTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blahTTTT test text blah blah");
	}

	/**
	 * Sets the text for this ConvoLabel.
	 *
	 * @param text the text to set to
	 */
	public void setText(String text) {
		innerLabel.setText(text);
	}

	/**
	 * Sets this entire ConvoLabel's visibility.
	 */
	public void setVisible(boolean visible) {
		outerLabel.setVisible(visible);
		innerLabel.setVisible(visible);
	}

	/**
	 * Gets this ConvoLabel's visibility.
	 * @return whether this ConvoLabel is visible
	 */
	public boolean isVisible() {
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
