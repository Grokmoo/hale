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
import net.sf.hale.ability.Ability;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.util.Point;


public class RoundTimer {
	private boolean active = false;
	
	private int AP;
	private int maxAP;
	
	private final Creature parent;
	
	public RoundTimer(Creature parent) {
		this.parent = parent;
	}
	
	public RoundTimer(RoundTimer other, Creature parent) {
		this.AP = other.AP;
		this.parent = parent;
	}
	
	public boolean hasTakenAnAction() {
		return (AP < maxAP);
	}
	
	public void reset() {
		active = true;
		
		maxAP = Game.ruleset.getValue("BaseActionPoints") + parent.stats().get(Bonus.Type.ActionPoint) * 100;
		
		AP = maxAP;
	}
	
	public int getMovementCost(List<Point> path) {
		if (path == null) return Integer.MAX_VALUE;
		
		int total = 0;
		for (Point p : path) {
			int bonus = Game.curCampaign.curArea.getMovementBonus(p.x, p.y);
			total += parent.stats().get(Stat.MovementCost) * (100 - bonus) / 100;
		}
		
		return total;
	}
	
	public boolean canMove(Point p) {
		List<Point> path = new ArrayList<Point>();
		path.add(p);
		
		return canMove(path);
	}
	
	public boolean canMove(List<Point> path) {
		if (parent.isImmobilized()) return false;
		
		if (!Game.isInTurnMode()) return true;
		
		if (getMovementCost(path) <= AP) return true;
		else return false;
	}
	
	public int getAP() { return this.AP; }
	
	public boolean canAttack() {
		return canPerformAction(parent.stats().get(Stat.AttackCost));
	}
	
	public boolean performAttack() {
		return performAction(parent.stats().get(Stat.AttackCost));
	}
	
	public boolean canPerformAction(String actionText) {
		return canPerformAction(Game.ruleset.getValue(actionText));
	}
	
	public boolean canPerformAction(int cost) {
		if (parent.isHelpless()) return false;
		
		if (this.AP < cost) return false;
		
		return true;
	}
	
	public boolean canActivateAbility(String abilityID) {
		Ability ability = Game.ruleset.getAbility(abilityID);
		
		return canPerformAction(ability.getAPCost());
	}
	
	private int getCostToEquipItem(Item item) {
		switch (item.getItemType()) {
		case WEAPON:
		case SHIELD:
			return Game.ruleset.getValue("EquipItemCost") * (100 - parent.stats().get(Bonus.Type.ActionPointEquipHands)) / 100;
		case ARMOR:
			return Game.ruleset.getValue("EquipArmorCost");
		default:
			return Game.ruleset.getValue("EquipItemCost");
		}
	}
	
	public boolean canPerformEquipAction(Item item) {
		return canPerformAction(getCostToEquipItem(item));
	}
	
	public int getMovementLeft() {
		int cost = parent.stats().get(Stat.MovementCost);
		int finalCost = cost / 100;
		
		return 5 * AP / finalCost;
	}
	
	public boolean move(Point p) {
		List<Point> path = new ArrayList<Point>();
		path.add(p);
		
		return move(path);
	}
	
	public boolean move(List<Point> path) { 
		if (!canMove(path)) return false;
		
		// only deduct AP in turn based mode
		if (Game.isInTurnMode()) {
			AP -= getMovementCost(path);
		}
		
		return true;
	}
	
	public void endTurn() {
		active = false;
		AP = 0;
	}
	
	public boolean performAction(String actionText) {
		return performAction(Game.ruleset.getValue(actionText));
	}
	
	public boolean performAction(int AP) {
		if (!canPerformAction(AP)) return false;
		
		// only deduct AP if game is in turn based mode
		if (Game.isInTurnMode()) {
			this.AP -= AP;
		}
		
		Game.mainViewer.updateEntity(parent);
		
		return true;
	}
	
	public boolean performEquipAction(Item item) {
		return performAction(this.getCostToEquipItem(item));
	}
	
	public boolean isActive() { return active; }
}
