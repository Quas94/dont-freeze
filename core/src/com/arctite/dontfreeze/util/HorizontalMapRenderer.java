package com.arctite.dontfreeze.util;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

/**
 * Extension of the OrthogonalTiledMapRenderer.
 *
 * Created by Quasar on 17/06/2015.
 */
public class HorizontalMapRenderer extends OrthogonalTiledMapRenderer {

	/** Map layers */
	public static final String SPRITE_LAYER = "sprites";
	public static final String OBSTACLES_LAYER = "obstacles";
	public static final String MONSTERS_LAYER = "monsters";
	public static final String COLLECTABLES_LAYER = "collectables";
	public static final String GROUNDLESS_LAYER = "groundless";
	public static final String BACKGROUND_LAYER = "background";
	public static final String EVENTS_LAYER = "events";

	/** Static constants for the renderSpriteLayerInternal() method */
	public static final int MAX_SPRITE_WIDTH = 300;
	public static final int MAX_SPRITE_HEIGHT = 300;

	public HorizontalMapRenderer(TiledMap tiledMap, SpriteBatch spriteBatch) {
		super(tiledMap, spriteBatch);
	}

	/**
	 * Called before the rendering of all layers starts. Overridden to not call batch.begin()
	 */
	@Override
	protected void beginRender () {
		// AnimatedTiledMapTile.updateAnimationBaseTime(); // not needed either since we don't use animated tiles
		// batch.begin();
	}

	/**
	 * Overriden to not call batch.end()
	 */
	@Override
	protected void endRender () {
		// batch.end();
	}

	/**
	 * Renders all map layers that are not the Sprite layer.
	 */
	public void renderBackgroundLayer() {
		super.beginRender();
		TiledMapImageLayer layer = (TiledMapImageLayer) map.getLayers().get(BACKGROUND_LAYER);
		renderImageLayer(layer);
		super.endRender();
	}

	/**
	 * Special method for rendering specific portions of the sprite layer.
	 *
	 * To start off, this method's first call, renders everything up to the player's Y position
	 * Player sprite/animation is then rendered
	 * Lastly, this method is called again, renders everything starting from the player's Y position (+1), and
	 * finishing off the rest of the screen.
	 *
	 * This is so that the Player/Entities/TiledObjects are rendered in order of their y coordinate, such that things
	 * that are lower y are rendered last (on top), for a more realistic effect.
	 *
	 * @param portionTop the y coordinate that rendering will begin at
	 * @param portionBot the y coordinate that rendering will stop at
	 * @param last whether or not this is the last call of this method for this particular frame
	 */
	public void renderSpriteLayer(int portionTop, int portionBot, boolean last) {
		beginRender();
		for (MapLayer layer : map.getLayers()) {
			if (layer.isVisible()) {
				if (layer.getName().equals(SPRITE_LAYER)) { // special sprite layer
					TiledMapTileLayer l = (TiledMapTileLayer) layer;
					renderSpriteLayerInternal(l, portionTop, portionBot, last);
				}
			}
		}
		endRender();
	}

	/**
	 * Renders the portion of the sprite layer specified.
	 *
	 * @param layer the TiledMap layer object
	 * @param portionTop the top coordinate of the portion (inclusive)
	 * @param portionBot the bottom coordinate of the potion (inclusive)
	 * @param last whether or not this is the last call of this method for this particular frame, in which case this
	 *             method will render deeper
	 */
	private void renderSpriteLayerInternal(TiledMapTileLayer layer, int portionTop, int portionBot, boolean last) {
		int sw = (int) viewBounds.width;
		// int sh = (int) viewBounds.height;
		int startX = (int) viewBounds.x;
		int endX = startX + sw;
		int startY = portionTop;
		int endY = portionBot;
		// now minus startX and endY by MAX_SPRITE_WIDTH/HEIGHT respectively because of the drawing issue
		startX -= MAX_SPRITE_WIDTH;
		if (last) { // render deeper only if this is the last call of the frame
			endY -= MAX_SPRITE_HEIGHT;
		}
		for (int y = startY; y >= endY; y--) { // render from the top down
			for (int x = startX; x <= endX; x++) {
				Cell cell = layer.getCell(x, y);
				if (cell == null) {
					continue; // don't render this position because nothing is here
				}
				TiledMapTile tile = cell.getTile();
				TextureRegion region = tile.getTextureRegion();
				batch.draw(region, x, y);
			}
		}
	}
}
