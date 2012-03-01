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

package net.sf.hale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.Skill;

/**
 * Class for managing the set of recipes available in a given campaign
 * @author Jared Stephen
 *
 */

public class RecipeManager {
	private Map<String, Recipe> recipes;
	
	private Map<String, ArrayList<String>> recipesBySkill;
	
	/**
	 * Creates a new RecipeManager containing no recipes
	 */
	
	public RecipeManager() {
		recipes = new HashMap<String, Recipe>();
		recipesBySkill = new HashMap<String, ArrayList<String>>();
	}
	
	/**
	 * Returns the recipe contained in this RecipeManager with the specified ID.
	 * If no such recipe is found, attempts to create a new recipe using the specified
	 * ID as the resource ID.  If a recipe is found at the specified resource location,
	 * it is then loaded into this RecipeManager for future use.
	 * @param id the ID of the recipe to return
	 * @return the recipe found with the specified ID, or null if no recipe can be located
	 * with the ID or resource ID.
	 */
	
	public Recipe getRecipe(String id) {
		Recipe recipe = recipes.get(id);
		
		if (recipe == null) {
			return addRecipe(id);
		} else {
			return recipe;
		}
	}
	
	private Recipe addRecipe(String id) {
		Recipe recipe = new Recipe(id);
		
		// add the recipe to the main list
		recipes.put(id, recipe);
		
		ArrayList<String> recipesOfSkill = recipesBySkill.get(recipe.getSkill());
		// if the skill list does not exist, create it
		if (recipesOfSkill == null) {
			recipesOfSkill = new ArrayList<String>();
			recipesBySkill.put(recipe.getSkill(), recipesOfSkill);
		}
		
		recipesOfSkill.add(id);
		
		return recipe;
	}
	
	/**
	 * Loads all recipes contained in the standard resource location "recipes" into this
	 * RecipeManager.
	 */
	
	public void loadRecipes() {
		recipes.clear();
		recipesBySkill.clear();
		
		Set<String> resources = ResourceManager.getResourcesInDirectory("recipes");
		for (String resource : resources) {
			String id = ResourceManager.getResourceID(resource, "recipes", ResourceType.Text);
			if (id == null) continue;
			
			addRecipe(id);
		}
		
		for (String skillID : recipesBySkill.keySet()) {
			recipesBySkill.get(skillID).trimToSize();
			
			// sort recipes by skill requirement
			Collections.sort(recipesBySkill.get(skillID), new Comparator<String>() {
				@Override public int compare(String id1, String id2) {
					Recipe r1 = recipes.get(id1);
					Recipe r2 = recipes.get(id2);
					return r1.getSkillRequirement() - r2.getSkillRequirement();
				}
			});
		}
	}
	
	/**
	 * Returns a set containing all the recipe IDs currently registered with this
	 * RecipeManager.  Note that the returned Set is unmodifiable
	 * @return a set containing all recipe IDs stored in this RecipeManager
	 */
	
	public Set<String> getAllRecipeIDs() {
		return Collections.unmodifiableSet(recipes.keySet());
	}
	
	/**
	 * Returns a List of all Recipe IDs in this Recipe Manager used via the specified skill.
	 * Note that the returned list is unmodifiable.  If there are no such recipes, returns an
	 * empty List
	 * @param skill the Skill that all returned recipe IDs use
	 * @return the List of recipe IDs
	 */
	
	public List<String> getRecipeIDsForSkill(Skill skill) {
		List<String> recipesOfSkill = recipesBySkill.get(skill.getID());
		
		if (recipesOfSkill == null) return Collections.emptyList();
		else return Collections.unmodifiableList(recipesOfSkill);
	}
}
