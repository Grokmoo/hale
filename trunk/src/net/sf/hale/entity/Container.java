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

import de.matthiasmann.twl.Color;
import net.sf.hale.Game;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.LootList;
import net.sf.hale.util.SimpleJSONObject;

public class Container extends Openable {
	private ItemList items;
	private LootList loot;
	private boolean workbench;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = super.save();
		
		if (loot.alreadyGenerated())
			data.put("lootAlreadyGenerated", true);
		
		if (items.size() > 0)
			data.put("items", items.save());
		
		if (getDescription().equals(Game.ruleset.getString("TempContainerDescription"))) {
			data.put("temporaryContainer", true);
		}
		
		return data;
	}
	
	@Override public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		super.load(data, refHandler);
		
		if (data.containsKey("lootAlreadyGenerated"))
			loot.setAlreadyGenerated(data.get("lootAlreadyGenerated", false));
		
		if (data.containsKey("items"))
			this.items.load(data.getArray("items"));
		else
			this.items.clear();
	}
	
	public static Container createTemporaryContainer(String id) {
		String icon = Game.ruleset.getString("LootBagIcon");
		String description = Game.ruleset.getString("TempContainerDescription");
		
		Container container = new Container(id, "Container",
				Item.ItemType.ITEM, description, icon, icon);
		container.setIconColor(Color.WHITE);
		
		return container;
	}
	
	public Container(String id, String name, Item.ItemType itemType,
			String description, String openIcon, String closedIcon) {
		super(id, name, closedIcon, itemType, description, new Currency(), openIcon, closedIcon);
		this.type = Entity.Type.CONTAINER;
		
		items = new ItemList(name);
		loot = new LootList();
		workbench = false;
	}
	
	public Container(Container other) {
		super(other);
		this.type = Entity.Type.CONTAINER;
		this.items = new ItemList(other.items);
		this.loot = new LootList(other.loot);
		this.workbench = other.workbench;
	}
	
	@Override public void open(Creature opener) {
		super.open(opener);
		
		if (this.isOpen()) {
			if (!loot.alreadyGenerated()) items.addItemsFromList(loot.generate());
		}
	}
	
	public boolean isWorkbench() { return workbench; }
	public void setWorkbench(boolean workbench) { this.workbench = workbench; }
	
	public LootList getLoot() { return loot; }
	
	public ItemList getItems() { return items; }
	
	public boolean isEmpty() { return items.size() == 0; }
	
	public int size() { return items.size(); }
	
	public int getQuantity(int index) { return items.getQuantity(index); }
	
	public Item getItem(int index) { return items.getItem(index); }
	
	public void removeItem(int index) { items.removeItem(index); }
	
	public void removeItem(Item item) { items.removeItem(item); }
	
	public void addItem(Item item) {
		items.addItem(item);
	}
	
	public void addItem(Item item, int quantity) { items.addItem(item, quantity); }
	
	public void removeItem(Item item, int quantity) { items.removeItem(item, quantity); }
	
	public void removeItem(int index, int quantity) { items.removeItem(index, quantity); }
}
