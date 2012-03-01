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

package net.sf.hale.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.ability.CreatureAbilitySet;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;

/**
 * A class for adding and removing abilities from a creature in the
 * CreatureEditor
 * 
 * @author Jared Stephen
 *
 */

public class AbilityEditor extends DialogLayout {
	private String currentType;
	
	private CreatureAbilitySet abilities;
	
	private ComboBox<String> abilityTypes;
	private ListModel<String> abilityTypesModel;
	
	private ListBox<Ability> abilitiesOfType;
	private SimpleChangableListModel<Ability> abilitiesOfTypeModel;
	
	private ListBox<AbilitySlot> abilitySlotsOfType;
	private SimpleChangableListModel<AbilitySlot> abilitySlotsOfTypeModel;
	
	private ListBox<CreatureAbilitySet.AbilityInstance> ownedAbilities;
	private SimpleChangableListModel<CreatureAbilitySet.AbilityInstance> ownedAbilitiesModel;
	
	/**
	 * Creates a new AbilityEditor that is empty until populated
	 * by calling {@link #setAbilityList(CreatureAbilitySet)}.
	 */
	
	public AbilityEditor() {
		this.setTheme("/editorlayout");
		
		// get all the unique types of all abilities in the current rules
		Set<String> types = new HashSet<String>();
		for (String key : Game.ruleset.getAllAbilityIDs()) {
			types.add(Game.ruleset.getAbility(key).getType());
		}
		
		// sort the ability types alphabetically
		List<String> typesSorted = new ArrayList<String>();
		typesSorted.addAll(types);
		Collections.sort(typesSorted);
		
		Label abilityTypeLabel = new Label("Type");
		
		abilityTypesModel = new SimpleChangableListModel<String>(typesSorted);
		abilityTypes = new ComboBox<String>(abilityTypesModel);
		abilityTypes.setTheme("mediumcombobox");
		abilityTypes.addCallback(new Runnable() {
			@Override public void run() {
				setAbilityTypeFilter();
			}
		});
		
		Group topH = this.createSequentialGroup(abilityTypeLabel, abilityTypes);
		Group topV = this.createParallelGroup(abilityTypeLabel, abilityTypes);
		
		Label allAbilitiesLabel = new Label("All Abilities of Type");
		abilitiesOfTypeModel = new SimpleChangableListModel<Ability>();
		abilitiesOfType = new ListBox<Ability>(abilitiesOfTypeModel);
		abilitiesOfType.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(ListBox.CallbackReason reason) {
				switch (reason) {
				case SET_SELECTED:
					break;
				default:
					ownedAbilities.setSelected(-1);
					abilitySlotsOfType.setSelected(-1);
				}
			}
			
		});
		Button addAbilityButton = new Button("Add Ability");
		addAbilityButton.addCallback(new Runnable() {
			@Override public void run() {
				addSelectedAbility();
			}
		});
		
		Group leftH = this.createParallelGroup(allAbilitiesLabel, abilitiesOfType, addAbilityButton);
		Group leftV = this.createSequentialGroup(allAbilitiesLabel, abilitiesOfType, addAbilityButton);
		
		Label ownedAbilitiesLabel = new Label("Owned Abilities of Type");
		ownedAbilitiesModel = new SimpleChangableListModel<CreatureAbilitySet.AbilityInstance>();
		ownedAbilities = new ListBox<CreatureAbilitySet.AbilityInstance>(ownedAbilitiesModel);
		ownedAbilities.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(ListBox.CallbackReason reason) {
				switch (reason) {
				case SET_SELECTED:
					break;
				default:
					abilitiesOfType.setSelected(-1);
					abilitySlotsOfType.setSelected(-1);
				}
			}
			
		});
		Button removeAbilityButton = new Button("Remove Ability");
		removeAbilityButton.addCallback(new Runnable() {
			@Override public void run() {
				removeSelectedAbility();
			}
		});
		
		Button readyAbilityButton = new Button("Ready Ability");
		readyAbilityButton.addCallback(new Runnable() {
			@Override public void run() {
				readySelectedAbility();
			}
		});
		
		Group middleH = this.createParallelGroup(ownedAbilitiesLabel, ownedAbilities,
				readyAbilityButton, removeAbilityButton);
		Group middleV = this.createSequentialGroup(ownedAbilitiesLabel, ownedAbilities,
				readyAbilityButton, removeAbilityButton);
		
		Label slotsLabel = new Label("Ability Slots");
		abilitySlotsOfTypeModel = new SimpleChangableListModel<AbilitySlot>();
		abilitySlotsOfType = new ListBox<AbilitySlot>(abilitySlotsOfTypeModel);
		abilitySlotsOfType.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(ListBox.CallbackReason reason) {
				switch (reason) {
				case SET_SELECTED:
					break;
				default:
					ownedAbilities.setSelected(-1);
					abilitiesOfType.setSelected(-1);
				}
			}
			
		});
		Button clearSlotButton = new Button("Clear Slot");
		clearSlotButton.addCallback(new Runnable() {
			@Override public void run() {
				clearSelectedSlot();
			}
		});
		
		Group rightH = createParallelGroup(slotsLabel, abilitySlotsOfType, clearSlotButton);
		Group rightV = createSequentialGroup(slotsLabel, abilitySlotsOfType, clearSlotButton);
		
		Group mainH = this.createSequentialGroup(leftH, middleH, rightH);
		Group mainV = this.createParallelGroup(leftV, middleV, rightV);
		
		this.setHorizontalGroup(this.createParallelGroup(topH, mainH));
		this.setVerticalGroup(this.createSequentialGroup(topV, mainV));
	}
	
	private void setAbilityTypeFilter() {
		abilitiesOfTypeModel.clear();
		ownedAbilitiesModel.clear();
		abilitySlotsOfTypeModel.clear();
		
		int index = abilityTypes.getSelected();
		if (index != -1) {
			currentType = abilityTypesModel.getEntry(index);
			
			for (String key : Game.ruleset.getAllAbilityIDs()) {
				Ability ability = Game.ruleset.getAbility(key);
				
				if (ability.getType().equals(currentType)) {
					abilitiesOfTypeModel.addElement(ability);
				}
			}
			
			ownedAbilitiesModel.addElements(abilities.getAbilityInstancesOfType(currentType));
			
			abilitySlotsOfTypeModel.addElements(abilities.getSlotsOfType(currentType));
		} else {
			currentType = null;
		}
	}
	
	private void addSelectedAbility() {
		int index = this.abilitiesOfType.getSelected();
		if (index == -1) return;
		
		Ability ability = this.abilitiesOfTypeModel.getEntry(index);
		// don't add an ability the creature already possesses
		if (abilities.has(ability)) return;
		
		// TODO support adding abilities at levels other than 1
		
		abilities.add(ability, 1);
		
		ownedAbilitiesModel.clear();
		ownedAbilitiesModel.addElements(abilities.getAbilityInstancesOfType(currentType));
		
		abilitySlotsOfTypeModel.clear();
		abilitySlotsOfTypeModel.addElements(abilities.getSlotsOfType(currentType));
	}
	
	private void removeSelectedAbility() {
		int index = this.ownedAbilities.getSelected();
		if (index == -1) return;
		
		CreatureAbilitySet.AbilityInstance abilityInstance = this.ownedAbilitiesModel.getEntry(index);
		if (abilityInstance.isRacialAbility() || abilityInstance.isRoleAbility()) return;
		
		abilities.remove(abilityInstance.getAbility());
		
		ownedAbilitiesModel.clear();
		ownedAbilitiesModel.addElements(abilities.getAbilityInstancesOfType(currentType));
		
		abilitySlotsOfTypeModel.clear();
		abilitySlotsOfTypeModel.addElements(abilities.getSlotsOfType(currentType));
	}
	
	private void readySelectedAbility() {
		int index = ownedAbilities.getSelected();
		if (index == -1) return;
		
		CreatureAbilitySet.AbilityInstance abilityInstance = this.ownedAbilitiesModel.getEntry(index);
		
		if (!abilityInstance.getAbility().isActivateable()) return;
		if (abilityInstance.getAbility().isFixed()) return;
		
		List<AbilitySlot> slots = abilities.getEmptySlotsOfType(currentType);
		if (slots.isEmpty()) return;
		
		abilities.readyAbilityInSlot(abilityInstance.getAbility(), slots.get(0));
		
		this.abilitySlotsOfType.invalidateLayout();
	}
	
	private void clearSelectedSlot() {
		int index = this.abilitySlotsOfType.getSelected();
		if (index == -1) return;
		
		AbilitySlot slot = abilitySlotsOfTypeModel.getEntry(index);
		
		if (slot.isFixed()) return;
		
		abilities.readyAbilityInSlot(null, slot);
		
		this.abilitySlotsOfType.invalidateLayout();
	}
	
	/**
	 * Sets the CreatureAbilityList that this AbilityEditor will be modifying
	 * to the specified List.
	 * 
	 * @param abilities the CreatureAbilityList to edit
	 */
	
	public void setAbilityList(CreatureAbilitySet abilities) {
		this.abilities = abilities;
		
		setAbilityTypeFilter();
	}
}
