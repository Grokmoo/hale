package net.sf.hale.area;

import net.sf.hale.Game;
import net.sf.hale.rules.Dice;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tileset;
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
		
		//area.getTileGrid().cacheSprites();
	}
}
