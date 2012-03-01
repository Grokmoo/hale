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

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import net.sf.hale.Area;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.tileset.FeatureType;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tile;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A widget for viewing and selecting available types of features
 * in the current tileset
 * @author Jared Stephen
 *
 */

public class FeaturePalette extends Widget {
	private final Area area;
	private final TerrainPalette terrain;
	private final Tileset tileset;
	
	private final ScrollPane scrollPane;
	private final Content scrollPaneContent;
	
	private FeatureViewer selectedViewer;
	
	private Set<String> featureSpriteIDs;
	
	/**
	 * Creates a new FeaturePalette for the features contained in the
	 * tileset for the specified Area
	 * @param area the Area
	 */
	
	public FeaturePalette(Area area, TerrainPalette terrain) {
		this.area = area;
		this.terrain = terrain;
		this.tileset = Game.curCampaign.getTileset(area.getTileset());
		
		setTheme("");
		
		scrollPaneContent = new Content();
		scrollPane = new ScrollPane(scrollPaneContent);
		scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		add(scrollPane);
		
		featureSpriteIDs = new HashSet<String>();
		for (String featureTypeID : tileset.getFeatureTypeIDs()) {
			FeatureType featureType = tileset.getFeatureType(featureTypeID);
			
			for (TerrainTile tile : featureType) {
				String spriteID = tileset.getLayer(tile.getLayerID()).getSpriteID(tile.getID());
				featureSpriteIDs.add(spriteID);
			}
		}
		
	}
	
	/**
	 * Returns the set of sprite IDs for all feature tiles in the tileset
	 * @return the set of sprite IDs for feature tiles
	 */
	
	public Set<String> getFeatureSpriteIDs() {
		return featureSpriteIDs;
	}
	
	@Override protected void layout() {
		scrollPane.setSize(getInnerWidth(), getInnerHeight());
		scrollPane.setPosition(getInnerX(), getInnerY());
	}
	
	/**
	 * Returns the preview Sprite for the currently selected feature type or null if
	 * no feature type is selected
	 * @return the preview Sprite for the selected feature type
	 */
	
	public Sprite getSelectedFeatureSprite() {
		if (selectedViewer == null)
			return null;
		else
			return selectedViewer.sprite;
	}
	
	private void removeFeaturesAtGridPoint(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		area.getTileGrid().removeTilesMatching(featureSpriteIDs, gridPoint.x, gridPoint.y);
		
		// set passability and transparency
		TerrainType terrainType = terrain.getTerrainAt(gridPoint);
		area.getPassability()[gridPoint.x][gridPoint.y] = terrainType != null ? terrainType.isPassable() : false;
		area.getTransparency()[gridPoint.x][gridPoint.y] = terrainType != null ? terrainType.isTransparent() : false;
	}
	
	/**
	 * Removes all features from the area centered on the specified gridPoint
	 * within the radius
	 * @param gridPoint the center point
	 * @param radius the radius of points around the center point
	 */
	
	public void removeAllFeatures(Point gridPoint, int radius) {
		removeFeaturesAtGridPoint(gridPoint);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				removeFeaturesAtGridPoint(current);
			}
		}
	}
	
	private void addFeatureAtGridPoint(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		TerrainType terrainType = terrain.getTerrainAt(gridPoint);
		if (!selectedViewer.featureType.canBeAddedTo(terrainType)) return;
		
		TerrainTile terrainTile = selectedViewer.featureType.getRandomTerrainTile();
		String spriteID = tileset.getLayer(terrainTile.getLayerID()).getSpriteID(terrainTile.getID());
		
		Tile tile = new Tile(terrainTile.getID(), spriteID);
		tile.cacheSprite();
		area.getTileGrid().addTile(tile, terrainTile.getLayerID(), gridPoint.x, gridPoint.y);
		
		// set base passability based on terrain & elevation
		terrain.setPassabilityAndTransparency(gridPoint, 0);
		
		// modify passability based on feature
		if (!selectedViewer.featureType.isPassable()) {
			area.getPassability()[gridPoint.x][gridPoint.y] = false;
		}
		
		// modify visibility based on feature
		if (!selectedViewer.featureType.isTransparent()) {
			area.getTransparency()[gridPoint.x][gridPoint.y] = false;
		}
	}
	
	/**
	 * Adds the currently selected feature to the area centered on the specified
	 * gridPoint within the radius
	 * This will remove any previous features in tiles that are affected
	 * @param gridPoint the center point
	 * @param radius the radius of points around the gridPoint to add features to
	 */
	
	public void addSelectedFeature(Point gridPoint, int radius) {
		if (selectedViewer == null) return;
		
		removeAllFeatures(gridPoint, radius);
		
		addFeatureAtGridPoint(gridPoint);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				addFeatureAtGridPoint(current);
			}
		}
	}
	
	/**
	 * Sets the internal grid of Features to fit any changes in area size
	 */
	
	public void resize() {
		// this is currently unused but may need to be used in the future if
		// we need to keep track of features like we do for terrain
	}
	
	private class Content extends Widget {
		private Content() {
			setTheme("");
			
			for (String featureID : tileset.getFeatureTypeIDs()) {
				FeatureType featureType = tileset.getFeatureType(featureID);
				FeatureViewer viewer = new FeatureViewer(featureType);
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
	
	private class FeatureViewer extends ToggleButton implements Runnable {
		private FeatureType featureType;
		private TerrainTile tile;
		private Sprite sprite;
		
		private FeatureViewer(FeatureType featureType) {
			super(featureType.getID());
			this.featureType = featureType;
			this.tile = featureType.getPreviewTile();
			String spriteID = tileset.getLayer(tile.getLayerID()).getSpriteID(tile.getID());
			this.sprite = SpriteManager.getImage(spriteID);
			
			addCallback(this);
		}
		
		@Override protected void applyTheme(ThemeInfo themeInfo) {
			super.applyTheme(themeInfo);
			
			setAlignment(Alignment.TOP);
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
