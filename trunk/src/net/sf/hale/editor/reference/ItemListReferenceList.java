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

import net.sf.hale.Game;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.LootList;
import net.sf.hale.rules.Merchant;

/**
 * A {@link ReferenceList} for an ItemList object.
 * 
 * @author Jared Stephen
 *
 */

public class ItemListReferenceList implements ReferenceList {
	private final ItemList itemList;
	
	/**
	 * Create a new ReferenceList with the specified ItemList
	 * 
	 * @param itemList the ItemList to create this list with
	 */
	
	public ItemListReferenceList(ItemList itemList) {
		this.itemList = itemList;
	}

	/**
	 * Returns a list of Strings representing all the references to the ItemList
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the ItemList specified at the creation
	 * of this ReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Item List: " + itemList.getID());
		
		// check the loot lists for all containers
		for (int i = 0; i < Game.campaignEditor.getItemsModel().getNumEntries(); i++) {
			Item item = Game.campaignEditor.getItemsModel().getEntry(i);
			
			if (item.getType() == Entity.Type.CONTAINER) {
				LootList loot = ((Container)item).getLoot();
				
				int count = 0;
				
				for (int j = 0; j < loot.size(); j++) {
					if (this.itemList.getID().equals(loot.getEntry(j).itemListID)) {
						count++;
					}
				}
				
				if (count > 0) {
					references.add("Loot for Container: " + item.getID() + " (" + count + "x)");
				}
			}
		}
		
		// check the loot lists of all creatures for references
		for (int i = 0; i < Game.campaignEditor.getCreaturesModel().getNumEntries(); i++) {
			Creature creature = Game.campaignEditor.getCreaturesModel().getEntry(i);
			LootList loot = creature.getLoot();
			
			for (int j = 0; j < loot.size(); j++) {
				if (this.itemList.getID().equals(loot.getEntry(j).itemListID)) {
					references.add("Loot for Creature: " + creature.getID());
				}
			}
		}
		
		// check the items of each merchant for references
		for (int i = 0; i < Game.campaignEditor.getMerchantsModel().getNumEntries(); i++) {
			Merchant merchant = Game.campaignEditor.getMerchantsModel().getEntry(i);
			
			LootList loot = merchant.getBaseItems();
			
			for (int j = 0; j < loot.size(); j++) {
				if (this.itemList.getID().equals(loot.getEntry(j).itemListID)) {
					references.add("Merchant: " + merchant.getID());
				}
			}
		}
		
		return references;
	}
}
