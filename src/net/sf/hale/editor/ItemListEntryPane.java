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

import net.sf.hale.editor.reference.ReferencePopupWindow;
import net.sf.hale.rules.ItemList;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class ItemListEntryPane extends DialogLayout {
	private final ItemList itemList;
	private final int index;
	private final SimpleChangableListModel<String> qualitiesModel;
	
	private final List<Callback> callbacks;
	
	private final ValueAdjusterInt quantity;
	private final Button infiniteQuantity;
	
	private final ComboBox<String> quality;
	
	private final Button refButton;
	
	public ItemListEntryPane(ItemList itemList, int index, SimpleChangableListModel<String> qualitiesModel, boolean showEquip) {
		this.itemList = itemList;
		this.index = index;
		this.qualitiesModel = qualitiesModel;
		callbacks = new LinkedList<Callback>();
		
		this.setTheme("/itemlistentrypane");
		
		Button remove = new Button("[x]");
		remove.setTooltipContent("Remove this item");
		
		remove.addCallback(new Runnable() {
			@Override public void run() {
				for (Callback callback : callbacks) {
					callback.entryRemoved(ItemListEntryPane.this.index);
				}
			}
		});
		
		Button equip = new Button("Eqp");
		equip.setTooltipContent("Equip this item");
		equip.setVisible(showEquip);
		equip.addCallback(new Runnable() {
			@Override public void run() {
				for (Callback callback : callbacks) {
					callback.entryEquipped(ItemListEntryPane.this.index);
				}
			}
		});
		
		Button offHand = new Button("Off");
		offHand.setTooltipContent("Equip this item off hand");
		offHand.setVisible(showEquip);
		offHand.addCallback(new Runnable() {
			@Override public void run() {
				for (Callback callback : callbacks) {
					callback.entryEquippedOffHand(ItemListEntryPane.this.index);
				}
			}
		});
		
		refButton = new Button(">>");
		refButton.addCallback(new Runnable() {
			@Override public void run() {
				new ReferencePopupWindow(refButton,
						ItemListEntryPane.this.itemList.getItem(ItemListEntryPane.this.index)).openPopup();
			}
		});
		
		Label name = new Label(itemList.getItem(index).getName());
		
		quantity = new ValueAdjusterInt(new SimpleIntegerModel(1, Integer.MAX_VALUE, 1));
		quantity.setValue(itemList.getQuantity(index));
		quantity.getModel().addCallback(new Runnable() {
			@Override public void run() {
				ItemListEntryPane.this.itemList.setQuantity(ItemListEntryPane.this.index, quantity.getValue());
			}
		});
		
		infiniteQuantity = new Button("\u221E");
		infiniteQuantity.addCallback(new Runnable() {
			@Override public void run() {
				quantity.setValue(Integer.MAX_VALUE);
			}
		});
		
		quality = new ComboBox<String>(qualitiesModel);
		if (qualitiesModel != null) {
			quality.setSelected(qualitiesModel.findElement(itemList.getQuality(index)));
			quality.addCallback(new Runnable() {
				@Override public void run() {
					ItemListEntryPane.this.itemList.setQuality(ItemListEntryPane.this.index,
							ItemListEntryPane.this.qualitiesModel.getEntry(quality.getSelected()));
				}
			});
		} else {
			quality.setVisible(false);
		}
		
		Group topH = this.createSequentialGroup(remove, name);
		Group topV = this.createParallelGroup(remove, name);
		
		Group leftH = this.createParallelGroup();
		Group leftV = this.createSequentialGroup();
		
		if (showEquip) {
			leftH.addWidgets(equip, offHand);
			leftV.addWidgets(equip, offHand);
		}
		
		Group middleH = this.createSequentialGroup(quantity);
		middleH.addGap(0);
		middleH.addWidget(infiniteQuantity);
		middleH.addWidget(refButton);

		Group middleV = this.createParallelGroup(quantity, infiniteQuantity, refButton);
		
		Group rightH = this.createParallelGroup(middleH);
		Group rightV = this.createSequentialGroup(middleV);
		
		if (qualitiesModel != null) {
			rightH.addWidget(quality);
			rightV.addWidget(quality);
		}
		
		Group bottomH = this.createSequentialGroup(leftH, rightH);
		Group bottomV = this.createParallelGroup(leftV, rightV);
		
		this.setHorizontalGroup(this.createParallelGroup(topH, bottomH));
		this.setVerticalGroup(this.createSequentialGroup(topV, bottomV));
	}
	
	public void addCallback(Callback callback) {
		callbacks.add(callback);
	}
	
	public void removeCallback(Callback callback) {
		callbacks.remove(callback);
	}
	
	public interface Callback {
		public void entryRemoved(int index);
		public void entryEquipped(int index);
		public void entryEquippedOffHand(int index);
	}
}
