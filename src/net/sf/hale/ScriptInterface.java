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

import java.io.File;
import java.io.IOException;

import net.sf.hale.ability.AbilityActivateCallback;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.ability.DelayedScriptCallback;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.area.Area;
import net.sf.hale.area.Transition;
import net.sf.hale.entity.CreatedItemModel;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.Encounter;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.EntityManager;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Location;
import net.sf.hale.entity.NPC;
import net.sf.hale.interfacelock.InterfaceLock;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.rules.Attack;
import net.sf.hale.rules.Campaign;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Date;
import net.sf.hale.rules.Dice;
import net.sf.hale.rules.Faction;
import net.sf.hale.rules.Quality;
import net.sf.hale.rules.QuestEntry;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.rules.WorldMapLocation;
import net.sf.hale.rules.XP;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.view.CampaignConclusionPopup;
import net.sf.hale.view.CutscenePopup;
import net.sf.hale.widgets.HTMLPopup;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;

public class ScriptInterface implements HasScriptState {
	/**
	 * This is a debugging tool.  When false, random encounters will not
	 * spawn, which makes traveling through areas much quicker
	 */
	
	public boolean SpawnRandomEncounters = true;
	
	public AIScriptInterface ai;
	

	/**
	 * Writes the current content of the messages box on the bottom right of the screen to a file
	 */
	
	public void writeMessageLog() {
		try {
			FileUtil.writeStringToFile(new File("message.html"), Game.mainViewer.getMessageBoxContents());
		} catch (IOException e) {
			Logger.appendToErrorLog("Error writing message log", e);
		}
	}
	
	
	
	public void addMessage(String text) {
		Game.mainViewer.addMessage(text);
	}
	
	public void addMessage(String font, String text) {
		Game.mainViewer.addMessage(font, text);
	}
	

	
	public void addFadeAway(String text, int x, int y, String color) {
		Game.mainViewer.addFadeAway(text, x, y, Color.parserColor(color));
	}
	
	public void addEncounterToArea(String encounterID, int x, int y) {
		Encounter encounter = Game.curCampaign.getEncounter(encounterID, new Location(Game.curCampaign.curArea, x, y));
		encounter.checkSpawnCreatures();
		
		Game.curCampaign.curArea.setEntityVisibility();
		
		Game.areaListener.getCombatRunner().checkAIActivation();
	}
	
	public void addItemToArea(Item item, Location location) {
		item.setLocation(location);
		Game.curCampaign.curArea.addItem(item);
	}
	
	

	
	/**
	 * Creates a new attack, computes whether the attack is a hit and the amount of damage and returns it
	 * No scripts are run on either the parent or target
	 * @param attacker
	 * @param defender
	 * @return the newly created attack
	 */
	
	public Attack getOffHandAttack(Creature attacker, Creature defender) {
		return attacker.getAttack(defender, Inventory.Slot.OffHand.toString());
	}
	
	public void singleAttack(Creature attacker, Point position) {
		singleAttack(attacker, Game.curCampaign.curArea.getCreatureAtGridPoint(position));
	}
	
	public void singleAttack(Creature attacker, Creature defender) {
		Game.areaListener.getCombatRunner().creatureSingleAttack(attacker, defender, Inventory.Slot.MainHand);
	}
	
	public void singleAttack(Creature attacker, Creature defender, String slot) {
		Game.areaListener.getCombatRunner().creatureSingleAttack(attacker, defender, Inventory.Slot.valueOf(slot));
	}
	
	
	/**
	 * Performs a standard attack for the attacker against the defender.  Note that this
	 * method waits on animations and attacks of opportunity and thus does not return
	 * immediately.  This method must not be called from the main thread or it will
	 * cause a deadlock
	 * @param attacker the creature attacking
	 * @param defender the creature being attacked
	 * @return whether the attack hit
	 */
	
	public boolean standardAttack(Creature attacker, Creature defender) {
		boolean result = false;
		
		try {
			DelayedAttackCallback cb = null;
			
			if (attacker.timer.canAttack()) {
				cb = Game.areaListener.getCombatRunner().creatureStandardAttack(attacker, defender);
			}
			
			if (cb != null) {
				
				// wait for cb to finish
				synchronized(cb) {
					while (cb.isAlive()) {
						cb.wait();
					}
				}
				
				result = cb.isAttackHit();
			}
			
			if (!(attacker instanceof PC)) {
				// wait on any targeters caused by attacks of opportunity
				synchronized(Game.areaListener.getTargeterManager()) {
					while (Game.areaListener.getTargeterManager().isInTargetMode()) {
						Game.areaListener.getTargeterManager().wait();
					}
				}
				
				Thread.sleep(Game.config.getCombatDelay() * 3);
			}
		} catch (InterruptedException e) {
			// the attack was interrupted and did not take place
			result = false;
		} catch (Exception e) {
			Logger.appendToErrorLog("Error executing standard attack", e);
			return false;
		}
		
		return result;
	}
	
	public boolean creatureCanTouchTarget(Creature creature, Creature target) {
		if (target == null || creature == null) return false;
		
		if (creature == target) return true;
		
		return creature.getLocation().getDistance(target) <= 1;
	}
	
	
	
	
	
	/* MOVED TO CREATURE (THE FOLLOWING 3 METHODS)
	public Creature createSummon(String creatureID, Creature parent, int duration) {
		NPC creature = EntityManager.getNPC(creatureID);
		creature.setFaction(parent.getFaction());
		
		if (parent.isPlayerFaction()) {
			Game.curCampaign.party.addSummon(creature);
		} else {
			// hostile summon
			creature.setEncounter(parent.getEncounter());
			parent.getEncounter().getCreaturesInArea().add(creature);
		}
		
		creature.setSummoned(duration);
		
		return creature;
	}
	*/
	/*
	public void finishSummon(NPC creature, Location location) {
		creature.resetTime();
		
		if (location.isPassable() && location.getCreature() == null) {
			creature.setLocation(location);
			location.getArea().getEntities().addEntity(creature);
		}
		
		if (Game.isInTurnMode()) {
			Game.areaListener.getCombatRunner().insertCreature(creature);
		}
		
		Game.mainViewer.updateInterface();
		Game.areaListener.getCombatRunner().checkAIActivation();
	}
	
	public void finishSummon(NPC creature, Point position) {
		finishSummon(creature, new Location(Game.curCampaign.curArea, position));
	}
	*/
	
	/**
	 * Causes all nearby creatures to attempt to search for the specified creature
	 * @param target
	 * @param hidePenalty
	 */
	
	public void performSearchChecksForCreature(Creature target, int hidePenalty) {
		// only search if this creature is hidden
		if (!target.stats.isHidden()) return;
		
		for (Creature creature : target.getLocation().getArea().getEntities().getCreaturesWithinRadius(target.getLocation().getX(),
				target.getLocation().getY(), Game.curCampaign.curArea.getVisibilityRadius())) {
			
			if (creature.getFaction().getRelationship(target.getFaction()) != Faction.Relationship.Hostile) continue;
			
			if ( !creature.hasVisibility(target.getLocation()) ) continue;			
			
			if (creature.performSearchCheck(target, hidePenalty)) {
				Game.mainViewer.addFadeAway("Spotted", target.getLocation().getX(),
						target.getLocation().getY(), new Color(0xFFAbA9A9));
				break;
			}
		}
	}
	
	public boolean creatureCanAttackTarget(Creature creature, Creature target) {
		if (target == null || creature == null) return false;
		
		if (creature == target) return false;
		
		if (!creature.timer.canAttack()) return false;
		
		return creature.canAttack(target.getLocation());
	}
	
	
	public Currency getPartyCurrency() {
		return Game.curCampaign.partyCurrency;
	}
	
	public Party getParty() {
		return Game.curCampaign.party;
	}
	
	
	
	public boolean hasQuestEntry(String title) {
		return Game.curCampaign.questEntries.hasEntry(title);
	}
	
	public QuestEntry getQuestEntry(String title) {
		// see if an entry with this title has already been created
		if (Game.curCampaign.questEntries.hasEntry(title))
			return Game.curCampaign.questEntries.getEntry(title);
		
		// create a new entry
		QuestEntry entry = new QuestEntry(title);
		Game.curCampaign.questEntries.addEntry(entry);
		
		return entry;
	}
	
	public void addPartyXP(int xp) {
		XP.addPartyXP(xp);
	}
	
	
	
	public void runExternalScript(String scriptLocation, String function) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		scriptable.executeFunction(function);
	}
	
	public void runExternalScript(String scriptLocation, String function, Object arg) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		scriptable.executeFunction(function, arg);
	}
	
	public void runExternalScript(String scriptLocation, String function, Object[] args) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		scriptable.executeFunction(function, args);
	}
	
	public void runExternalScriptWait(String scriptLocation, String function, float seconds) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		DelayedScriptCallback cb = new DelayedScriptCallback(scriptable, function);
		cb.setDelay(seconds);
		cb.start();
	}
	
	public void runExternalScriptWait(String scriptLocation, String function, float seconds, Object arg) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		DelayedScriptCallback cb = new DelayedScriptCallback(scriptable, function);
		cb.setDelay(seconds);
		cb.addArgument(arg);
		cb.start();
	}
	
	public void runExternalScriptWait(String scriptLocation, String function, float seconds, Object[] arg) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		DelayedScriptCallback cb = new DelayedScriptCallback(scriptable, function);
		cb.setDelay(seconds);
		cb.addArguments(arg);
		cb.start();
	}
	
	
	/**
	 * Reveals all world map locations in the current campaign to the player
	 */
	
	public void revealAllWorldMapLocations() {
		for (WorldMapLocation location : Game.curCampaign.worldMapLocations) {
			if (!location.isRevealed()) {
				location.setRevealed(true);
			}
		}
		
		Game.mainViewer.addMessage("link", "A new location has been added to your world map.");
	}
	
	public void revealWorldMapLocation(String ref) {
		WorldMapLocation location = Game.curCampaign.getWorldMapLocation(ref);
		
		if (location != null) {
			if (!location.isRevealed()) {
				location.setRevealed(true);
				Game.mainViewer.addMessage("link", "A new location has been added to your world map.");
			}
		} else {
			Logger.appendToErrorLog("Error revealing world map location.  " + ref + " not found.");
		}
	}
	
	
	public void activateTransition(String ref) {
		Transition transition = Game.curCampaign.getAreaTransition(ref);
		
		if (transition == null) {
			Logger.appendToErrorLog("Error activating area transition.  " + ref + " not found.");
		} else {
			transition.activate();
		}
	}
	
	
	
	public AbilityActivateCallback createButtonCallback(AbilitySlot slot, String function) {
		AbilityActivateCallback callback = new AbilityActivateCallback(slot, function);
		return callback;
	}
	
	public void addMenuButton(String label, AbilityActivateCallback callback) {
		Button button = new Button(label);
		button.addCallback(callback);
		
		Game.mainViewer.getMenu().addButton(button);
	}
	
	public void showMenu() {
		Game.mainViewer.getMenu().show();
	}
	
	public boolean addMenuLevel(String title) {
		return Game.mainViewer.getMenu().addMenuLevel(title);
	}
	
	public void hideOpenWindows() {
		Game.mainViewer.closeAllWindows();
	}
	

	public void showCutscene(String id) {
		Cutscene cutscene = Game.ruleset.getCutscene(id);
		if (cutscene == null) {
			Logger.appendToWarningLog("Cutscene " + id + " not found");
			return;
		}
		
		CutscenePopup popup = new CutscenePopup(Game.mainViewer, cutscene);
		Game.mainViewer.showPopup(popup);
	}
	
	
	public HTMLPopup createHTMLPopup(String resource) {
		try {
			HTMLPopup popup = new HTMLPopup(resource);
		
			return popup;
		} catch (Exception e) {
			Logger.appendToErrorLog("Error creating HTML popup", e);
		}
		
		return null;
	}

	public CombatRunner combatRunner() { return Game.areaListener.getCombatRunner(); }
	
	public Campaign campaign() { return Game.curCampaign; }
	
	public Ruleset ruleset() { return Game.ruleset; }
	public Area currentArea() { return Game.curCampaign.curArea; }
	
	public Area getArea(String id) { return Game.curCampaign.getArea(id); }
	
	public Dice dice() { return Game.dice; }
	
	public Date date() { return Game.curCampaign.getDate(); }
	
	public boolean isInCombatMode() { return Game.isInTurnMode(); }
	
	
	
	
	public void sleepStandardDelay(int multiple) {
		try {
			Thread.sleep(Game.config.getCombatDelay() * multiple);
		} catch (InterruptedException e) {
			return;
		}
	}
	
	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			return;
		}
	}
	
	
	public void put(String ref, Object data) {
		Game.curCampaign.scriptState.put(ref, data);
	}
	
	public Object get(String ref) {
		return Game.curCampaign.scriptState.get(ref);
	}
	
	public void lockInterface(float timeSeconds) {
		InterfaceLock lock = new InterfaceLock(null, (long)(1000.0f * timeSeconds));
		Game.interfaceLocker.add(lock);
		
		Game.interfaceLocker.interruptMovement();
	}
	
	public void clearRevealedAreas() {
		Game.timer.clearTemporarySightAreas();
	}
	
	public void revealArea(int x, int y, int radius, int duration) {
		Game.timer.addTemporarySightArea(new Point(x, y), radius, duration);
	}
	
	public void setFactionRelationship(String faction1, String faction2, String relationship) {
		Faction.CustomRelationship cr = new Faction.CustomRelationship(faction1, faction2, relationship);
		
		cr.setFactionRelationships();
		
		Game.curCampaign.addCustomRelationship(cr);
		
		Game.areaListener.getCombatRunner().checkAIActivation();
	}
	
	public void unlockInterface() {
		Game.interfaceLocker.clear();
	}
	
	public void scrollToPosition(int x, int y) {
		Point screenPoint = AreaUtil.convertGridToScreenAndCenter(x, y);
		
		Game.areaViewer.addDelayedScrollToScreenPoint(screenPoint);
	}
	
	public void scrollToCreature(String entityID) {
		Game.areaViewer.addDelayedScrollToCreature(Game.curCampaign.curArea.getEntityWithID(entityID));
	}
	
	public void scrollToCreature(Entity entity) {
		Game.areaViewer.addDelayedScrollToCreature(entity);
	}
	
	public void exitToMainMenu() {
		Game.mainViewer.exitToMainMenu();
	}
	
	public void shakeScreen() {
		Game.areaViewer.addScreenShake();
	}
	
	public CampaignConclusionPopup showCampaignConclusionPopup() {
		CampaignConclusionPopup popup = new CampaignConclusionPopup(Game.mainViewer);
		
		return popup;
	}
	
	public NPC getNPC(String entityID) {
		return EntityManager.getNPC(entityID);
	}
	
	public Item getItem(String itemID, Quality quality) {
		return EntityManager.getItem(itemID, quality);
	}
	
	public Item getItem(String itemID, String quality) {
		return EntityManager.getItem(itemID, quality);
	}
	
	public CreatedItemModel getCreatedItemModel(String baseItemID, String createdItemID) {
		return new CreatedItemModel(baseItemID, createdItemID);
	}
	
	/**
	 * Adds the specified PC as a companion in the party.  If the PC is already
	 * a member of the party, does nothing.
	 * @param companion
	 */
	
	public void addCompanion(PC companion) {
		Game.curCampaign.party.add(companion);
		Game.mainViewer.updateInterface();
	}
	
	/**
	 * Removes the specified PC as a companion to the party.  If the PC is not in
	 * the party, does nothing.
	 * @param companion
	 */
	
	public void removeCompanion(PC companion) {
		Game.curCampaign.party.remove(companion);
		Game.mainViewer.updateInterface();
	}
}
