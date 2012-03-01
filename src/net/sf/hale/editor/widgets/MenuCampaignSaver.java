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
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.MenuElement;
import de.matthiasmann.twl.MenuManager;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class MenuCampaignSaver extends MenuElement implements Runnable, CallbackWithReason<ListBox.CallbackReason> {
	private DialogLayout content;
	private MenuManager manager;
	
	private ListBox<String> campaignsBox;
	private SimpleChangableListModel<String> campaignsModel;
	
	private final Label warning;
	private final Button save;
	private final Label error;
	private final Button overwrite;
	
	private boolean forceWrite = false;
	
	public MenuCampaignSaver() {
		warning = new Label("Make sure any open assets are saved before proceeding.");
		
		save = new Button("Save to Zip");
		save.addCallback(this);
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		
		error = new Label(" ");
		error.setTheme("/labelred");
		
		overwrite = new Button("Overwrite");
		overwrite.addCallback(new Runnable() {
			@Override public void run() {
				forceWrite = true;
				MenuCampaignSaver.this.run();
			}
		});
	}
	
	@Override public void callback(ListBox.CallbackReason reason) {
		int index = campaignsBox.getSelected();
		
		save.setEnabled(index != -1);
		forceWrite = false;
	}
	
	@Override public void run() {
		int index = campaignsBox.getSelected();
		if (index == -1) return;
		
		String directoryName = "campaigns/" + campaignsModel.getEntry(index);
		String zipFileName = directoryName + ResourceType.Zip.getExtension();
		
		File zipFile = new File(zipFileName);
		if (zipFile.exists() && !forceWrite) {
			error.setText(zipFile.getName() + "\" already exists.");
			overwrite.setVisible(true);
			return;
		}
		
		try {
			FileUtil.saveToZipFile(directoryName, zipFileName);
		} catch (Exception e) {
			Logger.appendToErrorLog("Error generating zip file from " + directoryName, e);
		}
		
		Game.campaignEditor.updateStatusText(zipFileName + " saved to zip.");
		
		this.manager.getCloseCallback().run();
	}
	
	@Override protected Widget createMenuWidget(MenuManager manager, int level) {
		this.manager = manager;
		
		this.forceWrite = false;
		save.setEnabled(false);
		overwrite.setVisible(false);
		error.setText(" ");
		
		content.removeAllChildren();
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		
		campaignsModel = new SimpleChangableListModel<String>();
		for (String fileName : new File("campaigns").list()) {
			File f = new File("campaigns/" + fileName);
			
			if (!f.isDirectory() || f.getName().equals(".svn")) continue;
			
			campaignsModel.addElement(fileName);
		}
		
		campaignsBox = new ListBox<String>(campaignsModel);
		campaignsBox.setTheme("listboxlarge");
		campaignsBox.addCallback(this);
		
		Group bottomH = content.createSequentialGroup(error, overwrite);
		Group bottomV = content.createParallelGroup(error, overwrite);
		
		Group mainH = content.createParallelGroup(warning, campaignsBox, save);
		mainH.addGroup(bottomH);
		Group mainV = content.createSequentialGroup(warning, campaignsBox, save);
		mainV.addGroup(bottomV);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
		
		return content;
	}
}
