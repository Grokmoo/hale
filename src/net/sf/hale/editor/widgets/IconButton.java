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

import java.util.LinkedList;
import java.util.List;

import net.sf.hale.resource.SpriteManager;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.utils.TintAnimator;

public class IconButton extends Button implements Runnable {
	private final List<Callback> callbacks;
	
	private boolean drawWithIconOffset = true;
	private boolean drawWithTileOffset = false;
	
	private String icon;
	private final Label label;
	
	private String secondaryIcon;
	private final Label secondaryLabel;
	
	private float colorMultiple = 1.0f;
	
	public IconButton(String labelText) {
		super();
		this.setTheme("/iconbutton");
		this.addCallback(this);
		
		label = new Label(labelText);
		label.setTheme("/labelbig");
		label.setPosition(30, 5);
		label.setTintAnimator(new TintAnimator(new TintAnimator.GUITimeSource(this), new Color(0xFF009300)));
		this.add(label);
		
		secondaryLabel = new Label();
		secondaryLabel.setTheme("/labelbig");
		secondaryLabel.setPosition(2, 34);
		secondaryLabel.setTintAnimator(new TintAnimator(new TintAnimator.GUITimeSource(this), new Color(0xFF00FF00)));
		this.add(secondaryLabel);
		
		callbacks = new LinkedList<Callback>();
	}
	
	public void setDrawWithIconOffset(boolean useIconOffset) {
		this.drawWithIconOffset = useIconOffset;
	}
	
	public void setDrawWithTileOffset(boolean useTileOffset) {
		this.drawWithTileOffset = useTileOffset;
	}
	
	public void setSecondaryText(String text) {
		secondaryLabel.setText(text);
	}
	
	public void setSecondaryIcon(String icon) {
		this.secondaryIcon = icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public void setColorMultiple(float colorMultiple) {
		this.colorMultiple = colorMultiple;
	}
	
	@Override public void paintWidget(GUI gui) {
		GL11.glColor3f(1.0f * colorMultiple, 1.0f * colorMultiple, 1.0f * colorMultiple);
		
		int offset = 0;
		
		if (secondaryIcon != null) {
			offset = 5;
			if (drawWithIconOffset) 
				SpriteManager.getSprite(secondaryIcon).drawWithIconOffset(getInnerX() + offset, getInnerY());
			else
				SpriteManager.getSprite(secondaryIcon).draw(getInnerX() + offset, getInnerY());
		}
		
		if (icon != null) {
			if (drawWithIconOffset)
				SpriteManager.getSprite(icon).drawWithIconOffset(getInnerX() - offset, getInnerY());
			else if (drawWithTileOffset)
				SpriteManager.getSprite(icon).drawWithOffset(getInnerX() - offset, getInnerY());
			else
				SpriteManager.getSprite(icon).draw(getInnerX() - offset, getInnerY());
		}
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
		super.paintWidget(gui);
	}
	
	public void addCallback(Callback callback) {
		callbacks.add(callback);
	}
	
	@Override public void run() {
		for (Callback callback : callbacks) {
			callback.buttonLeftClicked();
		}
	}
	
	@Override public boolean handleEvent(Event evt) {
		if (evt.getType() == Event.Type.MOUSE_BTNUP && evt.getMouseButton() == Event.MOUSE_RBUTTON) {
			for (Callback callback : callbacks) {
				callback.buttonRightClicked();
			}
		}
		
		return super.handleEvent(evt);
	}
	
	public interface Callback {
		public void buttonLeftClicked();
		public void buttonRightClicked();
	}
}
