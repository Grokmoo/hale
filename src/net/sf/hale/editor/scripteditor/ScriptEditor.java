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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import net.sf.hale.Game;
import net.sf.hale.editor.EditorWindow;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.reference.Script;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;

import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.DefaultEditFieldModel;
import de.matthiasmann.twl.textarea.StyleSheet;

/**
 * A very basic ScriptEditor for use inside the Campaign Editor.  For more advanced functionality,
 * using an external editor with JavaScript support is recommended.
 * 
 * The Editor has two modes:
 * 
 * View - supports syntax highlighting and line numbers but is not editable
 * Edit - provides only a plain text view but is editable
 * 
 * @author Jared Stephen
 *
 */

public class ScriptEditor extends EditorWindow implements CallbackWithReason<ListBox.CallbackReason>, PopupCallback {
	
	/**
	 * One of the two modes this ScriptEditor can operate in
	 * @author Jared Stephen
	 *
	 */
	public enum Mode {
		View, Edit
	};
	
	/**
	 * One of the types of Scripts that can be edited by this ScriptEditor
	 * @author Jared Stephen
	 *
	 */
	public enum Type {
		Conversation, Quest, Item, Trigger, AI;
	};
	
	private Type currentType;
	private Mode currentMode;
	private String currentScriptPath;
	
	private final Content content;
	
	private final Label scriptsLabel;
	private final ListBox<Script> scriptsBox;
	
	private final JavaTextAreaModel textAreaModel;
	private final TextArea textArea;
	private final ScrollPane scrollPane;
	
	private final ScriptEditField editor;
	
	private final ScriptTemplatesList scriptTemplates;
	
	private final StyleSheet styleSheet;
	
	private final MenuBar menuBar;
	private final Widget menuBarWidget;
	
	/**
	 * Create a new ScriptEditor.  The Script Editor will use the lists of scripts
	 * maintained by the CampaignEditor when showing lists of which scripts can be edited.
	 * 
	 */
	
	public ScriptEditor() {
		super("Script Editor");
		
		// create the stylesheet for the view mode - supports syntax highlighting
		styleSheet = new StyleSheet();

		try {
			styleSheet.parse(
					"ol > li { padding-left: 5px; }" +
					"pre {font-family: code }" +
					".comment    { font-family: codeComment }" +
					".commentTag { font-family: codeCommentTag }" +
					".string     { font-family: codeString  }" +
					".keyword    { font-family: codeKeyword }");
		} catch(Exception e) {
			Logger.appendToErrorLog("Error parsing style sheet for script editor.", e);
		}
		
		// parse the set of script templates
		scriptTemplates = new ScriptTemplatesList("scriptTemplates");
		
		
		content = new Content();
		this.add(content);
		
		scriptsLabel = new Label();
		content.add(scriptsLabel);
		
		scriptsBox = new ReferenceListBox<Script>();
		scriptsBox.addCallback(this);
		content.add(scriptsBox);
		
		editor = new ScriptEditField(scriptTemplates, new DefaultEditFieldModel());
		
		textAreaModel = new JavaTextAreaModel();
		textArea = new TextArea(textAreaModel);
		textArea.setStyleClassResolver(styleSheet);
		
		scrollPane = new ScrollPane(editor);
		content.add(scrollPane);
		
		menuBar = new MenuBar(this);
		menuBarWidget = menuBar.createMenuBar();
		content.add(menuBarWidget);
	}
	
	/**
	 * Opens a popup window to confirm the creation of a new script of the currently
	 * specified type
	 */
	
	public void newScript() {
		if (currentType == null) return;
		
		saveSelected();
		
		NewScriptPopup popup = new NewScriptPopup(ScriptEditor.this, currentType, null);
		popup.openPopupCentered();
	}
	
	/**
	 * Opens a popup window to confirm the destination name of a copy of the currently
	 * selected script
	 */
	
	public void copySelected() {
		if (currentType == null || currentScriptPath == null || currentScriptPath.length() == 0)
			return;
		
		saveSelected();
		
		NewScriptPopup popup = new NewScriptPopup(ScriptEditor.this, currentType, currentScriptPath);
		popup.openPopupCentered();
	}

	/**
	 * Opens a popup window to confirm deletion of the currently selected script
	 */
	
	public void deleteSelected() {
		if (currentType == null || currentScriptPath == null || currentScriptPath.length() == 0) return;
		
		Script script = scriptsBox.getModel().getEntry(scriptsBox.getSelected());
		
		DeleteFilePopup popup = new DeleteFilePopup(ScriptEditor.this, currentScriptPath, script);
		popup.setCallback(ScriptEditor.this);
		popup.openPopupCentered();
	}
	
	/**
	 * Saves the currently selected script to disk.  If there is no selected script,
	 * does nothing
	 */
	
	@Override public void saveSelected() {
		if (this.currentMode != Mode.Edit) return;
		if (this.currentScriptPath == null) return;
		
		String scriptContents = editor.getText();
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(currentScriptPath)));
			
			out.write(scriptContents);
			
			out.close();
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving script " + currentScriptPath, e);
		}
		
		Game.campaignEditor.updateStatusText("Script " + currentScriptPath + " saved.");
	}
	
	/**
	 * This function is called whenever a Script is deleted.
	 */
	
	@Override public void deleteComplete() {
		Game.campaignEditor.updateScripts();
	}
	
	/**
	 * This function is called whenever a Script is copied.
	 */
	
	@Override public void copyComplete() { }
	
	/**
	 * This function is called whenever a new Script is created
	 */
	
	@Override public void newComplete() { }
	
	/**
	 * Sets the currently selected type of scripts being displayed to the specified type
	 * @param type the type of script to display and edit
	 */
	
	public void setSelectedType(Type type) {
		saveSelected();
		
		currentType = type;
		editor.setType(currentType);
		
		// set the currently listed scripts to the ones specified by the CampaignEditor
		// for the selected Type
		scriptsBox.setModel(Game.campaignEditor.getScripts(currentType));
		
		setLabelText();
	}
	
	/**
	 * Sets the current mode to either view only mode or edit mode
	 * @param mode the mode to set
	 */
	
	public void setSelectedMode(Mode mode) {
		saveSelected();
		
		this.currentMode = mode;
		
		switch (currentMode) {
		case Edit:
			scrollPane.setFixed(ScrollPane.Fixed.NONE);
			scrollPane.setContent(editor);
			break;
		case View:
			scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
			scrollPane.setContent(textArea);
			break;
		}
		
		setCurrentScript();
		
		setLabelText();
	}
	
	private void setLabelText() {
		if (currentMode == null || currentType == null) return;
		
		StringBuilder sb = new StringBuilder();
		
		switch (currentMode) {
		case Edit:
			sb.append("Editing ");
			break;
		case View:
			sb.append("Viewing ");
			break;
		}
		
		sb.append(currentType.toString());
		sb.append(" Scripts");
		
		scriptsLabel.setText(sb.toString());
	}
	
	/**
	 * Sets the currently shown script in the Viewer or Editor.
	 */
	
	private void setCurrentScript() {
		String script = null;
		int scriptIndex = scriptsBox.getSelected();
		if (scriptIndex != -1) {
			script = scriptsBox.getModel().getEntry(scriptIndex).getID();
		}
		
		if (script != null && script.length() > 0) {
			currentScriptPath = Game.campaignEditor.getPath() + "/scripts/" + script + ".js";
		} else {
			currentScriptPath = null;
		}
		
		try {
			switch (currentMode) {
			case View:
				textAreaModel.clear();
				if (currentScriptPath != null)
					textAreaModel.parse(new File(currentScriptPath), true);
				break;
			case Edit:
				if (currentScriptPath != null) {
					// replace tabs in the editor with spaces, which keeps line lengths down since \t is
					// by default translated to 8 spaces.
					editor.setText(FileUtil.readFileAsString(currentScriptPath).replace("\t", "    "));
				} else {
					editor.setText("");
				}
				break;
			}
		} catch (Exception e) {
			Logger.appendToErrorLog("Error reading script " + currentScriptPath, e);
		}
		
		scrollPane.setScrollPositionY(0);
		content.invalidateLayout();
	}
	
	/**
	 * This function is called whenever a script is selected.
	 * 
	 * @param reason the ListBox.CallbackReason for this function being called.
	 */

	@Override public void callback(ListBox.CallbackReason reason) {
		switch (reason) {
		case SET_SELECTED: case MODEL_CHANGED: break;
		default:
			saveSelected();
		}
		
		setCurrentScript();
	}
	
	@Override public boolean handleEvent(Event evt) {
		if (handleShortcutKeys(evt)) return true;
		
		return super.handleEvent(evt);
	}
	
	public boolean handleShortcutKeys(Event evt) {
		switch (evt.getType()) {
		case KEY_PRESSED:
			switch (evt.getKeyCode()) {
			case Event.KEY_D:
				if ((evt.getModifiers() & Event.MODIFIER_CTRL) != 0) {
					deleteSelected();
					return true;
				}
				
			case Event.KEY_S:
				if ((evt.getModifiers() & Event.MODIFIER_CTRL) != 0) {
					saveSelected();
					return true;
				}
				
			case Event.KEY_N:
				if ((evt.getModifiers() & Event.MODIFIER_CTRL) != 0) {
					newScript();
					return true;
				}
				
			}
			break;
		}
		
		return false;
	}
	
	private class Content extends Widget {
		private Content() {
			setTheme("/scripteditorlayout");
		}
		
		@Override protected void layout() {
			menuBarWidget.setSize(getInnerWidth(), menuBarWidget.getPreferredHeight());
			menuBarWidget.setPosition(getInnerX(), getInnerY());
			
			scriptsLabel.setSize(scriptsLabel.getPreferredWidth(), scriptsLabel.getPreferredHeight());
			scriptsLabel.setPosition(getInnerX(), menuBarWidget.getBottom() + 20);
			
			scriptsBox.setPosition(getInnerX(), scriptsLabel.getBottom());
			scriptsBox.setSize(scriptsBox.getMaxWidth(), getInnerBottom() - scriptsBox.getY());
			
			scrollPane.setPosition(scriptsBox.getRight(), menuBarWidget.getBottom());
			scrollPane.setSize(getInnerRight() - scrollPane.getX(), getInnerBottom() - scrollPane.getY());
		}
	}
}
