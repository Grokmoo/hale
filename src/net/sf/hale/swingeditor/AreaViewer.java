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

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.AnimationState;
import net.sf.hale.Area;
import net.sf.hale.tileset.AreaTileGrid;
import net.sf.hale.util.Point;

/**
 * A class for viewing an area in the swing editor
 * @author Jared
 *
 */

public class AreaViewer implements AreaTileGrid.AreaRenderer {
	private Canvas canvas;
	private Area area;
	
	private int scrollX, scrollY;
	
	/**
	 * Creates a new Viewer for the specified Area
	 * @param area
	 * @param canvas the canvas that is being drawn on
	 */
	
	public AreaViewer(Area area, Canvas canvas) {
		this.canvas = canvas;
		this.area = area;
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
	 * Handles input from the LWJGL input polling
	 */
	
	public void handleInput() {
		// handle key event
		while (Keyboard.next()) {
			
		}
	}
	
	@Override public Area getArea() {
		return area;
	}

	@Override public void drawTransitions() {
		// TODO Auto-generated method stub
		
	}

	@Override public void drawInterface(Point selected, AnimationState as) {
		// TODO Auto-generated method stub
		
	}

}
