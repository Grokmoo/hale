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
import java.util.Arrays;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.editor.widgets.ColorSelectorPopup;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.IconSelectorPopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.editor.widgets.SubItemEditorViewer;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.ItemQuality;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.rules.SubIcon;
import net.sf.hale.rules.Weight;
import net.sf.hale.util.ItemWriter;
import net.sf.hale.util.StringUtil;
import net.sf.hale.editor.widgets.SpriteViewer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class ItemEditor extends EditorWindow implements Updateable, PopupCallback {
	private final Updateable updateable;
	
	private final DialogLayout content;

	private final ItemListBox itemsBox;
	private final Button newItem;
	private final Button deleteItem;
	
	private Item selectedItem;
	
	private List<String> excludePostfixes;
	
	private final DialogLayout selectedItemContent;
	private final SubItemEditorViewer subEditorContent;
	
	private final Button selectedItemCopy;
	private final Button selectedItemSave;
	private final Label selectedItemNameLabel;
	private final EditField selectedItemName;
	private final Label selectedItemIconLabel;
	private final SpriteViewer selectedItemIcon;
	private final Button selectedItemSetIcon;
	private final Button selectedItemSetIconColor;
	private final Label selectedItemSubIconLabel;
	private final SpriteViewer selectedItemSubIcon;
	private final Button selectedItemSetSubIcon;
	private final Button selectedItemSetSubIconColor;
	private final Label selectedItemDescriptionLabel;
	private final EditField selectedItemDescription;
	private final Label selectedItemValueLabel;
	private final ValueAdjusterInt selectedItemValue;
	private final Label selectedItemWeightLabel;
	private final ValueAdjusterInt selectedItemStackValue;
	private final Label selectedItemStackValueLabel;
	private final ValueAdjusterInt selectedItemWeight;
	private final Label selectedItemItemTypeLabel;
	private final ComboBox<String> selectedItemType;
	private final SimpleChangableListModel<String> selectedItemTypeModel;
	
	private final ToggleButton selectedItemForceNoQuality;
	
	private final ColorSelectorPopup colorSelectorPopup;
	
	private final Label selectedItemQualityLabel;
	private final ComboBox<String> selectedItemQuality;
	private final SimpleChangableListModel<String> selectedItemQualityModel;
	
	private final AmmoEditor selectedItemAmmoProperties;
	private final ArmorEditor selectedItemArmorProperties;
	private final WeaponEditor selectedItemWeaponProperties;
	private final DoorEditor selectedItemDoorProperties;
	private final ContainerEditor selectedItemContainerProperties;
	private final TrapEditor selectedItemTrapProperties;
	
	private final ToggleButton selectedItemIngredient;
	private final ToggleButton selectedItemQuest;
	private final ToggleButton selectedItemUnremovable;
	private final Button selectedItemSetScriptButton;
	private final ScriptPropertiesPopup selectedItemScriptPropertiesPopup;
	
	public ItemEditor(Updateable update) {
		super("Item Editor");
		
		this.updateable = update;
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		this.add(content);
		
		selectedItemContent = new DialogLayout();
		selectedItemContent.setTheme("/editorlayout");
		
		itemsBox = new ItemListBox(Game.campaignEditor.getItemsModel(), "Select an item:");
		
		itemsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				saveSelected();
				
				update();
			}
		});
		
		newItem = new Button("New");
		newItem.addCallback(new Runnable() {
			@Override public void run() {
				NewItemPopup popup = new NewItemPopup(ItemEditor.this);
				popup.setCallback(ItemEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteItem = new Button("Delete");
		deleteItem.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedItem != null) {
					DeleteFilePopup popup = new DeleteFilePopup(ItemEditor.this,
							Game.campaignEditor.getPath() + "/items/" + selectedItem.getID() + ".txt", selectedItem);
					popup.setCallback(ItemEditor.this);
					popup.openPopupCentered();
				}
			}
		});
		
		selectedItemSave = new Button("Save");
		selectedItemSave.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedItem == null) return;
				
				saveSelected();
			}
		});
		
		selectedItemCopy = new Button("Copy");
		selectedItemCopy.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedItem == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(ItemEditor.this, "items", selectedItem.getID());
				popup.setCallback(ItemEditor.this);
				popup.openPopupCentered();
			}
		});
		
		Group bottomLeftH = content.createSequentialGroup(selectedItemSave, newItem, deleteItem, selectedItemCopy);
		Group bottomLeftV = content.createParallelGroup(selectedItemSave, newItem, deleteItem, selectedItemCopy);
		
		Group leftH = content.createParallelGroup(itemsBox);
		Group leftV = content.createSequentialGroup(itemsBox);
		leftH.addGroup(bottomLeftH);
		leftV.addGroup(bottomLeftV);
		
		Group topLevelH = content.createSequentialGroup(leftH);
		topLevelH.addWidget(selectedItemContent);
		
		Group topLevelV = content.createParallelGroup(leftV);
		topLevelV.addWidget(selectedItemContent);
		
		content.setHorizontalGroup(topLevelH);
		content.setVerticalGroup(topLevelV);
		
		selectedItemNameLabel = new Label("Name");
		
		selectedItemName = new EditField();
		selectedItemName.addCallback(new EditField.Callback() {
			@Override public void callback(int arg0) {
				selectedItem.setName(selectedItemName.getText());
			}
		});
		
		selectedItemIconLabel = new Label("Icon");
		
		selectedItemIcon = new SpriteViewer(Game.ICON_SIZE, Game.ICON_SIZE, 1);
		selectedItemIcon.setSelected(true);
		
		selectedItemSetIcon = new Button("Set");
		selectedItemSetIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(ItemEditor.this, "images/items", 45, true, 1);
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						if (selectedItem == null) return;

						selectedItem.setIcon(icon);
						selectedItemIcon.setSprite(SpriteManager.getSprite(icon));
						selectedItemIcon.setSpriteColor(selectedItem.getIconColor());
					}
				});
				popup.openPopupCentered();
			}
		});
		
		selectedItemSetIconColor = new Button("Color");
		selectedItemSetIconColor.addCallback(new Runnable() {
			@Override public void run() {
				colorSelectorPopup.clearCallbacks();
				colorSelectorPopup.setEntityIconColorOnAccept(selectedItem);
				colorSelectorPopup.setSpriteViewerSpriteColorOnAccept(selectedItemIcon);
				colorSelectorPopup.openPopupCentered();
			}
		});
		
		selectedItemSubIconLabel = new Label("Sub Icon");
		
		selectedItemSubIcon = new SpriteViewer(Game.ICON_SIZE, Game.ICON_SIZE, 1);
		selectedItemSubIcon.setSelected(true);
		
		selectedItemSetSubIcon = new Button("Set");
		selectedItemSetSubIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(ItemEditor.this, "images/subIcons", 45, true, 1, excludePostfixes);
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						if (selectedItem == null) return;

						selectedItem.setSubIcon(icon);
						selectedItemSubIcon.setSprite(SpriteManager.getSprite(icon));
						selectedItemSubIcon.setSpriteColor(selectedItem.getSubIconColor());
					}
				});
				popup.openPopupCentered();
			}
		});
		
		selectedItemSetSubIconColor = new Button("Color");
		selectedItemSetSubIconColor.addCallback(new Runnable() {
			@Override public void run() {
				colorSelectorPopup.clearCallbacks();
				colorSelectorPopup.setSubIconColorOnAccept(selectedItem);
				colorSelectorPopup.setSpriteViewerSpriteColorOnAccept(selectedItemSubIcon);
				colorSelectorPopup.openPopupCentered();
			}
		});
		
		colorSelectorPopup = new ColorSelectorPopup(this);
		
		excludePostfixes = new ArrayList<String>();
		excludePostfixes.add(Ruleset.Gender.Female.toString() + ".png");
		excludePostfixes.add(Ruleset.Gender.Male.toString() + ".png");
		excludePostfixes.add(SubIcon.Type.OffHandWeapon.toString() + ".png");
		for (Race race : Game.ruleset.getAllRaces()) {
			if (race.isPlayerSelectable()) excludePostfixes.add(race.getName() + ".png");
		}
		
		selectedItemDescriptionLabel = new Label("Description");
		
		selectedItemDescription = new EditField();
		selectedItemDescription.addCallback(new EditField.Callback() {
			@Override public void callback(int arg0) {
				selectedItem.setDescription(selectedItemDescription.getText());
			}
		});
		
		selectedItemValueLabel = new Label("Value in CP");
		
		selectedItemValue = new ValueAdjusterInt(new SimpleIntegerModel(0, 1000000, 0));
		selectedItemValue.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setValue(new Currency(selectedItemValue.getValue()));
			}
		});
		
		selectedItemStackValueLabel = new Label("per stack size");
		
		selectedItemStackValue = new ValueAdjusterInt(new SimpleIntegerModel(1, 1000, 0));
		selectedItemStackValue.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setValueStackSize(selectedItemStackValue.getValue());
			}
		});
		
		selectedItemWeightLabel = new Label("Weight in grams");
		
		selectedItemWeight = new ValueAdjusterInt(new SimpleIntegerModel(0, 1000000, 0));
		selectedItemWeight.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setWeight(new Weight(selectedItemWeight.getValue()));
			}
		});
		
		selectedItemItemTypeLabel = new Label("Item Type");
		
		selectedItemTypeModel = new SimpleChangableListModel<String>();
		selectedItemType = new ComboBox<String>(selectedItemTypeModel);
		selectedItemType.addCallback(new Runnable() {
			@Override public void run() {
				int index = selectedItemType.getSelected();
				String type = selectedItemTypeModel.getEntry(index).toUpperCase();
				selectedItem.setItemType(Item.ItemType.valueOf(type));
				update();
			}
		});
		
		selectedItemForceNoQuality = new ToggleButton("Force No Quality");
		selectedItemForceNoQuality.setTheme("/radiobutton");
		selectedItemForceNoQuality.addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setForceNoQuality(selectedItemForceNoQuality.isActive());
			}
		});
		
		List<Item.ItemType> types = Arrays.asList(Item.ItemType.values());
		for (Item.ItemType type : types) {
			selectedItemTypeModel.addElement(StringUtil.upperCaseToWord(type.toString()));
		}
		
		selectedItemQualityLabel = new Label("Quality");
		
		selectedItemQualityModel = new SimpleChangableListModel<String>();
		selectedItemQuality = new ComboBox<String>(selectedItemQualityModel);
		selectedItemQuality.setTheme("mediumcombobox");
		selectedItemQuality.addCallback(new Runnable() {
			@Override public void run() {
				String quality = selectedItemQualityModel.getEntry(selectedItemQuality.getSelected());
				selectedItem.setQuality(Game.ruleset.getItemQuality(quality));
			}
		});
		
		for (ItemQuality quality : Game.ruleset.getAllItemQualities()) {
			selectedItemQualityModel.addElement(quality.getName());
		}
		
		selectedItemIngredient = new ToggleButton("Ingredient");
		selectedItemIngredient.addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setIngredient(selectedItemIngredient.isActive());
			}
		});
		
		selectedItemQuest = new ToggleButton("Quest");
		selectedItemQuest.addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setQuestItem(selectedItemQuest.isActive());
			}
		});
		
		selectedItemUnremovable = new ToggleButton("Cursed");
		selectedItemUnremovable.addCallback(new Runnable() {
			@Override public void run() {
				selectedItem.setCursed(selectedItemUnremovable.isActive());
			}
		});
		
		selectedItemSetScriptButton = new Button("Set Script");
		selectedItemSetScriptButton.addCallback(new Runnable() {
			@Override public void run() {
				selectedItemScriptPropertiesPopup.openPopupCentered(selectedItem);
			}
		});
		
		selectedItemScriptPropertiesPopup = new ScriptPropertiesPopup(this);
		
		subEditorContent = new SubItemEditorViewer();
		
		selectedItemAmmoProperties = new AmmoEditor();
		
		selectedItemArmorProperties = new ArmorEditor();
		
		selectedItemWeaponProperties = new WeaponEditor();
		
		selectedItemDoorProperties = new DoorEditor();
		
		selectedItemContainerProperties = new ContainerEditor();
		
		selectedItemTrapProperties = new TrapEditor();
		
		Group mainRow1H = selectedItemContent.createSequentialGroup(selectedItemNameLabel, selectedItemName);
		mainRow1H.addGap(50);
		mainRow1H.addWidget(selectedItemSetScriptButton);
		
		Group mainRow1V = selectedItemContent.createParallelGroup(selectedItemNameLabel, selectedItemName, selectedItemSetScriptButton);
		
		Group mainRow2H = selectedItemContent.createSequentialGroup(selectedItemIconLabel, selectedItemIcon, selectedItemSetIcon,
				selectedItemSetIconColor, selectedItemSubIconLabel, selectedItemSubIcon, selectedItemSetSubIcon,
				selectedItemSetSubIconColor);
		Group mainRow2V = selectedItemContent.createParallelGroup(selectedItemIconLabel, selectedItemIcon, selectedItemSetIcon,
				selectedItemSetIconColor, selectedItemSubIconLabel, selectedItemSubIcon, selectedItemSetSubIcon,
				selectedItemSetSubIconColor);
		
		Group mainRow3H = selectedItemContent.createSequentialGroup(selectedItemDescriptionLabel, selectedItemDescription);
		Group mainRow3V = selectedItemContent.createParallelGroup(selectedItemDescriptionLabel, selectedItemDescription);
		
		Group mainRow4H = selectedItemContent.createSequentialGroup(selectedItemValueLabel, selectedItemValue,
				selectedItemStackValueLabel, selectedItemStackValue);
		Group mainRow4V = selectedItemContent.createParallelGroup(selectedItemValueLabel, selectedItemValue,
				selectedItemStackValueLabel, selectedItemStackValue);
		
		Group mainRow5H = selectedItemContent.createSequentialGroup(selectedItemWeightLabel, selectedItemWeight);
		Group mainRow5V = selectedItemContent.createParallelGroup(selectedItemWeightLabel, selectedItemWeight);
		
		Group mainRow6H = selectedItemContent.createSequentialGroup(selectedItemQualityLabel, selectedItemQuality);
		Group mainRow6V = selectedItemContent.createParallelGroup(selectedItemQualityLabel, selectedItemQuality);
		
		Group mainRow7H = selectedItemContent.createSequentialGroup(selectedItemIngredient, selectedItemQuest,
				selectedItemUnremovable, selectedItemForceNoQuality);
		Group mainRow7V = selectedItemContent.createParallelGroup(selectedItemIngredient, selectedItemQuest,
				selectedItemUnremovable, selectedItemForceNoQuality);
		
		Group mainRow8H = selectedItemContent.createSequentialGroup(selectedItemItemTypeLabel, selectedItemType);
		Group mainRow8V = selectedItemContent.createParallelGroup(selectedItemItemTypeLabel, selectedItemType);
		
		Group mainH = selectedItemContent.createParallelGroup(mainRow1H, mainRow2H, mainRow3H, mainRow4H, mainRow5H,
				mainRow6H, mainRow7H, mainRow8H);
		Group mainV = selectedItemContent.createSequentialGroup(mainRow1V, mainRow2V, mainRow3V, mainRow4V, mainRow5V,
				mainRow6V, mainRow7V, mainRow8V);
		mainH.addWidget(subEditorContent);
		mainV.addWidget(subEditorContent);
		
		selectedItemContent.setHorizontalGroup(mainH);
		selectedItemContent.setVerticalGroup(mainV);
	}
	
	public void openToItem(Item item) {
		int index = -1;
		
		for (int i = 0; i < Game.campaignEditor.getItemsModel().getNumEntries(); i++) {
			if (Game.campaignEditor.getItemsModel().getEntry(i).getID().equals(item.getID())) {
				index = i;
				break;
			}
		}
		
		if (!isVisible()) setVisible(true);
		
		if (index == -1) return;
		
		itemsBox.setSelectedFilter("All");
		itemsBox.setSelected(index);
		itemsBox.scrollToSelected();
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			itemsBox.resetView();
			update();
		}
	}
	
	@Override public void saveSelected() {
		if (selectedItem == null) return;
		
		switch (selectedItem.getType()) {
		case CONTAINER:
			ItemWriter.saveItemAsContainer(selectedItem);
			break;
		case DOOR:
			ItemWriter.saveItemAsDoor(selectedItem);
			break;
		case TRAP:
			ItemWriter.saveItemAsTrap(selectedItem);
			break;
		default:
			ItemWriter.saveItem(selectedItem);
		}
		
		Game.entityManager.removeEntity(selectedItem.getID());
		
		Game.campaignEditor.updateStatusText("Item " + selectedItem.getID() + " saved.");
	}
	
	@Override public void deleteComplete() {
		this.selectedItem = null;
		
		String currentFilter = ItemEditor.this.itemsBox.getSelectedFilter();
		updateable.update();
		ItemEditor.this.itemsBox.setSelectedFilter(currentFilter);
	}
	
	@Override public void copyComplete() {
		String currentFilter = ItemEditor.this.itemsBox.getSelectedFilter();
		updateable.update();
		ItemEditor.this.itemsBox.setSelectedFilter(currentFilter);
	}
	
	@Override public void newComplete() {
		String currentFilter = ItemEditor.this.itemsBox.getSelectedFilter();
		updateable.update();
		ItemEditor.this.itemsBox.setSelectedFilter(currentFilter);
	}
	
	@Override public void update() {
		int index = itemsBox.getSelected();
		if (index == -1) {
			selectedItem = null;
			selectedItemContent.setVisible(false);
		} else {
			selectedItemContent.setVisible(true);
			
			selectedItem = Game.campaignEditor.getItemsModel().getEntry(index);
			
			selectedItemForceNoQuality.setActive(selectedItem.isForceNoQuality());
			
			if (selectedItem.getName() != null) selectedItemName.setText(selectedItem.getName());
			else selectedItemName.setText("");
			
			if (selectedItem.getType() == Entity.Type.DOOR) {
				selectedItemIcon.setSprite(null);
				selectedItemSubIcon.setSprite(null);
				selectedItemSetIcon.setEnabled(false);
				selectedItemSetSubIcon.setEnabled(false);
			} else {
				
				if (selectedItem.getIcon() != null) {
					selectedItemIcon.setSprite(SpriteManager.getSprite(selectedItem.getIcon()));
					selectedItemIcon.setSpriteColor(selectedItem.getIconColor());
				} else {
					selectedItemIcon.setSprite(null);
					selectedItemIcon.setSpriteColor(null);
				}
				selectedItemSetIcon.setEnabled(true);
				
				if (selectedItem.getSubIcon() != null) {
					selectedItemSubIcon.setSprite(SpriteManager.getSprite(selectedItem.getSubIcon()));
					selectedItemSubIcon.setSpriteColor(selectedItem.getSubIconColor());
				} else {
					selectedItemSubIcon.setSprite(null);
					selectedItemSubIcon.setSpriteColor(null);
				}
				selectedItemSetSubIcon.setEnabled(true);
			}
			
			if (selectedItem.getDescription() != null) selectedItemDescription.setText(selectedItem.getDescription());
			else selectedItemDescription.setText("");
			
			selectedItemValue.setValue(selectedItem.getValue().getValue());
			selectedItemStackValue.setValue(selectedItem.getValueStackSize());
			selectedItemWeight.setValue(selectedItem.getWeightGrams());
			
			int typeIndex = selectedItemTypeModel.findElement(StringUtil.upperCaseToWord(selectedItem.getItemType().toString()));
			selectedItemType.setSelected(typeIndex);
			
			String quality = selectedItem.getQuality().getName();
			selectedItemQuality.setSelected(selectedItemQualityModel.findElement(quality));
			
			subEditorContent.setActiveEditor(null);
			
			selectedItemUnremovable.setActive(selectedItem.isCursed());
			selectedItemQuest.setActive(selectedItem.isQuestItem());
			selectedItemIngredient.setActive(selectedItem.isIngredient());
			
			switch (selectedItem.getType()) {
			case CONTAINER:
				subEditorContent.setActiveEditor(selectedItemContainerProperties);
				break;
			case DOOR:
				subEditorContent.setActiveEditor(selectedItemDoorProperties);
				break;
			case TRAP:
				subEditorContent.setActiveEditor(selectedItemTrapProperties);
			default:
				switch (selectedItem.getItemType()) {
				case AMMO:
					subEditorContent.setActiveEditor(selectedItemAmmoProperties);
					break;
				case WEAPON:
					subEditorContent.setActiveEditor(selectedItemWeaponProperties);
					break;
				case ARMOR:
				case SHIELD:
				case GLOVES:
				case HELMET:
				case BOOTS:
					subEditorContent.setActiveEditor(selectedItemArmorProperties);
					break;
				}
			}
			
			subEditorContent.getPropertiesFromItem(selectedItem);
		}
	}
}
