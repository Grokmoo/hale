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
import net.sf.hale.ScriptInterface;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Creature;

/**
 * A class that performs an attack after a specified delay
 * @author Jared Stephen
 *
 */

public class DelayedAttackCallback extends Thread{
	private long delayMillis;
	
	private Attack mainAttack, offHandAttack;
	
	private List<Runnable> callbacks;
	
	/**
	 * Creates a new DelayedCallback which will execute after the specified delay
	 * in a new Thread.
	 * @param delayMillis the delay in milliseconds
	 * @param mainAttack the attack to perform after the delay
	 * @param offHandAttack the second attack to perform or null if no second attack should be performed
	 */
	
	public DelayedAttackCallback(long delayMillis, Attack mainAttack, Attack offHandAttack) {
		this.delayMillis = delayMillis;
		
		this.mainAttack = mainAttack;
		this.offHandAttack = offHandAttack;
		
		this.callbacks = new ArrayList<Runnable>();
	}
	
	/**
	 * Returns true if the attack performed by this callback was a hit
	 * @return true if and only if the attack performed by this callback was a hit
	 */
	
	public boolean isAttackHit() {
		if (mainAttack != null && offHandAttack != null)
			return mainAttack.isHit() || offHandAttack.isHit();
		else if (mainAttack != null) {
			return mainAttack.isHit();
		} else if (offHandAttack != null) {
			return offHandAttack.isHit();
		} else {
			return false;
		}
	}
	
	/**
	 * Adds the specified callback to the list of callbacks that are run() after this attack is
	 * executed
	 * @param runnable the callback to add
	 */
	
	public void addCallback(Runnable runnable) {
		callbacks.add(runnable);
	}
	
	private void performAttack(Attack attack) {
		Creature attacker = attack.getAttacker();
		Creature defender = attack.getDefender();
		
		// compute mainViewer text color
		String color = "black";
		if (attacker.isPlayerSelectable()) color = "blue";
		else color = "green";
		
		// run onDefense and onAttack scripts
		defender.getEffects().executeOnAll(ScriptFunctionType.onDefense, attack);
		attacker.getEffects().executeOnAll(ScriptFunctionType.onAttack, attack);
		
		if (attack.getWeapon() != null) {
			if (attack.getWeapon().hasScript())
				attack.getWeapon().getScript().executeFunction(ScriptFunctionType.onAttack, attack.getWeapon(), attack);
			
			attack.getWeapon().getEffects().executeOnAll(ScriptFunctionType.onAttack, attack);
		}
		
		// determine if attack hits and compute damage if it does
		if (attack.computeIsHit()) {
			Game.mainViewer.addMessage(color, attack.getMessage());

			if (attack.causesDamage()) {
				
				int damage = 0;
				if (!attack.damageNegated())
					damage = defender.takeDamageFromAttack(attack, true);
				
				// execute on hit scripts
				defender.getEffects().executeOnAll(ScriptFunctionType.onDefenseHit, attack, damage);
				attacker.getEffects().executeOnAll(ScriptFunctionType.onAttackHit, attack, damage);
				
				if (attack.getWeapon() != null) {
					if (attack.getWeapon().hasScript()) {
						attack.getWeapon().getScript().executeFunction(ScriptFunctionType.onAttackHit,
								attack.getWeapon(), attack, damage);
					}
					
					attack.getWeapon().getEffects().executeOnAll(ScriptFunctionType.onAttackHit, attack, damage);
				}
			}
			
		} else {
			Game.mainViewer.addMessage(color, attack.getMessage());
			Game.mainViewer.addFadeAway("Miss", attacker.getX(), attacker.getY(), "grey");
		}
		
		// other creatures get an AoO for ranged attacks
		if (attack.isRanged() && !attacker.stats().has(Bonus.Type.AoOFromRangedImmunity)) {
			Game.areaListener.getCombatRunner().provokeAttacksOfOpportunity(attacker, null);
		}
		
		// other creatures get a chance to spot a hiding creature when it attacks
		ScriptInterface.performSearchChecksForCreature(attacker, Game.ruleset.getValue("HideAttackPenalty"));
		
		Game.mainViewer.updateEntity(attacker);
		Game.mainViewer.updateEntity(defender);
		Game.mainViewer.updateInterface();
	}
	
	@Override public void run() {
		try {
			if (delayMillis != 0l)
				Thread.sleep(delayMillis);
			
			if (mainAttack != null) performAttack(mainAttack);
			
			if (offHandAttack != null) performAttack(offHandAttack);
			
			for (Runnable callback : callbacks) {
				callback.run();
			}
			
		} catch (InterruptedException e) {
			// thread was interrupted, can exit
			return;
		}
		
		synchronized(this) {
			this.notifyAll();
		}
	}
}
