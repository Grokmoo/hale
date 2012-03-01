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

import net.sf.hale.Area;
import net.sf.hale.Encounter;
import net.sf.hale.Game;

/**
 * A {@link ReferenceList} for an Encounter object.
 * 
 * @author Jared Stephen
 *
 */

public class EncounterReferenceList implements ReferenceList {
	private final Encounter encounter;
	
	/**
	 * Create a new ReferenceList with the specified Encounter
	 * 
	 * @param encounter the encounter to create this list with
	 */
	
	public EncounterReferenceList(Encounter encounter) {
		this.encounter = encounter;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the Encounter
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the Encounter specified at the creation
	 * of this ReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Encounter: " + encounter.getID());
		
		// check all areas for instances of this encounter
		for (int i = 0; i < Game.campaignEditor.getAreasModel().getNumEntries(); i++) {
			Area area = Game.campaignEditor.getAreasModel().getEntry(i);
			
			for (int j = 0; j < area.getEncounters().size(); j++) {
				Encounter encounter = area.getEncounters().get(j);
				
				if (this.encounter.getID().equals(encounter.getID())) {
					references.add("Area: " + area.getName() + " at " + area.getEncounterPositions().get(j));
				}
			}
		}
		
		return references;
	}
}
