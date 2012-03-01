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

package net.sf.hale.entity;

import net.sf.hale.Game;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Faction;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A trap is a type of Item that is designed to "go off" when a hostile
 * Creature enters its tile.  It will then generally apply damage or some
 * other effect to the Creature.  Traps can be placed, disarmed, recovered.
 * 
 * @author Jared Stephen
 *
 */

public class Trap extends Item {
	private boolean spotted;
	private boolean armed;
	
	private int findDifficulty, placeDifficulty, disarmDifficulty, recoverDifficulty;
	private int reflexDifficulty;
	private boolean activateOnlyOnce;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = super.save();
		
		data.put("spotted", spotted);
		data.put("armed", armed);
		data.put("faction", this.getFaction().getName());
		
		return data;
	}
	
	@Override public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		super.load(data, refHandler);
		
		this.spotted = data.get("spotted", false);
		this.armed = data.get("armed", false);
		
		if (data.containsKey("faction"))
			this.setFaction(data.get("faction", null));
	}
	
	/**
	 * Create a new Trap with the specified parameters.
	 * 
	 * @param id the ID String for this Trap; must be unique among items
	 * @param name the plain text name for this Trap
	 * @param icon the icon to show on the area
	 * @param description text description of this item
	 * @param currency the value of this item
	 */
	
	public Trap(String id, String name, String icon, String description, Currency currency) {
		super(id, name, icon, Item.ItemType.ITEM, description, currency);
		this.type = Entity.Type.TRAP;
		this.spotted = false;
		this.armed = false;
	}
	
	/**
	 * Create a new Trap that is a copy of the specified other trap
	 * 
	 * @param other the Trap to copy
	 */
	
	public Trap(Trap other) {
		super(other);
		this.type = Entity.Type.TRAP;
		this.armed = other.armed;
		this.spotted = other.spotted;
		this.activateOnlyOnce = other.activateOnlyOnce;
		this.findDifficulty = other.findDifficulty;
		this.placeDifficulty = other.placeDifficulty;
		this.disarmDifficulty = other.disarmDifficulty;
		this.recoverDifficulty = other.recoverDifficulty;
		this.reflexDifficulty = other.reflexDifficulty;
	}
	
	public boolean canPlace(Creature parent) {
		if (!parent.stats().has(Bonus.Type.TrapHandling)) return false;
		
		Trap curTrap = Game.curCampaign.curArea.getTrapAtGridPoint(parent.getX(), parent.getY());
		
		if (curTrap != null) return false;
		
		return true;
	}
	
	public void tryPlace(Creature parent) {
		Trap curTrap = Game.curCampaign.curArea.getTrapAtGridPoint(parent.getX(), parent.getY());
		
		if (curTrap != null) {
			Game.mainViewer.addMessage("red", "There is already a trap there.");
			return;
		}
		
		int difficulty = modifyValueByQuality(placeDifficulty);
		int check = parent.getSkillCheck("Traps", difficulty, true);
		
		if (check >= difficulty) {
			setArmed(true);
			setSpotted(true);
			setPosition(parent.getX(), parent.getY());
			setFaction(parent.getFaction());
			Game.curCampaign.curArea.addItem(new Trap(this));
			parent.getInventory().removeItem(this);
		} else if (check < difficulty - Game.ruleset.getValue("TrapCriticalFailureThreshold")) {
			
			//critical failure
			fireTrap(parent);
			if (activateOnlyOnce) {
				armed = false;
				parent.getInventory().removeItem(this);
			}
		}
	}
	
	/**
	 * The specified Creature will attempt to recover this trap.  The
	 * Creature must succeed at a Traps check against this Trap's
	 * recoverDifficulty.
	 * 
	 * @param parent the Creature trying to recover this Trap
	 * @return true if and only if the Trap was recovered successfully
	 */
	
	public boolean tryRecover(Creature parent) {
		if (!armed) return false;
		
		int difficulty = modifyValueByQuality(recoverDifficulty);
		int check = parent.getSkillCheck("Traps", difficulty, true);
		
		if (check >= difficulty) {
			Game.curCampaign.curArea.removeEntity(this);
			parent.getInventory().addItem(this);
			return true;
		} else if (check < difficulty - Game.ruleset.getValue("TrapCriticalFailureThreshold")) {
			
			//critical failure
			fireTrap(parent);
		}
		
		return false;
	}
	
	/**
	 * The specified Creature will attempt to disarm this trap.  The
	 * Creature must succeed at a Traps check against this Trap's
	 * disarmDifficulty.
	 * 
	 * @param parent the Creature trying to disarm this Trap.
	 * @return true if and only if this Trap was disarmed successfully.
	 */
	
	public boolean tryDisarm(Creature parent) {
		if (!armed) return false;
		
		int difficulty = modifyValueByQuality(disarmDifficulty);
		int check = parent.getSkillCheck("Traps", difficulty, true);
		
		if (check >= difficulty) {
			Game.curCampaign.curArea.removeEntity(this);
			return true;
		} else if (check < difficulty - Game.ruleset.getValue("TrapCriticalFailureThreshold")) {
			
			//critical failure
			fireTrap(parent);
		}
		
		return false;
	}
	
	/**
	 * The specified Creature will attempt to spot this Trap.  The creature must
	 * succeed at a Search check against this Trap's findDifficulty.
	 * @param parent the Creature trying to spot the trap
	 * @return true if and only if the Creature succeeds in spotting the trap.  Returns
	 * false if the Trap was already visible.
	 */
	
	public boolean trySearch(Creature parent) {
		if (isVisible()) return false;
		
		int distancePenalty = 10 * AreaUtil.distance(parent.getX(), parent.getY(), this.getX(), this.getY());
		
		if (parent.skillCheck("Search", modifyValueByQuality(findDifficulty) + distancePenalty, false)) {
			Game.mainViewer.addMessage("orange", parent.getName() + " spots a trap!");
			Game.mainViewer.addFadeAway("Search: Success", this.getX(), this.getY(), "grey");
			
			setSpotted(true);
			
			return true;
		}
		
		return false;
	}
	
	public int modifyValueByQuality(int difficulty) {
		int num = Game.ruleset.getValue("TrapQualityDifficultyNumerator");
		int den = Game.ruleset.getValue("TrapQualityDifficultyDenomenator");
		
		int qualityBonus = this.getQuality().getModifier();
		
		int bonus = qualityBonus * num / den;
		
		return difficulty * (100 + bonus) / 100;
	}
	
	public void setArmed(boolean armed) { this.armed = armed; }
	public boolean isArmed() { return armed; }
	
	public boolean isVisible() { return !armed || spotted; }
	
	public boolean isDisarmable() { return armed && spotted; }
	
	public void setSpotted(boolean spotted) { this.spotted = spotted; }
	public boolean isSpotted() { return spotted; }
	
	private void fireTrap(Creature target) {
		if (this.hasScript()) {
			getScript().executeFunction(ScriptFunctionType.onSpringTrap, this, target);
		}
		
		if ( !target.reflexCheck(modifyValueByQuality(reflexDifficulty)) ) {
			
			if (hasScript())
				getScript().executeFunction(ScriptFunctionType.onTrapReflexFailed, this, target);
			
			int minDamage = modifyValueByQuality(getDamageMin());
			int maxDamage = modifyValueByQuality(getDamageMax());
			int damage = Game.dice.rand(minDamage, maxDamage);
	    
			if (damage != 0)
				target.takeDamage(damage, getDamageType().getName());
	    }
		
		if (activateOnlyOnce) {
			armed = false;
			Game.curCampaign.curArea.removeEntity(this);
		}
	}
	
	public boolean springTrap(Creature target) {
		// only fire on hostile creatures
		Faction faction = getFaction();
		if (faction != null) {
			if (faction.getRelationship(target) != Faction.Relationship.Hostile) return false; 
		}
		
		if (!armed) return false;
		
		Game.mainViewer.addMessage("red", target.getName() + " springs a trap.");
		
		fireTrap(target);
		
		return true;
	}
	
	public void setReflexDifficulty(int reflexDifficulty) { this.reflexDifficulty = reflexDifficulty; }
	public int getReflexDifficulty() { return reflexDifficulty; }
	
	public void setActivateOnlyOnce(boolean activateOnlyOnce) { this.activateOnlyOnce = activateOnlyOnce; }
	public boolean activatesOnlyOnce() { return activateOnlyOnce; }
	
	public void setFindDifficulty(int findDifficulty) { this.findDifficulty = findDifficulty; }
	public int getFindDifficulty() { return findDifficulty; }

	public void setPlaceDifficulty(int placeDifficulty) { this.placeDifficulty = placeDifficulty; }
	public int getPlaceDifficulty() { return placeDifficulty; }

	public void setDisarmDifficulty(int disarmDifficulty) { this.disarmDifficulty = disarmDifficulty; }
	public int getDisarmDifficulty() { return disarmDifficulty; }

	public void setRecoverDifficulty(int recoverDifficulty) { this.recoverDifficulty = recoverDifficulty; }
	public int getRecoverDifficulty() { return recoverDifficulty; }
}
