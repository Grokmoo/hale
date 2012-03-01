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
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Openable;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A default ability for picking a lock on a door or container.  If the parent is
 * not adjacent to the locked item, then the parent is moved.
 * @author Jared Stephen
 *
 */

public class PickLock implements DefaultAbility {
	private Move move;
	private Openable openable;
	
	@Override public String getActionName() {
		return "Pick Lock";
	}
	
	@Override public boolean canActivate(Creature parent, Point targetPosition) {
		if (!parent.getTimer().canPerformAction("OpenLockCost")) return false;
		
		if (!parent.stats().has(Bonus.Type.LockPicking)) return false;
		
		openable = Game.curCampaign.curArea.getOpenableAtGridPoint(targetPosition);
		if (openable == null) return false;
		
		if (!openable.isLocked()) return false;
		
		move = new Move();
		
		if (AreaUtil.distance(parent.getPosition(), targetPosition) > 1) {
			// need to move towards the door before opening
			return move.canMove(parent, targetPosition, 1);
		}
		
		return true;
		
	}

	@Override public void activate(Creature parent, Point targetPosition) {
		if (AreaUtil.distance(parent.getPosition(), targetPosition) > 1) {
			// move towards the door then open
			move.addCallback(new UnlockCallback(parent));
			move.moveTowards(parent, targetPosition, 1);
		} else {
			unlock(parent, openable);
		}
		
		Game.areaListener.computeMouseState();
	}
	
	/**
	 * The specified Creature will attempt to unlock the specified openable.
	 * 
	 * The Creature's AP is decreased by "openLockCost"
	 * 
	 * @param parent the Creature to attempt to unlock
	 * @param openable the object to unlock
	 * @return true if the object was unlocked, false if it couldn't be unlocked
	 * for any reason
	 */
	
	public boolean unlock(Creature parent, Openable openable) {
		if (AreaUtil.distance(parent.getX(), parent.getY(),
				openable.getX(), openable.getY()) > 1) return false;
		
		if (openable == null || !parent.getTimer().canPerformAction("OpenLockCost")) return false;
		
		if (!parent.stats().has(Bonus.Type.LockPicking)) return false;
		
		parent.getTimer().performAction("OpenLockCost");
		
		if (!openable.isLocked()) return false;
		
		return openable.tryPickLock(parent);
	}

	@Override public DefaultAbility getInstance() {
		return new PickLock();
	}
	
	private class UnlockCallback implements Runnable {
		private Creature parent;
		
		private UnlockCallback(Creature parent) {
			this.parent = parent;
		}
		
		@Override public void run() {
			unlock(parent, openable);
		}
	}
}
