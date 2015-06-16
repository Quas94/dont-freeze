package com.tbd.dontfreeze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/**
 * In-game screen where the actual gameplaying will take place.
 *
 * Map rendering will be done by rendering the current 640x480 chunk the player is standing on, and then rendering the
 * neighbouring chunks as necessary.
 *
 * Created by Quasar on 14/06/2015.
 */
public class WorldScreen extends AbstractScreen {

	/** To do with time stepping and frame handling */
	private static final float DELTA_STEP = 1 / 120F;

	/** Temporary constants, mostly to do with functionality that will be modified later */
	private static final String MAP_LOCATION = "assets/map1.tmx";
	private static final String MAP_WIDTH = "width";
	private static final String MAP_HEIGHT = "height";

	/** MapLoader that loads Tiled maps */
	private static final TmxMapLoader MAP_LOADER = new TmxMapLoader();

	/** Screen dimensions */
	private int winWidth;
	private int winHeight;

	/** Tiled Map tools */
	private TiledMap tiledMap;
	private OrthogonalTiledMapRenderer tiledRenderer;

	/** Map dimensions */
	private int width;
	private int height;
	/** Cameras - one for game stuff, other for UI/debug */
	private OrthographicCamera camera;
	private OrthographicCamera fixedCamera;
	/** Time stepping accumulator */
	private float deltaAccumulator;

	/** Graphics stuff */
	private SpriteBatch spriteBatch;
	private BitmapFont font;

	public WorldScreen(Game game) {
		super(game);

		// load Tiled stuffs
		this.tiledMap = MAP_LOADER.load(MAP_LOCATION);
		this.tiledRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		MapProperties mapProps = tiledMap.getProperties();
		// load in map dimensions
		this.width = mapProps.get(MAP_WIDTH, Integer.class);
		this.height = mapProps.get(MAP_HEIGHT, Integer.class);

		this.winWidth = Gdx.graphics.getWidth();
		this.winHeight = Gdx.graphics.getHeight();
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
		// the following line is because for some reason, when camera is (0, 0), tiled map (0, 0) is centre of screen...
		camera.position.set(winWidth / 2, winHeight / 2, 0);
		camera.translate(0, height - winHeight);
		camera.update();
		this.fixedCamera = new OrthographicCamera();
		fixedCamera.setToOrtho(false);

		// @TODO set camera initial position
	}

	@Override
	public void update(float delta) {
		// increment accumulator, this is the variable we'll base all our stepping things on now
		deltaAccumulator += delta;
		while (deltaAccumulator >= DELTA_STEP) {
			// step through DELTA_STEP milliseconds, and subtract it off the accumulator
			deltaAccumulator -= DELTA_STEP;
			// do stuff
			// camera.translate(0, 0.2f);
		}

		// update things that bind to camera
		camera.update();
		tiledRenderer.setView(camera);
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// render background
		tiledRenderer.render();

		// render game things
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
		// draw game stuff
		spriteBatch.end();

		// debug text
		spriteBatch.setProjectionMatrix(fixedCamera.combined);
		spriteBatch.begin();
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 460);
		font.draw(spriteBatch, "Camera: (" + camera.position.x + ", " + camera.position.y + ")", 20, 440);
		spriteBatch.end();
	}

	@Override
	public void show() {
		font = new BitmapFont();
		font.setColor(Color.BLACK);

		spriteBatch = new SpriteBatch();
	}
}
