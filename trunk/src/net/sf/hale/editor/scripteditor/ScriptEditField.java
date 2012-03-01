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

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Menu;
import de.matthiasmann.twl.model.EditFieldModel;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 * An edit field customized for use with editing JavaScript source files.
 * 
 * @author Jared Stephen
 *
 */

public class ScriptEditField extends EditField {
	
	// TODO support syntax highlighting in editor
	
	// TODO add undo / redo stack
	
	private int clickCount = 0;
	private long lastClickMillis = 0l;
	private final long clickTime = 500l;
	
	private int wordSelectionStart, wordSelectionEnd;
	
	private EditFieldModel editBuffer;
	private ScriptTemplatesList templatesList;
	private ScriptEditor.Type currentType;
	
	/**
	 * Create a new, empty ScriptEditField.
	 */
	
	public ScriptEditField(ScriptTemplatesList templatesList, EditFieldModel editBuffer) {
		super(null, editBuffer);
		
		this.editBuffer = editBuffer;
		this.templatesList = templatesList;
		
		this.setTheme("editfield");
		this.setMultiLine(true);
		this.setTooltipContent(null);
		this.setAutoCompletion(null);
	}
	
	/**
	 * Sets the current type of script being edited
	 * @param type the script type
	 */
	
	public void setType(ScriptEditor.Type type) {
		this.currentType = type;
	}
	
	// override the default double click behavior to select between quotes
	
	@Override protected void selectWordFromMouse(int index) {
		wordSelectionStart = index;
		wordSelectionEnd = index;
		
		while (wordSelectionStart > 0) {
			char c = editBuffer.charAt(wordSelectionStart - 1);
			
			if (!checkSelectionCharacter(c)) break;
			
			wordSelectionStart--;
		}
		
		while (wordSelectionEnd < editBuffer.length()) {
			char c = editBuffer.charAt(wordSelectionEnd);
			
			if (!checkSelectionCharacter(c)) break;
			
			wordSelectionEnd++;
		}
		
		setSelection(wordSelectionStart, wordSelectionEnd);
	}
	
	private boolean checkSelectionCharacter(char c) {
		if (Character.isWhitespace(c)) return false;
		
		if (!Character.isJavaIdentifierPart(c)) return false;
		
		return true;
	}
	
	/*
	 * override the behavior so the mouse cursor does not flash while typing
	 * (non-Javadoc)
	 * @see de.matthiasmann.twl.EditField#insertChar(char)
	 */
	
	@Override protected void insertChar(char ch) {
		super.insertChar(ch);
		
		resetCursorAnimation();
		
		// override the "enter" key behavior to do auto indenting
		if (ch == '\n') {
			boolean newBlock = false;
			
			int cursorPos = getCursorPos();
			int lineStart = computeLineStart(cursorPos - 1);
			int lineEnd = computeLineEnd(cursorPos - 1);
			
			int charPos = lineStart;
			int prevLineWhitespace = 0;
			int curLineWhitespace = 0;
			
			// compute the amount of indent on the previous line
			while (editBuffer.charAt(charPos) == ' ') {
				charPos++;
				prevLineWhitespace++;
			}
			
			// indent an additional 4 characters if the last line was a new block
			if (getLastNonWhitespace(lineStart, lineEnd) == '{') {
				curLineWhitespace += 4;
				newBlock = true;
			}
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < prevLineWhitespace + curLineWhitespace; i++) {
				sb.append(' ');
			}
			
			int destPos = cursorPos + sb.length();
			
			// if a new block was created and there are unclosed blocks,
			// auto close the block on a new line
			if (newBlock && hasUnclosedBlocks()) {
				sb.append('\n');
				for (int i = 0; i < prevLineWhitespace; i++) {
					sb.append(' ');
				}
				sb.append('}');
			}
			
			super.insertText(sb.toString());
			
			// move the cursor back to the correct line if we added more text
			// to end a block
			super.setCursorPos(destPos);
		}
	}
	
	private boolean hasUnclosedBlocks() {
		int unclosedBlocks = 0;
		
		for (int i = 0; i < editBuffer.length(); i++) {
			char c = editBuffer.charAt(i);
			
			switch (c) {
			case '{':
				unclosedBlocks++;
				break;
			case '}':
				unclosedBlocks--;
				break;
			}
		}
		
		return unclosedBlocks > 0;
	}
	
	private char getLastNonWhitespace(int start, int end) {
		for (int i = end; i >= start; i--) {
			char c = editBuffer.charAt(i);
			
			if ( !Character.isWhitespace(c) ) return c;
		}
		
		return editBuffer.charAt(start);
	}
	
	@Override protected void deletePrev() {
		super.deletePrev();
		
		resetCursorAnimation();
	}
	
	@Override protected void deleteNext() {
		super.deleteNext();
		
		resetCursorAnimation();
	}
	
	@Override protected void setCursorPos(int pos, boolean select) {
		super.setCursorPos(pos, select);
		
		resetCursorAnimation();
	}
	
	private void resetCursorAnimation() {
		getAnimationState().resetAnimationTime(StateKey.get("keyboardFocus"));
	}
	
	//override event handling to improve double and triple click behavior
	
	@Override public boolean handleEvent(Event evt) {
		// disallow the standard double and triple click routines
		if (evt.getType() == Event.Type.MOUSE_CLICKED)
			return false;
		
		boolean returnValue = super.handleEvent(evt);
		
		switch (evt.getType()) {
		case MOUSE_MOVED:
			clickCount = 0;
			break;
		case MOUSE_BTNDOWN:
			long curTime = System.currentTimeMillis();
			
			if (curTime - lastClickMillis < clickTime)
				clickCount++;
			else
				clickCount = 1;
			
			lastClickMillis = curTime;
			
			if (clickCount == 2) {
				doubleClick(evt);
			} else if (clickCount == 3) {
				tripleClick(evt);
			}
			
			break;
		}
		
		return returnValue;
	}
	
	private void doubleClick(Event evt) {
		// compute the selection
		selectWordFromMouse(getCursorPos());
		
		// setting the cursor pos will clear the selection, so set it again
		setCursorPos(wordSelectionEnd);
	
		setSelection(wordSelectionStart, wordSelectionEnd);
	}
	
	private void tripleClick(Event evt) {
		int lineStart = computeLineStart(getCursorPos());
		int lineEnd = computeLineEnd(getCursorPos());
		
		setCursorPos(lineEnd);
		setSelection(lineStart, lineEnd);
	}
	
	@Override protected void showPopupMenu(Event evt) {
		createPopupMenu().openPopupMenu(this, evt.getMouseX(), evt.getMouseY());
	}
	
	// add additional entries to the popup menu
	
	@Override protected Menu createPopupMenu() {
		Menu menu = new Menu();
		
		if (currentType != null) {
			Menu functionsMenu = new Menu("functions ->");
			for (String insertable : templatesList.getInsertables(currentType)) {
				functionsMenu.add(insertable, new FunctionCallback(insertable));
			}
			
			Menu templatesMenu = new Menu("templates ->");
			for (String template : templatesList.getTemplates(currentType)) {
				templatesMenu.add(template, new TemplateCallback(template));
			}
			
			menu.add(functionsMenu);
			menu.add(templatesMenu);
			
			menu.addSpacer();
		}
		
		Menu basicMenu = super.createPopupMenu();
		
		for (int i = 0; i < basicMenu.getNumElements(); i++) {
			menu.add(basicMenu.get(i));
		}
		
		return menu;
	}
	
	@Override public int getPreferredInnerWidth() {
		int width = getFont().computeMultiLineTextWidth(editBuffer);
		
		return Math.max(width, getParent().getInnerWidth() - getBorderHorizontal());
	}
	
	@Override public int getPreferredInnerHeight() {
		int height = super.getPreferredInnerHeight();
		
		return Math.max(height, getParent().getInnerHeight() - getBorderVertical());
	}
	
	private class FunctionCallback implements Runnable {
		private String id;
		
		private FunctionCallback(String id) {
			this.id = id;
		}
		
		@Override public void run() {
			insertText( templatesList.getInsertableString(currentType, id) );
		}
	}
	
	private class TemplateCallback implements Runnable {
		private String id;
		
		private TemplateCallback(String id) {
			this.id = id;
		}
		
		@Override public void run() {
			insertText( templatesList.getTemplateString(currentType, id) );
		}
	}
}