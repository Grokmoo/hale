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

package net.sf.hale;

import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;

public class Sprite {
	private int image;
	
	private final int width;
	private final int height;
	
	private final int offsetX, offsetY;
	private final int iconOffsetX, iconOffsetY;
	
	private final double texCoordStartX, texCoordStartY;
	private final double texCoordEndX, texCoordEndY;
	
	public Sprite(int im, int w, int h) {
		this.image = im;
		this.width = w;
		this.height = h;
		
		this.offsetX = (Game.TILE_SIZE - w) / 2;
		this.offsetY = (Game.TILE_SIZE - h) / 2;
		
		this.iconOffsetX = (Game.ICON_SIZE - w) / 2;
		this.iconOffsetY = (Game.ICON_SIZE - h) / 2;
		
		texCoordStartX = 0.0;
		texCoordStartY = 0.0;
		texCoordEndX = 1.0;
		texCoordEndY = 1.0;
	}
	
	public Sprite(int image, int width, int height, double texCoordStartX, double texCoordStartY,
				  double texCoordEndX, double texCoordEndY) {
		
		this.image = image;
		this.width = width;
		this.height = height;
		
		this.offsetX = (Game.TILE_SIZE - width) / 2;
		this.offsetY = (Game.TILE_SIZE - height) / 2;
		
		this.iconOffsetX = (Game.ICON_SIZE - width) / 2;
		this.iconOffsetY = (Game.ICON_SIZE - height) / 2;
		
		this.texCoordStartX = texCoordStartX;
		this.texCoordStartY = texCoordStartY;
		this.texCoordEndX = texCoordEndX;
		this.texCoordEndY = texCoordEndY;
	}
	
	/**
	 * This method should only be called by the AsyncTextureLoader.  Calling it
	 * anywhere else is not recommended.
	 * @param texture the integer representing the OpenGL texture for this Sprite
	 */
	
	public void setTexture(int texture) {
		this.image = texture;
	}
	
	public double getTexCoordStartX() { return texCoordStartX; }
	public double getTexCoordStartY() { return texCoordStartY; }
	public double getTexCoordEndX() { return texCoordEndX; }
	public double getTexCoordEndY() { return texCoordEndY; }
	
	public int getTextureImage() { return image; }
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public int getOffsetX() { return offsetX; }
	public int getOffsetY() { return offsetY; }
	
	public final void draw(int x, int y, float w, float h) {
		int width = (int) ((float)this.width * w);
		int height = (int) ((float)this.height * h);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, image);
		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2d(texCoordStartX, texCoordStartY);
		GL11.glVertex2i(x, y);
		
		GL11.glTexCoord2d(texCoordEndX * w, texCoordStartY);
		GL11.glVertex2i(x + width, y);
		
		GL11.glTexCoord2d(texCoordEndX * w, texCoordEndY * h);
		GL11.glVertex2i(x + width, y + height);
		
		GL11.glTexCoord2d(texCoordStartX, texCoordEndY * h);
		GL11.glVertex2i(x, y + height);
		
		GL11.glEnd();
	}
	
	public final void drawNoTextureBind(int x, int y) {
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2d(texCoordStartX, texCoordStartY);
		GL11.glVertex2i(x, y);
		
		GL11.glTexCoord2d(texCoordEndX, texCoordStartY);
		GL11.glVertex2i(x + width, y);
		
		GL11.glTexCoord2d(texCoordEndX, texCoordEndY);
		GL11.glVertex2i(x + width, y + height);
		
		GL11.glTexCoord2d(texCoordStartX, texCoordEndY);
		GL11.glVertex2i(x, y + height);
		
		GL11.glEnd();
	}
	
	public final void draw(int x, int y) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, image);
		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2d(texCoordStartX, texCoordStartY);
		GL11.glVertex2i(x, y);
		
		GL11.glTexCoord2d(texCoordEndX, texCoordStartY);
		GL11.glVertex2i(x + width, y);
		
		GL11.glTexCoord2d(texCoordEndX, texCoordEndY);
		GL11.glVertex2i(x + width, y + height);
		
		GL11.glTexCoord2d(texCoordStartX, texCoordEndY);
		GL11.glVertex2i(x, y + height);
		
		GL11.glEnd();
	}
	
	public final void draw(Point p) { draw(p.x, p.y); }
	public final void drawWithOffset(Point p) { draw(p.x + offsetX, p.y + offsetY); }
	public final void drawWithOffset(int x, int y) { draw(x + offsetX, y + offsetY); }
	public final void drawWithIconOffset(int x, int y) {draw(x + iconOffsetX, y + iconOffsetY); }
}
