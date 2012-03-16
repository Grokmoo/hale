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

package net.sf.hale.mainmenu;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;

/**
 * A popup window that starts an update process and displays the status to the user
 * @author Jared
 *
 */

public class CheckForUpdatesPopup extends PopupWindow {
	private Content content;
	private MainMenu mainMenu;
	
	private boolean errorOccurred;
	
	private CheckForUpdatesTask updateTask;
	
	/**
	 * Creates a new UpdatePopup
	 * @param parent
	 */
	
	public CheckForUpdatesPopup(MainMenu parent) {
		super(parent);
		
		this.mainMenu = parent;
		
		errorOccurred = false;
		
		content = new Content();
		add(content);
		
		setCloseOnClickedOutside(false);
		setCloseOnEscape(false);
		
		updateTask = new CheckForUpdatesTask();
		updateTask.start();
	}
	
	private class Content extends DialogLayout {
		private Label title;
		private Label error;
		private Button cancel;
		
		private Content() {
			title = new Label("Checking for updates...");
			title.setTheme("titlelabel");
			
			error = new Label();
			error.setTheme("errorlabel");
			
			cancel = new Button();
			cancel.addCallback(new Runnable() {
				@Override public void run() {
					updateTask.cancel();
					CheckForUpdatesPopup.this.closePopup();
				}
			});
			cancel.setTheme("cancelbutton");
			
			Group mainH = createParallelGroup();
			Group mainV = createSequentialGroup();
			
			mainH.addWidget(title);
			mainV.addWidget(title);
			
			mainH.addWidget(error);
			mainV.addWidget(error);
			
			mainH.addWidget(cancel);
			mainV.addWidget(cancel);
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
		}
	}
	
	@Override protected void paint(GUI gui) {
		super.paint(gui);
		
		if (updateTask.hasFoundUpdates()) {
			mainMenu.hidePopup(CheckForUpdatesPopup.this);
			// show updater popup
		} else if (!errorOccurred) {
			String errorText = updateTask.getError();
			
			if (errorText != null) {
				errorOccurred = true;
				content.error.setText(errorText);
				
				CheckForUpdatesPopup.this.invalidateLayout();
				CheckForUpdatesPopup.this.adjustSize();
				
				content.cancel.setText("OK");
			}
		}
	}
}
