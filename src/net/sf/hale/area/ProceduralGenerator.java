package net.sf.hale.area;

import net.sf.hale.Game;
import net.sf.hale.rules.Dice;
import net.sf.hale.tileset.Border;
import net.sf.hale.tileset.BorderTile;
import net.sf.hale.tileset.ElevationList;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.util.PointImmutable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * For handling of procedural generation of terrain within an area
 * @author Jared
 *
 */

public class ProceduralGenerator {
	private final Tileset tileset;
	private final Area area;
	private final String baseTerrain;
	
	private long seed;
	private Dice random;
	
	/**
	 * Creates a new ProceduralGeneration object for the specified area
	 * @param area
	 * @param data
	 */
	
	public ProceduralGenerator(Area area, SimpleJSONObject data) {
		this.area = area;
		this.tileset = Game.curCampaign.getTileset(area.getTileset());
		
		this.baseTerrain = data.get("baseTerrain", null);
		
		this.seed = Game.dice.randSeed();
	}
	
	/**
	 * Returns the value of the seed used by this generator
	 * @return the seed value
	 */
	
	public long getSeed() {
		return this.seed;
	}
	
	/**
	 * Sets the random seed being used by this generator
	 * @param seed
	 */
	
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	/**
	 * Generates layers and tiles for the parent area based on the attributes of this generator
	 */
	
	public void generateLayers() {
		this.random = new Dice(this.seed);
		
		TerrainType baseTerrain = tileset.getTerrainType(this.baseTerrain);
		
		for (int x = 0; x < area.getWidth(); x++) {
			for (int y = 0; y < area.getHeight(); y++) {
				TerrainTile tile = baseTerrain.getRandomTerrainTile(random);
				
				area.getTileGrid().addTile(tile.getID(), tile.getLayerID(), x, y);
				
				area.getTransparency()[x][y] = true;
				area.getPassability()[x][y] = true;
			}
		}
		
		// generate a simple maze
		int[][] elev = elevationMazeDepthFirst();
		
		// set elevation grid
		for (int x = 0; x < area.getWidth(); x++) {
			for (int y = 0; y < area.getHeight(); y++) {
				area.getElevationGrid().setElevation(x, y, (byte)elev[x][y]);
			}
		}
		
		// add elevation border tiles
		for (int x = 0; x < area.getWidth(); x++) {
			for (int y = 0; y < area.getHeight(); y++) {
				Point p = new Point(x, y);
				
				for (ElevationList.Elevation elevation : tileset.getElevationList().
						getMatchingElevationRules(area.getElevationGrid(), p)) {
					
					Border border = elevation.getBorder();
					
					for (BorderTile borderTile : border) {
						Point borderPoint = borderTile.getPosition().getRelativePoint(p);
						
						PointImmutable bP = new PointImmutable(borderPoint);
						if (!bP.isWithinBounds(area)) continue;
						
						area.getTileGrid().addTile(borderTile.getID(), borderTile.getLayerID(), bP.x, bP.y);
					}
				}
			}
		}
	}
	
	private int[][] elevationMazeDepthFirst() {
		// array representing the elevation at each point
		int[][] maze = new int[area.getWidth()][area.getHeight()];
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze[0].length; j++) {
				maze[i][j] = -1; // not visited
			}
		}
		
		int startX = 2;
		int startY = 2;
		// make sure transitions have ample space around them
		for (String transitionID : area.getTransitions()) {
			Transition transition = Game.curCampaign.getAreaTransition(transitionID);
			
			Transition.EndPoint endPoint = transition.getEndPointInArea(area);
			
			for (PointImmutable p : area.getPoints(endPoint.getX(), endPoint.getY(), 3)) {
				maze[p.x][p.y] = 0;
			}
			
			startX = endPoint.getX() + 3;
			startY = endPoint.getY() + 3;
		}
		
		traverse(maze, startX, startY);
		
		return maze;
	}
	
	private void traverse(int[][] maze, int curX, int curY) {
		maze[curX][curY] = 0;
		
		Point[] neighbors = AreaUtil.getAdjacentTiles(curX, curY);
		
		// shuffle array
		for (int i = neighbors.length - 1; i > 0; i--) {
			int index = random.rand(0, i);
			
			Point p = neighbors[index];
			neighbors[index] = neighbors[i];
			neighbors[i] = p;
		}
		
		boolean first = true;
		
		// go through list of neighbors
		for (Point neighbor : AreaUtil.getAdjacentTiles(curX, curY)) {
			if (neighbor.x < 0 || neighbor.y < 0 || neighbor.x >= area.getWidth() || neighbor.y >= area.getHeight())
				continue;
				
			if (maze[neighbor.x][neighbor.y] == -1) {
				if (first) {
					traverse(maze, neighbor.x, neighbor.y);
					first = false;
				} else {
					maze[neighbor.x][neighbor.y] = 1;
				}
			}
		}
	}
}
