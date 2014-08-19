package net.sf.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
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
		
		
		JPanel tilesPanel = new JPanel(new GridLayout(0, 2));
		
		for (String layerID : tileset.getLayerIDs()) {
			Layer layer = tileset.getLayer(layerID);
			
			for (String tileID : layer.getTiles()) {
				tilesPanel.add(new TileButton(tileID));
			}
		}
		
		JScrollPane tilesPane = new JScrollPane(tilesPanel);
		tilesPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(tilesPane, c);
	}
	
	private class TileButton extends JButton {
		private String tileID;
		
		private TileButton(String tileID) {
			this.tileID = tileID;
		}
	}
}
