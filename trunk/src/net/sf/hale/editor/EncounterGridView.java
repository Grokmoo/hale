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

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class EncounterGridView extends Widget {
	private Encounter encounter;
	private final EncounterEditor editor;
	
	private final List<Creature> creatures;
	private int radius;
	private Point startScreen = new Point(0, 0);
	
	private final Point selectedTile = new Point(false);
	private Point selectedTileScreen = new Point(false);
	
	public EncounterGridView(EncounterEditor editor) {
		this.editor = editor;
		this.setTheme("");
		this.radius = 0;
		
		this.creatures = new ArrayList<Creature>();
	}
	
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
		
		deselectTile();
		
		this.radius = encounter.getSize();
		this.startScreen = AreaUtil.convertGridToScreen(new Point(radius, radius));
		
		this.removeAllChildren();
		Label label = new Label("");
		label.setTheme("");
		label.setSize(10, 10);
		label.setPosition((2 * radius + 1) * Game.TILE_WIDTH + Game.TILE_SIZE / 4, (2 * radius + 1) * Game.TILE_SIZE);
		this.add(label);
		
		this.creatures.clear();
		for (Creature creature : encounter.getBaseCreatures()) {
			this.creatures.add(creature);
		}
	}
	
	@Override public int getPreferredInnerHeight() {
		return (2 * radius + 2) * Game.TILE_SIZE;
	}
	
	@Override public int getPreferredInnerWidth() {
		return (2 * radius + 3) * Game.TILE_WIDTH;
	}
	
	public Point getSelectedTile() { return selectedTile; }
	
	public void setSelectedTile(int x, int y) {
		selectedTile.x = x;
		selectedTile.y = y;
		selectedTile.valid = true;
		selectedTileScreen = AreaUtil.convertGridToScreen(x, y);
	}
	
	public void deselectTile() {
		selectedTile.valid = false;
		selectedTileScreen.valid = false;
	}
	
	@Override public void paintWidget(GUI gui) {
		super.paintWidget(gui);
		
		GL11.glPushMatrix();
		
		GL11.glTranslatef(getInnerX() + 5 + startScreen.x, getInnerY() + 5 + startScreen.y, 0);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glColor3f(0.0f, 0.0f, 0.0f);
		drawGridHex(0, 0);
		
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point grid = AreaUtil.convertPolarToGridCenter0(r, i);
				Point screen = AreaUtil.convertGridToScreen(grid);
				drawGridHex(screen.x, screen.y);
			}
		}
		
		if (selectedTileScreen.valid) {
			GL11.glColor3f(1.0f, 0.0f, 1.0f);
			drawGridHex(selectedTileScreen.x, selectedTileScreen.y);
		}
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		for (Creature creature : this.creatures) {
			creature.draw(AreaUtil.convertGridToScreen(creature.getX(), creature.getY()));
		}
		
		GL11.glPopMatrix();
	}
	
	@Override public boolean handleEvent(Event evt) {
		if (evt.getType() == Event.Type.MOUSE_ENTERED) return true;
		
		int x = evt.getMouseX() - getInnerX() - 5;
		int y = evt.getMouseY() - getInnerY() - 5;
		
		Point grid = AreaUtil.convertScreenToGrid(x, y);
		grid.x -= radius;
		grid.y -= radius;
		
		if (radius % 2 == 1) {
			if (grid.x % 2 != 0) grid.y -= 1;
		}
		
		if (evt.getType() == Event.Type.MOUSE_BTNDOWN && evt.getMouseButton() == Event.MOUSE_LBUTTON) {
			if (AreaUtil.distance(grid, new Point(0, 0)) <= radius) {

				editor.updateSelectedCreature(grid);
				setSelectedTile(grid.x, grid.y);

				Creature c = editor.getSelectedEncounterCreature();
				if (c == null) {
					c = editor.getSelectedAllCreature();

					if (c != null) {
						encounter.addCreature(new Creature(c), grid.x, grid.y);
						editor.updateSelectedEncounter();
						editor.updateSelectedCreature(grid);
						setSelectedTile(grid.x, grid.y);
					}
				}
			}
			
			return true;
			
		} else if (evt.getType() == Event.Type.MOUSE_BTNDOWN && evt.getMouseButton() == Event.MOUSE_RBUTTON) {
			editor.updateSelectedCreature(grid);
			setSelectedTile(grid.x, grid.y);
			
			Creature c = editor.getSelectedEncounterCreature();
			if (c != null) {
				encounter.removeCreature(c);
				editor.updateSelectedEncounter();
				editor.updateSelectedCreature(grid);
				setSelectedTile(grid.x, grid.y);
			}
			
			return true;
		}
		
		return false;
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
