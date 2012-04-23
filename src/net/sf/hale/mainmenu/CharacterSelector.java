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

package net.sf.hale.mainmenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.rules.Role;
import net.sf.hale.view.CharacterWindow;
import net.sf.hale.widgets.BasePortraitViewer;
import net.sf.hale.widgets.TextAreaNoInput;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A widget for displaying basic information about a character, with a button
 * to show detailed information via the {@link net.sf.hale.view.CharacterWindow}
 * and optionally a button with a supplied callback to add / remove the character
 * from the party
 * @author Jared Stephen
 *
 */

public class CharacterSelector extends Widget {
	private int numRoleLines;
	
	private int expandBoxY, expandBoxBorder;
	
	private Button details;
	private HTMLTextAreaModel textAreaModel;
	private TextArea textArea;
	
	private Button addRemove;
	private ExpandBox expand;
	
	private boolean characterMeetsLevelRequirements;
	private UniqueCharacter character;
	private Creature creature;
	private Widget parent;
	
	private BasePortraitViewer portrait;
	
	private DeleteButton deleteButton;
	
	private PartyFormationWindow newGameWindow;
	
	private boolean showDeleteButtons;
	
	private Set<String> charactersInParties;
	
	/**
	 * Creates a new CharacterSelector for the set of creatures contained in the specified UniqueCharacter.
	 * Any CharacterWindows that are created are added to the specified parent widget
	 * @param character the set of characters to view
	 * @param parent the parent widget to add any details windows to
	 */
	
	public CharacterSelector(UniqueCharacter character, Widget parent, Set<String> charactersInParties) {
		this.charactersInParties = charactersInParties;
		if (charactersInParties == null)
			this.charactersInParties = Collections.emptySet();
		
		this.character = character;
		this.creature = character.getBestCreature();

		characterMeetsLevelRequirements = creature != null;
		// use the first creature if none meet the requirements
		if (creature == null) {
			creature = character.iterator().next();
		}
		
		this.parent = parent;
		
		textAreaModel = new HTMLTextAreaModel();
		textArea = new TextAreaNoInput(textAreaModel);
        textArea.setTheme("description");
        textAreaModel.setHtml(getDescription());
        add(textArea);
        
        details = new Button();
        details.setTheme("detailsbutton");
        details.addCallback(new Runnable() {
        	@Override public void run() {
        		CharWindow window = new CharWindow();
        		window.updateContent(CharacterSelector.this.creature);
        		window.setPosition(details.getRight(), details.getY() - 150);
        		
        		CharacterSelector.this.parent.add(window);
        	}
        });
        add(details);
        
        portrait = new BasePortraitViewer(creature);
        portrait.setEnableEventHandling(false);
        add(portrait);
        
        expand = new ExpandBox();
       
        setDeleteExpandState();
	}
	
	/**
	 * Create a new CharacterSelector for the specified creature.  Any CharacterWindows
	 * that are created by this Widget are added to the supplied widget
	 * @param creature the creature to view
	 * @param parent the parent widget to add details windows to
	 */
	
	public CharacterSelector(Creature creature, Widget parent) {
		this(new UniqueCharacter(creature), parent, null);
	}
	
	/**
	 * Causes this CharacterSelector to show a delete button, even if it is only
	 * viewing a single creature
	 */
	
	public void showDeleteButtons() {
		showDeleteButtons = true;
		
		setDeleteExpandState();
	}
	
	private void setDeleteExpandState() {
		if (expand != null) {
			removeChild(expand);
			
			if (character.size() > 1) {
				add(expand);
			}
		}
		
		if (showDeleteButtons) {
			if (deleteButton != null) removeChild(deleteButton);
			
			if (character.size() <= 1) {
				deleteButton = new DeleteButton(character.getFirstCreature(), null);
				add(deleteButton);
			}
		}
	}
	
	/**
	 * Returns the Creature that this CharacterSelector was created with or is currently selected
	 * @return the Creature
	 */
	
	public Creature getCreature() {
		return creature;
	}
	
	/**
	 * Sets the new game window, which will be refreshed if all creatures in the 
	 * character being viewed are deleted
	 * @param window the window to refresh
	 */
	
	public void setNewGameWindow(PartyFormationWindow window) {
		this.newGameWindow = window;
	}
	
	private void setSelectedCreature(Creature creature) {
		this.creature = creature;
		textAreaModel.setHtml(getDescription());
		
		setDeleteExpandState();
	}
	
	/**
	 * Returns the ID string of the Creature that this CharacterSelector was
	 * created with
	 * @return the ID String of this CharacterSelector's creature
	 */
	
	public String getCreatureID() {
		return creature.getID();
	}
	
	/**
	 * Sets the text and callback for the add remove button for this
	 * character selector.  By default, the add remove button is not shown.
	 * @param text the text to display on the add / remove button.  If null is passed,
	 * the add remove button is cleared and not shown on this Widget
	 * @param callback the Callback that will be run() whenever the button is clicked
	 */
	
	public void setAddRemoveButton(String text, Runnable callback) {
		if (text == null) {
			if (addRemove != null) {
				removeChild(addRemove);
				addRemove = null;
			}
		} else {
			addRemove = new Button(text);
			addRemove.setTheme("addremovebutton");
			addRemove.addCallback(callback);
			addRemove.setEnabled(characterMeetsLevelRequirements);
			
			if (!addRemove.isEnabled()) {
				addRemove.setTooltipContent("No version of this character meets the level requirements");
			}
			
			add(addRemove);
		}
		
		invalidateLayout();
	}
	
	/**
	 * Sets the enabled state of the add remove button to the specified value.  If no
	 * add remove button is currently present in this Widget, no action is performed.
	 * 
	 * Note that if no character meets the level requirements, this widget will remain
	 * disabled even if this method is called with "true"
	 * @param enabled whether the add remove button should be set to enabled or disabled
	 */
	
	public void setAddRemoveEnabled(boolean enabled) {
		if (addRemove != null) {
			addRemove.setEnabled(enabled && characterMeetsLevelRequirements);
		}
	}
	
	private String getDescription() {
		numRoleLines = 0;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<div style=\"font-family: vera-bold;\">").append(creature.getName()).append("</div>");
		
		sb.append("<div style=\"font-family: vera;\">");
		sb.append(creature.getGender()).append(' ');
		sb.append("<span style=\"font-family: vera-blue;\">").append(creature.getRace().getName()).append("</span>");
		sb.append("</div>");
		
		sb.append("<div style=\"font-family: vera; margin-bottom: 1em\">");
		for (String roleID : creature.getRoles().getRoleIDs()) {
			Role role = Game.ruleset.getRole(roleID);
			int level = creature.getRoles().getLevel(role);
			
			sb.append("<p>");
			sb.append("Level <span style=\"font-family: vera-italic;\">").append(level).append("</span> ");
			sb.append("<span style=\"font-family: vera-red;\">").append(role.getName()).append("</span>");
			sb.append("</p>");
			
			numRoleLines++;
		}
		sb.append("</div>");
		
		return sb.toString();
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		expandBoxY = themeInfo.getParameter("expandboxy", 0);
		expandBoxBorder = themeInfo.getParameter("expandboxborder", 0);
	}
	
	@Override public int getPreferredWidth() {
		return portrait.getPreferredWidth() + textArea.getPreferredWidth() + getBorderHorizontal();
	}
	
	@Override public int getPreferredHeight() {
		int height = textArea.getPreferredInnerHeight() + textArea.getBorderVertical();
		height += details.getPreferredHeight();
		if (addRemove != null)
			height += addRemove.getPreferredHeight();
		
		
		return Math.max(height, portrait.getPreferredHeight()) + getBorderVertical();
	}
	
	@Override protected void layout() {
		portrait.setSize(portrait.getPreferredWidth(), portrait.getPreferredHeight());
		portrait.setPosition(getInnerX(), getInnerY() + (getInnerHeight() - portrait.getHeight()) / 2);
		
		textArea.setPosition(portrait.getRight(), getInnerY());
		textArea.setSize(textArea.getPreferredWidth(), textArea.getPreferredHeight());

		int availWidth = getInnerRight() - portrait.getRight();
		
		details.setSize(details.getPreferredWidth(), details.getPreferredHeight());
		details.setPosition(portrait.getRight() + availWidth / 2 - details.getWidth() / 2,
				getInnerBottom() - details.getHeight());
		
		expand.setSize(textArea.getWidth() - expandBoxBorder * 2, expand.getPreferredHeight());
		expand.setPosition(textArea.getX() + expandBoxBorder, getInnerY() + expandBoxY);
		
		if (addRemove != null) {
			addRemove.setSize(addRemove.getPreferredWidth(), addRemove.getPreferredHeight());
			addRemove.setPosition(portrait.getRight() + availWidth / 2 - addRemove.getWidth() / 2,
					details.getY() - addRemove.getHeight());
		}
		
		if (deleteButton != null) {
			deleteButton.setSize(deleteButton.getPreferredWidth(), deleteButton.getPreferredHeight());
			deleteButton.setPosition(getInnerRight() - deleteButton.getWidth() - expandBoxBorder,
					getInnerY() + expandBoxY + expandBoxBorder);
		}
	}
	
	private class CharWindow extends CharacterWindow {
		public CharWindow() {
			setTheme("characterwindow");
			
			hideExportButton();
		}
		
		// override the close callback
		@Override public void run() {
			parent.removeChild(this);
		}
	}
	
	private class ExpandBox extends Label implements CallbackWithReason<Label.CallbackReason> {
		private boolean boxHover;
		
		private Button expand;
		private Widget box;
		
		private ExpandBox() {
			box = new Widget(getAnimationState());
	        box.setTheme("box");
	        if (!characterMeetsLevelRequirements) {
	        	box.setEnabled(false);
	        	box.getAnimationState().setAnimationState(STATE_DISABLED, true);
	        }
	        add(box);
	        
	        expand = new Button(getAnimationState());
	        expand.setTheme("expandbutton");
	        expand.getModel().addStateCallback(new Runnable() {
	        	@Override public void run() {
	        		updateHover();
	        	}
	        });
	        expand.addCallback(new Runnable() {
	        	@Override public void run() {
	        		openPopup();
	        	}
	        });
	        add(expand);
	        
	        addCallback(this);
		}
		
		private void openPopup() {
			Popup popup = new Popup(parent);
			popup.openPopup();
			
			popup.setPosition(getX(), getBottom());
			popup.setSize(getWidth(), popup.getPreferredHeight());
		}
		
		// label clicked callback
		@Override public void callback(Label.CallbackReason reason) {
			openPopup();
		}
		
		private void updateHover() {
			getAnimationState().setAnimationState(Label.STATE_HOVER, boxHover || expand.getModel().isHover());
		}
		
		@Override public int getPreferredHeight() {
			return numRoleLines * expand.getPreferredHeight() + getBorderVertical() + box.getBorderVertical();
		}
		
		@Override protected void layout() {
			expand.setSize(expand.getPreferredWidth(), expand.getPreferredHeight());
			expand.setPosition(getInnerRight() - expand.getWidth() - box.getBorderRight(),
					getInnerY() + box.getBorderTop() + (getInnerHeight() - expand.getHeight()) / 2);
			
			box.setSize(getInnerWidth(), getInnerHeight());
			box.setPosition(getInnerX(), getInnerY());
		}
		
		@Override protected void handleMouseHover(Event evt) {
			if (evt.isMouseEvent()) {
				boolean newHover = evt.getType() != Event.Type.MOUSE_EXITED;
				if (newHover != boxHover) {
					boxHover = newHover;
					updateHover();
				}
			}
		}
	}
	
	private class Popup extends PopupWindow {
		private Popup(Widget parent) {
			super(parent);
			
			setTheme("characterselectorpopup");
			
			add(new PopupContent(this));
		}
	}
	
	private class PopupContent extends Widget {
		private List<CharacterButton> selectors;
		private List<DeleteButton> deleteButtons;
		
		private PopupContent(PopupWindow popup) {
			setTheme("content");
			
			selectors = new ArrayList<CharacterButton>();
			deleteButtons = new ArrayList<DeleteButton>();
			
			for (Creature creature : character) {
				CharacterButton button = new CharacterButton(creature, popup);
				add(button);
				selectors.add(button);
				
				DeleteButton deleteButton = new DeleteButton(creature, popup);
				if (showDeleteButtons)
					add(deleteButton);
				deleteButtons.add(deleteButton);
			}
		}
		
		@Override public int getPreferredHeight() {
			int height = getBorderVertical();
			
			for (CharacterButton child : selectors) {
				height += child.getPreferredHeight();
			}
			
			return height;
		}
		
		@Override protected void layout() {
			int curY = getInnerY();
			
			for (int i = 0; i < selectors.size(); i++) {
				CharacterButton selectButton = selectors.get(i);
				DeleteButton deleteButton = deleteButtons.get(i);
				
				selectButton.setSize(getInnerWidth(), selectButton.getPreferredHeight());
				selectButton.setPosition(getInnerX(), curY);
				
				deleteButton.setSize(deleteButton.getPreferredWidth(), deleteButton.getPreferredHeight());
				deleteButton.setPosition(selectButton.getInnerRight() - deleteButton.getWidth(),
						selectButton.getInnerY());
				
				curY = selectButton.getBottom();
			}
		}
	}
	
	private class DeleteButton extends Button implements Runnable {
		private Creature creature;
		private PopupWindow parent;
		
		private DeleteButton(Creature creature, PopupWindow parent) {
			this.creature = creature;
			this.parent = parent;
			addCallback(this);
			
			if (charactersInParties.contains(creature.getID())) {
				this.setEnabled(false);
				setTooltipContent("This character is in one or more parties and may not be deleted.");
			}
		}
		
		@Override public void run() {
			ConfirmationPopup popup = new ConfirmationPopup(CharacterSelector.this);
			
			StringBuilder sb = new StringBuilder();
			sb.append("Delete ");
			sb.append(creature.getName());
			
			sb.append(", Level ");
			sb.append(creature.getRoles().getTotalLevel());
			sb.append(" ");
			sb.append(creature.getRoles().getBaseRole().getName());
			
			sb.append("?");
			
			popup.setTitleText(sb.toString());
			
			popup.setWarningText("This action is permanent and cannot be undone.");
			
			popup.addCallback(new DeleteCallback(creature, parent));
			
			popup.openPopupCentered();
		}
	}
	
	private class DeleteCallback implements Runnable {
		private Creature creature;
		private PopupWindow parent;
		
		private DeleteCallback(Creature creature, PopupWindow parent) {
			this.creature = creature;
			this.parent = parent;
		}
		
		@Override public void run() {
			character.deleteCreature(creature);
			
			if ( character.size() == 0 ) {
				if (newGameWindow != null) {
					newGameWindow.removeSelector(CharacterSelector.this);
				}
			} else if (creature == CharacterSelector.this.creature) {
				Creature best = character.getBestCreature();
				
				if (best == null) {
					newGameWindow.removeSelector(CharacterSelector.this);
				} else {
					setSelectedCreature(character.getBestCreature());
				}
			}
			
			if (parent != null)
				parent.closePopup();
		}
	}
	
	private class CharacterButton extends ToggleButton implements Runnable {
		private int height, numRows;
		
		private PopupWindow popup;
		private Creature creature;
		
		private TextArea textArea;
		private HTMLTextAreaModel textAreaModel;
		
		private CharacterButton(Creature creature, PopupWindow popup) {
			
			if (!character.meetsLevelConstraints(creature)) {
				setEnabled(false);
			}
			
			if (creature == CharacterSelector.this.creature)
				setActive(true);
			
			this.popup = popup;
			this.creature = creature;
			
			textAreaModel = new HTMLTextAreaModel();
			
			numRows = 0;
			
			StringBuilder sb = new StringBuilder();
			sb.append("<div style=\"font-family: vera\">");
			for (String roleID : creature.getRoles().getRoleIDs()) {
				Role role = Game.ruleset.getRole(roleID);
				int level = creature.getRoles().getLevel(role);
				
				sb.append("<p>");
				sb.append("Level <span style=\"font-family: vera-italic;\">").append(level).append("</span> ");
				sb.append("<span style=\"font-family: vera-red;\">").append(role.getName()).append("</span>");
				sb.append("</p>");
				
				numRows++;
			}
			sb.append("</div>");
			
			textAreaModel.setHtml(sb.toString());
			
			textArea = new TextAreaNoInput(textAreaModel);
			textArea.setTheme("textarea");
			add(textArea);
			
			addCallback(this);
		}
		
		// button click callback
		
		@Override public void run() {
			popup.closePopup();
			
			setSelectedCreature(creature);
		}
		
		@Override protected void applyTheme(ThemeInfo themeInfo) {
			super.applyTheme(themeInfo);
			
			height = themeInfo.getParameter("height", 0);
			
			if (!isEnabled()) {
				int minLevel = Game.curCampaign.getMinStartingLevel();
				int maxLevel = Game.curCampaign.getMaxStartingLevel();
				
				if (minLevel == maxLevel) {
					setTooltipContent("All characters must be level " + minLevel);
				} else {
					setTooltipContent("All characters must be from level " + minLevel + " to " + maxLevel);
				}
			}
		}
		
		@Override public int getPreferredHeight() {
			return height * numRows;
		}
		
		@Override protected void layout() {
			textArea.setPosition(getX(), getY());
			textArea.setSize(getWidth(), textArea.getPreferredHeight());
		}
	}
}
