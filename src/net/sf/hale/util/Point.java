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

package net.sf.hale.util;

public class Point {
	public int x, y;
	
	public boolean valid = true;
	
	public Point(boolean valid) {
		this.valid = valid;
	}
	
	public Point(int a, int b) {
		x = a;
		y = b;
	}
	
	public Point(double x, double y) {
		this.x = (int)x;
		this.y = (int)y;
	}
	
	public Point() {
		x = 0;
		y = 0;
	}
	
	public Point(Point other) {
		this.valid = other.valid;
		this.x = other.x;
		this.y = other.y;
	}
	
	public boolean equals(Point p) {
		if (p == null) return false;
		
		if (!valid || !p.valid) return false;
		
		if (p.x == x && p.y == y) return true;
		
		return false;
	}
	
	public double angleTo(Point b) {
		Point aScreen = AreaUtil.convertGridToScreen(this);
		Point bScreen = AreaUtil.convertGridToScreen(b);
		
		int xDiff = bScreen.x - aScreen.x;
		
		int yDiff = bScreen.y - aScreen.y;
		
		if (xDiff == 0) return Math.signum(yDiff) * Math.PI / 2;
		else if (xDiff > 0) return Math.atan((double)yDiff / (double)xDiff);
		else return Math.atan((double)yDiff / (double)xDiff) + Math.PI;
	}
	
	public double screenDistance(Point b) {
		Point aScreen = AreaUtil.convertGridToScreen(this);
		Point bScreen = AreaUtil.convertGridToScreen(b);
		
		int distSquared = AreaUtil.euclideanDistance2(aScreen.x, aScreen.y, bScreen.x, bScreen.y);
		return Math.sqrt(distSquared);
	}
	
	public Point toScreen() {
		return AreaUtil.convertGridToScreenAndCenter(this);
	}
	
	public Point toGrid() {
		return AreaUtil.convertScreenToGrid(this);
	}
	
	public String toString() {
		return "(" + x + ", " + y + " : " + valid + ")";
	}
}
