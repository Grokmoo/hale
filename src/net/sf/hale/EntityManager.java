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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.matthiasmann.twl.Color;

import net.sf.hale.ability.Ability;
import net.sf.hale.ability.CreatureAbilitySet;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.CreatedItem;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Openable;
import net.sf.hale.entity.Trap;
import net.sf.hale.entity.Item.WeaponHanded;
import net.sf.hale.entity.Item.WeaponType;
import net.sf.hale.quickbar.Quickbar;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.ArmorType;
import net.sf.hale.rules.BaseWeapon;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.ItemQuality;
import net.sf.hale.rules.LootList;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Role;
import net.sf.hale.rules.RoleSet;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.rules.Size;
import net.sf.hale.rules.SkillSet;
import net.sf.hale.rules.SubIcon;
import net.sf.hale.rules.Weight;
import net.sf.hale.rules.XP;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;

/**
 * Class for storing and retrieving copies of creatures, items, and all item subclasses.
 * Only one instance is available, at Game.entityManager
 * 
 * This class does not manage some other types of Entities, including Talents and Spells, which
 * are managed by {@link net.sf.hale.rules.Ruleset}
 * 
 * @author Jared Stephen
 *
 */

public class EntityManager {
	private final Map<String, Entity> entities;
	
	/**
	 * Creates an empty EntityManager
	 */
	public EntityManager() {
		entities = new HashMap<String, Entity>();
	}
	
	/**
	 * Returns whether or not this EntityManager contains an entity with the specified ID.
	 * @param id the ID of the Entity to look for.
	 * @return true if this manager contains an entity with the specified ID, false otherwise
	 */
	public boolean hasEntity(String id) {
		return entities.containsKey(id);
	}
	
	/**
	 * Removes the entity with the specified ID from this manager.
	 * Future calls to getItem, getCreature, or getCharacter with ref as the argument
	 * will return null.
	 * @param id the ID of the entity to remove
	 */
	
	public void removeEntity(String id) {
		entities.remove(id);
	}
	
	/**
	 * Removes all entities from this EntityManager.  The EntityManager
	 * will be empty after this operation.
	 */
	
	public void clearEntities() { 
		entities.clear();
	}
	
	/**
	 * Determines whether the supplied creature is valid for the current Campaign at
	 * Game.curCampaign.
	 * 
	 * This includes verifying that all races, roles, talents, spells, skills, and icons
	 * are present in the current Campaign / core data files and that the character has a valid
	 * collection of attributes, roles, skills, talents, and spells that could have been obtained
	 * using the current Campaign rules.
	 * @param c the Creature to be verified
	 * @return true if the creature is valid, false otherwise
	 */
	
	public boolean isCharacterValidForCampaign(Creature c) {
		if (c == null) return false;
		
		String portrait = c.getPortrait();
		if (portrait != null && SpriteManager.getPortrait(portrait) == null) return false;
		
		String icon = c.getIcon();
		if (icon != null && SpriteManager.getSprite(icon) == null) return false;
		
		if (c.getRace() == null) return false;
		
		if (c.drawWithSubIcons()) {
			for (SubIcon subIcon : c.getSubIcons()) {
				icon = subIcon.getIcon();
				if (SpriteManager.getSprite(icon) == null) return false;
			}
		}
		
		if (c.getRoles().getTotalLevel() > Role.MAX_LEVELS) return false;
		
		for (String roleID : c.getRoles().getRoleIDs()) {
			if (Game.ruleset.getRole(roleID) == null) return false;
		}
		
		for (String skillID : c.getSkillSet().getSkills()) {
			if (Game.ruleset.getSkill(skillID) == null) return false;
		}
		
		// TODO validate abilities
		
		// no need to validate items as invalid ones are automatically removed
		
		return true;
	}
	
	/**
	 * Returns the Creature with the given reference ID.  If the creature
	 * is currently not stored in the EntityManager, it looks for the creature
	 * in the characters/ directory on disk.
	 * 
	 * @param id the ID of the Creature to get
	 * @return the Creature with the given reference ID, or null if no such Creature is found
	 */
	
	public Creature getCharacter(String id) {
		Entity entity = entities.get(id);

		if (entity == null) {
			File charFile = new File("characters/" + id + ResourceType.Text.getExtension());
			
			if (!charFile.exists()) return getCreature(id);
			
			Creature creature = null;
			try {
				creature = readCreatureFile(id, new FileKeyMap(charFile));
			} catch (Exception ex) {
				Logger.appendToErrorLog("Error reading creature " + id, ex);
			}
				
			if (creature != null) {
				entities.put(id, creature);
				
				Creature copy = new Creature(creature);
				return copy;
			} else
				return null;
		} else {
			if (entity.getType() == Entity.Type.CREATURE) {
				return new Creature((Creature) entity);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Returns the Creature with the given reference ID.  If the creature
	 * is currently not stored in the EntityManager, it looks for the creature
	 * in the creatures/ resource using the ResourceManager.
	 * 
	 * @param id the ID of the Creature to get
	 * @return the Creature with the given reference ID, or null if no such Creature is found
	 */
	
	public Creature getCreature(String id) {
		Entity entity = entities.get(id);

		if (entity == null) {
			Creature creature = null;
			try {
				creature = readCreatureFile(id, new FileKeyMap("creatures/" + id + ResourceType.Text.getExtension()));
			} catch (Exception ex) {
				Logger.appendToErrorLog("Error reading creature " + id, ex);
			}

			if (creature != null) {
				entities.put(id, creature);
				return new Creature(creature);
			} else
				return null;
		} else {
			if (entity.getType() == Entity.Type.CREATURE) return new Creature((Creature) entity);
			else return null;
		}
	}
	
	private Item getItemCopy(Entity entity) {
		if (entity == null) return null;
		
		switch (entity.getType()) {
		case ITEM: return new Item((Item)entity);
		case CONTAINER: return new Container((Container)entity);
		case DOOR: return new Door((Door)entity);
		case TRAP: return new Trap((Trap)entity);
		}
		
		return null;
	}
	
	/**
	 * Returns the Item with the given ID.  If the item is not currently stored
	 * in the EntityManager, it looks for the item in the items/ resource using
	 * the ResourceManager.
	 * 
	 * @param id the ID of the item to be retrieved
	 * @return the Item with the given ID, or null if no Item can be found.
	 */
	
	public Item getItem(String id) {
		// check if the entity is already loaded
		Entity entity = entities.get(id);
		if (entity != null) return getItemCopy(entity);
		
		// try to load the entity from a resource
		try {
			entity = readItemFile(id);
		} catch (Exception e) {
			Logger.appendToErrorLog("Error reading item " + id, e);
		}
			
		if (entity != null) {
			entities.put(id, entity);
			return getItemCopy(entity);
		}

		// try to load the entity as a created item
		CreatedItem createdItem = Game.curCampaign.getCreatedItem(id);
		if (createdItem == null) return null;
		
		Item baseItem = getItem(createdItem.getBaseItemID());
		if (baseItem == null) return null;
		
		// add the enchantments to the created item
		Item item = getItemCopy(baseItem);
		item.setID(createdItem.getCreatedItemID());
		for (String script : createdItem.getScripts()) {
			item.createEnchantment(script);
		}
		
		entities.put(id, item);
		
		return item;
	}
	
	private Item readItemFile(String ref) {
		String resource = "items/" + ref + ResourceType.Text.getExtension();
		if (!ResourceManager.hasResource(resource)) return null;
		
		FileKeyMap keyMap = new FileKeyMap(resource);
		if (!keyMap.resourceIsParsable()) return null;
		
		String name = keyMap.getValue("name", null);
		String icon = null;
		Color iconColor = Color.WHITE;
		String subIcon = null;
		Color subIconColor = Color.WHITE;
		String projectileIcon = null;
		Color projectileColor = Color.WHITE;
		String description = keyMap.getValue("description", null);
		String openIcon = keyMap.getValue("openicon", null);;
		boolean doorTransparent = keyMap.getValue("doortransparent", false);
		Weight weight = new Weight(keyMap.getValue("weight", 0));
		
		String conversationScript = keyMap.getValue("conversation", null);
		Item.ItemType itemType = Item.ItemType.valueOf(keyMap.getValue("itemtype", "ITEM"));
		
		BaseWeapon baseWeapon = Game.ruleset.getBaseWeapon(keyMap.getValue("baseweapon", null));
		WeaponType weaponType = WeaponType.valueOf(keyMap.getValue("weapontype", "MELEE"));
		WeaponHanded weaponHanded = WeaponHanded.valueOf(keyMap.getValue("weaponhanded", WeaponHanded.NONE.toString()));
		DamageType damageType = Game.ruleset.getDamageType(keyMap.getValue("damagetype", null));
		int criticalThreatRange = keyMap.getValue("criticalrange", 0);
		int criticalMultiplier = keyMap.getValue("criticalmultiplier", 0);
		int rangePenalty = keyMap.getValue("rangepenalty", 0);
		int maximumRange = keyMap.getValue("maximumrange", 0);
		int maxStrengthBonus = keyMap.getValue("maxstrengthbonus", 0);
		int attackCost = keyMap.getValue("attackcost", 2800);
		boolean threatens = keyMap.getValue("threatens", true);
		
		boolean container = keyMap.getValue("container", false);
		boolean door = keyMap.getValue("door", false);
		boolean trap = keyMap.getValue("trap", false);
		boolean quest = keyMap.getValue("quest", false);
		boolean ingredient = keyMap.getValue("ingredient", false);
		boolean workbench = keyMap.getValue("workbench", false);
		boolean cursed = keyMap.getValue("cursed", false);
		
		boolean forceNoQuality = keyMap.getValue("forcenoquality", false);
		
		ArmorType armorType = Game.ruleset.getArmorType(keyMap.getValue("armortype", null));
		int armorClass = keyMap.getValue("armorclass", 0);
		int armorPenalty = keyMap.getValue("armorpenalty", 0);
		int movementPenalty = keyMap.getValue("movementpenalty", 0);
		int shieldAttackPenalty = keyMap.getValue("shieldattackpenalty", 0);
		boolean coversBeardIcon = keyMap.getValue("coversbeardicon", false);
		
		String script = keyMap.getValue("script", null);
		String useButtonText = keyMap.getValue("usebuttontext", null);
		int useAPCost = keyMap.getValue("useapcost", 0);
		
		ItemQuality quality = Game.ruleset.getItemQuality(keyMap.getValue("quality", Game.ruleset.getString("DefaultItemQuality")));
		
		int lockDifficulty = keyMap.getValue("lock", -1);
		String key = keyMap.getValue("key", null);
		boolean keyRequired = keyMap.getValue("keyrequired", false);
		boolean removeKeyOnUnlock = keyMap.getValue("removekeyonunlock", false);
		boolean trapActivatesOnlyOnce = keyMap.getValue("trapactivatesonlyonce", false);
		
		int findDiff = -1, placeDiff = -1, disarmDiff = -1, recoverDiff = -1, reflexDiff = -1;
		Currency value = new Currency();
		int valueStackSize = 1;
		Size weaponSize = Size.Medium;
		int minWeaponReach = 1, maxWeaponReach = 1;
		int damageMin = 0, damageMax = 0;
		
		List<LootList.Entry> lootEntries = new ArrayList<LootList.Entry>();
		
		List<String> enchantments = new ArrayList<String>(1);
		
		for (LineKeyList line : keyMap.get("enchantment")) {
			enchantments.add(line.next());
		}
		
		for (LineKeyList line : keyMap.get("addloot")) {
			String itemListID = line.next();
			int probability = line.nextInt();
			LootList.ProbabilityMode mode = LootList.ProbabilityMode.valueOf(line.next().toUpperCase());
			
			lootEntries.add(new LootList.Entry(itemListID, probability, mode));
		}
		
		LineKeyList line = keyMap.getLast("value");
		if (line != null) {
			value.addFromString(line.next());
			if (line.hasNext()) valueStackSize = line.nextInt();
		}
		
		line = keyMap.getLast("icon");
		if (line != null) {
			icon = line.next();
			if (line.hasNext()) {
				iconColor = line.nextColor();
			}
		}
		
		if (openIcon == null)
			openIcon = icon;
		
		line = keyMap.getLast("subicon");
		if (line != null) {
			subIcon = line.next();
			if (line.hasNext()) {
				subIconColor = line.nextColor();
			}
		}
		
		line = keyMap.getLast("projectileicon");
		if (line != null) {
			projectileIcon = line.next();
			if (line.hasNext()) {
				projectileColor = line.nextColor();
			}
		}
		
		line = keyMap.getLast("reach");
		if (line != null) {
			minWeaponReach = line.nextInt();
			maxWeaponReach = line.nextInt();
		}
		
		line = keyMap.getLast("weaponsize");
		if (line != null) {
			String s = line.next().toLowerCase();
			
			if (s.equals("light")) weaponHanded = WeaponHanded.LIGHT;
			else if (s.equals("onehanded")) weaponHanded = WeaponHanded.ONE_HANDED;
			else if (s.equals("twohanded")) weaponHanded = WeaponHanded.TWO_HANDED;
			weaponSize = Size.valueOf(line.next());
		}
		
		line = keyMap.getLast("damage");
		if (line != null) {
			damageMin = line.nextInt();
			damageMax = line.nextInt();
		}
		
		line = keyMap.getLast("trapdifficulties");
		if (line != null) {
			findDiff = line.nextInt();
			placeDiff = line.nextInt();
			disarmDiff = line.nextInt();
			recoverDiff = line.nextInt();
			reflexDiff = line.nextInt();
		}
		
		// done reading file
		
		Item item;
		
		if (container) {
			item = new Container(ref, name, itemType, description, openIcon, icon);
			((Container)item).getLoot().addAll(lootEntries);
			((Container)item).setWorkbench(workbench);
		}
		else if (door) {
			item = new Door(ref, name, icon, openIcon, itemType, description, doorTransparent);
		} else if (trap) {
			Trap newTrap = new Trap(ref, name, icon, description, value);
			newTrap.setFindDifficulty(findDiff);
			newTrap.setPlaceDifficulty(placeDiff);
			newTrap.setDisarmDifficulty(disarmDiff);
			newTrap.setRecoverDifficulty(recoverDiff);
			newTrap.setReflexDifficulty(reflexDiff);
			newTrap.setActivateOnlyOnce(trapActivatesOnlyOnce);
			
			item = newTrap;
		} else
			item = new Item(ref, name, icon, itemType, description, value);
		
		if ((container || door) && lockDifficulty != -1) {
			Openable openable = ((Openable)item);
			
			openable.setLockDifficulty(lockDifficulty);
			openable.setKey(key);
			openable.setKeyRequired(keyRequired);
			openable.setRemoveKeyOnUnlock(removeKeyOnUnlock);
		}
		
		if (subIcon != null) item.setSubIcon(subIcon);
		if (subIconColor != null) item.setSubIconColor(subIconColor);
		
		if (projectileIcon != null) item.setProjectileIcon(projectileIcon);
		if (projectileColor != null) item.setProjectileIconColor(projectileColor);
		
		item.setForceNoQuality(forceNoQuality);
		
		item.setConversationScript(conversationScript);
		
		item.setWeaponProperties(weaponType, baseWeapon, weaponHanded, weaponSize, damageType, damageMin,
									 damageMax, minWeaponReach, maxWeaponReach, criticalThreatRange,
									 criticalMultiplier, rangePenalty, maximumRange, maxStrengthBonus, threatens, attackCost);
		item.setArmorProperties(armorType, armorClass, armorPenalty, movementPenalty, shieldAttackPenalty);
		// set properties after weapon and armor so quality modifiers are computed
		item.setProperties(quality, weight, valueStackSize, quest, ingredient, cursed);
		item.setScript(script);
		item.setUseAPCost(useAPCost);
		item.setUseButtonText(useButtonText);
		item.setIconColor(iconColor);
		item.setCoversBeardIcon(coversBeardIcon);
		item.setEnchantments(enchantments);
		
		keyMap.checkUnusedKeys();
		
		return item;
	}
	
	private Creature readCreatureFile(String id, FileKeyMap keyMap) {
		Creature c;
		String conversation = null;
		String name = null, icon = null, aiscript = null, portrait = null, description = null;
		Color iconColor = Color.WHITE;
		String faction = null;
		boolean playerCharacter = false;
		Ruleset.Gender gender = null;
		Race race = null;
		int[] attributes = {10, 10, 10, 10, 10, 10};
		int xp = -1;
		
		boolean drawWithSubIcons = false;
		String hairIcon = null;
		Color hairColor = Color.WHITE;
		
		String beardIcon = null;
		Color beardColor = Color.WHITE;
		
		Color skinColor = Color.WHITE;
		Color clothingColor = Color.WHITE;
		
		int minCurrencyReward = 0;
		int maxCurrencyReward = 0;
		
		RoleSet roles = new RoleSet(null);
		
		ItemList unequipped = new ItemList(id);
		ItemList equipped = new ItemList(id);
		ItemList offHand = new ItemList(id);
		
		CreatureAbilitySet abilities = new CreatureAbilitySet(null);
		
		SkillSet skills = new SkillSet();
		
		List<LootList.Entry> lootEntries = new ArrayList<LootList.Entry>();
		
		int unspentSkillPoints = keyMap.getValue("unspentskillpoints", 0);
		boolean immortal = keyMap.getValue("immortal", false);
		boolean drawOnlyHandSubIcons = keyMap.getValue("drawonlyhandsubicons", false);
		
		if (keyMap.has("name")) name = keyMap.getLast("name").next();
		
		if (keyMap.has("portrait")) portrait = keyMap.getLast("portrait").next();
		if (portrait != null && portrait.equals("null")) portrait = null;
		
		if (keyMap.has("gender")) gender = Ruleset.Gender.valueOf((keyMap.getLast("gender")).next());
		if (keyMap.has("race")) {
			race = Game.ruleset.getRace(keyMap.getLast("race").next());
			if (race == null) return null;
		}
		if (keyMap.has("xp")) xp = keyMap.getLast("xp").nextInt();
		if (keyMap.has("faction")) faction = keyMap.getLast("faction").next();
		if (keyMap.has("script")) aiscript = keyMap.getLast("script").next();
		if (keyMap.has("conversation")) conversation = keyMap.getLast("conversation").next();
		if (keyMap.has("playercharacter")) playerCharacter = keyMap.getLast("playercharacter").nextBoolean();
		if (keyMap.has("drawwithsubicons")) drawWithSubIcons = keyMap.getLast("drawwithsubicons").nextBoolean();
		
		if (keyMap.has("skincolor")) skinColor = keyMap.getLast("skincolor").nextColor();
		if (keyMap.has("clothingcolor")) clothingColor = keyMap.getLast("clothingcolor").nextColor();
		
		boolean pregenerated = keyMap.getValue("pregenerated", false);
		
		for (LineKeyList line : keyMap.get("role")) {
			Role role = Game.ruleset.getRole(line.next());
			
			if (role == null) return null;
			
			roles.addLevels(role, line.nextInt());
		}
		
		for (LineKeyList line : keyMap.get("createitem")) {
			String createdItemID = line.next();
			String baseItemID = line.next();
			List<String> scripts = new ArrayList<String>();
			
			while (line.hasNext()) {
				scripts.add(line.next());
			}
			
			CreatedItem createdItem = new CreatedItem(baseItemID, createdItemID, scripts);
			Game.curCampaign.addCreatedItem(createdItem);
		}
		
		for (LineKeyList line : keyMap.get("additem")) {
			String itemID = line.next();
			int itemQuantity = 1;
			String itemQuality = Game.ruleset.getString("DefaultItemQuality");
			
			if (line.hasNext()) itemQuantity = line.nextInt();
			if (line.hasNext()) itemQuality = line.next();
			
			unequipped.addItem(itemID, itemQuantity, itemQuality);
		}
		
		for (LineKeyList line : keyMap.get("equip")) {
			String itemID = line.next();
			String itemQuality = Game.ruleset.getString("DefaultItemQuality");
			
			if (line.hasNext()) itemQuality = line.next();
			
			equipped.addItem(itemID, 1, itemQuality);
		}
		
		for (LineKeyList line : keyMap.get("equipoffhand")) {
			String itemID = line.next();
			String itemQuality = Game.ruleset.getString("DefaultItemQuality");
			
			if (line.hasNext()) itemQuality = line.next();
			
			offHand.addItem(itemID, 1, itemQuality);
		}
		
		for (LineKeyList line : keyMap.get("addloot")) {
			String itemListID = line.next();
			int prob = line.nextInt();
			LootList.ProbabilityMode mode = LootList.ProbabilityMode.valueOf(line.next().toUpperCase());
			
			lootEntries.add(new LootList.Entry(itemListID, prob, mode));
		}
		
		for (LineKeyList line : keyMap.get("level")) {
			int level = line.nextInt();
			
			String type = line.next().toLowerCase();
			if (type.equals("addability")) {
				Ability ability = Game.ruleset.getAbility(line.next());
				abilities.add(ability, level);
			} else if (type.equals("improverole")) {
				// TODO implement saving the exact role progression by level
			} else if (type.equals("addskillranks")) {
				// TODO implement saving the exact skill progression by level
			}
		}
		
		for (LineKeyList line : keyMap.get("addskillranks")) {
			String skillID = line.next();
			
			if (Game.ruleset.getSkill(skillID) == null) {
				Logger.appendToWarningLog("Attempted to add ranks for nonexistant skill " +
						skillID + " to Creature " + id);
			} else {
				skills.addRanks(skillID, line.nextInt());
			}
		}
		
		LineKeyList line = keyMap.getLast("currencyreward");
		if (line != null) {
			minCurrencyReward = line.nextInt();
			maxCurrencyReward = line.nextInt();
		}
		
		line = keyMap.getLast("icon");
		if (line != null) {
			icon = line.next();
			
			if (line.hasNext()) {
				iconColor = line.nextColor();
			}
		}
		
		line = keyMap.getLast("attributes");
		if (line != null) {
			for (int i = 0; i < attributes.length; i++) {
				attributes[i] = line.nextInt();
			}
		}
		
		line = keyMap.getLast("hairicon");
		if (line != null) {
			hairIcon = line.next();
			
			if (line.hasNext())
				hairColor = line.nextColor();
		}
		
		line = keyMap.getLast("beardicon");
		if (line != null) {
			beardIcon = line.next();
			
			if (line.hasNext())
				beardColor = line.nextColor();
		}
		
		// done reading in file contents except for quickbar
		
		c = new Creature(id, portrait, icon, name, gender, race, faction, playerCharacter, new Point(false), description);
		
		c.stats().setAttributes(attributes);
		c.setPregenerated(pregenerated);
		
		if (aiscript != null && !aiscript.equals("null")) c.setAIScript(aiscript);
		
		if (conversation != null && conversation.length() == 0) conversation = null;
		c.setConversationScript(conversation);
		c.setIconColor(iconColor);
		c.setImmortal(immortal);
		c.setUnspentSkillPoints(unspentSkillPoints);
		
		if (drawWithSubIcons) {
			c.setDrawOnlyHandSubIcons(drawOnlyHandSubIcons);
			c.setDrawWithSubIcons(drawWithSubIcons);
			
			c.getSubIcons().setSkinColor(skinColor);
			c.getSubIcons().setClothingColor(clothingColor);
			
			if (!drawOnlyHandSubIcons) c.addBaseSubIcons();
			
			if (hairIcon != null) c.setHairSubIcon(hairIcon, hairColor);
			
			if (beardIcon != null) {
				c.setBeardSubIcon(beardIcon, beardColor);
			}
		}
		
		c.getRoles().addLevels(roles);
		
		c.getAbilities().addAll(abilities);

		c.getSkillSet().addRanksFromList(skills);
		
		c.getInventory().getUnequippedItems().addItemsFromList(unequipped);
		
		for (int i = 0; i < equipped.size(); i++) {
			c.getInventory().equipItem(equipped.getItem(i));
		}
		
		for (int i = 0; i < offHand.size(); i++) {
			c.getInventory().equipItem(offHand.getItem(i), Inventory.EQUIPPED_OFF_HAND);
		}
		
		if (xp == -1) c.setExperiencePoints(XP.getPointsForLevel(c.stats().get(Stat.CreatureLevel)));
		else c.setExperiencePoints(xp);
		
		c.setMinCurrencyReward(minCurrencyReward);
		c.setMaxCurrencyReward(maxCurrencyReward);
		
		c.getLoot().addAll(lootEntries);
		
		for (LineKeyList line2 : keyMap.get("readyability")) {
			String abilityID = line2.next();
			Ability ability = Game.ruleset.getAbility(abilityID);
			if (ability == null) {
				Logger.appendToWarningLog("Warning on line " + line2.getLineNumber() + " of " +
						keyMap.getFilePath() + ". Ability " + abilityID + " not found.");
			} else {
				c.getAbilities().readyAbilityInFirstEmptySlot(ability);
			}
		}
		
		// read in quickbar
		for (LineKeyList slotLine : keyMap.get("quickbarslot")) {
			int index = slotLine.nextInt();
			String type = slotLine.next();

			if (type.equals("Use")) {
				String itemID = slotLine.next();
				String qualityID = slotLine.next();
				
				Item item = Game.entityManager.getItem(itemID);
				item.setQuality(Game.ruleset.getItemQuality(qualityID));
				
				c.getQuickbar().setSlot( Quickbar.getQuickbarSlot(item, c), index );
			} else if (type.equals("Equip")) {
				String itemID = slotLine.next();
				String qualityID = slotLine.next();
				
				Item item = Game.entityManager.getItem(itemID);
				item.setQuality(Game.ruleset.getItemQuality(qualityID));
				
				c.getQuickbar().setSlot( Quickbar.getQuickbarSlot(item, c), index );
				
				if (slotLine.hasNext()) {
					String secondaryItemID = slotLine.next();
					String secondaryQualityID = slotLine.next();
					
					Item secondaryItem = Game.entityManager.getItem(secondaryItemID);
					secondaryItem.setQuality(Game.ruleset.getItemQuality(secondaryQualityID));
					
					c.getQuickbar().setSlot( Quickbar.getQuickbarSlot(secondaryItem, c), index );
				}
				
			} else if (type.equals("Ability")) {
				String abilityID = slotLine.next();
				
				Ability ability = Game.ruleset.getAbility(abilityID);
				
				c.getQuickbar().setSlot( Quickbar.getQuickbarSlot(ability, c), index );
			}
		}
		
		keyMap.checkUnusedKeys();
		
		return c;
	}
}
