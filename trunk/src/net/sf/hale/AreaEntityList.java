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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.matthiasmann.twl.Color;

import net.sf.hale.entity.Container;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Trap;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.loading.Saveable;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

public class AreaEntityList implements Saveable, Iterable<Entity> {
	private EntityList[][] entities;
	private Set<Entity> entitiesSet;
	
	private final List<Creature> deadCreaturesWithActiveEffects;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		Object[] deadCreaturesData = new Object[this.deadCreaturesWithActiveEffects.size()];
		int i = 0;
		for (Creature creature : this.deadCreaturesWithActiveEffects) {
			deadCreaturesData[i] = creature.save();
			i++;
		}
		
		if (deadCreaturesData.length != 0)
			data.put("deadCreaturesWithActiveEffects", deadCreaturesData);
		
		JSONOrderedObject[] entitiesData = new JSONOrderedObject[entitiesSet.size()];
		i = 0;
		for (Entity entity : entitiesSet) {
			entitiesData[i] = entity.save();
			i++;
		}
		data.put("entities", entitiesData);
		
		return data;
	}
	
	private Entity loadEntity(SimpleJSONObject entryData) {
		boolean tempContainer = false;
		if (entryData.containsKey("temporaryContainer")) {
			tempContainer = entryData.get("temporaryContainer", false);
		}
		
		String id = entryData.get("id", null);
		Entity.Type type = Entity.Type.valueOf( entryData.get("type", null));
		
		if (tempContainer) {
			return Container.createTemporaryContainer(id);
		}
		
		switch (type) {
		case CREATURE:
			if (entryData.get("isPCPartyMember", false)) {
				return createPCCreature(entryData);
			} else {
				// getCharacter will search for both characters and creatures
				return Game.entityManager.getCharacter(id);
			}	
		case ITEM: case CONTAINER: case DOOR: case TRAP:
			return Game.entityManager.getItem(id);
		}
		
		Logger.appendToErrorLog("Unable to load entity " + id);
		
		return null;
	}
	
	private Creature createPCCreature(SimpleJSONObject data) {
		String id = data.get("id", null);
		String name = data.get("name", null);
		Ruleset.Gender gender = Ruleset.Gender.valueOf(data.get("gender", null));
		Race race = Game.ruleset.getRace(data.get("race", null));
		String portrait = data.get("portrait", null);
		
		String icon = data.get("icon", null);
		
		Creature creature = new Creature(id, portrait, icon, name, gender, race,
				Game.ruleset.getString("PlayerFaction"), true, new Point(false), null);
		
		String iconColorString = data.get("iconColor", null);
		if (iconColorString != null) {
			Color iconColor = Color.parserColor(iconColorString);
			creature.setIconColor(iconColor);
		}
		
		int[] attributes = new int[6];
		attributes[0] = data.get("strength", 0);
		attributes[1] = data.get("dexterity", 0);
		attributes[2] = data.get("constitution", 0);
		attributes[3] = data.get("intelligence", 0);
		attributes[4] = data.get("wisdom", 0);
		attributes[5] = data.get("charisma", 0);
		
		creature.stats().setAttributes(attributes);
		
		creature.setDrawOnlyHandSubIcons(data.get("drawOnlyHandSubIcons", false));
		
		if (data.containsKey("skinColor")) {
			creature.setDrawWithSubIcons(true);
			
			Color skinColor = Color.parserColor(data.get("skinColor", null));
			Color clothingColor = Color.parserColor(data.get("clothingColor", null));
			
			creature.getSubIcons().setSkinColor(skinColor);
			creature.getSubIcons().setClothingColor(clothingColor);
			
			creature.addBaseSubIcons();
			
			if (data.containsKey("hairIcon")) {
				String hairIcon = data.get("hairIcon", null);
				Color hairIconColor = Color.parserColor(data.get("hairColor", null));
				
				creature.setHairSubIcon(hairIcon, hairIconColor);
			}
			
			if (data.containsKey("beardIcon")) {
				String beardIcon = data.get("beardIcon", null);
				Color beardIconColor = Color.parserColor(data.get("beardColor", null));
				
				creature.setBeardSubIcon(beardIcon, beardIconColor);
			}
		}
		
		return creature;
	}
	
	public void load(SimpleJSONObject data, ReferenceHandler refHandler) throws LoadGameException {
		if (data.containsKey("deadCreaturesWithActiveEffects")) {
			for (SimpleJSONArrayEntry entry : data.getArray("deadCreaturesWithActiveEffects")) {
				SimpleJSONObject entryData = entry.getObject();
				
				Creature entryCreature = (Creature)loadEntity(entryData);
				entryCreature.load(entryData, refHandler);
				
				deadCreaturesWithActiveEffects.add(entryCreature);
			}
		}
		
		for (SimpleJSONArrayEntry entry : data.getArray("entities")) {
			SimpleJSONObject entryData = entry.getObject();
			
			Entity entity = loadEntity(entryData);
			entity.load(entryData, refHandler);
			
			addEntity(entity);
		}
	}
	
	public AreaEntityList(int width, int height) {
		entities = new EntityList[width][height];
		entitiesSet = new LinkedHashSet<Entity>();
		
		deadCreaturesWithActiveEffects = new ArrayList<Creature>();
	}
	
	/**
	 * Returns true if this List contains the specified entity within the standard
	 * EntityList.  Does not return true for entities that have been removed and
	 * are still being tracked within the dead creatures list
	 * @param entity the entity to check for
	 * @return true if and only if this List contains the specified entity
	 */
	
	public final boolean containsEntity(Entity entity) {
		return entitiesSet.contains(entity);
	}
	
	public synchronized void addEntity(Entity entity) {
		if (entitiesSet.contains(entity)) return;
		
		addAt(entity.getX(), entity.getY(), entity);
		
		entitiesSet.add(entity);
	}
	
	/**
	 * Elapses the specified number of rounds for dead creatures currently being
	 * tracked by this List.  If any creature no longer has any active effects,
	 * it is removed and no longer tracked.
	 * @param rounds the number of rounds to elapse
	 */
	
	public void elapseRoundsForDeadCreatures(int rounds) {
		Iterator<Creature> iter = this.deadCreaturesWithActiveEffects.iterator();
		while (iter.hasNext()) {
			if (!iter.next().elapseRounds(rounds)) {
				iter.remove();
			}
		}
	}
	
	/**
	 * Adds the specified creature to the list of dead creatures with active effects being
	 * tracked.  This creature will be removed whenever {@link #elapseRoundsForDeadCreatures(int)}
	 * is called and the creature no longer has any active effects
	 * @param creature the creature to track
	 */
	
	public void trackDeadCreature(Creature creature) {
		deadCreaturesWithActiveEffects.add(creature);
	}
	
	private void setMatrix(boolean[][] matrix, boolean value, int x, int y) {
		if (x >= 0 && y >= 0 && x < matrix.length && y < matrix[0].length)
			matrix[x][y] = value;
	}
	
	/**
	 * Gets the matrix of passabilities for each tile in the map based on the
	 * specified creature
	 * @param mover the creature who is moving
	 * @return the matrix of passabilities
	 */
	
	public synchronized boolean[][] getEntityPassabilities(Creature mover) {
		boolean[][] pass = new boolean[entities.length][entities[0].length];
		AreaUtil.setMatrix(pass, true);
		
		for (Entity e : entitiesSet) {
			if (e.getType() == Entity.Type.DOOR) {
				if ( !((Door)e).isOpen() )  {
					setMatrix(pass, false, e.getX(), e.getY());
				}
			} else if (e.getType() == Entity.Type.CREATURE) {
				Creature c = (Creature) e;
				
				// mover can pass through creature unless:
				// creature is not friendly and is not helpless
				
				// if movement is interrupted while two creatures overlap, the mover will be pushed
				// back to their last position which will prevent the two remaining in the same tile.
				
				if (!c.getFaction().isFriendly(mover) && !c.isHelpless())
					setMatrix(pass, false, e.getX(), e.getY());
			}
		}
		
		return pass;
	}
	
	/**
	 * Starts any animations on effects in all contained creatures
	 */
	
	public void startEffectAnimations() {
		for (Entity entity : entitiesSet) {
			entity.getEffects().startAnimations();
		}
	}
	
	/**
	 * Removes the specified Entity from the set of entities within this Area.
	 * @param entity the entity to remove
	 */
	
	public synchronized void removeEntity(Entity entity) {
		if (!entitiesSet.contains(entity)) return;
		
		removeAt(entity.getX(), entity.getY(), entity);
		
		entitiesSet.remove(entity);
	}
	
	private void addAt(int x, int y, Entity entity) {
		if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return;
		
		if (entities[x][y] == null) {
			entities[x][y] = new EntityList();
		}
		
		entities[x][y].add(entity);
	}
	
	private void removeAt(int x, int y, Entity entity) {
		if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return;
		
		if (entities[x][y] == null) return;
		
		entities[x][y].remove(entity);
		
		if (entities[x][y].size() == 0) entities[x][y] = null;
	}
	
	public void moveEntity(Entity entity) {
		if (!entitiesSet.contains(entity)) return;
		
		removeAt(entity.getLastPositionX(), entity.getLastPositionY(), entity);
		
		addAt(entity.getX(), entity.getY(), entity);
	}
	
	public List<Entity> getEntitiesWithID(String id) {
		List<Entity> entities = new ArrayList<Entity>();
		
		for (Entity e : entitiesSet) {
			if (e.getID().equals(id)) entities.add(e);
		}
		
		return entities;
	}
	
	public Entity getEntityWithID(String id) {
		for (Entity e : entitiesSet) {
			if (e.getID().equals(id)) return e;
		}
		
		return null;
	}
	
	private final Entity getEntityOfTypeAtGridPoint(int x, int y, Entity.Type type) {
		if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return null;
		
		if (entities[x][y] == null) return null;
		
		for (Entity entity : entities[x][y]) {
			if (entity.getType() == type) return entity;
		}
		
		return null;
	}
	
	public final Door getDoor(int x, int y) {
		return (Door)getEntityOfTypeAtGridPoint(x, y, Entity.Type.DOOR);
	}
	
	public final Trap getTrap(int x, int y) {
		return (Trap)getEntityOfTypeAtGridPoint(x, y, Entity.Type.TRAP);
	}
	
	public final Container getContainer(int x, int y) {
		return (Container)getEntityOfTypeAtGridPoint(x, y, Entity.Type.CONTAINER);
	}
	
	public final Creature getCreature(int x, int y) {
		return (Creature)getEntityOfTypeAtGridPoint(x, y, Entity.Type.CREATURE);
	}
	
	public final List<Creature> getCreatures(int x, int y) {
		if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return new ArrayList<Creature>(0);
		
		if (entities[x][y] == null) return new ArrayList<Creature>(0);
		
		List<Creature> creatures = new ArrayList<Creature>();
		
		for (Entity entity : entities[x][y]) {
			if (entity.getType() == Entity.Type.CREATURE) creatures.add((Creature)entity);
		}
		
		return creatures;
	}
	
	public final Item getItem(int x, int y) {
		if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return null;
		
		if (entities[x][y] == null) return null;
		
		for (Entity entity : entities[x][y]) {
			switch (entity.getType()) {
			case DOOR: case TRAP: case CONTAINER:
				return (Item)entity;
			}
		}
		
		return null;
	}
	
	public final List<Entity> getEntities(int x, int y) {
		if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return new ArrayList<Entity>(0);
		
		if (entities[x][y] == null) return new ArrayList<Entity>(0);
		
		List<Entity> foundEntities = new ArrayList<Entity>(entities[x][y].size());
		
		for (Entity entity : entities[x][y]) {
			foundEntities.add(entity);
		}
		
		return foundEntities;
	}
	
	/**
	 * Returns the set of entities at the specified grid position.  This set must not
	 * be modified.
	 * @param x the x grid coordinate
	 * @param y the y grid coordinate
	 * @return the set of entities at the specified grid position.
	 */
	
	public final Set<Entity> getEntitiesSet(int x, int y) {
		return entities[x][y];
	}
	
	public List<Trap> getVisibleTraps(boolean[][] visibility) {
		List<Trap> traps = new LinkedList<Trap>();
		
		for (int i = 0; i < entities.length; i++) {
			for (int j = 0; j < entities[0].length; j++) {
				if (visibility[i][j]) {
					Trap t = getTrap(i, j);
					if (t != null) traps.add(t);
				}
			}
		}
		
		return traps;
	}
	
	public List<Creature> getVisibleCreatures(boolean[][] visibility) {
		List<Creature> creatures = new LinkedList<Creature>();
		
		for (int i = 0; i < entities.length; i++) {
			for (int j = 0; j < entities[0].length; j++) {
				if (visibility[i][j]) {
					Creature c = getCreature(i, j);
					if (c != null) creatures.add(c);
				}
			}
		}
		
		return creatures;
	}
	
	public List<Creature> getCreaturesWithinRadius(int x, int y, int radius) {
		List<Creature> creatures = new LinkedList<Creature>();
		
		Creature c = getCreature(x, y);
		if (c != null) creatures.add(c);
		
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < 6 * r; i++) {
				Point p = AreaUtil.convertPolarToGrid(x, y, r, i);
				c = getCreature(p.x, p.y);
				if (c != null) creatures.add(c);
			}
		}
		
		return creatures;
	}
	
	public int getNumberOfCreatures(int x, int y) {
		if (x < 0 || x >= entities.length || y < 0 || y >= entities[0].length) return 0;
		
		if (entities[x][y] == null) return 0;
		
		int count = 0;
		
		for (Entity e : entities[x][y]) {
			if (e.getType() == Entity.Type.CREATURE) count++;
		}
		
		return count;
	}
	
	public void resize(int newWidth, int newHeight) {
		EntityList[][] newEntities = new EntityList[newWidth][newHeight];
		HashSet<Entity> newHash = new HashSet<Entity>();
		
		int width = Math.min(this.entities.length, newWidth);
		int height = Math.min(this.entities[0].length, newHeight);
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				newEntities[i][j] = this.entities[i][j];
				
				if (entities[i][j] != null) newHash.addAll(entities[i][j]);
			}
		}
		
		this.entities = newEntities;
		this.entitiesSet = newHash;
	}
	
	@Override public Iterator<Entity> iterator() {
		return new EntityIterator();
	}
	
	private class EntityIterator implements Iterator<Entity> {
		private Iterator<Entity> hashSetIterator;
		private Entity last;
		
		private EntityIterator() {
			this.hashSetIterator = entitiesSet.iterator();
			last = null;
		}
		
		@Override public boolean hasNext() {
			return hashSetIterator.hasNext();
		}

		@Override public Entity next() {
			last = hashSetIterator.next();
			return last;
		}

		@Override public void remove() {
			hashSetIterator.remove();
			
			if (last != null) {
				int x = last.getX();
				int y = last.getY();
				
				if (x >= 0 && x < entities.length && y > 0 && y < entities[0].length) {
					entities[x][y].remove(last);
				}
				
				last = null;
			}
		}
	}
	
	private class EntityList extends TreeSet<Entity> {
		private static final long serialVersionUID = 7587119408526288199L;

		private EntityList() {
			super();
		}
	}
}
