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

package net.sf.hale.rules;

import net.sf.hale.Game;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileKeyMap;

public class Skill implements Comparable<Skill> {
	private final String descriptionFile;
	private final String id;
	private final String name;
	private final String verb, verbPast;
	private final String icon;
	private final String restrictToRole;
	private final boolean untrained;
	private final Stat keyAttribute;
	private final boolean hasArmorPenalty;
	private final boolean isCraftSkill;
	
	public Skill(String id) {
		this.id = id;
		
		FileKeyMap map = new FileKeyMap("skills/" + id + ResourceType.Text.getExtension());
		
		name = map.getValue("name", id);
		verb = map.getValue("verb", id);
		verbPast = map.getValue("verbpast", id);
		icon = map.getValue("icon", null);
		descriptionFile = map.getValue("descriptionfile", "descriptions/skills/" + name + ResourceType.HTML.getExtension());
		untrained = map.getValue("untrained", true);
		hasArmorPenalty = map.getValue("hasarmorpenalty", false);
		isCraftSkill = map.getValue("craft", false);
		restrictToRole = map.getValue("restricttorole", null);
		
		String keyAttributeString = map.getValue("keyattribute", null);
		keyAttribute = keyAttributeString == null ? Stat.Int : Stat.valueOf(keyAttributeString);
		
		map.checkUnusedKeys();
	}
	
	public boolean canUse(Role role) {
		if (restrictToRole == null) return true;
		
		return role.getID().equals(restrictToRole);
	}
	
	public boolean canUse(Creature creature) {
		if (restrictToRole == null) return true;
		
		Role role = Game.ruleset.getRole(restrictToRole);
		
		return (creature.getRoles().contains(role));
	}
	
	@Override public int compareTo(Skill other) {
		return this.getName().compareTo(other.getName());
	}
	
	public boolean isRestricted() { return restrictToRole != null; }
	public String getRestrictToRole() { return restrictToRole; }
	
	public String getDescription() {
		return ResourceManager.getResourceAsString(descriptionFile);
	}
	
	public String getVerbPastTense() { return verbPast; }
	public String getVerb() { return verb; }
	public String getIcon() { return icon; }
	public String getID() { return id; }
	public String getName() { return name; }
	public boolean usableUntrained() { return untrained; }
	public Stat getKeyAttribute() { return keyAttribute; }
	public boolean hasArmorPenalty() { return hasArmorPenalty; }
	public boolean isCraftSkill() { return isCraftSkill; }
}
