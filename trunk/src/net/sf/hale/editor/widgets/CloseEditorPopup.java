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

import net.sf.hale.Game;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.DialogLayout.Group;

/**
 * A popup window that confirms whether the user wishes to exit the editor
 * and whether they want to save their changes
 * @author Jared
 *
 */

public class CloseEditorPopup extends PopupWindow {
	/**
	 * Creates a new PopupWindow with the specified parent (owner) Widget
	 * @param parent the owner Widget for this PopupWindow
	 */
	
	public CloseEditorPopup(Widget parent) {
		super(parent);
		
		setTheme("");
		setCloseOnClickedOutside(false);
		setCloseOnEscape(true);
		
		Label title = new Label("Exiting the Campaign Editor");
		
		Label warning = new Label("Save any outstanding changes?");
		
		Button yes = new Button("Yes");
		yes.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
				
				saveContent();
				Game.campaignEditor.exit();
			}
		});
		
		Button no = new Button("No");
		no.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
				
				Game.campaignEditor.exit();
			}
		});
		
		Button cancel = new Button("Cancel");
		cancel.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
			}
		});
		
		DialogLayout content = new DialogLayout();
		content.setTheme("/filepopup");
		
		Group topH = content.createSequentialGroup();
		Group topV = content.createParallelGroup();
		
		topH.addGap(50);
		topH.addWidget(title);
		topV.addWidget(title);
		topH.addGap(50);
		
		Group mainH = content.createParallelGroup(topH);
		Group mainV = content.createSequentialGroup(topV);
		
		mainV.addGap(10);
		
		mainH.addWidget(warning);
		mainV.addWidget(warning);
		
		mainH.addGroup(content.createSequentialGroup(yes, no, cancel));
		mainV.addGroup(content.createParallelGroup(yes, no, cancel));
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
		
		this.add(content);
	}
	
	private void saveContent() {
		Game.campaignEditor.checkSaveArea();
		
		Game.campaignEditor.scriptEditor.saveSelected();
		Game.campaignEditor.merchantEditor.saveSelected();
		Game.campaignEditor.recipeEditor.saveSelected();
		Game.campaignEditor.areaTriggerEditor.saveSelected();
		Game.campaignEditor.itemListEditor.saveSelected();
		Game.campaignEditor.creatureEditor.saveSelected();
		Game.campaignEditor.itemEditor.saveSelected();
		Game.campaignEditor.encounterEditor.saveSelected();
		Game.campaignEditor.propertiesEditor.saveSelected();
		Game.campaignEditor.transitionEditor.saveSelected();
	}
}
