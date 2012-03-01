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

package net.sf.hale.interfacelock;

import net.sf.hale.Game;
import net.sf.hale.entity.Entity;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * An EntityOffsetAnimation that moves an entity from one tile to an
 * adjacent tile smoothly over a specified time
 * @author Jared Stephen
 *
 */

public class EntityMovementAnimation extends EntityOffsetAnimation {
	private Entity mover;
	private int initialX, initialY;
	
	private float duration;
	private float elapsed;
	private float xPerSecond, yPerSecond;
	
	/**
	 * Create a new Animation moving the specified entity to the destination point
	 * over Game.config.getCombatDelay() milliseconds
	 * @param mover the entity being moved
	 * @param destination the destination grid point
	 */
	
	public EntityMovementAnimation(Entity mover, Point destination) {
		super(mover.getAnimatingOffsetPoint());
		
		this.mover = mover;
		this.initialX = mover.getX();
		this.initialY = mover.getY();
		
		Point curScreen = AreaUtil.convertGridToScreen(mover.getPosition());
		Point destScreen = AreaUtil.convertGridToScreen(destination);
		
		duration = Game.config.getCombatDelay() / 1000.0f;
		elapsed = 0.0f;
		
		xPerSecond = (destScreen.x - curScreen.x) / duration;
		yPerSecond = (destScreen.y - curScreen.y) / duration;
		
		duration += 0.025f;
	}
	
	@Override protected boolean runAnimation(float seconds) {
		elapsed += seconds;
		
		if (elapsed > duration || initialX != mover.getX() || initialY != mover.getY()) {
			resetOffset();
			return true;
		} else {
			setOffset(xPerSecond * elapsed, yPerSecond * elapsed);
			return false;
		}
	}
}
