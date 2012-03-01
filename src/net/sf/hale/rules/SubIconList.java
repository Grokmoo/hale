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

package net.sf.hale.rules;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.hale.Sprite;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;

public class SubIconList implements Iterable<SubIcon> {
	private final List<SubIcon> subIcons;
	
	private Color skinColor;
	private Color clothingColor;
	
	private SubIcon beard;
	private SubIcon hair;
	private SubIcon ears;
	
	public SubIconList() {
		subIcons = new LinkedList<SubIcon>();
		
		skinColor = Color.WHITE;
		clothingColor = Color.WHITE;
	}
	
	public SubIconList(SubIconList other) {
		subIcons = new LinkedList<SubIcon>(other.subIcons);
		
		beard = other.beard;
		hair = other.hair;
		ears = other.ears;
		
		skinColor = other.skinColor;
		clothingColor = other.clothingColor;
	}
	
	public void setSkinColor(Color color) {
		this.skinColor = color;
	}
	
	public void setClothingColor(Color color) {
		this.clothingColor = color;
	}
	
	public Color getSkinColor() {
		return skinColor;
	}
	
	public Color getClothingColor() {
		return clothingColor;
	}
	
	public synchronized void add(SubIcon subIcon) {
		if (subIcon == null) return;
		
		if (subIcon.getIcon() == null) return;
		
		remove(subIcon.getType());
		
		subIcons.add(subIcon);
		
		Collections.sort(subIcons);
		
		switch (subIcon.getType()) {
		case Ears:
			ears = subIcon;
			break;
		case Beard:
			beard = subIcon;
			break;
		case Hair:
			hair = subIcon;
			break;
		case Head:
			remove(SubIcon.Type.Ears);
			remove(SubIcon.Type.Hair);
			break;
		}
		
		if (subIcon.coversBeard())
			remove(SubIcon.Type.Beard);
	}
	
	/**
	 * Called to manually recache all sprites in all sub icons in this sub icon list
	 */
	
	public void cacheSprites() {
		for (SubIcon icon : subIcons) {
			icon.cacheSprites();
		}
		
		if (hair != null) hair.cacheSprites();
		if (ears != null) ears.cacheSprites();
		if (beard != null) beard.cacheSprites();
	}
	
	public synchronized void clear() {
		subIcons.clear();
		hair = null;
		ears = null;
		beard = null;
	}
	
	public synchronized void remove(SubIcon.Type type) {
		Iterator<SubIcon> iter = subIcons.iterator();
		while (iter.hasNext()) {
			if (iter.next().getType() == type) {
				iter.remove();
			}
		}
		
		if (type == SubIcon.Type.Head && hair != null) {
			add(this.hair);
			add(this.ears);
		}
		
		if (type != SubIcon.Type.Beard) { 
			boolean addBeard = true;
			for (SubIcon subIcon : subIcons) {
				if (subIcon.coversBeard()) {
					addBeard = false;
					break;
				}

				if (subIcon.getType() == SubIcon.Type.Beard) {
					addBeard = false;
					break;
				}
			}

			if (addBeard && beard != null)
				add(this.beard);
		}
	}
	
	/**
	 * Returns the beard icon, even if the beard icon is not currently being
	 * drawn
	 * @return the beard icon
	 */
	
	public String getBeardIcon() {
		if (beard == null) return null;
		
		return beard.getIcon();
	}
	
	/**
	 * Returns the beard color, even if the beard icon is not currently being drawn
	 * @return the beard color
	 */
	
	public Color getBeardColor() {
		if (beard == null) return null;
		
		return beard.getColor();
	}
	
	/**
	 * Returns the hair icon, even if a helmet is equipped and the hair icon is not currently
	 * being drawn
	 * @return the hair icon
	 */
	
	public String getHairIcon() {
		if (hair == null) return null;
		
		return hair.getIcon();
	}
	
	/**
	 * Returns the hair color, even if a helmet is equipped and the hair icon is not currently
	 * being drawn
	 * @return the hair color
	 */
	
	public Color getHairColor() {
		if (hair == null) return null;
		
		return hair.getColor();
	}
	
	public Point getOffset(String type) { return getOffset(SubIcon.Type.valueOf(type)); }
	public Sprite getSprite(String type) { return getSprite(SubIcon.Type.valueOf(type)); }
	public Color getColor(String type) { return getColor(SubIcon.Type.valueOf(type)); }
	public String getIcon(String type) { return getIcon(SubIcon.Type.valueOf(type)); }
	
	public Point getOffset(SubIcon.Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon.getOffset();
		}
		
		return new Point(0, 0);
	}
	
	public Sprite getSprite(SubIcon.Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon.getSprite();
		}
		
		return null;
	}
	
	public Color getColor(SubIcon.Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon.getColor();
		}
		
		return null;
	}
	
	public String getIcon(SubIcon.Type type) {
		for (SubIcon subIcon : subIcons) {
			if (subIcon.getType() == type) return subIcon.getIcon();
		}
		
		return null;
	}
	
	public synchronized final void draw(int x, int y) {
		for (SubIcon subIcon : subIcons) {
			subIcon.draw(x, y);
		}
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
	}
	
	@Override public Iterator<SubIcon> iterator() {
		return subIcons.iterator();
	}
}
