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

package net.sf.hale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.bonus.Stat;
import net.sf.hale.defaultability.Move;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.interfacelock.MovementHandler;
import net.sf.hale.rules.Faction;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;

/**
 * Class containing static methods that are useful for many of the JavaScript scripts,
 * particularly creature AI scripts.
 * 
 * @author Jared Stephen
 *
 */

public class AIScriptInterface {
	
	/**
	 * Returns the path length (number of tiles the parent creature must move) in order
	 * to reach the specified position from its current position.
	 * @param parent the parent used as the starting point for the movement path
	 * @param position the destination position for the movement path
	 * @return the path length of the computed path or Integer.MAX_VALUE if no path is found
	 */
	
	public static List<Point> getMovementPath(Creature parent, Point position) {
		Move move = new Move();
		
		if (!move.canMove(parent, position, 0)) return null;
		
		return move.getComputedPath();
	}
	
	/**
	 * The parent creature will attempt to find a path and then move along that path towards the
	 * target position, until it is the specified distance away.  The function will wait until
	 * movement is completed before returning.
	 * 
	 * This function will return true if movement occurred, even if it was interrupted.  It will
	 * only return false if no movement occurred.
	 * 
	 * The movement from this method will provoke attacks of opportunity from any threatening
	 * creatures.
	 * 
	 * @param parent the Creature that will attempt to move
	 * @param x the x grid coordinate of the position to move towards
	 * @param y the y grid coordinate of the position to move towards
	 * @param distanceAway the desired distance from parent to target upon completion of the move
	 * @return true if the parent moved, false otherwise
	 */
	
	public static boolean moveTowards(Creature parent, int x, int y, int distanceAway) {
		return moveTowards(parent, new Point(x, y), distanceAway, true);
	}
	
	/**
	 * The parent creature will attempt to find a path and then move along that path towards the
	 * target position, until it is the specified distance away.  The function will wait until
	 * movement is completed before returning.
	 * 
	 * This function will return true if movement occurred, even if it was interrupted.  It will
	 * only return false if no movement occurred.
	 * 
	 * @param parent the Creature that will attempt to move
	 * @param position the position that parent will move towards.
	 * @param distanceAway the desired distance from parent to target upon completion of the move
	 * @param provokeAoOs whether to provoke Attacks of opportunity from threatening creatures
	 * @return true if the parent moved, false otherwise
	 */
	
	public static boolean moveTowards(Creature parent, Point position, int distanceAway, boolean provokeAoOs) {
		Move move = new Move();
		move.setTruncatePath(true);
		
		if (!move.canMove(parent, position, distanceAway)) {
			return false;
		}
		
		if (!move.moveTowards(parent, position, distanceAway, provokeAoOs)) {
			return false;
		}
		
		MovementHandler.Mover mover = move.getMover();
		if (mover == null) return false;
		
		try {
			// wait for the movement to complete
			synchronized(mover) {
				while (!mover.isFinished()) {
					mover.wait();
				}
			}
		} catch (InterruptedException e) {
			// thread was interrupted, should exit
			return false;
		}
		
		return true;
	}
	
	/**
	 * The parent creature will attempt to find a path and then move along that path towards the
	 * target position, until it is the specified distance away.  The function will wait until
	 * movement is completed before returning.
	 * 
	 * This function will return true if movement occurred, even if it was interrupted.  It will
	 * only return false if no movement occurred.
	 * 
	 * The movement from this method will provoke attacks of opportunity from any threatening
	 * creatures.
	 * 
	 * @param parent the Creature that will attempt to move
	 * @param position the position that parent will move towards.
	 * @param distanceAway the desired distance from parent to target upon completion of the move
	 * @return true if the parent moved, false otherwise
	 */
	
	public static boolean moveTowards(Creature parent, Point position, int distanceAway) {
		return moveTowards(parent, position, distanceAway, true);
	}
	
	/**
	 * Returns a point with the hex grid coordinates of the tile nearest to parent
	 * that is passable by creatures but unoccupied by any creature.
	 * 
	 * Note that this point may not be unique, however the distance between parent
	 * and the returned point will be less than or equal to the distance between
	 * parent and all other unoccupied points in the area
	 * 
	 * @param center the point to find the closest point to
	 * @param maxRadius the maximum search radius for the point.  The creature's visibility
	 * radius is often a good choice for this.
	 * @return the closest empty tile, or null if no empty tile was found within the
	 * search radius
	 */
	
	public static Point findClosestEmptyTile(Point center, int maxRadius) {
		if (checkEmpty(center)) return center;
		
		for (int r = 1; r <= maxRadius; r++) {
			for (int i = 0; i < r * 6; i++) {
				Point grid = AreaUtil.convertPolarToGrid(center, r, i);
				if (checkEmpty(grid)) return grid;
			}
		}
		
		return null;
	}
	
	private static boolean checkEmpty(Point grid) {
		if (!Game.curCampaign.curArea.isPassable(grid.x, grid.y)) return false;

		return (Game.curCampaign.curArea.getCreatureAtGridPoint(grid) == null);
	}
	
	/**
	 * All creatures currently threatening parent with available attacks of opportuntity will
	 * recieve one attack of opportunity against parent with their main weapon.
	 * 
	 * If one or more of the creatures getting an attack of opportunity is player controlled
	 * (isPlayerSelectable() returns true), then this function will wait until the player
	 * makes their choice (whether to use the attack or not) before returning.
	 * 
	 * @param parent the creature to provoke attacks against
	 */
	
	public static void provokeAttacksOfOpportunity(Creature parent) {
		Game.areaListener.getCombatRunner().provokeAttacksOfOpportunity(parent, null);
	}
	
	/**
	 * Returns the creature meeting the specified faction relationship with parent
	 * having the largest value of (maximumHP - currentHP) that is visible to parent
	 * and not dead.
	 * 
	 * @param parent the creature looking for the target, whose visibility and
	 * faction are used
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @return the visible creature meeting the relationship criterion with the most damage.  null
	 * if no visible creature meeting the relationship criterion is found.
	 */
	
	public static Creature findCreatureWithMostDamage(Creature parent, String relationship) {
		List<Creature> creatures = getLiveVisibleCreatures(parent, relationship);
		
		int bestDamage = Integer.MIN_VALUE;
		Creature bestCreature = null;
		
		for (Creature c : creatures) {
			int cDamage = c.stats().get(Stat.MaxHP) - c.getCurrentHP();
			
			if (cDamage > bestDamage) {
				bestCreature = c;
				bestDamage = cDamage;
			}
		}
		
		return bestCreature;
	}
	
	/**
	 * Returns the creature meeting the specified faction relationship with parent
	 * having the largest value of maximumHP that is visible to parent
	 * and not dead.
	 * 
	 * @param parent the creature looking for the target, whose visibility and
	 * faction are used
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @return the visible creature meeting the relationship criterion with the most maximum HP.  null
	 * if no visible creature meeting the relationship criterion is found.
	 */
	
	public static Creature findCreatureWithMostMaxHP(Creature parent, String relationship) {
		List<Creature> creatures = getLiveVisibleCreatures(parent, relationship);
		
		int bestMaxHP = 0;
		Creature bestCreature = null;
		
		for (Creature c : creatures) {
			if (c.stats().get(Stat.MaxHP) > bestMaxHP) {
				bestCreature = c;
				bestMaxHP = c.stats().get(Stat.MaxHP);
			}
		}
		
		return bestCreature;
	}
	
	/**
	 * Finds the creature meeting the specified relationship with the parent creature and being the
	 * nearest in terms of hex tile distance.  Ties are decided randomly
	 * @param parent the parent creature
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @return the nearest creature to the parent
	 */
	
	public static Creature findNearestCreature(Creature parent, String relationship) {
		// list of creatures tied for closest
		List<Creature> closest = new ArrayList<Creature>();

		List<Creature> creatures = getLiveVisibleCreatures(parent, relationship);

		int smallestDistance = Integer.MAX_VALUE;

		//compute the simple distance
		for (Creature target : creatures) {
			int distance = AreaUtil.distance(parent.getX(), parent.getY(), target.getX(), target.getY());

			if (distance < smallestDistance) {
				closest.clear();
				smallestDistance = distance;
				closest.add(target);
			} else if (distance == smallestDistance) {
				closest.add(target);
			}
		}

		if (closest.size() == 0) return null;
		else {
			// return randomly chosen target from list of closest
			return closest.get(Game.dice.rand(0, closest.size() - 1));
		}
	}
	
	/**
	 * Returns the creature meeting the specified relationship with parent and being the nearest
	 * in the sense explained below.
	 * 
	 * If parent is using a melee weapon, finds the creature with the shortest path
	 * distance that parent would need to travel in order to attack.  If parent has a
	 * weapon with a reach greater than one tile and there are multiple creatures parent
	 * can attack without moving, then creatures with a smaller hex grid distance to parent
	 * are preferred.
	 * 
	 * If parent is using a non-melee (ranged or thrown) weapon, finds the creature with the shortest
	 * hex distance to parent.
	 * 
	 * In either case, ties are decided randomly.
	 * 
	 * @param parent the creature to compare faction relationship with and whose
	 * visibility and position are used
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @return the nearest creature to parent meeting the criterion for the purposes of attacking.
	 * If no creatures are found, returns null.  If multiple creatures are tied, returns one from
	 * the list chosen randomly.
	 */
	
	public static Creature findNearestCreatureToAttack(Creature parent, String relationship) {
		try {
			// list of creatures tied for closest
			List<Creature> closest = new ArrayList<Creature>();

			List<Creature> creatures = getLiveVisibleCreatures(parent, relationship);

			int smallestDistance = Integer.MAX_VALUE;

			if (parent.getInventory().getMainWeapon().getWeaponType() != Item.WeaponType.MELEE) {
				// for non-melee weapons, compute using the simple distance; we actually want closest
				for (Creature target : creatures) {
					int distance = AreaUtil.distance(parent.getX(), parent.getY(), target.getX(), target.getY());

					if (distance < smallestDistance) {
						closest.clear();
						smallestDistance = distance;
						closest.add(target);
					} else if (distance == smallestDistance) {
						closest.add(target);
					}
				}
			} else {
				// for melee, compute using the distance to move towards the creature

				int reach = parent.getInventory().getMainWeapon().getWeaponReachMax();

				for (Creature target : creatures) {
					int distance = AreaUtil.distance(parent.getX(), parent.getY(), target.getX(), target.getY());

					// if the creature is not in melee range, compute the distance by finding a path
					if (distance > reach) {
						List<Point> path =
							Game.areaListener.getAreaUtil().findShortestPath(parent, target.getPosition(), reach);

						// distance is path length + 1
						if (path != null) distance = path.size() + 1;
						else distance = Integer.MAX_VALUE;
					}

					if (distance < smallestDistance) {
						closest.clear();
						smallestDistance = distance;
						closest.add(target);
					} else if (distance == smallestDistance) {
						closest.add(target);
					}
				}
			}

			if (closest.size() == 0) return null;
			else {
				// return randomly chosen target from list of closest
				return closest.get(Game.dice.rand(0, closest.size() - 1));
			}

		} catch (Exception e) {
			Logger.appendToErrorLog("Error finding creature to attack ", e);
		}

		return null;
	}
	
	/**
	 * Returns a list of all creatures that parent can attack from its current
	 * position without moving, using its currently equipped main weapon.
	 * 
	 * @param parent the creature to be attacking
	 * @return all creatures within reach of parent's main weapon without
	 * parent needing to move.
	 */
	
	public static List<Creature> getAttackableCreatures(Creature parent) {
		ArrayList<Creature> attackable = new ArrayList<Creature>();
		
		List<Creature> creatures;
		Encounter encounter = parent.getEncounter();
		
		// if parent has an encounter, use the encounter hostile list to save
		// needing to compute the list of visible creatures.  All visible creatures
		// will be on the knownHostiles list
		if (encounter != null) creatures = encounter.getKnownHostiles();
		else creatures = getLiveVisibleCreatures(parent, "Hostile");
		
		for (Creature c : creatures) {
			if (c.isDying() || c.isDead()) continue;
			
			if ( !parent.canAttackPosition(c.getX(), c.getY()) ) continue;
			
			if (c.stats().isHidden()) continue;
			
			attackable.add(c);
		}
		
		return attackable;
	}
	
	/**
	 * Returns a list of all creatures occupying tiles directly adjacent or equal to
	 * parent's position matching the specified faction relationship
	 * @param parent the creature whose position is used
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @return the list of all creatures that can be touched by a creature at parent's position.
	 * This list will generally include parent, unless parent does not actually occupy the area
	 * (as is the case for scroll casters).
	 */
	
	public static List<Creature> getTouchableCreatures(AbilityActivator parent, String relationship) {
		ArrayList<Creature> creatures = new ArrayList<Creature>();
		
		Faction activeFaction = parent.getFaction();
		
		Faction.Relationship rel = null;
		
		if (relationship != null) rel = Faction.Relationship.valueOf(relationship);
		
		Point[] positions = AreaUtil.getAdjacentTiles(parent.getX(), parent.getY());
		
		for (Point p : positions) {
			Creature c = Game.curCampaign.curArea.getCreatureAtGridPoint(p);
			if (c != null) {
				Faction.Relationship curRel = activeFaction.getRelationship(c.getFaction());
				if (rel == null || curRel == rel) {
					creatures.add(c);
				}
			}
		}
		
		// add the creature standing at the same position as caster.  This will be different
		// than just adding the caster for spells cast from scrolls
		Creature c = Game.curCampaign.curArea.getCreatureAtGridPoint(parent.getX(), parent.getY());
		if (c != null && (rel == null || activeFaction.getRelationship(activeFaction) == rel)) {
			creatures.add(c);
		}
		
		return creatures;
	}
	
	/**
	 * Returns the list of all creatures within line of sight of parent meeting the specified
	 * faction relationship and within the specified range.
	 * 
	 * @param parent the creature whose visibility is used and to compare faction and distance to
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @param range the maximum distance between parent and target allowed for returned creatures.
	 * This distance is in feet (equal to 5 times the hex grid distance).
	 * @return the list of all creatures visible to parent meeting the specified constraints.
	 */
	
	public static List<Creature> getVisibleCreaturesWithinRange(AbilityActivator parent, String relationship, int range) {
		Faction.Relationship rel = null;
		if (relationship != null) rel = Faction.Relationship.valueOf(relationship);
		
		List<Creature> creatures = AreaUtil.getVisibleCreatures(parent, rel);
		
		Iterator<Creature> iter = creatures.iterator();
		while (iter.hasNext()) {
			Creature c = iter.next();
			if (AreaUtil.distance(c.getX(), c.getY(), parent.getX(), parent.getY()) * 5 > range) {
				iter.remove();
			}
		}
		
		return creatures;
	}
	
	/**
	 * Returns the list of all creatures within the specified range of parent meeting the specified
	 * faction relationship.
	 * 
	 * @param parent the creature to compare faction and distance to
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @param range the maximum distance between parent and target allowed for returned creatures.
	 * This distance is in feet (equal to 5 times the hex grid distance).
	 * @return the list of all creatures within range of the parent meeting the faction relationship
	 * constraint.
	 */
	
	public static List<Creature> getAllCreaturesWithinRange(AbilityActivator parent, String relationship, int range) {
		Faction.Relationship rel = null;
		if (relationship != null) rel = Faction.Relationship.valueOf(relationship);
		
		List<Creature> creatures =
			Game.curCampaign.curArea.getEntities().getCreaturesWithinRadius(parent.getX(), parent.getY(), range / 5);
		
		if (rel != null) {
			Iterator<Creature> iter = creatures.iterator();
			while (iter.hasNext()) {
				if (parent.getFaction().getRelationship(iter.next().getFaction()) != rel) {
					iter.remove();
				}
			}
		}
		
		return creatures;
	}
	
	/**
	 * Returns the list of all creatures within line of sight of parent meeting the specified
	 * faction relationship and who are not dead.
	 * 
	 * @param parent the creature whose visibility is used and to compare faction with
	 * @param relationship The faction relationship between parent and the target that is found.
	 * Must be either "Hostile", "Neutral", or "Friendly"
	 * @return the list of all creatures visible to parent meeting the specified constraints.
	 */
	
	public static List<Creature> getLiveVisibleCreatures(AbilityActivator parent, String relationship) {
		Faction.Relationship rel = null;
		if (relationship != null) rel = Faction.Relationship.valueOf(relationship);
		
		Encounter encounter = parent.getEncounter();
		List<Creature> creatures;
		
		if (rel == Faction.Relationship.Hostile && encounter != null) {
			creatures = encounter.getKnownHostiles(); 
		} else {
			creatures = AreaUtil.getVisibleCreatures(parent, rel);
		}
		
		Iterator<Creature> iter = creatures.iterator();
		while (iter.hasNext()) {
			Creature c = iter.next();
			if (c.isDead() || c.isDying() || c.stats().isHidden()) {
				iter.remove();
			}
		}
		
		return creatures;
	}
	
	public static void sortCreatureListClosestFirst(AbilityActivator parent, List<Creature> creatures) {
		Collections.sort(creatures, new CreatureSorter(parent));
	}
	
	private static class CreatureSorter implements Comparator<Creature> {
		private AbilityActivator parent;
		
		private CreatureSorter(AbilityActivator parent) {
			this.parent = parent;
		}

		@Override public int compare(Creature a, Creature b) {
			return AreaUtil.distance(a.getX(), a.getY(), parent.getX(), parent.getY()) -
					AreaUtil.distance(b.getX(), b.getY(), parent.getX(), parent.getY());
		}
	}
}
