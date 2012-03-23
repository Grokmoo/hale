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

package net.sf.hale.view;

import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.Role;
import net.sf.hale.rules.XP;
import net.sf.hale.widgets.BasePortraitViewer;

import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget for displaying an overview of a character along with their portrait.
 * The overview includes stats like name, race, role, attributes, equipped items
 * in hands, most stats contained in {@link net.sf.hale.bonus.StatManager} and
 * any current Effects applied to the parent.
 * @author Jared Stephen
 *
 */

public class CharacterSheet extends ScrollPane {
	private Creature creature;
	
	private final TextArea textArea;
	private final HTMLTextAreaModel textAreaModel;
	
	private BasePortraitViewer viewer;
	
	private final Widget content;
	
	/**
	 * Create a new CharacterSheet.  You must call updateContent
	 * before this CharacterSheet will show anything.
	 */
	
	public CharacterSheet() {
		content = new Content();
		
		textAreaModel = new HTMLTextAreaModel();
        textArea = new TextArea(textAreaModel);
        
        this.setContent(content);
        this.setFixed(ScrollPane.Fixed.HORIZONTAL);
        
        content.add(textArea);
	}
	
	/**
	 * Sets this CharacterSheet to show stats and the portrait of the specified Creature
	 * @param parent the Creature to show stats for
	 */
	
	public void updateContent(Creature parent) {
		StringBuilder sb = new StringBuilder();
		
		if (parent == null) {
			textAreaModel.setHtml(sb.toString());
			creature = null;
			if (viewer != null) content.removeChild(viewer);
			invalidateLayout();
			return;
		}
		
		if (creature != parent) {
			this.creature = parent;
			
			if (viewer != null) content.removeChild(viewer);
			
			viewer = new BasePortraitViewer(creature);
			content.add(viewer);
		}
		
		sb.append("<div style=\"font-family: vera-bold;\">").append(parent.getName()).append("</div>");
		
		sb.append("<div style=\"font-family: vera;\">");
		sb.append(parent.getGender()).append(' ');
		sb.append("<span style=\"font-family: vera-blue;\">").append(parent.getRace().getName()).append("</span>");
		sb.append("</div>");
		
		sb.append("<div style=\"font-family: vera; margin-bottom: 1.5em\">");
		for (String roleID : parent.getRoles().getRoleIDs()) {
			Role role = Game.ruleset.getRole(roleID);
			int level = parent.getRoles().getLevel(role);
			
			sb.append("<p>");
			sb.append("Level <span style=\"font-family: vera-italic;\">").append(level).append("</span> ");
			sb.append("<span style=\"font-family: vera-red;\">").append(role.getName()).append("</span>");
			sb.append("</p>");
		}

		int nextLevel = XP.getPointsForLevel(parent.stats().get(Stat.CreatureLevel) + 1);
		sb.append("<p><span style=\"font-family: vera-italic-blue\">");
		sb.append(parent.getExperiencePoints()).append("</span> / <span style=\"font-family: vera-italic-blue\">");
		sb.append(nextLevel).append("</span> XP</p>");
		sb.append("</div>");
		
		sb.append("<div style=\"font-family: vera; margin-bottom: 1em\"><table style=\"width: 22ex\">");
		sb.append("<tr><td style=\"width: 14 ex;\">Hit Points</td><td style=\"text-align: right\">");
		sb.append("<span style=\"font-family: vera-italic-green\">");
		sb.append(parent.getCurrentHP()).append("</span> / <span style=\"font-family: vera-italic-green\">");
		sb.append(parent.stats().get(Stat.MaxHP)).append("</span></td></tr>");
		
		sb.append("<tr><td style=\"width: 14 ex;\">Attack Cost</td><td style=\"text-align: right\">");
		sb.append("<span style=\"font-family: vera-italic-red\">");
		
		float attackCost = parent.stats().get(Stat.AttackCost) / 100.0f; 
		
		sb.append(Game.numberFormat(1).format(attackCost)).append("</span> AP</td></tr>");
		
		sb.append("<tr><td style=\"width: 14 ex;\">Movement Cost</td><td style=\"text-align: right\">");
		sb.append("<span style=\"font-family: vera-italic-blue\">");
		
		float movementCost = parent.stats().get(Stat.MovementCost) / 100.0f;
		
		sb.append(Game.numberFormat(1).format(movementCost)).append("</span> AP</td></tr>");
		
		sb.append("<tr><td style=\"width: 14 ex;\">Armor Class</td><td style=\"text-align: right\">");
		sb.append("<span style=\"font-family: vera-italic-blue\">");
		sb.append(parent.stats().get(Stat.ArmorClass)).append("</span></td></tr>");
		
		sb.append("</table></div>");
		

		Item mainHand = parent.getInventory().getEquippedItem(Inventory.EQUIPPED_MAIN_HAND);
		Item offHand = parent.getInventory().getEquippedItem(Inventory.EQUIPPED_OFF_HAND);
		if (mainHand == null) mainHand = parent.getRace().getDefaultWeapon();
		
		{
			sb.append("<div style=\"margin-bottom: 1em; font-family: vera;\"><p>Main hand</p>"); 
			sb.append("<div style=\"font-family: vera-italic-blue\">").append(mainHand.getName()).append("</div>");

			int quiverAttackBonus = 0;
			int quiverDamageBonus = 0;
			if (mainHand.getWeaponType() != Item.WeaponType.MELEE && mainHand.getWeaponType() != Item.WeaponType.THROWN) {
				Item quiver = parent.getInventory().getEquippedItem(Inventory.EQUIPPED_QUIVER);
				if (quiver != null && quiver.getWeaponType() == mainHand.getWeaponType()) {
					quiverAttackBonus = quiver.getQuality().getAttackBonus() + quiver.bonuses().get(Bonus.Type.WeaponAttack);
					quiverDamageBonus = quiver.getQuality().getDamageBonus() + quiver.bonuses().get(Bonus.Type.WeaponDamage);
				}
			}
			
			int attackBonus = parent.stats().get(Stat.MainHandAttackBonus) + parent.stats().get(Stat.LevelAttackBonus) +
				mainHand.bonuses().get(Bonus.Type.WeaponAttack) + mainHand.getQuality().getAttackBonus() +
				quiverAttackBonus + parent.stats().get(mainHand.getDamageType().getName(), Bonus.Type.AttackForWeaponType);
			
			sb.append("<p>Attack Bonus <span style=\"font-family: vera-green;\">").append(attackBonus).append("</span></p>");
			
			float damageMult = 1.0f + (float)( parent.stats().get(Stat.LevelDamageBonus) +  
					parent.stats().get(Stat.MainHandDamageBonus) + mainHand.getQuality().getDamageBonus() + 
					parent.stats().get(mainHand.getDamageType().getName(), Bonus.Type.DamageForWeaponType) +
					mainHand.bonuses().get(Bonus.Type.WeaponDamage) + quiverDamageBonus) / 100.0f;
			float damageMin = ((float)mainHand.getDamageMin() * damageMult);
			float damageMax = ((float)mainHand.getDamageMax() * damageMult);

			sb.append("<p>Damage <span style=\"font-family: vera-red;\">").append(Game.numberFormat(1).format(damageMin));
			sb.append("</span> to <span style=\"font-family: vera-red;\">");
			sb.append(Game.numberFormat(1).format(damageMax)).append("</span></p></div>");
		}
		
		if (offHand != null) {
			sb.append("<div style=\"margin-bottom: 1em; font-family: vera;\"><p>Off hand</p>");
			sb.append("<div style=\"font-family: vera-italic-blue\">").append(offHand.getName()).append("</div>");

			if (offHand.getItemType() == Item.ItemType.SHIELD) {
				String armorClass = Game.numberFormat(1).format(offHand.getQualityArmorClass());

				sb.append("<p>Armor Class <span style=\"font-family: vera-green;\">").append(armorClass);
				sb.append("</span></p>");
			} else if (offHand.getItemType() == Item.ItemType.WEAPON) {
				int attackBonus = parent.stats().get(Stat.OffHandAttackBonus) + parent.stats().get(Stat.LevelAttackBonus) +
				offHand.bonuses().get(Bonus.Type.WeaponAttack) + offHand.getQuality().getAttackBonus();

				sb.append("<p>Attack Bonus <span style=\"font-family: vera-green;\">").append(attackBonus).append("</span></p>");

				float damageMult = 1.0f + (float)( parent.stats().get(Stat.LevelDamageBonus) +
						parent.stats().get(Stat.OffHandDamageBonus) + offHand.getQuality().getDamageBonus() + 
						parent.stats().get(offHand.getDamageType().getName(), Bonus.Type.DamageForWeaponType) +
						offHand.bonuses().get(Bonus.Type.WeaponDamage) ) / 100.0f ;
				float damageMin = ((float)offHand.getDamageMin() * damageMult);
				float damageMax = ((float)offHand.getDamageMax() * damageMult);

				sb.append("<p>Damage <span style=\"font-family: vera-red;\">").append(Game.numberFormat(1).format(damageMin));
				sb.append("</span> to <span style=\"font-family: vera-red;\">");
				sb.append(Game.numberFormat(1).format(damageMax)).append("</span></p>");
			}

			sb.append("</div>");
		}
		
		sb.append("<div style=\"font-family: vera\"><table style=\"width: 22ex;\">"); {
			sb.append("<tr><td style=\"width: 10ex;\">").append("Strength</td><td style=\"width: 3ex; font-family: vera-blue;\">");
			sb.append(parent.stats().getBaseStr()).append("</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">");
			sb.append(parent.stats().get(Bonus.Type.Str)).append("</td><td style=\"width: 2ex;\">=</td>");
			sb.append("<td style=\"width: 3ex; font-family: vera-bold\">").append(parent.stats().getStr()).append("</td></tr>");
			
			sb.append("<tr><td style=\"width: 10ex;\">").append("Dexterity</td><td style=\"width: 3ex; font-family: vera-blue;\">");
			sb.append(parent.stats().getBaseDex()).append("</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">");
			sb.append(parent.stats().get(Bonus.Type.Dex)).append("</td><td style=\"width: 2ex;\">=</td>");
			sb.append("<td style=\"width: 3ex; font-family: vera-bold\">").append(parent.stats().getDex()).append("</td></tr>");
			
			sb.append("<tr><td style=\"width: 10ex;\">").append("Constitution</td><td style=\"width: 3ex; font-family: vera-blue;\">");
			sb.append(parent.stats().getBaseCon()).append("</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">");
			sb.append(parent.stats().get(Bonus.Type.Con)).append("</td><td style=\"width: 2ex;\">=</td>");
			sb.append("<td style=\"width: 3ex; font-family: vera-bold\">").append(parent.stats().getCon()).append("</td></tr>");
			
			sb.append("<tr><td style=\"width: 10ex;\">").append("Intelligence</td><td style=\"width: 3ex; font-family: vera-blue;\">");
			sb.append(parent.stats().getBaseInt()).append("</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">");
			sb.append(parent.stats().get(Bonus.Type.Int)).append("</td><td style=\"width: 2ex;\">=</td>");
			sb.append("<td style=\"width: 3ex; font-family: vera-bold\">").append(parent.stats().getInt()).append("</td></tr>");
			
			sb.append("<tr><td style=\"width: 10ex;\">").append("Wisdom</td><td style=\"width: 3ex; font-family: vera-blue;\">");
			sb.append(parent.stats().getBaseWis()).append("</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">");
			sb.append(parent.stats().get(Bonus.Type.Wis)).append("</td><td style=\"width: 2ex;\">=</td>");
			sb.append("<td style=\"width: 3ex; font-family: vera-bold\">").append(parent.stats().getWis()).append("</td></tr>");
			
			sb.append("<tr><td style=\"width: 10ex;\">").append("Charisma</td><td style=\"width: 3ex; font-family: vera-blue;\">");
			sb.append(parent.stats().getBaseCha()).append("</td><td style=\"width: 2ex;\">+</td><td style=\"width: 2ex;\">");
			sb.append(parent.stats().get(Bonus.Type.Cha)).append("</td><td style=\"width: 2ex;\">=</td>");
			sb.append("<td style=\"width: 3ex; font-family: vera-bold\">").append(parent.stats().getCha()).append("</td></tr>");

		} sb.append("</table></div>");
		
		sb.append("<table style=\"margin-top: 1em;\">");
		for (DamageType damageType : Game.ruleset.getAllDamageTypes()) {
			if (damageType.getName().equals(Game.ruleset.getString("PhysicalDamageType"))) continue;
			
			int reduction = parent.stats().getDamageReduction(damageType);
			int immunity = parent.stats().getDamageImmunity(damageType);
			
			if (reduction != 0) {
				sb.append("<tr><td style=\"width: 13ex;\"><span style=\"font-family: blue;\">");
				sb.append(damageType.getName()).append("</span>: ");
				sb.append("</td><td><span style=\"font-family: red;\">").append(reduction).append("</span>");
				sb.append(" Damage Reduction</td></tr>");
			}
			
			if (immunity != 0) {
				sb.append("<tr><td style=\"width: 13ex;\"><span style=\"font-family: blue;\">");
				sb.append(damageType.getName()).append("</span>: ");
				sb.append("</td><td><span style=\"font-family: red;\">").append(immunity).append("</span>");
				if (immunity > 0) sb.append("% Damage Immunity</td></tr>");
				else sb.append("% Damage Vulnerability</td></tr>");
			}
		}
		sb.append("</table>");
		
		sb.append("<div style=\"margin-top: 1em; font-family: black \"><table style=\"width: 30ex; \">");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: blue\">Level Attack Bonus</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Stat.LevelAttackBonus)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: blue\">Level Damage Bonus</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Stat.LevelDamageBonus)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: green\">Mental Resistance</td><td style=\"font-family: black\">");
		sb.append(parent.stats().getMentalResistance()).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: green\">Physical Resistance</td><td style=\"font-family: black\">");
		sb.append(parent.stats().getPhysicalResistance()).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: green\">Reflex Resistance</td><td style=\"font-family: black\">");
		sb.append(parent.stats().getReflexResistance()).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: blue\">Attacks of Opportunity</td><td style=\"font-family: black\">");
		sb.append(parent.stats().getAttacksOfOpportunity()).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: red\">Touch Armor Class</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Stat.TouchArmorClass)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: red\">Armor Penalty</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Stat.ArmorPenalty)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: red\">Shield Attack Penalty</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Stat.ShieldAttackPenalty)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: red\">Touch Attack Bonus</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Stat.TouchAttackBonus)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: blue\">Initiative Bonus</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Stat.InitiativeBonus)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 25ex; font-family: blue\">Concealment</td><td style=\"font-family: black\">");
		sb.append(parent.stats().get(Bonus.Type.Concealment)).append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 10ex; font-family: blue\">").append("Spell Resistance</td><td style=\"font-family: black\">");
		int spellResistance = Math.min(100, Math.max(0, parent.stats().get(Bonus.Type.SpellResistance)) );
		sb.append(spellResistance);
		sb.append("</td></tr>");
		
		sb.append("</table></div>");
		
		synchronized(parent.getEffects()) {
			for (Effect effect : parent.getEffects()) {
				effect.appendDescription(sb);
			}
		}
		
		textAreaModel.setHtml(sb.toString());
		invalidateLayout();
	}
	
	private class Content extends Widget {
		@Override public int getPreferredWidth() {
			return textArea.getPreferredWidth();
		}
		
		@Override public int getPreferredHeight() {
			return textArea.getPreferredHeight();
		}
		
		@Override protected void layout() {
			super.layout();
			
			this.layoutChildFullInnerArea(textArea);
			
			if (viewer != null) {
				viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
				viewer.setPosition(textArea.getInnerX() + 160, textArea.getInnerY());
			}
		}
	}
}
