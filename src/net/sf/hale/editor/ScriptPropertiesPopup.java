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
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.editor.reference.ReferenceComboBox;
import net.sf.hale.editor.reference.Script;
import net.sf.hale.entity.Enchantment;
import net.sf.hale.entity.Item;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;

public class ScriptPropertiesPopup extends PopupWindow {
	private final DialogLayout content;
	
	private final Label title;
	private final Button accept;
	private final Button cancel;
	
	private final Label enchantmentLabel;
	private final EnchantmentListPane enchantments;
	private final Button addEnchantmentButton;
	
	private final Label onActivateAPCostLabel;
	private final ValueAdjusterInt onActivateAPCostAdjuster;
	private final Label onActivateButtonTextLabel;
	private final EditField onActivateButtonTextField;
	
	private final Label conversationLabel;
	private final ComboBox<Script> conversationBox;
	
	private final Label scriptLabel;
	private final ComboBox<Script> scriptBox;
	
	private Item item;
	
	public ScriptPropertiesPopup(Widget parent) {
		super(parent);
		
		this.setTheme("");
		this.setCloseOnClickedOutside(false);
		this.setSize(340, 420);
		this.setPosition(100, 100);
		
		content = new DialogLayout();
		content.setTheme("/gamepopup");
		this.add(content);
		
		title = new Label("Script Properties");
		title.setTheme("/titlelabel");
		
		enchantmentLabel = new Label("Enchantments");
		
		enchantments = new EnchantmentListPane();
		
		addEnchantmentButton = new Button("Add");
		addEnchantmentButton.addCallback(new Runnable() {
			@Override public void run() {
				enchantments.add("");
			}
		});
		
		onActivateAPCostLabel = new Label("Use AP Cost");
		
		onActivateAPCostAdjuster = new ValueAdjusterInt();
		onActivateAPCostAdjuster.setMinMaxValue(0, 10000);
		
		onActivateButtonTextLabel = new Label("Use Button Text");
		
		onActivateButtonTextField = new EditField();
		
		scriptLabel = new Label("Script");
		
		scriptBox = new ReferenceComboBox<Script>(Game.campaignEditor.getItemScripts());
		scriptBox.setTheme("largecombobox");
		
		Button removeScript = new Button("[x]");
		removeScript.setTooltipContent("Clear the script for this item");
		removeScript.addCallback(new Runnable() {
			@Override public void run() {
				scriptBox.setSelected(-1);
			}
		});
		
		conversationLabel = new Label("Conversation");
		
		conversationBox = new ReferenceComboBox<Script>(Game.campaignEditor.getConversationScripts());
		conversationBox.setTheme("largecombobox");
		
		accept = new Button("Accept");
		accept.addCallback(new Runnable() {
			@Override public void run() {
				setItemScriptProperties();
				closePopup();
			}
		});
		
		cancel = new Button("Cancel");
		cancel.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
			}
		});
		
		Group mainH = content.createParallelGroup();
		Group mainV = content.createSequentialGroup();
		
		Group row1H = content.createSequentialGroup();
		Group row1V = content.createSequentialGroup(title);
		row1H.addGap(180);
		row1H.addWidget(title);
		
		mainH.addGroup(row1H);
		mainV.addGroup(row1V);
		
		mainV.addGap(20);
		
		Group enchantTitleH = content.createSequentialGroup(enchantmentLabel);
		enchantTitleH.addGap(50);
		enchantTitleH.addWidget(addEnchantmentButton);
		
		Group enchantTitleV = content.createParallelGroup(enchantmentLabel, addEnchantmentButton);
		
		mainH.addGroup(enchantTitleH);
		mainV.addGroup(enchantTitleV);
		
		mainV.addWidget(enchantments);
		mainH.addWidget(enchantments);
		
		mainV.addGap(30);
		
		mainH.addGroup(content.createSequentialGroup(onActivateAPCostLabel, onActivateAPCostAdjuster));
		mainV.addGroup(content.createParallelGroup(onActivateAPCostLabel, onActivateAPCostAdjuster));
		
		mainH.addGroup(content.createSequentialGroup(onActivateButtonTextLabel, onActivateButtonTextField));
		mainV.addGroup(content.createParallelGroup(onActivateButtonTextLabel, onActivateButtonTextField));
		
		mainH.addGroup(content.createSequentialGroup(conversationLabel, conversationBox));
		mainV.addGroup(content.createParallelGroup(conversationLabel, conversationBox));
		
		mainH.addGroup(content.createSequentialGroup(scriptLabel, scriptBox, removeScript));
		mainV.addGroup(content.createParallelGroup(scriptLabel, scriptBox, removeScript));
		
		mainV.addGap(10);
		
		Group bottomH = content.createSequentialGroup();
		bottomH.addGap(180);
		bottomH.addWidgets(accept, cancel);
		Group bottomV = content.createParallelGroup(accept, cancel);
		
		mainH.addGroup(bottomH);
		mainV.addGroup(bottomV);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	public void setItemScriptProperties() {
		String script = null;
		int index = scriptBox.getSelected();
		if (index != -1) script = Game.campaignEditor.getItemScripts().getEntry(index).getID();
		
		item.setScript(script);
		item.setUseAPCost(onActivateAPCostAdjuster.getValue());
		item.setUseButtonText(onActivateButtonTextField.getText());
		
		List<String> scripts = new ArrayList<String>();
		for (int i = 0; i < enchantments.size(); i++) {
			scripts.add(enchantments.get(i));
		}
		
		item.setEnchantments(scripts);
		
		index = conversationBox.getSelected();
		if (index != -1) {
			String convo = Game.campaignEditor.getConversationScripts().getEntry(index).getID();
			item.setConversationScript(convo);
		}
		
	}
	
	public void openPopupCentered(Item item) {
		this.item = item;
		
		if (item == null) return;
		
		enchantments.clear();
		for (Enchantment enchantment : item.getEnchantments()) {
			enchantments.add(enchantment.getScript());
		}
		
		onActivateAPCostAdjuster.setValue(item.getUseAPCost());
		
		if (item.getUseButtonText() != null)
			onActivateButtonTextField.setText(item.getUseButtonText());
		else
			onActivateButtonTextField.setText("");
		
		scriptBox.setSelected(-1);
		if (item.hasScript()) {
			String script = item.getScript().getScriptLocation();
			
			for (int i = 0; i < Game.campaignEditor.getItemScripts().getNumEntries(); i++) {
				if (Game.campaignEditor.getItemScripts().getEntry(i).equals(script)) {
					scriptBox.setSelected(i);
					break;
				}
			}
		}
		
		String convo = item.getConversationScript();
		conversationBox.setSelected(-1);
		for (int i = 0; i < Game.campaignEditor.getConversationScripts().getNumEntries(); i++) {
			if (Game.campaignEditor.getConversationScripts().getEntry(i).equals(convo)) {
				conversationBox.setSelected(i);
				break;
			}
		}
		
		super.openPopupCentered();
	}
	
	private class EnchantmentListPane extends Widget {
		private List<EnchantmentField> fields;
		private int width;
		
		private EnchantmentListPane() {
			setTheme("");
			
			fields = new ArrayList<EnchantmentField>();
		}
		
		@Override protected void afterAddToGUI(GUI gui) {
			width = getParent().getInnerWidth();
		}
		
		private void add(String enchantment) {
			EnchantmentField field = new EnchantmentField(enchantment);
			fields.add(field);
			
			this.add(field);
		}
		
		private void remove(EnchantmentField field) {
			fields.remove(field);
			this.removeChild(field);
		}
		
		private void clear() {
			this.removeAllChildren();
			fields.clear();
		}
		
		@Override protected void layout() {
			super.layout();
			
			int lastY = getInnerY();
			for (EnchantmentField field : fields) {
				field.setSize(Math.max(field.getPreferredWidth(), getParent().getInnerWidth()), field.getPreferredHeight());
				field.setPosition(getInnerX(), lastY);
				
				lastY = field.getBottom();
			}
		}
		
		@Override public int getMinWidth() { return width; }
		@Override public int getMinHeight() { return 200; }
		
		private String get(int index) {
			return fields.get(index).getText();
		}
		
		private int size() {
			return fields.size();
		}
	}
	
	private class EnchantmentField extends DialogLayout implements Runnable {
		private EditField field;
		private Button remove;
		
		private EnchantmentField(String enchantment) {
			setTheme("");
			field = new EditField();
			field.setText(enchantment);
			
			remove = new Button("Remove");
			remove.addCallback(this);
			
			setHorizontalGroup(createSequentialGroup(field, remove));
			setVerticalGroup(createParallelGroup(field, remove));
		}
		
		@Override public void run() {
			enchantments.remove(this);
		}
		
		private String getText() { return field.getText(); }
	}
}
