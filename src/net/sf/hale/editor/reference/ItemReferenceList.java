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
import net.sf.hale.Game;
import net.sf.hale.Recipe;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Openable;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.Race;
import net.sf.hale.util.GrepUtil;

/**
 * A {@link ReferenceList} for an Item object.
 * 
 * @author Jared Stephen
 *
 */

public class ItemReferenceList implements ReferenceList {
	private final Item item;
	
	/**
	 * Create a new ReferenceList with the specified Item
	 * 
	 * @param item the Item to create this list with
	 */
	
	public ItemReferenceList(Item item) {
		this.item = item;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the Item
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the Item specified at the creation
	 * of this ReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Item: " + this.item.getID());
		
		// check all areas for this item being added directly to the area
		for (int i = 0; i < Game.campaignEditor.getAreasModel().getNumEntries(); i++) {
			Area area = Game.campaignEditor.getAreasModel().getEntry(i);
			
			for (Entity entity : area.getEntities()) {
				if (entity.getID().equals(this.item.getID())) {
					references.add("Area: " + area.getName() + " at (" + entity.getX() + ", " + entity.getY() + ")");
				}
				
				// check each container in the area to see if it contains this item
				if (entity.getType() == Entity.Type.CONTAINER) {
					Container container = (Container) entity;
					
					if (container.getItems().containsItem(this.item.getID())) {
						references.add("Area: " + area.getName() + " in container at (" + container.getX() + ", " + container.getY() + ")");
					}
				}
			}
		}
		
		// check all other items to see if this item is the key for any of those items
		for (int i = 0; i < Game.campaignEditor.getItemsModel().getNumEntries(); i++) {
			Item item = Game.campaignEditor.getItemsModel().getEntry(i);
			
			if (item.getType() == Entity.Type.CONTAINER || item.getType() == Entity.Type.DOOR) {
				Openable openable = (Openable) item;
				
				if (this.item.getID().equals(openable.getKey())) {
					references.add("Key for Openable: " + openable.getID());
				}
				
			}
		}
		
		// check all itemLists to see if they contain this item
		for (int i = 0; i < Game.campaignEditor.getItemListsModel().getNumEntries(); i++) {
			ItemList itemList = Game.campaignEditor.getItemListsModel().getEntry(i);
			if (itemList.findItem(this.item.getID()) != -1) {
				references.add("Item List: " + itemList.getID());
			}
		}
		
		// check all creature inventories to see if they contain this item
		for (int i = 0; i < Game.campaignEditor.getCreaturesModel().getNumEntries(); i++) {
			Creature creature = Game.campaignEditor.getCreaturesModel().getEntry(i);
			
			for (Item item : creature.getInventory().getEquippedItems()) {
				if (item == null) continue;
				
				if (this.item.getID().equals(item.getID())) {
					references.add("Equipped Item for Creature: " + creature.getID());
				}
			}
			
			ItemList ownedItems = creature.getInventory().getUnequippedItems();
			if (ownedItems.findItem(this.item) != -1) {
				references.add("Owned Item for Creature: " + creature.getID());
			}
		}
		
		// check all recipes to see if the result or any of the ingredients is this item
		for (String recipeID : Game.curCampaign.getAllRecipeIDs()) {
			Recipe recipe = Game.curCampaign.getRecipe(recipeID);
			
			String result = recipe.getResult();
			
			if (this.item.getID().equals(result)) {
				references.add("Result of Recipe: " + recipe.getID());
			}
			
			ItemList ingredients = recipe.getIngredients();
			if (ingredients.findItem(this.item.getID()) != -1) {
				references.add("Ingredient for Recipe: " + recipe.getID());
			}
		}
		
		// check for references to this item in the contents of all script files
		List<GrepUtil.Entry> entries = GrepUtil.findStringInTextFiles("\"" + this.item.getID() + "\"",
				Game.campaignEditor.getPath() + "/scripts");
		
		for (GrepUtil.Entry entry : entries) {
			if (entry.getLine().contains("getEntityWithID") || entry.getLine().contains("addItem")) {
				references.add("Script: " + entry.getPath() + " on line " + entry.getLineNumber());
			}
		}
		
		// check for default racial weapons
		if (this.item.getItemType() == Item.ItemType.WEAPON) {
			for (Race race : Game.ruleset.getAllRaces()) {
				if ( race.getDefaultWeapon().getID().equals(this.item.getID()) ) {
					references.add("Default Weapon for Race: " + race.getID() + " (This entry can not be removed)");
				}
			}
		}
		
		return references;
	}
}