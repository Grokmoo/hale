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

import net.sf.hale.entity.Item;
import net.sf.hale.util.LineParser;
import net.sf.hale.util.Logger;

public class Currency {
	private int value;
	
	public Currency() {
		this.value = 0;
	}
	
	public Currency(int value) {
		this.value = value;
	}
	
	public Currency(Currency other) {
		this.value = other.value;
	}
	
	public Currency(int pp, int gp, int sp, int cp) {
		this.value = cp + 10 * sp + 100 * gp + 1000 * pp;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public void setValue(int pp, int gp, int sp, int cp) {
		this.value = cp + 10 * sp + 100 * gp + 1000 * pp;
	}
	
	public int getTotalValueInCP() { return value; }
	public int getTotalValueInSP() { return value / 10; }
	public int getTotalValueinGP() { return value / 100; }
	public int getTotalValueInPP() { return value / 1000; }
	
	public int getCP() { return value % 10; }
	public int getSP() { return (value / 10) % 10; }
	public int getGP() { return (value / 100) % 10; }
	public int getPP() { return (value / 1000); }
	
	public int getValue() { return value; }
	
	public void add(int pp, int gp, int sp, int cp) {
		this.value += cp + 10 * sp + 100 * gp + 1000 * pp;
	}
	
	public void addCP(int cp) { this.value += cp; }
	public void addSP(int sp) { this.value += sp * 10; }
	public void addGP(int gp) { this.value += gp * 100; }
	public void addPP(int pp) { this.value += pp * 1000; }
	
	public void add(Currency other) { this.value += other.value; }
	public void subtract(Currency other) { this.value -= other.value; }
	
	public void subtract(int pp, int gp, int sp, int cp) {
		this.value -= (cp + 10 * sp + 100 * gp + 1000 * pp);
	}
	
	public void subtractCP(int cp) { this.value -= cp; }
	public void subtractSP(int sp) { this.value -= sp * 10; }
	public void subtractGP(int gp) { this.value -= gp * 100; }
	public void subtractPP(int pp) { this.value -= pp * 1000; }
	
	@Override
	public String toString() {
		return getPP() + " PP " + getGP() + " GP " + getSP() + " SP " + getCP() + " CP";
	}
	
	public String shortString(int percentage) {
		int oldValue = this.value;
		
		this.value = this.value * percentage / 100;
		
		StringBuilder builder = new StringBuilder();
		
		if (getPP() != 0) builder.append(getPP() + " PP ");
		if (getGP() != 0) builder.append(getGP() + " GP ");
		if (getSP() != 0) builder.append(getSP() + " SP ");
		if (getCP() != 0) builder.append(getCP() + " CP ");
		
		if (builder.length() == 0) builder.append("0 CP ");
		
		this.value = oldValue;
		
		return builder.toString().trim();
	}
	
	public String shortString() {
		StringBuilder builder = new StringBuilder();
		
		if (getPP() != 0) builder.append(getPP() + " PP ");
		if (getGP() != 0) builder.append(getGP() + " GP ");
		if (getSP() != 0) builder.append(getSP() + " SP ");
		if (getCP() != 0) builder.append(getCP() + " CP ");
		
		if (builder.length() == 0) builder.append("0 CP ");
		
		return builder.toString().trim();
	}
	
	public void addFromString(String s) {
		LineParser parser = new LineParser(s);
		
		try {
			while (parser.hasNext()) {
				int value = parser.nextInt();
				if (parser.hasNext()) {
					String type = parser.next().toLowerCase();
					
					if (type.equals("cp")) {
						addCP(value);
					} else if (type.equals("sp")) {
						addSP(value);
					} else if (type.equals("gp")) {
						addGP(value);
					} else if (type.equals("pp")) {
						addPP(value);
					}
				} else {
					Logger.appendToErrorLog("Warning.  Extra token in currency string " + s);
				}
			}
		} catch (Exception e) {
			Logger.appendToErrorLog("Error parsing currency string.", e);
		}
	}
	
	public int getMaxNumberAffordable(Item item, int markup) {
		int cost = item.getQualityValue().getValue() * markup / 100;
		if (cost == 0) return Integer.MAX_VALUE;
		
		int stackSize = item.getValueStackSize();
		
		return (this.value / cost) * stackSize;
	}
	
	public static Currency getPlayerBuyCost(Item item, int quantity, int markdown) {
		int cost = item.getQualityValue().getValue();
		
		int stackSize = item.getValueStackSize();
		
		int remainder = quantity % stackSize;
		int roundedQuantity;
		if (remainder != 0) {
			roundedQuantity = quantity + stackSize - remainder;
		} else {
			roundedQuantity = quantity;
		}
		
		int value = cost * roundedQuantity * markdown / (100 * stackSize);
		
		return new Currency(value);
	}
	
	public static Currency getPlayerSellCost(Item item, int quantity, int markdown) {
		int cost = item.getQualityValue().getValue();
		
		int stackSize = item.getValueStackSize();
		
		int value = cost * quantity * markdown / (100 * stackSize);
		
		return new Currency(value);
	}
}
