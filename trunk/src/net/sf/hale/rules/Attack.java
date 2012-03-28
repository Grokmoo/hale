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

import java.util.List;

import net.sf.hale.AreaEntityList;
import net.sf.hale.Game;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/*
 * An attack should be used as follows.  Call the constructor as normal.
 * Then after that call ON_ATTACK scripts for the attacker and ON_DEFENSE scripts for the defender.
 * These can modify various parts of the attack such as the defender AC.
 * After this, call computeIsHit() which computes whether the attack hits and also finishes the attack.
 * Finally, call the ON_HIT scripts and then apply the damage to the target if you so choose.
 * 
 * Note that calling computeIsHit() causes the attacker to be updated.  Before that point, the attacker is not
 * considered to have taken the attack.  The damage from the attack is not applied to the defender
 * anywhere in this class.
 */

public class Attack {
	private int attackRoll = 0;
	private int damageRoll = 0;
	private int threatRoll = 0;
	private int baseAttackBonus = 0;
	private int attackBonus = 0;
	private float damageBonus = 0;
	private int totalAttack = 0;
	private int totalDamage = 0;
	private int rangePenalty = 0;
	private int defenderAC = 0;
	
	private int extraDamage = 0;
	private int extraAttack = 0;
	
	private boolean negateDamage = false;
	private Damage damage = null;
	
	private int damageMin = 0, damageMax = 0;
	
	private int appliedDamage = 0;
	
	private int flankingBonus = 0;
	private boolean flankingAttack = false;
	
	private boolean hit = false;
	private boolean meleeTouchAttack = false;
	private boolean rangedTouchAttack = false;
	
	private Creature attacker;
	private Creature defender;
	private Item weapon;
	private int itemSlot;
	
	private StringBuilder message;
	
	public boolean damageNegated() { return negateDamage; }
	
	public void negateDamage() { negateDamage = true; }
	
	public void addDamage(Damage damage) {
		this.damage.add(damage);
	}
	
	public Damage getDamage() { return damage; }
	
	public String getMessage() { return message.toString(); }
	
	public int getExtraAttack() { return extraAttack; }
	public int getExtraDamage() { return extraDamage; }
	public int getAppliedDamage() { return appliedDamage; }
	
	public int getFlankingBonus() { return flankingBonus; }
	public boolean isFlankingAttack() { return flankingAttack; }
	
	public boolean isMeleeWeaponAttack() {
		if (meleeTouchAttack || rangedTouchAttack) return false;
		
		if (weapon == null) return false;
		
		return weapon.isMeleeWeapon();
	}
	
	public boolean isRangedWeaponAttack() {
		if (meleeTouchAttack || rangedTouchAttack) return false;
		
		if (weapon == null) return false;
		
		return !weapon.isMeleeWeapon();
	}
	
	public boolean isRanged() {
		if (rangedTouchAttack) return true;
		
		if (weapon == null) return false;
		
		return !weapon.isMeleeWeapon();
	}
	
	public boolean causesDamage() {
		// if touch attack
		if (damage == null) return false;
		
		return damage.causesDamage();
	}
	
	public int getAttackRoll() { return attackRoll; }
	public int getDamageRoll() { return damageRoll; }
	public int getThreatRoll() { return threatRoll; }
	public int getBaseAttackBonus() { return baseAttackBonus; }
	public int getAttackBonus() { return attackBonus; }
	public float getDamageBonus() { return damageBonus; }
	public int getTotalAttack() { return totalAttack; }
	public int getTotalDamage() { return totalDamage; }
	public int getRangePenalty() { return rangePenalty; }
	public int getDefenderAC() { return defenderAC; }
	
	public int getMinimumDamage() { return damageMin; }
	public int getMaximumDamage() { return damageMax; }
	
	public Creature getAttacker() { return attacker; }
	public Creature getDefender() { return defender; }
	public Item getWeapon() { return weapon; }
	public int getItemSlot() { return itemSlot; }
	
	public void addExtraAttack(int attack) { this.extraAttack += attack; }
	public void addExtraDamage(int damage) { this.extraDamage += damage; }
	
	public void setAppliedDamage(int appliedDamage) { this.appliedDamage = appliedDamage; }
	public void setDefenderAC(int defenderAC) { this.defenderAC = defenderAC; }
	public void setFlankingBonus(int flankingBonus) { this.flankingBonus = flankingBonus; }
	public void setFlankingAttack(boolean flankingAttack) { this.flankingAttack = flankingAttack; }
	
	// Touch attack (melee or ranged)
	public Attack(Creature attacker, Creature defender, boolean ranged) {
		this.attacker = attacker;
		this.defender = defender;
		
		meleeTouchAttack = !ranged;
		rangedTouchAttack = ranged;
		
		defenderAC = defender.stats().get(Stat.TouchArmorClass) + Game.curCampaign.curArea.getConcealment(attacker, defender);
	    attackBonus = attacker.stats().get(Stat.LevelAttackBonus) + attacker.stats().get(Stat.TouchAttackBonus);
		
	    for (RacialType racialType : attacker.getRace().getRacialTypes()) {
			defenderAC += defender.stats().get(racialType.getName(), Bonus.Type.ArmorClassVsRacialType);
		}
		
		for (RacialType racialType : defender.getRace().getRacialTypes()) {
			attackBonus += attacker.stats().get(racialType.getName(), Bonus.Type.AttackVsRacialType);
		}
	    
		attackRoll = Game.dice.d100();
		totalAttack = attackRoll + attackBonus;
	}
	
	// Dummy non-weapon attack
	public Attack(Creature attacker, Creature defender) {
		this.attacker = attacker;
		this.defender = defender;
	}
	
	// Attack with a weapon
	public Attack(Creature attacker, Creature defender, int itemSlot) {
		this.attacker = attacker;
		this.defender = defender;
		this.message = new StringBuilder();
		this.itemSlot = itemSlot;
		this.baseAttackBonus = attacker.stats().get(Stat.LevelAttackBonus);
		
		this.weapon = attacker.getInventory().getEquippedItem(itemSlot);
		if (itemSlot == Inventory.EQUIPPED_OFF_HAND && weapon == null) return;
		
		if (weapon == null) weapon = attacker.getRace().getDefaultWeapon();
		
		if (itemSlot == Inventory.EQUIPPED_MAIN_HAND) {
			damageBonus = 1.0f + (float)attacker.stats().get(Stat.MainHandDamageBonus) / 100.0f;
			attackBonus = attacker.stats().get(Stat.MainHandAttackBonus);
			
		} else if (itemSlot == Inventory.EQUIPPED_OFF_HAND) {
			damageBonus = 1.0f + (float)attacker.stats().get(Stat.OffHandDamageBonus) / 100.0f;
			attackBonus = attacker.stats().get(Stat.OffHandAttackBonus);
		}
		
		int concealment = Game.curCampaign.curArea.getConcealment(attacker, defender);
		if (!weapon.isMeleeWeapon()) {
			concealment = Math.max(0, concealment - attacker.stats().get(Bonus.Type.ConcealmentIgnoringRanged));
		}
		
		defenderAC = defender.stats().get(Stat.ArmorClass) + concealment;
		
		for (RacialType racialType : attacker.getRace().getRacialTypes()) {
			defenderAC += defender.stats().get(racialType.getName(), Bonus.Type.ArmorClassVsRacialType);
		}
		
		for (RacialType racialType : defender.getRace().getRacialTypes()) {
			attackBonus += attacker.stats().get(racialType.getName(), Bonus.Type.AttackVsRacialType);
			damageBonus += (float)attacker.stats().get(racialType.getName(), Bonus.Type.DamageVsRacialType) / 100.0f; 
		}
		
		Item quiver = attacker.getInventory().getEquippedItem(Inventory.EQUIPPED_QUIVER);
		int quiverAttackBonus = 0;
		int quiverDamageBonus = 0;
		
		if (weapon.getWeaponType() != Item.WeaponType.MELEE) {
			int weaponRangePenalty = weapon.getRangePenalty() * (100 - attacker.stats().get(Bonus.Type.RangePenalty)) / 100;
			
			int distance = 5 * (AreaUtil.distance(attacker.getX(), attacker.getY(), defender.getX(), defender.getY()));
			rangePenalty += (distance * weaponRangePenalty) / 100;
			
			if (weapon.getWeaponType() == Item.WeaponType.THROWN) {
				// remove thrown weapon from hand
				// automatically equip a new weapon of the same type if we have one in the inventory
				
				int index = attacker.getInventory().getUnequippedItems().findItem(weapon);
				attacker.getInventory().removeEquippedItem(weapon);
				
				if (index != -1) {
					Item replacement = attacker.getInventory().getUnequippedItems().getItem(index);
					attacker.getInventory().equipItem(replacement, Inventory.EQUIPPED_MAIN_HAND);
				}
			} else {
				// remove spent ammo
				// automatically equip new ammo if we have one in the inventory
				
				if (quiver == null) return;
				
				if (quiver.getWeaponType() != weapon.getWeaponType()) return;
				
				quiverAttackBonus = quiver.getQuality().getAttackBonus() + quiver.bonuses().get(Bonus.Type.WeaponAttack);
				quiverDamageBonus = quiver.getQuality().getDamageBonus() + quiver.bonuses().get(Bonus.Type.WeaponDamage);
				
				int index = attacker.getInventory().getUnequippedItems().findItem(quiver);
				if (index != -1) {
					Item replacement = attacker.getInventory().getUnequippedItems().getItem(index);
				
					attacker.getInventory().removeEquippedItem(quiver);
					attacker.getInventory().equipItem(replacement, Inventory.EQUIPPED_QUIVER);
				}
			} 
		}

		attackRoll = Game.dice.d100();
		damageRoll = Game.dice.rand(weapon.getDamageMin(), weapon.getDamageMax());

		damageBonus += ((float)attacker.stats().get(Stat.LevelDamageBonus)) / 100.0f;
		damageBonus += (float)(weapon.getQuality().getDamageBonus() + weapon.bonuses().get(Bonus.Type.WeaponDamage)) / 100.0f;
		damageBonus += (float)(quiverDamageBonus) / 100.0f;
		
		attackBonus += baseAttackBonus - rangePenalty;
		attackBonus += weapon.bonuses().get(Bonus.Type.WeaponAttack) + weapon.getQuality().getAttackBonus() + quiverAttackBonus;
		
		damageBonus += (float)attacker.stats().get(weapon.getDamageType().getName(), Bonus.Type.DamageForWeaponType) / 100.0f;
		attackBonus += attacker.stats().get(weapon.getDamageType().getName(), Bonus.Type.AttackForWeaponType); 
		
		totalAttack = attackRoll + attackBonus;
		totalDamage = (int)Math.round( ((float)damageRoll * damageBonus) );
		
		damageMin = (int)Math.round( ((float)weapon.getDamageMin() * damageBonus) );
		damageMax = (int)Math.round( ((float)weapon.getDamageMax() * damageBonus) );
		
		damage = new Damage(defender, weapon.getDamageType(), totalDamage);
		
		// add any standalone damage bonuses from the weapon or ammo
		switch (weapon.getWeaponType()) {
		case BOW: case CROSSBOW:
			damage.add(quiver.bonuses().rollStandaloneDamage(defender));
			// fall through here is intentional
		case MELEE: case THROWN:
			damage.add(weapon.bonuses().rollStandaloneDamage(defender));
			break;
		}
	}
	
	public void computeFlankingBonus(AreaEntityList entities) {
		if (weapon == null || weapon.getWeaponType() != Item.WeaponType.MELEE) return;
		
		//System.out.println("Computing flanking bonus for " + attacker + " attacking " + defender);
		
		Point screenAtt = AreaUtil.convertGridToScreenAndCenter(attacker.getX(), attacker.getY());
		Point screenDef = AreaUtil.convertGridToScreenAndCenter(defender.getX(), defender.getY());

		List<Creature> creatures = entities.getCreaturesWithinRadius(defender.getX(),
				defender.getY(), Game.curCampaign.curArea.getVisibilityRadius());
		
		for (Creature flanker : creatures) {
			//System.out.println("   Checking creature " + flanker.getName());
			
			if (flanker == attacker || flanker == defender) continue;
			
			if (!flanker.threatensPosition(defender.getX(), defender.getY())) continue;
			
			if (flanker.getFaction().getRelationship(defender) != Faction.Relationship.Hostile) continue;
			
			Point screenOth = AreaUtil.convertGridToScreenAndCenter(flanker.getX(), flanker.getY());

			double a2 = AreaUtil.euclideanDistance2(screenDef.x, screenDef.y, screenOth.x, screenOth.y);
			double b2 = AreaUtil.euclideanDistance2(screenDef.x, screenDef.y, screenAtt.x, screenAtt.y);
			double c2 = AreaUtil.euclideanDistance2(screenAtt.x, screenAtt.y, screenOth.x, screenOth.y);

			double a = Math.sqrt(a2);
			double b = Math.sqrt(b2);

			double theta = Math.acos((a2 + b2 - c2) / (2 * a * b)) * 360.0 / (2.0 * Math.PI);
			
			//System.out.println("      Computed angle of " + theta);
			
			if (theta > 140.0 - attacker.stats().get(Bonus.Type.FlankingAngle)) {
				flankingBonus = 20;
				flankingAttack = true;
				Game.mainViewer.addMessage("green", attacker.getName() + " and " + flanker.getName() + " are flanking " + defender.getName());
				return;
			}
		}
	}
	
	public boolean isHit() { return hit; }
	
	public boolean computeIsHit() {
		attackBonus += flankingBonus;
		totalAttack += flankingBonus;
		
		if (meleeTouchAttack) return isHitMeleeTouch();
		else if (rangedTouchAttack) return isHitRangedTouch();
		else return isHitNormal();
	}
	
	private boolean isHitRangedTouch() {
		message = new StringBuilder();
		message.append(attacker.getName() + " attempts ranged touch attack on " + defender.getName() + ": ");
		message.append(attackRoll + " + " + attackBonus + " = " + totalAttack + " vs " + defenderAC + ".  ");
		
		if (attackRoll > 97 || (attackRoll > 2 && totalAttack >= defenderAC)) {
			message.append("Succeeds.");
			hit = true;
		} else {
			message.append("Miss.");
			hit = false;
		}
		
		return hit;
	}
	
	private boolean isHitMeleeTouch() {
		message = new StringBuilder();
		message.append(attacker.getName() + " attempts melee touch attack on " + defender.getName() + ": ");
		message.append(attackRoll + " + " + attackBonus + " = " + totalAttack + " vs " + defenderAC + ".  ");
		
		if (attackRoll > 97 || (attackRoll > 2 && totalAttack >= defenderAC)) {
			message.append("Succeeds.");
			hit = true;
		} else {
			message.append("Miss.");
			hit = false;
		}
		
		return hit;
	}
	
	private boolean isHitNormal() {
		message = new StringBuilder();
		
		if (attacker.getInventory().hasEquippedOffHandWeapon()) {
			if (itemSlot == Inventory.EQUIPPED_MAIN_HAND) {
				message.append("<span style=\"font-family:green;\">[Main hand attack]</span> ");
			} else if (itemSlot == Inventory.EQUIPPED_OFF_HAND) {
				message.append("<span style=\"font-family:green;\">[Off hand attack]</span> ");
			}
		}
		
		boolean criticalHitImmunity = defender.stats().has(Bonus.Type.CriticalHitImmunity);
		
		// critical hit immunity also grants immunity to extraAttack / extraDamage
		if (!criticalHitImmunity) {
			totalAttack += extraAttack;
			attackBonus += extraAttack;
		}
			
		message.append(attacker.getName() + " attacks " + defender.getName() + ": " + attackToString() + " vs AC " + defenderAC);
		
		if (attackRoll > 97 || (attackRoll > 2 && totalAttack >= defenderAC)) {
			hit = true;
			
			int threatRange = weapon.getCriticalThreatRange() -
				attacker.stats().get(weapon.getBaseWeapon().getName(), Bonus.Type.BaseWeaponCriticalChance) -
				weapon.bonuses().get(Bonus.Type.WeaponCriticalChance);
			
			if ( attackRoll >= threatRange && !criticalHitImmunity ) {
				threatRoll = Game.dice.d100();

				message.append(". Critical threat: " + threatToString());

				int threatCheck = threatRoll + attackBonus;
				
				boolean isCriticalHit = threatCheck > 97 || threatCheck >= defenderAC;
				
				// no critical hits on PCs if that has been disabled by the difficulty manager
				if (defender.isPlayerSelectable() && !Game.ruleset.getDifficultyManager().criticalHitsOnPCs())
					isCriticalHit = false;
					
				if (isCriticalHit) {
					message.append(". Critical Hit" );

					int multiplier = weapon.getCriticalMultiplier() +
					attacker.stats().get(weapon.getBaseWeapon().getName(), Bonus.Type.BaseWeaponCriticalMultiplier) +
					weapon.bonuses().get(Bonus.Type.WeaponCriticalMultiplier);

					damage.add(weapon.getDamageType(), totalDamage * (multiplier - 1));
					damageRoll *= multiplier;
					totalDamage *= multiplier;
					
					// shake the screen on a critical hit
					Game.areaViewer.addScreenShake();

				} else {
					message.append(". Normal Hit");
				}
			}
			
			// critical hit immunity also grants immunity to extraAttack / extraDamage
			if (!criticalHitImmunity) {
				// extra damage doesn't get multiplied by critical hits
				damage.add(weapon.getDamageType(), extraDamage);
				totalDamage += extraDamage;
			}
			
			if (criticalHitImmunity && (attackRoll >= threatRange || extraDamage > 0 || extraAttack > 0)) {
				message.append(". ").append(defender.getName()).append(" is immune to critical hits");
			}
			
			message.append(". Hits for " + damageToString() + " damage.");
			
		} else {
			hit = false;
			message.append(". Miss.");
		}
		
		return hit;
	}
	
	public int computeAppliedDamage(boolean message) {
		this.appliedDamage = damage.computeAppliedDamage(message);
		
		return appliedDamage;
	}
	
	public String threatToString() {
		return (threatRoll + " + " + attackBonus + " = " + (threatRoll+attackBonus));
	}
	
	public String attackToString() {
		return (attackRoll + " + " + attackBonus + " = " + totalAttack);
	}
	
	public String damageToString() {
		String damageString = (damageRoll + " * " + Game.numberFormat(3).format(damageBonus));
		if (extraDamage != 0) damageString = damageString + " + " + extraDamage;
		return damageString + " = " + totalDamage;
	}
	
	@Override
	public String toString() {
		return ("Rolled " + attackToString() + " for " + damageToString() + " Damage");
	}
}
