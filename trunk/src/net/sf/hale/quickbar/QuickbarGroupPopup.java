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
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * A popup showing the abilities in a quickbar group
 * @author Jared
 *
 */

public class QuickbarGroupPopup extends PopupWindow {
	private final QuickbarViewer viewer;
	private final QuickbarGroup group;
	private final QuickbarGroupButton groupButton;
	
	/**
	 * Creates a new quickbargroup popup
	 * @param owner
	 * @param viewer
	 * @param groupButton
	 */
	
	public QuickbarGroupPopup(Widget owner, QuickbarViewer viewer, QuickbarGroupButton groupButton) {
		super(owner);
		
		this.viewer = viewer;
		this.group = groupButton.getGroup();
		this.groupButton = groupButton;
		
		this.setCloseOnEscape(true);
		this.setCloseOnClickedOutside(true);
		
		add(new Content());
	}
	
	@Override public void layout() {
		setSize(getPreferredWidth(), getPreferredHeight());
		
		Widget content = getChild(0);
		
		content.setSize(content.getPreferredWidth(), content.getPreferredHeight());

		setPosition(Math.min(groupButton.getX() - getWidth() / 2, super.getParent().getRight() - getWidth()),
				viewer.getY() - getHeight());
	}
	
	private class Content extends DialogLayout {
		private Content() {
			PC parent = Game.curCampaign.party.getSelected();
			Quickbar quickbar = parent.quickbar;
			
			Group mainH = createParallelGroup();
			Group mainV = createSequentialGroup();
			
			int index = 0;
			Group rowH = createSequentialGroup();
			Group rowV = createParallelGroup();
			
			for (Ability ability : group.getAbilities()) {
				if (!parent.abilities.has(ability)) continue;
				
				if (index >= Quickbar.GroupButtonsPerRow) { 
					mainH.addGroup(rowH);
					mainV.addGroup(rowV);
					
					rowH = createSequentialGroup();
					rowV = createParallelGroup();
					index = 0;
				}
				
				// add button for ability
				QuickbarSlotButton slotButton = new QuickbarSlotButton(100, viewer);
				slotButton.setSlot(new AbilityActivateSlot(ability, parent), quickbar, -1);
				slotButton.setShowIndexLabel(false);
				slotButton.setDisabledExceptActivate(true);
				slotButton.addCallback(new Runnable() {
					@Override public void run() {
						QuickbarGroupPopup.this.closePopup();
					}
				});
				rowH.addWidget(slotButton);
				rowV.addWidget(slotButton);
				
				index++;
			}
			
			if (index != 0) {
				// add the last group
				mainH.addGroup(rowH);
				mainV.addGroup(rowV);
			}
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
		}
	}
}
