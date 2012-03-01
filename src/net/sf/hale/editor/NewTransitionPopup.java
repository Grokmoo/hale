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

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class NewTransitionPopup extends NewFilePopup {

	private final EditField idField;
	
	private final ToggleButton oneWay;
	private final ToggleButton twoWay;
	
	private final ListBox<String> fromAreasBox;
	private final SimpleChangableListModel<String> areasModel;
	
	private final ListBox<String> toAreasBox;
	
	public NewTransitionPopup(Widget parent) {
		super(parent, "Create a new Transition", 100);
		
		idField = new EditField();
		
		oneWay = new ToggleButton("One Way");
		oneWay.setActive(true);
		oneWay.setTheme("/togglebutton");
		oneWay.addCallback(new Runnable() {
			@Override public void run() {
				oneWay.setActive(true);
				twoWay.setActive(false);
			}
		});
		
		twoWay = new ToggleButton("Two Way");
		twoWay.setTheme("/togglebutton");
		twoWay.addCallback(new Runnable() {
			@Override public void run() {
				oneWay.setActive(false);
				twoWay.setActive(true);
			}
		});
		
		areasModel = new SimpleChangableListModel<String>();
		areasModel.addElement("World Map");
		for (int i = 0; i < Game.campaignEditor.getAreasModel().getNumEntries(); i++) {
			areasModel.addElement(Game.campaignEditor.getAreasModel().getEntry(i).getID());
		}
		
		fromAreasBox = new ListBox<String>(areasModel);
		
		toAreasBox = new ListBox<String>(areasModel);
		
		this.addWidgetsAsGroup(new Label("ID"), idField);
		this.addGap(10);
		this.addWidgetsAsGroup(oneWay, twoWay);
		this.addGap(10);
		this.addWidget(new Label("From Area"));
		this.addWidget(fromAreasBox);
		this.addGap(10);
		this.addWidget(new Label("To Area"));
		this.addWidget(toAreasBox);
		this.addAcceptAndCancel();
	}
	
	@Override public String newFileAccept() {
		String name = idField.getText();
		if (name == null || name.length() == 0) {
			setError("Please enter an ID.");
			return null;
		}
		
		File tranFile = new File(Game.campaignEditor.getPath() + "/transitions/" + name + ".txt");
		if (tranFile.exists()) {
			setError("A transition with that ID already exists.");
			return null;
		}
		
		int fromIndex = fromAreasBox.getSelected();
		int toIndex = toAreasBox.getSelected();
		
		if (fromIndex == -1) {
			setError("Please select an origin area.");
			return null;
		}
		
		if (toIndex == -1) {
			setError("Please select a destination area.");
			return null;
		}
		
		if (fromIndex == toIndex) {
			setError("The origin area and destination area must be different.");
			return null;
		}
		
		try {
			tranFile.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(tranFile));
			
			out.write("icon transition");
			out.newLine();

			out.write("twoWay " + twoWay.isActive());
			out.newLine();
			
			out.write("from \"" + areasModel.getEntry(fromIndex) + "\" 0 0");
			out.newLine();
			
			out.write("to \"" + areasModel.getEntry(toIndex) + "\" 0 0");
			out.newLine();
			
			for (int i = 0; i < Game.curCampaign.getMaxPartySize(); i++) {
				out.write("addToPosition 0 0");
				out.newLine();
			}
			
			if (twoWay.isActive()) {
				for (int i = 0; i < Game.curCampaign.getMaxPartySize(); i++) {
					out.write("addFromPosition 0 0");
					out.newLine();
				}
			}
			
			out.close();
			
		} catch (Exception e) {
			setError("Please enter a valid ID.");
			return null;
		}
		
		Game.campaignEditor.updateStatusText("Transition " + name + " created.");
		
		return "transitions/" + name + ResourceType.Text.getExtension();
	}
}
