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
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.rules.Currency;
import net.sf.hale.util.SimpleJSONObject;

public abstract class Openable extends Item {
	private String openIcon;
	private String closedIcon;
	
	private boolean isOpen;
	private boolean locked = false;
	private boolean keyRequired = false;
	private boolean removeKeyOnUnlock = false;
	private String key;
	private int lockDifficulty;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = super.save();
		
		data.put("open", isOpen);
		data.put("locked", locked);
		
		return data;
	}
	
	@Override public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		super.load(data, refHandler);
		
		this.isOpen = data.get("open", false);
		this.locked = data.get("locked", false);
		
		if (isOpen)
			setIcon(this.openIcon);
		else
			setIcon(this.closedIcon);
	}
	
	public Openable(String id, String name, String icon, Item.ItemType itemType,
			String description, Currency currency, String openIcon, String closedIcon) {
		super(id, name, icon, itemType, description, currency);
		this.isOpen = false;
		this.openIcon = openIcon;
		this.closedIcon = closedIcon;
	}
	
	public Openable(Openable other) {
		super(other);
		this.locked = other.locked;
		this.lockDifficulty = other.lockDifficulty;
		this.key = other.key;
		this.keyRequired = other.keyRequired;
		this.isOpen = other.isOpen;
		this.removeKeyOnUnlock = other.removeKeyOnUnlock;
		this.openIcon = other.openIcon;
		this.closedIcon = other.closedIcon;
	}
	
	public void setOpenIcon(String openIcon) {
		this.openIcon = openIcon;
	}
	
	public void setClosedIcon(String closedIcon) {
		this.closedIcon = closedIcon;
		this.setIcon(closedIcon);
	}
	
	public String getOpenIcon() { return this.openIcon; }
	public String getClosedIcon() { return this.closedIcon; }
	
	/**
	 * The specified Creature will attempt to pick the lock on this Openable.
	 * If the Game is in combat mode, the Creature must make a "Locks" skill check.
	 * If not, the Creature will automatically roll 100 on the check.
	 * 
	 * Returns true if this Openable is unlocked, false if it remains locked.  Returns
	 * true if this Openable was already unlocked when this function was called.
	 * 
	 * @param unlocker the Creature picking the lock
	 * @return true if and only if this Openable is unlocked
	 */
	
	public boolean tryPickLock(Creature unlocker) {
		if (!this.locked) return true;
		
		if (!keyRequired) {
			if (Game.isInTurnMode()) {
				if (unlocker.skillCheck("Locks", lockDifficulty)) {
					unlock(unlocker);
					return true;
				}
			} else {
				if (unlocker.skillCheckRoll100("Locks", lockDifficulty)) {
					unlock(unlocker);
					return true;
				}
			}
		} else {
			Game.mainViewer.addMessage("orange", "A specific key is required to unlock that object.");
		}
		
		return false;
	}
	
	public void unlock() {
		if(locked && hasScript())
			getScript().executeFunction(ScriptFunctionType.onUnlock, this);
		
		this.locked = false;
		
		Game.mainViewer.updateInterface();
	}
	
	public void unlock(Creature unlocker) {
		if (locked && hasScript())
			getScript().executeFunction(ScriptFunctionType.onUnlock, this, unlocker);
		
		this.locked = false;
		
		Game.mainViewer.updateInterface();
	}
	
	public void open(Creature opener) {
		if (this.locked && this.key != null) {
			if (opener.getInventory().hasItem(Game.entityManager.getItem(this.key))) {
				Game.mainViewer.addMessage("orange", opener.getName() + " uses a key.");
				unlock(opener);
				if (this.removeKeyOnUnlock) opener.getInventory().removeItemEvenIfEquipped(this.key);
			}
		}
		
		if (!this.locked) {
			if (!isOpen && hasScript())
				getScript().executeFunction(ScriptFunctionType.onOpen, this, opener);
			
			this.isOpen = true;
			
			this.setIcon(this.openIcon);
			
		} else {
			Game.mainViewer.addMessage("orange", "That object is locked.");
		}
	}
	
	public void close(Creature closer) {
		if (isOpen && hasScript())
			getScript().executeFunction(ScriptFunctionType.onClose, this, closer);
		
		this.isOpen = false;
		
		this.setIcon(this.closedIcon);
	}
	
	public boolean isOpen() { return isOpen; }
	
	public boolean isLocked() { return locked; }
	public int getLockDifficulty() { return lockDifficulty; }
	public String getKey() { return key; }
	public boolean isKeyRequired() { return keyRequired; }
	public boolean removeKeyOnUnlock() { return removeKeyOnUnlock; }
	
	public void setLocked(boolean locked) { this.locked = locked; }
	
	public void setLockDifficulty(int difficulty) {
		this.locked = true;
		this.lockDifficulty = difficulty;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public void setKeyRequired(boolean keyRequired) {
		this.keyRequired = keyRequired;
	}
	
	public void setRemoveKeyOnUnlock(boolean removeKey) {
		this.removeKeyOnUnlock = removeKey;
	}
}
