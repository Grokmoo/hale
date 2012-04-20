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

import net.sf.hale.Sprite;
import net.sf.hale.ability.Ability;
import net.sf.hale.ability.AbilityActivator;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.InventoryCallbackFactory;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Merchant;
import net.sf.hale.view.DragAndDropHandler;
import net.sf.hale.view.DragTarget;
import net.sf.hale.view.DropTarget;
import net.sf.hale.view.ItemListViewer;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 * A widget for viewing the icon and quantity associated with a given item.
 * 
 * @author Jared Stephen
 *
 */

public class ItemIconViewer extends IconViewer implements DragTarget, DropTarget {
	public static final StateKey STATE_PROF_NOT_MET = StateKey.get("profNotMet");
	public static final StateKey STATE_UNAFFORDABLE = StateKey.get("unaffordable");
	
	// stored by this viewer for use in the right click callback
	private int itemIndex;
	
	private ItemListViewer listViewer;
	private Merchant merchant;
	private Container container;
	private Creature parent;
	
	private Item item;
	private int quantity;
	
	private DragAndDropHandler dragAndDropHandler;
	
	private Label numberLabel;
	
	private Listener callback;
	
	private boolean isHovering;
	
	/**
	 * Creates a new ItemIconViewer for the specified Item with the specified
	 * quantity.  If the quantity is greater than 1, a label is shown with
	 * the item quantity on top of the IconViewer.
	 */
	
	public ItemIconViewer(ItemListViewer listViewer) {
		this.listViewer = listViewer;
		
		numberLabel = new Label();
		numberLabel.setTheme("quantitylabel");
		add(numberLabel);
	}
	
	/**
	 * Returns the text that this widget should show when it is empty
	 * (doesn't contain an item) and is hovered over
	 * @return the empty hover text
	 */
	
	public String getEmptyHoverText() {
		return "";
	}
	
	/**
	 * Sets the item index that is used by RightClickCallbacks as the index
	 * of this Item within the ItemList being viewed
	 * @param itemIndex the item index for the Item being viewed
	 */
	
	public void setItemIndex(int itemIndex) {
		this.itemIndex = itemIndex;
	}
	
	/**
	 * Returns the index that was set by {@link #setItemIndex(int)} or 0 if
	 * no index was set
	 * @return the item index for this item
	 */
	
	public int getItemIndex() {
		return itemIndex;
	}
	
	/**
	 * Returns the Item being viewed by this IconViewer or null if no item has
	 * been set
	 * @return the Item being viewed by this IconViewer
	 */
	
	@Override public Item getItem() {
		return item;
	}
	
	/**
	 * Returns the quantity for the Item being viewed by this IconViewer or 0 if there
	 * is no item being viewed
	 * @return the quantity for the Item being viewed by this IconViewer
	 */
	
	public int getQuantity() {
		return quantity;
	}
	
	/**
	 * Sets the item currently being viewed by this IconViewer and the quantity
	 * displayed on the quantity label
	 * @param item the item to view the icon of
	 * @param quantity the quantity to display on the quantity label.  The quantity will only be displayed
	 * if it is greater than 1
	 * @param parent the parent of the item if the item is in a specific creature's inventory, null otherwise
	 * @param container the owning container of this item or null if it is not in a container
	 * @param merchant the owning merchant for this item or null if it is not owned by a merchant
	 */
	
	public void setItem(Item item, int quantity, Creature parent, Container container, Merchant merchant) {
		if (item == this.item && quantity == this.quantity)
			return;
		
		this.container = container;
		this.merchant = merchant;
		this.parent = parent;
		this.item = item;
		this.quantity = quantity;
		
		if (item != null) {
			this.setColor(item.getIconColor());
			this.setSprite(SpriteManager.getSprite(item.getIcon()));
			
		} else {
			this.setColor(Color.WHITE);
			this.setSprite(null);
			
			setTooltipContent(null);
		}
		
		if (quantity <= 1) {
			numberLabel.setText("");
		} else if (quantity == Integer.MAX_VALUE) {
			// the unicode infinity symbol
			numberLabel.setText("\u221E");
		} else {
			numberLabel.setText(Integer.toString(quantity));
		}
	}
	
	@Override protected void layout() {
		super.layout();
		
		Widget qtyLabel = getChild(0);
		qtyLabel.setPosition(getInnerX(), getInnerBottom() - qtyLabel.getPreferredHeight() / 2);
	}
	
	@Override public int getPreferredWidth() {
		int width = getBorderHorizontal();
		Sprite sprite = this.getSprite();
		if (sprite != null) width += sprite.getWidth();
		
		return width;
	}
	
	@Override public int getPreferredHeight() {
		int height = getBorderVertical();
		Sprite sprite = this.getSprite();
		if (sprite != null) height += sprite.getHeight();
		
		return height;
	}
	
	/**
	 * Sets the "profNotMet" animation state for this Widget to the specified value
	 * @param prof the value for the "profNotMet" animation state
	 */
	
	public void setStateProficiencies(boolean prof) {
		getAnimationState().setAnimationState(STATE_PROF_NOT_MET, prof);
	}
	
	/**
	 * Sets the unaffordable state for this Widget to the specified value
	 * @param unaffordable whether the Widget should have the "unaffordable"
	 * theme animation state set
	 */
	
	public void setStateUnaffordable(boolean unaffordable) {
		getAnimationState().setAnimationState(STATE_UNAFFORDABLE, unaffordable);
	}
	
	/**
	 * Sets the listener that will be called when this Widget is right
	 * clicked and the menu should be created, or on mouse hover events
	 * @param callback the callback
	 */
	
	public void setListener(Listener callback) {
		this.callback = callback;
	}
	
	@Override protected boolean handleEvent(Event evt) {
		if (evt.isMouseEvent()) {
			boolean hover = (evt.getType() != Event.Type.MOUSE_EXITED) && isMouseInside(evt);
			if (hover && !isHovering) {
				if (callback != null) callback.hoverStarted(this);
				isHovering = true;
			} else if (!hover && isHovering) {
				if (callback != null) callback.hoverEnded(this);
				isHovering = false;
			}
		}
		
		switch (evt.getType()) {
		case MOUSE_DRAGGED:
			if (dragAndDropHandler == null && item != null) {
				dragAndDropHandler = new DragAndDropHandler(this);
			}
			break;
		case MOUSE_BTNUP:
			if (!isMouseInside(evt)) break;
			switch (evt.getMouseButton()) {
			case Event.MOUSE_LBUTTON:
				if (item != null) {
					InventoryCallbackFactory.ExamineDetailsCallback cb =
						new InventoryCallbackFactory.ExamineDetailsCallback(item, evt.getMouseX(), evt.getMouseY());
					cb.run();
				}
				break;
			case Event.MOUSE_RBUTTON:
				if (callback != null) {
					callback.rightClicked(this, evt.getMouseX(), evt.getMouseY());
				}
				break;
			}
		case POPUP_OPENED:
			if (isHovering) {
				if (callback != null) callback.hoverEnded(this);
				isHovering = false;
			}
		}
		
		if (dragAndDropHandler != null) {
			if (!dragAndDropHandler.handleEvent(evt))
				dragAndDropHandler = null;
		}
		
		return super.handleEvent(evt);
	}
	
	/**
	 * The interface for a listener that wants to be notified on right click
	 * to create a menu, or when hovering
	 * @author Jared Stephen
	 *
	 */
	
	public interface Listener {
		/**
		 * Called when this Widget is right clicked and a menu should be created
		 * @param viewer this ItemIconViewer
		 * @param x the x coordinate of the mouse
		 * @param y the y coordinate of the mouse
		 */
		public void rightClicked(ItemIconViewer viewer, int x, int y);
		
		/**
		 * Called when the mouse enters this widget
		 * @param viewer the viewer being hovered
		 */
		
		public void hoverStarted(ItemIconViewer viewer);
		
		/**
		 * Called when the mouse exits this widget
		 * @param viewer the viewer being hovered
		 */
		
		public void hoverEnded(ItemIconViewer viewer);
	}

	@Override public Sprite getDragSprite() {
		return SpriteManager.getSprite(item.getIcon());
	}

	@Override public Color getDragSpriteColor() {
		return item.getIconColor();
	}

	@Override public Ability getAbility() { return null; }
	@Override public AbilityActivator getAbilityParent() { return null; }
	@Override public int getItemEquipSlot() { return -1; }
	
	@Override public Creature getItemParent() {
		return parent;
	}

	@Override public Container getItemContainer() {
		return container;
	}

	@Override public Merchant getItemMerchant() {
		return merchant;
	}

	@Override public void dragAndDropStartHover(DragTarget target) {
		listViewer.dragAndDropStartHover(target);
	}

	@Override public void dragAndDropStopHover(DragTarget target) {
		listViewer.dragAndDropStopHover(target);
	}

	@Override public void dropDragTarget(DragTarget target) {
		listViewer.dropDragTarget(target);
	}
}