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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Area;
import net.sf.hale.AreaTransition;
import net.sf.hale.AreaTrigger;
import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.widgets.StartLocationTab;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class AreaEditor extends Widget implements Updateable, Runnable {
	
	public enum Mode {
		Terrain, Features, Elevation, Items, Encounters, Transitions, Triggers, Visibility, Passability, Tiles, StartLocations
	};
	
	private final SimpleChangableListModel<AreaTransition> transitionsModel;
	private final SimpleChangableListModel<AreaTrigger> triggersModel;
	
	private final Widget rightPane;
	
	private final AreaEditViewer areaEditViewer;
	private final AreaEditListener areaEditListener;
	private final Area area;
	
	private final String path;
	
	private final Label nameLabel;
	
	private final Label widthLabel, heightLabel;
	private final ValueAdjusterInt widthAdjuster;
	private final ValueAdjusterInt heightAdjuster;
	
	private final Label tilesetLabel;
	private final ToggleButton exploredButton;
	private final Label visibilityRadiusLabel;
	private final ValueAdjusterInt visibilityRadiusAdjuster;
	private final Label activeModeLabel;
	
	private final TilePalette tilePaletteTab;
	
	private final ValueAdjusterInt brushSize;
	private final Label brushLabel;
	
	private final TerrainPalette terrainPaletteTab;
	private final FeaturePalette featurePaletteTab;
	private final StartLocationTab startLocationTab;
	private final ElevationPalette elevationPalette;
	
	private final Widget itemsTab;
	private final ItemListBox itemsBox;
	
	private final Widget transitionsTab;
	private final Label transitionsLabel;
	private final ListBox<AreaTransition> transitionsBox;
	
	private final Widget encountersTab;
	private final Label encountersLabel;
	private final ListBox<Encounter> encountersBox;
	
	private final Widget triggersTab;
	private final Label triggersLabel;
	private final ListBox<AreaTrigger> triggersBox;
	
	private final Label mousePosition;
	
	private final EncounterEditor encounterEditor;
	
	public AreaEditor(Area area, String areaPath, Point position) {
		setFocusKeyEnabled(false);
		
		this.setTheme("");
        this.setPosition(position.x, position.y);
        this.setSize(Game.config.getEditorResolutionX() - position.x, Game.config.getEditorResolutionY() - position.y);
        
		this.area = area;
        this.path = areaPath;
        
        areaEditViewer = new AreaEditViewer(this, area);
        areaEditViewer.setTheme("");
        areaEditViewer.setPosition(0, 30);
        areaEditViewer.setSize(this.getInnerWidth() - 250, this.getInnerHeight() - 30);
		areaEditListener = new AreaEditListener(this, area, areaEditViewer);
		areaEditViewer.setListener(areaEditListener);
		areaEditViewer.setArea(area);
        
		rightPane = new Widget();
		rightPane.setTheme("/button");
		rightPane.setSize(252, this.getInnerHeight());
		rightPane.setPosition(this.getInnerWidth() - 252, 0);
		
		this.add(areaEditViewer);
		
		this.add(rightPane);
		
		nameLabel = new Label(area.getName());
		nameLabel.setSize(100, 20);
		nameLabel.setPosition(this.getInnerWidth() - 250, 5);
		nameLabel.setTheme("/labelbigblack");
		
		widthLabel = new Label("Width");
		widthLabel.setPosition(this.getInnerWidth() - 250, 60);
		widthLabel.setTheme("/labelblack");
		
		widthAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(5, 99, area.getWidth()));
		widthAdjuster.setSize(60, 20);
		widthAdjuster.setPosition(this.getInnerWidth() - 200, 50);
		widthAdjuster.setTheme("/valueadjuster");
		widthAdjuster.getModel().addCallback(this);
		
		heightLabel = new Label("Height");
		heightLabel.setPosition(this.getInnerWidth() - 120, 60);
		heightLabel.setTheme("/labelblack");
		
		heightAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(5, 99, area.getHeight()));
		heightAdjuster.setSize(60, 20);
		heightAdjuster.setPosition(this.getInnerWidth() - 70, 50);
		heightAdjuster.setTheme("/valueadjuster");
		heightAdjuster.getModel().addCallback(this);
		
		exploredButton = new ToggleButton("Explored");
		exploredButton.setSize(70, 20);
		exploredButton.setPosition(this.getInnerWidth() - 250, 70);
		exploredButton.setTheme("/radiobutton");
		exploredButton.setActive(!this.area.unexplored());
		exploredButton.addCallback(new Runnable() {
			@Override
			public void run() {
				AreaEditor.this.area.setUnexplored(!exploredButton.isActive());
			}
		});
		
		tilesetLabel = new Label("Tileset: " + area.getTileset());
		tilesetLabel.setSize(100, 20);
		tilesetLabel.setPosition(this.getInnerWidth() - 250, 30);
		tilesetLabel.setTheme("/labelblack");
		
		visibilityRadiusLabel = new Label("Sight Radius");
		visibilityRadiusLabel.setPosition(this.getInnerWidth() - 140, 80);
		visibilityRadiusLabel.setTheme("/labelblack");
		this.add(visibilityRadiusLabel);
		
		visibilityRadiusAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 24, 1));
		visibilityRadiusAdjuster.setValue(area.getVisibilityRadius());
		visibilityRadiusAdjuster.setPosition(this.getInnerWidth() - 60, 70);
		visibilityRadiusAdjuster.setSize(55, 20);
		visibilityRadiusAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				AreaEditor.this.area.setVisibilityRadius(visibilityRadiusAdjuster.getValue());
			}
		});
		this.add(visibilityRadiusAdjuster);
		
		activeModeLabel = new Label("Active Mode: None");
		activeModeLabel.setTheme("/labelblack");
		activeModeLabel.setPosition(this.getInnerWidth() - 250, 100);
		 
		tilePaletteTab = new TilePalette(area);
		tilePaletteTab.setPosition(getInnerWidth() - 250, 110);
		tilePaletteTab.setSize(250, getInnerHeight() - 120);
		
		startLocationTab = new StartLocationTab(area);
		startLocationTab.setPosition(this.getInnerWidth() - 250, 150);
		startLocationTab.setSize(250, this.getInnerHeight() - 220);
		
		brushLabel = new Label("Brush Size");
		brushLabel.setTheme("/labelblack");
		brushLabel.setPosition(getInnerWidth() - 100, getInnerHeight() - 40);
		this.add(brushLabel);
		
		brushSize = new ValueAdjusterInt(new SimpleIntegerModel(0, 9, 0));
		brushSize.setPosition(getInnerWidth() - 100, getInnerHeight() - 30);
		brushSize.setSize(100, 20);
		brushSize.getModel().addCallback(new Runnable() {
			@Override public void run() {
				update();
			}
		});
		brushLabel.setLabelFor(brushSize);
		
		itemsTab = new Widget();
		itemsTab.setPosition(this.getInnerWidth() - 250, 110);
		itemsTab.setSize(250, this.getInnerHeight() - 110);
		itemsTab.setTheme("");
		
		itemsBox = new ItemListBox(Game.campaignEditor.getItemsModel(), "All Items");
		itemsBox.setPosition(0, 10);
		itemsBox.setSize(240, this.getInnerHeight() - 130);
		itemsTab.add(itemsBox);
		
		transitionsTab = new Widget();
		transitionsTab.setPosition(this.getInnerWidth() - 250, 110);
		transitionsTab.setSize(250, this.getInnerHeight() - 110);
		transitionsTab.setTheme("");
		
		transitionsLabel = new Label("Area Transitions for this Area:");
		transitionsLabel.setTheme("/titlelabel");
		transitionsLabel.setPosition(0, 35);
		transitionsTab.add(transitionsLabel);
		
		transitionsModel = new SimpleChangableListModel<AreaTransition>();
		transitionsBox = new ListBox<AreaTransition>(transitionsModel);
		transitionsBox.setPosition(0, 50);
		transitionsBox.setTheme("/listbox");
		transitionsBox.setSize(240, this.getInnerHeight() - 170);
		transitionsTab.add(transitionsBox);
		
		encountersTab = new Widget();
		encountersTab.setPosition(this.getInnerWidth() - 250, 110);
		encountersTab.setSize(250, this.getInnerHeight() - 110);
		encountersTab.setTheme("");
		
		encountersLabel = new Label("All Encounters:");
		encountersLabel.setTheme("/titlelabel");
		encountersLabel.setPosition(0, 35);
		encountersTab.add(encountersLabel);
		
		encountersBox = new ReferenceListBox<Encounter>(Game.campaignEditor.getEncountersModel());
		encountersBox.setPosition(0, 50);
		encountersBox.setTheme("/listbox");
		encountersBox.setSize(240, this.getInnerHeight() - 170);
		encountersTab.add(encountersBox);
		
		triggersTab = new Widget();
		triggersTab.setPosition(this.getInnerWidth() - 250, 110);
		triggersTab.setSize(250, this.getInnerHeight() - 110);
		triggersTab.setTheme("");
		
		triggersLabel = new Label("Triggers for this Area:");
		triggersLabel.setTheme("/titlelabel");
		triggersLabel.setPosition(0, 35);
		triggersTab.add(triggersLabel);
		
		triggersModel = new SimpleChangableListModel<AreaTrigger>();
		triggersBox = new ListBox<AreaTrigger>(triggersModel);
		triggersBox.setPosition(0, 50);
		triggersBox.setTheme("/listbox");
		triggersBox.setSize(240, this.getInnerHeight() - 170);
		triggersTab.add(triggersBox);
		
		terrainPaletteTab = new TerrainPalette(area);
		terrainPaletteTab.setPosition(this.getInnerWidth() - 250, 110);
		terrainPaletteTab.setSize(250, this.getInnerHeight() - 160);
		
		featurePaletteTab = new FeaturePalette(this.area, terrainPaletteTab);
		featurePaletteTab.setPosition(this.getInnerWidth() - 250, 110);
		featurePaletteTab.setSize(250, this.getInnerHeight() - 160);
		
		elevationPalette = new ElevationPalette(this.area, terrainPaletteTab, featurePaletteTab);
		elevationPalette.setPosition(this.getInnerWidth() - 250, 110);
		elevationPalette.setSize(250, this.getInnerHeight() - 160);
		
		terrainPaletteTab.setFeaturePalette(featurePaletteTab);
		terrainPaletteTab.setElevationPalette(elevationPalette);
		
		mousePosition = new Label("(0, 0)");
		mousePosition.setTheme("/labelblack");
		mousePosition.setPosition(this.getInnerWidth() - 70, 35);
		
		encounterEditor = new EncounterEditor(this);
		encounterEditor.setVisible(false);
		
		this.add(nameLabel);
		this.add(widthLabel);
		this.add(widthAdjuster);
		this.add(heightLabel);
		this.add(heightAdjuster);
		this.add(tilesetLabel);
		this.add(exploredButton);
		this.add(activeModeLabel);
		
		this.add(startLocationTab);
		this.add(featurePaletteTab);
		this.add(terrainPaletteTab);
		this.add(tilePaletteTab);
		this.add(elevationPalette);
		this.add(itemsTab);
		this.add(encountersTab);
		this.add(transitionsTab);
		this.add(triggersTab);
		
		this.add(mousePosition);
		
		this.add(brushSize);
		
		setMode(Game.campaignEditor.getMode());
	}
	
	public void setMode(Mode mode) {
		disableAllModes();
		
		if (mode == null) return;
		
		switch (mode) {
		case Items:
			itemsTab.setVisible(true);
			brushSize.setValue(0);
			itemsBox.resetView();
			break;
		case Transitions:
			transitionsTab.setVisible(true);
			brushSize.setValue(0);
			break;
		case Encounters:
			encountersTab.setVisible(true);
			brushSize.setValue(0);
			break;
		case Tiles:
			tilePaletteTab.setVisible(true);
			brushLabel.setVisible(true);
			brushSize.setVisible(true);
			break;
		case Visibility:
			brushLabel.setVisible(true);
			brushSize.setVisible(true);
			break;
		case Passability:
			brushLabel.setVisible(true);
			brushSize.setVisible(true);
			break;
		case Triggers:
			triggersTab.setVisible(true);
			break;
		case Terrain:
			terrainPaletteTab.setVisible(true);
			brushLabel.setVisible(true);
			brushSize.setVisible(true);
			break;
		case Features:
			featurePaletteTab.setVisible(true);
			brushLabel.setVisible(true);
			brushSize.setVisible(true);
			break;
		case Elevation:
			elevationPalette.setVisible(true);
			brushLabel.setVisible(true);
			brushSize.setVisible(true);
			break;
		case StartLocations:
			startLocationTab.setVisible(true);
		}
		
		activeModeLabel.setText("Active Mode: " + mode.toString());
	}
	
	public Area getArea() { return area; }
	
	public void setStartLocation(int index, Point location) {
		startLocationTab.setStartLocation(index, location);
	}
	
	public int getSelectedStartLocationIndex() {
		return startLocationTab.getSelectedIndex();
	}
	
	public TilePalette getTilePalette() { return tilePaletteTab; }
	public FeaturePalette getFeaturePalette() { return featurePaletteTab; }
	public TerrainPalette getTerrainPalette() { return terrainPaletteTab; }
	public ElevationPalette getElevationPalette() { return elevationPalette; }
	
	public String getAreaPath() { return path; }
	
	public void updateMousePosition(Point p) {
		mousePosition.setText("(" + p.x + ", " + p.y + ")");
	}
	
	// callback for width adjuster, height adjuster
	@Override public void run() {
		this.area.resize(widthAdjuster.getValue(), heightAdjuster.getValue());
		this.terrainPaletteTab.resize();
		this.featurePaletteTab.resize();
		this.elevationPalette.resize();
		this.areaEditViewer.setMaxScroll();
	}
	
	@Override public void update() {
		
	}
	
	public void reloadTriggersAndTransitions() {
		triggersModel.clear();
		transitionsModel.clear();
		
		for (int i = 0; i < Game.campaignEditor.getTriggersModel().getNumEntries(); i++) {
			AreaTrigger trigger = Game.campaignEditor.getTriggersModel().getEntry(i);
			if (trigger.getArea() != null && trigger.getArea().equals(this.area.getName())) {
				triggersModel.addElement(trigger);
			}
		}
		
		for (int i = 0; i < Game.campaignEditor.getTransitionsModel().getNumEntries(); i++) {
			AreaTransition transition = Game.campaignEditor.getTransitionsModel().getEntry(i);
			if (transition.getAreaFrom().equals(area.getName()) || transition.getAreaTo().equals(area.getName())) {
				transitionsModel.addElement(transition);
			}
		}
	}
	
	public void reloadEncounters() {
		Game.curCampaign.loadEncounters();
		
		List<String> refs = new ArrayList<String>();
		List<Point> points = new ArrayList<Point>();
		
		for (int i = 0; i < area.getEncounters().size(); i++) {
			refs.add(area.getEncounters().get(i).getName());
			points.add(new Point(area.getEncounterPositions().get(i)));
		}
		
		area.getEncounters().clear();
		area.getEncounterPositions().clear();
		
		for (int i = 0; i < refs.size(); i++) {
			Encounter e = Game.curCampaign.getEncounter(refs.get(i));
			e.addToArea(area, points.get(i).x, points.get(i).y);
		}
		
		Encounter.removeCreaturesFromArea(area);
	}
	
	public void saveArea() {
		for (String s : AreaEditor.this.area.getTriggers()) {
			Game.curCampaign.getTrigger(s).saveToFile();
		}
		
		for (String s : AreaEditor.this.area.getTransitions()) {
			Game.curCampaign.getAreaTransition(s).saveToFile();
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(Game.campaignEditor.getPath() + "/areas/" + path + ".txt"));
			
			out.write("size " + area.getWidth() + " " + area.getHeight());
			out.newLine();
			out.write("tileset " + area.getTileset());
			out.newLine();
			
			out.write("visibilityRadius " + area.getVisibilityRadius());
			out.newLine();
			
			if (area.unexplored()) {
				out.write("unexplored");
				out.newLine();
			}
			out.newLine();
			
			for (Point s : area.getStartLocations()) {
				out.write("addStartLocation " + s.x + " " + s.y);
				out.newLine();
			}
			out.newLine();
			
			for (int i = 0; i < area.getEncounters().size(); i++) {
				Encounter e = area.getEncounters().get(i);
				Point p = area.getEncounterPositions().get(i);
				out.write("encounter \"" + e.getName() + "\" " + p.x + " " + p.y);
				out.newLine();
			}
			out.newLine();
			
			for (Entity e : area.getEntities()) {
				switch (e.getType()) {
				case CONTAINER:
					Container co = (Container) e;
					if (!co.getDescription().equals(Game.ruleset.getString("TempContainerDescription"))) {
						out.write("item 1 \"" + co.getID() + "\" " + co.getX() + " " + co.getY());
					}
					
					for (int i = 0; i < co.size(); i++) {
						out.newLine();
						out.write("item " + co.getQuantity(i) + " \"" + co.getItem(i).getID() + "\" " + co.getX() + " " + co.getY());
					}
					
					out.newLine();
					break;
				case DOOR:
					out.write("item 1 \"" + e.getID() + "\" " + e.getX() + " " + e.getY());
					out.newLine();
					break;
				case TRAP:
					out.write("item 1 \"" + e.getID() + "\" " + e.getX() + " " + e.getY());
					out.newLine();
					break;
				case CREATURE:
					Creature c = (Creature) e;
					if (c.getEncounter() == null) {
						out.write("creature \"" + c.getID() + "\" " + c.getX() + " " + c.getY() + " " + c.getFaction().getName());
						out.newLine();
					}
				}
			}
			out.newLine();
			
			area.getTileGrid().write(out);
			out.newLine();
			
			area.getElevationGrid().write(out);
			out.newLine();
			
			boolean[][] data = area.getTransparency();
			out.newLine();
			out.write("opaque");
			for (int x = 0; x < area.getWidth(); x++) {
				for (int y = 0; y < area.getHeight(); y++) {
					if (!data[x][y]) out.write("  " + x + " " + y);
				}
			}
			out.newLine();
			
			data = area.getPassability();
			out.write("impass");
			for (int x = 0; x < area.getWidth(); x++) {
				for (int y = 0; y < area.getHeight(); y++) {
					if (!data[x][y]) out.write("  " + x + " " + y);
				}
			}
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving area " + area.getName(), e);
			
			Game.campaignEditor.updateStatusText("Error saving area: " + area.getName() + "!");
			return;
		}
		
		for (int i = 0; i < triggersModel.getNumEntries(); i++) {
			triggersModel.getEntry(i).saveToFile();
		}
		
		for (int i = 0; i < transitionsModel.getNumEntries(); i++) {
			transitionsModel.getEntry(i).saveToFile();
		}
		
		Game.campaignEditor.updateStatusText("Area: " + area.getName() + " saved successfully.");
	}
	
	public Mode getMode() {
		return Game.campaignEditor.getMode();
	}
	
	public void disableAllModes() {
		brushSize.setVisible(false);
		brushLabel.setVisible(false);
		
		featurePaletteTab.setVisible(false);
		terrainPaletteTab.setVisible(false);
		tilePaletteTab.setVisible(false);
		elevationPalette.setVisible(false);
		itemsTab.setVisible(false);
		encountersTab.setVisible(false);
		transitionsTab.setVisible(false);
		triggersTab.setVisible(false);
		startLocationTab.setVisible(false);
	}
	
	public void removeTransition(AreaTransition transition) {
		area.getTriggers().remove(transition.getID());
	}
	
	public void deleteEncounter(Encounter encounter) {
		for (int i = 0; i < area.getEncounters().size(); i++) {
			if (area.getEncounters().get(i).getName().equals(encounter.getName())) {
				area.getEncounters().remove(i);
				area.getEncounterPositions().remove(i);
				i--;
			}
		}
	}
	
	public int getBrushRadius() { return brushSize.getValue(); }
	
	public AreaTrigger getSelectedTrigger() {
		int index = triggersBox.getSelected();
		
		if (index == -1) return null;
		
		return triggersModel.getEntry(index);
	}
	
	public Encounter getSelectedEncounter() {
		int index = encountersBox.getSelected();
		
		if (index == -1) return null;
		
		return Game.campaignEditor.getEncountersModel().getEntry(index);
	}
	
	public Item getSelectedItem() {
		int index = itemsBox.getSelected();
		if (index == -1) return null;
		
		return Game.campaignEditor.getItemsModel().getEntry(index);
	}
	
	public AreaTransition getSelectedTransition() {
		int index = transitionsBox.getSelected();
		if (index == -1) return null;
		
		return transitionsModel.getEntry(index);
	}
}
