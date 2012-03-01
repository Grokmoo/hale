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

package net.sf.hale.view;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Widget;
import net.sf.hale.ability.AbilitySelectionList;
import net.sf.hale.characterbuilder.AbilitySelectionListPane;
import net.sf.hale.characterbuilder.AbilitySelectorButton;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.EntityViewer;

/**
 * A window for viewing an AbilitySelectionListPane
 * @author Jared Stephen
 *
 */

public class AbilityListWindow extends GameSubWindow implements EntityViewer, AbilitySelectorButton.HoverHolder {
	private ScrollPane scrollPane;
	
	private AbilitySelectionList list;
	private Creature parent;
	
	private Widget hoverTop, hoverBottom;
	
	/**
	 * Creates a new AbilityListWindow viewing the specified AbilitySelectionList by the
	 * specified creature
	 * 
	 * @param list the list to be viewed
	 * @param parent the Creature viewing the list
	 */
	
	public AbilityListWindow(AbilitySelectionList list, Creature parent) {
		setTitle(list.getName() + " Ability Tree for " + parent.getName());
		
		scrollPane = new ScrollPane();
		add(scrollPane);
		
		this.list = list;
		this.parent = parent;
		
		parent.addViewer(this);
	}
	
	/*
	 * This overrides the default close behavior of GameSubWindow
	 * @see net.sf.hale.view.GameSubWindow#run()
	 */
	
	@Override public void run() {
		parent.removeViewer(this);
		closeViewer();
	}
	
	@Override protected void afterAddToGUI(GUI gui) {
		entityUpdated();
	}

	@Override public void entityUpdated() {
		AbilitySelectionListPane pane = new AbilitySelectionListPane(list, parent, this);
		this.scrollPane.setContent(pane);
	}

	@Override public void closeViewer() {
		getParent().removeChild(this);
	}
	
	@Override public void removeHoverWidgets(Widget hoverTop, Widget hoverBottom) {
		if (this.hoverTop != null && this.hoverTop == hoverTop) {
			removeChild(this.hoverTop);
			this.hoverTop = null;
		}
		
		if (this.hoverBottom != null && this.hoverBottom == hoverBottom) {
			removeChild(this.hoverBottom);
			this.hoverBottom = null;
		}
	}
	
	@Override public void setHoverWidgets(Widget hoverTop, Widget hoverBottom) {
		if (this.hoverTop != null) removeChild(this.hoverTop);
		if (this.hoverBottom != null) removeChild(this.hoverBottom);
		
		this.hoverTop = hoverTop;
		this.hoverBottom = hoverBottom;
		
		if (hoverTop != null) {
			add(hoverTop);
		}
		
		if (hoverBottom != null) {
			add(hoverBottom);
		}
	}
}
