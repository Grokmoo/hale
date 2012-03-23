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
import net.sf.hale.ability.CircleTargeter;
import net.sf.hale.ability.ConeTargeter;
import net.sf.hale.ability.DelayedScriptCallback;
import net.sf.hale.ability.LineTargeter;
import net.sf.hale.ability.ListTargeter;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.ability.SpellScrollCaster;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.interfacelock.InterfaceLock;
import net.sf.hale.particle.AngleDistributionBase;
import net.sf.hale.particle.Animation;
import net.sf.hale.particle.CircleParticleGenerator;
import net.sf.hale.particle.DistanceDistributionBase;
import net.sf.hale.particle.DistributionBase;
import net.sf.hale.particle.EquallySpacedAngleDistribution;
import net.sf.hale.particle.FixedAngleDistribution;
import net.sf.hale.particle.FixedDistribution;
import net.sf.hale.particle.FixedDistributionWithBase;
import net.sf.hale.particle.GaussianAngleDistribution;
import net.sf.hale.particle.GaussianDistribution;
import net.sf.hale.particle.GaussianDistributionWithBase;
import net.sf.hale.particle.LineParticleGenerator;
import net.sf.hale.particle.ParticleGenerator;
import net.sf.hale.particle.RectParticleGenerator;
import net.sf.hale.particle.SpeedDistributionBase;
import net.sf.hale.particle.UniformAngleDistribution;
import net.sf.hale.particle.UniformArcDistribution;
import net.sf.hale.particle.UniformDistribution;
import net.sf.hale.particle.UniformDistributionWithBase;
import net.sf.hale.particle.VelocityTowardsPointDistribution;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.rules.Attack;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Date;
import net.sf.hale.rules.DelayedAttackCallback;
import net.sf.hale.rules.Dice;
import net.sf.hale.rules.Faction;
import net.sf.hale.rules.Merchant;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.rules.XP;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.view.CampaignConclusionPopup;
import net.sf.hale.view.ConversationPopup;
import net.sf.hale.view.CutscenePopup;
import net.sf.hale.widgets.HTMLPopup;

import de.matthiasmann.twl.Button;

public class ScriptInterface {
	public static AIScriptInterface ai;
	
	/**
	 * Writes the current content of the messages box on the bottom right of the screen to a file
	 */
	
	public static void writeMessageLog() {
		try {
			FileUtil.writeStringToFile(new File("message.html"), Game.mainViewer.getMessageBoxContents());
		} catch (IOException e) {
			Logger.appendToErrorLog("Error writing message log", e);
		}
	}
	
	public static void addMessage(String text) {
		Game.mainViewer.addMessage(text);
	}
	
	public static void addMessage(String font, String text) {
		Game.mainViewer.addMessage(font, text);
	}
	
	public static void addEncounterToArea(Encounter encounter, int x, int y) {
		encounter.addToArea(Game.curCampaign.curArea, x, y);
		
		Game.curCampaign.curArea.setEntityVisibility();
		
		Game.areaListener.getCombatRunner().checkAIActivation();
	}
	
	public static void addItemToArea(Item item, Point position) {
		item.setPosition(position.x, position.y);
		Game.curCampaign.curArea.addItem(item);
	}
	
	public static boolean rangedTouchAttack(SpellScrollCaster attacker, Creature defender) {
		return rangedTouchAttack(attacker.getParent(), defender);
	}
	
	public static boolean meleeTouchAttack(SpellScrollCaster attacker, Creature defender) {
		return meleeTouchAttack(attacker.getParent(), defender);
	}
	
	public static boolean rangedTouchAttack(Creature attacker, Creature defender) {
		boolean success = false;
		try {
			DelayedAttackCallback cb = Game.areaListener.getCombatRunner().creatureTouchAttack(attacker, defender, true);
			
			if (cb != null) {
				// wait for cb to finish
				synchronized(cb) {
					while (cb.isAlive()) {
						cb.wait();
					}
				}
				
				success = cb.isAttackHit();
			}
		} catch (Exception e) {
			Logger.appendToErrorLog("Error performing ranged touch attack.", e);
		}
		
		return success;
	}
	
	public static boolean meleeTouchAttack(Creature attacker, Creature defender) {
		boolean result = false;
		
		try {
			DelayedAttackCallback cb = Game.areaListener.getCombatRunner().creatureTouchAttack(attacker, defender, false);
			
			if (cb != null) {
				// wait for cb to finish
				synchronized(cb) {
					while (cb.isAlive()) {
						cb.wait();
					}
				}
				
				result = cb.isAttackHit();
			}
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error performing melee touch attack.", e);
		}
		
		return result;
	}
	
	public static boolean singleAttackAnimate(Creature attacker, Creature target) {
		boolean result = false;

		try {
			DelayedAttackCallback cb = null;

			if (attacker.getTimer().canAttack()) {
				cb = Game.areaListener.getCombatRunner().creatureSingleAttackAnimate(attacker, target, Inventory.EQUIPPED_MAIN_HAND);
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
		} catch (InterruptedException e) {
			// the attack was interrupted and did not take place
			result = false;
		}

		return result;
	}
	
	public static Attack getAttack(Creature attacker, Creature defender, int itemSlot) {
		Attack attack = attacker.attack(defender, itemSlot);
		attack.computeFlankingBonus(Game.curCampaign.curArea.getEntities());
		attack.computeIsHit();
		
		return attack;
	}
	
	/**
	 * Creates a new attack, computes whether the attack is a hit and the amount of damage and returns it
	 * No scripts are run on either the parent or target
	 * @param attacker
	 * @param defender
	 * @return the newly created attack
	 */
	
	public static Attack getMainHandAttack(Creature attacker, Creature defender) {
		return getAttack(attacker, defender, Inventory.EQUIPPED_MAIN_HAND);
	}
	
	/**
	 * Creates a new attack, computes whether the attack is a hit and the amount of damage and returns it
	 * No scripts are run on either the parent or target
	 * @param attacker
	 * @param defender
	 * @return the newly created attack
	 */
	
	public static Attack getOffHandAttack(Creature attacker, Creature defender) {
		return getAttack(attacker, defender, Inventory.EQUIPPED_OFF_HAND);
	}
	
	public static void singleAttack(Creature attacker, Point position) {
		singleAttack(attacker, Game.curCampaign.curArea.getCreatureAtGridPoint(position));
	}
	
	public static void singleAttack(Creature attacker, Creature defender) {
		Game.areaListener.getCombatRunner().creatureSingleAttack(attacker, defender, Inventory.EQUIPPED_MAIN_HAND);
	}
	
	public static void singleAttack(Creature attacker, Creature defender, int itemSlot) {
		Game.areaListener.getCombatRunner().creatureSingleAttack(attacker, defender, itemSlot);
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
	
	public static boolean standardAttack(Creature attacker, Creature defender) {
		boolean result = false;
		
		try {
			DelayedAttackCallback cb = null;
			
			if (attacker.getTimer().canAttack()) {
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
			
			if (!attacker.isPlayerSelectable()) {
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
	
	public static boolean creatureCanTouchTarget(Creature creature, Creature target) {
		if (target == null || creature == null) return false;
		
		if (creature == target) return true;
		
		Point[] tiles = AreaUtil.getAdjacentTiles(creature.getX(), creature.getY());
		for (int i = 0; i < tiles.length; i++) {
			if (target.getX() == tiles[i].x && target.getY() == tiles[i].y) return true;
		}
		
		return false;
	}
	
	public static Creature summonCreature(String creatureID, Point position, Creature parent, int duration) {
		Creature creature = Game.entityManager.getCreature(creatureID);
		
		if (Game.curCampaign.party.contains(parent)) {
			creature.setFaction(Game.ruleset.getString("PlayerFaction"));
			
			creature.setEncounter(Game.curCampaign.partyEncounter);
			Game.curCampaign.partyEncounter.addAreaCreature(creature);
			
			Game.curCampaign.party.add(creature);
		} else {
			creature.setFaction(parent.getFaction());
			creature.setEncounter(parent.getEncounter());
			parent.getEncounter().addAreaCreature(creature);
		}
		
		Area area = Game.curCampaign.curArea;
		
		if (area.isPassable(position.x, position.y) && area.getCreatureAtGridPoint(position) == null) {
			creature.setPosition(position.x, position.y);
			area.getEntities().addEntity(creature);
		}
		
		creature.setAIActive(true);
		creature.resetAll();
		creature.setVisibility(true);
		
		if (Game.isInTurnMode()) {
			Game.areaListener.getCombatRunner().insertCreature(creature);
		}
		
		creature.setSummoned(duration);
		
		Game.mainViewer.updateInterface();
		
		Game.areaListener.getCombatRunner().checkAIActivation();
		
		return creature;
	}
	
	public static void performSearchChecksForCreature(Creature parent, int hidePenalty) {
		// only search if this creature is hidden
		if (!parent.stats().isHidden()) return;
		
		for (Creature c : Game.curCampaign.curArea.getEntities().getCreaturesWithinRadius(parent.getX(),
				parent.getY(), Game.curCampaign.curArea.getVisibilityRadius())) {
			
			if (c.getFaction().getRelationship(parent.getFaction()) != Faction.Relationship.Hostile) continue;
			
			c.setVisibility(false);
			
			if (!c.getVisibility(parent.getX(), parent.getY())) continue;
			
			if (c.doSearchCheck(parent, hidePenalty)) {
				break;
			}
		}
	}
	
	public static boolean creatureCanAttackTarget(Creature creature, Creature target) {
		if (target == null || creature == null) return false;
		
		if (creature == target) return false;
		
		if (!creature.getTimer().canAttack()) return false;
		
		return creature.canAttackPosition(target.getX(), target.getY());
	}
	
	public static Currency getPartyCurrency() {
		return Game.curCampaign.partyCurrency;
	}
	
	public static Party getParty() {
		return Game.curCampaign.party;
	}
	
	public static void moveCreature(Creature c, int x, int y) {
		c.setPosition(x, y);
		c.setVisibility(false);
		Game.areaListener.getCombatRunner().checkAIActivation();
	}
	
	public static void showMerchant(String merchantName) {
		if (merchantName == null) return;
		
		Merchant merchant = Game.curCampaign.getMerchant(merchantName);
		if (merchant == null) {
			Logger.appendToErrorLog("Error locating merchant: " + merchantName);
			return;
		}
		
		Game.mainViewer.setMerchant(merchant);
		Game.mainViewer.merchantWindow.setVisible(true);
	}
	
	public static boolean hasQuestEntry(String title) {
		return Game.curCampaign.questEntries.hasEntry(title);
	}
	
	public static QuestEntry getQuestEntry(String title) {
		// see if an entry with this title has already been created
		if (Game.curCampaign.questEntries.hasEntry(title))
			return Game.curCampaign.questEntries.getEntry(title);
		
		// create a new entry
		QuestEntry entry = new QuestEntry(title);
		Game.curCampaign.questEntries.addEntry(entry);
		
		return entry;
	}
	
	public static void addPartyXP(int xp) {
		XP.addPartyXP(xp);
	}
	
	public static void runExternalScript(String scriptLocation, String function) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		scriptable.executeFunction(function);
	}
	
	public static void runExternalScript(String scriptLocation, String function, Object arg) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		scriptable.executeFunction(function, arg);
	}
	
	public static void runExternalScript(String scriptLocation, String function, Object[] args) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		scriptable.executeFunction(function, args);
	}
	
	public static void runExternalScriptWait(String scriptLocation, String function, float seconds) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		DelayedScriptCallback cb = new DelayedScriptCallback(scriptable, function);
		cb.setDelay(seconds);
		cb.start();
	}
	
	public static void runExternalScriptWait(String scriptLocation, String function, float seconds, Object arg) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		DelayedScriptCallback cb = new DelayedScriptCallback(scriptable, function);
		cb.setDelay(seconds);
		cb.addArgument(arg);
		cb.start();
	}
	
	public static void runExternalScriptWait(String scriptLocation, String function, float seconds, Object[] arg) {
		String script = ResourceManager.getScriptResourceAsString(scriptLocation);
		
		Scriptable scriptable = new Scriptable(script, scriptLocation, false);
		DelayedScriptCallback cb = new DelayedScriptCallback(scriptable, function);
		cb.setDelay(seconds);
		cb.addArguments(arg);
		cb.start();
	}
	
	public static void revealWorldMapLocation(String ref) {
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
	
	public static void activateTransition(String ref) {
		AreaTransition transition = Game.curCampaign.getAreaTransition(ref);
		
		if (transition == null) {
			Logger.appendToErrorLog("Error activating area transition.  " + ref + " not found.");
		} else {
			transition.activate();
		}
	}
	
	public static CircleTargeter createCircleTargeter(Creature parent, Scriptable scriptable) {
		return new CircleTargeter(parent, scriptable, null);
	}
	
	public static ListTargeter createListTargeter(Creature parent, Scriptable scriptable) {
		return new ListTargeter(parent, scriptable, null);
	}
	
	public static LineTargeter createLineTargeter(Creature parent, Scriptable scriptable) {
		return new LineTargeter(parent, scriptable, null);
	}
	
	public static ConeTargeter createConeTargeter(Creature parent, Scriptable scriptable) {
		return new ConeTargeter(parent, scriptable, null);
	}
	
	public static CircleTargeter createCircleTargeter(AbilitySlot slot) {
		return new CircleTargeter(slot.getParent(), slot.getAbility(), slot);
	}
	
	public static ListTargeter createListTargeter(AbilitySlot slot) {
		return new ListTargeter(slot.getParent(), slot.getAbility(), slot);
	}
	
	public static LineTargeter createLineTargeter(AbilitySlot slot) {
		return new LineTargeter(slot.getParent(), slot.getAbility(), slot);
	}
	
	public static ConeTargeter createConeTargeter(AbilitySlot slot) {
		return new ConeTargeter(slot.getParent(), slot.getAbility(), slot);
	}
	
	public static AbilityActivateCallback createButtonCallback(AbilitySlot slot, String function) {
		AbilityActivateCallback callback = new AbilityActivateCallback(slot, function);
		return callback;
	}
	
	public static void addMenuButton(String label, AbilityActivateCallback callback) {
		Button button = new Button(label);
		button.addCallback(callback);
		
		Game.mainViewer.getMenu().addButton(button);
	}
	
	public static void showMenu() {
		Game.mainViewer.getMenu().show();
	}
	
	public static void addMenuLevel(String title) {
		Game.mainViewer.getMenu().addMenuLevel(title);
	}
	
	public static void hideOpenWindows() {
		Game.mainViewer.closeAllWindows();
	}
	
	public static void startConversation(Entity parent, Entity target, String convoScriptID) {
		ConversationPopup popup = new ConversationPopup(parent, target, convoScriptID);
		popup.startConversation();
	}
	
	public static void showCutscene(String id) {
		Cutscene cutscene = Game.ruleset.getCutscene(id);
		if (cutscene == null) {
			Logger.appendToWarningLog("Cutscene " + id + " not found");
			return;
		}
		
		CutscenePopup popup = new CutscenePopup(Game.mainViewer, cutscene);
		Game.mainViewer.showPopup(popup);
	}
	
	public static HTMLPopup createHTMLPopup(String resource) {
		try {
			HTMLPopup popup = new HTMLPopup(resource);
		
			return popup;
		} catch (Exception e) {
			Logger.appendToErrorLog("Error creating HTML popup", e);
		}
		
		return null;
	}
	
	public static int distance(Point a, Point b) {
		if (a == null || b == null) return 0;
		
		return 5 * AreaUtil.distance(a, b);
	}
	
	public static int distance(Creature a, Creature b) {
		if (a == null || b == null) return 0;
		
		return 5 * AreaUtil.distance(a.getX(), a.getY(), b.getX(), b.getY());
	}
	
	public static EntityManager entities() { return Game.entityManager; }
	
	public static Campaign campaign() { return Game.curCampaign; }
	
	public static Ruleset ruleset() { return Game.ruleset; }
	public static Area currentArea() { return Game.curCampaign.curArea; }
	
	public static Area getArea(String id) { return Game.curCampaign.getArea(id); }
	
	public static Dice dice() { return Game.dice; }
	
	public static Date date() { return Game.curCampaign.getDate(); }
	
	public static boolean isInCombatMode() { return Game.isInTurnMode(); }
	
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
	
	public ParticleGenerator createParticleGenerator(String type, String mode, String particle, float numParticles) {
		if (type.equalsIgnoreCase("Point")) {
			return new ParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else if (type.equalsIgnoreCase("Line")) {
			return new LineParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else if (type.equalsIgnoreCase("Rect")) {
			return new RectParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else if (type.equalsIgnoreCase("Circle")) {
			return new CircleParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else {
			return null;
		}
	}
	
	public Animation createAnimation(String baseFrame) {
		return new Animation(baseFrame);
	}
	
	public Animation createAnimation(String baseFrame, float duration) {
		try {
			return new Animation(baseFrame, duration);
		} catch (Exception e) {
			Logger.appendToErrorLog("Error creating animation.", e);
		}
		
		return null;
	}
	
	public Animation getBaseAnimation(String id) {
		return Game.particleManager.getAnimation(id);
	}
	
	public ParticleGenerator getBaseParticleGenerator(String id) {
		return Game.particleManager.getParticleGenerator(id);
	}
	
	public void runAnimationWait(Animation animation) {
		Game.particleManager.add(animation);
		
		try {
			Thread.sleep((long) (animation.getSecondsRemaining() * 1000.0f));
		} catch (InterruptedException e) {
			return;
		}
	}
	
	public void runAnimationNoWait(Animation animation) {
		Game.particleManager.add(animation);
	}
	
	public void runParticleGeneratorWait(ParticleGenerator generator) {
		Game.particleManager.add(generator);
		
		try {
			Thread.sleep((long) (generator.getTimeLeft() * 1000.0f));
		} catch (InterruptedException e) {
			return;
		}
	}
	
	public void runParticleGeneratorNoWait(ParticleGenerator generator) {
		Game.particleManager.add(generator);
	}
	
	public EquallySpacedAngleDistribution getEquallySpacedAngleDistribution(float min, float max, float stepSize, float numParticles, float jitter) {
		return new EquallySpacedAngleDistribution(min, max, stepSize, numParticles, jitter);
	}
	
	public FixedDistribution getFixedDistribution(float value) {
		return new FixedDistribution(value);
	}
	
	public GaussianDistribution getGaussianDistribution(float mean, float stddev) {
		return new GaussianDistribution(mean, stddev);
	}
	
	public GaussianDistributionWithBase getGaussianDistributionWithBase(DistributionBase base, float mult, float offset, float stddevFraction) {
		return new GaussianDistributionWithBase(base, mult, offset, stddevFraction);
	}
	
	public DistanceDistributionBase getDistanceDistributionBase(Point screenPoint) {
		return new DistanceDistributionBase(screenPoint);
	}
	
	public SpeedDistributionBase getSpeedDistributionBase() {
		return new SpeedDistributionBase();
	}
	
	public AngleDistributionBase getAngleDistributionBase() {
		return new AngleDistributionBase();
	}
	
	public VelocityTowardsPointDistribution getVelocityTowardsPointDistribution(Point dest, float time) {
		return new VelocityTowardsPointDistribution(dest, time);
	}
	
	public UniformAngleDistribution getUniformAngleDistribution(float min, float max) {
		return new UniformAngleDistribution(min, max);
	}
	
	public GaussianAngleDistribution getGaussianAngleDistribution(float mean, float stddev) {
		return new GaussianAngleDistribution(mean, stddev);
	}
	
	public UniformArcDistribution getUniformArcDistribution(float magMin, float magMax, float angleMin, float angleMax) {
		return new UniformArcDistribution(magMin, magMax, angleMin, angleMax);
	}
	
	public FixedAngleDistribution getFixedAngleDistribution(float magMin, float magMax, float angle) {
		return new FixedAngleDistribution(magMin, magMax, angle);
	}
	
	public UniformDistribution getUniformDistribution(float min, float max) {
		return new UniformDistribution(min, max);
	}
	
	public UniformDistributionWithBase getUniformDistributionWithBase(DistributionBase base, float mult, float offset, float plusOrMinusFraction) {
		return new UniformDistributionWithBase(base, mult, offset, plusOrMinusFraction);
	}
	
	public FixedDistributionWithBase getFixedDistributionWithBase(DistributionBase base, float mult, float offset) {
		return new FixedDistributionWithBase(base, mult, offset);
	}
	
	public static SpellScrollCaster createScrollCaster(String abilityID) {
		return new SpellScrollCaster(abilityID);
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
}
