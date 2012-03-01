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

package net.sf.hale.editor.reference;

import java.util.LinkedList;
import java.util.List;

import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.util.GrepUtil;

/**
 * A {@link ReferenceList} for a Creature object.
 * 
 * @author Jared Stephen
 *
 */

public class CreatureReferenceList implements ReferenceList{
	private final Creature creature;
	
	/**
	 * Create a new ReferenceList with the specified Creature
	 * 
	 * @param creature the Creature to create this list with
	 */
	
	public CreatureReferenceList(Creature creature) {
		this.creature = creature;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the Creature
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the Creature specified at the creation
	 * of this ReferenceList in the current campaign.
	 */

	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Creature: " + this.creature.getID());
		
		// check the campaign starting character
		if (this.creature.getID().equals(Game.curCampaign.getStartingCharacter())) {
			references.add("Starting Character for Campaign: " + Game.curCampaign.getID());
		}
		
		// check all encounters for references
		for (int i = 0; i < Game.campaignEditor.getEncountersModel().getNumEntries(); i++) {
			Encounter encounter = Game.campaignEditor.getEncountersModel().getEntry(i);
			
			int count = 0;
			for (Creature creature : encounter.getBaseCreatures()) {
				if (this.creature.getID().equals(creature.getID())) {
					count++;
				}
			}
			
			if (count != 0) {
				references.add("Encounter: " + encounter.getName() + " (" + count + "x)");
			}
		}
		
		// check the contents of all scripts for references
		List<GrepUtil.Entry> entries = GrepUtil.findStringInTextFiles("\"" + this.creature.getID() + "\"",
				Game.campaignEditor.getPath() + "/scripts");
		
		for (GrepUtil.Entry entry : entries) {
			if (entry.getLine().contains("getEntityWithID")) {
				references.add("Script: " + entry.getPath() + " on line " + entry.getLineNumber());
			}
		}
		
		return references;
	}
}
