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
import java.util.Iterator;
import java.util.List;

import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.editor.reference.TriggerReferenceList;
import net.sf.hale.entity.Entity;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONObject;

public class AreaTrigger implements Referenceable, Saveable {
	private String area;
	//TODO support more efficient searching of points
	private final List<Point> gridPoints;
	private Scriptable script;
	
	private final List<Entity> entitiesEntered;
	
	private final String id;
	private boolean enteredByPlayer;
	private boolean areaLoaded;
	
	@Override public Object save() {
		// if we would only save the ID, return null to indicate this trigger doesn't need
		// to be saved
		if (!areaLoaded && !enteredByPlayer)
			return null;
		
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("id", id);
		
		if (areaLoaded) data.put("areaLoaded", areaLoaded);
		
		if (enteredByPlayer) data.put("enteredByPlayer", enteredByPlayer);
		
		return data;
	}
	
	public void load(SimpleJSONObject data) {
		if (data.containsKey("areaLoaded"))
			areaLoaded = data.get("areaLoaded", false);
		else
			areaLoaded = false;
		
		if (data.containsKey("enteredByPlayer"))
			enteredByPlayer = data.get("enteredByPlayer", false);
		else
			enteredByPlayer = false;
	}
	
	public AreaTrigger(String id) {
		this.id = id;
		this.area = "null";
		this.enteredByPlayer = false;
		this.areaLoaded = false;
		
		this.gridPoints = new ArrayList<Point>();
		
		this.entitiesEntered = new ArrayList<Entity>();
		
		readAreaTriggerFile(id);
	}
	
	public void checkOnAreaLoad(AreaTransition transition) {
		if (script == null) {
			areaLoaded = true;
			return;
		}
		
		if (!areaLoaded) {
			script.executeFunction(ScriptFunctionType.onAreaLoadFirstTime, Game.curCampaign.curArea, transition);
			areaLoaded = true;
		}
		
		script.executeFunction(ScriptFunctionType.onAreaLoad, Game.curCampaign.curArea, transition);
	}
	
	public void checkOnAreaExit(AreaTransition transition) {
		if (script == null) return;
		
		script.executeFunction(ScriptFunctionType.onAreaExit, Game.curCampaign.curArea, transition);
	}
	
	public void checkPlayerMoved(Entity entity) {
		if (!enteredByPlayer)
			checkPlayerEnterFirstTime(entity);
			
		checkPlayerEnter(entity);
		checkPlayerExit(entity);
	}
	
	private void checkPlayerEnterFirstTime(Entity e) {
		if (!this.containsPoint(e.getX(), e.getY())) return;
		
		if (script != null) {
			script.executeFunction(ScriptFunctionType.onPlayerEnterFirstTime, e, this);
		}
		
		enteredByPlayer = true;
	}
	
	private void checkPlayerEnter(Entity e) {
		if (entitiesEntered.contains(e)) return;
		if (!this.containsPoint(e.getX(), e.getY())) return;

		entitiesEntered.add(e);
		
		if (script != null) {
			script.executeFunction(ScriptFunctionType.onPlayerEnter, e, this);
		}
	}
	
	private void checkPlayerExit(Entity e) {
		if (!entitiesEntered.contains(e)) return;
		if (this.containsPoint(e.getX(), e.getY())) return;
		
		entitiesEntered.remove(e);
		
		if (script != null) {
			script.executeFunction(ScriptFunctionType.onPlayerExit, e, this);
		}
	}
	
	public void addPoint(Point point) {
		if (this.containsPoint(point)) return;
		
		this.gridPoints.add(point);
	}
	
	public void removePoint(Point point) {
		Iterator<Point> iter = gridPoints.iterator();
		while (iter.hasNext()) {
			if (iter.next().equals(point)) {
				iter.remove();
				return;
			}
		}
	}
	
	public void setArea(String area) {
		this.area = area;
	}
	
	public void setScript(String scriptFile) {
		if (scriptFile != null) {
			String scriptContents = ResourceManager.getScriptResourceAsString(scriptFile);
			this.script = new Scriptable(scriptContents, scriptFile, false);
		} else {
			this.script = null;
		}
	}
	
	public List<Point> getGridPoints() { return gridPoints; }
	public String getArea() { return area; }
	
	public boolean containsPoint(Point p) { return containsPoint(p.x, p.y); }
	
	public boolean containsPoint(int x, int y) {
		for (Point point : gridPoints) {
			if (point.x == x && point.y == y) return true;
		}
		
		return false;
	}
	
	@Override public String getID() { return id; }
	
	public String getScriptFile() {
		if (script != null) return script.getScriptLocation();
		else return null;
	}
	
	public void saveToFile() {
		File fout = new File("campaigns/" + Game.curCampaign.getID() + "/triggers/" + id + ".txt");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			
			out.write("area \"" + area + "\"");
			out.newLine();
			
			if (this.script != null) {
				out.write("script \"" + script.getScriptLocation() + "\"");
				out.newLine();
			}
			
			out.write("addPoints");
			for (Point p : gridPoints) {
				out.write("   " + p.x + " " + p.y);
			}
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving area trigger " + id, e);
		}
	}
	
	private void readAreaTriggerFile(String id) {
		FileKeyMap fileMap = new FileKeyMap("triggers/" + id + ResourceType.Text.getExtension());
		
		area = fileMap.getValue("area", null);
		
		String scriptFile = fileMap.getValue("script", null);
		this.setScript(scriptFile);
		
		for (LineKeyList line : fileMap.get("addpoints")) {
			while (line.hasNext()) {
				gridPoints.add(new Point(line.nextInt(), line.nextInt()));
			}
		}
		
		fileMap.checkUnusedKeys();
	}
	
	@Override public String getReferenceType() {
		return "Trigger";
	}
	
	@Override public String toString() {
		return id;
	}

	@Override public ReferenceList getReferenceList() {
		return new TriggerReferenceList(this);
	}
}
