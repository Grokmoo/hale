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
import net.sf.hale.Updateable;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.LootList;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class LootEditor extends DialogLayout implements Updateable {
	private LootList selectedLoot;
	
	private final ListBox<ItemList> allItemListsBox;
	private final Label allItemListsLabel;
	private final Button addItemList;
	
	private final List<Label> activeItemListLabels;
	private final List<ProbabilityAdjuster> activeItemListAdjusters;
	private final List<ModeSelectorButton> activeItemListModes;
	private final List<RemoverButton> activeItemListRemove;
	
	private final Label activeItemListsLabel;
	private final ScrollPane activeItemListsPane;
	private final Widget activeItemListsContent;
	
	private final Label activeItemListsItemList;
	private final Label activeItemListsProbability;
	
	private int defaultProbability;
	private LootList.ProbabilityMode defaultMode;
	
	public LootEditor() {
		this.setTheme("/editorlayout");
		
		allItemListsLabel = new Label("All Item Lists");
		
		allItemListsBox = new ReferenceListBox<ItemList>(Game.campaignEditor.getItemListsModel());
		allItemListsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				switch (reason) {
				case MOUSE_DOUBLE_CLICK: addSelectedItemList();
				}
			}
		});
		
		addItemList = new Button("Add");
		addItemList.addCallback(new Runnable() {
			@Override public void run() {
				addSelectedItemList();
			}
		});
		
		activeItemListsLabel = new Label("Active Item Lists");
		
		activeItemListsPane = new ScrollPane();
		activeItemListsPane.setTheme("lootscrollpane");
		
		activeItemListsContent = new Widget();
		activeItemListsContent.setTheme("");
		activeItemListsPane.setContent(activeItemListsContent);
		
		activeItemListsItemList = new Label("Name");
		
		activeItemListsProbability = new Label("Probability");
		
		activeItemListLabels = new ArrayList<Label>();
		activeItemListAdjusters = new ArrayList<ProbabilityAdjuster>();
		activeItemListRemove = new ArrayList<RemoverButton>();
		activeItemListModes = new ArrayList<ModeSelectorButton>();
		
		Group leftH = this.createParallelGroup(allItemListsLabel, allItemListsBox, addItemList);
		Group leftV = this.createSequentialGroup(allItemListsLabel, allItemListsBox, addItemList);
		
		Group rightH = this.createParallelGroup(activeItemListsLabel);
		Group rightV = this.createSequentialGroup(activeItemListsLabel);
		
		Group rightHeaderH = this.createSequentialGroup();
		rightHeaderH.addGap(30);
		rightHeaderH.addWidget(activeItemListsItemList);
		rightHeaderH.addGap(70);
		rightHeaderH.addWidget(activeItemListsProbability);
		
		Group rightHeaderV = this.createParallelGroup(activeItemListsItemList, activeItemListsProbability);
		
		rightH.addGroup(rightHeaderH);
		rightV.addGroup(rightHeaderV);
		
		rightH.addWidget(activeItemListsPane);
		rightV.addWidget(activeItemListsPane);
		
		this.setHorizontalGroup(this.createSequentialGroup(leftH, rightH));
		this.setVerticalGroup(this.createParallelGroup(leftV, rightV));
		
		this.defaultProbability = 50;
		this.defaultMode = LootList.ProbabilityMode.PER_LIST;
	}
	
	public void setDefaultMode(LootList.ProbabilityMode mode) {
		this.defaultMode = mode;
	}
	
	public void setDefaultProbability(int probability) {
		this.defaultProbability = probability;
	}
	
	public void addSelectedItemList() {
		int index = allItemListsBox.getSelected();
		
		if (index == -1) return;
		
		String selectedName = Game.campaignEditor.getItemListsModel().getEntry(index).getID();
		
		selectedLoot.add(selectedName, defaultProbability, defaultMode);
		
		update();
	}
	
	public void setLoot(LootList selectedLoot) {
		this.selectedLoot = selectedLoot;
		
		update();
	}
	
	@Override public void update() {
		activeItemListsContent.removeAllChildren();
		
		if (selectedLoot == null) return;
		
		for (int i = 0; i < selectedLoot.size(); i++) {
			LootList.Entry entry = selectedLoot.getEntry(i);
			
			RemoverButton remove = new RemoverButton(i);
			activeItemListRemove.add(remove);
			activeItemListsContent.add(remove);
			
			Label name = new Label(entry.itemListID);
			name.setTheme("/labelblack");
			name.setSize(50, 20);
			name.setPosition(25, i * 30);
			activeItemListLabels.add(name);
			activeItemListsContent.add(name);
			
			ProbabilityAdjuster adjuster = new ProbabilityAdjuster(i, entry.probability);
			activeItemListAdjusters.add(adjuster);
			activeItemListsContent.add(adjuster);
			
			ModeSelectorButton button = new ModeSelectorButton(i, entry.mode);
			activeItemListModes.add(button);
			activeItemListsContent.add(button);
		}
		
	}

	private class ModeSelectorButton extends ToggleButton implements Runnable {
		private final int index;
		private LootList.ProbabilityMode mode;
		
		private ModeSelectorButton(int index, LootList.ProbabilityMode mode) {
			super();
			
			this.index = index;
			this.mode = mode;
			this.setTheme("/togglebutton");
			this.setSize(50, 15);
			this.setPosition(185, index * 30);
			this.addCallback(this);
			
			setButtonState();
		}
		
		private void setButtonState() {
			this.setActive(false);
			
			switch(mode) {
			case PER_ITEM:
				this.setText("Per Item");
				break;
			case PER_LIST:
				this.setText("Per List");
				break;
			}
		}
		
		@Override
		public void run() {
			if (this.mode == LootList.ProbabilityMode.PER_ITEM) this.mode = LootList.ProbabilityMode.PER_LIST;
			else this.mode = LootList.ProbabilityMode.PER_ITEM;
			
			setButtonState();
			
			selectedLoot.setMode(index, this.mode);
		}
	}
	
	private class ProbabilityAdjuster extends ValueAdjusterInt implements Runnable {
		private final int index;
		
		private ProbabilityAdjuster(int index, int probability) {
			super(new SimpleIntegerModel(1, 100, probability));
			
			this.index = index;
			this.getModel().addCallback(this);
			this.setTheme("/valueadjuster");
			this.setSize(60, 20);
			this.setPosition(120, index * 30);
		}
		
		@Override
		public void run() {
			selectedLoot.setProbability(index, this.getValue());
		}
	}
	
	private class RemoverButton extends Button implements Runnable {
		private final int index;
		
		private RemoverButton(int index) {
			super("[x]");
			this.setTheme("/button");
			this.setSize(10, 15);
			this.index = index;
			this.setPosition(0, index * 30);
			this.addCallback(this);
		}
		
		@Override
		public void run() {
			selectedLoot.remove(index);
			
			update();
		}
	}
	
}
