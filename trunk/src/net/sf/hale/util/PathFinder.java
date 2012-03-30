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

package net.sf.hale.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.hale.Area;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;

/**
 * Utility functions for path finding between 2 points or between a point and a list of
 * possible goal points
 * @author Jared Stephen
 *
 */

public class PathFinder {
	/**
	 * Finds the shortest path between the specified start and end points.  Party members are
	 * ignored in determining the entity passabilities.
	 * @param mover the Creature moving at the start point
	 * @param end the end point
	 * @param data data built from the area being traversed
	 * @return the shortest path or null if no path exists
	 */
	
	public static List<Point> findPathIgnorePartyMembers(Creature mover, Point end, Data data) {
		return findPath(mover, end, Collections.singletonList(end), data, true);
	}
	
	/**
	 * Finds the shortest path between the specified start and any one of the specified goal
	 * points using the specified set of data based on the current Area.
	 * @param mover the creature starting at the start point
	 * @param end the end point - used to compute the straight line grid distance between
	 * points.  This should be located in the center of the goal points
	 * @param goals - the list of valid goal points that once reached will form the destination
	 * for the path
	 * @param data data built from the area being traversed
	 * @return the shortest path or null if no path exists
	 */
	
	public static List<Point> findPath(Creature mover, Point end, List<Point> goals, Data data) {
		return findPath(mover, end, goals, data, false);
	}
	
	private static List<Point> findPath(Creature mover, Point end, List<Point> goals, Data data, boolean ignoreParty) {
		List<Creature> threateningCreatures = computeThreateningCreatures(mover, data);
		
		Point start = mover.getPosition();
		Point lowest = new Point();
		
		// compute initial state of open and closed sets based on passabilitiy
		for (int i = 0; i < data.width; i++) {
			for (int j = 0; j < data.height; j++) {
				if (!data.pass[i][j] || !data.entityPass[i][j])
					data.closed[i][j] = true;
				else
					data.closed[i][j] = false;
				
				
				data.open[i][j] = false;
			}
		}
		data.openSet.clear();

		// add the starting point to the list of points we are following
		data.openSet.add(new Point(start.x, start.y));
		data.open[start.x][start.y] = true;
		
		data.gScore[start.x][start.y] = 0;
		data.hScore[start.x][start.y] = AreaUtil.distance(start, end);
		data.fScore[start.x][start.y] = data.hScore[start.x][start.y] + data.gScore[start.x][start.y];
		
		// loop as long as there is at least one point in the open set
		// if there are no points, the entire accessible area has been traversed and there is no path
		while (!data.openSet.isEmpty()) {
			
			// find the point within the open set with the lowest f score, which is most likely
			// to be along the correct path based on the simple grid distance heuristic
			int currentIndex = PathFinder.findLowestFScore(data.openSet, data.fScore, lowest);
			
			if (ignoreParty) {
				if (PathFinder.isEndPointIgnorePartyMembers(lowest, goals, data)) {
					// we are done, find the path using the parents list
					return PathFinder.getFinalPath(data, start, lowest);
				}
			} else {
				if (PathFinder.isEndPoint(lowest, goals, data)) {
					// we are done, find the path using the parents list
					return PathFinder.getFinalPath(data, start, lowest);
				}
			}
			
			data.openSet.remove(currentIndex);
			data.open[lowest.x][lowest.y] = false;
			data.closed[lowest.x][lowest.y] = true;
			
			byte lowestElev = data.area.getElevationGrid().getElevation(lowest.x, lowest.y);
			
			// get the set of possible adjacent points and compute the scores for each one
			Point[] adjacent = AreaUtil.getAdjacentTiles(lowest);
			for (int i = 0; i < adjacent.length; i++) {
				// if the point is outside the grid boundaries
				if (!PathFinder.checkCoordinates(adjacent[i], data)) continue;

				// if the point is in the closed set (already traversed or not passable)
				if (data.closed[adjacent[i].x][adjacent[i].y]) continue;

				// if the elevation is different from the previous point elevation
				if (data.area.getElevationGrid().getElevation(adjacent[i].x, adjacent[i].y) != lowestElev) continue;
				
				int tentativeGScore = data.gScore[lowest.x][lowest.y] + getCost(adjacent[i], threateningCreatures);
				
				boolean tentativeIsBetter;
				if (!data.open[adjacent[i].x][adjacent[i].y]) {
					data.openSet.add(adjacent[i]);
					data.open[adjacent[i].x][adjacent[i].y] = true;
					tentativeIsBetter = true;
				} else if (tentativeGScore < data.gScore[adjacent[i].x][adjacent[i].y]) {
					tentativeIsBetter = true;
				} else {
					tentativeIsBetter = false;
				}

				if (tentativeIsBetter) {
					data.parent[adjacent[i].x][adjacent[i].y].x = lowest.x;
					data.parent[adjacent[i].x][adjacent[i].y].y = lowest.y;

					data.gScore[adjacent[i].x][adjacent[i].y] = tentativeGScore;
					data.hScore[adjacent[i].x][adjacent[i].y] = AreaUtil.distance(adjacent[i], end);
					data.fScore[adjacent[i].x][adjacent[i].y] = data.gScore[adjacent[i].x][adjacent[i].y] +
						data.hScore[adjacent[i].x][adjacent[i].y];
				}
			}
		}
		
		return null;
	}
	
	/*
	 * Returns the list of all creatures that can potentially threaten AoOs against the mover, assuming
	 * the mover were to move into an appropriate position
	 */
	
	private static List<Creature> computeThreateningCreatures(Creature mover, Data data) {
		List<Creature> creatures = new ArrayList<Creature>();
		
		if (mover.stats().isHidden()) return creatures;
		if (!Game.isInTurnMode()) return creatures;
		
		synchronized(data.area.getEntities()) {
			for (Entity entity : data.area.getEntities()) {
				if (entity == mover) continue;
				if (entity.getType() != Entity.Type.CREATURE) continue;

				Creature creature = (Creature)entity;

				if (!creature.getFaction().isHostile(mover)) continue;

				if (creature.moveAoOTakenThisRound(mover)) continue;

				if (!creature.hasAttackOfOpportunityAvailable()) continue;

				creatures.add(creature);
			}
		}
		
		return creatures;
	}
	
	/*
	 * Gets the cost for moving into a given position.  Non-threatened tiles are preferred
	 */
	
	private static final int getCost(Point position, List<Creature> threateningCreatures) {
		for (Creature creature : threateningCreatures) {
			if (creature.threatensPosition(position.x, position.y)) return 2;
		}
		
		return 1;
	}
	
	/*
	 * Returns true if the specified point is a member of the goals set.  The point must not currently
	 * be occuppied by a creature, unless that creature is a party member
	 */
	
	private static final boolean isEndPointIgnorePartyMembers(Point check, List<Point> goals, Data data) {
		for (Point p : goals) {
			if (check.x == p.x && check.y == p.y) {
				Creature c = data.area.getCreatureAtGridPoint(check);
				
				if (c == null) return true;
				
				for (Creature partyMember : Game.curCampaign.party) {
					if (partyMember == c) return true;
				}
			}
		}
		
		return false;
	}
	
	/*
	 * Returns true if the specified point is a member of the goals set.  The point
	 * must not currently be occupied by another creature
	 */
	
	private static final boolean isEndPoint(Point check, List<Point> goals, Data data) {
		for (Point p : goals) {
			if (check.x == p.x && check.y == p.y) {
				return data.area.getCreatureAtGridPoint(check) == null;
			}
		}
		
		return false;
	}
	
	/*
	 * Verifies that a given point is a valid coordinate (inside the area bounds) for the
	 * area we are pathfinding in
	 */
	
	private static final boolean checkCoordinates(Point p, Data data) {
		return (p.x >= 0 && p.x < data.width && p.y >= 0 && p.y < data.height);
	}
	
	/*
	 * Traverses back through the linked list of parent points to compute the path from the
	 * start to end points
	 */
	
	private static final List<Point> getFinalPath(Data data, Point start, Point end) {
		List<Point> path = new ArrayList<Point>();
		Point cur = end;
		
		path.add(cur);
		while (cur.x != start.x || cur.y != start.y) {
			path.add(new Point(data.parent[cur.x][cur.y]));
			Point next = data.parent[cur.x][cur.y];
			cur = next;
		}
		
		if (path.size() > Game.ruleset.getValue("MaximumPathLength"))
			return null;
		
		return path;
	}
	
	/*
	 * Finds the point with the smallest fScore within the openSet
	 */
	
	private static final int findLowestFScore(List<Point> open, int[][] fScore, Point lowest) {
		int i = open.size() - 1;
		
		// Start at the end of the list and work backwards.  This prioritizes points that were
		// added to the open set more recently (and thus are likely to be closer to the end)
		// in the event of a tie.
		
		lowest.x = open.get(i).x;
		lowest.y = open.get(i).y;
		int curLowestFScore = fScore[lowest.x][lowest.y];
		int curLowestIndex = i;
		
		for (i = open.size() - 2; i >= 0; i--) {
			int x = open.get(i).x;
			int y = open.get(i).y;
			
			if (fScore[x][y] <= curLowestFScore) {
				curLowestFScore = fScore[x][y];
				lowest.x = x;
				lowest.y = y;
				curLowestIndex = i;
			}
		}
		
		return curLowestIndex;
	}
	
	/**
	 * The data set for a given area, used for pathfinding.  It is reusable but not
	 * synchronized.  The data set should be updated from the area (for changes in passability)
	 * each time prior to being used
	 * @author Jared Stephen
	 *
	 */
	
	public static class Data {
		/**
		 * Creates the data set for use with pathfinding for the specified Area.  This
		 * object is reusable but not synchronized, so multiple pathing attempts must not modify
		 * it simultaneously.
		 * @param area the Area to create the data for
		 */
		
		public Data(Area area) {
			this.area = area;
			
			boolean[][] passability = area.getPassability();
			
			width = passability.length;
			height = passability[0].length;
			
			pass = new boolean[width][height];
			
			gScore = new int[width][height];
			hScore = new int[width][height];
			fScore = new int[width][height];
			closed = new boolean[width][height];
			open = new boolean[width][height];
			parent = new Point[width][height];
			
			openSet = new ArrayList<Point>(100);
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					pass[i][j] = passability[i][j];
					
					closed[i][j] = !pass[i][j];
					open[i][j] = false;
					parent[i][j] = new Point();
				}
			}
		}
		
		/**
		 * Sets the entity passabilities to the specified matrix, which should be
		 * determined from the area being traversed.  This must be set each time
		 * for the Data prior to it being used.
		 * @param entityPass
		 */
		
		public void setEntityPassabilities(boolean[][] entityPass) {
			this.entityPass = entityPass;
		}
		
		private Area area;
		
		private boolean[][] entityPass;
		
		private final int width;
		private final int height;
		
		private final boolean[][] pass;
		
		private final int[][] gScore;
		private final int[][] hScore;
		private final int[][] fScore;
		private final boolean[][] closed;
		private final boolean[][] open;
		private final Point[][] parent;
		
		private final List<Point> openSet;
	}
}
