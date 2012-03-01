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
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Item;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;

public class DoorEditor extends SubItemEditor {
	private Door selectedDoor;
	
	private final Label title;
	
	private final ToggleButton transparentButton;
	
	private final OpenableEditor openableEditor;
	
	public DoorEditor() {
		this.setTheme("");
		
		title = new Label("Door Properties");
		title.setTheme("/titlelabel");
		add(title);
		
		transparentButton = new ToggleButton("Transparent if closed");
		transparentButton.addCallback(new Runnable() {
			@Override public void run() {
				selectedDoor.setTransparent(transparentButton.isActive());
			}
		});
		add(transparentButton);
		
		openableEditor = new OpenableEditor("images/doors", 100);
		add(openableEditor);
	}
	
	@Override public int getPreferredHeight() {
		return title.getPreferredHeight() + transparentButton.getPreferredHeight() +
				openableEditor.getPreferredHeight() + 10 + getBorderVertical();
	}
	
	@Override public int getPreferredWidth() {
		return openableEditor.getPreferredWidth() + getBorderHorizontal();
	}
	
	@Override protected void layout() {
		title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
		transparentButton.setSize(transparentButton.getPreferredWidth(), transparentButton.getPreferredHeight());
		
		title.setPosition(getInnerX(), getInnerY());
		transparentButton.setPosition(getInnerX(), title.getBottom() + 5);
		
		openableEditor.setSize(openableEditor.getPreferredWidth(), openableEditor.getPreferredHeight());
		openableEditor.setPosition(getInnerX(), transparentButton.getBottom() + 5);
	}
	
	@Override public void getPropertiesFromItem(Item item) {
		if (item == null) return;
		
		this.selectedDoor = (Door) item;
		transparentButton.setActive(selectedDoor.isTransparent());
		
		openableEditor.getPropertiesFromItem(item);
	}
}
