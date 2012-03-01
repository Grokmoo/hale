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

import net.sf.hale.Game;
import net.sf.hale.editor.widgets.NewFilePopup;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tileset;

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class NewAreaPopup extends NewFilePopup {
	private final EditField idField;
	private final ToggleButton explored;
	private final ValueAdjusterInt width, height;
	
	private final ListBox<String> tileset;
	private final SimpleChangableListModel<String> tilesetsModel;
	
	public NewAreaPopup(Widget parent) {
		super(parent, "Create a new area", 100);
		
		idField = new EditField();
		
		explored = new ToggleButton("Explored");
		
		width = new ValueAdjusterInt();
		width.setMinMaxValue(5, 99);
		
		height = new ValueAdjusterInt();
		height.setMinMaxValue(5, 99);
		
		tilesetsModel = new SimpleChangableListModel<String>();
		
		for (String tileset : Game.curCampaign.getAllTilesetIDs()) {
			tilesetsModel.addElement(tileset);
		}
		
		tileset = new ListBox<String>(tilesetsModel);
		
		this.addWidgetsAsGroup(new Label("Name"), idField);
		this.addWidget(explored);
		this.addWidgetsAsGroup(new Label("Size"), width, new Label("by"), height);
		this.addWidgetsAsGroup(new Label("Select a tileset:"));
		this.addWidgetsAsGroup(tileset);
		this.addAcceptAndCancel();
	}
	
	@Override public String newFileAccept() {
		String name = idField.getText();
		
		if (name == null || name.length() == 0) {
			setError("Please enter a name.");
			return null;
		}
		
		File areaFile = new File(Game.campaignEditor.getPath() + "/areas/" + name + ".txt");
		if (areaFile.exists()) {
			setError("An area of that name already exists.");
			return null;
		}
		
		int index = tileset.getSelected();
		if (index == -1) {
			setError("Please select a tileset.");
			return null;
		}
		
		try {
			areaFile.createNewFile();

			BufferedWriter out = new BufferedWriter(new FileWriter(areaFile));
			
			out.write("size " + width.getValue() + " " + height.getValue());
			out.newLine();
			
			out.write("tileset " + tilesetsModel.getEntry(tileset.getSelected()));
			out.newLine();
			
			Tileset t = Game.curCampaign.getTileset( tilesetsModel.getEntry(tileset.getSelected()) );
			
			out.write("visibilityRadius ");
			out.write( Integer.toString(t.getDefaultVisibilityRadius()) );
			out.newLine();
			
			if (!explored.isActive()) {
				out.write("unexplored");
				out.newLine();
			}
			
			// fill in default terrain
			String terrainTypeID = t.getDefaultTerrainType();
			if (terrainTypeID != null) {
				TerrainType terrainType = t.getTerrainType(terrainTypeID);
				
				for (int x = 0; x < width.getValue(); x++) {
					for (int y = 0; y < height.getValue(); y++) {
						TerrainTile terrainTile = terrainType.getRandomTerrainTile();
						
						out.write("tile ");
						out.write(terrainTile.getLayerID());
						out.write(" ");
						out.write(Integer.toString(x));
						out.write(" ");
						out.write(Integer.toString(y));
						out.write(" ");
						out.write(terrainTile.getID());
						out.newLine();
					}
				}
			}
			
			out.close();
			
		} catch (Exception e) {
			setError("Invalid file name.");
			return null;
		}
		
		Game.campaignEditor.updateStatusText("New area: " + name + " created.");
		
		return "areas/" + name + ResourceType.Text.getExtension();
	}
}
