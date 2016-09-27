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

package net.sf.hale.widgets;


import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import net.sf.hale.Game;
import net.sf.hale.Keybindings;

/**
 * The widget shown in the main game interface that records a history of
 * all messages the player has seen
 * @author Jared Stephen
 *
 */

public class MessageBox extends Widget {
	private volatile boolean cacheDirty;
	
	private final HotKeyButton popOut;
	
	private final TextArea textArea;
	private final HTMLTextAreaModel textAreaModel;
	private final ScrollPane textPane;
	
	private StringBuilder content;
	
	/**
	 * Creates a new Message Box
	 */
	
	public MessageBox() {
		textAreaModel = new HTMLTextAreaModel();
        textArea = new TextArea(textAreaModel);
        textPane = new ScrollPane();
        textPane.setContent(textArea);
        textPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        content = new StringBuilder();
        add(textPane);
        
        popOut = new HotKeyButton();
        popOut.setTheme("popoutbutton");
        popOut.setHotKeyBinding(new Keybindings.ToggleWindow(Game.mainViewer.messagesWindow, "MessagesWindow"));
        add(popOut);
		
		cacheDirty = true;
	}
	
	/**
	 * Returns the model used in this message box
	 * @return the model
	 */
	
	public HTMLTextAreaModel getModel() {
		return textAreaModel;
	}
	
	/**
	 * Returns a string of all the contents of the message box currently
	 * @return the message box contents
	 */
	
	public String getContents() {
		return content.toString();
	}
	
	@Override protected void layout() {
		super.layout();
		
		textPane.setPosition(getInnerX(), getInnerY());
		textPane.setSize(getInnerWidth(), getInnerHeight());
		
		popOut.setSize(popOut.getPreferredWidth(), popOut.getPreferredHeight());
		popOut.setPosition(getInnerRight() - popOut.getWidth(), getInnerY());
	}
	
	/**
	 * Adds the specified message with the specified font to the messages
	 * shown.  The message will be added as its own line
	 * @param font the font to apply to the message
	 * @param text the content of the message to add
	 */
	
	public void addMessage(String font, String text) {
		synchronized(content) {
			content.append("<div style=\"font-family: ").append(font).append("; \">");

			content.append(text);

			content.append("</div>");

			// Keep the content from becoming too long
			while (content.length() > 10000) {
				int index = content.indexOf("</div>");
				content.delete(0, index + 6);
			}
		}
		
		cacheDirty = true;
	}
	
	/**
	 * Updates the state of this message box with any new messages
	 * that have been added or any other changes.
	 */
	
	public void update() {
		if (!cacheDirty) return;
		
		cacheDirty = false;
		
		String contentString;
		synchronized(content) {
			contentString = content.toString();
		}
		
		textAreaModel.setHtml(contentString);

		textPane.validateLayout();
		textPane.setScrollPositionY(textPane.getMaxScrollPosY());
	}
}
