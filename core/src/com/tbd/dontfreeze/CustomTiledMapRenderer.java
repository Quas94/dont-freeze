package com.tbd.dontfreeze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

/**
 * Extension of the OrthogonalTiledMapRenderer.
 *
 * Created by Quasar on 17/06/2015.
 */
public class CustomTiledMapRenderer extends OrthogonalTiledMapRenderer {

	public static final String SPRITE_LAYER = "sprites";

	public CustomTiledMapRenderer(TiledMap tiledMap) {
		super(tiledMap);
	}

	/**
	 * Renders all map layers that are not the Sprite layer.
	 */
	public void renderNonSpriteLayers() {
		beginRender();
		for (MapLayer layer : map.getLayers()) {
			if (layer.isVisible() && !layer.getName().equals(SPRITE_LAYER)) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer((TiledMapTileLayer)layer);
				} if (layer instanceof TiledMapImageLayer) {
					renderImageLayer((TiledMapImageLayer)layer);
				} else {
					renderObjects(layer);
				}
			}
		}
		endRender();
	}

	/**
	 * Special method for rendering the sprite layer only.
	 *
	 * This method is called two times every frame.
	 *
	 * To start off, this method's first call, renders everything up to the player's Y position
	 * Player sprite/animation is then rendered
	 * Lastly, this method is called again, renders everything starting from the player's Y position (+1), and
	 * finishing off the rest of the screen.
	 *
	 * This is so the player is rendered behind obstacles that are further down, but rendered in front of obstacles that
	 * are further up.
	 *
	 * @param beforePlayer whether this is being rendered before the player has been rendered
	 * @param playerY the Y coordinate of the player
	 */
	public void renderSpriteLayer(boolean beforePlayer, float playerY) {
		beginRender();
		for (MapLayer layer : map.getLayers()) {
			if (layer.isVisible()) {
				if (layer.getName().equals(SPRITE_LAYER)) { // special sprite layer
					renderSpriteLayerInternal((TiledMapTileLayer) layer, beforePlayer, playerY);
				}
			}
		}
		endRender();
	}

	/**
	 * Static constants for renderSpriteLayerInternal (see below for further explanation).
	 */
	private static final int MAX_SPRITE_WIDTH = 300;
	private static final int MAX_SPRITE_HEIGHT = 300;

	/**
	 * Renders the sprite layer for the game.
	 *
	 * Majority of code borrowed from OrthogonalTiledMapRenderer#renderTileLayer with the following modifications:
	 *
	 * - the col1 and row1 local variables are decreased by MAX_SPRITE_WIDTH and MAX_SPRITE_HEIGHT respectively because
	 *   otherwise a Collection-of-Images-Tileset in Tiled will not be drawn at all if the bottom left corner is off
	 *   the screen, resulting in the sprite instantly disappearing completely as soon as the origin pixel is offscreen.
	 *   @TODO: this is a TEMPORARY solution: will not scale well when MAX_SPRITE_WIDTH/HEIGHT need to be very large
	 *
	 * - only the upper portion or lower portion of this layer will be rendered, depending on the value of beforePlayer.
	 *   see documentation for renderSpriteLayer() for further information on this.
	 *   @TODO: there seems to be an off-by-one-pixel error somewhere, as lines seem to get a tiny bit darker or lighter
	 *          depending on the Y coordinate of the player
	 *
	 * @param layer The TiledMapTileLayer object this method is rendering.
	 * @param beforePlayer Denotes which half of this layer is to be rendered: true means the first half, before the
	 *        player is to be rendered. False denotes the second half, which is to be done after the player is rendered.
	 *        This is so the player will appear to be behind sprites that are lower down that them.
	 */
	private void renderSpriteLayerInternal(TiledMapTileLayer layer, boolean beforePlayer, float playerYRaw) {
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		int col1 = Math.max(0, (int)(viewBounds.x / layerTileWidth));
		col1 -= MAX_SPRITE_WIDTH;
		int col2 = Math.min(layerWidth, (int)((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

		int row1 = Math.max(0, (int)(viewBounds.y / layerTileHeight));
		row1 -= MAX_SPRITE_HEIGHT;
		int row2 = Math.min(layerHeight, (int)((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));

		int playerY = Math.round(playerYRaw);
		if (beforePlayer) {
			row1 = playerY; // we will be rendering up to player y pos
		} else {
			row2 = playerY + 1; // we will be rendering everything after player y pos
		}

		float y = row2 * layerTileHeight;
		float xStart = col1 * layerTileWidth;
		final float[] vertices = this.vertices;

		for (int row = row2; row >= row1; row--) {
			float x = xStart;
			for (int col = col1; col < col2; col++) {
				final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
				if (cell == null) {
					x += layerTileWidth;
					continue;
				}
				final TiledMapTile tile = cell.getTile();

				if (tile != null) {
					final boolean flipX = cell.getFlipHorizontally();
					final boolean flipY = cell.getFlipVertically();
					final int rotations = cell.getRotation();

					TextureRegion region = tile.getTextureRegion();

					float x1 = x + tile.getOffsetX() * unitScale;
					float y1 = y + tile.getOffsetY() * unitScale;
					float x2 = x1 + region.getRegionWidth() * unitScale;
					float y2 = y1 + region.getRegionHeight() * unitScale;

					float u1 = region.getU();
					float v1 = region.getV2();
					float u2 = region.getU2();
					float v2 = region.getV();

					vertices[X1] = x1;
					vertices[Y1] = y1;
					vertices[C1] = color;
					vertices[U1] = u1;
					vertices[V1] = v1;

					vertices[X2] = x1;
					vertices[Y2] = y2;
					vertices[C2] = color;
					vertices[U2] = u1;
					vertices[V2] = v2;

					vertices[X3] = x2;
					vertices[Y3] = y2;
					vertices[C3] = color;
					vertices[U3] = u2;
					vertices[V3] = v2;

					vertices[X4] = x2;
					vertices[Y4] = y1;
					vertices[C4] = color;
					vertices[U4] = u2;
					vertices[V4] = v1;

					if (flipX) {
						float temp = vertices[U1];
						vertices[U1] = vertices[U3];
						vertices[U3] = temp;
						temp = vertices[U2];
						vertices[U2] = vertices[U4];
						vertices[U4] = temp;
					}
					if (flipY) {
						float temp = vertices[V1];
						vertices[V1] = vertices[V3];
						vertices[V3] = temp;
						temp = vertices[V2];
						vertices[V2] = vertices[V4];
						vertices[V4] = temp;
					}
					if (rotations != 0) {
						switch (rotations) {
							case Cell.ROTATE_90: {
								float tempV = vertices[V1];
								vertices[V1] = vertices[V2];
								vertices[V2] = vertices[V3];
								vertices[V3] = vertices[V4];
								vertices[V4] = tempV;

								float tempU = vertices[U1];
								vertices[U1] = vertices[U2];
								vertices[U2] = vertices[U3];
								vertices[U3] = vertices[U4];
								vertices[U4] = tempU;
								break;
							}
							case Cell.ROTATE_180: {
								float tempU = vertices[U1];
								vertices[U1] = vertices[U3];
								vertices[U3] = tempU;
								tempU = vertices[U2];
								vertices[U2] = vertices[U4];
								vertices[U4] = tempU;
								float tempV = vertices[V1];
								vertices[V1] = vertices[V3];
								vertices[V3] = tempV;
								tempV = vertices[V2];
								vertices[V2] = vertices[V4];
								vertices[V4] = tempV;
								break;
							}
							case Cell.ROTATE_270: {
								float tempV = vertices[V1];
								vertices[V1] = vertices[V4];
								vertices[V4] = vertices[V3];
								vertices[V3] = vertices[V2];
								vertices[V2] = tempV;

								float tempU = vertices[U1];
								vertices[U1] = vertices[U4];
								vertices[U4] = vertices[U3];
								vertices[U3] = vertices[U2];
								vertices[U2] = tempU;
								break;
							}
						}
					}
					batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
				}
				x += layerTileWidth;
			}
			y -= layerTileHeight;
		}
	}
}
