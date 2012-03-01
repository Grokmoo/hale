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
import net.sf.hale.entity.Entity;
import net.sf.hale.resource.ResourceType;

import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class NewItemPopup extends NewFilePopup {
	private final EditField idField;
	private final SimpleChangableListModel<Entity.Type> typeModel;
	private final ComboBox<Entity.Type> typeBox;
	
	public NewItemPopup(Widget parent) {
		super(parent, "Create a new Item", 50);
		
		idField = new EditField();
		
		typeModel = new SimpleChangableListModel<Entity.Type>();
		typeModel.addElements(Entity.Type.ITEM, Entity.Type.CONTAINER, Entity.Type.DOOR, Entity.Type.TRAP);
		typeBox = new ComboBox<Entity.Type>(typeModel);
		
		this.addWidgetsAsGroup(new Label("ID"), idField);
		this.addWidgetsAsGroup(new Label("Type"), typeBox);
		this.addAcceptAndCancel();
	}
	
	@Override public String newFileAccept() {
		String name = idField.getText();
		if (name == null || name.length() == 0) {
			setError("Please enter an ID.");
			return null;
		}
		
		int typeIndex = typeBox.getSelected();
		if (typeIndex == -1) {
			setError("Please select a type.");
			return null;
		}
		
		File f = new File(Game.campaignEditor.getPath() + "/items/" + name + ".txt");
		if (f.exists()) {
			setError("An item of this ID already exists.");
			return null;
		}
		
		File f2 = new File(Game.campaignEditor.getPath() + "/creatures/" + name + ".txt");
		if (f2.exists()) {
			setError("Another entity of this ID already exists.");
			return null;
		}
		
		try {
			f.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			
			Entity.Type type = typeModel.getEntry(typeIndex);
			
			out.write(type.toString() + " true");
			out.newLine();	
			out.close();
			
		} catch (Exception e) {
			setError("Invalid ID.");
			return null;
		}
		
		Game.campaignEditor.updateStatusText("New item " + name + " created.");
		
		return "items/" + name + ResourceType.Text.getExtension();
	}
}
