package net.sf.hale.view;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import net.sf.hale.Game;
import net.sf.hale.ability.Effect;
import net.sf.hale.bonus.Stat;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.EntityViewer;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.Role;

/**
 * A widget for displaying basic information about a creature in
 * a single window.  This is used by the "Examine" default ability.
 * @author Jared Stephen
 *
 */

public class CreatureDetailsWindow extends GameSubWindow implements EntityViewer {
	private Creature creature;
	private HTMLTextAreaModel textAreaModel;
	
	/**
	 * Create a new CreatureDetailsWindow displaying details for the specified Creature.
	 * @param creature the Creature to show details for
	 */
	
	public CreatureDetailsWindow(Creature creature) {
		this.creature = creature;
		creature.addViewer(this);
		
		this.setSize(280, 300);
		this.setTitle("Details for " + creature.getName());
        
        DialogLayout layout = new DialogLayout();
		layout.setTheme("content");
		this.add(layout);
		
		// set up the widgets for the top row
		Widget viewer = new Viewer();
		viewer.setTheme("iconviewer");
		Label title = new Label(creature.getName());
		title.setTheme("titlelabel");
		
		DialogLayout.Group topRowV = layout.createParallelGroup(viewer, title);
		
		DialogLayout.Group topRowH = layout.createSequentialGroup(viewer);
		topRowH.addGap(10);
		topRowH.addWidget(title);
		topRowH.addGap(10);
		
		// create widgets for details text area
		textAreaModel = new HTMLTextAreaModel();
        TextArea textArea = new TextArea(textAreaModel);
        ScrollPane textPane = new ScrollPane(textArea);
        textPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        textPane.setTheme("detailspane");
        
        // set the main top level layout
        Group mainGroupV = layout.createSequentialGroup();
		mainGroupV.addGroup(topRowV);
		mainGroupV.addGap(5);
		mainGroupV.addWidget(textPane);
		
		Group mainGroupH = layout.createParallelGroup();
		mainGroupH.addGroup(topRowH);
		mainGroupH.addWidget(textPane);
		
		layout.setHorizontalGroup(mainGroupH);
		layout.setVerticalGroup(mainGroupV);
		
		entityUpdated();
	}
	
	@Override public void closeViewer() {
		getParent().removeChild(this);
	}
	
	@Override public void entityUpdated() {
		textAreaModel.setHtml(getTextAreaContent(creature));
		
		invalidateLayout();
	}
	
	/*
	 * This overrides the default close behavior of GameSubWindow
	 * @see net.sf.hale.view.GameSubWindow#run()
	 */
	
	@Override public void run() {
		closeViewer();
		creature.removeViewer(this);
	}
	
	private static String getTextAreaContent(Creature creature) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<div style=\"font-family: vera;\">");
		sb.append(creature.getGender()).append(' ');
		sb.append("<span style=\"font-family: vera-blue;\">").append(creature.getRace().getName()).append("</span>");
		sb.append("</div>");
		
		sb.append("<div style=\"font-family: vera; margin-bottom: 1em\">");
		for (String roleID : creature.getRoles().getRoleIDs()) {
			Role role = Game.ruleset.getRole(roleID);
			int level = creature.getRoles().getLevel(role);
			
			sb.append("<p>");
			sb.append("Level <span style=\"font-family: vera-italic;\">").append(level).append("</span> ");
			sb.append("<span style=\"font-family: vera-red;\">").append(role.getName()).append("</span>");
			sb.append("</p>");
		}
		sb.append("</div>");
		
		sb.append("<div style=\"font-family: vera; margin-bottom: 1em\">");
		sb.append("Hit Points ");
		sb.append("<span style=\"font-family: vera-italic-green\">");
		sb.append(creature.getCurrentHP()).append("</span> / <span style=\"font-family: vera-italic-green\">");
		sb.append(creature.stats().get(Stat.MaxHP)).append("</span>");
		sb.append("</div>");
		
		Item mainHand = creature.getInventory().getMainWeapon();
		Item offHand = creature.getInventory().getEquippedOffHand();
		
		sb.append("<div style=\"margin-bottom: 1em; font-family: vera;\"><p>Main hand</p>"); 
		sb.append("<div style=\"font-family: vera-italic-blue\">");
		sb.append(mainHand.getName()).append("</div></div>");
		
		if (offHand != null) {
			sb.append("<div style=\"margin-bottom: 1em; font-family: vera;\"><p>Off hand</p>");
			sb.append("<div style=\"font-family: vera-italic-blue\">");
			sb.append(offHand.getName()).append("</div></div>");

		}
		
		for (Effect effect : creature.getEffects()) {
			effect.appendDescription(sb);
		}
		
		return sb.toString();
	}
	
	private class Viewer extends Widget {
		@Override public int getMinHeight() { return Game.TILE_SIZE + getBorderHorizontal(); }
		
		@Override public int getMinWidth() { return Game.TILE_SIZE + getBorderVertical(); }
		
		@Override protected void paintWidget(GUI gui) {
			super.paintWidget(gui);
			
			creature.draw(getInnerX(), getInnerY());
		}
	}
}
