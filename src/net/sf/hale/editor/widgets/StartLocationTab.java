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

package net.sf.hale.editor.widgets;

import net.sf.hale.Area;
import net.sf.hale.util.Point;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class StartLocationTab extends DialogLayout {
	private int selectedIndex = -1;
	
	private final Area area;
	private final Button newPoint, removePoint;
	
	private final ListBox<Point> locationsBox;
	private final SimpleChangableListModel<Point> locationsModel;
	
	public StartLocationTab(Area area) {
		this.area = area;
		
		newPoint = new Button("New Point");
		newPoint.addCallback(new Runnable() {
			@Override public void run() {
				Point p = new Point(0, 0);
			
				locationsModel.addElement(p);
				StartLocationTab.this.area.getStartLocations().add(p);
				
				selectedIndex = -1;
			}
		});
		
		removePoint = new Button("Remove Selected");
		removePoint.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedIndex != -1) {
					locationsModel.removeElement(selectedIndex);
					StartLocationTab.this.area.getStartLocations().remove(selectedIndex);
				}
				
				selectedIndex = -1;
			}
		});
		
		locationsModel = new SimpleChangableListModel<Point>();
		for (Point p : area.getStartLocations()) {
			locationsModel.addElement(p);
		}
		
		locationsBox = new ListBox<Point>(locationsModel);
		locationsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				int index = locationsBox.getSelected();
				
				if (index != -1)
					selectedIndex = index;
			}
			
		});
		
		this.setTheme("/dialoglayout");
		
		Group topH = this.createSequentialGroup(newPoint, removePoint);
		Group topV = this.createParallelGroup(newPoint, removePoint);
		
		Group mainH = this.createParallelGroup(topH);
		mainH.addWidget(locationsBox);
		
		Group mainV = this.createSequentialGroup(topV);
		mainV.addWidget(locationsBox);
		
		this.setHorizontalGroup(mainH);
		this.setVerticalGroup(mainV);
	}
	
	public void setStartLocation(int index, Point location) {
		if (index < 0 || index >= locationsModel.getNumEntries()) return;
		
		locationsModel.setElement(index, new Point(location));
		area.getStartLocations().set(index, new Point(location));
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
}
