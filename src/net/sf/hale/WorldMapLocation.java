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

package net.sf.hale;

import java.util.HashMap;
import java.util.Map;

import net.sf.hale.editor.reference.LocationReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.Point;

public class WorldMapLocation implements Referenceable, Saveable {
	private final Map<String, Integer> travelTimesInHours;
	
	private boolean revealed;
	private final Point position;
	private String name;
	private String icon;
	private String transition;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("name", name);
		data.put("revealed", revealed);
		
		return data;
	}
	
	public WorldMapLocation(String name, String transition, String icon, Point position) {
		this.name = name;
		this.transition = transition;
		this.icon = icon;
		this.position = new Point(position);
		this.revealed = false;
		this.travelTimesInHours = new HashMap<String, Integer>();
	}
	
	public void setName(String name) { this.name = name; }
	public void setIcon(String icon) { this.icon = icon; }
	public void setAreaTransition(String transition) { this.transition = transition; }
	
	public boolean isRevealed() { return revealed; }
	public Point getPosition() { return position; }
	public String getName() { return name; }
	public String getIcon() { return icon; }
	public String getAreaTransition() { return transition; }
	
	public void setRevealed(boolean revealed) { this.revealed = revealed; }
	
	public int getTravelTime(WorldMapLocation location) {
		if (travelTimesInHours.containsKey(location.getName())) {
			return travelTimesInHours.get(location.getName());
		} else {
			return 0;
		}
	}
	
	public int getTravelTime(String name) {
		return travelTimesInHours.get(name);
	}
	
	public void setTravelTime(String name, int timeInHours) {
		travelTimesInHours.put(name, timeInHours);
	}

	@Override public ReferenceList getReferenceList() {
		return new LocationReferenceList(this);
	}

	@Override public String getReferenceType() {
		return "Location";
	}
	
	@Override public String getID() {
		return name;
	}
	
	@Override public String toString() {
		return name;
	}
}
