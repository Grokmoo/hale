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

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.entity.PC;
import net.sf.hale.widgets.MessageBox;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A widget for viewing an entire Quickbar
 * 
 * @author Jared Stephen
 *
 */

public class QuickbarViewer extends Widget {
	private Quickbar quickbar;
	private List<QuickbarSlotButton> buttons;
	private List<QuickbarGroupButton> groupButtons;
	
	private Widget hover;
	
	private int buttonGap, rowGap;
	
	// quickbar viewer holds the message box due to the layout
	private MessageBox messageBox;
	
	/**
	 * Create a new QuickbarViewer widget.  The widget
	 * is empty until a {@link #setQuickbar(Quickbar)} is
	 * called.
	 */
	
	public QuickbarViewer() {
		buttons = new ArrayList<QuickbarSlotButton>(Quickbar.ItemSlots);

		groupButtons = new ArrayList<QuickbarGroupButton>();
		for (QuickbarGroup group : Game.ruleset.getAllQuickbarGroups()) {
			QuickbarGroupButton button = new QuickbarGroupButton(this, group);
			groupButtons.add(button);
			add(button);
		}
		
		for (int i = 0; i < Quickbar.ItemSlots; i++) {
			QuickbarSlotButton button = new QuickbarSlotButton(i);
			buttons.add(button);
			add(button);
		}
		
		setQuickbar(quickbar);
		
		messageBox = new MessageBox();
		add(messageBox);
	}
	
	/**
	 * Returns the message box that is being held by this quickbar viewer
	 * @return the message box
	 */
	
	public MessageBox getMessageBox() {
		return messageBox;
	}
	
	@Override protected void layout() {
		int curX = getInnerX();
		for (Button b : groupButtons) {
			b.setSize(b.getPreferredWidth(), b.getPreferredHeight());
			b.setPosition(curX, getInnerBottom() - b.getHeight());
			
			curX = b.getRight() + buttonGap;
		}
		
		for (Button b : buttons) {
			b.setSize(b.getPreferredWidth(), b.getPreferredHeight());
			b.setPosition(curX, getInnerBottom() - b.getHeight());
			
			curX = b.getRight() + buttonGap;
		}
		
		messageBox.setSize(getRight() - buttons.get(0).getX(), messageBox.getPreferredHeight());
		messageBox.setPosition(buttons.get(0).getX(), getY());
	}
	
	@Override public int getPreferredInnerWidth() {
		int width = 0;
		for (Button b : groupButtons) {
			width += b.getPreferredWidth() + buttonGap;
		}
		for (Button b : buttons) {
			width += b.getPreferredWidth() + buttonGap;
		}
		
		return width;
	}
	
	@Override public int getPreferredInnerHeight() {
		return groupButtons.get(0).getPreferredHeight() + rowGap + messageBox.getPreferredHeight() - getBorderTop();
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		buttonGap = themeInfo.getParameter("buttonGap", 0);
		rowGap = themeInfo.getParameter("rowGap", 0);
	}
	
	/**
	 * Sets the Quickbar being viewed by this Widget to the specified Quickbar
	 * 
	 * @param quickbar the Quickbar to be viewed
	 */
	
	public void setQuickbar(Quickbar quickbar) {
		this.quickbar = quickbar;
		if (quickbar == null) {
			for (QuickbarSlotButton button : buttons) {
				button.setSlot(null, null);
			}
		} else {
			for (int i = 0; i < Quickbar.ItemSlots; i++) {
				QuickbarSlot slot = quickbar.getSlot(i);
				buttons.get(i).setSlot(slot, quickbar);
			}
		}
	}
	
	/**
	 * Finds the QuickbarSlotButton that is under the specified mouse coordinates,
	 * if any, and returns the Quickbar index corresponding to that Slot.
	 * 
	 * @param x the mouse x coordinate
	 * @param y the mouse y coordinate
	 * @return the Quickbar index the mouse coordinates are over, or -1 if the mouse
	 * is not over a QuickbarSlotButton
	 */
	
	public int findSlotIndexUnderMouse(int x, int y) {
		for (QuickbarSlotButton button : buttons) {
			if (button.isInside(x, y))
				return button.getIndex();
		}
		
		return -1;
	}
	
	/**
	 * Returns the QuickbarSlotButton with the specified index
	 * 
	 * @param index the Quickbar index of the QuickbarSlotButton to find.
	 * @return the QuickbarSlotButton viewing the specified index.
	 */
	
	public QuickbarSlotButton getButton(int index) {
		if (index < 0 || index >= Quickbar.ItemSlots) return null; 
		
		return buttons.get(index);
	}
	
	/**
	 * Returns the QuickbarSlot at the specified view index.  This is the index of the
	 * Button as shown on the screen from left to right.  (Note that the displayed index
	 * is 1 greater than the actual index)
	 * 
	 * @param index the view index of QuickbarSlotButton to retrieve
	 * @return the QuickbarSlotButton at the specified view index
	 */
	
	public QuickbarSlotButton getButtonAtViewIndex(int index) {
		return buttons.get(index);
	}
	
	/**
	 * Returns the Quickbar that is currently being viewed by this QuickbarViewer.
	 * Returns null if no Quickbar is being viewed.
	 * @return the Quickbar currently being viewed by this QuickbarViewer
	 */
	
	public Quickbar getQuickbar() { return quickbar; }
	
	/**
	 * All buttons in this QuickbarViewer are updated with the current state of the
	 * associated Quickbar entry.  For example, cooldown rounds and item quantities
	 * are updated.
	 * 
	 * @param selected the Creature that is currently selected, whose Quickbar should
	 * be displayed
	 */
	
	public void updateContent(PC selected) {
		if (selected != null && quickbar != selected.quickbar) {
			setQuickbar(selected.quickbar);
			
			// set enabled status of the group buttons
			for (QuickbarGroupButton groupButton : groupButtons) {
				boolean hasAbility = false;
				for (Ability ability : groupButton.getGroup().getAbilities()) {
					if (selected.abilities.has(ability)) {
						hasAbility = true;
						break;
					}
				}
				
				groupButton.setEnabled(hasAbility);
			}
		}
	}
	
	/**
	 * Adds the specified widget at the hover over for the quickbar.  removes
	 * any existing hover over
	 * @param popup
	 */
	
	public void setHoverPopup(Widget popup) {
		if (this.hover != null) Game.mainViewer.removeChild(this.hover);
		
		this.hover = popup;
		
		if (this.hover != null) {
			Game.mainViewer.add(popup);
		}
	}
	
	/**
	 * removes the specified hover if it is the current hover.  does not remove
	 * the existing hover if it is not the passed in widget
	 * @param popup
	 */
	
	public void removeHoverPopup(Widget popup) {
		if (this.hover == popup) {
			Game.mainViewer.removeChild(this.hover);
			this.hover = null;
		}
	}
}
