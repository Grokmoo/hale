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


package net.sf.hale.quickbar;

import net.sf.hale.Game;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * A PopupWindow that displays the entire contents of the quickbar at once
 * @author Jared
 *
 */

public class QuickbarPopup extends PopupWindow {
	private QuickbarViewer quickbarViewer;
	
	/**
	 * Creates a new Popup with the specified parent widget
	 * @param owner
	 * @param quickbarViewer
	 */
	
	public QuickbarPopup(Widget owner, QuickbarViewer quickbarViewer) {
		super(owner);
		
		this.setCloseOnEscape(true);
		this.setCloseOnClickedOutside(true);
		
		add(new Content());
		
		this.quickbarViewer = quickbarViewer;
	}
	
	@Override public void layout() {
		setSize(getPreferredWidth(), getPreferredHeight());
		
		Widget content = getChild(0);
		
		content.setSize(content.getPreferredWidth(), content.getPreferredHeight());

		setPosition(quickbarViewer.getButtonAtViewIndex(0).getX() - getBorderLeft() - getChild(0).getBorderLeft(),
				getParent().getBottom() - getHeight());
	}
	
	private class Content extends DialogLayout {
		private Content() {
			Quickbar quickbar = Game.curCampaign.party.getSelected().getQuickbar();
			
			Group mainH = createParallelGroup();
			Group mainV = createSequentialGroup();
			
			for (int rowIndex = 0; rowIndex < Quickbar.SetsOfSlots; rowIndex++) {
				Group rowH = createSequentialGroup();
				Group rowV = createParallelGroup();
				
				for (int colIndex = 0; colIndex < Quickbar.SlotsAtOnce; colIndex++) {
					int slotIndex = rowIndex * Quickbar.SlotsAtOnce + colIndex;
					
					QuickbarSlotButton slotButton = new QuickbarSlotButton(slotIndex, quickbarViewer);
					slotButton.setSlot(quickbar.getSlot(slotIndex), quickbar, slotIndex);
					slotButton.setShowIndexLabel(false);
					slotButton.setDisabledExceptActivate(true);
					slotButton.addCallback(new Runnable() {
						@Override public void run() {
							QuickbarPopup.this.closePopup();
						}
					});
					rowH.addWidget(slotButton);
					rowV.addWidget(slotButton);
				}
				
				mainH.addGroup(rowH);
				mainV.addGroup(rowV);
			}
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
		}
		
		@Override protected boolean handleEvent(Event evt) {
			if (evt.getType() == Event.Type.KEY_PRESSED) {
				if (Game.mainViewer.getKeyBindings().isCloseQuickbarPopupKey(evt.getKeyCode())) {
					QuickbarPopup.this.closePopup();
				}
			}
			
			return super.handleEvent(evt);
		}
	}

}
