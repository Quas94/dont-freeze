package com.arctite.dontfreeze;

import com.arctite.dontfreeze.ui.ConversationBox;
import com.arctite.dontfreeze.ui.SkinManager;
import com.arctite.dontfreeze.util.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.arctite.dontfreeze.entities.*;
import com.arctite.dontfreeze.entities.player.WorldInputHandler;
import com.arctite.dontfreeze.entities.player.Player;

import java.util.*;

import static com.arctite.dontfreeze.util.SaveManager.*;
import static com.arctite.dontfreeze.util.HorizontalMapRenderer.*;

/**
 * In-game screen where the actual gameplaying will take place.
 *
 * Map rendering will be done by rendering the current chunk the player is standing on, and then rendering the
 * neighbouring chunks as necessary.
 *
 * Created by Quasar on 14/06/2015.
 */
public class WorldScreen extends AbstractScreen {

	/** New game stuff */
	public static final int NEW_GAME_CHUNK_X = 1;
	public static final int NEW_GAME_CHUNK_Y = 5;
	public static final int NEW_GAME_PLAYER_X = 400;
	public static final int NEW_GAME_PLAYER_Y = 400;

	/** Map chunk info */
	public static final int LEFTMOST_CHUNK_X = 0;
	public static final int RIGHTMOST_CHUNK_X = 4;
	public static final int LOWEST_CHUNK_Y = 0;
	public static final int HIGHEST_CHUNK_Y = 5;
	public static final int CHUNK_WIDTH = 1920;
	public static final int CHUNK_HEIGHT = 1440;

	/** Camera limits */
	private static final int CAM_MIN_X = GameMain.GAME_WINDOW_WIDTH / 2;
	private static final int CAM_MAX_X = CHUNK_WIDTH - CAM_MIN_X;
	private static final int CAM_MIN_Y = GameMain.GAME_WINDOW_HEIGHT / 2;
	private static final int CAM_MAX_Y = CHUNK_HEIGHT - CAM_MIN_Y;

	/** To do with time stepping and frame handling */
	private static final float DELTA_STEP = 1 / 120F;

	/** Map (TileD) related constants */
	private static final String DIRECTORY = "assets/maps/";
	private static final String UNDERSCORE = "_";
	private static final String EXT = ".tmx";
	private static final String TILED_PROP_X = "x";
	private static final String TILED_PROP_Y = "y";
	private static final String TILED_PROP_WIDTH = "width";
	private static final String TILED_PROP_HEIGHT = "height";
	private static final String TILED_PROP_TYPE = "type"; // id of entities, and type of events
	/** MapLoader that loads Tiled maps */
	private static final TmxMapLoader MAP_LOADER = new TmxMapLoader();
	/** Map dimensions */
	private static final String TILED_PROP_MAP_WIDTH = "width";
	private static final String TILED_PROP_MAP_HEIGHT = "height";

	/** Button texts */
	private static final String END_GAME = "Continue";
	private static final String RESUME_GAME = "Resume Game";
	private static final String SAVE_AND_EXIT = "Save & Exit";

	/** Debugging settings/tools */
	private boolean debugMode;
	private ShapeRenderer debugRenderer;

	/** Input handling */
	private InputMultiplexer inputMultiplexer; // since both WorldInputHandler and Stage will need inputs on this screen

	/** Whether or not this world is paused */
	private boolean paused;
	/** Scene2d UI fields */
	private Stage stage;
	private TextButton endGameButton;
	private TextButton resumeButton;
	private TextButton saveAndExitButton;
	/** Conversation box label */
	private ConversationBox convoBox;

	/** Tiled Map stuff */
	private TiledMap tiledMap;
	private int chunkX;
	private int chunkY;
	private HorizontalMapRenderer mapRenderer;
	private final List<Rectangle> obstacleRects;
	private final List<RectangleBoundedPolygon> obstaclePolys;
	private final List<Rectangle> allRects; // unmodifiable
	private final List<RectangleBoundedPolygon> allPolys; // unmodifiable

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

	/** Separate font for debugging. For non-debug fonts, use SkinManager.getFont() */
	private BitmapFont font;

	/** Player and other entities in this World */
	private Player player;
	private boolean playerExpireComplete;
	// NOTE: all monster add/remove methods must be done on both orderedEntities and monsters
	private ArrayList<LiveEntity> orderedEntities; // (sorted by y-coord) list of all active monsters and the player
	private Comparator<LiveEntity> orderedEntitiesComp;
	private HashMap<String, Monster> monsters; // hashmap of <name, monsterobj>
	private HashMap<String, Collectable> collectables;
	private ArrayList<Projectile> projectiles;
	private ArrayList<Event> events;

	/**
	 * Creates a new WorldScreen. Initialises all fields, initialises and sets an InputHandler.
	 * Loads Tiled map data and initialises cameras.
	 *
	 * @param game the Game object that this screen belongs to
	 * @param worldInputHandler the WorldInputHandler which remains consistent throughout the entire game application
	 * @param p the Player object
	 * @param spriteBatch the SpriteBatch that this game is using
	 */
	public WorldScreen(GameMain game, WorldInputHandler worldInputHandler, Player p, SpriteBatch spriteBatch) {
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
		Skin menuButtonSkin = SkinManager.getSkin(SkinManager.MENU_BUTTON_SKIN);

		// conversation box style
		this.convoBox = new ConversationBox();
		stage.addActor(convoBox);

		// middle of the screen position for endgame and pause buttons
		float buttonX = (winWidth / 2) - (SkinManager.MENU_BUTTON_WIDTH / 2);
		float buttonY = (winHeight / 2) - (SkinManager.MENU_BUTTON_HEIGHT / 2);
		// endgame button
		this.endGameButton = new TextButton(END_GAME, menuButtonSkin);
		endGameButton.setPosition(buttonX, buttonY);
		endGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SoundManager.playClick();
				getGame().setMenu();
			}
		});
		endGameButton.setVisible(false);
		stage.addActor(endGameButton);
		// resume from pause button
		this.resumeButton = new TextButton(RESUME_GAME, menuButtonSkin);
		resumeButton.setPosition(buttonX, winHeight / 2 + 5);
		resumeButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SoundManager.playClick();
				flipPaused();
			}
		});
		resumeButton.setVisible(false);
		stage.addActor(resumeButton);
		this.saveAndExitButton = new TextButton(SAVE_AND_EXIT, menuButtonSkin);
		saveAndExitButton.setPosition(buttonX, (winHeight / 2) - SkinManager.MENU_BUTTON_HEIGHT - 5);
		saveAndExitButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SoundManager.playClick();
				saveValues(); // save the game and save player too
				player.save();
				SaveManager.getSaveManager().saveToJson();
				getGame().setMenu();
			}
		});
		saveAndExitButton.setVisible(false);
		stage.addActor(saveAndExitButton);

		// input handlers
		this.inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(stage); // stage gets priority for UI
		inputMultiplexer.addProcessor(worldInputHandler);

		// will need to flip y coordinate (flipHeight - y) because tiled coordinates are y-down
		// player spawnpoint
		if (p == null) {
			this.player = new Player(this, worldInputHandler, NEW_GAME_PLAYER_X, NEW_GAME_PLAYER_Y);
			player.setChunk(WorldScreen.NEW_GAME_CHUNK_X, WorldScreen.NEW_GAME_CHUNK_Y);
		} else {
			this.player = p;
			player.setWorld(this, worldInputHandler);
		}
		this.orderedEntities = new ArrayList<LiveEntity>();
		this.orderedEntitiesComp = new Comparator<LiveEntity>() {
			@Override
			public int compare(LiveEntity e1, LiveEntity e2) {
				float y1 = e1.getY();
				float y2 = e2.getY();
				if (y1 < y2) {
					return 1;
				} else if (y1 > y2) {
					return -1;
				}
				return 0; // equal
			}
		};
		orderedEntities.add(player); // add player to the sorted entities list

		// load Tiled stuffs
		this.chunkX = player.getChunkX();
		this.chunkY = player.getChunkY();
		this.tiledMap = MAP_LOADER.load(DIRECTORY + chunkX + UNDERSCORE + chunkY + EXT);
		this.mapRenderer = new HorizontalMapRenderer(tiledMap, spriteBatch);
		MapProperties mapProps = tiledMap.getProperties();
		// load in map dimensions
		this.width = mapProps.get(TILED_PROP_MAP_WIDTH, Integer.class);
		this.height = mapProps.get(TILED_PROP_MAP_HEIGHT, Integer.class);
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
		this.cameraPos = camera.position;
		// camera is centred in middle of the screen
		camera.position.set(winWidth / 2, winHeight / 2, 0);
		camera.translate(0, height - winHeight);
		camera.update();
		this.fixedCamera = new OrthographicCamera();
		fixedCamera.setToOrtho(false);
		// process collision layer polygons and decompose concave into convex, and put into arraylist fields
		MapLayers layers = tiledMap.getLayers();
		MapObjects objs = layers.get(OBSTACLES_LAYER).getObjects(); // load in obstacles first
		List<Rectangle> modiRects = new ArrayList<Rectangle>();
		List<RectangleBoundedPolygon> modiPolys = new ArrayList<RectangleBoundedPolygon>();
		Array<RectangleMapObject> rectObjs = objs.getByType(RectangleMapObject.class);
		Array<PolygonMapObject> polyObjs = objs.getByType(PolygonMapObject.class);
		for (RectangleMapObject o : rectObjs) {
			modiRects.add(o.getRectangle());
		}
		for (PolygonMapObject o : polyObjs) {
			modiPolys.add(new RectangleBoundedPolygon(o.getPolygon())); // constructor will take care of splitting concave polygons
		}
		// set rect and polygon lists unmodifiable
		this.obstacleRects = Collections.unmodifiableList(modiRects);
		this.obstaclePolys = Collections.unmodifiableList(modiPolys);
		modiRects = new ArrayList<Rectangle>();
		modiPolys = new ArrayList<RectangleBoundedPolygon>();
		// copy over obstacle rects/polys
		for (Rectangle or : obstacleRects) {
			modiRects.add(or);
		}
		for (RectangleBoundedPolygon op : obstaclePolys) {
			modiPolys.add(op);
		}
		objs = layers.get(GROUNDLESS_LAYER).getObjects();
		// load in groundless rects/polys and combine them with obstacle rects/polys for allRects and allPolys
		rectObjs = objs.getByType(RectangleMapObject.class);
		polyObjs = objs.getByType(PolygonMapObject.class);
		// add cumulatively onto the obstacle shapes for 'all'
		for (RectangleMapObject o : rectObjs) {
			modiRects.add(o.getRectangle());
		}
		for (PolygonMapObject o : polyObjs) {
			modiPolys.add(new RectangleBoundedPolygon(o.getPolygon())); // constructor will take care of splitting concave polygons
		}
		this.allRects = Collections.unmodifiableList(modiRects);
		this.allPolys = Collections.unmodifiableList(modiPolys);

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
			int id = Integer.parseInt(obj.getProperties().get(TILED_PROP_TYPE, String.class));
			Monster monster = new Monster(this, id, mx, my);
			monsters.put(name, monster); // add monster to hashmap by key = unique name
			orderedEntities.add(monster);
		}
		// sort ordered entities list
		sortOrderedEntities();

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
			int id = Integer.parseInt(obj.getProperties().get(TILED_PROP_TYPE, String.class));
			Collectable collectable = new Collectable(this, id, cx, cy);
			collectables.put(name, collectable); // add collectable to hashmap by key = unique name
		}

		// initialise projectiles
		this.projectiles = new ArrayList<Projectile>();

		// load in event objects
		this.events = new ArrayList<Event>();
		MapObjects eventObjects = tiledMap.getLayers().get(EVENTS_LAYER).getObjects();
		for (MapObject obj : eventObjects) {
			String eventName = obj.getName();
			String eventType = obj.getProperties().get(TILED_PROP_TYPE, String.class);
			float ex = obj.getProperties().get(TILED_PROP_X, Float.class);
			float ey = obj.getProperties().get(TILED_PROP_Y, Float.class);
			float ew = obj.getProperties().get(TILED_PROP_WIDTH, Float.class);
			float eh = obj.getProperties().get(TILED_PROP_HEIGHT, Float.class);
			Event event = new Event(eventName, eventType, (int) ex, (int) ey, ew, eh);
			events.add(event);
		}

		updateCamera();
	}

	/**
	 * Loads values from the save file into this game world.
	 */
	public void loadValues() {
		SaveManager saver = SaveManager.getSaveManager();
		// map number prefix
		String chunkId = (new String() + chunkX) + chunkY;
		if (saver.hasDataValue(VISITED_CHUNK + chunkId)) { // otherwise the constructor has loaded defaults for this map
			// check monsters
			ArrayList<String> removeKeys = new ArrayList<String>();
			for (String mkey : monsters.keySet()) { // key is name
				Monster monster = monsters.get(mkey);
				boolean active = saver.hasDataValue(chunkId + MONSTER + mkey + ACTIVE);
				if (active) {
					monster.load(chunkId, mkey);
				} else {
					removeKeys.add(mkey);
					// remove from orderedlist also
					orderedEntities.remove(monster);
				}
			}
			// remove monsters that weren't found in save file
			for (String mkey : removeKeys) {
				monsters.remove(mkey);
			}
			// sort ordered entities now that monsters have been changed around
			sortOrderedEntities();

			// check collectables
			removeKeys.clear();
			for (String ckey : collectables.keySet()) {
				boolean active = saver.hasDataValue(chunkId + COLLECTABLE + ckey + ACTIVE);
				if (!active) {
					removeKeys.add(ckey);
				}
			}
			// remove collectables that weren't found in save file
			for (String ckey : removeKeys) {
				collectables.remove(ckey);
			}
			// update event triggered flags
			for (Event event : events) {
				boolean triggered = saver.getDataValue(chunkId + EVENT + event.getName() + TRIGGERED, Boolean.class);
				event.setTriggered(triggered);
			}
		}
		updateCamera();
	}

	/**
	 * Saves this game world into the SaveManager (but does not explicitly write to file).
	 */
	public void saveValues() {
		String chunkId = (new String() + chunkX) + chunkY;
		SaveManager saver = SaveManager.getSaveManager();
		// firstly, remove all previous values associated with this map chunk
		saver.removeChunkDataValues(chunkId);
		// set chunk as visited
		saver.setDataValue(VISITED_CHUNK + chunkId, true);

		// now write the updated values in
		for (String mkey : monsters.keySet()) {
			Monster m = monsters.get(mkey);
			if (m.getHealth() > 0) { // excludes expiring monsters
				// firstly: set this monster as active
				saver.setDataValue(chunkId + MONSTER + mkey + ACTIVE, true);
				// then save fields
				m.save(chunkId, mkey);
			}
		}
		for (String ckey : collectables.keySet()) {
			saver.setDataValue(chunkId + COLLECTABLE + ckey + ACTIVE, true);
		}
		// save the events
		for (Event event : events) {
			// here, active = not triggered
			saver.setDataValue(chunkId + EVENT + event.getName() + TRIGGERED, event.hasTriggered());
		}
	}

	/**
	 * Sorts the ordered entities ArrayList. Should be called whenever monsters have changed.
	 */
	private void sortOrderedEntities() {
		Collections.sort(orderedEntities, orderedEntitiesComp);
	}
	/**
	 * Gets the x coordinate of the chunk id that's currently loaded
	 *
	 * @return chunk x coord
	 */
	public int getChunkX() {
		return chunkX;
	}

	/**
	 * Gets the y coordinate of the chunk id that's currently loaded
	 *
	 * @return chunk y coord
	 */
	public int getChunkY() {
		return chunkY;
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
	 * Resets monster aggro of all monsters. Called upon Player death or Player leaving the map chunk.
	 */
	public void deaggroMonsters() {
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
		// in case player paused during the final hit
		resumeButton.setVisible(false);
		saveAndExitButton.setVisible(false);
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
	 * @param delta The amount of time that has passed since the last frame
	 */
	@Override
	public void update(float delta) {
		// update scene2d first regardless of this world's pause status
		stage.act(delta);

		// check toggle sound
		getGame().checkToggleSound();

		// enter = next message, if convo box is active
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			if (convoBox.isVisible()) {
				convoBox.next();
			}
		}

		// either esc-pause or convobox active are effectively pauses for game logic update purposes
		boolean convoActive = convoBox.isVisible();
		boolean effectivePause = paused || convoActive;

		// toggle paused mode if player isn't dead
		if (!playerExpireComplete && !convoActive && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			flipPaused();
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

			// update player and monsters
			player.update(DELTA_STEP, effectivePause, allRects, allPolys);
			// list to store keys for removal, which is done after iteration to prevent ConcurrentModificationException
			ArrayList<String> removeKeys = new ArrayList<String>();
			for (String mkey : monsters.keySet()) {
				Monster monster = monsters.get(mkey);
				monster.update(DELTA_STEP, effectivePause, allRects, allPolys);
				if (monster.expireComplete()) {
					removeKeys.add(mkey);
					orderedEntities.remove(monster); // remove from orderedEntities as well as monsters list
				}
			}
			for (String mkey : removeKeys) {
				monsters.remove(mkey);
			}

			for (Collectable collectable : collectables.values()) {
				collectable.update(DELTA_STEP, effectivePause, allRects, allPolys);
			}
			Iterator<Projectile> projIterator = projectiles.iterator();
			while (projIterator.hasNext()) {
				Projectile projectile = projIterator.next();
				projectile.update(DELTA_STEP, effectivePause, obstacleRects, obstaclePolys); // projectiles can fly over groundless
				// check for expiry of projectiles, and remove from list if so
				if (projectile.expireComplete()) {
					projIterator.remove();
				} else if (projectile.getAction() == Action.INITIALISING || projectile.getAction() == Action.LOOPING) {
					// check for player's projectile collision with monsters
					for (Monster monster : monsters.values()) {
						if (monster.getAction() != Action.EXPIRING) { // ignore already-expiring monsters
							// check for collision between projectile's main bounds and monster's defense bounds
							if (Collisions.collidesShapes(projectile.getCollisionBounds(), monster.getDefenseCollisionBounds())) {
								projectile.setAction(Action.EXPIRING);
								Direction from = Direction.getOpposite(projectile.getDirection());
								monster.hit(from);
								SoundManager.playSound(SoundManager.SoundInfo.PLAYER_SPECIAL_EXPLOSION);
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
				if (Collisions.collidesShapes(player.getCollisionBounds(), c.getCollisionBounds())) {
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
						if (Collisions.collidesShapes(player.getAttackCollisionBounds(), monster.getDefenseCollisionBounds())) {
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
					if (Collisions.collidesShapes(player.getDefenseCollisionBounds(), monster.getAttackCollisionBounds())) {
						monster.setMeleeHit();
						Direction from = Direction.getOpposite(monster.getDirection());
						player.hit(from);
						SoundManager.playSound(SoundManager.SoundInfo.MONSTER_MELEE);
					}
				}
			}

			// event triggering
			for (Event event : events) {
				if (!event.hasTriggered()) {
					if (Collisions.collidesShapes(player.getCollisionBounds(), event.getBounds())) {
						// trigger this event
						event.trigger(this);
					}
				}
			}

			// update camera
			updateCamera();
		}

		// only need to sort once per frame (as opposed to every DELTA_STEP)
		sortOrderedEntities();

		// update things that bind to camera
		camera.update();
	}

	/**
	 * Gets the convo box label in this world.
	 */
	public ConversationBox getConvoBox() {
		return convoBox;
	}

	/**
	 * Updates camera and moves it to be in the appropriate place relative to the player.
	 */
	private void updateCamera() {
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
		// make sure camera stays within bounds so no black screen portions are shown
		if (cameraPos.x < CAM_MIN_X) {
			cameraPos.x = CAM_MIN_X;
		} else if (cameraPos.x > CAM_MAX_X) {
			cameraPos.x = CAM_MAX_X;
		}
		if (cameraPos.y < CAM_MIN_Y) {
			cameraPos.y = CAM_MIN_Y;
		} else if (cameraPos.y > CAM_MAX_Y) {
			cameraPos.y = CAM_MAX_Y;
		}
	}

	@Override
	public void render() {
		// clear screen
		clearScreen();

		// start actual rendering
		mapRenderer.setView(camera);

		// @TODO layer projectiles as well - this is gonna be a bit special since the down projectile should be on top
		// of the player... but it won't be (unless it's made to be very tall? that's an idea)

		// render background
		mapRenderer.renderBackgroundLayer();

		// now render map obstacles and entities interchangeably, top to bottom
		int camY = Math.round(cameraPos.y);
		int screenTop = camY + (winHeight / 2);
		int screenBot = camY - (winHeight / 2);

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();

		int numEntities = orderedEntities.size();
		if (numEntities > 0) {
			int highestEntityY = (int) orderedEntities.get(0).getY();
			// first: render from top of screen to highest LiveEntity's y
			mapRenderer.renderSpriteLayer(screenTop, highestEntityY, false);
			int lowestRenderedEntityY = 0;
			// loop and interchangeably render sprites and map layers
			int i = 0;
			while (i < numEntities) {
				LiveEntity e = orderedEntities.get(i);
				int thisY = (int) e.getY();
				if (thisY < screenBot - HorizontalMapRenderer.MAX_SPRITE_HEIGHT) {
					// we're low enough to ignore everything from here on, break out of loop
					break;
				}
				if (e instanceof Monster || !playerExpireComplete) {
					e.render(spriteBatch);
					lowestRenderedEntityY = thisY;
				} // else: this entity is completely expired player, so don't render

				i++; // increment i in the middle because next portion needs to check it

				if (i < numEntities) { // if there is another LiveEntity lower, render the gap
					int nextHighestY = (int) orderedEntities.get(i).getY();
					mapRenderer.renderSpriteLayer(thisY - 1, nextHighestY, false);
				}
			}
			// lastly, render from lowestRenderedEntityY to bottom of screen
			//if (screenBot >= lowestRenderedEntityY - 1) {
				mapRenderer.renderSpriteLayer(lowestRenderedEntityY - 1, screenBot, true);
			//}
		} else {
			// no entities, this shouldn't really happen but just in case - render everything
			mapRenderer.renderSpriteLayer(screenTop, screenBot, true);
		}

		// collectables and projectiles always rendered on the very top
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
		font.draw(spriteBatch, "Current Map: (" + chunkX + ", " + chunkY + ")", 20, winHeight - 50);
		font.draw(spriteBatch, "HP: " + player.getHealth(), 20, winHeight - 65);
		font.draw(spriteBatch, "Sound: " + (SoundManager.isEnabled() ? "On" : "Off"), 20, winHeight - 80);
		spriteBatch.end();
		// draw hitboxes and stuff here
		if (debugMode) {
			debugRenderer.setProjectionMatrix(camera.combined);
			debugRenderer.setColor(Color.BLACK);
			debugRenderer.begin(ShapeType.Line);
			// draw player hitboxes if player alive
			Rectangle r;
			if (!playerExpireComplete) {
				r = player.getDefenseCollisionBounds();
				debugRender(r);
				// draw player terrain collision box too
				r = player.getCollisionBounds();
				debugRender(r);
			}
			// draw terrain collision bounds
			for (RectangleBoundedPolygon rbp : allPolys) { // all includes obstacle
				List<Polygon> ps = rbp.getSubPolygons();
				for (Polygon p : ps) {
					debugRenderer.polygon(p.getTransformedVertices()); // render subpolygon
				}
				// render bounding rectangle of the RectangleBoundedPolygon
				debugRender(rbp.getBoundingRectangle());
			}
			for (Rectangle rect : allRects) {
				debugRender(rect);
			}

			// draw other entities boxes
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
			debugRenderer.setColor(Color.GREEN);
			debugRenderer.begin(ShapeType.Line);
			for (Event event : events) {
				if (!event.hasTriggered()) debugRender(event.getBounds()); // draw untriggered events in green
			}
			debugRenderer.end();
			debugRenderer.setColor(Color.RED);
			debugRenderer.begin(ShapeType.Line);
			for (Event event : events) {
				if (event.hasTriggered()) debugRender(event.getBounds()); // draw already-triggered events in red
			}
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
