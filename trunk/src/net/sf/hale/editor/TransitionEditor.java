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

import java.util.List;

import net.sf.hale.AreaTransition;
import net.sf.hale.Game;
import net.sf.hale.WorldMapLocation;
import net.sf.hale.editor.reference.ReferenceComboBox;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.util.Point;
import net.sf.hale.widgets.ExpandableScrollPane;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class TransitionEditor extends EditorWindow implements Updateable, PopupCallback {
	private final Updateable updateable;
	private AreaTransition selectedTransition;
	
	private final DialogLayout content;
	
	private final Label transitionLabel;
	private final ListBox<AreaTransition> transitionsBox;
	
	private final Button newTransition;
	private final Button deleteTransition;
	private final Button copyTransition;
	private final Button saveTransition;
	
	private final Label iconLabel;
	private final Label twoWayLabel;
	private final Label fromAreaLabel;
	private final Label toAreaLabel;
	
	private final ToggleButton initiallyActivated;
	
	private final Label locationLabel;
	private final ComboBox<WorldMapLocation> locationBox;
	
	private final ToggleButton editToPositions;
	private final ToggleButton editFromPositions;
	
	private final Label gridTitle;
	private final TransitionGridView gridView;
	private final ScrollPane gridScrollPane;
	private final ListBox<String> positionsBox;
	private final SimpleChangableListModel<String> positionsModel;
	
	public TransitionEditor(Updateable update) {
		super("Transition Editor");
		
		this.updateable = update;
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		this.add(content);
		
		transitionLabel = new Label("Select a transition:");
		
		transitionsBox = new ReferenceListBox<AreaTransition>(Game.campaignEditor.getTransitionsModel());
		transitionsBox.setTheme("listboxnoexpand");
		transitionsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				saveSelected();
				
				if (transitionsBox.getSelected() == -1) selectedTransition = null;
				else selectedTransition = Game.campaignEditor.getTransitionsModel().getEntry(transitionsBox.getSelected());
				
				editToPositions.setActive(false);
				editFromPositions.setActive(false);
				updateSelectedTransition();
			}
		});
		
		newTransition = new Button("New");
		newTransition.addCallback(new Runnable() {
			@Override public void run() {
				NewTransitionPopup popup = new NewTransitionPopup(TransitionEditor.this);
				popup.setCallback(TransitionEditor.this);
				popup.openPopupCentered();
			}
		});
		
		copyTransition = new Button("Copy");
		copyTransition.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedTransition == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(TransitionEditor.this, "transitions", selectedTransition.getID());
				popup.setCallback(TransitionEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteTransition = new Button("Delete");
		deleteTransition.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedTransition == null) return;
				
				DeleteFilePopup popup = new DeleteFilePopup(TransitionEditor.this, Game.campaignEditor.getPath() +
						"/transitions/" + selectedTransition.getName() + ".txt", selectedTransition);
				popup.setCallback(TransitionEditor.this);
				popup.openPopupCentered();
			}
		});
		
		saveTransition = new Button("Save");
		saveTransition.addCallback(new Runnable() {
			@Override public void run() {
				saveSelected();
			}
		});
		
		iconLabel = new Label("Icon: ");
		iconLabel.setTheme("/labelblack");
		
		twoWayLabel = new Label("Two Way: ");
		twoWayLabel.setTheme("/labelblack");
		
		
		
		fromAreaLabel = new Label("From area: ");
		
		toAreaLabel = new Label("To area: ");
		
		initiallyActivated = new ToggleButton("Initially Activated");
		initiallyActivated.setTheme("/radiobutton");
		initiallyActivated.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedTransition == null) return;
				
				selectedTransition.setInitiallyActivated(initiallyActivated.isActive());
			}
		});
		
		locationLabel = new Label("World Map Location");
		locationLabel.setTheme("/labelblack");
		
		locationBox = new ReferenceComboBox<WorldMapLocation>(Game.campaignEditor.getLocationsModel());
		locationBox.setTheme("/combobox");
		locationBox.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedTransition == null) return;
				
				int index = locationBox.getSelected();
				if (index != -1) {
					WorldMapLocation location = Game.campaignEditor.getLocationsModel().getEntry(index);
					selectedTransition.setWorldMapLocation(location.getID());
				}
			}
		});
		
		
		editToPositions = new ToggleButton("Edit To Area Positions");
		editToPositions.setTheme("/togglebutton");
		editToPositions.addCallback(new Runnable() {
			@Override public void run() {
				editToPositions.setActive(true);
				editFromPositions.setActive(false);
				updateSelectedTransition();
			}
		});
		
		editFromPositions = new ToggleButton("Edit From Area Positions");
		editFromPositions.setTheme("/togglebutton");
		editFromPositions.addCallback(new Runnable() {
			@Override public void run() {
				editToPositions.setActive(false);
				editFromPositions.setActive(true);
				updateSelectedTransition();
			}
		});
		
		positionsBox = new ListBox<String>();
		positionsBox.setTheme("listboxsmall");
		positionsModel = new SimpleChangableListModel<String>();
		positionsBox.setModel(positionsModel);
		
		positionsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				gridView.setSelectedPoint(positionsBox.getSelected());
			}
		});
		
		gridScrollPane = new ExpandableScrollPane();
		gridScrollPane.setTheme("/scrollpane");
		gridScrollPane.setPosition(310, 145);
		gridScrollPane.setSize(300, 300);
		
		gridTitle = new Label("Select a point and left click on the hex grid to move it.");
		
		gridView = new TransitionGridView(this);
		gridScrollPane.setContent(gridView);
		
		Group leftH = content.createParallelGroup(transitionLabel, transitionsBox);
		Group leftV = content.createSequentialGroup(transitionLabel, transitionsBox);
		
		leftH.addGroup(content.createSequentialGroup(saveTransition, newTransition, deleteTransition, copyTransition));
		leftV.addGroup(content.createParallelGroup(saveTransition, newTransition, deleteTransition, copyTransition));
		
		Group rightH = content.createParallelGroup(iconLabel, twoWayLabel, fromAreaLabel, toAreaLabel, initiallyActivated);
		Group rightV = content.createSequentialGroup(iconLabel, twoWayLabel, fromAreaLabel, toAreaLabel, initiallyActivated);
		
		rightH.addGroup(content.createSequentialGroup(locationLabel, locationBox));
		rightV.addGroup(content.createParallelGroup(locationLabel, locationBox));
		
		rightH.addGroup(content.createSequentialGroup(editToPositions, editFromPositions));
		rightV.addGroup(content.createParallelGroup(editToPositions, editFromPositions));
		
		rightH.addWidget(gridTitle);
		rightV.addWidget(gridTitle);
		
		rightH.addGroup(content.createSequentialGroup(positionsBox, gridScrollPane));
		rightV.addGroup(content.createParallelGroup(positionsBox, gridScrollPane));
		
		Group mainH = content.createSequentialGroup(leftH, rightH);
		Group mainV = content.createParallelGroup(leftV, rightV);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	@Override public void saveSelected() {
		if (selectedTransition == null) return;
		
		selectedTransition.saveToFile();
		Game.campaignEditor.updateStatusText("Transition " + selectedTransition.getName() + " saved.");
		Game.campaignEditor.reloadTriggersAndTransitions();
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) updateSelectedTransition();
	}
	
	public void setSelectedPoint(int index) {
		positionsBox.setSelected(index);
	}
	
	public void populatePositionsList(AreaTransition transition) {
		gridScrollPane.setVisible(true);
		gridTitle.setVisible(true);
		positionsBox.setVisible(true);
		
		positionsModel.clear();
		
		if (transition != null) {
			List<Point> points;
			
			if (editToPositions.isActive()) points = transition.getToPositions();
			else points = transition.getFromPositions();
			
			for (int i = 0; i < points.size(); i++) {
				int index = i + 1;
				positionsModel.addElement(index + ": " + points.get(i).toString());
			}
		}
	}
	
	public void updateSelectedTransition() {
		iconLabel.setVisible(false);
		twoWayLabel.setVisible(false);
		fromAreaLabel.setVisible(false);
		toAreaLabel.setVisible(false);
		editToPositions.setVisible(false);
		editFromPositions.setVisible(false);
		initiallyActivated.setVisible(false);
		locationBox.setVisible(false);
		locationLabel.setVisible(false);
		
		gridScrollPane.setVisible(false);
		gridTitle.setVisible(false);
		positionsBox.setVisible(false);
		
		if (selectedTransition == null) {
			editToPositions.setActive(false);
			editFromPositions.setActive(false);
		} else {
			AreaTransition t = selectedTransition;
						
			iconLabel.setVisible(true);
			twoWayLabel.setVisible(true);
			fromAreaLabel.setVisible(true);
			toAreaLabel.setVisible(true);
			initiallyActivated.setVisible(true);
			locationBox.setVisible(true);
			locationLabel.setVisible(true);
			
			if (!t.getAreaTo().equals("World Map")) editToPositions.setVisible(true);
			if (t.twoWay() && !t.getAreaFrom().equals("World Map")) editFromPositions.setVisible(true);
			
			iconLabel.setText("Icon: " + t.getIcon());
			twoWayLabel.setText("Two Way: " + t.twoWay());
			fromAreaLabel.setText("From area: " + t.getAreaFrom() + " at " + t.getAreaFromX() + ", " + t.getAreaFromY());
			toAreaLabel.setText("To area: " + t.getAreaTo() + " at " + t.getAreaToX() + ", " + t.getAreaToY());
			
			if (editToPositions.isActive()) {
				populatePositionsList(t);
				gridView.setMode(t, true);
				
			} else if (editFromPositions.isActive()) {
				populatePositionsList(t);
				gridView.setMode(t, false);
			}
			
			initiallyActivated.setActive(t.initiallyActivated());
			
			String locationID = selectedTransition.getWorldMapLocation();
			if (locationID == null) {
				locationBox.setSelected(-1);
			} else {
				WorldMapLocation location = Game.curCampaign.getWorldMapLocation(locationID);
				
				int index = Game.campaignEditor.getLocationsModel().findElement(location);
				locationBox.setSelected(index);
			}
		}
	}
	
	@Override public void update() {
		updateable.update();
	}

	@Override public void newComplete() {
		updateable.update();
	}

	@Override public void copyComplete() {
		updateable.update();
	}

	@Override public void deleteComplete() {
		Game.curCampaign.removeAreaTransition(this.selectedTransition.getID());
		AreaEditor areaEditor = Game.campaignEditor.areaEditor;
		if (areaEditor != null) areaEditor.removeTransition(selectedTransition);
		
		this.selectedTransition = null;
		
		update();
	}
}
