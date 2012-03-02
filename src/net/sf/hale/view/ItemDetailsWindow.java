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

import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.entity.Enchantment;
import net.sf.hale.entity.EntityViewer;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.StringUtil;
import net.sf.hale.widgets.IconViewer;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget for displaying details about a specific Item
 * in a single window.
 * 
 * @author Jared Stephen
 *
 */

public class ItemDetailsWindow extends GameSubWindow implements EntityViewer {
	private Item item;
	private HTMLTextAreaModel textAreaModel;
	
	/**
	 * Create a new ItemDetailsWindow that shows the details for
	 * the specified Item
	 * 
	 * @param item the Item to show details for
	 */
	
	public ItemDetailsWindow(Item item) {
		setTitle("Details for " + item.getName());
		item.addViewer(this);
		this.item = item;
        
        DialogLayout layout = new DialogLayout();
		layout.setTheme("content");
		this.add(layout);
		
		// set up the widgets for the top row
		String titleString = item.getFullName();
		
		IconViewer iconViewer = new IconViewer();
		iconViewer.setEventHandlingEnabled(false);
		if (item.getIcon() != null) {
			iconViewer.setSprite(SpriteManager.getSprite(item.getIcon()));
			iconViewer.setColor(item.getIconColor());
		}
		Label title = new Label(titleString);
		title.setTheme("titlelabel");
		
		DialogLayout.Group topRowV = layout.createParallelGroup(iconViewer, title);
		
		DialogLayout.Group topRowH = layout.createSequentialGroup(iconViewer);
		topRowH.addGap(10);
		topRowH.addWidget(title);
		topRowH.addGap(10);
		
		// create widgets for details text area
		textAreaModel = new HTMLTextAreaModel();
        TextArea textArea = new TextArea(textAreaModel);
        ScrollPane textPane = new ScrollPane(textArea);
        textPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        textPane.setTheme("detailspane");
        
        // set the main top level layout
        Group mainGroupV = layout.createSequentialGroup();
		mainGroupV.addGroup(topRowV);
		mainGroupV.addGap(5);
		mainGroupV.addWidget(textPane);
		
		Group mainGroupH = layout.createParallelGroup();
		mainGroupH.addGroup(topRowH);
		mainGroupH.addWidget(textPane);
		
		layout.setHorizontalGroup(mainGroupH);
		layout.setVerticalGroup(mainGroupV);
		
		entityUpdated();
	}
	
	@Override public void closeViewer() {
		getParent().removeChild(this);
	}
	
	@Override public void entityUpdated() {
		textAreaModel.setHtml(getTextAreaContent(item));
		
		invalidateLayout();
	}
	
	/*
	 * This overrides the default close behavior of GameSubWindow
	 * @see net.sf.hale.view.GameSubWindow#run()
	 */
	
	@Override public void run() {
		closeViewer();
		item.removeViewer(this);
	}
	
	private String getTextAreaContent(Item item) {
		StringBuilder sb = new StringBuilder();
		
		this.appendItemTypeString(item, sb);
		
		switch (item.getItemType()) {
		case ARMOR: case SHIELD: case GLOVES: case BOOTS: case HELMET:
			appendArmorString(item, sb);
			break;
		case WEAPON:
			appendWeaponString(item, sb);
			break;
		}
		
		switch (item.getType()) {
		case ITEM: case TRAP:
			appendItemString(item, sb);
			break;
		}
		
		synchronized(item.getEffects()) {
			for (Effect effect : item.getEffects()) {
				effect.appendDescription(sb);
			}
		}
		
		List<Enchantment> enchantments = item.getEnchantments();
		if (enchantments.size() > 0) {
			sb.append("<div style=\"margin-bottom: 1em;\">");
			sb.append("<span style=\"font-family: vera-blue;\">Enchantments</span>");
			for (Enchantment enchantment : enchantments) {
				sb.append(enchantment.getBonuses().getDescription());
			}
			sb.append("</div>");
		}
		
		if (!item.getDescription().equals(Game.ruleset.getString("TempContainerDescription"))) {
			sb.append("<div style=\"margin-top: 1em;\">");
			sb.append(item.getDescription());
			sb.append("</div>");
		}
		
		return sb.toString();
	}

	private void appendItemString(Item item, StringBuilder sb) {
		sb.append("<table style=\"font-family: vera; vertical-align: middle; margin-bottom: 1em;\">");
		
		if (item.hasQuality()) {
			sb.append("<tr><td style=\"width: 10ex;\">");
			sb.append("Quality");
			sb.append("</td><td style=\"font-family: vera-red\">");
			sb.append(item.getQuality().getName());
			sb.append("</td></tr>");
		}
		
		sb.append("<tr><td style=\"width: 10ex;\">");
		sb.append("Value");
		sb.append("</td><td>");
		sb.append("<span style=\"font-family: vera-green\">");
		sb.append(item.getQualityValue().shortString());
		sb.append("</span>");
		if (item.getValueStackSize() != 1) {
			sb.append(" per ");
			sb.append("<span style=\"font-family: vera-green\">");
			sb.append(item.getValueStackSize());
			sb.append("</span>");
		}
		sb.append("</td></tr>");
		
		sb.append("<tr><td style=\"width: 10ex;\">");
		sb.append("Weight");
		sb.append("</td><td>");
		sb.append("<span style=\"font-family: vera-blue\">");
		sb.append(item.getWeight().toStringKilograms());
		sb.append("</span>");
		sb.append(" kg");
		sb.append("</td></tr>");
		
		sb.append("</table>");
	}
	
	private void appendArmorString(Item item, StringBuilder sb) {
		String armorClass = Game.numberFormat(1).format(item.getQualityArmorClass());
		String armorPenalty = Game.numberFormat(1).format(item.getQualityArmorPenalty());
		String movementPenalty = Game.numberFormat(1).format(item.getQualityMovementPenalty());
		
		sb.append("<table style=\"font-family: vera; vertical-align: middle; margin-bottom: 1em;\">");
		
		sb.append("<tr><td style=\"width: 16ex;\">");
		sb.append("Armor Class");
		sb.append("</td><td style=\"font-family: vera-blue\">");
		sb.append(armorClass);
		sb.append("</td></tr>");
		
		sb.append("<tr><td>");
		sb.append("Armor Penalty");
		sb.append("</td><td style=\"font-family: vera-red\">");
		sb.append(armorPenalty);
		sb.append("</td></tr>");
		
		if (item.getMovementPenalty() != 0) {
			sb.append("<tr><td>");
			sb.append("Movement Penalty");
			sb.append("</td><td style=\"font-family: vera-red\">");
			sb.append(movementPenalty);
			sb.append("</td></tr>");
		}
		
		if (item.getItemType() == Item.ItemType.SHIELD) {
			sb.append("<tr><td>");
			sb.append("Attack Penalty");
			sb.append("</td><td style=\"font-family: vera-red\">");
			sb.append(item.getShieldAttackPenalty());
			sb.append("</td></tr>");
		}
		
		sb.append("</table>");
	}
	
	private void appendWeaponString(Item item, StringBuilder sb) {
		float damageMult = 1.0f + (float)(item.getQuality().getDamageBonus()) / 100.0f;

		String damageMin = Game.numberFormat(1).format(((float)item.getDamageMin() * damageMult));
		String damageMax = Game.numberFormat(1).format(((float)item.getDamageMax() * damageMult));

		sb.append("<table style=\"font-family: vera; vertical-align: middle; margin-bottom: 1em;\">");
		
		sb.append("<tr><td style=\"width: 13ex;\">");
		sb.append("Damage");
		sb.append("</td><td>");
		sb.append("<span style=\"font-family: vera-red\">");
		sb.append(damageMin);
		sb.append("</span>");
		sb.append(" to ");
		sb.append("<span style=\"font-family: vera-red\">");
		sb.append(damageMax);
		sb.append("</span>");
		sb.append("</td></tr>");
		
		sb.append("<tr><td>");
		sb.append("Damage Type");
		sb.append("</td><td style=\"font-family: vera-blue\">");
		sb.append(item.getDamageType().getName());
		sb.append("</td></tr>");
		
		sb.append("<tr><td>");
		sb.append("Critical");
		sb.append("</td><td>");
		sb.append("<span style=\"font-family: vera-green\">");
		if (item.getCriticalThreatRange() == 100) sb.append("100");
		else sb.append(item.getCriticalThreatRange()).append(" - 100");
		sb.append("</span>");
		sb.append(" / x");
		sb.append("<span style=\"font-family: vera-blue\">");
		sb.append(item.getCriticalMultiplier());
		sb.append("</span>");
		sb.append("</td></tr>");

		sb.append("<tr><td>");
		sb.append("Attack Cost");
		sb.append("</td><td>");
		sb.append("<span style=\"font-family: vera-blue\">");
		sb.append(item.getAttackCost() / 100);
		sb.append("</span>");
		sb.append(" AP");
		sb.append("</td></tr>");
		
		if (item.getMaxStrengthBonus() != 0) {
			sb.append("<tr><td>");
			sb.append("Max Str Bonus");
			sb.append("</td><td style=\"font-family: vera-blue\">");
			sb.append(item.getMaxStrengthBonus());
			sb.append("</td></tr>");
		}
		
		if (item.getRangePenalty() != 0) {
			String rangePenalty = Game.numberFormat(1).format(((float)item.getRangePenalty() / 20.0f));
			
			sb.append("<tr><td>");
			sb.append("Range Penalty");
			sb.append("</td><td>");
			sb.append("<span style=\"font-family: vera-blue\">");
			sb.append(rangePenalty);
			sb.append("</span>");
			sb.append(" per 5 feet");
			sb.append("</td></tr>");
		}
		
		sb.append("</table>");
	}
	
	private void appendItemTypeString(Item item, StringBuilder sb) {
		switch (item.getItemType()) {
		case WEAPON:
			String weaponType = null;
			switch (item.getWeaponType()) {
			case MELEE:
				weaponType = "Melee";
				break;
			case BOW: case CROSSBOW: case SLING:
				weaponType = "Ranged";
				break;
			case THROWN:
				weaponType = "Thrown";
				break;
			}

			sb.append("<div style=\"font-family: vera; margin-bottom: 1em;\">");
			sb.append("<span style=\"font-family: vera-blue\">");
			sb.append( StringUtil.upperCaseToWord(item.getWeaponHanded().toString()) );
			sb.append("</span> <span style=\"font-family: vera-red\">");
			sb.append(weaponType);
			sb.append("</span> ");
			sb.append( StringUtil.upperCaseToWord(item.getItemType().toString()) );
			sb.append("</div>");
			break;
		case ARMOR: case GLOVES: case BOOTS: case HELMET:
			if (!item.getArmorType().getName().equals(Game.ruleset.getString("DefaultArmorType"))) {
				sb.append("<div style=\"font-family: vera; margin-bottom: 1em;\">");
				sb.append("<span style=\"font-family: vera-blue\">");
				sb.append(item.getArmorType().getName());
				sb.append("</span> ");
				sb.append( StringUtil.upperCaseToWord(item.getItemType().toString()) );
				sb.append("</div>");
				break;
			}
		}
	}
}
