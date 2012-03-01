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

import net.sf.hale.Area;
import net.sf.hale.AreaTrigger;
import net.sf.hale.Game;
import net.sf.hale.Updateable;
import net.sf.hale.editor.reference.ReferenceComboBox;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.reference.Script;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.util.Point;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class AreaTriggerEditor extends EditorWindow implements Updateable, PopupCallback {
	private AreaTrigger selectedTrigger;
	
	private final Updateable updateable;
	private final DialogLayout content;
	
	private final Label triggersLabel;
	private final ListBox<AreaTrigger> triggersBox;
	
	private final Button newTrigger;
	private final Button deleteTrigger;
	private final Button copyTrigger;
	
	private final DialogLayout selectedTriggerContent;
	
	private final Label selectedTriggerName;
	
	private final Label selectedTriggerAreaLabel;
	private final ListBox<Area> selectedTriggerAreaBox;
	
	private final Label selectedTriggerScriptLabel;
	private final ComboBox<Script> selectedTriggerScriptBox;
	
	private final Label pointsLabel;
	private final ListBox<String> pointsBox;
	private final SimpleChangableListModel<String> pointsModel;
	
	private final Button saveTrigger;
	
	public AreaTriggerEditor(Updateable updateable) {
		super("Trigger Editor");
		
		this.updateable = updateable;
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		this.add(content);
		
		triggersLabel = new Label("Select a trigger:");
		
		triggersBox = new ReferenceListBox<AreaTrigger>(Game.campaignEditor.getTriggersModel());
		triggersBox.setTheme("listboxnoexpand");
		triggersBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				saveSelected();
				
				if (triggersBox.getSelected() == -1) selectedTrigger = null;
				else selectedTrigger = Game.campaignEditor.getTriggersModel().getEntry(triggersBox.getSelected());
				
				updateSelectedTrigger();
			}
		});
		
		newTrigger = new Button("New");
		newTrigger.addCallback(new Runnable() {
			@Override
			public void run() {
				NewTriggerPopup popup = new NewTriggerPopup(AreaTriggerEditor.this);
				popup.setCallback(AreaTriggerEditor.this);
				popup.openPopupCentered();
			}
		});
		
		copyTrigger = new Button("Copy");
		copyTrigger.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedTrigger == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(AreaTriggerEditor.this, "triggers", selectedTrigger.getID());
				popup.setCallback(AreaTriggerEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteTrigger = new Button("Delete");
		deleteTrigger.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedTrigger == null) return;
				
				DeleteFilePopup popup = new DeleteFilePopup(AreaTriggerEditor.this,
						Game.campaignEditor.getPath() + "/triggers/" + selectedTrigger.getID() + ".txt", selectedTrigger);
				popup.setCallback(AreaTriggerEditor.this);
				popup.openPopupCentered();
			}
		});
		
		selectedTriggerContent = new DialogLayout();
		selectedTriggerContent.setTheme("/editorlayout");
		
		saveTrigger = new Button("Save");
		saveTrigger.addCallback(new Runnable() {
			@Override public void run() {
				saveSelected();
			}
		});
		
		selectedTriggerName = new Label();
		
		selectedTriggerAreaLabel = new Label("Area:");
		
		selectedTriggerAreaBox = new ReferenceListBox<Area>(Game.campaignEditor.getAreasModel());
		selectedTriggerAreaBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(ListBox.CallbackReason reason) {
				setTriggerArea();
			}
		});
		
		selectedTriggerScriptLabel = new Label("Script:");
		
		selectedTriggerScriptBox = new ReferenceComboBox<Script>(Game.campaignEditor.getTriggerScripts());
		selectedTriggerScriptBox.setTheme("largecombobox");
		selectedTriggerScriptBox.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedTrigger == null) return;
				int index = selectedTriggerScriptBox.getSelected();
				if (index != -1) {
					selectedTrigger.setScript(Game.campaignEditor.getTriggerScripts().getEntry(index).getID());
				}
			}
		});
		
		Button selectedTriggerClearScript = new Button("[x]");
		selectedTriggerClearScript.setTooltipContent("Clear the script");
		selectedTriggerClearScript.addCallback(new Runnable() {
			@Override public void run() {
				selectedTrigger.setScript(null);
				selectedTriggerScriptBox.setSelected(-1);
			}
		});
		
		pointsLabel = new Label("Points:");
		
		pointsModel = new SimpleChangableListModel<String>();
		pointsBox = new ListBox<String>(pointsModel);
		pointsBox.setTheme("listboxsmall");
		
		selectedTriggerContent.setVisible(false);
		
		Group rightH = selectedTriggerContent.createParallelGroup(selectedTriggerName);
		Group rightV = selectedTriggerContent.createSequentialGroup(selectedTriggerName);
		selectedTriggerContent.setHorizontalGroup(rightH);
		selectedTriggerContent.setVerticalGroup(rightV);
		
		rightH.addGroup(selectedTriggerContent.createSequentialGroup(selectedTriggerScriptLabel,
				selectedTriggerScriptBox, selectedTriggerClearScript));
		rightV.addGroup(selectedTriggerContent.createParallelGroup(selectedTriggerScriptLabel,
				selectedTriggerScriptBox, selectedTriggerClearScript));
		
		Group areasH = selectedTriggerContent.createParallelGroup(selectedTriggerAreaLabel, selectedTriggerAreaBox);
		Group areasV = selectedTriggerContent.createSequentialGroup(selectedTriggerAreaLabel, selectedTriggerAreaBox);
		
		Group pointsH = selectedTriggerContent.createParallelGroup(pointsLabel, pointsBox);
		Group pointsV = selectedTriggerContent.createSequentialGroup(pointsLabel, pointsBox);
		
		rightH.addGroup(selectedTriggerContent.createSequentialGroup(areasH, pointsH));
		rightV.addGroup(selectedTriggerContent.createParallelGroup(areasV, pointsV));
		
		Group leftH = content.createParallelGroup(triggersLabel, triggersBox);
		Group leftV = content.createSequentialGroup(triggersLabel, triggersBox);
		
		leftH.addGroup(content.createSequentialGroup(saveTrigger, newTrigger, deleteTrigger, copyTrigger));
		leftV.addGroup(content.createParallelGroup(saveTrigger, newTrigger, deleteTrigger, copyTrigger));
		
		Group mainH = content.createSequentialGroup(leftH);
		mainH.addWidget(selectedTriggerContent);
		Group mainV = content.createParallelGroup(leftV);
		mainV.addWidget(selectedTriggerContent);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	private void setTriggerArea() {
		int index = selectedTriggerAreaBox.getSelected();
		if (index == -1) return;
		
		if (selectedTrigger == null) return;
		
		Area area = Game.campaignEditor.getAreasModel().getEntry(index);
		
		selectedTrigger.setArea(area.getID());
		
		if (Game.campaignEditor.areaEditor != null) {
			if (area.getID().equals(Game.campaignEditor.areaEditor.getArea().getID())) {
				Game.campaignEditor.areaEditor.reloadTriggersAndTransitions();
				area.getTriggers().add(selectedTrigger.getID());
			}
		}
	}
	
	@Override public void saveSelected() {
		if (selectedTrigger == null) return;
		
		selectedTrigger.saveToFile();
		Game.campaignEditor.updateStatusText("Trigger " + selectedTrigger.getID() + " saved.");
		Game.campaignEditor.reloadTriggersAndTransitions();
	}
	
	@Override public void newComplete() {
		updateable.update();
	}

	@Override public void copyComplete() {
		updateable.update();
	}

	@Override public void deleteComplete() {
		this.selectedTrigger = null;
		
		updateable.update();
	}
	
	public void updateSelectedTrigger() {
		selectedTriggerContent.setVisible(selectedTrigger != null);
		
		if (selectedTrigger == null) return;
		
		selectedTriggerName.setText("Name: " + selectedTrigger.getID());
		
		selectedTriggerAreaBox.setSelected(-1);
		for (int i = 0; i < Game.campaignEditor.getAreasModel().getNumEntries(); i++) {
			String areaName = Game.campaignEditor.getAreasModel().getEntry(i).getID();
			if (areaName.equals(selectedTrigger.getArea())) {
				selectedTriggerAreaBox.setSelected(i);
			}
		}
		
		String script = selectedTrigger.getScriptFile();
		
		selectedTriggerScriptBox.setSelected(-1);
		for (int i = 0; i < Game.campaignEditor.getTriggerScripts().getNumEntries(); i++) {
			if (Game.campaignEditor.getTriggerScripts().getEntry(i).equals(script)) {
				selectedTriggerScriptBox.setSelected(i);
				break;
			}
		}
		
		updateSelectedTriggerPoints();
	}
	
	public void updateSelectedTriggerPoints() {
		if (selectedTrigger == null) return;
		
		pointsModel.clear();
		for (Point p : selectedTrigger.getGridPoints()) {
			pointsModel.addElement(p.toString());
		}
	}
	
	@Override public void update() {
		updateable.update();
	}
}
