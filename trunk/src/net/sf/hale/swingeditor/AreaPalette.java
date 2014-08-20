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

/**
 * Class for selecting different types of objects such as terrain which
 * can then be painted onto the current area
 * @author Jared
 *
 */

public class AreaPalette extends JPanel {
	private Area area;
	private Tileset tileset;
	
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
	
	public void setArea(Area area) {
		this.area = area;
		
		this.removeAll();
		
		if (area != null) {
			this.tileset = Game.curCampaign.getTileset(area.getTileset());
			
			addWidgets();
		}
	}
	
	private void addWidgets() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.insets = new Insets(2, 5, 2, 5);
		
		JLabel title = new JLabel("Tileset: " + area.getTileset());
		add(title, c);
		
		c.gridy++;
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
		TileAction action = new TileAction( tileID, getIconFromImage(tileID, layerID) );
		
		return new JButton(action);
	}
	
	private JButton createTerrainButton(TerrainType terrainType) {
		TerrainTile previewTile = terrainType.getPreviewTile();
		String tileID = previewTile.getID();
		String layerID = previewTile.getLayerID();
		
		return new JButton( new TerrainAction(terrainType, tileID, getIconFromImage(tileID, layerID)) );
	}
	
	private JButton createFeatureButton(FeatureType featureType) {
		TerrainTile previewTile = featureType.getPreviewTile();
		String tileID = previewTile.getID();
		String layerID = previewTile.getLayerID();
		
		return new JButton( new FeatureAction(featureType, tileID, getIconFromImage(tileID, layerID)) );
	}
	
	private class TileAction extends AbstractAction {
		private final String tileID;
		
		private TileAction(String tileID, Icon icon) {
			super(null, icon);
			
			this.tileID = tileID;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			
		}
	}
	
	private class TerrainAction extends TileAction {
		private final TerrainType terrainType;
		
		private TerrainAction(TerrainType terrainType, String tileID, Icon icon) {
			super(tileID, icon);
			
			this.terrainType = terrainType;
		}
		
		@Override public void actionPerformed(ActionEvent evt) {
			
		}
	}
	
	private class FeatureAction extends TileAction {
		private final FeatureType featureType;
		
		private FeatureAction(FeatureType featureType, String tileID, Icon icon) {
			super(tileID, icon);
			
			this.featureType = featureType;
		}
		
		@Override public void actionPerformed(ActionEvent evt) {
			
		}
	}
}
