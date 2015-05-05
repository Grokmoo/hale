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

package net.sf.hale;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

import net.sf.hale.resource.URLResourceStreamHandler;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;

/**
 * The class containing global configuration variables, such as screen resolution
 * debug settings, and anything else contained in the config.txt file
 * @author Jared Stephen
 *
 */

public class Config {
	/** The config file version.  Used to know if a config file is out of date and should be replaced.  Shouldn't be used anywhere else **/
	private static final int Version = 1;
	
	private int resolutionX, resolutionY;
	private final boolean fullscreen;
	private final boolean showFPS, capFPS;
	private final boolean combatAutoScroll;
	private final int toolTipDelay;
	private final long randSeed;
	private final boolean randSeedSet;
	private final boolean scriptConsoleEnabled;
	private final boolean debugMode;
	private final boolean warningMode;
	private final int combatDelay;
	private final long checkForUpdatesInterval;
	
	private final String versionID;
	
	private final Map<String, Integer> keyBindingActions; 
	
	/**
	 * Returns the amount of time the game should wait between checking for updates
	 * @return the amount of time the game waits between checking for updates
	 */
	
	public long getCheckForUpdatesInterval() {
		return checkForUpdatesInterval;
	}
	
	/**
	 * Returns true if a random seed has been set in the config file, false otherwise
	 * @return true if a random seed has been set, false otherwise
	 */
	
	public boolean randSeedSet() { return randSeedSet; }
	
	/**
	 * Returns the random seed set in the config file or 0l if it was not set
	 * @return the random seed set in the config file
	 */
	
	public long getRandSeed() { return randSeed; }
	
	/**
	 * Returns the base combat speed in milliseconds.  This is used to determine
	 * the amount of time movement and cycling through hostiles takes
	 * @return the base combat speed
	 */
	
	public int getCombatDelay() { return combatDelay; }
	
	/**
	 * Returns the horizontal display resolution set in the config file
	 * @return the horizontal display resolution set in the config file
	 */
	
	public int getResolutionX() { return resolutionX; }
	
	/**
	 * Returns the vertical display resolution set in the config file
	 * @return the vertical display resolution
	 */
	
	public int getResolutionY() { return resolutionY; }
	
	/**
	 * Returns true if the view should automatically scroll to show combat actions such as movement
	 * and attacks of opportunity, false otherwise
	 * @return whether to auto scroll in combat
	 */
	
	public boolean autoScrollDuringCombat() { return combatAutoScroll; }
	
	/**
	 * Returns true if the Frames per second (FPS) should be shown in the mainViewer, false otherwise
	 * @return whether the mainViewer should show FPS
	 */
	
	public boolean showFPS() { return showFPS; }
	
	/**
	 * Returns true if the game should be run in fullscreen mode, false otherwise
	 * @return whether the game should be run in fullscreen mode
	 */
	
	public boolean getFullscreen() { return fullscreen; }
	
	/**
	 * Returns true if the refresh rate is capped at 60 hz, false if it is not capped
	 * @return whether the referesh rate is capped
	 */
	
	public boolean capFPS() { return capFPS; }
	
	/**
	 * Returns true if the user can access the script console with the ~ key, false otherwise
	 * @return whether the script console is accessible
	 */
	
	public boolean isScriptConsoleEnabled() { return scriptConsoleEnabled; }
	
	/**
	 * Returns true if debug mode is enabled and error messages will be printed to the
	 * standard output in addition to being logged to log/error.txt
	 * @return whether debug mode is enabled
	 */
	
	public boolean isDebugModeEnabled() { return debugMode; }
	
	/**
	 * Returns true if warning mode is enabled and warning message will be printed to the
	 * standard output in addition to being logged to log/warning.txt
	 * @return whether warning mode is enabled
	 */
	
	public boolean isWarningModeEnabled() { return warningMode; }
	
	/**
	 * Returns the global tooltip delay set in the config file
	 * @return the global tooltip delay
	 */
	
	public int getTooltipDelay() { return toolTipDelay; }
	
	/**
	 * Returns the unique version ID for the current binary version of hale being run
	 * @return the unique version ID for the current binary version being run
	 */
	
	public String getVersionID() { return versionID; }
	
	/**
	 * Returns the integer keyboard code associated with the given action
	 * @param actionName
	 * @return the integer key code, or -1 if no key is associated with the action
	 */
	
	public int getKeyForAction(String actionName) {
		Integer key = keyBindingActions.get(actionName);
		
		if (key == null)
			return -1;
		else
			return key.intValue();
	}
	
	/**
	 * Returns the list of all keyboard action names in this config.  The list is
	 * not sorted
	 * @return the list of keyboard action names
	 */
	
	public List<String> getKeyActionNames() {
		List<String> names = new ArrayList<String>();
		
		for (String key : keyBindingActions.keySet()) {
			names.add(key);
		}
		
		return names;
	}
	
	/**
	 * Creates a new Config from the specified file
	 * @param fileName the name of the file to read the config from
	 */
	
	public Config(String fileName) {
		versionID = FileUtil.getHalfMD5Sum(new File("hale.jar"));
		
		File configFile = new File(fileName);
		
		// create the config file if it does not already exist or is old
		if (!checkConfigFile(configFile)) {
			createConfigFile(fileName);
		}
		
		SimpleJSONParser parser = new SimpleJSONParser(configFile);
		
		SimpleJSONArray resArray = parser.getArray("Resolution");
		Iterator<SimpleJSONArrayEntry> iter = resArray.iterator();
		
		resolutionX = iter.next().getInt(800);
		resolutionY = iter.next().getInt(600);
		
		fullscreen = parser.get("Fullscreen", false);
		
		SimpleJSONArray edResArray = parser.getArray("EditorResolution");
		iter = edResArray.iterator();
		
		showFPS = parser.get("ShowFPS", false);
		combatAutoScroll = parser.get("CombatAutoScroll", true);
		capFPS = parser.get("CapFPS", false);
		toolTipDelay = parser.get("TooltipDelay", 400);
		combatDelay = parser.get("CombatDelay", 150);
		scriptConsoleEnabled = parser.get("ScriptConsoleEnabled", false);
		debugMode = parser.get("DebugMode", false);
		warningMode = parser.get("WarningMode", false);
		checkForUpdatesInterval = parser.get("CheckForUpdatesInterval", 86400000);
		
		if (parser.containsKey("RandomSeed")) {
			randSeedSet = true;
			randSeed = parser.get("RandomSeed", 0);
		} else {
			randSeedSet = false;
			randSeed = 0l;
		}
		
		keyBindingActions = new HashMap<String, Integer>();
		
		SimpleJSONObject bindingsObject = parser.getObject("Keybindings");
		for (String bindingName : bindingsObject.keySet()) {
			String keyboardKey = bindingsObject.get(bindingName, null);
			
			if (keyboardKey.length() > 0) {
				keyBindingActions.put(bindingName, Event.getKeyCodeForName(keyboardKey));
			} else {
				keyBindingActions.put(bindingName, -1);
			}
			
		}
		
		// prevent an unused key warning
		parser.get("ConfigVersion", 0);
		
		parser.warnOnUnusedKeys();
	}
	
	/**
	 * Checks whether the current config file is ok to be used.  If it is not ok, then
	 * the default config should be copied over
	 * @param configFile
	 * @return true if the file is ok, false if the default file should be copied over (either the
	 * config doesn't exist or it is out of date)
	 */
	
	private boolean checkConfigFile(File configFile) {
		if (!configFile.isFile()) return false;
		
		SimpleJSONParser parser = new SimpleJSONParser(configFile);
		
		if (parser.get("ConfigVersion", 0) != Config.Version) {
			Logger.appendToWarningLog("Removing existing config file as version does not match " + Config.Version);
			configFile.delete();
			return false;
		}
		
		return true;
	}
	
	private void createConfigFile(String fileName) {
		try {
			FileUtil.copyFile(new File("docs/defaultConfig.json"), new File(fileName));
		} catch (IOException e) {
			Logger.appendToErrorLog("Error creating configuration file.", e);
		}
	}
	
	/**
	 * Writes the specified time to disk as the last check for updates time
	 * @param time the time in milliseconds since midnight, January 1, 1970 UTC
	 */
	
	public static void writeCheckForUpdatesTime(long time) {
		try {
			FileUtil.writeStringToFile(new File(Game.getConfigBaseDirectory() + "lastUpdateTime.txt"), Long.toString(time));
		} catch (IOException e) {
			Logger.appendToErrorLog("Error writing last update time to file", e);
		}
	}
	
	/**
	 * Gets the last time that updates were checked for, or 0 if 
	 * updates have never been checked for
	 * @return the last update check time (in milliseconds since midnight, January 1, 1970 UTC)
	 */
	
	public static long getLastCheckForUpdatesTime() {
		File file = new File(Game.getConfigBaseDirectory() + "lastUpdateTime.txt");
		if (file.canRead()) {
			String time = null;
			try {
				time = FileUtil.readFileAsString(Game.getConfigBaseDirectory() + "lastUpdateTime.txt");
			} catch (IOException e) {
				Logger.appendToErrorLog("Error reading last update time", e);
			}
			
			if (time != null) {
				try {
					return Long.parseLong(time);
				} catch (Exception e) {
					Logger.appendToErrorLog("Error parsing last update time", e);
				}
			}
			
		}
		
		return 0l;
	}
	
	/**
	 * Returns a List of all DisplayModes that have Bits Per Pixel matching
	 * the user's Desktop BPP, are fullscreen capable, and have a high enough
	 * resolution (800x600 or greater)
	 * 
	 * The returned List will be sorted by Display Resolution.
	 * 
	 * @return a List of all usable DisplayModes
	 * @throws LWJGLException
	 */
	
	public static List<DisplayMode> getUsableDisplayModes() throws LWJGLException {
		DisplayMode desktop = Display.getDesktopDisplayMode();
		
		DisplayMode[] allModes = Display.getAvailableDisplayModes();
		
		List<DisplayMode> goodModes = new ArrayList<DisplayMode>();
		
		// check each mode in the list of all modes to see if we can use it
		for (DisplayMode mode : allModes) {
			if (mode.getBitsPerPixel() != desktop.getBitsPerPixel()) continue;
			
			if (!mode.isFullscreenCapable()) continue;
			
			if (mode.getWidth() < 800 || mode.getHeight() < 600) continue;
			
			// we need to verify that a mode with the same width and height has not
			// already been added to the list, so we don't add what will look like the
			// same mode to the user twice
			boolean modeDoesNotMatchExisting = true;
			
			for (DisplayMode existingMode : goodModes) {
				if (existingMode.getWidth() == mode.getWidth() && existingMode.getHeight() == mode.getHeight()) {
					modeDoesNotMatchExisting = false;
					break;
				}
			}
			
			if (modeDoesNotMatchExisting) {
				goodModes.add(mode);
			}
		}
		
		// sort the list of usable modes by Display Resolution
		Collections.sort(goodModes, new Comparator<DisplayMode>() {
			@Override public int compare(DisplayMode m1, DisplayMode m2) {
				if (m1.getWidth() > m2.getWidth()) return 1;
				else if (m1.getWidth() < m2.getWidth()) return -1;
				else {
					if (m1.getHeight() > m2.getHeight()) return 1;
					else return -1;
				}
			}
			
		});
		
		return goodModes;
	}
	
	/**
	 * Searches the global list of DisplayModes at {@link Game#allDisplayModes} for a
	 * DisplayMode with the specified horizontal and vertical resolutions.
	 * 
	 * @param resX the horizontal resolution of the DisplayMode to be found
	 * @param resY the vertical resolution of the DisplayMode to be found
	 * @return the index of the matching display mode in Game.allDisplayModes, or -1 if no
	 * such DisplayMode was found.
	 */
	
	public static int getMatchingDisplayMode(int resX, int resY) {
		for (int i = 0; i < Game.allDisplayModes.size(); i++) {
			DisplayMode mode = Game.allDisplayModes.get(i);

			if (mode.getWidth() == resX && mode.getHeight() == resY) return i;
		}

		return -1;
	}
	
	/**
	 * Creates a new LWJGL display with the configured x and y game resolution.  If the
	 * specified x and y resolution is invalid, falls back to an 800x600 display mode.
	 * 
	 * This method then sets up the OpenGL context for 2D drawing, and sets up TWL using
	 * the theme file at gui/simple.xml.
	 */
	
	public static void createGameDisplay() {
		if ( !createDisplay(Game.config.getResolutionX(), Game.config.getResolutionY()) ) {
			Game.config.resolutionX = 800;
			Game.config.resolutionY = 600;
		}
	}
	
	/**
	 * Creates a new LWJGL display with the specified x and y resolutions.  If no such
	 * display is found in the list of usable DisplayModes at {@link Game#allDisplayModes},
	 * attempts to fallback to an 800x600 display mode.
	 * 
	 * Once the DisplayMode has been set, the OpenGL context is set up for 2D drawing
	 * and the TWL Theme file at gui/simple.xml is loaded.
	 * 
	 * @param resX the horizontal resolution of the Display to create
	 * @param resY the vertical resolution of the Display to create
	 * 
	 * @return true if the requested display mode was set, false if the display mode was not
	 * able to set and the method fell back to 800x600
	 */
	
	private static boolean createDisplay(int resX, int resY) {
		boolean returnValue = false;
		
		try {
			int index = getMatchingDisplayMode(resX, resY);
			if (index == -1) {
				Logger.appendToErrorLog("No display mode available with configuration: " + resX + "x" + resY + ".  Falling back to 800x600.");
				
				index = getMatchingDisplayMode(800, 600);
				
				if (index == -1) {
					Logger.appendToErrorLog("Unable to find display mode for fallback 800x600 display.  Exiting.");
					System.exit(1);
				}
			} else {
				returnValue = true;
			}
			
			Game.displayMode = Game.allDisplayModes.get(index);
			
			Display.setDisplayMode(Game.displayMode);
			Display.setFullscreen(Game.config.getFullscreen());
			Display.create();
			
			Game.renderer = new LWJGLRenderer();
			
		} catch (LWJGLException e) {
			Logger.appendToErrorLog("Error creating display.", e);
			System.exit(0);
		}
		
        GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Game.config.getResolutionX(), Game.config.getResolutionY(), 0, 0, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL14.GL_COLOR_SUM);
		
//		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);
		
		URL theme = null;
		try {
			theme = new URL("resource", "localhost", -1, "gui/theme.xml", new URLResourceStreamHandler());
		} catch (MalformedURLException e) {
			Logger.appendToErrorLog("Error creating URL for theme file", e);
		}
		
		try {
			Game.themeManager = ThemeManager.createThemeManager(theme, Game.renderer);
		} catch (IOException e) {
			Logger.appendToErrorLog("Error creating theme manager", e);
		}
		
		return returnValue;
	}
}
