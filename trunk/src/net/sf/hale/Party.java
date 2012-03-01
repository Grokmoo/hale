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
import java.util.Iterator;
import java.util.List;

import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

public class Party implements Iterable<Creature>, Saveable {
	public static final int MaxQuickBarSlots = 100;
	private int iteratorIndex;
	private int iteratorSize;
	private boolean recomputePortraits = false;
	
	private String name;
	private final List<Creature> characters;
	private int selectedCharacterIndex;
	private boolean defeated = false;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("name", name);
		data.put("selectedCharacter", selectedCharacterIndex);
		
		if (defeated) data.put("defeated", defeated);
		
		Object[] charactersData = new Object[characters.size()];
		int i = 0;
		for (Creature c : characters) {
			charactersData[i] = SaveGameUtil.getRef(c);
			i++;
		}
		data.put("characters", charactersData);
		
		return data;
	}
	
	public static Party load(SimpleJSONObject data, ReferenceHandler refHandler) {
		Party party = new Party();
		
		party.name = data.get("name", null);
		party.selectedCharacterIndex = data.get("selectedCharacter", 0);
		
		if (data.containsKey("defeated"))
			party.defeated = data.get("defeated", false);
		
		for (SimpleJSONArrayEntry entry : data.getArray("characters")) {
			String charID = entry.getString();
			
			Creature creature = (Creature)refHandler.getEntity(charID);
			creature.setFaction("Player");
			creature.setEncounter(Game.curCampaign.partyEncounter);
			Game.curCampaign.partyEncounter.addAreaCreature(creature);
			
			party.characters.add( creature );
		}
		
		return party;
	}
	
	public Party() {
		characters = new ArrayList<Creature>(3);
		
		iteratorIndex = 0;
		
		selectedCharacterIndex = -1;
		
		recomputePortraits = true;
	}

	public void movePartyMember(Creature creature, int placesForward) {
		if (placesForward == 0) throw new IllegalArgumentException("Cannot move a creature by 0.");
		
		int indexOld = characters.indexOf(creature);
		int indexNew = indexOld + placesForward;
		
		if (indexNew < indexOld) {
			characters.add(indexNew, creature);
			characters.remove(indexOld + 1);
		} else if (indexNew > indexOld) {
			characters.add(indexNew + 1, creature);
			characters.remove(indexOld);
		}
		
		recomputePortraits = true;
	}
	
	/**
	 * Returns true if and only if the specified creature is a non-summoned member of this
	 * current player character party
	 * @param creature the creature to check
	 * @return whether the creature is a player character
	 */
	
	public boolean isPCPartyMember(Creature creature) {
		if (!creature.isPlayerSelectable()) return false;
		if (creature.isSummoned()) return false;
		
		for (Creature character : characters) {
			if (creature == character) return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the name of this party
	 * @return the name of the party
	 */
	
	public String getName() { return name; }
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDefeated(boolean defeated) {
		this.defeated = defeated;
		
		if (defeated) {
			for (Creature c : characters){
				c.kill();
			}
		}
	}
	
	public boolean isDefeated() {
		return defeated;
	}
	
	public int getSelectedIndex() {
		return selectedCharacterIndex;
	}
	
	public void setSelected(int index) {
		this.selectedCharacterIndex = index;
	}
	
	public void setSelected(Creature creature) {
		this.selectedCharacterIndex = characters.indexOf(creature);
	}
	
	public Creature getSelected() {
		if (selectedCharacterIndex == -1) return null;
		
		// if the selected character no longer exists switch to a valid party member
		if (selectedCharacterIndex >= characters.size()) {
			selectedCharacterIndex = 0;
		}
		
		return characters.get(selectedCharacterIndex);
	}
	
	public int size() { return characters.size(); }
	
	public Creature get(int index) {
		return characters.get(index);
	}
	
	public void add(Creature creature) {
		characters.add(creature);
		
		recomputePortraits = true;
	}
	
	public boolean contains(Creature creature) {
		return characters.contains(creature);
	}
	
	public void remove(Creature creature) {
		int index = characters.indexOf(creature);
		
		if (index != -1) {
			characters.remove(index);
		}
		
		recomputePortraits = true;
	}
	
	/**
	 * Returns the total quantity of the item with the specified ID across all party
	 * member inventories
	 * @param itemID the ID of the item to check for
	 * @return the total item quantity
	 */
	
	public int getQuantity(String itemID) {
		int quantityHeld = 0;
		
		for (Creature creature : characters) {
			quantityHeld += creature.getInventory().getQuantity(itemID);
		}
		
		return quantityHeld;
	}
	
	/**
	 * Returns true if and only if the entire party's combined inventory holds
	 * the specified quantity of the specified item
	 * @param itemID the item to check for
	 * @param quantity the quantity
	 * @return whether the party has the specified quantity of the item
	 */
	
	public boolean hasItem(String itemID, int quantity) {
		int quantityHeld = 0;
		
		for (Creature creature : characters) {
			quantityHeld += creature.getInventory().getQuantity(itemID);
		}
		
		return quantityHeld >= quantity;
	}
	
	/**
	 * Searches for an instance of an item with the specified itemID in all party member
	 * inventories, and removes the first instance found, then returns.  If no item is
	 * found, no action is performed
	 * @param itemID the entity ID of the item to remove
	 */
	
	public void removeItem(String itemID) {
		for (Creature creature : characters) {
			Inventory inv = creature.getInventory();
			
			if (inv.removeItemEvenIfEquipped(itemID)) return;
		}
	}
	
	/**
	 * Searches through all party inventories and removes up to the specified quantity of the
	 * item
	 * @param itemID the item to remove
	 * @param quantity the quantity to remove
	 */
	
	public void removeItem(String itemID, int quantity) {
		int qtyRemoved = 0;
		int qtyLeft = quantity;
		
		for (Creature creature : characters) {
			qtyRemoved += creature.getInventory().getUnequippedItems().removeUpTo(itemID, qtyLeft);
			qtyLeft = quantity - qtyRemoved;
			
			if (qtyLeft == 0) return;
		}
	}
	
	public boolean recomputePortraits() {
		boolean returnValue = recomputePortraits;
		
		recomputePortraits = false;
		
		return returnValue;
	}
	
	@Override public Iterator<Creature> iterator() {
		iteratorIndex = 0;
		iteratorSize = characters.size();
		
		return new PartyIterator();
	}
	
	private class PartyIterator implements Iterator<Creature> {
		@Override public boolean hasNext() {
			return iteratorIndex < iteratorSize;
		}

		@Override public Creature next() {
			Creature next = characters.get(iteratorIndex);
			
			iteratorIndex++;
			
			return next;
		}

		@Override public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
