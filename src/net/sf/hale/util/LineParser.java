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

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

public class LineParser extends StreamTokenizer {
	public LineParser(String s) {
		super(new StringReader(s));
		
		this.resetSyntax();
		this.wordChars('\u0000', '\uFFFF');
		this.whitespaceChars(' ', ' ');
		this.whitespaceChars('\t', '\t');
		this.quoteChar('\"');
		this.quoteChar('\'');
		this.commentChar('#');
	}
	
	public boolean hasNext() throws IOException {
		
		if (this.nextToken() == StreamTokenizer.TT_EOF) return false;
		this.pushBack();
		if (this.nextToken() == StreamTokenizer.TT_EOL) return false;
		this.pushBack();
		
		return true;
	}
	
	public String next() throws IOException {
		this.nextToken();
		
		return this.sval;
	}
	
	public long nextLong() throws IOException {
		this.nextToken();
		
		return Long.parseLong(this.sval);
	}
	
	public int nextInt() throws IOException {
		this.nextToken();
		
		return Integer.parseInt(this.sval);
	}
	
	public boolean nextBoolean() throws IOException {
		this.nextToken();
		
		return Boolean.parseBoolean(this.sval);
	}
}
