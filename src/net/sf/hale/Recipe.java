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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.hale.editor.reference.RecipeReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.entity.CreatedItem;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.ItemList;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;

public class Recipe implements Referenceable {
	private final String id;
	private String name;
	private String description;
	
	private Item.ItemType anyItemOfTypeIngredient;
	private final ItemList ingredients;
	private String result;
	private int resultQuantity;
	private boolean resultIsIngredient;
	private boolean resultHasQuality;
	
	private String skill;
	private int skillRankRequirement;
	
	private final List<Integer> qualitySkillModifierRequirements;
	private final List<String> enchantments;
	
	public Recipe(String id) {
		this.id = id;
		this.name = id;
		this.ingredients = new ItemList(id);
		this.qualitySkillModifierRequirements = new ArrayList<Integer>();
		this.enchantments = new ArrayList<String>();
		this.anyItemOfTypeIngredient = null;
		this.resultIsIngredient = false;
		this.resultHasQuality = true;
		this.resultQuantity = 1;
		
		readRecipeFile(id);
		
	}
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Item.ItemType getAnyItemOfTypeIngredient() { return anyItemOfTypeIngredient; }
	public void setAnyItemOfTypeIngredient(Item.ItemType type) { this.anyItemOfTypeIngredient = type; }
	
	public boolean resultIsIngredient() { return resultIsIngredient; }
	public void setResultIsIngredient(boolean resultIsIngredient) { this.resultIsIngredient = resultIsIngredient; }
	
	public String getEnchantment(int index) { return enchantments.get(index); }
	public void setEnchantment(int index, String value) { enchantments.set(index, value); }
	public int getQualityModifier(int index) { return qualitySkillModifierRequirements.get(index); }
	public void setQualityModifier(int index, int value) { qualitySkillModifierRequirements.set(index, value); }
	public int getNumQualityModifiers() { return qualitySkillModifierRequirements.size(); }
	public int getNumEnchantments() { return enchantments.size(); }
	
	public boolean resultHasQuality() { return resultHasQuality; }
	@Override
	public String getID() { return id; }
	public String getResult() { return result; }
	public int getResultQuantity() { return resultQuantity; }
	public String getSkill() { return skill; }
	public int getSkillRequirement() { return skillRankRequirement; }
	
	public int getNumIngredients() { return ingredients.size(); }
	public Item getIngredient(int index) { return ingredients.getItem(index); }
	public int getIngredientQuantity(int index) { return ingredients.getQuantity(index); }
	
	public ItemList getIngredients() { return ingredients; }
	
	public void setSkill(String skillID) { this.skill = skillID; }
	public void setSkillRequirement(int requirement) { this.skillRankRequirement = requirement; }
	public void setResult(String result) { this.result = result; }
	public void setResultQuantity(int quantity) { this.resultQuantity = quantity; }
	public void setResultHasQuality(boolean hasQuality) { this.resultHasQuality = hasQuality; }
	
	public void removeIngredient(int index) {
		ingredients.removeItem(index, ingredients.getQuantity(index));
	}
	
	public void addIngredient(String ingredient) {
		ingredients.addItem(ingredient);
	}
	
	public boolean canCraft() {
		for (int i = 0; i < ingredients.size(); i++) {
			if ( !Game.curCampaign.party.hasItem(ingredients.getItemID(i), ingredients.getQuantity(i)) ) {
				return false;
			}
		}
		
		return true;
	}
	
	private void removeIngredients() {
		// remove the ingredients
		for (int i = 0; i < ingredients.size(); i++) {
			Game.curCampaign.party.removeItem(ingredients.getItemID(i), ingredients.getQuantity(i));
		}
	}
	
	private int getQuality() {
		int skillModifier = Game.curCampaign.getBestPartySkillModifier(skill);
		// compute the quality level of the result
		
		if (resultHasQuality) {
			for (int i = qualitySkillModifierRequirements.size() - 1; i >= 0; i--) {
				if (skillModifier > qualitySkillModifierRequirements.get(i)) {
					return i;
				}
			}
		}
		
		String quality = Game.ruleset.getString("DefaultItemQuality");
		for (int i = 0; i < Game.ruleset.getNumItemQualities(); i++) {
			if (Game.ruleset.getItemQuality(i).getName().equals(quality)) {
				return i;
			}
		}
		
		return 0;
	}
	
	private void addMessage(Item item, int quantity, Creature parent, boolean equipped) {
		String message = item.getFullName() + " crafted and ";
		
		if (equipped)
			message += "equipped by " + parent.getName() + ".";
		else
			message += "in inventory of " + parent.getName() + ".";
		
		if (quantity != 1)
			message = quantity + "x " + message;
		
		Game.mainViewer.addMessage(message);
		
		Game.mainViewer.updateInterface();
	}
	
	/**
	 * Craft this recipe, using the specified item as the base item.  This method may only
	 * be called on recipes where {@link #resultIsIngredient()} is true
	 * @param selectedItem the item to use as the result
	 * @param owner the owning creature
	 * @param isEquipped whether the specified item is equipped (true) or not equipped (false)
	 */
	
	public void craft(Item selectedItem, Creature owner, boolean isEquipped) {
		if (!resultIsIngredient)
			throw new IllegalArgumentException("Must not call craft with a specified item for recipe " + getID());
		
		int qualityIndex = getQuality();
		
		removeIngredients();
		
		String enchantment = this.getEnchantment(qualityIndex);
		if (enchantment != null && enchantment.length() > 0) {
			String originalID = selectedItem.getID();
			String baseID = selectedItem.getID();
			
			// if the base item is actually a created item
			if (Game.curCampaign.getCreatedItem(baseID) != null) {
				baseID = Game.curCampaign.getCreatedItem(baseID).getBaseItemID();
			}
			
			selectedItem.createEnchantment(enchantment);
			selectedItem.setID( baseID + "-0x" + Integer.toHexString(enchantment.hashCode()) );
			
			CreatedItem createdItem = new CreatedItem(baseID, selectedItem);
			
			Game.curCampaign.addCreatedItem(createdItem);
			
			// if the item is equipped, we have already done the modification
			// otherwise, we modified a copy and need to add the new item
			if (!isEquipped) {
				owner.getInventory().getUnequippedItems().removeItem(originalID, 1);
				owner.getInventory().getUnequippedItems().addItem(selectedItem);
			}
			
			addMessage(selectedItem, 1, owner, isEquipped);
		}
	}
	
	/**
	 * Crafts this recipe.  This method may only be called on recipes where {@link #resultIsIngredient()} is
	 * false
	 */
	
	public void craft() {
		if (!canCraft()) return;
		
		if (resultIsIngredient)
			throw new IllegalArgumentException("Must call craft with a specified item for recipe " + id);
		
		Creature target = Game.curCampaign.party.getSelected();
		
		int qualityIndex = getQuality();
		
		removeIngredients();

		// add the new item
		Item item = Game.entityManager.getItem(result);
		item.setQuality(Game.ruleset.getItemQuality(qualityIndex));
		int quantity = Math.max(1, resultQuantity);

		target.getInventory().addItem(item, quantity);

		addMessage(item, quantity, target, false);
	}
	
	private void readRecipeFile(String id) {
		FileKeyMap keyMap = new FileKeyMap("recipes/" + id + ResourceType.Text.getExtension());
		
		name = keyMap.getValue("name", null);
		skill = keyMap.getValue("skill", null);
		skillRankRequirement = keyMap.getValue("skillrequirement", 0);
		resultIsIngredient = keyMap.getValue("resultisingredient", false);
		resultHasQuality = keyMap.getValue("resulthasquality", true);
		result = keyMap.getValue("result", null);
		resultQuantity = keyMap.getValue("resultquantity", 1);
		description = keyMap.getValue("description", null);
		
		String anyItemIngredientString = keyMap.getValue("anyitemoftypeingredient", null);
		if (anyItemIngredientString != null)
			this.anyItemOfTypeIngredient = Item.ItemType.valueOf(anyItemIngredientString);
		
		for (LineKeyList line : keyMap.get("ingredient")) {
			ingredients.addItem(line.next(), line.nextInt());
		}
		
		for (LineKeyList line : keyMap.get("qualitymodifier")) {
			qualitySkillModifierRequirements.add(line.nextInt());
			if (line.hasNext()) enchantments.add(line.next());
			else enchantments.add("");
		}
		
		keyMap.checkUnusedKeys();
	}
	
	public void saveToFile() {
		File fout = new File("campaigns/" + Game.curCampaign.getID() + "/recipes/" + id + ".txt");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			
			out.write("name \"" + name + "\"");
			out.newLine();
			
			out.write("description \"" + description + "\"");
			out.newLine();
			
			out.write("skill \"" + skill + "\"");
			out.newLine();
			
			out.write("skillRequirement " + skillRankRequirement);
			out.newLine();
			
			if (result != null) {
				out.write("result \"" + result + "\"");
				out.newLine();
			}
			
			out.write("resultQuantity " + resultQuantity);
			out.newLine();
			
			out.write("resultHasQuality \"" + resultHasQuality);
			out.newLine();
			
			if (anyItemOfTypeIngredient != null) {
				out.write("anyItemOfTypeIngredient \"" + anyItemOfTypeIngredient.toString() + "\"");
				out.newLine();
			}
			
			out.write("resultIsIngredient " + resultIsIngredient);
			out.newLine();
			
			for (int i = 0; i < ingredients.size(); i++) {
				out.write("ingredient \"" + ingredients.getItem(i).getID() + "\" " + ingredients.getQuantity(i));
				out.newLine();
			}
			
			for (int i = 0; i < enchantments.size(); i++) {
				out.write("qualityModifier " + qualitySkillModifierRequirements.get(i) + " ");
				out.write("\"" + enchantments.get(i) + "\"");
				out.newLine();
			}
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving recipe: " + id, e);
		}
	}
	
	@Override public String getReferenceType() {
		return "Recipe";
	}
	
	@Override public String toString() {
		return this.id;
	}
	
	@Override public ReferenceList getReferenceList() {
		return new RecipeReferenceList(this);
	}
}
