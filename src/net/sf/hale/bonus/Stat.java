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

package net.sf.hale.bonus;

public enum Stat {
	CreatureLevel, CasterLevel, LevelAttackBonus, LevelDamageBonus, MaxHP,
	BaseStr, BaseDex, BaseCon, BaseInt, BaseWis, BaseCha,
	Str("Strength"), Dex("Dexterity"), Con("Constitution"), Int("Intelligence"), Wis("Wisdom"), Cha("Charisma"),
	MentalResistance, PhysicalResistance, ReflexResistance,
	WeightLimit,
	AttacksOfOpportunity,
	ArmorClass, TouchArmorClass,
	ShieldAttackPenalty, ArmorPenalty, MovementBonus, MovementCost,
	InitiativeBonus,
	AttackCost, MainHandAttackBonus, MainHandDamageBonus, OffHandAttackBonus, OffHandDamageBonus,
	TouchAttackBonus;
	
	private Stat() {
		name = toString();
	}
	
	private Stat(String name) {
		this.name = name;
	}
	
	public final String name;
};
