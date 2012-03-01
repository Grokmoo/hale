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

package net.sf.hale.widgets;

import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Widget;

public class ExpandableScrollPane extends ScrollPane {
	public ExpandableScrollPane() {
		super();
		this.setTheme("scrollpane");
	}
	
	public ExpandableScrollPane(Widget content) {
		super(content);
		this.setTheme("scrollpane");
	}
	
	@Override public int getPreferredInnerWidth() {
		int width = getContent().getPreferredInnerWidth();
		if (getVerticalScrollbar().isVisible())
			width += this.getVerticalScrollbar().getWidth();
		
		return width;
	}
	
	@Override public int getPreferredInnerHeight() {
		int height = getContent().getPreferredInnerHeight();
		if (getHorizontalScrollbar().isVisible())
			height += this.getHorizontalScrollbar().getHeight();
		
		return height;
	}
}
