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

import net.sf.hale.Sprite;
import net.sf.hale.editor.EventCallback;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

public class SpriteViewer extends Widget {
	private Sprite sprite;
	private boolean selected;
	private int scale = 1;
	private float invScale = 1.0f;
	
	private float red = 1.0f;
	private float green = 1.0f;
	private float blue = 1.0f;
	
	private Color spriteColor;
	
	private boolean iconOffset = true;
	
	private final List<EventCallback> callbacks;
	
	public SpriteViewer(Sprite sprite, int scale) {
		this(sprite);
		
		this.scale = scale;
		this.invScale = 1.0f / (float)scale;
		this.setSize(sprite.getWidth() / scale, sprite.getHeight() / scale);
		this.setMinSize(this.getWidth(), this.getHeight());
	}
	
	public SpriteViewer(Sprite sprite) {
		this.sprite = sprite;
		this.spriteColor = Color.WHITE;
		this.selected = false;
		
		this.setTheme("");
		this.setSize(sprite.getWidth(), sprite.getHeight());
		this.setMinSize(sprite.getWidth(), sprite.getHeight());
		
		callbacks = new ArrayList<EventCallback>();
	}
	
	public SpriteViewer(int x, int y, int scale) {
		this.sprite = null;
		this.spriteColor = Color.WHITE;
		this.selected = false;
		
		this.setTheme("");
		this.setMinSize(x, y);
		this.setSize(x, y);
		this.scale = scale;
		this.invScale = 1.0f / (float)scale;
		
		callbacks = new ArrayList<EventCallback>();
	}
	
	public void setIconOffset(boolean iconOffset) {
		this.iconOffset = iconOffset;
	}
	
	public Sprite getSprite() { return sprite; }
	
	// new sprite should be the same size as the old sprite
	
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}
	
	public void setSpriteColor(Color color) {
		if (color == null) spriteColor = Color.WHITE;
		else this.spriteColor = color;
	}
	
	public void setSelectedColor(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public void addCallback(EventCallback callback) {
		callbacks.add(callback);
	}
	
	public boolean isSelected() { return this.selected; }
	
	@Override protected boolean handleEvent(Event evt) {
		super.handleEvent(evt);
		
		if (evt.getType() == Event.Type.MOUSE_ENTERED) {
			return true;
		}
		
		if (evt.getType() == Event.Type.MOUSE_BTNDOWN && evt.getMouseButton() == Event.MOUSE_LBUTTON) {
			for (EventCallback callback : callbacks) {
				callback.handleEvent(evt, this);
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override public void paintWidget(GUI gui) {
		super.paintWidget(gui);
		
		GL11.glPushMatrix();
		GL11.glScalef(invScale, invScale, 1.0f);
		
		GL11.glColor4ub(spriteColor.getR(), spriteColor.getG(), spriteColor.getB(), spriteColor.getA());
		
		if (sprite != null) {
			if (iconOffset) sprite.drawWithIconOffset(getInnerX() * scale, getInnerY() * scale);
			else sprite.draw(getInnerX() * scale, getInnerY() * scale);
		}
		
		GL11.glPopMatrix();
		
		if (selected) {
			GL11.glColor3f(red, green, blue);
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LINE_SMOOTH);

			GL11.glBegin(GL11.GL_LINE_LOOP);

			GL11.glVertex2i(getInnerX() + 1, getInnerY() + 1);
			GL11.glVertex2i(getInnerX() + getWidth(), getInnerY() + 1);
			GL11.glVertex2i(getInnerX() + getWidth(), getInnerY() + getHeight());
			GL11.glVertex2i(getInnerX() + 1, getInnerY() + getHeight());

			GL11.glEnd();

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
		}
	}
}
