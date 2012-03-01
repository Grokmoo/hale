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

import net.sf.hale.AreaTransition;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.interfacelock.InterfaceCallbackLock;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * Default ability for activating an area transition and traveling to a new area.  If the
 * parent is not adjacent to the area transition, the parent is first moved.
 * @author Jared Stephen
 *
 */

public class Travel implements DefaultAbility {
	// storage for movement properties if movement is needed
	private Move move;
	private AreaTransition target;
	
	@Override public String getActionName() {
		return "Travel";
	}

	@Override public boolean canActivate(Creature parent, Point targetPosition) {
		target = Game.curCampaign.curArea.getTransitionAtGridPoint(targetPosition);
		
		// if target is an active (visible) area transition
		if (target != null && target.isActivated()) {
			move = new Move();
			
			if (AreaUtil.distance(parent.getPosition(), targetPosition) > 1) {
				// need to move towards the container before opening
				return move.canMove(parent, targetPosition, 1);
			}
			
			return true;
		}
		
		return false;
	}

	@Override public void activate(Creature parent, Point targetPosition) {
		if (AreaUtil.distance(parent.getPosition(), targetPosition) > 1) {
			// move towards the transition then travel
			move.addCallback(new TravelCallback(parent, targetPosition));
			move.moveTowards(parent, targetPosition, 1);
		} else {
			Game.curCampaign.transition(target, Game.curCampaign.curArea.getName());
		}
		
		Game.areaListener.computeMouseState();
	}

	@Override public DefaultAbility getInstance() {
		return new Travel();
	}
	
	/*
	 * Callback used to Travel after movement
	 */
	
	private class TravelCallback implements Runnable {
		private Creature parent;
		private Point targetPosition;
		private boolean alreadyLocked;
		
		private TravelCallback(Creature parent, Point targetPosition) {
			this.parent = parent;
			this.targetPosition = targetPosition;
		}
		
		@Override public void run() {
			// if the interface is currently locked (from outstanding movement for example,
			// wait until it unlocks and then call this callback again
			// only do this at most once
			if (!alreadyLocked && Game.interfaceLocker.locked()) {
				InterfaceCallbackLock lock = new InterfaceCallbackLock(parent, Game.config.getCombatDelay());
				lock.addCallback(this);
				Game.interfaceLocker.add(lock);
				alreadyLocked = true;
				return;
			}
			
			if (AreaUtil.distance(parent.getPosition(), targetPosition) <= 1) {
				Game.curCampaign.transition(target, Game.curCampaign.curArea.getName());
			}
		}
	}

}
