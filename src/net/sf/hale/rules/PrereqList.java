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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;

/**
 * A List of Prerequisites that a Creature must meet prior to being able to
 * select a Role, Ability, or anything else with prereqs.  The list can include
 * required skill ranks, stats, proficiencies, roles, and abilities.
 * 
 * @author Jared Stephen
 *
 */

public class PrereqList {
	private final SkillSet skillPrereqs;
	private final List<String> abilityPrereqs;
	
	private final Map<Stat, Integer> statPrereqs;
	
	private final List<String> weaponProficiencyPrereqs;
	private final List<String> armorProficiencyPrereqs;
	
	// unlike all the others these are implemented as an OR condition,
	// meaning the creature must have at least one of the specified roles
	private final List<String> rolePrereqs;
	private final List<Integer> roleLevelPrereqs;
	
	/**
	 * Creates a new, empty PrereqList.  All Creatures meet the
	 * requirements of an empty PrereqList
	 */
	
	public PrereqList() {
		skillPrereqs = new SkillSet();
		abilityPrereqs = new ArrayList<String>();
		statPrereqs = new HashMap<Stat, Integer>();
		
		weaponProficiencyPrereqs = new ArrayList<String>();
		armorProficiencyPrereqs = new ArrayList<String>();
		
		rolePrereqs = new ArrayList<String>();
		roleLevelPrereqs = new ArrayList<Integer>();
	}
	
	private void addSkillPrereq(String skillID, int ranks) {
		skillPrereqs.addRanks(skillID, ranks);
	}
	
	private void addAbilityPrereq(String abilityID) {
		abilityPrereqs.add(abilityID);
	}
	
	private void addWeaponProficiencyPrereq(String baseWeapon) {
		weaponProficiencyPrereqs.add(baseWeapon);
	}
	
	private void addArmorProficiencyPrereq(String armorType) {
		armorProficiencyPrereqs.add(armorType);
	}
	
	private void addStatPrereq(String statString, int value) {
		Stat stat = Stat.valueOf(statString);
		if (stat == null) {
			Logger.appendToErrorLog("Error.  Stat prereq " + statString + " not found.");
			return;
		}
		
		statPrereqs.put(stat, value);
		
	}
	
	private void addRolePrereq(String role, int level) {
		rolePrereqs.add(role);
		roleLevelPrereqs.add(level);
	}
	
	/**
	 * Adds a new prerequisite from the specified LineKeyList.  The
	 * LineKeyList must contain a list of tokens specifying the prereq.
	 * These keys are read off using the {@link net.sf.hale.util.LineKeyList#next()}
	 * method.  The first token must be one of "skill", "ability", "role", "stat",
	 * "weaponproficiency", or "armorproficiency".  Case is ignored for this token.
	 * The next token must then specify the String ID of the skill, ability, role, stat,
	 * weapon type, or armor type to be required.  For skill, role, and stat, a third
	 * token is required, representing the integer value of the skill, role, or stat.
	 * 
	 * @param sLine the LineKeyList that the tokens representing the prereq to add
	 * will be read off of
	 */
	
	public void addPrereq(LineKeyList sLine) {
		String type = sLine.next().toLowerCase();
		if (type.equals("skill")) {
			addSkillPrereq(sLine.next(), sLine.nextInt());
		} else if (type.equals("ability")) {
			addAbilityPrereq(sLine.next());
		} else if (type.equals("role")) {
			addRolePrereq(sLine.next(), sLine.nextInt());
		} else if (type.equals("stat")) {
			addStatPrereq(sLine.next(), sLine.nextInt());
		} else if (type.equals("weaponproficiency")) {
			addWeaponProficiencyPrereq(sLine.next());
		} else if (type.equals("armorproficiency")) {
			addArmorProficiencyPrereq(sLine.next());
		} else {
			Logger.appendToWarningLog("Prereq type " + type + " not found in " +
					sLine.getFilePath() + " on line " + sLine.getLineNumber());
		}
	}
	
	/**
	 * Returns true if and only if the specified creature has at least one level of one of the
	 * roles in this prereq list.  The creature need not meet the actual role prereq, or any other
	 * prereqs.  This method will return true if this prereq list contains no role prereqs.
	 * @param c the creature to check
	 * @return whether this creature has at least one level in any role prereq in this list
	 */
	
	public boolean hasRolePrereqs(Creature c) {
		if (rolePrereqs.size() == 0) return true;
		
		for (int i = 0; i < rolePrereqs.size(); i++) {
			Role cc = Game.ruleset.getRole(rolePrereqs.get(i));
			
			if (cc != null) {
				if (c.getRoles().getLevel(cc) >= 1) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if and only if the specified creature meets the role prereqs of this
	 * prereq list.  See {@link #meetsPrereqs(Creature)} for how the role prereqs are defined.
	 * Note that if this prereq list contains no role prereqs, then this method will return true
	 * @param c the creature to check
	 * @return whether the creature meets the role prereqs
	 */
	
	public boolean meetsRolePrereqs(Creature c) {
		if (rolePrereqs.size() == 0) return true;
		
		for (int i = 0; i < rolePrereqs.size(); i++) {
			Role cc = Game.ruleset.getRole(rolePrereqs.get(i));
			int level = roleLevelPrereqs.get(i);
			
			if (cc != null) {
				if (c.getRoles().getLevel(cc) >= level) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if and only if the specified Creature meets all prereqs in this
	 * prereq list.  This includes having at least the specified ranks in each
	 * skill prereq, possessing each required ability, having at least the specified
	 * stat in all stat prereqs, having all weapon and armor proficiencies, and
	 * having at least the required number of levels in at least one of the role
	 * prereqs.
	 * 
	 * Note that while all other prereqs are implemented as an "AND" condition (meaning
	 * all individual prereqs must be met), the role prereq is implemented as an "OR"
	 * condition, meaning only one of possibly several role prereqs must be met.  All other
	 * types of prereqs must still be met in this case.
	 * 
	 * @param c the creature to check prereqs against
	 * @return true if and only if the creature meets all prereqs.
	 */
	
	public boolean meetsPrereqs(Creature c) {
		for (Stat stat : statPrereqs.keySet()) {
			if (c.stats().get(stat) < statPrereqs.get(stat)) return false;
		}
		
		for (String skillID : skillPrereqs.getSkills()) {
			int ranksRequired = skillPrereqs.getRanks(skillID);
			if (c.getSkillSet().getRanks(skillID) < ranksRequired) return false;
		}
		
		if (!meetsRolePrereqs(c)) return false;
		
		for (String abilityID : abilityPrereqs) {
			if (!c.getAbilities().has(abilityID)) return false;
		}
		
		for (String s : weaponProficiencyPrereqs) {
			if (!c.stats().hasWeaponProficiency(s)) return false;
		}
		
		for (String s : armorProficiencyPrereqs) {
			if (!c.stats().hasArmorProficiency(s)) return false;
		}
					
		return true;
	}
	
	/**
	 * Returns true if and only if this PrereqList is empty, meaning
	 * that all Creatures will meet the PrereqList and that no
	 * Prereqs have been added.
	 * 
	 * @return true if and only if this PrereqList is empty
	 */
	
	public boolean isEmpty() {
		if (!weaponProficiencyPrereqs.isEmpty()) return false;
		if (!armorProficiencyPrereqs.isEmpty()) return false;
		if (!abilityPrereqs.isEmpty()) return false;
		if (!rolePrereqs.isEmpty()) return false;
		if (!statPrereqs.isEmpty()) return false;
		
		return (skillPrereqs.size() == 0);
	}
	
	/**
	 * Appends a String HTML description of this List of Prereqs to the
	 * specified StringBuilder.  This will include a mention of all
	 * required prereqs for this list to be met by a Creature.
	 * 
	 * @param sb the StringBuilder to append to
	 */
	
	public void appendDescription(StringBuilder sb) {
		if (isEmpty()) return;
		
		sb.append("<div style=\"font-family: vera-bold-blue; margin-top : 1em;\">Prereqs</div>");
		
		for (Stat stat : statPrereqs.keySet()) {
			sb.append("<p><span style=\"font-family: purple;\">").append(stat.toString());
			sb.append("</span> ").append(statPrereqs.get(stat)).append("</p>");
		}
		
		for (String skillID : skillPrereqs.getSkills()) {
			Skill skill = Game.ruleset.getSkill(skillID);
			
			if (skill == null)
				throw new NullPointerException("Skill " + skillID + " not found in prereq list.");
			
			sb.append("<p>").append(skillPrereqs.getRanks(skillID));
			sb.append(" ranks in <span style=\"font-family: blue;\">");
			sb.append(skill.getName()).append("</span></p>");
		}
		
		if (rolePrereqs.size() > 0)
			sb.append("<p>");
		
		for (int i = 0; i < rolePrereqs.size(); i++) {
			Role role = Game.ruleset.getRole(rolePrereqs.get(i));
			
			if (role == null)
				throw new NullPointerException("Role " + rolePrereqs.get(i) + " not found in prereq list");
			
			sb.append("Level <span style=\"font-family: red;\">");
			sb.append(roleLevelPrereqs.get(i)).append("</span> in ");
			sb.append("<span style=\"font-family: blue;\">");
			sb.append(role.getName()).append("</span>");
			
			if (i != rolePrereqs.size() - 1) 
				sb.append(" OR ");
			else
				sb.append("</p>");
		}
		
		for (String abilityID : abilityPrereqs) {
			Ability ability = Game.ruleset.getAbility(abilityID);
			
			if (ability == null)
				throw new NullPointerException("Ability " + abilityID + " not found in prereq list.");
			
			sb.append("<p>Ability: ");
			sb.append("<span style=\"font-family: orange;\">");
			sb.append(ability.getName()).append("</span></p>");
		}
		
		for (String s : weaponProficiencyPrereqs) {
			sb.append("<p>Weapon Proficiency: ");
			sb.append("<span style=\"font-family: red;\">").append(s).append("</span></p>");
		}
		
		for (String s : armorProficiencyPrereqs) {
			sb.append("<p>Armor Proficiency: ");
			sb.append("<span style=\"font-family: green;\">").append(s).append("</span></p>");
		}
	}
}
