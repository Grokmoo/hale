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

package net.sf.hale;

import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.Targeter;
import net.sf.hale.defaultability.DefaultAbility;
import net.sf.hale.defaultability.MouseActionList;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.interfacelock.InterfaceCombatLock;
import net.sf.hale.rules.CombatRunner;
import net.sf.hale.rules.ItemList;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.view.AreaViewer;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ThemeInfo;

public class AreaListener {
	private int lastMouseX, lastMouseY;
	
	private Point curGridPoint;
	private MouseActionList.Condition curMouseCondition;
	
	private ThemeInfo themeInfo;
	
	private Area area;
	private final AreaViewer areaViewer;
	private AreaUtil areaUtil;
	
	private final CombatRunner combatRunner;
	
	private final Point mouseDragStart = new Point(false);
	
	private boolean mouseClickedWithoutDragging = true;
	
	private TargeterManager targeterManager;
	
	public AreaListener(Area area, AreaViewer areaViewer) {
		this.areaViewer = areaViewer;
		
		setArea(area);
		
		combatRunner = new CombatRunner();
		
		targeterManager = new TargeterManager();
	}
	
	public void setArea(Area area) {
		this.area = area;
		
		areaUtil = new AreaUtil(area);
	}
	
	public void setThemeInfo(ThemeInfo themeInfo) {
		this.themeInfo = themeInfo;
	}
	
	public AreaUtil getAreaUtil() { return areaUtil; }
	public CombatRunner getCombatRunner() { return combatRunner; }
	public AreaViewer getAreaViewer() { return areaViewer; }
	
	public void checkKillEntity(Entity e) {
		if (e.getType() != Entity.Type.CREATURE) return;
		
		if (!area.getEntities().containsEntity(e)) return;
		
		Creature c = (Creature)e;
		
		if (c.isSummoned() && c.getCurrentHP() <= 0) {
			boolean runNextCombatTurn = false;
			
			if (c.isPlayerSelectable() && combatRunner.lastActiveCreature() == c) {
				runNextCombatTurn = true;
			}
			
			Game.mainViewer.addMessage("red", c.getName() + " is unsummoned.");
			area.getEntities().removeEntity(c);
			Game.curCampaign.party.remove(c);
			
			if (runNextCombatTurn) {
				Game.interfaceLocker.add(new InterfaceCombatLock(c, 500));
			}
			
			c.getEffects().endAllAnimations();
			c.getEffects().executeOnAll(ScriptFunctionType.onTargetExit, c);
				
		} else if (!c.isPlayerSelectable() && c.getCurrentHP() <= -20) {
			Game.mainViewer.addMessage("red", c.getName() + " is dead.");
			area.getEntities().removeEntity(c);
			
			if (!c.getLoot().alreadyGenerated()) {
				ItemList loot = c.getLoot().generate();
				for (int i = 0; i < loot.size(); i++) {
					Item item = loot.getItem(i);
					item.setPosition(c.getX(), c.getY());
					area.addItem(item, loot.getQuantity(i));
				}
			}
			
			c.getEffects().endAllAnimations();
			c.getEffects().executeOnAll(ScriptFunctionType.onTargetExit, c);
		}
	}
	
	public void nextTurn() {
		areaUtil.setPartyVisibility(area);
		
		areaViewer.mouseHoverValid = true;
		
		if (Game.isInTurnMode()) {
			combatRunner.nextCombatTurn();
		} else {
			Game.curCampaign.getDate().incrementRound();
			Game.curCampaign.curArea.getEntities().elapseRoundsForDeadCreatures(1);
			
			for (Entity e : Game.curCampaign.curArea.getEntities()) {
				if (e.getType() == Entity.Type.CREATURE) {
					Creature c = (Creature)e;
					
					if (c.isPlayerSelectable() || c.isAIActive()) {
						c.newEncounter();
						c.elapseRounds(1);
					}
					
					if (c.isDead()) {
						Game.mainViewer.updateEntity(c);
					}
				}
			}
			
			Game.selectedEntity = Game.curCampaign.party.getSelected();
			Game.mainViewer.updateInterface();
		}
	}
	
	private boolean isPartyMoving() {
		for (Creature partyMember : Game.curCampaign.party) {
			if (partyMember.isCurrentlyMoving()) return true;
		}
		
		return false;
	}
	
	public TargeterManager getTargeterManager() { return targeterManager; }
	
	public boolean handleEvent(Event evt) {
		if (evt.getType() == Event.Type.MOUSE_MOVED) {
			this.lastMouseX = evt.getMouseX();
			this.lastMouseY = evt.getMouseY();
			
			computeMouseState();
		}
		
		if (curGridPoint == null) return true;
		
		switch (evt.getType()) {
		case MOUSE_BTNDOWN:
			mouseClickedWithoutDragging = true;
			
			if (!isPartyMoving() && evt.getMouseButton() == Event.MOUSE_RBUTTON) {
				if (targeterManager.isInTargetMode()) {
					targeterManager.getCurrentTargeter().showMenu(evt.getMouseX() - 2, evt.getMouseY() - 25);
				} else if (Game.interfaceLocker.locked()) {
					// Do nothing
				} else {
					Game.mouseActions.showDefaultAbilitiesMenu(Game.curCampaign.party.getSelected(),
							curGridPoint, evt.getMouseX() - 2, evt.getMouseY() - 25);
				}
			}
			break;
		case MOUSE_BTNUP:
			if (evt.getMouseButton() != Event.MOUSE_LBUTTON) break; 

			if (mouseDragStart.valid) {
				mouseDragStart.valid = false;
			} else if (targeterManager.isInTargetMode() && mouseClickedWithoutDragging) {
				targeterManager.getCurrentTargeter().performLeftClickAction();
			} else if (curMouseCondition != null && mouseClickedWithoutDragging) {
				DefaultAbility ability = curMouseCondition.getAbility();
				if (ability != null) {
					Creature parent = Game.curCampaign.party.getSelected();
					if (ability.canActivate(parent, curGridPoint)) ability.activate(parent, curGridPoint);
				}
				
			}
			break;
		case MOUSE_DRAGGED:
			mouseClickedWithoutDragging = false;
			
			if (mouseDragStart.valid) {
				mouseDragStart.valid = false;
				areaViewer.scroll(-2 * (evt.getMouseX() - mouseDragStart.x), -2 * (evt.getMouseY() - mouseDragStart.y));
			} else {
				mouseDragStart.x = evt.getMouseX();
				mouseDragStart.y = evt.getMouseY();
				mouseDragStart.valid = true;
			}
			break;
		}
		
		return true;
	}
	
	public void computeMouseState() {
		int xOffset = lastMouseX + areaViewer.getScrollX() - areaViewer.getX();
		int yOffset = lastMouseY + areaViewer.getScrollY() - areaViewer.getY();
		
		curGridPoint = AreaUtil.convertScreenToGrid(xOffset, yOffset);
		
		Game.mainViewer.getMouseOver().setPoint(curGridPoint);
		
		areaViewer.mouseHoverTile.valid = true;
		areaViewer.mouseHoverTile.x = curGridPoint.x;
		areaViewer.mouseHoverTile.y = curGridPoint.y;
		
		if (targeterManager.isInTargetMode()) {
			Targeter targeter = targeterManager.getCurrentTargeter();
			
			if (targeter.getParent().isPlayerSelectable()) {
				targeter.setMousePosition(xOffset, yOffset, curGridPoint);
				
				areaViewer.mouseHoverValid = targeter.mouseHoverValid();
				curMouseCondition = targeter.getMouseActionCondition();
				
				targeter.setTitleText();
			}
			
		} else if (Game.interfaceLocker.locked()) {
			curMouseCondition = MouseActionList.Condition.Cancel;
			areaViewer.mouseHoverValid = false;
			
			Game.mainViewer.clearTargetTitleText();
		} else {
			curMouseCondition = Game.mouseActions.getDefaultMouseCondition(Game.curCampaign.party.getSelected(),
					curGridPoint);
			
			areaViewer.mouseHoverValid = curMouseCondition != MouseActionList.Condition.Cancel;
			
			Game.mainViewer.clearTargetTitleText();
		}
		
		String cursor = Game.mouseActions.getMouseCursor(curMouseCondition);
		areaViewer.setMouseCursor(themeInfo.getMouseCursor(cursor));
	}
	
	public int getMouseGUIX() { return lastMouseX; }
	public int getMouseGUIY() { return lastMouseY; }
	
	public int getLastMouseX() { return lastMouseX + areaViewer.getScrollX() - areaViewer.getX(); }
	public int getLastMouseY() { return lastMouseY + areaViewer.getScrollY() - areaViewer.getY(); }
}