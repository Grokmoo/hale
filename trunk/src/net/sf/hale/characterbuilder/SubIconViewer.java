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

package net.sf.hale.characterbuilder;

import net.sf.hale.Game;
import net.sf.hale.rules.SubIcon;
import net.sf.hale.rules.SubIconList;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

/**
 * A widget for viewing a list of sub icons, typically associated with a
 * creature.  This can be used to show a creature's appearance in places
 * other than the main area.
 * @author Jared Stephen
 *
 */

public class SubIconViewer extends Widget {
	private SubIconList subIconList;
	
	/**
	 * Create an empty new SubIconViewer.
	 */
	
	public SubIconViewer() {
		this.setSize(Game.TILE_SIZE, Game.TILE_SIZE);
	}
	
	/**
	 * Create a new SubIconViewer viewing the specified subIconList.
	 * @param subIconList the SubIconList to view.
	 */
	
	public SubIconViewer(SubIconList subIconList) {
		this();
		setSubIconList(subIconList);
	}
	
	/**
	 * Sets the SubIconList being viewed to the specified.
	 * @param subIconList the SubIconList to view
	 */
	
	public void setSubIconList(SubIconList subIconList) {
		this.subIconList = subIconList;
	}
	
	@Override public int getPreferredWidth() {
		return Game.TILE_SIZE;
	}
	
	@Override public int getPreferredHeight() {
		return Game.TILE_SIZE;
	}
	
	@Override public int getMinWidth() { return Game.TILE_SIZE; }
	@Override public int getMinHeight() { return Game.TILE_SIZE; }
	
	@Override public void paintWidget(GUI gui) {
		super.paintWidget(gui);
		
		if (subIconList == null) return;
		
		for (SubIcon subIcon : this.subIconList) {
			subIcon.draw(getInnerX(), getInnerY());
		}
	}
}
