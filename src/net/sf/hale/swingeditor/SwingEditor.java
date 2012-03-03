/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.swingeditor;

import java.awt.BorderLayout;
import java.awt.Canvas;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.sf.hale.Config;
import net.sf.hale.EntityManager;
import net.sf.hale.Game;
import net.sf.hale.loading.AsyncTextureLoader;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.util.JSEngineManager;

/**
 * A campaign editor using swing widgets rather than TWL
 * @author Jared
 *
 */

public class SwingEditor extends JFrame {
	/**
	 * The main entry point for the editor.  Any arguments are ignored
	 * @param args
	 */
	
	public static void main(String[] args) {
		// create the basic objects used by the campaign editor
		Game.textureLoader = new AsyncTextureLoader();
		Game.config = new Config();
		Game.scriptEngineManager = new JSEngineManager();
		Game.entityManager = new EntityManager();
		
		ResourceManager.registerCorePackage();
		
		Game.ruleset = new Ruleset();
		
		// create the editor frame
		SwingEditor editor = new SwingEditor();
		editor.setVisible(true);
	}
	
	private JMenuBar menuBar;

	private Canvas canvas;
	private OpenGLThread glThread;
	
	private SwingEditor() {
		setSize(Game.config.getEditorResolutionX(), Game.config.getEditorResolutionY());
		setTitle("Hale Campaign Editor");
		
		menuBar = new EditorMenuBar(this);
		setJMenuBar(menuBar);
		
		// set up the OpenGL canvas
		canvas = new Canvas() {
			@Override public void addNotify() {
				super.addNotify();
				glThread = new OpenGLThread(canvas);
				glThread.start();
			}
			
			@Override public void removeNotify() {
				glThread.destroyDisplay();
			}
		};
		add(canvas, BorderLayout.CENTER);
	}
}
