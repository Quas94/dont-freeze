package com.arctite.dontfreeze.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.HashMap;

import static com.arctite.dontfreeze.ui.ConvoLabel.*;

/**
 * Static class which initialises and manages skins for all of the game's UI elements.
 *
 * Created by Quasar on 29/06/2015.
 */
public class SkinManager {

	public static final String MENU_BUTTON_SKIN = "menubuttonskin";
	public static final String CONVO_LABEL_SKIN = "convolabelskin";

	private static HashMap<String, Skin> skins;
	private static BitmapFont font;

	private static final String DEFAULT = "default";
	private static final String BACKGROUND = "background";

	/** Menu Button attributes */
	public static final int MENU_BUTTON_WIDTH = 130;
	public static final int MENU_BUTTON_HEIGHT = 30;

	public static BitmapFont getFont() {
		if (font == null) {
			throw new RuntimeException("calling SkinManager.getFont() before SkinManager.loadSkins()");
		}
		return font;
	}

	/**
	 * Loads all skins and sets them up.
	 */
	public static void loadSkins() {
		skins = new HashMap<String, Skin>();
		font = new BitmapFont();

		// initialise menu button skin
		Skin menuButtonSkin = new Skin();
		menuButtonSkin.add(DEFAULT, font);

		// button background pixmap
		Pixmap pixmap = new Pixmap(MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT, Pixmap.Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		menuButtonSkin.add(BACKGROUND, new Texture(pixmap));
		// create button style
		TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
		style.up = menuButtonSkin.newDrawable(BACKGROUND, Color.GRAY);
		style.down = menuButtonSkin.newDrawable(BACKGROUND, Color.DARK_GRAY);
		// style.checked = skin.newDrawable(BACKGROUND, Color.DARK_GRAY);
		style.over = menuButtonSkin.newDrawable(BACKGROUND, Color.LIGHT_GRAY);
		style.font = menuButtonSkin.getFont(DEFAULT);
		menuButtonSkin.add(DEFAULT, style);
		// add to hashmap
		skins.put(MENU_BUTTON_SKIN, menuButtonSkin);

		// initialise game convo box skin
		Skin convoSkin = new Skin();
		convoSkin.add(DEFAULT, font);
		// button background pixmap
		pixmap = new Pixmap(CONVO_WIDTH, CONVO_HEIGHT, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		convoSkin.add(BACKGROUND, new Texture(pixmap));
		// create button style
		Label.LabelStyle convoStyle = new Label.LabelStyle();
		convoStyle.background = convoSkin.newDrawable(BACKGROUND, CLC[0], CLC[1], CLC[2], CLC[3]);
		convoStyle.font = convoSkin.getFont(DEFAULT);
		convoSkin.add(DEFAULT, convoStyle);
		// add to hashmap
		skins.put(CONVO_LABEL_SKIN, convoSkin);

		// initialise other skins
	}

	public static Skin getSkin(String skinName) {
		if (!skins.containsKey(skinName)) {
			throw new RuntimeException("non-existent skin requested: " + skinName);
		}
		return skins.get(skinName);
	}
}
