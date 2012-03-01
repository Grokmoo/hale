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

package net.sf.hale.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.InventoryCallbackFactory;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.Merchant;
import net.sf.hale.widgets.ItemIconHover;
import net.sf.hale.widgets.ItemIconViewer;
import net.sf.hale.widgets.RightClickMenu;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * Widget for viewing the items contained within an ItemList.  The includes viewing
 * the contents of a container, a creature's inventory, or a merchant's wares.
 * @author Jared Stephen
 *
 */

public class ItemListViewer extends Widget implements ItemIconViewer.Listener, DropTarget {
	/**
	 * Controls the types of actions available for each ItemIconViewer
	 * @author Jared Stephen
	 *
	 */
	public enum Mode {
		/** Viewing the inventory of a creature */
		INVENTORY,
		
		/** Viewing a merchant's wares */
		MERCHANT,
		
		/** Viewing the contents of a container */
		CONTAINER
	};
	
	private enum Filter {
		All, Weapons, Armor, Apparel, Ammo, Misc, Usable, Ingredients, Traps, Quest;
	};
	
	private int gridGap;
	
	private List<ToggleButton> filterButtons;
	
	private final ScrollPane scrollPane;
	private final Content content;
	
	private List<ItemIconViewer> viewers;
	
	private Mode mode;
	private Creature creature;
	private Merchant merchant;
	private ItemList items;
	private InventoryCallbackFactory callbackFactory;
	
	private Filter activeFilter;
	private ToggleButton activeButton;
	
	private List<ItemIconHover> itemHovers;
	
	/**
	 * Creates a new ItemList viewer with no items yet viewed.  You must use
	 * updateContent to set the ItemList and mode
	 */
	
	public ItemListViewer() {
		itemHovers = new ArrayList<ItemIconHover>();
		
		viewers = new ArrayList<ItemIconViewer>();
		
		content = new Content();
		scrollPane = new ScrollPane(content);
		scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		scrollPane.setExpandContentSize(true);
		scrollPane.setTheme("itemspane");
		this.add(scrollPane);
		
		filterButtons = new ArrayList<ToggleButton>();
		for (Filter filter : Filter.values()) {
			ToggleButton button = new ToggleButton();
			button.setTheme(filter.toString().toLowerCase() + "filter");
			button.addCallback(new FilterButtonCallback(button, filter));
			
			add(button);
			filterButtons.add(button);
		}
		
		// initially set the "All" filter to active
		activeButton = filterButtons.get(0);
		activeFilter = Filter.All;
		activeButton.setActive(true);
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		gridGap = themeInfo.getParameter("gridGap", 0);
	}
	
	@Override protected void layout() {
		super.layout();
		
		int curX = getInnerX();
		int curY = getInnerY();
		int maxRowBottom = 0;
		for (ToggleButton button : filterButtons) {
			button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
			
			if (curX + button.getWidth() > getInnerRight()) {
				// move to the next row
				curX = getInnerX();
				curY = maxRowBottom;
			}
			
			button.setPosition(curX, curY);
			
			curX = button.getRight();
			maxRowBottom = Math.max(maxRowBottom, button.getBottom());
		}
		
		scrollPane.setPosition(getInnerX(), maxRowBottom);
		scrollPane.setSize(getInnerWidth(), Math.max(0, getInnerBottom() - maxRowBottom));
	}
	
	private void applyModeToViewer(ItemIconViewer viewer, Item item) {
		boolean prof = false;
		switch (item.getItemType()) {
		case SHIELD: case ARMOR: case GLOVES: case HELMET: case BOOTS:
			if (!creature.stats().hasArmorProficiency(item.getArmorType().getName())) prof = true;
			break;
		case WEAPON:
			if (!creature.stats().hasWeaponProficiency(item.getBaseWeapon().getName())) prof = true;
			break;
		}
		viewer.setStateProficiencies(prof);
		
		boolean unafford = false;
		
		switch (mode) {
		case MERCHANT:
			int maxAffordable = Game.curCampaign.getPartyCurrency().getMaxNumberAffordable(item,
					merchant.getCurrentSellPercentage());
			unafford = maxAffordable < 1;
			break;
		}
		viewer.setStateUnaffordable(unafford);
	}
	
	private void updateViewers() {
		int viewerIndex = 0;
		for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
			Item item = items.getItem(itemIndex);
			if (!itemMatchesFilter(item)) continue;
			
			ItemIconViewer viewer;
			if (viewerIndex == viewers.size()) {
				// if there aren't enough viewers, add a new one
				viewer = new ItemIconViewer(this);
				viewer.setListener(this);
				
				content.add(viewer);
				viewers.add(viewer);
			} else {
				viewer = viewers.get(viewerIndex);
			}
			
			viewer.setItemIndex(itemIndex);
			
			switch (mode) {
			case INVENTORY:
				viewer.setItem(item, items.getQuantity(itemIndex), creature, null, null);
				break;
			case CONTAINER:
				viewer.setItem(item, items.getQuantity(itemIndex), null, Game.mainViewer.containerWindow.getContainer(), null);
				break;
			case MERCHANT:
				viewer.setItem(item, items.getQuantity(itemIndex), null, null, merchant);
				break;
			}
			
			applyModeToViewer(viewer, item);
			
			viewerIndex++;
			
		}
		
		// if there are too many viewers remove them
		for (int i = viewers.size() - 1; i >= viewerIndex; i--) {
			content.removeChild(i);
			viewers.remove(i);
		}
	}
	
	/**
	 * Sets this ItemListViewer as operating in the specified mode viewing the specified ItemList.
	 * Creature and Merchant are optional parameters used in the Creature, Container, and Merchant modes
	 * @param mode the mode that this ItemListViewer is operating in
	 * @param creature the parent Creature that owns the ItemList in Mode Inventory and Merchant and
	 * is interacting with the container in mode Container
	 * @param merchant the merchant that the parent is trading with in Mode Merchant
	 * @param items the List of items to view
	 */
	
	public void updateContent(Mode mode, Creature creature, Merchant merchant, ItemList items) {
		this.mode = mode;
		this.creature = creature;
		this.merchant = merchant;
		this.items = items;
		this.callbackFactory = creature.getInventory().getCallbackFactory();
		
		updateViewers();
	}
	
	/**
	 * Adds an item hover for the corresponding equipped item for the specified viewer
	 * @param viewer the item viewer to add the hover for
	 */
	
	private void addEquippedHovers(ItemIconViewer viewer) {
		Item item = viewer.getItem();
		if (item == null) return;
		if (!Game.mainViewer.inventoryWindow.isVisible()) return;
		
		InventoryWindow inv = Game.mainViewer.inventoryWindow;
		
		List<ItemIconViewer> viewersToAdd = new ArrayList<ItemIconViewer>();
		
		switch (item.getItemType()) {
		case WEAPON:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_MAIN_HAND));
			
			ItemIconViewer offHand = inv.getEquippedViewer(Inventory.EQUIPPED_OFF_HAND);
			if (offHand.getItem() != null && offHand.getItem().getItemType() == Item.ItemType.WEAPON)
				viewersToAdd.add(offHand);
			break;
		case ARMOR:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_ARMOR));
			break;
		case GLOVES:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_GLOVES));
			break;
		case HELMET:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_HELMET));
			break;
		case CLOAK:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_CLOAK));
			break;
		case BOOTS:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_BOOTS));
			break;
		case BELT:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_BELT));
			break;
		case AMULET:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_AMULET));
			break;
		case RING:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_RING_RIGHT));
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_RING_LEFT));
			break;
		case AMMO:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_QUIVER));
			break;
		case SHIELD:
			viewersToAdd.add(inv.getEquippedViewer(Inventory.EQUIPPED_OFF_HAND));
		}
		
		for (ItemIconViewer equippedViewer : viewersToAdd) {
			// make a viewer with the source widget the original viewer
			addHover(Mode.INVENTORY, equippedViewer.getItem(), viewer, equippedViewer.getEmptyHoverText(),
					equippedViewer.getX(), equippedViewer.getY());
		}
	}
	
	private void addHover(Mode mode, Item item, ItemIconViewer viewer, String emptyText, int x, int y) {
		ItemIconHover hover = new ItemIconHover(item, viewer);
		hover.setEmptyHoverText(emptyText);
		
		// set mode specific information
		switch (mode) {
		case INVENTORY:
			if (merchant != null)
				hover.setValue("Sell Price", merchant.getCurrentBuyPercentage());
			break;
		case MERCHANT:
			hover.setValue("Buy Price", merchant.getCurrentSellPercentage());
			break;
		}
		
		// set type specific information
		if (item != null) {
			switch (item.getItemType()) {
			case SHIELD: case ARMOR: case GLOVES: case HELMET: case BOOTS:
				if ( !creature.stats().hasArmorProficiency(item.getArmorType().getName()) ) {
					hover.setRequiresText("Armor Proficiency: " + item.getArmorType().getName());
				}
				break;
			case WEAPON:
				if ( !creature.stats().hasWeaponProficiency(item.getBaseWeapon().getName()) ) {
					hover.setRequiresText("Weapon Proficiency: " + item.getBaseWeapon().getName());
				}
				break;
			}
		}
		
		hover.updateText();
		
		itemHovers.add(hover);
		
		// add and set the widget position and size
		this.getGUI().getRootPane().add(hover);
		hover.setSize(hover.getPreferredWidth(), hover.getPreferredHeight());
		hover.setPosition(x, y - hover.getHeight());
	}
	
	/**
	 * Removes all mouse over hovers for item icons
	 */
	
	public void clearAllItemHovers() {
		// clean up all old hovers
		Iterator<ItemIconHover> iter = itemHovers.iterator();
		while (iter.hasNext()) {
			ItemIconHover hover = iter.next();
			iter.remove();
			hover.getParent().removeChild(hover);
		}
	}
	
	@Override public void hoverStarted(ItemIconViewer viewer) {
		clearAllItemHovers();
				
		addEquippedHovers(viewer);
		addHover(this.mode, viewer.getItem(), viewer, viewer.getEmptyHoverText(), viewer.getX(), viewer.getY());
	}
	
	@Override public void hoverEnded(ItemIconViewer viewer) {
		// set hovers invisible for now, they will be removed eventually
		// when hoverStarted() is called again
		for (ItemIconHover hover : itemHovers) {
			if (hover.getHoverSource() == viewer) {
				hover.setVisible(false);
			}
		}
	}
	
	@Override public void rightClicked(ItemIconViewer viewer, int x, int y) {
		Item item = viewer.getItem();
		int quantity = viewer.getQuantity();
		int index = viewer.getItemIndex();
		
		RightClickMenu menu = Game.mainViewer.getMenu();
		
		menu.clear();
		menu.addMenuLevel(item.getFullName());
		menu.setPosition(x - 2, y - 25);
		
		switch (mode) {
		case INVENTORY:
			addInventoryButtons(item, quantity, index, menu);
			break;
		case MERCHANT:
			addMerchantButtons(item, quantity, index, menu);
			break;
		case CONTAINER:
			addContainerButtons(item, quantity, index, menu);
			break;
		}
		
		Button details = new Button("View Details");
		details.addCallback(callbackFactory.getDetailsCallback(item, x, y));
		menu.addButton(details);
		
		
		menu.show();
		
		// show popup immediately
		if (menu.shouldPopupToggle()) {
			menu.togglePopup();
		}
	}
	
	private void addContainerButtons(Item item, int quantity, int index, RightClickMenu menu) {
		if (creature.getTimer().canPerformAction(Game.ruleset.getValue("PickUpAndWieldItemCost"))) {
			if ((creature.getInventory().getEquippedMainHand() == null && item.getItemType() == Item.ItemType.WEAPON) ||
				(creature.getInventory().getEquippedOffHand() == null && item.getItemType() == Item.ItemType.SHIELD)) {
				
				Button button = new Button("Take and Wield");
				button.addCallback(callbackFactory.getTakeAndWieldCallback(item, index));
				menu.addButton(button);
			}
		}
		
		if (creature.getTimer().canPerformAction(Game.ruleset.getValue("PickUpItemCost"))) {
			Button button = new Button("Take");
			button.addCallback(callbackFactory.getTakeCallback(item, index, 1));
			menu.addButton(button);
			
			if (quantity > 1) {
				button = new Button("Take Multiple...");
				button.addCallback(callbackFactory.getTakeCallback(item, index, quantity));
				menu.addButton(button);
			}
		}
	}
	
	public static int getMerchantBuyMaxQuantity(Merchant merchant, Item item, int quantityAvailable) {
		int maxAffordable = Game.curCampaign.getPartyCurrency().getMaxNumberAffordable(item,
				merchant.getCurrentSellPercentage());
		
		return Math.min(quantityAvailable, maxAffordable);
	}
	
	private void addMerchantButtons(Item item, int quantity, int index, RightClickMenu menu) {
		int maxBuy = getMerchantBuyMaxQuantity(merchant, item, quantity);
		
		StringBuilder buyText = new StringBuilder();
		buyText.append("Buy for ");
		buyText.append(item.getQualityValue().shortString(merchant.getCurrentSellPercentage()));
		if (item.getValueStackSize() != 1) {
			buyText.append(" per ");
			buyText.append(item.getValueStackSize());
		}
		
		Button button = new Button(buyText.toString());
		button.addCallback(callbackFactory.getBuyCallback(item, merchant, 1));
		button.setEnabled(maxBuy >= 1);
		menu.addButton(button);
		
		if (maxBuy > 1) {
			button = new Button("Buy Multiple...");
			button.addCallback(callbackFactory.getBuyCallback(item, merchant, maxBuy));
			menu.addButton(button);
		}
	}
	
	private void addInventoryButtons(Item item, int quantity, int index, RightClickMenu menu) {
		if (merchant != null && !item.isQuestItem()) {
			StringBuilder sellText = new StringBuilder();
			sellText.append("Sell for ");
			sellText.append(item.getQualityValue().shortString(merchant.getCurrentBuyPercentage()));
			if (item.getValueStackSize() != 1) {
				sellText.append(" per ");
				sellText.append(item.getValueStackSize());
			}
			
			Button button = new Button(sellText.toString());
			button.addCallback(callbackFactory.getSellCallback(item, merchant, 1));
			menu.addButton(button);
			
			if (quantity > 1) {
				button = new Button("Sell Multiple...");
				button.addCallback(callbackFactory.getSellCallback(item, merchant, quantity));
				menu.addButton(button);
			}
		}
		
		if ( creature.getInventory().hasPrereqsToEquip(item) ) {
			Button button = new Button("Equip");
			button.addCallback(callbackFactory.getEquipCallback(item));
			
			if (!creature.getTimer().canPerformEquipAction(item)) {
				button.setEnabled(false);
				button.setTooltipContent("Not enough AP to equip");
			}
			
			if (!creature.getInventory().canUnequipCurrentItemInSlot(item, 0)) {
				button.setEnabled(false);
				button.setTooltipContent("The currently equipped item cannot be removed.");
			}
			
			menu.addButton(button);
		}

		if (creature.getInventory().canEquipAsOffHandWeapon(item)) {
			Button button = new Button("Equip Off Hand");
			button.addCallback(callbackFactory.getEquipOffHandCallback(item));
			
			if (!creature.getTimer().canPerformEquipAction(item)) {
				button.setEnabled(false);
				button.setTooltipContent("Not enough AP to equip");
			}
			
			if (!creature.getInventory().canUnequipCurrentItemInSlot(item, Inventory.EQUIPPED_OFF_HAND)) {
				button.setEnabled(false);
				button.setTooltipContent("The currently equipped item cannot be removed.");
			}
			
			menu.addButton(button);
		}
		
		if (item.canUse(creature)) {
			Button button = new Button(item.getUseButtonText());
			button.addCallback(callbackFactory.getUseCallback(item));
			menu.addButton(button);
		}

		if ((creature.getTimer().canPerformAction(Game.ruleset.getValue("GiveItemCost")) ||
				!Game.isInTurnMode()) && Game.curCampaign.party.size() > 1) {
			Button button = new Button("Give >>");
			button.addCallback(callbackFactory.getGiveCallback(item, 1));
			menu.addButton(button);

			if (quantity > 1) {
				button = new Button("Give Multiple >>");
				button.addCallback(callbackFactory.getGiveCallback(item, quantity));
				menu.addButton(button);
			}
		}
		if (!item.isQuestItem() && creature.getTimer().canPerformAction(Game.ruleset.getValue("DropItemCost"))) {
			Button button = new Button("Drop");
			button.addCallback(callbackFactory.getDropCallback(index, 1));
			menu.addButton(button);

			if (quantity > 1) {
				button = new Button("Drop Multiple...");
				button.addCallback(callbackFactory.getDropCallback(index, quantity));
				menu.addButton(button);
			}
		}
	}
	
	private boolean itemMatchesFilter(Item item) {
		switch (activeFilter) {
		case All: return true;
		case Weapons: return item.getItemType() == Item.ItemType.WEAPON;
		case Armor:
			switch (item.getItemType()) {
			case ARMOR: case SHIELD: case GLOVES: case HELMET: case BOOTS:
				return !item.getArmorType().getName().equals(Game.ruleset.getString("DefaultArmorType"));
			}
			break;
		case Apparel:
			switch (item.getItemType()) {
			case CLOAK: case BELT: case AMULET: case RING:
				return true;
			case GLOVES: case HELMET: case BOOTS: case ARMOR: case SHIELD:
				return item.getArmorType().getName().equals(Game.ruleset.getString("DefaultArmorType"));
			}
			break;
		case Ammo: return item.getItemType() == Item.ItemType.AMMO;
		case Misc: return item.getItemType() == Item.ItemType.ITEM;
		case Usable: return item.isUsable();
		case Ingredients: return item.isIngredient();
		case Traps: return item.getType() == Entity.Type.TRAP;
		case Quest: return item.isQuestItem();
		}

		return false;
	}
	
	private class FilterButtonCallback implements Runnable {
		private ToggleButton button;
		private Filter filter;
		
		private FilterButtonCallback(ToggleButton button, Filter filter) {
			this.button = button;
			this.filter = filter;
		}
		
		@Override public void run() {
			if (ItemListViewer.this.activeButton != null) {
				ItemListViewer.this.activeButton.setActive(false);
			}
			
			ItemListViewer.this.activeButton = button;
			ItemListViewer.this.activeFilter = filter;
			
			button.setActive(true);
			updateContent(mode, creature, merchant, items);
		}
	}
	
	private class Content extends Widget implements DropTarget {
		@Override protected void layout() {
			super.layout();
			
			int curX = getInnerX();
			int curY = getInnerY();
			int maxBottom = getInnerY();
			
			for (int i = 0; i < getNumChildren(); i++) {
				Widget child = getChild(i);
				
				// set the min size for ItemIconViewers instead of preferred size
				child.setSize(child.getMinWidth(), child.getMinHeight());
				
				if (curX + child.getWidth() + gridGap > getInnerRight()) {
					curX = getInnerX();
					curY = maxBottom + gridGap;
				}
				
				child.setPosition(curX, curY);
				
				curX = child.getRight() + gridGap;
				maxBottom = Math.max(maxBottom, child.getBottom());
			}
		}
		
		private boolean validateTarget(DragTarget target) {
			if (target.getItem() == null) return false;
			
			switch (mode) {
			case INVENTORY:
				if (target.getItemParent() != null && target.getItemEquipSlot() == -1) return false;
				
				if (target.getItem().isCursed()) return false;
				break;
			case CONTAINER:
				if (target.getItemParent() == null) return false;
				break;
			case MERCHANT:
				if (target.getItemParent() == null) return false;
				break;
			}
			
			return true;
		}
		
		private void dragDropMerchantFromEquipped(DragTarget target) {
			target.getItemParent().getInventory().getCallbackFactory().getSellEquippedCallback(target.getItemEquipSlot(), merchant).run();
		}
		
		private void dragDropMerchantFromInventory(DragTarget target) {
			int maxQuantity = target.getItemParent().getInventory().getQuantity(target.getItem());
			
			target.getItemParent().getInventory().getCallbackFactory().getSellCallback(target.getItem(), merchant, maxQuantity).run();
		}
		
		private void dragDropInventoryFromMerchant(DragTarget target) {
			Merchant merchant = target.getItemMerchant();
			int merchantQuantity = target.getItemMerchant().getCurrentItems().getQuantity(target.getItem());
			int maxQuantity = getMerchantBuyMaxQuantity(merchant, target.getItem(), merchantQuantity);
			
			creature.getInventory().getCallbackFactory().getBuyCallback(target.getItem(), target.getItemMerchant(), maxQuantity).run();
		}
		
		private void dragDropInventoryFromEquipped(DragTarget target) {
			creature.getInventory().getCallbackFactory().getUnequipCallback(target.getItem()).run();
		}
		
		private void dragDropInventoryFromContainer(DragTarget target) {
			Container container = target.getItemContainer();
			
			int index = container.getItems().findItem(target.getItem());
			int quantity = container.getItems().getQuantity(index);
			
			creature.getInventory().getCallbackFactory().getTakeCallback(target.getItem(), index, quantity).run();
		}
		
		private void dragDropContainerFromInventory(DragTarget target) {
			Inventory srcInventory = target.getItemParent().getInventory();
			
			int slot = srcInventory.getEquippedSlot(target.getItem());
			
			if (slot == -1) {
				int index = srcInventory.getUnequippedItems().findItem(target.getItem());
				int quantity = srcInventory.getUnequippedItems().getQuantity(index);
				
				srcInventory.getCallbackFactory().getDropCallback(index, quantity).run();
			} else {
				srcInventory.getCallbackFactory().getDropEquippedCallback(slot).run();
			}
		}
		
		@Override public void dragAndDropStartHover(DragTarget target) {
			if (validateTarget(target))
				getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, true);
		}

		@Override public void dragAndDropStopHover(DragTarget target) {
			getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, false);
		}

		@Override public void dropDragTarget(DragTarget target) {
			if (validateTarget(target)) {
				switch (mode) {
				case INVENTORY:
					if (target.getItemContainer() != null) {
						dragDropInventoryFromContainer(target);
					} else if (target.getItemMerchant() != null) {
						dragDropInventoryFromMerchant(target);
					} else if (target.getItemEquipSlot() != -1) {
						dragDropInventoryFromEquipped(target);
					}
					break;
				case CONTAINER:
					dragDropContainerFromInventory(target);
					break;
				case MERCHANT:
					if (target.getItemEquipSlot() != -1)
						dragDropMerchantFromEquipped(target);
					else
						dragDropMerchantFromInventory(target);
					break;
				}
			}
			
			getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, false);
		}
	}

	@Override public void dragAndDropStartHover(DragTarget target) {
		content.dragAndDropStartHover(target);
	}

	@Override public void dragAndDropStopHover(DragTarget target) {
		content.dragAndDropStopHover(target);
	}

	@Override public void dropDragTarget(DragTarget target) {
		content.dropDragTarget(target);
	}
}
