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

public class CopyFilePopup extends PopupWindow implements Runnable {
	private PopupCallback callback;
	
	private final DialogLayout content;
	
	private final String directory;
	private final String source;
	
	private final EditField idField;
	
	private final Label error;
	
	public CopyFilePopup(Widget parent, String directory, String source) {
		super(parent);
		
		this.setTheme("");
		this.setCloseOnClickedOutside(false);
		
		content = new DialogLayout();
		content.setTheme("/filepopup");
		this.add(content);
		
		this.directory = directory;
		this.source = source;
		
		Label title = new Label("Copy " + directory + "/" + source + ".txt to ...");
		
		Label idLabel = new Label("ID");
		
		idField = new EditField();
		
		Button accept = new Button("Copy");
		accept.addCallback(this);
		
		Button cancel = new Button("Cancel");
		cancel.addCallback(new Runnable() {
			@Override public void run() {
				CopyFilePopup.this.closePopup();
			}
		});
		
		error = new Label(" ");
		error.setTheme("/labelred");
		
		Group mainH = content.createParallelGroup(title);
		Group mainV = content.createSequentialGroup(title);
		
		mainV.addGap(10);
		
		mainH.addGroup(content.createSequentialGroup(idLabel, idField));
		mainV.addGroup(content.createParallelGroup(idLabel, idField));
		
		mainV.addGap(10);
		
		mainH.addGroup(content.createSequentialGroup(accept, cancel));
		mainV.addGroup(content.createParallelGroup(accept, cancel));
		
		mainH.addWidget(error);
		mainV.addWidget(error);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	// copy accept callback
	@Override public void run() {
		String name = idField.getText();
		if (name == null || name.length() == 0) {
			error.setText("Please enter an ID.");
			return;
		}
		
		File fCopy = new File(Game.campaignEditor.getPath() + "/" + directory + "/" + name + ".txt");
		if (fCopy.exists()) {
			error.setText("That ID is already in use.");
			return;
		}
		
		File fOriginal = new File(Game.campaignEditor.getPath() + "/" + directory + "/" + source + ".txt");
		
		try {
			FileUtil.copyFile(fOriginal, fCopy);
			ResourceManager.addCampaignResource(directory + "/" + name + ResourceType.Text.getExtension());
			
		} catch (Exception e) {
			error.setText("Invalid ID.");
			return;
		}
		
		this.closePopup();
		
		if (callback != null) {
			callback.copyComplete();
		}
	}
	
	public void setCallback(PopupCallback callback) {
		this.callback = callback;
	}
}
