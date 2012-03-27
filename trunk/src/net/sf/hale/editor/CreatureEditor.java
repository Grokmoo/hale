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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.bonus.Stat;
import net.sf.hale.editor.reference.ReferenceComboBox;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.reference.Script;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.NewFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.entity.Creature;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Role;
import net.sf.hale.rules.RoleSet;
import net.sf.hale.util.CreatureWriter;
import net.sf.hale.widgets.ExpandableTabbedPane;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class CreatureEditor extends EditorWindow implements Updateable, PopupCallback {
	private final Updateable updateable;
	
	private final DialogLayout content;
	
	private Creature selectedCreature;
	
	private final Label creatureLabel;
	private final ListBox<Creature> creaturesBox;
	private final Button newCreature;
	private final Button deleteCreature;
	
	private final DialogLayout selectedCreatureContent;
	
	private final Button selectedCreatureCopy;
	private final Button selectedCreatureSave;
	
	private final Label selectedCreatureNameLabel;
	private final EditField selectedCreatureNameField;
	
	private final Label selectedCreatureRaceLabel;
	private final ComboBox<String> selectedCreatureRaceBox;
	private final SimpleChangableListModel<String> selectedCreatureRaceModel;
	
	private final Label selectedCreatureAttributesLabel;
	private final List<AttributeAdjuster> selectedCreatureAttributes;
	
	private final Label selectedCreatureRoleLabel;
	private final ComboBox<String> selectedCreatureRoleBox;
	private final SimpleChangableListModel<String> selectedCreatureRoleModel;
	private final Label selectedCreatureLevelLabel;
	private final ValueAdjusterInt selectedCreatureLevelAdjuster;
	
	private final Label selectedCreatureAIScriptLabel;
	private final ComboBox<Script> selectedCreatureAIScriptBox;
	
	private final Label selectedCreatureConversationLabel;
	private final ComboBox<Script> selectedCreatureConversationBox;
	
	private final Label selectedCreatureCurrencyRewardLabel, selectedCreatureCurrencyRewardLabel2;
	private final ValueAdjusterInt selectedCreatureMinCurrencyReward, selectedCreatureMaxCurrencyReward;
	
	private final ToggleButton selectedCreatureImmortal;
	
	private final TabbedPane secondaryEditors;
	
	private final InventoryEditor selectedCreatureInventoryEditor;
	private final LootEditor selectedCreatureLootEditor;
	private final SkillEditor selectedCreatureSkillEditor;
	private final AbilityEditor selectedCreatureAbilityEditor;
	private final CosmeticEditor selectedCreatureCosmeticEditor;
	private final CreatureStatsViewer selectedCreatureStatsViewer;
	
	private final SetCreatureRoleCallback setCreatureRoleCallback;
	
	public CreatureEditor(Updateable updateable) {
		super("Creature Editor");
		this.updateable = updateable;
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		this.add(content);
		
		selectedCreatureContent = new DialogLayout();
		selectedCreatureContent.setTheme("/editorlayout");
		
		creatureLabel = new Label("Select a creature:");
		
		creaturesBox = new ReferenceListBox<Creature>(Game.campaignEditor.getCreaturesModel());
		creaturesBox.setTheme("listboxnoexpand");
		
		creaturesBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				saveSelected();
				
				update();
			}
		});
		
		newCreature = new Button("New");
		newCreature.addCallback(new Runnable() {
			@Override public void run() {
				NewCreaturePopup popup = new NewCreaturePopup(CreatureEditor.this);
				popup.setCallback(CreatureEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteCreature = new Button("Delete");
		deleteCreature.addCallback(new Runnable() {
			@Override
			public void run() {
				if (selectedCreature == null) return;
				
				DeleteFilePopup popup = new DeleteFilePopup(CreatureEditor.this,
						Game.campaignEditor.getPath() + "/creatures/" + selectedCreature.getID() + ".txt", selectedCreature);
				popup.setCallback(CreatureEditor.this);
				popup.openPopupCentered();
			}
		});
		
		selectedCreatureSave = new Button("Save");
		selectedCreatureSave.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedCreature == null) return;
				
				saveSelected();
			}
		});
		
		selectedCreatureCopy = new Button("Copy");
		selectedCreatureCopy.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedCreature == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(CreatureEditor.this, "creatures", selectedCreature.getID());
				popup.setCallback(CreatureEditor.this);
				popup.openPopupCentered();
			}
		});
		
		selectedCreatureNameLabel = new Label("Name");
		
		selectedCreatureNameField = new EditField();
		selectedCreatureNameField.addCallback(new EditField.Callback() {
			@Override public void callback(int arg0) {
				selectedCreature.setName(selectedCreatureNameField.getText());
			}
		});
		
		selectedCreatureRaceLabel = new Label("Race");
		
		selectedCreatureRaceModel = new SimpleChangableListModel<String>();
		selectedCreatureRaceBox = new ComboBox<String>(selectedCreatureRaceModel);
		selectedCreatureRaceBox.setTheme("mediumcombobox");
		selectedCreatureRaceBox.addCallback(new Runnable() {
			@Override public void run() {
				String raceID = selectedCreatureRaceModel.getEntry(selectedCreatureRaceBox.getSelected());
				selectedCreature.setRace(Game.ruleset.getRace(raceID));
				
				if (selectedCreature.drawWithSubIcons()) {
					if (!selectedCreature.drawOnlyHandSubIcons())
						selectedCreature.addBaseSubIcons();
					
					selectedCreature.getInventory().addAllSubIcons();
				}
				
				update();
			}
		});
		
		for (Race race : Game.ruleset.getAllRaces()) {
			selectedCreatureRaceModel.addElement(race.getID());
		}
		
		selectedCreatureRoleLabel = new Label("Role");
		
		setCreatureRoleCallback = new SetCreatureRoleCallback();
		
		selectedCreatureRoleModel = new SimpleChangableListModel<String>();
		selectedCreatureRoleBox = new ComboBox<String>(selectedCreatureRoleModel);
		selectedCreatureRoleBox.setTheme("mediumcombobox");
		
		for (Role role : Game.ruleset.getAllRoles()) {
			selectedCreatureRoleModel.addElement(role.getID());
		}
		
		selectedCreatureLevelLabel = new Label("Level");
		
		selectedCreatureLevelAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 99, 1));
		
		selectedCreatureAttributes = new ArrayList<AttributeAdjuster>();
		
		selectedCreatureAttributesLabel = new Label("Attributes");
		
		selectedCreatureAIScriptLabel = new Label("AI Script");
		
		selectedCreatureAIScriptBox = new ComboBox<Script>(Game.campaignEditor.getAIScripts());
		selectedCreatureAIScriptBox.setTheme("largecombobox");
		selectedCreatureAIScriptBox.addCallback(new Runnable() {
			@Override public void run() {
				int index = selectedCreatureAIScriptBox.getSelected();
				if (index != -1)
					selectedCreature.setAIScript(Game.campaignEditor.getAIScripts().getEntry(index).getID());
			}
		});
		
		Button selectedCreatureClearAI = new Button("[x]");
		selectedCreatureClearAI.setTooltipContent("Clear AI Script");
		selectedCreatureClearAI.addCallback(new Runnable() {
			@Override public void run() {
				selectedCreature.setAIScript(null);
				selectedCreatureAIScriptBox.setSelected(-1);
			}
		});
		
		selectedCreatureConversationLabel = new Label("Conversation");
		
		selectedCreatureConversationBox = new ReferenceComboBox<Script>(Game.campaignEditor.getConversationScripts());
		selectedCreatureConversationBox.setTheme("largecombobox");
		selectedCreatureConversationBox.addCallback(new Runnable() {
			@Override public void run() {
				int index = selectedCreatureConversationBox.getSelected();
				if (index != -1)
					selectedCreature.setConversationScript(Game.campaignEditor.getConversationScripts().getEntry(index).getID());
			}
		});
		
		Button selectedCreatureClearConvo = new Button("[x]");
		selectedCreatureClearConvo.setTooltipContent("Clear Conversation Script");
		selectedCreatureClearConvo.addCallback(new Runnable() {
			@Override public void run() {
				selectedCreature.setConversationScript(null);
				selectedCreatureConversationBox.setSelected(-1);
			}
		});
		
		selectedCreatureCurrencyRewardLabel = new Label("Currency Reward");
		
		selectedCreatureCurrencyRewardLabel2 = new Label("to");
		
		selectedCreatureMinCurrencyReward = new ValueAdjusterInt(new SimpleIntegerModel(0, 1000000, 0));
		selectedCreatureMinCurrencyReward.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedCreature.setMinCurrencyReward(selectedCreatureMinCurrencyReward.getValue());
			}
		});
		
		selectedCreatureImmortal = new ToggleButton("Immortal");
		selectedCreatureImmortal.addCallback(new Runnable() {
			@Override public void run() {
				selectedCreature.setImmortal(selectedCreatureImmortal.isActive());
			}
		});
		
		selectedCreatureMaxCurrencyReward = new ValueAdjusterInt(new SimpleIntegerModel(0, 1000000, 0));
		selectedCreatureMaxCurrencyReward.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedCreature.setMaxCurrencyReward(selectedCreatureMaxCurrencyReward.getValue());
			}
		});
		
		secondaryEditors = new ExpandableTabbedPane();
		
		selectedCreatureStatsViewer = new CreatureStatsViewer();
		secondaryEditors.addTab("Stats", selectedCreatureStatsViewer);
		
		selectedCreatureInventoryEditor = new InventoryEditor();
		secondaryEditors.addTab("Inventory", selectedCreatureInventoryEditor);
		
		selectedCreatureLootEditor = new LootEditor();
		secondaryEditors.addTab("Loot", selectedCreatureLootEditor);
		
		selectedCreatureSkillEditor = new SkillEditor();
		secondaryEditors.addTab("Skills", selectedCreatureSkillEditor);
		
		selectedCreatureAbilityEditor = new AbilityEditor();
		secondaryEditors.addTab("Abilities", selectedCreatureAbilityEditor);
		
		selectedCreatureCosmeticEditor = new CosmeticEditor();
		secondaryEditors.addTab("Cosmetic", selectedCreatureCosmeticEditor);
		
		selectedCreatureContent.setVisible(false);
		
		Group rightH = selectedCreatureContent.createParallelGroup();
		Group rightV = selectedCreatureContent.createSequentialGroup();
		
		rightH.addGroup(selectedCreatureContent.createSequentialGroup(selectedCreatureNameLabel,
				selectedCreatureNameField));
		rightV.addGroup(selectedCreatureContent.createParallelGroup(selectedCreatureNameLabel,
				selectedCreatureNameField));
		
		rightH.addGroup(selectedCreatureContent.createSequentialGroup(selectedCreatureRaceLabel,
				selectedCreatureRaceBox, selectedCreatureImmortal));
		rightV.addGroup(selectedCreatureContent.createParallelGroup(selectedCreatureRaceLabel,
				selectedCreatureRaceBox, selectedCreatureImmortal));
		
		rightH.addGroup(selectedCreatureContent.createSequentialGroup(selectedCreatureRoleLabel,
				selectedCreatureRoleBox, selectedCreatureLevelLabel, selectedCreatureLevelAdjuster));
		rightV.addGroup(selectedCreatureContent.createParallelGroup(selectedCreatureRoleLabel,
				selectedCreatureRoleBox, selectedCreatureLevelLabel, selectedCreatureLevelAdjuster));
		
		Group attrRowH = selectedCreatureContent.createSequentialGroup(selectedCreatureAttributesLabel);
		Group attrRowV = selectedCreatureContent.createParallelGroup(selectedCreatureAttributesLabel);
		
		for (int i = 0; i < 6; i++) {
			AttributeAdjuster adjuster = new AttributeAdjuster(i);
			selectedCreatureAttributes.add(adjuster);
			attrRowH.addWidget(adjuster);
			attrRowV.addWidget(adjuster);
		}
		rightH.addGroup(attrRowH);
		rightV.addGroup(attrRowV);
		
		rightH.addGroup(selectedCreatureContent.createSequentialGroup(selectedCreatureAIScriptLabel,
				selectedCreatureAIScriptBox, selectedCreatureClearAI));
		rightV.addGroup(selectedCreatureContent.createParallelGroup(selectedCreatureAIScriptLabel,
				selectedCreatureAIScriptBox, selectedCreatureClearAI));
		
		rightH.addGroup(selectedCreatureContent.createSequentialGroup(selectedCreatureConversationLabel,
				selectedCreatureConversationBox, selectedCreatureClearConvo));
		rightV.addGroup(selectedCreatureContent.createParallelGroup(selectedCreatureConversationLabel,
				selectedCreatureConversationBox, selectedCreatureClearConvo));
		
		rightH.addGroup(selectedCreatureContent.createSequentialGroup(selectedCreatureCurrencyRewardLabel,
				selectedCreatureMinCurrencyReward, selectedCreatureCurrencyRewardLabel2, selectedCreatureMaxCurrencyReward));
		rightV.addGroup(selectedCreatureContent.createParallelGroup(selectedCreatureCurrencyRewardLabel,
				selectedCreatureMinCurrencyReward, selectedCreatureCurrencyRewardLabel2, selectedCreatureMaxCurrencyReward));
		
		rightH.addWidget(secondaryEditors);
		rightV.addWidget(secondaryEditors);
		
		selectedCreatureContent.setHorizontalGroup(rightH);
		selectedCreatureContent.setVerticalGroup(rightV);
		
		Group leftH = content.createParallelGroup(creatureLabel, creaturesBox);
		Group leftV = content.createSequentialGroup(creatureLabel, creaturesBox);
		
		Group bottomLeftH = content.createSequentialGroup(selectedCreatureSave, newCreature, deleteCreature, selectedCreatureCopy);
		Group bottomLeftV = content.createParallelGroup(selectedCreatureSave, newCreature, deleteCreature, selectedCreatureCopy);
		
		leftH.addGroup(bottomLeftH);
		leftV.addGroup(bottomLeftV);
		
		Group mainH = content.createSequentialGroup(leftH);
		Group mainV = content.createParallelGroup(leftV);
		
		mainH.addWidget(selectedCreatureContent);
		mainV.addWidget(selectedCreatureContent);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	public void selectedCreatureSetRole() {
		RoleSet roles = selectedCreature.getRoles();
		roles.clear();
		
		int index = selectedCreatureRoleBox.getSelected();
		
		if (index != -1) {
			Role role = Game.ruleset.getRole(selectedCreatureRoleModel.getEntry(index));
			roles.addLevels(role, selectedCreatureLevelAdjuster.getValue());
		}
		
		update();
	}
	
	@Override public void saveSelected() {
		if (selectedCreature == null) return;
		
		CreatureWriter.saveCreature(selectedCreature);
		
		Game.entityManager.removeEntity(selectedCreature.getID());
		
		Game.campaignEditor.updateStatusText("Creature " + selectedCreature.getName() + " saved.");
	}
	
	@Override public void update() {
		int index = creaturesBox.getSelected();
		
		if (index == -1) {
			selectedCreature = null;
			selectedCreatureContent.setVisible(false);
		} else {
			selectedCreature = Game.campaignEditor.getCreaturesModel().getEntry(index);
			selectedCreatureContent.setVisible(true);
			
			String name = selectedCreature.getName();
			if (name != null)
				selectedCreatureNameField.setText(name);
			else
				selectedCreatureNameField.setText("");
			
			selectedCreatureImmortal.setActive(selectedCreature.isImmortal());
			
			String raceID = selectedCreature.getRace().getID();
			selectedCreatureRaceBox.setSelected(selectedCreatureRaceModel.findElement(raceID));
			
			selectedCreatureAttributes.get(0).setValue(selectedCreature.stats().get(Stat.BaseStr));
			selectedCreatureAttributes.get(1).setValue(selectedCreature.stats().get(Stat.BaseDex));
			selectedCreatureAttributes.get(2).setValue(selectedCreature.stats().get(Stat.BaseCon));
			selectedCreatureAttributes.get(3).setValue(selectedCreature.stats().get(Stat.BaseInt));
			selectedCreatureAttributes.get(4).setValue(selectedCreature.stats().get(Stat.BaseWis));
			selectedCreatureAttributes.get(5).setValue(selectedCreature.stats().get(Stat.BaseCha));
			
			// remove the callback for setting the creature role so we don't overwrite the creature's
			// role here
			selectedCreatureLevelAdjuster.getModel().removeCallback(setCreatureRoleCallback);
			selectedCreatureRoleBox.removeCallback(setCreatureRoleCallback);
			
			Role baseRole = selectedCreature.getRoles().getBaseRole();
			if (baseRole != null) {
				int level = selectedCreature.getRoles().getLevel(baseRole);
				
				selectedCreatureRoleBox.setSelected(selectedCreatureRoleModel.findElement(baseRole.getID()));
				selectedCreatureLevelAdjuster.setValue(level);
			} else {
				selectedCreatureRoleBox.setSelected(-1);
				selectedCreatureLevelAdjuster.setValue(0);
			}
			
			// add the callback back
			selectedCreatureLevelAdjuster.getModel().addCallback(setCreatureRoleCallback);
			selectedCreatureRoleBox.addCallback(setCreatureRoleCallback);
			
			if (selectedCreature.hasAI()) {
				String ai = selectedCreature.getAIScript();
				
				selectedCreatureAIScriptBox.setSelected(-1);
				for (int i = 0; i < Game.campaignEditor.getAIScripts().getNumEntries(); i++) {
					if (Game.campaignEditor.getAIScripts().getEntry(i).equals(ai)) {
						selectedCreatureAIScriptBox.setSelected(i);
					}
				}
			} else {
				selectedCreatureAIScriptBox.setSelected(-1);
			}
			
			String convo = selectedCreature.getConversationScript();
			
			selectedCreatureConversationBox.setSelected(-1);
			for (int i = 0; i < Game.campaignEditor.getConversationScripts().getNumEntries(); i++) {
				if (Game.campaignEditor.getConversationScripts().getEntry(i).equals(convo)) {
					selectedCreatureConversationBox.setSelected(i);
					break;
				}
			}
			
			selectedCreatureMinCurrencyReward.setValue(selectedCreature.getMinCurrencyReward());
			selectedCreatureMaxCurrencyReward.setValue(selectedCreature.getMaxCurrencyReward());
			
			selectedCreatureInventoryEditor.setInventory(selectedCreature.getInventory());
			selectedCreatureLootEditor.setLoot(selectedCreature.getLoot());
			selectedCreatureSkillEditor.setCreature(selectedCreature);
			selectedCreatureAbilityEditor.setAbilityList(selectedCreature.getAbilities());
			selectedCreatureCosmeticEditor.setCreature(selectedCreature);
			selectedCreatureStatsViewer.setCreature(selectedCreature);
		}
	}
	
	private class AttributeAdjuster extends ValueAdjusterInt implements Runnable {
		private Stat stat;
		
		public AttributeAdjuster(int index) {
			super(new SimpleIntegerModel(1, 99, 10));
			
			this.setTheme("smalladjuster");
			this.setSize(55, 20);
			this.getModel().addCallback(this);
			
			switch (index) {
			case 0: stat = Stat.BaseStr; break;
			case 1: stat = Stat.BaseDex; break;
			case 2: stat = Stat.BaseCon; break;
			case 3: stat = Stat.BaseInt; break;
			case 4: stat = Stat.BaseWis; break;
			case 5: stat = Stat.BaseCha; break;
			}
		}
		
		@Override
		public void run() {
			selectedCreature.stats().setStat(stat, this.getValue());
			update();
		}
	}
	
	@Override public void newComplete() {
		updateable.update();
	}

	@Override public void copyComplete() {
		updateable.update();
	}

	@Override public void deleteComplete() {
		this.selectedCreature = null;
		updateable.update();
	}
	
	private class SetCreatureRoleCallback implements Runnable {
		@Override public void run() {
			selectedCreatureSetRole();
		}
	}
	
	public class NewCreaturePopup extends NewFilePopup {
		private final EditField idField;
		
		public NewCreaturePopup(Widget parent) {
			super(parent, "Create a new creature", 50);
			
			idField = new EditField();
			
			this.addWidgetsAsGroup(new Label("ID"), idField);
			this.addAcceptAndCancel();
		}

		@Override public String newFileAccept() {
			String name = idField.getText();
			if (name == null || name.length() == 0) {
				setError("Please enter an ID.");
				return null;
			}
			
			File f = new File(Game.campaignEditor.getPath() + "/creatures/" + name + ".txt");
			if (f.exists()) {
				setError("A creature of this ID already exists.");
				return null;
			}
			
			File f2 = new File(Game.campaignEditor.getPath() + "/items/" + name + ".txt");
			if (f2.exists()) {
				setError("Another entity of this ID already exists.");
				return null;
			}
			
			try {
				f.createNewFile();
				
				BufferedWriter out = new BufferedWriter(new FileWriter(f));
				out.write("race ");
				out.write(Game.ruleset.getString("DefaultRace"));
				out.newLine();
				out.write("gender ");
				out.write(Game.ruleset.getString("DefaultGender"));
				
				out.close();
				
			} catch (Exception e) {
				setError("Invalid ID.");
				return null;
			}
			
			Game.campaignEditor.updateStatusText("New creature " + name + " created.");
			
			return "creatures/" + name + ResourceType.Text.getExtension();
		}
	}
}
