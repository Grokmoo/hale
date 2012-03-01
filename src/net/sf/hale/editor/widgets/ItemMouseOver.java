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

package net.sf.hale.editor.widgets;

import net.sf.hale.Game;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.ItemList;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class ItemMouseOver extends DialogLayout {
	private final Item item;
	
	private final Label itemID;
	private final Button editTopLevel;
	
	private final Group bottomH;
	private final Group bottomV;
	
	public ItemMouseOver(Item item) {
		super();
		
		this.item = item;
		
		this.setTheme("/gamepopup");
		this.setCanAcceptKeyboardFocus(true);
		
		itemID = new Label(item.getID());
		
		bottomH = createParallelGroup();
		bottomV = createSequentialGroup();
		
		switch (item.getType()) {
		case CONTAINER:
			Label containerTitle = new Label("Contents:");
			containerTitle.setTheme("/titlelabel");
			
			bottomH.addWidget(containerTitle);
			bottomV.addWidget(containerTitle);
			
			Container container = (Container)item;
			ItemList items = container.getItems();
			for (int i = 0; i < items.size(); i++) {
				Label itemID = new Label(items.getItemID(i));
				
				Group curRowH = createSequentialGroup();
				Group curRowV = createParallelGroup();
				
				ValueAdjusterInt adjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 10000, items.getQuantity(i)));
				adjuster.getModel().addCallback(new AdjustQuantityCallback(items, i, adjuster.getModel()));
				
				Button editItem = new Button("Edit");
				editItem.addCallback(new EditItemCallback(items.getItem(i)));
				
				Button removeItem = new Button("Remove");
				removeItem.addCallback(new RemoveItemCallback(items, i, curRowH, curRowV));
				
				curRowH.addWidgets(itemID, adjuster, editItem, removeItem);
				curRowV.addWidgets(itemID, adjuster, editItem, removeItem);
				
				bottomH.addGroup(curRowH);
				bottomV.addGroup(curRowV);
			}
			
			break;
		}
		
		editTopLevel = new Button("Edit");
		editTopLevel.addCallback(new EditItemCallback(this.item));
		
		Group topH = createSequentialGroup(itemID, editTopLevel);
		Group topV = createParallelGroup(itemID, editTopLevel);
		
		Group mainH = this.createParallelGroup(topH, bottomH);
		Group mainV = this.createSequentialGroup(topV);
		mainV.addGap(10);
		mainV.addGroup(bottomV);
		
		setHorizontalGroup(mainH);
		setVerticalGroup(mainV);
	}
	
	@Override public boolean handleEvent(Event evt) {
		switch (evt.getType()) {
		case MOUSE_ENTERED: return true;
		case MOUSE_MOVED: return true;
		}
		
		return false;
	}
	
	private class RemoveItemCallback implements Runnable {
		private final ItemList items;
		private final int index;
		private final Group groupH;
		private final Group groupV;
		
		private RemoveItemCallback(ItemList items, int index, Group groupH, Group groupV) {
			this.items = items;
			this.index = index;
			this.groupH = groupH;
			this.groupV = groupV;
		}
		
		@Override public void run() {
			items.removeItem(index, items.getQuantity(index));
			bottomH.removeGroup(groupH, true);
			bottomV.removeGroup(groupV, true);
			adjustSize();
		}
	}
	
	private class AdjustQuantityCallback implements Runnable {
		private final ItemList items;
		private final int index;
		private final IntegerModel model;
		
		private AdjustQuantityCallback(ItemList items, int index, IntegerModel model) {
			this.items = items;
			this.index = index;
			this.model = model;
		}
		
		@Override public void run() {
			items.setQuantity(index, model.getValue());
		}
	}
	
	private class EditItemCallback implements Runnable {
		private final Item item;
		
		private EditItemCallback(Item item) {
			this.item = item;
		}
		
		@Override public void run() {
			Game.campaignEditor.itemEditor.openToItem(item);
		}
	}
}
