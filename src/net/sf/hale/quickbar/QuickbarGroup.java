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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.hale.ability.Ability;
import net.sf.hale.icon.Icon;
import net.sf.hale.icon.IconFactory;
import net.sf.hale.util.SimpleJSONObject;

/**
 * a set of activateable abilities listed together under a common grouping in the quickbar
 * @author Jared
 *
 */

public class QuickbarGroup {
	private final String name;
	private final Icon icon;
	private final String tooltip;
	private final List<Ability> abilities;
	
	/**
	 * Creates a new QuickbarGroup by parsing the JSON
	 * @param data
	 * @param abilitiesByGroup
	 */
	
	public QuickbarGroup(SimpleJSONObject data, Map<String, List<Ability>> abilitiesByGroup) {
		this.name = data.get("name", null);
		this.tooltip = data.get("tooltip", null);
		
		if (data.containsKey("icon")) {
			icon = IconFactory.createIcon(data.getObject("icon"));
		} else {
			icon = IconFactory.emptyIcon;
		}
		
		List<Ability> abilities = abilitiesByGroup.get(this.name);
		if (abilities == null) {
			this.abilities = Collections.emptyList();
		} else {
			this.abilities = Collections.unmodifiableList(abilities);
		}
	}
	
	/**
	 * Returns the mouseover text for the button associated with this group
	 * @return the mouseover text
	 */
	
	public String getTooltip() { return tooltip; }
	
	/**
	 * Returns the icon for this group
	 * @return the icon
	 */
	
	public Icon getIcon() { return icon; }
	
	/**
	 * Returns the name / ID of this group
	 * @return the name / ID
	 */
	
	public String getName() { return name; }
	
	/**
	 * Returns the list of abilities in this group.  Note that this list may not be
	 * modified
	 * @return the list of abilities in this group
	 */
	
	public List<Ability> getAbilities() { return abilities; }
}