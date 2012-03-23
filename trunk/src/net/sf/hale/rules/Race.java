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
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;

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
	
	private int baseStr, baseDex, baseCon, baseInt, baseWis, baseCha;
	
	public Race(String id) {
		FileKeyMap map = new FileKeyMap("races/" + id + ResourceType.Text.getExtension());
		
		this.id = id;
		this.name = id;
		
		this.baseStr = 10;
		this.baseDex = 10;
		this.baseCon = 10;
		this.baseInt = 10;
		this.baseWis = 10;
		this.baseCha = 10;
		
		abilitySelectionLists = new HashMap<Integer, List<String>>();
		
		abilities = new ArrayList<String>();
		racialTypes = new ArrayList<RacialType>();
		iconOffsets = new HashMap<SubIcon.Type, Point>();
		
		this.descriptionFile = map.getValue("descriptionfile", "descriptions/races/" + name + ResourceType.HTML.getExtension());
		
		String sizeString = map.getValue("size", null);
		this.size = sizeString == null ? Size.Medium : Size.valueOf(sizeString);
		this.icon = map.getValue("icon", null);
		
		movementCost = map.getValue("movementcost", 1000);

		String defaultWeaponString = map.getValue("unarmedweapon", null);
		this.defaultWeapon = Game.entityManager.getItem(defaultWeaponString);
		
		this.playerSelectable = map.getValue("playerselectable", false);
		
		this.maleBackgroundIcon = map.getValue("malebackgroundicon", null);
		this.maleForegroundIcon = map.getValue("maleforegroundicon", null);
		this.maleEarsIcon = map.getValue("maleearsicon", null);
		
		this.femaleBackgroundIcon = map.getValue("femalebackgroundicon", null);
		this.femaleForegroundIcon = map.getValue("femaleforegroundicon", null);
		this.femaleEarsIcon = map.getValue("femaleearsicon", null);
		
		this.maleClothesIcon = map.getValue("maleclothesicon", null);
		this.femaleClothesIcon = map.getValue("femaleclothesicon", null);
		
		for (LineKeyList line : map.get("racialtype")) {
			racialTypes.add(Game.ruleset.getRacialType(line.next()));
		}
		
		for (LineKeyList line : map.get("addability")) {
			abilities.add(line.next());
		}
		
		for (LineKeyList line : map.get("iconoffset")) {
			SubIcon.Type type = SubIcon.Type.valueOf(line.next());
			iconOffsets.put(type, new Point(line.nextInt(), line.nextInt()));
		}
		
		for (LineKeyList line : map.get("level")) {
			int level = line.nextInt();
			
			String type = line.next();
			
			List<String> lists = abilitySelectionLists.get(level);
			if (lists == null) {
				lists = new ArrayList<String>(1);
				abilitySelectionLists.put(level, lists);
			}
			
			if (type.equals("selectAbilityFromList")) {
				String abilityListID = line.next();
				lists.add(abilityListID);
			} else {
				Logger.appendToErrorLog("Unrecognized type " + type + " while loading race " + id);
			}
		}
		
		LineKeyList line = map.getLast("baseattributes");
		if (line != null) {
			baseStr = line.nextInt();
			baseDex = line.nextInt();
			baseCon = line.nextInt();
			baseInt = line.nextInt();
			baseWis = line.nextInt();
			baseCha = line.nextInt();
		}
		
		map.checkUnusedKeys();
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
