package com.tbd.dontfreeze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector3;
import com.tbd.dontfreeze.player.InputHandler;
import com.tbd.dontfreeze.player.Player;

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
	private static final String COLLISION_LAYER = "collision";

	/** MapLoader that loads Tiled maps */
	private static final TmxMapLoader MAP_LOADER = new TmxMapLoader();

	/** Input handling */
	private InputHandler inputHandler;

	/** Screen dimensions */
	private int winWidth;
	private int winHeight;

	/** Tiled Map tools */
	private TiledMap tiledMap;
	private CustomTiledMapRenderer tiledRenderer;

	/** Map dimensions */
	private int width;
	private int height;
	/** Cameras - one for game stuff, other for UI/debug */
	private OrthographicCamera camera;
	private Vector3 cameraPos;
	private OrthographicCamera fixedCamera;
	/** Time stepping accumulator */
	private float deltaAccumulator;

	/** Graphics stuff */
	private SpriteBatch spriteBatch;
	private BitmapFont font;

	/** Entities in this World */
	private Player player;

	public WorldScreen(Game game) {
		super(game);

		this.inputHandler = new InputHandler();
		Gdx.input.setInputProcessor(inputHandler);

		// load Tiled stuffs
		this.tiledMap = MAP_LOADER.load(MAP_LOCATION);
		this.tiledRenderer = new CustomTiledMapRenderer(tiledMap);
		MapProperties mapProps = tiledMap.getProperties();
		// load in map dimensions
		this.width = mapProps.get(MAP_WIDTH, Integer.class);
		this.height = mapProps.get(MAP_HEIGHT, Integer.class);

		this.winWidth = Gdx.graphics.getWidth();
		this.winHeight = Gdx.graphics.getHeight();
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
		cameraPos = camera.position;
		// the following line is because for some reason, when camera is (0, 0), tiled map (0, 0) is centre of screen...
		camera.position.set(winWidth / 2, winHeight / 2, 0);
		camera.translate(0, height - winHeight);
		camera.update();
		this.fixedCamera = new OrthographicCamera();
		fixedCamera.setToOrtho(false);

		// @TODO un-hardcode starting positions etc
		this.player = new Player(this, inputHandler, winWidth / 2, height - (winHeight / 2));
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public void update(float delta) {
		// increment accumulator, this is the variable we'll base all our stepping things on now
		deltaAccumulator += delta;
		while (deltaAccumulator >= DELTA_STEP) {
			// step through DELTA_STEP milliseconds, and subtract it off the accumulator
			deltaAccumulator -= DELTA_STEP;

			// get collision objects
			MapLayer collisionLayer = tiledMap.getLayers().get(COLLISION_LAYER);
			MapObjects objects = collisionLayer.getObjects();

			// update entities
			player.update(DELTA_STEP, objects.getByType(PolygonMapObject.class), objects.getByType(RectangleMapObject.class));

			// get player details
			float playerX = player.getX();
			float playerY = player.getY();
			int playerWidth = player.getWidth();
			int playerHeight = player.getHeight();

			// move camera depending on player position
			float distSide = ((float) winWidth) / 4; // 20% of the window size
			float distUd = ((float) winHeight) / 4; // also 20% of win size. ud = up/down
			// difference between player sprite (nearest edge) and side of the window
			float diffLeft = playerX - (cameraPos.x - (winWidth / 2));
			float diffRight = (cameraPos.x + (winWidth / 2)) - (playerX + playerWidth);
			float diffDown = playerY - (cameraPos.y - (winHeight / 2));
			float diffUp = (cameraPos.y + (winHeight / 2)) - (playerY + playerHeight);
			// if player is getting too close to the edge of the screen, move the camera
			float translateX = 0;
			float translateY = 0;
			if (diffLeft < distSide) {
				translateX = -(distSide - diffLeft);
			} else if (diffRight < distSide) {
				translateX = distSide - diffRight;
			}
			if (diffUp < distUd) {
				translateY = distUd - diffUp;
			} else if (diffDown < distUd) {
				translateY = -(distUd - diffDown);
			}
			camera.translate(translateX, translateY);
		}

		// update things that bind to camera
		camera.update();
		tiledRenderer.setView(camera);
	}

	@Override
	public void render(float delta) {
		if (delta > DELTA_LIMIT) {
			// delta too high, probably recovered from long freeze, skip it
			return;
		}

		// call update() first
		update(delta);

		// clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// start actual rendering

		// render background
		tiledRenderer.renderNonSpriteLayers();
		// then render sprite layer
		float playerY = player.getY();
		tiledRenderer.renderSpriteLayer(true, playerY);

		// render game things
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();

		player.render(spriteBatch);

		spriteBatch.end();

		// render foreground of tiled map
		tiledRenderer.renderSpriteLayer(false, playerY);

		// delete
		spriteBatch.begin();
		//player.render(spriteBatch);
		spriteBatch.end();
		// delete

		// debug text
		spriteBatch.setProjectionMatrix(fixedCamera.combined);
		spriteBatch.begin();
		font.draw(spriteBatch, "Camera: (" + camera.position.x + ", " + camera.position.y + ")", 20, winHeight - 35);
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, winHeight - 20);
		spriteBatch.end();
	}

	@Override
	public void show() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);

		spriteBatch = new SpriteBatch();
	}
}
