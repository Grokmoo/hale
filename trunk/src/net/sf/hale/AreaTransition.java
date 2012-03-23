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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL14;

import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.editor.reference.TransitionReferenceList;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONObject;

public class AreaTransition implements Referenceable, Saveable {
	private Sprite iconSprite;
	private String icon;
	private final String name;
	
	private String areaFrom;
	private Point areaFromPosition;
	
	private String areaTo;
	private Point areaToPosition;
	
	private final List<Point> toPositions;
	private final List<Point> fromPositions;
	
	private boolean twoWay;
	private boolean initiallyActivated;
	private boolean activated;
	
	private String worldMapLocation;
	
	@Override public Object save() {
		// if there is nothing to save, return null to indicate this transition
		// does not need to be saved
		if (initiallyActivated == activated)
			return null;
		
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("name", name);
		data.put("activated", activated);
		
		return data;
	}
	
	public void load(SimpleJSONObject data) {
		this.activated = data.get("activated", false);
	}
	
	public AreaTransition(String ref) {
		this.name = ref;
		toPositions = new ArrayList<Point>();
		fromPositions = new ArrayList<Point>();
		
		readAreaTransitionFile(ref);
	}
	
	private void readAreaTransitionFile(String ref) {
		FileKeyMap fileMap = new FileKeyMap("transitions/" + ref + ResourceType.Text.getExtension());
		
		icon = fileMap.getValue("icon", null);
		if (icon != null) iconSprite = SpriteManager.getSprite(icon);
		
		twoWay = fileMap.getValue("twoway", false);
		activated = fileMap.getValue("activated", true);
		initiallyActivated = activated;
		
		worldMapLocation = fileMap.getValue("location", null);
		
		for (LineKeyList line : fileMap.get("addtoposition")) {
			toPositions.add(new Point(line.nextInt(), line.nextInt()));
		}
		
		for (LineKeyList line : fileMap.get("addfromposition")) {
			fromPositions.add(new Point(line.nextInt(), line.nextInt()));
		}
		
		LineKeyList line = fileMap.getLast("from");
		areaFrom = line.next();
		areaFromPosition = new Point(line.nextInt(), line.nextInt());
		
		line = fileMap.getLast("to");
		areaTo = line.next();
		areaToPosition = new Point(line.nextInt(), line.nextInt());
		
		fileMap.checkUnusedKeys();
	}
	
	public void saveToFile() {
		File file = new File("campaigns/" + Game.curCampaign.getID() + "/transitions/" + getName() + ".txt");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			out.write("icon \"" + getIcon() + "\"");
			out.newLine();

			out.write("twoWay " + twoWay());
			out.newLine();
			
			out.write("activated " + initiallyActivated);
			out.newLine();
			
			if (worldMapLocation != null) {
				out.write("location \"");
				out.write(worldMapLocation);
				out.write("\"");
				out.newLine();
			}
			
			out.write("from \"" + getAreaFrom() + "\" " + getAreaFromX() + " " + getAreaFromY());
			out.newLine();
			
			out.write("to \"" + getAreaTo() + "\" " + getAreaToX() + " " + getAreaToY());
			out.newLine();
			
			for (Point p : getToPositions()) {
				out.write("addToPosition " + p.x + " " + p.y);
				out.newLine();
			}
			
			for (Point p : getFromPositions()) {
				out.write("addFromPosition " + p.x + " " + p.y);
				out.newLine();
			}
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving transition " + name, e);
		}
	}
	
	public void setFromPosition(int x, int y) {
		this.areaFromPosition.x = x;
		this.areaFromPosition.y = y;
	}
	
	public void setToPosition(int x, int y) {
		this.areaToPosition.x = x;
		this.areaToPosition.y = y;
	}
	
	public int getMaxCharacters() {
		if (twoWay) {
			return Math.min(toPositions.size(), fromPositions.size());
		} else {
			return toPositions.size();
		}
	}
	
	public String getWorldMapLocation() {
		return worldMapLocation;
	}
	
	public void setWorldMapLocation(String location) {
		this.worldMapLocation = location;
	}
	
	public List<Point> getToPositions() { return toPositions; }
	public List<Point> getFromPositions() { return fromPositions; }
	
	public Point getToPosition(int index) { return new Point(toPositions.get(index)); }
	public Point getFromPosition(int index) { return new Point(fromPositions.get(index)); }
	
	public void activate() { this.activated = true; }
	
	public void setInitiallyActivated(boolean activated) {
		this.initiallyActivated = activated;
	}
	
	public boolean initiallyActivated() { return initiallyActivated; }
	public boolean isActivated() { return activated; }
	public boolean twoWay() { return twoWay; }
	public String getName() { return name; }
	public String getIcon() { return icon; }
	public String getAreaFrom() { return areaFrom; }
	public int getAreaFromX() { return areaFromPosition.x; }
	public int getAreaFromY() { return areaFromPosition.y; }
	public String getAreaTo() { return areaTo; }
	public int getAreaToX() { return areaToPosition.x; }
	public int getAreaToY() { return areaToPosition.y; }
	
	@Override public String getReferenceType() {
		return "Area Transition";
	}
	
	@Override public String toString() {
		return name;
	}

	@Override public ReferenceList getReferenceList() {
		return new TransitionReferenceList(this);
	}

	@Override public String getID() {
		return name;
	}
	
	/**
	 * Draw with a simple, hardcoded flashing animation
	 * @param p
	 * @param currentTimeMillis
	 */
	
	public final void draw(Point p, long currentTimeMillis) {
		float color = (currentTimeMillis % 1000) / 1000.0f;
		if (color > 0.5f) color = 1.0f - color;
		
		GL14.glSecondaryColor3f(color, color, color);
		
		iconSprite.drawWithOffset(p);
		
		GL14.glSecondaryColor3f(0.0f, 0.0f, 0.0f);
	}
}
