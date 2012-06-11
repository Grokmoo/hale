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
import java.util.Collections;
import java.util.List;

import de.matthiasmann.twl.Color;

import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.ability.AsyncScriptable;
import net.sf.hale.ability.ListTargeter;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.ability.Targeter;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.interfacelock.EntityAttackAnimation;
import net.sf.hale.interfacelock.InterfaceAILock;
import net.sf.hale.interfacelock.InterfaceCombatLock;
import net.sf.hale.interfacelock.InterfaceLock;
import net.sf.hale.interfacelock.InterfaceTargeterLock;
import net.sf.hale.interfacelock.MovementHandler;
import net.sf.hale.mainmenu.InGameMenu;
import net.sf.hale.particle.Animation;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;

public class CombatRunner {
	private final List<Creature> creatures = new ArrayList<Creature>();
	private int activeCreatureIndex = -1;
	private boolean forceCombatMode = false;
	
	private boolean combatModeInitiating = false;
	
	private int combatStartRound;
	
	public void checkForceCombatMode() {
		if (forceCombatMode) {
			if (!Game.isInTurnMode()) {
				Game.setTurnMode(true);
				startCombat();
				
				int selectedIndex = creatures.indexOf(Game.curCampaign.party.getSelected());
				if (selectedIndex != -1) this.activeCreatureIndex = selectedIndex - 1;
				
				runTurn();
			}
		} else {
			if (Game.isInTurnMode() && !checkContinueCombat()) {
				exitCombat();
			}
		}
	}
	
	public void setForceCombatMode(boolean forceCombatMode) {
		this.forceCombatMode = forceCombatMode;
		
		checkForceCombatMode();
	}
	
	public void nextCombatTurn() {
		if (!Game.isInTurnMode()) {
			Game.setTurnMode(true);
			
			startCombat();
		}
		
		runTurn(); 
	}
	
	/**
	 * Returns the list of creatures currently threatening the specified target.  This is
	 * assumed to be threatening for a purpose other than movement.  To get the list of
	 * threatening creatures for AoOs due to movement, use {@link #getThreateningCreaturesAtNextPosition(Creature)}
	 * Returns an empty list if there are no threatening creatures.
	 * @param target the creature being threatening
	 * @return the list of threatening creatures
	 */
	
	public List<Creature> getThreateningCreatures(AbilityActivator target) {
		List<Creature> threatens = new ArrayList<Creature>();
		
		if (!Game.isInTurnMode()) return threatens;
		
		if (target.stats().isHidden()) return threatens;
		
		for (Creature current : creatures) {
			if (current == target) continue;
			
			if (!current.threatensPosition(target.getX(), target.getY())) continue;
			
			if (!current.hasAttackOfOpportunityAvailable()) continue;
			
			if (current.getFaction().getRelationship(target.getFaction()) != Faction.Relationship.Hostile) continue;
			
			threatens.add(current);
		}
		
		return threatens;
	}
	
	private List<Creature> getThreateningCreaturesAtNextPosition(Creature target) {
		Point curPos = target.getPosition();
		
		List<Creature> creatures = new ArrayList<Creature>();
		
		if (!Game.isInTurnMode()) return creatures;
		
		if (target.stats().isHidden()) return creatures;
		
		for (Creature current : this.creatures) {
			if (current == target) continue;
			
			if (!current.threatensPosition(curPos.x, curPos.y)) continue;
			
			if (!current.hasAttackOfOpportunityAvailable()) continue;
			
			if (!current.getFaction().isHostile(target)) continue;
			
			if (current.moveAoOTakenThisRound(target)) continue;
			
			creatures.add(current);
		}
		
		return creatures;
	}
	
	/**
	 * Causes the specified target creature to provoke attacks of opportunity from all threatening
	 * creatures
	 * @param target the target creature for the AoOs
	 * @param mover the mover that will be paused by any attacks of opportunity, if this is an
	 * attack of opportuntiy due to movement.  If it is not due to movement, this should be null
	 * @return true if at least one attack of opportuntiy requiring a pause was provoked, false otherwise.
	 * Only attacks of opportunity from player characters require a pause
	 */
	
	public boolean provokeAttacksOfOpportunity(Creature target, MovementHandler.Mover mover) {
		boolean lockInterface = false;
		boolean alreadyScrolled = false;
		
		List<Creature> threateningCreatures;
		if (mover == null) threateningCreatures = getThreateningCreatures(target);
		else threateningCreatures = getThreateningCreaturesAtNextPosition(target);
		
		for (Creature current : threateningCreatures) {
			if (mover != null) current.takeMoveAoO(target);
			
			Game.mainViewer.addMessage("green", current.getName() + " gets an Attack of Opportunity against " +
					target.getName());

			TakeAoOCallback takeCallback = new TakeAoOCallback(current, target);
			
			if (current.isPlayerSelectable()) {
				lockInterface = true;
				
				// create a list targeter with no scriptable callback and no ability slot for current
				ListTargeter targeter = new ListTargeter(current, null, null);
				targeter.addAllowedPoint(target.getPosition());
				targeter.setMenuTitle("Attack of Opportunity");
				targeter.setActivateCallback(takeCallback);
				
				// the targeter can only proceed if the target is alive when it is set
				targeter.setCheckValidCallback(new CheckAoOCallback(target));
				
				Game.areaListener.getTargeterManager().addTargeter(targeter);
				
				if (mover != null) {
					mover.incrementPauseCount();
					takeCallback.moverToUnPause = mover;
					
					CancelAoOCallback cancelCallback = new CancelAoOCallback();
					cancelCallback.moverToUnPause = mover;
					targeter.setCancelCallback(cancelCallback);
				}
				
				// scroll to the first creature with an AoO
				if (!alreadyScrolled) {
					Game.areaViewer.addDelayedScrollToCreature(current);
					alreadyScrolled = true;
				}
				
			} else if (current.hasAI()) {
				if (current.getAI().hasFunction(ScriptFunctionType.takeAttackOfOpportunity)) {
					Object executeAttack = current.getAI().executeFunction(
							ScriptFunctionType.takeAttackOfOpportunity, current, target);
					if (Boolean.TRUE.equals(executeAttack)) takeCallback.takeAoO();
				} else {
					takeCallback.takeAoO();
				}
			}
		}
		
		if (lockInterface) {
			Game.interfaceLocker.add(new InterfaceTargeterLock(target));
			return true;
		} else {
			return false;
		}
	}
	
	private void startCombat() {
		creatures.clear();
		Game.timer.resetTime();
		
		List<CreatureWithInitiative> creatureInitiatives = new ArrayList<CreatureWithInitiative>();
		
		for (Entity e : Game.curCampaign.curArea.getEntities()) {
			if (e.getType() != Entity.Type.CREATURE) continue;
			
			Creature c = (Creature) e;
			
			c.getTimer().endTurn(); // disable all actions until c's turn comes up
		
			// roll initiative
			creatureInitiatives.add(new CreatureWithInitiative(c));
		}
		
		// add the creatures sorted in initiative order
		Collections.sort(creatureInitiatives);
		for (CreatureWithInitiative cwi : creatureInitiatives) {
			creatures.add(cwi.creature);
		}
		
		Game.mainViewer.getPortraitArea().disableAllLevelUp();
		Game.curCampaign.getDate().incrementRound();
		Game.curCampaign.curArea.getEntities().elapseRoundsForDeadCreatures(1);
		
		activeCreatureIndex = -1;
		
		combatModeInitiating = false;
	}
	
	public boolean checkAIActivation() {
		boolean aiActivated = false;
		
		for (Creature c : Game.curCampaign.party) {
			aiActivated = aiActivated || checkAIActivation(c);
		}
		
		if (aiActivated && !combatModeInitiating) {
			combatModeInitiating = true;
			Game.mainViewer.addMessage("link", "Hostile creature spotted.  Combat initiated.");
			Game.interfaceLocker.add( new InterfaceCombatLock(Game.curCampaign.party.getSelected(),
					Game.config.getCombatDelay() * 6) );
			combatStartRound = Game.curCampaign.getDate().getTotalRoundsElapsed();
		}
		
		return aiActivated;
	}
	
	private boolean checkAIActivation(Creature creature) {
		Encounter friendlyEncounter = creature.getEncounter();
		List<Creature> friendlies = new ArrayList<Creature>();
		friendlies.add(creature);
		if (friendlyEncounter != null) {
			creature.getEncounter().activateCreatures();
			
			for (Creature c : friendlyEncounter.getCreatures()) {
				friendlies.add(c);
			}
		}
		
		List<Creature> visibleHostiles = AreaUtil.getVisibleCreatures(creature, Faction.Relationship.Hostile);
		
		for (Creature hostile : visibleHostiles) {
			if (friendlyEncounter != null) {
				friendlyEncounter.addHostile(hostile);
			}
			
			if (hostile.getEncounter() != null) {
				hostile.getEncounter().activateCreatures();
				
				// don't add the friendly encounter if the party hasn't been spotted yet
				if (!creature.stats().isHidden()) {
					hostile.getEncounter().addHostiles(friendlies);
				}
				
				if (friendlyEncounter != null) {
					friendlyEncounter.addHostiles(hostile.getEncounter().getCreatures());
				}
			}
		}
		
		if (!Game.isInTurnMode() && visibleHostiles.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	// all attacks end up in this function
	// if animate is false, the attack is completed immediately and this method
	// returns after that, otherwise, the attack could be completed async
	
	private DelayedAttackCallback creatureAttack(Attack attack, Attack offHand, boolean animate) {
		if (attack == null) {
			Logger.appendToErrorLog("Error attempting to attack.");
			return null;
		}
		
		Creature attacker = attack.getAttacker();
		Creature defender = attack.getDefender();
		
		attack.computeFlankingBonus(Game.curCampaign.curArea.getEntities());
		
		if (animate) {
			DelayedAttackCallback cb;
			
			long delay = 0l;
			
			if (attack.isRanged()) {
				if (!attacker.isPlayerSelectable()) {
					// for NPCs, scroll to half way between the attacker and defender for
					// ranged attacks
					Point aScreen = attacker.getScreenPosition();
					Point dScreen = defender.getScreenPosition();
					int avgX = (aScreen.x + dScreen.x) / 2;
					int avgY = (aScreen.y + dScreen.y) / 2;
					
					Game.areaViewer.addDelayedScrollToScreenPoint(new Point(avgX, avgY));
				}
				
				String icon = null;
				Color color = null;
				
				Item weapon = attack.getWeapon();
				switch (weapon.getWeaponType()) {
				case THROWN:
					icon = attack.getWeapon().getProjectileIcon();
					color = attack.getWeapon().getProjectileIconColor();
					break;
				default:
					Item ammo = attacker.getInventory().getEquippedItem(Inventory.EQUIPPED_QUIVER);
					
					if (ammo != null) {
						icon = ammo.getProjectileIcon();
						color = ammo.getProjectileIconColor();
					}
					break;
				}
				
				if (icon != null) {
					// create the animation for ranged attacks
					Animation animation = new Animation(icon);
					animation.setAlpha(1.0f);
					animation.setColor(color);

					Point start = attacker.getPosition();
					Point end = defender.getPosition();

					float distance = (float)start.screenDistance(end);
					float angle = (float)start.angleTo(end);
					float speed = 576.0f;

					animation.setPosition(start.toScreen());
					animation.setVelocityMagnitudeAngle(speed, angle);
					animation.setDuration(distance / speed);
					animation.setRotation(angle * 180.0f / (float)Math.PI);
					
					Game.particleManager.add(animation);
					
					delay = (long) (1000.0 * distance / speed);
				}

			} else {
				//create the attacking animation for melee attacks
				EntityAttackAnimation animation = new EntityAttackAnimation(attacker, defender);
				attacker.addOffsetAnimation(animation);
				Game.particleManager.addEntityOffsetAnimation(animation);
				
				delay = Game.config.getCombatDelay();
			}
			
			// create the callback to compute isHit and damage
			cb = new DelayedAttackCallback(delay, attack, offHand);
			cb.start();

			InterfaceLock lock = new InterfaceLock(attacker, 2l * Game.config.getCombatDelay());
			Game.interfaceLocker.add(lock);
			
			return cb;
			
		} else {
			// run the attack immediately in this thread
			DelayedAttackCallback cb = new DelayedAttackCallback(0, attack, offHand);
			cb.run();
			
			return cb;
		}
	}
	
	public DelayedAttackCallback creatureStandardAttack(Creature attacker, Creature defender) {
		if (defender == null || attacker == null) return null;
		
		if (!attacker.canAttackPosition(defender.getX(), defender.getY())) return null;
		if (!attacker.getTimer().canAttack()) return null;
		
		Attack attack = attacker.mainHandAttack(defender);
		Attack offHand = null;
		
		// create the off hand attack if the attacker is dual-wielding
		if (attacker.getInventory().hasEquippedOffHandWeapon()) {
			offHand = attacker.offHandAttack(defender);
		}
		
		return creatureAttack(attack, offHand, true);
	}
	
	private DelayedAttackCallback creatureAoOAttack(Creature attacker, Creature defender) {
		if (defender == null) return null;
		
		if (!attacker.canAttackPosition(defender.getX(), defender.getY())) return null;
		
		Attack attack = attacker.mainHandAttack(defender);
		
		return creatureAttack(attack, null, true);
	}
	
	public boolean creatureSingleAttack(Creature attacker, Creature defender, int itemSlot) {
		if (defender == null) return false;
		
		if (!attacker.canAttackPosition(defender.getX(), defender.getY())) return false;

		creatureAttack(attacker.attack(defender, itemSlot), null, false);

		return true;
	}
	
	public DelayedAttackCallback creatureSingleAttackAnimate(Creature attacker, Creature defender, int itemSlot) {
		if (defender == null) return null;
		
		if (!attacker.canAttackPosition(defender.getX(), defender.getY())) return null;

		return creatureAttack(attacker.attack(defender, itemSlot), null, true);
	}
	
	public DelayedAttackCallback creatureTouchAttack(Creature attacker, Creature defender, boolean ranged) {
		Attack attack = new Attack(attacker, defender, ranged);
		
		if (ranged)
			return creatureAttack(attack, null, false);
		else
			return creatureAttack(attack, null, true);
	}
	
	public boolean checkContinueCombat() {
		Faction.Relationship rel;
		
		for (int i = 0; i < creatures.size(); i++) {		
			if (!creatures.get(i).isPlayerSelectable() && !creatures.get(i).isAIActive()) continue;
			if (!creatures.get(i).isPlayerSelectable() && (creatures.get(i).isDying() || creatures.get(i).isDead())) continue;
			
			for (int j = i; j < creatures.size(); j++) {
				if (!creatures.get(j).isPlayerSelectable() && !creatures.get(j).isAIActive()) continue;
				if (!creatures.get(j).isPlayerSelectable() && (creatures.get(j).isDying() || creatures.get(j).isDead())) continue;
				
				rel = creatures.get(i).getFaction().getRelationship(creatures.get(j).getFaction());
				if (rel == Faction.Relationship.Hostile) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void runTurn() {	
		boolean continueCombat = checkContinueCombat();
		
		boolean partyDefeated = true;
		for (Creature c : Game.curCampaign.party) {
			if (!c.isDead() && !c.isDying() && !c.isSummoned()) partyDefeated = false;
		}
		
		while ((continueCombat || forceCombatMode) && !partyDefeated) {
			Creature last = lastActiveCreature();
			if (last != null) last.getTimer().endTurn();

			Creature current = nextActiveCreature();
			current.elapseRounds(1);
			
			// dead or dying creatures don't get a turn
			if (current.isDead() || current.isDying()) continue;
			
			Game.selectedEntity = current;
			Game.areaViewer.addDelayedScrollToCreature(current);
			current.searchForHidingCreatures();
			
			if (current.isAIActive()) {
				checkAIActivation(current);
			}
			
			if (current.isPlayerSelectable()) {
				// if current is a PC
				
				Game.curCampaign.party.setSelected(current);
				Game.areaListener.getAreaUtil().setPartyVisibility(Game.curCampaign.curArea);

				// allow the player to take their turn
				break;

			} else if (current.hasAI() && current.isAIActive()) {
				// if current is an NPC with an AI
				try {
					net.sf.hale.ability.Scriptable ai = current.getAI();
					
					AsyncScriptable runner = new AsyncScriptable(ai);
					// set delay prior to starting runner's turn
					runner.setDelayMillis(Game.config.getCombatDelay() * 3);
					runner.executeAsync(ScriptFunctionType.runTurn, current);
					
					Game.interfaceLocker.add(new InterfaceAILock(current, runner));

				} catch (Exception e) {
					Logger.appendToErrorLog("Error running AI script " + current.getAIScript(), e);
				}

				InterfaceCombatLock lock = new InterfaceCombatLock(current, Game.config.getCombatDelay() * 6);
				Game.interfaceLocker.add(lock);
				break;
			}
		}
		
		if (partyDefeated) partyDefeated();
		else if (!continueCombat && !forceCombatMode) exitCombat();
		
		Game.mainViewer.updateInterface();
	}
	
	public void activeCreatureWait(int activePlacesForward) { 
		
		Creature activeCreature = creatures.get(activeCreatureIndex);
		
		// dead, dying, helpless creatures can't change their initiative
		if (activeCreature.isHelpless() || activeCreature.isDying() || activeCreature.isDead())
			return;
		
		// First, figure out how many places forward we need to move
		// Inactive creatures don't count.
		int curIndex = activeCreatureIndex + 1;
		int placesForward = 0;
		int activePlacesLeft = activePlacesForward;
		while (activePlacesLeft > 0) {
			if (curIndex >= creatures.size()) curIndex = 0;
			
			if (curIndex == activeCreatureIndex) {
				//you can't wait more than 1 turn
				return;
			}
			
			Creature c = creatures.get(curIndex);
			if ((!c.isDead() && c.isAIActive()) || c.isPlayerSelectable() ) activePlacesLeft--;
			
			placesForward++;
			curIndex++;
		}
		
		// Now compute the new index
		int newIndex = activeCreatureIndex + placesForward;
		int newActiveCreatureIndex = activeCreatureIndex - 1;
		
		if (newIndex >= creatures.size()) {
			newIndex -= (creatures.size() - 1);
			newActiveCreatureIndex = activeCreatureIndex;
		}
		
		// move the active creature to the new index
		creatures.remove(activeCreatureIndex);
		creatures.add(newIndex, activeCreature);
		activeCreature.getTimer().endTurn();
		
		activeCreatureIndex = newActiveCreatureIndex;
		nextCombatTurn();
		
		Game.mainViewer.updateInterface();
	}
	
	public void partyDefeated() {
		Game.mainViewer.addMessage("link", "The party has been defeated.");
		Game.setTurnMode(false);
		
		Game.curCampaign.party.setDefeated(true);
		
		InGameMenu menu = new InGameMenu(Game.mainViewer);
		menu.openPopupCentered();
	}
	
	public void exitCombat() {
		for (Creature creature : creatures) {
			if (creature.isDead())
				Game.curCampaign.curArea.getEntities().trackDeadCreature(creature);
		}
		
		if (Game.isInTurnMode()) {
			Game.mainViewer.addMessage("link", "Combat has ended.");
		}
		
		Game.setTurnMode(false);
		
		Game.mainViewer.getPortraitArea().enableAllLevelUp();
		
		for (Creature creature : Game.curCampaign.party) {
			if (creature.isDying() && !creature.isDead()) {
				int hp = -creature.getCurrentHP();
				creature.healDamage(hp);
			}
		}
		
		int combatLength = Game.curCampaign.getDate().getTotalRoundsElapsed() - combatStartRound;
		
		for (Encounter encounter : Game.curCampaign.curArea.getEncounters()) {
			if (encounter.isCompleted()) {
				XP.assignEncounterXPAndGold(encounter, combatLength);
			}
		}
		
		Game.curCampaign.partyEncounter.clearHostiles();
		
		Game.selectedEntity = Game.curCampaign.party.getSelected();
		
		Game.mainViewer.updateInterface();
		Game.timer.forceRoundUpdate();
	}
	
	public List<Creature> getNextCreatures(int n) {
		List<Creature> next = new ArrayList<Creature>(n);
		
		int curIndex = activeCreatureIndex;
		
		while (next.size() < n) {
			Creature c = creatures.get(curIndex);
			
			if ( !c.isDead() && (c.isAIActive() || c.isPlayerSelectable()) ) {
				next.add(creatures.get(curIndex));
			}
			
			curIndex++;
			if (curIndex == creatures.size()) curIndex = 0;
		}
		
		return next;
	}
	
	public void insertCreature(Creature creature) {
		creatures.add(activeCreatureIndex + 1, creature);
	}
	
	public Creature lastActiveCreature() { 
		if (activeCreatureIndex == -1) return null;
		
		return creatures.get(activeCreatureIndex);
	}
	
	public Creature nextActiveCreature() {
		activeCreatureIndex++;
		
		if (activeCreatureIndex == creatures.size()) {
			activeCreatureIndex = 0;
		
			Game.curCampaign.getDate().incrementRound();
			Game.curCampaign.curArea.getEntities().elapseRoundsForDeadCreatures(1);
		}
		
		return creatures.get(activeCreatureIndex);
	}
	
	public Creature getActiveCreature() {
		if (activeCreatureIndex == -1) return null;
		else return creatures.get(activeCreatureIndex);
	}
	
	private class CreatureWithInitiative implements Comparable<CreatureWithInitiative> {
		private Creature creature;
		private int initiative;
		
		private CreatureWithInitiative(Creature creature) {
			this.creature = creature;
			this.initiative = creature.stats().get(Bonus.Type.Initiative) + Game.dice.d100();
		}

		@Override public int compareTo(CreatureWithInitiative other) {
			int c1Init = initiative;
			int c2Init = other.initiative;
			
			if (c1Init > c2Init) return -1;
			else if (c1Init < c2Init) return 1;
			else {
				// initiatives are equal, choose based on bonus
				int c1Mod = creature.stats().get(Bonus.Type.Initiative);
				int c2Mod = other.creature.stats().get(Bonus.Type.Initiative);
				
				if (c1Mod > c2Mod) return -1;
				else if (c1Mod < c2Mod) return 1;
				else {
					// bonuses are equal, choose randomly
					return Game.dice.d3() - 2;
				}
			}
		}
	}
	
	/*
	 * Checks if an AoO Targeter is valid when it is set; the target must still
	 * be alive
	 */
	
	private class CheckAoOCallback implements Targeter.CheckValidCallback {
		private Creature target;
		
		private CheckAoOCallback(Creature target) {
			this.target = target;
		}
		
		@Override public boolean isValid() {
			return !target.isDead();
		}
	}
	
	private class CancelAoOCallback implements Runnable {
		private MovementHandler.Mover moverToUnPause;
		
		@Override public void run() {
			Game.areaListener.getTargeterManager().cancelCurrentTargeter();
			Game.mainViewer.getMenu().hide();
			
			if (moverToUnPause != null)
				moverToUnPause.decrementPauseCount();
		}
	}
	
	private class TakeAoOCallback implements Runnable {
		private Creature parent, target;
		private MovementHandler.Mover moverToUnPause;
		
		private TakeAoOCallback(Creature parent, Creature target) {
			this.parent = parent;
			this.target = target;
		}
		
		@Override public void run() {
			Game.areaListener.getTargeterManager().endCurrentTargeter();
			Game.mainViewer.getMenu().hide();
			
			takeAoO();
			
			if (moverToUnPause != null)
				moverToUnPause.decrementPauseCount();
		}
		
		private void takeAoO() {
			parent.takeAttackOfOpportunity();
			CombatRunner.this.creatureAoOAttack(parent, target);
		}
	}
}
