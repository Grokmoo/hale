/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.tileset;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.matthiasmann.twl.AnimationState;

import net.sf.hale.Area;
import net.sf.hale.Game;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A grid of tiles for a given area.  Handles the drawing of the Area
 * @author Jared Stephen
 *
 */

public class AreaTileGrid {
	private Tileset tileset;
	private Map<String, TileLayerList> tiles;
	
	private Point[][] screenCoordinates;
	
	/**
	 * Creates a new AreaTileGrid of the specified dimensions with the
	 * specified tileset
	 * @param tileset determines the set of layers that will be used for this Grid
	 * @param width the width of the area
	 * @param height the height of the area
	 */
	
	public AreaTileGrid(Tileset tileset, int width, int height) {
		this.tileset = tileset;
		
		tiles = new LinkedHashMap<String, TileLayerList>();
		
		for (String layerID : tileset.getLayerIDs()) {
			tiles.put(layerID, new TileLayerList(layerID, width, height));
		}
		
		// precompute screen coordinates
		screenCoordinates = new Point[width][height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				screenCoordinates[x][y] = AreaUtil.convertGridToScreen(x, y);
			}
		}
	}
	
	/**
	 * Resizes this TileGrid to the specified dimensions.  Tile data is saved
	 * @param newWidth the new width
	 * @param newHeight the new height
	 */
	
	public void resize(int newWidth, int newHeight) {
		for (String layerID : tiles.keySet()) {
			tiles.get(layerID).resize(newWidth, newHeight);
		}
		
		// precompute screen coordinates
		screenCoordinates = new Point[newWidth][newHeight];
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				screenCoordinates[x][y] = AreaUtil.convertGridToScreen(x, y);
			}
		}
	}
	
	/**
	 * Writes the tile data contained in this List out to the specified Writer
	 * @param out the Writer to write with
	 * @throws IOException 
	 */
	
	public void write(BufferedWriter out) throws IOException {
		for (String layerID : tileset.getLayerIDs()) {
			tiles.get(layerID).write(out);
		}
	}
	
	/**
	 * Returns the TileLayerList with the specified Layer ID, or null if no
	 * such TileLayerList exists
	 * @param layerID the ID of the layer to return
	 * @return the layer with the specified ID
	 */
	
	public TileLayerList getLayer(String layerID) {
		return tiles.get(layerID);
	}
	
	/**
	 * Returns the set containing the IDs of all layers contained in this AreaTileGrid
	 * @return the set of layer IDs for this AreaTileGrid
	 */
	
	public Set<String> getLayerIDs() {
		return Collections.unmodifiableSet(tiles.keySet());
	}
	
	/**
	 * Caches all tile sprites within this tile grid for faster drawing
	 */
	
	public void cacheSprites() {
		for (String layerID : tileset.getLayerIDs()) {
			tiles.get(layerID).cacheSprites();
		}
	}
	
	/**
	 * Removes all tiles in all layers at the specified coordinates that match
	 * any of the sprite IDs contained in the set
	 * @param spriteIDs the set of sprite IDs to check against
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	public void removeTilesMatching(Set<String> spriteIDs, int x, int y) {
		for (String layerID : tileset.getLayerIDs()) {
			tiles.get(layerID).removeTilesMatching(spriteIDs, x, y);
		}
	}
	
	/**
	 * Removes all tiles from all layers at the specified coordinates, except for
	 * those tiles with a Sprite ID contained in the specified set
	 * @param spriteIDs the set of sprite IDs 
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	public void removeTilesExceptMatching(Set<String> spriteIDs, int x, int y) {
		for (String layerID : tileset.getLayerIDs()) {
			tiles.get(layerID).removeTilesExceptMatching(spriteIDs, x, y);
		}
	}
	
	/**
	 * Removes all tiles in all layers at the specified grid coordinates
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	public void removeAllTiles(int x, int y) {
		for (String layerID : tileset.getLayerIDs()) {
			tiles.get(layerID).removeTiles(x, y);
		}
	}
	
	/**
	 * Removes all tiles in the specified layer at the specified coordinates
	 * @param layerID the ID String of the layer
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	public void removeTilesInLayer(String layerID, int x, int y) {
		tiles.get(layerID).removeTiles(x, y);
	}
	
	/**
	 * Adds a tile at the specified layer and coordinates
	 * @param tileID the tile resource image based on the layer
	 * @param layerID the String ID of the layer to add this tile to
	 * @param x the x grid coordinate for the tile
	 * @param y the y grid coordinate for the tile
	 */
	
	public void addTile(String tileID, String layerID, int x, int y) {
		TileLayerList tileLayerList = tiles.get(layerID);
		
		tileLayerList.addTile(tileID, tileset.getLayer(layerID).getSpriteID(tileID), x, y);
	}
	
	/**
	 * Adds the specified tile to the specified layer at the specified coordinates.  This method
	 * enforces that no duplicate tiles will be added
	 * @param tile the tile to add
	 * @param layerID the ID String of the layer to add the tile to
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	public void addTile(Tile tile, String layerID, int x, int y) {
		TileLayerList tileLayerList = tiles.get(layerID);
		
		tileLayerList.addTile(tile, x, y);
	}
	
	/**
	 * Draws this AreaTileGrid
	 * @param renderer the renderer for the Area being drawn
	 * @param as the current GUI animation state
	 * @param topLeft the top left grid point drawing bound, if non null then must be within
	 * the area bounds and topLeft.x must be an even number
	 * @param bottomRight the bottom right grid point drawing bound, if non null then must be within
	 * the area bounds
	 */
	
	public void draw(AreaRenderer renderer, AnimationState as, Point topLeft, Point bottomRight) {
		Point selected = null;
		
		String entityLayerID = tileset.getEntityLayerID();
		String interfaceLayerID = tileset.getInterfaceLayerID();
		
		// compute bounds if none were specified
		if (topLeft == null) {
			topLeft = new Point(0, 0);
		}
		
		if (bottomRight == null) {
			bottomRight = new Point(screenCoordinates.length - 1, screenCoordinates[0].length - 1);
		} 
		
		// TODO draw transitions within the layer like entities
		
		for (String layerID : tileset.getLayerIDs()) {
			if (layerID.equals(entityLayerID)) {
				Game.particleManager.drawBelowEntities();
				renderer.drawTransitions();
				selected = tiles.get(layerID).draw(screenCoordinates, renderer, topLeft, bottomRight);
				Game.particleManager.drawAboveEntities();
				
			} else if (layerID.equals(interfaceLayerID)) {
				tiles.get(layerID).draw(screenCoordinates, topLeft, bottomRight);
				renderer.drawInterface(selected, as);
			} else {
				tiles.get(layerID).draw(screenCoordinates, topLeft, bottomRight);
			}
		}
	}
	
	/**
	 * The interface used by this class when drawing in order to draw the non tile and
	 * entity objects in the Area
	 * @author Jared Stephen
	 *
	 */
	
	public interface AreaRenderer {
		/**
		 * Returns the Area that is being rendered by this AreaRenderer
		 * @return the Area being rendered
		 */
		
		public Area getArea();
		
		/**
		 * Draws all transitions in the Area at their coordinates
		 */
		
		public void drawTransitions();
		
		/**
		 * Draws interface elements such as the mouse hover over and the highlight
		 * for the currently selected entity
		 * @param selected the position of the currently selected entity, or null
		 * if there is no selected entity
		 */
		
		public void drawInterface(Point selected, AnimationState as);
	}
}
