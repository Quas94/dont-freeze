package com.tbd.dontfreeze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.entities.Collectable;
import com.tbd.dontfreeze.entities.Monster;
import com.tbd.dontfreeze.entities.Projectile;
import com.tbd.dontfreeze.entities.player.InputHandler;
import com.tbd.dontfreeze.entities.player.Player;

import java.util.ArrayList;
import java.util.Iterator;

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

	/** Debugging settings/tools */
	private boolean debugMode;
	private ShapeRenderer debugRenderer;

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

	/** Player and other entities in this World */
	private Player player;
	private ArrayList<Monster> monsters;
	private ArrayList<Collectable> collectables; // @TODO: there should be a better data structure for this than ALs
	private ArrayList<Projectile> projectiles;

	/**
	 * Creates a new WorldScreen. Initialises all fields, initialises and sets an InputHandler.
	 * Loads Tiled map data and initialises cameras.
	 *
	 * @param game the Game object that this screen belongs to
	 */
	public WorldScreen(Game game) {
		super(game);

		this.debugMode = false;
		this.debugRenderer = new ShapeRenderer();

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
		this.monsters = new ArrayList<Monster>();
		this.collectables = new ArrayList<Collectable>();
		this.projectiles = new ArrayList<Projectile>();
		Monster snowMonster = new Monster(this, winWidth / 2 + 100, height - (winHeight / 2));
		monsters.add(snowMonster);
		Collectable fire = new Collectable(this, winWidth / 2 + 50, height - (winHeight / 2) - 100);
		collectables.add(fire);
	}

	/**
	 * Returns the height of the current map.
	 *
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the width of the current map.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Adds a given Projectile to this world's current list of Projectiles.
	 *
	 * @param projectile The projectile to add
	 */
	public void addProjectile(Projectile projectile) {
		projectiles.add(projectile);
	}

	@Override
	public void update(float delta) {
		// debug toggle
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			debugMode = !debugMode;
		}

		// increment accumulator, this is the variable we'll base all our stepping things on now
		deltaAccumulator += delta;
		while (deltaAccumulator >= DELTA_STEP) {
			// step through DELTA_STEP milliseconds, and subtract it off the accumulator
			deltaAccumulator -= DELTA_STEP;

			// get collision objects
			MapLayer collisionLayer = tiledMap.getLayers().get(COLLISION_LAYER);
			MapObjects objects = collisionLayer.getObjects();

			// update player and monsters
			Array<PolygonMapObject> polys = objects.getByType(PolygonMapObject.class);
			Array<RectangleMapObject> rects = objects.getByType(RectangleMapObject.class);
			player.update(DELTA_STEP, polys, rects);
			for (Monster monster : monsters) {
				monster.update(delta, polys, rects);
			}
			for (Collectable collectable : collectables) {
				collectable.update(delta, polys, rects);
			}
			Iterator<Projectile> projIterator = projectiles.iterator();
			while (projIterator.hasNext()) {
				Projectile projectile = projIterator.next();
				projectile.update(delta, polys, rects); // update the projectile
				if (projectile.isExpired()) {
					projIterator.remove(); // remove the expired projectile from the list
				}
			}
			// update player collision stuff last, after both player and entities have had a chance to move
			player.updateCollision(monsters, collectables, projectiles);
			// @TODO projectile collision with monsters

			// now check camera
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

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
		player.render(spriteBatch);
		spriteBatch.end();

		// render foreground of tiled map
		tiledRenderer.renderSpriteLayer(false, playerY);

		// render monsters on top of everything (@TODO: prevent monsters from going near obstacles to circumvent need
		// render everything else as well @TODO render monsters and obstacles interchangeably, sort by y value
		// for layering of monster sprites with environment)
		// @TODO layer projectiles as well - this is gonna be a bit special since the down projectile should be on top
		// of the player... but it won't be (unless it's made to be very tall? that's an idea)
		spriteBatch.begin();
		for (Monster monster : monsters) {
			monster.render(spriteBatch);
		}
		for (Collectable collectable : collectables) {
			collectable.render(spriteBatch);
		}
		for (Projectile projectile : projectiles) {
			projectile.render(spriteBatch);
		}
		spriteBatch.end();

		// debug stuff
		spriteBatch.setProjectionMatrix(fixedCamera.combined);
		spriteBatch.begin();
		font.draw(spriteBatch, "Camera: (" + camera.position.x + ", " + camera.position.y + ")", 20, winHeight - 35);
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, winHeight - 20);
		font.draw(spriteBatch, "Fires: " + player.getFireCount(), 20, winHeight - 50);
		font.draw(spriteBatch, "# Projectiles: " + projectiles.size(), 20, winHeight - 65);
		spriteBatch.end();
		// draw hitboxes and stuff here
		if (debugMode) {
			debugRenderer.setProjectionMatrix(camera.combined);
			debugRenderer.setColor(Color.BLACK);
			debugRenderer.begin(ShapeType.Line);
			// draw player hitboxes
			Rectangle r = player.getCollisionBounds();
			debugRenderer.rect(r.x, r.y, r.width, r.height);
			for (Monster m : monsters) {
				r = m.getCollisionBounds();
				debugRenderer.rect(r.x, r.y, r.width, r.height);
			}
			for (Collectable c : collectables) {
				r = c.getCollisionBounds();
				debugRenderer.rect(r.x, r.y, r.width, r.height);
			}
			for (Projectile p : projectiles) {
				r = p.getCollisionBounds();
				debugRenderer.rect(r.x, r.y, r.width, r.height);
			}
			debugRenderer.end();
		}
	}

	@Override
	public void show() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);

		spriteBatch = new SpriteBatch();
	}
}
