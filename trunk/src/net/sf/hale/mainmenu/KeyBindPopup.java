/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.mainmenu;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

public class KeyBindPopup extends PopupWindow {
	private final Callback callback;
	private final Content content;
	
	/**
	 * Creates a new KeyBindPopup
	 */
	
	public KeyBindPopup(Widget parent, Callback callback) {
		super(parent);
		
		setCloseOnEscape(false);
		setCloseOnClickedOutside(true);
		
		this.callback = callback;
		
		content = new Content();
		add(content);
	}
	
	@Override public int getPreferredInnerWidth() {
		return content.getPreferredWidth();
	}
	
	@Override public int getPreferredInnerHeight() {
		return content.getPreferredHeight();
	}
	
	private class Content extends Widget {
		private Label label;
		
		private Content() {
			label = new Label();
			add(label);
		}
		
		@Override protected void layout() {
			label.setSize(label.getPreferredWidth(), label.getPreferredHeight());
			label.setPosition(getInnerX() + getInnerWidth() / 2 - label.getWidth() / 2,
					getInnerY() + getInnerHeight() / 2 - label.getHeight());
			
			this.requestKeyboardFocus();
		}
		
		@Override public int getPreferredInnerWidth() {
			return label.getPreferredWidth();
		}
		
		@Override public int getPreferredInnerHeight() {
			return label.getPreferredHeight();
		}
		
		@Override public boolean handleEvent(Event evt) {
			switch (evt.getType()) {
			case KEY_PRESSED:
				int key = evt.getKeyCode();
				
				callback.keyBound(key);
				KeyBindPopup.this.closePopup();
				break;
			case MOUSE_ENTERED:
				return true;
			}
			
			return super.handleEvent(evt);
		}
	}
	
	/**
	 * An interface to be used as a callback when the user has selected a
	 * key
	 * @author Jared
	 *
	 */
	
	public interface Callback {
		public void keyBound(int keyCode);
	}
}
