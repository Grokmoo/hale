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

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;

/**
 * A class for managing all of the separate editors currently in existance
 * and creating new ones, as well as managing the set of assets available
 * @author Jared
 *
 */

public class EditorManager {
	private static AssetModel<Item> itemsModel;
	private static AssetModel<Creature> creaturesModel;
	
	private static List<AssetEditor> editors = new ArrayList<AssetEditor>();
	
	/**
	 * Creates a new Empty editor
	 */
	
	public static void createNewEditor() {
		AssetEditor editor = new AssetEditor();
		editor.setVisible(true);
		
		editors.add(editor);
	}
	
	/**
	 * Closes all currently open editors
	 */
	
	public static void closeAllEditors() {
		for (AssetEditor editor : editors) {
			editor.dispose();
		}
		
		editors.clear();
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
		editors.remove(editor);
		editor.dispose();
	}
	
	/**
	 * Clears all existing asset models and reloads them from disk
	 */
	
	public static void loadAllAssets() {
		itemsModel = new AssetModel<Item>(AssetType.Items);
		creaturesModel = new AssetModel<Creature>(AssetType.Creatures);
	}
}
