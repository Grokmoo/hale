package net.sf.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.tileset.Layer;
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
		
		
		JPanel tilesPanel = new JPanel(new GridBagLayout());
		
		int col = 0;
		int row = 0;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(2, 2, 2, 2);
		
		for (String layerID : tileset.getLayerIDs()) {
			Layer layer = tileset.getLayer(layerID);
			
			for (String tileID : layer.getTiles()) {
				c2.gridx = col;
				c2.gridy = row;
				
				tilesPanel.add(new TileButton(tileID, layerID), c2);
				
				col++;
				if (col == 2) {
					col = 0;
					row++;
				}
			}
		}
		
		JScrollPane tilesPane = new JScrollPane(tilesPanel);
		tilesPane.getVerticalScrollBar().setUnitIncrement(64);
		tilesPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(tilesPane, c);
	}
	
	private class TileButton extends JButton {
		private String tileID;
		
		private TileButton(String tileID, String layerID) {
			this.tileID = tileID;
			
			String spriteID = tileset.getLayer(layerID).getSpriteID(tileID);
			
			String spriteSheetID = ResourceManager.getResourceDirectory(spriteID) + ResourceType.PNG.getExtension();
			
			BufferedImage sourceImage = SpriteManager.getSourceImage(spriteSheetID);
			
			Sprite tileSprite = SpriteManager.getImage(spriteID);
			
			int x = (int) (tileSprite.getTexCoordStartX() * sourceImage.getWidth());
			int y = (int) (tileSprite.getTexCoordStartY() * sourceImage.getHeight());
			int x2 = (int) (tileSprite.getTexCoordEndX() * sourceImage.getWidth());
			int y2 = (int) (tileSprite.getTexCoordEndY() * sourceImage.getHeight());
			
			BufferedImage subImage = sourceImage.getSubimage(x, y, x2 - x, y2 - y);
			
			this.setIcon(new ImageIcon(subImage));
		}
	}
}
