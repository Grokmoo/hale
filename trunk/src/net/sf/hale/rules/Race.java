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

package net.sf.hale.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilitySelectionList;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;

public class Race {
	private final String id;
	private final String name;
	
	private final String icon;
	private final Size size;
	private final int movementCost;
	private final Item defaultWeapon;
	private final boolean playerSelectable;
	private final List<RacialType> racialTypes;
	private final String descriptionFile;
	
	private final String maleForegroundIcon;
	private final String maleBackgroundIcon;
	private final String maleEarsIcon;
	
	private final String femaleForegroundIcon;
	private final String femaleBackgroundIcon;
	private final String femaleEarsIcon;
	
	private final String maleClothesIcon;
	private final String femaleClothesIcon;
	
	private final Map<Integer, List<String>> abilitySelectionLists;
	
	private final Map<SubIcon.Type, Point> iconOffsets;
	
	private List<String> abilities;
	
	private final int baseStr, baseDex, baseCon, baseInt, baseWis, baseCha;
	
	public Race(String id) {
		SimpleJSONParser parser = new SimpleJSONParser("races/" + id, ResourceType.JSON);
		
		this.id = id;
		this.name = parser.get("name", id);
		this.defaultWeapon = Game.entityManager.getItem(parser.get("unarmedWeapon", null));
		
		parser.setWarnOnMissingKeys(false);
		
		this.size = Size.valueOf(parser.get("size", "Medium"));
		this.movementCost = parser.get("baseMovementCost", 1000);
		this.descriptionFile = parser.get("descriptionFile", "descriptions/races/" + name + ResourceType.HTML.getExtension());
		this.icon = parser.get("icon", null);
		this.playerSelectable = parser.get("playerSelectable", false);
		
		if (parser.containsKey("baseAttributes")) {
			SimpleJSONArray attrArray = parser.getArray("baseAttributes");
			
			Iterator<SimpleJSONArrayEntry> iter = attrArray.iterator();
			
			this.baseStr = iter.next().getInt(10);
			this.baseDex = iter.next().getInt(10);
			this.baseCon = iter.next().getInt(10);
			this.baseInt = iter.next().getInt(10);
			this.baseWis = iter.next().getInt(10);
			this.baseCha = iter.next().getInt(10);
		} else {
			this.baseStr = 10;
			this.baseDex = 10;
			this.baseCon = 10;
			this.baseInt = 10;
			this.baseWis = 10;
			this.baseCha = 10;
		}
		
		abilities = new ArrayList<String>();
		if (parser.containsKey("abilities")) {
			SimpleJSONArray array = parser.getArray("abilities");
			for (SimpleJSONArrayEntry entry : array) {
				abilities.add(entry.getString());
			}
		}
		((ArrayList<String>)abilities).trimToSize();
		
		racialTypes = new ArrayList<RacialType>();
		if (parser.containsKey("racialTypes")) {
			SimpleJSONArray array = parser.getArray("racialTypes");
			for (SimpleJSONArrayEntry entry : array) {
				racialTypes.add(Game.ruleset.getRacialType(entry.getString()));
			}
		}
		((ArrayList<RacialType>)racialTypes).trimToSize();
		
		if (parser.containsKey("icons")) {
			SimpleJSONObject obj = parser.getObject("icons");
			
			this.maleBackgroundIcon = obj.get("maleBackground", null);
			this.maleForegroundIcon = obj.get("maleForeground", null);
			this.maleEarsIcon = obj.get("maleEars", null);
			this.femaleBackgroundIcon = obj.get("femaleBackground", null);
			this.femaleForegroundIcon = obj.get("femaleForeground", null);
			this.femaleEarsIcon = obj.get("femaleEars", null);
			this.maleClothesIcon = obj.get("maleClothes", null);
			this.femaleClothesIcon = obj.get("femaleClothes", null);
		} else {
			this.maleBackgroundIcon = null;
			this.maleForegroundIcon = null;
			this.maleEarsIcon = null;
			this.femaleBackgroundIcon = null;
			this.femaleForegroundIcon = null;
			this.femaleEarsIcon = null;
			this.maleClothesIcon = null;
			this.femaleClothesIcon = null;
		}
		
		iconOffsets = new HashMap<SubIcon.Type, Point>();
		if (parser.containsKey("iconOffsets")) {
			SimpleJSONObject obj = parser.getObject("iconOffsets");
			
			for (String key : obj.keySet()) {
				SubIcon.Type type = SubIcon.Type.valueOf(key);
				
				SimpleJSONArray array = obj.getArray(key);
				Iterator<SimpleJSONArrayEntry> iter = array.iterator();
				
				int x = iter.next().getInt(0);
				int y = iter.next().getInt(0);
				
				iconOffsets.put(type, new Point(x, y));
			}
		}
		
		abilitySelectionLists = new HashMap<Integer, List<String>>();
		if (parser.containsKey("abilitySelectionsFromList")) {
			SimpleJSONObject obj = parser.getObject("abilitySelectionsFromList");
			
			for (String listID : obj.keySet()) {
				SimpleJSONArray array = obj.getArray(listID);
				
				for (SimpleJSONArrayEntry entry : array) {
					int level = entry.getInt(0);
					
					addAbilitySelectionListAtLevel(listID, level);
				}
			}
		}
		
		parser.warnOnUnusedKeys();
	}
	
	private void addAbilitySelectionListAtLevel(String listID, int level) {
		List<String> listsAtLevel = abilitySelectionLists.get(level);
		if (listsAtLevel == null) {
			listsAtLevel = new ArrayList<String>(1);
			abilitySelectionLists.put(level, listsAtLevel);
		}
		
		listsAtLevel.add(listID);
	}
	
	public Point getIconOffset(SubIcon.Type type) {
		if (!iconOffsets.containsKey(type)) {
			iconOffsets.put(type, new Point(0, 0));
		}
		
		return new Point(iconOffsets.get(type));
	}
	
	public String getMaleClothesIcon() { return maleClothesIcon; }
	public String getFemaleClothesIcon() { return femaleClothesIcon; }
	
	public String getFemaleEarsIcon() { return femaleEarsIcon; }
	public String getFemaleForegroundIcon() { return femaleForegroundIcon; }
	public String getFemaleBackgroundIcon() { return femaleBackgroundIcon; }
	public String getMaleEarsIcon() { return maleEarsIcon; }
	public String getMaleForegroundIcon() { return maleForegroundIcon; }
	public String getMaleBackgroundIcon() { return maleBackgroundIcon; }
	
	public String getDescriptionFile() {
		return ResourceManager.getResourceAsString(descriptionFile);
	}
	
	public String getID() { return id; }
	public String getName() { return name; }
	public String getIcon() { return icon; }
	public Size getSize() { return size; }
	public int getMovementCost() { return movementCost; }
	public boolean isPlayerSelectable() { return playerSelectable; }

	public int getBaseStr() { return baseStr; }
	public int getBaseDex() { return baseDex; }
	public int getBaseCon() { return baseCon; }
	public int getBaseInt() { return baseInt; }
	public int getBaseWis() { return baseWis; }
	public int getBaseCha() { return baseCha; }
	
	public boolean hasRacialType(String racialTypeID) {
		for (RacialType type : racialTypes) {
			if (type.getName().equals(racialTypeID)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * For each AbilitySelectionList in the returned List, the Creature gaining
	 * the creature level specified pick one Ability from that
	 * AbilitySelectionList.  If their are no selections to be made, the List
	 * will be empty.
	 * 
	 * @param level the creature level that has been gained
	 * @return the List of AbilitySelectionLists to choose abilities from
	 */
	
	public List<AbilitySelectionList> getAbilitySelectionsAddedAtLevel(int level) {
		List<String> idList = abilitySelectionLists.get(level);
		
		if (idList == null) return Collections.emptyList();
		
		List<AbilitySelectionList> lists = new ArrayList<AbilitySelectionList>(idList.size());
		for (String id : idList) {
			lists.add(Game.ruleset.getAbilitySelectionList(id));
		}
		
		return lists;
	}
	
	public List<RacialType> getRacialTypes() {
		return new ArrayList<RacialType>(racialTypes);
	}
	
	/**
	 * Adds all Racial abilities for this Race to the specified Creature.
	 * 
	 * @param creature the Creature to add abilities to
	 */
	
	public void addAbilitiesToCreature(Creature creature) {
		for (String abilityID : abilities) {
			Ability ability = Game.ruleset.getAbility(abilityID);
			if (ability == null) {
				Logger.appendToWarningLog("Racial ability " + abilityID + " for race " + this.id + " not found.");
				continue;
			}
			
			creature.getAbilities().addRacialAbility(ability);
		}
	}
	
	/**
	 * Returns a set containing all AbilitySelectionLists that are referenced at any
	 * level within this Race
	 * @return the set of AbilitySelectionLists
	 */
	
	public Set<AbilitySelectionList> getAllReferencedAbilitySelectionLists() {
		Set<AbilitySelectionList> lists = new LinkedHashSet<AbilitySelectionList>();
		
		for (int level : abilitySelectionLists.keySet()) {
			for (String listID : abilitySelectionLists.get(level)) {
				AbilitySelectionList list = Game.ruleset.getAbilitySelectionList(listID);
				lists.add(list);
			}
		}
		
		return lists;
	}
	
	public Item getDefaultWeapon() {
		return new Item(defaultWeapon);
	}
}
