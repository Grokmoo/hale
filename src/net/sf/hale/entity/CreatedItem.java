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

package net.sf.hale.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A user created (enchanted) item that needs to be stored separately for saving / loading purposes
 * @author Jared
 *
 */

public class CreatedItem implements Saveable {
	private final String baseItemID;
	private final String createdItemID;
	private final List<String> scripts;

	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("baseItemID", baseItemID);
		data.put("createdItemID", createdItemID);
		data.put("scripts", scripts.toArray());
		
		return data;
	}
	
	/**
	 * Creates a new CreatedItem from the data in the specified JSON object
	 * @param data the data to load
	 * @return the new CreatedItem
	 */
	
	public static CreatedItem load(SimpleJSONObject data) {
		String baseItemID = data.get("baseItemID", null);
		String createdItemID = data.get("createdItemID", null);
		
		List<String> scripts = new ArrayList<String>();
		for (SimpleJSONArrayEntry entry : data.getArray("scripts")) {
			scripts.add(entry.getString());
		}
		
		return new CreatedItem(baseItemID, createdItemID, scripts);
	}
	
	/**
	 * Creates a new CreatedItem with the specified parameters
	 * @param baseItemID the ID of the base item (resource)
	 * @param createdItemID the ID of the created item
	 * @param scripts the list of scripts that should be applied to the created item as enchantments
	 */
	
	public CreatedItem(String baseItemID, String createdItemID, List<String> scripts) {
		this.baseItemID = baseItemID;
		this.createdItemID = createdItemID;
		this.scripts = scripts;
	}
	
	/**
	 * Creates a new CreatedItem with the specified parameters
	 * @param baseItemID the ID of the base item (resource ID)
	 * @param item the newly created item
	 */
	
	public CreatedItem(String baseItemID, Item item) {
		this.baseItemID = baseItemID;
		
		this.createdItemID = item.getID();
		
		this.scripts = new ArrayList<String>();
		
		for (Enchantment enchantment : item.getEnchantments()) {
			if (enchantment.isUser()) {
				this.scripts.add(enchantment.getScript());
			}
		}
	}
	
	/**
	 * Returns the ID of the base (resource) item
	 * @return the ID of the base item
	 */
	
	public String getBaseItemID() {
		return baseItemID;
	}
	
	/**
	 * Returns the ID of the created item
	 * @return the ID of the created item
	 */
	
	public String getCreatedItemID() {
		return createdItemID;
	}
	
	/**
	 * Gets the list of scripts that have been user added as enchantments to the created item
	 * @return the list of user added scripts
	 */
	
	public List<String> getScripts() {
		return Collections.unmodifiableList(scripts);
	}
}
