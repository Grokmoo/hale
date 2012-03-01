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

package net.sf.hale.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.matthiasmann.twl.Color;

import net.sf.hale.AreaTrigger;
import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.ability.CreatureAbilitySet;
import net.sf.hale.ability.Effect;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.bonus.StatManager;
import net.sf.hale.editor.reference.CreatureReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.quickbar.Quickbar;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.rules.Attack;
import net.sf.hale.rules.Damage;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.Faction;
import net.sf.hale.rules.LootList;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Role;
import net.sf.hale.rules.RoleSet;
import net.sf.hale.rules.RoundTimer;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.rules.Skill;
import net.sf.hale.rules.SkillSet;
import net.sf.hale.rules.SubIcon;
import net.sf.hale.rules.Ruleset.Gender;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONObject;

public class Creature extends Entity implements Referenceable, AbilityActivator {
	private Ruleset.Gender gender = null;
	private String race = null;
	private String name = null;
	private String portrait = null;
	
	private Scriptable aiScriptManager;
	private int minCurrencyReward = 0;
	private int maxCurrencyReward = 0;
	private Set<AbilityActivator> moveAoOsThisRound;
	private int attacksOfOpportunityThisRound = 0;
	private RoundTimer roundTimer;
	private boolean alreadySearchedForHiddenCreatures = false;
	private boolean isCurrentlyMoving = false;
	private boolean pregenerated;
	private boolean aiActive = false;
	private StatManager statManager;
	
	private int experiencePoints = 0;
	private int currentHP = 1;
	private int temporaryHP = 0;
	private boolean dying = false;
	private boolean dead = false;
	private boolean summoned = false;
	private int summonedDuration = 0;
	private int summonedRoundNumber = 0;
	private boolean immortal = false;
	private int unspentSkillPoints = 0;
	private Encounter aiEncounter;
	private LootList loot;
	private Inventory inventory;
	private SkillSet skillset;
	private RoleSet roleSet;
	private CreatureAbilitySet abilities;
	private Quickbar quickbar;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = super.save();
		
		if (Game.curCampaign.party.isPCPartyMember(this)) {
			data.put("isPCPartyMember", true);
			data.put("name", name);
			data.put("gender", gender.toString());
			data.put("race", race);
			data.put("portrait", portrait);
			
			if (this.drawOnlyHandSubIcons) {
				data.put("drawOnlyHandSubIcons", true);
			}
			
			if (subIconList != null) {
				if (subIconList.getHairIcon() != null) {
					data.put("hairIcon", subIconList.getHairIcon());
					data.put( "hairColor", '#' + Integer.toHexString(subIconList.getHairColor().toARGB()) );
				}
				
				if (subIconList.getBeardIcon() != null) {
					data.put("beardIcon", subIconList.getBeardIcon());
					data.put( "beardColor", '#' + Integer.toHexString(subIconList.getBeardColor().toARGB()) );
				}
				
				data.put( "skinColor", '#' + Integer.toHexString(subIconList.getSkinColor().toARGB()) );
				data.put( "clothingColor", '#' + Integer.toHexString(subIconList.getClothingColor().toARGB()) );

			}
			
			if (subIconList == null || drawOnlyHandSubIcons) {
				data.put("icon", getIcon());
				data.put( "iconColor", '#' + Integer.toHexString(getIconColor().toARGB()) );
			}
			
			data.put("strength", statManager.get(Stat.BaseStr));
			data.put("dexterity", statManager.get(Stat.BaseDex));
			data.put("constitution", statManager.get(Stat.BaseCon));
			data.put("intelligence", statManager.get(Stat.BaseInt));
			data.put("wisdom", statManager.get(Stat.BaseWis));
			data.put("charisma", statManager.get(Stat.BaseCha));
		}
		
		data.put("xp", experiencePoints);
		data.put("currentHP", currentHP);
		
		// if the encounter is not set, then the creature faction must be set directly
		if (this.getEncounter() == null)
			data.put("faction", this.getFaction().getName());
		
		if (temporaryHP > 0) data.put("temporaryHP", temporaryHP);
		
		if (dying) data.put("dying", true);
		if (dead) data.put("dead", true);
		
		if (summoned) {
			data.put("summoned", true);
			data.put("summonedDuration", summonedDuration);
			data.put("summonedRoundNumber", summonedRoundNumber);
		}
		
		if (immortal) data.put("immortal", true);
		
		data.put("unspentSkillPoints", unspentSkillPoints);
		
		if (loot.alreadyGenerated())
			data.put("lootAlreadyGenerated", true);
		
		JSONOrderedObject invData = inventory.save();
		if (!invData.isEmpty())
			data.put("inventory", invData);
		
		JSONOrderedObject skillData = skillset.save();
		if (!skillData.isEmpty())
			data.put("skillSet", skillData);
		
		data.put("roleSet", roleSet.save());
		
		JSONOrderedObject abilityData = abilities.save();
		if (!abilityData.isEmpty())
			data.put("abilitySet", abilityData);
		
		if (quickbar != null)
			data.put("quickbar", quickbar.save());
		
		return data;
	}
	
	@Override public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		super.load(data, refHandler);
		
		this.experiencePoints = data.get("xp", 0);
		this.currentHP = data.get("currentHP", 0);
		
		if (data.containsKey("faction"))
			this.setFaction(data.get("faction", null));
		
		if (data.containsKey("temporaryHP"))
			this.temporaryHP = data.get("temporaryHP", 0);
		
		if (data.containsKey("dying"))
			this.dying = data.get("dying", false);
		
		if (data.containsKey("dead"))
			this.dead = data.get("dead", false);
		
		if (data.containsKey("summoned")) {
			this.summoned = data.get("summoned", false);
			this.summonedDuration = data.get("summonedDuration", 0);
			this.summonedRoundNumber = data.get("summonedRoundNumber", 0);
			this.aiActive = summoned;
		}
		
		if (data.containsKey("immortal"))
			this.immortal = data.get("immortal", false);
		
		this.unspentSkillPoints = data.get("unspentSkillPoints", 0);
		
		if (data.containsKey("lootAlreadyGenerated"))
			this.loot.setAlreadyGenerated(data.get("lootAlreadyGenerated", false));
		
		if (data.containsKey("skillSet"))
			skillset.load(data.getObject("skillSet"));
		else
			skillset.clear();
		
		// clear pre-loaded abilities
		abilities.clear();
		Game.ruleset.getRace(race).addAbilitiesToCreature(this);
		
		// add the roles which will also add role abilities
		roleSet.load(data.getObject("roleSet"), this);
		
		if (data.containsKey("abilitySet"))
			abilities.load(data.getObject("abilitySet"), refHandler);
		
		if (data.containsKey("inventory"))
			inventory.load(data.getObject("inventory"), refHandler);
		else
			inventory.clear();
		
		// quickbar needs to be loaded last since it reference inventory and abilities
		if (data.containsKey("quickbar"))
			quickbar.load(data.getObject("quickbar"));
		
		this.reloadAllSubIcons();
		this.stats().recomputeAllStats();
		
		if (isPlayerSelectable() || summoned) {
			roundTimer.reset();
		}
	}
	
	public Creature(String id, String portrait, String icon, String name, Ruleset.Gender gender, Race race, String faction,
					boolean playerSelectable, Point position, String description) {
		super(id, icon, position, description, faction);
		this.type = Entity.Type.CREATURE;
		this.name = name;
		this.gender = gender;
		this.portrait = portrait;
		this.setPlayerSelectable(playerSelectable);
		this.inventory = new Inventory(this);
		this.skillset = new SkillSet();
		this.roleSet = new RoleSet(this);
		this.roundTimer = new RoundTimer(this);
		
		this.statManager = new StatManager(this);
		
		this.loot = new LootList();
		
		this.abilities = new CreatureAbilitySet(this);
		
		if (playerSelectable) {
			quickbar = new Quickbar(this);
		}
		
		setRace(race);
		
		moveAoOsThisRound = new HashSet<AbilityActivator>();
	}
	
	public Creature(String id, String name) {
		super(id);
		this.type = Entity.Type.ENTITY;
		this.name = name;
	}
	
	public Creature(Creature other) { 
		super(other);
		
		this.type = Entity.Type.CREATURE;
		this.name = other.name;
		this.gender = other.gender;
		this.race = other.race;
		this.portrait = other.portrait;
		this.experiencePoints = other.experiencePoints;
		this.currentHP = other.currentHP;
		this.temporaryHP = other.temporaryHP;
		this.dying = other.dying;
		this.dead = other.dead;
		
		this.statManager = new StatManager(other.statManager, this);
		
		this.roundTimer = new RoundTimer(other.roundTimer, this);
		
		this.inventory = new Inventory(other.getInventory(), this);
		this.skillset = new SkillSet(other.skillset);
		this.roleSet = new RoleSet(other.roleSet, this);
		
		this.minCurrencyReward = other.minCurrencyReward;
		this.maxCurrencyReward = other.maxCurrencyReward;
		
		this.loot = new LootList(other.loot);
		
		this.immortal = other.immortal;
		
		this.unspentSkillPoints = other.unspentSkillPoints;
		
		this.abilities = new CreatureAbilitySet(other.abilities, this);
		
		if (other.aiScriptManager != null)
			this.aiScriptManager = new Scriptable(other.aiScriptManager);

		if (other.quickbar != null)
			this.quickbar = new Quickbar(other.quickbar, this);
		
		moveAoOsThisRound = new HashSet<AbilityActivator>();
		
		this.pregenerated = other.pregenerated;
	}
	
	public boolean isPregenerated() { return pregenerated; }
	
	public void setPregenerated(boolean pregenerated) { this.pregenerated = pregenerated; }
	
	@Override public RoundTimer getTimer() { return roundTimer; }
	
	public Quickbar getQuickbar() { return quickbar; }
	
	public void setAIScript(String scriptLocation) {
		if (scriptLocation == null) {
			this.aiScriptManager = null;
		} else {

			String script = ResourceManager.getScriptResourceAsString(scriptLocation);

			this.aiScriptManager = new Scriptable(script, scriptLocation, false);
		}
	}
	
	public String getAIScript() {
		return aiScriptManager.getScriptLocation();
	}
	
	public boolean hasAI() {
		return aiScriptManager != null;
	}
	
	public Scriptable getAI() {
		return aiScriptManager;
	}
	
	public int getUnspentSkillPoints() { return unspentSkillPoints; }
	public void setUnspentSkillPoints(int points) { this.unspentSkillPoints = points; }
	
	public boolean isImmortal() { return immortal; }
	public void setImmortal(boolean immortal) { this.immortal = immortal; }
	
	public Color getHairColor() {
		if (subIconList == null) return Color.WHITE;
		
		return subIconList.getHairColor();
	}
	
	public String getHairIcon() {
		if (subIconList == null) return null;
		
		return subIconList.getHairIcon();
	}
	
	public void setBeardSubIcon(String icon, Color color) {
		if (subIconList == null) return;
		
		this.subIconList.remove(SubIcon.Type.Beard);
		
		SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Beard, getRace(), gender);
		factory.setPrimaryIcon(icon, color);
		this.subIconList.add(factory.createSubIcon());
	}
	
	public void setHairSubIcon(String hairSubIcon, Color color) {
		if (subIconList == null) return;
		
		this.subIconList.remove(SubIcon.Type.Hair);
		
		SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Hair, getRace(), gender);
		factory.setPrimaryIcon(hairSubIcon, color);
		this.subIconList.add(factory.createSubIcon());
	}
	
	public void reloadAllSubIcons() {
		if (subIconList == null) return;

		String hairIcon = subIconList.getHairIcon();
		Color hairColor = subIconList.getHairColor();

		String beardIcon = subIconList.getBeardIcon();
		Color beardColor = subIconList.getBeardColor();

		subIconList.clear();

		if (!drawOnlyHandSubIcons())
			addBaseSubIcons();

		setHairSubIcon(hairIcon, hairColor);
		setBeardSubIcon(beardIcon, beardColor);

		inventory.addAllSubIcons();
	}
	
	public void clearSubIcons() {
		if (this.subIconList == null) return;
		
		this.subIconList.clear();
	}
	
	public String getSubIcon(String type) {
		return subIconList.getIcon(type);
	}
	
	public Color getSubIconColor(String type) {
		return subIconList.getColor(type);
	}
	
	public String getSubIcon(SubIcon.Type type) {
		return subIconList.getIcon(type);
	}
	
	public Color getSubIconColor(SubIcon.Type type) {
		return subIconList.getColor(type);
	}
	
	public void addBaseSubIcons() {
		if (this.subIconList == null) return;
		if (this.race == null || this.gender == null) return;
		
		Race race = this.getRace();
		
		subIconList.remove(SubIcon.Type.BaseBackground);
		subIconList.remove(SubIcon.Type.BaseForeground);
		subIconList.remove(SubIcon.Type.Ears);
		
		switch (this.gender){
		case Male:
			if (race.getMaleBackgroundIcon() != null) {
				SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseBackground, race, gender);
				factory.setPrimaryIcon(race.getMaleBackgroundIcon(), subIconList.getSkinColor());
				factory.setSecondaryIcon(race.getMaleClothesIcon(), subIconList.getClothingColor());
				subIconList.add(factory.createSubIcon());
			}
			
			if (race.getMaleForegroundIcon() != null) {
				SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseForeground, race, gender);
				factory.setPrimaryIcon(race.getMaleForegroundIcon(), subIconList.getSkinColor());
				subIconList.add(factory.createSubIcon());
			}
			
			if (race.getMaleEarsIcon() != null) {
				SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Ears, race, gender);
				factory.setPrimaryIcon(race.getMaleEarsIcon(), subIconList.getSkinColor());
				subIconList.add(factory.createSubIcon());
			}
			break;
		case Female:
			if (race.getFemaleBackgroundIcon() != null) {
				SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseBackground, race, gender);
				factory.setPrimaryIcon(race.getFemaleBackgroundIcon(), subIconList.getSkinColor());
				factory.setSecondaryIcon(race.getFemaleClothesIcon(), subIconList.getClothingColor());
				subIconList.add(factory.createSubIcon());
			}
			
			if (race.getFemaleForegroundIcon() != null) {
				SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.BaseForeground, race, gender);
				factory.setPrimaryIcon(race.getFemaleForegroundIcon(), subIconList.getSkinColor());
				subIconList.add(factory.createSubIcon());
			}
			
			if (race.getFemaleEarsIcon() != null) {
				SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Ears, race, gender);
				factory.setPrimaryIcon(race.getFemaleEarsIcon(), subIconList.getSkinColor());
				subIconList.add(factory.createSubIcon());
			}
			break;
		}
	}
	
	public boolean isCurrentlyMoving() { return isCurrentlyMoving; }
	public void setCurrentlyMoving(boolean currentlyMoving) { this.isCurrentlyMoving = currentlyMoving; }
	
	public LootList getLoot() { return loot; }
	
	public Encounter getEncounter() { return this.aiEncounter; }
	public void setEncounter(Encounter encounter) { this.aiEncounter = encounter; }
	
	public void setAIActive(boolean active) {
		this.aiActive = active;
	}
	public boolean isAIActive() {
		return this.aiActive;
	}
	
	@Override public void resetAll() {
		super.resetAll();
		
		elapseRounds(1);
		
		if (!this.isDead()) {
			this.temporaryHP = 0;
			this.currentHP = this.statManager.get(Stat.MaxHP);
		}
	}
	
	@Override public void newEncounter() {
		super.newEncounter();
		
		roundTimer.reset();
		
		if (!this.isDead()) {
			if (this.currentHP < this.statManager.get(Stat.MaxHP)) {
				this.healDamage(1 + this.statManager.get(Stat.MaxHP) / Game.ruleset.getValue("OutsideCombatHealingFactor"));
			}
		}
	}
	
	@Override public boolean elapseRounds(int rounds) {
		super.elapseRounds(rounds);
		
		moveAoOsThisRound.clear();
		
		inventory.elapseRounds(rounds);
		
		roundTimer.reset();
		
		attacksOfOpportunityThisRound = 0;
		
		if (dying && Game.isInTurnMode()) {
			takeDamage(rounds, null, false);
			Game.mainViewer.addMessage("red", getName() + " is dying with " + getCurrentHP() + " HP.");
		}
		
		if (summoned && summonedRoundNumber + summonedDuration <= Game.curCampaign.getDate().getTotalRoundsElapsed())
			kill();
		
		alreadySearchedForHiddenCreatures = false;
		
		boolean returnValue = abilities.elapseRounds(rounds);
		
		updateViewers();
		
		return returnValue;
	}
	
	public boolean moveAoOTakenThisRound(AbilityActivator creature) {
		return moveAoOsThisRound.contains(creature);
	}
	
	public void takeMoveAoO(Creature creature) {
		this.moveAoOsThisRound.add(creature);
	}
	
	public void takeAttackOfOpportunity() { attacksOfOpportunityThisRound++; }
	
	public boolean hasAttackOfOpportunityAvailable() {
		return (attacksOfOpportunityThisRound < stats().getAttacksOfOpportunity());
	}
	
	public Attack attack(Creature defender, int itemSlot) {
		return new Attack(this, defender, itemSlot);
	}
	
	public Attack mainHandAttack(Creature defender) {
		roundTimer.performAttack();
		
		return attack(defender, Inventory.EQUIPPED_MAIN_HAND);
	}
	
	public Attack offHandAttack(Creature defender) {
		//roundTimer.performAttack();
		
		return attack(defender, Inventory.EQUIPPED_OFF_HAND);
	}
	
	public boolean threatensPosition(int x, int y) {
		if (this.isHelpless()) return false;
		
		Item weapon = inventory.getEquippedItem(Inventory.EQUIPPED_MAIN_HAND);
		if (weapon == null) weapon = getRace().getDefaultWeapon();
		
		if (!this.getVisibility()[x][y]) return false;
		
		if (!weapon.threatens()) return false;
		
		// can only attack on same elevation with melee weapons
		if (weapon.isMeleeWeapon()) {
			byte curElev = Game.curCampaign.curArea.getElevationGrid().getElevation(this.getX(), this.getY());
			byte targetElev = Game.curCampaign.curArea.getElevationGrid().getElevation(x, y);
			if (curElev != targetElev) return false;
		}
		
		int dist = AreaUtil.distance(this.getX(), this.getY(), x, y);
		if (dist > weapon.getThreatenMax() || dist < weapon.getThreatenMin()) return false;
		
		return true;
	}
	
	public boolean canAttackPosition(int x, int y) {
		if (this.isHelpless()) return false;
		
		Item weapon = inventory.getEquippedItem(Inventory.EQUIPPED_MAIN_HAND);
		if (weapon == null) weapon = getRace().getDefaultWeapon();
		
		if (!this.getVisibility(x, y)) return false;
		
		int dist = AreaUtil.distance(this.getX(), this.getY(), x, y);
		
		if (weapon.getWeaponType() == Item.WeaponType.MELEE) {
			if (dist > weapon.getThreatenMax() || dist < weapon.getThreatenMin()) return false;
			
			// can only attack on same elevation with a melee weapon
			byte curElev = Game.curCampaign.curArea.getElevationGrid().getElevation(this.getX(), this.getY());
			byte targetElev = Game.curCampaign.curArea.getElevationGrid().getElevation(x, y);
			if (curElev != targetElev) return false;
			
		} else if (weapon.getWeaponType() == Item.WeaponType.THROWN) {
			if (dist > weapon.getMaximumRange()) return false;
		}
		else {
			if (dist > weapon.getMaximumRange()) return false;
			
			Item quiver = inventory.getEquippedItem(Inventory.EQUIPPED_QUIVER);
			
			if (quiver == null || quiver.getWeaponType() != weapon.getWeaponType()) return false;
		}
		
		return true;
	}
	
	public void raiseFromDead() {
		if (!this.dead) return;
		
		this.dead = false;
		this.dying = false;
		
		currentHP = 1;
		temporaryHP = 0;
		
		int xpPenalty = this.stats().get(Stat.CreatureLevel) *
			Game.ruleset.getValue("DeathXPPenaltyCharacterLevelFactor");
		
		int currentXP = this.getExperiencePoints();
		currentXP = Math.max(0, currentXP - xpPenalty);
		
		this.setExperiencePoints(currentXP);
		
		Game.mainViewer.updateEntity(this);
	}
	
	public void removeTemporaryHP(int hp) {
		if (temporaryHP > hp) takeDamage(hp, "Effect");
		else if (temporaryHP > 0) takeDamage(temporaryHP, "Effect");
	}
	
	public void addTemporaryHP(int hp) {
		temporaryHP += hp;
		
		if (getCurrentHP() > 0) dying = false;
		
		if (Game.mainViewer != null) {
			Game.mainViewer.addFadeAway("" + hp, this.getX(), this.getY(), "blue");
			Game.mainViewer.updateEntity(this);
		}
	}
	
	public void healDamage(int damage) {
		healDamage(damage, true);
	}
	
	public void healDamage(int damage, boolean message) {
		currentHP += damage;
		
		if (currentHP > this.stats().get(Stat.MaxHP)) currentHP = this.stats().get(Stat.MaxHP);
		if (getCurrentHP() > 0) dying = false;
		
		if (Game.mainViewer != null) {
			Game.mainViewer.addFadeAway("" + damage, this.getX(), this.getY(), "blue");
			if (message) {
				Game.mainViewer.addMessage("blue", getName() + " is healed by " + damage + " hit points.");
			}
			Game.mainViewer.updateEntity(this);
		}
	}
	
	private void takeDamage(Damage damage, boolean message) {
		// dead creatures can't take damage
		if (this.isDead()) return;
		
		getEffects().executeOnAll(ScriptFunctionType.onDamaged, damage);
		
		int damageLeftToApply = damage.getAppliedDamage();
		
		if (immortal) damageLeftToApply = 0;
		
		if (temporaryHP > damageLeftToApply) {
			temporaryHP -= damageLeftToApply;
			damageLeftToApply = 0;
		} else {
			damageLeftToApply -= temporaryHP;
			temporaryHP = 0;
		}
		
		currentHP -= damageLeftToApply;
		
		if (message && Game.mainViewer != null)
			Game.mainViewer.addFadeAway("" + damage.getAppliedDamage(), this.getX(), this.getY(), "red");
		
		if (getCurrentHP() <= 0 && getCurrentHP() > -20) {
			dying = true;
			if (summoned || !isPlayerSelectable()) kill();
			else if (message && Game.mainViewer != null)
				Game.mainViewer.addMessage("red", getName() + " is dying with " + getCurrentHP() + " HP.");
		} else if (getCurrentHP() < -19) {
			kill();
		}
		
		if (Game.mainViewer != null) {
			Game.mainViewer.updateEntity(this);
			if (message)
				Game.mainViewer.updateInterface();
		}
	}
	
	public int takeDamageFromAttack(Attack attack, boolean message) {
		// dead creatures can't take damage
		if (this.isDead()) return 0;
		
		int appliedDamage = attack.computeAppliedDamage(message);
		
		takeDamage(attack.getDamage(), message);
		
		return appliedDamage;
	}
	
	public int takeDamage(int damage, DamageType damageType, boolean message) {
		// dead creatures can't take damage
		if (this.isDead()) return 0;
		
		Damage dam = new Damage(this, damageType, damage);
		dam.computeAppliedDamage(message);
		takeDamage(dam, message);
		return dam.getAppliedDamage();
	}
	
	public int takeDamage(int damage, String type) {
		// dead creatures can't take damage
		if (this.isDead()) return 0;
		
		DamageType damageType = Game.ruleset.getDamageType(type);
		
		Damage dam = new Damage(this, damageType, damage);
		dam.computeAppliedDamage(true);
		
		takeDamage(dam, true);
		
		return dam.getAppliedDamage();
	}
	
	public void kill() {
		if (this.immortal) return; 
		
		if (currentHP > -20) currentHP = -20;
		dying = false;
		dead = true;
		temporaryHP = 0;
		
		getEffects().endAllAnimations();
		getInventory().endAllEffectAnimations();
		
		Game.mainViewer.updateEntity(this);
		
		if (hasAI()) {
			getAI().executeFunction(ScriptFunctionType.onCreatureDeath, this);
		}
	}
	
	public void setSummoned(int duration) {
		this.summoned = true;
		this.summonedDuration = duration;
		this.summonedRoundNumber = Game.curCampaign.getDate().getTotalRoundsElapsed();
	
	}
	public boolean isSummoned() { return summoned; }
	
	public boolean mentalResistanceCheck(int DC) {
		return resistanceCheck("Mental Resistance", stats().getMentalResistance(), DC);
	}
	
	public boolean physicalResistanceCheck(int DC) {
		return resistanceCheck("Physical Resistance", stats().getPhysicalResistance(), DC);
	}
	
	public boolean reflexCheck(int DC) {
		return resistanceCheck("Reflex", stats().getReflexResistance(), DC);
	}
	
	private boolean resistanceCheck(String name, int modifier, int DC) {
		int roll = Game.dice.d100();
		int total = roll + modifier;
		
		String message = getName() + " attempts " + name + " check with difficulty " + DC +
						 ": " + roll + " + " + modifier + " = " + total;
		
		if (DC > total) {
			Game.mainViewer.addMessage("orange", message + ". Failed.");
			return false;
		} else {
			Game.mainViewer.addMessage("orange", message + ". Success.");
			return true;
		}
	}
	
	public int getSkillRanks(String skillID) {
		return skillset.getRanks(skillID);
	}
	
	public final int getSkillModifier(String skillID) {
		return getSkillModifier(Game.ruleset.getSkill(skillID));
	}
	
	public final int getSkillModifier(Skill skill) {
		int ranks = skillset.getRanks(skill);
		int modifier = stats().getSkillBonus(skill.getID()) + (stats().get(skill.getKeyAttribute()) - 10) * 2;
		if (skill.hasArmorPenalty()) modifier -= stats().get(Stat.ArmorPenalty);
		
		return ranks + modifier;
	}
	
	public boolean skillCheck(String skillName, int DC) {
		return skillCheck(skillName, DC, true);
	}
	
	public int getSkillCheck(String skillID) {
		return getSkillCheck(skillID, 0, false);
	}
	
	public int getSkillCheck(String skillID, int DC, boolean showMessage) {
		return getSkillCheck(skillID, DC, Game.dice.d100(), showMessage);
	}
	
	private int getSkillCheck(String skillID, int DC, int roll, boolean showMessage) {
		Skill skill = Game.ruleset.getSkill(skillID);
		int ranks = getSkillSet().getRanks(skill);
		
		// can't use untrained skills unless you have at least 1 rank
		if (!skill.usableUntrained() && ranks < 1) return 0;
		
		int base = getSkillModifier(skill);
		
		int total = roll + base;
		
		if (showMessage) skillCheckMessage(skill, DC, roll, base, total);
		
		return total;
	}
	
	private void skillCheckMessage(Skill skill, int DC, int roll, int base, int total) {
		String message =  getName() + " attempts " + skill.getName() + " check with difficulty " + DC +
			": " + roll + " + " + base + " = " + total;

		if (DC > total) {
			Game.mainViewer.addMessage("orange", message + ". Failed.");
			Game.mainViewer.addFadeAway(skill.getName() + ": Failure", this.getX(), this.getY(), "grey");
		} else {
			Game.mainViewer.addMessage("orange", message + ". Success.");
			Game.mainViewer.addFadeAway(skill.getName() + ": Success", this.getX(), this.getY(), "grey");
		}
	}
	
	public boolean skillCheckRoll100(String skillID, int DC) {
		return skillCheckRoll100(skillID, DC, true);
	}
	
	public boolean skillCheckRoll100(String skillID, int DC, boolean showMessage) {
		int check = getSkillCheck(skillID, DC, 100, showMessage);
		return check >= DC;
	}
	
	public boolean skillCheck(String skillName, int DC, boolean showMessage) {
		int check = getSkillCheck(skillName, DC, showMessage);
		
		return check >= DC;
	}
	
	public void setRace(Race race) {
		// clear all old racial abilities
		abilities.removeRacialAbilities();
		
		if (race == null) return;
		
		this.race = race.getID();
		
		race.addAbilitiesToCreature(this);
	}
	
	public void setGender(Ruleset.Gender gender) {
		this.gender = gender;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
	
	public int getMinCurrencyReward() { return this.minCurrencyReward; }
	public int getMaxCurrencyReward() { return this.maxCurrencyReward; }
	
	public void setMinCurrencyReward(int reward) { this.minCurrencyReward = reward; }
	public void setMaxCurrencyReward(int reward) { this.maxCurrencyReward = reward; }
	
	// used to determine if the creature can move
	@Override public boolean isImmobilized() { return dying || dead || stats().isImmobilized(); }
	
	// used to determine if the creature can perform non-movement actions
	public boolean isHelpless() { return dying || dead || stats().isHelpless(); }
	
	public boolean isDying() { return dying; }
	public boolean isDead() { return dead; }
	
	public void setExperiencePoints(int xp) { this.experiencePoints = xp; }
	public void addExperiencePoints(int xp) { this.experiencePoints += xp; }
	public int getExperiencePoints() { return this.experiencePoints; }
	
	public int getCurrentHP() { return currentHP + temporaryHP; }
	public Inventory getInventory() { return inventory; }
	public SkillSet getSkillSet() { return skillset; }
	public RoleSet getRoles() { return roleSet; }
	
	@Override public int getCasterLevel() { return roleSet.getCasterLevel(); }
	
	public CreatureAbilitySet getAbilities() { return abilities; }
	
	@Override public StatManager stats() { return statManager; }
	
	public Race getRace() {
		if (race == null)
			return Game.ruleset.getRace(Game.ruleset.getString("DefaultRace"));
		else
			return Game.ruleset.getRace(race);
	}
	
	public String getPortrait() { return portrait; }
	
	public String getGenderString() {
		if (gender == null) return Gender.Male.toString();
		else return gender.toString();
	}
	
	public Ruleset.Gender getGender() {
		if (gender == null) return Gender.Male;
		else return gender;
	}
	
	@Override public String getName() { return name; }
	
	@Override public boolean setPosition(int x, int y) {
		Point screenOld = this.getScreenPosition();
		
		super.setPosition(x, y);
		
		// offset animation positions for inventory
		Point screenCur = getScreenPosition();
		for (Item item : inventory.getEquippedItems()) {
			if (item == null) continue;
			
			item.getEffects().offsetAnimationPositions(screenCur.x - screenOld.x, screenCur.y - screenOld.y);
		}
		
		if (Game.curCampaign.curArea == null) return true;
		
		// TODO handle exiting and entering from area transitions (oldEffects will not be valid)
		
		List<Effect> oldEffects = Game.curCampaign.curArea.getEffectsAt(getLastPositionX(), getLastPositionY());
		//this.moveAurasToCaster();
		List<Effect> newEffects = Game.curCampaign.curArea.getEffectsAt(getX(), getY());
		
		for (Effect effect : oldEffects) {
			// dont run onExit scripts if this creature is the direct target (i.e. if the effect is an aura)
			if (effect.getTarget() != this && !newEffects.contains(effect)) {
				effect.executeFunction(ScriptFunctionType.onTargetExit, effect, this);
			}
		}
		
		for (Effect effect : newEffects) {
			// dont run onEnter scripts if this creature is the direct target (i.e. if the effect is an aura)
			if (effect.getTarget() != this && !oldEffects.contains(effect)) {
				effect.executeFunction(ScriptFunctionType.onTargetEnter, effect, this);
			}
		}
		
		boolean interrupted = false;
		
		Trap trap = Game.curCampaign.curArea.getTrapAtGridPoint(this.getX(), this.getY());
		if (trap != null) {
			if (trap.springTrap(this)) interrupted = true;
		}
		
		if (this.isPlayerSelectable()) {
			
			for (String s : Game.curCampaign.curArea.getTriggers()) {
				AreaTrigger t = Game.curCampaign.getTrigger(s);

				t.checkPlayerMoved(this);
			}
			
			boolean[][] vis = this.getVisibility();
			
			if (vis == null) return false;
			
			for (Entity e : Game.curCampaign.curArea.getEntities()) {
				if (e.getX() < 0 || e.getY() < 0 || e.getX() >= vis.length || e.getY() >= vis[0].length) continue;
				if (!vis[e.getX()][e.getY()]) continue;
				if (e.getType() != Entity.Type.TRAP) continue;
				
				trap = (Trap)e;
				
				if (trap.trySearch(this)) {
					interrupted = true;
				}
			}
			
		}
		
		return interrupted;
	}
	
	public void searchForHidingCreatures() {
		// we only allow searching once per round per creature
		// this in theory is reset every round.  Currently, you can shuffle round timer orders
		// using CombatRunner.activateCreatureWait and get around this
		if (alreadySearchedForHiddenCreatures) return;
		
		alreadySearchedForHiddenCreatures = true;
		
		for (Creature c : Game.curCampaign.curArea.getEntities().getVisibleCreatures(visibility)) {
			if (!c.stats().isHidden()) continue;
			
			if (c.getFaction().getRelationship(this) != Faction.Relationship.Hostile) continue;
			
			doSearchCheck(c, 0);
		}
	}
	
	public boolean doSearchCheck(Creature target, int penalty) {
		int baseDifficulty = Game.ruleset.getValue("SearchCheckCreatureBaseDifficulty");
		int distanceMultiplier = Game.ruleset.getValue("SearchCheckDistanceMultiplier");
		
		int checkPenalty = distanceMultiplier * AreaUtil.distance(this.getX(), this.getY(), target.getX(), target.getY()) + baseDifficulty;
		int concealment = Math.min(100, Game.curCampaign.curArea.getConcealment(this, target));
		
		if (skillCheck("Search", target.getSkillModifier("Hide") + checkPenalty + concealment - penalty, true)) {
			// deactivate hide mode
			for (AbilitySlot slot : target.abilities.getSlotsWithReadiedAbility("Hide")) {
				slot.deactivate();
			}
			
			Game.areaListener.getCombatRunner().checkAIActivation();
			return true;
		} else {
			return false;
		}
	}
	
	@Override protected void applyEffectBonuses(Effect effect) {
		statManager.addAll(effect.getBonuses());
	}
	
	@Override protected void removeEffectBonuses(Effect effect) {
		statManager.removeAll(effect.getBonuses());
	}
	
	@Override public String toString() {
		return getID();
	}
	
	@Override public String getReferenceType() {
		return "Creature";
	}
	
	@Override public ReferenceList getReferenceList() {
		return new CreatureReferenceList(this);
	}
	
	@Override public String getFullName() {
		return name;
	}
	
	@Override public int getBaseSpellFailure(int spellLevel) {
		Role role = roleSet.getBaseRole();
		
		int abilityScore = statManager.get(role.getSpellCastingAttribute());
		int casterLevel = getCasterLevel();
		
		int failure = role.getSpellFailureBase() + role.getSpellFailureSpellLevelFactor() * spellLevel;
		failure -= (abilityScore - 10) * role.getSpellFailureAbilityScoreFactor();
		failure -= casterLevel * role.getSpellFailureCasterLevelFactor();
		failure -= statManager.get(Bonus.Type.SpellFailure);
		
		return failure;
	}
	
	@Override public int getSpellResistance() {
		return stats().get(Bonus.Type.SpellResistance);
	}
	
	@Override public int getSpellCastingAttribute() {
		return stats().get(roleSet.getBaseRole().getSpellCastingAttribute());
	}

	@Override public boolean isValidEffectTarget() {
		return !this.isDead();
	}
}
