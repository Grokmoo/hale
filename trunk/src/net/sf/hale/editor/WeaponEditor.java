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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.editor.widgets.SubItemEditor;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.BaseWeapon;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.Size;
import net.sf.hale.util.StringUtil;

import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class WeaponEditor extends SubItemEditor {
	private Item selectedItem;
	
	private Label title;
	
	private Label baseWeaponLabel;
	private ComboBox<String> baseWeaponBox;
	private SimpleChangableListModel<String> baseWeaponModel;
	
	private Label weaponTypeLabel;
	private ComboBox<String> weaponTypeBox;
	private SimpleChangableListModel<String> weaponTypeModel;
	
	private Label weaponHandedLabel;
	private ComboBox<String> weaponHandedBox;
	private SimpleChangableListModel<String> weaponHandedModel;
	private Label weaponSizeLabel;
	private ComboBox<String> weaponSizeBox;
	private SimpleChangableListModel<String> weaponSizeModel;
	
	private ToggleButton threatensButton;
	
	private Label minReachLabel, maxReachLabel;
	private ValueAdjusterInt minReachAdjuster, maxReachAdjuster;
	
	private Label damageTypeLabel;
	private ComboBox<String> damageTypeBox;
	private SimpleChangableListModel<String> damageTypeModel;
	
	private Label damageLabel, maxDamageLabel;
	private ValueAdjusterInt minDamageAdjuster, maxDamageAdjuster;
	
	private Label criticalLabel, criticalMultiplierLabel;
	private ValueAdjusterInt criticalRangeAdjuster, criticalMultiplierAdjuster;
	
	private Label attackCostLabel;
	private ValueAdjusterInt attackCostAdjuster;
	
	private Label rangePenaltyLabel;
	private ValueAdjusterInt rangePenaltyAdjuster;
	
	private Label maximumRangeLabel;
	private ValueAdjusterInt maximumRangeAdjuster;
	
	private Label maxStrengthBonusLabel;
	private ValueAdjusterInt maxStrengthBonusAdjuster;
	
	private ProjectileIconEditor projectileIconEditor;
	
	public WeaponEditor() {
		this.setTheme("");
		title = new Label("Weapon Properties");
		title.setTheme("/titlelabel");
		title.setSize(100, 20);
		title.setPosition(5, 0);
		this.add(title);
		
		baseWeaponLabel = new Label("Base Weapon");
		baseWeaponLabel.setTheme("/labelblack");
		baseWeaponLabel.setSize(50, 20);
		baseWeaponLabel.setPosition(5, 25);
		this.add(baseWeaponLabel);
		
		baseWeaponModel = new SimpleChangableListModel<String>();
		baseWeaponBox = new ComboBox<String>(baseWeaponModel);
		baseWeaponBox.setTheme("/combobox");
		baseWeaponBox.setSize(130, 20);
		baseWeaponBox.setPosition(100, 25);
		baseWeaponBox.addCallback(new Runnable() {
			@Override
			public void run() {
				if (baseWeaponBox.getSelected() == -1) return;
				
				String base = baseWeaponModel.getEntry(baseWeaponBox.getSelected());
				selectedItem.setBaseWeapon(Game.ruleset.getBaseWeapon(base));
			}
		});
		this.add(baseWeaponBox);
		
		Collection<BaseWeapon> weaps = Game.ruleset.getAllBaseWeaponTypes();
		List<BaseWeapon> weapons = new ArrayList<BaseWeapon>();
		for (BaseWeapon weapon : weaps) {
			weapons.add(weapon);
		}
		Collections.sort(weapons, new Comparator<BaseWeapon>() {
			@Override
			public int compare(BaseWeapon b1, BaseWeapon b2) {
				return b1.getName().compareTo(b2.getName());
			}
		});
		
		for (BaseWeapon wep : weapons) {
			baseWeaponModel.addElement(wep.getName());
		}
		
		weaponTypeLabel = new Label("Weapon Type");
		weaponTypeLabel.setTheme("/labelblack");
		weaponTypeLabel.setSize(50, 20);
		weaponTypeLabel.setPosition(5, 50);
		this.add(weaponTypeLabel);
		
		weaponTypeModel = new SimpleChangableListModel<String>();
		weaponTypeBox = new ComboBox<String>(weaponTypeModel);
		weaponTypeBox.setTheme("/combobox");
		weaponTypeBox.setSize(130, 20);
		weaponTypeBox.setPosition(100, 50);
		weaponTypeBox.addCallback(new Runnable() {
			@Override
			public void run() {
				if (weaponTypeBox.getSelected() == -1) return;
				
				String type = weaponTypeModel.getEntry(weaponTypeBox.getSelected()).toUpperCase();
				selectedItem.setWeaponType(Item.WeaponType.valueOf(type));				
			}
		});
		this.add(weaponTypeBox);
		
		for (Item.WeaponType type : Item.WeaponType.values()) {
			weaponTypeModel.addElement(StringUtil.upperCaseToWord(type.toString()));
		}
		
		weaponHandedLabel = new Label("Weapon Handed");
		weaponHandedLabel.setTheme("/labelblack");
		weaponHandedLabel.setSize(50, 20);
		weaponHandedLabel.setPosition(5, 75);
		this.add(weaponHandedLabel);
		
		weaponHandedModel = new SimpleChangableListModel<String>();
		weaponHandedBox = new ComboBox<String>(weaponHandedModel);
		weaponHandedBox.setTheme("/combobox");
		weaponHandedBox.setSize(115, 20);
		weaponHandedBox.setPosition(115, 75);
		weaponHandedBox.addCallback(new Runnable() {
			@Override
			public void run() {
				if (weaponHandedBox.getSelected() == -1) return;
				
				Item.WeaponHanded weaponHanded = null;
				String handedString = weaponHandedModel.getEntry(weaponHandedBox.getSelected());
				if (handedString.equals("Light")) weaponHanded = Item.WeaponHanded.LIGHT;
				else if (handedString.equals("One Handed")) weaponHanded = Item.WeaponHanded.ONE_HANDED;
				else if (handedString.equals("Two Handed")) weaponHanded = Item.WeaponHanded.TWO_HANDED;
				
				selectedItem.setWeaponHanded(weaponHanded);
			}
		});
		this.add(weaponHandedBox);
		
		for (Item.WeaponHanded handed : Item.WeaponHanded.values()) {
			weaponHandedModel.addElement(StringUtil.upperCaseToWord(handed.toString()));
		}
		
		weaponSizeLabel = new Label("for Creature Size");
		weaponSizeLabel.setTheme("/labelblack");
		weaponSizeLabel.setSize(100, 20);
		weaponSizeLabel.setPosition(20, 100);
		this.add(weaponSizeLabel);
		
		weaponSizeModel = new SimpleChangableListModel<String>();
		weaponSizeBox = new ComboBox<String>(weaponSizeModel);
		weaponSizeBox.setTheme("/combobox");
		weaponSizeBox.setSize(100, 20);
		weaponSizeBox.setPosition(130, 100);
		weaponSizeBox.addCallback(new Runnable() {
			@Override
			public void run() {
				if (weaponSizeBox.getSelected() == -1) return;
				
				String size = weaponSizeModel.getEntry(weaponSizeBox.getSelected());
				selectedItem.setWeaponSize(Size.get(size));
			}
		});
		this.add(weaponSizeBox);
		
		for (String name : Size.names) {
			weaponSizeModel.addElement(name);
		}
		
		threatensButton = new ToggleButton("Threatens");
		threatensButton.setTheme("/radiobutton");
		threatensButton.setSize(75, 15);
		threatensButton.setPosition(5, 125);
		threatensButton.addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setThreatens(threatensButton.isActive());
			}
		});
		this.add(threatensButton);
		
		minReachLabel = new Label("Reach");
		minReachLabel.setTheme("/labelblack");
		minReachLabel.setSize(50, 20);
		minReachLabel.setPosition(5, 150);
		this.add(minReachLabel);
		
		minReachAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 9, 1));
		minReachAdjuster.setTheme("/valueadjuster");
		minReachAdjuster.setSize(50, 20);
		minReachAdjuster.setPosition(50, 150);
		minReachAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setMinWeaponReach(minReachAdjuster.getValue());
				if (minReachAdjuster.getValue() > maxReachAdjuster.getValue()) {
					maxReachAdjuster.setValue(minReachAdjuster.getValue());
				}
			}
		});
		this.add(minReachAdjuster);
		
		maxReachLabel = new Label("to");
		maxReachLabel.setTheme("/labelblack");
		maxReachLabel.setSize(50, 20);
		maxReachLabel.setPosition(105, 150);
		this.add(maxReachLabel);
		
		maxReachAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 9, 1));
		maxReachAdjuster.setTheme("/valueadjuster");
		maxReachAdjuster.setSize(50, 20);
		maxReachAdjuster.setPosition(125, 150);
		maxReachAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setMaxWeaponReach(maxReachAdjuster.getValue());
				if (maxReachAdjuster.getValue() < minReachAdjuster.getValue()) {
					minReachAdjuster.setValue(maxReachAdjuster.getValue());
				}
			}
		});
		this.add(maxReachAdjuster);
		
		damageTypeLabel = new Label("Damage Type");
		damageTypeLabel.setTheme("/labelblack");
		damageTypeLabel.setSize(50, 20);
		damageTypeLabel.setPosition(5, 175);
		this.add(damageTypeLabel);
		
		damageTypeModel = new SimpleChangableListModel<String>();
		damageTypeBox = new ComboBox<String>(damageTypeModel);
		damageTypeBox.setTheme("/combobox");
		damageTypeBox.setSize(120, 20);
		damageTypeBox.setPosition(100, 175);
		damageTypeBox.addCallback(new Runnable() {
			@Override
			public void run() {
				if (damageTypeBox.getSelected() == -1) return;
				
				String type = damageTypeModel.getEntry(damageTypeBox.getSelected());
				selectedItem.setDamageType(Game.ruleset.getDamageType(type));
			}
		});
		this.add(damageTypeBox);
		
		for (DamageType type : Game.ruleset.getAllDamageTypes()) {
			if (type.isPhysical() && !type.getName().equals(Game.ruleset.getString("PhysicalDamageType")))
				damageTypeModel.addElement(type.getName());
		}
		
		damageLabel = new Label("Damage");
		damageLabel.setTheme("/labelblack");
		damageLabel.setSize(50, 20);
		damageLabel.setPosition(5, 200);
		this.add(damageLabel);
		
		minDamageAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 99, 1));
		minDamageAdjuster.setTheme("/valueadjuster");
		minDamageAdjuster.setSize(55, 20);
		minDamageAdjuster.setPosition(60, 200);
		minDamageAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setMinDamage(minDamageAdjuster.getValue());
				if (minDamageAdjuster.getValue() > maxDamageAdjuster.getValue()) {
					maxDamageAdjuster.setValue(minDamageAdjuster.getValue());
				}
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
		maxDamageAdjuster.setSize(55, 20);
		maxDamageAdjuster.setPosition(140, 200);
		maxDamageAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setMaxDamage(maxDamageAdjuster.getValue());
				if (maxDamageAdjuster.getValue() < minDamageAdjuster.getValue()) {
					minDamageAdjuster.setValue(maxDamageAdjuster.getValue());
				}
			}
		});
		this.add(maxDamageAdjuster);
		
		criticalLabel = new Label("Critical");
		criticalLabel.setTheme("/labelblack");
		criticalLabel.setSize(50, 20);
		criticalLabel.setPosition(5, 225);
		this.add(criticalLabel);
		
		criticalRangeAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 100, 97));
		criticalRangeAdjuster.setTheme("/valueadjuster");
		criticalRangeAdjuster.setSize(60, 20);
		criticalRangeAdjuster.setPosition(55, 225);
		criticalRangeAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setCriticalThreatRange(criticalRangeAdjuster.getValue());
			}
		});
		this.add(criticalRangeAdjuster);
		
		criticalMultiplierLabel = new Label("to 100 / x");
		criticalMultiplierLabel.setTheme("/labelblack");
		criticalMultiplierLabel.setSize(50, 20);
		criticalMultiplierLabel.setPosition(120, 225);
		this.add(criticalMultiplierLabel);
		
		criticalMultiplierAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 10, 2));
		criticalMultiplierAdjuster.setTheme("/valueadjuster");
		criticalMultiplierAdjuster.setSize(60, 20);
		criticalMultiplierAdjuster.setPosition(185, 225);
		criticalMultiplierAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setCriticalMultiplier(criticalMultiplierAdjuster.getValue());
			}
		});
		this.add(criticalMultiplierAdjuster);
		
		attackCostLabel = new Label("Attack Cost");
		attackCostLabel.setTheme("/labelblack");
		attackCostLabel.setSize(50, 20);
		attackCostLabel.setPosition(265, 25);
		this.add(attackCostLabel);
		
		attackCostAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 10000, 4000));
		attackCostAdjuster.setTheme("/valueadjuster");
		attackCostAdjuster.setSize(75, 20);
		attackCostAdjuster.setPosition(345, 25);
		attackCostAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setAttackCost(attackCostAdjuster.getValue());
			}
		});
		this.add(attackCostAdjuster);
		
		rangePenaltyLabel = new Label("Range Penalty");
		rangePenaltyLabel.setTheme("/labelblack");
		rangePenaltyLabel.setSize(50, 20);
		rangePenaltyLabel.setPosition(265, 50);
		this.add(rangePenaltyLabel);
		
		rangePenaltyAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		rangePenaltyAdjuster.setTheme("/valueadjuster");
		rangePenaltyAdjuster.setSize(60, 20);
		rangePenaltyAdjuster.setPosition(360, 50);
		rangePenaltyAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setRangePenalty(rangePenaltyAdjuster.getValue());
			}
		});
		this.add(rangePenaltyAdjuster);
		
		maximumRangeLabel = new Label("Maximum Range");
		maximumRangeLabel.setTheme("/labelblack");
		maximumRangeLabel.setSize(50, 20);
		maximumRangeLabel.setPosition(255, 75);
		this.add(maximumRangeLabel);
		
		maximumRangeAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 999, 0));
		maximumRangeAdjuster.setTheme("/valueadjuster");
		maximumRangeAdjuster.setSize(60, 20);
		maximumRangeAdjuster.setPosition(360, 75);
		maximumRangeAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setMaximumRange(maximumRangeAdjuster.getValue());
			}
		});
		this.add(maximumRangeAdjuster);
		
		maxStrengthBonusLabel = new Label("Maximum Strength Bonus");
		maxStrengthBonusLabel.setTheme("/labelblack");
		maxStrengthBonusLabel.setSize(50, 20);
		maxStrengthBonusLabel.setPosition(240, 100);
		this.add(maxStrengthBonusLabel);
		
		maxStrengthBonusAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		maxStrengthBonusAdjuster.setTheme("/valueadjuster");
		maxStrengthBonusAdjuster.setSize(60, 20);
		maxStrengthBonusAdjuster.setPosition(360, 100);
		maxStrengthBonusAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedItem.setMaxStrengthBonus(maxStrengthBonusAdjuster.getValue());
			}
		});
		this.add(maxStrengthBonusAdjuster);
		
		projectileIconEditor = new ProjectileIconEditor();
		projectileIconEditor.setPosition(240, 150);
		projectileIconEditor.setSize(200, 100);
		this.add(projectileIconEditor);
	}
	
	@Override
	public void getPropertiesFromItem(Item item) {
		this.selectedItem = item;
		
		if (item.getBaseWeapon() != null) {
			baseWeaponBox.setSelected(baseWeaponModel.findElement(item.getBaseWeapon().getName()));
		} else {
			baseWeaponBox.setSelected(-1);
		}
		
		if (item.getWeaponType() != null) {
			weaponTypeBox.setSelected(weaponTypeModel.findElement(StringUtil.upperCaseToWord(item.getWeaponType().toString())));
		} else
			weaponTypeBox.setSelected(-1);
		
		
		if (item.getWeaponHanded() != null) {
			weaponHandedBox.setSelected(weaponHandedModel.findElement(StringUtil.upperCaseToWord(item.getWeaponHanded().toString())));
		} else
			weaponHandedBox.setSelected(-1);
		
		if (item.getWeaponSize() != null) {
			weaponSizeBox.setSelected(weaponSizeModel.findElement(item.getWeaponSize().getName()));
		} else
			weaponSizeBox.setSelected(-1);
		
		
		this.threatensButton.setActive(item.threatens());
		
		// get the reach before setting either adjuster, as setting the adjusters
		// will change the values
		int minReach = item.getWeaponReachMin();
		int maxReach = item.getWeaponReachMax();
		
		this.minReachAdjuster.setValue(minReach);
		this.maxReachAdjuster.setValue(maxReach);
		
		if (item.getDamageType() != null) {
			damageTypeBox.setSelected(damageTypeModel.findElement(item.getDamageType().getName()));
		} else
			damageTypeBox.setSelected(-1);
		
		// get the damage values before setting either adjuster, as setting the adjusters
		// will change the values
		int minDamage = item.getDamageMin();
		int maxDamage = item.getDamageMax();
		
		this.minDamageAdjuster.setValue(minDamage);
		this.maxDamageAdjuster.setValue(maxDamage);
		
		this.criticalRangeAdjuster.setValue(item.getCriticalThreatRange());
		this.criticalMultiplierAdjuster.setValue(item.getCriticalMultiplier());
		this.attackCostAdjuster.setValue(item.getAttackCost());
		
		this.rangePenaltyAdjuster.setValue(item.getRangePenalty());
		this.maximumRangeAdjuster.setValue(item.getMaximumRange());
		this.maxStrengthBonusAdjuster.setValue(item.getMaxStrengthBonus());
		
		projectileIconEditor.setVisible(item.getWeaponType() == Item.WeaponType.THROWN);
		
		projectileIconEditor.getPropertiesFromItem(item);
	}
}
