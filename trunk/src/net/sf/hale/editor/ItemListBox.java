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

import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;

import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class ItemListBox extends Widget implements Runnable, ListModel.ChangeListener {
	private final Label titleLabel;
	
	private final ListModel<Item> allItemsModel;
	
	private final SimpleChangableListModel<Item> filteredItemsModel;
	private final ReferenceListBox<Item> itemsBox;
	
	private final Label filtersLabel;
	private final ComboBox<String> filtersBox;
	private final SimpleChangableListModel<String> filtersModel;
	
	public ItemListBox(ListModel<Item> model, String title) {
		super();
		this.setTheme("");
		
		titleLabel = new Label(title);
		titleLabel.setTheme("/titlelabel");
		titleLabel.setPosition(2, 10);
		this.add(titleLabel);
		
		this.allItemsModel = model;
		this.allItemsModel.addChangeListener(this);
		this.filteredItemsModel = new SimpleChangableListModel<Item>();
		
		filtersModel = new SimpleChangableListModel<String>();
		filtersModel.addElement("All");
		filtersModel.addElement("Containers");
		filtersModel.addElement("Doors");
		filtersModel.addElement("Traps");
		filtersModel.addElement("Items");
		for (Item.ItemType type : Item.ItemType.values()) {
			filtersModel.addElement("  " + type.toString());
			
			if (type == Item.ItemType.WEAPON) {
				for (Item.WeaponType weaponType : Item.WeaponType.values()) {
					filtersModel.addElement("    " + weaponType.toString());
				}
			}
		}
		
		filtersLabel = new Label("Filter");
		filtersLabel.setTheme("/labelborder");
		filtersLabel.setSize(34, 16);
		filtersLabel.setPosition(0, 20);
		this.add(filtersLabel);
		
		filtersBox = new ComboBox<String>(filtersModel);
		filtersBox.setTheme("/combobox");
		filtersBox.setPosition(40, 20);
		filtersBox.addCallback(this);
		this.add(filtersBox);
		
		itemsBox = new ReferenceListBox<Item>(filteredItemsModel);
		itemsBox.setTheme("/listbox");
		itemsBox.setPosition(0, 40);
		this.add(itemsBox);
	}
	
	@Override public void run() {
		int index = filtersBox.getSelected();
		
		filteredItemsModel.clear();
		
		if (index == -1) return;
		String filter = filtersModel.getEntry(index);
		
		if (filter.equals("All")) {
			for (int i = 0; i < allItemsModel.getNumEntries(); i++) {
				filteredItemsModel.addElement(allItemsModel.getEntry(i));
			}
		} else if (filter.equals("Containers")) {
			addEntitiesOfType(Entity.Type.CONTAINER);
		} else if (filter.equals("Doors")) {
			addEntitiesOfType(Entity.Type.DOOR);
		} else if (filter.equals("Traps")) {
			addEntitiesOfType(Entity.Type.TRAP);
		} else if (filter.equals("Items")) {
			addEntitiesOfType(Entity.Type.ITEM);
		} else if (filter.startsWith("  ") && !filter.startsWith("   ")) {
			Item.ItemType itemType = Item.ItemType.valueOf(filter.trim());
			for (int i = 0; i < allItemsModel.getNumEntries(); i++) {
				Item item = allItemsModel.getEntry(i);
				if (item.getType() == Entity.Type.ITEM && item.getItemType() == itemType)
					filteredItemsModel.addElement(item);
			}
		} else {
			Item.WeaponType weaponType = Item.WeaponType.valueOf(filter.trim());
			for (int i = 0; i < allItemsModel.getNumEntries(); i++) {
				Item item = allItemsModel.getEntry(i);
				if (item.getType() == Entity.Type.ITEM && item.getItemType() == Item.ItemType.WEAPON &&
					item.getWeaponType() == weaponType)
					filteredItemsModel.addElement(item);
			}
		}
	}
	
	private void addEntitiesOfType(Entity.Type type) {
		for (int i = 0; i < allItemsModel.getNumEntries(); i++) {
			Item item = allItemsModel.getEntry(i);
			if (item.getType() == type) {
				filteredItemsModel.addElement(item);
			}
		}
	}
	
	public String getSelectedFilter() {
		int index = this.filtersBox.getSelected();
		
		if (index != -1) return this.filtersModel.getEntry(index);
		else return null;
	}
	
	public void setSelectedFilter(String filter) {
		int index = this.filtersModel.findElement(filter);
		this.filtersBox.setSelected(index);
	}
	
	@Override public int getPreferredInnerHeight() {
		return Short.MAX_VALUE;
	}
	
	@Override public int getPreferredInnerWidth() {
		return Short.MAX_VALUE;
	}
	
	@Override public int getMinWidth() {
		return 225;
	}
	
	@Override public int getMaxWidth() {
		return 225;
	}
	
	@Override public boolean setSize(int width, int height) {
		filtersBox.setSize(Math.max(width - 45 - this.getBorderHorizontal(), 0), 20);
		itemsBox.setSize(Math.max(width - this.getBorderHorizontal() - 5, 0), Math.max(height - 40 - this.getBorderVertical() - 5, 0));
		
		return super.setSize(width, height);
	}
	
	public void addCallback(CallbackWithReason<ListBox.CallbackReason> callback) {
		itemsBox.addCallback(callback);
	}
	
	public void resetView() {
		filtersBox.setSelected(0);
	}
	
	public void deselect() {
		itemsBox.setSelected(-1);
	}
	
	public void setSelected(int index) {
		itemsBox.setSelected(index);
	}
	
	public void scrollToSelected() {
		itemsBox.scrollToSelected();
	}
	
	public int getSelected() {
		int index = itemsBox.getSelected();
		if (index == -1) return -1;
		
		Item item = filteredItemsModel.getEntry(index);
		
		for (int i = 0; i < allItemsModel.getNumEntries(); i++) {
			if (item.getID().equals(allItemsModel.getEntry(i).getID())) return i;
		}
		
		return -1;
	}
	
	@Override public void allChanged() {
		filtersBox.setSelected(-1);
	}

	@Override public void entriesChanged(int first, int last) { }
	@Override public void entriesDeleted(int first, int last) { }
	@Override public void entriesInserted(int first, int last) { }
}
