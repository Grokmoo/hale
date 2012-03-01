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

import java.util.Iterator;
import java.util.LinkedList;

import de.matthiasmann.twl.Color;

public class LineKeyList extends LinkedList<String> {
	private static final long serialVersionUID = 5669920901932745802L;
	
	private int lineNumber;
	private String filePath;
	
	private Iterator<String> iterator;
	
	public LineKeyList(String filePath, int lineNumber) {
		super();
		
		this.filePath = filePath;
		this.lineNumber = lineNumber;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public void setIterator() {
		this.iterator = super.iterator();
	}
	
	public final boolean hasNext() {
		return iterator.hasNext();
	}
	
	public final String next() {
		return iterator.next();
	}
	
	public final byte nextByte() {
		return Byte.parseByte(iterator.next());
	}
	
	public final int nextInt() {
		return Integer.parseInt(iterator.next());
	}
	
	public final boolean nextBoolean() {
		return Boolean.parseBoolean(iterator.next());
	}
	
	public final long nextLong() {
		return Long.parseLong(iterator.next());
	}
	
	public final float nextFloat() {
		return Float.parseFloat(iterator.next());
	}
	
	public final Color nextColor() {
		return Color.parserColor(iterator.next());
	}
}
