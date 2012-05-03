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

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.ability.Aura;
import net.sf.hale.ability.Effect;
import net.sf.hale.ability.EffectTarget;
import net.sf.hale.ability.EntityEffectSet;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.interfacelock.EntityOffsetAnimation;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Faction;
import net.sf.hale.ScriptState;
import net.sf.hale.rules.SubIconList;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.view.ConversationPopup;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Color;

public abstract class Entity implements Comparable<Entity>, EffectTarget, Saveable {
	public enum Type {
		ENTITY, CREATURE, ITEM, CONTAINER, DOOR, TRAP,
	};
	
	protected SubIconList subIconList;
	private String faction;
	private Point screenPosition;
	private String description;
	private String icon;
	private Sprite iconSprite;
	private Color iconColor;
	protected Type type = Type.ENTITY;
	private String conversationScript = null;
	protected boolean[][] visibility;
	private EntityOffsetAnimation curOffsetAnimation;
	protected boolean drawOnlyHandSubIcons;
	private List<EntityViewer> viewers;
	private Point animatingOffset;
	
	private String id;
	private ScriptState scriptState;
	private final Point position;
	private final Point lastPosition = new Point(0, 0);
	private boolean playerSelectable;
	private EntityEffectSet effects;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("id", id);
		data.put("type", getType().toString());
		data.put("ref", SaveGameUtil.getRef(this));
		
		if (position.valid) {
			data.put("xPosition", position.x);
			data.put("yPosition", position.y);
		}

		if (playerSelectable) data.put("playerSelectable", playerSelectable);
		
		JSONOrderedObject effectsData = effects.save();
		if (effectsData.size() > 0)
			data.put("effects", effectsData);
		
		if (!scriptState.isEmpty())
			data.put("scriptState", scriptState.save());
		
		return data;
	}
	
	public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		refHandler.add(data.get("ref", null), this);
		
		if (data.containsKey("xPosition")) {
			position.x = data.get("xPosition", 0);
			position.y = data.get("yPosition", 0);
			position.valid = true;
		} else {
			position.x = 0;
			position.y = 0;
			position.valid = false;
		}
		
		screenPosition = AreaUtil.convertGridToScreen(position);
		
		if (data.containsKey("playerSelectable")) playerSelectable = data.get("playerSelectable", false);
		
		if (data.containsKey("effects"))
			effects.load(data.getObject("effects"), refHandler, this);
		else
			effects.clear();
		
		if (data.containsKey("scriptState"))
			scriptState = ScriptState.load(data.getObject("scriptState"));
		
		this.faction = Game.ruleset.getString("DefaultFaction");
	}
	
	protected Entity(String id) {
		this(id, "", new Point(false), null, null);
	}
	
	public Entity(Entity e) {
		this(e.id, e.icon, new Point(e.position), e.description, e.faction);
		
		this.playerSelectable = e.playerSelectable;
		this.conversationScript = e.conversationScript;
		
		if (e.subIconList != null) {
			this.subIconList = new SubIconList(e.subIconList);
		}
		
		this.iconColor = e.iconColor;
		this.drawOnlyHandSubIcons = e.drawOnlyHandSubIcons;
		
		effects = new EntityEffectSet(e.effects, this);
	}
	
	public Entity(String id, String icon, int x, int y) {
		this(id, icon, new Point(x, y), null, null);
	}
	
	public Entity(String id, String icon, String description) {
		this(id, icon, new Point(false), description, null);
	}
	
	public Entity(String id, String icon, Point position, String description, String faction) {
		this.id = id;
		setIcon(icon);
		this.position = position;
		this.screenPosition = AreaUtil.convertGridToScreen(position);
		this.playerSelectable = false;
		this.description = description;
		this.faction = faction;
		
		effects = new EntityEffectSet();
		viewers = new ArrayList<EntityViewer>();
		
		animatingOffset = new Point();
		
		scriptState = new ScriptState();
	}
	
	public Point getSubIconScreenPosition(String type) {
		Point screenPoint = new Point(screenPosition);
		
		if (subIconList == null) return screenPoint;
		
		Sprite sprite = subIconList.getSprite(type);
		Point offset = subIconList.getOffset(type);
		
		if (sprite == null || offset == null)
			return screenPoint;
		
		screenPoint.x += offset.x + sprite.getWidth() / 2;
		screenPoint.y += offset.y + sprite.getHeight() / 2;
		return screenPoint;
	}
	
	/**
	 * Adds the specified EntityViewer to the list of EntityViewers
	 * for this Entity
	 * @param viewer the EntityViewer to add
	 */
	
	public void addViewer(EntityViewer viewer) {
		viewers.add(viewer);
	}
	
	/**
	 * Removes the specified EntityViewer from the list of EntityViewers
	 * for this Entity
	 * @param viewer the viewer to remove
	 */
	
	public void removeViewer(EntityViewer viewer) {
		viewers.remove(viewer);
	}
	
	/**
	 * Causes all EntityViewers that are registered with this Entity via
	 * {@link #addViewer(EntityViewer)} to be updated via {@link EntityViewer#entityUpdated()}
	 */
	
	public void updateViewers() {
		for (EntityViewer viewer : viewers) {
			viewer.entityUpdated();
		}
	}
	
	public void closeViewers() {
		for (EntityViewer viewer : viewers) {
			viewer.closeViewer();
		}
		
		viewers.clear();
	}
	
	public SubIconList getSubIcons() { return subIconList; }
	
	public boolean drawWithSubIcons() { return subIconList != null; }
	
	public boolean drawOnlyHandSubIcons() {
		return this.drawOnlyHandSubIcons;
	}
	
	public void setDrawOnlyHandSubIcons(boolean draw) {
		this.drawOnlyHandSubIcons = draw;
	}
	
	public void setDrawWithSubIcons(boolean draw) {
		if (!draw) {
			subIconList = null;
		} else {
			subIconList = new SubIconList();
		}
	}
	
	public void cancelOffsetAnimation() {
		if (curOffsetAnimation != null)
			curOffsetAnimation.cancel();
		
		animatingOffset.x = 0;
		animatingOffset.y = 0;
	}
	
	public void addOffsetAnimation(EntityOffsetAnimation animation) {
		if (curOffsetAnimation != null)
			curOffsetAnimation.cancel();
		
		this.curOffsetAnimation = animation;
	}
	
	public Point getAnimatingOffsetPoint() {
		return this.animatingOffset;
	}
	
	public final void draw(Point p) {
		draw(p.x, p.y);
	}
	
	public final void draw(int x, int y) {
		if (icon != null) {
			GL11.glColor4ub(iconColor.getR(), iconColor.getG(), iconColor.getB(), iconColor.getA());
			iconSprite.drawWithOffset(x, y);
		}
		
		if (subIconList != null) {
			subIconList.draw(x, y);
		}
	}
	
	public final void drawForArea(int x, int y) {
		if (icon != null) {
			GL11.glColor4ub(iconColor.getR(), iconColor.getG(), iconColor.getB(), iconColor.getA());
			iconSprite.drawWithOffset(x + animatingOffset.x, y + animatingOffset.y);
		}
		
		if (subIconList != null) {
			subIconList.draw(x + animatingOffset.x, y + animatingOffset.y);
		}
	}
	
	public boolean getVisibility(Point p) { return visibility[p.x][p.y]; }
	
	public boolean getVisibility(int x, int y) { return visibility[x][y]; }
	
	public boolean[][] getVisibility() { return visibility; }
	
	public void setVisibility(boolean newArea) {
		if (visibility == null || newArea) visibility = Game.curCampaign.curArea.getMatrixOfSize();
		
		Game.areaListener.getAreaUtil().setVisibilityWithRespectToPosition(visibility, position);
	}
	
	public void startConversation(Entity talker) {
		ConversationPopup popup = new ConversationPopup(this, talker, this.getConversationScript());
		popup.startConversation();
	}
	
	public void setConversationScript(String script) { this.conversationScript = script; }
	public String getConversationScript() { return this.conversationScript; }
	
	public boolean isSelected() { return Game.selectedEntity == this; }
	
	public void resetAll() { }
	
	public void newEncounter() { }
	
	/**
	 * Elapses the specified number of rounds for all effects applied to this Entity
	 * not otherwise being tracked and all effects created by this Entity
	 * @param rounds the number of rounds to elapse
	 * @return true if and only if this Entity has Effects applied to other entities
	 * that it continues to track after calling this method.
	 */
	
	public boolean elapseRounds(int rounds) {
		effects.elapseRounds(this, rounds);
		
		return false;
	}
	
	public EntityEffectSet getEffects() { return effects; }
	
	public Effect createEffect() {
		return new Effect();
	}
	
	public Effect createEffect(String scriptID) {
		return new Effect(scriptID);
	}
	
	public Aura createAura(String scriptID) {
		return new Aura(scriptID);
	}
	
	public void applyEffect(Effect effect) {
		effect.setTarget(this);
		effect.executeFunction(ScriptFunctionType.onApply, effect);
		
		applyEffectBonuses(effect);
		
		effect.startAnimations();
		
		this.effects.add(effect, position.valid);
	}
	
	@Override public int getSpellResistance() { return 0; }
	
	@Override public void removeEffect(Effect effect) {
		effect.executeFunction(ScriptFunctionType.onRemove, effect);
		
		removeEffectBonuses(effect);
		
		effect.endAnimations();
		
		this.effects.remove(effect);
	}
	
	protected void applyEffectBonuses(Effect effect) { }
	protected void removeEffectBonuses(Effect effect) { }
	
	public final boolean positionValid() { return position.valid; }
	public final int getX() { return position.x; }
	public final int getY() { return position.y; }
	
	public int getLastPositionX() { return lastPosition.x; }
	public int getLastPositionY() { return lastPosition.y; }
	
	public Point getScreenPosition() {
		return AreaUtil.convertGridToScreenAndCenter(this.getX(), this.getY());
	}
	
	public boolean setPosition(Point position) {
		boolean returnValue = setPosition(position.x, position.y);
		
		this.position.valid = position.valid;
		
		return returnValue;
	}
	
	public void invalidatePosition() {
		this.position.valid = false;
	}
	
	public boolean setPosition(int x, int y) {
		this.lastPosition.x = this.position.x;
		this.lastPosition.y = this.position.y;
		this.lastPosition.valid = this.position.valid;
		
		this.position.x = x;
		this.position.y = y;
		this.position.valid = true;
		
		Point screenOld = screenPosition;
		screenPosition = AreaUtil.convertGridToScreen(position);
		
		effects.offsetAnimationPositions(screenPosition.x - screenOld.x, screenPosition.y - screenOld.y);
		effects.moveAuras();
		
		if (Game.curCampaign.curArea != null) {
			Game.curCampaign.curArea.getEntities().moveEntity(this);
		}
		
		animatingOffset.x = 0;
		animatingOffset.y = 0;
		
		return false;
	}
	
	public Point getPosition() { return new Point(position); }
	
	public void setID(String id) { this.id = id; }
	public void setPlayerSelectable(boolean playerSelectable) { this.playerSelectable = playerSelectable; }
	public void setFaction(Faction faction) { this.faction = faction.getName(); }
	public void setFaction(String faction) { this.faction = faction; }
	public void setDescription(String description) { this.description = description; }
	
	public void setIcon(String icon) {
		this.icon = icon;
		if (icon != null) {
			this.iconSprite = SpriteManager.getSprite(icon);
		}
	}
	
	public void setIconColor(Color color) {
		if (color == null) this.iconColor = Color.WHITE;
		else this.iconColor = color;
	}
	
	public boolean isPlayerSelectable() { return playerSelectable; }
	public String getID() { return id; }
	public Color getIconColor() { return iconColor; }
	public String getIcon() { return icon; }
	public String getDescription() { return description; }
	public String getName() { return id; }
	public Type getType() { return type; }
	public Faction getFaction() { return Game.ruleset.getFaction(faction); }

	public boolean isCreature() { return type == Entity.Type.CREATURE; }
	
	public Object get(String ref) { return scriptState.get(ref); }
	
	public void put(String ref, Object data) {
		scriptState.put(ref, data);
	}
	
	public String getFullName() {
		return id;
	}
	
	@Override public int compareTo(Entity other) {
		int thisPriority = 0;
		int otherPriority = 0;
		
		switch (this.getType()) {
		case DOOR: thisPriority = 1; break;
		case TRAP: thisPriority = 2; break;
		case CONTAINER: thisPriority = 3; break;
		case CREATURE: thisPriority = 4; break;
		}
		
		switch (other.getType()) {
		case DOOR: otherPriority = 1; break;
		case TRAP: otherPriority = 2; break;
		case CONTAINER: otherPriority = 3; break;
		case CREATURE: otherPriority = 4; break;
		}
		
		if (thisPriority > otherPriority) return 1;
		if (thisPriority < otherPriority) return -1;
		else return this.hashCode() - other.hashCode();
	}
}
