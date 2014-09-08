/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.swingeditor;

import java.awt.Canvas;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SpinnerNumberModel;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;
import net.sf.hale.area.Area;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.tileset.AreaTileGrid;
import net.sf.hale.tileset.Tile;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.util.PointImmutable;

/**
 * A class for viewing an area in the swing editor
 * @author Jared
 *
 */

public class AreaRenderer implements AreaTileGrid.AreaRenderer {
	private final Canvas canvas;
	private final Area area;
	
	private SpinnerNumberModel mouseRadius;
	private Point mouseGrid;
	private Point mouseScreen;
	private int scrollX, scrollY;
	
	private Tile actionPreviewTile;
	private AreaPalette.AreaClickHandler clickHandler;
	
	private static final int MaxRadius = 20;
	
	private long lastClickTime;
	private static final int MouseTimeout = 400;
	private boolean[] lastMouseState;
	
	private Point prevMouseGrid;
	
	/**
	 * Creates a new Viewer for the specified Area
	 * @param area
	 * @param canvas
	 */
	
	public AreaRenderer(Area area, Canvas canvas) {
		this.canvas = canvas;
		this.area = area;
		mouseRadius = new SpinnerNumberModel(0, 0, AreaRenderer.MaxRadius, 1);
		lastMouseState = new boolean[Mouse.getButtonCount()];
	}
	
	/**
	 * Draws the area this viewer is viewing
	 */
	
	public void draw() {
		GL11.glPushMatrix();
		GL11.glTranslatef(-scrollX, -scrollY, 0.0f);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		area.getTileGrid().draw(this);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glPopMatrix();
	}
	
	/**
	 * Sets the sprite that is drawn under the mouse cursor to preview
	 * the action that clicking will cause
	 * @param tile
	 */
	
	public void setActionPreviewTile(Tile tile) {
		this.actionPreviewTile = tile;
		tile.cacheSprite();
	}
	
	/**
	 * Sets the object which is notified of right and left click events
	 * @param handler
	 */
	
	public void setClickHandler(AreaPalette.AreaClickHandler handler) {
		this.clickHandler = handler;
	}
	
	/**
	 * Handles input from the LWJGL input polling
	 */
	
	public void handleInput() {
		// handle key event
		while (Keyboard.next()) {
			
		}
		
		// handle mouse
		int mouseX = Mouse.getX() + scrollX;
		int mouseY = (canvas.getHeight() - Mouse.getY()) + scrollY;
		mouseGrid = AreaUtil.convertScreenToGrid(mouseX, mouseY);
		mouseScreen = AreaUtil.convertGridToScreen(mouseGrid);
		
		if (Mouse.isButtonDown(2)) {
			int mouseDX = Mouse.getDX();
			int mouseDY = Mouse.getDY();
			
			if (Mouse.isGrabbed()) {
				scrollX -= (mouseDX);
				scrollY += (mouseDY);
			}

			Mouse.setGrabbed(true);
		} else if (Mouse.isGrabbed()) {
			Mouse.setGrabbed(false);
		}
		
		long curTime = System.currentTimeMillis();
		if (curTime - lastClickTime > MouseTimeout) {
			if (Mouse.isButtonDown(0)) {
				clickHandler.leftClicked(getHoverPoints());
				lastClickTime = curTime;
			} else if (Mouse.isButtonDown(1)) {
				lastClickTime = curTime;
				clickHandler.rightClicked(getHoverPoints());
			}
		}
		
		int scrollAmount = Mouse.getDWheel();
		if (scrollAmount > 0 && mouseRadius.getNextValue() != null) {
			mouseRadius.setValue(mouseRadius.getNextValue());
		} else if (scrollAmount < 0 && mouseRadius.getPreviousValue() != null) {
			mouseRadius.setValue(mouseRadius.getPreviousValue());
		}
		
		// reset the click timeout and check for updates to the mouse state
		for (int i = 0; i < Mouse.getButtonCount(); i++) {
			if (Mouse.isButtonDown(i) != lastMouseState[i]) {
				lastClickTime = 0l;
				lastMouseState[i] = !lastMouseState[i];
			}
		}
		
		if (!mouseGrid.equals(prevMouseGrid)) {
			lastClickTime = 0l;
			prevMouseGrid = mouseGrid;
		}
	}
	
	/**
	 * Gets the number model containing the value of the mouse radius
	 * @return the mouse radius model
	 */
	
	public SpinnerNumberModel getMouseRadiusModel() {
		return mouseRadius;
	}
	
	/**
	 * @return a list of all points (in grid coordinates) that the mouse is hovering over
	 */
	
	private List<PointImmutable> getHoverPoints() {
		List<PointImmutable> points = new ArrayList<PointImmutable>();
		
		if (mouseGrid != null) {
			points.add(new PointImmutable(mouseGrid));
			
			int radius = (Integer)mouseRadius.getValue();
			
			for (int r = 1; r <= radius; r++) {
				for (int i = 0; i < 6 * r; i++) {
					points.add(new PointImmutable(AreaUtil.convertPolarToGrid(mouseGrid, r, i)));
				}
			}
		}
		
		return points;
	}
	
	@Override public Area getArea() {
		return area;
	}

	@Override public void drawTransitions() { }

	@Override public void drawInterface(AnimationState as) { 
		if (mouseScreen != null) {
			SpriteManager.getSprite("editor/hexBorder").draw(mouseScreen.x, mouseScreen.y);
			int radius = (Integer)mouseRadius.getValue();
			for (int r = 1; r <= radius; r++) {
				for (int i = 0; i < 6 * r; i++) {
					Point pGrid = AreaUtil.convertPolarToGrid(mouseGrid.x, mouseGrid.y, r, i);
					Point pScreen = AreaUtil.convertGridToScreen(pGrid);
					
					SpriteManager.getSprite("editor/hexBorder").draw(pScreen.x, pScreen.y);
				}
			}
			
			if (actionPreviewTile != null) {
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
				
				actionPreviewTile.draw(mouseScreen.x, mouseScreen.y);
				
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			}
		}
	}
}
