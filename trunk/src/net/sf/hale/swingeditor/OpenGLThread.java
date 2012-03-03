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

import java.awt.Canvas;

import net.sf.hale.Game;
import net.sf.hale.util.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * The thread responsible for managing the OpenGL context and running
 * the main OpenGL rendering loop
 * @author Jared
 *
 */

public class OpenGLThread extends Thread {
	private Canvas canvas;
	
	private boolean running = true;
	
	/**
	 * Creates a new OpenGL thread which will run using the specified canvas
	 * @param parent
	 */
	
	public OpenGLThread(Canvas parent) {
		this.canvas = parent;
	}
	
	/**
	 * This should be called prior to application exit to close out the display
	 */
	
	public void destroyDisplay() {
		Display.destroy();
	}
	
	private void initGL() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, canvas.getWidth(), canvas.getHeight(), 0, 0, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL14.GL_COLOR_SUM);
	}
	
	@Override public void run() {
		try {
			Display.setParent(canvas);
			Display.create();
			
			initGL();
			
		} catch (LWJGLException e) {
			Logger.appendToErrorLog("Error creating canvas", e);
		}
		Display.setVSyncEnabled(true);
		
		while (running) {
			Game.textureLoader.update();
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			GL11.glColor3f(1.0f, 0.0f, 0.0f);
			
			Display.update(false);
            GL11.glGetError();
            Display.sync(60);
            Display.processMessages();
		}
		
		Display.destroy();
	}
}
