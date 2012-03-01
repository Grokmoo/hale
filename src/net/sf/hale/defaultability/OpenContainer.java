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
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A default ability for opening a container.  Can also move towards a container
 * and then open it, as needed.
 * @author Jared Stephen
 *
 */

public class OpenContainer implements DefaultAbility {
	// storage for movement properties if movement is needed
	private Move move;
	private Container container;
	
	@Override public String getActionName() {
		return "Open Container";
	}

	@Override public boolean canActivate(Creature parent, Point targetPosition) {
		if (!parent.getTimer().canPerformAction("OpenContainerCost")) return false;
		
		container = Game.curCampaign.curArea.getContainerAtGridPoint(targetPosition);
		
		if (container != null) {
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
			// move towards the container then open
			move.addCallback(new OpenContainerCallback(parent));
			move.moveTowards(parent, targetPosition, 1);
		} else {
			openContainer(parent, Game.curCampaign.curArea.getContainerAtGridPoint(targetPosition));
		}
		
		Game.areaListener.computeMouseState();
	}

	@Override public DefaultAbility getInstance() {
		return new OpenContainer();
	}
	
	/**
	 * The specified Creature will attempt to open the specified container
	 * 
	 * The creature's AP is decreased by the "openContainerCost"
	 * 
	 * There are many reasons why a creature might fail to open the object, including
	 * not having enough AP, a lock on the object, or not being adjacent or on top of the container.
	 * 
	 * @param parent the Creature that will attempt to open the container
	 * @param container the container to be opened
	 * @return true if the Container was successfully opened, false otherwise
	 */
	
	public boolean openContainer(Creature parent, Container container) {
		if (AreaUtil.distance(parent.getX(), parent.getY(),
				container.getX(), container.getY()) > 1) return false;

		if (container != null && parent.getTimer().canPerformAction("OpenContainerCost")) {
			parent.getTimer().performAction("OpenContainerCost");
			
			container.open(parent);

			// if the container was locked, it may not have actually opened
			// so check before showing the contents
			if (container.isOpen()) {
				if (container.isWorkbench()) {
					Game.mainViewer.craftingWindow.setVisible(true);
				} else {
					Game.mainViewer.containerWindow.setOpenerContainer(parent, container);
					Game.mainViewer.containerWindow.setVisible(true);
				}
				
				return true;
			}
			
		}
		
		return false;
	}

	
	/*
	 * A callback that is used to open the container after finishing
	 * moving towards the container
	 */
	
	private class OpenContainerCallback implements Runnable {
		private Creature parent;
		
		private OpenContainerCallback(Creature parent) {
			this.parent = parent;
		}
		
		@Override public void run() {
			openContainer(parent, container);
		}
	}
}
