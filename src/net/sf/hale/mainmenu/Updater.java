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

package net.sf.hale.mainmenu;

import java.io.IOException;

import net.sf.hale.Game;
import net.sf.hale.util.Logger;

/**
 * The class that calls the updater which in turn will restart hale
 * @author Jared
 *
 */

public class Updater {
	private String argument;
	private String updaterCommand;
	
	/**
	 * Creates a new Updater instance
	 */
	
	public Updater() {
		try {
			argument = Game.getProgramCommand();
		} catch (IOException e) {
			Logger.appendToErrorLog("Error getting program restart command", e);
		}
		
		updaterCommand = System.getProperty("java.home") + "/bin/java -jar updater.jar" + " " + argument;
	}
	
	/**
	 * Restarts hale and applies the update
	 */
	
	public void runUpdater() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					Runtime.getRuntime().exec(updaterCommand);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		System.exit(0);
	}
}
