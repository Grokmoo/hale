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

package net.sf.hale.editor.widgets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import net.sf.hale.Game;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.util.FileUtil;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.MenuElement;
import de.matthiasmann.twl.MenuManager;
import de.matthiasmann.twl.Widget;

public class MenuNewCampaign extends MenuElement implements Runnable {
	private final Button create;
	private final EditField name;
	private final Label error;
	private final Label nameLabel;
	private MenuManager manager;
	private DialogLayout content;
	
	public MenuNewCampaign() {
		create = new Button("Create Campaign");
		create.addCallback(this);
		
		name = new EditField();
		name.setTheme("editfieldlarge");
		
		nameLabel = new Label("ID");
		
		error = new Label(" ");
		error.setTheme("/labelred");
		
		content = new DialogLayout();
		content.setTheme("");
	}

	@Override public void run() {
		String id = name.getText();
		
		if (id == null || id.length() == 0) {
			error.setText("Enter an ID.");
			return;
		}
		
		File dir = new File("campaigns/" + id);
		if (dir.exists()) {
			error.setText("A campaign with that ID already exists.");
			return;
		}
		
		if (!dir.mkdir()) {
			error.setText("Invalid ID.");
			return;
		}
		
		try {
			String path = dir.getPath();
			// create directory structure
			new File(path + "/areas").mkdir();
			new File(path + "/creatures").mkdir();
			new File(path + "/encounters").mkdir();
			new File(path + "/itemLists").mkdir();
			new File(path + "/items").mkdir();
			new File(path + "/merchants").mkdir();
			new File(path + "/recipes").mkdir();
			new File(path + "/scripts").mkdir();
			new File(path + "/scripts/ai").mkdir();
			new File(path + "/scripts/conversations").mkdir();
			new File(path + "/scripts/items").mkdir();
			new File(path + "/scripts/quests").mkdir();
			new File(path + "/scripts/triggers").mkdir();
			new File(path + "/transitions").mkdir();
			new File(path + "/triggers").mkdir();
			new File(path + "/cutscenes").mkdir();
			new File(path + "/images").mkdir();
			
			// create default description file
			new File(dir.getPath() + "/description.html").createNewFile();
			
			// copy over default items
			for (String itemID : ResourceManager.getCoreResourcesInDirectory("items")) {
				String fileContents = ResourceManager.getCoreResourceAsString(itemID);
				FileUtil.writeStringToFile(new File(dir.getPath() + "/" + itemID), fileContents);
			}
			
			// copy over default creatures
			for (String creatureID : ResourceManager.getCoreResourcesInDirectory("creatures")) {
				String fileContents = ResourceManager.getCoreResourceAsString(creatureID);
				FileUtil.writeStringToFile(new File(dir.getPath() + "/" + creatureID), fileContents);
			}
			
			// copy over default item scripts
			for (String scriptID : ResourceManager.getCoreResourcesInDirectory("scripts/items")) {
				String fileContents = ResourceManager.getCoreResourceAsString(scriptID);
				FileUtil.writeStringToFile(new File(dir.getPath() + "/" + scriptID), fileContents);
			}
			
			// copy over default AI scripts
			for (String scriptID : ResourceManager.getCoreResourcesInDirectory("scripts/ai")) {
				String fileContents = ResourceManager.getCoreResourceAsString(scriptID);
				FileUtil.writeStringToFile(new File(dir.getPath() + "/" + scriptID), fileContents);
			}
			
			// copy over default item lists
			for (String itemListID : ResourceManager.getCoreResourcesInDirectory("itemLists")) {
				String fileContents = ResourceManager.getCoreResourceAsString(itemListID);
				FileUtil.writeStringToFile(new File(dir.getPath() + "/" + itemListID), fileContents);
			}
			
			// copy over default recipes
			for (String recipeID : ResourceManager.getCoreResourcesInDirectory("recipes")) {
				String fileContents = ResourceManager.getCoreResourceAsString(recipeID);
				FileUtil.writeStringToFile(new File(dir.getPath() + "/" + recipeID), fileContents);
			}
			
			// write out default campaign file
			File campaignFile = new File(dir.getPath() + "/campaign.txt");
			campaignFile.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(campaignFile));
			out.write("startArea startArea");
			out.newLine();
			out.close();
			
			// create default starting area
			File startArea = new File(dir.getPath() + "/areas/startArea.txt");
			startArea.createNewFile();
			out = new BufferedWriter(new FileWriter(startArea));
			out.write("size 5 5"); out.newLine();
			out.write("tileset outdoor"); out.newLine();
			out.close();
			
		} catch (Exception e) {
			error.setText("Error creating directory structure.");
			return;
		}
		
		// load the newly created campaign
		Game.campaignEditor.openCampaign(name.getText());
		manager.getCloseCallback().run();
		name.setText("");
		error.setText(" ");
		Game.campaignEditor.update();
	}

	@Override protected Widget createMenuWidget(MenuManager manager, int level) {
		this.manager = manager;
		
		error.setText(" ");
		name.setText("");
		
		content.removeAllChildren();
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		
		Group topH = content.createSequentialGroup(nameLabel, name);
		Group topV = content.createParallelGroup(nameLabel, name);
		
		Group mainH = content.createParallelGroup(topH);
		Group mainV = content.createSequentialGroup(topV);
		mainH.addWidgets(create, error);
		mainV.addWidgets(create, error);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
		
		return content;
	}
}
