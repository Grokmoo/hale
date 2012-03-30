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

import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.bonus.StatManager;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.Faction;
import net.sf.hale.rules.RoundTimer;
import net.sf.hale.util.Point;

/**
 * A SpellCaster designed to allow the casting of spells from items
 * (spell scrolls).
 * @author Jared Stephen
 *
 */

public class SpellScrollCaster implements AbilityActivator {
	private String abilityID;
	private Creature parent;
	private Item itemToUse;
	
	private int castingAttribute = 10;
	private int casterLevel;
	private int spellFailure;
	
	private StatManager stats;
	private CreatureAbilitySet abilities;
	// effects will be empty
	private EntityEffectSet effects;
	
	/**
	 * Creates a new SpellScrollCaster that will cast the spell with the
	 * specified ID.
	 * 
	 * @param abilityID the ID of the spell / ability to cast
	 */
	
	public SpellScrollCaster(String abilityID) {
		this.abilityID = abilityID;
		this.stats = new StatManager((Creature)null);
		this.abilities = new CreatureAbilitySet();
		this.effects = new EntityEffectSet();
	}
	
	/**
	 * Activates the ability associated with this SpellScrollCaster
	 */
	
	public void activate() {
		Ability ability = Game.ruleset.getAbility(abilityID);
		ScrollAbilitySlot slot = new ScrollAbilitySlot(ability);
		new AbilityActivateCallback(slot, ScriptFunctionType.onActivate).run();
	}
	
	/**
	 * The specified Item will be used up when this SpellScrollCaster is activated.
	 * It will be removed from the parent Creature's inventory.
	 * 
	 * @param item the item to use up
	 */
	
	public void setItemToUse(Item item) {
		this.itemToUse = item;
	}
	
	/**
	 * Returns the parent creature for this SpellScrollCaster
	 * @return the parent Creature
	 */
	
	public Creature getParent() {
		return parent;
	}
	
	/**
	 * Sets the parent Creature that is activating this SpellScrollCaster
	 * @param parent the parent for this SpellScrollCaster
	 */
	
	public void setParent(Creature parent) {
		this.parent = parent;
	}
	
	/**
	 * Sets the effective caster level for the item casting this spell for the purposes
	 * of casting the spell
	 * 
	 * @param casterLevel the effective caster level
	 */
	
	public void setCasterLevel(int casterLevel) {
		this.casterLevel = casterLevel;
	}
	
	/**
	 * Sets the attribute value for the primary spell casting attribute for spells
	 * cast via this SpellScrollCaster
	 * @param castingAttribute the integer value of the primary spell casting attribute
	 */
	
	public void setCastingAttribute(int castingAttribute) {
		this.castingAttribute = castingAttribute;
	}
	
	/**
	 * Sets the base spell failure percentage for casting the spell via this item.  The
	 * default of 0 is the usual choice.
	 * 
	 * @param spellFailure the base spell failure
	 */
	
	public void setSpellFailurePercentage(int spellFailure) {
		this.spellFailure = spellFailure;
	}
	
	// return our own, empty stats.  We do not want bonuses or penalties
	// on the parent creature to affect the scroll casting
	@Override public StatManager stats() {
		return stats;
	}
	
	// allow the item to set its own caster level
	@Override public int getCasterLevel() {
		return casterLevel;
	}
	
	// allow the item to set its own casting attribute
	@Override public int getSpellCastingAttribute() {
		return castingAttribute;
	}
	
	// scrolls will not usually have spell failure, but allow
	// a custom value to be set
	@Override public int getBaseSpellFailure(int spellLevel) {
		return spellFailure;
	}
	
	// effects not relevant for scroll caster
	@Override public EntityEffectSet getEffects() {
		return effects;
	}

	@Override public CreatureAbilitySet getAbilities() {
		return abilities;
	}
	
	@Override public boolean isImmobilized() {
		return false;
	}
	
	@Override public Point getPosition() {
		return parent.getPosition();
	}

	@Override public int getX() {
		return parent.getX();
	}

	@Override public int getY() {
		return parent.getY();
	}

	@Override public Faction getFaction() {
		return parent.getFaction();
	}

	@Override public String getName() {
		return parent.getName();
	}

	@Override public Encounter getEncounter() {
		return parent.getEncounter();
	}

	@Override public boolean[][] getVisibility() {
		return parent.getVisibility();
	}

	@Override public RoundTimer getTimer() {
		return parent.getTimer();
	}
	
	@Override public boolean isPlayerSelectable() {
		return parent.isPlayerSelectable();
	}
	
	// an ability slot that removes the optional Item when activated
	private class ScrollAbilitySlot extends AbilitySlot {
		private ScrollAbilitySlot(Ability ability) {
			super(ability.getType(), SpellScrollCaster.this);
			this.setAbility(ability);
		}
		
		@Override public void activate() {
			super.activate();
			
			if (parent != null) {
				if (itemToUse != null) parent.getInventory().removeItem(itemToUse);
				
				parent.getAbilities().trackTempAbilitySlot(this);
			}
		}
	}
}
