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
 * A {@link ReferenceList} for a WorldMapLocation object.
 * 
 * @author Jared Stephen
 *
 */

public class LocationReferenceList implements ReferenceList {
	private final WorldMapLocation location;
	
	/**
	 * Create a new ReferenceList with the specified WorldMapLocation
	 * 
	 * @param location the WorldMapLocation to create this list with
	 */
	
	public LocationReferenceList(WorldMapLocation location) {
		this.location = location;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the WorldMapLocation
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the WorldMapLocation specified at the creation
	 * of this ReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("World Map Location: " + location.getID());
		
		// check each transition
		for (int i = 0; i < Game.campaignEditor.getTransitionsModel().getNumEntries(); i++) {
			AreaTransition transition = Game.campaignEditor.getTransitionsModel().getEntry(i);
			
			if ( location.getID().equals(transition.getWorldMapLocation()) ) {
				references.add("Transition: " + transition.getName());
			}
		}
		
		// look for references in the contents of Script Files
		List<GrepUtil.Entry> entries = GrepUtil.findStringInTextFiles("\"" + location.getName() + "\"",
				Game.campaignEditor.getPath() + "/scripts");
		
		for (GrepUtil.Entry entry : entries) {
			if (entry.getLine().contains("revealWorldMapLocation")) {
				references.add("Script: " + entry.getPath() + " on line " + entry.getLineNumber());
			}
		}
		
		return references;
	}
}
