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
import net.sf.hale.AreaTransition;
import net.sf.hale.AreaTrigger;
import net.sf.hale.Game;

/**
 * A {@link ReferenceList} for an Area object.
 * 
 * @author Jared Stephen
 *
 */

public class AreaReferenceList implements ReferenceList {
	private final Area area;
	
	/**
	 * Create a new ReferenceList with the specified Area
	 * 
	 * @param area the Area to create this list with
	 */
	
	public AreaReferenceList(Area area) {
		this.area = area;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the Area
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the Area specified at the creation
	 * of this ReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Area: " + this.area.getID());
		
		// check the start Area of this campaign
		if (this.area.getID().equals(Game.curCampaign.getStartArea())) {
			references.add("Campaign Start Area: " + Game.curCampaign.getID());
		}
		
		// check the area associated with all triggers
		for (int i = 0; i < Game.campaignEditor.getTriggersModel().getNumEntries(); i++) {
			AreaTrigger trigger = Game.campaignEditor.getTriggersModel().getEntry(i);
			if (trigger.getArea().equals(this.area.getName())) {
				references.add("Trigger: " + trigger.getID());
			}
		}
		
		// check the to and from areas for all AreaTransitions
		for (int i = 0; i < Game.campaignEditor.getTransitionsModel().getNumEntries(); i++) {
			AreaTransition transition = Game.campaignEditor.getTransitionsModel().getEntry(i);
			
			if (transition.getAreaFrom().equals(area.getName())) {
				references.add("Area From for Transition: " + transition.getName());
			}
			
			if (transition.getAreaTo().equals(area.getName())) {
				references.add("Area to for Transition: " + transition.getName());
			}
		}
		
		return references;
	}
}
