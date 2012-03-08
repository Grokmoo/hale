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

package net.sf.hale.quickbar;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.widgets.RightClickMenu;

/**
 * A quickbar slot for holding a usable item.  When activated, the specified
 * item is used if possible.
 * 
 * @author Jared Stephen
 *
 */

public class ItemUseSlot extends QuickbarSlot {
	private Item item;
	private Creature parent;
	
	private Color spriteColor;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("type", "use");
		data.put("itemID", item.getID());
		data.put("itemQuality", item.getQuality().getName());
		
		return data;
	}
	
	/**
	 * Create a new ItemUseSlot with the specified Item owned by the specified parent
	 * @param item
	 * @param parent
	 */
	
	public ItemUseSlot(Item item, Creature parent) {
		this.item = item;
		this.parent = parent;
		this.spriteColor = item.getIconColor();
		if (spriteColor == null) spriteColor = Color.WHITE;
	}
	
	@Override public Sprite getSprite() {
		return SpriteManager.getSprite(item.getIcon());
	}

	@Override public Color getSpriteColor() {
		return spriteColor;
	}

	@Override public String getLabelText() {
		int quantity = parent.getInventory().getQuantity(item);
		
		return Integer.toString(quantity);
	}

	@Override public boolean isChildActivateable() {
		return parent.getTimer().canPerformAction(item.getUseAPCost());
	}

	@Override public void childActivate() {
		if (item.canUse(parent) && parent.getInventory().getQuantity(item) > 0) {
			parent.getInventory().getCallbackFactory().getUseCallback(item).run();
		}
	}
	
	@Override public void createRightClickMenu(QuickbarSlotButton widget) {
		RightClickMenu menu = Game.mainViewer.getMenu();
		menu.addMenuLevel(item.getName());
		
		Button activate = new Button(item.getUseButtonText());
		activate.setEnabled(isActivateable());
		activate.addCallback(new QuickbarSlotButton.ActivateSlotCallback(this));
		menu.addButton(activate);
		
		Button examine = new Button("View Details");
		Runnable cb = parent.getInventory().getCallbackFactory().getDetailsCallback(item, menu.getX(), menu.getY());
		examine.addCallback(cb);
		menu.addButton(examine);
		
		Button clearSlot = new Button("Clear Slot");
		clearSlot.addCallback(new QuickbarSlotButton.ClearSlotCallback(widget));
		menu.addButton(clearSlot);
		
		menu.show();
		// show popup immediately
		if (menu.shouldPopupToggle()) {
			menu.togglePopup();
		}
	}

	@Override public String getTooltipText() {
		return "Use " + item.getFullName();
	}
	
	@Override public Sprite getSecondarySprite() { return null; }
	
	@Override public Color getSecondarySpriteColor() { return Color.WHITE; }

	@Override public String getSaveDescription() {
		return "Use \"" + item.getID() + "\" \"" + item.getQuality().getName() + "\"";
	}

	@Override public QuickbarSlot getCopy(Creature parent) {
		return new ItemUseSlot(new Item(this.item), parent);
	}
}
