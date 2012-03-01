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

import java.util.LinkedList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.editor.AreaEditor;

import de.matthiasmann.twl.Menu;
import de.matthiasmann.twl.MenuCheckbox;
import de.matthiasmann.twl.model.SimpleBooleanModel;

public class MenuAreaModeSelector extends Menu {
	private AreaEditor.Mode selectedMode;
	private final List<ModeCheckbox> checkBoxes;
	
	public MenuAreaModeSelector(String name) {
		super(name);
		
		checkBoxes = new LinkedList<ModeCheckbox>();
		
		for (AreaEditor.Mode mode : AreaEditor.Mode.values()) {
			ModeCheckbox box = new ModeCheckbox(mode, new SimpleBooleanModel());
			checkBoxes.add(box);
			this.add(box);
		}
	}
	
	public AreaEditor.Mode getSelectedMode() {
		return selectedMode;
	}
	
	private class ModeCheckbox extends MenuCheckbox implements Runnable {
		private final AreaEditor.Mode mode;
		private final SimpleBooleanModel model;
		
		private ModeCheckbox(AreaEditor.Mode mode, SimpleBooleanModel model) {
			super(mode.toString(), model);
			this.mode = mode;
			this.model = model;
			model.addCallback(this);
		}
		
		public void setActive(boolean active) {
			model.setValue(active);
		}
		
		@Override public void run() {
			if (!this.model.getValue()) return;
			
			selectedMode = mode;
			
			for (ModeCheckbox box : checkBoxes) {
				if (box != this) box.setActive(false);
			}
			
			if (Game.campaignEditor.areaEditor != null) {
				Game.campaignEditor.areaEditor.setMode(mode);
			}
		}
	}
}
