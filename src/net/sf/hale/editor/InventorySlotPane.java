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

import java.util.LinkedList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.editor.reference.ReferencePopupWindow;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class InventorySlotPane extends DialogLayout {
	private final List<Callback> callbacks;
	
	private final Inventory inventory;
	private final int slot;
	private final SimpleChangableListModel<String> qualitiesModel;
	private final ComboBox<String> qualityBox;
	private final Button refButton;
	
	public InventorySlotPane(Inventory inventory, int slot, SimpleChangableListModel<String> qualitiesModel) {
		callbacks = new LinkedList<Callback>();
		
		this.inventory = inventory;
		this.slot = slot;
		this.qualitiesModel = qualitiesModel;
		
		this.setTheme("/itemlistentrypane");
		
		Button remove = new Button("[x]");
		remove.addCallback(new Runnable() {
			@Override public void run() {
				for (Callback callback : callbacks) {
					callback.entryUnequipped(InventorySlotPane.this.slot);
				}
			}
		});
		
		Label name = new Label(inventory.getEquippedItem(slot).getName());
		
		qualityBox = new ComboBox<String>(qualitiesModel);
		qualityBox.setSelected(qualitiesModel.findElement(inventory.getEquippedItem(slot).getQuality().getName()));
		qualityBox.addCallback(new Runnable() {
			@Override public void run() {
				Item item = InventorySlotPane.this.inventory.getEquippedItem(InventorySlotPane.this.slot);
				String quality = InventorySlotPane.this.qualitiesModel.getEntry(qualityBox.getSelected());
				
				item.setQuality(Game.ruleset.getItemQuality(quality));
			}
		});
		
		refButton = new Button(">>");
		refButton.addCallback(new Runnable() {
			@Override public void run() {
				new ReferencePopupWindow(refButton,
						InventorySlotPane.this.inventory.getEquippedItem(InventorySlotPane.this.slot)).openPopup();
			}
		});
		
		Label slotLabel = new Label();
		switch (slot) {
		case Inventory.EQUIPPED_MAIN_HAND: slotLabel.setText("Main Hand"); break;
		case Inventory.EQUIPPED_OFF_HAND: slotLabel.setText("Off Hand"); break;
		case Inventory.EQUIPPED_ARMOR: slotLabel.setText("Armor"); break;
		case Inventory.EQUIPPED_GLOVES: slotLabel.setText("Gloves"); break;
		case Inventory.EQUIPPED_HELMET: slotLabel.setText("Helmet"); break;
		case Inventory.EQUIPPED_CLOAK: slotLabel.setText("Cloak"); break;
		case Inventory.EQUIPPED_BOOTS: slotLabel.setText("Boots"); break;
		case Inventory.EQUIPPED_BELT: slotLabel.setText("Belt"); break;
		case Inventory.EQUIPPED_AMULET: slotLabel.setText("Amulet"); break;
		case Inventory.EQUIPPED_RING_RIGHT: slotLabel.setText("Ring (Right)"); break;
		case Inventory.EQUIPPED_RING_LEFT: slotLabel.setText("Ring (Left)"); break;
		case Inventory.EQUIPPED_QUIVER: slotLabel.setText("Quiver"); break;
		}
		
		Group bottomMiddleH = this.createSequentialGroup(slotLabel, refButton);
		Group bottomMiddleV = this.createParallelGroup(slotLabel, refButton);
		
		Group leftH = this.createParallelGroup(qualityBox);
		Group leftV = this.createSequentialGroup(qualityBox);
		
		leftH.addGroup(bottomMiddleH);
		leftV.addGroup(bottomMiddleV);
		
		Group bottomH = this.createSequentialGroup();
		bottomH.addWidget(remove);
		bottomH.addGroup(leftH);
		
		Group bottomV = this.createParallelGroup();
		bottomV.addWidget(remove);
		bottomV.addGroup(leftV);
		
		Group mainH = this.createParallelGroup();
		mainH.addWidget(name);
		mainH.addGroup(bottomH);
		
		Group mainV = this.createSequentialGroup();
		mainV.addWidget(name);
		mainV.addGroup(bottomV);
		
		this.setHorizontalGroup(mainH);
		this.setVerticalGroup(mainV);
	}
	
	public void addCallback(Callback callback) {
		callbacks.add(callback);
	}
	
	public void removeCallback(Callback callback) {
		callbacks.remove(callback);
	}
	
	public interface Callback {
		public void entryUnequipped(int slot);
	}
}
