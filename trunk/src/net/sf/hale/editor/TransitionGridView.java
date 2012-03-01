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

import net.sf.hale.AreaTransition;
import net.sf.hale.Game;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class TransitionGridView extends Widget {
	private TransitionEditor transitionEditor;
	private boolean editToPositions = true;
	private AreaTransition transition;
	private Point selectedHex = new Point(false);
	private int selectedIndex = -1;
	
	public TransitionGridView(TransitionEditor editor) {
		this.transitionEditor = editor;
		this.setTheme("");
		
		Label label = new Label("");
		label.setTheme("");
		label.setSize(10, 10);
		label.setPosition(7 * Game.TILE_WIDTH + Game.TILE_SIZE / 4, 7 * Game.TILE_SIZE);
		this.add(label);
	}
	
	public void setMode(AreaTransition transition, boolean editToPositions) {
		this.transition = transition;
		this.editToPositions = editToPositions;
	}
	
	public void setSelectedPoint(int index) {
		this.selectedIndex = index;
		
		if (index == -1) {
			selectedHex.valid = false;
		} else {
			if (transition != null) {
				selectedHex.valid = true;
				if (editToPositions) {
					//selectedHex.x = transition.getToPosition(index).x - transition.getAreaToX();
					//selectedHex.y = transition.getToPosition(index).y - transition.getAreaToY();
					
					selectedHex.x = transition.getToPosition(index).x;
					selectedHex.y = transition.getToPosition(index).y;
					
				} else {
					//selectedHex.x = transition.getFromPosition(index).x - transition.getAreaFromX();
					//selectedHex.y = transition.getFromPosition(index).y - transition.getAreaFromY();
					
					selectedHex.x = transition.getFromPosition(index).x;
					selectedHex.y = transition.getFromPosition(index).y;
				}
			} else {
				selectedHex.valid = false;
			}
		}
	}
	
	@Override public boolean handleEvent(Event evt) {
		if (transition == null) return false;
		
		if (evt.getType() == Event.Type.MOUSE_ENTERED) return true;
		
		int x = evt.getMouseX() - getInnerX() - 5;
		int y = evt.getMouseY() - getInnerY() - 5 + Game.TILE_SIZE / 2;
		
		Point grid = AreaUtil.convertScreenToGrid(x, y);
		grid.x -= 3;
		grid.y -= 3;
		if (grid.x % 2 !=  0) grid.y -= 1;
		
		int offsetX = 0;
		int offsetY = 0;
		
		//int offsetX = editToPositions ? transition.getAreaToX() : transition.getAreaFromX();
		//int offsetY = editToPositions ? transition.getAreaToY() : transition.getAreaFromY();
		List<Point> points = editToPositions ? transition.getToPositions() : transition.getFromPositions();
		
		if (evt.getType() == Event.Type.MOUSE_BTNDOWN && evt.getMouseButton() == Event.MOUSE_LBUTTON) {
			boolean matchFound = false;
			
			for (int i = 0; i < points.size(); i++) {
				Point p = points.get(i);
				
				if (p.x - offsetX == grid.x && p.y - offsetY == grid.y) {
					matchFound = true;
					transitionEditor.setSelectedPoint(i);
					break;
				}
			}
			
			if (!matchFound && selectedIndex != -1) {
				Point p = points.get(selectedIndex);
				p.x = grid.x + offsetX;
				p.y = grid.y + offsetY;
				
				selectedHex.x = grid.x;
				selectedHex.y = grid.y;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override public void paintWidget(GUI gui) {
		super.paintWidget(gui);
		
		GL11.glPushMatrix();
		
		GL11.glTranslatef(getInnerX() + 5 + 3 * Game.TILE_WIDTH, getInnerY() + 5 + 3 * Game.TILE_SIZE, 0);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		
		GL11.glColor3f(0.0f, 0.0f, 0.0f);
		drawGridHex(0, 0);
		
		for (int r = 1; r <= 3; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point grid = AreaUtil.convertPolarToGridCenter0(r, i);
				Point screen = AreaUtil.convertGridToScreen(grid);
				drawGridHex(screen.x, screen.y);
			}
		}
		
		if (selectedHex.valid) {
			GL11.glColor3f(1.0f, 0.0f, 0.0f);
			drawGridHex(AreaUtil.convertGridToScreen(selectedHex.x, selectedHex.y));
		}
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
		if (transition != null) {
			SpriteManager.getSprite(transition.getIcon()).drawWithOffset(0, 0);
			
			GL11.glColor3f(0.0f, 1.0f, 0.0f);
			
			List<Point> points = editToPositions ? transition.getToPositions() : transition.getFromPositions();
			//int offsetX = editToPositions ? transition.getAreaToX() : transition.getAreaFromX();
			//int offsetY = editToPositions ? transition.getAreaToY() : transition.getAreaFromY();
			
			int offsetX = 0;
			int offsetY = 0;
			
			for (int i = 0; i < points.size(); i++) {
				int x = points.get(i).x - offsetX;
				int y = points.get(i).y - offsetY;
				//if (-x % 2 == 1) y -= 1;
				int index = i + 1;
				
				SpriteManager.getSprite("editor/" + index).draw(AreaUtil.convertGridToScreen(x, y));
			}
		}
		
		GL11.glPopMatrix();
	}
	
	public void drawGridHex(Point screen) {
		drawGridHex(screen.x, screen.y);
	}
	
	public void drawGridHex(int x, int y) {
		GL11.glBegin(GL11.GL_LINE_LOOP);
		
		GL11.glVertex2i(x, y + Game.TILE_SIZE / 2);
		GL11.glVertex2i(x + Game.TILE_SIZE / 4, y);
		GL11.glVertex2i(x + Game.TILE_WIDTH, y);
		GL11.glVertex2i(x + Game.TILE_SIZE, y + Game.TILE_SIZE / 2);
		GL11.glVertex2i(x + Game.TILE_WIDTH, y + Game.TILE_SIZE);
		GL11.glVertex2i(x + Game.TILE_SIZE / 4, y + Game.TILE_SIZE);
		
		GL11.glEnd();
	}
}
