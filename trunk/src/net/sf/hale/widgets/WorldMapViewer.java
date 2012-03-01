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

package net.sf.hale.widgets;

import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.WorldMapLocation;
import net.sf.hale.resource.SpriteManager;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

/**
 * A Widget that displays the {@link Sprite} that is specified as the
 * world map image for a given Campaign.  The Widget can also display
 * icons for a list of WorldMapLocations.
 * 
 * @author Jared Stephen
 *
 */

public class WorldMapViewer extends Widget {
	private Sprite sprite;
	private List<WorldMapLocation> locations;
	
	/**
	 * Create a new WorldMapViewer widget.  No locations are shown
	 * until {@link #updateLocations(List)} is called.  The sprite that
	 * is drawn will be the current campaign world map sprite.
	 */
	
	public WorldMapViewer() {
		updateSprite();
	}
	
	/**
	 * Sets the sprite drawn by this viewer to the current campaign world map sprite,
	 * if one exists
	 */
	
	public void updateSprite() {
		if (Game.curCampaign != null)
			this.sprite = Game.curCampaign.getWorldMapSprite();
		else
			this.sprite = null;
	}
	
	@Override public void paintWidget(GUI gui) {
		super.paintWidget(gui);
		
		int offsetX = getInnerX();
		int offsetY = getInnerY();
		
		GL11.glPushMatrix();
		
		GL11.glTranslatef(offsetX, offsetY, 0.0f);
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
		if (sprite != null) {
			sprite.draw(0, 0);
		}
		
		if (locations != null) {
			for (WorldMapLocation location : locations) {
				if (location.getIcon() != null)
					SpriteManager.getSprite(location.getIcon()).draw(location.getPosition());
			}
		}
		
		GL11.glPopMatrix();
	}
	
	@Override public int getPreferredWidth() {
		if (sprite == null) return getBorderHorizontal();
		else return sprite.getWidth() + getBorderHorizontal();
	}
	
	@Override public int getPreferredHeight() {
		if (sprite == null) return getBorderVertical();
		else return sprite.getHeight() + getBorderVertical();
	}
	
	/**
	 * The specified WorldMapLocations will be displayed as icons in
	 * future rendering of this Widget.  Any previous locations not in
	 * the supplied list are discarded.
	 * 
	 * @param locations The List of WorldMapLocations to be shown
	 */
	
	public void updateLocations(List<WorldMapLocation> locations) {
		this.locations = locations;
	}
}
