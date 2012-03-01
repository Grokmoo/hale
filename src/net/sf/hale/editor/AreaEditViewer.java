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

import net.sf.hale.Area;
import net.sf.hale.AreaTransition;
import net.sf.hale.AreaTrigger;
import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.entity.Creature;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.view.AreaViewer;
import net.sf.hale.tileset.AreaTileGrid;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;

public class AreaEditViewer extends AreaViewer implements AreaTileGrid.AreaRenderer {
	private AreaEditListener areaEditListener;
	private final AreaEditor areaEditor;
	
	public AreaEditViewer(AreaEditor areaEditor, Area area) {
		super(area);
		
		this.areaEditor = areaEditor;
	}
	
	@Override protected void setMaxScroll() {
		super.setMaxScroll();
		
		getMaxScroll().x += Game.TILE_SIZE * 5;
		getMaxScroll().y += Game.TILE_SIZE * 5;
		
		getMinScroll().x -= Game.TILE_SIZE * 5;
		getMinScroll().y -= Game.TILE_SIZE * 5;
	}
	
	/**
	 * Draws a tile outline at the specified grid coordinates
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 * @param border the amount by which the tile should be drawn inside the
	 * normal tile radius
	 */
	
	private final void drawGridTile(int x, int y, int border) {
		Point screenPoint = AreaUtil.convertGridToScreen(x, y);
		
		GL11.glBegin(GL11.GL_LINE_LOOP);
		
		GL11.glVertex2i(screenPoint.x + border, screenPoint.y + Game.TILE_SIZE / 2);
		GL11.glVertex2i(screenPoint.x + Game.TILE_SIZE / 4 + border, screenPoint.y + border);
		GL11.glVertex2i(screenPoint.x + Game.TILE_WIDTH - border, screenPoint.y + border);
		GL11.glVertex2i(screenPoint.x + Game.TILE_SIZE - border, screenPoint.y + Game.TILE_SIZE / 2);
		GL11.glVertex2i(screenPoint.x + Game.TILE_WIDTH - border, screenPoint.y + Game.TILE_SIZE - border);
		GL11.glVertex2i(screenPoint.x + Game.TILE_SIZE / 4 + border, screenPoint.y + Game.TILE_SIZE - border);
		
		GL11.glEnd();
	}
	
	@Override public void drawInterface(Point selected, AnimationState as) {
		Sprite sprite = null;
		if (mouseHoverTile.valid && areaEditor.getMode() != null) {
			switch (areaEditor.getMode()) {
			case Tiles:
				sprite = areaEditor.getTilePalette().getSelectedTileSprite();
				break;
			case Terrain:
				sprite = areaEditor.getTerrainPalette().getSelectedTerrainSprite();
				break;
			case Features:
				sprite = areaEditor.getFeaturePalette().getSelectedFeatureSprite();
				break;
			}
		}

		if (sprite != null) {
			int radius = areaEditor.getBrushRadius();

			GL11.glColor4f(0.7f, 0.7f, 0.7f, 0.7f);
			Point screen = AreaUtil.convertGridToScreen(mouseHoverTile);
			sprite.drawWithOffset(screen);

			for (int r = 1; r <= radius; r++) {
				for (int i = 0; i < r * 6; i++) {
					Point grid = AreaUtil.convertPolarToGrid(mouseHoverTile, r, i);
					screen = AreaUtil.convertGridToScreen(grid);
					sprite.drawWithOffset(screen);
				}
			}
		}
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
	}
	
	@Override public void paintWidget(GUI gui) {
		AnimationState as = gui.getAnimationState();
		
		GL11.glPushMatrix();
		GL11.glTranslatef(getInnerX() - getScrollX(), getInnerY() - getScrollY(), 0.0f);
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		getArea().getTileGrid().draw(this, as, null, null);
		
		// draw encounter creatures
		for (Encounter e : getArea().getEncounters()) {
			for (Creature c : e.getCreatures()) {
				Point p = AreaUtil.convertGridToScreen(c.getX(), c.getY());
				c.draw(p);
			}
		}
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glColor4f(1.0f, 0.0f, 0.5f, 0.5f);
		for (Point p : getArea().getEncounterPositions()) {
			fillGridTile(p.x, p.y);
		}
		
		AreaTrigger selectedTrigger = areaEditor.getSelectedTrigger();
		
		for (String s : getArea().getTriggers()) {
			AreaTrigger t = Game.curCampaign.getTrigger(s);
			
			for (Point p : t.getGridPoints()) {
				GL11.glColor4f(0.0f, 1.0f, 0.5f, 0.4f);
				fillGridTile(p.x, p.y);
				
				if (t == selectedTrigger) {
					GL11.glColor3f(1.0f, 1.0f, 1.0f);
					drawGridTile(p.x, p.y, 2);
				}
			}
		}
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
		if (mouseHoverTile.valid) {
			drawGridTile(mouseHoverTile.x, mouseHoverTile.y, 0);
			
			int radius = areaEditor.getBrushRadius();
			for (int r = 1; r <= radius; r++) {
				for (int i = 0; i < r * 6; i++) {
					Point grid = AreaUtil.convertPolarToGrid(mouseHoverTile, r, i);
					drawGridTile(grid.x, grid.y, 0);
				}
			}
		}
		
		AreaEditor.Mode mode = areaEditor.getMode();
		
		if (mode == AreaEditor.Mode.Visibility || mode == AreaEditor.Mode.Passability) {
			boolean[][] vis = getArea().getTransparency();
			boolean[][] pass = getArea().getPassability();
			for (int x = 0; x < getArea().getWidth(); x++) {
				for (int y = 0; y < getArea().getHeight(); y++) {
					if (!vis[x][y]) {
						GL11.glColor3f(1.0f, 1.0f, 0.0f);
						drawGridTile(x, y, 2);
					}

					if (!pass[x][y]) {
						GL11.glColor3f(1.0f, 0.0f, 0.0f);
						drawGridTile(x, y, 5);
					}
				}
			}
		}
		
		AreaTransition t = areaEditor.getSelectedTransition();
		if (t != null) {
			GL11.glColor3f(0.0f, 1.0f, 1.0f);
			if (this.getArea().getName().equals(t.getAreaFrom())) {
				drawGridTile(t.getAreaFromX(), t.getAreaFromY(), -1);
			} else {
				drawGridTile(t.getAreaToX(), t.getAreaToY(), -1);
			}
		}
		
		GL11.glColor3f(1.0f, 0.0f, 1.0f);
		GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex2i(Game.TILE_SIZE, Game.TILE_SIZE);
			GL11.glVertex2i((getArea().getWidth() - 1) * Game.TILE_WIDTH, Game.TILE_SIZE);
			GL11.glVertex2i((getArea().getWidth() - 1) * Game.TILE_WIDTH, getArea().getHeight() * Game.TILE_SIZE - Game.TILE_SIZE / 2);
			GL11.glVertex2i(Game.TILE_SIZE, getArea().getHeight() * Game.TILE_SIZE - Game.TILE_SIZE / 2);
		GL11.glEnd();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		if (mode == AreaEditor.Mode.StartLocations) { 
			int index = 1;
			for (Point p : getArea().getStartLocations()) {
				SpriteManager.getSprite("editor/" + index).drawWithOffset(AreaUtil.convertGridToScreen(p));
				index++;
			}
		} else if (mode == AreaEditor.Mode.Elevation && areaEditor.getElevationPalette().showValues()) {
			for (int x = 0; x < getArea().getWidth(); x++) {
				for (int y = 0; y < getArea().getHeight(); y++) {
					byte elev = getArea().getElevationGrid().getElevation(x, y);
					
					SpriteManager.getSprite("editor/" + elev).drawWithOffset(AreaUtil.convertGridToScreen(x, y));
				}
			}
		}
		
		GL11.glPopMatrix();
	}
	
	@Override public Point scroll(int x, int y) {
		Point p = super.scroll(x, y);
		
		areaEditListener.scroll(p);
		
		return p;
	}
	
	@Override protected boolean handleEvent(Event evt) {
		return areaEditListener.handleEvent(evt);
    }
	
	public void setListener(AreaEditListener listener) {
		this.areaEditListener = listener;
	}
}
