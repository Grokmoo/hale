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
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * A widget for selecting an image and optionally an associated color
 * @author Jared
 *
 */

public class ImageSelector extends JPanel {
	private List<String> choices;
	private List<ImageIcon> icons;
	
	private JComboBox selector;
	
	/**
	 * Creates a new ImageSelector choosing between the specified set of choices
	 * By default it will not show a color selector
	 * @param defaultChoice the initially set choice
	 * @param choicesMap the choices
	 */
	
	public ImageSelector(String defaultChoice, Map<String, ImageIcon> choicesMap) {
		super(new BorderLayout());
		
		this.choices = new ArrayList<String>();
		this.icons = new ArrayList<ImageIcon>();
		
		Iterator<String> iter = choicesMap.keySet().iterator();
		
		int initialIndex = 0;
		Integer[] intArray = new Integer[choicesMap.size()];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = new Integer(i);
			
			String iconString = iter.next();
			
			choices.add(iconString);
			icons.add(choicesMap.get(iconString));
			
			if (iconString.equals(defaultChoice))
				initialIndex = i;
		}
		
		selector = new JComboBox(intArray);
		selector.setSelectedIndex(initialIndex);
		selector.setRenderer(new SelectorRenderer());
		add(selector, BorderLayout.CENTER);
	}
	
	/**
	 * Sets the maximum number of rows that will be shown at once in this popup
	 * @param count
	 */
	
	public void setMaximumRowCount(int count) {
		selector.setMaximumRowCount(count);
	}
	
	private class SelectorRenderer extends JLabel implements ListCellRenderer {
		private SelectorRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		@Override public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			
			if (value == null) {
				setIcon(null);
			} else {
				int selectedIndex = ((Integer)value).intValue();
			
				setIcon(icons.get(selectedIndex));
			}
				
			return this;
		}
	}
}
