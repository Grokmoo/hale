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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import de.matthiasmann.twl.Color;

import net.sf.hale.Game;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Enchantment;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Openable;
import net.sf.hale.entity.Trap;
import net.sf.hale.rules.LootList;

public class ItemWriter {
	public static void saveItem(Item item) {
		saveItem(item, false, false, false);
	}
	
	public static void saveItemAsDoor(Item item) {
		saveItem(item, false, true, false);
	}
	
	public static void saveItemAsContainer(Item item) {
		saveItem(item, true, false, false);
	}
	
	public static void saveItemAsTrap(Item item) {
		saveItem(item, false, false, true);
	}
	
	private static void saveItem(Item item, boolean container, boolean door, boolean isTrap) {
		try {
			File file = new File("campaigns/" + Game.curCampaign.getID() + "/items/" + item.getID() + ".txt");
			file.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			out.write("name \"" + item.getName() + "\"");
			out.newLine();
			
			if (item.getIcon() != null) {
				out.write("icon \"" + item.getIcon() + "\"");
				
				Color iconColor = item.getIconColor();
				if (iconColor != null) {
					out.write(" \"#" + Integer.toHexString(iconColor.toARGB()) + "\"");
				}
				
				out.newLine();
			}
			
			if (item.getSubIcon() != null) {
				out.write("subIcon \"" + item.getSubIcon() + "\"");
				
				Color subIconColor = item.getSubIconColor();
				if (subIconColor != null) {
					out.write(" \"#" + Integer.toHexString(subIconColor.toARGB()) + "\"");
				}
				
				out.newLine();
			}
			
			if (item.getProjectileIcon() != null) {
				out.write("projectileIcon \"" + item.getProjectileIcon() + "\"");
				
				Color projectileColor = item.getProjectileIconColor();
				if (projectileColor != null) {
					out.write(" \"#" + Integer.toHexString(projectileColor.toARGB()) + "\"");
				}
				
				out.newLine();
			}
			
			if (item.isForceNoQuality()) {
				out.write("forceNoQuality true");
				out.newLine();
			}
			
			out.write("ingredient " + item.isIngredient());
			out.newLine();
			
			out.write("quest " + item.isQuestItem());
			out.newLine();
			
			out.write("cursed " + item.isCursed());
			out.newLine();
			
			out.write("description \"" + item.getDescription() + "\"");
			out.newLine();
			
			out.write("value \"" + item.getValue().shortString() + "\" " + item.getValueStackSize());
			out.newLine();
			
			out.write("weight " + item.getWeight().grams);
			out.newLine();
			
			out.write("itemType \"" + item.getItemType().toString() + "\"");
			out.newLine();
			
			out.write("quality \"" + item.getQuality().getName() + "\"");
			out.newLine();
			
			if (container) {
				out.write("container true");
				out.newLine();
				LootList loot = ((Container)item).getLoot();
				
				for (int i = 0; i < loot.size(); i++) {
					LootList.Entry entry = loot.getEntry(i);
					
					out.write("addloot \"" + entry.itemListID + "\" " + entry.probability + " " + entry.mode.toString());
					out.newLine();
				}
				
				out.write("workbench " + ((Container)item).isWorkbench());
				out.newLine();
			}
			
			if (door) {
				out.write("door true");
				out.newLine();
				out.write("doorTransparent \"" + ((Door)item).isTransparent() + "\"");
				out.newLine();
			}
			
			if (door || container) {
				out.write("openicon \"" + ((Openable)item).getOpenIcon() + "\"");
				out.newLine();
			}
			
			if (isTrap) {
				Trap trap = (Trap)item;
				
				out.write("trap true");
				out.newLine();
				
				out.write("trapDifficulties " + trap.getFindDifficulty() + " " + trap.getPlaceDifficulty() + " ");
				out.write(trap.getDisarmDifficulty() + " " + trap.getRecoverDifficulty() + " " + trap.getReflexDifficulty());
				out.newLine();
				
				out.write("trapActivatesOnlyOnce " + trap.activatesOnlyOnce());
				out.newLine();
				
				out.write("damageType \"" + item.getDamageType().getName() + "\"");
				out.newLine();
				
				out.write("damage " + item.getDamageMin() + " " + item.getDamageMax());
				out.newLine();
			}
			
			if (door || container) {
				Openable openable = (Openable)item;
				
				if (openable.isLocked()) {
					out.write("lock " + openable.getLockDifficulty());
					out.newLine();
				}
				
				if (openable.getKey() != null) {
					out.write("key \"" + openable.getKey() + "\"");
					out.newLine();
				}
				
				if (openable.isKeyRequired()) {
					out.write("keyRequired true");
					out.newLine();
				}
				
				if (openable.removeKeyOnUnlock()) {
					out.write("removeKeyOnUnlock true");
					out.newLine();
				}
			}
			
			for (Enchantment enchantment : item.getEnchantments()) {
				out.write("enchantment \"");
				out.write(enchantment.getScript());
				out.write("\"");
				out.newLine();
			}
			
			if (item.hasScript()) {
				out.write("script \"" + item.getScript().getScriptLocation() + "\"");
				out.newLine();
			}
			
			if (item.getConversationScript() != null) {
				out.write("conversation \"" + item.getConversationScript() + "\"");
				out.newLine();
			}
			
			if (item.getUseButtonText() != null) {
				out.write("useButtonText \"" + item.getUseButtonText() + "\"");
				out.newLine();
			}
			
			if (item.getUseAPCost() != 0) {
				out.write("useAPCost " + item.getUseAPCost());
				out.newLine();
			}
			
			switch (item.getItemType()) {
			case AMMO:
				out.write("weaponType \"" + item.getWeaponType().toString() + "\"");
				out.newLine();
				break;
			case WEAPON:
				if (item.getBaseWeapon() != null) {
					out.write("baseWeapon \"" + item.getBaseWeapon().getName() + "\"");
					out.newLine();
				}
				
				if (item.getWeaponType() != null) {
					out.write("weaponType \"" + item.getWeaponType().toString() + "\"");
					out.newLine();
				}
				
				if (item.getWeaponHanded() != null && item.getWeaponSize() != null) {
					String handedString = null;
					switch (item.getWeaponHanded()) {
					case LIGHT: handedString = "light"; break;
					case ONE_HANDED: handedString = "onehanded"; break;
					case TWO_HANDED: handedString = "twohanded"; break;
					}

					out.write("weaponSize \"" + handedString + "\" \"" + item.getWeaponSize() + "\"");
					out.newLine();
				}
				
				out.write("reach " + item.getWeaponReachMin() + " " + item.getWeaponReachMax());
				out.newLine();
				
				out.write("threatens " + item.threatens());
				out.newLine();
				
				if (item.getDamageType() != null) {
					out.write("damageType \"" + item.getDamageType().getName() + "\"");
					out.newLine();
				}
				
				out.write("damage " + item.getDamageMin() + " " + item.getDamageMax());
				out.newLine();
				
				out.write("criticalRange " + item.getCriticalThreatRange());
				out.newLine();
				
				out.write("criticalMultiplier " + item.getCriticalMultiplier());
				out.newLine();
				
				out.write("attackCost " + item.getAttackCost());
				out.newLine();
				
				if (item.getWeaponType() != Item.WeaponType.MELEE) {
					out.write("rangePenalty " + item.getRangePenalty());
					out.newLine();
					
					out.write("maximumRange " + item.getMaximumRange());
					out.newLine();
					
					out.write("maxStrengthBonus " + item.getMaxStrengthBonus());
					out.newLine();
				}
				break;
			case SHIELD:
				out.write("shieldAttackPenalty " + item.getShieldAttackPenalty());
				out.newLine();
				// intended to fall through to the next level
			case ARMOR:
			case HELMET:
			case BOOTS:
			case GLOVES:
				if (item.getArmorType() != null) {
					out.write("armorType \"" + item.getArmorType().getName() + "\"");
					out.newLine();
				}
				
				out.write("armorClass " + item.getArmorClass());
				out.newLine();
				
				out.write("armorPenalty " + item.getArmorPenalty());
				out.newLine();
				
				out.write("movementPenalty " + item.getMovementPenalty());
				out.newLine();
				
				if (item.coversBeardIcon()) {
					out.write("coversBeardIcon true");
					out.newLine();
				}
				break;
			}
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving item " + item.getID(), e);
		}
	}
}
