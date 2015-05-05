/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2015 Jared Stephen
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

package net.sf.hale.quickbar;

import net.sf.hale.Game;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;

/**
 * Class for a button that when clicked shows all available abilities in a quickbargroup
 * @author Jared
 *
 */

public class QuickbarGroupButton extends Button {
	private QuickbarViewer viewer;
	private final QuickbarGroup group;
	
	/**
	 * Creates a new button for the specified group
	 * @param viewer
	 * @param group
	 */
	
	public QuickbarGroupButton(QuickbarViewer viewer, QuickbarGroup group) {
		this.viewer = viewer;
		this.group = group;
	}
	
	@Override protected void layout() {
		super.layout();
	}
	
	@Override public boolean handleEvent(Event evt) {
		if (Game.interfaceLocker.locked()) return super.handleEvent(evt);
		
		switch (evt.getType()) {
		case MOUSE_DRAGGED:
		case MOUSE_BTNUP:
			if (!isMouseInside(evt)) break;
			switch (evt.getMouseButton()) {
			case Event.MOUSE_LBUTTON:
				QuickbarGroupPopup popup = new QuickbarGroupPopup(Game.mainViewer, viewer, this);
				popup.openPopup();
				break;
			}
		default:
		}
		
		return super.handleEvent(evt);
	}
	
	/**
	 * Returns the group associated with this button
	 * @return the quickbarGroup
	 */
	
	public QuickbarGroup getGroup() { return group; }
}
