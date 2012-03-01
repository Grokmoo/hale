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

package net.sf.hale.rules;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.util.Logger;

public class LootList {
	/**
	 * The type of loot generation that a given list uses
	 * @author Jared
	 *
	 */
	
	public enum ProbabilityMode {
		/** There is an individual probability for each item to be drawn */
		PER_ITEM,
		
		/** One item is drawn from the entire list with the specified probability */
		PER_LIST;
	};
	
	private final List<Entry> entries;
	private boolean alreadyGenerated;
	
	public LootList() {
		entries = new ArrayList<Entry>();
		alreadyGenerated = false;
	}
	
	public LootList(LootList other) {
		this.entries = new ArrayList<Entry>(other.entries);
		this.alreadyGenerated = other.alreadyGenerated;
	}
	
	public void addAll(List<Entry> entries) {
		this.entries.addAll(entries);
	}
	
	public void add(String list, int probability, ProbabilityMode mode) {
		this.entries.add(new Entry(list, probability, mode));
	}
	
	public void setAlreadyGenerated(boolean generated) { this.alreadyGenerated = generated; }
	
	public boolean alreadyGenerated() { return alreadyGenerated; }
	
	public Entry getEntry(int index) { return entries.get(index); }
	public int size() { return entries.size(); }
	
	public void setProbability(int index, int probability) {
		Entry oldEntry = entries.get(index);
		
		Entry entry = new Entry(oldEntry.itemListID, probability, oldEntry.mode);
		entries.set(index, entry);
	}
	
	public void setMode(int index, ProbabilityMode mode) {
		Entry oldEntry = entries.get(index);
		
		Entry entry = new Entry(oldEntry.itemListID, oldEntry.probability, mode);
		entries.set(index, entry);
	}
	
	public void remove(int index) {
		entries.remove(index);
	}
	
	public ItemList generate() {
		ItemList loot = new ItemList("loot");
		
		for (Entry entry : entries) {
			ItemList itemList = Game.ruleset.getItemList(entry.itemListID);
			
			if (itemList == null) {
				Logger.appendToErrorLog("Attempting to generate loot list but item list " + entry.itemListID + " does not exist.");
				continue;
			}
			
			// can't generate an item if there are none to choose from
			if (itemList.size() == 0) continue;
			
			switch (entry.mode) {
			case PER_LIST:
				if (Game.dice.rand(1, 100) <= entry.probability) {
					int itemIndex = 0;
					
					if (itemList.size() > 1)
						itemIndex = Game.dice.rand(0, itemList.size() - 1);
					
					// randomize quantity unless it is unlimited
					int quantity = itemList.getQuantity(itemIndex);
					if (quantity != Integer.MAX_VALUE)
						quantity = Game.dice.rand(1, itemList.getQuantity(itemIndex));
					
					loot.addItem(itemList.getItem(itemIndex), quantity);
				}
				break;
			case PER_ITEM:
				for (int j = 0; j < itemList.size(); j++) {
					if (Game.dice.rand(1, 100) <= entry.probability) {
						// randomize quantity unless it is unlimited
						int quantity = itemList.getQuantity(j);
						if (quantity != Integer.MAX_VALUE)
							quantity = Game.dice.rand(1, itemList.getQuantity(j));
						
						loot.addItem(itemList.getItem(j), quantity);
					}
				}
				break;
			}
		}
		
		alreadyGenerated = true;
		
		return loot;
	}
	
	/**
	 * A single entry in a loot list
	 * @author Jared
	 *
	 */
	
	public static class Entry {
		/** The itemList that this LootEntry will draw items from */
		public final String itemListID;
		
		/** The probability of drawing from the list, depending on the mode */
		public final int probability;
		
		/** The probability mode that this entry operates under */
		public final ProbabilityMode mode;
		
		/**
		 * Creates a new LootList entry based on the specified parameters
		 * @param itemListID the ID of the item list to use
		 * @param probability the probability of generating the item
		 * @param mode the mode
		 */
		
		public Entry(String itemListID, int probability, ProbabilityMode mode) {
			this.itemListID = itemListID;
			this.probability = probability;
			this.mode = mode;
		}
	}
}
