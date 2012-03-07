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

public class Size {
	public static final String[] names = { "Minuscule", "Tiny", "Small", "Medium",
									  "Large", "Huge", "Gigantic" };
	private static final Size[] sizes = new Size[7];
	
	private int size;
	private int modifier;
	
	public static Size[] values() {
		get(names[0]);
		
		return sizes;
	}
	
	public static Size get(String name) {
		if (sizes[0] == null) {
			for (int i = 0; i < sizes.length; i++) {
				sizes[i] = new Size(i);
			}
		}
		
		for (int i = 0; i < names.length; i++) {
			if (name.equalsIgnoreCase(names[i])) {
				return sizes[i];
			}
		}
		
		return sizes[3];
	}
	
	private Size(int size) { 
		this.size = size;
		
		if (this.size == 0)      this.modifier =  4;
		else if (this.size == 1) this.modifier =  2;
		else if (this.size == 2) this.modifier =  1;
		else if (this.size == 3) this.modifier =  0;
		else if (this.size == 4) this.modifier = -1;
		else if (this.size == 5) this.modifier = -2;
		else if (this.size == 6) this.modifier = -4;
	}
	
	public int getModifier() { return modifier * 5; }
	
	public int getSize() {	return size; }
	public String getName() { return names[size]; }
	public int getDifference(Size other) { return (this.size - other.getSize()); }
	
	public String toString() { return names[size]; }
}
