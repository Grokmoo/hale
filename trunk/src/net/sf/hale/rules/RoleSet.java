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
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.entity.Creature;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * Stores the set of Roles and associated levels that a given Creature possesses.
 * 
 * @author Jared Stephen
 *
 */

public class RoleSet implements Saveable {
	private String baseRoleID;
	private final Map<String, Integer> roles;
	
	private int casterLevel;
	private int totalLevel;
	
	private Creature parent;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("baseRoleID", baseRoleID);
		
		for (String key : roles.keySet()) {
			int value = roles.get(key);
			
			if (value != 0) {
				data.put(key, value);
			}
		}
		
		return data;
	}
	
	public void load(SimpleJSONObject data, Creature parent) {
		clear();
		
		this.parent = parent;
		
		baseRoleID = data.get("baseRoleID", null);
		
		for (String roleID : data.keySet()) {
			if (roleID.equals("baseRoleID")) continue;
			
			Role role = Game.ruleset.getRole(roleID);
			int value = data.get(roleID, 0);
			
			// set the new level
			roles.put(roleID, value);
			
			// compute new total level
			totalLevel += value;

			// check each added level for new caster levels and new abilities
			for (int level = 1; level <= value; level++) {
				casterLevel += role.getCasterLevelAddedAtLevel(level);

				List<Ability> abilities = role.getAbilitiesAddedAtLevel(level);
				for (Ability ability : abilities) {
					parent.getAbilities().loadAbility(ability, level, true, false);
				}

				// do not load any slots as those are loaded separately
			}
		}
	}
	
	/**
	 * Creates a new, empty RoleSet with no roles, and 0 levels.
	 * @param parent the owning creature
	 */
	
	public RoleSet(Creature parent) {
		roles = new LinkedHashMap<String, Integer>();
		
		casterLevel = 0;
		totalLevel = 0;
		
		this.parent = parent;
	}
	
	/**
	 * Creates a new RoleSet that is an exact copy of the specified
	 * RoleSet
	 * @param other the RoleSet to copy
	 * @param parent the owning creature
	 */
	
	public RoleSet(RoleSet other, Creature parent) {
		this(parent);
		
		for (String roleID : other.roles.keySet()) {
			this.roles.put(roleID, other.roles.get(roleID));
		}
		
		this.casterLevel = other.casterLevel;
		this.totalLevel = other.totalLevel;
		
		this.baseRoleID = other.baseRoleID;
	}
	
	/**
	 * Adds all the role levels in the specified set to this RoleSet
	 * @param other the set to add role levels from
	 */
	
	public void addLevels(RoleSet other) {
		for (String roleID : other.roles.keySet()) {
			Role role = Game.ruleset.getRole(roleID);
			
			this.addLevels(role, other.roles.get(roleID));
		}
	}
	
	/**
	 * Adds the specified number of levels of the specified Role for the parent Creature.
	 * 
	 * @param roleID the Role to add.  If this is the first Role that has been added to this
	 * RoleSet, then it will be set as the base role.
	 * @param levelsToAdd the number of levels of the role to add
	 * method
	 */
	
	public void addLevels(String roleID, int levelsToAdd) {
		addLevels(Game.ruleset.getRole(roleID), levelsToAdd);
	}
	
	/**
	 * Adds the specified number of levels of the specified Role for the parent Creature.
	 * 
	 * @param role the Role to add.  If this is the first Role that has been added to this
	 * RoleSet, then it will be set as the base role.
	 * @param levelsToAdd the number of levels of the role to add
	 * method
	 */
	
	public void addLevels(Role role, int levelsToAdd) {
		if (levelsToAdd < 1) throw new IllegalArgumentException("Only a positive number of levels " +
				"may be added to a RoleSet.");
		
		if (baseRoleID == null) baseRoleID = role.getID();
		
		// compute what the new level will be
		int oldLevel;
		
		Integer integerLevel = roles.get(role.getID());
		if (integerLevel != null) oldLevel = integerLevel.intValue();
		else oldLevel = 0;
		
		int newLevel = oldLevel + levelsToAdd;
		
		// set the new level
		roles.put(role.getID(), newLevel);
		
		// compute new total level
		totalLevel += levelsToAdd;
		
		// check each added level for new caster levels and new abilities
		for (int level = oldLevel + 1; level <= newLevel; level++) {
			casterLevel += role.getCasterLevelAddedAtLevel(level);
			
			if (parent != null) {
				// add abilities first in case any of the slots are adding an ability that
				// is added at the same level
				List<Ability> abilities = role.getAbilitiesAddedAtLevel(level);
				for (Ability ability : abilities) {
					parent.getAbilities().addRoleAbility(ability, level);
				}
				
				List<AbilitySlot> slots = role.getAbilitySlotsAddedAtLevel(level, parent);
				for (AbilitySlot slot : slots) {
					parent.getAbilities().add(slot);
				}
			}
		}
		
		if (parent != null) parent.stats().recomputeAllStats();
	}
	
	/**
	 * Returns the number of levels of the Role with the specified ID in
	 * this RoleSet.  If the role is not present in this Set, returns 0.
	 * @param roleID the ID of the Role
	 * @return the number of levels of the Role
	 */
	
	public int getLevel(String roleID) {
		if (!roles.containsKey(roleID)) return 0;
		else return roles.get(roleID);
	}
	
	/**
	 * Returns the number of levels of the specified Role in
	 * this RoleSet.  If the role is not present in this Set, returns 0.
	 * @param role the Role
	 * @return the number of levels of the Role
	 */
	
	public int getLevel(Role role) {
		return getLevel(role.getID());
	}
	
	/**
	 * Returns the caster level obtained by adding all the caster levels of the
	 * roles in this RoleSet.
	 * @return the caster level of this RoleSet
	 */
	
	public int getCasterLevel() {
		return casterLevel;
	}
	
	/**
	 * Returns the total number of levels added to this RoleSet
	 * @return the total number of levels added to this RoleSet
	 */
	
	public int getTotalLevel() {
		return totalLevel;
	}
	
	/**
	 * Returns true if and only if levels for the specified Role have been added to
	 * this RoleSet
	 * @param role the Role to search for
	 * @return true if and only if this RoleSet contains the specified Role
	 */
	
	public boolean contains(Role role) {
		return roles.containsKey(role.getID());
	}
	
	/**
	 * Returns true if and only if levels for the Role with the specified ID
	 * have been added to this RoleSet
	 * @param roleID the ID of the Role to search for
	 * @return true if and only if this RoleSet contains the specified Role
	 */
	
	public boolean contains(String roleID) {
		return roles.containsKey(roleID);
	}
	
	/**
	 * Returns the base Role for this RoleSet.  This is the Role that was
	 * added first to the set.
	 * @return the base role for this RoleSet
	 */
	
	public Role getBaseRole() {
		if (baseRoleID == null) return null;
		
		return Game.ruleset.getRole(baseRoleID);
	}
	
	/**
	 * Removes all levels of all roles from this RoleSet.  After this
	 * method returns, this RoleSet will contain no roles, and
	 * total level and caster level will be 0.
	 */
	
	public void clear() {
		this.roles.clear();
		this.totalLevel = 0;
		this.casterLevel = 0;
		
		this.baseRoleID = null;
	}
	
	/**
	 * Returns the Set of the IDs of all the Roles contained within this RoleSet.  The
	 * returned set is unmodifiable.
	 * @return the Set of all Role IDs contained in this RoleSet
	 */
	
	public Set<String> getRoleIDs() {
		return Collections.unmodifiableSet(roles.keySet());
	}
}