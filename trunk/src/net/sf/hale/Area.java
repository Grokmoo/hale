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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.hale.ability.AreaEffectList;
import net.sf.hale.ability.Aura;
import net.sf.hale.ability.Effect;
import net.sf.hale.ability.EffectTarget;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.editor.reference.AreaReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Openable;
import net.sf.hale.entity.Trap;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.tileset.AreaElevationGrid;
import net.sf.hale.tileset.AreaTileGrid;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

public class Area implements Referenceable, EffectTarget, Saveable {
	private final List<String> transitions;
	private final Set<String> triggers;
	private final List<Point> encounterPositions;
	private Point size;
	private int visibilityRadius;
	private boolean unexplored;
	private boolean[][] passable;
	private boolean[][] transparency;
	private boolean[][] visibility;
	private AreaElevationGrid elevation;
	private AreaTileGrid tileGrid;
	private String tileset;
	private List<Point> startLocations;
	
	private AreaEntityList entityList;
	private AreaEffectList effects;
	private final List<Encounter> encounters;
	private final String name;
	private boolean[][] explored;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("ref", SaveGameUtil.getRef(this));
		data.put("name", name);
		
		// write out the explored matrix
		List<Object> exp = new ArrayList<Object>();
		for (int x = 0; x < explored.length; x++) {
			for (int y = 0; y < explored[0].length; y++) {
				if (explored[x][y]) {
					// write this as a JSON formated object, but with multiple
					// entries per line
					exp.add(Integer.toString(x) + ',' + Integer.toString(y));
				}
			}
		}
		data.put("explored", exp.toArray());
		
		Object[] encounterData = new Object[encounters.size()];
		for (int i = 0; i < encounterData.length; i++) {
			encounterData[i] = encounters.get(i).save();
		}
		
		data.put("encounters", encounterData);
		
		if (effects.size() > 0)
			data.put("effects", effects.save());
		
		data.put("entities", entityList.save());
		
		return data;
	}
	
	public static Area load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		Area area = new Area(data.get("name", null), true);
		
		refHandler.add(data.get("ref", null), area);
		
		// parse explored data
		SimpleJSONArray exploredArray = data.getArray("explored");
		for (SimpleJSONArrayEntry entry : exploredArray) {
			String exploredString = entry.getString();
			
			String[] coords = exploredString.split(",");
			
			if (coords.length != 2) {
				Logger.appendToErrorLog("Error parsing explored entry " + exploredString);
			} else {
				int x = Integer.parseInt(coords[0]);
				int y = Integer.parseInt(coords[1]);
				
				area.explored[x][y] = true;
			}
		}
		
		// parse entities
		area.entityList.load(data.getObject("entities"), refHandler);
		
		if (data.containsKey("effects"))
			area.effects.load(data.getArray("effects"), refHandler);
		
		for (SimpleJSONArrayEntry entry : data.getArray("encounters")) {
			SimpleJSONObject entryObject = entry.getObject();
			
			Encounter encounter = Encounter.load(entryObject, refHandler, area);
			
			area.encounters.add(encounter);
			area.encounterPositions.add(encounter.getAreaPosition());
		}
		
		return area;
	}
	
	private Area(String ref, boolean loaded) {
		size = new Point(0, 0);
		transitions = new ArrayList<String>();
		triggers = new HashSet<String>();
		
		encounters = new ArrayList<Encounter>();
		encounterPositions = new ArrayList<Point>();
		
		startLocations = new ArrayList<Point>();
		
		name = ref;
		
		visibilityRadius = 12;
		
		readAreaFile(name, loaded);
		
		for (Entity e : entityList) {
			if (e.getType() == Entity.Type.DOOR) {
				Door d = (Door)e;
				
				if (!d.isOpen() && !d.isTransparent())
					transparency[e.getX()][e.getY()] = false;
			}
		}
		
		for (AreaTransition transition : Game.curCampaign.getAreaTransitions()) {
			if (transition.getAreaFrom().equals(name)) {
				this.transitions.add(transition.getName());
			} else if (transition.twoWay() && transition.getAreaTo().equals(name)) {
				this.transitions.add(transition.getName());
			}
		}
		
		for (AreaTrigger trigger : Game.curCampaign.getTriggers()) {
			if (trigger.getArea() != null && trigger.getArea().equals(name)) {
				this.triggers.add(trigger.getID());
			}
		}
		
		for (int i = 0; i < size.x; i++) {
			passable[i][0] = false;
			passable[i][size.y - 1] = false;
		}
		
		for (int i = 0; i < size.y; i++) {
			passable[0][i] = false;
			passable[size.x - 1][i] = false;
		}
	}
	
	public Area(String ref) {
		this(ref, false);
	}
	
	public void readAreaFile(String ref, boolean saved) {
		List<Encounter> encounters = new LinkedList<Encounter>();
		List<Point> encounterPoints = new LinkedList<Point>();
		
		FileKeyMap keyMap = new FileKeyMap("areas/" + ref + ResourceType.Text.getExtension());
		
		LineKeyList line2 = keyMap.getLast("size");
		if (line2 != null) {
			size.x = line2.nextInt();
			size.y = line2.nextInt();

			unexplored = false;
			explored = new boolean[size.x][size.y];
			transparency = new boolean[size.x][size.y];
			
			entityList = new AreaEntityList(size.x, size.y);
			effects = new AreaEffectList(this);
			
			passable = new boolean[size.x][size.y];
			visibility = new boolean[size.x][size.y];
			for (int i = 0; i < size.x; i++) {
				for (int j = 0; j < size.y; j++) {
					passable[i][j] = true;
					
					visibility[i][j] = true;
					explored[i][j] = true;
					transparency[i][j] = true;
				}
			}
		}
		
		this.tileset = keyMap.getValue("tileset", null);
		this.visibilityRadius = keyMap.getValue("visibilityradius", 0);
		
		if (keyMap.has("unexplored")) {
			keyMap.get("unexplored"); // to set this key as having been read
			unexplored = true;
			AreaUtil.setMatrix(explored, false);
		}

		if (!saved) {
			for (LineKeyList line : keyMap.get("encounter")) {
				encounters.add(Game.curCampaign.getEncounter(line.next()));
				encounterPoints.add(new Point(line.nextInt(), line.nextInt()));
			}

			for (LineKeyList line : keyMap.get("creature")) {
				Creature creature = Game.entityManager.getCreature(line.next());
				creature.setPosition(line.nextInt(), line.nextInt());
				creature.setFaction(Game.ruleset.getFaction(line.next()));
				entityList.addEntity(creature);
			}

			for (LineKeyList line : keyMap.get("item")) {
				int quantity = line.nextInt();
				Item item = Game.entityManager.getItem(line.next());
				item.setPosition(line.nextInt(), line.nextInt());
				if (item.getType() == Entity.Type.TRAP) {
					// arm traps added directly to the map
					((Trap)item).setArmed(true);
					((Trap)item).setFaction(Game.ruleset.getFaction("Hostile"));
				}
				this.addItem(item, quantity);
			}
		}

		for (LineKeyList line : keyMap.get("opaque")) {
			while (line.hasNext()) {
				transparency[line.nextInt()][line.nextInt()] = false;
			}
		}
		
		for (LineKeyList line : keyMap.get("addstartlocation")) {
			startLocations.add(new Point(line.nextInt(), line.nextInt()));
		}
		
		for (LineKeyList line : keyMap.get("impass")) {
			while (line.hasNext()) {
				passable[line.nextInt()][line.nextInt()] = false;
			}
		}
		
		tileGrid = new AreaTileGrid(Game.curCampaign.getTileset(this.tileset), size.x, size.y);
		
		for (LineKeyList line : keyMap.get("tile")) {
			String layer = line.next();
			int x = line.nextInt();
			int y = line.nextInt();
			String tileID = line.next().replace('\\', '/');
			tileGrid.addTile(tileID, layer, x, y);
		}
		
		elevation = new AreaElevationGrid(size.x, size.y);
		
		for (LineKeyList line : keyMap.get("elevation")) {
			int y = line.nextInt();
			
			int x = 0;
			while (line.hasNext()) {
				byte elev = line.nextByte();
				elevation.setElevation(x, y, elev);
				x++;
			}
		}
		
		if (!saved) {
			for (int i = 0; i < encounters.size(); i++) {
				encounters.get(i).addToArea(this, encounterPoints.get(i).x, encounterPoints.get(i).y);
			}
			
			keyMap.checkUnusedKeys();
		}
	}
	
	public void runOnAreaLoad(AreaTransition transition) {
		for (String triggerName : triggers) {
			Game.curCampaign.getTrigger(triggerName).checkOnAreaLoad(transition);
		}
	}
	
	public void runOnAreaExit(AreaTransition transition) {
		for (String triggerName : triggers) {
			Game.curCampaign.getTrigger(triggerName).checkOnAreaExit(transition);
		}
	}
	
	public void checkEncounterRespawns() {
		for (Encounter encounter : encounters) {
			encounter.checkForRespawn();
		}
	}
	
	public String getTileset() { return tileset; }
	public String getName() { return name; }
	
	public int getWidth() { return size.x; }
	public int getHeight() { return size.y; }
	
	public List<Point> getStartLocations() { return startLocations; }
	public List<String> getTransitions() { return transitions; }
	public Set<String> getTriggers() { return triggers; }
	public AreaEntityList getEntities() { return entityList; }
	public AreaTileGrid getTileGrid() { return tileGrid; }
	public AreaElevationGrid getElevationGrid() { return elevation; }
	
	public List<Encounter> getEncounters() { return encounters; }
	public List<Point> getEncounterPositions() { return encounterPositions; }
	
	public void setUnexplored(boolean unexplored) { this.unexplored = unexplored; }
	
	public int getVisibilityRadius() { return visibilityRadius; }
	public void setVisibilityRadius(int visibilityRadius) { this.visibilityRadius = visibilityRadius; }
	
	public boolean unexplored() { return unexplored; }
	public boolean[][] getExplored() { return explored; }
	public boolean[][] getTransparency() { return transparency; }
	public boolean[][] getPassability() { return passable; }
	public boolean[][] getVisibility() { return visibility; }
	
	public void applyEffect(Effect effect, List<Point> points) {
		if (! (effect instanceof Aura))
			effect.setTarget(this);
			
		effects.add(effect, points);
	}
	
	@Override public int getSpellResistance() { return 0; }
	
	@Override public void removeEffect(Effect effect) {
		effects.remove(effect);
	}
	
	public List<Creature> getAffectedCreatures(Effect effect) {
		return effects.getAffectedCreatures(effect, this.entityList);
	}
	
	public List<Effect> getEffectsAt(int x, int y) {
		return effects.getEffectsAt(x, y);
	}
	
	public void moveAura(Aura aura, List<Point> points) {
		effects.move(aura, points);
	}
	
	public int getMovementBonus(Point p) { return getMovementBonus(p.x, p.y); }
	
	public int getMovementBonus(int x, int y) { return effects.getBonusAt(Bonus.Type.Movement, x, y); }
	
	public boolean isSilenced(int x, int y) { return effects.hasBonusAt(Bonus.Type.Silence, x, y); }
	
	public boolean isSilenced(Point p) { return isSilenced(p.x, p.y); }
	
	private int getConcealment(AbilityActivator attacker, AbilityActivator defender, int x, int y) {
		int concealment = 0;
		int obstructionsInPathConcealment = 0;
		
		Point from = AreaUtil.convertGridToScreenAndCenter(attacker.getX(), attacker.getY());
		
		Point to = AreaUtil.convertGridToScreenAndCenter(x, y);
		
		if (from.x == to.x && from.y == to.y) return 0;
		
		// note that this list will include the defender's position but will not include the attacker's position.
		// So, concealment on the attacker's tile doesn't affect this calculation
		List<Point> minPath = AreaUtil.findIntersectingHexes(from.x, from.y, to.x, to.y);
		
		// we compute the average concealment of all the tiles in the path.  However, the straight line
		// path might cross more tiles than are neccessary, adding too much concealment.
		// To smooth over this sort of difference, we take the average and multiply it by the distance
		// between the points rather than the path length.
		
		int areaPathConcealment = 0;
		for (Point p : minPath) {
			areaPathConcealment += effects.getBonusAt(Bonus.Type.Concealment, p.x, p.y);
			areaPathConcealment -= effects.getBonusAt(Bonus.Type.ConcealmentNegation, p.x, p.y);
			
			if (!this.transparency[p.x][p.y]) obstructionsInPathConcealment += 15;
			else {
				Creature c = this.getCreatureAtGridPoint(p);
				if (c != null && c != defender) obstructionsInPathConcealment += 15;
			}
		}
		
		obstructionsInPathConcealment = Math.min(obstructionsInPathConcealment, 30);

		float areaPathConcealmentAverage = ((float)areaPathConcealment) / ((float)minPath.size());
		
		concealment += (areaPathConcealmentAverage * AreaUtil.distance(attacker.getX(), attacker.getY(), x, y));
		
		// now compute the amount of concealment based on defender and attacker stats
		
		int defenderConcealment = 0;
		
		if (defender != null)
			defenderConcealment += defender.stats().get(Bonus.Type.Concealment) -
				defender.stats().get(Bonus.Type.ConcealmentNegation);
		
		if (attacker.stats().has(Bonus.Type.Blind)) defenderConcealment += 100;
		defenderConcealment = Math.min(100, defenderConcealment);
		
		int attackerIgnoring = attacker.stats().get(Bonus.Type.ConcealmentIgnoring);
		
		int defenderBonus = Math.min(100, Math.max(0, defenderConcealment - attackerIgnoring));
		
		concealment = Math.min(100, concealment);
		
		return concealment + defenderBonus + obstructionsInPathConcealment;
	}
	
	public int getConcealment(AbilityActivator attacker, Point position) {
		Creature target = this.entityList.getCreature(position.x, position.y);
		
		return getConcealment(attacker, target, position.x, position.y);
	}
	
	public int getConcealment(AbilityActivator attacker, AbilityActivator defender) {
		return getConcealment(attacker, defender, defender.getX(), defender.getY());
	}
	
	public void resize(int newWidth, int newHeight) {
		if (newWidth <= 0 || newHeight <= 0) return;
		if (newWidth == size.x && newHeight == size.y) return;
		
		this.size.x = newWidth;
		this.size.y = newHeight;
		
		boolean[][] newExp = this.getMatrixOfSize(); AreaUtil.setMatrix(newExp, !unexplored);
		boolean[][] newPass = this.getMatrixOfSize(); AreaUtil.setMatrix(newPass, true);
		boolean[][] newTrans = this.getMatrixOfSize(); AreaUtil.setMatrix(newTrans, true);
		boolean[][] newVis = this.getMatrixOfSize(); AreaUtil.setMatrix(newVis, true);
		
		int width = Math.min(this.explored.length, size.x);
		int height = Math.min(this.explored[0].length, size.y);
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				newExp[i][j] = this.explored[i][j];
				newPass[i][j] = this.passable[i][j];
				newTrans[i][j] = this.transparency[i][j];
				newVis[i][j] = this.visibility[i][j];
			}
		}
		
		this.explored = newExp;
		this.passable = newPass;
		this.transparency = newTrans;
		this.visibility = newVis;
		
		entityList.resize(newWidth, newHeight);
		
		effects.resize(newWidth, newHeight);
		
		tileGrid.resize(newWidth, newHeight);
		
		elevation.resize(newWidth, newHeight);
	}
	
	public boolean[][] getMatrixOfSize() {
		boolean[][] matrix = new boolean[size.x][size.y];
		
		return matrix;
	}
	
	public void removeEntity(Entity entity) {
		entityList.removeEntity(entity);
	}
	
	public void addItem(Item item) {
		addItem(item, 1);
	}

	public void addItem(Item item, int quantity) {
		if (item == null || item.positionValid() == false) return;

		switch (item.getType()) {
		case DOOR:
			Door door = getDoorAtGridPoint(item.getX(), item.getY());
			if (door == null) entityList.addEntity(item);
			break;
		case TRAP:
			Trap trap = getTrapAtGridPoint(item.getX(), item.getY());
			if (trap == null && ((Trap)item).isArmed()) entityList.addEntity(item);
			if (((Trap)item).isArmed()) break;
		default:
			Container container = getContainerAtGridPoint(item.getX(), item.getY());
			if (container == null) {
				if (item.getType() == Entity.Type.CONTAINER) {
					entityList.addEntity(item);
				} else {
					container = Container.createTemporaryContainer(item.getID() + "_container");
					container.setPosition(item.getX(), item.getY());
					container.addItem(item, quantity);
					entityList.addEntity(container);
				}
			} else {
				container.addItem(item, quantity);
			}
		}
	}
	
	public AreaTransition getTransitionAtGridPoint(Point p) {
		return getTransitionAtGridPoint(p.x, p.y);
	}
	
	public AreaTransition getTransitionAtGridPoint(int x, int y) {
		for (String s : transitions) {
			AreaTransition t = Game.curCampaign.getAreaTransition(s);
			
			if ( t.getAreaFrom().equals(this.getName()) ) {
				if (t.getAreaFromX() == x && t.getAreaFromY() == y) return t;
			} else if (t.getAreaTo().equals(this.getName()) ) {
				if (t.getAreaToX() == x && t.getAreaToY() == y) return t;
			}
		}
		
		return null;
	}
	
	public Item getItemAtGridPoint(Point p) {
		return getItemAtGridPoint(p.x, p.y);
	}
	
	public Item getItemAtGridPoint(int x, int y) {
		return entityList.getItem(x, y);
	}
	
	public Openable getOpenableAtGridPoint(Point p) {
		return getOpenableAtGridPoint(p.x, p.y);
	}
	
	public Openable getOpenableAtGridPoint(int x, int y) {
		Openable openable = getDoorAtGridPoint(x, y);
		if (openable == null) return getContainerAtGridPoint(x, y);
		else return openable;
	}
	
	public Door getDoorAtGridPoint(int x, int y) {
		return entityList.getDoor(x, y);
	}
	
	public Door getDoorAtGridPoint(Point p) {
		return entityList.getDoor(p.x, p.y);
	}
	
	public Container getContainerAtGridPoint(int x, int y) {
		return entityList.getContainer(x, y);
	}
	
	public Container getContainerAtGridPoint(Point p) {
		return entityList.getContainer(p.x, p.y);
	}
	
	public Trap getTrapAtGridPoint(int x, int y) {
		return entityList.getTrap(x, y);
	}
	
	public Trap getTrapAtGridPoint(Point p) {
		return entityList.getTrap(p.x, p.y);
	}
	
	public List<Entity> getEntitiesWithID(String id) {
		return entityList.getEntitiesWithID(id);
	}
	
	public Entity getEntityWithID(String id) {
		return entityList.getEntityWithID(id);
	}
	
	public Creature getCreatureAtGridPoint(Point p) {
		return entityList.getCreature(p.x, p.y);
	}
	
	public Creature getCreatureAtGridPoint(int x, int y) {
		return entityList.getCreature(x, y);
	}
	
	public boolean[][] getEntityPassabilities(Creature mover) {
		return entityList.getEntityPassabilities(mover);
	}
	
	public List<Entity> getEntitiesAtGridPoint(Point p) {
		return getEntitiesAtGridPoint(p.x, p.y);
	}
	
	public List<Entity> getEntitiesAtGridPoint(int x, int y) {
		return entityList.getEntities(x, y);
	}
	
	public final boolean isVisible(Point p) {
		return isVisible(p.x, p.y);
	}
	
	public final boolean isVisible(int x, int y) {
		if (x < 0 || x >= size.x || y < 0 || y >= size.y) return false;
		
		return this.visibility[x][y];
	}
	
	public final boolean isTransparent(Point p) {
		return isTransparent(p.x, p.y);
	}
	
	public final boolean isTransparent(int x, int y) {
		if (x < 0 || x >= size.x || y < 0 || y >= size.y) return false;
		
		return this.transparency[x][y];
	}
	
	public boolean[][] getCurrentPassable() {
		boolean[][] pass = new boolean[size.x][size.y];
		
		for (int x = 0; x < size.x; x++) {
			for (int y = 0; y < size.y; y++) {
				if (entityList.getCreature(x, y) != null) pass[x][y] = false;
				else {
					Door d = entityList.getDoor(x, y);
					if (d != null && !d.isOpen()) pass[x][y] = false;
					else pass[x][y] = this.passable[x][y];
				}
			}
		}
		
		return pass;
	}
	
	public final boolean isPassable(int x, int y) {
		if (x < 0 || x >= size.x || y < 0 || y >= size.y) return false;
		
		return passable[x][y];
	}
	
	public void setEntityVisibility() {
		for (Entity e : entityList) {
			if (e.getType() == Entity.Type.CREATURE) {
				((Creature)e).setVisibility(true);
			}
		}
	}
	
	public void addPlayerCharacters() {
		Creature lastCreature = null;
		
		Iterator<Creature> iter = Game.curCampaign.party.iterator();
		Iterator<Point> posIter = startLocations.iterator();
		while (iter.hasNext()) {
			Creature c = iter.next();
			
			if (posIter.hasNext()) {
				c.setPosition(posIter.next());
			} else {
				// if there are no more explicit transition locations, just add the creature
				// wherever there is a nearby space
				Point p = AIScriptInterface.findClosestEmptyTile(lastCreature.getPosition(), 3);
				
				if (p != null)
					c.setPosition(p);
				else
					Logger.appendToErrorLog("Warning: Unable to find enough starting positions for area " + name);
			}
			
			entityList.addEntity(c);
			
			lastCreature = c;
		}
	}
	
	@Override public String getReferenceType() {
		return "Area";
	}
	
	@Override public String getID() {
		return name;
	}
	
	@Override public String toString() {
		return name;
	}
	
	@Override public ReferenceList getReferenceList() {
		return new AreaReferenceList(this);
	}

	@Override public boolean isValidEffectTarget() {
		return true;
	}
}
