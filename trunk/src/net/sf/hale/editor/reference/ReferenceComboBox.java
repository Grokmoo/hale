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

package net.sf.hale.editor.reference;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.model.ListModel;

/**
 * A ComboBox for Referenceable objects.  The currently selected entry in the
 * ComboBox has a button which when clicked brings up a PopupWindow with the
 * list of all references to the selected object.
 * 
 * @author Jared
 *
 * @param <T> A Referenceable object, such as a Creature or Item.
 */

public class ReferenceComboBox<T extends Referenceable> extends ComboBox<T> implements Runnable {
	private final Button refButton;
	
	/**
	 * Create a new ReferenceComboBox using the specified ListModel as the data structure
	 * containing all list elements.  Updates to model will automatically update this
	 * ComboBox.  The ComboBox will initially be populated with the entries specified in
	 * model.
	 * 
	 * @param model the data model used to hold entries for this ComboBox.
	 */
	
	public ReferenceComboBox(ListModel<T> model) {
		super(model);
		this.setTheme("combobox");
		
		refButton = new Button(">>");
		refButton.setTheme("/button");
		refButton.addCallback(this);
		
		refButton.setVisible(false);
		this.add(refButton);
	}
	
	/**
	 * Positions and sizes all sub elements to this ComboBox, such as the
	 * button used to show the reference list.
	 */
	
	@Override public void layout() {
		super.layout();
		
		refButton.setSize(20, 16);
		refButton.setPosition(getX() + getWidth() - 40, getY() + 2);
	}
	
	/**
	 * This method is called whenever the currently selected entry changes.
	 * The button to show references is only visible when there is a selected
	 * entry.
	 * 
	 * @param close whether the popup window has been closed by the selection
	 */
	
	@Override protected void listBoxSelectionChanged(boolean close) {
		super.listBoxSelectionChanged(close);
		
		refButton.setVisible(getSelected() != -1);
	}
	
	/**
	 * Called whenever the References button is clicked.  This function
	 * creates a new ReferencePopupWindow showing the list of all references
	 * to the currently selected entry in this ComboBox.
	 */
	
	@Override public void run() {
		if (getModel() == null || getSelected() == -1) return;
		
		new ReferencePopupWindow(refButton, getModel().getEntry(getSelected())).openPopup();
	}
	
//	@Override public boolean setSize(int width, int height) {
//		
//		refButton.setPosition(getX() + getWidth() - 45, getY() + 2);
//		
//		return super.setSize(width, height);
//	}
}
