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

import net.sf.hale.Area;
import net.sf.hale.AreaTransition;
import net.sf.hale.AreaTrigger;
import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.editor.widgets.ItemMouseOver;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Trap;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Widget;

public class AreaEditListener {
	private final AreaEditor areaEditor;
	private final Area area;
	private final AreaEditViewer areaEditViewer;
	
	private Point previousGridPoint;
	private Widget previousMouseOver;
	
	Point mouseDragStart = new Point(false);
	
	Point mouseClick = new Point(false);
	
	public AreaEditListener(AreaEditor areaEditor, Area area, AreaEditViewer areaEditViewer) {
		this.areaEditor = areaEditor;
		this.areaEditViewer = areaEditViewer;
		this.area = area;
	}
	
	public void handleElevationMode(Event evt, Point gridPoint) {
		int radius = areaEditor.getBrushRadius();

		switch (evt.getMouseButton()) {
		case Event.MOUSE_LBUTTON:
			areaEditor.getElevationPalette().modifyElevation((byte)1, gridPoint, radius);
			break;
		case Event.MOUSE_RBUTTON:
			areaEditor.getElevationPalette().modifyElevation((byte)-1, gridPoint, radius);
			break;
		}
	}
	
	public void handleStartLocationsMode(Event evt, Point gridPoint) {
		int index = areaEditor.getSelectedStartLocationIndex();
		
		if (index != -1) {
			areaEditor.setStartLocation(index, gridPoint);
		}
	}
	
	public void handleTileMode(Event evt, Point gridPoint) {
		int radius = areaEditor.getBrushRadius();
		
		switch (evt.getMouseButton()) {
		case Event.MOUSE_LBUTTON:
			areaEditor.getTilePalette().addSelectedTile(gridPoint, radius);
			break;
		case Event.MOUSE_RBUTTON:
			areaEditor.getTilePalette().removeCurrentLayerTiles(gridPoint, radius);
			break;
		}
	}
	
	public void handleFeaturesMode(Event evt, Point gridPoint) {
		int radius = areaEditor.getBrushRadius();
		
		switch (evt.getMouseButton()) {
		case Event.MOUSE_LBUTTON:
			areaEditor.getFeaturePalette().addSelectedFeature(gridPoint, radius);
			break;
		case Event.MOUSE_RBUTTON:
			areaEditor.getFeaturePalette().removeAllFeatures(gridPoint, radius);
			break;
		}
	}
	
	public void handleTerrainMode(Event evt, Point gridPoint) {
		int radius = areaEditor.getBrushRadius();
		
		switch (evt.getMouseButton()) {
		case Event.MOUSE_LBUTTON:
			areaEditor.getTerrainPalette().addSelectedTerrain(gridPoint, radius);
			break;
		}
	}
	
	public void handleItemMode(Event evt, Point gridPoint) {
		Item item = areaEditor.getSelectedItem();
		
		if (evt.getMouseButton() == Event.MOUSE_LBUTTON && item != null) {
			switch (item.getType()) {
			case ITEM:
				item = new Item(item);
				break;
			case CONTAINER:
				item = new Container((Container)item);
				break;
			case DOOR:
				item = new Door((Door)item);
				break;
			case TRAP:
				item = new Trap((Trap)item);
				
				//force spot traps when we add them so we can see them in the editor
				//force arm them so they are added directly to the map, not in a container
				((Trap)item).setSpotted(true);
				((Trap)item).setArmed(true);
				break;
			}
			
			item.setPosition(gridPoint.x, gridPoint.y);
			
			area.addItem(item);
			
			addMouseOver();
			
		} else if (evt.getMouseButton() == Event.MOUSE_RBUTTON) {
			Item itemToRemove = area.getItemAtGridPoint(gridPoint);
			
			area.getEntities().removeEntity(itemToRemove);
			areaEditor.removeChild(previousMouseOver);
		}
	}
	
	public void handleTransitionMode(Event evt, Point gridPoint) {
		AreaTransition t = areaEditor.getSelectedTransition();
		
		if (t != null && evt.getMouseButton() == Event.MOUSE_LBUTTON) {
			if (t.getAreaFrom().equals(area.getName())) {
				t.setFromPosition(gridPoint.x, gridPoint.y);
			} else if (t.getAreaTo().equals(area.getName())) {
				t.setToPosition(gridPoint.x, gridPoint.y);
			}
			
			t.saveToFile();
			
			Game.campaignEditor.getTransitionEditor().updateSelectedTransition();
		}
	}
	
	public void handleTriggerMode(Event evt, Point gridPoint) {
		AreaTrigger trigger = areaEditor.getSelectedTrigger();
		
		if (trigger == null) return;
		
		if (evt.getMouseButton() == Event.MOUSE_LBUTTON) {
			trigger.addPoint(gridPoint);
		} else if (evt.getMouseButton() == Event.MOUSE_RBUTTON) {
			trigger.removePoint(gridPoint);
		}
		
		Game.campaignEditor.getTriggerEditor().updateSelectedTriggerPoints();
	}
	
	public void handleEncounterMode(Event evt, Point gridPoint) {
		List<Encounter> encounters = area.getEncounters();
		List<Point> positions = area.getEncounterPositions();
		
		Encounter encounter = areaEditor.getSelectedEncounter();
		
		if (evt.getMouseButton() == Event.MOUSE_LBUTTON) {
			if (encounter != null) {
				for (int i = 0; i < positions.size(); i++) {
					if (positions.get(i).equals(gridPoint)) return;
				}
				
				Encounter e = new Encounter(encounter);
				e.addToArea(area, gridPoint.x, gridPoint.y);
				
				Encounter.removeCreaturesFromArea(area);
			}
		} else if (evt.getMouseButton() == Event.MOUSE_RBUTTON) {
			for (int i = 0; i < positions.size(); i++) {
				if (positions.get(i).equals(gridPoint)) {
					encounters.remove(i);
					positions.remove(i);
				}
			}
		}
	}
	
	public void handleBoolMode(boolean[][] data, Event evt, Point gridPoint) {
		int radius = areaEditor.getBrushRadius();
		boolean value = false;
		if (evt.getMouseButton() == Event.MOUSE_RBUTTON) value = true;
		
		AreaUtil.setMatrix(data, gridPoint, value);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point grid = AreaUtil.convertPolarToGrid(gridPoint, r, i);
				AreaUtil.setMatrix(data, grid, value);
			}
		}
	}
	
	private void addMouseOver() {
		areaEditor.removeChild(previousMouseOver);
		
		Item item = area.getItemAtGridPoint(previousGridPoint);
		if (item != null) {
			previousMouseOver = new ItemMouseOver(item);
			areaEditor.add(previousMouseOver);
			previousMouseOver.adjustSize();
			
			Point position = AreaUtil.convertGridToScreen(previousGridPoint);
			position.x -= areaEditViewer.getScrollX() - areaEditViewer.getX() +
				previousMouseOver.getWidth() / 2 - Game.TILE_SIZE / 2;
			
			position.y -= areaEditViewer.getScrollY() - areaEditViewer.getY() - Game.TILE_WIDTH;
			
			previousMouseOver.setPosition(position.x, position.y);
		}
	}
	
	public boolean handleEvent(Event evt) {
		int x = evt.getMouseX() - areaEditViewer.getInnerX();
		int y = evt.getMouseY() - areaEditViewer.getInnerY();
		Point gridPoint = AreaUtil.convertScreenToGrid(x + areaEditViewer.getScrollX(), y + areaEditViewer.getScrollY());
		
		if (evt.getType() == Event.Type.MOUSE_MOVED) {
			
			if (areaEditor.getMode() == AreaEditor.Mode.Items && !gridPoint.equals(previousGridPoint)) {
				previousGridPoint = gridPoint;
				addMouseOver();
			}
			
			if (!areaEditViewer.mouseHoverTile.equals(gridPoint)) {
				areaEditViewer.mouseHoverValid = true; // whether the mouse is hovering over a valid selection
				areaEditViewer.mouseHoverTile.valid = true; // whether the mouse is hovering at all
				areaEditViewer.mouseHoverTile.x = gridPoint.x;
				areaEditViewer.mouseHoverTile.y = gridPoint.y;
				
				areaEditor.updateMousePosition(areaEditViewer.mouseHoverTile);
			}
		} else if (evt.getType() == Event.Type.MOUSE_DRAGGED) {
			if (mouseDragStart.valid) {
				int dragX = 2 * (x - mouseDragStart.x);
				int dragY = 2 * (y - mouseDragStart.y);
			
				mouseDragStart.valid = false;
				areaEditViewer.scroll(-dragX, -dragY);
			} else {
				mouseDragStart.x = x;
				mouseDragStart.y = y;
				mouseDragStart.valid = true;
			}
		} else if (evt.getType() == Event.Type.MOUSE_BTNDOWN) {
			mouseClick.x = x;
			mouseClick.y = y;
			mouseClick.valid = true;
		
		} else if (evt.getType() == Event.Type.MOUSE_BTNUP) {
			if (x == mouseClick.x && y == mouseClick.y) {
				if (areaEditor.getMode() == null) return false;
				
				switch (areaEditor.getMode()) {
				case Tiles:
					handleTileMode(evt, gridPoint);
					break;
				case Encounters:
					handleEncounterMode(evt, gridPoint);
					break;
				case Transitions:
					handleTransitionMode(evt, gridPoint);
					break;
				case Items:
					handleItemMode(evt, gridPoint);
					break;
				case Visibility:
					handleBoolMode(area.getTransparency(), evt, gridPoint);
					break;
				case Passability:
					handleBoolMode(area.getPassability(), evt, gridPoint);
					break;
				case Triggers:
					handleTriggerMode(evt, gridPoint);
					break;
				case Terrain:
					handleTerrainMode(evt, gridPoint);
					break;
				case Features:
					handleFeaturesMode(evt, gridPoint);
					break;
				case Elevation:
					handleElevationMode(evt, gridPoint);
				case StartLocations:
					handleStartLocationsMode(evt, gridPoint);
					break;
				}
			}
			mouseDragStart.valid = false;
		}
		
		if (evt.getType() == Event.Type.KEY_PRESSED) {
        	if (evt.getKeyCode() == Event.KEY_LEFT) {
        		areaEditViewer.scroll(-20, 0);
        	} else if (evt.getKeyCode() == Event.KEY_RIGHT) {
        		areaEditViewer.scroll(20, 0);
        	} else if (evt.getKeyCode() == Event.KEY_UP) {
        		areaEditViewer.scroll(0, -20);
        	} else if (evt.getKeyCode() == Event.KEY_DOWN) {
        		areaEditViewer.scroll (0, 20);
        	}
        }
		
		return true;
	}
	
	public void scroll(Point amount) {
		if (previousMouseOver == null) return;
		
		int prevX = previousMouseOver.getX();
		int prevY = previousMouseOver.getY();
		
		previousMouseOver.setPosition(prevX - amount.x, prevY - amount.y);
	}
}
