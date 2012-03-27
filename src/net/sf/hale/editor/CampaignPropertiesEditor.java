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
import net.sf.hale.AreaTransition;
import net.sf.hale.Game;
import net.sf.hale.WorldMapLocation;
import net.sf.hale.editor.reference.ReferenceComboBox;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.widgets.IconSelectorPopup;
import net.sf.hale.editor.widgets.SpriteViewer;
import net.sf.hale.editor.widgets.TravelTimeSelector;
import net.sf.hale.entity.Creature;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Merchant;
import net.sf.hale.util.Point;
import net.sf.hale.view.WorldMapPopup;
import net.sf.hale.widgets.WorldMapViewer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class CampaignPropertiesEditor extends EditorWindow implements Updateable,
		CallbackWithReason<ListBox.CallbackReason>, IconSelectorPopup.Callback {
	
	private WorldMapLocation selectedLocation;
	
	private final Updateable parent;
	
	private final DialogLayout content;

	private final Label locationsLabel;
	private final ListBox<WorldMapLocation> locationsBox;
	
	private final Button saveButton;

	private final Button newButton, deleteButton;
	
	private final DialogLayout locationContent;
	
	private final Label nameLabel;
	private final EditField nameField;
	
	private final Label iconLabel;
	private final SpriteViewer iconViewer;
	private final Button setIconButton;
	
	private final Label startingTransitionLabel;
	private final ComboBox<AreaTransition> startingTransitionBox;
	
	private final Label positionLabel;
	private final ValueAdjusterInt xPositionAdjuster, yPositionAdjuster;
	
	private final Label travelTimeLabel;
	private final ScrollPane travelTimePane;
	private final DialogLayout travelTimePaneContent;
	
	private final ScrollPane worldMapPane;
	private final WorldMapViewer worldMapContent;
	
	private final Button showPopupButton;
	
	private final Label campaignNameLabel;
	private final EditField campaignNameField;
	
	private final Label startAreaLabel;
	private final ComboBox<Area> startAreaBox;
	
	private final Label startingMerchantLabel;
	private final ComboBox<Merchant> startingMerchantBox;
	private final Button clearStartingMerchant;
	
	private final Label startingCharacterLabel;
	private final ComboBox<Creature> startingCharacterBox;
	private final Button clearStartingCharacter;
	
	private final ToggleButton stripStartingCharacters;
	
	private final Label worldMapLabel;
	private final EditField worldMapField;
	private final Button worldMapSetButton;
	
	private final Label partySizeLabel;
	private final Label partySizeToLabel;
	private final ValueAdjusterInt partyMinSize;
	private final ValueAdjusterInt partyMaxSize;
	
	private final Label startingLevelLabel;
	private final Label startingLevelToLabel;
	private final ValueAdjusterInt startingLevelMin;
	private final ValueAdjusterInt startingLevelMax;
	
	private final Label roundsPerMinuteLabel;
	private final ValueAdjusterInt roundsPerMinuteAdjuster;
	
	private final Label minutesPerHourLabel;
	private final ValueAdjusterInt minutesPerHourAdjuster;
	
	private final Label hoursPerDayLabel;
	private final ValueAdjusterInt hoursPerDayAdjuster;
	
	private final Label daysPerMonthLabel;
	private final ValueAdjusterInt daysPerMonthAdjuster;
	
	public CampaignPropertiesEditor(Updateable updateable) {
		super("Campaign Properties and Locations");
		this.parent = updateable;
		
		content = new DialogLayout();
		content.setTheme("/locationeditorlayout");
		this.add(content);
		
		locationsLabel = new Label("Locations");
		
		locationsBox = new ReferenceListBox<WorldMapLocation>(Game.campaignEditor.getLocationsModel());
		locationsBox.addCallback(this);
		
		locationContent = new DialogLayout();
		locationContent.setTheme("/locationeditorlayout");
		
		saveButton = new Button("Save All");
		saveButton.addCallback(new Runnable() {
			@Override public void run() {
				saveSelected();
			}
		});
		
		newButton = new Button("New");
		newButton.addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.worldMapLocations.add(new WorldMapLocation("New Location", null, null, new Point(0, 0)));
				parent.update();
				update();
			}
		});
		
		deleteButton = new Button("Delete");
		deleteButton.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedLocation == null) return;
				
				DeleteLocationPopup popup = new DeleteLocationPopup(CampaignPropertiesEditor.this);
				popup.openPopupCentered();
			}
		});
		
		Group contentLeftH = content.createSequentialGroup(locationsLabel, newButton, deleteButton);
		Group contentLeftV = content.createParallelGroup(locationsLabel, newButton, deleteButton);
		
		Group leftH = content.createParallelGroup(saveButton);
		leftH.addGroup(contentLeftH);
		leftH.addWidget(locationsBox);
		Group leftV = content.createSequentialGroup(saveButton);
		leftV.addGroup(contentLeftV);
		leftV.addWidget(locationsBox);
		
		Group topH = content.createSequentialGroup(leftH);
		topH.addGap(20);
		topH.addWidget(locationContent);
		
		Group topV = content.createParallelGroup(leftV);
		topV.addWidget(locationContent);
		
		Group bottomLeftH = content.createParallelGroup();
		Group bottomLeftV = content.createSequentialGroup();
		
		showPopupButton = new Button("Show World Map Popup");
		showPopupButton.setTheme("/button");
		showPopupButton.addCallback(new Runnable() {
			@Override public void run() {
				WorldMapPopup popup = new WorldMapPopup(Game.campaignEditor, null);
				popup.setShowAllLocations(true);
				popup.openPopupCentered();
			}
		});
		
		bottomLeftH.addWidget(showPopupButton);
		bottomLeftV.addWidget(showPopupButton);
		
		campaignNameLabel = new Label("Campaign Name");
		campaignNameField = new EditField();
		campaignNameField.addCallback(new EditField.Callback() {
			@Override public void callback(int key) {
				Game.curCampaign.setName(campaignNameField.getText());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(campaignNameLabel, campaignNameField));
		bottomLeftV.addGroup(content.createParallelGroup(campaignNameLabel, campaignNameField));
		
		startAreaLabel = new Label("Start Area");
		startAreaBox = new ReferenceComboBox<Area>(Game.campaignEditor.getAreasModel());
		startAreaBox.addCallback(new Runnable() {
			@Override public void run() {
				int index = startAreaBox.getSelected();
				if (index == -1) return;
				
				Game.curCampaign.setStartArea(Game.campaignEditor.getAreasModel().getEntry(index).getID());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(startAreaLabel, startAreaBox));
		bottomLeftV.addGroup(content.createParallelGroup(startAreaLabel, startAreaBox));
		
		startingMerchantLabel = new Label("Starting Merchant");
		startingMerchantBox = new ReferenceComboBox<Merchant>(Game.campaignEditor.getMerchantsModel());
		startingMerchantBox.addCallback(new Runnable() {
			@Override public void run() {
				int index = startingMerchantBox.getSelected();
				if (index == -1) return;
				
				Game.curCampaign.setStartingMerchant(Game.campaignEditor.getMerchantsModel().getEntry(index).getID());
			}
		});
		
		clearStartingMerchant = new Button("[x]");
		clearStartingMerchant.setTooltipContent("Clear the starting merchant");
		clearStartingMerchant.addCallback(new Runnable() {
			@Override public void run() {
				startingMerchantBox.setSelected(-1);
				Game.curCampaign.setStartingMerchant(null);
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(startingMerchantLabel,
				startingMerchantBox, clearStartingMerchant));
		bottomLeftV.addGroup(content.createParallelGroup(startingMerchantLabel,
				startingMerchantBox, clearStartingMerchant));
		
		startingCharacterLabel = new Label("Starting Character");
		startingCharacterBox = new ReferenceComboBox<Creature>(Game.campaignEditor.getCreaturesModel());
		startingCharacterBox.addCallback(new Runnable() {
			@Override public void run() {
				int index = startingCharacterBox.getSelected();
				if (index == -1) return;
				
				Game.curCampaign.setStartingCharacter(Game.campaignEditor.getCreaturesModel().getEntry(index).getID());
			}
		});
		
		clearStartingCharacter = new Button("[x]");
		clearStartingCharacter.setTooltipContent("Clear the starting character");
		clearStartingCharacter.addCallback(new Runnable() {
			@Override public void run() {
				startingCharacterBox.setSelected(-1);
				Game.curCampaign.setStartingCharacter(null);
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(startingCharacterLabel,
				startingCharacterBox, clearStartingCharacter));
		bottomLeftV.addGroup(content.createParallelGroup(startingCharacterLabel,
				startingCharacterBox, clearStartingCharacter));
		
		stripStartingCharacters = new ToggleButton("Strip starting characters");
		stripStartingCharacters.setTheme("radiobutton");
		stripStartingCharacters.addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.setStripStartingCharacters(stripStartingCharacters.isActive());
			}
		});
		
		bottomLeftH.addWidget(stripStartingCharacters);
		bottomLeftV.addWidget(stripStartingCharacters);
		
		worldMapLabel = new Label("World Map Image:");
		worldMapField = new EditField();
		worldMapSetButton = new Button("Apply");
		worldMapSetButton.addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.setWorldMapImage(worldMapField.getText());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(worldMapLabel, worldMapField, worldMapSetButton));
		bottomLeftV.addGroup(content.createParallelGroup(worldMapLabel, worldMapField, worldMapSetButton));
		
		partySizeLabel = new Label("Party Size");
		partySizeToLabel = new Label(" to ");
		partyMinSize = new ValueAdjusterInt(new SimpleIntegerModel(1, 9, 1));
		partyMinSize.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.setMinPartySize(partyMinSize.getValue());
			}
		});
		partyMaxSize = new ValueAdjusterInt(new SimpleIntegerModel(1, 9, 1));
		partyMaxSize.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.setMaxPartySize(partyMaxSize.getValue());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(partySizeLabel, partyMinSize, partySizeToLabel, partyMaxSize));
		bottomLeftV.addGroup(content.createParallelGroup(partySizeLabel, partyMinSize, partySizeToLabel, partyMaxSize));
		
		startingLevelLabel = new Label("Starting Level");
		startingLevelToLabel = new Label(" to ");
		startingLevelMin = new ValueAdjusterInt(new SimpleIntegerModel(1, 99, 1));
		startingLevelMin.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.setMinStartingLevel(startingLevelMin.getValue());
			}
		});
		startingLevelMax = new ValueAdjusterInt(new SimpleIntegerModel(1, 99, 1));
		startingLevelMax.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.setMaxStartingLevel(startingLevelMax.getValue());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(startingLevelLabel, startingLevelMin, startingLevelToLabel, startingLevelMax));
		bottomLeftV.addGroup(content.createParallelGroup(startingLevelLabel, startingLevelMin, startingLevelToLabel, startingLevelMax));
		
		roundsPerMinuteLabel = new Label("Rounds per Minute");
		roundsPerMinuteAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		roundsPerMinuteAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.getDate().setRoundsPerMinute(roundsPerMinuteAdjuster.getValue());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(roundsPerMinuteLabel, roundsPerMinuteAdjuster));
		bottomLeftV.addGroup(content.createParallelGroup(roundsPerMinuteLabel, roundsPerMinuteAdjuster));
		
		minutesPerHourLabel = new Label("Minutes per Hour");
		minutesPerHourAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		minutesPerHourAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.getDate().setMinutesPerHour(minutesPerHourAdjuster.getValue());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(minutesPerHourLabel, minutesPerHourAdjuster));
		bottomLeftV.addGroup(content.createParallelGroup(minutesPerHourLabel, minutesPerHourAdjuster));
		
		hoursPerDayLabel = new Label("Hours per Day");
		hoursPerDayAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		hoursPerDayAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.getDate().setHoursPerDay(hoursPerDayAdjuster.getValue());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(hoursPerDayLabel, hoursPerDayAdjuster));
		bottomLeftV.addGroup(content.createParallelGroup(hoursPerDayLabel, hoursPerDayAdjuster));
		
		daysPerMonthLabel = new Label("Days per Month");
		daysPerMonthAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		daysPerMonthAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				Game.curCampaign.getDate().setDaysPerMonth(daysPerMonthAdjuster.getValue());
			}
		});
		
		bottomLeftH.addGroup(content.createSequentialGroup(daysPerMonthLabel, daysPerMonthAdjuster));
		bottomLeftV.addGroup(content.createParallelGroup(daysPerMonthLabel, daysPerMonthAdjuster));
		
		
		Group bottomH = content.createSequentialGroup(bottomLeftH);
		Group bottomV = content.createParallelGroup(bottomLeftV);
		
		worldMapContent = new WorldMapViewer();
		worldMapPane = new ScrollPane(worldMapContent);
		
		bottomH.addWidget(worldMapPane);
		bottomV.addWidget(worldMapPane);
		
		Group mainH = content.createParallelGroup(topH, bottomH);
		
		Group mainV = content.createSequentialGroup(topV);
		mainV.addGap(10);
		mainV.addGroup(bottomV);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
		
		Group locationTopV = locationContent.createParallelGroup();
		Group locationTopH = locationContent.createSequentialGroup();
		
		Group locationLeftV = locationContent.createSequentialGroup();
		Group locationLeftH = locationContent.createParallelGroup();
		
		nameLabel = new Label("Name");
		
		nameField = new EditField();
		nameField.addCallback(new EditField.Callback() {
			@Override public void callback(int arg0) {
				if (selectedLocation == null) return;
				
				selectedLocation.setName(nameField.getText());
			}
		});
		
		locationLeftV.addGap(10);
		locationLeftV.addGroup(locationContent.createParallelGroup(nameLabel, nameField));
		locationLeftH.addGroup(locationContent.createSequentialGroup(nameLabel, nameField));
		
		iconLabel = new Label("Icon");
		
		iconViewer = new SpriteViewer(75, 75, 1);
		iconViewer.setSelected(true);
		iconViewer.setIconOffset(false);
		
		setIconButton = new Button("Set");
		setIconButton.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(CampaignPropertiesEditor.this, "images/worldmap", 75, false, 1);
				popup.setCallback(CampaignPropertiesEditor.this);
				popup.openPopupCentered();
			}
		});
		
		locationLeftV.addGap(10);
		locationLeftV.addGroup(locationContent.createParallelGroup(iconLabel, iconViewer, setIconButton));
		locationLeftH.addGroup(locationContent.createSequentialGroup(iconLabel, iconViewer, setIconButton));
		
		startingTransitionLabel = new Label("Starting Transition");
		startingTransitionBox = new ReferenceComboBox<AreaTransition>(Game.campaignEditor.getTransitionsModel());
		startingTransitionBox.addCallback(new Runnable() {
			@Override public void run() {
				if (startingTransitionBox.getSelected() == -1) return;
				
				selectedLocation.setAreaTransition(
						Game.campaignEditor.getTransitionsModel().getEntry(startingTransitionBox.getSelected()).getID());
			}
		});
		
		locationLeftV.addGap(10);
		locationLeftV.addGroup(locationContent.createParallelGroup(startingTransitionLabel, startingTransitionBox));
		locationLeftH.addGroup(locationContent.createSequentialGroup(startingTransitionLabel, startingTransitionBox));
		
		positionLabel = new Label("World Map Position");
		xPositionAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 2000, 0));
		xPositionAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedLocation.getPosition().x = xPositionAdjuster.getValue();
			}
		});
		
		yPositionAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 2000, 0));
		yPositionAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedLocation.getPosition().y = yPositionAdjuster.getValue();
			}
		});
		
		locationLeftV.addGap(10);
		locationLeftV.addGroup(locationContent.createParallelGroup(positionLabel, xPositionAdjuster, yPositionAdjuster));
		locationLeftH.addGroup(locationContent.createSequentialGroup(positionLabel, xPositionAdjuster, yPositionAdjuster));
		
		travelTimeLabel = new Label("Travel Times");
		travelTimePaneContent = new DialogLayout();
		travelTimePaneContent.setTheme("content");
		travelTimePane = new ScrollPane(travelTimePaneContent);
		travelTimePaneContent.setHorizontalGroup(travelTimePaneContent.createParallelGroup());
		travelTimePaneContent.setVerticalGroup(travelTimePaneContent.createSequentialGroup());
		
		locationTopV.addGroup(locationLeftV);
		locationTopV.addGroup(locationContent.createSequentialGroup(travelTimeLabel, travelTimePane));
		locationTopH.addGroup(locationLeftH);
		locationTopH.addGroup(locationContent.createParallelGroup(travelTimeLabel, travelTimePane));
		
		locationContent.setHorizontalGroup(locationTopH);
		locationContent.setVerticalGroup(locationTopV);
	}
	
	@Override public void iconSelected(String icon) {
		if (selectedLocation == null) return;
		
		selectedLocation.setIcon(icon);
		iconViewer.setSprite(SpriteManager.getSprite(icon));
	}
	
	@Override public void saveSelected() {
		Game.curCampaign.writeCampaignFile();
		Game.campaignEditor.updateStatusText("Campaign properties saved.");
		parent.update();
		update();
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) update();
	}

	@Override public void callback(ListBox.CallbackReason reason) {
		switch (reason) {
		case KEYBOARD:
		case MOUSE_CLICK:
			int index = locationsBox.getSelected();
			if (index != -1) {
				selectedLocation = Game.curCampaign.worldMapLocations.get(index);
				update();
			}
		}
	}
	
	@Override public void update() {
		worldMapContent.updateLocations(Game.curCampaign.worldMapLocations);
		worldMapContent.updateSprite();
		
		campaignNameField.setText(Game.curCampaign.getName());
		
		startAreaBox.setSelected(-1);
		for (int i = 0; i < Game.campaignEditor.getAreasModel().getNumEntries(); i++) {
			if (Game.campaignEditor.getAreasModel().getEntry(i).getID().equals(Game.curCampaign.getStartArea())) {
				startAreaBox.setSelected(i);
				break;
			}
		}
		
		startingMerchantBox.setSelected(-1);
		for (int i = 0; i < Game.campaignEditor.getMerchantsModel().getNumEntries(); i++) {
			if (Game.campaignEditor.getMerchantsModel().getEntry(i).getID().equals(Game.curCampaign.getStartingMerchant())) {
				startingMerchantBox.setSelected(i);
				break;
			}
		}
		
		String startChar = Game.curCampaign.getStartingCharacter();
		startingCharacterBox.setSelected(-1);
		for (int i = 0; i < Game.campaignEditor.getCreaturesModel().getNumEntries(); i++) {
			if (Game.campaignEditor.getCreaturesModel().getEntry(i).getID().equals(startChar)) {
				startingCharacterBox.setSelected(i);
				break;
			}
		}
		
		stripStartingCharacters.setActive(Game.curCampaign.stripStartingCharacters());
		
		if (Game.curCampaign.getWorldMapImage() != null) {
			worldMapField.setText(Game.curCampaign.getWorldMapImage());
		}
		
		partyMinSize.setValue(Game.curCampaign.getMinPartySize());
		partyMaxSize.setValue(Game.curCampaign.getMaxPartySize());
		startingLevelMin.setValue(Game.curCampaign.getMinStartingLevel());
		startingLevelMax.setValue(Game.curCampaign.getMaxStartingLevel());
		roundsPerMinuteAdjuster.setValue(Game.curCampaign.getDate().ROUNDS_PER_MINUTE);
		minutesPerHourAdjuster.setValue(Game.curCampaign.getDate().MINUTES_PER_HOUR);
		hoursPerDayAdjuster.setValue(Game.curCampaign.getDate().HOURS_PER_DAY);
		daysPerMonthAdjuster.setValue(Game.curCampaign.getDate().DAYS_PER_MONTH);
		
		if (this.selectedLocation == null) {
			locationContent.setVisible(false);
			return;
		}
		
		locationContent.setVisible(true);
		
		nameField.setText(selectedLocation.getName());
		iconViewer.setSprite(SpriteManager.getSprite(selectedLocation.getIcon()));
		
		startingTransitionBox.setSelected(-1);
		for (int i = 0; i < Game.campaignEditor.getTransitionsModel().getNumEntries(); i++) {
			if (Game.campaignEditor.getTransitionsModel().getEntry(i).getID().equals(selectedLocation.getAreaTransition())) {
				startingTransitionBox.setSelected(i);
				break;
			}
		}
		
		xPositionAdjuster.setValue(selectedLocation.getPosition().x);
		yPositionAdjuster.setValue(selectedLocation.getPosition().y);
		
		Group travelTimeH = travelTimePaneContent.getHorizontalGroup();
		Group travelTimeV = travelTimePaneContent.getVerticalGroup();
		travelTimeH.clear(true);
		travelTimeV.clear(true);
		
		for (int i = 0; i < Game.campaignEditor.getLocationsModel().getNumEntries(); i++) {
			WorldMapLocation currentLocation = Game.curCampaign.worldMapLocations.get(i);
			
			if (currentLocation.getName().equals(selectedLocation.getName())) continue;
			
			TravelTimeSelector selector = new TravelTimeSelector(selectedLocation, currentLocation,
					selectedLocation.getTravelTime(currentLocation));
			
			travelTimeH.addWidget(selector);
			travelTimeV.addWidget(selector);
		}
	}
	
	private class DeleteLocationPopup extends PopupWindow implements Runnable {
		private final DialogLayout content;
		
		public DeleteLocationPopup(Widget parent) {
			super(parent);
			this.setTheme("");
			this.setCloseOnClickedOutside(false);
			
			content = new DialogLayout();
			content.setTheme("/filepopup");
			this.add(content);
			
			Label titleLabel = new Label("Delete Location " + selectedLocation.getName() + "?");
			titleLabel.setTheme("/titlelabel");
			
			Button accept = new Button("Yes");
			accept.addCallback(this);
			
			Button cancel = new Button("No");
			cancel.addCallback(new Runnable() {
				@Override public void run() {
					closePopup();
				}
			});
			
			Group mainH = content.createParallelGroup(titleLabel);
			Group mainV = content.createSequentialGroup(titleLabel);
			
			mainV.addGap(10);
			
			mainH.addGroup(content.createSequentialGroup(accept, cancel));
			mainV.addGroup(content.createParallelGroup(accept, cancel));
			
			content.setHorizontalGroup(mainH);
			content.setVerticalGroup(mainV);
		}
		
		@Override public void run() {
			Game.curCampaign.worldMapLocations.remove(selectedLocation);
			Game.curCampaign.writeCampaignFile();
			selectedLocation = null;
			parent.update();
			update();
			closePopup();
		}
	}
}
