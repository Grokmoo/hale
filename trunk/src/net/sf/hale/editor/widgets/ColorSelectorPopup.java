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

package net.sf.hale.editor.widgets;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.ColorSelector;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.ColorSpaceHSL;

public class ColorSelectorPopup extends PopupWindow {
	private final DialogLayout content;
	private final ColorSelector colorSelector;
	
	private Entity setIconColorEntity;
	private Item setSubIconColorItem;
	private Item setProjectileIconColorItem;
	private SpriteViewer setSpriteColorViewer;
	private Creature setHairIconCreature;
	
	private List<Callback> callbacks;
	
	public void clearCallbacks() {
		this.setIconColorEntity = null;
		this.setSubIconColorItem = null;
		this.setSpriteColorViewer = null;
		this.setHairIconCreature = null;
		this.setProjectileIconColorItem = null;
		
		this.callbacks.clear();
	}
	
	public void setProjectileIconColorOnAccept(Item item) {
		this.setProjectileIconColorItem = item;
	}
	
	public void setEntityIconColorOnAccept(Entity entity) {
		this.setIconColorEntity = entity;
	}
	
	public void setHairIconColorOnAccept(Creature creature) {
		this.setHairIconCreature = creature;
	}
	
	public void setSpriteViewerSpriteColorOnAccept(SpriteViewer viewer) {
		this.setSpriteColorViewer = viewer;
	}
	
	public void setSubIconColorOnAccept(Item item) {
		this.setSubIconColorItem = item;
	}
	
	public void addCallback(Callback callback) {
		this.callbacks.add(callback);
	}
	
	public ColorSelectorPopup(Widget parent) {
		super(parent);
		
		this.callbacks = new ArrayList<Callback>();
		
		this.setTheme("filepopup");
		this.setCloseOnClickedOutside(false);
		
		content = new DialogLayout();
		content.setTheme("");
		this.add(content);
		
		Label titleLabel = new Label("Select a Color");
		
		colorSelector = new ColorSelector(new ColorSpaceHSL());
		colorSelector.setTheme("/colorselector");
		colorSelector.setUseColorArea2D(true);
		colorSelector.setUseLabels(true);
		colorSelector.setShowPreview(true);
		colorSelector.setShowAlphaAdjuster(false);
		
		Button accept = new Button("Accept");
		accept.addCallback(new Runnable() {
			@Override public void run() {
				Color color = colorSelector.getColor();
				
				if (setSubIconColorItem != null) {
					setSubIconColorItem.setSubIconColor(color);
				}
				
				if (setSpriteColorViewer != null) {
					setSpriteColorViewer.setSpriteColor(color);
				}
				
				if (setHairIconCreature != null) {
					setHairIconCreature.setHairSubIcon(setHairIconCreature.getHairIcon(), color);
				}
				
				if (setIconColorEntity != null) {
					setIconColorEntity.setIconColor(color);
				}
				
				if (setProjectileIconColorItem != null) {
					setProjectileIconColorItem.setProjectileIconColor(color);
				}
				
				for (Callback callback : callbacks) {
					callback.colorSelected(color);
				}
				
				ColorSelectorPopup.this.closePopup();
			}
		});
		
		Button cancel = new Button("Cancel");
		cancel.addCallback(new Runnable() {
			@Override public void run() {
				ColorSelectorPopup.this.closePopup();
			}
		});
		
		Group verticalButtons = content.createParallelGroup(accept, cancel);
		Group horizontalButtons = content.createSequentialGroup(accept, cancel);
		
		Group vertical = content.createSequentialGroup(titleLabel);
		vertical.addGap(10);
		vertical.addWidget(colorSelector);
		vertical.addGap(10);
		vertical.addGroup(verticalButtons);
		
		Group horizontal = content.createParallelGroup(titleLabel);
		horizontal.addWidget(colorSelector);
		horizontal.addGroup(horizontalButtons);
		
		content.setVerticalGroup(vertical);
		content.setHorizontalGroup(horizontal);
	}
	
	public interface Callback {
		public void colorSelected(Color color);
	}
}
