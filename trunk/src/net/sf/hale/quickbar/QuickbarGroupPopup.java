/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2015 Jared Stephen
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
import net.sf.hale.ability.Ability;
import net.sf.hale.entity.PC;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A popup showing the abilities in a quickbar group
 * @author Jared
 *
 */

public class QuickbarGroupPopup extends Widget {
	private final QuickbarViewer viewer;
	private final QuickbarGroup group;
	private final QuickbarGroupButton groupButton;
	
	/**
	 * Creates a new quickbargroup popup
	 * @param owner
	 * @param viewer
	 * @param groupButton
	 */
	
	public QuickbarGroupPopup(QuickbarViewer viewer, QuickbarGroupButton groupButton) {
		this.viewer = viewer;
		this.group = groupButton.getGroup();
		this.groupButton = groupButton;
		PC parent = Game.curCampaign.party.getSelected();
		
		// add buttons for relevant abilities
		for (Ability ability : group.getAbilities()) {
			if (!parent.abilities.has(ability)) continue;
			
			QuickbarGroupSlotButton slotButton = new QuickbarGroupSlotButton(-1);
			slotButton.setSlot(new AbilityActivateSlot(ability, parent), parent.quickbar);
			slotButton.setShowIndexLabel(false);
			slotButton.setDisabledExceptActivate(true);
			slotButton.addCallback(new Runnable() {
				@Override public void run() {
					QuickbarGroupPopup.this.viewer.setHoverPopup(null);
				}
			});
			add(slotButton);
		}
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
	}
	
	@Override public void layout() {
		setPosition(groupButton.getX() - getWidth() / 2 + groupButton.getWidth() / 2,
				groupButton.getInnerY() - getHeight());
		
		int xIndex = 0;
		int x = 0;
		int y = 0;
		int rowHeight = 0;
		int maxX = 0;
		
		for (int index = 0; index < getNumChildren(); index++) {
			if (xIndex == Quickbar.GroupButtonsPerRow) {
				xIndex = 0;
				
				x = 0;
				y += rowHeight;
				rowHeight = 0;
			}
			
			Widget child = getChild(index);
			
			child.setPosition(getInnerX() + x, getInnerY() + y);
			child.setSize(child.getPreferredWidth(), child.getPreferredHeight());
			
			x += child.getWidth();
			rowHeight = Math.max(rowHeight, child.getHeight());
			maxX = Math.max(maxX, x);
			xIndex++;
		}
		
		setSize(maxX + getBorderHorizontal(), y + rowHeight + getBorderVertical());
	}
	
	@Override protected boolean handleEvent(Event evt) {
		groupButton.handleHover(evt);

		return evt.isMouseEvent();
	}
	
	private class QuickbarGroupSlotButton extends QuickbarSlotButton {
		public QuickbarGroupSlotButton(int index) {
			super(index);
			setTheme("quickbarslotbutton");
		}
		
		@Override protected boolean handleEvent(Event evt) {
			groupButton.handleHover(evt);
			
			return super.handleEvent(evt);
		}
		
	}
}
