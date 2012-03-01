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

package net.sf.hale.entity;

import net.sf.hale.Game;
import net.sf.hale.ScriptInterface;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.rules.Currency;
import net.sf.hale.util.SimpleJSONObject;

public class Door extends Openable {
	private boolean transparent;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = super.save();
		
		// no additional data needs stored
		
		return data;
	}
	
	@Override public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		super.load(data, refHandler);
		
		// no additional data needs loaded
	}
	
	public Door(String id, String name, String closedIcon, String openIcon, Item.ItemType itemType,
				String description, boolean transparent) {
		super(id, name, closedIcon, itemType, description, new Currency(), openIcon, closedIcon);
		this.type = Entity.Type.DOOR;
		
		this.transparent = transparent;
	}
	
	public Door(Door other) {
		super(other);
		this.type = Entity.Type.DOOR;
		
		this.transparent = other.transparent;
	}
	
	public boolean isTransparent() { return this.transparent; }
	public void setTransparent(boolean transparent) { this.transparent = transparent; }
	
	@Override public void open(Creature opener) {
		super.open(opener);
		
		if (this.isOpen()) {
			setAreaTransparency();

			Game.areaListener.getAreaUtil().updateVisibility(Game.curCampaign.curArea);
			Game.areaListener.getCombatRunner().checkAIActivation();
			Game.areaViewer.mouseHoverValid = true;
		}
		
		ScriptInterface.performSearchChecksForCreature(opener, Game.ruleset.getValue("HideOpenDoorPenalty"));
	}
	
	@Override public void close(Creature closer) {
		super.close(closer);

		if (!this.isOpen()) {
			setAreaTransparency();

			Game.areaListener.getAreaUtil().updateVisibility(Game.curCampaign.curArea);

			Game.areaViewer.mouseHoverValid = false;
		}
	}
	
	public void setAreaTransparency() {
		if (!this.transparent)
			Game.curCampaign.curArea.getTransparency()[this.getX()][this.getY()] = this.isOpen();
	}
}
