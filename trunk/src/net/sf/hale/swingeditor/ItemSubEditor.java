/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.swingeditor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.hale.Game;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Enchantment;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Openable;
import net.sf.hale.entity.Trap;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.ArmorType;
import net.sf.hale.rules.BaseWeapon;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.Size;

/**
 * A sub editor for editing items
 * @author Jared
 *
 */

public class ItemSubEditor extends SubEditorPanel {
	private Item item;
	
	private JTextField nameField;
	private JTextArea descriptionField;
	private JSpinner value;
	private JSpinner stackSize;
	private JSpinner weight;
	private JComboBox quality;
	private JComboBox type;
	private JCheckBox ingredient, quest, cursed, noQuality;
	
	private JTabbedPane tabs;
	private AppearancePanel appearance;
	private ScriptPanel script;
	private AmmoPanel ammo;
	private ArmorPanel armor;
	private WeaponTypePanel weaponType;
	private WeaponValuesPanel weaponValues;
	private TrapPanel trap;
	private DoorPanel door;
	private OpenablePanel openable;
	private ContainerPanel container;
	
	/**
	 * Creates a new SubEditor for the specified item
	 * @param parent the parent frame
	 * @param item the item being edited
	 */
	
	protected ItemSubEditor(JFrame parent, Item item) {
		super(parent);
		this.item = item;
		
		// add ID label
		JLabel id = new JLabel(item.getID());
		addRow("ID", id);
		
		// add name field and editor
		nameField = new JTextField(item.getName());
		addRowFilled("Name", nameField);
		
		// add description field and editor
		descriptionField = new JTextArea(item.getDescription());
		descriptionField.setLineWrap(true);
		addRowFilled("Description", descriptionField);
		
		// add value field
		value = new JSpinner(new SpinnerNumberModel(item.getValue().getValue(), 0, 100000, 1));
		stackSize = new JSpinner(new SpinnerNumberModel(item.getValueStackSize(), 1, 1000, 1));
		addRow("Value in CP", value, new JLabel("for Stack Size"), stackSize);
		
		// add weight field
		weight = new JSpinner(new SpinnerNumberModel(item.getWeightGrams(), 0, 100000, 100));
		addRow("Weight in Grams", weight);
		
		// add quality field
		quality = new JComboBox();
		for (int i = 0; i < Game.ruleset.getNumItemQualities(); i++) {
			quality.addItem(Game.ruleset.getItemQuality(i).getName());
			
			if (item.getQuality() == Game.ruleset.getItemQuality(i))
				quality.setSelectedIndex(i);
		}
		addRow("Quality", quality);
		
		// add type field
		type = new JComboBox();
		int i = 0;
		for (Item.ItemType itemType : Item.ItemType.values()) {
			type.addItem(itemType.toString());
			
			if (item.getItemType() == itemType) type.setSelectedIndex(i);
			
			i++;
		}
		addRow("Item Type", type);
		
		// add toggle options
		ingredient = new JCheckBox("Ingredient", item.isIngredient());
		quest = new JCheckBox("Quest", item.isQuestItem());
		cursed = new JCheckBox("Cursed", item.isCursed());
		noQuality = new JCheckBox("Force No Quality", item.isForceNoQuality());
		addRow(ingredient, quest, cursed, noQuality);
		
		// add tabbed pane
		tabs = new JTabbedPane();
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy++;
		c.gridx = 0;
		add(tabs, c);
		
		// add appearance tab
		appearance = new AppearancePanel();
		tabs.addTab("Appearance", appearance);
		
		switch (item.getType()) {
		case CONTAINER:
			openable = new OpenablePanel(parentFrame);
			container = new ContainerPanel();
			tabs.addTab("Container Properties", container);
			tabs.addTab("Openable Properties", openable);
			break;
		case DOOR:
			openable = new OpenablePanel(parentFrame);
			door = new DoorPanel();
			tabs.addTab("Door Properties", door);
			tabs.addTab("Openable Properties", openable);
			break;
		case TRAP:
			trap = new TrapPanel(parentFrame);
			tabs.addTab("Trap Properties", trap);
			break;
		}
		
		switch (item.getItemType()) {
		case AMMO:
			// add ammo tab
			ammo = new AmmoPanel();
			tabs.addTab("Ammo Properties", ammo);
			break;
		case ARMOR: case BOOTS: case GLOVES: case HELMET: case SHIELD:
			// add armor tab
			armor = new ArmorPanel(parentFrame);
			tabs.addTab("Armor Properties", armor);
			break;
		case WEAPON:
			// add weapon tabs
			weaponType = new WeaponTypePanel(parentFrame);
			tabs.addTab("Weapon Properties", weaponType);
			
			weaponValues = new WeaponValuesPanel(parentFrame);
			tabs.addTab("Weapon Values", weaponValues);
		}
		
		// add script tab
		script = new ScriptPanel(parentFrame);
		tabs.addTab("Script", script);
	}
	
	private class ContainerPanel extends JPanel {
		private JCheckBox workbench;
		private LootSubEditor loot;
		
		private ContainerPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			Container container = (Container)item;
			
			workbench = new JCheckBox("Workbench");
			workbench.setSelected(container.isWorkbench());
			
			add(workbench);
			
			loot = new LootSubEditor(parentFrame);
			add(loot);
		}
	}
	
	private class OpenablePanel extends SubEditorPanel {
		private JCheckBox locked;
		private JSpinner lockDifficulty;
		private JButton setKey;
		private JCheckBox keyRequired;
		private JCheckBox removeKeyOnUnlock;
		
		private OpenablePanel(JFrame parent) {
			super(parent);
			
			Openable openable = (Openable)item;
			
			locked = new JCheckBox("Locked");
			locked.setSelected(openable.isLocked());
			addRow(locked);
			
			lockDifficulty = new JSpinner(new SpinnerNumberModel(openable.getLockDifficulty(), 0, 999, 1));
			addRow("Lock Difficulty", lockDifficulty);
			
			setKey = new JButton(openable.getKey());
			setKey.setAction(new SetKeyAction());
			if (openable.getKey() == null)
				setKey.setText("None");
			addRow("Key", setKey);
			
			keyRequired = new JCheckBox("Key Required");
			keyRequired.setSelected(openable.isKeyRequired());
			addRow(keyRequired);
			
			removeKeyOnUnlock = new JCheckBox("Remove Key on Unlock");
			removeKeyOnUnlock.setSelected(openable.removeKeyOnUnlock());
			addRow(removeKeyOnUnlock);
		}
		
		private class KeySelectedAction extends AbstractAction {
			@Override public void actionPerformed(ActionEvent evt) {
				setKey.setText(evt.getActionCommand());
			}
		}
		
		private class SetKeyAction extends AbstractAction {
			@Override public void actionPerformed(ActionEvent evt) {
				SetKeyDialog window = new SetKeyDialog();
				window.setVisible(true);
			}
		}
		
		private class SetKeyDialog extends JDialog {
			private SetKeyDialog() {
				super(parentFrame, "Choose an Item", true);
				
				ItemSelector selector = new ItemSelector();
				selector.setDialogMode(this);
				selector.addSelectionAction(new KeySelectedAction());
				
				add(selector);
				
				pack();
				setLocationRelativeTo(parentFrame);
			}
			
			@Override public Dimension getPreferredSize() {
				Dimension sup = super.getPreferredSize();
				
				if (sup.height > 600) sup.height = 600;
				
				return sup;
			}
		}
	}
	
	private class DoorPanel extends JPanel {
		private JCheckBox transparent;
		
		private DoorPanel() {
			Door door = (Door)item;
			
			transparent = new JCheckBox("Transparent if Closed");
			transparent.setSelected(door.isTransparent());
			
			add(transparent);
		}
	}
	
	private class TrapPanel extends SubEditorPanel {
		private JComboBox damageType;
		private JSpinner damageMin, damageMax;
		private JCheckBox activateOnce;
		
		private JSpinner find, place, disarm, recover, reflex;
		
		public TrapPanel(JFrame parent) {
			super(parent);
			
			Trap trap = (Trap)item;
			
			damageType = new JComboBox();
			int i = 0;
			for (DamageType type : Game.ruleset.getAllDamageTypes()) {
				if ( type.getName().equals(Game.ruleset.getString("PhysicalDamageType")) ) continue;
				
				damageType.addItem(type.getName());
				
				if (type == trap.getDamageType()) damageType.setSelectedIndex(i);
				
				i++;
			}
			
			damageMin = new JSpinner(new SpinnerNumberModel(trap.getDamageMin(), 0, 100, 1));
			damageMax = new JSpinner(new SpinnerNumberModel(trap.getDamageMax(), 0, 100, 1));
			addRow("Damage", damageMin, new JLabel(" to "), damageMax, damageType);
			
			activateOnce = new JCheckBox("Activate Only Once");
			activateOnce.setSelected(trap.activatesOnlyOnce());
			addRow(activateOnce);
			
			find = new JSpinner(new SpinnerNumberModel(trap.getFindDifficulty(), 0, 999, 1));
			addRow("Find Difficulty (Search)", find);
			
			place = new JSpinner(new SpinnerNumberModel(trap.getPlaceDifficulty(), 0, 999, 1));
			addRow("Place Difficulty (Traps)", place);
			
			disarm = new JSpinner(new SpinnerNumberModel(trap.getDisarmDifficulty(), 0, 999, 1));
			addRow("Disarm Difficulty (Traps)", disarm);
			
			recover = new JSpinner(new SpinnerNumberModel(trap.getRecoverDifficulty(), 0, 999, 1));
			addRow("Recover Difficulty (Traps)", recover);
			
			reflex = new JSpinner(new SpinnerNumberModel(trap.getReflexDifficulty(), 0, 999, 1));
			addRow("Reflex Difficulty", reflex);
		}
	}
	
	private class WeaponTypePanel extends SubEditorPanel {
		private JComboBox baseWeapon, weaponType, weaponHanded, weaponSize;
		private JCheckBox threatens;
		
		public WeaponTypePanel(JFrame parent) {
			super(parent);
			
			baseWeapon = new JComboBox();
			int i = 0;
			for (BaseWeapon base : Game.ruleset.getAllBaseWeaponTypes()) {
				baseWeapon.addItem(base.getName());
				
				if (base == item.getBaseWeapon()) baseWeapon.setSelectedIndex(i);
				
				i++;
			}
			addRow("Base Weapon", baseWeapon);
			
			weaponType = new JComboBox();
			i = 0;
			for (Item.WeaponType type : Item.WeaponType.values()) {
				weaponType.addItem(type.toString());
				
				if (type == item.getWeaponType()) weaponType.setSelectedIndex(i);
				
				i++;
			}
			addRow("Weapon Type", weaponType);
			
			weaponHanded = new JComboBox();
			i = 0;
			for (Item.WeaponHanded handed : Item.WeaponHanded.values()) {
				weaponHanded.addItem(handed.toString());
				
				if (handed == item.getWeaponHanded()) weaponHanded.setSelectedIndex(i);
				
				i++;
			}
			
			weaponSize = new JComboBox();
			i = 0;
			for (Size size : Size.values()) {
				weaponSize.addItem(size.toString());
				
				if (size == item.getWeaponSize()) weaponSize.setSelectedIndex(i);
				
				i++;
			}
			
			addRow("Weapon Handed", weaponHanded, new JLabel(" for size "), weaponSize);
			
			threatens = new JCheckBox("Threatens AoOs in Melee");
			threatens.setSelected(item.threatens());
			
			addRow(threatens);
		}
	}
	
	private class WeaponValuesPanel extends SubEditorPanel {
		private JSpinner attackCost;
		private JComboBox damageType;
		private JSpinner damageMin, damageMax;
		private JSpinner criticalRange;
		private JSpinner criticalMultiplier;
		
		// melee only widgets
		private JSpinner reachMin, reachMax;
		
		// ranged only widgets
		private JSpinner rangePenalty;
		private JSpinner maxRange;
		private JSpinner maxStrength;
		
		public WeaponValuesPanel(JFrame parent) {
			super(parent);
			
			attackCost = new JSpinner(new SpinnerNumberModel(item.getAttackCost(), 0, 10000, 100));
			addRow("Attack Cost", attackCost);
			
			damageType = new JComboBox();
			int i = 0;
			for (DamageType type : Game.ruleset.getAllDamageTypes()) {
				if (!type.isPhysical()) continue;
				if ( type.getName().equals(Game.ruleset.getString("PhysicalDamageType")) ) continue;
				
				damageType.addItem(type.getName());
				
				if (type == item.getDamageType()) damageType.setSelectedIndex(i);
				
				i++;
			}
			
			damageMin = new JSpinner(new SpinnerNumberModel(item.getDamageMin(), 0, 100, 1));
			damageMax = new JSpinner(new SpinnerNumberModel(item.getDamageMax(), 0, 100, 1));
			addRow("Damage", damageMin, new JLabel(" to "), damageMax, damageType);
			
			criticalRange = new JSpinner(new SpinnerNumberModel(item.getCriticalThreatRange(), 1, 100, 1));
			criticalMultiplier = new JSpinner(new SpinnerNumberModel(item.getCriticalMultiplier(), 1, 9, 1));
			addRow("Critical", criticalRange, new JLabel(" to 100 / x"), criticalMultiplier);
			
			if (item.getWeaponType() == Item.WeaponType.MELEE) {
				reachMin = new JSpinner(new SpinnerNumberModel(item.getWeaponReachMin(), 1, 9, 1));
				reachMax = new JSpinner(new SpinnerNumberModel(item.getWeaponReachMax(), 1, 9, 1));
				addRow("Reach", reachMin, new JLabel(" to "), reachMax);
			} else {
				rangePenalty = new JSpinner(new SpinnerNumberModel(item.getRangePenalty(), 0, 100, 1));
				addRow("Range Penalty", rangePenalty, new JLabel(" per 100 feet"));
				
				maxRange = new JSpinner(new SpinnerNumberModel(item.getMaximumRange(), 0, 1000, 1));
				addRow("Maximum Range", maxRange, new JLabel(" feet"));
				
				maxStrength = new JSpinner(new SpinnerNumberModel(item.getMaxStrengthBonus(), 0, 100, 1));
				addRow("Maximum Strength Bonus", maxStrength);
			}
		}
	}
	
	private class ArmorPanel extends SubEditorPanel {
		private JComboBox armorType;
		private JSpinner armorClass, armorPenalty, movementPenalty, shieldPenalty;
		private JCheckBox coversBeard;
		
		private ArmorPanel(JFrame parent) {
			super(parent);
			
			coversBeard = new JCheckBox("Covers Beard Icon");
			coversBeard.setSelected(item.coversBeardIcon());
			
			c.gridy = 0;
			addRow(coversBeard);
			
			armorType = new JComboBox();
			int i = 0;
			int initialValue = 0;
			for (ArmorType type : Game.ruleset.getAllArmorTypes()) {
				armorType.addItem(type.getName());
				
				if (type == item.getArmorType()) initialValue = i;
				
				i++;
			}
			armorType.setSelectedIndex(initialValue);
			addRow("Armor Type", armorType);
			
			armorClass = new JSpinner(new SpinnerNumberModel(item.getArmorClass(), 0, 100, 1));
			addRow("Armor Class", armorClass);
			
			armorPenalty = new JSpinner(new SpinnerNumberModel(item.getArmorPenalty(), 0, 100, 1));
			addRow("Armor Penalty", armorPenalty);
			
			movementPenalty = new JSpinner(new SpinnerNumberModel(item.getMovementPenalty(), 0, 100, 1));
			addRow("Movement Penalty", movementPenalty);
			
			if (item.getItemType() == Item.ItemType.SHIELD) {
				shieldPenalty = new JSpinner(new SpinnerNumberModel(item.getShieldAttackPenalty(), 0, 100, 1));
				addRow("Shield Penalty", shieldPenalty);
			}
		}
	}
	
	private class AmmoPanel extends JPanel {
		private JComboBox ammoType;
		
		private AmmoPanel() {
			ammoType = new JComboBox();
			int i = 0;
			for (Item.WeaponType weaponType : Item.WeaponType.values()) {
				ammoType.addItem(weaponType.toString());
				
				if (weaponType == item.getWeaponType()) ammoType.setSelectedIndex(i);
				
				i++;
			}
			
			add(ammoType);
		}
	}
	
	private class ScriptPanel extends SubEditorPanel {
		private JSpinner useAPCost;
		private JTextField useButtonText;
		private JComboBox convo, script;
		private JTextArea enchantments;
		
		public ScriptPanel(JFrame parent) {
			super(parent);
			
			useAPCost = new JSpinner(new SpinnerNumberModel(item.getUseAPCost(), 0, 10000, 100));
			addRow("Use AP Cost", useAPCost);
			
			useButtonText = new JTextField(item.getUseButtonText());
			addRowFilled("Use Button Text", useButtonText);
			
			convo = new JComboBox();
			addRowFilled("Conversation Script", convo);
			
			script = new JComboBox();
			addRowFilled("Script", script);
			
			addRow(new JLabel("Enchantments (One Per Line)"));
			
			enchantments = new JTextArea();
			for (Enchantment e : item.getEnchantments()) {
				enchantments.append(e.getScript());
				enchantments.append(SwingEditor.NewLine);
			}
			addRowFilled(enchantments);
		}
	}
	
	private class AppearancePanel extends JPanel {
		private ImageSelector openIcon, closedIcon;
		private ImageSelector icon;
		private ImageSelector subIcon;
		private ImageSelector projectileIcon;
		
		private AppearancePanel() {
			switch (item.getType()) {
			case CONTAINER:
				Container container = (Container)item;
				openIcon = new ImageSelector(parentFrame, SpriteManager.getResourceID(container.getOpenIcon()),
						EditorManager.getItemIconChoices());
				openIcon.setTitle("Open Icon");
				openIcon.setShowColorChooser(false);
				add(openIcon);
				
				closedIcon = new ImageSelector(parentFrame, SpriteManager.getResourceID(container.getClosedIcon()),
						EditorManager.getItemIconChoices());
				closedIcon.setTitle("Closed Icon");
				closedIcon.setShowColorChooser(false);
				add(closedIcon);
				
				break;
			case DOOR:
				Door door = (Door)item;
				openIcon = new ImageSelector(parentFrame, SpriteManager.getResourceID(door.getOpenIcon()),
						EditorManager.getDoorIconChoices());
				openIcon.setTitle("Open Icon");
				openIcon.setShowColorChooser(false);
				add(openIcon);
				
				closedIcon = new ImageSelector(parentFrame, SpriteManager.getResourceID(door.getClosedIcon()),
						EditorManager.getDoorIconChoices());
				closedIcon.setTitle("Closed Icon");
				closedIcon.setShowColorChooser(false);
				add(closedIcon);
				
				break;
			default:
				icon = new ImageSelector(parentFrame, SpriteManager.getResourceID(item.getIcon()),
						EditorManager.getItemIconChoices());
				icon.setTitle("Icon");
				icon.setDefaultColor(item.getIconColor());
				add(icon);
			}
			
			// add widgets based on item type
			if (item.getType() == Entity.Type.ITEM) {
				Map<String, BufferedImage> choices = EditorManager.getSubIconChoices(item.getItemType());
				
				if (choices != null) {
					subIcon = new ImageSelector(parentFrame, SpriteManager.getResourceID(item.getSubIcon()), choices);
					subIcon.setTitle("Sub Icon");
					subIcon.setDefaultColor(item.getSubIconColor());
					add(subIcon);
				}
				
				switch (item.getItemType()) {
				case AMMO:
					// add ammo widgets
					addProjectileIcon();
					break;
				case WEAPON:
					if (item.getWeaponType() == Item.WeaponType.THROWN)
						addProjectileIcon();
				}
			}
		}
		
		private void addProjectileIcon() {
			projectileIcon = new ImageSelector(parentFrame, SpriteManager.getResourceID(item.getProjectileIcon()),
					EditorManager.getProjectileIconChoices());
			projectileIcon.setTitle("Projectile Icon");
			projectileIcon.setDefaultColor(item.getProjectileIconColor());
			add(projectileIcon);
		}
	}
	
}
