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
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilityActivateCallback;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.ability.AbilityExamineCallback;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Merchant;
import net.sf.hale.view.DragAndDropHandler;
import net.sf.hale.view.DragTarget;

/**
 * A Widget for showing the Icon and manipulating an {@link net.sf.hale.ability.AbilitySlot}.
 * 
 * @author Jared Stephen
 *
 */

public class AbilitySlotViewer extends IconViewer implements DragTarget {
	private Color disabledColor = Color.WHITE;
	
	private DragAndDropHandler dragAndDropHandler;
	
	private Label cooldownLabel;
	
	private AbilitySlot slot;
	
	/**
	 * Creates a new AbilitySlotViewer that will display the contents
	 * of the specified AbilitySlot.
	 * 
	 * @param slot the AbilitySlot to view with this Widget
	 */
	
	public AbilitySlotViewer(AbilitySlot slot) {
		this.slot = slot;

		cooldownLabel = new Label();
		cooldownLabel.setTheme("cooldownlabel");
		add(cooldownLabel);
		
		update();
	}
	
	/**
	 * Updates the state of this Widget to match any changes to the AbilitySlot being viewed
	 */
	
	public void update() {
		Ability ability = slot.getAbility();
		
		if (ability == null) {
			setTooltipContent(null);
			setSprite(null);
		} else {
			String tooltip = slot.getAbility().getName() + " (Click for details)";
			setSprite(SpriteManager.getSprite(ability.getIcon()));
			
			if (!tooltip.equals(getTooltipContent())) {
				setTooltipContent(tooltip);
			}
		}
		
		cooldownLabel.setText(slot.getLabelText());
		cooldownLabel.setVisible(slot.getCooldownRoundsLeft() > 0);
		
		if (slot.getCooldownRoundsLeft() > 0)
			setColor(disabledColor);
		else
			setColor(Color.WHITE);
		
		invalidateLayout();
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		disabledColor = themeInfo.getParameter("disabledColor", Color.WHITE);

		update();
	}
	
	@Override protected void layout() {
		super.layout();
		
		cooldownLabel.setPosition(getInnerX(), getInnerBottom() - cooldownLabel.getPreferredHeight() / 2);
	}
	
	@Override protected boolean handleEvent(Event evt) {
		int x = evt.getMouseX();
		int y = evt.getMouseY();
		
		if (slot.getAbility() != null) {
			
			switch (evt.getType()) {
			case MOUSE_DRAGGED:
				// only drag active, not passive abilities
				if (dragAndDropHandler == null && slot.getAbility().isActivateable()) {
					dragAndDropHandler = new DragAndDropHandler(this);
				}
				break;
			case MOUSE_BTNUP:
				if (!isMouseInside(evt)) break;
				switch (evt.getMouseButton()) {
				case Event.MOUSE_LBUTTON:
					AbilityExamineCallback cb = new AbilityExamineCallback(slot.getAbility(), this, slot.getParent());
					cb.setWindowCenter(x, y);
					cb.run();
					break;
				case Event.MOUSE_RBUTTON:
					if (Game.mainViewer != null) createRightClickMenu(x, y);
				}
				break;
			}
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
		menu.addMenuLevel(slot.getAbility().getName());
		menu.setPosition(x - 2, y - 25);
		
		// add activate slot callback
		if (slot.canActivate()) {
			Button activate = new Button("Activate");
			activate.addCallback(new AbilityActivateCallback(slot, ScriptFunctionType.onActivate));
			menu.addButton(activate);
		} else if (slot.canDeactivate()) {
			Button deactivate = new Button("Deactivate");
			deactivate.addCallback(new AbilityActivateCallback(slot, ScriptFunctionType.onDeactivate));
			menu.addButton(deactivate);
		}
		
		//add examine callback
		Button examine = new Button("View Details");
		AbilityExamineCallback cb = new AbilityExamineCallback(slot.getAbility(), this, slot.getParent());
		cb.setWindowCenter(x, y);
		examine.addCallback(cb);
		menu.addButton(examine);
		
		// add clear/empty slot callback
		if (slot.isSettable()) {
			Button clear = new Button("Clear Slot");
			clear.addCallback(new ClearSlotCallback());
			menu.addButton(clear);
		}
		
		menu.show();
		
		// show popup immediately
		if (menu.shouldPopupToggle()) {
			menu.togglePopup();
		}
	}
	
	/**
	 * This callback is run whenever the "Clear Slot" button is pressed
	 * 
	 * @author Jared Stephen
	 *
	 */
	
	private class ClearSlotCallback implements Runnable {
		@Override public void run() {
			slot.getParent().getAbilities().readyAbilityInSlot(null, slot);
			
			Game.mainViewer.getMenu().hide();
			Game.mainViewer.updateInterface();
		}
	}
	
	@Override public Sprite getDragSprite() {
		return SpriteManager.getSprite(slot.getAbility().getIcon());
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
		return slot.getAbility();
	}

	@Override public AbilityActivator getAbilityParent() {
		return slot.getParent();
	}
}
