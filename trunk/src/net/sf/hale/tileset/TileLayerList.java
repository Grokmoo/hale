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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import net.sf.hale.Area;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Trap;
import net.sf.hale.util.Point;

/**
 * A List of tiles at a given layer within a tileset in an area.
 * @author Jared Stephen
 *
 */

public class TileLayerList {
	private TileList[][] tiles;
	private String layerID;
	
	// helpers for drawing entity tiles
	private boolean[][] explored;
	private boolean[][] visibility;
	private Area area;
	
	/**
	 * Creates a new TileLayerList of the specified size.  All added tiles
	 * must be within the size of the list
	 * @param width the width of the area
	 * @param height the height of the area
	 */
	
	public TileLayerList(String layerID, int width, int height) {
		this.layerID = layerID;
		tiles = new TileList[width][height];
		
		// initialize an empty TileList at each point
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				tiles[x][y] = new TileList();
			}
		}
	}
	
	/**
	 * Resizes this layer to the specified dimensions.  Tile data is saved
	 * @param newWidth the new width
	 * @param newHeight the new height
	 */
	
	public void resize(int newWidth, int newHeight) {
		if (newWidth == tiles.length && newHeight == tiles[0].length) return;
		
		TileList[][] newTiles = new TileList[newWidth][newHeight];

		int copyWidth = Math.min(newTiles.length, tiles.length);
		int copyHeight = Math.min(newTiles[0].length, tiles[0].length);
		
		for (int i = 0; i < copyWidth; i++) {
			for (int j = 0; j < copyHeight; j++) {
				newTiles[i][j] = tiles[i][j];
			}
		}
		
		// initialize an empty TileList at each empty point
		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				if (newTiles[x][y] == null)
					newTiles[x][y] = new TileList();
			}
		}
		
		this.tiles = newTiles;
	}
	
	/**
	 * Returns the list of tiles at the specified coordinates in this layer.  The
	 * returned list is unmodifiable
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 * @return the list of tiles at the specified coordinates
	 */
	
	public List<Tile> getTilesAt(int x, int y) {
		return Collections.unmodifiableList(tiles[x][y]);
	}
	
	/**
	 * Writes the tile data contained in this List out to the specified Writer
	 * @param out the Writer to write with
	 */
	
	protected void write(BufferedWriter out) throws IOException {
		for (int y = tiles[0].length - 1; y >= 0; y--) {
			for (int x = tiles.length - 1; x >= 0; x--) {
				tiles[x][y].write(out, x, y);
			}
		}
	}
	
	/**
	 * Caches all sprites to be drawn within this layer for faster drawing
	 */
	
	protected void cacheSprites() {
		for (int y = tiles[0].length - 1; y >= 0; y--) {
			for (int x = tiles.length - 1; x >= 0; x--) {
				tiles[x][y].cacheSprites();
			}
		}
	}
	
	/**
	 * Adds the specified sprite to be drawn at the specified coordinates
	 * @param tileID the ID of the tile to add
	 * @param spriteID the sprite to add
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	protected void addTile(String tileID, String spriteID, int x, int y) {
		tiles[x][y].add(new Tile(tileID, spriteID));
	}
	
	/**
	 * Adds the specified tile to the list of tiles in this layer.  This method
	 * enforces that no duplicate tiles are added
	 * @param tile the tile to add
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	protected void addTile(Tile tile, int x, int y) {
		for (Tile existingTile : tiles[x][y]) {
			if (existingTile.getTileID().equals(tile.getTileID())) return;
		}
		
		tiles[x][y].add(tile);
	}
	
	/**
	 * Removes all tiles in this layer at the specified coordinates except those with a
	 * sprite ID contained in the set
	 * @param spriteIDs the set of sprite IDs
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	protected void removeTilesExceptMatching(Set<String> spriteIDs, int x, int y) {
		Iterator<Tile> iter = tiles[x][y].iterator();
		while (iter.hasNext()) {
			if (!spriteIDs.contains(iter.next().getSpriteID()) ) {
				iter.remove();
			}
		}
	}
	
	/**
	 * Removes all tiles in this layer at the specified coordinates that match
	 * any of the sprite IDs contained in the set
	 * @param spriteIDs the set of sprite IDs to check against
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	protected void removeTilesMatching(Set<String> spriteIDs, int x, int y) {
		Iterator<Tile> iter = tiles[x][y].iterator();
		while (iter.hasNext()) {
			if ( spriteIDs.contains(iter.next().getSpriteID()) ) {
				iter.remove();
			}
		}
	}
	
	/**
	 * Removes all Tiles in this layer at the specified coordinates
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 */
	
	protected void removeTiles(int x, int y) {
		tiles[x][y].clear();
	}
	
	/**
	 * Draws all tiles in this TileLayerList
	 * @param screenCoordinates the array of screen coordinates for the set
	 * of grid points in this layer
	 * @param topLeft the top left grid point drawing bound, this point must be within the
	 * bounds of the tile grid and the x coordinate must be even
	 * @param bottomRight the bottom right grid point drawing bound, this point must be within the
	 * bounds of the tile grid
	 */
	
	protected void draw(Point[][] screenCoordinates, Point topLeft, Point bottomRight) {
		for (int y = topLeft.y; y <= bottomRight.y; y++) {
			for (int x = topLeft.x; x <= bottomRight.x; x += 2) {
				draw(x, y, screenCoordinates[x][y].x, screenCoordinates[x][y].y);
			}
			
			for (int x = topLeft.x + 1; x <= bottomRight.x; x += 2) {
				draw(x, y, screenCoordinates[x][y].x, screenCoordinates[x][y].y);
			}
		}
	}
	
	private final void drawEntityTile(int x, int y, Point screen, Point selected) {
		draw(x, y, screen.x, screen.y);
		
		// dont draw entities in unexplored tiles
		if (!explored[x][y]) return;
		
		Set<Entity> entities = area.getEntities().getEntitiesSet(x, y);
		if (entities == null) return;
		
		for (Entity e : entities) {
			if (!visibility[x][y] && e.getType() != Entity.Type.DOOR) continue;
			
			if (e.getType() == Entity.Type.TRAP && !((Trap)e).isVisible()) continue;
			
			if (e.isSelected()) {
				selected.x = e.getX();
				selected.y = e.getY();
				selected.valid = true;
			}
			
			e.drawForArea(screen.x, screen.y);
			
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
		}
	}
	
	/**
	 * Draws all tiles in this layer, also drawing entities within the layer
	 * @param screenCoordinates the array of screen coordinates for the set
	 * of grid points in this layer
	 * @param renderer the renderer for the area being drawn
	 * @returns the grid location of the selected entity, or null if there is no
	 * selected entity
	 * @param topLeft the top left grid point drawing bound, this point must be within the
	 * bounds of the tile grid and the x coordinate must be even
	 * @param bottomRight the bottom right grid point drawing bound, this point must be within the
	 * bounds of the tile grid
	 */
	
	protected Point draw(Point[][] screenCoordinates, AreaTileGrid.AreaRenderer renderer, Point topLeft, Point bottomRight) {
		Point selected = new Point(false);
		
		area = renderer.getArea();
		visibility = area.getVisibility();
		explored = area.getExplored();
		
		for (int y = topLeft.y; y <= bottomRight.y; y++) {
			for (int x = topLeft.x; x <= bottomRight.x; x += 2) {
				drawEntityTile(x, y, screenCoordinates[x][y], selected);
			}
			
			for (int x = topLeft.x + 1; x <= bottomRight.x; x += 2) {
				drawEntityTile(x, y, screenCoordinates[x][y], selected);
			}
		}
		
		return selected;
	}
	
	private final void draw(int gridX, int gridY, int screenX, int screenY) {
		for (Tile tile : tiles[gridX][gridY]) {
			tile.draw(screenX, screenY);
		}
	}
	
	private class TileList extends ArrayList<Tile> {
		private static final long serialVersionUID = 1L;

		private TileList() {
			super(1);
		}
		
		private void cacheSprites() {
			for (Tile tile : this) {
				tile.cacheSprite();
			}
		}
		
		private void write(BufferedWriter out, int x, int y) throws IOException {
			for (Tile tile : this) {
				out.write("tile ");
				out.write(layerID);
				out.write(" ");
				out.write(Integer.toString(x));
				out.write(" ");
				out.write(Integer.toString(y));
				out.write(" ");
				out.write(tile.getTileID());
				out.newLine();
			}
		}
	}
}