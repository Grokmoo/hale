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

import org.lwjgl.opengl.GL11;

import net.sf.hale.Area;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.tileset.Tile;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.tileset.Layer;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

/**
 * A widget for viewing and selecting from the list of tiles within
 * the current tileset
 * @author Jared Stephen
 *
 */

public class TilePalette extends Widget {
	private Area area;
	
	private TileViewer selectedTile;
	
	private Tileset tileset;
	private int currentLayerIndex;
	private SimpleChangableListModel<String> layers;
	
	private Label layerAdjusterLabel;
	private LayerAdjuster layerAdjuster;
	private ScrollPane tilesPane;
	private TilesContent tilesContent;
	
	/**
	 * Creates a new TilePalette using the tileset for the given area.
	 * @param area
	 */
	
	public TilePalette(Area area) {
		this.area = area;
		
		setTheme("");
		
		tileset = Game.curCampaign.getTileset(area.getTileset());
		
		layers = new SimpleChangableListModel<String>();
		layers.addElements(tileset.getLayerIDs());
		currentLayerIndex = 0;
		
		layerAdjusterLabel = new Label("Layer");
		layerAdjusterLabel.setTheme("labelblack");
		add(layerAdjusterLabel);
		
		layerAdjuster = new LayerAdjuster();
		layerAdjuster.setTheme("");
		add(layerAdjuster);
		
		tilesContent = new TilesContent();
		tilesPane = new ScrollPane(tilesContent);
		tilesPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		add(tilesPane);
		
		tilesContent.addCurrentLayerTiles();
	}
	
	/**
	 * Returns the sprite for the currently selected tile, or null if no tile is selected
	 * @return the sprite for the currently selected tile
	 */
	
	public Sprite getSelectedTileSprite() {
		if (selectedTile == null)
			return null;
		else
			return selectedTile.sprite;
	}
	
	private void removeTilesAtGridPoint(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		area.getTileGrid().removeTilesInLayer(layers.getEntry(currentLayerIndex), gridPoint.x, gridPoint.y);
	}
	
	private void addTileAtGridPoint(Point gridPoint) {
		if (gridPoint.x < 0 || gridPoint.x >= area.getWidth()) return;
		if (gridPoint.y < 0 || gridPoint.y >= area.getHeight()) return;
		
		Tile tile = new Tile(selectedTile.tileID, selectedTile.spriteID);
		tile.cacheSprite();
		area.getTileGrid().addTile(tile, layers.getEntry(currentLayerIndex), gridPoint.x, gridPoint.y);
	}
	
	/**
	 * Adds the currently selected tile from the tileset to the current area
	 * @param gridPoint the center grid point to add the tile to
	 * @param radius the radius of points around the grid point to add the tile to
	 */
	
	public void addSelectedTile(Point gridPoint, int radius) {
		if (selectedTile == null) return;
		
		addTileAtGridPoint(gridPoint);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				addTileAtGridPoint(current);
			}
		}
	}
	
	/**
	 * Removes all tiles at the specified points from the currently selected layer
	 * @param gridPoint the center grid point to remove tiles from
	 * @param radius the radius of points around the center to remove tiles from
	 */
	
	public void removeCurrentLayerTiles(Point gridPoint, int radius) {
		removeTilesAtGridPoint(gridPoint);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				removeTilesAtGridPoint(current);
			}
		}
	}
	
	@Override protected void layout() {
		super.layout();
		
		layerAdjuster.setSize(layerAdjuster.getPreferredWidth(), layerAdjuster.getPreferredHeight());
		layerAdjuster.setPosition(getInnerX(), getInnerBottom() - layerAdjuster.getHeight());
		
		layerAdjusterLabel.setPosition(getInnerX(),
				layerAdjuster.getY() - layerAdjusterLabel.getPreferredHeight() / 2);
		
		tilesPane.setPosition(getInnerX(), getInnerY());
		tilesPane.setSize(getInnerWidth(),
				layerAdjusterLabel.getY() - layerAdjusterLabel.getPreferredHeight() / 2 - getInnerY());
	}
	
	private class TilesContent extends Widget {
		private TilesContent() {
			setTheme("");
		}
		
		private void addCurrentLayerTiles() {
			removeAllChildren();
			
			Layer layer = tileset.getLayer(layers.getEntry(currentLayerIndex));
			
			for (String tileID : layer.getTiles()) {
				String spriteID = layer.getSpriteID(tileID);
				Sprite sprite = SpriteManager.getImage(spriteID);
				TileViewer viewer = new TileViewer(sprite, spriteID, tileID);
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
	
	private class TileViewer extends ToggleButton implements Runnable {
		private Sprite sprite;
		private String tileID;
		private String spriteID;
		
		private TileViewer(Sprite sprite, String spriteID, String tileID) {
			this.sprite = sprite;
			this.spriteID = spriteID;
			this.tileID = tileID;
			
			addCallback(this);
			
			setTooltipContent(spriteID);
		}
		
		@Override public void run() {
			if (selectedTile != null) {
				selectedTile.setActive(false);
			}
			
			selectedTile = this;
			this.setActive(true);
		}
		
		@Override public int getPreferredWidth() {
			return sprite.getWidth() + getBorderHorizontal();
		}
		
		@Override public int getPreferredHeight() {
			return sprite.getHeight() + getBorderVertical();
		}
		
		@Override protected void paintWidget(GUI gui) {
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
			sprite.draw(getInnerX(), getInnerY());
		}
	}
	
	private class LayerAdjuster extends DialogLayout {
		private ComboBox<String> layerSelector;
		
		private LayerAdjuster() {
			layerSelector = new ComboBox<String>(layers);
			layerSelector.setSelected(currentLayerIndex);
			layerSelector.addCallback(new Runnable() {
				@Override public void run() {
					currentLayerIndex = layerSelector.getSelected();
					
					setCurrentLayer();
				}
			});
			
			Button next = new Button("+");
			next.addCallback(new Runnable() {
				@Override public void run() {
					nextLayer();
				}
			});
			
			Button prev = new Button("-");
			prev.addCallback(new Runnable() {
				@Override public void run() {
					prevLayer();
				}
			});
			
			setHorizontalGroup(createSequentialGroup(prev, layerSelector, next));
			setVerticalGroup(createParallelGroup(prev, layerSelector, next));
		}
		
		private void nextLayer() {
			currentLayerIndex++;
			if (currentLayerIndex >= layers.getNumEntries()) currentLayerIndex = 0;
			
			setCurrentLayer();
		}
		
		private void prevLayer() {
			currentLayerIndex--;
			if (currentLayerIndex < 0) currentLayerIndex = layers.getNumEntries() - 1;
			
			setCurrentLayer();
		}
		
		private void setCurrentLayer() {
			selectedTile = null;
			
			layerSelector.setSelected(currentLayerIndex);
			adjustSize();
			
			tilesContent.addCurrentLayerTiles();
		}
	}
}
