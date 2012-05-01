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

package net.sf.hale.quickbar;

import org.lwjgl.opengl.GL11;

import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.view.DragTarget;
import net.sf.hale.view.DropTarget;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A Button for viewing and activating an individual Quickbar slot.  Each
 * QuickbarSlotButton shows the icon for one Quickbar and an index indicating
 * the keyboard hotkey.
 * 
 * @author Jared Stephen
 *
 */

public class QuickbarSlotButton extends Button implements DropTarget {
	private QuickbarViewer viewer;
	
	private QuickbarSlot slot;
	private Quickbar quickbar;
	private int quickbarIndex;
	
	private Sprite overrideSprite;
	private Color overrideColor;
	
	private Sprite sprite;
	private Color color;
	
	private Sprite secondarySprite;
	private Color secondaryColor;
	
	private int index;
	
	private Label primaryLabel, indexLabel;
	private String emptyTooltip;
	
	private QuickbarSlot dragSlotToAdd;
	
	private boolean disabledExceptActivate;
	
	/**
	 * Creates a new QuickbarSlotButton that can be used to activate a QuickbarSlot.
	 * 
	 * @param index the index that will displayed by this Quickbar.  In general, this
	 * will not be the actual QuickbarSlot index.  It is instead the index within
	 * the currently displayed set of QuickbarSlots by the QuickbarViewer.
	 * @param viewer the quickbarviewer that created this button
	 */
	
	public QuickbarSlotButton(int index, QuickbarViewer viewer) {
		this.viewer = viewer;
		this.index = index;
		
		indexLabel = new Label(Integer.toString((index + 1) % Quickbar.SlotsAtOnce));
		indexLabel.setTheme("indexlabel");
		this.add(indexLabel);
		
		primaryLabel = new Label();
		primaryLabel.setTheme("primarylabel");
		this.add(primaryLabel);
		
		this.color = Color.WHITE;
	}
	
	/**
	 * Sets whether or not the label showing the viewer index is shown
	 * @param show
	 */
	
	public void setShowIndexLabel(boolean show) {
		indexLabel.setVisible(show);
	}
	
	/**
	 * Sets whether all actions except for the basic left click should be disabled
	 * @param disable whether all actions (show right click menu, drag & drop) except
	 * for left click activate are disabled
	 */
	
	public void setDisabledExceptActivate(boolean disable) {
		this.disabledExceptActivate = disable;
	}
	
	@Override protected void layout() {
		super.layout();
		
		indexLabel.setPosition(getInnerRight() - indexLabel.getPreferredWidth(), getInnerY() + indexLabel.getPreferredHeight() / 2);
		primaryLabel.setPosition(getInnerX(), getInnerBottom() - primaryLabel.getPreferredHeight() / 2);
	}
	
	/**
	 * Sets the override Sprite for this Button.  This causes the Button to display
	 * the specified Sprite while the override is in effect.  Once the override is
	 * cleared with {@link #clearOverrideSprite()}, the Button goes back to displaying
	 * the usual Sprite.  This is used by the QuickbarDragHandler as a cue for drag
	 * and drop.
	 * 
	 * @param sprite the Sprite to display as an override
	 * @param color the color to display the override sprite
	 */
	
	public void setOverrideSprite(Sprite sprite, Color color) {
		this.overrideSprite = sprite;
		this.overrideColor = color;
	}
	
	public void clearOverrideSprite() {
		this.overrideSprite = null;
		this.overrideColor = null;
	}
	
	/**
	 * Sets this QuickbarSlotButton to use the specified QuickbarSlot.  If the
	 * passed parameter is null, the icon and text displayed by this button are
	 * cleared.
	 * 
	 * @param slot the QuickbarSlot to display.
	 * @param quickbar the Quickbar containing the specified slot
	 * @param index the index of the specified Slot within the Quickbar
	 */
	
	public void setSlot(QuickbarSlot slot, Quickbar quickbar, int index) {
		this.slot = slot;
		this.quickbar = quickbar;
		this.quickbarIndex = index;
		
		if (slot == null) {
			if (this.getTooltipContent() != emptyTooltip)
				this.setTooltipContent(emptyTooltip);
			
			this.sprite = null;
			primaryLabel.setText("");
			
			this.secondarySprite = null;
		} else {
			this.sprite = slot.getSprite();
			this.secondarySprite = slot.getSecondarySprite();
			Color c = slot.getSpriteColor();
			Color c2 = slot.getSecondarySpriteColor();
			
			primaryLabel.setText(slot.getLabelText());
			
			if (!slot.isActivateable()) {
				this.color = c.multiply(new Color(0xFF7F7F7F));
				this.secondaryColor = c2.multiply(new Color(0xFF7F7F7F));
			} else {
				this.color = c;
				this.secondaryColor = c2;
			}
			
			String tooltip = slot.getTooltipText();
			if (!tooltip.equals(getTooltipContent())) {
				this.setTooltipContent(tooltip);
			}
		}
	}
	
	/**
	 * Returns the index within the set of displayed QuickbarSlots of this
	 * QuickbarSlotButton.
	 * @return the index of this QuickbarSlotButton
	 */
	
	public int getIndex() { return index; }
	
	@Override public boolean handleEvent(Event evt) {
		if (Game.interfaceLocker.locked()) return super.handleEvent(evt);
		
		switch (evt.getType()) {
		case MOUSE_DRAGGED:
		case MOUSE_BTNUP:
			if (!isMouseInside(evt)) break;
			switch (evt.getMouseButton()) {
			case Event.MOUSE_LBUTTON:
				activateSlot(getRight(), getY());
				break;
			case Event.MOUSE_RBUTTON:
				if (disabledExceptActivate) break;
				
				createRightClickMenu(getRight(), getY());
				break;
			}
		}
		
		return super.handleEvent(evt);
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		emptyTooltip = themeInfo.getParameter("emptytooltip", (String)null);
	}
	
	@Override public void paintWidget(GUI gui) {
		super.paintWidget(gui);
		
		if (overrideSprite != null) {
			GL11.glColor4ub(overrideColor.getR(), overrideColor.getG(),
					overrideColor.getB(), overrideColor.getA());
			
			overrideSprite.drawWithIconOffset(getInnerX(), getInnerY());
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
		} else {
			int offset;
			if (secondarySprite != null) offset = 5;
			else offset = 0;
			
			if (secondarySprite != null) {
				GL11.glColor4ub(secondaryColor.getR(), secondaryColor.getG(),
						secondaryColor.getB(), secondaryColor.getA());
				
				secondarySprite.drawWithIconOffset(getInnerX() + offset, getInnerY());
			}
			
			if (sprite != null) {
				GL11.glColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
				sprite.drawWithIconOffset(getInnerX() - offset, getInnerY());
				GL11.glColor3f(1.0f, 1.0f, 1.0f);
			}
		}
	}
	
	/**
	 * Activates the quickbar slot held by this QuickbarSlotButton.
	 * Any menu that is opened as a result of activating will be opened at the
	 * specified position
	 * @param menuPositionX the menu x coordinate if a menu is opened
	 * @param menuPositionY the menu y coordinate if a menu is opened
	 */
	
	public void activateSlot(int menuPositionX, int menuPositionY) {
		if (slot == null) return;
		
		Game.mainViewer.getMenu().clear();
		Game.mainViewer.getMenu().setPosition(menuPositionX, menuPositionY);
		
		slot.activate();
	}
	
	/**
	 * Creates a new right click menu at the specified coordinates using the 
	 * current slot for this QuickbarSlotButton.  If this QuickbarSlotButton
	 * is empty, no action is taken
	 * @param menuPositionX the x screen coordinate
	 * @param menuPositionY the y screen coordinate
	 */
	
	public void createRightClickMenu(int menuPositionX, int menuPositionY) {
		if (slot == null) return;
		
		Game.mainViewer.getMenu().clear();
		Game.mainViewer.getMenu().setPosition(menuPositionX, menuPositionY);
		
		slot.createRightClickMenu(this);
	}
	
	/**
	 * A callback for use in clearing this QuickbarSlotButton
	 * @author Jared Stephen
	 *
	 */
	
	public static class ClearSlotCallback implements Runnable {
		private QuickbarSlotButton button;
		
		/**
		 * Creates a new ClearSlotCallback
		 * @param button the QuickbarSlotButton to clear
		 */
		
		public ClearSlotCallback(QuickbarSlotButton button) {
			this.button = button;
		}
		
		@Override public void run() {
			button.quickbar.setSlot(null, button.quickbarIndex);
			button.setSlot(null, button.quickbar, button.quickbarIndex);
			Game.mainViewer.getMenu().hide();
		}
	}
	
	/**
	 * A callback for use in activating a QuickbarSlot. this callback can be used
	 * instead of the normal left click activate for this slot
	 * @author Jared Stephen
	 *
	 */
	
	public static class ActivateSlotCallback implements Runnable {
		private QuickbarSlot slot;
		
		/**
		 * Creates a new ActivateSlotCallback
		 * @param slot the QuickbarSlot to activate
		 */
		
		public ActivateSlotCallback(QuickbarSlot slot) {
			this.slot = slot;
		}
		
		@Override public void run() {
			Game.mainViewer.getMenu().hide();
			slot.activate();
		}
	}

	@Override public void dragAndDropStartHover(DragTarget target) {
		if (target.getItem() != null && target.getItemParent() == this.quickbar.getParent()) {
			dragSlotToAdd = Quickbar.getQuickbarSlot(target.getItem(), target.getItemParent());
		} else if (target.getAbility() != null && target.getAbilityParent() == this.quickbar.getParent()) {
			dragSlotToAdd = Quickbar.getQuickbarSlot(target.getAbility(), target.getAbilityParent());
		} else {
			dragSlotToAdd = null;
		}
		
		if (dragSlotToAdd != null) {
			this.setOverrideSprite(target.getDragSprite(),
					target.getDragSpriteColor().multiply(new Color(0xFF555555)));
		}
	}

	@Override public void dragAndDropStopHover(DragTarget target) {
		this.clearOverrideSprite();
	}

	@Override public void dropDragTarget(DragTarget target) {
		this.clearOverrideSprite();
		
		int slotIndex = viewer.getSlotIndex(this);
		
		if (dragSlotToAdd != null) {
			quickbar.setSlot(dragSlotToAdd, slotIndex);
			Game.mainViewer.updateInterface();
		}
	}
}
