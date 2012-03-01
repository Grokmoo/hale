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
import net.sf.hale.editor.widgets.SubItemEditor;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.ArmorType;

import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class ArmorEditor extends SubItemEditor {
	private Item selectedItem;
	
	private Label title;
	
	private Label armorTypeLabel;
	private ComboBox<String> armorType;
	private SimpleChangableListModel<String> armorTypeModel;
	
	private Label armorClassLabel;
	private ValueAdjusterInt armorClass;
	
	private Label armorPenaltyLabel;
	private ValueAdjusterInt armorPenalty;
	
	private Label movementPenaltyLabel;
	private ValueAdjusterInt movementPenalty;
	
	private Label shieldAttackPenaltyLabel;
	private ValueAdjusterInt shieldAttackPenalty;
	
	private ToggleButton coversBeard;
	
	public ArmorEditor() {
		this.setTheme("");
		
		title = new Label("Armor and Shield Properties");
		title.setTheme("/titlelabel");
		title.setSize(100, 20);
		title.setPosition(5, 5);
		this.add(title);
		
		armorTypeLabel = new Label("Armor / Shield Type");
		armorTypeLabel.setTheme("/labelblack");
		armorTypeLabel.setSize(50, 20);
		armorTypeLabel.setPosition(5, 30);
		this.add(armorTypeLabel);
		
		armorTypeModel = new SimpleChangableListModel<String>();
		armorType = new ComboBox<String>(armorTypeModel);
		armorType.setTheme("/combobox");
		armorType.setSize(120, 20);
		armorType.setPosition(135, 30);
		armorType.addCallback(new Runnable() {
			@Override
			public void run() {
				if (armorType.getSelected() == -1) return;
				
				String type = armorTypeModel.getEntry(armorType.getSelected());
				selectedItem.setArmorType(Game.ruleset.getArmorType(type));
			}
		});
		this.add(armorType);
		
		for (ArmorType type : Game.ruleset.getAllArmorTypes()) {
			armorTypeModel.addElement(type.getName());
		}
		
		armorClassLabel = new Label("Armor Class");
		armorClassLabel.setTheme("/labelblack");
		armorClassLabel.setSize(50, 20);
		armorClassLabel.setPosition(5, 55);
		this.add(armorClassLabel);
		
		armorClass = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		armorClass.setTheme("/valueadjuster");
		armorClass.setSize(65, 20);
		armorClass.setPosition(85, 55);
		armorClass.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setArmorClass(armorClass.getValue());
			}
		});
		this.add(armorClass);
		
		armorPenaltyLabel = new Label("Armor Penalty");
		armorPenaltyLabel.setTheme("/labelblack");
		armorPenaltyLabel.setSize(50, 20);
		armorPenaltyLabel.setPosition(5, 80);
		this.add(armorPenaltyLabel);
		
		armorPenalty = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		armorPenalty.setTheme("/valueadjuster");
		armorPenalty.setSize(65, 20);
		armorPenalty.setPosition(100, 80);
		armorPenalty.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setArmorPenalty(armorPenalty.getValue());
			}
		});
		this.add(armorPenalty);
		
		movementPenaltyLabel = new Label("Movement Penalty");
		movementPenaltyLabel.setTheme("/labelblack");
		movementPenaltyLabel.setSize(50, 20);
		movementPenaltyLabel.setPosition(5, 105);
		this.add(movementPenaltyLabel);
		
		movementPenalty = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		movementPenalty.setTheme("/valueadjuster");
		movementPenalty.setSize(65, 20);
		movementPenalty.setPosition(125, 105);
		movementPenalty.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setMovementPenalty(movementPenalty.getValue());
			}
		});
		this.add(movementPenalty);
		
		shieldAttackPenaltyLabel = new Label("Shield Attack Penalty");
		shieldAttackPenaltyLabel.setTheme("/labelblack");
		shieldAttackPenaltyLabel.setPosition(5, 140);
		this.add(shieldAttackPenaltyLabel);
		
		shieldAttackPenalty = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		shieldAttackPenalty.setTheme("valueadjuster");
		shieldAttackPenalty.setSize(65, 20);
		shieldAttackPenalty.setPosition(145, 130);
		shieldAttackPenalty.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setShieldAttackPenalty(shieldAttackPenalty.getValue());
			}
		});
		this.add(shieldAttackPenalty);
		
		coversBeard = new ToggleButton("Covers Beard");
		coversBeard.setTheme("/radiobutton");
		coversBeard.setPosition(5, 155);
		coversBeard.setSize(100, 20);
		coversBeard.addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setCoversBeardIcon(coversBeard.isActive());
			}
		});
		add(coversBeard);
		
	}
	
	@Override public void getPropertiesFromItem(Item item) {
		this.selectedItem = item;
		
		if (item.getArmorType() != null) {
			String armorTypeString = item.getArmorType().getName();
			this.armorType.setSelected(armorTypeModel.findElement(armorTypeString));
		} else {
			this.armorType.setSelected(-1);
		}
		
		shieldAttackPenalty.setVisible(item.getItemType() == Item.ItemType.SHIELD);
		shieldAttackPenaltyLabel.setVisible(item.getItemType() == Item.ItemType.SHIELD);
		
		this.armorClass.setValue(item.getArmorClass());
		this.armorPenalty.setValue(item.getArmorPenalty());
		this.movementPenalty.setValue(item.getMovementPenalty());
		this.shieldAttackPenalty.setValue(item.getShieldAttackPenalty());
		
		this.coversBeard.setActive(item.coversBeardIcon());
	}
}
