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
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.ItemQuality;
import net.sf.hale.widgets.ExpandableScrollPane;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class ItemListEditor extends EditorWindow implements Updateable, ItemListEntryPane.Callback, PopupCallback {
	private ItemList selectedItemList;
	
	private final Updateable parent;
	private final DialogLayout content;
	
	private final Label itemListsLabel;
	private final ListBox<ItemList> itemListsBox;
	private final Button saveItemLists, newItemList, deleteItemList, copyItemList;
	
	private final Label setQualitiesLabel;
	private final ComboBox<String> setQualitiesBox;
	
	private final DialogLayout itemListContent;
	
	private final Label itemListName;
	
	private final Button addItem;
	private final ItemListBox allItemsBox;
	
	private final SimpleChangableListModel<String> qualitiesModel;
	private final ScrollPane ownedItemsPane;
	private final DialogLayout ownedItemsContent;
	
	public ItemListEditor(Updateable parent) {
		super("Item List Editor");
		
		this.parent = parent;
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		this.add(content);
		
		itemListsLabel = new Label("Select an item list:");
		
		itemListsBox = new ReferenceListBox<ItemList>(Game.campaignEditor.getItemListsModel());
		itemListsBox.setTheme("listboxnoexpand");
		itemListsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				saveSelected();
				
				update();
			}
		});
		
		saveItemLists = new Button("Save");
		saveItemLists.addCallback(new Runnable() {
			@Override public void run() {
				saveSelected();
			}
		});
		
		newItemList = new Button("New");
		newItemList.addCallback(new Runnable() {
			@Override public void run() {
				NewItemListPopup popup = new NewItemListPopup(ItemListEditor.this);
				popup.setCallback(ItemListEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteItemList = new Button("Delete");
		deleteItemList.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedItemList != null) {
					int index = itemListsBox.getSelected();
					if (index == -1) return;
					
					ItemList itemList = Game.campaignEditor.getItemListsModel().getEntry(index);
					String path = Game.campaignEditor.getPath() + "/itemLists/" + itemList.getID() + ".txt";
					
					DeleteFilePopup popup = new DeleteFilePopup(ItemListEditor.this, path, selectedItemList);
					popup.setCallback(ItemListEditor.this);
					popup.openPopupCentered();
				}
			}
		});
		
		copyItemList = new Button("Copy");
		copyItemList.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedItemList == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(ItemListEditor.this, "itemLists", selectedItemList.getID());
				popup.setCallback(ItemListEditor.this);
				popup.openPopupCentered();
			}
		});
		
		Group leftH = content.createParallelGroup(itemListsLabel, itemListsBox);
		Group leftV = content.createSequentialGroup(itemListsLabel, itemListsBox);
		
		leftH.addGroup(content.createSequentialGroup(saveItemLists, newItemList, deleteItemList, copyItemList));
		leftV.addGroup(content.createParallelGroup(saveItemLists, newItemList, deleteItemList, copyItemList));
		
		itemListContent = new DialogLayout();
		itemListContent.setTheme("/editorlayout");
		
		itemListName = new Label();
		
		allItemsBox = new ItemListBox(Game.campaignEditor.getItemsModel(), "All Items");
		allItemsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				if (allItemsBox.getSelected() != -1) updateButtonState();
				
				switch (reason) {
				case MOUSE_DOUBLE_CLICK: addSelectedItem();
				}
			}
		});
		
		qualitiesModel = new SimpleChangableListModel<String>();
		for (ItemQuality quality : Game.ruleset.getAllItemQualities()) {
			qualitiesModel.addElement(quality.getName());
		}
		
		ownedItemsPane = new ExpandableScrollPane();
		
		ownedItemsContent = new DialogLayout();
		ownedItemsContent.setTheme("/editorlayout");
		ownedItemsContent.setHorizontalGroup(ownedItemsContent.createParallelGroup());
		ownedItemsContent.setVerticalGroup(ownedItemsContent.createSequentialGroup());
		
		ownedItemsPane.setContent(ownedItemsContent);
		
		addItem = new Button("Add");
		addItem.addCallback(new Runnable() {
			@Override
			public void run() {
				addSelectedItem();
			}
		});
		
		setQualitiesLabel = new Label("Set All Item Qualities to ");
		setQualitiesBox = new ComboBox<String>(qualitiesModel);
		setQualitiesBox.addCallback(new Runnable() {
			@Override public void run() {
				if (setQualitiesBox.getSelected() == -1) return;
				if (selectedItemList == null) return;
				
				for (int i = 0; i < selectedItemList.size(); i++) {
					selectedItemList.setQuality(i, qualitiesModel.getEntry(setQualitiesBox.getSelected()));
				}
				
				updateOwnedItemsModel();
				setQualitiesBox.setSelected(-1);
			}
		});
		
		Group bottomRightH = itemListContent.createSequentialGroup(setQualitiesLabel, setQualitiesBox);
		Group bottomRightV = itemListContent.createParallelGroup(setQualitiesLabel, setQualitiesBox);
		
		Group centerH = itemListContent.createParallelGroup(allItemsBox, addItem);
		Group centerV = itemListContent.createSequentialGroup(allItemsBox, addItem);
		
		Group rightH = itemListContent.createParallelGroup(itemListName, ownedItemsPane);
		Group rightV = itemListContent.createSequentialGroup(itemListName, ownedItemsPane);
		
		rightH.addGroup(bottomRightH);
		rightV.addGroup(bottomRightV);
		
		itemListContent.setHorizontalGroup(itemListContent.createSequentialGroup(centerH, rightH));
		itemListContent.setVerticalGroup(itemListContent.createParallelGroup(centerV, rightV));
		
		Group mainH = content.createSequentialGroup(leftH);
		Group mainV = content.createParallelGroup(leftV);
		mainH.addWidget(itemListContent);
		mainV.addWidget(itemListContent);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	@Override public void saveSelected() {
		if (selectedItemList == null) return;
		
		selectedItemList.saveToDisk();
		
		Game.campaignEditor.updateStatusText("Item List " + selectedItemList.getID() + " saved.");
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) update();
	}
	
	public void addSelectedItem() {
		int index = allItemsBox.getSelected();
		
		if (index == -1) return;
		
		selectedItemList.addItem(Game.campaignEditor.getItemsModel().getEntry(index));
		updateOwnedItemsModel();
		updateButtonState();
	}
	
	@Override public void update() {
		int index = itemListsBox.getSelected();
		if (index == -1) {
			selectedItemList = null;
			itemListContent.setVisible(false);
		} else {
			selectedItemList = Game.campaignEditor.getItemListsModel().getEntry(index);
			itemListContent.setVisible(true);
			
			itemListName.setText("Name: " + selectedItemList.getID());
			setQualitiesBox.setSelected(-1);
			
			updateOwnedItemsModel();
			updateButtonState();
			
			allItemsBox.resetView();
		}
	}
	
	public void updateOwnedItemsModel() {
		ownedItemsContent.removeAllChildren();
		
		Group mainH = ownedItemsContent.createParallelGroup();
		Group mainV = ownedItemsContent.createSequentialGroup();
		
		for (int i = 0; i < selectedItemList.size(); i++) {
			ItemListEntryPane pane = new ItemListEntryPane(selectedItemList, i, qualitiesModel, false);
			pane.addCallback(this);
			
			mainH.addWidget(pane);
			mainV.addWidget(pane);
		}
		
		ownedItemsContent.setHorizontalGroup(mainH);
		ownedItemsContent.setVerticalGroup(mainV);
	}
	
	@Override public void entryEquippedOffHand(int index) { }
	@Override public void entryEquipped(int index) { }
	
	@Override public void entryRemoved(int index) {
		selectedItemList.removeItem(index, selectedItemList.getQuantity(index));
		updateOwnedItemsModel();
	}
	
	public void updateButtonState() {
		addItem.setEnabled(false);
		
		if (selectedItemList == null) return;
		
		if (allItemsBox.getSelected() != -1) {
			addItem.setEnabled(true);
		}
	}

	@Override public void newComplete() {
		parent.update();
	}

	@Override public void copyComplete() {
		parent.update();
	}

	@Override public void deleteComplete() {
		this.selectedItemList = null;
		
		parent.update();
	}
}
