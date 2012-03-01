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

package net.sf.hale.ability;

import net.sf.hale.Encounter;
import net.sf.hale.bonus.StatManager;
import net.sf.hale.rules.Faction;
import net.sf.hale.rules.RoundTimer;
import net.sf.hale.util.Point;

/**
 * An AbilityActivator is an Object capable of being used by a Spell or Ability
 * as the caster, or user.  Generally, creatures are spell casters, but
 * spells can also be cast from items.
 * 
 * @author Jared Stephen
 *
 */

public interface AbilityActivator {
	/**
	 * Returns the grid point position of this AbilityActivator
	 * @return the grid point position of this AbilityActivator 
	 */
	public Point getPosition();
	
	/**
	 * Returns the x grid coordinate of the position of this AbilityActivator
	 * within the Area.
	 * 
	 * @return the x grid coordinate of this AbilityActivator
	 */
	
	public int getX();
	
	/**
	 * Returns the y grid coordinate of the position of this AbilityActivator
	 * within the Area.
	 * 
	 * @return the y grid coordinate of this AbilityActivator
	 */
	
	public int getY();
	
	/**
	 * Returns the stat manager which handles all stats and bonuses for this
	 * AbilityActivator.
	 * 
	 * @return the stat manager for this AbilityActivator
	 */
	
	public StatManager stats();
	
	/**
	 * Returns the caster level for this AbilityActivator.  This is used by
	 * spells in determining the chance of casting failure and usually the
	 * spell's damage or power.  This is set by the role levels for
	 * Creatures.
	 * 
	 * @return the caster level for this AbilityActivator
	 */
	
	public int getCasterLevel();
	
	/**
	 * Returns the integer value of the primary attribute for the spell casting of
	 * this AbilityActivator.  10 is the default value.
	 * @return the primary attribute for spell casting for this AbilityActivator
	 */
	
	public int getSpellCastingAttribute();
	
	/**
	 * Returns the base spell failure for a Spell with the given spell level.
	 * This does not consider any effects that have been applied to the
	 * AbilityActivator or any equipment the AbilityActivator is wearing.  For a Creature,
	 * this value is based on the SpellFailure values set for the Creature's
	 * base role.
	 * 
	 * @param spellLevel the spell level of the spell to be cast
	 * @return the base failure rate for a spell of the specified level
	 */
	
	public int getBaseSpellFailure(int spellLevel);
	
	/**
	 * Returns the Faction that this SpellCaster is allied with.  For creatures,
	 * this is the Entity faction.
	 * 
	 * @return the Faction that this SpellCaster is allied with
	 */
	
	public Faction getFaction();
	
	/**
	 * Returns true if this AbilityActivator is immobilized and unable to cast
	 * any spell with a somatic component, false otherwise
	 * @return true if and only if this AbilityActivator is unable to cast
	 * any spell with a somatic component
	 */
	
	public boolean isImmobilized();
	
	/**
	 * Returns a String representing this AbilityActivator's name
	 * @return a String representing this AbilityActivator's name
	 */
	
	public String getName();
	
	/**
	 * Returns the Encounter that this AbilityActivator is a part of.  Encounters
	 * share information, such as the location of hostiles between each all
	 * members of the encounter.
	 * 
	 * @return the encounter that this AbilityActivator is a part of.
	 */
	
	public Encounter getEncounter();
	
	/**
	 * Returns the visibility matrix for this AbilityActivator for the current Area.
	 * 
	 * @return the visibility matrix for this AbilityActivator
	 */
	
	public boolean[][] getVisibility();
	
	/**
	 * Returns the RoundTimer that controls the Action Points (AP) available to this
	 * AbilityActivator.
	 * 
	 * @return the RoundTimer for this AbilityActivator
	 */
	
	public RoundTimer getTimer();
	
	/**
	 * Returns the set of Effects that are currently applied to this AbilityActivator
	 * 
	 * @return the set of Effects applied to this AbilityActivator
	 */
	
	public EntityEffectSet getEffects();
	
	/**
	 * Returns the CreatureAbilityList containing all the Abilities owned by this
	 * AbilityActivator.
	 * 
	 * @return the CreatureAbilityList of Abilities
	 */
	
	public CreatureAbilitySet getAbilities();
	
	/**
	 * Returns whether this AbilityActivator is selectable and usable as a player
	 * character by the player.
	 * 
	 * @return true if and only if this AbilityActivator is player selectable
	 */
	
	public boolean isPlayerSelectable();
}
