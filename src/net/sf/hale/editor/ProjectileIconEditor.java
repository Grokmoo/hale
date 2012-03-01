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

package net.sf.hale.editor;

import net.sf.hale.Game;
import net.sf.hale.editor.widgets.ColorSelectorPopup;
import net.sf.hale.editor.widgets.IconSelectorPopup;
import net.sf.hale.editor.widgets.SpriteViewer;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.SpriteManager;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;

/**
 * A widget for editing the icon and color for the projectile icon for an item
 * @author Jared Stephen
 *
 */

public class ProjectileIconEditor extends DialogLayout implements IconSelectorPopup.Callback {
	private Item item;
	
	private Label label;
	private Button setColor;
	private Button setIcon;
	private SpriteViewer iconViewer;
	
	/**
	 * Creates a new ProjectileIconEditor
	 */
	
	public ProjectileIconEditor() {
		setTheme("");
		
		label = new Label("Projectile Icon");
		label.setTheme("/labelblack");
		
		setColor = new Button("Set Color");
		setColor.addCallback(new Runnable() {
			@Override public void run() {
				ColorSelectorPopup popup = new ColorSelectorPopup(ProjectileIconEditor.this);
				popup.setProjectileIconColorOnAccept(item);
				popup.setSpriteViewerSpriteColorOnAccept(iconViewer);
				popup.openPopupCentered();
			}
		});
		setColor.setTheme("/button");
		
		setIcon = new Button("Set Icon");
		setIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(ProjectileIconEditor.this, "images/subIcons",
						45, true, 1, null, "projectile");
				popup.setCallback(ProjectileIconEditor.this);
				popup.openPopupCentered();
			}
		});
		setIcon.setTheme("/button");
		
		iconViewer = new SpriteViewer(Game.ICON_SIZE, Game.ICON_SIZE, 1);
		iconViewer.setSelected(true);
		
		Group mainH = createParallelGroup(label);
		Group mainV = createSequentialGroup(label);
		
		Group row2H = createSequentialGroup(iconViewer);
		Group row2V = createParallelGroup(iconViewer);
		
		row2H.addGroup(createParallelGroup(setIcon, setColor));
		row2V.addGroup(createSequentialGroup(setIcon, setColor));
		
		mainH.addGroup(row2H);
		mainV.addGroup(row2V);
		
		setHorizontalGroup(mainH);
		setVerticalGroup(mainV);
	}

	@Override public void iconSelected(String icon) {
		if (item == null) return;

		item.setProjectileIcon(icon);
		iconViewer.setSprite(SpriteManager.getSprite(icon));
		iconViewer.setSpriteColor(item.getProjectileIconColor());
	}

	public void getPropertiesFromItem(Item item) {
		this.item = item;
		
		if (item.getProjectileIcon() != null) {
			iconViewer.setSprite(SpriteManager.getSprite(item.getProjectileIcon()));
			iconViewer.setSpriteColor(item.getProjectileIconColor());
		} else {
			iconViewer.setSprite(null);
			iconViewer.setSpriteColor(null);
		}
	}
}
