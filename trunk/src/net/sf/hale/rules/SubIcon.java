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

import net.sf.hale.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;

public class SubIcon implements Comparable<SubIcon> {
	public enum Type {
		Quiver,
		Cloak,
		BaseBackground,
		Boots,
		Torso,
		BaseForeground,
		Gloves,
		Hair,
		Ears,
		Head,
		Beard,
		Shield,
		MainHandWeapon,
		OffHandWeapon
	};
	
	private Color color;
	private Sprite sprite;
	private Color secondaryColor;
	private Sprite secondarySprite;
	
	private boolean coversBeard;
	private String icon;
	private String secondaryIcon;
	private Type type;
	private int order;
	private Point offset;
	
	private SubIcon() { }
	
	private void setOrder() {
		switch (type) {
		case Quiver: this.order = -20; break;
		case Cloak: this.order = -10; break;
		case BaseBackground: this.order = 0; break;
		case Torso: this.order = 10; break;
		case BaseForeground: this.order = 20; break;
		case Gloves: this.order = 30; break;
		case Hair: this.order = 40; break;
		case Ears: this.order = 45; break;
		case Head: this.order = 40; break;
		case Beard: this.order = 35; break;
		case Shield: this.order = 50; break;
		case MainHandWeapon: this.order = 60; break;
		case OffHandWeapon: this.order = 70; break;
		}
	}
	
	private void initialize(Race race, Ruleset.Gender gender) {
		// look for a secondary icon if one has not been set
		if (secondaryIcon == null) {
			String secondaryBase = icon + "Secondary";
			String secondaryRaceGender = icon + race.getName() + gender + "Secondary";
			String secondaryRace = icon + race.getName() + "Secondary";

			if (SpriteManager.hasSprite(secondaryRaceGender))
				secondaryIcon = secondaryRaceGender;
			else if (SpriteManager.hasSprite(secondaryRace))
				secondaryIcon = secondaryRace;
			else if (SpriteManager.hasSprite(secondaryBase))
				secondaryIcon = secondaryBase;
		}
		
		if (type == Type.OffHandWeapon) {
			// look for an off hand specified icon
			String iconOffhand = icon + Type.OffHandWeapon.toString();
			if (SpriteManager.hasSprite(iconOffhand)) {
				this.icon = iconOffhand;
			}
			
		} else {
			// look for a racial / gender specific icon
			String iconRaceGender = icon + race.getName() + gender;
			String iconRace = icon + race.getName();
			
			if (SpriteManager.hasSprite(iconRaceGender))
				this.icon = iconRaceGender;
			else if (SpriteManager.hasSprite(iconRace))
				this.icon = iconRace;
		}
		
		if (color == null)
			this.color = Color.WHITE;
		
		if (secondaryColor == null)
			secondaryColor = Color.WHITE;
		
		this.offset = race.getIconOffset(type);
		
		setOrder();
		
		cacheSprites();
	}
	
	public boolean coversBeard() {
		return coversBeard;
	}
	
	public final void draw(int x, int y) {
		GL11.glColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
		
		sprite.draw(x + offset.x, y + offset.y);
		
		if (secondarySprite != null) {
			GL11.glColor4ub(secondaryColor.getR(), secondaryColor.getG(),
					secondaryColor.getB(), secondaryColor.getA());
			
			secondarySprite.draw(x + offset.x, y + offset.y);
		}
	}
	
	/**
	 * Called to manually recache all sprites held by this sub icon
	 */
	
	protected void cacheSprites() {
		this.sprite = SpriteManager.getSprite(this.icon);
		
		if (this.secondaryIcon != null)
			this.secondarySprite = SpriteManager.getSprite(this.secondaryIcon);
	}
	
	public Color getSecondaryColor() { return secondaryColor; }
	public Sprite getSecondarySprite() { return secondarySprite; }
	public String getSecondaryIcon() { return secondaryIcon; }
	
	public Sprite getSprite() { return sprite; }
	public Color getColor() { return color; }
	public Point getOffset() { return offset; }
	public String getIcon() { return icon; }
	public Type getType() { return type; }

	@Override public int compareTo(SubIcon other) {
		return this.order - other.order;
	}
	
	/**
	 * A factory class for easily creating subIcons with many different parameter types
	 * @author Jared
	 *
	 */
	
	public static class Factory {
		private String icon;
		private Color color;
		private Type type;
		private Race race;
		private Ruleset.Gender gender;
		private boolean coversBeard;
		
		private String secondaryIcon;
		private Color secondaryColor;
		
		public Factory(Type type, Race race, Ruleset.Gender gender) {
			this.type = type;
			this.race = race;
			this.gender = gender;
		}
		
		public void setCoversBeard(boolean coversBeard) {
			this.coversBeard = coversBeard;
		}
		
		public void setPrimaryIcon(String icon, Color color) {
			this.icon = icon;
			this.color = color;
		}
		
		public void setSecondaryIcon(String icon, Color color) {
			this.secondaryIcon = icon;
			this.secondaryColor = color;
		}
		
		/**
		 * Creates a subIcon using the fields from this factory
		 * @return the newly created subicon
		 */
		
		public SubIcon createSubIcon() {
			SubIcon subIcon = new SubIcon();
			
			subIcon.icon = icon;
			subIcon.color = color;
			subIcon.type = type;
			
			subIcon.coversBeard = coversBeard;
			
			subIcon.secondaryIcon = secondaryIcon;
			subIcon.secondaryColor = secondaryColor;
			
			subIcon.initialize(race, gender);
			
			return subIcon;
		}
	}
}
