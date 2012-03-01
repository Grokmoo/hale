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

package net.sf.hale.rules;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;

public class Damage {
	private Creature parent;
	private int appliedDamage;
	
	private List<DamageType> types;
	private List<Integer> damages;
	private List<Integer> appliedDamages;
	
	public Damage(Creature parent) {
		this.parent = parent;
		this.appliedDamage = 0;
		
		this.types = new ArrayList<DamageType>();
		this.damages = new ArrayList<Integer>();
		this.appliedDamages = new ArrayList<Integer>();
	}
	
	public Damage(Creature parent, DamageType type, int damage) {
		this(parent);
		add(type, damage);
	}
	
	public Damage(Damage other) {
		this.parent = other.parent;
		this.appliedDamage = other.appliedDamage;
		
		this.types = new ArrayList<DamageType>(other.types);
		this.damages = new ArrayList<Integer>(other.damages);
		this.appliedDamages = new ArrayList<Integer>(other.appliedDamages);
	}
	
	public void add(DamageType type, int damage) {
		int index = types.indexOf(type);
		
		if (index != -1) {
			damages.set(index, damages.get(index) + damage);
		} else {
			types.add(type);
			damages.add(damage);
			appliedDamages.add(0);
		}
	}
	
	public void clear() {
		for (int i = 0; i < types.size(); i++) {
			appliedDamages.set(i, Integer.valueOf(0));
		}
		
		appliedDamage = 0;
	}
	
	public void remove(DamageType type) {
		int index = types.indexOf(type);
		
		if (index != -1) {
			types.remove(index);
			damages.remove(index);
			appliedDamages.remove(index);
			
			// recompute total applied damage
			appliedDamage = 0;
			for (Integer damage : appliedDamages) {
				appliedDamage += damage;
			}
		}
	}
	
	public void stack(Damage other) {
		for (int i = 0; i < other.types.size(); i++) {
			add(other.types.get(i), other.damages.get(i));
		}
	}
	
	public int computeAppliedDamage(boolean message) {
		DifficultyManager diffManager = Game.ruleset.getDifficultyManager();
		
		this.appliedDamage = 0;
		
		StringBuilder str = new StringBuilder();
		
		for (int i = 0; i < types.size(); i++) {
			int baseDamageOfType = damages.get(i);
			if (parent.isPlayerSelectable()) {
				// apply difficulty settings to PCs
				baseDamageOfType = baseDamageOfType * diffManager.getDamageFactorOnPCs() / 100;
			}
			
			int damage = parent.stats().getAppliedDamage(baseDamageOfType, types.get(i));
			
			appliedDamages.set( i, damage );
			appliedDamage += damage;
			
			if (types.get(i) != null) {
				int dr = parent.stats().getDamageReduction(types.get(i));
				int percent = parent.stats().getDamageImmunity(types.get(i));
				
				str.append(" (" + baseDamageOfType + " " + types.get(i).getName());
				if (dr > 0) str.append(", DR " + dr);
				
				if (percent > 0) str.append(", " + percent + "% Immun");
				else if (percent < 0) str.append(", " + percent + "% Vuln");
				str.append(")");
			}
		}
		
		str.append(".");
		str.insert(0, parent.getName() + " takes " + appliedDamage + " damage");
		
		if (message && !parent.isImmortal() && Game.mainViewer != null) {
			Game.mainViewer.addMessage("red", str.toString());
		}
		
		return appliedDamage;
	}
	
	@Override public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Damage list: ");
		
		for (int i = 0; i < types.size(); i++) {
			str.append(damages.get(i) + " " + types.get(i).getName() + " ");
		}
		
		return str.toString();
	}
	
	public Creature getDefender() { return parent; }
	public int getAppliedDamage() { return appliedDamage; }
	
	public List<DamageType> getTypes() { return types; }
	public List<Integer> getDamages() { return damages; }
	
	public boolean causesDamage() {
		for (int damage : damages) {
			if (damage > 0) return true;
		}
		
		return false;
	}
}
