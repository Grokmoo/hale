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

package net.sf.hale.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilitySelectionList;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.ability.CreatureAbilitySet;
import net.sf.hale.entity.Creature;
import net.sf.hale.rules.Role;
import net.sf.hale.widgets.AbilityIconViewer;
import net.sf.hale.widgets.AbilitySlotViewer;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The view Widget for the set of known Abilities and AbilitySlots for a given
 * Creature.  In the Game, the AbilitiesSheet is contained within the
 * CharacterPane as one of its Tabs.
 * 
 * Each time updateContent is called, the entire contents of this view are
 * recreated, allowing a single instance to show Abilities for different Creatures
 * and to update as stats change.
 * 
 * @author Jared Stephen
 *
 */

public class AbilitiesSheet extends ScrollPane implements CreatureAbilitySet.Listener {
	private boolean abilitySetModified;
	private Creature parent;
	
	private int activePassiveGap, typeGap, viewerGap;
	
	private Content content;
	
	private Label abilityLists, active, passive;
	
	private Map<String, ActiveAbilityList> activeTypeLists;
	private Map<String, PassiveAbilityList> passiveTypeLists;
	
	private List<AbilityListButton> abilityListButtons;
	
	/**
	 * Creates a new, empty AbilitiesSheet
	 */
	
	public AbilitiesSheet() {
		setFixed(ScrollPane.Fixed.HORIZONTAL);
		// set up the overall layout
		
		content = new Content();
		setContent(content);
		
		// create the widgets
		abilityLists = new Label();
		abilityLists.setTheme("listslabel");
		
		active = new Label();
		active.setTheme("activelabel");
		
		passive = new Label();
		passive.setTheme("passivelabel");
		
		activeTypeLists = new LinkedHashMap<String, ActiveAbilityList>();
		passiveTypeLists = new LinkedHashMap<String, PassiveAbilityList>();
		
		abilityListButtons = new ArrayList<AbilityListButton>();
	}
	
	/**
	 * Called whenever the parent creature has a significant state change affecting this
	 * widget, such as adding or removing an ability from its ability set.  The next
	 * call to {@link #updateContent(Creature)} will force a full rebuild of this
	 * Widget
	 */
	
	public void abilitySetModified() {
		abilitySetModified = true;
	}
	
	/**
	 * Rebuilds the content of this Widget to show the Abilities and
	 * AbilitySlots for the specified Creature.
	 * 
	 * @param parent the Creature whose Abilities are to be shown in this
	 * Widget
	 */
	
	public void updateContent(Creature parent) {
		if (parent == this.parent) {
			if (abilitySetModified) {
				abilitySetModified = false;
				rebuildViewersList();
			} else {
				updateCurrentViewers();
			}
		} else {
			// remove this listener from old parent
			if (this.parent != null)
				parent.getAbilities().removeListener(this);
			
			this.parent = parent;
			abilitySetModified = false;
			
			parent.getAbilities().addListener(this);
			rebuildAbilityListButtons();
			rebuildViewersList();
		}
	}
	
	private void rebuildAbilityListButtons() {
		abilityListButtons.clear();
		
		Set<AbilitySelectionList> lists = new LinkedHashSet<AbilitySelectionList>();
		
		// get all the list referenced by all roles for the parent Creature
		for (String id : parent.getRoles().getRoleIDs()) {
			Role role = Game.ruleset.getRole(id);
			
			lists.addAll(role.getAllReferencedAbilitySelectionLists());
		}
		
		// get all the lists references by the race for the parent creature
		lists.addAll(parent.getRace().getAllReferencedAbilitySelectionLists());
		
		// add the list buttons
		for (AbilitySelectionList list : lists) {
			abilityListButtons.add(new AbilityListButton(list));
		}
	}
	
	private void updateCurrentViewers() {
		for (String key : activeTypeLists.keySet()) {
			ActiveAbilityList list = activeTypeLists.get(key);
			
			for (AbilitySlotViewer viewer : list.slotViewers) {
				viewer.update();
			}
		}
	}
	
	private void rebuildViewersList() {
		content.removeAllChildren();
		
		for (AbilityListButton button : abilityListButtons) {
			content.add(button);
		}
		
		content.add(abilityLists);
		content.add(active);
		content.add(passive);
		
		activeTypeLists.clear();
		passiveTypeLists.clear();
		
		for (String type : parent.getAbilities().getAllTypes()) {
			// create the lists to store passive and active abilities
			ActiveAbilityList activeList = new ActiveAbilityList(type);
			PassiveAbilityList passiveList = new PassiveAbilityList(type);
			
			// add viewers for known abilities; sort each ability on whether it is
			// active or passive
			for (Ability ability : parent.getAbilities().getAbilitiesOfType(type)) {
				// don't add viewers for fixed abilities as these will already
				// get a viewer below for their fixed, unique ability slot
				if (ability.isFixed()) continue;
				
				AbilityIconViewer viewer = new AbilityIconViewer(ability, parent);
				
				if (ability.isActivateable()) {
					activeList.abilityViewers.add(viewer);
				} else {
					passiveList.abilityViewers.add(viewer);
				}
				
				content.add(viewer);
			}
			
			// add viewers for readied ability slots; all slots go in active
			for (AbilitySlot slot : parent.getAbilities().getSlotsOfType(type)) {
				AbilitySlotViewer viewer = new AbilitySlotViewer(slot);
				activeList.slotViewers.add(viewer);
				content.add(viewer);
			}
			
			// add only the non-empty lists for each type
			if (!activeList.isEmpty()) {
				activeTypeLists.put(type, activeList);
				content.add(activeList.type);
				
				// if there are only slots and no viewers, we will show all the slots in
				// one column with the ability type label as the title
				if (activeList.hasViewers()) content.add(activeList.slots);
			}
			
			if (!passiveList.isEmpty()) {
				passiveTypeLists.put(type, passiveList);
				content.add(passiveList.type);
			}
		}
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		activePassiveGap = themeInfo.getParameter("activePassiveGap", 0);
		typeGap = themeInfo.getParameter("typeGap", 10);
		viewerGap = themeInfo.getParameter("viewerGap", 5);
	}
	
	private class AbilityListButton extends Button implements Runnable {
		private AbilitySelectionList list;
		
		private AbilityListButton(AbilitySelectionList list) {
			this.list = list;
			addCallback(this);
			setText("View " + list.getName());
		}
		
		@Override public void run() {
			AbilityListWindow window = new AbilityListWindow(list, parent);
			getGUI().getRootPane().add(window);
			window.requestKeyboardFocus();
		}
	}
	
	private class Content extends Widget {
		@Override public void layout() {
			super.layout();

			// start known abilities on the left, readied abilities in the center
			int knownBaseX = getInnerX();
			int readyBaseX = getInnerX() + getInnerWidth() / 2;

			int curY = getInnerY();
			abilityLists.setSize(abilityLists.getPreferredWidth(), abilityLists.getPreferredHeight());
			abilityLists.setPosition(knownBaseX, curY);
			curY = abilityLists.getBottom();
			
			for (AbilityListButton button : abilityListButtons) {
				button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
				button.setPosition(getInnerX() + (getInnerWidth() - button.getWidth()) / 2, curY);
				curY = button.getBottom();
			}
			
			active.setSize(active.getPreferredWidth(), active.getPreferredHeight());
			active.setPosition(knownBaseX, curY + typeGap);

			curY = active.getBottom() + typeGap;
			
			//group abilities and abilitySlots together by type.  Iterate
			//through all the active types
			for (String type : activeTypeLists.keySet()) {
				ActiveAbilityList list = activeTypeLists.get(type);

				list.type.setSize(list.type.getPreferredWidth(), list.type.getPreferredHeight());
				list.type.setPosition(knownBaseX, curY);

				// only show slots label if there are viewers and slots
				if (list.hasViewers()) list.slots.setPosition(readyBaseX, curY);

				curY = list.type.getBottom() + viewerGap;
				
				int maxRowBottom = curY;
				
				// set positions for known abilities
				int knownX = knownBaseX;
				int knownY = curY;
				for (AbilityIconViewer viewer : list.abilityViewers) {
					viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());

					// if this viewer is too far over, we will need to start a new
					// row and reset the column position to the left
					if (knownX + viewer.getWidth() + viewerGap >= readyBaseX) {
						knownX = knownBaseX;
						knownY += viewer.getHeight() + viewerGap;
					}

					viewer.setPosition(knownX, knownY);
					knownX = viewer.getRight() + viewerGap;
					
					maxRowBottom = Math.max(maxRowBottom, viewer.getBottom());
				}

				// if there are only slots and no viewers, we show this type as one
				// column, otherwise two columns
				int slotBaseX;
				if (list.hasViewers()) slotBaseX = readyBaseX;
				else slotBaseX = knownBaseX;

				// set positions for readied ability slots
				int readyX = slotBaseX;
				int readyY = curY;
				for (AbilitySlotViewer viewer : list.slotViewers) {
					viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());

					// if this viewer is too far over, start a new row
					if (readyX + viewer.getWidth() + viewerGap > getInnerRight()) {
						readyX = slotBaseX;
						readyY += viewer.getHeight() + viewerGap;
					}

					viewer.setPosition(readyX, readyY);

					readyX = viewer.getRight() + viewerGap;
					
					maxRowBottom = Math.max(maxRowBottom, viewer.getBottom());
				}

				// the next set of abilities by type starts at a new row
				// for both known and readied.  This way, known and readied
				// of the same type will always line up
				curY = maxRowBottom + typeGap;
			}

			// we have double counted typegap on the last row
			curY += activePassiveGap - typeGap;
			passive.setSize(passive.getPreferredWidth(), passive.getPreferredHeight());
			passive.setPosition(knownBaseX, curY);
			curY = passive.getBottom() + typeGap;

			// passive abilities have only one column (no ability slots)
			// so just use known positions
			for (String type : passiveTypeLists.keySet()) {
				PassiveAbilityList list = passiveTypeLists.get(type);

				list.type.setSize(list.type.getPreferredWidth(), list.type.getPreferredHeight());
				list.type.setPosition(knownBaseX, curY);

				curY = list.type.getBottom() + viewerGap;
				
				int maxRowBottom = curY;
				
				// set positions for all passive abilities of this type
				int knownX = knownBaseX;
				int knownY = curY;
				for (AbilityIconViewer viewer : list.abilityViewers) {
					viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
					// if this viewer is too far over, we will need to start a new
					// row and reset the column position to the left
					if (knownX + viewer.getWidth() + viewerGap > getInnerRight()) {
						knownX = knownBaseX;
						knownY += viewer.getHeight() + viewerGap;
					}

					viewer.setPosition(knownX, knownY);

					knownX = viewer.getRight() + viewerGap;
					
					maxRowBottom = Math.max(maxRowBottom, viewer.getBottom());
				}

				curY = maxRowBottom + typeGap;
			}

		}
	}
	
	/**
	 * A list of all the active abilities associated with a given type to
	 * be viewed by this Widget
	 * @author Jared Stephen
	 *
	 */
	
	private class ActiveAbilityList {
		private Label type;
		private Label slots;
		private List<AbilityIconViewer> abilityViewers;
		private List<AbilitySlotViewer> slotViewers;
		
		private ActiveAbilityList(String type) {
			this.type = new Label(type);
			this.type.setTheme("typelabel");
			
			this.slots = new Label("Slots");
			this.slots.setTheme("slotslabel");
			
			abilityViewers = new ArrayList<AbilityIconViewer>();
			slotViewers = new ArrayList<AbilitySlotViewer>();
		}
		
		private boolean hasViewers() {
			return abilityViewers.size() > 0;
		}
		
		private boolean isEmpty() {
			return abilityViewers.size() == 0 && slotViewers.size() == 0;
		}
	}
	
	/**
	 * A list of all the passive abilities associated with a given type to
	 * be viewed by this Widget
	 * @author Jared Stephen
	 *
	 */
	
	private class PassiveAbilityList {
		private Label type;
		private List<AbilityIconViewer> abilityViewers;
		
		private PassiveAbilityList(String type) {
			this.type = new Label(type);
			this.type.setTheme("typelabel");
			
			abilityViewers = new ArrayList<AbilityIconViewer>();
		}
		
		private boolean isEmpty() {
			return abilityViewers.size() == 0;
		}
	}
}
