package com.tbd.dontfreeze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.tbd.dontfreeze.entities.*;
import com.tbd.dontfreeze.entities.player.WorldInputHandler;
import com.tbd.dontfreeze.entities.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

	/** Map (TileD) related constants */
	private static final String MAP_LOCATION = "assets/map1.tmx";
	private static final String TILED_PROP_X = "x";
	private static final String TILED_PROP_Y = "y";
	/** MapLoader that loads Tiled maps */
	private static final TmxMapLoader MAP_LOADER = new TmxMapLoader();
	/** Map dimensions */
	private static final String MAP_WIDTH = "width";
	private static final String MAP_HEIGHT = "height";
	/** Map layers */
	private static final String COLLISION_LAYER = "collision";
	private static final String MONSTERS_LAYER = "monsters";
	private static final String COLLECTABLES_LAYER = "collectables";
	/** Property name constants */
	private static final String PLAYER_SPAWN_X = "player_spawn_x";
	private static final String PLAYER_SPAWN_Y = "player_spawn_y";

	/** Button texts */
	private static final String END_GAME = "Continue";
	private static final String RESUME_GAME = "Resume Game";
	private static final String SAVE_AND_EXIT = "Save & Exit";

	/** Debugging settings/tools */
	private boolean debugMode;
	private ShapeRenderer debugRenderer;

	/** Input handling */
	private InputMultiplexer inputMultiplexer; // since both WorldInputHandler and Stage will need inputs on this screen
	private WorldInputHandler worldInputHandler;

	/** Whether or not this world is paused */
	private boolean paused;
	/** Scene2d UI fields */
	private Stage stage;
	private TextButton endGameButton;
	private TextButton resumeButton;
	private TextButton saveAndExitButton;

	/** Tiled Map tools */
	private TiledMap tiledMap;
	private CustomTiledMapRenderer tiledRenderer;

	/** Screen dimensions */
	private int winWidth;
	private int winHeight;
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
	private BitmapFont font;

	/** Player and other entities in this World */
	private Player player;
	private boolean playerExpireComplete;
	private HashMap<String, Monster> monsters;
	private HashMap<String, Collectable> collectables; // @TODO: there should be a better data structure for this than ALs
	private ArrayList<Projectile> projectiles;

	/**
	 * Creates a new WorldScreen. Initialises all fields, initialises and sets an InputHandler.
	 * Loads Tiled map data and initialises cameras.
	 *
	 * @param game the Game object that this screen belongs to
	 */
	public WorldScreen(GameMain game, SpriteBatch spriteBatch) {
		super(game, spriteBatch);

		// screen dimensions
		this.winWidth = GameMain.GAME_WINDOW_WIDTH;
		this.winHeight = GameMain.GAME_WINDOW_HEIGHT;

		// drawing stuff
		this.font = new BitmapFont();
		font.setColor(Color.GREEN);

		// debug mode
		this.debugMode = false;
		this.debugRenderer = new ShapeRenderer();

		// not paused
		this.paused = false;
		// intialise scene2d and related ui fields
		this.stage = new Stage();
		Skin skin = GameMain.getDefaultSkin();
		// middle of the screen position for endgame and pause buttons
		float buttonX = (winWidth / 2) - (GameMain.BUTTON_WIDTH / 2);
		float buttonY = (winHeight / 2) - (GameMain.BUTTON_HEIGHT / 2);
		// endgame button
		this.endGameButton = new TextButton(END_GAME, skin);
		endGameButton.setPosition(buttonX, buttonY);
		endGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getGame().setMenu();
			}
		});
		endGameButton.setVisible(false);
		stage.addActor(endGameButton);
		// resume from pause button
		this.resumeButton = new TextButton(RESUME_GAME, skin);
		resumeButton.setPosition(buttonX, winHeight / 2 + 5);
		resumeButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				flipPaused();
			}
		});
		resumeButton.setVisible(false);
		stage.addActor(resumeButton);
		this.saveAndExitButton = new TextButton(SAVE_AND_EXIT, skin);
		saveAndExitButton.setPosition(buttonX, (winHeight / 2) - GameMain.BUTTON_HEIGHT - 5);
		saveAndExitButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				// @TODO: save
				getGame().setMenu();
			}
		});
		saveAndExitButton.setVisible(false);
		stage.addActor(saveAndExitButton);

		// input handlers
		this.worldInputHandler = new WorldInputHandler();
		this.inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(stage); // stage gets priority for UI
		inputMultiplexer.addProcessor(worldInputHandler);

		// load Tiled stuffs
		this.tiledMap = MAP_LOADER.load(MAP_LOCATION);
		this.tiledRenderer = new CustomTiledMapRenderer(tiledMap);
		MapProperties mapProps = tiledMap.getProperties();
		// load in map dimensions
		this.width = mapProps.get(MAP_WIDTH, Integer.class);
		this.height = mapProps.get(MAP_HEIGHT, Integer.class);
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
		cameraPos = camera.position;
		// the following line is because for some reason, when camera is (0, 0), tiled map (0, 0) is centre of screen...
		camera.position.set(winWidth / 2, winHeight / 2, 0);
		camera.translate(0, height - winHeight);
		camera.update();
		this.fixedCamera = new OrthographicCamera();
		fixedCamera.setToOrtho(false);

		// will need to flip y coordinate (flipHeight - y) because tiled coordinates are y-down
		int flipHeight = height - 1;
		// player spawnpoint
		int playerSpawnX = Integer.parseInt(mapProps.get(PLAYER_SPAWN_X, String.class));
		int playerSpawnY = flipHeight - Integer.parseInt(mapProps.get(PLAYER_SPAWN_Y, String.class));
		this.player = new Player(this, worldInputHandler, playerSpawnX, playerSpawnY);

		// monster spawn points
		this.monsters = new HashMap<String, Monster>();
		MapObjects monsterObjects = tiledMap.getLayers().get(MONSTERS_LAYER).getObjects();
		HashSet<String> names = new HashSet<String>();
		for (MapObject obj : monsterObjects) {
			String name = obj.getName();
			if (names.contains(name)) { // check that all names are unique on this layer
				throw new RuntimeException("invalid tiled map - duplicated name '" + name + "' on MONSTERS layer");
			}
			names.add(name);
			float mx = obj.getProperties().get(TILED_PROP_X, Float.class);
			float my = obj.getProperties().get(TILED_PROP_Y, Float.class);
			Monster monster = new Monster(this, mx, my);
			monsters.put(name, monster); // add monster to hashmap by key = unique name
		}

		// collectable spawn points
		this.collectables = new HashMap<String, Collectable>();
		MapObjects collectableObjects = tiledMap.getLayers().get(COLLECTABLES_LAYER).getObjects();
		names.clear(); // prep names set for collectable name checking
		for (MapObject obj : collectableObjects) {
			String name = obj.getName();
			if (names.contains(name)) {
				throw new RuntimeException("invalid tiled map - duplicated name '" + name + "' on COLLECTABLES layer");
			}
			names.add(name);
			float cx = obj.getProperties().get(TILED_PROP_X, Float.class);
			float cy = obj.getProperties().get(TILED_PROP_Y, Float.class);
			Collectable collectable = new Collectable(this, cx, cy);
			collectables.put(name, collectable); // add collectable to hashmap by key = unique name
		}

		this.projectiles = new ArrayList<Projectile>();
	}

	/**
	 * Flips the pause state and performs the relevant actions for pause and resume actions.
	 */
	private void flipPaused() {
		paused = !paused;
		resumeButton.setVisible(paused);
		saveAndExitButton.setVisible(paused);
	}

	/**
	 * Notifies this world that the Player has died, and the death animation has just started.
	 *
	 * Performs end-game operations that should be done as soon as the Player's health has hit zero:
	 * - resets monster aggro
	 */
	public void notifyPlayerDeath() {
		for (Monster m : monsters.values()) {
			m.setAggressive(false);
		}
	}

	/**
	 * Notifies this world that the Player's death animation has been completed and the Player should no longer be
	 * displayed.
	 *
	 * Also enables the end game button.
	 */
	public void notifyPlayerDeathComplete() {
		playerExpireComplete = true;
		endGameButton.setVisible(true);
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

	/**
	 * Gets the Player object within this game world.
	 *
	 * @return the Player object
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Updates this world's state with the given delta.
	 *
	 * @TODO toy with ways to make projectiles go a little further into collision boxes before exploding
	 * @TODO related but could be separate - consider using centre-points of entities as defense "bounds" (points)
	 *
	 * @param delta The amount of time that has passed since the last time this method was called
	 */
	@Override
	public void update(float delta) {
		// update scene2d first regardless of this world's pause status
		stage.act(delta);

		// toggle paused mode if player isn't dead
		if (!playerExpireComplete && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			flipPaused();
		}
		// if paused, return from this method early without updating the world with delta
		if (paused) {
			return;
		}

		// toggle debug mode
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
			// list to store keys for removal, which is done after iteration to prevent ConcurrentModificationException
			ArrayList<String> removeKeys = new ArrayList<String>();
			for (String mkey : monsters.keySet()) {
				Monster monster = monsters.get(mkey);
				monster.update(DELTA_STEP, polys, rects);
				if (monster.expireComplete()) {
					removeKeys.add(mkey);
				}
			}
			for (String mkey : removeKeys) {
				monsters.remove(mkey);
			}
			for (Collectable collectable : collectables.values()) {
				collectable.update(DELTA_STEP, polys, rects);
			}
			Iterator<Projectile> projIterator = projectiles.iterator();
			while (projIterator.hasNext()) {
				Projectile projectile = projIterator.next();
				projectile.update(DELTA_STEP, polys, rects); // update the projectile
				// check for expiry of projectiles, and remove from list if so
				if (projectile.expireComplete()) {
					projIterator.remove();
				} else if (projectile.getAction() == Action.IDLE_MOVE) { // check for collision with monsters
					for (Monster monster : monsters.values()) {
						if (monster.getAction() != Action.EXPIRING) { // ignore already-expiring monsters
							// check for collision between projectile's main bounds and monster's defense bounds
							if (EntityUtil.collides(projectile.getCollisionBounds(), monster.getDefenseCollisionBounds())) {
								projectile.setAction(Action.EXPIRING);
								Direction from = Direction.getOpposite(projectile.getDirection());
								monster.hit(from);
							}
						}
					}
				}
			}
			// update player collision stuff last, after both player and entities have had a chance to move
			// process collectables collision
			removeKeys.clear(); // prep remove keys list for removal from collectables hashmap
			for (String ckey : collectables.keySet()) {
				Collectable c = collectables.get(ckey);
				if (EntityUtil.collides(player.getCollisionBounds(), c.getCollisionBounds())) {
					// remove from the map, because we picked it up
					removeKeys.add(ckey);
					// increment our collected counter
					player.collectFire();
				}
			}
			for (String ckey : removeKeys) {
				collectables.remove(ckey);
			}

			// @TODO process player and enemy projectile collision, once that is implemented
			// player's projectile collision with monsters is done in projectiles update above
			// player melee attack collision with monsters
			if (player.getAction() == Action.MELEE && !player.getMeleeHit() && player.getMeleeCanHit()) {
				for (Monster monster : monsters.values()) {
					if (monster.getAction() != Action.EXPIRING) { // ignore already-expiring monsters
						if (EntityUtil.collides(player.getAttackCollisionBounds(), monster.getDefenseCollisionBounds())) {
							player.setMeleeHit(); // set hit flag, so this melee hit won't be able to hit anything else now
							Direction from = Direction.getOpposite(player.getDirection());
							monster.hit(from);
						}
					}
				}
			}
			// monster melee attack collision with player
			for (Monster monster : monsters.values()) {
				if (monster.getAction() == Action.MELEE && !monster.getMeleeHit() && monster.getMeleeCanHit()) {
					if (EntityUtil.collides(player.getDefenseCollisionBounds(), monster.getAttackCollisionBounds())) {
						monster.setMeleeHit();
						Direction from = Direction.getOpposite(monster.getDirection());
						player.hit(from);
					}
				}
			}

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
		clearScreen();

		// start actual rendering

		// render background
		tiledRenderer.renderNonSpriteLayers();
		// then render sprite layer
		float playerY = player.getY();
		tiledRenderer.renderSpriteLayer(true, playerY);

		spriteBatch.setProjectionMatrix(camera.combined);
		if (!playerExpireComplete) {
			spriteBatch.begin();
			player.render(spriteBatch);
			spriteBatch.end();
		}

		// render foreground of tiled map
		tiledRenderer.renderSpriteLayer(false, playerY);

		// render monsters on top of everything (@TODO: prevent monsters from going near obstacles to circumvent need
		// render everything else as well @TODO render monsters and obstacles interchangeably, sort by y value
		// for layering of monster sprites with environment)
		// @TODO layer projectiles as well - this is gonna be a bit special since the down projectile should be on top
		// of the player... but it won't be (unless it's made to be very tall? that's an idea)
		spriteBatch.begin();
		for (Monster monster : monsters.values()) {
			monster.render(spriteBatch);
		}
		for (Collectable collectable : collectables.values()) {
			collectable.render(spriteBatch);
		}
		for (Projectile projectile : projectiles) {
			projectile.render(spriteBatch);
		}
		spriteBatch.end();

		// render UI last (excluding debug stuff)
		stage.draw();

		// debug stuff
		spriteBatch.setProjectionMatrix(fixedCamera.combined);
		spriteBatch.begin();
		font.draw(spriteBatch, "Camera: (" + camera.position.x + ", " + camera.position.y + ")", 20, winHeight - 35);
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, winHeight - 20);
		font.draw(spriteBatch, "Fires: " + player.getFireCount(), 20, winHeight - 50);
		font.draw(spriteBatch, "HP: " + player.getHealth(), 20, winHeight - 65);
		spriteBatch.end();
		// draw hitboxes and stuff here
		if (debugMode) {
			debugRenderer.setProjectionMatrix(camera.combined);
			debugRenderer.setColor(Color.BLACK);
			debugRenderer.begin(ShapeType.Line);
			// draw player hitboxes
			Rectangle r = player.getDefenseCollisionBounds();
			debugRender(r);
			for (Monster m : monsters.values()) {
				r = m.getDefenseCollisionBounds();
				debugRender(r);
				r = m.getCollisionBounds();
				debugRender(r);
				if (m.getAction() == Action.MELEE) {
					r = m.getAttackCollisionBounds();
					debugRender(r);
				}
			}
			for (Collectable c : collectables.values()) {
				r = c.getCollisionBounds();
				debugRender(r);
			}
			for (Projectile p : projectiles) {
				r = p.getCollisionBounds();
				debugRender(r);
			}
			r = player.getAttackCollisionBounds();
			if (player.getAction() == Action.MELEE && r != null) {
				debugRender(r);
			}
			r = player.getDefenseCollisionBounds();
			debugRender(r);
			debugRenderer.end();
		}
	}

	/**
	 * Causes the debug renderer to draw the given Rectangle. This method does not handle the debug render start or end.
	 *
	 * @param r the Rectangle to draw
	 */
	private void debugRender(Rectangle r) {
		debugRenderer.rect(r.x, r.y, r.width, r.height);
	}

	@Override
	public void show() {
		// set input handler since it will be menuscreen's handler before this
		Gdx.input.setInputProcessor(inputMultiplexer);
	}
}
