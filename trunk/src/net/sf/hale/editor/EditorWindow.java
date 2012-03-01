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

package net.sf.hale.editor;

import net.sf.hale.Game;

import de.matthiasmann.twl.ResizableFrame;

public abstract class EditorWindow extends ResizableFrame {
	private CloseCallback closeCallback;
	
	public EditorWindow(String title) {
		this.closeCallback = new CloseCallback();
		
		this.setTheme("infodialog");
		this.setTitle(title);
		this.setPosition(30, 30);
		this.setSize(640, 480);
		this.addCloseCallback(closeCallback);
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) Game.campaignEditor.moveToFront(this);
	}
	
	public void fireDefaultCloseCallback() {
		closeCallback.run();
	}
	
	public abstract void saveSelected();
	
	private class CloseCallback implements Runnable {
		@Override public void run() {
			setVisible(false);
			saveSelected();
		}
	}
}
