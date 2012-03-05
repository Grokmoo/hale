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

package net.sf.hale.mainmenu;

import java.io.File;
import java.io.FileWriter;

import net.sf.hale.Campaign;
import net.sf.hale.Game;
import net.sf.hale.Sprite;
import net.sf.hale.loading.CampaignLoadingTaskList;
import net.sf.hale.loading.LoadingTaskList;
import net.sf.hale.loading.LoadingWaitPopup;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The main menu widget.  This is what is displayed when the player first starts the game.
 * 
 * It handles choosing a campaign and selecting a party.  From this menu, the player
 * will either start the Campaign Editor (see {@link net.sf.hale.editor.CampaignEditor}),
 * start the game proper (see {@link net.sf.hale.view.MainViewer}) or exit the game.
 * 
 * @author Jared Stephen
 *
 */

public class MainMenu extends Widget implements LoadGamePopup.Callback {
	private Runnable exitCallback;
	
	private Sprite backgroundSprite;
	private Point backgroundSpriteOffset;
	
	private final GUI gui;
	
	private LoadingTaskList loader;
	private LoadingWaitPopup popup;
	
	private boolean menuRunning = true;
	private boolean exit = false;
	private boolean restart = false;
	private boolean launchEditor = false;
	private boolean exitOnLoad = false;
	
	private String loadGame = null;
	
	private final Label campaignLabel;
	
	private int buttonGap, titleOffset;
	
	private final Button campaignButton;
	private final Button newGameButton;
	private final Button loadGameButton;
	private final Button editorButton;
	private final Button optionsButton;
	
	private final Button exitButton;
	
	private final Label versionLabel;
	
	/**
	 * Create a new MainMenu, with buttons for choosing campaign, loading games,
	 * launching the editor, etc.
	 */
	
	public MainMenu() {
		this.setTheme("mainmenu");

		gui = new GUI(this, Game.renderer);
		gui.setSize();
        gui.applyTheme(Game.themeManager);
        gui.setTooltipDelay(Game.config.getTooltipDelay());
        
        campaignLabel = new Label();
        campaignLabel.setTheme("campaignlabel");
        this.add(campaignLabel);
        
        versionLabel = new Label("Build ID: " + Game.config.getVersionID());
        versionLabel.setTheme("versionlabel");
        this.add(versionLabel);
        
        campaignButton = new Button();
        campaignButton.setTheme("campaignbutton");
        campaignButton.addCallback(new Runnable() {
        	@Override public void run() {
        		CampaignPopup popup = new CampaignPopup(MainMenu.this);
        		popup.openPopupCentered();
        	}
        });
        this.add(campaignButton);
        
        newGameButton = new Button();
        newGameButton.setTheme("newgamebutton");
        newGameButton.addCallback(new Runnable() {
        	@Override public void run() {
        		NewGameWindow window = new NewGameWindow(MainMenu.this);
        		add(window);
        	}
        });
        this.add(newGameButton);
        
        loadGameButton = new Button();
        loadGameButton.setTheme("loadgamebutton");
        loadGameButton.addCallback(new Runnable() {
        	@Override public void run() {
        		// show the load game popup without any warnings about losing progress
        		LoadGamePopup popup = new LoadGamePopup(MainMenu.this, true);
        		popup.setCallback(MainMenu.this);
        		popup.openPopupCentered();
        	}
        });
        this.add(loadGameButton);
        
        editorButton = new Button();
        editorButton.setTheme("editorbutton");
        editorButton.addCallback(new Runnable() {
        	@Override public void run() {
        		launchEditor = true;
        		menuRunning = false;
        	}
        });
        this.add(editorButton);
        
        optionsButton = new Button();
        optionsButton.setTheme("optionsbutton");
        optionsButton.addCallback(new Runnable() {
        	@Override public void run() {
        		OptionsPopup popup = new OptionsPopup(MainMenu.this);
        		popup.openPopupCentered();
        	}
        });
        this.add(optionsButton);
        
        exitButton = new Button();
        exitButton.setTheme("exitbutton");
        exitButton.addCallback(new Runnable() {
        	@Override public void run() {
        		exit = true;
        		menuRunning = false;
        	}
        });
        this.add(exitButton);
        
        // load the background image
        backgroundSprite = SpriteManager.getSpriteAnyExtension("mainmenu");
        backgroundSpriteOffset = new Point();
        if (backgroundSprite != null) {
        	backgroundSpriteOffset.x = (Game.displayMode.getWidth() - backgroundSprite.getWidth()) / 2;
        	backgroundSpriteOffset.y = (Game.displayMode.getHeight() - backgroundSprite.getHeight()) / 2;
        }
        
        // load last open campaign from file if it exists
        String campaignID = this.getLastOpenCampaign();
        if (campaignID != null) this.loadCampaign(campaignID);
	}
	
	/**
	 * Sets the visible state of all buttons in this main menu to the
	 * specified value.  Note that if a widget hides these buttons,
	 * it needs to re-show them prior to closing
	 * @param visible
	 */
	
	protected void setButtonsVisible(boolean visible) {
		campaignButton.setVisible(visible);
		newGameButton.setVisible(visible);
		loadGameButton.setVisible(visible);
		editorButton.setVisible(visible);
		optionsButton.setVisible(visible);
		exitButton.setVisible(visible);
	}
	
	@Override protected void paintWidget(GUI gui) {
		if (backgroundSprite != null) {
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
			backgroundSprite.draw(backgroundSpriteOffset.x, backgroundSpriteOffset.y);
		}
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		buttonGap = themeInfo.getParameter("buttonGap", 0);
		titleOffset = themeInfo.getParameter("titleOffset", 0);
	}
	
	@Override protected void layout() {
		super.layout();
		
		int resX = Game.config.getResolutionX();
		
		campaignButton.setSize(campaignButton.getPreferredWidth(), campaignButton.getPreferredHeight());
		newGameButton.setSize(newGameButton.getPreferredWidth(), newGameButton.getPreferredHeight());
		loadGameButton.setSize(loadGameButton.getPreferredWidth(), loadGameButton.getPreferredHeight());
		editorButton.setSize(editorButton.getPreferredWidth(), editorButton.getPreferredHeight());
		optionsButton.setSize(optionsButton.getPreferredWidth(), optionsButton.getPreferredHeight());
		exitButton.setSize(exitButton.getPreferredWidth(), exitButton.getPreferredHeight());
		
		int buttonHeight = campaignButton.getHeight() + newGameButton.getHeight() +
			loadGameButton.getHeight() + editorButton.getHeight() + optionsButton.getHeight() +
			exitButton.getHeight() + 5 * buttonGap;
		
		int buttonY = (Game.config.getResolutionY() - buttonHeight) / 2;
		
		campaignButton.setPosition((resX - campaignButton.getWidth()) / 2, buttonY);
		newGameButton.setPosition((resX - newGameButton.getWidth()) / 2, campaignButton.getBottom() + buttonGap);
		loadGameButton.setPosition((resX - loadGameButton.getWidth()) / 2, newGameButton.getBottom() + buttonGap);
		editorButton.setPosition((resX - editorButton.getWidth()) / 2, loadGameButton.getBottom() + buttonGap);
		optionsButton.setPosition((resX - optionsButton.getWidth()) / 2, editorButton.getBottom() + buttonGap);
		exitButton.setPosition((resX - exitButton.getWidth()) / 2, optionsButton.getBottom() + buttonGap);
		
		campaignLabel.setSize(campaignLabel.getPreferredWidth(), campaignLabel.getPreferredHeight());
		campaignLabel.setPosition((resX - campaignLabel.getWidth()) / 2, buttonY - campaignLabel.getHeight() - titleOffset);
		
		versionLabel.setSize(versionLabel.getPreferredWidth(), versionLabel.getPreferredHeight());
		versionLabel.setPosition(getInnerRight() - versionLabel.getWidth(), getInnerBottom() - versionLabel.getHeight());
	}
	
	/**
	 * This function is called after creating the MainMenu.  Runs the main display
	 * loop for the menu until the user either starts or loads a game, launches the
	 * editor, exits the menu, or selects a new resolution in the config (which causes
	 * the MainMenu to be reloaded)
	 * @return the MainMenuAction that should be taken by the game
	 */
	
	public MainMenuAction mainLoop() {
		update();
		
		while (menuRunning) {
			if (loader != null && !loader.isAlive()) {
				popup.closePopup();
				MainMenu.this.update();
				loader = null;
				
				if (exitOnLoad) {
					menuRunning = false;
				}
			}
			
			Game.textureLoader.update();
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			gui.update();
			
			Display.update(false);
            GL11.glGetError();
            Display.sync(60);
            Display.processMessages();
			
			if (Display.isCloseRequested()) {
				menuRunning = false;
				exit = true;
			}
		}
		
		if (exitCallback != null)
			exitCallback.run();
		
		
		gui.destroy();
		
		if (exit) return new MainMenuAction(MainMenuAction.Action.Exit);
		else if (restart) return new MainMenuAction(MainMenuAction.Action.Restart);
		else if (launchEditor) return new MainMenuAction(MainMenuAction.Action.LaunchEditor);
		else if (loadGame != null) return new MainMenuAction(loadGame);
		else return new MainMenuAction(MainMenuAction.Action.NewGame);
	}
	
	/**
	 * Sets the specified callback to be run after this main menu has completed its
	 * main loop, just prior to exiting
	 * @param callback the callback to run()
	 */
	
	public void setExitCallback(Runnable callback) {
		this.exitCallback = callback;
	}
	
	/**
	 * Sets this MainMenu to exit its main loop upon completion of the campaign loading process
	 * It will not exit until completion of the load, regardless of other exit commands
	 */
	
	public void setExitOnLoad() {
		exitOnLoad = true;
	}
	
	/**
	 * Specify that the menu should be restarted.  The main loop
	 * will terminate on its next iteration after calling this
	 */
	
	public void restartMenu() {
		this.menuRunning = false;
		this.restart = true;
	}
	
	/**
	 * Save the specified Campaign ID to the campaigns/lastOpenCampaign.txt
	 * file.  This file is automatically read at game startup and will
	 * automatically choose the specified campaign when the MainMenu is started.
	 * @param id the ID of the campaign to write
	 */
	
	public static void writeLastOpenCampaign(String id) {
		File f = new File("campaigns/lastOpenCampaign.txt");
		
		try {
			FileWriter writer = new FileWriter(f, false);
			writer.write(id);
			writer.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error writing last open campaign file.", e);
		}
	}
	
	/**
	 * Called when the state of the menu has changed.  This method controls
	 * enabling and disabling buttons as appropriate, setting title text,
	 * and exiting the menu when a save game has been loaded
	 * 
	 * Note that this method should be called after one of the sub menu's
	 * has either completed or been canceled, in order to enable the main
	 * menu buttons again
	 */
	
	public void update() {
		if (Game.curCampaign == null) {
			// if no campaign is selected, disable starting or loading a game
			newGameButton.setEnabled(false);
			loadGameButton.setEnabled(false);
		}
		else {
			newGameButton.setEnabled(true);
			loadGameButton.setEnabled(SaveGameUtil.getSaveGames().size() > 0);

			if (Game.curCampaign != null) {
				
				if (Game.curCampaign.party.size() > 0 || loadGame != null) {
					
					if (exitOnLoad) {
						setButtonsVisible(false);
						campaignLabel.setVisible(false);
						
					} else {
						menuRunning = false;
						
						// hide the menu while loading takes place
						setVisible(false);
					}
					
				}
				
				campaignLabel.setText(Game.curCampaign.getName());
			}
		}
	}
	
	private String getLastOpenCampaign() {
		try {
			return FileUtil.readFileAsString("campaigns/lastOpenCampaign.txt");
		} catch (Exception e) {
			Logger.appendToErrorLog("Error loading last open campaign file.", e);
			return null;
		}
	}

	@Override public void loadGameAccepted(String saveGame) {
		this.loadGame = saveGame;
		update();
	}
	
	/**
	 * The specified campaign is loaded into memory and set as the current campaign
	 * in Game.curCampaign
	 * @param campaignID the resource ID of the campaign to load
	 */
	
	public void loadCampaign(String campaignID) {
		Game.curCampaign = new Campaign(campaignID);
		
		loader = new CampaignLoadingTaskList();
		
		// recreate the bg sprite after clearing the sprite manager
		backgroundSprite = SpriteManager.getSpriteAnyExtension("mainmenu");
		
		loader.start();
		
		popup = new LoadingWaitPopup(MainMenu.this, "Loading " + campaignID);
		popup.setBGSprite(backgroundSprite);
		popup.setLoadingTaskList(loader);
		popup.openPopupCentered();
	}
}