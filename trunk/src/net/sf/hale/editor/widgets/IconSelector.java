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

package net.sf.hale.editor.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.hale.EventCallback;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.FileUtil;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Widget;

public class IconSelector extends ScrollPane implements EventCallback {
	private final Runnable callback;
	
	private final List<SpriteViewer> icons;
	private final List<String> names;
	
	private List<String> excludePostfixes;
	private String includeOnlyContaining;
	
	private final Widget content;
	
	private int scale = 1;
	private boolean iconOffset = true;
	private String selected = null;
	private int colSize = 75;
	private int rowSize = 75;
	private int numCols = 4;
	private final String dir;
	
	private float red = 1.0f;
	private float green = 1.0f;
	private float blue = 1.0f;
	
	public IconSelector(Runnable callback, String dir) {
		this.setTheme("");
		this.callback = callback;
		this.dir = dir;
		
		icons = new ArrayList<SpriteViewer>();
		names = new ArrayList<String>();

		content = new Widget();
		content.setTheme("");
		this.setContent(content);
		
		update();
	}
	
	public void update() {
		icons.clear();
		names.clear();
		content.removeAllChildren();
		
		int row = 0;
		int col = 0;
		
		Set<String> sprites = SpriteManager.getSpriteIDs();
		List<String> spritesList = new ArrayList<String>(sprites);
		Collections.sort(spritesList);
		
		for (String s : spritesList) {
			if (!s.endsWith(ResourceType.PNG.getExtension())) continue;
			
			boolean sValid = true;
			if (excludePostfixes != null) {
				for (String postfix : excludePostfixes) {
					if (s.endsWith(postfix)) {
						sValid = false; 
						break; 
					}
				}
			}
			
			if (includeOnlyContaining != null) {
				if (!s.contains(includeOnlyContaining)) sValid = false;
			}
			
			if (!sValid) continue;
			
			if (!sValid || !s.startsWith(dir)) continue;
			
			SpriteViewer viewer = new SpriteViewer(colSize - 1, rowSize - 1, scale);

			icons.add( viewer );
			names.add(s);
			
			viewer.setSprite(SpriteManager.getImage(s));
			viewer.setPosition(col * this.colSize, row * this.rowSize);
			viewer.addCallback(this);
			viewer.setSelectedColor(red, green, blue);
			viewer.setIconOffset(this.iconOffset);

			content.add(viewer);

			col++;
			if (col == numCols) {
				col = 0;
				row++;
			}
		}
	}
	
	public void setScale(int scale) { this.scale = scale; }
	
	public void setIconOffset(boolean iconOffset) {
		this.iconOffset = iconOffset;
	}
	
	public void setNumCols(int numCols) {
		this.numCols = numCols;
	}
	
	public void setIconSize(int colSize, int rowSize) {
		this.colSize = colSize;
		this.rowSize = rowSize;
	}
	
	public void setSelectedColor(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public void includeOnlyContaining(String containing) {
		this.includeOnlyContaining = containing;
	}
	
	public void excludePostfixes(List<String> postfixes) {
		this.excludePostfixes = postfixes;
	}
	
	public void setSelected(String name) {
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).equals(name)) {
				setSelectedIcon(i);
				return;
			}
		}
	}
	
	public String getSelected() {
		return selected;
	}
	
	@Override public void handleEvent(Event evt, Widget widget) {
		int index = icons.indexOf(widget);
		setSelectedIcon(index);
		
		if (dir.endsWith("portraits")) {
			selected = FileUtil.getRelativePath("portraits", names.get(index));
		} else {
			selected = FileUtil.getRelativePath("images", names.get(index));
		}
		
		
		selected = selected.substring(0, selected.length() - 4);
		
		callback.run();
	}
	
	public void deselectAll() {
		for (SpriteViewer viewer : icons) {
			viewer.setSelected(false);
		}
	}
	
	private void setSelectedIcon(int index) {
		deselectAll();
		
		icons.get(index).setSelected(true);
	}
}
