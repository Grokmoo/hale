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
import java.util.Iterator;
import java.util.List;

import net.sf.hale.editor.reference.EncounterReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.Faction;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

public class Encounter implements Referenceable, Saveable {
	private final List<Creature> baseCreatures; // the base list of creatures specified in the config file
	private final List<Creature> hostiles; // hostile creatures that anyone in the encounter is aware of
	private int size;
	
	private boolean randomize;
	private int minRandomCreatures;
	private int maxRandomCreatures;
	private int challenge;
	private boolean respawn;
	private int respawnHours;
	private boolean canAwardXP;
	private Area area = null;
	
	private final List<Creature> areaCreatures; // creatures that have actually been added to the area
	private String faction;
	private String name;
	private Point areaPosition;
	private int lastSpawnRounds;
	
	public JSONOrderedObject save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("name", name);
		data.put("x", areaPosition.x);
		data.put("y", areaPosition.y);
		data.put("lastSpawnTime", lastSpawnRounds);
		data.put("faction", faction);
		
		// store the list of creatures for this encounter
		List<Object> creaturesData = new ArrayList<Object>();
		for (Creature creature : areaCreatures) {
			if ( !area.getEntities().containsEntity(creature) ) continue;
			
			creaturesData.add(SaveGameUtil.getRef(creature));
		}
		
		if (!creaturesData.isEmpty())
			data.put("creatures", creaturesData.toArray());
		
		return data;
	}
	
	public static Encounter load(SimpleJSONObject data, ReferenceHandler refHandler, Area parent) {
		String name = data.get("name", null);
		
		Encounter encounter = new Encounter(name);
		encounter.area = parent;
		
		encounter.areaPosition = new Point(data.get("x", 0), data.get("y", 0));
		encounter.lastSpawnRounds = data.get("lastSpawnTime", 0);
		encounter.faction = data.get("faction", null);
		
		encounter.hostiles.clear();
		encounter.areaCreatures.clear();
		
		if (data.containsKey("creatures")) {
			for (SimpleJSONArrayEntry entry : data.getArray("creatures")) {
				String creatureRef = entry.getString();
				
				Creature creature = (Creature)refHandler.getEntity(creatureRef);
				creature.setEncounter(encounter);
				creature.setFaction(encounter.faction);
				
				encounter.areaCreatures.add(creature);
			}
			
			encounter.canAwardXP = true;
		}
		
		return encounter;
	}
	
	public Encounter() {
		this.name = "dummyEncounter";
		this.baseCreatures = new ArrayList<Creature>();
		this.hostiles = new ArrayList<Creature>();
		this.areaCreatures = new ArrayList<Creature>();
		this.size = 0;
		this.randomize = false;
		this.minRandomCreatures = 0;
		this.maxRandomCreatures = 0;
		this.challenge = 0;
		
		this.canAwardXP = false;
		
		this.respawn = false;
		this.respawnHours = 0;
	}
	
	public Encounter(String ref) {
		this();
		this.name = ref;
		
		readEncounterFile(ref);
	}
	
	public Encounter(Encounter other) {
		this.name = other.name;
		this.size = other.size;
		this.faction = other.faction;
		
		this.baseCreatures = new ArrayList<Creature>();
		for (Creature otherCreature : other.baseCreatures) {
			this.baseCreatures.add(new Creature(otherCreature));
		}
		
		this.hostiles = new ArrayList<Creature>();
		for (Creature otherHostile : other.hostiles) {
			this.hostiles.add(otherHostile);
		}
		
		this.areaCreatures = new ArrayList<Creature>();
		
		this.randomize = other.randomize;
		
		this.minRandomCreatures = other.minRandomCreatures;
		this.maxRandomCreatures = other.maxRandomCreatures;
		
		this.canAwardXP = false;
		this.challenge = other.challenge;
		
		this.respawn = other.respawn;
		this.respawnHours = other.respawnHours;
	}
	
	public static void removeCreaturesFromArea(Area area) {
		Iterator<Entity> iter = area.getEntities().iterator();
		
		while (iter.hasNext()) {
			Entity e = iter.next();
			
			if (e.getType() == Entity.Type.CREATURE) {
				if ( ((Creature)e).getEncounter() != null) {
					iter.remove();
				}
			}
		}
	}
	
	private void spawnRandom() {
		int numCreatures;
		if (minRandomCreatures == maxRandomCreatures)
			numCreatures = minRandomCreatures;
		else
			numCreatures = Game.dice.rand(minRandomCreatures, maxRandomCreatures);

		int max = this.baseCreatures.size() - 1;
		for (int index = 0; index < numCreatures; index++) {
			Creature c = new Creature(this.baseCreatures.get(Game.dice.rand(0, max)));
			c.setEncounter(this);
			c.resetAll();
			
			// give up eventually if we can't find a spot
			for (int count = 0; count < 100; count++) {
				int r = Game.dice.rand(0, this.size);
				int i = Game.dice.rand(0, r * 6);

				Point p = AreaUtil.convertPolarToGrid(areaPosition, r, i);
				
				if (area.isPassable(p.x, p.y) && area.getCreatureAtGridPoint(p) == null &&
					area.getDoorAtGridPoint(p) == null) {
					c.setPosition(p.x, p.y);
					area.getEntities().addEntity(c);
					areaCreatures.add(c);
					break;
				}
			}
		}
	}
	
	private void spawnFixed() {
		for (Creature c : baseCreatures) {
			int cx = areaPosition.x + c.getX();
			int cy = areaPosition.y + c.getY();
			
			if (c.getX() % 2 != 0 && areaPosition.x % 2 != 0) cy += 1;

			c.setPosition(cx, cy);
			c.setEncounter(this);
			c.resetAll();
			area.getEntities().addEntity(c);
			areaCreatures.add(c);
		}
	}
	
	private void spawnCreatures() {
		for (Creature c : areaCreatures) {
			area.getEntities().removeEntity(c);
		}
		areaCreatures.clear();
		
		this.lastSpawnRounds = Game.curCampaign.getDate().getTotalRoundsElapsed();
		
		if (this.randomize) {
			if (ScriptInterface.SpawnRandomEncounters) {
				spawnRandom();
			}
		} else {
			spawnFixed();
		}
		
		this.canAwardXP = true;
	}
	
	public void checkForRespawn() {
		if (this.area == null || !this.respawn) return;
		
		int currentRound = Game.curCampaign.getDate().getTotalRoundsElapsed();
		
		int elapsed = currentRound - this.lastSpawnRounds;
		
		if (elapsed >= this.respawnHours * Game.curCampaign.getDate().ROUNDS_PER_HOUR){
			spawnCreatures();
		}
	}
	
	public Point getAreaPosition() {
		return new Point(areaPosition);
	}
	
	public void addToArea(Area area, int x, int y) {
		this.area = area;
		this.areaPosition = new Point(x, y);
		
		area.getEncounters().add(this);
		area.getEncounterPositions().add(areaPosition);
		
		spawnCreatures();
	}
	
	public int getRespawnHours() { return respawnHours; }
	public void setRespawnHours(int hours) { this.respawnHours = hours; }
	
	public boolean isRespawn() { return respawn; }
	public void setRespawn(boolean respawn) { this.respawn = respawn; }
	
	public boolean canAwardXP() { return canAwardXP; }
	public void setCanAwardXP(boolean canAwardXP) { this.canAwardXP = canAwardXP; }
	
	public boolean isCompleted() {
		if (!canAwardXP) return false;
		
		for (Creature creature : areaCreatures) {
			if (!creature.isDying() && !creature.isDead()) return false;
		}
		
		canAwardXP = false;
		return true;
	}
	
	public void removeCreature(Creature creature) {
		this.baseCreatures.remove(creature);
	}
	
	public void addCreature(Creature creature, int x, int y) {
		creature.setFaction(this.getFaction());
		creature.setPosition(x, y);
		this.baseCreatures.add(creature);
	}
	
	public void clearHostiles() { hostiles.clear(); }
	
	public List<Creature> getBaseCreatures() { return baseCreatures; }
	public List<Creature> getCreatures() { return areaCreatures; }
	public List<Creature> getKnownHostiles() { return hostiles; }
	
	public void addHostiles(List<Creature> hostiles) {
		if (hostiles == null) return;
		
		for (Creature hostile : hostiles) {
			addHostile(hostile);
		}
	}
	
	public void addAreaCreature(Creature creature) {
		this.areaCreatures.add(creature);
	}
	
	public void addHostile(Creature hostile) {
		if (hostile == null) return;
		
		if (!hostiles.contains(hostile)) hostiles.add(hostile);
	}
	
	public void setRandomize(boolean randomize) { this.randomize = randomize; }
	
	public void setNumRandomCreatures(int min, int max) {
		this.minRandomCreatures = min;
		this.maxRandomCreatures = max;
	}
	
	public boolean randomize() { return this.randomize; }
	public int getMinRandomCreatures() { return this.minRandomCreatures; }
	public int getMaxRandomCreatures() { return this.maxRandomCreatures; }
	
	public String getName() { return this.name; }
	public int getSize() { return size; }
	public Faction getFaction() { return Game.ruleset.getFaction(faction); }
	
	public int getChallenge() { return challenge; }
	public void setChallenge(int challenge) { this.challenge = challenge; }
	
	public void setFaction(Faction faction) {
		this.faction = faction.getName();
	}
	
	public void setCreatureFaction(String faction) {
		setFaction(Game.ruleset.getFaction(faction));
		
		for (Creature c : areaCreatures) {
			c.setFaction(Game.ruleset.getFaction(faction));
		}
		
		Game.areaListener.getCombatRunner().checkAIActivation();
	}
	
	public void setSize(int size) { this.size = size; }
	
	public void activateCreatures() {
		for (Creature c : areaCreatures) {
			if (!c.isPlayerSelectable()) c.setAIActive(true);
		}
	}
	
	private void readEncounterFile(String ref) {
		FileKeyMap fileMap = new FileKeyMap("encounters/" + ref + ResourceType.Text.getExtension());
		
		size = fileMap.getValue("size", 0);
		challenge = fileMap.getValue("challenge", 0);
		faction = fileMap.getValue("faction", null);
		randomize = fileMap.getValue("randomize", false);
		respawn = fileMap.getValue("respawn", false);
		respawnHours = fileMap.getValue("respawnhours", 0);
		
		for (LineKeyList line : fileMap.get("creature")) {
			addCreature(Game.entityManager.getCreature(line.next()), line.nextInt(), line.nextInt());
		}
		
		if (fileMap.has("numrandomcreatures")) {
			LineKeyList line = fileMap.getLast("numrandomcreatures");
			minRandomCreatures = line.nextInt();
			maxRandomCreatures = line.nextInt();
		}
		
		fileMap.checkUnusedKeys();
	}
	
	@Override public String getReferenceType() {
		return "Encounter";
	}
	
	@Override public String getID() {
		return name;
	}
	
	@Override public String toString() {
		return name;
	}
	
	@Override public ReferenceList getReferenceList() {
		return new EncounterReferenceList(this);
	}
}
