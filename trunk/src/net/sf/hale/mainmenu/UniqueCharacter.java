/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.mainmenu;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.entity.Creature;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;

/**
 * A unique character is a construct used in listing out and selecting a
 * character in the new game window.  Characters that are just different
 * level versions of each other are grouped together
 * @author Jared
 *
 */

public class UniqueCharacter implements Iterable<Creature> {
	private final List<Creature> creatures;
	
	private final String portraitID;
	private final String name;
	private final Ruleset.Gender gender;
	private final Race race;
	
	private int minLevel = 1;
	private int maxLevel = 30;
	
	/**
	 * Creates a new new UniqueCharacter with the specified creature as the base.
	 * Only creatures with matching portrait, name, gender, and race will be able to
	 * be added to this UniqueCharacter
	 * @param creature the base creature
	 */
	
	public UniqueCharacter(Creature creature) {
		this.creatures = new ArrayList<Creature>();
		
		creatures.add(creature);
		
		this.portraitID = creature.getPortrait();
		this.name = creature.getName();
		this.gender = creature.getGender();
		this.race = creature.getRace();
	}
	
	/**
	 * Returns the number of creatures contained in this UniqueCharacter
	 * @return the number of creatures
	 */
	
	public int size() {
		return creatures.size();
	}
	
	/**
	 * Sets the minimum and maximum level for valid creatures in this unique character.
	 * Note that this constraint does not prevent creatures from being added.
	 * @param min the minimum level constraint
	 * @param max the maximum level constraint
	 */
	
	public void setMinMaxLevel(int min, int max) {
		this.minLevel = min;
		this.maxLevel = max;
	}
	
	/**
	 * Returns true if and only if the specified creature meets the minimum and maximum level
	 * constraints for this unique character
	 * @param creature the creature to check
	 * @return true if the level constraints are met, false otherwise
	 */
	
	public boolean meetsLevelConstraints(Creature creature) {
		int level = creature.getRoles().getTotalLevel();
		
		return level >= this.minLevel && level <= this.maxLevel;
	}
	
	/**
	 * Returns the highest level creature that meets the minimum level and
	 * maximum level criterion from the list of creatures contained in this UniqueCharacter
	 * If no creature meets the criterion, returns null
	 * 
	 * @return the highest level creature
	 */
	
	public Creature getBestCreature() {
		int highestLevel = -1;
		Creature maxCreature = null;
		
		for (Creature creature : creatures) {
			int level = creature.getRoles().getTotalLevel();
			
			if (level > highestLevel && level >= this.minLevel && level <= this.maxLevel) {
				highestLevel = level;
				maxCreature = creature;
			}
		}
		
		return maxCreature;
	}
	
	/**
	 * Checks if the specified creature matches and can be added to this unique character.  If
	 * the creature matches, it is added to this UniqueCharacter
	 * @param creature the creature to check
	 * @return true if the creature matches, false otherwise
	 */
	
	public boolean addIfMatches(Creature creature) {
		if (!this.portraitID.equals(creature.getPortrait())) return false;
		
		if (!this.name.equals(creature.getName())) return false;
		
		if (this.gender != creature.getGender()) return false;
		
		if (this.race != creature.getRace()) return false;
		
		this.creatures.add(creature);
		
		return true;
	}
	
	/**
	 * Deletes the file corresponding to the specified creature on disk and removes
	 * the creature from this UniqueCharacter.  If the creature is not contained in this
	 * unique character, no action is performed
	 * @param creature the creature to delete
	 */
	
	public void deleteCreature(Creature creature) {
		int index = this.creatures.indexOf(creature);
		
		if (index == -1) return;
		
		String fileName = "characters/" + creature.getID() + ResourceType.Text.getExtension();
		
		new File(fileName).delete();
		
		this.creatures.remove(index);
		
		
	}

	@Override public Iterator<Creature> iterator() {
		return creatures.iterator();
	}
}
