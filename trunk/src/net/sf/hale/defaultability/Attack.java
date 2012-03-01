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

package net.sf.hale.defaultability;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.util.Point;

/**
 * A default ability for using a standard attack against an opponent.  If the
 * opponent is not within reach, the parent is first moved towards the hostile
 * @author Jared Stephen
 *
 */

public class Attack implements DefaultAbility {
	private Move move;
	private Creature target;
	
	@Override public String getActionName() {
		return "Attack";
	}

	@Override public boolean canActivate(Creature parent, Point targetPosition) {
		if (!parent.getTimer().canAttack()) return false;
		
		if (parent.getPosition().equals(targetPosition)) return false;
		
		target = Game.curCampaign.curArea.getCreatureAtGridPoint(targetPosition);
		
		if (target != null && parent.getFaction().isHostile(target)) {
			move = new Move();
			
			if (parent.canAttackPosition(targetPosition.x, targetPosition.y)) {
				return true;
			} else {
				Item weapon = parent.getInventory().getMainWeapon();
				
				if (weapon.isMeleeWeapon()) {
					return move.canMove(parent, targetPosition, weapon.getWeaponReachMax());
				}
			}
		}
		
		return false;
	}

	@Override public void activate(Creature parent, Point targetPosition) {
		if (parent.canAttackPosition(targetPosition.x, targetPosition.y)) {
			Game.areaListener.getCombatRunner().creatureStandardAttack(parent, target);
		} else {
			Item weapon = parent.getInventory().getMainWeapon();
			
			if (weapon.isMeleeWeapon()) {
				move.addCallback(new AttackCallback(parent));
				move.moveTowards(parent, targetPosition, weapon.getWeaponReachMax());
			}
		}
		
		Game.areaListener.computeMouseState();
	}

	@Override public DefaultAbility getInstance() {
		return new Attack();
	}

	/*
	 * Callback used for Attacking after movement
	 */
	
	private class AttackCallback implements Runnable {
		private Creature parent;
		
		private AttackCallback(Creature parent) {
			this.parent = parent;
		}
		
		@Override public void run() {
			if (parent.canAttackPosition(target.getX(), target.getY())) {
				Game.areaListener.getCombatRunner().creatureStandardAttack(parent, target);
			} 
		}
	}
}
