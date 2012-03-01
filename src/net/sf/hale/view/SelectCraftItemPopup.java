/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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
import net.sf.hale.Recipe;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.ItemList;
import net.sf.hale.widgets.IconViewer;
import net.sf.hale.widgets.TextAreaNoInput;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * The popup used to select an item when crafting an item where the result is one
 * of the ingredients (i.e. enchantments)
 * @author Jared
 *
 */

public class SelectCraftItemPopup extends PopupWindow {
	private ItemViewer selectedItemViewer;
	
	private Recipe recipe;
	private Content content;
	
	private ScrollPane scrollPane;
	private DialogLayout scrollPaneContent;
	
	private Label title, recipeName;
	private Button cancel, accept;
	
	/**
	 * Creates a new PopupWindow with the specified parent
	 * @param parent the parent widget
	 */
	
	public SelectCraftItemPopup(Widget parent, Recipe recipe) {
		super(parent);
		
		this.recipe = recipe;
		
		this.setCloseOnEscape(false);
		this.setCloseOnClickedOutside(false);
		
		content = new Content();
		add(content);
		
		String skill = Game.ruleset.getSkill(recipe.getSkill()).getVerb();
		String titleText = "Select an Item to " + skill;

		title = new Label(titleText);
		title.setTheme("titlelabel");
		content.add(title);
		
		recipeName = new Label("Recipe: " + recipe.getName());
		recipeName.setTheme("recipelabel");
		content.add(recipeName);
		
		cancel = new Button();
		cancel.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
			}
		});
		cancel.setTheme("cancelbutton");
		content.add(cancel);
		
		accept = new Button();
		
		accept.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
				SelectCraftItemPopup.this.recipe.craft(selectedItemViewer.item,
						selectedItemViewer.parent, selectedItemViewer.equipped);
			}
		});
		accept.setTheme("acceptbutton");
		content.add(accept);

		scrollPaneContent = new DialogLayout();
		scrollPaneContent.setTheme("content");
		scrollPane = new ScrollPane(scrollPaneContent);
		scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		content.add(scrollPane);
		
		DialogLayout.Group mainH = scrollPaneContent.createParallelGroup();
		DialogLayout.Group mainV = scrollPaneContent.createSequentialGroup();
		
		populateItemsList(mainH, mainV);
		
		scrollPaneContent.setHorizontalGroup(mainH);
		scrollPaneContent.setVerticalGroup(mainV);
		
		setCraftEnabled();
	}
	
	private void setCraftEnabled() {
		accept.setEnabled(selectedItemViewer != null);
	}
	
	private void populateItemsList(DialogLayout.Group mainH, DialogLayout.Group mainV) {
		for (Creature creature : Game.curCampaign.party) {
			Inventory inventory = creature.getInventory();
			
			// check equipped items
			for (int i = 0; i < Inventory.EQUIPPED_SIZE; i++) {
				Item item = inventory.getEquippedItem(i);
				
				if (item == null) continue;
				
				// don't allow enchanting already enchanted items
				if (item.getEnchantments().size() > 0) continue;
				
				if (item.getItemType() == recipe.getAnyItemOfTypeIngredient()) {
					ItemViewer viewer = new ItemViewer(item, creature, true);
					
					mainH.addWidget(viewer);
					mainV.addWidget(viewer);
				}
			}
			
			ItemList unequipped = inventory.getUnequippedItems();
			// check unequipped items
			for (int i = 0; i < unequipped.size(); i++) {
				Item item = unequipped.getItem(i);
				
				if (item.getItemType() == recipe.getAnyItemOfTypeIngredient()) {
					ItemViewer viewer = new ItemViewer(item, creature, false);
					
					mainH.addWidget(viewer);
					mainV.addWidget(viewer);
				}
			}
		}
	}
	
	private class Content extends Widget {
		private int gap;
		private int maxHeight;
		
		@Override protected void applyTheme(ThemeInfo themeInfo) {
			super.applyTheme(themeInfo);
			
			this.gap = themeInfo.getParameter("gap", 0);
			this.maxHeight = themeInfo.getParameter("maxHeight", 0);
		}
		
		@Override protected void layout() {
			int centerX = getInnerX() + getInnerWidth() / 2;
			
			title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
			title.setPosition(centerX - title.getWidth() / 2, getInnerY());
			
			recipeName.setSize(recipeName.getPreferredWidth(), recipeName.getPreferredHeight());
			recipeName.setPosition(centerX - recipeName.getWidth() / 2, title.getBottom());
			
			accept.setSize(accept.getPreferredWidth(), accept.getPreferredHeight());
			cancel.setSize(cancel.getPreferredWidth(), cancel.getPreferredHeight());
			
			accept.setPosition(centerX - accept.getWidth() - gap, getInnerBottom() - accept.getHeight());
			cancel.setPosition(centerX + gap, getInnerBottom() - cancel.getHeight());
			
			scrollPane.setSize(getInnerWidth(), getInnerHeight() - recipeName.getHeight() -
					title.getHeight() - accept.getHeight() - 2 * gap);
			scrollPane.setPosition(getInnerX(), recipeName.getBottom() + gap);
		}
		
		@Override public int getPreferredWidth() {
			int width = Math.max(title.getPreferredWidth(), scrollPane.getPreferredWidth());
			
			return width + getBorderHorizontal();
		}
		
		@Override public int getPreferredHeight() {
			int height = title.getPreferredHeight() + 2 * gap + recipeName.getPreferredHeight() + 
					scrollPane.getPreferredHeight() + accept.getPreferredHeight();
			
			return Math.min(maxHeight, height + getBorderVertical());
		}
	}
	
	private class ItemViewer extends ToggleButton implements Runnable {
		private IconViewer viewer;
		private TextArea textArea;
		
		private boolean equipped;
		private Creature parent;
		private Item item;
		
		private ItemViewer(Item item, Creature parent, boolean equipped) {
			this.item = item;
			this.parent = parent;
			this.equipped = equipped;
			
			addCallback(this);
			
			viewer = new IconViewer(SpriteManager.getSprite(item.getIcon()));
			viewer.setEventHandlingEnabled(false);
			viewer.setColor(item.getIconColor());
			add(viewer);
			
			HTMLTextAreaModel model = new HTMLTextAreaModel();
			
			StringBuilder sb = new StringBuilder();
			appendItemText(sb);
			
			model.setHtml(sb.toString());
			
			textArea = new TextAreaNoInput(model);
			add(textArea);
		}
		
		@Override public void run() {
			if (selectedItemViewer != null)
				selectedItemViewer.setActive(false);
			
			this.setActive(true);
			selectedItemViewer = this;
			
			setCraftEnabled();
		}
		
		private void appendItemText(StringBuilder sb) {
			sb.append("<div style=\"font-family: vera-bold;\">");
			
			sb.append(item.getFullName());
			sb.append("</div>");
			
			sb.append("Value: <span style=\"font-family: blue;\">");
			sb.append(item.getQualityValue().shortString(100));
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
			
			sb.append("<div>");
			if (equipped) {
				sb.append("<span style=\"font-family: blue\">Equipped</span> by ");
			} else
				sb.append("Owned by ");
				
			sb.append("<span style=\"font-family: green\">");
			sb.append(parent.getName());
			sb.append("</span></div>");
		}
		
		@Override protected void layout() {
			viewer.setSize(viewer.getPreferredWidth(), viewer.getPreferredHeight());
			viewer.setPosition(getInnerX(), getInnerY() + getInnerHeight() / 2 - viewer.getHeight() / 2);
			
			textArea.setPosition(viewer.getRight(), getInnerY());
			textArea.setSize(Math.max(0, getInnerWidth() - viewer.getWidth()), getInnerHeight());
		}
		
		@Override public int getPreferredHeight() {
			return Math.max(viewer.getPreferredHeight(), textArea.getPreferredInnerHeight() +
					textArea.getBorderVertical()) + getBorderVertical();
		}
	}
}
