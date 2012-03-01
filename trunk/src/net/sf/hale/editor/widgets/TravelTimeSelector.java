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

import net.sf.hale.WorldMapLocation;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class TravelTimeSelector extends DialogLayout implements Runnable {
	private final Label areaName;
	private final ValueAdjusterInt timeAdjuster;
	private final Label hoursLabel;
	
	private final WorldMapLocation start, end;
	
	public TravelTimeSelector(WorldMapLocation start, WorldMapLocation end, int hours) {
		this.setTheme("/itemlistentrypane");
		
		this.start = start;
		this.end = end;
		
		areaName = new Label(end.getName());
		hoursLabel = new Label("hours");
		timeAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 999, hours));
		timeAdjuster.getModel().addCallback(this);
		
		Group bottomH = this.createSequentialGroup(timeAdjuster, hoursLabel);
		Group bottomV = this.createParallelGroup(timeAdjuster, hoursLabel);
		
		Group mainH = this.createParallelGroup(areaName);
		mainH.addGroup(bottomH);
		
		Group mainV = this.createSequentialGroup(areaName);
		mainV.addGroup(bottomV);
		
		this.setHorizontalGroup(mainH);
		this.setVerticalGroup(mainV);
	}
	
	@Override public void run() {
		end.setTravelTime(start.getName(), timeAdjuster.getValue());
		start.setTravelTime(end.getName(), timeAdjuster.getValue());
	}
}
