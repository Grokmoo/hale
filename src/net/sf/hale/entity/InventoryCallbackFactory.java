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

import de.matthiasmann.twl.Button;

import net.sf.hale.Game;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Merchant;
import net.sf.hale.view.ItemDetailsWindow;
import net.sf.hale.widgets.RightClickMenu;
import net.sf.hale.widgets.MultipleItemPopup;

/**
 * Factory class for inventory callbacks.  Contains methods to create
 * callbacks for all of the inventory and merchant right click menu
 * options.
 * 
 * @author Jared Stephen
 *
 */

public class InventoryCallbackFactory {
	private Inventory inventory;
	
	/**
	 * Creates a new InventoryCallbackFactory with the specified parent Inventory.
	 * 
	 * @param inventory the parent inventory
	 */
	
	public InventoryCallbackFactory(Inventory inventory) {
		this.inventory = inventory;
	}
	
	/**
	 * Returns a callback that, when run, will use the specified Item
	 * in this inventory.
	 * @param item the item to use
	 * @return the use callback
	 */
	
	public Runnable getUseCallback(Item item) {
		return new UseCallback(item);
	}
	
	/**
	 * Returns a callback that, when run, will equip both the main hand
	 * and off hand items as one action.  This allows the player to equip
	 * a sword and shield, or two swords at once, for example.
	 * 
	 * @param mainHand the main hand item to equip
	 * @param offHand the off hand item to equip
	 * @return the double equip callback
	 */
	
	public Runnable getDoubleEquipCallback(Item mainHand, Item offHand) {
		return new DoubleEquipCallback(mainHand, offHand);
	}
	
	/**
	 * Returns a callback that when run will equip the specified item to the
	 * specified inventory slot if possible
	 * @param item the item to equip
	 * @param slot the inventory slot to equip the item to
	 * @param containerIndex the index of the item in the currently open container, or -1 if
	 * the item is not in a container
	 * @return the callback
	 */
	
	public Runnable getEquipSlotCallback(Item item, int slot, int containerIndex) {
		return new EquipSlotCallback(item, slot, containerIndex);
	}
	
	/**
	 * Returns an equip callback that, when run, will equip the specified
	 * Item in this inventory.
	 * 
	 * @param item the item to equip
	 * @return an equip callback
	 */
	
	public Runnable getEquipCallback(Item item) {
		return new EquipCallback(item, true, false);
	}
	
	/**
	 * Returns a callback that, when run, will equip the specified
	 * Item in this inventory as an off hand weapon.  This is only
	 * possible for one handed or light weapons.
	 * 
	 * @param item the item to equip off hand
	 * @return the equip callback
	 */
	
	public Runnable getEquipOffHandCallback(Item item) {
		return new EquipCallback(item, true, true);
	}
	
	/**
	 * Returns a callback that, when run, will unequip the specified
	 * Item from this inventory.
	 * @param item the item to unequip
	 * @return the unequip callback
	 */
	
	public Runnable getUnequipCallback(Item item) {
		return new EquipCallback(item, false, false);
	}
	
	/**
	 * Returns a callback that, when run, will display the details
	 * window for the specified Item centered at the specified
	 * mouse coordinates
	 * 
	 * @param item the Item to view details for
	 * @return the details callback
	 */
	
	public Runnable getDetailsCallback(Item item, int x, int y) {
		return new ExamineDetailsCallback(item, x, y);
	}
	
	/**
	 * Returns a callback that, when run, will take the specified Item
	 * at the specified ItemList index from the currently open container.
	 * If maxQuantity is 1, only a single item will be taken.  Otherwise,
	 * a popup will be opened allowing the user to select a quantity
	 * to take.
	 * 
	 * @param item the Item to take
	 * @param index the index of the Item in the Container's ItemList
	 * @param maxQuantity the maximum number of items that can be taken
	 * @return the take item callback
	 */
	
	public Runnable getTakeCallback(Item item, int index, int maxQuantity) {
		return new TakeCallback(item, maxQuantity, index);
	}
	
	/**
	 * Returns a callback that, when run, will take the specified Item
	 * at the specified ItemList index from the currently open container.
	 * The item will then be wielded by the owner of this Inventory.  This
	 * is only possible for weapons and shields.
	 * 
	 * @param item the item to pickup and wield
	 * @param index the index of the Item in the Container's ItemList
	 * @return the take and wield callback
	 */
	
	public Runnable getTakeAndWieldCallback(Item item, int index) {
		return new TakeAndWieldCallback(item, index);
	}
	
	/**
	 * Returns a callback that, when run, will drop the Item at the
	 * specified index in the Inventory's ItemList from the owning
	 * Creature.  If the ContainerWindow is open, the Item will go
	 * in the Container, otherwise, it will go on the ground under
	 * the Creature.  If maxQuantity is 1, a single item will be
	 * dropped.  Otherwise, a popup will be opened allowing the user
	 * to specify the quantity of items to drop.
	 * 
	 * @param index the ItemList index of the item to be dropped
	 * @param maxQuantity the maximum number of items to drop
	 * @return the drop callback
	 */
	
	public Runnable getDropCallback(int index, int maxQuantity) {
		return new DropCallback(inventory.getItem(index), maxQuantity, index);
	}
	
	/**
	 * Returns a callback that when run, will unequip and drop the item
	 * in the specified equipped item slot.  If the container window is open
	 * the item will go in the container, otherwise it will go on the ground
	 * below the owner's feet
	 * @param slot the equipped slot to drop
	 * @return the callback
	 */
	
	public Runnable getDropEquippedCallback(int slot) {
		return new DropEquippedCallback(slot);
	}
	
	/**
	 * Returns a callback that, when run, will buy the specified Item
	 * from the specified merchant for the owner of this Inventory.  If
	 * maxQuantity is 1, only a single item will be bought.  Otherwise, a
	 * popup will be opened allowing the user to select a quantity to buy.
	 * 
	 * @param item the Item to buy
	 * @param merchant the Merchant to buy from
	 * @param maxQuantity the maximum number of items to buy
	 * @return the buy callback
	 */
	
	public Runnable getBuyCallback(Item item, Merchant merchant, int maxQuantity) {
		return new BuyCallback(item, maxQuantity, merchant);
	}
	
	/**
	 * Returns a callback, that, when run, will sell the specified Item
	 * to the specified merchant from the owner of this Inventory.  If
	 * maxQuantity is 1, only a single item will be sold.  Otherwise, a
	 * popup will be opened allowing the user to select a quantity to sell.
	 * 
	 * @param item the Item to sell
	 * @param merchant the Merchant to sell to
	 * @param maxQuantity the maximum number of items that can be sold
	 * @return the sell callback
	 */
	
	public Runnable getSellCallback(Item item, Merchant merchant, int maxQuantity) {
		return new SellCallback(item, maxQuantity, merchant);
	}
	
	/**
	 * Returns a callback that when run will sell the specified equipped item
	 * to the specified merchant from the owner of this inventory
	 * @param slot the equipped item slot to sell
	 * @param merchant the merchant to sell to
	 * @return the sell callback
	 */
	
	public Runnable getSellEquippedCallback(int slot, Merchant merchant) {
		return new SellEquippedCallback(slot, merchant);
	}
	
	/**
	 * Returns a callback that, when run, will show the menu of possible
	 * targets for a give action.  When a menu selection is made, either a
	 * single item will be given if the maxQuantity is 1, or a popup will
	 * be opened allowing the user to specify a quantity
	 * 
	 * @param item the Item to give
	 * @param maxQuantity the maximum selectable quantity to give
	 * @return the give menu opener callback
	 */
	
	public Runnable getGiveCallback(Item item, int maxQuantity) {
		return new GiveMenuCallback(item, maxQuantity);
	}
	
	/**
	 * Returns a callback that when run, will give the item to the specified creature.
	 * If maxQuantity is greater than 1, will first open a popup window to confirm the
	 * quantity
	 * @param item the item to give
	 * @param maxQuantity the maximum selectable quantity to give
	 * @param target the target to give the item to
	 * @return the give callback
	 */
	
	public Runnable getGiveCallback(Item item, int maxQuantity, Creature target) {
		return new GiveCallback(item, maxQuantity, target);
	}
	
	/**
	 * Returns a callback that, when run, gives the item in the specified equipped slot
	 * to the specified target
	 * @param slot the inventory slot for the equipped item
	 * @param target the target to give the item to
	 * @return the runnable give callback
	 */
	
	public Runnable getGiveEquippedCallback(int slot, Creature target) {
		return new GiveEquippedCallback(slot, target);
	}
	
	private class UseCallback implements Runnable {
		private Item itemToUse;
		
		private UseCallback(Item itemToUse) {
			this.itemToUse = itemToUse;
		}
		
		@Override public void run() {
			itemToUse.use(inventory.getParent());
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class DoubleEquipCallback implements Runnable {
		
		private Item mainHand, offHand;
		
		private DoubleEquipCallback(Item mainHand, Item offHand) {
			this.mainHand = mainHand;
			this.offHand = offHand;
		}
		
		@Override public void run() {
			inventory.doubleEquipmentAction(mainHand, offHand);
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class EquipSlotCallback implements Runnable {
		private Item item;
		private int slot;
		private int containerIndex;
		
		private EquipSlotCallback(Item item, int slot, int containerIndex) {
			this.item = item;
			this.slot = slot;
			this.containerIndex = containerIndex;
		}
		
		@Override public void run() {
			if (containerIndex != -1) {
				inventory.pickupItemAction(item);
			}
			
			if (inventory.equipAction(item, slot) && containerIndex != -1) {
				Container container = Game.mainViewer.containerWindow.getContainer();
				container.removeItem(containerIndex);
				if (container.isEmpty() &&
						container.getDescription().equals(Game.ruleset.getString("TempContainerDescription"))) {
					Game.curCampaign.curArea.removeEntity(container);
				}
			}
		}
	}
	
	private class EquipCallback implements Runnable {
		private Item itemToEquip;
		private boolean equipOffHand;
		private boolean equip;
		
		/*
		 * Creates a new EquipCallback.  The specified item is equipped or unequipped in the specified
		 * inventory.  If equip is true, the item is equipped, if false, the item is unequiped.  If
		 * equipOffHand is true, the item is equipped to the off hand slot, if false, it is equipped
		 * normally.
		 */
		
		private EquipCallback(Item itemToEquip, boolean equip, boolean equipOffHand) {
			this.itemToEquip = itemToEquip;
			this.equip = equip;
			this.equipOffHand = equipOffHand;
		}
		
		@Override public void run() {
			inventory.equipmentAction(itemToEquip, equip, equipOffHand);
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	public static class ExamineDetailsCallback implements Runnable {
		private Item item;
		private int x, y;
		
		public ExamineDetailsCallback(Item item, int x, int y) {
			this.item = item;
			this.x = x;
			this.y = y;
		}
		
		@Override public void run() {
			ItemDetailsWindow window = new ItemDetailsWindow(item);
			window.setPosition(x - window.getWidth() / 2, y - window.getHeight() / 2);
			Game.mainViewer.add(window);
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class TakeAndWieldCallback implements Runnable {
		protected Item item;
		private int itemIndex;
		
		private TakeAndWieldCallback(Item item, int itemIndex) {
			this.item = item;
			this.itemIndex = itemIndex;
		}
		
		@Override public void run() {
			inventory.pickupAndWieldAction(item);
			Container container = Game.mainViewer.containerWindow.getContainer();
			container.removeItem(itemIndex);
    		if (container.isEmpty() &&
    				container.getDescription().equals(Game.ruleset.getString("TempContainerDescription"))) {
    			Game.curCampaign.curArea.removeEntity(container);
    		}
    		
			Game.mainViewer.getMenu().hide();
		}
	}
	
	/*
	 * Opens the menu to show give targets.  Doesn't directly
	 * give any item.
	 */
	
	private class GiveMenuCallback implements Runnable {
		private Item item;
		private int multipleMax;
		
		private GiveMenuCallback(Item item, int multipleMax) {
			this.item = item;
			this.multipleMax = multipleMax;
		}
		
		@Override public void run() {
			RightClickMenu menu = Game.mainViewer.getMenu();
			menu.removeMenuLevelsAbove(1);
			menu.addMenuLevel("Give");
			for (Creature c : Game.curCampaign.party) {
				if (c == inventory.getParent() || c.isSummoned()) continue;
				
				Button button = new Button();
				button.setText(c.getName());
				button.addCallback(new GiveCallback(item, multipleMax, c));
				
				menu.addButton(button);
			}
			
			menu.show();
		}
	}
	
	/*
	 * Base class for all MultipleItemCallbacks.  The single item callbacks
	 * are just special cases where the maxQuantity is 1.
	 */
	
	private abstract class MultipleCallback implements MultipleItemPopup.Callback {
		protected final Item item;
		private final int maxQuantity;
		private final String labelText;
		
		private MultipleCallback(Item item, int maxQuantity, String labelText) {
			this.item = item;
			this.maxQuantity = maxQuantity;
			this.labelText = labelText;
		}
		
		@Override public void run() {
			if (maxQuantity == 1) {
				performItemAction(1);
			} else {
				MultipleItemPopup popup = new MultipleItemPopup(Game.mainViewer);
				popup.openPopupCentered(this);
			}
		}
		
		@Override public String getLabelText() { return labelText; }
		
		@Override public int getMaximumQuantity() { return maxQuantity; }
		
		@Override public String getValueText(int quantity) { return ""; }
	}
	
	private class SellEquippedCallback implements Runnable {
		private int slot;
		private Merchant merchant;
		
		private SellEquippedCallback(int slot, Merchant merchant) {
			this.slot = slot;
			this.merchant = merchant;
		}
		
		@Override public void run() {
			Item item = inventory.getEquippedItem(slot);
			if (item.isQuestItem()) return;
			
			inventory.unequipItem(slot);
			
			merchant.buyItem(item, inventory.getParent());
		}
	}
	
	private class DropEquippedCallback implements Runnable {
		private int slot;
		
		private DropEquippedCallback(int slot) {
			this.slot = slot;
		}
		
		@Override public void run() {
			Item item = inventory.getEquippedItem(slot);
			if (item.isQuestItem()) return;
			
			Container container = Game.mainViewer.containerWindow.getContainer();
			if (container != null)
				item.setPosition(container.getX(), container.getY());
			else
				item.setPosition(inventory.getParent().getX(), inventory.getParent().getY());
			
			Game.curCampaign.curArea.addItem(item, 1);
			
			inventory.dropEquippedItemAction(slot);
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class GiveEquippedCallback implements Runnable {
		private Creature target;
		private int slot;
		
		private GiveEquippedCallback(int slot, Creature target) {
			this.slot = slot;
			this.target = target;
		}
		
		@Override public void run() {
			inventory.giveEquippedItemAction(slot, target);
		}
	}
	
	/*
	 * The callback that actually gives the item to the target.  If maxQuantity
	 * is greater than 1, it first opens a popup window in order to determine 
	 * the quantity.
	 */
	
	private class GiveCallback extends MultipleCallback {
		private Creature target;
		
		private GiveCallback(Item item, int maxQuantity, Creature target) {
			super(item, maxQuantity, "Give");
			this.target = target;
		}

		@Override public void performItemAction(int quantity) {
			inventory.giveItemAction(item, target, quantity);
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class BuyCallback extends MultipleCallback {
		private Merchant merchant;
		
		private BuyCallback(Item item, int maxQuantity, Merchant merchant) {
			super(item, maxQuantity, "Buy");
			this.merchant = merchant;
		}

		@Override public String getValueText(int quantity) {
			int percent = merchant.getCurrentSellPercentage();
			return "Price: " + Currency.getPlayerBuyCost(item, quantity, percent).shortString();
		}

		@Override public void performItemAction(int quantity) {
			merchant.sellItem(item, inventory.getParent(), quantity);
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class SellCallback extends MultipleCallback {
		private Merchant merchant;
		
		private SellCallback(Item item, int maxQuantity, Merchant merchant) {
			super(item, maxQuantity, "Sell");
			this.merchant = merchant;
		}
		
		@Override public String getValueText(int quantity) {
			int percent = merchant.getCurrentBuyPercentage();
			return "Price: " + Currency.getPlayerSellCost(item, quantity, percent).shortString();
		}
		
		@Override public void performItemAction(int quantity) {
			merchant.buyItem(item, inventory.getParent(), quantity);
			
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class TakeCallback extends MultipleCallback {
		private int itemIndex;
		
		private TakeCallback(Item item, int maxQuantity, int itemIndex) {
			super(item, maxQuantity, "Take");
			this.itemIndex = itemIndex;
		}
		
		@Override public void performItemAction(int quantity) {
			inventory.pickupItemAction(item, quantity);
			
			Container container = Game.mainViewer.containerWindow.getContainer();
			container.removeItem(itemIndex, quantity);
    		if (container.isEmpty() &&
    				container.getDescription().equals(Game.ruleset.getString("TempContainerDescription"))) {
    			Game.curCampaign.curArea.removeEntity(container);
    		}
    		
			Game.mainViewer.getMenu().hide();
		}
	}
	
	private class DropCallback extends MultipleCallback {
		private int itemIndex;
		
		private DropCallback(Item item, int maxQuantity, int itemIndex) {
			super(item, maxQuantity, "Drop");
			this.itemIndex = itemIndex;
		}
		
		@Override public void performItemAction(int quantity) {
			// if the container window is open, drop it in the container,
			// otherwise drop it at the creature's feet
			Container container = Game.mainViewer.containerWindow.getContainer();
			if (container != null)
				item.setPosition(container.getX(), container.getY());
			else
				item.setPosition(inventory.getParent().getX(), inventory.getParent().getY());
			
			Game.curCampaign.curArea.addItem(item, quantity);
			
			inventory.dropItemAction(itemIndex, quantity);
			
			Game.mainViewer.getMenu().hide();
		}
	}
}
