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

package net.sf.hale.view;

import net.sf.hale.Area;
import net.sf.hale.AreaTransition;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Trap;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;

/**
 * A widget for showing a scaled down map of the current area with symbols
 * for different entities within the area
 * @author Jared Stephen
 *
 */

public class MiniMapWindow extends GameSubWindow {
	private Image tile, impass, friendly, neutral, hostile, container, door, trap, transition;
	
	private Area area;
	
	private ScrollPane scrollPane;
	private Content content;
	
	/**
	 * Creates an empty mini map Widget.  updateContent should be used to set
	 * the Area being viewed.
	 */
	
	public MiniMapWindow() {
		content = new Content();
		scrollPane = new ScrollPane(content);
		scrollPane.setTheme("mappane");
		this.add(scrollPane);
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		tile = themeInfo.getImage("tile");
		impass = themeInfo.getImage("impassableTile");
		friendly = themeInfo.getImage("friendlyCreature");
		neutral = themeInfo.getImage("neutralCreature");
		hostile = themeInfo.getImage("hostileCreature");
		container = themeInfo.getImage("container");
		door = themeInfo.getImage("door");
		trap = themeInfo.getImage("trap");
		transition = themeInfo.getImage("transition");
		
		content.tileSize = tile.getWidth();
		content.tileWidth = content.tileSize * 3 / 4;
		content.tileHalf = content.tileSize / 2;
		content.tileQuarter = content.tileSize / 4;
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		// scroll to selected creature
		if (visible) {
			Entity selected = Game.curCampaign.party.getSelected();
			
			int screenX = selected.getX() * content.tileWidth;
			int screenY = selected.getY() * content.tileSize;
			if (selected.getX() % 2 == 1) screenY += content.tileHalf;
			
			scrollPane.setScrollPositionX(screenX - scrollPane.getWidth() / 2);
			scrollPane.setScrollPositionY(screenY - scrollPane.getHeight() / 2);
		}
	}
	
	public void updateContent(Area area) {
		if (this.area != area && area != null) {
			this.area = area;
			this.setTitle(area.getName());
			
			invalidateLayout();
			scrollPane.invalidateLayout();
			if (getWidth() > getMaxWidth() || getHeight() > getMaxHeight()) {
				setSize(getMaxWidth(), getMaxHeight());
			}
		}
	}
	
	@Override public int getMaxWidth() {
		return content.getPreferredWidth() + scrollPane.getBorderHorizontal() + getBorderHorizontal();
	}
	
	@Override public int getMaxHeight() {
		return content.getPreferredHeight() + scrollPane.getBorderVertical() + getBorderVertical();
	}
	
	private class Content extends Widget {
		private int tileSize, tileWidth, tileHalf, tileQuarter;
		
		@Override public int getPreferredWidth() {
			return tileWidth * area.getWidth() - tileHalf + getBorderHorizontal();
		}
		
		@Override public int getPreferredHeight() {
			return tileSize * area.getHeight() - tileSize + getBorderVertical();
		}
		
		@Override public void paintWidget(GUI gui) {
			Creature selected = Game.curCampaign.party.getSelected();
			
			AnimationState as = getAnimationState();
			
			GL11.glPushMatrix();
			GL11.glTranslatef(getInnerX() - tileQuarter, getInnerY() - tileHalf, 0.0f);
			
			boolean[][] pass = area.getPassability();
			boolean[][] explored = area.getExplored();
			boolean[][] visible = area.getVisibility();
			
			for (int y = 0; y < explored[0].length; y++) {
				for (int x = 0; x < explored.length; x++) {
					if (!explored[x][y]) continue;
					
					int screenX = x * tileWidth;
					int screenY = y * tileSize;
					if (x % 2 == 1) screenY += tileHalf;
					
					if (!pass[x][y] || area.getElevationGrid().getElevation(x, y) != 0)
						impass.draw(as, screenX, screenY);
					else
						tile.draw(as, screenX, screenY);
					
					// only draw entities for visible tiles
					if (!visible[x][y]) continue;
					
					AreaTransition areaTransition = area.getTransitionAtGridPoint(x, y);
					if (areaTransition != null && areaTransition.isActivated())
						transition.draw(as, screenX, screenY);
					
					if (area.getContainerAtGridPoint(x, y) != null)
						container.draw(as, screenX, screenY);
					
					if (area.getDoorAtGridPoint(x, y) != null)
						door.draw(as, screenX, screenY);
					
					Trap areaTrap = area.getTrapAtGridPoint(x, y);
					if (areaTrap != null && areaTrap.isVisible())
						trap.draw(as, screenX, screenY);
					
					Creature creature = area.getCreatureAtGridPoint(x, y);
					if (creature != null) {
						switch (selected.getFaction().getRelationship(creature)) {
						case Hostile:
							hostile.draw(as, screenX, screenY);
							break;
						case Neutral:
							neutral.draw(as, screenX, screenY);
							break;
						case Friendly:
							friendly.draw(as, screenX, screenY);
							break;
						}
					}
					
					
				}
			}
			
			GL11.glPopMatrix();
		}
	}
}
