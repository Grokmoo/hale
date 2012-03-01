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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONObject;


/**
 * A Set of Skills and a corresponding integer number of ranks for each Skill.
 * 
 * @author Jared Stephen
 */

public class SkillSet implements Saveable {
	private final Map<String, Integer> skills;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		for (String key : skills.keySet()) {
			int value = skills.get(key);
			if (value != 0) {
				data.put(key, value);
			}
		}
		
		return data;
	}
	
	public void load(SimpleJSONObject data) {
		clear();
		
		for (String skillID : data.keySet()) {
			int value = data.get(skillID, 0);
			
			addRanks(skillID, value);
		}
	}
	
	/**
	 * Constructs an empty skill list with no ranks in any skill.
	 */
	
	public SkillSet() {
		this.skills = new LinkedHashMap<String, Integer>();
	}
	
	/**
	 * Constructs a SkillSet containing the same skills and ranks as the
	 * supplied SkillSet.
	 * @param other The SkillSet to copy the skills and ranks from.
	 */
	
	public SkillSet(SkillSet other) {
		this();
		for (String skillID : other.skills.keySet()) {
			this.skills.put(skillID, other.skills.get(skillID));
		}
	}
	
	/**
	 * Returns true if and only if the number of ranks for all skills in this
	 * skillset equals the number of ranks for all skills in the specified skillset
	 * @param other the SkillSet to compare this one to
	 * @return true if and only if the SkillSets are equal
	 */
	
	public boolean equals(SkillSet other) {
		if (other == null) return false;
		
		for (String skillID : skills.keySet()) {
			if ( !skills.get(skillID).equals(other.skills.get(skillID)) )
				return false;
		}
		
		for (String skillID : other.skills.keySet()) {
			if ( !other.skills.get(skillID).equals(skills.get(skillID)) )
				return false;
		}
		
		return true;
	}
	
	/**
	 * For each Skill in the supplied SkillSet, the ranks associated with that Skill
	 * are added to the Skill in this SkillSet.
	 * @param other The SkillSet to add ranks from.
	 */
	
	public void addRanksFromList(SkillSet other) {
		if (other == null) return;
		
		for (String skillID : other.skills.keySet()) {
			addRanks(skillID, other.skills.get(skillID));
		}
	}
	
	/**
	 * Returns the number of skills with ranks in this SkillSet
	 * @return the number of skills in this SkillSet
	 */
	
	public int size() { return skills.size(); }
	
	/**
	 * Add the specified number of ranks to the specified skill.
	 * 
	 * @param skillID The ID String for the {@link net.sf.hale.rules.Skill} 
	 * @param ranks The number of ranks to be added.
	 */
	
	public void addRanks(String skillID, int ranks) {
		if (Game.ruleset.getSkill(skillID) == null) {
			Logger.appendToErrorLog("Skill ID: " + skillID + " not found");
		} else {
			int curRanks = getRanks(skillID);
			
			skills.put(skillID, Integer.valueOf(curRanks + ranks));
		}
		
		
	}
	
	/**
	 * Sets the number of ranks for the specified skill.
	 * 
	 * @param skillID The ID String for the {@link Skill}
	 * @param ranks The number of ranks to be set
	 */
	
	public void setRanks(String skillID, int ranks) {
		if (Game.ruleset.getSkill(skillID) == null) {
			Logger.appendToErrorLog("Skill ID: " + skillID + " not found");
		} else {
			skills.put(skillID, Integer.valueOf(ranks));
		}
	}
	
	/**
	 * Adds the specified number of ranks to the specified Skill.
	 * 
	 * @param skill The Skill to have ranks added
	 * @param ranks The number of ranks to add.
	 */
	
	public void addRanks(Skill skill, int ranks) {
		addRanks(skill.getID(), ranks);
	}
	
	/**
	 * Returns true if and only if this SkillSet contains a non zero
	 * number of ranks for the specified Skill.
	 * 
	 * @param skill the Skill to check
	 * @return true if and only if this SkillSet contains ranks for the
	 * specified Skill
	 */
	
	public boolean hasRanks(Skill skill) {
		return hasRanks(skill.getID());
	}
	
	/**
	 * Returns true if and only if this SkillSet contains a non zero
	 * number of ranks for the Skill with the specified ID.
	 * 
	 * @param skillID the ID of the Skill to check
	 * @return true if and only if this SkillSet contains ranks for the
	 * specified Skill
	 */
	
	public boolean hasRanks(String skillID) {
		Integer ranks = this.skills.get(skillID);
		if (ranks == null) return false;
		
		return (ranks.intValue() != 0);
	}
	
	/**
	 * Returns the number of ranks for the Skill with the specified ID.  If the
	 * Skill has never had ranks added to this SkillSet, returns 0.
	 * 
	 * @param skillID the ID String of the Skill.
	 * @return the number of ranks for the Skill with the specified I
	 */
	
	public int getRanks(String skillID) {
		Integer ranks = this.skills.get(skillID);
		if (ranks == null) return 0;
		
		return ranks.intValue();
	}
	
	/**
	 * Returns the number of ranks for the specified SKill.  If the
	 * Skill has never had ranks added to this SkillSet, returns 0.
	 * 
	 * @param skill the Skill to get ranks for
	 * @return the number of ranks for the Skill with the specified I
	 */
	
	public int getRanks(Skill skill) {
		return getRanks(skill.getID());
	}
	
	/**
	 * Set the ranks of all Skills in this Set to 0.
	 */
	
	public void clear() {
		skills.clear();
	}
	
	/**
	 * Returns the set of Skill IDs that have been added to this SkillSet
	 * via addRanks, setRanks, etc.  It is possible that some of these
	 * Skills will have zero ranks.  The set is unmodifiable
	 * 
	 * @return the set of Skill IDs that have been added to this SkillSet.
	 */
	
	public Set<String> getSkills() {
		return Collections.unmodifiableSet(skills.keySet());
	}
}
