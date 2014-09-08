package net.sf.hale.swingeditor;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.tileset.Border;
import net.sf.hale.tileset.BorderList;
import net.sf.hale.tileset.BorderTile;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tile;
import net.sf.hale.tileset.TileLayerList;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.util.PointImmutable;

/**
 * for editing the tiles that make up an area. terrain type is defined for each tile and then border
 * tiles can be added based on that
 * @author jared
 *
 */

public class TerrainGrid {
	private final Tileset tileset;
	private final Area area;
	
	private final int width, height;
	private final TerrainType[][] terrain;
	
	private final TerrainTile[][] terrainTiles; // the actual tiles present on each terrain location
	
	/**
	 * Creates a new terrain grid for the given area.  initializes the terrain type for each
	 * part of the grid
	 * @param area
	 */
	
	public TerrainGrid(Area area) {
		this.tileset = Game.curCampaign.getTileset(area.getTileset());
		this.area = area;
		this.width = area.getWidth();
		this.height = area.getHeight();
		
		terrain = new TerrainType[width][height];
		terrainTiles = new TerrainTile[width][height];
		
		List<Tile> tiles = new ArrayList<Tile>();
		
		// at each grid point, figure out what the terrain type is
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles.clear();
				
				for (String layerID : area.getTileGrid().getLayerIDs()) {
					tiles.addAll(area.getTileGrid().getLayer(layerID).getTilesAt(x, y));
				}
				
				setTerrainType(tiles, x, y);
			}
		}
	}
	
	/**
	 * Sets the terrain type and terrain tile at the specified coordinates based on the list of
	 * tiles.  if no matching terrain is found, nothing is set
	 * @param tiles
	 * @param x
	 * @param y
	 */
	
	private void setTerrainType(List<Tile> tiles, int x, int y) {
		for (String terrainTypeID : tileset.getTerrainTypeIDs() ) {
			TerrainType terrainType = tileset.getTerrainType(terrainTypeID);
			
			for (Tile tile : tiles) {
				TerrainTile terrainTile = terrainType.getTerrainTile(tile.getTileID());
				
				if (terrainTile != null) {
					terrain[x][y] = terrainType;
					terrainTiles[x][y] = terrainTile;
					
					return;
				}
			}
		}
	}
	
	private void addBorderTiles(int x, int y) {
		if (terrain[x][y] == null) return;
		
		Point p = new Point(x, y);
		
		for (BorderList borderList : tileset.getMatchingBorderLists(terrain, p) ) {
			for (Border border : borderList.getMatchingBorders(terrain, p) ) {
				for (BorderTile borderTile : border) {
					Point borderPoint = borderTile.getPosition().getRelativePoint(p);
					
					PointImmutable bP = new PointImmutable(borderPoint);
					if (!bP.isWithinBounds(area)) continue;
					
					area.getTileGrid().addTile(borderTile.getID(), borderTile.getLayerID(), bP.x, bP.y);
				}
			}
		}
		
	}
	
	/**
	 * Removes all tiles and clears the terrain at the specified grid points.  for
	 * any grid points outside the area bounds, no action is taken
	 * @param x the grid x coordinate
	 * @param y the grid y coordinate
	 * @param r the grid radius
	 */
	
	public void removeAllTiles(int x, int y, int r) {
		for (PointImmutable p : getPoints(x, y, r)) {
			removeAllTiles(p.x, p.y);
		}
	}
	
	private void removeAllTiles(int x, int y) {
		for (String layerID : area.getTileGrid().getLayerIDs()) {
			TileLayerList list = area.getTileGrid().getLayer(layerID);
			
			list.removeTiles(x, y);
			
			terrain[x][y] = null;
			terrainTiles[x][y] = null;
		}
	}
	
	private void removeButNotTerrain(int x, int y) {
		for (String layerID : area.getTileGrid().getLayerIDs()) {
			TileLayerList list = area.getTileGrid().getLayer(layerID);
			
			list.removeTiles(x, y);
		}
	}
	
	/**
	 * Sets the terrain type at the specified grid points.  sets an appropriate
	 * tile based on the terrain.  for any grid points outside the area bounds,
	 * no action is taken
	 * @param x the grid x coordinate
	 * @param y the grid y coordinate
	 * @param radius the grid radius
	 * @param type
	 */
	
	public void setTerrain(int x, int y, int radius, TerrainType type) {
		removeButNotTerrain(x, y);
		for (int r = 1; r <= radius + 1; r++) {
			for (int i = 0; i < 6 * r; i++) {
				PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));
				
				if (!p.isWithinBounds(area)) continue;
				
				removeButNotTerrain(p.x, p.y);
			}
		}
		
		setTerrain(x, y, type);
		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < 6 * r; i++) {
				PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));
				
				if (!p.isWithinBounds(area)) continue;
				
				setTerrain(p.x, p.y, type);
			}
		}
		
		// re-add nearby tiles
		for (int i = 0; i < 6 * (radius + 1); i++) {
			PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, radius + 1, i));
			
			if (!p.isWithinBounds(area)) continue;
			
			setTerrain(p.x, p.y, terrain[p.x][p.y]);
		}
		
		// re-add nearby border tiles
		for (int i = 0; i < 6 * (radius + 2); i++) {
			PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, radius + 2, i));
			
			if (!p.isWithinBounds(area)) continue;
			
			addBorderTiles(p.x, p.y);
		}
		
		area.getTileGrid().cacheSprites();
	}
	
	private void setTerrain(int x, int y, TerrainType type) {
		if (type == null) return;
		
		TerrainTile tile = type.getRandomTerrainTile();
		terrain[x][y] = type;
		terrainTiles[x][y] = tile;
		area.getTileGrid().addTile(tile.getID(), tile.getLayerID(), x, y);
		
		addBorderTiles(x, y);
	}
	
	/**
	 * @return a list of all points (in grid coordinates) based on the x, y, r
	 */
	
	private List<PointImmutable> getPoints(int x, int y, int radius) {
		List<PointImmutable> points = new ArrayList<PointImmutable>();
		
		PointImmutable pCenter = new PointImmutable(x, y);
		
		if (pCenter.isWithinBounds(area)) {
			points.add(pCenter);
		}

		for (int r = 1; r <= radius; r++) {
			for (int i = 0; i < 6 * r; i++) {
				PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));
				
				if (!p.isWithinBounds(area)) continue;
				
				points.add(p);
			}
		}

		return points;
	}
}
