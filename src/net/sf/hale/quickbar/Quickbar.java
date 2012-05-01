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

package net.sf.hale.quickbar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A Quickbar is a collection of 100 "slots".  Each slot is designed to allow the
 * player to quickly use or equip an Item or activate an Ability.  A Quickbar is
 * viewed through a QuickbarViewer, which views the slots in sets of 10 at a time
 * to make things more manageable.
 * 
 * @author Jared Stephen
 *
 */

public class Quickbar implements Saveable {
	/** The maximum total number of QuickbarSlots in any one Quickbar */
	public static final int TotalSlots = 50;
	
	/** The number of QuickbarSlots shown at one time by the QuickbarViewer */
	public static final int SlotsAtOnce = 10;
	
	/** The number of set of QuickbarSlots shown in total by the QuickbarViewer */
	public static final int SetsOfSlots = TotalSlots / SlotsAtOnce;
	
	private int lastViewSet;
	private Map<Integer, QuickbarSlot> slots;
	private Creature parent;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("lastViewSet", lastViewSet);
		
		JSONOrderedObject slotsData = new JSONOrderedObject();
		for (Integer key : slots.keySet()) {
			QuickbarSlot slot = slots.get(key);
			if (slot != null)
				slotsData.put("slot" + key.toString(), slot.save());
		}
		
		data.put("slots", slotsData);
		
		return data;
	}
	
	public void load(SimpleJSONObject data) {
		this.clear();
		
		this.lastViewSet = data.get("lastViewSet", 0);
		
		SimpleJSONObject slotsObject = data.getObject("slots");
		
		for (String key : slotsObject.keySet()) {
			int index = Integer.parseInt( key.substring(4, key.length()) );
			
			SimpleJSONObject slotData = slotsObject.getObject(key);
			
			String type = slotData.get("type", null);
			
			if (type.equals("ability")) {
				String abilityID = slotData.get("abilityID", null);
				QuickbarSlot slot = new AbilityActivateSlot(Game.ruleset.getAbility(abilityID), parent);
				slots.put(index, slot);
				
			} else if (type.equals("use")) {
				String itemID = slotData.get("itemID", null);
				String qualityID = slotData.get("itemQuality", null);
				
				Item item = Game.entityManager.getItem(itemID);
				item.setQuality(qualityID);
				
				QuickbarSlot slot = new ItemUseSlot(item, parent);
				slots.put(index, slot);
				
			} else if (type.equals("equip")) {
				String itemID = slotData.get("itemID", null);
				String qualityID = slotData.get("itemQuality", null);
				
				Item item = Game.entityManager.getItem(itemID);
				item.setQuality(qualityID);
				
				ItemEquipSlot slot = new ItemEquipSlot(item, parent);
				
				if (slotData.containsKey("secondaryItem")) {
					String secondaryItemID = slotData.get("secondaryItemID", null);
					String secondaryQualityID = slotData.get("secondaryItemQuality", null);
					
					Item secondaryItem = Game.entityManager.getItem(secondaryItemID);
					item.setQuality(secondaryQualityID);
					
					slot.setSecondaryItem(secondaryItem);
				}
				
				slots.put(index, slot);
			}
		}
	}
	
	/**
	 * Creates a new Quickbar that is a copy of the specified quickbar
	 * @param other the quickbar to copy
	 * @param parent the new parent creature for this quickbar
	 */
	
	public Quickbar(Quickbar other, Creature parent) {
		slots = new HashMap<Integer, QuickbarSlot>();
		
		for (Integer index : other.slots.keySet()) {
			QuickbarSlot slot = other.slots.get(index);
			
			if (slot != null) {
				this.slots.put(index, slot.getCopy(parent));
			}
		}
		
		lastViewSet = other.lastViewSet;
		this.parent = parent;
	}
	
	/**
	 * Creates a new Quickbar with the specified Creature as the parent that will
	 * be used whenever a QuickbarSlot is activated.  The Quickbar is initially
	 * empty.
	 * 
	 * @param parent the parent owner of this Quickbar
	 */
	
	public Quickbar(Creature parent) {
		slots = new HashMap<Integer, QuickbarSlot>();
		lastViewSet = 0;
		this.parent = parent;
	}
	
	/**
	 * Removes all current quickbar slots so that the quickbar is empty
	 */
	
	public void clear() {
		slots.clear();
		lastViewSet = 0;
	}
	
	/**
	 * Returns the QuickbarSlot with the specified Quickbar index
	 * 
	 * @param index the index of the Slot to retrieve
	 * @return the QuickbarSlot at the specified index or null if no
	 * QuickbarSlot is found at that index
	 */
	
	public QuickbarSlot getSlot(int index) {
		if (index >= Quickbar.TotalSlots || index < 0) return null;
		
		return slots.get(Integer.valueOf(index));
	}
	
	/**
	 * Returns the owner, parent creature of this quickbar
	 * @return the parent creature of this quickbar
	 */
	
	public Creature getParent() {
		return parent;
	}
	
	/**
	 * Sets the QuickbarSlot at the specified index to the specified
	 * QuickbarSlot.  If the slot is already set to equip an item and the
	 * specified slot is a compatible secondary item equip, then the
	 * current slot will be modified rather than setting a new slot
	 * 
	 * @param slot the QuickbarSlot to set
	 * @param index the index of the Slot to set
	 */
	
	public void setSlot(QuickbarSlot slot, int index) {
		// handle the special case of one equip slot holding two items
		QuickbarSlot current = slots.get(Integer.valueOf(index));
		
		if (current instanceof ItemEquipSlot && slot instanceof ItemEquipSlot) {
			if ( ((ItemEquipSlot)current).setSecondaryItem(((ItemEquipSlot)slot).getItem()) ) {
				return;
			}
		}
		
		slots.put(Integer.valueOf(index), slot);
	}
	
	/**
	 * Helper function to easily add the specified Item to this Quickbar.  The
	 * specified Item is added to the QuickbarSlot with the lowest index that
	 * is currently empty.  If all QuickbarSlots are currently occupied, no
	 * action is performed.
	 * 
	 * @param item the Item to add
	 */
	
	public void addToFirstEmptySlot(Item item) {
		addToFirstEmptySlot(Quickbar.getQuickbarSlot(item, parent));
	}
	
	/**
	 * Helper function to easily add the specified Ability to this Quickbar.  The
	 * specified Ability is added to the QuickbarSlot with the lowest index that
	 * is currently empty.  If all QuickbarSlots are currently occupied, no
	 * action is perfomed.
	 * 
	 * @param ability the Ability to add
	 */
	
	public void addToFirstEmptySlot(Ability ability) {
		addToFirstEmptySlot(Quickbar.getQuickbarSlot(ability, parent));
	}
	
	private void addToFirstEmptySlot(QuickbarSlot slot) {
		if (slot == null) return;
		
		for (int i = 0; i < Quickbar.TotalSlots; i++) {
			if (getSlot(i) == null) {
				setSlot(slot, i);
				break;
			}
		}
		
		Game.mainViewer.updateInterface();
	}
	
	/**
	 * Sets the most recent viewed set of QuickbarSlots to the specified
	 * index for this Quickbar.  The viewSetIndex can can be from 0 to
	 * Quickbar.SetsOfSlots, and indicates the set of slots that was
	 * most recently viewed with the QuickbarViewer for this Quickbar.
	 * 
	 * @param viewSetIndex the most recently viewed index
	 */
	
	public void setLastViewSetIndex(int viewSetIndex) {
		if (viewSetIndex < 0) viewSetIndex = 0;
		if (viewSetIndex >= Quickbar.SetsOfSlots) viewSetIndex = Quickbar.SetsOfSlots - 1;
		
		this.lastViewSet = viewSetIndex;
	}
	
	/**
	 * Returns the most recently viewed set of QuickbarSlots for this
	 * Quickbar by the QuickbarViewer.  See {@link #setLastViewSetIndex(int)}
	 * 
	 * @return the index of the most recently viewed set of QuickbarSlots.
	 */
	
	public int getLastViewSetIndex() {
		return lastViewSet;
	}
	
	/**
	 * Adds all Abilities owned by the parent of this Quickbar to empty
	 * slots in this Quickbar, as possible.  Abilities are added first to
	 * slots with the lowest index.
	 */
	
	public void addAbilitiesToEmptySlots() {
		List<Ability> abilities = parent.getAbilities().getActivateableAbilities();
		Iterator<Ability> abilitiesIter = abilities.iterator();
		
		// if there are no abilities to add, we are done
		if (!abilitiesIter.hasNext()) return;
		
		for (int i = 0; i < Quickbar.TotalSlots; i++) {
			if (getSlot(i) == null) {
				QuickbarSlot slot = Quickbar.getQuickbarSlot(abilitiesIter.next(), parent);
				this.setSlot(slot, i);
				
				// if there are no more abilities, we are done
				if (!abilitiesIter.hasNext()) return;
			}
		}
		
		// if control reaches this point, that means we ran out of slots before
		// we ran out of abilities.
	}
	
	/**
	 * Returns a QuickbarSlot for the specified Item owned by the parent.  The
	 * QuickbarSlot can then be added to a Quickbar.  This QuickbarSlot may
	 * either equip or use the Item depending on its ItemType.  Some Items
	 * can not be added to Quickbars and this method will return null for those
	 * Items.
	 * 
	 * @param item the Item to be encapsulated by the QuickbarSlot
	 * @param parent the owner of the Item and Quickbar
	 * @return a QuickbarSlot for the specified Item
	 */
	
	public static QuickbarSlot getQuickbarSlot(Item item, Creature parent) {
		switch (item.getItemType()) {
		case ITEM:
			if (item.isUsable()) {
				return new ItemUseSlot(item, parent);
			}
			break;
		default:
			return new ItemEquipSlot(item, parent);
		}
		
		return null;
	}
	
	/**
	 * Returns a QuickbarSlot for the specified Ability that can be added to
	 * a Quickbar
	 * 
	 * @param ability the Ability the QuickbarSlot will encapsulate
	 * @param parent the parent Activator / Creature that owns the specified Ability
	 * @return a QuickbarSlot for the Ability
	 */
	
	public static QuickbarSlot getQuickbarSlot(Ability ability, AbilityActivator parent) {
		return new AbilityActivateSlot(ability, parent);
	}
}
