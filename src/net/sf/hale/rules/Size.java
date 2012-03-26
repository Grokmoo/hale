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

/**
 * A class representing a creature size, which confers certain bonuses or penalties
 * @author Jared
 *
 */

public enum Size {
	Minuscule(4), Tiny(2), Small(1), Medium(0), Large(-1), Huge(-2), Gigantic(-4);
	
	/**
	 * The Armor Class and Attack modifier for this Size
	 */
	
	public final int modifier;
	
	private final int sizeCategory;
	
	private Size(int modifier) {
		this.modifier = 5 * modifier;
		this.sizeCategory = modifier;
	}
	
	/**
	 * Returns the number of size categories different two sizes are
	 * @param other the size to compare to
	 * @return the number of size categories different the sizes are
	 */
	
	public int getDifference(Size other) { return (other.sizeCategory - this.sizeCategory); }
}
