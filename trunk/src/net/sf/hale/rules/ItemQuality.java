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

public class ItemQuality {
	private String name;
	private int armorPenaltyBonus;
	private int armorClassBonus;
	private int movementPenaltyBonus;
	private int attackBonus;
	private int damageBonus;
	private int modifier;
	private int valueAdjustment;
	
	public ItemQuality(String name, int armorPenaltyBonus, int armorClassBonus, int movementPenaltyBonus,
					   int attackBonus, int damageBonus, int modifier, int valueAdjustment) {
		
		this.name = name;
		this.armorPenaltyBonus = armorPenaltyBonus;
		this.armorClassBonus = armorClassBonus;
		this.movementPenaltyBonus = movementPenaltyBonus;
		this.attackBonus = attackBonus;
		this.damageBonus = damageBonus;
		this.modifier = modifier;
		this.valueAdjustment = valueAdjustment;
	}
	
	public String getName() { return name; }
	public int getArmorPenaltyBonus() { return armorPenaltyBonus; }
	public int getArmorClassBonus() { return armorClassBonus; }
	public int getMovementPenaltyBonus() { return movementPenaltyBonus; }
	public int getAttackBonus() { return attackBonus; }
	public int getDamageBonus() { return damageBonus; }
	public int getModifier() { return modifier; }
	public int getValueAdjustment() { return valueAdjustment; }
}
