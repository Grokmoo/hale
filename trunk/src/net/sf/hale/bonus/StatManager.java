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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.Damage;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.Role;
import net.sf.hale.rules.RoleSet;

public class StatManager {
	private enum RecomputeMode {
		Removal, Addition
	};
	
	private final Creature parent;
	
	private final Map<Stat, Integer> stats;
	
	private final BonusManager bonuses;
	
	public StatManager(Creature parent) {
		this.parent = parent;
		this.bonuses = new BonusManager();
		this.stats = new HashMap<Stat, Integer>();
	}
	
	public StatManager(StatManager other, Creature parent) {
		this.parent = parent;
		
		this.bonuses = new BonusManager(other.bonuses);
		
		this.stats = new HashMap<Stat, Integer>(other.stats);
	}
	
	public void removeEffectPenaltiesOfType(String bonusType) {
		Bonus.Type type = Bonus.parseType(bonusType);
		
		this.removeAll(parent.getEffects().getPenaltiesOfType(type));
	}
	
	public void removeEffectBonusesOfType(String bonusType) {
		Bonus.Type type = Bonus.parseType(bonusType);
		
		this.removeAll(parent.getEffects().getBonusesOfType(type));
	}
	
	public int reducePenaltiesOfTypeByAmount(String bonusType, int amount) {
		if (amount == 0) return 0;
		
		BonusList bonusesToRemove = new BonusList();
		BonusList bonusesToAdd = new BonusList();
		
		Bonus.Type type = Bonus.parseType(bonusType);
		
		int amountLeft = amount;
		
		for (Effect effect : parent.getEffects().getEffectsWithBonusesOfType(type)) {
			for (Bonus bonus : effect.getBonuses()) {
				if (bonus.getType() == type) {
					int bonusValue = -bonus.getValue();
					
					if (bonusValue >= amountLeft) {
						Bonus newBonus = bonus.cloneWithReduction(-amountLeft);
						amountLeft = 0;
						
						effect.getBonuses().add(newBonus);
						
						bonusesToRemove.add(bonus);
						bonusesToAdd.add(newBonus);
					} else {
						bonusesToRemove.add(bonus);
						
						amountLeft -= bonusValue;
					}
				}
				
				if (amountLeft == 0) break;
			}
			
			if (amountLeft == 0) break;
		}
		
		this.removeAll(bonusesToRemove);
		this.addAll(bonusesToAdd);
		
		return amountLeft;
	}
	
	public void changeEquipment(Item.ItemType itemType) {
		switch (itemType) {
		case WEAPON: case SHIELD:
			// recompute attack bonus for shield swaps due to shield attack penalty
			recomputeAttackBonus();
			// recompute armor class for weapon swaps for a few fringe cases where weapons affect your AC
		case ARMOR: case GLOVES: case HELMET: case BOOTS:
			recomputeArmorClass();
			break;
		}
	}
	
	private void checkRecompute(BonusList bonuses, RecomputeMode mode, int oldConBonus) {
		boolean recomputeArmorClass = false;
		boolean recomputeAttackBonus = false;
		boolean recomputeStr = false;
		boolean recomputeDex = false;
		boolean recomputeCon = false;
		boolean recomputeInt = false;
		boolean recomputeWis = false;
		boolean recomputeCha = false;
		
		for (Bonus bonus : bonuses) {
			switch (bonus.getType()) {
			case Immobilized: case Initiative: case ArmorClass: case ArmorPenalty:
			case ArmorTypeArmorPenalty: case ArmorTypeMovementPenalty: case ArmorTypeArmorClass:
			case DualWieldArmorClass: case Movement: case ImmobilizationImmunity: case UndispellableImmobilized:
				recomputeArmorClass = true;
				break;
			case Attack: case Damage: case AttackCost: case ShieldAttack:
			case MainHandAttack: case OffHandAttack: case MainHandDamage: case OffHandDamage:
			case BaseWeaponAttack: case BaseWeaponDamage: case BaseWeaponSpeed:
			case BaseWeaponCriticalChance: case BaseWeaponCriticalMultiplier:
			case LightMeleeWeaponDamage: case OneHandedMeleeWeaponDamage: case TwoHandedMeleeWeaponDamage:
			case RangedDamage: case RangedAttack: case DualWieldAttack: case DualWieldStrDamage:
			case LightMeleeWeaponAttack: case OneHandedMeleeWeaponAttack: case TwoHandedMeleeWeaponAttack:
				recomputeAttackBonus = true;
				break;
			case BaseStr: case Str:
				recomputeStr = true;
				break;
			case BaseDex: case Dex:
				recomputeDex = true;
				break;
			case BaseCon: case Con:
				recomputeCon = true;
				break;
			case BaseInt: case Int:
				recomputeInt = true;
				break;
			case BaseWis: case Wis:
				recomputeWis = true;
				break;
			case BaseCha: case Cha:
				recomputeCha = true;
				break;
			case TemporaryHP:
				switch (mode) {
				case Removal:
					parent.removeTemporaryHP(bonus.getValue());
					break;
				case Addition:
					parent.addTemporaryHP(bonus.getValue());
					break;
				}
				break;
			}
		}
		
		if (recomputeStr && recomputeDex) {
			recomputeStrNoAttackBonus();
			recomputeDex();
			recomputeArmorClass = false;
			recomputeAttackBonus = false;
		} else if (recomputeStr) {
			recomputeStr();
			recomputeAttackBonus = false;
		} else if (recomputeDex) {
			recomputeDex();
			recomputeArmorClass = false;
			recomputeAttackBonus = false;
		}
		
		if (recomputeCon) recomputeCon();
		if (recomputeInt) recomputeInt();
		if (recomputeWis) recomputeWis();
		if (recomputeCha) recomputeCha();
		
		if (recomputeArmorClass) recomputeArmorClass();
		if (recomputeAttackBonus) recomputeAttackBonus();
		
		int currentConBonus = this.get(Bonus.Type.Con);
		
		if (currentConBonus != oldConBonus) {
			int oldConHP = (getCon() - currentConBonus + oldConBonus - 10) * get(Stat.CreatureLevel) / 3;
			int newConHP = (getCon() - 10) * get(Stat.CreatureLevel) / 3;
			
			if (oldConHP > newConHP) {
				parent.takeDamage(oldConHP - newConHP, "Effect");
			} else if (oldConHP < newConHP) {
				parent.healDamage(newConHP - oldConHP);
			}
		}
	}
	
	public void removeAll(BonusList bonuses) {
		int oldConBonus = this.get(Bonus.Type.Con);
		
		this.bonuses.removeAll(bonuses);
		
		checkRecompute(bonuses, RecomputeMode.Removal, oldConBonus);
	}
	
	public void addAll(BonusList bonuses) {
		int oldConBonus = this.get(Bonus.Type.Con);
		
		this.bonuses.addAll(bonuses);
		
		checkRecompute(bonuses, RecomputeMode.Addition, oldConBonus);
	}
	
	private void addAllNoRecompute(BonusList bonuses) {
		this.bonuses.addAll(bonuses);
	}
	
	public boolean has(String type) {
		return has(Bonus.parseType(type));
	}
	
	public boolean has(Bonus.Type type) {
		return bonuses.has(type);
	}
	
	public int get(String superType, Bonus.Type type) {
		return bonuses.get(superType, type);
	}
	
	public int getDamageReduction(String damageType) {
		return bonuses.getDamageReduction(Game.ruleset.getDamageType(damageType));
	}
	
	public int getDamageReduction(DamageType damageType) {
		return bonuses.getDamageReduction(damageType);
	}
	
	public int getDamageImmunity(String damageType) {
		return bonuses.getDamageImmunity(Game.ruleset.getDamageType(damageType));
	}
	
	public int getDamageImmunity(DamageType damageType) {
		return bonuses.getDamageImmunity(damageType);
	}
	
	public int get(Bonus.Type type, Bonus.StackType stackType) {
		return bonuses.get(type, stackType);
	}
	
	public int get(Bonus.Type type, Bonus.StackType... stackTypes) {
		int total = 0;
		
		for (Bonus.StackType t : stackTypes) {
			total += bonuses.get(type, t);
		}
		
		return total;
	}
	
	public int get(Bonus.Type type) {
		return bonuses.get(type);
	}
	
	public int get(Stat stat) {
		if (stats.containsKey(stat)) return stats.get(stat);
		else return 0;
	}
	
	public boolean hasWeaponProficiency(String baseWeapon) {
		if ( baseWeapon.equals(Game.ruleset.getString("DefaultBaseWeapon")) ) return true;
		
		return bonuses.hasWeaponProficiency(baseWeapon);
	}
	
	public boolean hasArmorProficiency(String armorType) {
		if ( armorType.equals(Game.ruleset.getString("DefaultArmorType")) ) return true;
		
		return bonuses.hasArmorProficiency(armorType);
	}
	
	public int getSkillBonus(String skillID) {
		return bonuses.getSkillBonus(skillID);
	}
	
	public Damage rollStandaloneDamage(Creature parent) {
		return bonuses.rollStandaloneDamage(parent);
	}
	
	public int getAppliedDamage(int damage, DamageType damageType) {
		return bonuses.getAppliedDamage(damage, damageType);
	}
	
	private void addToStat(Stat stat, int addedValue) {
		int currentValue = stats.get(stat);
		
		stats.put(stat, currentValue + addedValue);
	}
	
	private void zeroStats(Stat... statsToZero) {
		for (Stat stat : statsToZero) {
			stats.put(stat, 0);
		}
	}
	
	public void recomputeStr() {
		stats.put(Stat.Str, getBaseStr() + get(Bonus.Type.Str));
		recomputeWeightLimit();
		recomputeAttackBonus();
	}
	
	// for use when recomputing all stats so we don't compute attackBonus twice - once for str and again for dex
	public void recomputeStrNoAttackBonus() {
		stats.put(Stat.Str, getBaseStr() + get(Bonus.Type.Str));
		recomputeWeightLimit();
	}
	
	public void recomputeDex() {
		stats.put(Stat.Dex, getBaseDex() + get(Bonus.Type.Dex));
		recomputeReflexResistance();
		recomputeArmorClass();
		recomputeAttackBonus();
	}
	
	public void recomputeCon() {
		stats.put(Stat.Con, getBaseCon() + get(Bonus.Type.Con));
		recomputeLevelAndMaxHP();
		recomputePhysicalResistance();
	}
	
	public void recomputeInt() {
		stats.put(Stat.Int, getBaseInt() + get(Bonus.Type.Int));
	}
	
	public void recomputeWis() {
		stats.put(Stat.Wis, getBaseWis() + get(Bonus.Type.Wis));
		recomputeMentalResistance();
	}
	
	public void recomputeCha() {
		stats.put(Stat.Cha, getBaseCha() + get(Bonus.Type.Cha));
	}
	
	public void recomputeLevelAndMaxHP() {
		RoleSet roleSet = parent.getRoles();
		zeroStats(Stat.LevelAttackBonus, Stat.LevelDamageBonus, Stat.MaxHP);
		
		stats.put(Stat.CasterLevel, roleSet.getCasterLevel());
		stats.put(Stat.CreatureLevel, roleSet.getTotalLevel());
		
		for (String roleID : roleSet.getRoleIDs()) {
			Role role = Game.ruleset.getRole(roleID);
			int level = roleSet.getLevel(roleID);
			
			addToStat(Stat.LevelAttackBonus, level * role.getAttackBonusPerLevel());
			addToStat(Stat.LevelDamageBonus, level * role.getDamageBonusPerLevel());
			
			if (role == roleSet.getBaseRole()) {
				addToStat(Stat.MaxHP, role.getLevel1HP() + ( (level - 1) * role.getHPPerLevel()) );
			} else {
				addToStat(Stat.MaxHP, level * role.getHPPerLevel());
			}
		}
		
		addToStat(Stat.MaxHP, ((getCon() - 10) * get(Stat.CreatureLevel)) / 3);
	}
	
	public void recomputeMentalResistance() {
		stats.put(Stat.MentalResistance, (getWis() - 10) * 2 + getCreatureLevel() * 3);
	}
	
	public void recomputePhysicalResistance() {
		stats.put(Stat.PhysicalResistance, (getCon() - 10) * 2 + getCreatureLevel() * 3);
	}
	
	public void recomputeReflexResistance() {
		stats.put(Stat.ReflexResistance, (getDex() - 10) * 2 + getCreatureLevel() * 3);
	}
	
	public void recomputeWeightLimit() {
		stats.put(Stat.WeightLimit, Game.ruleset.getValue("WeightLimitBase") +
				(getStr() - 10) * Game.ruleset.getValue("WeightLimitStrengthFactor"));
	}
	
	public void recomputeArmorClass() {
		int sizeModifier = parent.getRace().getSize().modifier;
		
		float itemsArmorClass = 0.0f;
		float itemsArmorPenalty = 0.0f;
		float itemsMovementPenalty = 0.0f;
		float itemsShieldAC = 0.0f;
		
		Item mainWeapon = parent.getInventory().getEquippedItem(Inventory.EQUIPPED_MAIN_HAND);
		if (mainWeapon == null) mainWeapon = parent.getRace().getDefaultWeapon();
		
		Item offItem = parent.getInventory().getEquippedItem(Inventory.EQUIPPED_OFF_HAND);
		Item offWeapon = (offItem != null && offItem.getItemType() == Item.ItemType.WEAPON) ? offItem : null;
		
		List<Item> armorItems = new ArrayList<Item>(5);
		armorItems.add(parent.getInventory().getEquippedItem(Inventory.EQUIPPED_ARMOR));
		armorItems.add(parent.getInventory().getEquippedItem(Inventory.EQUIPPED_HELMET));
		armorItems.add(parent.getInventory().getEquippedItem(Inventory.EQUIPPED_GLOVES));
		armorItems.add(parent.getInventory().getEquippedItem(Inventory.EQUIPPED_BOOTS));
		armorItems.add(parent.getInventory().getEquippedItem(Inventory.EQUIPPED_OFF_HAND));
		for (Item item : armorItems) {
			if (item == null) continue;
			
			switch (item.getItemType()) {
			case WEAPON: continue;
			case SHIELD:
				itemsShieldAC += item.getQualityArmorClass() * 
					(100.0f + get(item.getArmorType().getName(), Bonus.Type.ArmorTypeArmorClass)) / 100.0f;
			default:
				itemsArmorClass += item.getQualityArmorClass() * 
					(100.0f + get(item.getArmorType().getName(), Bonus.Type.ArmorTypeArmorClass)) / 100.0f;
				itemsArmorPenalty += item.getQualityArmorPenalty() *
					(100.0f - get(item.getArmorType().getName(), Bonus.Type.ArmorTypeArmorPenalty)) / 100.0f;
				itemsMovementPenalty += item.getQualityMovementPenalty() *
					(100.0f - get(item.getArmorType().getName(), Bonus.Type.ArmorTypeMovementPenalty)) / 100.0f;
			}
		}
		// we double counted shield AC bonus
		itemsArmorClass -= itemsShieldAC;
		
		if (offWeapon != null) itemsShieldAC = get(Bonus.Type.DualWieldArmorClass);
		
		zeroStats(Stat.ArmorClass, Stat.TouchArmorClass, Stat.ArmorPenalty, Stat.MovementBonus);
		zeroStats(Stat.MovementCost, Stat.InitiativeBonus);
		
		addToStat(Stat.InitiativeBonus, get(Bonus.Type.Initiative));
		addToStat(Stat.ArmorPenalty, get(Bonus.Type.ArmorPenalty) + (int)itemsArmorPenalty);
		
		// immobilization immunity prevents being slowed down as well
		int baseMovementBonus = get(Bonus.Type.Movement);
		if (this.has(Bonus.Type.ImmobilizationImmunity) && baseMovementBonus < 0)
			baseMovementBonus = 0;
		
		addToStat(Stat.MovementBonus, baseMovementBonus - (int)itemsMovementPenalty);
		addToStat(Stat.MovementCost, parent.getRace().getMovementCost() * (100 - get(Stat.MovementBonus)) / 100);
		
		int deflection = this.get(Bonus.Type.ArmorClass, Bonus.StackType.DeflectionBonus, Bonus.StackType.DeflectionPenalty);
		int naturalArmor = this.get(Bonus.Type.ArmorClass, Bonus.StackType.NaturalArmorBonus, Bonus.StackType.NaturalArmorPenalty);
		int armor = this.get(Bonus.Type.ArmorClass, Bonus.StackType.ArmorBonus, Bonus.StackType.ArmorPenalty);
		int shield = this.get(Bonus.Type.ArmorClass, Bonus.StackType.ShieldBonus, Bonus.StackType.ShieldPenalty);
		
		int dodgeAC = this.get(Bonus.Type.ArmorClass, Bonus.StackType.StackableBonus, Bonus.StackType.StackablePenalty);
		
		float armorModifier = (100.0f - this.get(Stat.ArmorPenalty)) / 100.0f;
		
		// if dex AC value is positive, it is decreased by the armor modifier
		// if it is negative, apply the full amount
		float dexACValue = 4.0f * (getDex() - 10.0f);
		int dexACBonus = 0;
		if (dexACValue > 0.0) dexACBonus = (int)(dexACValue * armorModifier);
		else dexACBonus = (int)dexACValue;
		
		armor = Math.max(armor, (int)itemsArmorClass);
		shield = Math.max(shield, (int)itemsShieldAC);
		
		addToStat(Stat.ArmorClass, 50 + sizeModifier + deflection + naturalArmor + armor + shield);
		addToStat(Stat.TouchArmorClass, 50 + sizeModifier + deflection + shield);
		
		if (parent.isImmobilized()) {
			addToStat(Stat.ArmorClass, -20);
			addToStat(Stat.TouchArmorClass, -20);
		} else {
			addToStat(Stat.ArmorClass, dodgeAC + dexACBonus);
			addToStat(Stat.TouchArmorClass, dodgeAC + dexACBonus);
			addToStat(Stat.InitiativeBonus, (getDex() - 10) * 2);
		}
	}
	
	public void recomputeAttackBonus() {
		int sizeModifier = parent.getRace().getSize().modifier;
		
		Item mainWeapon = parent.getInventory().getEquippedItem(Inventory.EQUIPPED_MAIN_HAND);
		if (mainWeapon == null) mainWeapon = parent.getRace().getDefaultWeapon();
		
		Item offItem = parent.getInventory().getEquippedItem(Inventory.EQUIPPED_OFF_HAND);
		Item offWeapon = (offItem != null && offItem.getItemType() == Item.ItemType.WEAPON) ? offItem : null;
		
		zeroStats(Stat.MainHandAttackBonus, Stat.MainHandDamageBonus, Stat.OffHandAttackBonus, Stat.OffHandDamageBonus);
		zeroStats(Stat.AttackCost, Stat.TouchAttackBonus, Stat.ShieldAttackPenalty);
		
		if (offItem != null && offItem.getItemType() == Item.ItemType.SHIELD) {
			addToStat( Stat.ShieldAttackPenalty,
					Math.min(0, get(Bonus.Type.ShieldAttack) - offItem.getShieldAttackPenalty()) );
		}
		
		String mainBaseWeapon = mainWeapon.getBaseWeapon().getName();
		
		addToStat(Stat.MainHandAttackBonus, get(Bonus.Type.MainHandAttack) + get(Bonus.Type.Attack) + sizeModifier);
		addToStat(Stat.MainHandDamageBonus, get(Bonus.Type.MainHandDamage) + get(Bonus.Type.Damage));
		addToStat(Stat.MainHandAttackBonus, get(mainBaseWeapon, Bonus.Type.BaseWeaponAttack));
		addToStat(Stat.MainHandDamageBonus, get(mainBaseWeapon, Bonus.Type.BaseWeaponDamage));
		addToStat(Stat.MainHandAttackBonus, get(Stat.ShieldAttackPenalty));
		
		switch (mainWeapon.getWeaponType()) {
		case MELEE:
			
			switch (mainWeapon.getWeaponHandedForCreature(parent.getRace().getSize())) {
			case LIGHT:
				addToStat(Stat.MainHandAttackBonus, (Math.max(getDex(), getStr()) - 10) * 2);
				addToStat(Stat.MainHandDamageBonus, get(Bonus.Type.LightMeleeWeaponDamage));
				addToStat(Stat.MainHandAttackBonus, get(Bonus.Type.LightMeleeWeaponAttack));
				break;
			case ONE_HANDED:
				addToStat(Stat.MainHandAttackBonus, get(Bonus.Type.OneHandedMeleeWeaponAttack) + (getStr() - 10) * 2);
				addToStat(Stat.MainHandDamageBonus, get(Bonus.Type.OneHandedMeleeWeaponDamage));
				break;
			case TWO_HANDED:
				addToStat(Stat.MainHandAttackBonus, get(Bonus.Type.TwoHandedMeleeWeaponAttack) + (getStr() - 10) * 2);
				addToStat(Stat.MainHandDamageBonus, get(Bonus.Type.TwoHandedMeleeWeaponDamage));
			}
			
			if (offWeapon == null) addToStat(Stat.MainHandDamageBonus, 8 * (getStr() - 10));
			
			break;
		case CROSSBOW:
			addToStat(Stat.MainHandAttackBonus, (getDex() - 10) * 2 + get(Bonus.Type.RangedAttack));
			addToStat(Stat.MainHandDamageBonus, get(Bonus.Type.RangedDamage));
			break;
		default:
			addToStat(Stat.MainHandAttackBonus, (getDex() - 10) * 2 + get(Bonus.Type.RangedAttack));
			addToStat(Stat.MainHandDamageBonus, Math.min( (getStr() - 10) * 8, mainWeapon.getMaxStrengthBonus() ));
			addToStat(Stat.MainHandDamageBonus, get(Bonus.Type.RangedDamage));
		}
		
		int mainWeaponSpeedBonus = get(mainBaseWeapon, Bonus.Type.BaseWeaponSpeed) + get(Bonus.Type.AttackCost);
		int mainWeaponSpeed = mainWeapon.getAttackCost() * (100 - mainWeaponSpeedBonus) / 100;
		int offWeaponSpeed = 0;
		
		if (offWeapon != null) {
			String offBaseWeapon = offWeapon.getBaseWeapon().getName();
			
			addToStat(Stat.OffHandAttackBonus, get(Bonus.Type.OffHandAttack) + get(Bonus.Type.Attack) + sizeModifier);
			addToStat(Stat.OffHandDamageBonus, get(Bonus.Type.OffHandDamage) + get(Bonus.Type.Damage));
			addToStat(Stat.OffHandAttackBonus, get(offBaseWeapon, Bonus.Type.BaseWeaponAttack));
			addToStat(Stat.OffHandDamageBonus, get(offBaseWeapon, Bonus.Type.BaseWeaponDamage));
			
			int offWeaponSpeedBonus = get(offBaseWeapon, Bonus.Type.BaseWeaponSpeed) + get(Bonus.Type.AttackCost);
			offWeaponSpeed = offWeapon.getAttackCost() * (100 - offWeaponSpeedBonus) / 100;
			
			int offWeaponLightBonus = 0;
			
			switch (offWeapon.getWeaponHandedForCreature(parent.getRace().getSize())) {
			case LIGHT:
				addToStat(Stat.OffHandAttackBonus, (getDex() - 10) * 2 + get(Bonus.Type.LightMeleeWeaponAttack));
				addToStat(Stat.OffHandDamageBonus, get(Bonus.Type.LightMeleeWeaponDamage));
				offWeaponLightBonus = 10;
				break;
			case ONE_HANDED:
				addToStat(Stat.OffHandAttackBonus, (getStr() - 10) * 2 + get(Bonus.Type.OneHandedMeleeWeaponAttack));
				addToStat(Stat.OffHandDamageBonus, get(Bonus.Type.OneHandedMeleeWeaponDamage));
				break;
			}
			
			addToStat(Stat.OffHandDamageBonus, (3 + get(Bonus.Type.DualWieldStrDamage)) * (getStr() - 10));
			addToStat(Stat.MainHandDamageBonus, (5 + get(Bonus.Type.DualWieldStrDamage)) * (getStr() - 10));
			
			addToStat(Stat.MainHandAttackBonus, get(Bonus.Type.DualWieldAttack) - 25 + offWeaponLightBonus);
			addToStat(Stat.OffHandAttackBonus, get(Bonus.Type.DualWieldAttack) - 35 + offWeaponLightBonus);
		}
		
		addToStat(Stat.AttackCost, Math.max(mainWeaponSpeed, offWeaponSpeed));
		
		addToStat(Stat.TouchAttackBonus, (getDex() - 10) * 2 + sizeModifier + get(Bonus.Type.Attack));
	}
	
	public void recomputeAllStats() {
		bonuses.clear();
		
		synchronized(parent.getEffects()) {
			for (Effect effect: parent.getEffects()) {
				addAllNoRecompute(effect.getBonuses());
			}
		}
		
		for (Item item : parent.getInventory().getEquippedItems()) {
			if (item == null) continue;
			
			addAllNoRecompute(item.getAllAppliedBonuses());
		}
		
		recomputeStrNoAttackBonus();
		recomputeDex();
		recomputeCon();
		recomputeInt();
		recomputeWis();
		recomputeCha();
	}
	
	public void setStat(String stat, int value) {
		setStat(Stat.valueOf(stat), value);
	}
	
	public void setStat(Stat stat, int value) {
		stats.put(stat, value);
		
		recomputeAllStats();
	}
	
	public void setAttributes(int[] attributes) {
		if (attributes == null) return;
		
		if (attributes.length == 6) {
			stats.put(Stat.BaseStr, attributes[0]);
			stats.put(Stat.BaseDex, attributes[1]);
			stats.put(Stat.BaseCon, attributes[2]);
			stats.put(Stat.BaseInt, attributes[3]);
			stats.put(Stat.BaseWis, attributes[4]);
			stats.put(Stat.BaseCha, attributes[5]);
		}
	}
	
	public int getCasterLevel() { return get(Stat.CasterLevel); }
	public int getCreatureLevel() { return get(Stat.CreatureLevel); }
	public int getLevelDamageBonus() { return get(Stat.LevelDamageBonus); }
	public int getLevelAttackBonus() { return get(Stat.LevelAttackBonus); }
	public int getMaxHP() { return get(Stat.MaxHP); }
	
	public int getBaseStr() { return get(Stat.BaseStr) + get(Bonus.Type.BaseStr); }
	public int getBaseDex() { return get(Stat.BaseDex) + get(Bonus.Type.BaseDex); }
	public int getBaseCon() { return get(Stat.BaseCon) + get(Bonus.Type.BaseCon); }
	public int getBaseInt() { return get(Stat.BaseInt) + get(Bonus.Type.BaseInt); }
	public int getBaseWis() { return get(Stat.BaseWis) + get(Bonus.Type.BaseWis); }
	public int getBaseCha() { return get(Stat.BaseCha) + get(Bonus.Type.BaseCha); }
	
	public int getStr() { return get(Stat.Str); }
	public int getDex() { return get(Stat.Dex); }
	public int getCon() { return get(Stat.Con); }
	public int getInt() { return get(Stat.Int); }
	public int getWis() { return get(Stat.Wis); }
	public int getCha() { return get(Stat.Cha); }
	
	public int getMentalResistance() { return get(Stat.MentalResistance) + get(Bonus.Type.MentalResistance); }
	public int getPhysicalResistance() { return get(Stat.PhysicalResistance) + get(Bonus.Type.PhysicalResistance); }
	public int getReflexResistance() { return get(Stat.ReflexResistance) + get(Bonus.Type.ReflexResistance); }
	
	public int getWeightLimit() { return get(Stat.WeightLimit) + get(Bonus.Type.WeightLimit); }
	
	public boolean isHidden() { return bonuses.has(Bonus.Type.Hidden); }
	
	public boolean isImmobilized() {
		return (bonuses.has(Bonus.Type.Immobilized) || bonuses.has(Bonus.Type.UndispellableImmobilized))
				&& !bonuses.has(Bonus.Type.ImmobilizationImmunity);
	}
	
	public boolean isHelpless() {
		return (bonuses.has(Bonus.Type.Helpless) || bonuses.has(Bonus.Type.UndispellableHelpless))
				&& !bonuses.has(Bonus.Type.ImmobilizationImmunity);
	}
	
	public int getAttacksOfOpportunity() { return 1 + get(Bonus.Type.AttacksOfOpportunity); }
	
	public int getAttackCost() { return stats.get(Stat.AttackCost); }
	public int getMovementCost() { return stats.get(Stat.MovementCost); }
}
