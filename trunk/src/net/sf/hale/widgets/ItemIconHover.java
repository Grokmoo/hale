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

package net.sf.hale.widgets;

import net.sf.hale.Game;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Enchantment;
import net.sf.hale.entity.Item;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A tooltip for an item icon, showing basic information about the item
 * @author Jared Stephen
 *
 */

public class ItemIconHover extends TextArea {
	private HTMLTextAreaModel textAreaModel;
	
	private ItemIconViewer parent;
	private Item item;
	
	private String emptyHoverText;
	private String valueDescription;
	private int valuePercentage;
	
	private String requiresText;
	
	/**
	 * Creates a new ItemIconTooltip
	 * @param item the item that this hover will display information for
	 * @param hoverSource the parent widget that created this hover
	 */
	
	public ItemIconHover(Item item, ItemIconViewer hoverSource) {
		this.textAreaModel = new HTMLTextAreaModel();
		this.setModel(textAreaModel);
		
		valueDescription = "Value";
		valuePercentage = 100;
		
		this.parent = hoverSource;
		this.item = item;
	}
	
	@Override public int getPreferredWidth() {
		// the + 1 here prevents wrap around that sometimes occurs; most likely a bug with
		// de.matthiasmann.twl.TextArea
		return super.getPreferredInnerWidth() + getBorderHorizontal() + 1;
	}
	
	/**
	 * Sets the text specifying the missing requirement for this item
	 * @param text the text
	 */
	
	public void setRequiresText(String text) {
		this.requiresText = text;
	}
	
	/**
	 * Set the text that is displayed when this hover is not displaying
	 * information on an item
	 * @param text the text to display
	 */
	
	public void setEmptyHoverText(String text) {
		this.emptyHoverText = text;
	}
	
	/**
	 * Returns the widget responsible for the creation of this hover
	 * @return the hover source widget
	 */
	
	public Widget getHoverSource() {
		return parent;
	}
	
	/**
	 * Sets the value being displayed by this ItemIconTooltip
	 * @param description the label for the value
	 * @param percentage the percentage multiplier for the value being displayed
	 */
	
	public void setValue(String description, int percentage) {
		this.valueDescription = description;
		this.valuePercentage = percentage;
	}
	
	@Override protected boolean handleEvent(Event evt) {
		// don't swallow any events
		return false;
	}
	
	/**
	 * Sets the text being displayed by this tooltip
	 */
	
	public void updateText() {
		StringBuilder sb = new StringBuilder();
		
		if (item == null) {
			appendEmptyText(sb);
		} else {
			appendItemText(sb);
		}
		
		textAreaModel.setHtml(sb.toString());
	}
	
	private void appendEmptyText(StringBuilder sb) {
		sb.append("<div style=\"font-family: vera-bold;\">");
		sb.append(emptyHoverText);
		sb.append("</div>");
	}
	
	private void appendItemText(StringBuilder sb) {
		sb.append("<div style=\"font-family: vera-bold;\">");
		
		sb.append(item.getFullName());
		sb.append("</div>");
		
		if (requiresText != null) {
			sb.append("<div style=\"font-family: red\">");
			sb.append("Requires ");
			sb.append(requiresText);
			sb.append("</div>");
		}
		
		sb.append(valueDescription);
		sb.append(": <span style=\"font-family: blue;\">");
		sb.append(item.getQualityValue().shortString(valuePercentage));
		sb.append("</span>");
		
		switch (item.getItemType()) {
		case WEAPON:
			sb.append("<div>Base Damage: ");
			
			float damageMult = 1.0f + (float)( item.getQuality().getDamageBonus() +
					item.bonuses().get(Bonus.Type.WeaponDamage)) / 100.0f;
			float damageMin = ((float)item.getDamageMin() * damageMult);
			float damageMax = ((float)item.getDamageMax() * damageMult);
			
			sb.append("<span style=\"font-family: red;\">");
			sb.append(Game.numberFormat(1).format(damageMin));
			sb.append("</span>");
			
			sb.append(" to <span style=\"font-family: red;\">");
			sb.append(Game.numberFormat(1).format(damageMax));
			sb.append("</span>");
			
			sb.append("</div>");
			break;
		case ARMOR: case BOOTS: case GLOVES: case HELMET: case SHIELD:
			sb.append("<div>Armor Class <span style=\"font-family: green;\">");
			sb.append(Game.numberFormat(1).format(item.getQualityArmorClass()));
			sb.append("</span></div>");
			
			break;
		}
		
		if (item.isQuestItem()) {
			sb.append("<div style=\"font-family: green\">Quest Item</div>");
		}
		
		for (Enchantment enchantment : item.getEnchantments()) {
			sb.append("<div>");
			sb.append(enchantment.getBonuses().getDescription());
			sb.append("</div>");
		}
	}
}