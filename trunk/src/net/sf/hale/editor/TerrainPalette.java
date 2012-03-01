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

package net.sf.hale.editor;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.sf.hale.Area;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.tileset.Border;
import net.sf.hale.tileset.BorderList;
import net.sf.hale.tileset.BorderTile;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tile;
import net.sf.hale.tileset.TileLayerList;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * A widget for viewing and selecting from the available types of terrain
 * in the current tileset
 * @author Jared Stephen
 *
 */

public class TerrainPalette extends Widget {
	private TerrainViewer selectedViewer;
	
	private final Area area;
	private final Tileset tileset;
	
	private final ScrollPane scrollPane;
	private final Content scrollPaneContent;
	
	private TerrainType[][] terrain;
	
	private ElevationPalette elevation;
	private FeaturePalette features;
	
	/**
	 * Creates a new TerrainPalette using the tileset for the given area
	 * @param area the area
	 */
	
	public TerrainPalette(Area area) {
		this.area = area;
		this.tileset = Game.curCampaign.getTileset(area.getTileset());
		this.terrain = new TerrainType[area.getWidth()][area.getHeight()];
		
		this.setTheme("");
		
		scrollPaneContent = new Content();
		scrollPane = new ScrollPane(scrollPaneContent);
		scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		add(scrollPane);
	}
	
	/**
	 * Sets the FeaturePalette for use in determining pre-existing features
	 * when setting the terrain
	 * @param features the FeaturePalette
	 */
	
	public void setFeaturePalette(FeaturePalette features) {
		this.features = features;
	}
	
	public void setElevationPalette(ElevationPalette elevation) {
		this.elevation = elevation;
	}
	
	@Override protected void layout() {
		scrollPane.setSize(getInnerWidth(), getInnerHeight());
		scrollPane.setPosition(getInnerX(), getInnerY());
	}
	
	/**
	 * Returns the TerrainType for the Area at the specified grid point
	 * @param gridPoint the point to get the TerrainType at
	 * @return the TerrainType at the specified Point
	 */
	
	public TerrainType getTerrainAt(Point gridPoint) {
		if (terrain[gridPoint.x][gridPoint.y] == null)
			computeTerrainTypeAtPoint(gridPoint);
		
		return terrain[gridPoint.x][gridPoint.y];
	}
	
	/**
	 * Resizes the internal grid of terrain types to fit any changes in the
	 * size of the Area
	 */
	
	public void resize() {
		if (area.getWidth() == terrain.length && area.getHeight() == terrain[0].length) return;
		
		terrain = new TerrainType[area.getWidth()][area.getHeight()];
	}
	
	private void computeTerrainType(Point center, int startRadius, int endRadius) {
		for (int r = startRadius; r <= endRadius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(center, r, i);
				computeTerrainTypeAtPoint(current);
			}
		}
	}
	
	private void computeTerrainTypeAtPoint(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		for (String layerID : area.getTileGrid().getLayerIDs()) {
			TileLayerList list = area.getTileGrid().getLayer(layerID);
			terrain[gridPoint.x][gridPoint.y] =
				getTerrainTypeForTiles(list.getTilesAt(gridPoint.x, gridPoint.y));
			
			// stop searching through layers once we find a terrain type that works
			if (terrain[gridPoint.x][gridPoint.y] != null) return;
		}
	}
	
	private TerrainType getTerrainTypeForTiles(List<Tile> tiles) {
		for (Tile tile : tiles) {
			for (String terrainID : tileset.getTerrainTypeIDs()) {
				TerrainType type = tileset.getTerrainType(terrainID);
				if ( type.containsTileWithID(tile.getTileID()) ) {
					return type;
				}
			}
		}
		
		return null;
	}
	
	private void addBorderTilesAt(Point gridPoint) {
		for (BorderList borderList : tileset.getMatchingBorderLists(terrain, gridPoint)) {
			addMatchingTiles(gridPoint, borderList.getMatchingBorders(terrain, gridPoint));
		}
	}
	
	private void addMatchingTiles(Point gridPoint, List<Border> borders) {
		for (Border border : borders) {
			for (BorderTile borderTile : border) {
				Point p = borderTile.getPosition().getRelativePoint(gridPoint);
				if (p.x < 0 || p.x >= terrain.length || p.y < 0 || p.y >= terrain[0].length)
					continue;

				String spriteID = tileset.getLayer(borderTile.getLayerID()).getSpriteID(borderTile.getID());
				Tile tile = new Tile(borderTile.getID(), spriteID);
				tile.cacheSprite();
				
				//System.out.println("Adding " + spriteID + " " + SpriteManager.getImage(spriteID));
				
				area.getTileGrid().addTile(tile, borderTile.getLayerID(), p.x, p.y);
			}
		}
	}
	
	/**
	 * Returns the preview Sprite for the currently selected terrain type or null if
	 * no terrain type is selected
	 * @return the preview Sprite for the selected terrain type
	 */
	
	public Sprite getSelectedTerrainSprite() {
		if (selectedViewer == null)
			return null;
		else
			return selectedViewer.sprite;
	}
	
	private void removeTilesAtGridPointExceptFeatures(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		area.getTileGrid().removeTilesExceptMatching(features.getFeatureSpriteIDs(),
				gridPoint.x, gridPoint.y);
	}
	
	private void removeAllTilesAtGridPoint(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		area.getTileGrid().removeAllTiles(gridPoint.x, gridPoint.y);
	}
	
	/**
	 * Sets the terrain at the specified location to the currently selected terrain
	 * type.  If no terrain type is selected, no action is performed.  Also interprets
	 * all border rules and adds appropriate border tiles as needed
	 * @param gridPoint the center location
	 * @param radius the radius of tiles
	 */
	
	public void addSelectedTerrain(Point gridPoint, int radius) {
		if (selectedViewer == null) return;
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		// first clear the area of all tiles including features
		removeAllTilesAtGridPoint(gridPoint);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				removeAllTilesAtGridPoint(current);
			}
		}
		
		// compute terrain at all nearby tiles
		computeTerrainType(gridPoint, radius + 1, radius + 3);
		
		// set terrain at the specified tiles
		terrain[gridPoint.x][gridPoint.y] = selectedViewer.terrainType;
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				if (p.x < 0 || p.x >= terrain.length || p.y < 0 || p.y >= terrain[0].length)
					continue;
				
				terrain[p.x][p.y] = selectedViewer.terrainType;
			}
		}
		
		// clear the area of all tiles except feature tiles
		removeTilesAtGridPointExceptFeatures(gridPoint);
		for (int r = 1; r <= radius + 1; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				removeTilesAtGridPointExceptFeatures(current);
			}
		}
		
		//add randomly chosen terrain tiles to the area
		addTerrainTileAtGridPoint(gridPoint);
		for (int r = 1; r <= radius + 1; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				if (p.x < 0 || p.x >= terrain.length || p.y < 0 || p.y >= terrain[0].length)
					continue;
				
				addTerrainTileAtGridPoint(p);
			}
		}
		
		// add border tiles as needed
		addBorderTilesAt(gridPoint);
		for (int r = 1; r <= radius + 2; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				if (p.x < 0 || p.x >= terrain.length || p.y < 0 || p.y >= terrain[0].length)
					continue;
				
				addBorderTilesAt(p);
			}
		}
		
		// compute the passability and transparency at affected points
		setPassabilityAndTransparency(gridPoint, radius);
		
		// redo elevation for points as needed
		elevation.modifyElevation((byte)0, gridPoint, radius);
	}
	
	/**
	 * Sets the passability and transparency of the specified area based on the
	 * terrain passability and transparency rules and the elevation passability
	 * rules.  It is assumed that the area has no features
	 * @param gridPoint the center of the area to set
	 * @param radius the radius of the area to set
	 */
	
	public void setPassabilityAndTransparency(Point gridPoint, int radius) {
		setPassabilityAndTransparency(gridPoint);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				setPassabilityAndTransparency(p);
			}
		}
	}
	
	private void setPassabilityAndTransparency(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		boolean elevImpass = elevation.isImpassable(gridPoint);
		
		TerrainType terrainType = terrain[gridPoint.x][gridPoint.y];
		
		if (terrainType != null) {
			area.getPassability()[gridPoint.x][gridPoint.y] = terrainType.isPassable() && !elevImpass;
			area.getTransparency()[gridPoint.x][gridPoint.y] = terrainType.isTransparent();
		}
	}
	
	private void addTerrainTileAtGridPoint(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		TerrainType terrainType = terrain[gridPoint.x][gridPoint.y];
		if (terrainType == null) return;
		
		TerrainTile terrainTile = terrainType.getRandomTerrainTile();
		
		String spriteID = tileset.getLayer(terrainTile.getLayerID()).getSpriteID(terrainTile.getID());
		
		Tile tile = new Tile(terrainTile.getID(), spriteID);
		tile.cacheSprite();
		area.getTileGrid().addTile(tile, terrainTile.getLayerID(), gridPoint.x, gridPoint.y);
	}
	
	private class Content extends Widget {
		private Content() {
			setTheme("");
			
			for (String terrainID : tileset.getTerrainTypeIDs()) {
				TerrainType terrainType = tileset.getTerrainType(terrainID);
				TerrainViewer viewer = new TerrainViewer(terrainType);
				viewer.setTheme("/iconbutton");
				add(viewer);
			}
		}
		
		@Override protected void layout() {
			int maxBottom = getInnerY();
			
			int curY = getInnerY();
			int curX = getInnerX();
			
			for (int i = 0; i < getNumChildren(); i++) {
				Widget child = getChild(i);
				child.setSize(child.getPreferredWidth(), child.getPreferredHeight());
				if (curX + child.getWidth() > getInnerRight()) {
					curY = maxBottom + 1;
					curX = getInnerX();
				}
				
				child.setPosition(curX, curY);
				
				curX = child.getRight() + 1;
				maxBottom = Math.max(maxBottom, child.getBottom());
			}
		}
	}
	
	private class TerrainViewer extends ToggleButton implements Runnable {
		private TerrainType terrainType;
		private TerrainTile tile;
		private Sprite sprite;
		
		private TerrainViewer(TerrainType terrainType) {
			super(terrainType.getID());
			this.terrainType = terrainType;
			this.tile = terrainType.getPreviewTile();
			String spriteID = tileset.getLayer(tile.getLayerID()).getSpriteID(tile.getID());
			this.sprite = SpriteManager.getImage(spriteID);
			
			addCallback(this);
		}
		
		@Override public void run() {
			if (selectedViewer != null) {
				selectedViewer.setActive(false);
			}
			
			selectedViewer = this;
			setActive(true);
		}
		
		@Override public int getPreferredWidth() {
			return sprite.getWidth() + getBorderHorizontal();
		}
		
		@Override public int getPreferredHeight() {
			return sprite.getHeight() + getBorderVertical();
		}
		
		@Override protected void paintWidget(GUI gui) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);
			sprite.draw(getInnerX(), getInnerY());
			
			super.paintWidget(gui);
		}
	}
}
