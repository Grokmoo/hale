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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.matthiasmann.twl.Color;

import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.BonusList;
import net.sf.hale.bonus.BonusManager;
import net.sf.hale.editor.reference.ItemReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.rules.ArmorType;
import net.sf.hale.rules.BaseWeapon;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.ItemQuality;
import net.sf.hale.rules.Size;
import net.sf.hale.rules.Weight;
import net.sf.hale.util.SimpleJSONObject;

public class Item extends Entity implements Referenceable {
	public enum ItemType {
		ITEM, WEAPON, ARMOR, GLOVES, HELMET, CLOAK, BOOTS, BELT, AMULET, RING, AMMO, SHIELD
	};
	
	public enum WeaponType {
		MELEE, THROWN, SLING, CROSSBOW, BOW
	};
	
	public enum WeaponHanded {
		LIGHT, ONE_HANDED, TWO_HANDED, NONE
	};
	
	private boolean forceNoQuality;
	private String name;
	private String fullName;
	private Currency value;
	private int valueStackSize;
	private Weight weight;
	private ItemType itemType;
	private ArmorType armorType;
	private int armorClass;
	private int armorPenalty;
	private int movementPenalty;
	private int shieldAttackPenalty;
	private boolean coversBeardIcon;
	private WeaponType weaponType;
	private BaseWeapon baseWeapon;
	private WeaponHanded weaponHanded;
	private int minWeaponReach;
	private int maxWeaponReach;
	private Size weaponSize;
	private DamageType damageType;
	private int damageMin;
	private int damageMax;
	private int criticalThreatRange;
	private int criticalMultiplier;
	private int attackCost;
	private int rangePenalty;
	private int maximumRange;
	private int maxStrengthBonus;
	private ItemQuality quality;
	private float qualityArmorPenalty, qualityArmorClass, qualityMovementPenalty;
	private Currency qualityValue;
	private boolean ingredient;
	private boolean threatens;
	private boolean quest;
	private Scriptable script;
	private String useButtonText;
	private int useAPCost;
	private String subIcon;
	private Color subIconColor;
	private String projectileIcon;
	private Color projectileIconColor;
	private int threatenMin = 0, threatenMax = 0;
	private final BonusManager bonusManager;
	private Creature owner;
	
	private boolean cursed;
	private String qualityName;
	private List<Enchantment> enchantments;

	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = super.save();
		
		data.put("itemQuality", qualityName);
		
		if (cursed) data.put("cursed", cursed);
		
		return data;
	}
	
	@Override public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		super.load(data, refHandler);
		
		if (data.containsKey("cursed"))
			this.cursed = data.get("cursed", false);
		else
			this.cursed = false;
		
		String qualityName = data.get("itemQuality", null);
		setQuality(qualityName);
		
		this.recomputeBonuses();
	}
	
	public Item(String id, String name, String icon, ItemType itemType, String description, Currency value) {
		super(id, icon, description);
		this.type = Entity.Type.ITEM;
		this.name = name;
		this.itemType = itemType;
		this.value = value;
		this.valueStackSize = 1;
		this.setQuality(Game.ruleset.getItemQuality(Game.ruleset.getString("DefaultItemQuality")));
		this.weight = new Weight();

		this.bonusManager = new BonusManager();
		
		this.subIconColor = Color.WHITE;
		this.projectileIconColor = Color.WHITE;
		this.enchantments = new ArrayList<Enchantment>();
		
		setFullName();
	}
	
	public Item(Item c) {
		super(c);
		this.type = Entity.Type.ITEM;
		this.name = c.name;
		this.itemType = c.itemType;
		this.value = c.value;
		
		this.forceNoQuality = c.forceNoQuality;
		
		this.setWeaponProperties(c.weaponType, c.baseWeapon, c.weaponHanded, c.weaponSize, c.damageType, c.damageMin,
				                 c.damageMax, c.minWeaponReach, c.maxWeaponReach, c.criticalThreatRange,
				                 c.criticalMultiplier, c.rangePenalty, c.maximumRange, c.maxStrengthBonus, c.threatens, c.attackCost);
		
		this.setArmorProperties(c.armorType, c.armorClass, c.armorPenalty, c.movementPenalty, c.shieldAttackPenalty);
		
		// set properties after weapon and armor so quality modifiers are computed
		this.setProperties(c.getQuality(), new Weight(c.weight), c.valueStackSize, c.quest, c.ingredient, c.cursed);
		
		this.bonusManager = new BonusManager(c.bonusManager);
		
		if (c.hasScript()) {
			this.script = new Scriptable(c.script);
		}
		
		this.useAPCost = c.useAPCost;
		this.useButtonText = c.useButtonText;
		
		this.subIcon = c.subIcon;
		this.subIconColor = c.subIconColor;
		
		this.projectileIcon = c.projectileIcon;
		this.projectileIconColor = c.projectileIconColor;
		
		this.coversBeardIcon = c.coversBeardIcon;
		
		this.enchantments = new ArrayList<Enchantment>(c.enchantments);
		
		setFullName();
	}
	
	public void setForceNoQuality(boolean force) {
		this.forceNoQuality = force;
	}
	
	public boolean isForceNoQuality() {
		return forceNoQuality;
	}
	
	public List<Enchantment> getEnchantments() {
		return Collections.unmodifiableList(enchantments);
	}
	
	public void createEnchantment(String script) {
		Enchantment enchantment = new Enchantment(script, true);
		
		enchantments.add(enchantment);
		
		recomputeBonuses();
	}
	
	public void setEnchantments(List<String> scripts) {
		enchantments.clear();
		
		for (String script : scripts) {
			enchantments.add(new Enchantment(script));
		}
		
		recomputeBonuses();
	}
	
	public void setCoversBeardIcon(boolean covers) {
		this.coversBeardIcon = covers;
	}
	
	public boolean coversBeardIcon() {
		return coversBeardIcon;
	}
	
	public Color getProjectileIconColor() { return projectileIconColor; }
	public void setProjectileIconColor(Color projectileIconColor) { this.projectileIconColor = projectileIconColor; }
	
	public String getProjectileIcon() { return projectileIcon; }
	public void setProjectileIcon(String projectileIcon) { this.projectileIcon = projectileIcon; }
	
	public Color getSubIconColor() { return subIconColor; }
	public void setSubIconColor(Color subIconColor) { this.subIconColor = subIconColor; }
	
	public String getSubIcon() { return subIcon; }
	public void setSubIcon(String subIcon) { this.subIcon = subIcon; }
	
	public String getUseButtonText() { return useButtonText; }
	public int getUseAPCost() { return useAPCost; }
	
	public void setScript(String scriptLocation) {
		if (scriptLocation == null) {
			this.script = null;
		} else {
			String script = ResourceManager.getScriptResourceAsString(scriptLocation);

			this.script = new Scriptable(script, scriptLocation, false);
		}
		
		setFullName();
	}
	
	public Scriptable getScript() { return script; }
	
	public boolean hasScript() { return script != null; }
	
	/**
	 * Returns true if and only if this Item is usable.  This means it
	 * can be used via the inventory right click menu
	 * 
	 * @return true if and only if this Item is usable
	 */
	
	public boolean isUsable() {
		if (hasScript()) {
			return script.hasFunction(ScriptFunctionType.onUse);
		}
		
		return false;
	}
	
	/**
	 * Returns true if and only if the specified parent Creature can use this
	 * item with its current state, including AP.  If this Item has a canUse()
	 * script function, that function must return true in order for this method
	 * to return true.
	 * 
	 * @param parent the Creature to use this item
	 * @return true if and only if this item can be used by the specified parent
	 */
	
	public boolean canUse(Creature parent) {
		if (!isUsable()) return false;
		
		if (!parent.getTimer().canPerformAction(useAPCost)) return false;
		
		if (script.hasFunction(ScriptFunctionType.canUse)) {
			Object returnValue = script.executeFunction(ScriptFunctionType.canUse, this, parent);
			return Boolean.TRUE.equals(returnValue);
		} else {
			return true;
		}
	}
	
	/**
	 * The specified parent Creature uses this Item.  This item's onUse
	 * script Function is called.
	 * 
	 * @param parent the Creature using this Item.
	 */
	
	public void use(Creature parent) {
		if (!isUsable()) return;
		
		parent.getTimer().performAction(useAPCost);
		
		script.executeFunction(ScriptFunctionType.onUse, this, parent);
	}
	
	public void setUseButtonText(String text) {
		this.useButtonText = text;
	}
	
	public void setUseAPCost(int cost) {
		this.useAPCost = cost;
	}
	
	public void setProperties(ItemQuality quality, Weight weight, int valueStackSize,
							  boolean quest, boolean ingredient, boolean cursed) {
		
		this.weight = weight;
		this.valueStackSize = valueStackSize;
		this.quest = quest;
		this.ingredient = ingredient;
		this.cursed = cursed;
		
		this.setQuality(quality);
	}
	
	public void setArmorProperties(ArmorType armorType, int armorClass, int armorPenalty,
			int movementPenalty, int shieldAttackPenalty) {
		this.setArmorType(armorType);
		this.armorClass = armorClass;
		this.armorPenalty = armorPenalty;
		this.movementPenalty = movementPenalty;
		this.shieldAttackPenalty = shieldAttackPenalty;
	}
	
	public void setWeaponProperties(WeaponType weaponType, BaseWeapon baseWeapon, WeaponHanded weaponHanded, Size weaponSize,
			                        DamageType damageType, int damageMin, int damageMax,
			                        int minWeaponReach, int maxWeaponReach, int criticalThreatRange,
			                        int criticalMultiplier, int rangePenalty, int maximumRange, int maxStrengthBonus,
			                        boolean threatens, int attackCost) {
		this.weaponType = weaponType;
		this.setBaseWeapon(baseWeapon);
		this.weaponHanded = weaponHanded;
		this.weaponSize = weaponSize;
		this.damageMin = damageMin;
		this.damageMax = damageMax;
		this.minWeaponReach = minWeaponReach;
		this.maxWeaponReach = maxWeaponReach;
		this.criticalThreatRange = criticalThreatRange;
		this.criticalMultiplier = criticalMultiplier;
		this.rangePenalty = rangePenalty;
		this.maximumRange = maximumRange;
		this.maxStrengthBonus = maxStrengthBonus;
		this.threatens = threatens;
		this.setDamageType(damageType);
		this.attackCost = attackCost;
		
		if (weaponSize != null) setThreaten();
	}
	
	public void setBaseWeapon(BaseWeapon baseWeapon) {
		this.baseWeapon = baseWeapon;
	}
	
	public void setArmorType(ArmorType armorType) {
		this.armorType = armorType;
	}
	
	public void setArmorClass(int armorClass) { this.armorClass = armorClass; }
	public void setArmorPenalty(int armorPenalty) { this.armorPenalty = armorPenalty; }
	public void setMovementPenalty(int movementPenalty) { this.movementPenalty = movementPenalty; }
	public void setShieldAttackPenalty(int shieldAttackPenalty) { this.shieldAttackPenalty = shieldAttackPenalty; }
	
	public void setDamageType(DamageType damageType) {
		this.damageType = damageType;
	}
	
	public void setWeaponHanded(WeaponHanded weaponHanded) { this.weaponHanded = weaponHanded; }
	public void setWeaponSize(Size weaponSize) { this.weaponSize = weaponSize; }
	public void setMinWeaponReach(int minWeaponReach) { this.minWeaponReach = minWeaponReach; }
	public void setMaxWeaponReach(int maxWeaponReach) { this.maxWeaponReach = maxWeaponReach; }
	public void setCriticalThreatRange(int criticalThreatRange) { this.criticalThreatRange = criticalThreatRange; }
	public void setCriticalMultiplier(int criticalMultiplier) { this.criticalMultiplier = criticalMultiplier; }
	public void setRangePenalty(int rangePenalty) { this.rangePenalty = rangePenalty; }
	public void setMaximumRange(int maximumRange) { this.maximumRange = maximumRange; }
	public void setMaxStrengthBonus(int maxStrengthBonus) { this.maxStrengthBonus = maxStrengthBonus; }
	public void setThreatens(boolean threatens) { this.threatens = threatens; }
	public void setMinDamage(int minDamage) { this.damageMin = minDamage; }
	public void setMaxDamage(int maxDamage) { this.damageMax = maxDamage; }
	
	private void setThreaten() {
		threatenMin = minWeaponReach;
		threatenMax = maxWeaponReach;
		
		if (threatenMax < threatenMin) threatenMax = threatenMin;
	}
	
	public ItemQuality getQuality() { return quality; }
	
	public int getAttackCost() { return attackCost; }
	public int getWeaponReachMin() { return minWeaponReach; }
	public int getWeaponReachMax() { return maxWeaponReach; }
	public int getThreatenMin() { return threatenMin; }
	public int getThreatenMax() { return threatenMax; }
	
	public int getWeaponSizeDifference(Item other) {
		if (other == null) return 0;
		
		if (this.itemType != ItemType.WEAPON) return 0;
		if (other.itemType != ItemType.WEAPON) return 0;
		
		int otherSize = other.weaponSize.getSize();
		if (other.weaponHanded == WeaponHanded.LIGHT) otherSize--;
		else if (other.weaponHanded == WeaponHanded.TWO_HANDED) otherSize++;
		
		
		int thisSize = this.weaponSize.getSize();
		if (this.weaponHanded == WeaponHanded.LIGHT) thisSize--;
		else if (this.weaponHanded == WeaponHanded.TWO_HANDED) thisSize++;
		
		return thisSize - otherSize;
	}
	
	public boolean canWieldInOffHand(Creature wielder, Item mainHand) {
		if (!canWieldInOneHand(wielder)) return false;
		
		if (this.itemType == Item.ItemType.WEAPON) {
			if (mainHand == null) return false;
			
			if (!mainHand.isMeleeWeapon()) return false;
			
			if (!this.isMeleeWeapon()) return false;
			
			return wielder.stats().has(Bonus.Type.DualWieldTraining);
		}
		
		return true;
	}
	
	public boolean canWieldInOneHand(Creature wielder) {
		switch (this.itemType) { 
		case WEAPON:
			WeaponHanded handed = getWeaponHandedForCreature(wielder.getRace().getSize());

			switch (handed) {
			case LIGHT: case ONE_HANDED:
				return true;
			default:
				return false;
			}
		case SHIELD:
			return true;
		default:
			return false;
		}
	}
	
	public WeaponHanded getWeaponHandedForCreature(Creature other) {
		return getWeaponHandedForCreature(other.getRace().getSize());
	}
	
	public WeaponHanded getWeaponHandedForCreature(Size wielderSize) {
		int diff = this.weaponSize.getDifference(wielderSize);
		
		// items that are at most two size categories different than the wielder can be used
		switch (diff) {
		case -2:
			switch (weaponHanded) {
			case LIGHT: return WeaponHanded.LIGHT;
			case ONE_HANDED: return WeaponHanded.LIGHT;
			case TWO_HANDED: return WeaponHanded.LIGHT;
			}
			break;
		case -1:
			switch (weaponHanded) {
			case LIGHT: return WeaponHanded.LIGHT;
			case ONE_HANDED: return WeaponHanded.LIGHT;
			case TWO_HANDED: return WeaponHanded.ONE_HANDED;
			}
			break;
		case 0:
			return weaponHanded;
		case 1:
			switch (weaponHanded) {
			case LIGHT: return WeaponHanded.ONE_HANDED;
			case ONE_HANDED: return WeaponHanded.TWO_HANDED;
			}
			break;
		case 2:
			switch (weaponHanded) {
			case LIGHT: return WeaponHanded.TWO_HANDED;
			}
			break;
		}
		
		return WeaponHanded.NONE;
	}
	
	public boolean isIngredient() { return ingredient; }
	
	public boolean isCursed() { return cursed; }
	public boolean isQuestItem() { return quest; }
	public boolean threatens() { return threatens; }
	
	public void setIngredient(boolean ingredient) { this.ingredient = ingredient; }
	public void setCursed(boolean cursed) { this.cursed = cursed; }
	public void setQuestItem(boolean quest) { this.quest = quest; }
	public void setAttackCost(int attackCost) { this.attackCost = attackCost; }
	public void setValueStackSize(int valueStackSize) { this.valueStackSize = valueStackSize; }
	public void setValue(Currency value) { this.value = value; }
	public void setWeight(Weight weight) { this.weight = weight; }
	public void setName(String name) { this.name = name; }
	public void setWeaponType(WeaponType weaponType) { this.weaponType = weaponType; }
	public void setItemType(ItemType itemType) { this.itemType = itemType; }
	public ItemType getItemType() { return itemType; }
	
	public int getWeightGrams() { return weight.getWeight(); }
	public Weight getWeight() { return new Weight(weight); }
	public Currency getValue() { return new Currency(value); }
	public int getValueStackSize() { return valueStackSize; }
	public Currency getQualityValue() { return new Currency(qualityValue); }
	
	public boolean isArmor() {
		return (itemType == ItemType.ARMOR &&
				armorType != Game.ruleset.getArmorType(Game.ruleset.getString("DefaultArmorType")));
	}
	
	public boolean isBoots() { return itemType == ItemType.BOOTS; }
	public boolean isGloves() { return itemType == ItemType.GLOVES; }
	public boolean isHelmet() { return itemType == ItemType.HELMET; }
	public boolean isMeleeWeapon() { return (itemType == ItemType.WEAPON && weaponType == WeaponType.MELEE); }
	public boolean isShield() { return itemType == ItemType.SHIELD; }
	
	public ArmorType getArmorType() { return armorType; }
	public DamageType getDamageType() { return damageType; }
	public WeaponType getWeaponType() { return weaponType; }
	public WeaponHanded getWeaponHanded() { return weaponHanded; }
	public BaseWeapon getBaseWeapon() { return baseWeapon; }
	public Size getWeaponSize() { return weaponSize; }
	public int getCriticalThreatRange() { return criticalThreatRange; }
	public int getCriticalMultiplier() { return criticalMultiplier; }
	public int getRangePenalty() { return rangePenalty; }
	public int getMaximumRange() { return maximumRange; }
	public int getMaxStrengthBonus() { return maxStrengthBonus; }
	
	public int getDamageMin() { return damageMin; }
	public int getDamageMax() { return damageMax; }
	public int getArmorClass() { return armorClass; }
	public int getArmorPenalty() { return armorPenalty; }
	public int getMovementPenalty() { return movementPenalty; }
	public int getShieldAttackPenalty() { return shieldAttackPenalty; }
	
	public void setQuality(String qualityID) {
		ItemQuality quality = Game.ruleset.getItemQuality(qualityID);
		setQuality(quality);
	}
	
	public void setQuality(ItemQuality quality) {
		this.quality = quality;
		this.qualityName = quality.getName();
		
		this.qualityArmorPenalty = armorPenalty * (1.0f - ((float)quality.getArmorPenaltyBonus()) / 100.0f);
		this.qualityArmorClass = armorClass * (1.0f + ((float)quality.getArmorClassBonus()) / 100.0f);
		this.qualityMovementPenalty = movementPenalty * (1.0f - ((float)quality.getMovementPenaltyBonus()) / 100.0f);
		
		this.qualityValue = new Currency(value.getValue() * quality.getValueAdjustment() / 100);
		
		setFullName();
	}
	
	public boolean hasQuality() {
		if (forceNoQuality) return false;
		
		switch (itemType) {
		case WEAPON: case ARMOR: case GLOVES: case HELMET: case BOOTS: case SHIELD:
			return true;
		}
		
		return isUsable();
	}
	
	private void setFullName() {
		if (hasQuality())
			fullName = getQuality().getName() + " " + name;
		else
			fullName = name;
	}
	
	public float getQualityArmorPenalty() { return qualityArmorPenalty; }
	public float getQualityArmorClass() { return qualityArmorClass; }
	public float getQualityMovementPenalty() { return qualityMovementPenalty; }
	
	@Override public String getName() { return name; }
	
	public BonusManager bonuses() { return bonusManager; }
	
	@Override public String getReferenceType() {
		return "Item";
	}
	
	@Override public String toString() { return getID(); }
	
	@Override public ReferenceList getReferenceList() {
		return new ItemReferenceList(this);
	}
	
	@Override public String getFullName() {
		return fullName;
	}
	
	@Override protected void applyEffectBonuses(Effect effect) {
		bonusManager.addAll(effect.getBonuses());
		
		if (owner != null) owner.stats().addAll(effect.getBonuses());
	}
	
	@Override protected void removeEffectBonuses(Effect effect) {
		bonusManager.removeAll(effect.getBonuses());
		
		if (owner != null) owner.stats().removeAll(effect.getBonuses());
	}
	
	public void recomputeBonuses() {
		bonusManager.clear();
		
		for (Effect effect: getEffects()) {
			bonusManager.addAll(effect.getBonuses());
		}
		
		for (Enchantment enchantment : enchantments) {
			bonusManager.addAll(enchantment.getBonuses());
		}
	}
	
	public BonusList getAllAppliedBonuses() {
		BonusList bonuses = new BonusList();

		for (Effect effect : this.getEffects()) {
			bonuses.addAll(effect.getBonuses());
		}
		
		for (Enchantment enchantment : enchantments) {
			bonuses.addAll(enchantment.getBonuses());
		}
		
		return bonuses;
	}
	
	public void setOwner(Creature owner) {
		this.owner = owner;
	}
	
	@Override public boolean elapseRounds(int rounds) {
		boolean returnValue = super.elapseRounds(rounds);
		
		this.updateViewers();
		
		return returnValue;
	}

	@Override public boolean isValidEffectTarget() {
		if (owner == null) return true;
		else return !owner.isDead();
	}
}
