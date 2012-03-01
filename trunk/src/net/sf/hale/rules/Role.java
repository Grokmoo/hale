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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilitySelectionList;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;

public class Role {
	public static int MAX_LEVELS = 30;
	
	private final String descriptionFile;
	private final String id;
	private final String name;
	private final String icon;
	private final int skillsPerLevel;
	private final int hpPerLevel;
	private final int level1HP;
	private final int attackBonusPerLevel;
	private final int damageBonusPerLevel;
	
	private final int[] casterLevels;
	
	private final Stat spellCastingAttribute;
	
	private final int spellFailureBase;
	private final int spellFailureSpellLevelFactor;
	private final int spellFailureAbilityScoreFactor;
	private final int spellFailureCasterLevelFactor;
	
	private final boolean playerSelectable;
	private final boolean baseRole;
	private final int[] defaultPlayerAttributeSelections;
	
	private final PrereqList prereqs;
	
	private final List<LevelUpList> levelUpLists;
	
	public Role(String id, String path) {
		this.id = id;
		
		this.levelUpLists = new ArrayList<LevelUpList>(Role.MAX_LEVELS);
		for (int i = 0; i < Role.MAX_LEVELS; i++) {
			levelUpLists.add(new LevelUpList());
		}
		
		casterLevels = new int[Role.MAX_LEVELS];
		for (int i = 0; i < casterLevels.length; i++) {
			casterLevels[i] = 0;
		}
		
		prereqs = new PrereqList();
		
		FileKeyMap map = new FileKeyMap(path);
		
		name = map.getValue("name", id);
		icon = map.getValue("icon", null);
		playerSelectable = map.getValue("playerselectable", false);
		descriptionFile = map.getValue("descriptionfile", "descriptions/roles/" + name + ResourceType.HTML.getExtension());
		baseRole = map.getValue("baserole", false);
		
		damageBonusPerLevel = map.getValue("damagebonusperlevel", 1);
		attackBonusPerLevel = map.getValue("attackbonusperlevel", 1);
		level1HP = map.getValue("level1hp", 6);
		hpPerLevel = map.getValue("hpperlevel", 2);
		skillsPerLevel = map.getValue("skillpointsperlevel", 5);
		
		String castingAttString = map.getValue("spellcastingattribute", null);
		spellCastingAttribute = castingAttString == null ? Stat.Wis : Stat.valueOf(castingAttString);

		spellFailureBase = map.getValue("spellfailurebase", 0);
		spellFailureSpellLevelFactor = map.getValue("spellfailurespelllevelfactor", 0);
		spellFailureAbilityScoreFactor = map.getValue("spellfailureabilityscorefactor", 0);
		spellFailureCasterLevelFactor = map.getValue("spellfailurecasterlevelfactor", 0);
		
		for (LineKeyList line : map.get("addprereq")) {
			prereqs.addPrereq(line);
		}
		
		for (LineKeyList line : map.get("level")) {
			int level = line.nextInt();
			String type = line.next().toLowerCase();
			if (type.equals("addcasterlevel")) {
				casterLevels[level - 1]++;
			} else if (type.equals("addability")) {
				String abilityID = line.next();
				
				Ability ability = Game.ruleset.getAbility(abilityID);
				if (ability == null) {
					throw new NullPointerException("Ability ID \"" + abilityID + "\" not found while reading " + this.id);
				}
				
				levelUpLists.get(level - 1).abilities.add(ability.getID());
			} else if (type.equals("selectabilityfromlist")) {
				levelUpLists.get(level - 1).abilitySelectionLists.add(line.next());
			} else if (type.equals("addabilityslot")) {
				levelUpLists.get(level - 1).abilitySlots.add(line.next());
			} else {
				Logger.appendToWarningLog("Invalid key " + type + " in " + line.getFilePath() +
						" on " + line.getLineNumber());
			}
		}
		
		LineKeyList line = map.getLast("defaultplayerattributeselections");
		if (line == null) this.defaultPlayerAttributeSelections = null;
		else {
			this.defaultPlayerAttributeSelections = new int[6];
			for (int i = 0; i < 6; i++) {
				this.defaultPlayerAttributeSelections[i] = line.nextInt();
			}
		}
		
		map.checkUnusedKeys();
	}
	
	public int[] getDefaultPlayerAttributeSelections() {
		if (defaultPlayerAttributeSelections == null) return null;
		
		return Arrays.copyOf(defaultPlayerAttributeSelections, 6);
	}
	
	public int getLevel1HP() { return level1HP; }
	
	public int getCasterLevelAddedAtLevel(int level) { return casterLevels[level - 1]; }
	
	public String getDescription() {
		return ResourceManager.getResourceAsString(descriptionFile);
	}
	
	public Stat getSpellCastingAttribute() { return spellCastingAttribute; }
	
	public int getSpellFailureBase() { return spellFailureBase; }
	public int getSpellFailureSpellLevelFactor() { return spellFailureSpellLevelFactor; }
	public int getSpellFailureAbilityScoreFactor() { return spellFailureAbilityScoreFactor; }
	public int getSpellFailureCasterLevelFactor() { return spellFailureCasterLevelFactor; }
	
	public boolean isBaseRole() { return baseRole; }
	public boolean isPlayerSelectable() { return playerSelectable; }
	
	public String getID() { return id; }
	public String getName() { return name; }
	public String getIcon() { return icon; }
	public int getSkillsPerLevel() { return skillsPerLevel; }
	public int getHPPerLevel() { return hpPerLevel; }
	public int getAttackBonusPerLevel() { return attackBonusPerLevel; }
	public int getDamageBonusPerLevel() { return damageBonusPerLevel; }
	
	public void appendDescription(StringBuilder sb) {
		sb.append("<div style=\"font-family: large-red;\">");
		sb.append(name).append("</div>");
		
		sb.append("<div style=\"font-family: vera\">");
		if (baseRole) sb.append("Base Role");
		else sb.append("Specialization");
		sb.append("</div>");
		
		
		prereqs.appendDescription(sb);
		
		sb.append("<div style=\"font-family: vera-bold-blue; margin-top: 1em;\">");
		sb.append("Stats");
		sb.append("</div>");
		sb.append("<table>");
		
		if (baseRole) {
			sb.append("<tr><td style=\"text-align:right; width:4ex;\"><span style=\"font-family: blue;\">");
			sb.append(level1HP);
			sb.append("</span></td><td style=\"margin-left: 1ex;\"> Hit Points at first level</td></tr>");
		}

		sb.append("<tr style=\"margin-bottom: 1em;\">");
		sb.append("<td style=\"text-align:right; width:4ex;\">+<span style=\"font-family: blue;\">");
		sb.append(hpPerLevel);
		sb.append("</span></td><td style=\"margin-left: 1ex;\">Hit Points at each level</td></tr>");

		sb.append("<tr><td style=\"text-align:right; width:4ex;\">+<span style=\"font-family: blue;\">");
		sb.append(attackBonusPerLevel);
		sb.append("</span></td><td style=\"margin-left: 1ex;\">Attack Bonus per level</td></tr>");
		
		sb.append("<tr style=\"margin-bottom: 1em\">");
		sb.append("<td style=\"text-align:right; width: 4ex;\">+<span style=\"font-family: blue;\">");
		sb.append(damageBonusPerLevel);
		sb.append("</span></td><td style=\"margin-left: 1 ex;\">Damage Bonus per level</td></tr>");
		
		sb.append("<tr><td style=\"text-align:right; width:4ex;\">+<span style=\"font-family: blue;\">");
		sb.append(skillsPerLevel);
		sb.append("</span></td><td style=\"margin-left: 1ex;\"> Skill Points per level</td></tr>");
		
        sb.append("</table>");
		
		sb.append(getDescription());
	}
	
	public boolean creatureCanAdd(Creature c) {
		if (c == null) return false;
		
		if (!prereqs.meetsPrereqs(c)) return false;
		
		if (baseRole && c.getRoles().getBaseRole() != null) {
			if (c.getRoles().getBaseRole() != this) return false;
		}
		
		return true;
	}
	
	/**
	 * Returns true if this role is a specialization and the specified creature
	 * meets role requirements (but not neccessarily the needed role level, or any other requirements)
	 * for this role
	 * @param creature the creature to test against
	 * @return whether this is a specialization for the specified base role
	 */
	
	public boolean creatureHasRolePrereqs(Creature creature) {
		if (this.baseRole) return false;
		
		return prereqs.hasRolePrereqs(creature);
	}
	
	/**
	 * Returns the List of all Abilities that should be added to a Creature
	 * upon gaining the specified level of this Role.  If their are no Abilities
	 * to be added, the List will be empty.
	 * 
	 * @param level the level that has been gained
	 * @return the List of Abilities to add
	 */
	
	public List<Ability> getAbilitiesAddedAtLevel(int level) {
		List<String> abilityIDs = this.levelUpLists.get(level - 1).abilities;
		
		List<Ability> abilities = new ArrayList<Ability>(abilityIDs.size());
		for (String id : abilityIDs) {
			abilities.add(Game.ruleset.getAbility(id));
		}
		
		return abilities;
	}
	
	/**
	 * Returns the List of all AbilitySlots that should be added to a Creature
	 * upon gaining the specified level of this Role.  If their are no AbilitySlots
	 * to be added, the List will be empty.  Note that this List does not include
	 * AbilitySlots added due to fixed Abilities being added
	 * 
	 * @param level the level that has been gained
	 * @param parent the parent Creature; owner of the AbilitySlots to be added
	 * @return the List of AbilitySlots to add
	 */
	
	public List<AbilitySlot> getAbilitySlotsAddedAtLevel(int level, Creature parent) {
		List<String> types = this.levelUpLists.get(level - 1).abilitySlots;
		
		List<AbilitySlot> slots = new ArrayList<AbilitySlot>(types.size());
		for (String type : types) {
			slots.add(new AbilitySlot(type, parent));
		}
		
		return slots;
	}
	
	/**
	 * For each AbilitySelectionList in the returned List, the Creature gaining
	 * the level specified of this Role should pick one Ability from that
	 * AbilitySelectionList.  If their are no selections to be made, the List
	 * will be empty.
	 * 
	 * @param level the role level that has been gained
	 * @return the List of AbilitySelectionLists to choose abilities from
	 */
	
	public List<AbilitySelectionList> getAbilitySelectionsAddedAtLevel(int level) {
		List<String> listIDs = this.levelUpLists.get(level - 1).abilitySelectionLists;
		
		List<AbilitySelectionList> lists = new ArrayList<AbilitySelectionList>(listIDs.size());
		for (String id : listIDs) {
			lists.add(Game.ruleset.getAbilitySelectionList(id));
		}
		
		return lists;
	}
	
	/**
	 * Returns a set containing all AbilitySelectionLists that are referenced at any
	 * level within this Role
	 * @return the set of AbilitySelectionLists
	 */
	
	public Set<AbilitySelectionList> getAllReferencedAbilitySelectionLists() {
		Set<AbilitySelectionList> lists = new LinkedHashSet<AbilitySelectionList>();
		
		for (LevelUpList levelUpList : levelUpLists) {
			for (String id : levelUpList.abilitySelectionLists) {
				AbilitySelectionList list = Game.ruleset.getAbilitySelectionList(id);
				lists.add(list);
			}
		}
		
		return lists;
	}
	
	/**
	 * A list containing new abilities, ability slots, and bonuses gained when
	 * reaching a given level in this Role
	 * 
	 * @author Jared Stephen
	 *
	 */
	
	private class LevelUpList {
		private List<String> abilitySlots;
		private List<String> abilitySelectionLists;
		private List<String> abilities;
		
		private LevelUpList() {
			this.abilitySlots = new ArrayList<String>(1);
			this.abilitySelectionLists = new ArrayList<String>(1);
			this.abilities = new ArrayList<String>(1);
		}
	}
}