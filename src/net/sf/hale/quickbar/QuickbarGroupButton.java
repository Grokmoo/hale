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

import net.sf.hale.icon.Icon;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;

/**
 * Class for a button that when clicked shows all available abilities in a quickbargroup
 * @author Jared
 *
 */

public class QuickbarGroupButton extends Button {
	private QuickbarViewer viewer;
	private final QuickbarGroup group;
	private Icon icon;
	
	private QuickbarGroupPopup popup;
	private boolean isHovering;
	private boolean isClicked;
	
	/**
	 * Creates a new button for the specified group
	 * @param viewer
	 * @param group
	 */
	
	public QuickbarGroupButton(QuickbarViewer viewer, QuickbarGroup group) {
		this.viewer = viewer;
		this.group = group;
		setTooltipContent(group.getTooltip());
	}
	
	@Override protected void layout() {
		super.layout();
	}
	
	/**
	 * Handle the specified event to check for hover.  the hover over popup
	 * should pass its events to this
	 * @param evt
	 */
	
	protected void handleHover(Event evt) {
		if (evt.isMouseEvent()) {
			boolean hover = (evt.getType() != Event.Type.MOUSE_EXITED) &&
			(isMouseInside(evt)  || popup.isInside(evt.getMouseX(), evt.getMouseY()) );
			
			if (hover && !isHovering) {
				startHover();
			} else if (!hover && isHovering) {
				endHover();
			}
		}
	}
	
	private void startHover() {
		popup = new QuickbarGroupPopup(viewer, this);
		viewer.setHoverPopup(popup);
		isHovering = true;
	}
	
	private void endHover() {
		if (popup != null) {
			viewer.removeHoverPopup(popup);
		}
		
		isHovering = false;
	}
	
	@Override public boolean handleEvent(Event evt) {
		if (isEnabled()) {
			handleHover(evt);
		}
		
		return super.handleEvent(evt);
	}
	
	@Override public void paintWidget(GUI gui) {
		super.paintWidget(gui);
		if (icon != null) {
			icon.drawCentered(getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
		}
	}
	
	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (enabled) {
			icon = group.getIcon();
		} else {
			icon = group.getIcon().multiplyByColor(new Color(0xFF7F7F7F));
		}
	}
	
	/**
	 * Returns the group associated with this button
	 * @return the quickbarGroup
	 */
	
	public QuickbarGroup getGroup() { return group; }
}
