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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.matthiasmann.twl.Color;

import net.sf.hale.Game;
import net.sf.hale.ability.AbilitySlot;
import net.sf.hale.ability.CreatureAbilitySet;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.CreatedItem;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.quickbar.Quickbar;
import net.sf.hale.quickbar.QuickbarSlot;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.LootList;
import net.sf.hale.rules.SubIcon;

/**
 * Contains static methods for writing creatures to a file for later use.
 * 
 * @author Jared Stephen
 *
 */

public class CreatureWriter {
	
	/**
	 * Saves the given creature to a file in the current campaign creatures directory based on its ID.
	 * 
	 * @param c The creature to be saved to disk.
	 */
	
	public static void saveCreature(Creature c) {
		File file = new File("campaigns/" + Game.curCampaign.getID() + "/creatures/" + c.getID() + ".txt");
		saveCreature(c, file);
	}
	
	/**
	 * Saves the given creature to a file based on its ID and the current time in the characters/ directory.
	 * 
	 * @param c The creature to be saved to disk.
	 * @return the file id that the creature was written to
	 */
	
	public static String saveCharacter(Creature c) {
		File dir = new File("characters/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		// get unique ID string
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String id = c.getName() + "-" + format.format(Calendar.getInstance().getTime());
		
		File file = new File("characters/" + id + ".txt");
		saveCreature(c, file);
		
		return id;
	}
	
	private static void saveCreature(Creature c, File file) {
		try {
			file.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			out.write("playerCharacter " + c.isPlayerSelectable()); out.newLine();
			if (c.getFaction() != null) out.write("faction \"" + c.getFaction().getName() + "\""); out.newLine();
			out.write("name \"" + c.getName() + "\""); out.newLine();
			
			if (c.getIcon() != null) {
				out.write("icon \"" + c.getIcon() + "\"");
				Color iconColor = c.getIconColor();
				if (iconColor != null) {
					out.write(" \"#" + Integer.toHexString(iconColor.toARGB()) + "\"");
				}
				out.newLine();
			}
			
			if (c.getPortrait() != null) {
				out.write("portrait \"" + c.getPortrait() + "\""); out.newLine();
				out.newLine();
			}
			
			out.write("gender \"" + c.getGender().toString() + "\""); out.newLine();
			out.write("race \"" + c.getRace().getID() + "\""); out.newLine();
			
			if (c.isImmortal()) {
				out.write("immortal true");
				out.newLine();
			}
			
			out.write("unspentSkillPoints " + c.getUnspentSkillPoints());
			out.newLine();
			
			if (c.drawWithSubIcons()) {
				if (c.drawOnlyHandSubIcons()) {
					out.write("drawOnlyHandSubIcons true");
					out.newLine();
				}
				
				out.write("drawWithSubIcons true");
				out.newLine();
				
				String hairIcon = c.getHairIcon();
				if (hairIcon != null) {
					out.write("hairicon \"" + hairIcon + "\"");
				}
				
				Color hairColor = c.getHairColor();
				if (hairColor != null) {
					out.write(" \"#" + Integer.toHexString(hairColor.toARGB()) + "\"");
				}
				
				out.newLine();
				
				String beardIcon = c.getSubIcon(SubIcon.Type.Beard);
				if (beardIcon != null) {
					out.write("beardicon \"" + beardIcon + "\"");
				}
				
				Color beardColor = c.getSubIconColor(SubIcon.Type.Beard);
				if (beardColor != null) {
					out.write(" \"#" + Integer.toHexString(beardColor.toARGB()) + "\"");
				}
				
				out.newLine();
				
				Color skinColor = c.getSubIcons().getSkinColor();
				out.write("skincolor \"#" + Integer.toHexString(skinColor.toARGB()) + "\"");
				out.newLine();
				
				Color clothingColor = c.getSubIcons().getClothingColor();
				out.write("clothingcolor \"#" + Integer.toHexString(clothingColor.toARGB()) + "\"");
				out.newLine();
			}
			
			if (c.getExperiencePoints() != 0) {
				out.write("xp " + c.getExperiencePoints());
				out.newLine();
			}
			
			for (String roleID : c.getRoles().getRoleIDs()) {
				out.write("role \"" + roleID + "\" " + c.getRoles().getLevel(roleID));
				out.newLine();
			}
			
			out.write("attributes " + c.stats().get(Stat.BaseStr) + " " + c.stats().get(Stat.BaseDex) + " ");
			out.write(c.stats().get(Stat.BaseCon) + " " + c.stats().get(Stat.BaseInt) + " ");
			out.write(c.stats().get(Stat.BaseWis) + " " + c.stats().get(Stat.BaseCha));
			out.newLine();
			
			if (c.hasAI()) {
				out.write("script \"" + c.getAIScript() + "\"");
				out.newLine();
			}
			
			if (c.getConversationScript() != null) {
				out.write("conversation \"" + c.getConversationScript() + "\"");
				out.newLine();
			}
			
			for (String skillID : c.getSkillSet().getSkills()) {
				int ranks = c.getSkillSet().getRanks(skillID);
				if (ranks != 0) {
					out.write("addSkillRanks \"" + skillID + "\" " + ranks);
					out.newLine();
				}
			}
			
			CreatureAbilitySet abilities = c.getAbilities();
			for (String type : abilities.getAllTypes()) {
				for (CreatureAbilitySet.AbilityInstance abilityInstance : abilities.getAbilityInstancesOfType(type)) {
					if (abilityInstance.isRacialAbility() || abilityInstance.isRoleAbility()) continue;
					
					out.write("level " + abilityInstance.getLevel() + " addAbility \"");
					out.write(abilityInstance.getAbility().getID() + "\"");
					out.newLine();
				}
			}
			
			for (String type : abilities.getActivateableTypes()) {
				for (AbilitySlot slot : abilities.getSlotsOfType(type)) {
					if (slot.getAbilityID() != null && !slot.isFixed()) {
						out.write("readyAbility \"" + slot.getAbilityID() + "\"");
						out.newLine();
					}
				}
			}
			
			out.newLine();
			
			if (c.getMaxCurrencyReward() != 0) {
				out.write("currencyReward " + c.getMinCurrencyReward() + " " + c.getMaxCurrencyReward());
				out.newLine();
			}
			
			for (int i = 0; i < Inventory.EQUIPPED_SIZE; i++) {
				Item item = c.getInventory().getEquippedItem(i);
				
				if (item != null) {
					// check for a created item
					writeCreatedItem(Game.curCampaign.getCreatedItem(item.getID()), out);
					
					if (i == Inventory.EQUIPPED_OFF_HAND && item.isMeleeWeapon()) {
						out.write("equipoffhand \"");
					} else {
						out.write("equip \"");
					}
					out.write(item.getID() + "\" " + item.getQuality().getName());
					out.newLine();
				}
			}
			
			ItemList itemList = c.getInventory().getUnequippedItems();
			for (int i = 0; i < itemList.size(); i++) {
				Item item = itemList.getItem(i);
				int quantity = itemList.getQuantity(i);
				
				// check for a created item
				writeCreatedItem(Game.curCampaign.getCreatedItem(item.getID()), out);
				
				out.write("additem \"" + item.getID() + "\" " + quantity + " " + itemList.getQuality(i));
				out.newLine();
			}
			
			LootList loot = c.getLoot();
			for (int i = 0; i < loot.size(); i++) {
				LootList.Entry entry = loot.getEntry(i);
				
				out.write("addloot \"" + entry.itemListID + "\" " + entry.probability + " " + entry.mode.toString());
				out.newLine();
			}
			
			if (c.isPlayerSelectable()) {
				for (int i = 0; i < Quickbar.TotalSlots; i++) {
					QuickbarSlot slot = c.getQuickbar().getSlot(i);
					if (slot == null) continue;

					out.write("quickbarslot " + i + " ");
					out.write(slot.getSaveDescription());
					out.newLine();
				}
			}
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving creature " + c.getName(), e);
		}
	}
	
	private static void writeCreatedItem(CreatedItem createdItem, BufferedWriter out) throws IOException {
		if (createdItem != null) {
			out.write("createItem \"");
			out.write(createdItem.getCreatedItemID());
			out.write("\" \"");
			out.write(createdItem.getBaseItemID());
			out.write("\"");
			for (String script : createdItem.getScripts()) {
				out.write(" \"");
				out.write(script);
				out.write("\"");
			}
			out.newLine();
		}
	}
}
