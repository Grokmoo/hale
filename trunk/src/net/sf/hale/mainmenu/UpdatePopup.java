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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.util.Logger;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ProgressBar;

/**
 * A popup window that handles the task of downloading and extracting the latest
 * version
 * @author Jared
 *
 */

public class UpdatePopup extends PopupWindow {
	private MainMenu mainMenu;
	private Content content;
	
	private ProgressBar progress;
	private Label task;
	private Label error;
	private Button cancel;
	
	private UpdateTask updateTask;
	
	private boolean errorOccurred;
	
	/**
	 * Creates a new UpdatePopup with the specified main menu as the parent widget,
	 * downloading the specified file
	 * @param parent
	 * @param fileToDownload
	 */
	
	public UpdatePopup(MainMenu parent, CheckForUpdatesTask.UpdateInfo updateInfo) {
		super(parent);
		
		this.mainMenu = parent;
		
		content = new Content();
		add(content);
		
		setCloseOnClickedOutside(false);
		setCloseOnEscape(false);
		
		updateTask = new UpdateTask(updateInfo);
		updateTask.start();
	}
	
	private class Content extends DialogLayout {
		private Content() {
			Label title = new Label("Updating...");
			title.setTheme("titlelabel");
			
			error = new Label();
			error.setTheme("errorlabel");
			
			cancel = new Button();
			cancel.addCallback(new Runnable() {
				@Override public void run() {
					updateTask.cancel();
					closePopup();
				}
			});
			cancel.setTheme("cancelbutton");
			
			Group mainH = createParallelGroup();
			Group mainV = createSequentialGroup();
			
			mainH.addWidgets(title, error);
			mainV.addWidgets(title, error);
			
			task = new Label("Downloading");
			task.setTheme("tasklabel");
			
			progress = new ProgressBar();
			
			mainH.addWidgets(task, progress);
			mainV.addWidgets(task, progress);
			
			mainH.addWidget(cancel);
			mainV.addWidget(cancel);
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
		}
	}
	
	private void showRestartPopup() {
		List<String> messages = new ArrayList<String>();
		messages.add("The update was applied successfully.");
		messages.add("Hale will now restart.");
		
		ErrorPopup popup = new ErrorPopup(mainMenu, messages);
		popup.setCallback(new Runnable() {
			@Override public void run() {
				try {
					Game.restartApplication(null);
				} catch (IOException e) {
					Logger.appendToErrorLog("Error restarting application", e);
				}
			}
		});
		
		mainMenu.showPopup(popup);
		mainMenu.hidePopup(UpdatePopup.this);
	}
	
	@Override protected void paint(GUI gui) {
		super.paint(gui);
		
		if (errorOccurred) return;
		
		if (updateTask.isDone()) {
			showRestartPopup();
			return;
		}
		
		String errorText = updateTask.getError();
		if (errorText != null) {
			error.setText(errorText);
			errorOccurred = true;
			
			cancel.setText("OK");
			task.setText("");
			
			adjustSize();
		}
		
		task.setText(updateTask.getTaskText());
		progress.setValue(updateTask.getProgress());
	}
}
