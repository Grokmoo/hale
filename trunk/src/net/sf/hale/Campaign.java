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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.entity.CreatedItem;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Date;
import net.sf.hale.rules.Faction;
import net.sf.hale.rules.Merchant;
import net.sf.hale.rules.Skill;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;
import net.sf.hale.view.WorldMapPopup;

public class Campaign {
	private final String id;
	private String startArea;
	private String name;
	private int minPartySize = 1;
	private int maxPartySize = 4;
	private int minStartingLevel = 1;
	private int maxStartingLevel = 1;
	private boolean stripStartingCharacters = false;
	private String worldMapImage;
	private String startingMerchant;
	private String startingCharacter;
	private RecipeManager recipeManager;
	private Map<String, Tileset> tilesets;
	private Map<String, Encounter> encounters;
	public Encounter partyEncounter;
	
	public ScriptState scriptState;
	
	private Map<String, CreatedItem> createdItems;
	private List<Faction.CustomRelationship> customRelationships;
	public List<WorldMapLocation> worldMapLocations;
	public QuestEntryList questEntries;
	private Map<String, Merchant> merchants;
	private Map<String, AreaTransition> transitions;
	private Map<String, AreaTrigger> triggers;
	private Map<String, Area> areas;
	public Area curArea;
	public Party party;
	public Currency partyCurrency;
	private Date date;
	private String currentDifficulty;
	
	/**
	 * Gets a JSONObject with all of the save game data from this campaign
	 * @return the JSONObject
	 */
	
	public JSONOrderedObject getSaveGameData() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("comment", "Automatically generated Save file.  Edit at your own risk!");
		
		data.put("id", id);
		data.put("currentDifficulty", currentDifficulty);
		data.put("date", date.getTotalRoundsElapsed());
		data.put("partyCurrency", partyCurrency.getValue());
		
		data.put("currentArea", SaveGameUtil.getRef(curArea));
		
		data.put("party", party.save());
		
		Object[] areasData = new Object[areas.size()];
		int i = 0;
		for (String areaID : areas.keySet()) {
			areasData[i] = areas.get(areaID).save();
			i++;
		}
		data.put("loadedAreas", areasData);
		
		if (createdItems.size() > 0) {
			Object[] createdItemsData = new Object[createdItems.size()];
			i = 0;
			for (CreatedItem createdItem : createdItems.values()) {
				createdItemsData[i] = createdItem.save();
				i++;
			}
			
			data.put("createdItems", createdItemsData);
		}
		
		List<Object> triggerData = new ArrayList<Object>();
		for (String triggerID : triggers.keySet()) {
			Object trigger = triggers.get(triggerID).save();
			
			if (trigger != null)
				triggerData.add(trigger);
		}
		data.put("triggers", triggerData.toArray());
		
		List<Object> transitionData = new ArrayList<Object>();
		for (String transitionID : transitions.keySet()) {
			Object transition = transitions.get(transitionID).save();
			
			if (transition != null)
				transitionData.add(transition);
		}
		data.put("transitions", transitionData.toArray());
		
		Object[] merchantData = new Object[merchants.size()];
		i = 0;
		for (String merchantID : merchants.keySet()) {
			merchantData[i] = merchants.get(merchantID).save();
			i++;
		}
		data.put("merchants", merchantData);
		
		data.put("questEntries", questEntries.save());
		
		List<Object> locationData = new ArrayList<Object>();
		for (WorldMapLocation location : worldMapLocations) {
			if (location.isRevealed())
				locationData.add(location.save());
		}
		
		if (locationData.size() > 0)
			data.put("worldMapLocations", locationData.toArray());
		
		if (!scriptState.isEmpty())
			data.put("scriptState", scriptState.save());
		
		if (customRelationships.size() > 0) {
			i = 0;
			Object[] crData = new Object[customRelationships.size()];
			for (Faction.CustomRelationship cr : customRelationships) {
				crData[i] = cr.save();
				i++;
			}
			data.put("factionRelationships", crData);
		}
		
		return data;
	}
	
	public void load(SimpleJSONParser data) throws LoadGameException {
		ReferenceHandler refHandler = new ReferenceHandler();
		
		if (!this.id.equals(data.get("id", null))) {
			throw new LoadGameException("Campaign Save file ID does not match");
		}
		
		partyEncounter = new Encounter();
		
		Game.ruleset.getDifficultyManager().setCurrentDifficulty(data.get("currentDifficulty", null));
		
		date.reset();
		date.incrementRounds(data.get("date", 0));
		
		partyCurrency = new Currency();
		partyCurrency.addCP(data.get("partyCurrency", 0));
		
		this.createdItems.clear();
		if (data.containsKey("createdItems")) {
			for (SimpleJSONArrayEntry entry : data.getArray("createdItems")) {
				CreatedItem createdItem = CreatedItem.load(entry.getObject());
				
				this.createdItems.put(createdItem.getCreatedItemID(), createdItem);
			}
		}
		
		areas.clear();
		SimpleJSONArray loadedAreas = data.getArray("loadedAreas");
		for (SimpleJSONArrayEntry entry : loadedAreas) {
			SimpleJSONObject areaData = entry.getObject();
			
			Area area = Area.load(areaData, refHandler);
			areas.put(area.getName(), area);
		}
		
		this.party = Party.load(data.getObject("party"), refHandler);
		
		this.curArea = refHandler.getArea(data.get("currentArea", null));
		
		for (SimpleJSONArrayEntry entry : data.getArray("triggers")) {
			SimpleJSONObject entryData = entry.getObject();
			
			String id = entryData.get("id", null);
			
			this.getTrigger(id).load(entryData);
		}
		
		for (SimpleJSONArrayEntry entry : data.getArray("transitions")) {
			SimpleJSONObject entryData = entry.getObject();
			
			String name = entryData.get("name", null);
			
			this.getAreaTransition(name).load(entryData);
		}
		
		for (SimpleJSONArrayEntry entry : data.getArray("merchants")) {
			SimpleJSONObject entryData = entry.getObject();
			
			String id = entryData.get("id", null);
			
			this.getMerchant(id).load(entryData);
		}
		
		this.questEntries = QuestEntryList.load(data.getObject("questEntries"));
		
		if (data.containsKey("worldMapLocations")) {
			for (SimpleJSONArrayEntry entry : data.getArray("worldMapLocations")) {
				SimpleJSONObject entryData = entry.getObject();
				
				String name = entryData.get("name", null);
				boolean revealed = entryData.get("revealed", false);
				
				// find the location with the specified name and set the revealed status
				for (WorldMapLocation location : worldMapLocations) {
					if (location.getName().equals(name)) {
						location.setRevealed(revealed);
					}
				}
			}
		}
		
		if (data.containsKey("scriptState"))
			this.scriptState = ScriptState.load(data.getObject("scriptState"));
		else
			this.scriptState = new ScriptState();
		
		if (data.containsKey("factionRelationships")) {
			for (SimpleJSONArrayEntry entry : data.getArray("factionRelationships")) {
				Faction.CustomRelationship cr = Faction.CustomRelationship.load(entry.getObject());
				cr.setFactionRelationships();
				addCustomRelationship(cr);
			}
		}
		
		refHandler.resolveAllReferences();
	}
	
	public void setMinPartySize(int size) { this.minPartySize = size; }
	public void setMaxPartySize(int size) { this.maxPartySize = size; }
	public void setMinStartingLevel(int level) { this.minStartingLevel = level; }
	public void setMaxStartingLevel(int level) { this.maxStartingLevel = level; }
	
	public void setWorldMapImage(String image) { this.worldMapImage = image; }
	public void setStripStartingCharacters(boolean strip) { this.stripStartingCharacters = strip; }
	public void setName(String name) { this.name = name; }
	public void setStartArea(String startArea) { this.startArea = startArea; }
	public void setStartingMerchant(String merchant) { this.startingMerchant = merchant; }
	public void setStartingCharacter(String character) { this.startingCharacter = character; }
	
	public String getStartArea() { return startArea; }
	
	public void setCurrentDifficulty(String difficulty) {
		this.currentDifficulty = difficulty;
	}
	
	public void readCampaignFile() {
		FileKeyMap fileMap = new FileKeyMap("campaign.txt");
		
		startArea = fileMap.getValue("startarea", null);
		
		name = fileMap.getValue("name", id);
		worldMapImage = fileMap.getValue("worldmapimage", null);
		minPartySize = fileMap.getValue("minpartysize", 1);
		maxPartySize = fileMap.getValue("maxpartysize", 1);
		minStartingLevel = fileMap.getValue("minstartinglevel", 1);
		maxStartingLevel = fileMap.getValue("maxstartinglevel", 1);
		stripStartingCharacters = fileMap.getValue("stripstartingcharacters", false);
		startingMerchant = fileMap.getValue("startingmerchant", null);
		startingCharacter = fileMap.getValue("startingcharacter", null);
		
		if (startingMerchant != null && startingMerchant.equals("null")) startingMerchant = null;
		if (worldMapImage != null && worldMapImage.equals("null")) worldMapImage = null;
		
		int roundsPerMinute = fileMap.getValue("roundsperminute", 10);
		int minutesPerHour = fileMap.getValue("minutesperhour", 60);
		int hoursPerDay = fileMap.getValue("hoursperday", 24);
		int daysPerMonth = fileMap.getValue("dayspermonth", 30);
		
		date = new Date(roundsPerMinute, minutesPerHour, hoursPerDay, daysPerMonth);
		
		for (LineKeyList line : fileMap.get("addlocation")) {
			String name = line.next();
			String transition = line.next();
			String icon = line.next();
			Point position = new Point(line.nextInt(), line.nextInt());
			if (icon.equals("null")) icon = null;
			if (transition.equals("null")) transition = null;
			
			worldMapLocations.add(new WorldMapLocation(name, transition, icon, position));
		}
		
		for (LineKeyList line : fileMap.get("settraveltime")) {
			String from = line.next();
			String to = line.next();
			int hours = line.nextInt();
			
			getWorldMapLocation(from).setTravelTime(to, hours);
			getWorldMapLocation(to).setTravelTime(from, hours);
		}
		
		loadAreaTransitions();
		loadTriggers();
		loadEncounters();
		loadRecipes();
		loadTilesets();
		
		if (startArea != null) {
			curArea = getArea(startArea);
		} else {
			Logger.appendToErrorLog("No start area defined for campaign: " + getID());
		}
		
		fileMap.checkUnusedKeys();
	}
	
	public void writeCampaignFile() {
		File fout = new File("campaigns/" + id + "/campaign.txt");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			
			out.write("name \"" + name + "\""); out.newLine();
			
			out.write("startArea \"" + startArea + "\""); out.newLine();
			
			out.write("minPartySize " + minPartySize); out.newLine();
			out.write("maxPartySize " + maxPartySize); out.newLine();
			
			out.write("minStartingLevel " + minStartingLevel); out.newLine();
			out.write("maxStartingLevel " + maxStartingLevel); out.newLine();
			
			out.write("roundsPerMinute " + date.ROUNDS_PER_MINUTE); out.newLine();
			out.write("minutesPerHour " + date.MINUTES_PER_HOUR); out.newLine();
			out.write("hoursPerDay " + date.HOURS_PER_DAY); out.newLine();
			out.write("daysPerMonth " + date.DAYS_PER_MONTH); out.newLine();
			
			out.write("stripStartingCharacters " + stripStartingCharacters); out.newLine();
			
			if (startingMerchant != null) {
				out.write("startingMerchant \"" + startingMerchant + "\"");
				out.newLine();
			}
			
			if (startingCharacter != null) {
				out.write("startingCharacter \"" + startingCharacter + "\"");
				out.newLine();
			}
			
			if (worldMapImage != null) {
				out.write("worldMapImage \"" + worldMapImage + "\"");
				out.newLine();
			}
			
			for (WorldMapLocation location : worldMapLocations) {
				out.write("addLocation \"" + location.getName() + "\" \"" + location.getAreaTransition() + "\" \"");
				out.write(location.getIcon() + "\" " + location.getPosition().x + " " + location.getPosition().y);
				out.newLine();
			}
			
			for (int i = 0; i < worldMapLocations.size(); i++) {
				for (int j = i + 1; j < worldMapLocations.size(); j++) {
					out.write("setTravelTime \"" + worldMapLocations.get(i).getName() + "\" \"");
					out.write(worldMapLocations.get(j).getName() + "\" ");
					out.write("" + worldMapLocations.get(i).getTravelTime(worldMapLocations.get(j)));
					out.newLine();
				}
			}
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving campaign file " + id, e);
		}
	}
	
	public Campaign(String id) {
		this.id = id;
		name = "";
		areas = new HashMap<String, Area>();
		transitions = new HashMap<String, AreaTransition>();
		triggers = new HashMap<String, AreaTrigger>();
		party = new Party();
		encounters = new HashMap<String, Encounter>();
		merchants = new HashMap<String, Merchant>();
		recipeManager = new RecipeManager();
		partyCurrency = new Currency();
		partyEncounter = new Encounter();
		questEntries = new QuestEntryList();
		worldMapLocations = new ArrayList<WorldMapLocation>();
		
		createdItems = new HashMap<String, CreatedItem>();
		tilesets = new HashMap<String, Tileset>();
		
		scriptState = new ScriptState();
		
		customRelationships = new ArrayList<Faction.CustomRelationship>();
	}
	
	/**
	 * Clears most campaign related data (transitions, triggers, encounters, merchants)
	 * but not all data, such as party data
	 */
	
	public void clear() {
		areas.clear();
		transitions.clear();
		triggers.clear();
		encounters.clear();
		merchants.clear();
		worldMapLocations.clear();
		createdItems.clear();
		tilesets.clear();
		scriptState = new ScriptState();
	}
	
	public void addCustomRelationship(Faction.CustomRelationship cr) {
		customRelationships.add(cr);
	}
	
	public Collection<CreatedItem> getCreatedItems() {
		return createdItems.values();
	}
	
	public CreatedItem getCreatedItem(String createdItemID) {
		return createdItems.get(createdItemID);
	}
	
	public void addCreatedItem(CreatedItem createdItem) {
		this.createdItems.put(createdItem.getCreatedItemID(), createdItem);
	}
	
	public void addCreatedItem(String baseItemID, Item item) {
		this.createdItems.put(item.getID(), new CreatedItem(baseItemID, item));
	}
	
	public Sprite getWorldMapSprite() {
		if (worldMapImage == null) return null;
		
		return SpriteManager.getSpriteAnyExtension(worldMapImage);
	}
	
	public String getWorldMapImage() { return worldMapImage; }
	
	public int getBestPartySkillCheck(String skillID) {
		int modifier = getBestPartySkillModifier(skillID);
		
		return modifier + Game.dice.d100();
	}
	
	public int getBestPartySkillRanks(String skillID) {
		int bestRanks = -1;
		
		for (Creature c : party) {
			int currentRanks = c.getSkillSet().getRanks(skillID);
			
			if (currentRanks > bestRanks) {
				bestRanks = currentRanks;
			}
		}
		
		return bestRanks;
	}
	
	public int getBestPartySkillModifier(String skillID) {
		int bestModifier = 0;
		
		for (Creature c : party) {
			int currentModifier = c.getSkillModifier(skillID);
			if (currentModifier > bestModifier) bestModifier = currentModifier;
		}

		return bestModifier;
	}
	
	/**
	 * Initializes the Player Character "party" (group of playable characters) for
	 * this Campaign.  If this Campaign has a specified starting character
	 * (if @link {@link #getStartingCharacter()} returns non-null) then the starting
	 * character returned by getStartingCharacter is added to the party. Otherwise,
	 * all of the characters specified by the given list of IDs is added.
	 * 
	 * @param characterIDs the list of character IDs to be added to the party for this
	 * @param name the name of the party
	 * Campaign if the starting character is null.
	 */
	
	public void addParty(List<String> characterIDs, String name) {
		if (startingCharacter != null) {
			Creature c = Game.entityManager.getCreature(startingCharacter);
			c.setPlayerSelectable(true);
			c.setFaction(Game.ruleset.getString("PlayerFaction"));
			addCreatureToParty(c);
			
		} else {
			for (String id : characterIDs) {
				Creature c = Game.entityManager.getCharacter(id);
				addCreatureToParty(c);
			}
		}
		
		party.setName(name);
	}
	
	/**
	 * Adds the specified characters directly to the party, bypassing any campaign
	 * default starting character
	 * @param characters the characters to add
	 * @param name the name of the party
	 */
	
	public void addPartyCreatures(List<Creature> characters, String name) {
		for (Creature creature : characters) {
			addCreatureToParty(creature);
		}
		
		party.setName(name);
	}
	
	private void addCreatureToParty(Creature c) {
		c.getInventory().checkAllItemsValid();
		c.setEncounter(Game.curCampaign.partyEncounter);
		Game.curCampaign.partyEncounter.addAreaCreature(c);
		party.add(c);
	}
	
	public void checkEncounterRespawns() {
		for (Area area : areas.values()) {
			area.checkEncounterRespawns();
		}
	}
	
	public void transition(String transitionID) {
		AreaTransition transition = this.getAreaTransition(transitionID);
		
		transition(transition, Game.curCampaign.curArea.getName());
	}
	
	public void transition(AreaTransition transition, String curAreaName) {
		// reveal world map location if it is not already revealed
		WorldMapLocation location = Game.curCampaign.getWorldMapLocation(transition.getWorldMapLocation());
		if (location != null) {
			location.setRevealed(true);
		}
		
		// run current area onExit script
		curArea.runOnAreaExit(transition);
		
		Point endBasePos = new Point(false);
		List<Point> endPos = new ArrayList<Point>();
		String endArea = null;
		
		if (curAreaName.equals(transition.getAreaFrom())) {
			endArea = transition.getAreaTo();
			endBasePos = new Point(transition.getAreaToX(), transition.getAreaToY());
			endPos.addAll(transition.getToPositions());
			
		} else if (transition.twoWay() && curAreaName.equals(transition.getAreaTo())) {
			endArea = transition.getAreaFrom();
			endBasePos = new Point(transition.getAreaFromX(), transition.getAreaFromY());
			endPos.addAll(transition.getFromPositions());
			
		} else if (transition.getAreaFrom().equals("World Map")) {
			endArea = transition.getAreaTo();
			endBasePos = new Point(transition.getAreaToX(), transition.getAreaToY());
			endPos.addAll(transition.getToPositions());
			
		} else if (transition.twoWay() && transition.getAreaTo().equals("World Map")) {
			endArea = transition.getAreaFrom();
			endBasePos = new Point(transition.getAreaFromX(), transition.getAreaFromY());
			endPos.addAll(transition.getFromPositions());
			
		} else {
			Logger.appendToErrorLog("Error transitioning with " + transition.getName() +
					".  No match for " + curArea.getName() + " found.");
			return;
		}
		
		if (endArea.equals("World Map")) {
			new WorldMapPopup(Game.mainViewer, transition).openPopupCentered();
			return;
		}
		
		Creature c = Game.curCampaign.party.getSelected();
		
		List<AbilitySlot> canceledAuraSlots = new ArrayList<AbilitySlot>();
		for (Creature p : Game.curCampaign.party) {
			canceledAuraSlots.addAll(p.getAbilities().cancelAllAuras());
		}
		
		for (Creature p : Game.curCampaign.party) {
			curArea.getEntities().removeEntity(p);
		}
		
		getTileset(curArea.getTileset()).freeTiles();
		
		// now we switch over to the new area
		curArea = getArea(endArea);
		curArea.runOnAreaLoad(transition);
		
		c.setVisibility(true);
		
		Point cPos = new Point(endPos.get(0).x + endBasePos.x, endPos.get(0).y + endBasePos.y);
		if (endPos.get(0).x % 2 != 0 && endBasePos.x % 2 != 0) cPos.y += 1;
		c.setPosition(cPos);
		
		curArea.getEntities().addEntity(c);
		
		Creature lastCreature = c;
		int index = 1;
		for (Creature p : Game.curCampaign.party) {
			if (p != c) {
				curArea.getEntities().addEntity(p);
				
				p.setVisibility(true);
				
				Point pPos = new Point();
				
				if (index < endPos.size()) {
					pPos.x = endPos.get(index).x + endBasePos.x;
					pPos.y = endPos.get(index).y + endBasePos.y;
					
					if (endPos.get(index).x %2 != 0 && endBasePos.x % 2 != 0)
						pPos.y += 1;
				} else {
					// if there are no more explicit transition locations, just add the creature
					// wherever there is a nearby space
					pPos = AIScriptInterface.findClosestEmptyTile(lastCreature.getPosition(), 3);
				}
				
				if (pPos == null) {
					// no valid point was found, we must remove the creature to continue
					p.kill();
					Game.areaListener.checkKillEntity(p);
					
				} else {
					p.setPosition(pPos);
				}
				
				index++;
				lastCreature = p;
			}
		}
		
		Game.mainViewer.addMessage("red", "Entered area " + Game.curCampaign.curArea.getName());
		if (Game.isInTurnMode()) Game.areaListener.getCombatRunner().exitCombat();
		Game.areaViewer.setArea(curArea);
		Game.areaListener.setArea(curArea);
		
		curArea.setEntityVisibility();
		
		Game.areaListener.nextTurn();
		
		Game.areaListener.getCombatRunner().checkAIActivation();
		
		Game.areaViewer.scrollToCreature(Game.curCampaign.party.getSelected());
		
		Game.areaListener.getCombatRunner().checkForceCombatMode();
		
		for (AbilitySlot slot : canceledAuraSlots) {
			// reactivate cancelable mode ability slots
			// that were canceled due to auras being removed
			slot.getAbility().executeFunction(ScriptFunctionType.onReactivate, slot);
		}
	}
	
	public WorldMapLocation getWorldMapLocation(String ref) {
		for (WorldMapLocation location : worldMapLocations) {
			if (location.getName().equals(ref)) return location;
		}
		
		return null;
	}
	
	public Encounter getEncounter(String ref) {
		Encounter encounter = encounters.get(ref);
		
		if (encounter == null) {
			encounter = new Encounter(ref);
			encounters.put(ref, encounter);
		}
		
		return new Encounter(encounter);
	}
	
	public void removeArea(String id) {
		areas.remove(id);
	}
	
	public Area getArea(String ref) {
		Area area = areas.get(ref);
		
		if (area == null) {
			try {
				area = new Area(ref);
				areas.put(ref, area);
			} catch (Exception e) {
				Logger.appendToErrorLog("Error loading area " + ref, e);
			}
		}
		
		return area;
	}
	
	public void loadEncounters() {
		encounters.clear();
		
		Set<String> resources = ResourceManager.getResourcesInDirectory("encounters");
		for (String resource : resources) {
			
			String id = ResourceManager.getResourceID(resource, "encounters", ResourceType.Text);
			if (id == null) continue;
			
			getEncounter(id);
		}
	}
	
	public void loadAreaTransitions() {
		transitions.clear();
		
		Set<String> resources = ResourceManager.getResourcesInDirectory("transitions");
		for (String resource : resources) {
			
			String id = ResourceManager.getResourceID(resource, "transitions", ResourceType.Text);
			if (id == null) continue;
			
			AreaTransition transition = getAreaTransition(id);
			if (transition.getMaxCharacters() < this.maxPartySize) {
				Logger.appendToWarningLog("Maximum party size for campaign " + this.getID() + " is " +
								   this.getMaxPartySize() + " but area transition " + transition.getName() +
								   " has only " + transition.getMaxCharacters() + " positions.");
			}
		}
	}
	
	public void loadTriggers() {
		triggers.clear();
		
		Set<String> resources = ResourceManager.getResourcesInDirectory("triggers");
		for (String resource : resources) {
			String id = ResourceManager.getResourceID(resource, "triggers", ResourceType.Text);
			if (id == null) continue;
			
			getTrigger(id);
		}
	}
	
	private void loadTilesets() {
		for (String resource : ResourceManager.getResourcesInDirectory("tilesets")) {
			String id = ResourceManager.getResourceID(resource, "tilesets", ResourceType.JSON);
			if (id == null) continue;
			
			Tileset tileset = new Tileset(id, resource);
			tilesets.put(id, tileset);
		}
	}
	
	public void loadRecipes() {
		recipeManager.loadRecipes();
	}
	
	public void removeTrigger(String ref) {
		triggers.remove(ref);
	}
	
	public AreaTrigger getTrigger(String ref) {
		AreaTrigger trigger = triggers.get(ref);
		
		if (trigger == null) {
			trigger = new AreaTrigger(ref);
			triggers.put(ref, trigger);
		}
		
		return trigger;
	}
	
	public void removeAreaTransition(String ref) {
		transitions.remove(ref);
	}
	
	public AreaTransition getAreaTransition(String ref) {
		AreaTransition transition = transitions.get(ref);
		
		if (transition == null) {
			transition = new AreaTransition(ref);
			transitions.put(ref, transition);
		}
		
		return transition;
	}
	
	public Merchant getMerchant(String name) {
		Merchant merchant = merchants.get(name);
		
		if (merchant == null) {
			merchant = new Merchant(name);
			if (merchant != null) {
				merchants.put(name, merchant);
			}
		}
		
		return merchant;
	}
	
	public Recipe getRecipe(String id) {
		return recipeManager.getRecipe(id);
	}
	
	public Tileset getTileset(String id) { return tilesets.get(id); }
	public Set<String> getAllTilesetIDs() { return tilesets.keySet(); }
	
	public Set<String> getAllRecipeIDs() { return recipeManager.getAllRecipeIDs(); }
	
	public List<String> getRecipeIDsForSkill(Skill skill) { return recipeManager.getRecipeIDsForSkill(skill); }
	
	public Collection<AreaTrigger> getTriggers() { return triggers.values(); }
	public Collection<AreaTransition> getAreaTransitions() { return transitions.values(); }
	public Collection<Merchant> getMerchants() { return merchants.values(); }
	public Collection<Encounter> getEncounters() { return encounters.values(); }
	public Collection<Area> getLoadedAreas() { return areas.values(); }
	public String getStartingCharacter() { return startingCharacter; }
	public String getStartingMerchant() { return startingMerchant; }
	public boolean stripStartingCharacters() { return stripStartingCharacters; }
	public Currency getPartyCurrency() { return partyCurrency; }
	public int getMinPartySize() { return minPartySize; }
	public int getMaxPartySize() { return maxPartySize; }
	public int getMinStartingLevel() { return minStartingLevel; }
	public int getMaxStartingLevel() { return maxStartingLevel; }
	public Date getDate() { return date; }
	public String getID() { return id; }
	public String getName() { return name; }
}
