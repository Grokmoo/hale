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

package net.sf.hale.editor.reference;

import net.sf.hale.Game;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Widget;

/**
 * A simple PopupWindow with a scrollable Text Area.
 * 
 * The Text Area shows a list of References to a Referenceable object.
 * Each Reference is shown on one line.
 * 
 * @author Jared
 *
 */

public class ReferencePopupWindow extends PopupWindow {
	private final int HEIGHT = 300;
	private final int WIDTH = 600;
	
	private final ScrollPane scrollPane;
	private final DialogLayout content;
	
	/**
	 * Create a new ReferencePopupWindow.  User input is blocked on the root
	 * widget of the specified parent.  The list of References displayed by
	 * this PopupWindow is the list of references to the specified Referenceable.
	 * @param parent the parent Widget to open the popup on.
	 * @param selected the Referenceable from which the List of References is obtained.
	 */
	
	public ReferencePopupWindow(Widget parent, Referenceable selected) {
		super(parent);
		this.setTheme("/gamepopup");
		this.setSize(WIDTH, HEIGHT);
		
		// set the position just to the right of the parent widget, but don't go off the screen
		
		int posY = parent.getY() + (parent.getHeight() - HEIGHT) / 2;
		posY = Math.min(posY, Game.config.getEditorResolutionY() - HEIGHT);
		int posX = parent.getX() + parent.getWidth();
		
		this.setPosition(Math.min(posX, Game.config.getEditorResolutionX() - WIDTH), Math.max(posY, 0));
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		
		scrollPane = new ScrollPane(content);
		this.add(scrollPane);
		
		Group mainH = content.createParallelGroup();
		Group mainV = content.createSequentialGroup();
		
		Label title = new Label("References to " + selected.getID());
		title.setTheme("/labelbigblack");
		
		mainH.addWidget(title);
		mainV.addWidget(title);
		mainV.addGap(20);
		
		for (String reference : selected.getReferenceList().getReferences()) {
			Label refLabel = new Label(reference);
			
			mainH.addWidget(refLabel);
			mainV.addWidget(refLabel);
		}
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
}
