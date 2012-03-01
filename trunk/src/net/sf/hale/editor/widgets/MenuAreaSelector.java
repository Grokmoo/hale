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

import net.sf.hale.Area;
import net.sf.hale.Game;
import net.sf.hale.editor.NewAreaPopup;
import net.sf.hale.editor.reference.ReferencePopupWindow;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.MenuElement;
import de.matthiasmann.twl.MenuManager;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Widget;

public class MenuAreaSelector extends MenuElement {
	private final Button newArea, saveArea;
	
	private MenuManager menuManager;
	private DialogLayout content;
	
	private String selectedArea;
	
	public MenuAreaSelector() {
		newArea = new Button("Create New Area");
		newArea.addCallback(new Runnable() {
			@Override public void run() {
				NewAreaPopup popup = new NewAreaPopup(Game.campaignEditor);
				popup.setCallback(Game.campaignEditor);
				popup.openPopupCentered();
				
				//Game.campaignEditor.newArea();
				
				menuManager.getCloseCallback().run();
			}
		});
		
		saveArea = new Button("Save Current Area");
		saveArea.addCallback(new Runnable() {
			@Override public void run() {
				Game.campaignEditor.checkSaveArea();
				menuManager.getCloseCallback().run();
			}
		});
		
		content = new DialogLayout();
		content.setTheme("");
	}
	
	public String getSelectedArea() {
		return selectedArea;
	}
	
	@Override protected Widget createMenuWidget(MenuManager manager, int level) {
		this.menuManager = manager;
		
		content.removeAllChildren();
		
		content = new DialogLayout();
		content.setTheme("");
		
		AreasBox areasBox = new AreasBox();
		areasBox.setTheme("/areasbox");
		
		Group groupH = content.createParallelGroup(newArea, saveArea, areasBox);
		Group groupV = content.createSequentialGroup(newArea, saveArea, areasBox);
		
		content.setHorizontalGroup(groupH);
		content.setVerticalGroup(groupV);
		
		return content;
	}

	private class AreasBox extends ScrollPane {
		private final DialogLayout content;
		
		private AreasBox() {
			content = new DialogLayout();
			content.setTheme("/dialoglayout");
			
			Group contentV = content.createSequentialGroup();
			Group contentH = content.createParallelGroup();
			
			for (int i = 0; i < Game.campaignEditor.getAreasModel().getNumEntries(); i++) {
				AreaSelectorWidget widget = new AreaSelectorWidget(i);
				
				contentV.addWidget(widget);
				contentH.addWidget(widget);
			}
			
			content.setHorizontalGroup(contentH);
			content.setVerticalGroup(contentV);
			this.setContent(content);
		}
	}
	
	private class AreaSelectorWidget extends DialogLayout {
		private final Label name;
		private final Button open, delete, refs;
		private final int index;
		
		private AreaSelectorWidget(int index) {
			this.setTheme("/itemlistentrypane");
			this.index = index;
			
			name = new Label(Game.campaignEditor.getAreasModel().getEntry(index).getID());
			open = new Button("Open");
			delete = new Button("Delete");
			refs = new Button(">>");
			
			this.setHorizontalGroup(this.createSequentialGroup(name, open, delete, refs));
			this.setVerticalGroup(this.createParallelGroup(name, open, delete, refs));
			
			open.addCallback(new Runnable() {
				@Override public void run() {
					selectedArea = Game.campaignEditor.getAreasModel().getEntry(AreaSelectorWidget.this.index).getID();
					Game.campaignEditor.checkOpenArea(selectedArea);
					menuManager.getCloseCallback().run();
				}
			});
			
			delete.addCallback(new Runnable() {
				@Override public void run() {
					Area area = Game.campaignEditor.getAreasModel().getEntry(AreaSelectorWidget.this.index);
					selectedArea = area.getID();
					
					DeleteFilePopup popup = new DeleteFilePopup(Game.campaignEditor,
							Game.campaignEditor.getPath() + "/areas/" + selectedArea + ".txt", area);
					popup.setCallback(Game.campaignEditor);
					popup.openPopupCentered();
					
					menuManager.getCloseCallback().run();
				}
			});
			
			refs.addCallback(new Runnable() {
				@Override public void run() {
					String id = Game.campaignEditor.getAreasModel().getEntry(AreaSelectorWidget.this.index).getID();
					Area area = Game.curCampaign.getArea(id);
					
					new ReferencePopupWindow(refs, area).openPopup();
				}
			});
		}
	}
}
