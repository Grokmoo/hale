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

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.widgets.RightClickMenu;

/**
 * A quickbar slot for holding an equippable item.  When activated, the
 * specified Item is equipped if not currently equipped and unequipped
 * if currently equipped.
 * 
 * @author Jared Stephen
 *
 */

public class ItemEquipSlot extends QuickbarSlot {
	private Item item;
	private Item secondaryItem;
	private Creature parent;
	
	private Color spriteColor;
	
	private Color secondaryColor;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("type", "equip");
		data.put("itemID", item.getID());
		data.put("itemQuality", item.getQuality().getName());
		
		if (secondaryItem != null) {
			data.put("secondaryItemID", secondaryItem.getID());
			data.put("secondaryItemQuality", secondaryItem.getQuality().getName());
		}
		
		return data;
	}
	
	/**
	 * Creates a new ItemEquipSlot for the specified Item with the parent Creature
	 * @param item the item to equip / unequip
	 * @param parent the parent that will be equipping or unequipping the item
	 */
	
	public ItemEquipSlot(Item item, Creature parent) {
		this.item = item;
		this.parent = parent;
		this.spriteColor = item.getIconColor();
		if (spriteColor == null) spriteColor = Color.WHITE;
		
		secondaryColor = Color.WHITE;
	}
	
	/**
	 * Returns the Item that this ItemEquipSlot is holding.
	 * 
	 * @return the Item that this ItemEquipSlot is holding.
	 */
	
	public Item getItem() {
		return item;
	}
	
	/**
	 * Attempts to set the secondary item for this ItemEquipSlot to the specified Item.
	 * This is only possible in very specific scenarios: if the combination of the primary
	 * and secondary item can be wielded in the parent Creature's two hands.
	 * 
	 * @param secondaryItem the item to attempt to add
	 * @return true if the secondary item is added successfully, false otherwise
	 */
	
	public boolean setSecondaryItem(Item secondaryItem) {
		if (secondaryItem == null) return false;
		
		// if a secondary item is already set, this causes trying to add another
		// item to reset the slot
		if (this.secondaryItem != null) return false;
		
		if (!parent.getInventory().hasPrereqsToEquip(item)) return false;
		if (!parent.getInventory().hasPrereqsToEquip(secondaryItem)) return false;
		
		// make sure the player didn't drop the same item on the slot twice
		if (!parent.getInventory().hasBothItems(item, secondaryItem)) return false;
		
		switch (item.getItemType()) {
		case WEAPON:
			switch (secondaryItem.getItemType()) {
			case WEAPON:
				return checkWeaponWeapon(item, secondaryItem);
			case SHIELD:
				return checkWeaponShield(item, secondaryItem);
			}
		case SHIELD:
			switch (secondaryItem.getItemType()) {
			case WEAPON:
				return checkWeaponShield(secondaryItem, item);
			}
		}
		
		return false;
	}
	
	private boolean checkWeaponWeapon(Item main, Item offHand) {
		if (!main.isMeleeWeapon() || !offHand.isMeleeWeapon()) return false;
		
		if (!parent.stats().has(Bonus.Type.DualWieldTraining)) return false;
		
		switch (main.getWeaponHandedForCreature(parent)) {
		case TWO_HANDED: case NONE: return false;
		}
		
		switch (offHand.getWeaponHandedForCreature(parent)) {
		case TWO_HANDED: case NONE: return false;
		}
		
		// checks are ok, so set the main and secondary items and sprites
		item = main;
		spriteColor = item.getIconColor();
		if (spriteColor == null) spriteColor = Color.WHITE;
		
		secondaryItem = offHand;
		secondaryColor = secondaryItem.getIconColor();
		if (secondaryColor == null) secondaryColor = Color.WHITE;
		
		return true;
	}
	
	private boolean checkWeaponShield(Item weapon, Item shield) {
		switch (weapon.getWeaponHandedForCreature(parent)) {
		case TWO_HANDED: case NONE: return false;
		}
		
		// checks are ok, so set the main and secondary items and sprites
		item = weapon;
		spriteColor = item.getIconColor();
		if (spriteColor == null) spriteColor = Color.WHITE;
		
		secondaryItem = shield;
		secondaryColor = secondaryItem.getIconColor();
		if (secondaryColor == null) secondaryColor = Color.WHITE;
		
		return true;
	}
	
	@Override public Sprite getSprite() {
		return SpriteManager.getSprite(item.getIcon());
	}

	@Override public Color getSpriteColor() {
		return spriteColor;
	}
	
	@Override public String getLabelText() {
		switch (item.getItemType()) {
		case AMMO: return Integer.toString(parent.getInventory().getQuantity(item));
		default: return "";
		}
	}

	@Override public boolean isChildActivateable() {
		if (parent.getInventory().hasEquippedItem(item) && item.isCursed())
			return false;
		
		if (!parent.getInventory().canUnequipCurrentItemInSlot(item, 0)) return false;
		
		if (secondaryItem != null && parent.getInventory().hasEquippedItem(secondaryItem) &&
				secondaryItem.isCursed())
			return false;
		
		return parent.getInventory().canEquip(item);
	}

	@Override public void childActivate() {
		if (!parent.getInventory().canEquip(item)) return;
		
		if (!parent.getInventory().canUnequipCurrentItemInSlot(item, 0)) return;
		
		if (secondaryItem != null) {
			if (!parent.getInventory().hasBothItems(item, secondaryItem)) return;
			
			parent.getInventory().doubleEquipmentAction(item, secondaryItem);
		} else {
			
			if (!parent.getInventory().hasItem(item)) return;
			
			if (parent.getInventory().hasEquippedItem(item)) {
				parent.getInventory().getCallbackFactory().getUnequipCallback(item).run();
			} else {
				parent.getInventory().getCallbackFactory().getEquipCallback(item).run();
			}
		}
	}
	
	@Override public void createRightClickMenu(QuickbarSlotButton widget) {
		RightClickMenu menu = Game.mainViewer.getMenu();
		
		String menuTitle = item.getName();
		if (secondaryItem != null) menuTitle = menuTitle + " and " + secondaryItem.getName();
		
		menu.addMenuLevel(menuTitle);
		
		String disabledTooltip = null;
		String activateText = null;
		if (parent.getInventory().hasEquippedItem(item)) {
			activateText = "Unequip";
			if (item.isCursed()) disabledTooltip = "Item is cursed and cannot be removed.";
			else disabledTooltip = "Not enough AP to unequip";
			
		}
		else {
			activateText = "Equip";
			disabledTooltip = "Not enough AP to equip";
		}
		
		Button activate = new Button(activateText);
		activate.setEnabled(isActivateable());
		if (!activate.isEnabled()) activate.setTooltipContent(disabledTooltip);
		activate.addCallback(new QuickbarSlotButton.ActivateSlotCallback(this));
		menu.addButton(activate);
		
		
		Button examine = new Button(item.getName() + " Details");
		Runnable cb = parent.getInventory().getCallbackFactory().getDetailsCallback(item,
				menu.getX(), menu.getY());
		examine.addCallback(cb);
		menu.addButton(examine);
		
		if (secondaryItem != null) {
			Button examineS = new Button(secondaryItem.getName() + " Details");
			Runnable cbS = parent.getInventory().getCallbackFactory().getDetailsCallback(secondaryItem,
					menu.getX(), menu.getY());
			examineS.addCallback(cbS);
			menu.addButton(examineS);
		}
		
		Button clearSlot = new Button("Clear Slot");
		clearSlot.addCallback(new QuickbarSlotButton.ClearSlotCallback(widget));
		menu.addButton(clearSlot);
		
		menu.show();
		// show popup immediately
		if (menu.shouldPopupToggle()) {
			menu.togglePopup();
		}
	}
	
	@Override public String getTooltipText() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Equip ");
		sb.append(item.getFullName());
		if (secondaryItem != null) {
			sb.append(" and ");
			sb.append(secondaryItem.getFullName());
		}
		
		return sb.toString();
	}

	@Override public Sprite getSecondarySprite() {
		if (secondaryItem != null) return SpriteManager.getSprite(secondaryItem.getIcon());
		else return null;
	}

	@Override public Color getSecondarySpriteColor() {
		return secondaryColor;
	}

	@Override public String getSaveDescription() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Equip ");
		
		sb.append("\"");
		sb.append(item.getID());
		sb.append("\" \"");
		sb.append(item.getQuality().getName());
		sb.append("\"");
		
		if (secondaryItem != null) {
			sb.append(" \"");
			sb.append(secondaryItem.getID());
			sb.append("\" \"");
			sb.append(secondaryItem.getQuality().getName());
			sb.append("\"");
		}
		
		return sb.toString();
	}

	@Override
	public QuickbarSlot getCopy(Creature parent) {
		ItemEquipSlot slot = new ItemEquipSlot(new Item(this.item), parent);
		
		if (this.secondaryItem != null)
			slot.setSecondaryItem(new Item(this.secondaryItem));
		
		return slot;
	}

}
