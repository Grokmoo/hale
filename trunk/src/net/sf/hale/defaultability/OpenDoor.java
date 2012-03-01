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
import net.sf.hale.entity.Door;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A DefaultAbility for opening a door.  Can also move to the door and
 * then open, if needed.
 * 
 * @author Jared Stephen
 *
 */

public class OpenDoor implements DefaultAbility {
	private static final String doorOpenAction = "Open Door";
	private static final String doorCloseAction = "Close Door";
	
	// storage for movement properties as needed
	private Move move;
	private Door door;
	
	private String actionName;
	
	@Override public String getActionName() {
		return actionName;
	}

	@Override public boolean canActivate(Creature parent, Point targetPosition) {
		if (!parent.getTimer().canPerformAction("OpenDoorCost")) return false;
		
		door = Game.curCampaign.curArea.getDoorAtGridPoint(targetPosition);
		
		if (door != null) {
			if (door.isOpen()) actionName = OpenDoor.doorCloseAction;
			else actionName = OpenDoor.doorOpenAction;
			
			move = new Move();
			
			if (AreaUtil.distance(parent.getPosition(), targetPosition) > 1) {
				// need to move towards the door before opening
				return move.canMove(parent, targetPosition, 1);
			}
			
			return true;
		}
		
		return false;
	}

	@Override public void activate(Creature parent, Point targetPosition) {
		if (AreaUtil.distance(parent.getPosition(), targetPosition) > 1) {
			// move towards the door then open
			move.addCallback(new OpenDoorCallback(parent));
			move.moveTowards(parent, targetPosition, 1);
		} else {
			toggleDoor(parent, Game.curCampaign.curArea.getDoorAtGridPoint(targetPosition));
		}
		
		Game.areaListener.computeMouseState();
	}

	/**
	 * The specified Creature will attempt to open the specified door
	 * 
	 * The creature's AP is decreased by the "openDoorCost"
	 * 
	 * There are many reasons why a creature might fail to open the object, including
	 * not having enough AP, a lock on the object, or not being adjacent or on top of the container.
	 * 
	 * @param parent the Creature that will attempt to open the container
	 * @param door the door to be opened
	 * @return true if the Door was successfully opened, false otherwise
	 */
	
	public boolean toggleDoor(Creature parent, Door door) {
		if (AreaUtil.distance(parent.getX(), parent.getY(),
				door.getX(), door.getY()) > 1) return false;

		if (door == null || !parent.getTimer().canPerformAction("OpenDoorCost")) return false;
		
		parent.getTimer().performAction("OpenDoorCost");
		
		if (door.isOpen()) {
			Creature creature = Game.curCampaign.curArea.getCreatureAtGridPoint(door.getPosition());
			if (creature == null) {
				door.close(parent);
			} else {
				Game.mainViewer.addMessage("red", creature.getName() + " blocks the door.");
			}
			
			return !door.isOpen();
		}
		else {
			door.open(parent);
			
			return door.isOpen();
		}
	}
	
	@Override public DefaultAbility getInstance() {
		return new OpenDoor();
	}
	
	/*
	 * A callback that is used to open the door after finishing
	 * moving towards the door
	 */
	
	private class OpenDoorCallback implements Runnable {
		private Creature parent;
		
		private OpenDoorCallback(Creature parent) {
			this.parent = parent;
		}
		
		@Override public void run() {
			toggleDoor(parent, door);
		}
	}
}
