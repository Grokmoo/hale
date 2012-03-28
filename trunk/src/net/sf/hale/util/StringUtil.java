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

public class StringUtil {
	public static String upperCaseToWord(String in) {
		String inUpper = in.toUpperCase();
		String inLower = in.toLowerCase();
		StringBuilder sb = new StringBuilder(in.length());
		
		boolean newWord = true;
		
		for (int i = 0; i < in.length(); i++) {
			if (newWord) {
				sb.append(inUpper.charAt(i));
				newWord = false;
			} else if (in.charAt(i) == '_') {
				sb.append(' ');
				newWord = true;
			} else {
				sb.append(inLower.charAt(i));
			}
		}
		
		return sb.toString();
	}

	public static String getOrdinalPostfix(int input) {
		int mod100 = input % 100;
		int mod10 = input % 10;
		
		if (mod100 - mod10 == 10) return "th";

		switch (mod10) {
		case 1: return "st";
		case 2: return "nd";
		case 3: return "rd";
		default: return "th";
		}
	}
}
