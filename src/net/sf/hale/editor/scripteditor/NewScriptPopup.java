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

package net.sf.hale.editor.scripteditor;

import java.io.File;

import net.sf.hale.Game;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileUtil;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * A window that pops up when the user clicks the button to create a new Script object
 * or copy an existing script object.
 * 
 * The type of script will be determined based on the user's current filter choices.
 * 
 * For a new script, the user can either enter a name and create the new script or cancel.
 * For a script copy, the process is very similar but the source that is being copied from is
 * also shown.
 * 
 * @author Jared Stephen
 *
 */

public class NewScriptPopup extends PopupWindow implements Runnable {
	private final DialogLayout content;
	private final String copyPath;
	
	private final EditField nameField;
	private final Label error;
	
	private final String newFilePathPrefix;
	
	/**
	 * Create a PopupWindow with the needed elements for creating a new script or copying an existing script.
	 * This includes an Accept button, Cancel button, and a Label for the copy source in the case of a copy
	 * and an EditField for the new name in the case of a new script or copy script.
	 * 
	 * @param parent the Widget to create this popup over.  Input will be blocked from this Widget's root.
	 * @param type The Type of Script that this Popup will create.  This will be shown as the prefix of the
	 * filename being created in the Popup.
	 * @param copyPath The filename of the script to copy from.  If this is null, the PopupWindow will
	 * show for creating a new Script.
	 */
	
	public NewScriptPopup(Widget parent, ScriptEditor.Type type, String copyPath) {
		super(parent);
		
		this.copyPath = copyPath;
		
		this.setTheme("");
		this.setCloseOnClickedOutside(false);
		
		content = new DialogLayout();
		content.setTheme("/filepopup");
		this.add(content);
		
		Label titleLabel = new Label();
		titleLabel.setTheme("/labelblack");
		if (copyPath != null) {
			titleLabel.setText("Copy script from " + copyPath);
		} else {
			titleLabel.setText("Creating new script of type " + type);
		}
		
		Label nameLabel = new Label("Enter a name:");
		
		Label nameTypeLabel = new Label();
		switch (type) {
		case Conversation: newFilePathPrefix = "conversations/"; break;
		case Quest: newFilePathPrefix = "quests/"; break;
		case Item: newFilePathPrefix = "items/"; break;
		case Trigger: newFilePathPrefix = "triggers/"; break;
		case AI: newFilePathPrefix = "ai/"; break;
		default: newFilePathPrefix = null;
		}
		
		nameTypeLabel.setText(newFilePathPrefix);
		
		Label nameJSLabel = new Label(".js");
		
		nameField = new EditField();
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addCallback(new Runnable() {
			@Override public void run() {
				NewScriptPopup.this.closePopup();
			}
		});
		
		Button okButton = new Button();
		if (copyPath != null) {
			okButton.setText("Copy");
		} else {
			okButton.setText("Create");
		}
		okButton.addCallback(this);
		
		error = new Label(" ");
		error.setTheme("/labelred");
		
		Group mainH = content.createParallelGroup();
		Group mainV = content.createSequentialGroup();
		
		mainH.addWidget(titleLabel);
		mainV.addWidget(titleLabel);
		
		mainV.addGap(30);
		
		mainH.addWidget(nameLabel);
		mainV.addWidget(nameLabel);
		
		mainH.addGroup(content.createSequentialGroup(nameTypeLabel, nameField, nameJSLabel));
		mainV.addGroup(content.createParallelGroup(nameTypeLabel, nameField, nameJSLabel));
		
		mainV.addGap(10);
		
		mainH.addGroup(content.createSequentialGroup(okButton, cancelButton));
		mainV.addGroup(content.createParallelGroup(okButton, cancelButton));
		
		mainH.addWidget(error);
		mainV.addWidget(error);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	/**
	 * This method is called whenever the user clicks the "Accept" button.  It will validate the specified name
	 * of the script to be created.  If the name is invalid in any way, an error will be shown in the Popup
	 * and the script will not be created.  If the name is valid, a new script file will be created and this
	 * NewScriptPopup will close. 
	 */
	
	@Override public void run() {
		String name = nameField.getText();
		if (name == null || name.length() == 0) {
			error.setText("Please enter a name.");
			return;
		}
		
		String newFilePath = Game.campaignEditor.getPath() + "/scripts/" + newFilePathPrefix + name + ".js";
		File newFile = new File(newFilePath);
		if (newFile.exists()) {
			error.setText("A script of that name already exists.");
			return;
		}
		
		try {
			if (copyPath != null) {
				FileUtil.copyFile(new File(copyPath), newFile);
			} else {
				newFile.createNewFile();
			}
			
		} catch (Exception e) {
			error.setText("Please enter a valid filename.");
			return;
		}
		
		NewScriptPopup.this.closePopup();
		ResourceManager.addCampaignResource("scripts/" + newFilePathPrefix + name + ResourceType.JavaScript.getExtension());
		Game.campaignEditor.updateStatusText("Script file " + newFilePathPrefix + name + " created.");
		Game.campaignEditor.updateScripts();
	}
}
