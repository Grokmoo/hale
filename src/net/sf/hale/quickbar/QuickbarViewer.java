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
import net.sf.hale.entity.Creature;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;

/**
 * A widget for viewing an entire Quickbar, with buttons to scroll through
 * all the Quickbar's slots, 10 at a time.
 * 
 * @author Jared Stephen
 *
 */

public class QuickbarViewer extends DialogLayout {
	private Quickbar quickbar;
	private List<QuickbarSlotButton> buttons;
	
	private Button scrollUp;
	private Button scrollDown;
	private Label currentIndexLabel;
	
	private int currentIndex;
	
	private Button expand;
	
	/**
	 * Create a new QuickbarViewer widget.  The widget
	 * is empty until a {@link #setQuickbar(Quickbar)} is
	 * called.
	 */
	
	public QuickbarViewer() {
		currentIndexLabel = new Label();
		currentIndexLabel.setTheme("currentindexlabel");
		
		scrollUp = new Button();
		scrollUp.setTheme("incrementbutton");
		scrollUp.addCallback(new Runnable() {
			@Override public void run() {
				int index = getIndexWrapAround(currentIndex - 1);
				
				if (quickbar != null) quickbar.setLastViewSetIndex(index);
				setCurrentIndex(index);
				setQuickbar(quickbar);
			}
		});
		
		scrollDown = new Button();
		scrollDown.setTheme("decrementbutton");
		scrollDown.addCallback(new Runnable() {
			@Override public void run() {
				int index = getIndexWrapAround(currentIndex + 1);
				
				if (quickbar != null) quickbar.setLastViewSetIndex(index);
				setCurrentIndex(index);
				setQuickbar(quickbar);
			}
		});
		
		Group scrollButtonsH = this.createParallelGroup(scrollUp, scrollDown);
		Group scrollButtonsV = this.createSequentialGroup(scrollUp, scrollDown);
		
		Group mainH = this.createSequentialGroup(currentIndexLabel);
		Group mainV = this.createParallelGroup(currentIndexLabel);
		mainH.addGroup(scrollButtonsH);
		mainV.addGroup(scrollButtonsV);
		
		buttons = new ArrayList<QuickbarSlotButton>(Quickbar.SlotsAtOnce);
		for (int i = 0; i < Quickbar.SlotsAtOnce; i++) {
			QuickbarSlotButton button = new QuickbarSlotButton(i, this);
			buttons.add(button);
			
			mainH.addWidget(button);
			mainV.addWidget(button);
		}
		
		expand = new Button();
		expand.setTheme("expandbutton");
		expand.addCallback(new Runnable() {
			@Override public void run() {
				showQuickbarPopup();
			}
		});
		mainH.addWidget(expand);
		mainV.addWidget(expand);
		
		this.setHorizontalGroup(mainH);
		this.setVerticalGroup(mainV);
		
		setCurrentIndex(1);
		setQuickbar(quickbar);
	}
	
	/**
	 * Displays the popup showing the entire quickbar (with all slots) to the user
	 */
	
	public void showQuickbarPopup() {
		QuickbarPopup popup = new QuickbarPopup(Game.mainViewer, this);
		popup.openPopup();
	}
	
	private int getIndexWrapAround(int index) {
		if (index >= Quickbar.SetsOfSlots) return 0;
		else if (index < 0) return Quickbar.SetsOfSlots - 1;
		else return index;
	}
	
	/**
	 * This function should only be called with an index known to be in the valid
	 * range from 0 to Quickbar.SetsOfSlots - 1 inclusive
	 * @param index
	 */
	
	private void setCurrentIndex(int index) {
		if (index == currentIndex) return;
		
		this.currentIndex = index;
		
		currentIndexLabel.setText(Integer.toString(currentIndex));
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
				button.setSlot(null, null, 0);
			}
		} else {
			int startIndex = currentIndex * Quickbar.SlotsAtOnce;
			int i = 0;
			for (int index = startIndex; index < startIndex + Quickbar.SlotsAtOnce; index++) {
				QuickbarSlot slot = quickbar.getSlot(index);
				buttons.get(i).setSlot(slot, quickbar, index);
				i++;
			}
		}
	}
	
	/**
	 * Returns the absolute position quickbar slot index of the specified slot, or
	 * -1 if the slot is not currently being viewed by this viewer
	 * @param slotButton the slot to find the index of
	 * @return the index of the specified slot
	 */
	
	public int getSlotIndex(QuickbarSlotButton slotButton) {
		if (!buttons.contains(slotButton)) return -1;
		
		return slotButton.getIndex() + currentIndex * Quickbar.SlotsAtOnce;
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
				return button.getIndex() + currentIndex * Quickbar.SlotsAtOnce;
		}
		
		return -1;
	}
	
	/**
	 * Returns the QuickbarSlotButton that is currently displaying the QuickbarSlot
	 * with the specified Quickbar index.  Note that the QuickbarSlotButton's internal
	 * index will not in general be equal to this index.  If there is no QuickbarSlotButton
	 * currently viewing the specified index, returns null.
	 * 
	 * @param index the Quickbar index of the QuickbarSlotButton to find.
	 * @return the QuickbarSlotButton viewing the specified index.
	 */
	
	public QuickbarSlotButton getButton(int index) {
		int viewerIndex = index - currentIndex * Quickbar.SlotsAtOnce;
		
		if (viewerIndex < 0 || viewerIndex >= Quickbar.SlotsAtOnce) return null;
		
		return buttons.get(viewerIndex);
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
	
	public void updateContent(Creature selected) {
		if (selected != null) {
			Quickbar quickbar = selected.getQuickbar();
			
			if (quickbar != null) {
				int index = quickbar.getLastViewSetIndex();
				if (index != this.currentIndex) setCurrentIndex(index);
			}
			
			setQuickbar(quickbar);
		}
	}
}
