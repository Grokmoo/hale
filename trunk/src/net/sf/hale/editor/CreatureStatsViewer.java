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

package net.sf.hale.editor;

import net.sf.hale.bonus.Bonus;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.widgets.ExpandableScrollPane;

import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

public class CreatureStatsViewer extends ExpandableScrollPane {

	private Creature creature;

	private final TextArea textArea;
	private final HTMLTextAreaModel textAreaModel;
	private StringBuilder textAreaContent;

	public CreatureStatsViewer() {
		super();

		this.setTheme("/scrollpane");
		this.setFixed(ScrollPane.Fixed.HORIZONTAL);
		this.setCanAcceptKeyboardFocus(false);

		textAreaModel = new HTMLTextAreaModel();
		textArea = new TextArea(textAreaModel);
		this.setContent(textArea);
	}

	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible && creature != null) {
			this.setCreature(creature);
		}
	}

	public void setCreature(Creature creature) {
		this.creature = creature;

		creature.stats().recomputeAllStats();

		textAreaContent = new StringBuilder();
		textAreaContent.append("<html><body>");

		textAreaContent.append("<table><tr><td style=\"vertical-align: top; \">");

		textAreaContent.append("<p>AC: " + creature.stats().get(Stat.ArmorClass) + "</p>");
		textAreaContent.append("<p>Max HP: " + creature.stats().get(Stat.MaxHP) + "</p>");
		textAreaContent.append("<p>Attack Bonus from Levels: " + creature.stats().get(Stat.LevelAttackBonus) + "</p>");
		textAreaContent.append("<p>Damage Bonus from Levels: " + creature.stats().get(Stat.LevelDamageBonus) + "</p>");
		textAreaContent.append("<p>Attack Cost: " + (creature.stats().get(Stat.AttackCost) / 100) + " AP</p>");

		Item mainHand = creature.getInventory().getEquippedItem(Inventory.EQUIPPED_MAIN_HAND);
		Item offHand = creature.getInventory().getEquippedItem(Inventory.EQUIPPED_OFF_HAND);

		if (mainHand == null) mainHand = creature.getRace().getDefaultWeapon();

		{   textAreaContent.append("<div style=\"margin-top: 1em; \"><p>Main hand: "); 
		textAreaContent.append(mainHand.getName() + "</p>");

		int quiverAttackBonus = 0;
		int quiverDamageBonus = 0;
		if (mainHand.getWeaponType() != Item.WeaponType.MELEE && mainHand.getWeaponType() != Item.WeaponType.THROWN) {
			Item quiver = creature.getInventory().getEquippedItem(Inventory.EQUIPPED_QUIVER);
			if (quiver != null && quiver.getWeaponType() == mainHand.getWeaponType()) {
				quiverAttackBonus = quiver.getQuality().getAttackBonus() + quiver.bonuses().get(Bonus.Type.WeaponAttack);
				quiverDamageBonus = quiver.getQuality().getDamageBonus() + quiver.bonuses().get(Bonus.Type.WeaponDamage);
			}
		}

		int attackBonus = creature.stats().get(Stat.MainHandAttackBonus) + creature.stats().get(Stat.LevelAttackBonus) +
		mainHand.bonuses().get(Bonus.Type.WeaponAttack) + mainHand.getQuality().getAttackBonus() +
		quiverAttackBonus + creature.stats().get(mainHand.getDamageType().getName(), Bonus.Type.AttackForWeaponType);

		textAreaContent.append("<p>Attack Bonus: " + attackBonus + "</p>");

		float damageMult = 1.0f + (float)(creature.stats().get(Stat.LevelDamageBonus) + 
				creature.stats().get(Stat.MainHandDamageBonus) + mainHand.getQuality().getDamageBonus() + 
				creature.stats().get(mainHand.getDamageType().getName(), Bonus.Type.DamageForWeaponType) +
				mainHand.bonuses().get(Bonus.Type.WeaponDamage) + quiverDamageBonus ) / 100.0f;
		int damageMin = (int) ((float)mainHand.getDamageMin() * damageMult);
		int damageMax = (int) ((float)mainHand.getDamageMax() * damageMult);

		textAreaContent.append("<p>Damage: " + damageMin + " to " + damageMax + "</p>");
		textAreaContent.append("</div>");
		}

		if (offHand != null) {
			textAreaContent.append("<div style=\"margin-top: 1em; \"><p>Off hand: ");
			textAreaContent.append(offHand.getName() + "</p>");

			if (offHand.getItemType() == Item.ItemType.SHIELD) {
				textAreaContent.append("<p>AC: " + offHand.getArmorClass() + "</p>");
				textAreaContent.append("<p>Armor Penalty: " + offHand.getArmorPenalty() + "</p>");
				textAreaContent.append("<p>Shield Attack Penalty: " + offHand.getShieldAttackPenalty() + "</p>");
			} else if (offHand.getItemType() == Item.ItemType.WEAPON) {
				int attackBonus = creature.stats().get(Stat.OffHandAttackBonus) + creature.stats().get(Stat.LevelAttackBonus) +
				offHand.bonuses().get(Bonus.Type.WeaponAttack) + offHand.getQuality().getAttackBonus();

				textAreaContent.append("<p>Attack Bonus: " + attackBonus + "</p>");

				float damageMult = 1.0f + (float)(creature.stats().get(Stat.OffHandDamageBonus) + offHand.getQuality().getDamageBonus() + 
						creature.stats().get(offHand.getDamageType().getName(), Bonus.Type.DamageForWeaponType) +
						offHand.bonuses().get(Bonus.Type.WeaponDamage) ) / 100.0f ;
				int damageMin = (int) ((float)offHand.getDamageMin() * damageMult);
				int damageMax = (int) ((float)offHand.getDamageMax() * damageMult);

				textAreaContent.append("<p>Damage: " + damageMin + " to " + damageMax + "</p>");
			}

			textAreaContent.append("</div>");
		}

		textAreaContent.append("</td></tr></table>");

		textAreaContent.append("</body></html>");

		textAreaModel.setHtml(textAreaContent.toString());
		this.validateLayout();
	}
}
