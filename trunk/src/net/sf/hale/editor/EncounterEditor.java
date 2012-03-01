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

import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.Updateable;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.NewFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.entity.Creature;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.Faction;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.widgets.ExpandableScrollPane;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class EncounterEditor extends EditorWindow implements Updateable, PopupCallback {
	private final Updateable updateable;
	
	private final Label encountersLabel;
	private final Button newEncounter;
	private final Button deleteEncounter;
	private final Button copyEncounter;
	
	private final ListBox<Encounter> encountersBox;
	
	private final DialogLayout content;
	
	private final DialogLayout selectedContent;
	
	private Encounter selectedEncounter;
	private final Label encounterSizeLabel;
	private final ValueAdjusterInt encounterSize;
	private final ListBox<Creature> creaturesBox;
	private final SimpleChangableListModel<Creature> creaturesModel;
	
	private final Label gridTitle;
	private final ScrollPane gridScrollPane;
	private final EncounterGridView gridView;
	
	private Creature selectedCreature;
	
	private final Label allCreaturesTitle;
	private final ListBox<Creature> allCreaturesBox;
	
	private final List<Faction> allFactions;
	private final Label factionsTitle;
	private final ComboBox<String> factionsBox;
	private final SimpleChangableListModel<String> factionsModel;
	
	private final Label challengeLabel;
	private final ValueAdjusterInt challengeAdjuster;
	
	private final ToggleButton respawnButton;
	private final ValueAdjusterInt respawnAdjuster;
	private final Label respawnAdjusterLabel;
	
	private final ToggleButton randomizeButton;
	
	private final Label minCreaturesLabel;
	private final ValueAdjusterInt minCreaturesAdjuster;
	
	private final Label maxCreaturesLabel;
	private final ValueAdjusterInt maxCreaturesAdjuster;
	
	private final Button saveEncounter;
	
	public EncounterEditor(Updateable update) {
		super("Encounter Editor");
		
		this.updateable = update;
		
		content = new DialogLayout();
		content.setTheme("/encountereditorlayout");
		this.add(content);
		
		encountersLabel = new Label("Select an Encounter");
		
		encountersBox = new ReferenceListBox<Encounter>(Game.campaignEditor.getEncountersModel());
		
		encountersBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				saveSelected();
				
				if (encountersBox.getSelected() == -1) selectedEncounter = null;
				else selectedEncounter = Game.campaignEditor.getEncountersModel().getEntry(encountersBox.getSelected());
				
				updateSelectedEncounter();
			}
		});
		
		newEncounter = new Button("New");
		newEncounter.addCallback(new Runnable() {
			@Override public void run() {
				NewEncounterPopup popup = new NewEncounterPopup(EncounterEditor.this);
				popup.setCallback(EncounterEditor.this);
				popup.openPopupCentered();
			}
		});
		
		copyEncounter = new Button("Copy");
		copyEncounter.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedEncounter == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(EncounterEditor.this, "encounters", selectedEncounter.getID());
				popup.setCallback(EncounterEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteEncounter = new Button("Delete");
		deleteEncounter.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedEncounter == null) return;
				
				DeleteFilePopup popup = new DeleteFilePopup(EncounterEditor.this, Game.campaignEditor.getPath() +
						"/encounters/" + selectedEncounter.getName() + ".txt", selectedEncounter);
				popup.setCallback(EncounterEditor.this);
				popup.openPopupCentered();
			}
		});
		
		saveEncounter = new Button("Save");
		saveEncounter.setTheme("/button");
		saveEncounter.setSize(100, 25);
		saveEncounter.setPosition(525, 130);
		saveEncounter.addCallback(new Runnable() {
			@Override
			public void run() {
				saveSelected();
			}
		});
		
		selectedContent = new DialogLayout();
		selectedContent.setTheme("/encountereditorlayout");
		
		allFactions = new ArrayList<Faction>();
		factionsModel = new SimpleChangableListModel<String>();
		
		allCreaturesTitle = new Label("Creatures");
		allCreaturesBox = new ReferenceListBox<Creature>(Game.campaignEditor.getCreaturesModel());
		
		factionsTitle = new Label("Faction");
		
		factionsBox = new ComboBox<String>(factionsModel);
		factionsBox.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedEncounter != null && factionsBox.getSelected() != -1) {
					selectedEncounter.setFaction(allFactions.get(factionsBox.getSelected()));
				}
			}
		});
		
		encounterSizeLabel = new Label("Size");
		
		encounterSize = new ValueAdjusterInt(new SimpleIntegerModel(0, 10, 1));
		encounterSize.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedEncounter.setSize(encounterSize.getValue());
				EncounterEditor.this.update();
			}
		});
		
		challengeLabel = new Label("Challenge");
		
		challengeAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		challengeAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedEncounter.setChallenge(challengeAdjuster.getValue());
			}
		});
		
		respawnButton = new ToggleButton("Respawn every");
		respawnButton.setTheme("/radiobutton");
		respawnButton.addCallback(new Runnable() {
			@Override public void run() {
				selectedEncounter.setRespawn(respawnButton.isActive());
			}
		});
		
		respawnAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 0));
		respawnAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedEncounter.setRespawnHours(respawnAdjuster.getValue());
			}
		});
		
		respawnAdjusterLabel = new Label("hours");
		
		randomizeButton = new ToggleButton("Randomize");
		randomizeButton.setTheme("/radiobutton");
		randomizeButton.addCallback(new Runnable() {
			@Override public void run() {
				selectedEncounter.setRandomize(randomizeButton.isActive());
			}
		});
		
		minCreaturesLabel = new Label("Min Creatures");
		
		minCreaturesAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		minCreaturesAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				setRandomCreatures();
			}
		});
		
		maxCreaturesLabel = new Label("Max Creatures");
		
		maxCreaturesAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 0));
		maxCreaturesAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				setRandomCreatures();
			}
		});
		
		Group middleV = selectedContent.createSequentialGroup();
		Group middleH = selectedContent.createParallelGroup();
		
		middleV.addGroup(selectedContent.createParallelGroup(factionsTitle, factionsBox));
		middleH.addGroup(selectedContent.createSequentialGroup(factionsTitle, factionsBox));
		
		middleV.addGroup(selectedContent.createParallelGroup(encounterSizeLabel, encounterSize));
		middleH.addGroup(selectedContent.createSequentialGroup(encounterSizeLabel, encounterSize));
		
		middleV.addGroup(selectedContent.createParallelGroup(challengeLabel, challengeAdjuster));
		middleH.addGroup(selectedContent.createSequentialGroup(challengeLabel, challengeAdjuster));
		
		middleV.addGroup(selectedContent.createParallelGroup(respawnButton, respawnAdjuster, respawnAdjusterLabel));
		middleH.addGroup(selectedContent.createSequentialGroup(respawnButton, respawnAdjuster, respawnAdjusterLabel));
		
		middleV.addWidget(randomizeButton);
		middleH.addWidget(randomizeButton);
		
		middleV.addGroup(selectedContent.createParallelGroup(minCreaturesLabel, minCreaturesAdjuster));
		middleH.addGroup(selectedContent.createSequentialGroup(minCreaturesLabel, minCreaturesAdjuster));
		
		middleV.addGroup(selectedContent.createParallelGroup(maxCreaturesLabel, maxCreaturesAdjuster));
		middleH.addGroup(selectedContent.createSequentialGroup(maxCreaturesLabel, maxCreaturesAdjuster));
		
		middleV.addGap(20);
		
		middleV.addWidget(allCreaturesTitle);
		middleH.addWidget(allCreaturesTitle);
		
		middleV.addWidget(allCreaturesBox);
		middleH.addWidget(allCreaturesBox);
		
		creaturesModel = new SimpleChangableListModel<Creature>();
		creaturesBox = new ReferenceListBox<Creature>(creaturesModel);
		creaturesBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				updateSelectedCreature();
			}
		});
		
		gridView = new EncounterGridView(this);
		gridScrollPane = new ExpandableScrollPane(gridView);
		
		gridTitle = new Label("Left click to add, right click to remove.");
		
		Group rightV = selectedContent.createSequentialGroup(creaturesBox, gridTitle, gridScrollPane);
		Group rightH = selectedContent.createParallelGroup(creaturesBox, gridTitle, gridScrollPane);
		
		selectedContent.setHorizontalGroup(selectedContent.createSequentialGroup(middleH, rightH));
		selectedContent.setVerticalGroup(selectedContent.createParallelGroup(middleV, rightV));
		
		Group leftV = content.createSequentialGroup(encountersLabel, encountersBox);
		Group leftH = content.createParallelGroup(encountersLabel, encountersBox);
		
		leftV.addGroup(content.createParallelGroup(saveEncounter, newEncounter, deleteEncounter, copyEncounter));
		leftH.addGroup(content.createSequentialGroup(saveEncounter, newEncounter, deleteEncounter, copyEncounter));
		
		Group mainV = content.createParallelGroup(leftV);
		mainV.addWidget(selectedContent);
		
		Group mainH = content.createSequentialGroup(leftH);
		mainH.addWidget(selectedContent);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	@Override public void saveSelected() {
		if (selectedEncounter == null) return;
		
		File fout = new File(Game.campaignEditor.getPath() + "/encounters/" + selectedEncounter.getName() + ".txt");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			
			out.write("size " + selectedEncounter.getSize());
			out.newLine();
			
			out.write("faction \"" + selectedEncounter.getFaction().getName() + "\"");
			out.newLine();
			
			out.write("challenge " + selectedEncounter.getChallenge());
			out.newLine();
			
			if (selectedEncounter.randomize()) {
				out.write("randomize true");
				out.newLine();
				
				out.write("numRandomCreatures " + selectedEncounter.getMinRandomCreatures() +
						  " " + selectedEncounter.getMaxRandomCreatures());
				out.newLine();
			}
			
			if (selectedEncounter.isRespawn()) {
				out.write("respawn true");
				out.newLine();
				
				out.write("respawnHours " + selectedEncounter.getRespawnHours());
				out.newLine();
			}
			
			for (Creature creature : selectedEncounter.getBaseCreatures()) {
				out.write("creature \"" + creature.getID() + "\" " + creature.getX() + " " + creature.getY());
				out.newLine();
			}
			
			out.close();
			
		} catch (Exception e) {
			Game.campaignEditor.updateStatusText("Error saving encounter: " + selectedEncounter.getName());
			Logger.appendToErrorLog("Error saving encounter " + selectedEncounter.getName(), e);
			return;
		}
		
		Game.campaignEditor.reloadEncounters();
		Game.campaignEditor.updateStatusText("Encounter " + selectedEncounter.getName() + " saved.");
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			populateFactionsList();
			updateSelectedEncounter();
		}
	}
	
	public void setRandomCreatures() {
		int min = minCreaturesAdjuster.getValue();
		int max = maxCreaturesAdjuster.getValue();
		
		selectedEncounter.setNumRandomCreatures(min, max);
	}
	
	public Creature getSelectedAllCreature() {
		if (allCreaturesBox.getSelected() == -1) return null;
		else return Game.campaignEditor.getCreaturesModel().getEntry(allCreaturesBox.getSelected());
	}
	
	public Creature getSelectedEncounterCreature() { return selectedCreature; }
	public Encounter getSelectedEncounter() { return selectedEncounter; }
	
	public void populateFactionsList() {
		allFactions.clear();
		factionsModel.clear();
		
		allFactions.addAll(Game.ruleset.getAllFactions());
		
		for (Faction faction : allFactions) {
			factionsModel.addElement(faction.getName());
		}
	}
	
	public void updateSelectedEncounter() {
		creaturesModel.clear();
		gridView.deselectTile();
		
		if (selectedEncounter == null) {
			selectedContent.setVisible(false);
		} else {
			selectedContent.setVisible(true);
			
			// read in new versions of all the base creatures
			List<Creature> baseCreatures = selectedEncounter.getBaseCreatures();
			List<Creature> newCreatures = new ArrayList<Creature>(baseCreatures.size());
			
			for (Creature baseCreature : baseCreatures) {
				Creature newCreature = Game.entityManager.getCreature(baseCreature.getID());
				newCreature.setPosition(baseCreature.getX(), baseCreature.getY());
				newCreature.setFaction(baseCreature.getFaction());
				newCreatures.add(newCreature);
			}
			
			// copy over the new creatures
			baseCreatures.clear();
			for (Creature newCreature : newCreatures) {
				baseCreatures.add(newCreature);
			}
			
			encounterSize.setValue(selectedEncounter.getSize());
			gridView.setEncounter(selectedEncounter);
			
			int index = allFactions.indexOf(selectedEncounter.getFaction());
			if (index != -1) factionsBox.setSelected(index);
			else factionsBox.setSelected(0);
			
			for (Creature creature : selectedEncounter.getBaseCreatures()) {
				creaturesModel.addElement(creature);
			}
			
			challengeAdjuster.setValue(selectedEncounter.getChallenge());
			
			randomizeButton.setActive(selectedEncounter.randomize());
			
			int min = selectedEncounter.getMinRandomCreatures();
			int max = selectedEncounter.getMaxRandomCreatures();
			
			minCreaturesAdjuster.setValue(min);
			maxCreaturesAdjuster.setValue(max);
			
			respawnButton.setActive(selectedEncounter.isRespawn());
			respawnAdjuster.setValue(selectedEncounter.getRespawnHours());
		}
	}

	public void updateSelectedCreature(Point position) {
		for (int i = 0; i < selectedEncounter.getBaseCreatures().size(); i++) {
			Creature creature = selectedEncounter.getBaseCreatures().get(i);
			
			if (position.x == creature.getX() && position.y == creature.getY()) {
				creaturesBox.setSelected(i);
				updateSelectedCreature();
				return;
			}
		}
		
		creaturesBox.setSelected(-1);
		updateSelectedCreature();
	}
	
	public void updateSelectedCreature() {
		if (creaturesBox.getSelected() == -1) {
			selectedCreature = null;
			gridView.deselectTile();
		} else {
			selectedCreature = selectedEncounter.getBaseCreatures().get(creaturesBox.getSelected());
			gridView.setSelectedTile(selectedCreature.getX(), selectedCreature.getY());
		}
	}
	
	@Override public void newComplete() {
		updateable.update();
	}

	@Override public void copyComplete() {
		updateable.update();
	}

	@Override public void deleteComplete() {
		this.selectedEncounter = null;
		updateable.update();
	}
	
	@Override public void update() {
		gridView.setEncounter(selectedEncounter);
	}
	
	public class NewEncounterPopup extends NewFilePopup {
		private final EditField idField;
		
		public NewEncounterPopup(Widget parent) {
			super(parent, "Create a new encounter", 50);
			
			idField = new EditField();
			
			addWidgetsAsGroup(new Label("ID"), idField);
			addAcceptAndCancel();
		}
		
		@Override public String newFileAccept() {
			String name = idField.getText();
			if (name == null || name.length() == 0) {
				setError("Please enter an ID.");
				return null;
			}
			
			File f = new File(Game.campaignEditor.getPath() + "/encounters/" + name + ".txt");
			if (f.exists()) {
				setError("An encounter with this ID name already exists.");
				return null;
			}
			
			try {
				f.createNewFile();
				
				BufferedWriter out = new BufferedWriter(new FileWriter(f));
				
				out.write("size 0");
				out.newLine();
				out.write("challenge 0");
				out.newLine();
				out.write("faction Hostile");
				out.newLine();
				
				out.close();
				
			} catch (Exception e) {
				setError("Invalid ID.");
				return null;
			}
			
			Game.campaignEditor.updateStatusText("New encounter " + name + " created.");
			
			return "encounters/" + name + ResourceType.Text.getExtension();
		}
	}
}
