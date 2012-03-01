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


import java.util.ArrayList;
import java.util.List;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ToggleButton;
import net.sf.hale.entity.Creature;
import net.sf.hale.util.CreatureWriter;
import net.sf.hale.view.GameSubWindow;

/**
 * The character builder is the set of screens with functions to create
 * and edit characters (Creatures).  It is shown when creating a new
 * character from the main menu and when leveling up.
 * @author Jared Stephen
 *
 */

public class CharacterBuilder extends GameSubWindow {
	private List<FinishCallback> finishCallbacks;
	
	private PaneSelectorButton[] paneButtons;
	
	private Buildable character;
	
	private AbstractBuilderPane activePane;
	
	private final DialogLayout content;
	private final DialogLayout.Group mainContentH;
	private final DialogLayout.Group mainContentV;
	
	/**
	 * Create a new CharacterBuilder for the specified Buildable character.
	 */
	
	public CharacterBuilder(Buildable character) {
		this.character = character;
		this.finishCallbacks = new ArrayList<FinishCallback>();
		this.setSize(750, 550);
		
		content = new DialogLayout();
		content.setTheme("content");
		this.add(content);
		
		paneButtons = new PaneSelectorButton[6];
		paneButtons[0] = new PaneSelectorButton(new BuilderPaneRace(this, character));
		paneButtons[1] = new PaneSelectorButton(new BuilderPaneRole(this, character));
		paneButtons[2] = new PaneSelectorButton(new BuilderPaneAttributes(this, character));
		paneButtons[3] = new PaneSelectorButton(new BuilderPaneSkills(this, character));
		paneButtons[4] = new PaneSelectorButton(new BuilderPaneAbilities(this, character));
		paneButtons[5] = new PaneSelectorButton(new BuilderPaneCosmetic(this, character));
		
		Group topH = content.createSequentialGroup(paneButtons);
		Group topV = content.createParallelGroup(paneButtons);
		
		mainContentH = content.createSequentialGroup();
		mainContentV = content.createSequentialGroup();
		
		Group mainH = content.createParallelGroup(topH, mainContentH);
		Group mainV = content.createSequentialGroup(topV, mainContentV);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
		
		if (character.isNewCharacter()) {
			this.setTitle("Character Builder");
			
			// show the race selector
			paneButtons[0].select();
		} else {
			this.setTitle("Character Builder for " + getCharacter().getName());
			
			paneButtons[0].setEnabled(false);
			paneButtons[2].setEnabled(false);
			paneButtons[5].setEnabled(false);
			
			// show the role selector
			paneButtons[1].select();
		}
	}
	
	@Override protected void afterAddToGUI(GUI gui) {
		super.afterAddToGUI(gui);
		this.setPositionCentered();
	}
	
	/**
	 * Adds a callback to the list of callbacks that are run when this CharacterBuilder
	 * finishes and exits
	 * @param callback the callback to add
	 */
	
	public void addFinishCallback(FinishCallback callback) {
		finishCallbacks.add(callback);
	}
	
	/**
	 * Called when the user is finished building or updating the character.
	 * Saves the character if it is new or updates it if not, then closes
	 * this Widget
	 */
	
	protected void finish() {
		Creature creature = character.getWorkingCopy();
		
		String id = creature.getID();
		if (character.isNewCharacter()) {
			id = CreatureWriter.saveCharacter(creature);
		} else {
			character.applySelectionsToCreature();
		}
		
		getCloseCallback().run();
		
		for (FinishCallback callback : finishCallbacks) {
			callback.creatureModified(id);
		}
	}
	
	/**
	 * Returns the Buildable character that is currently being edited / created by this
	 * CharacterBuilder
	 * @return the character being edited by this CharacterBuilder
	 */
	
	public Buildable getCharacter() {
		return character;
	}
	
	/**
	 * Returns the BuilderPane that is previous to the specified pane
	 * in the list of BuilderPanes, or null if the pane is the first
	 * pane.
	 * @param pane the BuilderPane to find the previous pane for
	 * @return the BuilderPane just prior to the specified BuilderPane
	 */
	
	public AbstractBuilderPane getPreviousPane(AbstractBuilderPane pane) {
		int i;
		
		for (i = 0; i < paneButtons.length; i++) {
			if (paneButtons[i].pane == pane) {
				break;
			}
		}
		
		if (i == 0) return null;
		else return paneButtons[i - 1].pane;
	}
	
	/**
	 * Returns the BuilderPane that is next to the specified pane
	 * in the list of BuilderPanes, or null if the pane specified
	 * is the last pane.
	 * 
	 * @param pane the BuilderPane to get the next for
	 * @return the BuilderPane just after the specified BuilderPane
	 */
	
	public AbstractBuilderPane getNextPane(AbstractBuilderPane pane) {
		int i;
		
		for (i = 0; i < paneButtons.length; i++) {
			if (paneButtons[i].pane == pane) {
				break;
			}
		}
		
		if (i >= paneButtons.length - 1) return null;
		else return paneButtons[i + 1].pane;
	}
	
	/**
	 * Sets the active pane that is currently being displayed by this CharacterBuilder
	 * to the specified BuilderPane
	 * @param pane the BuilderPane to show
	 */
	
	public void setActivePane(AbstractBuilderPane pane) {
		this.activePane = pane;
		
		mainContentH.clear(true);
		mainContentV.clear(true);
		
		mainContentH.addWidget(activePane);
		mainContentV.addWidget(activePane);
		
		content.invalidateLayout();
		
		for (PaneSelectorButton button : paneButtons) {
			button.setActive(button.pane == pane);
		}
		
		this.activePane.updateCharacter();
	}
	
	/**
	 * Instances of this interface are used whenever the CharacterBuilder exits,
	 * either from succesfully adding a level or succesfully creating a new creature
	 * @author Jared Stephen
	 *
	 */
	
	public interface FinishCallback {
		/**
		 * Called whenever this CharacterBuilder finishes, either from
		 * leveling up or creating a new creature
		 * @param id the ID of the creature that has just leveled up or
		 * been created
		 */
		
		public void creatureModified(String id);
	}
	
	// close callback
	@Override public void run() {
		getParent().removeChild(this);
	}
	
	private class PaneSelectorButton extends ToggleButton {
		private AbstractBuilderPane pane;
		
		private PaneSelectorButton(AbstractBuilderPane pane) {
			super(pane.getName());
			this.pane = pane;
		}
		
		private void select() {
			setActivePane(pane);
		}
		
		// disable all animation and clicking by refusing all events
		@Override protected boolean handleEvent(Event evt) {
			return false;
		}
	}
}
