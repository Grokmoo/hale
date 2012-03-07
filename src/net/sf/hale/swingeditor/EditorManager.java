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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import net.sf.hale.Sprite;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.Logger;

/**
 * A class for managing all of the separate editors currently in existance
 * and creating new ones, as well as managing the set of assets available
 * @author Jared
 *
 */

public class EditorManager {
	private static SwingEditor editor;
	
	private static LogViewer logViewer;
	private static List<String> logEntries;
	
	private static AssetModel<Item> itemsModel;
	private static AssetModel<Creature> creaturesModel;
	
	private static List<AssetEditor> subEditors;
	
	private static Map<String, BufferedImage> itemIcons;
	private static Map<Item.ItemType, Map<String, BufferedImage>> subIcons;
	
	/**
	 * Initializes the EditorManager with the specified editor window
	 * @param editor the editor window
	 */
	
	public static void initialize(SwingEditor editor) {
		EditorManager.editor = editor;
		
		EditorManager.logEntries = new ArrayList<String>();
		EditorManager.subEditors = new ArrayList<AssetEditor>();
		
		addLogEntry("Created Campaign Editor");
	}
	
	/**
	 * Shows the log viewer
	 */
	
	public static void showLogViewer() {
		if (logViewer != null) {
			logViewer.dispose();
		}
		
		logViewer = new LogViewer(Collections.unmodifiableList(logEntries));
		logViewer.setVisible(true);
	}
	
	/**
	 * Adds the specified log entry to the log.  The most recent log entry is always
	 * displayed in the upper right corner of the main editor
	 * @param entry
	 */
	
	public static void addLogEntry(String entry) {
		logEntries.add(entry);
		
		editor.setLogEntry(entry);
		
		if (logViewer != null)
			logViewer.addLogEntry(entry);
	}
	
	/**
	 * Creates a new Empty editor
	 */
	
	public static void createNewEditor() {
		AssetEditor editor = new AssetEditor();
		editor.setVisible(true);
		
		subEditors.add(editor);
	}
	
	/**
	 * Closes all currently open editors
	 */
	
	public static void closeAllEditors() {
		for (AssetEditor editor : subEditors) {
			editor.dispose();
		}
		
		subEditors.clear();
	}
	
	/**
	 * Returns the list model storing references to all available items
	 * @return the items model
	 */
	
	public static AssetModel<Item> getItemsModel() {
		return itemsModel;
	}
	
	/**
	 * Returns the list model storing references to all available creatures
	 * @return the creature list model
	 */
	
	public static AssetModel<Creature> getCreaturesModel() {
		return creaturesModel;
	}
	
	/**
	 * Closes the specified editor
	 * @param editor
	 */
	
	public static void closeEditor(AssetEditor editor) {
		subEditors.remove(editor);
		editor.dispose();
	}
	
	/**
	 * Clears all existing asset models and reloads them from disk
	 */
	
	public static void loadAllAssets() {
		itemsModel = new AssetModel<Item>(AssetType.Items);
		creaturesModel = new AssetModel<Creature>(AssetType.Creatures);
		
		// get sprites and sort them alphabetically
		Set<String> sprites = SpriteManager.getSpriteIDs();
		List<String> spritesList = new ArrayList<String>(sprites);
		Collections.sort(spritesList);
		
		BufferedImage subItemsImage = null;
		BufferedImage itemsImage = null;
		try {
			itemsImage = ImageIO.read(ResourceManager.getStream("images/items.png"));
			subItemsImage = ImageIO.read(ResourceManager.getStream("images/subIcons.png"));
		} catch (IOException e) {
			Logger.appendToErrorLog("Error loading items.png spritesheet");
			e.printStackTrace();
		}
		
		itemIcons = new LinkedHashMap<String, BufferedImage>();
		subIcons = new HashMap<Item.ItemType, Map<String, BufferedImage>>();
		
		// go through the list of sprites and add them to the icon lists as needed
		for (String s : spritesList) {
			if (s.startsWith("images/items/")) {
				itemIcons.put(s, getImage(itemsImage, s));
			} else if (s.startsWith("images/subIcons/")) {
				String shortID = s.substring(16);
				
				int index = shortID.indexOf('-');
				if (index > 0) {
					Item.ItemType type = Item.ItemType.valueOf(shortID.substring(0, index).toUpperCase());
					
					Map<String, BufferedImage> map = subIcons.get(type);
					if (map == null) {
						map = new LinkedHashMap<String, BufferedImage>();
						subIcons.put(type, map);
					}
					
					map.put(s, getImage(subItemsImage, s));
				}
			}
		}
	}
	
	private static BufferedImage getImage(BufferedImage spriteSheet, String resourceID) {
		Sprite sprite = SpriteManager.getImage(resourceID);
		
		int startX = (int)(spriteSheet.getWidth() * sprite.getTexCoordStartX());
		int startY = (int)(spriteSheet.getHeight() * sprite.getTexCoordStartY());
		int endX = (int)(spriteSheet.getWidth() * sprite.getTexCoordEndX());
		int endY = (int)(spriteSheet.getHeight() * sprite.getTexCoordEndY());
		
		return spriteSheet.getSubimage(startX, startY, endX - startX, endY - startY);
	}
	
	/**
	 * Gets the list of all valid icon choices for items
	 * @return the list of all valid icon choices
	 */
	
	public static Map<String, BufferedImage> getItemIconChoices() {
		return itemIcons;
	}
	
	/**
	 * Gets the list of valid sub icon choices for the specified
	 * item type
	 * @param type
	 * @return the list of valid icon choices
	 */
	
	public static Map<String, BufferedImage> getSubIconChoices(Item.ItemType type) {
		return subIcons.get(type);
	}
	
	/**
	 * Creates a deep copy of the specified buffered image, with all
	 * pixels multiplied by the specified color
	 * @param in
	 * @param color
	 * @return the buffered image
	 */
	
	public static BufferedImage copy(BufferedImage in, Color color) {
		double r = color.getRed() / 255.0f;
		double g = color.getGreen() / 255.0f;
		double b = color.getBlue() / 255.0f;
		double a = color.getAlpha() / 255.0f;
		
		BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		for (int x = 0; x < out.getWidth(); x++) {
			for (int y = 0; y < out.getHeight(); y++) {
				int argb = in.getRGB(x, y);
				
				Color cIn = new Color(argb, true);
				
				int rOut = Math.min( 255, Math.max(0, (int)(cIn.getRed() * r)) );
				int gOut = Math.min( 255, Math.max(0, (int)(cIn.getGreen() * g)) );
				int bOut = Math.min( 255, Math.max(0, (int)(cIn.getBlue() * b)) );
				int aOut = Math.min( 255, Math.max(0, (int)(cIn.getAlpha() * a)) );
				
				Color cOut = new Color(rOut, gOut, bOut, aOut);
				
				out.setRGB(x, y, cOut.getRGB());
			}
		}
		
		return out;
	}
}
