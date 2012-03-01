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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.sf.hale.Game;
import net.sf.hale.editor.reference.ItemListReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.entity.Item;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.util.LineParser;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A List of Items.  Each item has an associated quantity and quality.  The list can contain
 * multiple copies of items with the same id, but items with the same id and quality will
 * automatically stack.
 * 
 * There are predefined ItemLists created from data files as well as ItemLists which represent
 * internal changable states such as inventories and merchant lists.
 * @author Jared Stephen
 *
 */

public class ItemList implements Referenceable, Saveable {
	private final List<Listener> listeners;
	
	private final List<Entry> entries;
	
	private final String id;
	
	@Override public Object save() {
		Object[] data = new Object[entries.size()];

		int i = 0;
		for (Entry entry : entries) {
			JSONOrderedObject entryData = new JSONOrderedObject();
			entryData.put("itemID", entry.itemID);
			entryData.put("quality", entry.quality);
			entryData.put("quantity", entry.quantity);

			data[i] = entryData;

			i++;
		}

		return data;
	}
	
	public void load(SimpleJSONArray data) {
		this.clear();

		for (SimpleJSONArrayEntry arrayEntry : data) {
			SimpleJSONObject entryObject = arrayEntry.getObject();


			String itemID = entryObject.get("itemID", null);
			String quality = entryObject.get("quality", null);
			int quantity = entryObject.get("quantity", 0);

			Entry entry = new Entry(itemID, quantity, quality);

			entries.add(entry);
		}
	}
	
	/**
	 * Create a new ItemList with the specified ID String.  The ItemList is initially empty.
	 * @param id the ID String for this ItemList
	 */
	
	public ItemList(String id) {
		this.id = id;
		this.entries = new ArrayList<Entry>();
		this.listeners = new ArrayList<Listener>(1);
	}
	
	/**
	 * Create a new ItemList that is an exact copy of the specified ItemList
	 * @param other the ItemList to copy
	 */
	
	public ItemList(ItemList other) {
		this(other, other.id);
	}
	
	/**
	 * Create a new ItemList that is a copy of the specified ItemList, except the new
	 * ItemList has the specified ID.
	 * @param other the ItemList to copy
	 * @param id the ID for the new ItemList
	 */
	
	public ItemList(ItemList other, String id) {
		this(id);
		
		for (Entry entry : other.entries) {
			this.entries.add(new Entry(entry));
		}
	}
	
	/**
	 * Adds all the items in the specified ItemList to this ItemList.  This means adding the
	 * quantity held in other for each item of each quality to this ItemList.
	 * @param other the ItemList to add items from
	 */
	
	public void addItemsFromList(ItemList other) {
		if (other == null) return;
		
		for (int i = 0; i < other.size(); i++) {
			Entry entry = other.entries.get(i);
			this.addItem(entry.itemID, entry.quantity, entry.quality);
		}
	}
	
	/**
	 * Adds all items in the specified ItemList to this ItemList.  The quantity added for each
	 * item to this ItemList is the quantity held in the other ItemList multiplied by multiple.
	 * @param other the ItemList to add Items from
	 * @param multiple the multiplier for the quantity of each Item to add
	 */
	
	public void addItemsFromList(ItemList other, int multiple) {
		if (other == null) return;
		
		for (int i = 0; i < other.size(); i++) {
			Entry entry = other.entries.get(i);
			
			int qty = (multiple == Integer.MAX_VALUE ? multiple : entry.quantity * multiple);
			
			this.addItem(entry.itemID, qty, entry.quality);
		}
	}
	
	public int findItem(String itemID) {
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).itemID.equals(itemID)) return i;
		}
		
		return -1;
	}
	
	public int findItem(String itemID, int quantity) {
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).itemID.equals(itemID) && entries.get(i).quantity >= quantity) return i;
		}
		
		return -1;
	}
	
	public int findItem(String itemID, int quantity, String quality) {
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).itemID.equals(itemID) && entries.get(i).quantity >= quantity &&
					entries.get(i).quality.equals(quality)) return i;
		}
		
		return -1;
	}
	
	public int findItem(String itemID, String quality) {
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).itemID.equals(itemID) && entries.get(i).quality.equals(quality))
				return i;
		}
		
		return -1;
	}
	
	public int findItem(Item item) {
		return findItem(item.getID(), item.getQuality().getName());
	}
	
	public boolean containsItem(String itemID, int quantity, String quality) {
		if (quantity <= 0) return true;
		
		return findItem(itemID, quantity, quality) != -1;
	}
	
	public boolean containsItem(String itemID, int quantity) {
		if (quantity <= 0) return true;
		
		return findItem(itemID, quantity) != -1;
	}
	
	public boolean containsItem(String itemID) {
		return findItem(itemID) != -1;
	}
	
	public void addItem(String itemID, int quantity, String quality) {
		int index = findItem(itemID, quality);
		
		if (index == -1) {
			entries.add(new Entry(itemID, quantity, quality));
			
			notifyListeners();
		} else if (entries.get(index).quantity != Integer.MAX_VALUE) {
			entries.get(index).quantity += quantity;
		}
	}
	
	public void addItem(Item item, int quantity, String quality) {
		addItem(item.getID(), quantity, quality);
	}
	
	public void addItem(Item item, int quantity) {
		addItem(item.getID(), quantity, item.getQuality().getName());
	}
	
	public void addItem(String itemID, int quantity) {
		addItem(itemID, quantity, Game.ruleset.getString("DefaultItemQuality"));
	}
	
	public void addItem(Item item) {
		addItem(item.getID(), 1, item.getQuality().getName());
	}
	
	public void addItem(String itemID) {
		addItem(itemID, 1, Game.ruleset.getString("DefaultItemQuality"));
	}
	
	public void removeItem(Item item) {
		removeItem(this.findItem(item), 1);
	}
	
	public void removeItem(Item item, int quantityToRemove) {
		removeItem(this.findItem(item), quantityToRemove);
	}
	
	public void removeItem(String itemID, int quantityToRemove) {
		removeItem(this.findItem(itemID, quantityToRemove), quantityToRemove);
	}
	
	public void removeItem(int index, int quantity) {
		int quantityHeld = entries.get(index).quantity;
		
		if (quantityHeld != Integer.MAX_VALUE) {
			if (quantityHeld - quantity <= 0) {
				entries.remove(index);
				
				notifyListeners();
			} else {
				entries.get(index).quantity -= quantity;
			}
		}
	}
	
	/**
	 * Removes up to the specified quantity of the item
	 * @param itemID the ID of the item to remove
	 * @param quantity the max quantity to remove
	 * @return the number of items that were removed
	 */
	
	public int removeUpTo(String itemID, int quantity) {
		int qtyRemoved = 0;
		int qtyLeft = quantity;
		
		for (int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
			
			if (entry.itemID.equals(itemID)) {
				if (entry.quantity >= qtyLeft) {
					// we have removed the max quantity
					
					removeItem(i, qtyLeft);
					
					return quantity;
				} else {
					// remove the entire entry
					qtyRemoved += entry.quantity;
					qtyLeft -= entry.quantity;
					
					removeItem(i, entry.quantity);
					
					i--;
				}
			}
		}
		
		return qtyRemoved;
	}
	
	public void removeItem(int index) {
		removeItem(index, 1);
	}
	
	public String getItemID(int index) {
		return entries.get(index).itemID;
	}
	
	public Item getItem(int index) {
		Item item = Game.entityManager.getItem(entries.get(index).itemID);
		
		if (item == null) return null;
		
		if (!item.getQuality().getName().equals(entries.get(index).quality))
			item.setQuality(Game.ruleset.getItemQuality(entries.get(index).quality));
		
		return item;
	}
	
	public int getQuantity(int index) {
		return entries.get(index).quantity;
	}
	
	public String getQuality(int index) {
		return entries.get(index).quality;
	}
	
	public void setQuality(int index, String quality) {
		entries.get(index).quality = quality;
	}
	
	public int getQuantity(Item item) {
		int count = 0;
		
		for (int i = 0; i < entries.size(); i++) {
			if ( entries.get(i).itemID.equals(item.getID()) &&
					entries.get(i).quality.equals(item.getQuality().getName()) ) {
				
				count += entries.get(i).quantity;
			}
		}
		
		return count;
	}
	
	public int getQuantity(String itemID) {
		int count = 0;
		
		for (Entry entry : entries) {
			if (entry.itemID.equals(itemID))
				count += entry.quantity;
		}
		
		return count;
	}
	
	public void setQuantity(int index, int value) {
		entries.get(index).quantity = value;
	}
	
	public int size() { return entries.size(); }
	
	public void clear() {
		entries.clear();
		
		notifyListeners();
	}
	
	@Override public String toString() { return id; }

	@Override public ReferenceList getReferenceList() {
		return new ItemListReferenceList(this);
	}

	@Override public String getReferenceType() {
		return "ItemList";
	}
	
	@Override public String getID() {
		return id;
	}
	
	/**
	 * Adds the specified Listener to be notified when an entry is added or
	 * removed from this ItemList
	 * @param listener the listener to add
	 */
	
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes the specified Listener from this ItemList
	 * @param listener the listener to remove
	 */
	
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners() {
		for (Listener listener : listeners) {
			listener.itemListModified();
		}
	}
	
	/**
	 * Saves this Item List to the current campaign data file based on its ID.  This will be the
	 * file "campaigns/" + campaignID + "/itemLists/" + id + ".txt"
	 */
	
	public void saveToDisk() {
		File f = new File("campaigns/" + Game.curCampaign.getID() + "/itemLists/" + getID() + ".txt");
		
		try {
			f.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			
			for (int i = 0; i < size(); i++) {
				out.write("\"" + getItem(i).getID() + "\" ");
				out.write(getQuantity(i) + " ");
				out.write("\"" + getQuality(i) + "\"");
				out.newLine();
			}
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving item list " + getID(), e);
		}
	}
	
	/**
	 * Factory method for ItemLists.  Creates an ItemList with the specified ID and then reads
	 * the ItemList contents from the specified resource.
	 * @param id the ID of the ItemList to create
	 * @param resource the resource to read the itemList contents
	 * @return a new ItemList with the specified ID and items added from the specified resource
	 */
	
	public static ItemList readItemList(String id, String resource) {
		ItemList itemList = new ItemList(id);
		int lineNumber = 0;
		
		try {
			Scanner sFile = ResourceManager.getScanner(resource);
			
			while (sFile.hasNextLine()) {
				LineParser sLine = new LineParser(sFile.nextLine());
				
				if (sLine.hasNext()) {
					String name = sLine.next();
					int quantity = 1;
					if (sLine.hasNext()) quantity = sLine.nextInt();
					String quality = Game.ruleset.getString("DefaultItemQuality");
					if (sLine.hasNext()) quality = sLine.next();
					
					itemList.addItem(Game.entityManager.getItem(name), quantity, quality);
				}
			}
			
			sFile.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error reading resource " + resource + " on line " + lineNumber, e);
		}
		
		return itemList;
	}
	
	/**
	 * The interface for any widgets that wish to be notified when an entry is added or removed
	 * from this ItemList
	 * @author Jared Stephen
	 *
	 */
	
	public interface Listener {
		/**
		 * Method that is called when an entry is added or removed from an ItemList.  Note that this
		 * method is not called when a quantity is changed, only when an entirely new entry is added
		 * or an existing entry is removed from this ItemList.
		 */
		public void itemListModified();
	}
	
	private class Entry {
		private String itemID;
		private int quantity;
		private String quality;
		
		private Entry(String itemID, int quantity, String quality) {
			this.itemID = itemID;
			this.quantity = quantity;
			this.quality = quality;
		}
		
		private Entry(String itemID, int quantity) {
			this.itemID = itemID;
			this.quantity = quantity;
			this.quality = Game.ruleset.getString("DefaultItemQuality");
		}
		
		private Entry(Entry other) {
			this.itemID = other.itemID;
			this.quantity = other.quantity;
			this.quality = other.quality;
		}
	}
}
