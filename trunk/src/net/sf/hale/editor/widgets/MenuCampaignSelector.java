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

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.MenuElement;
import de.matthiasmann.twl.MenuManager;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class MenuCampaignSelector extends MenuElement implements Runnable, CallbackWithReason<ListBox.CallbackReason> {
	private DialogLayout content;
	private ListBox<String> campaignIDs;
	private SimpleChangableListModel<String> campaignIDsModel;
	private final Button open;
	private MenuManager manager;
	
	public MenuCampaignSelector() {
		open = new Button("Open Campaign");
		open.addCallback(this);
		
		content = new DialogLayout();
		content.setTheme("");
	}
	
	// list box callback
	@Override public void callback(ListBox.CallbackReason reason) {
		int index = campaignIDs.getSelected();
		
		open.setEnabled(index != -1);
	}
	
	// open campaign callback
	@Override public void run() {
		int index = campaignIDs.getSelected();
		if (index == -1) return;
		
		Game.campaignEditor.openCampaign(campaignIDsModel.getEntry(index));
		
		this.manager.getCloseCallback().run();
		Game.campaignEditor.update();
	}

	@Override protected Widget createMenuWidget(MenuManager manager, int level) {
		this.manager = manager;
		
		open.setEnabled(false);
		
		content.removeAllChildren();
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		
		campaignIDsModel = new SimpleChangableListModel<String>();
		File campaignDir = new File("campaigns");
		String[] fileList = campaignDir.list();
		for (int i = 0; i < fileList.length; i++) {
			File f = new File("campaigns/" + fileList[i]);
			if (f.isDirectory() && !f.getName().equals(".svn")) {
				campaignIDsModel.addElement(fileList[i]);
			}
		}
		
		campaignIDs = new ListBox<String>(campaignIDsModel);
		campaignIDs.setTheme("listboxlarge");
		campaignIDs.addCallback(this);
		
		content.setHorizontalGroup(content.createParallelGroup(campaignIDs, open));
		content.setVerticalGroup(content.createSequentialGroup(campaignIDs, open));
		
		return content;
	}
}
