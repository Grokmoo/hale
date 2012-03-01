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
import de.matthiasmann.twl.Widget;

public class NewTriggerPopup extends NewFilePopup {
	private final EditField idField;
	
	public NewTriggerPopup(Widget parent) {
		super(parent, "Create a new Trigger", 50);
		
		idField = new EditField();
		
		this.addWidgetsAsGroup(new Label("ID"), idField);
		this.addAcceptAndCancel();
	}
	
	@Override
	public String newFileAccept() {
		String name = idField.getText();
		if (name == null || name.length() == 0) {
			setError("Please enter an ID.");
			return null;
		}
		
		File trigFile = new File(Game.campaignEditor.getPath() + "/triggers/" + name + ".txt");
		if (trigFile.exists()) {
			setError("A trigger with that ID already exists.");
			return null;
		}
		
		try {
			trigFile.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(trigFile));
			
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			setError("Please enter a valid ID.");
			return null;
		}
		
		Game.campaignEditor.updateStatusText("Trigger " + name + " updated.");
		
		return "triggers/" + name + ResourceType.Text.getExtension();
	}
}
