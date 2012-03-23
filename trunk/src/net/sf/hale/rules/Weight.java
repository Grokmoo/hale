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

import java.text.NumberFormat;

public class Weight {
	private int grams;
	
	public Weight() {
		grams = 0;
	}
	
	public Weight(int grams) {
		this.grams = grams;
	}
	
	public Weight(Weight other) {
		this.grams = other.grams;
	}
	
	public void add(Weight other) {
		this.grams += other.grams;
	}
	
	public void add(Weight other, int quantity) {
		this.grams += quantity * other.grams;
	}
	
	public int getWeight() { return grams; }
	
	public void setWeight(int grams) { this.grams = grams; }
	
	public String toStringGrams() { return grams + ""; }
	public String toStringKilograms() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		return nf.format(grams / 1000.0);
	}
}
