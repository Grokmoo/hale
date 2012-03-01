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

import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;

public class XP {
	private final List<Integer> pointsForLevel;
	
	public XP() {
		int levelMultFactor = Game.ruleset.getValue("XPLevelMultFactor");
		int levelExpBaseInv = Game.ruleset.getValue("XPLevelExpBaseInv");
		double levelExpBase = 1.0 + 1.0 / ((double)levelExpBaseInv);
		
		this.pointsForLevel = new ArrayList<Integer>();
		
		pointsForLevel.add(0);
		pointsForLevel.add(0);
		double currentPoints = 0.0;
		
		for (int level = 1; level <= Role.MAX_LEVELS; level++) {
			currentPoints += level * levelMultFactor * Math.pow(levelExpBase, level);
			
			int pointsForThisLevel = (int)((currentPoints + 50) / 100) * 100;
			
			pointsForLevel.add(pointsForThisLevel);
		}
	}
	
	public int getPointsForLevel(int level) { return pointsForLevel.get(level); }
	
	public void assignEncounterXPAndGold(Encounter encounter) {
		float EC = (float)encounter.getChallenge() / Game.ruleset.getValue("EncounterChallengeFactor");
		
		int baseXP = (int) (EC * (float)Game.ruleset.getValue("EncounterXPFactor"));
		
		Currency reward = new Currency();
		for (Creature c : encounter.getCreatures()) {
			reward.addCP(Game.dice.rand(c.getMinCurrencyReward(), c.getMaxCurrencyReward()));
		}
		
		Game.curCampaign.partyCurrency.add(reward);
		
		rewardXP(baseXP);
		
		Game.mainViewer.addMessage("green", "The party earned " + baseXP + " experience points and " + reward.shortString() + ".");
	}
	
	public void addPartyXP(int xp) {
		rewardXP(xp);
		
		Game.mainViewer.addMessage("green", "The party has earned " + xp + " experience points.");
	}
	
	private void rewardXP(int baseXP) {
		int partySize = 0;
		for (Creature c : Game.curCampaign.party) {
			if (!c.isDead() && !c.isSummoned()) partySize++;
		}
		
		int perCharacterXP = baseXP / partySize;
		
		for (Creature c : Game.curCampaign.party) {
			if (!c.isDead() && !c.isSummoned()) {
				c.addExperiencePoints(perCharacterXP);
			}
		}
	}
}
