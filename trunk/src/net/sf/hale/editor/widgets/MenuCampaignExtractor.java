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
import net.sf.hale.util.Logger;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.MenuElement;
import de.matthiasmann.twl.MenuManager;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class MenuCampaignExtractor extends MenuElement implements Runnable, CallbackWithReason<ListBox.CallbackReason> {
	private DialogLayout content;
	private MenuManager manager;
	
	private ListBox<String> campaignsBox;
	private SimpleChangableListModel<String> campaignsModel;
	
	private final Button extract;
	private final Label error;
	
	public MenuCampaignExtractor() {
		extract = new Button("Extract");
		extract.addCallback(this);
		
		error = new Label(" ");
		error.setTheme("/labelred");
		
		content = new DialogLayout();
		content.setTheme("");
	}
	
	// list box callback
	@Override public void callback(ListBox.CallbackReason reason) {
		int index = campaignsBox.getSelected();
		
		extract.setEnabled(index != -1);
	}
	
	@Override public void run() {
		int index = campaignsBox.getSelected();
		if (index == -1) return;
		
		String zipFileName = "campaigns/" + campaignsModel.getEntry(index);
		String directoryName = ResourceManager.getResourceID(zipFileName, ResourceType.Zip);
		
		File dirFile = new File(directoryName);
		if (dirFile.exists() && dirFile.isDirectory()) {
			error.setText("Directory \"" + dirFile.getName() + "\" already exists.");
			return;
		}
		
		try {
			FileUtil.extractZipFile(directoryName, zipFileName);
		} catch (Exception e) {
			Logger.appendToErrorLog("Error extracting zip file " + zipFileName, e);
		}
		
		Game.campaignEditor.updateStatusText(zipFileName + " extracted.");
		
		this.manager.getCloseCallback().run();
	}
	
	@Override protected Widget createMenuWidget(MenuManager manager, int level) {
		this.manager = manager;
		
		extract.setEnabled(false);
		error.setText(" ");
		
		content.removeAllChildren();
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		
		campaignsModel = new SimpleChangableListModel<String>();
		for (String fileName : new File("campaigns").list()) {
			File f = new File("campaigns/" + fileName);
			
			if (!f.isFile() || !f.getName().endsWith(ResourceType.Zip.getExtension())) continue;
			
			campaignsModel.addElement(fileName);
		}
		
		campaignsBox = new ListBox<String>(campaignsModel);
		campaignsBox.setTheme("listboxlarge");
		campaignsBox.addCallback(this);
		
		content.setHorizontalGroup(content.createParallelGroup(campaignsBox, extract, error));
		content.setVerticalGroup(content.createSequentialGroup(campaignsBox, extract, error));
		
		return content;
	}
}
