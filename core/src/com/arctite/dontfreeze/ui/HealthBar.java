package com.arctite.dontfreeze.ui;

import com.arctite.dontfreeze.entities.Entity;
import com.arctite.dontfreeze.entities.LiveEntity;
import com.arctite.dontfreeze.entities.Monster;
import com.arctite.dontfreeze.entities.player.Player;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Player health bar UI element.
 *
 * Created by Quasar on 4/07/2015.
 */
public class HealthBar extends Group {

	/** Progress Bar object that this class wraps around */
	private ProgressBar progressBar;
	/** Rectangle that defines the outline of this bar */
	private Rectangle outline;
	/** ShapeRenderer that will render the outline */
	private ShapeRenderer srend;
	/** Label containing the text inside this health bar */
	private Label label;
	/** Entity that this health bar is a part of */
	private LiveEntity entity;
	/** Whether or not the attached entity is a player */
	private boolean isPlayer;
	/** Coordinates/size of this entire health bar */
	private float x;
	private float y;
	private int width;
	private int height;

	/** Maximum possible health of this health bar */
	private int maxHealth;
	/** Current health value */
	private int health;

	/**
	 * Creates a health bar to be displayed on the screen at the given location, with the given settings.
	 *
	 * @param entity the Entity that this health bar is a part of
	 * @param x x-coordinate to display this bar at
	 * @param y y-coordinate to display this bar at
	 * @param width width of this bar
	 * @param height height of this bar
	 * @param maxHealth maximum health of this bar
	 * @param health current health of this bar
	 */
	public HealthBar(LiveEntity entity, float x, float y, int width, int height, int maxHealth, int health) {
		this.entity = entity;
		this.isPlayer = (entity instanceof Player);

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		this.maxHealth = maxHealth;
		this.health = health;

		this.srend = new ShapeRenderer();
		srend.setColor(Color.BLACK);
		this.outline = new Rectangle(x, y, width - 1, height - 1);

		ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
		Pixmap white = new Pixmap(1, height, Pixmap.Format.RGB888); // outline
		white.setColor(Color.WHITE);
		white.fill();
		Pixmap black = new Pixmap(1, height, Pixmap.Format.RGB888); // outline
		black.setColor(Color.BLACK);
		black.fill();
		Pixmap orange = new Pixmap(1, height, Pixmap.Format.RGB888);
		orange.setColor(1.0F, 153F / 255, 51F / 255, 1);
		orange.fill();
		Drawable orangeDraw = new TextureRegionDrawable(new TextureRegion(new Texture(orange)));
		Drawable whiteDraw = new TextureRegionDrawable(new TextureRegion(new Texture(white)));
		Drawable blackDraw = new TextureRegionDrawable(new TextureRegion(new Texture(black)));
		style.knobBefore = orangeDraw;
		style.knob = whiteDraw; // so that when hp = 0, no orange at all
		style.knobAfter = whiteDraw;
		style.background = blackDraw;

		this.progressBar = new ProgressBar(0, maxHealth, 1, false, style);
		progressBar.setValue(health); // set to current health
		progressBar.setPosition(x, y);
		progressBar.setSize(width, progressBar.getPrefHeight());
		progressBar.setAnimateDuration(0.333F);
		progressBar.setAnimateInterpolation(Interpolation.linear);
		addActor(progressBar);

		// add text label after to render on top
		if (isPlayer) {
			Label.LabelStyle labelStyle = new Label.LabelStyle();
			labelStyle.font = SkinManager.getFont();
			labelStyle.fontColor = Color.BLACK;
			this.label = new Label(null, labelStyle);
			label.setPosition(x + 50, y + 10);
			addActor(label);
		}
	}

	/**
	 * Gets current health.
	 *
	 * @return current health
	 */
	public int getHealth() {
		return health;
	}

	/**
	 * Sets current health.
	 *
	 * @param health current health to set to
	 */
	public void setHealth(int health) {
		this.health = health;
		progressBar.setValue(health);
	}

	/**
	 * Decrements the current health by the given value.
	 *
	 * @param damage about to decrease current health by
	 */
	public void decrementHealth(int damage) {
		setHealth(health - damage);
	}

	/**
	 * Gets maximum health.
	 * @return maximum health
	 */
	public int getMaxHealth() {
		return maxHealth;
	}

	/**
	 * Updates this player health bar with the given time passed
	 *
	 * @param delta time passed since last frame
	 */
	@Override
	public void act(float delta) {
		// update health into the progress bar object
		progressBar.setValue(health);

		// update label text, width and position for player health bar, but not monsters
		if (isPlayer) {
			label.setText(health + " / " + maxHealth);
			float prefWidth = label.getPrefWidth();
			label.setWidth(prefWidth); // set preferred width with the new text
			float newX = (width - prefWidth) / 2 + x;
			label.setPosition(newX, label.getY());
		} else { // can only be monster
			Monster monster = (Monster) entity;
			boolean aggro = monster.isAggressive();
			setVisible(aggro);
			if (aggro) {
				// update position
				setPosition(monster.getX(), monster.getY() + (monster.getHeight() * 0.9F));
			}
		}

		// call act on all of health bar's actors last
		super.act(delta);
	}

	/**
	 * Draws this player health bar.
	 *
	 * @param batch the batch used to render
	 * @param parentAlpha the alpha value of the parent
	 */
	@Override
	public void draw(Batch batch, float parentAlpha) {
		// draw bar and text
		super.draw(batch, parentAlpha);
		if (isPlayer) { // only draw outline for player health bar @TODO fix for monsters
			// draw outline (will override edge pixels of the bar)
			srend.begin(ShapeRenderer.ShapeType.Line);
			// the 0.5 offset is to correct missing corner pixels
			srend.rect(outline.x, outline.y, outline.width + 0.5F, outline.height + 0.5F);
			srend.end();
		}
	}

	@Override
	public void setPosition(float x, float y){
		this.x = x;
		this.y = y;
		progressBar.setPosition(x, y);
	}
}
