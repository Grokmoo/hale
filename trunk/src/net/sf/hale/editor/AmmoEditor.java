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

import net.sf.hale.editor.widgets.SubItemEditor;
import net.sf.hale.entity.Item;
import net.sf.hale.util.StringUtil;

import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class AmmoEditor extends SubItemEditor {
	private Item selectedItem;
	
	private Label title;
	
	private Label weaponTypeLabel;
	private ComboBox<String> weaponType;
	private SimpleChangableListModel<String> weaponTypeModel;
	
	private ProjectileIconEditor projectileIconEditor;
	
	public AmmoEditor() {
		this.setTheme("");
		
		title = new Label("Ammo Properties");
		title.setTheme("/titlelabel");
		title.setSize(100, 20);
		title.setPosition(5, 5);
		this.add(title);
		
		weaponTypeLabel = new Label("Weapon Type");
		weaponTypeLabel.setTheme("/labelblack");
		weaponTypeLabel.setSize(50, 20);
		weaponTypeLabel.setPosition(5, 30);
		this.add(weaponTypeLabel);
		
		weaponTypeModel = new SimpleChangableListModel<String>();
		weaponType = new ComboBox<String>(weaponTypeModel);
		weaponType.setTheme("/combobox");
		weaponType.setSize(120, 20);
		weaponType.setPosition(100, 30);
		weaponType.addCallback(new Runnable() {
			@Override
			public void run() {
				if (weaponType.getSelected() == -1) return;
				
				String type = weaponTypeModel.getEntry(weaponType.getSelected()).toUpperCase();
				selectedItem.setWeaponType(Item.WeaponType.valueOf(type));
			}
		});
		this.add(weaponType);
		
		for (Item.WeaponType type : Item.WeaponType.values()) {
			weaponTypeModel.addElement(StringUtil.upperCaseToWord(type.toString()));
		}
		
		projectileIconEditor = new ProjectileIconEditor();
		projectileIconEditor.setPosition(5, 60);
		projectileIconEditor.setSize(200, 100);
		this.add(projectileIconEditor);
	}
	
	@Override public void getPropertiesFromItem(Item item) {
		this.selectedItem = item;
		
		if (item.getWeaponType() != null) {
			String type = StringUtil.upperCaseToWord(item.getWeaponType().toString());
			weaponType.setSelected(weaponTypeModel.findElement(type));
		} else {
			weaponType.setSelected(-1);
		}
		
		projectileIconEditor.getPropertiesFromItem(item);
	}
}
