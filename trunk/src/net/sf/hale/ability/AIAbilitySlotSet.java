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

package net.sf.hale.ability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability.ActionType;
import net.sf.hale.ability.Ability.GroupType;
import net.sf.hale.ability.Ability.RangeType;
import net.sf.hale.widgets.RightClickMenu;
import net.sf.hale.widgets.RightClickMenuLevel;

/**
 * A class for efficient storage of the set of activateable ability slots
 * contained in a given Creature's CreatureAbilitySet.  This class contains
 * many helper functions to allow the Creature's AI to easily and efficiently
 * pick the best AbilitySlot to activate in any given situation.
 * @author Jared Stephen
 *
 */

public class AIAbilitySlotSet {
	private Map<ActionType, List<AbilitySlot>> slotsByAction;
	private Map<GroupType, List<AbilitySlot>> slotsByGroup;
	private Map<RangeType, List<AbilitySlot>> slotsByRange;
	
	/**
	 * Creates a new AIAbilitySlotSet from the AbilitySlots contained in the
	 * specified Map.  Only AbilitySlots that have a non null ActionType,
	 * GroupType, or RangeType and AbilitySlots that are currently
	 * activateable by their parent will be included in this set.
	 * 
	 * @param slots the set of AbilitySlots that will make up this
	 * AIAbilitySlotSet.  The top level of the map is keyed on Ability Type.
	 * Each contained type then contains a list of all AbilitySlots readying
	 * Abilities of that type.
	 */
	
	public AIAbilitySlotSet(Map<String, List<AbilitySlot>> slots) {
		slotsByAction = new HashMap<ActionType, List<AbilitySlot>>();
		slotsByGroup = new HashMap<GroupType, List<AbilitySlot>>();
		slotsByRange = new HashMap<RangeType, List<AbilitySlot>>();
		
		for (String type : slots.keySet()) {
			for (AbilitySlot slot : slots.get(type)) {
				if (!slot.canActivate() && !slot.canDeactivate()) continue;
				
				Ability ability = slot.getAbility();
				
				if (ability.getActionType() != null) add(ability.getActionType(), slot);
				
				if (ability.getGroupType() != null) add(ability.getUpgradedGroupType(slot.getParent()), slot);
				
				if (ability.getRangeType() != null) add(ability.getUpgradedRangeType(slot.getParent()), slot);
			}
		}
		
		// sort by usefulness of abilities
		for (ActionType type : slotsByAction.keySet()) {
			Collections.sort(slotsByAction.get(type), new AbilityComparator());
		}
		
		for (GroupType type : slotsByGroup.keySet()) {
			Collections.sort(slotsByGroup.get(type), new AbilityComparator());
		}
		
		for (RangeType type : slotsByRange.keySet()) {
			Collections.sort(slotsByRange.get(type), new AbilityComparator());
		}
	}
	
	/**
	 * Returns the number of abilities in this set with one of the specified action types
	 * @param actionTypes the actionTypes to search for
	 * @return the number of abilities in this set with one of the specified action types
	 */
	
	public int getNumAbilitiesOfActionType(String[] actionTypes) {
		int total = 0;
		
		for (String actionTypeID : actionTypes) {
			ActionType type = ActionType.valueOf(actionTypeID);
			
			if (slotsByAction.containsKey(type))
				total += slotsByAction.get(type).size();
		}
		
		return total;
	}
	
	/**
	 * Returns a List of all AbilitySlots in this AIAbilitySlotSet that have
	 * the specified ActionType.  The returned list can be modified if it is
	 * non-empty, and the modifications will affect this AIAbilitySlotSet.
	 * 
	 * @param actionTypeID the ID (enum name) of the ActionType
	 * @return the List of AbilitySlots with the specified ActionType
	 */
	
	public List<AbilitySlot> getWithActionType(String actionTypeID) {
		ActionType actionType = ActionType.valueOf(actionTypeID);
		
		if (!slotsByAction.containsKey(actionType)) return Collections.emptyList();
		
		return slotsByAction.get(actionType);
	}
	
	/**
	 * Returns a List of all AbilitySlots in this AIAbilitySlotSet that have
	 * the specified GroupType.  The returned list can be modified if it is
	 * non-empty, and the modifications will affect this AIAbilitySlotSet.
	 * 
	 * @param groupTypeID the ID (enum name) of the GroupType
	 * @return the List of AbilitySlots with the specified GroupType
	 */
	
	public List<AbilitySlot> getWithGroupType(String groupTypeID) {
		GroupType groupType = GroupType.valueOf(groupTypeID);
		
		if (!slotsByGroup.containsKey(groupType)) return Collections.emptyList();
		
		return slotsByGroup.get(groupType);
	}
	
	/**
	 * Returns a List of all AbilitySlots in this AIAbilitySlotSet that have
	 * the specified RangeType.  The returned list can be modified if it is
	 * non-empty, and the modifications will affect this AIAbilitySlotSet.
	 * 
	 * @param rangeTypeID the ID (enum name) of the RangeType
	 * @return the List of AbilitySlots with the specified RangeType
	 */
	
	public List<AbilitySlot> getWithRangeType(String rangeTypeID) {
		RangeType rangeType = RangeType.valueOf(rangeTypeID);
		
		if (!slotsByRange.containsKey(rangeType)) return Collections.emptyList();
		
		return slotsByRange.get(rangeType);
	}
	
	/**
	 * Sorts the specified list of ability slots to be in order with the range type of the
	 * specified type first, and subsequent entries ordered by their closeness to the specified range
	 * @param slots the list of slots to sort
	 * @param order "CLOSEST" to sort with shortest distance range types first, "FURTHEST" to sort with
	 * longest range types first
	 */
	
	public void sortByRangeType(List<AbilitySlot> slots, String order) {
		if (order.equals("CLOSEST")) {
			Collections.sort(slots, new RangeSorter(+1));
		} else if (order.equals("FURTHEST")) {
			Collections.sort(slots, new RangeSorter(-1));
		} else {
			throw new IllegalArgumentException("Range type sort order must be either CLOSEST or FURTHEST");
		}
	}
	
	/**
	 * Sorts the specified list of ability slots to be in the specified order
	 * @param slots the list of slots to sort
	 * @param order "SINGLE" to sort with single targeted abilities first, "MULTIPLE" to sort with
	 * multiple targeted abilities first
	 */
	
	public void sortByGroupType(List<AbilitySlot> slots, String order) {
		if (order.equals("SINGLE")) {
			Collections.sort(slots, new GroupSorter(+1));
		} else if (order.equals("MULTIPLE")) {
			Collections.sort(slots, new GroupSorter(-1));
		} else {
			throw new IllegalArgumentException("Group type sort order must be either SINGLE or MULTIPLE");
		}
	}
	
	/**
	 * Calls the standard AbilityActivateCallback for the given AbilitySlot.
	 * If the AbilitySlot creates a targeter in its onActivate script function,
	 * then returns that Targeter.  If the script opens a menu in its onActivate,
	 * selects the specified menu selection and then attempts to return the targeter from that.
	 * If no menu selection is found with the specified text, selects a menu item randomly instead
	 * This method is used by AI scripts to activate AbilitySlots.
	 * 
	 * @param slot the AbilitySlot to activate
	 * @param menuSelection the menu selection to make if a menu is opened
	 * @return the Targeter created by the onActivate script function
	 */
	
	public Targeter activateAndGetTargeter(AbilitySlot slot, String menuSelection) {
		Targeter curTargeter = tryActivateSlotAndGetTargeter(slot);
		
		RightClickMenu menu = Game.mainViewer.getMenu();
		
		if (curTargeter == null && (menu.isOpen() || menu.isOpening()) ) {
			synchronized(menu) {
				// don't show the menu popup if it hasn't opened yet
				// or close it if it has
				menu.hide();
			}
			
			RightClickMenuLevel level = menu.getLowestMenuLevel();
			
			// attempt to select the specified menu selection
			boolean activated = false;
			for (int i = 0; i < level.getNumSelections(); i++) {
				String text = level.getSelectionText(i);
				
				if (text.equals(menuSelection)) {
					level.activateSelection(i);
					activated = true;
					break;
				}
			}
			
			// fall back to a random selection if needed
			if (!activated) {
				level.activateSelection(Game.dice.rand(0, level.getNumSelections() - 1));
			}
		}
		
		return Game.areaListener.getTargeterManager().getCurrentTargeter();
	}
	
	/**
	 * Calls the standard AbilityActivateCallback for the given AbilitySlot.
	 * If the AbilitySlot creates a targeter in its onActivate script function,
	 * then returns that Targeter.  If the script opens a menu in its onActivate,
	 * make a random menu selection and then attempts to return the targeter from that
	 * This method is used by AI scripts to activate AbilitySlots.
	 * 
	 * @param slot the AbilitySlot to activate
	 * @return the Targeter created by the onActivate script function
	 */
	
	public Targeter activateAndGetTargeter(AbilitySlot slot) {
		Targeter curTargeter = tryActivateSlotAndGetTargeter(slot);
		
		RightClickMenu menu = Game.mainViewer.getMenu();
		
		if (curTargeter == null && (menu.isOpen() || menu.isOpening()) ) {
			synchronized(menu) {
				// don't show the menu popup if it hasn't opened yet
				// or close it if it has
				menu.hide();
			}
			
			RightClickMenuLevel level = menu.getLowestMenuLevel();
			
			int max = level.getNumSelections();
			
			level.activateSelection(Game.dice.rand(0, max - 1));
		}
		
		return Game.areaListener.getTargeterManager().getCurrentTargeter();
	}
	
	private Targeter tryActivateSlotAndGetTargeter(AbilitySlot slot) {
		if (slot.canActivate()) {
			new AbilityActivateCallback(slot, ScriptFunctionType.onActivate).run();
		} else if (slot.canDeactivate()) {
			new AbilityActivateCallback(slot, ScriptFunctionType.onDeactivate).run();
		}
		
		return Game.areaListener.getTargeterManager().getCurrentTargeter();
	}
	
	private void add(ActionType type, AbilitySlot slot) {
		if (slotsByAction.containsKey(type)) {
			slotsByAction.get(type).add(slot);
		} else {
			List<AbilitySlot> slots = new ArrayList<AbilitySlot>();
			slots.add(slot);
			slotsByAction.put(type, slots);
		}
	}
	
	private void add(GroupType type, AbilitySlot slot) {
		if (slotsByGroup.containsKey(type)) {
			slotsByGroup.get(type).add(slot);
		} else {
			List<AbilitySlot> slots = new ArrayList<AbilitySlot>();
			slots.add(slot);
			slotsByGroup.put(type, slots);
		}
	}
	
	private void add(RangeType type, AbilitySlot slot) {
		if (slotsByRange.containsKey(type)) {
			slotsByRange.get(type).add(slot);
		} else {
			List<AbilitySlot> slots = new ArrayList<AbilitySlot>();
			slots.add(slot);
			slotsByRange.put(type, slots);
		}
	}
	
	private class GroupSorter implements Comparator<AbilitySlot> {
		private int sense;
		
		/**
		 * creates a new groupsorter
		 * @param sense +1 to sort single to multiple, -1 to sort multiple to single
		 */
		
		private GroupSorter(int sense) {
			this.sense = sense;
		}

		@Override public int compare(AbilitySlot o1, AbilitySlot o2) {
			return sense * (o1.getAbility().getUpgradedGroupType(o1.getParent()).ordinal() -
					o2.getAbility().getUpgradedGroupType(o2.getParent()).ordinal());
		}
	}
	
	private class RangeSorter implements Comparator<AbilitySlot> {
		private int sense;
		
		/**
		 * creates a new rangesorter
		 * @param sense +1 to sort closest to furthest, -1 to sort furthest to closest
		 */
		
		private RangeSorter(int sense) {
			this.sense = sense;
		}
		
		@Override public int compare(AbilitySlot arg0, AbilitySlot arg1) {
			return sense * (arg0.getAbility().getUpgradedRangeType(arg0.getParent()).ordinal() -
					arg1.getAbility().getUpgradedRangeType(arg1.getParent()).ordinal());
		}
		
	}
	
	private class AbilityComparator implements Comparator<AbilitySlot> {
		@Override public int compare(AbilitySlot a, AbilitySlot b) {
			return b.getAbility().getUpgradedAIPower(b.getParent()) -
					a.getAbility().getUpgradedAIPower(a.getParent());
		}
	}
}
