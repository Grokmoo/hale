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

package net.sf.hale.editor.scripteditor;

import java.util.HashMap;
import java.util.Map;

import de.matthiasmann.twl.Menu;
import de.matthiasmann.twl.MenuAction;
import de.matthiasmann.twl.MenuCheckbox;
import de.matthiasmann.twl.model.SimpleBooleanModel;

/**
 * The menu bar with various file, edit, options for the Script Editor
 * @author Jared Stephen
 *
 */

public class MenuBar extends Menu {
	private ScriptEditor scriptEditor;
	
	private ScriptEditor.Type lastType;
	private ScriptEditor.Mode lastMode;
	
	private Map<ScriptEditor.Mode, ModeModel> modeModels;
	private Map<ScriptEditor.Type, ScriptTypeModel> typeModels;
	
	/**
	 * Creates a new MenuBar populated with the default entries
	 */
	
	public MenuBar(ScriptEditor scriptEditor) {
		this.scriptEditor = scriptEditor;
		
		Menu fileMenu = new Menu("File");
		
		fileMenu.add(new MenuAction("New (Ctrl+N)", new Runnable() {
			@Override public void run() {
				MenuBar.this.scriptEditor.newScript();
			}
		}));
		
		fileMenu.add(new MenuAction("Save (Ctrl+S)", new Runnable() {
			@Override public void run() {
				MenuBar.this.scriptEditor.saveSelected();
			}
		}));
		
		fileMenu.add(new MenuAction("Copy", new Runnable() {
			@Override public void run() {
				MenuBar.this.scriptEditor.copySelected();
			}
		}));
		
		fileMenu.add(new MenuAction("Delete (Ctrl+D)", new Runnable() {
			@Override public void run() {
				MenuBar.this.scriptEditor.deleteSelected();
			}
		}));
		
		fileMenu.add(new MenuAction("Close", new Runnable() {
			@Override public void run() {
				MenuBar.this.scriptEditor.fireDefaultCloseCallback();
			}
		}));
		
		add(fileMenu);
		
		Menu editMenu = new Menu("Edit");
		
		editMenu.add(new MenuAction("Undo", new Runnable() {
			@Override public void run() {
				
			}
		}));
		
		editMenu.add(new MenuAction("Redo", new Runnable() {
			@Override public void run() {
				
			}
		}));
		
		// TODO add edit menu once undo / redo is implemented
		//add(editMenu);
		
		typeModels = new HashMap<ScriptEditor.Type, ScriptTypeModel>();
		
		Menu scriptTypeMenu = new Menu("Type");
		
		for (ScriptEditor.Type type : ScriptEditor.Type.values()) {
			ScriptTypeModel model = new ScriptTypeModel(type);
			typeModels.put(type, model);
			
			scriptTypeMenu.add( new MenuCheckbox(type.toString(), model) );
		}
		
		add(scriptTypeMenu);
		
		modeModels = new HashMap<ScriptEditor.Mode, ModeModel>();
		
		Menu modeMenu = new Menu("Mode");
		
		for (ScriptEditor.Mode mode : ScriptEditor.Mode.values()) {
			ModeModel model = new ModeModel(mode);
			modeModels.put(mode, model);
			
			modeMenu.add( new MenuCheckbox(mode.toString(), model) );
		}
		
		add(modeMenu);
		
		// set default mode and script type
		typeModels.get(ScriptEditor.Type.Conversation).setValue(true);
		modeModels.get(ScriptEditor.Mode.Edit).setValue(true);
		
	}
	
	private class ScriptTypeModel extends SimpleBooleanModel implements Runnable {
		private ScriptEditor.Type type;
		private boolean disableCallback = false;
		
		private ScriptTypeModel(ScriptEditor.Type type) {
			this.type = type;
			addCallback(this);
		}
		
		@Override public void run() {
			if (disableCallback) return;
			
			if (lastType != null) {
				ScriptTypeModel last = typeModels.get(lastType);
				
				last.disableCallback = true;
				last.setValue(false);
				last.disableCallback = false;
				
				lastType = null;
			}
			
			disableCallback = true;
			setValue(true);
			disableCallback = false;
			
			lastType = type;
			
			scriptEditor.setSelectedType(type);
		}
	}
	
	private class ModeModel extends SimpleBooleanModel implements Runnable {
		private ScriptEditor.Mode mode;
		private boolean disableCallback = false;
		
		private ModeModel(ScriptEditor.Mode mode) {
			this.mode = mode;
			addCallback(this);
		}
		
		@Override public void run() {
			if (disableCallback) return;
			
			if (lastMode != null) {
				ModeModel last = modeModels.get(lastMode);
				
				last.disableCallback = true;
				last.setValue(false);
				last.disableCallback = false;
				
				lastMode = null;
			}
			
			disableCallback = true;
			setValue(true);
			disableCallback = false;
			
			lastMode = mode;
			
			scriptEditor.setSelectedMode(mode);
		}
	}
}
