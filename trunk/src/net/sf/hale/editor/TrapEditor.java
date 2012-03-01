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
import net.sf.hale.entity.Trap;
import net.sf.hale.rules.DamageType;

import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class TrapEditor extends SubItemEditor {
	private Trap selectedTrap;
	
	private final Label title;
	
	private final Label findTrapLabel, placeTrapLabel, disarmTrapLabel, recoverTrapLabel, reflexTrapLabel;
	
	private final ValueAdjusterInt findTrapAdjuster, placeTrapAdjuster, disarmTrapAdjuster,
							 recoverTrapAdjuster, reflexTrapAdjuster;
	
	private final ToggleButton activateOnceButton;
	
	private final Label damageTypeLabel;
	private final ComboBox<String> damageTypeBox;
	private final SimpleChangableListModel<String> damageTypeModel;
	
	private final Label damageLabel, maxDamageLabel;
	private final ValueAdjusterInt minDamageAdjuster, maxDamageAdjuster;
	
	public TrapEditor() {
		this.setTheme("");
		
		title = new Label("Trap Properties");
		title.setTheme("/titlelabel");
		title.setPosition(5, 15);
		this.add(title);
		
		findTrapLabel = new Label("Find Trap Difficulty (Search)");
		findTrapLabel.setTheme("/labelblack");
		findTrapLabel.setPosition(5, 45);
		this.add(findTrapLabel);
		
		placeTrapLabel = new Label("Place Trap Difficulty (Traps)");
		placeTrapLabel.setTheme("/labelblack");
		placeTrapLabel.setPosition(5, 65);
		this.add(placeTrapLabel);
		
		disarmTrapLabel = new Label("Disarm Trap Difficulty (Traps)");
		disarmTrapLabel.setTheme("/labelblack");
		disarmTrapLabel.setPosition(5, 85);
		this.add(disarmTrapLabel);
		
		recoverTrapLabel = new Label("Recover Trap Difficulty (Traps)");
		recoverTrapLabel.setTheme("/labelblack");
		recoverTrapLabel.setPosition(5, 105);
		this.add(recoverTrapLabel);
		
		reflexTrapLabel = new Label("Reflex Difficulty (Reflex)");
		reflexTrapLabel.setTheme("/labelblack");
		reflexTrapLabel.setPosition(5, 125);
		this.add(reflexTrapLabel);
		
		findTrapAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		findTrapAdjuster.setTheme("/valueadjuster");
		findTrapAdjuster.setSize(70, 20);
		findTrapAdjuster.setPosition(190, 35);
		findTrapAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setFindDifficulty(findTrapAdjuster.getValue());
			}
		});
		this.add(findTrapAdjuster);
		
		placeTrapAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		placeTrapAdjuster.setTheme("/valueadjuster");
		placeTrapAdjuster.setSize(70, 20);
		placeTrapAdjuster.setPosition(190, 55);
		placeTrapAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setPlaceDifficulty(placeTrapAdjuster.getValue());
			}
		});
		this.add(placeTrapAdjuster);
		
		disarmTrapAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		disarmTrapAdjuster.setTheme("/valueadjuster");
		disarmTrapAdjuster.setSize(70, 20);
		disarmTrapAdjuster.setPosition(190, 75);
		disarmTrapAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setDisarmDifficulty(disarmTrapAdjuster.getValue());
			}
		});
		this.add(disarmTrapAdjuster);
		
		recoverTrapAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		recoverTrapAdjuster.setTheme("/valueadjuster");
		recoverTrapAdjuster.setSize(70, 20);
		recoverTrapAdjuster.setPosition(190, 95);
		recoverTrapAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setRecoverDifficulty(recoverTrapAdjuster.getValue());
			}
		});
		this.add(recoverTrapAdjuster);
		
		reflexTrapAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		reflexTrapAdjuster.setTheme("/valueadjuster");
		reflexTrapAdjuster.setSize(70, 20);
		reflexTrapAdjuster.setPosition(190, 115);
		reflexTrapAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setReflexDifficulty(reflexTrapAdjuster.getValue());
			}
		});
		this.add(reflexTrapAdjuster);
		
		activateOnceButton = new ToggleButton("Activate Only Once");
		activateOnceButton.setTheme("/radiobutton");
		activateOnceButton.setSize(120, 15);
		activateOnceButton.setPosition(5, 140);
		activateOnceButton.addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setActivateOnlyOnce(activateOnceButton.isActive());
			}
		});
		this.add(activateOnceButton);
		
		damageTypeLabel = new Label("Damage Type");
		damageTypeLabel.setTheme("/labelblack");
		damageTypeLabel.setPosition(5, 185);
		this.add(damageTypeLabel);
		
		damageTypeModel = new SimpleChangableListModel<String>();
		damageTypeBox = new ComboBox<String>(damageTypeModel);
		damageTypeBox.setTheme("/combobox");
		damageTypeBox.setSize(120, 20);
		damageTypeBox.setPosition(100, 175);
		damageTypeBox.addCallback(new Runnable() {
			@Override
			public void run() {
				if (damageTypeBox.getSelected() != -1) {
					selectedTrap.setDamageType(Game.ruleset.getDamageType(damageTypeModel.getEntry(damageTypeBox.getSelected())));
				}
			}
		});
		this.add(damageTypeBox);
		
		for (DamageType type : Game.ruleset.getAllDamageTypes()) {
			if (!type.getName().equals(Game.ruleset.getString("PhysicalDamageType")))
				damageTypeModel.addElement(type.getName());
		}
		
		damageLabel = new Label("Damage");
		damageLabel.setTheme("/labelblack");
		damageLabel.setPosition(5, 210);
		this.add(damageLabel);
		
		minDamageAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 99, 1));
		minDamageAdjuster.setTheme("/valueadjuster");
		minDamageAdjuster.setSize(55, 20);
		minDamageAdjuster.setPosition(60, 200);
		minDamageAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setMinDamage(minDamageAdjuster.getValue());
			}
		});
		this.add(minDamageAdjuster);
		
		maxDamageLabel = new Label("to");
		maxDamageLabel.setTheme("/labelblack");
		maxDamageLabel.setSize(50, 20);
		maxDamageLabel.setPosition(120, 200);
		this.add(maxDamageLabel);
		
		maxDamageAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 99, 1));
		maxDamageAdjuster.setTheme("/valueadjuster");
		maxDamageAdjuster.setMinMaxValue(1, 99);
		maxDamageAdjuster.setSize(55, 20);
		maxDamageAdjuster.setPosition(140, 200);
		maxDamageAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedTrap.setMaxDamage(maxDamageAdjuster.getValue());
			}
		});
		this.add(maxDamageAdjuster);
	}
	
	@Override
	public void getPropertiesFromItem(Item item) {
		this.selectedTrap = (Trap)item;
		
		findTrapAdjuster.setValue(selectedTrap.getFindDifficulty());
		placeTrapAdjuster.setValue(selectedTrap.getPlaceDifficulty());
		disarmTrapAdjuster.setValue(selectedTrap.getDisarmDifficulty());
		recoverTrapAdjuster.setValue(selectedTrap.getRecoverDifficulty());
		reflexTrapAdjuster.setValue(selectedTrap.getReflexDifficulty());
		
		activateOnceButton.setActive(selectedTrap.activatesOnlyOnce());
		
		if (item.getDamageType() != null) {
			this.damageTypeBox.setSelected(damageTypeModel.findElement(item.getDamageType().getName()));
		}
		
		this.minDamageAdjuster.setValue(item.getDamageMin());
		this.maxDamageAdjuster.setValue(item.getDamageMax());
	}
}
