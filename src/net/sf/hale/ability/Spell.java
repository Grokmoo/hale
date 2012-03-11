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

import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.ScriptInterface;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.util.FileKeyMap;

/**
 * A Spell is a special type of Ability with some additional information such
 * as spell components and spell level.  Note that spells do not support
 * inline scripts; the script must be specified as an external file
 * @author Jared Stephen
 *
 */

public class Spell extends Ability {
	private final boolean verbalComponent;
	private final boolean somaticComponent;
	private final boolean spellResistanceApplies;
	
	/**
	 * Create a new Spell with the specified parameters
	 * @param id the ID String of the Spell
	 * @param script the script contents of the Spell
	 * @param scriptLocation the resource location of the script for the Spell
	 * @param map the FileKeyMap containing keys with values for the data making up
	 * this Spell
	 */
	
	protected Spell(String id, String script, String scriptLocation, FileKeyMap map) {
		super(id, script, scriptLocation, map, false);
		
		this.verbalComponent = map.getValue("verbalcomponent", true);
		this.somaticComponent = map.getValue("somaticcomponent", true);
		this.spellResistanceApplies = map.getValue("spellresistance", true);
	}
	
	/**
	 * Returns true if and only if this Spell has a verbal component.  Spells with
	 * verbal components are affected by Silence effects.
	 * 
	 * @return true if and only if this Spell has a verbal component
	 */
	
	public boolean hasVerbalComponent() {
		return verbalComponent;
	}
	
	/**
	 * Returns true if and only if this Spell has a somatic component.  Spells with
	 * somatic components are affected by spell failure due to armor.
	 * 
	 * @return true if and only if this Spell has a somatic component
	 */
	
	public boolean hasSomaticComponent() {
		return somaticComponent;
	}
	
	/**
	 * Returns true if and only if this Spell is affected by spell resistance.  Spell
	 * resistance can reduce the damage and duration of spells, but some spells are not
	 * affected by it.
	 * 
	 * @return true if and only if this Spell is affected by spell resistance
	 */
	
	public boolean spellResistanceApplies() {
		return spellResistanceApplies;
	}
	
	/**
	 * In addition to the actions performed by the parent Object,
	 * also performs search checks for the parent Entity.
	 * 
	 * See {@link Ability#activate(AbilityActivator)}
	 */
	
	@Override public void activate(AbilityActivator parent) {
		super.activate(parent);
		
		if (parent instanceof Creature) {
			ScriptInterface.performSearchChecksForCreature((Creature)parent,
					Game.ruleset.getValue("HideCastSpellPenalty"));
		}
	}
	
	@Override public int getCooldown(AbilityActivator parent) {
		int baseCooldown = super.getCooldown(parent);
		
		return baseCooldown - parent.stats().get(Bonus.Type.SpellCooldown);
	}
	
	/**
	 * Determines whether the parent casting this Spell should suffer random Spell
	 * failure.  It is assumed that the parent is suffering no additional failure due
	 * to target concealment.  This is typically the situation when the caster is casting
	 * a spell targeting themselves, an ally, or a point in the Area (not a hostile creature)
	 * 
	 * @param parent the parent Creature that is casting this Spell
	 * @return true if the spell should be cast successfully, false if the spell should fail
	 */
	
	public boolean checkSpellFailure(AbilityActivator parent) {
		return checkSpellFailure(parent, 0);
	}
	
	/**
	 * Determines whether the parent casting this Spell should suffer random Spell failure.
	 * The parent takes a concealment penalty based on the concealment between themselves
	 * and the specified target.  This concealment penalty will most commonly be 0.  This
	 * is the typical case when casting a spell targeting a single hostile creature.
	 * 
	 * @param parent the Creature casting this Spell
	 * @param target the target Creature for this Spell
	 * @return true if the spell should be cast successfully, false if the spell should fail
	 */
	
	public boolean checkSpellFailure(AbilityActivator parent, Creature target) {
		if (parent.getX() == target.getX() && parent.getY() == target.getY())
			return checkSpellFailure(parent, 0);
		
		int concealment = Game.curCampaign.curArea.getConcealment(parent, target);
		
		return checkSpellFailure(parent, concealment);
	}
	
	/**
	 * Determines whether the parent casting this Spell should suffer random Spell failure.
	 * The parent takes a concealment penalty based on the largest concealment between
	 * themselves and any individual target.  This case should be used when casting a spell
	 * targeting multiple individual hostile creatures.
	 * 
	 * @param parent the Creature casting this Spell
	 * @param targets the List of targets for this Spell
	 * @return true if the spell should be cast successfully, false if the spell should fail
	 */
	
	public boolean checkSpellFailure(AbilityActivator parent, List<Creature> targets) {
		int bestConcealment = 0;
		for (Creature defender : targets) {
			bestConcealment = Math.max(bestConcealment, Game.curCampaign.curArea.getConcealment(parent, defender));
		}
		
		return checkSpellFailure(parent, bestConcealment);
	}
	
	/**
	 * Returns the integer percentage chance for the specified parent casting this spell to fail, assuming
	 * no concealment penalty
	 * @param parent the parent casting the spell
	 * @return the spell failure chance
	 */
	
	public int getSpellFailurePercentage(AbilityActivator parent) {
		int failure = parent.getBaseSpellFailure(getSpellLevel(parent));
		
		if (verbalComponent) {
			int verbalFailure = -parent.stats().get(Bonus.Type.VerbalSpellFailure);
			failure += Math.max(0, verbalFailure);
			
			// if area is silenced or parent creature is silenced
			if (Game.curCampaign.curArea.isSilenced(parent.getX(), parent.getY()) ||
					parent.stats().has(Bonus.Type.Silence)) {
				return 100;
			}
		}
		
		if (somaticComponent) {
			// add the spell failure from armor
			int armorFailure = parent.stats().get(Stat.ArmorPenalty) - parent.stats().get(Bonus.Type.ArmorSpellFailure);
			failure += Math.max(0, armorFailure);
		}
		
		// determine the failure due to threatening creatures
		List<Creature> threatens = Game.areaListener.getCombatRunner().getThreateningCreatures(parent);
		if (threatens.size() != 0) {
			int meleeCombatFailure = 0;
			for (Creature attacker : threatens) {
				int curFailure = 30;
				
				// concealment for the caster against attackers decreases spell failure
				int concealment = Game.curCampaign.curArea.getConcealment(attacker, parent);
				curFailure = curFailure * (100 - concealment) / 100;
				meleeCombatFailure += Math.max(0, curFailure);
			}
			
			meleeCombatFailure = Math.max(0, meleeCombatFailure - parent.stats().get(Bonus.Type.MeleeSpellFailure));
			
			failure += meleeCombatFailure;
		}
		
		// spell failure due to deafness
		if (parent.stats().has(Bonus.Type.Deaf) && verbalComponent) {
			int deafnessPenalty = 30;
			failure += deafnessPenalty;
		}
		
		return failure;
	}
	
	/*
	 * The base method used to determine spell failure in all of the above cases
	 */
	
	private boolean checkSpellFailure(AbilityActivator parent, int concealmentPenalty) {
		int failure = parent.getBaseSpellFailure(getSpellLevel(parent)) + concealmentPenalty;
		int check = Game.dice.d100();
		
		String message = null;
		
		if (verbalComponent) {
			int verbalFailure = -parent.stats().get(Bonus.Type.VerbalSpellFailure);
			failure += Math.max(0, verbalFailure);
			
			// if area is silenced or parent creature is silenced
			if (Game.curCampaign.curArea.isSilenced(parent.getX(), parent.getY()) ||
					parent.stats().has(Bonus.Type.Silence)) {
				Game.mainViewer.addMessage("red", parent.getName() + " failed to cast " + getName() + " due to silence.");
				return false;
			}
		}
		
		if (somaticComponent) {
			// add the spell failure from armor
			int armorFailure = parent.stats().get(Stat.ArmorPenalty) - parent.stats().get(Bonus.Type.ArmorSpellFailure);
			failure += Math.max(0, armorFailure);
		}
		
		// determine the failure due to threatening creatures
		List<Creature> threatens = Game.areaListener.getCombatRunner().getThreateningCreatures(parent);
		if (threatens.size() != 0) {
			int meleeCombatFailure = 0;
			for (Creature attacker : threatens) {
				int curFailure = 30;
				
				// concealment for the caster against attackers decreases spell failure
				int concealment = Game.curCampaign.curArea.getConcealment(attacker, parent);
				curFailure = curFailure * (100 - concealment) / 100;
				meleeCombatFailure += Math.max(0, curFailure);
			}
			
			meleeCombatFailure = Math.max(0, meleeCombatFailure - parent.stats().get(Bonus.Type.MeleeSpellFailure));
			
			message = parent.getName() + " suffers an extra " + meleeCombatFailure +
				"% spell failure due to threatening creatures";
			
			failure += meleeCombatFailure;
		}
		
		if (concealmentPenalty != 0) {
			if (message == null) {
				message = parent.getName() + " suffers an extra " + concealmentPenalty +
					"% spell failure due to target concealment";
			} else {
				message += " and " + concealmentPenalty + "% spell failure due to target concealment";
			}
		}
		
		// spell failure due to deafness
		if (parent.stats().has(Bonus.Type.Deaf) && verbalComponent) {
			int deafnessPenalty = 30;
			failure += deafnessPenalty;
			if (message == null) {
				message = parent.getName() + " suffers an extra " + deafnessPenalty + "% spell failure due to deafness";
			} else {
				message += " and " + deafnessPenalty + "% spell failure due to deafness";
			}
		}
		
		if (message != null) {
			message += ".";
			Game.mainViewer.addMessage("green", message);
		}
		
		if (check <= failure) {
			Game.mainViewer.addMessage("red", parent.getName() + " failed to cast " + getName() + " with " + failure + "% failure.");
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * The specified target takes the specified type and amount of damage.  The damage can be
	 * modified by spell bonuses on the parent Creature.  The damage will be modified by
	 * spell resistance if this Spell has {@link #spellResistanceApplies()} equal to true.
	 * 
	 * @param parent the parent or caster of this Spell
	 * @param target the target that will take the damage
	 * @param damage the number of hit points of damage to apply before modification based
	 * on resistances and bonuses
	 * @param damageType the {@link net.sf.hale.rules.DamageType} of the damage to apply
	 */
	
	public void applyDamage(AbilityActivator parent, Creature target, int damage, String damageType) {
		// determine multiplier from the parent caster
		int spellDamageMult = parent.stats().get(Bonus.Type.SpellDamage) +
			parent.stats().get(damageType, Bonus.Type.DamageForSpellType);
		
		damage = (damage * (100 + spellDamageMult)) / 100;
		
		// determine spell resistance factor
		int spellResistance = Math.max( 0, spellResistanceApplies ? target.stats().get(Bonus.Type.SpellResistance) : 0 );
		
		if (spellResistance != 0) {
			Game.mainViewer.addMessage("blue", target.getName() + "'s Spell Resistance absorbs " + spellResistance + "% of the spell.");
		}
		
		int damageMult = 100 - spellResistance;
		if (damageMult < 0) damageMult = 0;
		
		damage = (damage * damageMult) / 100;
		target.takeDamage(damage, damageType);
	}
	
	/**
	 * The specified target is healed by the specified number of hit points of damage.  This damage
	 * can be modified based on bonuses on the parent Creature and spell resistance on the target
	 * Creature, if spell resistance applies to this Spell.
	 * 
	 * @param parent the parent caster of this Spell
	 * @param target the target that will have damage healed
	 * @param damage the number of hit points to heal
	 */
	
	public void applyHealing(AbilityActivator parent, Creature target, int damage) {
		int bonusHealingFactor = parent.stats().get(Bonus.Type.SpellHealing);

		damage = (damage * (100 + bonusHealingFactor)) / 100;

		int spellResistance = Math.max( 0, spellResistanceApplies ? target.stats().get(Bonus.Type.SpellResistance) : 0 );
		
		if (spellResistance == 0) {
			target.healDamage(damage);
		} else {
			int damageMult = Math.min(0, 100 - spellResistance);
			target.healDamage(damage * damageMult / 100);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.hale.ability.Ability#setSpellDuration(net.sf.hale.ability.Effect, net.sf.hale.ability.AbilityActivator)
	 */
	
	@Override public void setSpellDuration(Effect effect, AbilityActivator parent) {
		int duration = effect.getRoundsRemaining();
		// compute SpellDuration bonus
		int durationBonus = parent.stats().get(Bonus.Type.SpellDuration);
		duration = duration * (100 + durationBonus) / 100;
		
		// compute spell resistance modification
		if (spellResistanceApplies) {
			int spellResistanceMultiplier = Math.min( 100, Math.max(0, 100 - effect.getTarget().getSpellResistance()) );
			duration = duration * spellResistanceMultiplier / 100;
		}
		
		effect.setDuration(duration);
	}
	
	public int getCheckDifficulty(AbilityActivator parent) {
		return 50 + ( parent.getSpellCastingAttribute() - 10 ) * 2 + 3 * parent.getCasterLevel();
	}
	
	@Override public void appendDetails(StringBuilder sb, AbilityActivator parent) {
		sb.append("<p><span style=\"font-family: vera-blue\">Spell</span></p>");
		
		super.appendDetails(sb, parent);
		
		sb.append("<table style=\"font-family: vera; vertical-align: middle; margin-top: 1em;\">");
		
		sb.append("<tr><td style=\"width: 10ex;\">");
		sb.append("Spell Level</td><td style=\"font-family: vera-blue\">");
		sb.append(getSpellLevel(parent)).append("</td></tr>");
		
		sb.append("</table>");
		
		sb.append("<table style=\"font-family: vera; vertical-align: middle;\">");
		if (verbalComponent) {
			sb.append("<tr><td>Verbal Component</td></tr>");
		}
		
		if (somaticComponent) {
			sb.append("<tr><td>Somatic Component</td></tr>");
		}
		
		if (spellResistanceApplies) {
			sb.append("<tr><td>Affected by ");
		} else {
			sb.append("<tr><td>Ignores ");
		}
		sb.append("<span style=\"font-family: vera-red\">Spell Resistance</span></td></tr>");
		sb.append("</table>");
	}
}
