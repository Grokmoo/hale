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

import net.sf.hale.Recipe;

/**
 * A {@link ReferenceList} for a Recipe object.
 * 
 * @author Jared Stephen
 *
 */

public class RecipeReferenceList implements ReferenceList {
	private final Recipe recipe;
	
	/**
	 * Create a new ReferenceList with the specified recipe
	 * 
	 * @param recipe the recipe to create this list with
	 */
	
	public RecipeReferenceList(Recipe recipe) {
		this.recipe = recipe;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the Recipe
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the Recipe specified at the creation
	 * of this ReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Recipe: " + recipe.getID());
		
		return references;
	}
}