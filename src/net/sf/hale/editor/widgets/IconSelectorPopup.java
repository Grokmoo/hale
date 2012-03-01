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

import java.util.List;


import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

public class IconSelectorPopup extends PopupWindow implements Runnable {
	private final Widget content;

	private final Label title;

	private final IconSelector iconSelector;

	private final Button accept;
	private final Button cancel;

	private String icon;

	private Callback callback;

	public IconSelectorPopup(Widget parent, String path, int iconSize, boolean offset, int scale) {
		this(parent, path, iconSize, offset, scale, null, null);
	}

	public IconSelectorPopup(Widget parent, String path, int iconSize, boolean offset, int scale, List<String> excludePostfixes) {
		this(parent, path, iconSize, offset, scale, excludePostfixes, null);
	}

	public IconSelectorPopup(Widget parent, String path, int iconSize, boolean offset, int scale,
			List<String> excludePostfixes, String includeOnly) {
		
		super(parent);

		this.setTheme("");
		this.setCloseOnClickedOutside(false);
		this.setSize(500, 400);
		this.setPosition(100, 100);

		content = new Widget();
		content.setTheme("/filepopup");
		this.add(content);

		title = new Label("Select an Icon");
		title.setSize(100, 20);
		title.setPosition(200, 5);
		content.add(title);

		accept = new Button("Accept");
		accept.setSize(60, 20);
		accept.setPosition(170, 340);
		accept.addCallback(new Runnable() {
			@Override public void run() {
				if (icon != null && callback != null) {
					callback.iconSelected(icon);
				}

				closePopup();
			}
		});
		content.add(accept);

		cancel = new Button("Cancel");
		cancel.setSize(60, 20);
		cancel.setPosition(245, 340);
		cancel.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
			}
		});
		content.add(cancel);

		iconSelector = new IconSelector(this, path);
		iconSelector.setSize(475, 300);
		iconSelector.setPosition(5, 30);
		iconSelector.setTheme("/scrollpane");
		iconSelector.setNumCols(450 / iconSize);
		iconSelector.setIconSize(iconSize, iconSize);
		iconSelector.setIconOffset(offset);
		iconSelector.setScale(scale);
		iconSelector.setSelectedColor(0.0f, 0.0f, 0.0f);
		if (excludePostfixes != null) iconSelector.excludePostfixes(excludePostfixes);
		if (includeOnly != null) iconSelector.includeOnlyContaining(includeOnly);
		content.add(iconSelector);
		iconSelector.update();
	}
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public String getSelectedIcon() { return this.icon; }

	@Override public boolean openPopup() {
		this.icon = null;
		iconSelector.deselectAll();
		
		return super.openPopup();
	}
	
	@Override public void run() {
		this.icon = iconSelector.getSelected();
	}
	
	public interface Callback {
		public void iconSelected(String icon);
	}
}