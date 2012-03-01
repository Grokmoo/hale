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

import de.matthiasmann.twl.Color;

import net.sf.hale.Sprite;
import net.sf.hale.entity.Creature;
import net.sf.hale.loading.Saveable;

/**
 * A QuickbarSlot is a single slot within the Quickbar.  A slot can hold one usable
 * Item or Ability, or one or two equippable Items (A weapon and shield is the only
 * way to have two).
 * 
 * @author Jared Stephen
 *
 */

public interface QuickbarSlot extends Saveable {
	/**
	 * Returns the Sprite that should be displayed by the viewer for this
	 * QuickbarSlot.
	 * 
	 * @return the Sprite associated with this QuickbarSlot
	 */
	
	public Sprite getSprite();
	
	/**
	 * Returns the Color that the specified Sprite should be drawn by the
	 * viewer for this QuickbarSlot.
	 * 
	 * @return the Color for the associated Sprite
	 */
	
	public Color getSpriteColor();
	
	/**
	 * Returns the secondary Sprite that should be displayed by the viewer for this
	 * QuickbarSlot, if one exists.
	 * 
	 * @return the secondary Sprite
	 */
	
	public Sprite getSecondarySprite();
	
	/**
	 * Returns the color that the secondary sprite should be drawn by the viewer for
	 * this QuickbarSlot
	 * 
	 * @return the Color for the secondary Sprite.
	 */
	
	public Color getSecondarySpriteColor();
	
	/**
	 * Returns the text that will be displayed on the left side of the
	 * QuickbarSlotButton.  Should be at most 5 characters.
	 * 
	 * @return the label text
	 */
	
	public String getLabelText();
	
	/**
	 * Returns the text that will displayed as a tooltip for the QuickbarSlotButton
	 * viewing this QuickbarSlot.
	 * @return the tooltip text
	 */
	
	public String getTooltipText();
	
	/**
	 * Returns true if this QuickbarSlot is currently activateable.  This is
	 * also used in setting the draw color for the viewer.
	 * 
	 * @return true if and only if this QuickbarSlot is currently activateable.
	 */
	
	public boolean isActivateable();
	
	/**
	 * Activates this QuickbarSlot and performs some action, such as equipping an
	 * item, using an item, or activating an ability.
	 */
	
	public void activate();
	
	/**
	 * Creates the right click menu for this QuickbarSlot and adds the set of
	 * appropriate buttons to the menu.
	 * @param parent the parent Widget creating the right click menu
	 */
	
	public void createRightClickMenu(QuickbarSlotButton parent);
	
	/**
	 * Returns a String that is used to save this quickbar slot to a character file
	 * @return the String used to save the quickbar slot
	 */
	
	public String getSaveDescription();
	
	/**
	 * Returns a new QuickbarSlot that is a copy of this slot
	 * @param parent the parent for the new quickbarslot
	 * @return a new QuickbarSlot
	 */
	
	public QuickbarSlot getCopy(Creature parent);
}
