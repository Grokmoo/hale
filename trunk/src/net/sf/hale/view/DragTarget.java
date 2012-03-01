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

package net.sf.hale.view;

import de.matthiasmann.twl.Color;
import net.sf.hale.Sprite;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.Merchant;

/**
 * An interface for any widget that should be able to be dragged to another widget
 * via drag and drop
 * @author Jared Stephen
 *
 */

public interface DragTarget {
	/**
	 * Gets the sprite that should be shown under the cursor while dragging
	 * @return the sprite to show under the drag cursor
	 */
	
	public Sprite getDragSprite();
	
	/**
	 * Returns the color that should be applied to the drag sprite
	 * @return the color to apply to the drag sprite
	 */
	
	public Color getDragSpriteColor();
	
	/**
	 * Returns the item that this drag target can drag, or null
	 * if this drag target does not drag an item
	 * @return the item that is dragged by this drag target
	 */
	
	public Item getItem();
	
	/**
	 * Returns the parent of the item that this drag target can drag, or null
	 * if the drag target does not drag an item or the item does not have a parent creature
	 * @return the parent of the item being dragged
	 */
	
	public Creature getItemParent();
	
	/**
	 * Returns the container of the item that this drag target can drag, or null
	 * if the drag target does not drag an item or the item does not have a parent container
	 * @return the parent container of the item being dragged
	 */
	
	public Container getItemContainer();
	
	/**
	 * Returns the merchant owning the item that this drag target can drag, or null
	 * if the drag target does not drag an item or the item does not have a parent merchant
	 * @return the parent merchant of the item being dragged
	 */
	
	public Merchant getItemMerchant();
	
	/**
	 * Returns the equipment slot that the item being dragged currently resides in, or -1
	 * if the item is not an equipped item
	 * @return the equipment slot
	 */
	
	public int getItemEquipSlot();
	
	/**
	 * Returns the ability that this drag target can drag, or null
	 * if this drag target does not drag an ability
	 * @return the ability that is dragged by this drag target
	 */
	
	public Ability getAbility();
	
	/**
	 * Returns the parent of the ability that this drag target can drag, or null
	 * if this drag target does not drag an ability
	 * @return the parent of the ability that is dragged by this drag target
	 */
	
	public AbilityActivator getAbilityParent();
}
