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

package net.sf.hale.widgets;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.ability.AbilityExamineCallback;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Merchant;
import net.sf.hale.view.DragAndDropHandler;
import net.sf.hale.view.DragTarget;

/**
 * A Widget for viewing the Icon associated with an {@link net.sf.hale.ability.Ability}
 * and for showing a menu to perform actions with that Ability
 * 
 * @author Jared Stephen
 *
 */

public class AbilityIconViewer extends IconViewer implements DragTarget {
	private DragAndDropHandler dragAndDropHandler;
	
	private Ability ability;
	private Creature parent;
	
	/**
	 * Create a new AbilityIconViewer for viewing the specified Ability.
	 * The tooltip text will be the name of the ability;
	 * 
	 * @param ability the Ability to view
	 * @param parent the Creature owning this Ability
	 */
	
	public AbilityIconViewer(Ability ability, Creature parent) {
		super(SpriteManager.getSprite(ability.getIcon()),
				ability.getName() + " (Click for details)");
		
		this.ability = ability;
		this.parent = parent;
	}
	
	@Override protected boolean handleEvent(Event evt) {
		int x = evt.getMouseX();
		int y = evt.getMouseY();
		
		switch (evt.getType()) {
		case MOUSE_DRAGGED:
			// only drag active abilities
			if (dragAndDropHandler == null && ability.isActivateable()) {
				dragAndDropHandler = new DragAndDropHandler(this);
			}
			break;
		case MOUSE_BTNUP:
			if (!isMouseInside(evt)) break;
			switch (evt.getMouseButton()) {
			case Event.MOUSE_LBUTTON:
				AbilityExamineCallback cb = new AbilityExamineCallback(ability, this, parent);
				cb.setWindowCenter(x, y);
				cb.run();
				break;
			case Event.MOUSE_RBUTTON:
				if (Game.mainViewer != null) createRightClickMenu(x, y);
			}
			break;
		}
		
		if (dragAndDropHandler != null) {
			if (!dragAndDropHandler.handleEvent(evt)) {
				dragAndDropHandler = null;
			}
		}
		
		return super.handleEvent(evt);
	}
	
	private void createRightClickMenu(int x, int y) {
		RightClickMenu menu = Game.mainViewer.getMenu();
		menu.clear();
		menu.addMenuLevel(ability.getName());
		menu.setPosition(x - 2, y - 25);
		
		// add ready callback if needed
		if (!ability.isFixed() && parent.getAbilities().getNumberOfEmptySlotsOfType(ability.getType()) > 0) {
			Button readyInSlot = new Button("Ready in Slot");
			readyInSlot.addCallback(new AbilityReadyCallback());
			menu.addButton(readyInSlot);
		}
		
		// add examine callback
		Button examine = new Button("View Details");
		AbilityExamineCallback cb = new AbilityExamineCallback(ability, this, parent);
		cb.setWindowCenter(x, y);
		examine.addCallback(cb);
		menu.addButton(examine);
		
		menu.show();
		
		// show popup immediately
		if (menu.shouldPopupToggle()) {
			menu.togglePopup();
		}
	}
	
	/**
	 * The callback this is run when the "Ready in Slot" button is clicked
	 * on an Ability's right click menu
	 * @author Jared Stephen
	 *
	 */
	
	private class AbilityReadyCallback implements Runnable {
		@Override public void run() {
			Game.mainViewer.getMenu().hide();
			
			AbilitySlot slot = parent.getAbilities().getFirstEmptySlotOfType(ability.getType());
			parent.getAbilities().readyAbilityInSlot(ability, slot);
			Game.mainViewer.updateInterface();
		}
	}

	@Override public Sprite getDragSprite() {
		return SpriteManager.getSprite(ability.getIcon());
	}

	@Override public Color getDragSpriteColor() {
		return Color.WHITE;
	}

	@Override public Item getItem() { return null; }
	@Override public Creature getItemParent() { return null; }
	@Override public Container getItemContainer() { return null; }
	@Override public Merchant getItemMerchant() { return null; }
	@Override public int getItemEquipSlot() { return -1; }
	
	@Override public Ability getAbility() {
		return ability;
	}

	@Override public AbilityActivator getAbilityParent() {
		return parent;
	}

	
}
