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

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.Recipe;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Skill;
import net.sf.hale.util.StringUtil;
import net.sf.hale.widgets.ExpandableWidget;
import net.sf.hale.widgets.IconViewer;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.ToggleButtonModel;

/**
 * A widget for viewing the set of recipes available to the current party.
 * Contained within the LogWindow.
 * @author Jared Stephen
 *
 */

// allow filtering on recipe sub category - light, medium, heavy, shields for Craft Armor, for example

public class RecipeSetViewer extends Widget {
	private boolean showCraftButtons;
	
	private int currentSkillBestRanks;
	private Skill currentSkill;
	
	private CraftSkillIconButton activeButton;
	private List<CraftSkillIconButton> skillButtons;
	private List<RecipeViewer> viewers;
	
	private int scrollPaneOverlap;
	private ScrollPane pane;
	private DialogLayout paneContent;
	
	/**
	 * Creates a new empty RecipeViewer widget.  The content must be first
	 * set with {@link #updateContent()}
	 */
	
	public RecipeSetViewer(boolean showCraftButtons) {
		this.showCraftButtons = showCraftButtons;
		
		viewers = new ArrayList<RecipeViewer>();
		
		paneContent = new DialogLayout();
		paneContent.setTheme("content");
		paneContent.setIncludeInvisibleWidgets(false);
		pane = new ScrollPane(paneContent);
		pane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		add(pane);
		
		skillButtons = new ArrayList<CraftSkillIconButton>();
		for (Skill skill : Game.ruleset.getAllSkills()) {
			if (!skill.isCraftSkill()) continue;
			
			CraftSkillIconButton button = new CraftSkillIconButton(skill);
			button.setTooltipContent(skill.getName());
			skillButtons.add(button);
			add(button);
		}
		
		// initially set the first button active
		activeButton = skillButtons.get(0);
		skillButtons.get(0).getModel().setSelected(true);
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		scrollPaneOverlap = themeInfo.getParameter("scrollPaneOverlap", 0);
	}
	
	@Override protected void layout() {
		super.layout();
		
		// set size and compute total button bar width
		int buttonsWidth = 0;
		for (CraftSkillIconButton button : skillButtons) {
			button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
			buttonsWidth += button.getWidth();
		}
		
		// layout centered skill button bar
		int maxBottom = getInnerY();
		int curX = getInnerX() + getInnerWidth() / 2 - buttonsWidth / 2;
		for (CraftSkillIconButton button : skillButtons) {
			button.setPosition(curX, getInnerY());
			curX = button.getRight();
			maxBottom = Math.max(maxBottom, button.getBottom());
		}
		
		// set pane to overlap its border with the tab buttons to get the appearance
		// of a tabbed pane
		pane.setPosition(getInnerX(), maxBottom - scrollPaneOverlap);
		pane.setSize(getInnerWidth(), getInnerBottom() - pane.getY());
	}
	
	/**
	 * Updates the list of valid recipes shown with the available recipes to the
	 * current party
	 */
	
	public void updateContent() {
		if (activeButton.skill != currentSkill) {
			currentSkill = activeButton.skill;
			
			viewers.clear();
			paneContent.removeAllChildren();
			DialogLayout.Group mainH = paneContent.createParallelGroup();
			DialogLayout.Group mainV = paneContent.createSequentialGroup();
			
			for (String recipeID : Game.curCampaign.getRecipeIDsForSkill(currentSkill)) {
				Recipe recipe = Game.curCampaign.getRecipe(recipeID);
				String itemID = recipe.getResult();
				String icon = itemID != null ? Game.entityManager.getItem(itemID).getIcon() : null;
				
				RecipeViewer viewer = new RecipeViewer(recipe, icon, showCraftButtons);
				
				viewers.add(viewer);
				mainH.addWidget(viewer);
				mainV.addWidget(viewer);
			}
			
			paneContent.setHorizontalGroup(mainH);
			paneContent.setVerticalGroup(mainV);
		}
		
		// update existing viewers
		
		currentSkillBestRanks = Game.curCampaign.getBestPartySkillRanks(currentSkill.getID());
		for (RecipeViewer viewer : viewers) {
			viewer.update();
		}
	}
	
	private class RecipeViewer extends ExpandableWidget implements Runnable {
		private final Recipe recipe;
		private final boolean showCraftButton;
		
		private Button craft;
		
		private RecipeViewer(Recipe recipe, String icon, boolean showCraftButton) {
			super(icon);
			this.showCraftButton = showCraftButton;
			this.recipe = recipe;
			
			craft = new Button();
			craft.addCallback(this);
			craft.setTheme("craftbutton");
			if (showCraftButton)
				add(craft);
		}
		
		// craft callback
		@Override public void run() {
			if (recipe.resultIsIngredient()) {
				SelectCraftItemPopup popup = new SelectCraftItemPopup(Game.mainViewer, recipe);
				popup.openPopupCentered();
			} else {
				recipe.craft();
			}
			
			Game.mainViewer.updateInterface();
		}
		
		@Override protected void layout() {
			super.layout();
			
			craft.setSize(craft.getPreferredWidth(), craft.getPreferredHeight());
			craft.setPosition(getTextArea().getInnerRight() - craft.getWidth(), getTextArea().getInnerY());
		}
		
		@Override public int getPreferredHeight() {
			return Math.max(super.getPreferredHeight(), getBorderVertical() + getTextArea().getBorderTop() +
					craft.getPreferredHeight() + getButtonHeight());
		}
		
		@Override public void update() {
			super.update();
			
			// hide recipes where we are no where close to the skill requirement
			setVisible(currentSkillBestRanks >= recipe.getSkillRequirement() - Game.ruleset.getValue("RecipeVisibleSkillThreshold"));
			
			if (showCraftButton) {
				craft.setEnabled(recipe.canCraft() && currentSkillBestRanks >= recipe.getSkillRequirement());
			}
		}
		
		@Override protected void appendDescriptionMain(StringBuilder sb) {
			sb.append("<div style=\"font-family: vera-bold;\">");
			sb.append(recipe.getName());
			sb.append("</div>");
			
			if (recipe.getSkillRequirement() > currentSkillBestRanks) {
				sb.append("<div style=\"font-family: red;\">Requires ");
				sb.append(recipe.getSkillRequirement()).append(" ");
				sb.append(currentSkill.getName());
				sb.append("</div>");
			} else {
				sb.append("<div style=\"font-family: black;\">Requires ");
				sb.append("<span style=\"font-family: green;\">");
				sb.append(recipe.getSkillRequirement());
				sb.append("</span> <span style=\"font-family: blue;\">");
				sb.append(currentSkill.getName());
				sb.append("</span></div>");
			}
		}
		
		@Override protected void appendDescriptionDetails(StringBuilder sb) {
			sb.append("<p>Party best ");
			sb.append(currentSkill.getName()).append(": ");
			sb.append(currentSkillBestRanks);
			sb.append("</p>");
			
			sb.append("<div style=\"font-family: vera-bold; margin-top: 1em;\">Ingredients</div>");
			
			sb.append("<table>");
			sb.append("<tr style=\"font-family: vera\"><td style=\"padding-right: 1em; text-align: center; vertical-align: middle\">");
			sb.append("Recipe</td><td style=\"text-align: center; vertical-align: middle\">Party Quantity");
			sb.append("</td></tr>");
			
			if (recipe.getAnyItemOfTypeIngredient() != null) {
				sb.append("<tr><td>");
				sb.append("Any <span style=\"font-family: blue;\">");
				sb.append(StringUtil.upperCaseToWord(recipe.getAnyItemOfTypeIngredient().toString()));
				sb.append("</span></td><td></td></tr>");
			}
			
			for (int i = 0; i < recipe.getNumIngredients(); i++) {
				Item ingredient = recipe.getIngredient(i);
				int quantity = recipe.getIngredientQuantity(i);
				
				int partyQuantity = Game.curCampaign.party.getQuantity(ingredient.getID());
				
				sb.append("<tr>");
				
				if (partyQuantity >= quantity)
					sb.append("<td style=\"font-family: green;\">");
				else
					sb.append("<td style=\"font-family: red;\">");
					
				if (quantity != 1) {
					sb.append(quantity);
					sb.append("x ");
				}
				sb.append(ingredient.getName());
				
				sb.append("</td><td style=\"text-align: center\">");
				
				sb.append(Integer.toString(partyQuantity));
				
				sb.append("</td></tr>");
			}
			
			sb.append("</table>");
			
			sb.append("<div style=\"margin-top: 1em;\">");
			sb.append(recipe.getDescription()).append("</div>");
		}
	}
	
	private class CraftSkillIconButton extends IconViewer implements Runnable {
		private Skill skill;
		
		private CraftSkillIconButton(Skill skill) {
			super( SpriteManager.getSprite(skill.getIcon()) );
			
			this.skill = skill;
			
			this.setModel(new ToggleButtonModel());
			addCallback(this);
		}
		
		// button left clicked callback
		@Override public void run() {
			if (activeButton != null) {
				activeButton.getModel().setSelected(false);
			}
			
			activeButton = this;
			getModel().setSelected(true);
			
			updateContent();
		}
	}
}
