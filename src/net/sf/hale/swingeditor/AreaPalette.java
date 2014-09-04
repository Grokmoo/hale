package net.sf.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.tileset.FeatureType;
import net.sf.hale.tileset.Layer;
import net.sf.hale.tileset.TerrainTile;
import net.sf.hale.tileset.TerrainType;
import net.sf.hale.tileset.Tileset;
import net.sf.hale.util.PointImmutable;

/**
 * Class for selecting different types of objects such as terrain which
 * can then be painted onto the current area
 * @author Jared
 *
 */

public class AreaPalette extends JPanel {
	private AreaRenderer renderer;
	private Area area;
	private Tileset tileset;
	
	private TerrainGrid grid;
	
	/**
	 * Creates a new palette.  It is empty until an area is set
	 */
	
	public AreaPalette() {
		super(new GridBagLayout());
	}
	
	/**
	 * Sets the area this palette is interacting with.  If non-null,
	 * adds widgets for the area's tileset.  If null, all children
	 * are removed from this palette
	 * @param area
	 */
	
	public void setArea(AreaRenderer areaRenderer) {
		this.renderer = areaRenderer;
		this.area = areaRenderer.getArea();
		
		this.removeAll();
		
		if (area != null) {
			this.tileset = Game.curCampaign.getTileset(area.getTileset());
			
			addWidgets();
		}
		
		renderer.setClickHandler(new DefaultClickHandler());

		grid = new TerrainGrid(area);
	}
	
	/**
	 * Returns the area associated with this palette
	 * @return the area
	 */
	
	public Area getArea() {
		return area;
	}
	
	private void addWidgets() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 5, 2, 5);
		c.ipadx = 100;
		
		JLabel title = new JLabel("Tileset: " + area.getTileset());
		add(title, c);
		
		c.gridx++;
		c.ipadx = 0;
		add(new JLabel("Radius"), c);
		
		c.gridx++;
		JSpinner radius = new JSpinner(renderer.getMouseRadiusModel());
		add(radius, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		JTabbedPane contentPane = new JTabbedPane();
		add(contentPane, c);
		
		// add terrain tab
		List<JButton> tileButtons = new ArrayList<JButton>();
		
		for (String terrainTypeID : tileset.getTerrainTypeIDs()) {
			tileButtons.add(createTerrainButton(tileset.getTerrainType(terrainTypeID)));
		}
		
		contentPane.addTab("Terrain", getTabPanel(tileButtons));
		
		// add features tab
		tileButtons.clear();
		
		for (String featureTypeID : tileset.getFeatureTypeIDs()) {
			tileButtons.add(createFeatureButton(tileset.getFeatureType(featureTypeID)));
		}
		
		contentPane.addTab("Features", getTabPanel(tileButtons));
		
		// add tiles tab
		tileButtons.clear();
		
		for (String layerID : tileset.getLayerIDs()) {
			Layer layer = tileset.getLayer(layerID);
			
			for (String tileID : layer.getTiles()) {
				tileButtons.add(createTileButton(tileID, layerID));
			}
		}
		
		contentPane.addTab("Tiles", getTabPanel(tileButtons));
	}
	
	private JScrollPane getTabPanel(List<JButton> tileButtons) {
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		
		int row = 0;
		int col = 0;
		
		for (JButton tileButton : tileButtons) {
			c.gridx = row;
			c.gridy = col;
			
			panel.add(tileButton, c);
			
			row++;
			if (row == 2) {
				row = 0;
				col++;
			}
		}
		
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(64);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		return scrollPane;
	}
	
	private Icon getIconFromImage(String tileID, String layerID) {
		String spriteID = tileset.getLayer(layerID).getSpriteID(tileID);
		
		String spriteSheetID = ResourceManager.getResourceDirectory(spriteID) + ResourceType.PNG.getExtension();
		
		BufferedImage sourceImage = SpriteManager.getSourceImage(spriteSheetID);
		
		Sprite tileSprite = SpriteManager.getImage(spriteID);
		
		int x = (int) (tileSprite.getTexCoordStartX() * sourceImage.getWidth());
		int y = (int) (tileSprite.getTexCoordStartY() * sourceImage.getHeight());
		int x2 = (int) (tileSprite.getTexCoordEndX() * sourceImage.getWidth());
		int y2 = (int) (tileSprite.getTexCoordEndY() * sourceImage.getHeight());
		
		return new ImageIcon(sourceImage.getSubimage(x, y, x2 - x, y2 - y));
	}
	
	private JButton createTileButton(String tileID, String layerID) {
		return new JButton( new TileAction(tileID, layerID, getIconFromImage(tileID, layerID)) );
	}
	
	private JButton createTerrainButton(TerrainType terrainType) {
		TerrainTile previewTile = terrainType.getPreviewTile();
		String tileID = previewTile.getID();
		String layerID = previewTile.getLayerID();
		
		return new JButton( new TerrainAction(terrainType, tileID, layerID, getIconFromImage(tileID, layerID)) );
	}
	
	private JButton createFeatureButton(FeatureType featureType) {
		TerrainTile previewTile = featureType.getPreviewTile();
		String tileID = previewTile.getID();
		String layerID = previewTile.getLayerID();
		
		return new JButton( new FeatureAction(featureType, tileID, layerID, getIconFromImage(tileID, layerID)) );
	}
	
	private class DefaultClickHandler implements AreaClickHandler {
		@Override public void leftClicked(List<PointImmutable> points) { /* do nothign */ }

		@Override public void rightClicked(List<PointImmutable> points) {
			grid.removeAllTiles(points);
		}
	}
	
	
	private class TileAction extends AbstractAction implements AreaClickHandler {
		private final String tileID;
		private final String layerID;
		private final String spriteID;
		
		private TileAction(String tileID, String layerID, Icon icon) {
			super(null, icon);
			
			this.tileID = tileID;
			this.layerID = layerID;
			this.spriteID = tileset.getLayer(layerID).getSpriteID(tileID);
		}

		// called when the button is clicked
		@Override public void actionPerformed(ActionEvent evt) {
			renderer.setActionPreviewTile(SpriteManager.getImage(spriteID));
			renderer.setClickHandler(this);
		}

		@Override public void leftClicked(List<PointImmutable> points) {
			
		}

		@Override public void rightClicked(List<PointImmutable> points) {
			grid.removeAllTiles(points);
		}
	}
	
	private class TerrainAction extends TileAction {
		private final TerrainType terrainType;
		
		private TerrainAction(TerrainType terrainType, String tileID, String layerID, Icon icon) {
			super(tileID, layerID, icon);
			
			this.terrainType = terrainType;
		}
		
		@Override public void leftClicked(List<PointImmutable> points) {
			grid.setTerrain(points, terrainType);
		}
	}
	
	private class FeatureAction extends TileAction {
		private final FeatureType featureType;
		
		private FeatureAction(FeatureType featureType, String tileID, String layerID, Icon icon) {
			super(tileID, layerID, icon);
			
			this.featureType = featureType;
		}
		
		@Override public void leftClicked(List<PointImmutable> points) {
			
		}
	}
	
	/**
	 * Used by AreaRenderer to pass information about a given click
	 * back to the appropriate action
	 * @author jared
	 *
	 */
	
	public interface AreaClickHandler {
		/**
		 * Called when the user left clicks on the area
		 * @param points the points in grid coordinates that are selected
		 */
		
		public void leftClicked(List<PointImmutable> points);
		
		/**
		 * Called when the user right clicks on the area
		 * @param points the points in grid coordinates that are selected
		 */
		
		public void rightClicked(List<PointImmutable> points);
	}
}
