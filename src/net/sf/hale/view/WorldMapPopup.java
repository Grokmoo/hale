package net.sf.hale.view;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.AreaTransition;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.WorldMapLocation;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Date;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.widgets.WorldMapViewer;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A popup window showing a graphical map of the game world (using the Campaign
 * worldMapImage) and icons with buttons enabling the user to travel to known
 * locations.
 * 
 * @author Jared Stephen
 *
 */

public class WorldMapPopup extends PopupWindow {
	private int labelOverlap;
	private int buttonOverlap;
	
	private final Content content;
	
	private final WorldMapViewer viewer;
	private final Button close;
	
	private final List<Location> locations;
	
	private final AreaTransition transition;
	
	private boolean showAllLocations;
	
	/**
	 * Creates a new WorldMapPopup with the specified parent Widget.  All input
	 * in the parent Widget and all children of that Widget is blocked while the
	 * popup is open.
	 * 
	 * @param parent the parent Widget used to block input
	 */
	
	public WorldMapPopup(Widget parent, AreaTransition transition) {
		super(parent);
		
		this.setCloseOnEscape(false);
		this.setCloseOnClickedOutside(false);
		
		content = new Content();
		this.add(content);
		
		viewer = new WorldMapViewer();
		content.add(viewer);
		
		close = new Button();
		close.setTheme("closebutton");
		close.addCallback(new Runnable() {
			@Override public void run() {
				closePopup();
			}
		});
		content.add(close);
		
		this.transition = transition;
		
		locations = new ArrayList<Location>();
		createLocationsList();
	}
	
	/**
	 * Sets whether this popup will show all locations, or only revealed locations
	 * @param showAll whether all locations will be shown
	 */
	
	public void setShowAllLocations(boolean showAll) {
		this.showAllLocations = showAll;
		
		createLocationsList();
	}
	
	private void createLocationsList() {
		for (Location location : locations) {
			content.removeChild(location.button);
			content.removeChild(location.label);
		}
		locations.clear();
		
		// get the list of revealed locations
		List<WorldMapLocation> mapLocations = new ArrayList<WorldMapLocation>();
		for (WorldMapLocation mapLocation : Game.curCampaign.worldMapLocations) {
			if (showAllLocations || mapLocation.isRevealed())
				mapLocations.add(mapLocation);
		}
		
		viewer.updateLocations(mapLocations);
		
		String currentID = null;
		
		if (transition != null)
			currentID = transition.getWorldMapLocation();
		
		WorldMapLocation current = null;
		if (currentID != null) {
			current = Game.curCampaign.getWorldMapLocation(currentID);
		}
		
		// now create the label and button widgets
		for (WorldMapLocation mapLocation : mapLocations) {
			Label label = new Label(mapLocation.getName());
			TravelButton button;
			
			if (mapLocation != current) {
				button = new TravelButton(current, mapLocation);
				
				label.setTheme("locationlabel");
			} else {
				button = null;
				
				label.setTheme("currentlocationlabel");
			}
			
			content.add(label);
			if (button != null) content.add(button);
			
			locations.add(new Location(mapLocation, label, button));
		}
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		labelOverlap = themeInfo.getParameter("labelOverlap", 0);
		buttonOverlap = themeInfo.getParameter("buttonOverlap", 0);
	}
	
	private class Location {
		private Location(WorldMapLocation location, Label label, TravelButton button) {
			this.location = location;
			this.label = label;
			this.button = button;
		}
		
		private WorldMapLocation location;
		private Label label;
		private TravelButton button;
	}
	
	private class Content extends Widget {
		@Override protected void layout() {
			super.layout();
			
			close.setSize(close.getPreferredWidth(), close.getPreferredHeight());
			close.setPosition(getInnerRight() - close.getWidth(), getInnerY());
			
			for (Location location : locations) {
				Point p = location.location.getPosition();
				Sprite sprite = SpriteManager.getSprite(location.location.getIcon());
				
				int baseX = getInnerX() + p.x + sprite.getWidth() / 2;
				int baseY = getInnerX() + p.y;
				
				if (location.button != null) {
					Button button = location.button;
					button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
					button.setPosition(baseX - button.getWidth() / 2, baseY + sprite.getHeight() - buttonOverlap);
				}
				
				Label label = location.label;
				label.setPosition(baseX - label.getPreferredWidth() / 2,
						baseY - label.getPreferredHeight() / 2 + labelOverlap);
			}
		}
		
		@Override public int getPreferredWidth() {
			return viewer.getPreferredWidth() + getBorderHorizontal();
		}
		
		@Override public int getPreferredHeight() {
			return viewer.getPreferredHeight() + getBorderVertical();
		}
	}
	
	private class TravelButton extends Button implements Runnable {
		private final WorldMapLocation origin;
		private final WorldMapLocation destination;
		
		public TravelButton(WorldMapLocation origin, WorldMapLocation destination) {
			// if origin is null, this is a mock popup for the editor
			if (origin == null) {
				setText("9 Days 23 Hours");
			} else {
				setText(Game.curCampaign.getDate().getDateString(0, 0, origin.getTravelTime(destination), 0, 0));
			}
			
			this.origin = origin;
			this.destination = destination;
			
			this.setTheme("travelbutton");
			this.addCallback(this);
		}
		
		@Override public void run() {
			// if origin is null, this is a mock popup for the editor
			if (origin == null) return;
			
			String transitionID = destination.getAreaTransition();
			if (transitionID == null) {
				Logger.appendToErrorLog("World Map Location " + destination.getID() + " has no area transition defined.");
			}
			
			AreaTransition transition = Game.curCampaign.getAreaTransition(transitionID);
			
			WorldMapPopup.this.closePopup();
			
			int travelTime = origin.getTravelTime(destination);
			Date date = Game.curCampaign.getDate();
			int travelTimeRounds = date.ROUNDS_PER_HOUR * travelTime;
			
			// elapse the rounds for all entities being tracked
			Game.curCampaign.curArea.getEntities().elapseRoundsForDeadCreatures(travelTimeRounds);
			for (Entity e : Game.curCampaign.curArea.getEntities()) {
				if (e.getType() == Entity.Type.CREATURE) {
					Creature c = (Creature)e;
					c.elapseRounds(travelTimeRounds);
				}
			}
			
			date.incrementHours(travelTime);
			Game.curCampaign.transition(transition, "World Map");
			Game.timer.resetTime();
		}
	}
}
