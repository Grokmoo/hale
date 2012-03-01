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

package net.sf.hale.editor;

import net.sf.hale.editor.widgets.SubItemEditor;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.ToggleButton;

public class ContainerEditor extends SubItemEditor {
	private Container selectedContainer;
	
	private final Label title;
	private final ToggleButton workbenchButton;
	
	private final TabbedPane tabs;
	private final OpenableEditor openableEditor;
	private final LootEditor lootEditor;
	
	public ContainerEditor() {
		this.setTheme("");
		
		title = new Label("Container Properties");
		title.setTheme("/titlelabel");
		add(title);
		
		workbenchButton = new ToggleButton("Workbench");
		workbenchButton.setTheme("/radiobutton");
		workbenchButton.addCallback(new Runnable() {
			@Override public void run() {
				selectedContainer.setWorkbench(workbenchButton.isActive());
			}
		});
		add(workbenchButton);
		
		lootEditor = new LootEditor();
		lootEditor.setSize(400, 200);
		
		openableEditor = new OpenableEditor("images/items", 34);
		
		tabs = new TabbedPane();
		tabs.setTheme("/tabbedpane");
		
		tabs.addTab("Openable", openableEditor);
		tabs.addTab("Loot", lootEditor);
		add(tabs);
	}
	
	@Override public int getPreferredHeight() {
		return 10 + getBorderVertical() + title.getPreferredHeight() +
				workbenchButton.getPreferredHeight() + tabs.getPreferredHeight();
	}
	
	@Override public int getPreferredWidth() {
		return tabs.getPreferredWidth() + getBorderHorizontal();
	}
	
	@Override protected void layout() {
		title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
		title.setPosition(getInnerX(), getInnerY());
		
		workbenchButton.setSize(workbenchButton.getPreferredWidth(), workbenchButton.getPreferredHeight());
		workbenchButton.setPosition(getInnerX(), title.getBottom() + 5);
		
		tabs.setSize(getInnerWidth(), getInnerBottom() - workbenchButton.getBottom() - 5);
		tabs.setPosition(getInnerX(), workbenchButton.getBottom() + 5);
	}
	
	@Override public void getPropertiesFromItem(Item item) {
		if (item == null) return;
		
		if (item.getType() == Entity.Type.CONTAINER) {
			selectedContainer = (Container)item;
		}
		
		if (selectedContainer != null) {
			lootEditor.setLoot(selectedContainer.getLoot());
			workbenchButton.setActive(selectedContainer.isWorkbench());
			
			openableEditor.getPropertiesFromItem(item);
		}
	}
}
