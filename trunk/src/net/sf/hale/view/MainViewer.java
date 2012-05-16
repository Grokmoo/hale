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

package net.sf.hale.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.AreaListener;
import net.sf.hale.Game;
import net.sf.hale.Keybindings;
import net.sf.hale.QuestEntry;
import net.sf.hale.ScriptInterface;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.loading.LoadingTaskList;
import net.sf.hale.loading.LoadingWaitPopup;
import net.sf.hale.mainmenu.ConfirmQuitPopup;
import net.sf.hale.mainmenu.MainMenuAction;
import net.sf.hale.quickbar.QuickbarViewer;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Merchant;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.widgets.EntityMouseover;
import net.sf.hale.widgets.InitiativeTicker;
import net.sf.hale.widgets.MainPane;
import net.sf.hale.widgets.MessageBox;
import net.sf.hale.widgets.OverHeadFadeAway;
import net.sf.hale.widgets.PortraitArea;
import net.sf.hale.widgets.RightClickMenu;
import net.sf.hale.widgets.TextAreaNoInput;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

public class MainViewer extends DesktopArea {
	private final GUI gui;
	private boolean isRunning = false;
	private boolean exitGame = false;
	
	private boolean updateInterface = false;
	private QuestEntry newQuestEntry;
	private final List<Entity> entityUpdateList = new ArrayList<Entity>();
	private final List<PopupWindow> popupsToShow = new ArrayList<PopupWindow>();
	private final List<PopupWindow> popupsToHide = new ArrayList<PopupWindow>();
	
	private final FPSCounter fpsCounter;
	
	public final CharacterWindow characterWindow;
	public final InventoryWindow inventoryWindow;
	public final MiniMapWindow miniMapWindow;
	public final LogWindow logWindow;
	
	public final ContainerWindow containerWindow;
	public final MerchantWindow merchantWindow;
	public final CraftingWindow craftingWindow;
	public final ScriptConsole scriptConsole;
	
	private final MessageBox messageBox;
	private final QuickbarViewer quickbarViewer;
	private final PortraitArea portraitArea;
	private final InitiativeTicker ticker;
	private final MainPane mainPane;
	
	private final RightClickMenu menu;
	private final EntityMouseover mouseOver;
	
	private final TextArea targeterDescription;
	private final HTMLTextAreaModel targeterDescriptionModel;
	
	private final List<OverHeadFadeAway> fadeAways;
	private final List<OverHeadFadeAway> fadeAwaysToAdd;
	
	private Keybindings keyBindings;
	
	private MainMenuAction action;
	
	public int mouseX, mouseY;
	
	public MainViewer() {
		Game.mainViewer = this;
		
		fadeAways = new ArrayList<OverHeadFadeAway>();
		fadeAwaysToAdd = new ArrayList<OverHeadFadeAway>();
		
		this.setTheme("");
		
		gui = new GUI(this, Game.renderer);
		gui.setSize();
        gui.applyTheme(Game.themeManager);
        gui.setTooltipDelay(Game.config.getTooltipDelay());
        
		characterWindow = new CharacterWindow();
        characterWindow.setVisible(false);
        
        inventoryWindow = new InventoryWindow();
        inventoryWindow.setVisible(false);
        
        containerWindow = new ContainerWindow();
        containerWindow.setVisible(false);
        
        craftingWindow = new CraftingWindow();
        craftingWindow.setVisible(false);
        
        miniMapWindow = new MiniMapWindow();
        miniMapWindow.setVisible(false);
        
        logWindow = new LogWindow();
        logWindow.setVisible(false);
        
        merchantWindow = new MerchantWindow();
        merchantWindow.setVisible(false);
        
        scriptConsole = new ScriptConsole();
        scriptConsole.setVisible(false);
        
        mouseOver = new EntityMouseover();
        mouseOver.setVisible(false);
        
        Game.areaViewer = new AreaViewer(Game.curCampaign.curArea);
		Game.areaListener = new AreaListener(Game.curCampaign.curArea, Game.areaViewer);
		Game.areaViewer.setListener(Game.areaListener);
		
        menu = new RightClickMenu(this);
        
        mainPane = new MainPane();
        fpsCounter = new FPSCounter();
        quickbarViewer = new QuickbarViewer();
		portraitArea = new PortraitArea();
        ticker = new InitiativeTicker();
        messageBox = new MessageBox();
        
        targeterDescriptionModel = new HTMLTextAreaModel();
        targeterDescription = new TextAreaNoInput(targeterDescriptionModel);
        targeterDescription.setTheme("targeterdescription");
        
        // create the default key bindings
        keyBindings = new Keybindings();
	}
	
	private void addWidgets() {
		this.add(Game.areaViewer);
		
		this.add(mainPane);
		if (Game.config.showFPS()) this.add(fpsCounter);
		this.add(quickbarViewer);
		this.add(portraitArea);
		this.add(ticker);
        this.add(mouseOver);
		this.add(messageBox);
		this.add(targeterDescription);
		
		this.add(characterWindow);
        this.add(inventoryWindow);
        this.add(containerWindow);
        this.add(craftingWindow);
        this.add(miniMapWindow);
        this.add(logWindow);
        this.add(merchantWindow);
        this.add(scriptConsole);
	}
	
	public void clearTargetTitleText() {
		targeterDescriptionModel.setHtml("");
	}
	
	public void setTargetTitleText(String line1, String line2, String line3) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\"font-family: large-red; text-align: center\">");
		sb.append(line1);
		sb.append("</div>");
		
		if (line2 != null) {
			sb.append("<div style=\"font-family: yellow; text-align: center;\">");
			sb.append(line2);
			sb.append("</div>");
		}
		
		if (line3 != null) {
			sb.append("<div style=\"font-family: orange; text-align: center;\">");
			sb.append(line3);
			sb.append("</div>");
		}
		
		targeterDescriptionModel.setHtml(sb.toString());
	}
	
	public MainPane getMainPane() { return mainPane; }
	
	public PortraitArea getPortraitArea() { return portraitArea; }
	
	public QuickbarViewer getQuickbarViewer() { return quickbarViewer; }
	
	public Point getMouseGridPoint() {
		int x = Game.areaListener.getLastMouseX();
		int y = Game.areaListener.getLastMouseY();
		
		return AreaUtil.convertScreenToGrid(x, y);
	}
	
	public void setLoadGame(String loadGame) {
		this.action = new MainMenuAction(loadGame);
	}
	
	public void setMainMenuAction(MainMenuAction action) {
		this.action = action;
	}
	
	public void runLoadingLoop(LoadingTaskList loader, LoadingWaitPopup popup) {
		boolean running = true;
		
		while (running) {
			if (loader != null && !loader.isAlive()) {
				running = false;
				this.hidePopup(popup);
			}
			
			Game.textureLoader.update();
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			gui.update();
			
			Display.update(false);
            GL11.glGetError();
            Display.sync(60);
            Display.processMessages();
			
			if (Display.isCloseRequested()) {
				running = false;
			}
		}
	}
	
	public MainMenuAction runCampaign(boolean newGame) {
		addWidgets();
		
		// clear any old particles
		Game.particleManager.clear();
		
		isRunning = true;
		
		if (newGame) {
			// load tileset
			Game.curCampaign.getTileset(Game.curCampaign.curArea.getTileset()).loadTiles();
			Game.curCampaign.curArea.getTileGrid().cacheSprites();
			
			for (Entity e : Game.curCampaign.curArea.getEntities()) {
				e.resetAll();
				if (e.getType() == Entity.Type.CREATURE) {
					((Creature)e).getTimer().endTurn();
				}
			}
			
			Game.mainViewer.addMessage("red", "Entered area " + Game.curCampaign.curArea.getName());
			Game.areaListener.nextTurn();
			if (Game.curCampaign.getStartingMerchant() != null) {
				ScriptInterface.showMerchant(Game.curCampaign.getStartingMerchant());
			}
			
			Game.curCampaign.curArea.runOnAreaLoad(null);
		}
		
		updateContent(System.currentTimeMillis());
		gui.update();
		
		Game.areaViewer.scrollToCreature(Game.curCampaign.party.getSelected());
		
		while (isRunning) {
			// load any async textures
			Game.textureLoader.update();
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
			
			long curTime = System.currentTimeMillis();
			
			Game.areaViewer.update(curTime);
			Game.particleManager.update(curTime);
			Game.interfaceLocker.checkTime(curTime);
			Game.timer.updateTime(curTime);
			
			if (menu.shouldPopupToggle()) {
				menu.togglePopup();
			}
			
			updateContent(curTime);
			
			gui.update();
			
            Display.update(false);
            GL11.glGetError();
            if (Game.config.capFPS()) Display.sync(60);
            Display.processMessages();
            
			if (Display.isCloseRequested()) {
				new ConfirmQuitPopup(this, ConfirmQuitPopup.QuitMode.ExitGame).openPopupCentered();
			}
		}
		
		gui.destroy();
		
		// interrupt any currently executing threads
		for (Thread thread : Game.getActiveThreads()) {
			thread.interrupt();
		}
		
		Game.interfaceLocker.clear();
		
		if (action != null) return action;
		else if (exitGame) return new MainMenuAction(MainMenuAction.Action.Exit);
		else return new MainMenuAction(MainMenuAction.Action.ShowMainMenu);
	}
	
	public void addFadeAway(String text, int x, int y, String color) {
		Point gridPoint = new Point(x, y);
		
		int offsetY = 0;
		
		synchronized (fadeAwaysToAdd) {
			for (OverHeadFadeAway fadeAway : fadeAwaysToAdd) {
				if (fadeAway.getGridPoint().equals(gridPoint)) {
					offsetY += 22;
				}
			}

			OverHeadFadeAway fadeAway = new OverHeadFadeAway(text, gridPoint, color);
			fadeAway.setOffset(0, offsetY);
			fadeAwaysToAdd.add(fadeAway);
		}
	}
	
	public List<OverHeadFadeAway> getFadeAways() { return fadeAways; }
	
	public void exitGame() {
		this.isRunning = false;
		this.exitGame = true;
	}
	
	public void exitToMainMenu() {
		this.isRunning = false;
	}
	
	public void closeAllWindows() {
		craftingWindow.setVisible(false);
		containerWindow.setVisible(false);
		characterWindow.setVisible(false);
		inventoryWindow.setVisible(false);
		merchantWindow.setVisible(false);
		miniMapWindow.setVisible(false);
		logWindow.setVisible(false);
		
		for (Entity entity : Game.curCampaign.curArea.getEntities()) {
			entity.closeViewers();
		}
		
		portraitArea.closeLevelUpWindows();
	}
	
	public void setMerchant(Merchant merchant) {
		merchantWindow.setMerchant(merchant);
		inventoryWindow.setMerchant(merchant);
		updateInterface();
	}
	
	public EntityMouseover getMouseOver() { return mouseOver; }
	public RightClickMenu getMenu() { return menu; }
	
	public void updateEntity(Entity e) {
		if (e == null) return;
		
		synchronized(entityUpdateList) {
			if (!entityUpdateList.contains(e)) {
				entityUpdateList.add(e);
			}
		}
		
		if (e.isPlayerSelectable() || e == mouseOver.getSelectedEntity()) {
			updateInterface();
		}
	}
	
	public void hidePopup(PopupWindow popup) {
		synchronized(popupsToHide) {
			popupsToHide.add(popup);
		}
	}
	
	public void showPopup(PopupWindow popup) {
		synchronized(popupsToShow) {
			popupsToShow.add(popup);
		}
	}
	
	public boolean isMoveDisabledDueToOpenWindows() {
		return (merchantWindow.isVisible() || containerWindow.isVisible() || craftingWindow.isVisible());
	}
	
	public void updateInterface() {
		this.updateInterface = true;
	}
	
	private void updateContent(long curTime) {
		Iterator<OverHeadFadeAway> iter = fadeAways.iterator();
		while (iter.hasNext()) {
			OverHeadFadeAway fadeAway = iter.next();
			fadeAway.updateTime(curTime);
			if (fadeAway.isFinished()) {
				iter.remove();
				this.removeChild(fadeAway);
			}
		}
		
		synchronized(popupsToShow) {
			for (PopupWindow p : popupsToShow) {
				p.openPopupCentered();
				
				// cancel any current movement
				Game.interfaceLocker.interruptMovement();
			}
			popupsToShow.clear();
		}
		
		synchronized(popupsToHide) {
			for (PopupWindow p : popupsToHide) {
				p.closePopup();
			}
			
			popupsToHide.clear();
		}
		
		synchronized(fadeAwaysToAdd) {
			for (OverHeadFadeAway fadeAway : fadeAwaysToAdd) {
				fadeAways.add(fadeAway);
				this.insertChild(fadeAway, 1);
				fadeAway.initialize(curTime);
			}
			
			fadeAwaysToAdd.clear();
		}
		
		synchronized(entityUpdateList) {
			for (Entity e : entityUpdateList) {
				Game.areaListener.checkKillEntity(e);
				e.updateViewers();
			}
			entityUpdateList.clear();
		}
		
		if (updateInterface) {
			this.updateInterface = false;
			
			characterWindow.updateContent(Game.curCampaign.party.getSelected());
			inventoryWindow.updateContent(Game.curCampaign.party.getSelected());
			merchantWindow.updateContent(Game.curCampaign.party.getSelected());
			miniMapWindow.updateContent(Game.curCampaign.curArea);
			logWindow.updateContent();
			containerWindow.updateContent();
			craftingWindow.updateContent();
			quickbarViewer.updateContent(Game.curCampaign.party.getSelected());
			portraitArea.updateContent();
			
			if (mouseOver.getPoint() != null) mouseOver.setPoint(mouseOver.getPoint());
			
			if (newQuestEntry != null) {
				logWindow.notifyNewEntry(newQuestEntry);
				newQuestEntry = null;
			}
			
			mainPane.update();
			ticker.updateContent();

			messageBox.update();
			
			Game.areaListener.getTargeterManager().checkCurrentTargeter();
		}
		
		synchronized(this) {
			this.notifyAll();
		}
	}
	
	public void setNewQuestEntry(QuestEntry entry) {
		this.newQuestEntry = entry;
	}
	
	public void addMessage(String text) {
		messageBox.addMessage("black", text);
	}
	
	public void addMessage(String font, String text) {
		messageBox.addMessage(font, text);
		
		updateInterface();
	}
	
	public String getMessageBoxContents() {
		return messageBox.getContents();
	}
	
	public Keybindings getKeyBindings() {
		return keyBindings;
	}
	
	public void quickSave() {
		Game.mainViewer.updateInterface();
		
		if (Game.isInTurnMode() || Game.curCampaign.party.isDefeated()) {
			Game.mainViewer.addMessage("red", "You cannot save the game while in combat mode.");
			return;
		}
		
		File fout = SaveGameUtil.getNextQuickSaveFile();
		
		try {
			SaveGameUtil.saveGame(fout);
			Game.mainViewer.addMessage("link", "Quicksave successful.");
		} catch (Exception e) {
			Logger.appendToErrorLog("Error when quicksaving to " + fout.getPath(), e);
			Game.mainViewer.addMessage("red", "Error saving game!");
			fout.delete();
		}
	}
	
	@Override protected boolean handleEvent(Event evt) {
		mouseX = evt.getMouseX();
		mouseY = evt.getMouseY();
		
		if (evt.getType() == Event.Type.KEY_PRESSED) {
			if (!scriptConsole.hasKeyboardFocus()) {
				keyBindings.fireKeyEvent(evt.getKeyCode());
				return true;
			}
		}
		
		return super.handleEvent(evt);
	}
    
    @Override protected void layout() {
		super.layout();
		
		mainPane.setSize(getInnerWidth(), mainPane.getPreferredHeight());
		mainPane.setPosition(getInnerX(), getInnerBottom() - mainPane.getHeight());
		//mainPane.update(); // update end turn button
		
		int centerX = getInnerX() + getInnerWidth() / 2;
		
		quickbarViewer.setSize(quickbarViewer.getPreferredWidth(), quickbarViewer.getPreferredHeight());
		quickbarViewer.setPosition(centerX - quickbarViewer.getWidth() / 2,
				getInnerBottom() - quickbarViewer.getHeight());
		
		int messageBoxWidthA = mainPane.getButtonsMinX() - mainPane.getInnerX();
		int messageBoxWidthB = messageBox.getMaxWidth();
		
		if (messageBoxWidthA > messageBoxWidthB) {
			messageBox.setSize(messageBoxWidthB, messageBox.getPreferredHeight());
			int availableWidth = messageBoxWidthA - messageBoxWidthB;
			messageBox.setPosition(mainPane.getInnerX() + availableWidth / 2, quickbarViewer.getY() - messageBox.getHeight());
		} else {
			messageBox.setSize(messageBoxWidthA, messageBox.getPreferredHeight());
			messageBox.setPosition(mainPane.getInnerX(), quickbarViewer.getY() - messageBox.getHeight());	
		}
        
        ticker.setPosition(getInnerX(), getInnerY());
        
		ticker.setSize(ticker.getPreferredWidth(), mainPane.getY() - getInnerY());
		
		portraitArea.setSize(portraitArea.getPreferredWidth(), getInnerHeight() - mainPane.getHeight());
		portraitArea.setPosition(getInnerRight() - portraitArea.getWidth(), getInnerY());
		
		fpsCounter.setPosition(ticker.getRight(), getInnerY() + fpsCounter.getPreferredHeight() / 2);
		
		if (ticker.isVisible()) Game.areaViewer.setPosition(ticker.getRight(), getInnerY());
		else Game.areaViewer.setPosition(getInnerX(), getInnerY());
		
		Game.areaViewer.setSize(portraitArea.getX() - Game.areaViewer.getX(), mainPane.getY() - getInnerY());
		
		if (mouseOver.isVisible()) {
			mouseOver.setSize(mouseOver.getPreferredWidth(), mouseOver.getPreferredHeight());
			Point screen = AreaUtil.convertGridToScreen(mouseOver.getPoint());
			screen.x -= Game.areaViewer.getScrollX() - Game.areaViewer.getInnerX();
			screen.y -= Game.areaViewer.getScrollY() - Game.areaViewer.getInnerY();
			
			int x = screen.x + Game.TILE_SIZE / 2 - mouseOver.getWidth() / 2;
			int y = screen.y - mouseOver.getHeight();
			
			if (x < 0)
				x = 0;
			
			if (y < 0)
				y = 0;
			
			if (x + mouseOver.getWidth() > Game.config.getResolutionX())
				x = Game.config.getResolutionX() - mouseOver.getWidth();
			
			if (y + mouseOver.getHeight() > Game.config.getResolutionY())
				y = Game.config.getResolutionY() - mouseOver.getHeight();
			
			mouseOver.setPosition(x, y);
		}
		
		targeterDescription.setSize(getInnerWidth(), targeterDescription.getPreferredHeight());
		targeterDescription.setPosition(getInnerX(), getInnerY());
	}

    @Override protected void keyboardFocusChildChanged(Widget child) {
    	// only change the order for GameSubWindows
    	if (child != null && child instanceof GameSubWindow) {
    		int fromIdx = getChildIndex(child);
    		assert fromIdx >= 0;
    		int numChildren = getNumChildren();
    		if (fromIdx < numChildren - 1) {
    			moveChild(fromIdx, numChildren - 1);
    		}
    	}
    }
    
    public long getTextureMemoryUsage() {
    	return SpriteManager.getTextureMemoryUsage();
    }
}
