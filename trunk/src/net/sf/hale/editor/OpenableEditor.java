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
import net.sf.hale.editor.reference.ReferenceComboBox;
import net.sf.hale.editor.widgets.IconSelectorPopup;
import net.sf.hale.editor.widgets.SpriteViewer;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Openable;
import net.sf.hale.resource.SpriteManager;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class OpenableEditor extends DialogLayout {
	private final ToggleButton lockedButton;
	private final ValueAdjusterInt lockDifficultyAdjuster;
	private final ToggleButton hasKeyButton;
	private final ComboBox<Item> keyBox;
	private final ToggleButton keyRequiredButton;
	private final ToggleButton removeKeyOnUnlock;
	
	private final Button setOpenIcon;
	private final SpriteViewer openIcon;
	
	private final Button setClosedIcon;
	private final SpriteViewer closedIcon;
	
	private Openable openable;

	private String iconSheet;
	private int iconSize;
	
	public OpenableEditor(String iconSheet, int iconSize) {
		this.iconSheet = iconSheet;
		this.iconSize = iconSize;
		
		this.setTheme("/editorlayout");
		
		lockedButton = new ToggleButton("Lock");
		lockedButton.addCallback(new Runnable() {
			@Override public void run() {
				openable.setLocked(lockedButton.isActive());
				setLockVisibility(lockedButton.isActive());
				if (!lockedButton.isActive()) {
					keyRequiredButton.setActive(false);
					openable.setKeyRequired(false);
				}
			}
		});
		
		lockDifficultyAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		lockDifficultyAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				openable.setLockDifficulty(lockDifficultyAdjuster.getValue());
			}
		});
		this.add(lockDifficultyAdjuster);
		
		hasKeyButton = new ToggleButton("Has key");
		hasKeyButton.addCallback(new Runnable() {
			@Override
			public void run() {
				if (hasKeyButton.isActive()) {
					keyBox.setVisible(true);
				} else {
					keyBox.setVisible(false);
					keyBox.setSelected(-1);
					openable.setKey(null);
				}
			}
		});
		
		keyBox = new ReferenceComboBox<Item>(Game.campaignEditor.getItemsModel());
		keyBox.setTheme("smallcombobox");
		keyBox.addCallback(new Runnable() {
			@Override
			public void run() {
				if (keyBox.getSelected() != -1) {
					openable.setKey(Game.campaignEditor.getItemsModel().getEntry(keyBox.getSelected()).getID());
				}
			}
		});
		
		keyRequiredButton = new ToggleButton("Key Required");
		keyRequiredButton.addCallback(new Runnable() {
			@Override public void run() {
				openable.setKeyRequired(keyRequiredButton.isActive());
			}
		});
		
		removeKeyOnUnlock = new ToggleButton("Remove Key on Unlock");
		removeKeyOnUnlock.addCallback(new Runnable() {
			@Override
			public void run() {
				openable.setRemoveKeyOnUnlock(removeKeyOnUnlock.isActive());
			}
		});
		
		setOpenIcon = new Button("Set Open Icon");
		setOpenIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(OpenableEditor.this,
						OpenableEditor.this.iconSheet, OpenableEditor.this.iconSize, false, 1);
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						openable.setOpenIcon(icon);
						openIcon.setSprite(SpriteManager.getSprite(icon));
					}
				});
				popup.openPopupCentered();
			}
		});
		
		openIcon = new SpriteViewer(100, 100, 1);
		openIcon.setIconOffset(false);
		openIcon.setSelected(true);
		
		setClosedIcon = new Button("Set Closed Icon");
		setClosedIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(OpenableEditor.this,
						OpenableEditor.this.iconSheet, OpenableEditor.this.iconSize, false, 1);
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						openable.setClosedIcon(icon);
						closedIcon.setSprite(SpriteManager.getSprite(icon));
					}
				});
				popup.openPopupCentered();
			}
		});
		
		closedIcon = new SpriteViewer(100, 100, 1);
		closedIcon.setIconOffset(false);
		closedIcon.setSelected(true);
		
		Group mainH = this.createParallelGroup();
		Group mainV = this.createSequentialGroup();
		
		mainH.addGroup(this.createSequentialGroup(lockedButton, lockDifficultyAdjuster));
		mainV.addGroup(this.createParallelGroup(lockedButton, lockDifficultyAdjuster));
		
		mainH.addGroup(this.createSequentialGroup(hasKeyButton, keyBox));
		mainV.addGroup(this.createParallelGroup(hasKeyButton, keyBox));
		
		mainH.addGroup(this.createSequentialGroup(keyRequiredButton, removeKeyOnUnlock));
		mainV.addGroup(this.createParallelGroup(keyRequiredButton, removeKeyOnUnlock));
		
		Group openV = createSequentialGroup();
		Group openH = createParallelGroup();
		
		openV.addWidgets(setOpenIcon, openIcon);
		openH.addWidgets(setOpenIcon, openIcon);
		
		Group closedV = createSequentialGroup();
		Group closedH = createParallelGroup();
		
		closedV.addWidgets(setClosedIcon, closedIcon);
		closedH.addWidgets(setClosedIcon, closedIcon);
		
		mainH.addGroup(createSequentialGroup(openH, closedH));
		mainV.addGroup(createParallelGroup(openV, closedV));
		
		this.setHorizontalGroup(mainH);
		this.setVerticalGroup(mainV);
	}
	
	public void getPropertiesFromItem(Item item) {
		if (item == null) return;
		
		switch (item.getType()) {
		case CONTAINER:
		case DOOR:
			openable = (Openable) item;
			break;
		default:
			openable = null;
		}
		
		if (openable != null) {
			openIcon.setSprite(SpriteManager.getSprite(openable.getOpenIcon()));
			closedIcon.setSprite(SpriteManager.getSprite(openable.getClosedIcon()));
			
			lockedButton.setActive(openable.isLocked());
			setLockVisibility(openable.isLocked());
			
			if (openable.isLocked()) {
				lockDifficultyAdjuster.setValue(openable.getLockDifficulty());
			}
			
			keyRequiredButton.setActive(openable.isKeyRequired());
			removeKeyOnUnlock.setActive(openable.removeKeyOnUnlock());
			
			String key = openable.getKey();
			if (key != null) {
				keyBox.setVisible(true);
				
				for (int i = 0; i < Game.campaignEditor.getItemsModel().getNumEntries(); i++) {
					Item keyItem = Game.campaignEditor.getItemsModel().getEntry(i);
					if (keyItem.getID().equals(key)) {
						keyBox.setSelected(i);
						break;
					}
				}
				
				hasKeyButton.setActive(true);
			} else {
				keyBox.setVisible(false);
				keyBox.setSelected(-1);
				hasKeyButton.setActive(false);
			}
		}
	}
	
	public void setLockVisibility(boolean visible) {
		lockDifficultyAdjuster.setVisible(visible);
		keyRequiredButton.setVisible(visible);
		keyBox.setVisible(visible);
		hasKeyButton.setVisible(visible);
		removeKeyOnUnlock.setVisible(visible);
	}
}
