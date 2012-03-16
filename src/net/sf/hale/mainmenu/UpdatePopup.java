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

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * A popup window that starts an update process and displays the status to the user
 * @author Jared
 *
 */

public class UpdatePopup extends PopupWindow {
	private Content content;
	
	public UpdatePopup(Widget parent) {
		super(parent);
		
		content = new Content();
		add(content);
		
		setCloseOnClickedOutside(false);
		setCloseOnEscape(false);
	}
	
	private class Content extends DialogLayout {
		private Label title;
		
		private Content() {
			title = new Label("Checking for updates...");
			title.setTheme("titlelabel");
			
			Group mainH = createParallelGroup();
			Group mainV = createSequentialGroup();
			
			mainH.addWidget(title);
			mainV.addWidget(title);
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
		}
	}
}
