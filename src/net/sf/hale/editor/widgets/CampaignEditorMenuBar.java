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

package net.sf.hale.editor.widgets;

import java.util.Calendar;

import net.sf.hale.Game;
import net.sf.hale.editor.AreaEditor;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Menu;
import de.matthiasmann.twl.MenuAction;
import de.matthiasmann.twl.Widget;

public class CampaignEditorMenuBar extends Widget {
	private final Widget menuBar;
	
	private final Menu editorsMenu;
	private final MenuAreaSelector menuAreaSelector;
	private final MenuAreaModeSelector modeMenu;
	
	private final Label statusText;
	
	public CampaignEditorMenuBar() {
		super();
		this.setTheme("menubar");
		
		Menu campaignMenu = new Menu("Campaign");
		
		Menu newCampaignMenu = new Menu("New");
		newCampaignMenu.add(new MenuNewCampaign());
		campaignMenu.add(newCampaignMenu);
		
		Menu openCampaignMenu = new Menu("Open");
		openCampaignMenu.add(new MenuCampaignSelector());
		campaignMenu.add(openCampaignMenu);
		
		Menu extractCampaignMenu = new Menu("Extract Zip");
		extractCampaignMenu.add(new MenuCampaignExtractor());
		campaignMenu.add(extractCampaignMenu);
		
		Menu saveCampaignMenu = new Menu("Save to Zip");
		saveCampaignMenu.add(new MenuCampaignSaver());
		campaignMenu.add(saveCampaignMenu);
		
		campaignMenu.add(new MenuAction("Exit", new Runnable() {
			@Override public void run() {
				CloseEditorPopup popup = new CloseEditorPopup(Game.campaignEditor);
				popup.openPopupCentered();
			}
		}));
		
		editorsMenu = new Menu("Editors");
		editorsMenu.add(new MenuAction("Properties", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.propertiesEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Scripts", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.scriptEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Items", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.itemEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Item Lists", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.itemListEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Creatures", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.creatureEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Encounters", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.encounterEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Merchants", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.merchantEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Triggers", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.areaTriggerEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Transitions", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.transitionEditor.setVisible(true);
			}
		}));
		
		editorsMenu.add(new MenuAction("Recipes", new Runnable() {
			@Override public void run() {
				Game.campaignEditor.recipeEditor.setVisible(true);
			}
		}));
		
		menuAreaSelector = new MenuAreaSelector();
		
		Menu areaMenu = new Menu("Area");
		areaMenu.add(menuAreaSelector);
		
		modeMenu = new MenuAreaModeSelector("Mode");
		
		Menu main = new Menu();
		main.add(campaignMenu);
		main.add(editorsMenu);
		main.add(areaMenu);
		main.add(modeMenu);
		
		menuBar = main.createMenuBar();
		
		this.add(menuBar);
		
		statusText = new Label();
		statusText.setTheme("/labelblack");
		this.add(statusText);
	}
	
	public void setMenuHiddenState() {
		if (Game.curCampaign == null) {
			menuBar.getChild(1).setVisible(false);
			menuBar.getChild(2).setVisible(false);
			menuBar.getChild(3).setVisible(false);
		} else {
			menuBar.getChild(1).setVisible(true);
			menuBar.getChild(2).setVisible(true);
			
			menuBar.getChild(3).setVisible(Game.campaignEditor.areaEditor != null);
		}
	}
	
	public AreaEditor.Mode getMode() {
		return modeMenu.getSelectedMode();
	}
	
	public String getSelectedArea() {
		return menuAreaSelector.getSelectedArea();
	}
	
	public void updateStatusText(String text) {
		statusText.setText(Calendar.getInstance().getTime().toString() + " : " + text);
		
		int positionY = this.getY() + (this.getHeight()) / 2;
		int positionX = this.getX() + (this.getWidth() - statusText.computeTextWidth() - 10);
		
		statusText.setPosition(positionX, positionY);
	}
	
	@Override public boolean setSize(int width, int height) {
		boolean returnValue = super.setSize(width, height);
		
		menuBar.setSize(width, height);
		
		return returnValue;
	}
}
