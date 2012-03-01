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
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBoxDisplay;
import de.matthiasmann.twl.model.ListModel;

/**
 * A ListBox for Referenceable Objects.  Each entry in the ListBox
 * is also accompanied by a button that allows the user to see the
 * entire list of references to the selected object in the current
 * campaign.
 * 
 * @author Jared
 *
 * @param <T> A Referenceable object, such as a Creature or Item.
 */

public class ReferenceListBox<T extends Referenceable> extends ListBox<T> {
	/**
	 * Create a new ReferenceListBox using the specified ListModel as the data structure
	 * containing all list elements.  Updates to model will automatically update this
	 * ListBox.  The ListBox will initially be populated with the entries specified in
	 * model.
	 * 
	 * @param model the data model used to hold entries for this ListBox.
	 */
	
	public ReferenceListBox(ListModel<T> model) {
		super(model);
		this.setTheme("listbox");
	}
	
	/**
	 * Creates a new, empty ListBox.  In order to add entries after creating the
	 * ListBox using this constructor, you will need to use setModel
	 */
	
	public ReferenceListBox() {
		super();
		this.setTheme("listbox");
	}
	
	/**
	 * Returns a new Label for a single list box element.
	 */
	
	@Override protected ListBoxDisplay createDisplay() {
		return new ListBoxLabel();
	}
	
	/**
	 * Returns Short.MAX_VALUE.  This will make the ListBox auto expand
	 * horizontally to fill any available space in the DialogLayout or
	 * similar layouts.
	 */
	
	@Override public int getPreferredInnerWidth() {
		return Short.MAX_VALUE;
	}
	
	/**
	 * A Label for a single ListBoxElement.  Consists of a text representing the
	 * element and a button that when clicked will show the list of references to
	 * the selected element.
	 * 
	 * @author Jared
	 *
	 */
	
	protected class ListBoxLabel extends ListBox.ListBoxLabel implements Runnable {
		private final Button refButton;
		
		/**
		 * Create a new ListBoxLabel.
		 */
		
		public ListBoxLabel() {
			super();
			
			refButton = new Button(">>");
			refButton.setTheme("/button");
			refButton.setSize(12, 12);
			refButton.setVisible(false);
			refButton.addCallback(this);
			this.add(refButton);
		}
		
		/**
		 * Called whenever the References button is clicked.  This function
		 * creates a new ReferencePopupWindow showing the list of all references
		 * to the object referenced by this ListBoxLabel.
		 */
		
		@Override public void run() {
			if (getModel() == null || getSelected() == -1) return;
			
			new ReferencePopupWindow(refButton, getModel().getEntry(getSelected())).openPopup();
		}
		
		/**
		 * Sets whether the entry corresponding to this Label is selected.
		 * When a label is selected, it is generally highlighted.
		 * 
		 * The references button is only visible when the Label is selected.
		 * 
		 * @param selected whether this entry should be selected
		 */
		
		@Override public void setSelected(boolean selected) {
			super.setSelected(selected);
			
			refButton.setVisible(isSelected());
		}
		
		/**
		 * Sets the size of this label to width x height
		 * 
		 * @param width the width to set this label's size to
		 * @param height the height to set this label's size to
		 * @return true if the size was set succesfully to the specified
		 * values, false otherwise
		 */
		
		@Override public boolean setSize(int width, int height) {
			refButton.setPosition(getX() + width - 25, getY());
			
			return super.setSize(width, height);
		}
	}
}
