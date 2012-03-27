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

package net.sf.hale.editor;

import net.sf.hale.Game;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.ItemQuality;
import net.sf.hale.widgets.ExpandableScrollPane;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class InventoryEditor extends DialogLayout implements Updateable, ItemListEntryPane.Callback, InventorySlotPane.Callback {
	private final ItemListBox allItemsBox;
	
	private final Label ownedItemsLabel;
	private final ScrollPane ownedItemsPane;
	private final DialogLayout ownedItemsContent;

	private final Label equippedItemsLabel;
	private final ScrollPane equippedItemsPane;
	private final DialogLayout equippedItemsContent;
	
	private final SimpleChangableListModel<String> qualitiesModel;
	
	private final Button addItem;
	
	private Inventory inventory;
	
	public InventoryEditor() {
		this.setTheme("/editorlayout");
		
		qualitiesModel = new SimpleChangableListModel<String>();
		for (ItemQuality quality : Game.ruleset.getAllItemQualities()) {
			qualitiesModel.addElement(quality.getName());
		}
		
		allItemsBox = new ItemListBox(Game.campaignEditor.getItemsModel(), "All Items");
		allItemsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				switch (reason) {
				case MOUSE_DOUBLE_CLICK:
					inventory.addItem(Game.campaignEditor.getItemsModel().getEntry(allItemsBox.getSelected()));
					updateModel();
				}
				
				if (allItemsBox.getSelected() != -1) update();
			}
		});
		
		ownedItemsLabel = new Label("Owned Items");
		ownedItemsLabel.setTheme("/titlelabel");
		
		ownedItemsPane = new ExpandableScrollPane();
		
		ownedItemsContent = new DialogLayout();
		ownedItemsContent.setTheme("/editorlayout");
		ownedItemsContent.setHorizontalGroup(ownedItemsContent.createParallelGroup());
		ownedItemsContent.setVerticalGroup(ownedItemsContent.createSequentialGroup());
		ownedItemsPane.setContent(ownedItemsContent);
		
		equippedItemsLabel = new Label("Equipped Items");
		equippedItemsLabel.setTheme("/titlelabel");
		
		equippedItemsPane = new ExpandableScrollPane();
		
		equippedItemsContent = new DialogLayout();
		equippedItemsContent.setTheme("/editorlayout");
		equippedItemsContent.setHorizontalGroup(equippedItemsContent.createParallelGroup());
		equippedItemsContent.setVerticalGroup(equippedItemsContent.createSequentialGroup());
		
		equippedItemsPane.setContent(equippedItemsContent);
		
		addItem = new Button("Add");
		addItem.addCallback(new Runnable() {
			@Override public void run() {
				if (allItemsBox.getSelected() == -1) return;
				
				inventory.addItem(Game.campaignEditor.getItemsModel().getEntry(allItemsBox.getSelected()));
				updateModel();
			}
		});
		
		Group leftH = this.createParallelGroup(allItemsBox, addItem);
		Group leftV = this.createSequentialGroup(allItemsBox, addItem);
		
		Group centerH = this.createParallelGroup(ownedItemsLabel, ownedItemsPane);
		Group centerV = this.createSequentialGroup(ownedItemsLabel, ownedItemsPane);
		
		Group rightH = this.createParallelGroup(equippedItemsLabel, equippedItemsPane);
		Group rightV = this.createSequentialGroup(equippedItemsLabel, equippedItemsPane);
		
		this.setHorizontalGroup(this.createSequentialGroup(leftH, centerH, rightH));
		this.setVerticalGroup(this.createParallelGroup(leftV, centerV, rightV));
	}
	
	@Override public void entryRemoved(int index) {
		ItemList items = inventory.getUnequippedItems();
		
		items.removeItem(index, items.getQuantity(index));
		
		updateOwnedItemsModel();
	}
	
	@Override public void entryEquipped(int index) {
		Item item = inventory.getUnequippedItems().getItem(index);
		inventory.equipItem(item, 0);
		
		updateModel();
	}
	
	@Override public void entryEquippedOffHand(int index) {
		Item item = inventory.getUnequippedItems().getItem(index);
		
		inventory.equipItem(item, Inventory.EQUIPPED_OFF_HAND);
		
		updateModel();
	}
	
	@Override public void entryUnequipped(int slot) {
		inventory.unequipItem(slot);
		updateModel();
	}
	
	private void updateEquippedItemsModel() {
		equippedItemsContent.removeAllChildren();
		
		Group mainH = equippedItemsContent.createParallelGroup();
		Group mainV = equippedItemsContent.createSequentialGroup();
		
		for (int i = 0; i < Inventory.EQUIPPED_SIZE; i++) {
			if ( inventory.getEquippedItem(i) == null ) continue;
			
			InventorySlotPane pane = new InventorySlotPane(inventory, i, qualitiesModel);
			pane.addCallback(this);
			
			mainH.addWidget(pane);
			mainV.addWidget(pane);
		}
		
		equippedItemsContent.setHorizontalGroup(mainH);
		equippedItemsContent.setVerticalGroup(mainV);
	}
	
	private void updateOwnedItemsModel() {
		ownedItemsContent.removeAllChildren();
		
		Group mainH = ownedItemsContent.createParallelGroup();
		Group mainV = ownedItemsContent.createSequentialGroup();
		
		ItemList ownedItems = inventory.getUnequippedItems();
		
		for (int i = 0; i < ownedItems.size(); i++) {
			ItemListEntryPane pane = new ItemListEntryPane(ownedItems, i, qualitiesModel, true);
			pane.addCallback(this);
			mainH.addWidget(pane);
			mainV.addWidget(pane);
		}
		
		ownedItemsContent.setHorizontalGroup(mainH);
		ownedItemsContent.setVerticalGroup(mainV);
	}
	
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
		
		updateModel();
		
		allItemsBox.resetView();
	}

	public void updateModel() {
		updateOwnedItemsModel();
		
		updateEquippedItemsModel();
		
		update();
	}
	
	@Override public void update() {
		addItem.setEnabled(allItemsBox.getSelected() != -1);
	}
}
