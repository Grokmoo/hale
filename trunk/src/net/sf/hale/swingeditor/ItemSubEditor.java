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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.hale.Game;
import net.sf.hale.entity.Enchantment;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;

/**
 * A sub editor for editing items
 * @author Jared
 *
 */

public class ItemSubEditor extends JPanel {
	private GridBagConstraints c;
	
	/**
	 * Creates a new SubEditor for the specified item
	 * @param item the item being edited
	 */
	
	protected ItemSubEditor(Item item) {
		super(new GridBagLayout());
		
		c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.insets = new Insets(2, 5, 2, 5);
		
		// add ID label
		JLabel id = new JLabel(item.getID());
		addRow("ID", id);
		
		// add name field and editor
		JTextField nameField = new JTextField(item.getName());
		addRow("Name", nameField);
		
		// add description field and editor
		JTextArea descriptionField = new JTextArea(item.getDescription());
		descriptionField.setLineWrap(true);
		addRow("Description", descriptionField);
		
		// add value field
		JSpinner value = new JSpinner(new SpinnerNumberModel(item.getValue().getTotalValueInCP(), 0, 100000, 1));
		JSpinner stackSize = new JSpinner(new SpinnerNumberModel(item.getValueStackSize(), 1, 1000, 1));
		addRowElements("Value in CP", value, new JLabel("for Stack Size"), stackSize);
		
		// add weight field
		JSpinner weight = new JSpinner(new SpinnerNumberModel(item.getWeightGrams(), 0, 100000, 100));
		addRow("Weight in Grams", weight, false);
		
		// add quality field
		JComboBox quality = new JComboBox();
		int initialValue = 0;
		for (int i = 0; i < Game.ruleset.getNumItemQualities(); i++) {
			quality.addItem(Game.ruleset.getItemQuality(i).getName());
			
			if (item.getQuality() == Game.ruleset.getItemQuality(i))
				initialValue = i;
		}
		quality.setSelectedIndex(initialValue);
		addRow("Quality", quality, false);
		
		// add type field
		JComboBox type = new JComboBox();
		initialValue = 0;
		int i = 0;
		for (Item.ItemType itemType : Item.ItemType.values()) {
			type.addItem(itemType.toString());
			
			if (item.getItemType() == itemType)
				initialValue = i;
			
			i++;
		}
		type.setSelectedIndex(initialValue);
		addRow("Item Type", type, false);
		
		// add toggle options
		JCheckBox ingredient = new JCheckBox("Ingredient", item.isIngredient());
		JCheckBox quest = new JCheckBox("Quest", item.isQuestItem());
		JCheckBox cursed = new JCheckBox("Cursed", item.isCursed());
		JCheckBox noQuality = new JCheckBox("Force No Quality", item.isForceNoQuality());
		addRowElements(null, ingredient, quest, cursed, noQuality);
		
		// add tabbed pane
		JTabbedPane tabs = new JTabbedPane();
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy++;
		c.gridx = 0;
		add(tabs, c);
		
		// add appearance tab
		JPanel appearanceTab = new JPanel();
		tabs.addTab("Appearance", appearanceTab);
		
		ImageSelector icon = new ImageSelector(SpriteManager.getResourceID(item.getIcon()), EditorManager.getItemIconChoices());
		icon.setTitle("Icon");
		icon.setDefaultColor(item.getIconColor());
		appearanceTab.add(icon);
		
		// add script tab
		JPanel scriptTab = new JPanel(new GridBagLayout());
		tabs.addTab("Script", scriptTab);
		
		c.gridy = 0;
		JSpinner useAPCost = new JSpinner(new SpinnerNumberModel(item.getUseAPCost(), 0, 10000, 100));
		addRow("Use AP Cost", useAPCost, scriptTab, false);
		
		JTextField useButtonText = new JTextField(item.getUseButtonText());
		addRow("Use Button Text", useButtonText, scriptTab, true);
		
		JComboBox convo = new JComboBox();
		addRow("Conversation Script", convo, scriptTab, true);
		
		JComboBox script = new JComboBox();
		addRow("Script", script, scriptTab, true);
		
		addRow(null, new JLabel("Enchantments (One Per Line)"), scriptTab, false);
		
		JTextArea enchantments = new JTextArea();
		for (Enchantment e : item.getEnchantments()) {
			enchantments.append(e.getScript());
			enchantments.append(SwingEditor.NewLine);
		}
		addRow(null, enchantments, scriptTab, true);
	}
	
	private void addRow(String name, JComponent component, JComponent parent, boolean fill) {
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		
		if (name != null) {
			c.gridx = GridBagConstraints.RELATIVE;
			
			JLabel label = new JLabel(name);
			label.setLabelFor(component);
		
			c.weightx = 0.0;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.EAST;
			parent.add(label, c);
		} else {
			c.gridx = 1;
		}
		
		c.weightx = 0.5;
		c.gridwidth = 3;
		if (fill) c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		parent.add(component, c);
	}
	
	private void addRow(String name, JComponent component, boolean fill) {
		addRow(name, component, this, fill);
	}
	
	private void addRow(String name, JComponent component) {
		addRow(name, component, true);
	}
	
	private void addRowElements(String name, JComponent... components) {
		JPanel pane = new JPanel();
		for (JComponent component : components) {
			pane.add(component);
		}
		
		addRow(name, pane, false);
	}
}
