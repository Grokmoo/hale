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
import net.sf.hale.bonus.Stat;
import net.sf.hale.characterbuilder.Buildable;
import net.sf.hale.characterbuilder.CharacterBuilder;
import net.sf.hale.defaultability.Select;
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Inventory;
import net.sf.hale.rules.Merchant;
import net.sf.hale.rules.XP;
import net.sf.hale.view.DragAndDropHandler;
import net.sf.hale.view.DragTarget;
import net.sf.hale.view.DropTarget;
import net.sf.hale.view.ItemListViewer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.TextWidget;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.renderer.Image;

/**
 * A Widget for displaying the portrait of a party member, name, and their most important
 * associated stats - name, Hit Points (HP) and Action Points (AP)
 * @author Jared Stephen
 *
 */

public class PortraitViewer extends BasePortraitViewer implements Runnable, DropTarget {
	private PortraitArea portraitArea;
	
	private int nameOverlap;
	
	private final Label name;
	
	private final LevelUpButton levelUp;
	
	private StatBar apBar, hpBar;
	
	// the currently open level up window if it exists
	private CharacterBuilder builder;
	
	/**
	 * Creates a new PortraitViewer showing the portrait of the specified Creature
	 * @param creature the Creature to show the portrait of
	 */
	
	public PortraitViewer(Creature creature, PortraitArea portraitArea) {
		super(creature);
		this.portraitArea = portraitArea;
		
		name = new Label(creature.getName());
		name.setTheme("namelabel");
		add(name);
		
		hpBar = new StatBar();
		hpBar.setTheme("hpbar");
		add(hpBar);
		
		apBar = new StatBar();
		apBar.setTheme("apbar");
		add(apBar);
		
		levelUp = new LevelUpButton();
		levelUp.setTheme("levelupbutton");
		levelUp.addCallback(new Runnable() {
			@Override public void run() {
				builder = new CharacterBuilder(new Buildable(getCreature()));
		        Game.mainViewer.add(builder);
		        builder.addFinishCallback(new CharacterBuilder.FinishCallback() {
		        	@Override public void creatureModified(String id) {
		        		Game.mainViewer.updateInterface();
		        		builder = null;
		        	}
		        });
			}
		});
		add(levelUp);
		
		addCallback(this);
		setEnableEventHandling(true);
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		nameOverlap = themeInfo.getParameter("nameOverlap", 0);
		
		levelUp.getAnimationState().setAnimationState(MainPane.STATE_NOTIFICATION, true);
	}
	
	@Override public int getPreferredHeight() {
		int height = super.getPreferredHeight();

		height += name.getPreferredHeight() - nameOverlap;
		height += apBar.getPreferredHeight();
		height += hpBar.getPreferredHeight();
		
		return height;
	}
	
	@Override protected void layout() {
		super.layout();
		
		int centerX = getInnerX() + getInnerWidth() / 2;
		
		name.setSize(name.getPreferredWidth(), name.getPreferredHeight());
		name.setPosition(centerX - name.getWidth() / 2, getInnerY());
		
		this.setPortraitY(name.getHeight() - nameOverlap);
		int spriteHeight = this.getPortraitSpriteHeight();
		
		apBar.setSize(apBar.getPreferredWidth(), apBar.getPreferredHeight());
		hpBar.setSize(hpBar.getPreferredWidth(), hpBar.getPreferredHeight());
		
		hpBar.setPosition(centerX - hpBar.getWidth() / 2, name.getBottom() + spriteHeight - nameOverlap);
		apBar.setPosition(centerX - apBar.getWidth() / 2, hpBar.getBottom());
		
		levelUp.setSize(levelUp.getPreferredWidth(), levelUp.getPreferredHeight());
		levelUp.setPosition(centerX - levelUp.getWidth() / 2, hpBar.getY() - levelUp.getHeight() - 1);
	}
	
	/**
	 * Closes any level up (CharacterBuilder) window associated with this
	 * PortraitViewer
	 */
	
	public void closeLevelUpWindow() {
		if (builder != null && builder.getParent() != null) {
			builder.getParent().removeChild(builder);
			builder = null;
		}
	}
	
	/**
	 * Sets the level up button on this Widget to the specified enabled state
	 * @param enabled the enabled state for the level up button
	 */
	
	public void setLevelUpEnabled(boolean enabled) {
		levelUp.setEnabled(enabled);
	}
	
	/**
	 * Updates all the label text, ap bar, and hp bar of this PortraitViewer with
	 * any changes to the creature being viewed
	 */
	
	public void updateContent() {
		Creature creature = getCreature();
		
		setActive(creature.isSelected());
		
		hpBar.setText("HP: " + creature.getCurrentHP() + "/" + creature.stats().get(Stat.MaxHP));
		
		if (creature.isDead()) {
			apBar.setText("Dead");
			hpBar.setText("");
		} else if (creature.isDying()) {
			apBar.setText("Dying");
		} else {
			apBar.setText("AP: " + (creature.getTimer().getAP() / 100));
		}
		
		int charLevel = creature.stats().get(Stat.CreatureLevel);
		int xpForNext = XP.getPointsForLevel(charLevel + 1);
		if (creature.getExperiencePoints() >= xpForNext) {
			levelUp.setVisible(true);
		} else {
			levelUp.setVisible(false);
		}
		
		float healthWidth = Math.min( ((float)creature.getCurrentHP()) / ((float)creature.stats().get(Stat.MaxHP)), 1.0f );
		float apWidth = Math.min(((float)creature.getTimer().getAP()) / (10000.0f), 1.0f);
		
		hpBar.setValue(healthWidth);
		apBar.setValue(apWidth);
	}
	
	// button clicked callback
	@Override public void run() {
		setActive(true);
		Select.selectCreature(getCreature());
	}
	
	@Override protected boolean handleEvent(Event evt) {
		// don't handle events during interface lock
		if (Game.interfaceLocker.locked()) return false;
		
		switch (evt.getType()) {
		case MOUSE_DRAGGED:
			portraitArea.checkMouseDrag(this, evt);
			break;
		case MOUSE_BTNUP:
			portraitArea.checkMouseDragRelease(this);
			break;
		}
		
		return super.handleEvent(evt);
	}
	
	@Override protected boolean isMouseInside(Event evt) {
		return super.isMouseInside(evt);
	}
	
	private class LevelUpButton extends Button {
		private String disabledTooltip;
		
		@Override protected void applyTheme(ThemeInfo themeInfo) {
			super.applyTheme(themeInfo);
			
			disabledTooltip = themeInfo.getParameter("disabledtooltip", (String)null);
		}
		
		@Override public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			
			if (enabled) {
				setTooltipContent("Level up " + getCreature().getName());
			} else {
				setTooltipContent(disabledTooltip);
			}
		}
	}
	
	private class StatBar extends TextWidget {
		private Image fullImage;
		private Image emptyImage;
		private float value;
		
		private void setText(String text) {
			setCharSequence(text);
		}
		
		@Override protected void applyTheme(ThemeInfo themeInfo) {
			super.applyTheme(themeInfo);
			
			this.fullImage = themeInfo.getImage("fullImage");
			this.emptyImage = themeInfo.getImage("emptyImage");
		}
		
		private void setValue(float value) {
			this.value = value;
		}
		
		@Override public int getPreferredWidth() {
			Image bg = getBackground();
			
			return bg != null ? bg.getWidth() : 0;
		}
		
		@Override public int getPreferredHeight() {
			Image bg = getBackground();
			
			return bg != null ? bg.getHeight() : 0;
		}
		
		@Override protected void paint(GUI gui) {
			if (fullImage != null && emptyImage != null) {
				int cutOff = (int)(fullImage.getWidth() * value);
				
				fullImage.draw(getAnimationState(), getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
				emptyImage.draw(getAnimationState(), getInnerX() + cutOff, getInnerY(),
						getInnerWidth() - cutOff, getInnerHeight());
			}
			
			paintBackground(gui);
			paintWidget(gui);
			paintChildren(gui);
			paintOverlay(gui);
		}
	}

	@Override public void dragAndDropStartHover(DragTarget target) {
		if (target.getItemParent() == getCreature()) return;
		
		if (target.getItem() != null) {
			if (target.getItemParent() != null) {
				// an attempt at a give drag & drop
				getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, true);
			} else if (target.getItemContainer() != null && Game.mainViewer.containerWindow.getOpener() == getCreature()) {
				// an attempt at a pick up drag & drop
				getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, true);
			} else if (target.getItemMerchant() != null) {
				// an attempt at a buy drag and drop
				getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, true);
			}
		}
	}

	@Override public void dragAndDropStopHover(DragTarget target) {
		getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, false);
	}

	@Override public void dropDragTarget(DragTarget target) {
		if (target.getItemParent() == getCreature()) return;
		
		
		if (target.getItem() != null) {
			Creature parent = getCreature();
			
			if (target.getItemParent() != null) {
				// attempt give drag & drop
				Inventory givingInventory = target.getItemParent().getInventory();
				
				if (target.getItemEquipSlot() != -1) {
					givingInventory.getCallbackFactory().getGiveEquippedCallback(target.getItemEquipSlot(), parent).run();
				} else {
					int quantity = givingInventory.getUnequippedItems().getQuantity(target.getItem());
					givingInventory.getCallbackFactory().getGiveCallback(target.getItem(), quantity, parent).run();
				}
				
			} else if (target.getItemContainer() != null && Game.mainViewer.containerWindow.getOpener() == getCreature()) {
				// attempt pick up drag & drop, only for the container opener
				Container container = target.getItemContainer();
				
				int containerIndex = container.getItems().findItem(target.getItem());
				int quantity = container.getItems().getQuantity(containerIndex);
				
				parent.getInventory().getCallbackFactory().getTakeCallback(target.getItem(), containerIndex, quantity).run();
			} else if (target.getItemMerchant() != null) {
				// attempt buy drag & drop
				
				Merchant merchant = target.getItemMerchant();
				int merchantQuantity = target.getItemMerchant().getCurrentItems().getQuantity(target.getItem());
				int maxQuantity = ItemListViewer.getMerchantBuyMaxQuantity(merchant, target.getItem(), merchantQuantity);
					
				parent.getInventory().getCallbackFactory().getBuyCallback(target.getItem(), target.getItemMerchant(), maxQuantity).run();
			}
		}
		
		getAnimationState().setAnimationState(DragAndDropHandler.STATE_DRAG_HOVER, false);
	}
}
