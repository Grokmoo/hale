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

/**
 * A Referenceable is an object (such as an Area, Creature, Encounter, Item, ...)
 * in the editor that has a method for getting a list
 * of all references to itself in a given Campaign.
 * 
 * This allows the user of the Campaign Editor to very quickly and easy locate
 * all the uses of a given object.
 * 
 * @author Jared
 *
 */

public interface Referenceable {
	/**
	 * Returns a list of all references to this object in the current campaign.
	 * This list is then displayed by either ReferenceListBox or ReferenceComboBox
	 * typically, so that the user can easily see all places where a given
	 * Referenceable is being used in the campaign.
	 * 
	 * @return a list of all references to this object in the current campaign
	 */
	
	public ReferenceList getReferenceList();
	
	/**
	 * Returns a string for this general type of referenceable
	 * @return a string for this type
	 */
	
	public String getReferenceType();
	
	/**
	 * Returns a unique ID String for this object.
	 * @return a unique ID string for this object
	 */
	
	public String getID();
}
