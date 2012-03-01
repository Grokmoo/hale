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

import net.sf.hale.AreaTrigger;

/**
 * A {@link ReferenceList} for an AreaTrigger object.
 * 
 * @author Jared Stephen
 *
 */

public class TriggerReferenceList implements ReferenceList {
	private final AreaTrigger trigger;
	
	/**
	 * Create a new TriggerReferenceList for the specified trigger
	 * @param trigger the trigger to create this list with
	 */
	
	public TriggerReferenceList(AreaTrigger trigger) {
		this.trigger = trigger;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the AreaTrigger
	 * specified by this TriggerReferenceList.
	 * 
	 * @return the list of references to the AreaTrigger specified at the creation
	 * of this TriggerReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Trigger: " + trigger.getID());
		
		return references;
	}
}
