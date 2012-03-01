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

import java.util.HashSet;
import java.util.Set;

import net.sf.hale.Area;
import net.sf.hale.Game;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.tileset.AreaElevationGrid;
import net.sf.hale.tileset.Border;
import net.sf.hale.tileset.BorderTile;
import net.sf.hale.tileset.ElevationList;
import net.sf.hale.tileset.Tile;
import net.sf.hale.tileset.Tileset;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleIntegerModel;

/**
 * A widget for handling changes to elevation in an area
 * @author Jared
 *
 */

public class ElevationPalette extends Widget {
	private ToggleButton showValues;
	private final SimpleIntegerModel minElevModel, maxElevModel;
	
	private final AreaElevationGrid grid;
	
	private final Area area;
	private final TerrainPalette terrain;
	private final FeaturePalette features;
	private final Tileset tileset;
	
	private final Set<String> elevationTileIDs;
	
	private boolean[][] impass;
	
	/**
	 * Creates a new ElevationPalette for the specified area
	 * @param area the area
	 * @param terrain the terrain palette specifying the terrain type for
	 * the specified area
	 */
	
	public ElevationPalette(Area area, TerrainPalette terrain, FeaturePalette features) {
		this.area = area;
		this.terrain = terrain;
		this.features = features;
		this.tileset = Game.curCampaign.getTileset(area.getTileset());
		
		this.grid = area.getElevationGrid();
		
		setTheme("");
		
		// get set of elevation tile IDs
		elevationTileIDs = new HashSet<String>();
		for (ElevationList.Elevation elevation : tileset.getElevationList().getElevationRules()) {
			Border border = elevation.getBorder();
			
			for (BorderTile tile : border) {
				String spriteID = tileset.getLayer(tile.getLayerID()).getSpriteID(tile.getID());
				elevationTileIDs.add(spriteID);
			}
		}
		
		// compute the initial state of the impass matrix
		impass = new boolean[area.getWidth()][area.getHeight()];
		for (int x = 0; x < area.getWidth(); x++) {
			for (int y = 0; y < area.getHeight(); y++) {
				computeImpassAt(new Point(x, y));
			}
		}
		
		minElevModel = new SimpleIntegerModel(-9, 0, -9);
		maxElevModel = new SimpleIntegerModel(0, 9, 9);
		
		add(new Content());
	}
	
	@Override protected void layout() {
		Widget child = getChild(0);
		
		child.setSize(getInnerWidth(), getInnerHeight());
		child.setPosition(getInnerX(), getInnerY());
	}
	
	/**
	 * Returns true if and only if the specified grid point is impassable based
	 * on the elevation rules
	 * @param gridPoint the point to test
	 * @return whether the point is impassable
	 */
	
	public boolean isImpassable(Point gridPoint) {
		return impass[gridPoint.x][gridPoint.y];
	}
	
	private void modifyElevationAt(byte amount, Point gridPoint) {
		byte curElev = grid.getElevation(gridPoint.x, gridPoint.y);
		
		if (amount > 0 && curElev + amount > maxElevModel.getValue())
			amount = 0;
		else if (amount < 0 && curElev + amount < minElevModel.getValue())
			amount = 0;
		
		grid.modifyElevation(gridPoint.x, gridPoint.y, amount);
	}
	
	/**
	 * Modifies the elevation of all tiles around the specified grid point by the specified amount
	 * @param amount the amount to modify the elevation by
	 * @param gridPoint the center tile to modify
	 * @param radius the radius of tiles around the center to modify
	 */
	
	public void modifyElevation(byte amount, Point gridPoint, int radius) {
		if (!isValidCoordinates(gridPoint)) return;
		
		// set the elevation to the specified value at each point
		modifyElevationAt(amount, gridPoint);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				if (!isValidCoordinates(current)) continue;
				
				modifyElevationAt(amount, current);
			}
		}
		
		// remove all features in the area
		features.removeAllFeatures(gridPoint, radius + 1);
		
		// remove all elevation tiles in the area
		removeElevationTilesAt(gridPoint);
		for (int r = 1; r <= radius + 1; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				if (!isValidCoordinates(p)) continue;

				removeElevationTilesAt(p);
			}
		}
		
		// reset impassability for the area
		resetImpass(gridPoint, radius + 1);
		
		// add elevation border tiles
		addBorderTilesAt(gridPoint);
		for (int r = 1; r <= radius + 2; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				if (!isValidCoordinates(p)) continue;

				addBorderTilesAt(p);
			}
		}
		
		// set passability / visibility for the area
		terrain.setPassabilityAndTransparency(gridPoint, radius + 1);
	}
	
	private void resetImpass(Point gridPoint, int radius) {
		impass[gridPoint.x][gridPoint.y] = false;
		for (int r = 1; r <= radius + 2; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point p = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				if (!isValidCoordinates(p)) continue;

				impass[p.x][p.y] = false;
			}
		}
	}
	
	private void computeImpassAt(Point gridPoint) {
		for (ElevationList.Elevation elevation : tileset.getElevationList().getMatchingElevationRules(grid, gridPoint)) {
			if (elevation.getImpassable() != null) {
				Point impassPoint = elevation.getImpassable().getRelativePoint(gridPoint);
				
				if (isValidCoordinates(impassPoint))
					impass[impassPoint.x][impassPoint.y] = true;
			}
		}
	}
	
	private void removeElevationTilesAt(Point gridPoint) {
		if (!isValidCoordinates(gridPoint)) return;
		
		area.getTileGrid().removeTilesMatching(elevationTileIDs, gridPoint.x, gridPoint.y);
	}
	
	private void addBorderTilesAt(Point gridPoint) {
		for (ElevationList.Elevation elevation : tileset.getElevationList().getMatchingElevationRules(grid, gridPoint)) {
			Border border = elevation.getBorder();
			
			for (BorderTile borderTile : border) {
				Point p = borderTile.getPosition().getRelativePoint(gridPoint);
				
				if (!isValidCoordinates(p)) continue;

				String spriteID = tileset.getLayer(borderTile.getLayerID()).getSpriteID(borderTile.getID());
				Tile tile = new Tile(borderTile.getID(), spriteID);
				tile.cacheSprite();
				
				area.getTileGrid().addTile(tile, borderTile.getLayerID(), p.x, p.y);
			}
			
			if (elevation.getImpassable() != null) {
				Point impassPoint = elevation.getImpassable().getRelativePoint(gridPoint);
				
				if (isValidCoordinates(impassPoint))
					impass[impassPoint.x][impassPoint.y] = true;
			}
		}
	}
	
	private boolean isValidCoordinates(Point p) {
		if (p.x < 0 || p.y < 0) return false;
		
		return p.x < area.getWidth() && p.y < area.getHeight();
	}
	
	/**
	 * Resizes the internal grid of elevation types to fit any changes in the
	 * size of the Area
	 */
	
	public void resize() {
		boolean[][] newImpass = new boolean[area.getWidth()][area.getHeight()];
		
		int copyWidth = Math.min(area.getWidth(), impass.length);
		int copyHeight = Math.min(area.getHeight(), impass[0].length);
		
		for (int x = 0; x < copyWidth; x++) {
			for (int y = 0; y < copyHeight; y++) {
				newImpass[x][y] = impass[x][y];
			}
		}
		
		this.impass = newImpass;
	}
	
	/**
	 * Returns whether the elevation values should be shown throughout the area
	 * @return whether the elevation values should be shown
	 */
	
	public boolean showValues() {
		return showValues.isActive();
	}
	
	private class Content extends DialogLayout {
		private Content() {
			setTheme("");
			
			Label leftClick = new Label("Left click to increase elevation.");
			leftClick.setTheme("labelblack");
			
			Label rightClick = new Label("Right click to decrease elevation");
			rightClick.setTheme("labelblack");
			
			add(leftClick);
			add(rightClick);
			
			Label explain = new Label("Bounds on current elevation edits:");
			explain.setTheme("labelblack");
			
			Label maxLabel = new Label("Max");
			maxLabel.setTheme("labelblack");
			
			ValueAdjusterInt max = new ValueAdjusterInt(maxElevModel);
			
			Label minLabel = new Label("Min");
			minLabel.setTheme("labelblack");
			
			ValueAdjusterInt min = new ValueAdjusterInt(minElevModel);
			
			// the spaces in the label here actually improve the button layout
			showValues = new ToggleButton("   Show Elevation");
			showValues.setTheme("checkbutton");
			
			Group valsH = createSequentialGroup(showValues);
			valsH.addGap(1);
			Group valsV = createParallelGroup(showValues);
			
			Group maxH = createSequentialGroup(maxLabel, max);
			Group maxV = createParallelGroup(maxLabel, max);
			
			Group minH = createSequentialGroup(minLabel, min);
			Group minV = createParallelGroup(minLabel, min);
			
			Group mainH = createParallelGroup(leftClick, rightClick);
			mainH.addWidget(explain);
			mainH.addGroups(minH, maxH, valsH);
			
			Group mainV = createSequentialGroup(leftClick, rightClick);
			mainV.addGap(20);
			mainV.addWidget(explain);
			mainV.addGroups(minV, maxV);
			mainV.addGap(20);
			mainV.addGroup(valsV);
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
		}
	}
}
