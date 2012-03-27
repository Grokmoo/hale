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

package net.sf.hale.entity;

import net.sf.hale.Game;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.SubIcon;
import net.sf.hale.rules.Weight;
import net.sf.hale.util.SimpleJSONObject;

public class Inventory implements Saveable {
	private final InventoryCallbackFactory callbackFactory;
	
	private Creature parent;
	
	private final Item[] equipped;
	private ItemList items;
	
	public static final int EQUIPPED_SIZE = 12;
	
	public static final int EQUIPPED_MAIN_HAND = 0;
	public static final int EQUIPPED_OFF_HAND = 1;
	public static final int EQUIPPED_ARMOR = 2;
	public static final int EQUIPPED_GLOVES = 3;
	public static final int EQUIPPED_HELMET = 4;
	public static final int EQUIPPED_CLOAK = 5;
	public static final int EQUIPPED_BOOTS = 6;
	public static final int EQUIPPED_BELT = 7;
	public static final int EQUIPPED_AMULET = 8;
	public static final int EQUIPPED_RING_RIGHT = 9;
	public static final int EQUIPPED_RING_LEFT = 10;
	public static final int EQUIPPED_QUIVER = 11;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			Item item = equipped[i];
			if (item == null) continue;
			
			data.put("equippedSlot" + i, item.save());
		}
		
		if (items.size() > 0)
			data.put("items", items.save());
		
		return data;
	}
	
	public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		this.clear();
		
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			String key = "equippedSlot" + i;
			
			if (!data.containsKey(key)) continue;
			
			SimpleJSONObject equippedSlotData = data.getObject(key);
			
			Item item = Game.entityManager.getItem(equippedSlotData.get("id", null));
			item.load(equippedSlotData, refHandler);
			
			equipped[i] = item;
		}
		
		if (data.containsKey("items"))
			this.items.load(data.getArray("items"));
		else
			this.items.clear();
	}
	
	public Inventory(Creature parent) {
		this.items = new ItemList(parent.getName());
		this.equipped = new Item[EQUIPPED_SIZE];
		this.parent = parent;
		this.callbackFactory = new InventoryCallbackFactory(this);
	}
	
	public Inventory(Inventory other, Creature parent) {
		this(parent);

		this.items = new ItemList(other.items, parent.getName());
		
		for (int i = 0; i < this.equipped.length; i++) {
			if (other.equipped[i] != null) {
				this.equipped[i] = new Item(other.equipped[i]);
				this.equipped[i].setOwner(parent);
			}
		}
	}
	
	public void endAllEffectAnimations() {
		for (int i = 0; i < equipped.length; i++) {
			if (equipped[i] == null) continue;
			
			equipped[i].getEffects().endAllAnimations();
		}
	}
	
	public InventoryCallbackFactory getCallbackFactory() { return callbackFactory; }
	
	public Creature getParent() { return parent; }
	
	public void elapseRounds(int rounds) {
		for (Item item : equipped) {
			if (item != null) item.elapseRounds(rounds);
		}
	}
	
	/**
	 * Removes the first instance of an Item with the specified ID from this Inventory.
	 * @param item the item to remove
	 * @return true if and only if the item was removed.
	 */
	
	public boolean removeItemEvenIfEquipped(String item) {
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] != null && equipped[i].getID().equals(item)) {
				unequip(i);
				return true;
			}
		}
		
		int index = items.findItem(item, 1);
		
		if (index == -1) return false;
		else {
			items.removeItem(index);
			return true;
		}
	}
	
	public void removeEquippedItem(Item item) {
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] != null && equipped[i] == item) {
				unequip(i);
				return;
			}
		}
	}
	
	public void unequipItem(String ref) {
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] != null && equipped[i].getID().equals(ref)) {
				unequipItem(i);
				break;
			}
		}
	}
	
	public void unequipItem(int slot) {
		
		addItem(equipped[slot]);
		
		unequip(slot);
	}
	
	public boolean canUnequipCurrentItemInSlot(Item item, int slot) {
		if (item == null) return false;
		
		switch (item.getItemType()) {
		case CLOAK:
			if (equipped[EQUIPPED_CLOAK] != null) return !equipped[EQUIPPED_CLOAK].isCursed();
			break;
		case BELT:
			if (equipped[EQUIPPED_BELT] != null) return !equipped[EQUIPPED_BELT].isCursed();
			break;
		case AMULET:
			if (equipped[EQUIPPED_AMULET] != null) return !equipped[EQUIPPED_AMULET].isCursed();
			break;
		case RING:
			if (equipped[EQUIPPED_RING_RIGHT] != null && equipped[EQUIPPED_RING_LEFT] != null)
				return (!equipped[EQUIPPED_RING_RIGHT].isCursed()) || (!equipped[EQUIPPED_RING_LEFT].isCursed());
			break;
		case AMMO:
			if (equipped[EQUIPPED_QUIVER] != null) return !equipped[EQUIPPED_QUIVER].isCursed();
			break;
		case ARMOR:
			if (equipped[EQUIPPED_ARMOR] != null) return !equipped[EQUIPPED_ARMOR].isCursed();
			break;
		case SHIELD:
			if (equipped[EQUIPPED_MAIN_HAND] != null) {
				if (equipped[EQUIPPED_MAIN_HAND].getWeaponHandedForCreature(parent) == Item.WeaponHanded.TWO_HANDED)
					return false;
			}
			
			if (equipped[EQUIPPED_OFF_HAND] != null) return !equipped[EQUIPPED_OFF_HAND].isCursed();
			break;
		case GLOVES:
			if (equipped[EQUIPPED_GLOVES] != null) return !equipped[EQUIPPED_GLOVES].isCursed();
			break;
		case BOOTS:
			if (equipped[EQUIPPED_BOOTS] != null) return !equipped[EQUIPPED_BOOTS].isCursed();
			break;
		case HELMET:
			if (equipped[EQUIPPED_HELMET] != null) return !equipped[EQUIPPED_HELMET].isCursed();
			break;
		case WEAPON:
			if (slot == EQUIPPED_OFF_HAND) {
				if (equipped[EQUIPPED_MAIN_HAND] != null) {
					if (equipped[EQUIPPED_MAIN_HAND].getWeaponHandedForCreature(parent) == Item.WeaponHanded.TWO_HANDED)
						return false;
				}
				
				if (equipped[EQUIPPED_OFF_HAND] != null) return !equipped[EQUIPPED_OFF_HAND].isCursed();
			} else {
				if (equipped[EQUIPPED_MAIN_HAND] != null) return !equipped[EQUIPPED_MAIN_HAND].isCursed();
			}
			break;
		}
		
		return true;
	}
	
	public boolean hasPrereqsToEquip(Item item) {
		if (item == null) return false;
		
		switch (item.getItemType()) {
		case ARMOR:
		case SHIELD:
		case GLOVES:
		case BOOTS:
		case HELMET:
			return parent.stats().hasArmorProficiency(item.getArmorType().getName());
		case WEAPON:
			return parent.stats().hasWeaponProficiency(item.getBaseWeapon().getName());
		case ITEM:
			return false;
		default:
			return true;
		}
	}
	
	public boolean canEquipAsOffHandWeapon(Item item) {
		if (item == null) return false;
		
		if (item.getItemType() != Item.ItemType.WEAPON) return false;
		
		if (!item.isMeleeWeapon()) return false;
		
		Item mainWeapon = equipped[EQUIPPED_MAIN_HAND];
		if (mainWeapon == null) mainWeapon = parent.getRace().getDefaultWeapon();
		
		if (!mainWeapon.isMeleeWeapon()) return false;
		
		switch (mainWeapon.getWeaponHandedForCreature(parent.getRace().getSize())) {
		case NONE: return false;
		case TWO_HANDED: return false;
		}
		
		switch (item.getWeaponHandedForCreature(parent.getRace().getSize())) {
		case NONE: return false;
		case TWO_HANDED: return false;
		}
		
		if (!parent.stats().has(Bonus.Type.DualWieldTraining)) return false;
		
		return hasPrereqsToEquip(item);
	}
	
	public boolean hasEquippedOffHandWeapon() {
		Item item = equipped[Inventory.EQUIPPED_OFF_HAND];
		
		if (item == null) return false;
		
		return item.isMeleeWeapon();
	}
	
	private SubIcon.Type getType(int slot, Item item) {
		
		switch (slot) {
		case EQUIPPED_MAIN_HAND:
			if (item.getWeaponType() == Item.WeaponType.BOW) return SubIcon.Type.OffHandWeapon;
			else return SubIcon.Type.MainHandWeapon;
		case EQUIPPED_OFF_HAND:
			if (item.getItemType() == Item.ItemType.SHIELD) return SubIcon.Type.Shield;
			else return SubIcon.Type.OffHandWeapon;
		case EQUIPPED_ARMOR: return SubIcon.Type.Torso;
		case EQUIPPED_GLOVES: return SubIcon.Type.Gloves;
		case EQUIPPED_HELMET: return SubIcon.Type.Head;
		case EQUIPPED_BOOTS: return SubIcon.Type.Boots;
		case EQUIPPED_QUIVER: return SubIcon.Type.Quiver;
		case EQUIPPED_CLOAK: return SubIcon.Type.Cloak;
		default: return null;
		}
	}
	
	public void addAllSubIcons() {
		if (!parent.drawWithSubIcons()) return;
		
		if (parent.getRace() == null || parent.getGender() == null) return;
		
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] == null) continue;
			
			SubIcon.Type type = getType(i, equipped[i]);
			if (type == null || equipped[i].getSubIcon() == null) continue;
			
			if (parent.drawOnlyHandSubIcons) {
				switch (type) {
				case Shield: case MainHandWeapon: case OffHandWeapon:
					break;
				default:
					continue;
				}
			}
			
			// create the sub icon and add it
			SubIcon.Factory factory = new SubIcon.Factory(type, parent.getRace(), parent.getGender());
			factory.setPrimaryIcon(equipped[i].getSubIcon(), equipped[i].getSubIconColor());
			factory.setSecondaryIcon(null, parent.getSubIcons().getClothingColor());
			factory.setCoversBeard(equipped[i].coversBeardIcon());
			this.getParent().subIconList.add(factory.createSubIcon());
		}
	}
	
	private void equip(Item item, int slot) {
		equipped[slot] = item;
		item.setOwner(this.getParent());
		
		this.getParent().stats().changeEquipment(item.getItemType());
		this.getParent().stats().addAll(item.getAllAppliedBonuses());
		
		if (item.hasScript()) 
			item.getScript().executeFunction(ScriptFunctionType.onEquipItem, this.parent, item);
		
		if (!parent.drawWithSubIcons()) return;
		
		SubIcon.Type type = getType(slot, item);
		
		if (type == null || item.getSubIcon() == null) return;
		
		if (parent.drawOnlyHandSubIcons) {
			switch (type) {
			case Shield: case MainHandWeapon: case OffHandWeapon:
				break;
			default:
				return;
			}
		}
		
		// create the sub icon and add it
		SubIcon.Factory factory = new SubIcon.Factory(type, parent.getRace(), parent.getGender());
		factory.setPrimaryIcon(item.getSubIcon(), item.getSubIconColor());
		factory.setSecondaryIcon(null, parent.getSubIcons().getClothingColor());
		factory.setCoversBeard(item.coversBeardIcon());
		this.getParent().subIconList.add(factory.createSubIcon());
	}
	
	private void unequip(int slot) {
		Item item = equipped[slot];
		if (item == null) return;
		
		item.setOwner(null);
		equipped[slot] = null;
		
		this.getParent().stats().changeEquipment(item.getItemType());
		this.getParent().stats().removeAll(item.getAllAppliedBonuses());
		
		if (parent.drawWithSubIcons()) {
			SubIcon.Type type = getType(slot, item);
			if (type != null && item.getSubIcon() != null) {
				this.getParent().subIconList.remove(type);
			}
		}
		
		// stop all active animations on the item
		item.getEffects().endAllAnimations();
		
		if (item.hasScript()) 
			item.getScript().executeFunction(ScriptFunctionType.onUnequipItem, this.parent, item);
	}
	
	public boolean equipItem(String itemID) {
		return equipItem(Game.entityManager.getItem(itemID), 0);		
	}
	
	public boolean canEquip(String itemID) {
		return canEquip(Game.entityManager.getItem(itemID));
	}
	
	public boolean canEquip(Item itemToEquip) {
		if (itemToEquip == null) return false;
		
		if (!hasPrereqsToEquip(itemToEquip)) return false;
		
		switch (itemToEquip.getItemType()) {
		case ITEM: return false;
		case WEAPON:
			if (itemToEquip.getWeaponHandedForCreature(parent.getRace().getSize()) == Item.WeaponHanded.NONE)
				return false; 
		}
		
		return parent.getTimer().canPerformEquipAction(itemToEquip);
	}
	
	public boolean equipItem(Item itemToEquip) {
		return equipItem(itemToEquip, 0);
	}
	
	public boolean equipItem(Item itemToEquip, int slot) {
		if (itemToEquip == null) return false;
		
		Item item = new Item(itemToEquip);
		
		if (!hasPrereqsToEquip(item)) {
			return false;
		}
		
		switch(item.getItemType()) {
		case ITEM: return false;
		case AMMO:
			addItem(equipped[EQUIPPED_QUIVER]);
			unequip(EQUIPPED_QUIVER);
			equip(item, EQUIPPED_QUIVER);
			break;
		case AMULET:
			addItem(equipped[EQUIPPED_AMULET]);
			unequip(EQUIPPED_AMULET);
			equip(item, EQUIPPED_AMULET);
			break;
		case ARMOR:
			addItem(equipped[EQUIPPED_ARMOR]);
			unequip(EQUIPPED_ARMOR);
			equip(item, EQUIPPED_ARMOR);
			break;
		case BELT:
			addItem(equipped[EQUIPPED_BELT]);
			unequip(EQUIPPED_BELT);
			equip(item, EQUIPPED_BELT);
			break;
		case BOOTS:
			addItem(equipped[EQUIPPED_BOOTS]);
			unequip(EQUIPPED_BOOTS);
			equip(item, EQUIPPED_BOOTS);
			break;
		case CLOAK:
			addItem(equipped[EQUIPPED_CLOAK]);
			unequip(EQUIPPED_CLOAK);
			equip(item, EQUIPPED_CLOAK);
			break;
		case GLOVES:
			addItem(equipped[EQUIPPED_GLOVES]);
			unequip(EQUIPPED_GLOVES);
			equip(item, EQUIPPED_GLOVES);
			break;
		case HELMET:
			addItem(equipped[EQUIPPED_HELMET]);
			unequip(EQUIPPED_HELMET);
			equip(item, EQUIPPED_HELMET);
			break;
		case RING:
			if (slot == EQUIPPED_RING_RIGHT) {
				addItem(equipped[EQUIPPED_RING_RIGHT]);
				unequip(EQUIPPED_RING_RIGHT);
				equip(item, EQUIPPED_RING_RIGHT);
			} else if (slot == EQUIPPED_RING_LEFT) {
				addItem(equipped[EQUIPPED_RING_LEFT]);
				unequip(EQUIPPED_RING_LEFT);
				equip(item, EQUIPPED_RING_LEFT);
			} else {
				if (equipped[EQUIPPED_RING_RIGHT] == null) {
					equip(item, EQUIPPED_RING_RIGHT);
				} else if (equipped[EQUIPPED_RING_LEFT] == null) {
					equip(item, EQUIPPED_RING_LEFT);
				} else if (!equipped[EQUIPPED_RING_LEFT].isCursed()) {
					addItem(equipped[EQUIPPED_RING_LEFT]);
					unequip(EQUIPPED_RING_LEFT);
					equip(item, EQUIPPED_RING_LEFT);
				} else if (!equipped[EQUIPPED_RING_RIGHT].isCursed()) {
					addItem(equipped[EQUIPPED_RING_RIGHT]);
					unequip(EQUIPPED_RING_RIGHT);
					equip(item, EQUIPPED_RING_RIGHT);
				}
			}
			break;
		case SHIELD:
			if (equipped[EQUIPPED_OFF_HAND] != null) {
				addItem(equipped[EQUIPPED_OFF_HAND]);
				unequip(EQUIPPED_OFF_HAND);
				equip(item, EQUIPPED_OFF_HAND);
			} else if (equipped[EQUIPPED_MAIN_HAND] == null) {
				equip(item, EQUIPPED_OFF_HAND);
			} else {
				Item.WeaponHanded handed = equipped[EQUIPPED_MAIN_HAND].getWeaponHandedForCreature(parent.getRace().getSize());
				if (handed == Item.WeaponHanded.TWO_HANDED) {
					addItem(equipped[EQUIPPED_MAIN_HAND]);
					unequip(EQUIPPED_MAIN_HAND);
					equip(item, EQUIPPED_OFF_HAND);
				}
				else {
					equip(item, EQUIPPED_OFF_HAND);
				}
			}
			break;
		case WEAPON:
			Item.WeaponHanded handed = item.getWeaponHandedForCreature(parent.getRace().getSize());

			if (slot == EQUIPPED_OFF_HAND) {
				if (!parent.stats().has(Bonus.Type.DualWieldTraining)) return false; 
				
				if (equipped[EQUIPPED_MAIN_HAND] == null) {
					if (handed == Item.WeaponHanded.NONE || handed == Item.WeaponHanded.TWO_HANDED) return false;
					else {
						addItem(equipped[EQUIPPED_OFF_HAND]);
						unequip(EQUIPPED_OFF_HAND);
						equip(item, EQUIPPED_OFF_HAND);
					}
				} else {
					if (!equipped[EQUIPPED_MAIN_HAND].isMeleeWeapon()) return false;
					Item.WeaponHanded mainHanded = equipped[EQUIPPED_MAIN_HAND].getWeaponHandedForCreature(parent.getRace().getSize());
					if (mainHanded == Item.WeaponHanded.TWO_HANDED) {
						return false;
					} else {
						if (handed == Item.WeaponHanded.NONE || handed == Item.WeaponHanded.TWO_HANDED) return false;
						else {
							addItem(equipped[EQUIPPED_OFF_HAND]);
							unequip(EQUIPPED_OFF_HAND);
							equip(item, EQUIPPED_OFF_HAND);
						}
					}
				}
			} else {
				if (equipped[EQUIPPED_OFF_HAND] == null) {
					if (handed == Item.WeaponHanded.NONE) return false;
					else {
						addItem(equipped[EQUIPPED_MAIN_HAND]);
						unequip(EQUIPPED_MAIN_HAND);
						equip(item, EQUIPPED_MAIN_HAND);
					}
				} else {
					if (handed == Item.WeaponHanded.NONE) return false;
					else if (handed == Item.WeaponHanded.TWO_HANDED) {
						addItem(equipped[EQUIPPED_MAIN_HAND]);
						addItem(equipped[EQUIPPED_OFF_HAND]);
						unequip(EQUIPPED_MAIN_HAND);
						unequip(EQUIPPED_OFF_HAND);
						equip(item, EQUIPPED_MAIN_HAND);
					}
					else {
						addItem(equipped[EQUIPPED_MAIN_HAND]);
						unequip(EQUIPPED_MAIN_HAND);
						equip(item, EQUIPPED_MAIN_HAND);
					}
				}
			}
			break;
		}
		
		int index = this.items.findItem(itemToEquip);
		if (index != -1) removeItem(index);
		
		return true;
	}
	
	public void dropEquippedItemAction(int slotIndex) {
		Item item = equipped[slotIndex];
		if (!item.isQuestItem()) {
			
			if (!parent.getTimer().performAction(Game.ruleset.getValue("DropItemCost")))
				return;
			
			unequip(slotIndex);
		}
	}
	
	public void dropItemAction(int itemIndex, int quantity) {
		Item item = items.getItem(itemIndex);
		if (!item.isQuestItem()) {

			if (!parent.getTimer().performAction(Game.ruleset.getValue("DropItemCost")))
				return;

			items.removeItem(itemIndex, quantity);
		}
	}
	
	public void pickupItemAction(Item item, int quantity) {
		if (!parent.getTimer().performAction(Game.ruleset.getValue("PickUpItemCost")))
			return;
		
		addItem(item, quantity);
	}
	
	public void pickupItemAction(Item item) {
		if (!parent.getTimer().performAction(Game.ruleset.getValue("PickUpItemCost")))
			return;
		
		addItem(item);
	}
	
	public void pickupAndWieldAction(Item item) {
		if (!parent.getTimer().performAction(Game.ruleset.getValue("PickUpAndWieldItemCost")))
			return;
		
		addItem(item);
		if (item.getItemType() == Item.ItemType.WEAPON) {
			equipItem(item, EQUIPPED_MAIN_HAND);
		} else if (item.getItemType() == Item.ItemType.SHIELD) {
			equipItem(item, EQUIPPED_OFF_HAND);
		}
	}
	
	public boolean equipAction(Item item, int slot) {
		if (!parent.getTimer().performEquipAction(item))
			return false;
		
		return equipItem(item, slot);
	}
	
	public void equipmentAction(Item item, boolean equip, boolean offHand) {
		if (!parent.getTimer().performEquipAction(item))
			return;
		
		if (equip) {
			if (offHand) equipItem(item, Inventory.EQUIPPED_OFF_HAND);
			else equipItem(item, 0);
		} else {
			if (offHand) this.unequipItem(Inventory.EQUIPPED_OFF_HAND);
			else unequipItem(item.getID());
		}
	}
	
	public void doubleEquipmentAction(Item mainItem, Item secondaryItem) {
		if (!parent.getTimer().performEquipAction(mainItem))
			return;
		
		if (this.hasEquippedItem(mainItem)) {
			// this is for handling the case when the main weapon and off hand weapon have the same ID and quality
			if (secondaryItem == null && mainItem.getItemType() == Item.ItemType.WEAPON) {
				if (equipped[Inventory.EQUIPPED_MAIN_HAND] == null) {
					equipItem(mainItem);
				} else {
					unequipItem(Inventory.EQUIPPED_MAIN_HAND);
				}
				
			} else {
				unequipItem(mainItem.getID());
			}
				
			if (secondaryItem != null) {
				unequipItem(Inventory.EQUIPPED_OFF_HAND);
			}
		} else {
			equipItem(mainItem);
			
			if (secondaryItem != null) {
				equipItem(secondaryItem, Inventory.EQUIPPED_OFF_HAND);
			}
		}
	}
	
	public void giveEquippedItemAction(int slot, Creature creature) {
		if (!parent.getTimer().performAction(Game.ruleset.getValue("GiveItemCost")))
			return;
		
		Item item = equipped[slot];
		
		unequip(slot);
		
		creature.getInventory().getUnequippedItems().addItem(item, 1);
		
	}
	
	public void giveItemAction(Item item, Creature creature, int quantity) {
		if (!parent.getTimer().performAction(Game.ruleset.getValue("GiveItemCost")))
			return;
		
		this.items.removeItem(item, quantity);
		
		creature.getInventory().getUnequippedItems().addItem(item, quantity);
	}
	
	public void clear() {
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			this.unequip(i);
		}
		
		items.clear();
	}
	
	public boolean hasBothItems(Item item1, Item item2) {
		if ( item1 != null && item2 != null && item1.getID().equals(item2.getID()) && item1.getQuality().equals(item2.getQuality()) ) {
			return hasItem(item1.getID(), 2, item1.getQuality().getName());
			
			// make sure count of identical items is greater than 2
		} else {
			if (item1 != null) {
				if (!hasItem(item1)) return false;
			}
			
			if (item2 != null) {
				if (!hasItem(item2)) return false;
			}
		}
		
		return true;
	}
	
	public boolean hasItem(Item item) {
		if (item == null) return false;
		
		return hasItem(item.getID(), 1, item.getQuality().getName());
	}
	
	public boolean hasItem(String itemID, int quantity, String quality) {
		if (itemID == null) return false;
		
		int count = 0;
		
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] != null && equipped[i].getID().equals(itemID) && equipped[i].getQuality().getName().equals(quality))
				count++;
		}
		
		return items.containsItem(itemID, quantity - count, quality);
	}
	
	public boolean hasAmmoEquippedForWeapon() {
		Item weapon = getEquippedMainHand();
		
		if (weapon == null) return true;
		
		if (weapon.getWeaponType() == Item.WeaponType.THROWN  || weapon.getWeaponType() == Item.WeaponType.MELEE)
			return true;
		
		Item ammo = equipped[EQUIPPED_QUIVER];
		if (ammo == null) return false;
		
		if (ammo.getWeaponType() == weapon.getWeaponType()) return true;
		else return false;
	}
	
	public Item getOffHandWeapon() {
		Item weapon = getEquippedOffHand();
		
		if (weapon == null) return null;
		
		if (weapon.isMeleeWeapon()) return weapon;
		else return null;
	}
	
	public Item getMainWeapon() {
		Item weapon = getEquippedMainHand();
		
		if (weapon == null) return parent.getRace().getDefaultWeapon();
		else return weapon;
	}
	
	public boolean hasEquippedShield() {
		Item shield = getEquippedOffHand();
		
		return (shield != null && shield.getItemType() == Item.ItemType.SHIELD); 
	}
	
	public Item getEquippedBoots() { return equipped[EQUIPPED_BOOTS]; }
	public Item getEquippedGloves() { return equipped[EQUIPPED_GLOVES]; }
	public Item getEquippedHelmet() { return equipped[EQUIPPED_HELMET]; }
	public Item getEquippedArmor() { return equipped[EQUIPPED_ARMOR]; }
	public Item getEquippedMainHand() { return equipped[EQUIPPED_MAIN_HAND]; }
	public Item getEquippedOffHand() { return equipped[EQUIPPED_OFF_HAND]; }
	
	public Item getEquippedItem(int slot) {
		if (slot < 0 || slot >= EQUIPPED_SIZE) return null;
		
		return equipped[slot];
	}
	
	public boolean hasEquippedItem(Item item) {
		for (int i = 0; i < equipped.length; i++) {
			if (equipped[i] != null) {
				if (equipped[i].getID().equals(item.getID()) && equipped[i].getQuality().equals(item.getQuality())) return true;
			}
		}
		
		return false;
	}
	
	public int getEquippedSlot(Item item) {
		for (int i = 0; i < equipped.length; i++) {
			if (equipped[i] != null) {
				if (equipped[i].getID().equals(item.getID()) && equipped[i].getQuality().equals(item.getQuality()))
					return i;
			}
		}
		
		return -1;
	}
	
	public Item[] getEquippedItems() { return equipped; }
	
	public ItemList getUnequippedItems() {
		return items;
	}
	
	public ItemList getAllItems() {
		ItemList list = new ItemList(this.items);
		
		for (int i = 0; i < equipped.length; i++) {
			if (equipped[i] != null) list.addItem(equipped[i]);
		}
		
		return list;
	}
	
	public Item addItem(String itemID, int quantity, String quality) {
		if (itemID != null) {
			
			Item item = Game.entityManager.getItem(itemID);
			item.setQuality(Game.ruleset.getItemQuality(quality));
			addItem(item, quantity);
			
			return item;
		}
		
		return null;
	}
	
	public Item addItem(String itemID, String quality) {
		return addItem(itemID, 1, quality);
	}
	
	public Item addItemAndEquip(String itemID, String quality) {
		Item item = addItem(itemID, quality);
		equipItem(item);
		
		return item;
	}
	
	public void addItemAndEquip(Item item) {
		addItem(item);
		equipItem(item);
	}
	
	public void addItem(Item item) {
		if (item != null) {
			items.addItem(item);
			
			if (item.hasScript()) 
				item.getScript().executeFunction(ScriptFunctionType.onAddItem, this.parent, item);
		}
	}
	
	public void addItem(Item item, int quantity) {
		if (item != null) {
			items.addItem(item, quantity);
			
			if (item.hasScript()) 
				item.getScript().executeFunction(ScriptFunctionType.onAddItem, this.parent, item);
		}
	}
	
	public void removeItem(Item item) {
		if (item != null) items.removeItem(item);
	}
	
	public void removeItem(int index) {
		if (items.size() > index && index >= 0) items.removeItem(index);
	}
	
	public Item getItem(int index) {
		if (items.size() > index && index >= 0) return items.getItem(index);
		
		return null;
	}
	
	public int getQuantity(String itemID) {
		int count = 0;
		
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] != null && equipped[i].getID().equals(itemID)) {
				count++;
			}
		}
		
		return count + items.getQuantity(itemID);
	}
	
	public int getQuantity(Item item) {
		int count = 0;
		
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] != null && equipped[i].getID().equals(item.getID()) && equipped[i].getQuality().equals(item.getQuality())) {
				count++;
			}
		}
		
		return count + items.getQuantity(item);
	}
	
	public int size() { return items.size(); }
	
	public Weight getTotalWeight() {
		return new Weight(getTotalWeightInGrams());
	}
	
	public int getTotalWeightInGrams() {
		int totalGrams = 0;
		
		for (int i = 0; i < EQUIPPED_SIZE; i++) {
			if (equipped[i] != null)
				totalGrams += equipped[i].getWeight().grams;
		}
		
		for (int i = 0; i < items.size(); i++) {
			totalGrams += items.getItem(i).getWeight().grams * items.getQuantity(i);
		}
		
		return totalGrams;
	}
	
	/**
	 * Checks if all currently stored items in this inventory are valid for the
	 * current campaign.  Any items that are not valid are discarded.
	 */
	
	public void checkAllItemsValid() {
		for (int i = 0; i < equipped.length; i++) {
			Item item = equipped[i];
			
			if (item == null) continue;
			
			if (Game.entityManager.getItem(item.getID()) == null) {
				unequip(i);
			}
		}
		
		for (int i = 0; i < items.size(); i++) {
			String id = items.getItemID(i);
			
			if ( Game.entityManager.getItem(id) == null) {
				items.removeItem(i, items.getQuantity(i));
				i--;
			}
		}
	}
}
