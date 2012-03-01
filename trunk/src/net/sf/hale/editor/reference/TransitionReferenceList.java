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

import net.sf.hale.AreaTransition;
import net.sf.hale.Game;
import net.sf.hale.WorldMapLocation;
import net.sf.hale.util.GrepUtil;

/**
 * A {@link ReferenceList} for an AreaTransition object.
 * 
 * @author Jared Stephen
 *
 */

public class TransitionReferenceList implements ReferenceList {
	private final AreaTransition transition;
	
	/**
	 * Create a new TransitionReferenceList with the specified transition
	 * 
	 * @param transition the transition to create this list with
	 */
	
	public TransitionReferenceList(AreaTransition transition) {
		this.transition = transition;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the AreaTransition
	 * specified by this TransitionReferenceList.
	 * 
	 * @return the list of references to the AreaTransition specified at the creation
	 * of this TransitionReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Area Transition: " + transition.getID());
		
		// check all world map locations for their starting transition
		for (int i = 0; i < Game.campaignEditor.getLocationsModel().getNumEntries(); i++) {
			WorldMapLocation location = Game.campaignEditor.getLocationsModel().getEntry(i);

			if (this.transition.getID().equals(location.getAreaTransition())) {
				references.add("Starting Transition for Location: " + location.getID());
			}
		}
		
		// search for all references to the AreaTransition in scripts
		List<GrepUtil.Entry> entries = GrepUtil.findStringInTextFiles("\"" + transition.getID() + "\"",
				Game.campaignEditor.getPath() + "/scripts");
		
		for (GrepUtil.Entry entry : entries) {
			if (entry.getLine().contains("activateTransition")) {
				references.add("Script: " + entry.getPath() + " on line " + entry.getLineNumber());
			}
		}
		
		return references;
	}
}
