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

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.interfacelock.MovementHandler;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A DefaultAbility for moving a Creature to a specified location.
 * 
 * Stores the results of {@link #canMove(Creature, Point, int)}
 * pathfinding computations when applying moves.  Supports adding
 * callbacks to InterfaceMovementLock when moving.
 * @author Jared Stephen
 *
 */

public class Move implements DefaultAbility {
	// stores the path that is computed in canActivate() for use in activate()
	private List<Point> computedPath;
	
	// stores the list of callbacks that will be added to the InterfaceMovementLock
	private List<Runnable> callbacks = new ArrayList<Runnable>();
	
	private MovementHandler.Mover mover;
	
	private boolean truncatePath = false;
	
	/**
	 * Sets whether computed paths will be truncated based on how much AP the mover has left.
	 * If false, movement paths longer than this max length will return false for canMove.
	 * By default this is set to false.  Even if this is set to true, canMove will still
	 * return false if the mover doesn't have enough AP to move even a single tile.
	 * @param truncatePath whether long paths are truncated to the maximum path length for the
	 * mover based on its AP.
	 */
	
	public void setTruncatePath(boolean truncatePath) {
		this.truncatePath = truncatePath;
	}
	
	/**
	 * Adds a callback to the list of callbacks that will be added to the
	 * InterfaceMovementLock that is created by this Object when implementing
	 * a move.  These callbacks will be run when the InterfaceMovementLock
	 * finishes.
	 * 
	 * @param callback the Runnable callback to add
	 */
	
	public void addCallback(Runnable callback) {
		callbacks.add(callback);
	}
	
	/**
	 * Checks to see if the specified Creature can move to the specified position.
	 * If distanceAway is greater than 0, then checks to see if the specified Creature
	 * can move up to a tile the specified distance away from the targetPosition.
	 * 
	 * @param parent the Creature to move
	 * @param targetPosition the position to move
	 * @param distanceAway the distance away from the targetPosition, in tiles, that should be
	 * reached
	 * @return true if the Creature can move to the specified position, false otherwise
	 */
	
	public boolean canMove(Creature parent, Point targetPosition, int distanceAway) {
		boolean[][] explored = Game.curCampaign.curArea.getExplored();
		boolean[][] passable = Game.curCampaign.curArea.getPassability();
		
		if (targetPosition.x < 0 || targetPosition.y < 0 || targetPosition.x >= explored.length ||
				targetPosition.y >= explored[0].length) return false;
		
		if (parent.isPlayerSelectable()) {
			// player characters can't move to unexplored areas
			if (!explored[targetPosition.x][targetPosition.y])
				return false;
			
			// if the interface is disabling movement
			if (Game.mainViewer.isMoveDisabledDueToOpenWindows())
				return false;
		}
		
		// check for passability
		if (!passable[targetPosition.x][targetPosition.y]) return false;
		
		// check for an elevation difference
		byte curElev = Game.curCampaign.curArea.getElevationGrid().getElevation(parent.getX(), parent.getY());
		byte targetElev = Game.curCampaign.curArea.getElevationGrid().getElevation(targetPosition.x, targetPosition.y);
		if (curElev != targetElev) return false;

		if (parent.isImmobilized()) return false;
		
		// if parent is already at targetPosition
		if (parent.getPosition().equals(targetPosition)) return false;
		
		// if the parent already has a movement lock
		if (parent.isCurrentlyMoving()) return false;
		
		// check to see if a valid path exists up to distanceAway from targetPosition
		computedPath = Game.areaListener.getAreaUtil().findShortestPath(parent, targetPosition, distanceAway);
		
		if (computedPath == null) {
			return false;
		}
		
		// truncate path to how far the parent can actually move
		if (truncatePath) {
			int maxLength = parent.getTimer().getMovementLeft() / 5;
			while (computedPath.size() > maxLength) {
				computedPath.remove(0);
			}
		}
		
		// check that the parent has sufficient Action Points (AP) to move
		// note that normally the computedPath should be a size such that
		// the parent can move it; if there are movement reducing effects
		// then the path may need to be truncated further
		while (!parent.getTimer().canMove(computedPath) && computedPath.size() > 0) {
			computedPath.remove(0);
		}
		
		// cannot move at all
		if (computedPath.size() == 0) return false;
		
		return true;
	}
	
	@Override public boolean canActivate(Creature parent, Point targetPosition) {
		return canMove(parent, targetPosition, 0);
	}

	/**
	 * The parent creature will attempt to find a path and then move along that path towards the
	 * target position, until it is the specified distance away.
	 * 
	 * @param parent the Creature that will attempt to move
	 * @param position the position that parent will move towards.
	 * @param distanceAway the desired distance from parent to target upon completion of the move
	 * @param provokeAoOs whether to provoke attacks of opportunity from threatening creatures
	 * @return true if parent will begin the move and callback will be called, false if
	 * no movement will occur and callback will not be called.  Note that movement can
	 * still be interrupted at a later time, even if this function returns true.  The
	 * specified callback will still be called in that case.
	 */
	
	public boolean moveTowards(Creature parent, Point position, int distanceAway, boolean provokeAoOs) {
		if (!canMove(parent, position, distanceAway)) return false;
		
		if (!checkOverburdened(parent)) return false;

		if (computedPath != null && computedPath.size() != 0) {
			// if we found a path, create the movementlock which will actually do the movement
			// and block player input while it is occuring
			createMover(parent, provokeAoOs);
		} 
		
		// TODO reimplement opening doors in the AI
//		if (parent.stats().get(Stat.Int) >= Game.ruleset.getRuleValue("minIntelligenceToOpenDoors"))  {
//			
//		}

		return true;
	}
	
	/**
	 * The parent creature will attempt to find a path and then move along that path towards the
	 * target position, until it is the specified distance away.  This movement will provoke
	 * attacks of opportunity from any threatening creatures.
	 * 
	 * @param parent the Creature that will attempt to move
	 * @param position the position that parent will move towards.
	 * @param distanceAway the desired distance from parent to target upon completion of the move
	 * @return true if parent will begin the move and callback will be called, false if
	 * no movement will occur and callback will not be called.  Note that movement can
	 * still be interrupted at a later time, even if this function returns true.  The
	 * specified callback will still be called in that case.
	 */
	
	public boolean moveTowards(Creature parent, Point position, int distanceAway) {
		return moveTowards(parent, position, distanceAway, true);
	}
	
	@Override public void activate(Creature parent, Point targetPosition) {
		if (!checkOverburdened(parent)) return;
		
		createMover(parent, true);
		
		Game.areaListener.computeMouseState();
	}

	/*
	 * Checks to see if the parent Creature is overburdened and unable to move
	 */
	
	private boolean checkOverburdened(Creature parent) {
		if (parent.getInventory().getTotalWeightInGrams() > parent.stats().getWeightLimit()) {
			Game.mainViewer.addMessage("red", parent.getName() + " is overburdened and cannot move.");
			return false;
		}
		
		return true;
	}
	
	/*
	 * Adds a new movement lock for this movement path to the global InterfaceLocker
	 */
	
	private void createMover(Creature parent, boolean provoke) {
		this.mover = Game.interfaceLocker.addMove(parent, computedPath, provoke);
		mover.addCallbacks(callbacks);
		
		if (parent.isPlayerSelectable()) {
			mover.setBackground(true);
			
			if (!Game.isInTurnMode() && Game.interfaceLocker.getMovementMode() == MovementHandler.Mode.Party) {
				movePartyInFormation(parent);
			}
		}
	}
	
	private void movePartyInFormation(Creature main) {
		// get passabilities; party members are moving so their current positions
		// will be passable
		
		boolean[][] pass = Game.curCampaign.curArea.getCurrentPassable();
		for (Creature current : Game.curCampaign.party) {
			pass[current.getX()][current.getY()] = true;
		}
		Point lastPosition = computedPath.get(0);
		pass[lastPosition.x][lastPosition.y] = false;
		
		int creatureIndex = 0;
		int pathIndex = 1;
		
		// first add movers for party members to points in the path
		// behind the main mover
		while (true) {
			// we have added all the paths we can based on computedPath
			if (pathIndex >= computedPath.size()) break;
			
			// we have created a path for all creatures in the party
			if (creatureIndex >= Game.curCampaign.party.size()) return;
			
			Creature curCreature = Game.curCampaign.party.get(creatureIndex);
			
			// don't create a mover for main as it already has one
			if (curCreature != main && checkOverburdened(curCreature)) {
				
				List<Point> curPath = Game.areaListener.getAreaUtil().findShortestPath(curCreature,
						computedPath.get(pathIndex), 0);
				
				pathIndex++;
				
				// if no path exists to this point, then we continue to the next point
				if (curPath == null) continue;
				
				// move the curCreature, make it background so it doesn't update the interface like the
				// main mover
				MovementHandler.Mover mover = Game.interfaceLocker.addMove(curCreature, curPath, true);
				mover.setBackground(true);
				
				// the destination for the current creature will be occupied
				lastPosition = curPath.get(0);
				pass[lastPosition.x][lastPosition.y] = false;
			}
			
			creatureIndex++;
		}
		
		if (creatureIndex >= Game.curCampaign.party.size()) return;
		
		Point[] adjacent = AreaUtil.getAdjacentTiles(lastPosition);
		int direction = computePathDirection(adjacent, main);
		Point lastTarget = lastPosition;
		
		// now add movers for any additional party members beyond the length of the path above
		while (creatureIndex < Game.curCampaign.party.size()) {
			
			Creature curCreature = Game.curCampaign.party.get(creatureIndex);
			
			// don't create a mover for main as it already has one
			if (curCreature == main) {
				creatureIndex++;
				continue;
			}
			
			// find the direction for the next tile to use based on the direction
			// that the main creature traveled
			adjacent = AreaUtil.getAdjacentTiles(lastTarget);
			
			// set the new target position for the next iteration
			lastTarget = adjacent[direction];
			
			Point newPosition = getNearestAvailablePoint(lastTarget, pass, computedPath.get(0));
			
			// if no new point is found, we are stuck and must exit
			if (newPosition == null) return;
			
			List<Point> curPath = Game.areaListener.getAreaUtil().findShortestPathIgnoreParty(curCreature, newPosition, pass);
			
			// if no path is found to this point, we can try to find a different point
			if (curPath == null) {
				pass[newPosition.x][newPosition.y] = false;
				continue;
			}
			
			// if curCreature is already at the right point
			if (curPath.size() == 0) {
				lastPosition = newPosition;
				pass[newPosition.x][newPosition.y] = false;
				creatureIndex ++;
				continue;
			}
			
			if (AreaUtil.distance(curPath.get(0), computedPath.get(0)) > 6) {
				// if the distance away is too great, better to exit than send the party member on a long journey across the map
				return;
			}
			
			if ( curPath.size() > 3 * AreaUtil.distance(curCreature.getPosition(), computedPath.get(0)) ) {
				// if the path is too long, try to find another point
				pass[newPosition.x][newPosition.y] = false;
				continue;
			}
			
			// move the curCreature, make it background so it doesn't update the interface like the
			// main mover
			MovementHandler.Mover mover = Game.interfaceLocker.addMove(curCreature, curPath, true);
			mover.setBackground(true);
			
			// the destination for the current creature will be occupied
			lastPosition = curPath.get(0);
			pass[lastPosition.x][lastPosition.y] = false;
			
			creatureIndex++;
		}
	}
	
	private static final int MaxPartySearchRadius = 4;
	
	// finds the empty point closest to the specified point; mainCreaturePosition is used
	// for tie breaking
	
	private Point getNearestAvailablePoint(Point target, boolean[][] pass, Point mainCreaturePosition) {
		if (checkCoordinates(target)) {
			if (pass[target.x][target.y]) return target;
		}
		
		// search at each radius up to the max radius
		for (int r = 1; r <= MaxPartySearchRadius; r++) {
			int minDistance = Integer.MAX_VALUE;
			Point minPoint = null;
			
			// find the point at this radius that is closest to mainCreaturePosition
			for (int i = 0; i < r * 6; i++) {
				Point current = AreaUtil.convertPolarToGrid(target, r, i);
				
				// check that the point is inside the area
				if (!checkCoordinates(current)) continue;
				
				if (pass[current.x][current.y]) {
					int distance = AreaUtil.distance(current, mainCreaturePosition);
					
					if (distance < minDistance) {
						minDistance = distance;
						minPoint = current;
					}
				}
			}
			
			// if at least one point was passable, return it, otherwise go to the next radius
			if (minPoint != null) return minPoint;
		}
		
		return null;
	}
	
	private final boolean checkCoordinates(Point p) {
		return (p.x >= 0 && p.x < Game.curCampaign.curArea.getWidth() &&
				p.y >= 0 && p.y < Game.curCampaign.curArea.getHeight());
	}
	
	// computes the index of the direction from the destination of the computed path to the
	// start of the computed path.  The index is from 0 to 5 inclusive, and is used with
	// AreaUtil.findAdjacentPoints
	
	private int computePathDirection(Point[] adjacent, Creature main) {
		Point end = main.getPosition();
		
		int smallestDistance = Integer.MAX_VALUE;
		int smallestIndex = 0;
		
		for (int i = 0; i < adjacent.length; i++) {
			int distance = AreaUtil.distance(adjacent[i], end);
			
			if (distance < smallestDistance) {
				smallestDistance = distance;
				smallestIndex = i;
			}
		}
		
		return smallestIndex;
	}
	
	@Override public DefaultAbility getInstance() {
		return new Move();
	}
	
	@Override public String getActionName() {
		return "Move";
	}
	
	/**
	 * Returns the current computed path for this Move, or null if there is no computed path.
	 * @return the computed path for this Move
	 */
	
	public List<Point> getComputedPath() {
		return computedPath;
	}
	
	/**
	 * Returns the total path length of the computed path for this Move.  Calling this when
	 * there is no computed path will throw a NullPointerException.  The path is computed only
	 * by a call to {@link #canMove(Creature, Point, int)} that returns true.
	 * @return the total path length of the computed path for this Move.
	 */
	
	public int getComputedPathLength() {
		return computedPath.size();
	}
	
	/**
	 * Returns the InterfaceMovementLock associated with this Move if one has
	 * been created, or null otherwise
	 * @return the InterfaceMovementLock associated with this Move
	 */
	
	public MovementHandler.Mover getMover() {
		return mover;
	}
}
