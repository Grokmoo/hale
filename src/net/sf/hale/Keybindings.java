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

package net.sf.hale;

import java.util.HashMap;
import java.util.Map;

import net.sf.hale.mainmenu.InGameMenu;
import net.sf.hale.quickbar.Quickbar;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Widget;

/**
 * A class for managing a set of keybindings
 * @author Jared
 *
 */

public class Keybindings {
	private final int closeQuickbarPopup;
	private final int closeScriptConsole;
	private Map<Integer, Binding> bindings;
	
	/**
	 * Creates a new Keybindings object with the default bindings.  This must be initialized
	 * after the mainviewer
	 */
	
	public Keybindings() {
		bindings = new HashMap<Integer, Binding>();
		
		closeQuickbarPopup = Event.KEY_Q;
		closeScriptConsole = Event.KEY_GRAVE;
		
		bindings.put(Event.KEY_C, new ToggleWindow(Game.mainViewer.characterWindow, "CharacterWindow"));
		bindings.put(Event.KEY_I, new ToggleWindow(Game.mainViewer.inventoryWindow, "InventoryWindow"));
		bindings.put(Event.KEY_L, new ToggleWindow(Game.mainViewer.logWindow, "LogWindow"));
		bindings.put(Event.KEY_M, new ToggleWindow(Game.mainViewer.miniMapWindow, "MiniMap"));
		bindings.put(Event.KEY_Q, new ToggleQuickbarPopup());
		bindings.put(Event.KEY_GRAVE, new ToggleWindow(Game.mainViewer.scriptConsole, "ScriptConsole"));
		bindings.put(Event.KEY_X, new CancelMovement());
		bindings.put(Event.KEY_ESCAPE, new ShowMenu());
		bindings.put(Event.KEY_SPACE, new EndTurn());
		bindings.put(Event.KEY_F5, new Quicksave());
		
		for (int i = 0; i < Quickbar.SlotsAtOnce; i++) {
			// this will bind to KEY_(i + 1) (KEY_2 for i = 1, KEY_3 for i = 2, etc)
			bindings.put(Event.KEY_1 + i, new ActivateQuickbarSlot(i));
		}
	}
	
	/**
	 * Checks whether the specified key is the key associated with closing
	 * the quickbar popup
	 * @param key
	 * @return true if the key is the key to close the quickbarpopup
	 */
	
	public boolean isCloseQuickbarPopupKey(int key) {
		return key == closeQuickbarPopup;
	}
	
	/**
	 * Checks whether the specified key is the key associated with closing
	 * the script console.  If it is, then toggles the script console
	 * @param key
	 * @return true if the key is the key to close the script console
	 */
	
	public boolean checkToggleScriptConsole(int key) {
		if (key == closeScriptConsole) {
			new ToggleWindow(Game.mainViewer.scriptConsole, "ScriptConsole").run();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Activates the event associated with the given key binding, or does nothing
	 * if no binding exists with that key
	 * @param key
	 */
	
	public void fireKeyEvent(int key) {
		if (bindings.containsKey(key))
			bindings.get(key).run();
	}
	
	private abstract class Binding implements Runnable {
		
		/**
		 * Returns the descriptive action name for this binding, used in the
		 * configuration file when defining key bindings
		 * @return the action name String
		 */
		
		public String getActionName() {
			return getClass().getSimpleName();
		}
	}
	
	private class ToggleWindow extends Binding {
		private final Widget window;
		private final String windowName;
		
		private ToggleWindow(Widget window, String windowName) {
			this.window = window;
			this.windowName = windowName;
		}
		
		@Override public String getActionName() {
			return "Toggle" + windowName;
		}

		@Override public void run() {
			window.setVisible(!window.isVisible());
		}
	}
	
	private class ToggleQuickbarPopup extends Binding {
		@Override public void run() {
			Game.mainViewer.getQuickbarViewer().showQuickbarPopup();
		}
	}
	
	private class CancelMovement extends Binding {
		@Override public void run() {
			Game.mainViewer.getMainPane().cancelAllOrders();
		}
	}
	
	private class ShowMenu extends Binding {
		@Override public void run() {
			InGameMenu menu = new InGameMenu(Game.mainViewer);
			menu.openPopupCentered();
		}
	}
	
	private class EndTurn extends Binding {
		@Override public void run() {
			if (Game.mainViewer.getMainPane().isEndTurnEnabled())
				Game.areaListener.nextTurn();
		}
	}
	
	private class Quicksave extends Binding {
		@Override public void run() {
			Game.mainViewer.quickSave();
		}
	}
	
	private class ActivateQuickbarSlot extends Binding {
		private final int index;
		
		private ActivateQuickbarSlot(int index) {
			this.index = index;
		}
		
		@Override public String getActionName() {
			return "ActivateQuickbarSlot" + index;
		}
		
		@Override public void run() {
			Game.mainViewer.getQuickbarViewer().getButtonAtViewIndex(index).activateSlot(Game.mainViewer.mouseX - 2,
					Game.mainViewer.mouseY - 25);
		}
	}
}
