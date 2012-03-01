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

package net.sf.hale.characterbuilder;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ScrollPane;
import net.sf.hale.Game;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.Race;

/**
 * The BuilderPane for specifying a character's primary attributes.
 * @author Jared Stephen
 *
 */

public class BuilderPaneAttributes extends BuilderPane implements PointAllocatorModel.Listener {
	private PointAllocatorModel points;
	private AttributeSelector[] selectors;
	
	private final double expBase;
	
	private final Button defaultButton, raceRoleButton;
	
	/**
	 * Creates a new BuilderPaneAttributes editing the specified character.
	 * 
	 * @param builder the CharacterBuilder containing this BuilderPane
	 * @param character the Buildable character being edited
	 */
	
	public BuilderPaneAttributes(CharacterBuilder builder, Buildable character) {
		super(builder, "Attributes", character);
		
		points = new PointAllocatorModel(Game.ruleset.getValue("StartingAttributePoints"));
        points.addListener(this);
		
		expBase = Game.ruleset.getValue("AttributePointCostExpBaseNumerator") /
		(double)Game.ruleset.getValue("AttributePointCostExpBaseDemonenator");
        
        setTitleText("Select Attributes");
        
        selectors = new AttributeSelector[6];
        selectors[0] = new AttributeSelector("Strength", Game.ruleset.getString("StrengthIcon"));
        selectors[1] = new AttributeSelector("Dexterity", Game.ruleset.getString("DexterityIcon"));
        selectors[2] = new AttributeSelector("Constitution", Game.ruleset.getString("ConstitutionIcon"));
        selectors[3] = new AttributeSelector("Intelligence", Game.ruleset.getString("IntelligenceIcon"));
        selectors[4] = new AttributeSelector("Wisdom", Game.ruleset.getString("WisdomIcon"));
        selectors[5] = new AttributeSelector("Charisma", Game.ruleset.getString("CharismaIcon"));
        
        for (AttributeSelector selector : selectors) {
        	addSelector(selector);
        	selector.setPointAllocatorModel(points);
        }
        
        allocatorModelUpdated();
        
        defaultButton = new Button("Racial Defaults");
        defaultButton.setTheme("defaultbutton");
        defaultButton.addCallback(new Runnable() {
        	@Override public void run() {
        		setDefaultValues();
        	}
        });
        this.add(defaultButton);
        
        raceRoleButton = new Button("Suggested Values");
        raceRoleButton.setTheme("racerolebutton");
        raceRoleButton.addCallback(new Runnable() {
        	@Override public void run() {
        		setRoleValues();
        	}
        });
        this.add(raceRoleButton);
	}
	
	private void setDefaultValues() {
		for (AttributeSelector selector : selectors) {
			int valueDiff = selector.baseValue - selector.getValue();
			
			selector.addModelPoints(valueDiff);
			selector.addValue(valueDiff);
		}
	}
	
	private void setRoleValues() {
		int[] defaultSelections = getCharacter().getSelectedRole().getDefaultPlayerAttributeSelections();
		if (defaultSelections == null) return;
		
		int i = 0;
		for (AttributeSelector selector : selectors) {
			int valueDiff = selector.baseValue + defaultSelections[i] - selector.getValue();
			
			selector.addModelPoints(valueDiff);
			selector.addValue(valueDiff);
			
			i++;
		}
	}
	
	@Override protected void layout() {
		super.layout();
		
		ScrollPane pane = getSelectorPane();
		
		defaultButton.setSize(defaultButton.getPreferredWidth(), defaultButton.getPreferredHeight());
		raceRoleButton.setSize(raceRoleButton.getPreferredWidth(), raceRoleButton.getPreferredHeight());
		
		defaultButton.setPosition(getInnerX(), pane.getBottom());
		raceRoleButton.setPosition(defaultButton.getRight(), pane.getBottom());
	}
	
	@Override protected void next() {
		setCharacterAttributes();
		
		super.next();
	}
	
	private void setCharacterAttributes() {
		int[] attributes = new int[6];
		for (int i = 0; i < attributes.length; i++) {
			attributes[i] = selectors[i].getValue();
		}
		
		getCharacter().setSelectedAttributes(attributes);
	}
	
	// called when this builder pane is shown
	@Override public void updateCharacter() {
		points.setPointsRemaining(Game.ruleset.getValue("StartingAttributePoints"));
		
		Race race = getCharacter().getSelectedRace();
		
		selectors[0].baseValue = race.getBaseStr();
		selectors[1].baseValue = race.getBaseDex();
		selectors[2].baseValue = race.getBaseCon();
		selectors[3].baseValue = race.getBaseInt();
		selectors[4].baseValue = race.getBaseWis();
		selectors[5].baseValue = race.getBaseCha();
		
		int[] alreadySelected = getCharacter().getSelectedAttributes();
		int[] defaultSelections = getCharacter().getSelectedRole().getDefaultPlayerAttributeSelections();
		
		raceRoleButton.setEnabled(defaultSelections != null);
		
		int i = 0;
		for (AttributeSelector selector : selectors) {
			selector.setMinMaxValue(selector.baseValue - 3, selector.baseValue + 10);
			selector.setValue(selector.baseValue);
			
			int added = 0;
			
			if (alreadySelected != null) {
				added = alreadySelected[i] - selector.baseValue;
			} else if (defaultSelections != null) {
				added = defaultSelections[i];
			}
			
			selector.addModelPoints(added);
			selector.addValue(added);
			
			i++;
		}
		
		allocatorModelUpdated();
	}
	
	@Override public void allocatorModelUpdated() {
		double points = this.points.getRemainingPoints();
		
		boolean nextEnabled = true;
		for (AttributeSelector selector : selectors) {
			if (selector.canIncrement()) {
				nextEnabled = false;
				break;
			}
		}

		getNextButton().setEnabled(nextEnabled);
		
		setPointsText(Game.numberFormat(1).format(points) + " points remaining");
	}
	
	private class AttributeSelector extends BuildablePropertySelector {
		private String name;
		private int baseValue;
		
		public AttributeSelector(String name, String icon) {
			super(name, icon, true);
			
			this.name = name;
			this.baseValue = 0;
		}
		
		private final double getCost(int curValue, int baseValue) {
			double baseCost = curValue - baseValue;
			
			return baseCost * Math.pow(expBase, baseCost);
		}
		
		private boolean canIncrement() {
			double incrementCost = getCost(getValue() + 1, baseValue) - getCost(getValue(), baseValue);
			return incrementCost < points.getRemainingPoints();
		}
		
		@Override protected void setIncrementDecrementEnabled() {
			boolean modelOK = points != null ? canIncrement() : true;
			
			getIncrementButton().setEnabled(isEnabled() && getValue() < getMaxValue() && modelOK);
			getDecrementButton().setEnabled(isEnabled() && getValue() > getMinValue());
		}
		
		@Override protected void addModelPoints(int points) {
			double cost = getCost(getValue() + points, baseValue) - getCost(getValue(), baseValue);
			BuilderPaneAttributes.this.points.allocatePoints(cost);
		}
		
		@Override protected void onMouseHover() {
			StringBuilder content = new StringBuilder();
			content.append("<html><body>");
			
			content.append("<div style=\"font-family: red; margin-bottom: 1em;\">" + name + "</div>");
			
			content.append(ResourceManager.getResourceAsString("descriptions/stats/" + name, ResourceType.HTML));
			
			content.append("</body></html>");
			getTextModel().setHtml(content.toString());
			getTextPane().invalidateLayout();
		}
		
		@Override protected void addValue(int value) {
			super.addValue(value);
			
			// update the listeners again after adding points
			points.allocatePoints(0.0);
		}
	}
}
