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

public class NewRecipePopup extends NewFilePopup {
	private final EditField idField;
	
	public NewRecipePopup(Widget parent) {
		super(parent, "Create a new recipe", 50);
		
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
		
		File recipeFile = new File(Game.campaignEditor.getPath() + "/recipes/" + name + ".txt");
		if (recipeFile.exists()) {
			setError("A recipe with that ID already exists.");
			return null;
		}
		
		try {
			recipeFile.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(recipeFile));
			
			for (int i = 0; i < Game.ruleset.getNumItemQualities(); i++) {
				out.write("qualityModifier 0");
				out.newLine();
			}
			
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			setError("Please enter a valid ID.");
			return null;
		}
		
		Game.campaignEditor.updateStatusText("Recipe " + name + " created.");
		
		return "recipes/" + name + ResourceType.Text.getExtension();
	}
}
