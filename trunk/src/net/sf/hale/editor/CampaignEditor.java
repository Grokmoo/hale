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

package net.sf.hale.editor;

import java.io.File;
import java.util.Collection;
import java.util.List;

import net.sf.hale.Area;
import net.sf.hale.AreaTransition;
import net.sf.hale.AreaTrigger;
import net.sf.hale.Campaign;
import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.Updateable;
import net.sf.hale.WorldMapLocation;
import net.sf.hale.editor.reference.Script;
import net.sf.hale.editor.scripteditor.ScriptEditor;
import net.sf.hale.editor.widgets.CampaignEditorMenuBar;
import net.sf.hale.editor.widgets.CloseEditorPopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Item;
import net.sf.hale.entity.Trap;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.Merchant;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.DirectoryListing;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class CampaignEditor extends DesktopArea implements Updateable, PopupCallback {
	private final GUI gui;
	private boolean isRunning = false;
	
	private String path;
	private String areaPath;
	
	private final SimpleChangableListModel<Item> itemsModel;
	private final SimpleChangableListModel<Creature> creaturesModel;
	private final SimpleChangableListModel<Encounter> encountersModel;
	private final SimpleChangableListModel<ItemList> itemListsModel;
	private final SimpleChangableListModel<AreaTransition> transitionsModel;
	private final SimpleChangableListModel<AreaTrigger> triggersModel;
	private final SimpleChangableListModel<Area> areasModel;
	private final SimpleChangableListModel<WorldMapLocation> locationsModel;
	private final SimpleChangableListModel<Merchant> merchantsModel;
	
	private final SimpleChangableListModel<Script> aiScripts;
	private final SimpleChangableListModel<Script> conversationScripts;
	private final SimpleChangableListModel<Script> itemScripts;
	private final SimpleChangableListModel<Script> questScripts;
	private final SimpleChangableListModel<Script> triggerScripts;
	
	private final CampaignEditorMenuBar menuBar;
	
	public AreaEditor areaEditor;
	
	public final EncounterEditor encounterEditor;
	public final TransitionEditor transitionEditor;
	public final ItemEditor itemEditor;
	public final CreatureEditor creatureEditor;
	public final ItemListEditor itemListEditor;
	public final AreaTriggerEditor areaTriggerEditor;
	public final RecipeEditor recipeEditor;
	public final MerchantEditor merchantEditor;
	public final CampaignPropertiesEditor propertiesEditor;
	public final ScriptEditor scriptEditor;
	
	public CampaignEditor(String campaignPath) {
		setFocusKeyEnabled(false);
		
		this.setTheme("");
		
		gui = new GUI(this, Game.renderer);
		gui.setSize();
        gui.applyTheme(Game.themeManager);
        
        Game.campaignEditor = this;
        Game.ruleset = new Ruleset();
        if (campaignPath != null) {
        	openCampaign(campaignPath);
        } else {
        	Game.ruleset.readData();
        }
        
        locationsModel = new SimpleChangableListModel<WorldMapLocation>();
        itemsModel = new SimpleChangableListModel<Item>();
        creaturesModel = new SimpleChangableListModel<Creature>();
        encountersModel = new SimpleChangableListModel<Encounter>();
        transitionsModel = new SimpleChangableListModel<AreaTransition>();
        triggersModel = new SimpleChangableListModel<AreaTrigger>();
        itemListsModel = new SimpleChangableListModel<ItemList>();
        merchantsModel = new SimpleChangableListModel<Merchant>();
        
        aiScripts = new SimpleChangableListModel<Script>();
        conversationScripts = new SimpleChangableListModel<Script>();
        itemScripts = new SimpleChangableListModel<Script>();
        questScripts = new SimpleChangableListModel<Script>();
        triggerScripts = new SimpleChangableListModel<Script>();
        
        areasModel = new SimpleChangableListModel<Area>();
		
		encounterEditor = new EncounterEditor(this);
		encounterEditor.setVisible(false);
		
		transitionEditor = new TransitionEditor(this);
		transitionEditor.setVisible(false);
		
		itemEditor = new ItemEditor(this);
		itemEditor.setVisible(false);
		
		creatureEditor = new CreatureEditor(this);
		creatureEditor.setVisible(false);
		
		itemListEditor = new ItemListEditor(this);
		itemListEditor.setVisible(false);
		
		areaTriggerEditor = new AreaTriggerEditor(this);
		areaTriggerEditor.setVisible(false);
		
		recipeEditor = new RecipeEditor(this);
		recipeEditor.setVisible(false);
		
		merchantEditor = new MerchantEditor(this);
		merchantEditor.setVisible(false);
		
		propertiesEditor = new CampaignPropertiesEditor(this);
		propertiesEditor.setVisible(false);
		
		scriptEditor = new ScriptEditor();
		scriptEditor.setVisible(false);
		
		this.add(areaTriggerEditor);
		this.add(itemListEditor);
		this.add(creatureEditor);
		this.add(itemEditor);
		this.add(encounterEditor);
		this.add(transitionEditor);
		this.add(recipeEditor);
		this.add(merchantEditor);
		this.add(propertiesEditor);
		this.add(scriptEditor);
		
		menuBar = new CampaignEditorMenuBar();
		menuBar.setSize(Game.config.getEditorResolutionX(), 20);
		this.add(menuBar);
		
		if (campaignPath != null) {
			this.updateStatusText("Loaded campaign from " + this.path);
		}
	}
	
	public void openCampaign(String id) {
		// free any tileset memory
		if (areaEditor != null) {
			Game.curCampaign.getTileset(this.areaEditor.getArea().getTileset()).freeTiles();
		}
		
		Game.entityManager.clearEntities();
		
		Game.curCampaign = new Campaign(id);
		
		this.path = "campaigns/" + Game.curCampaign.getID();
		ResourceManager.registerCampaignPackage();
		
		Game.ruleset.readData();
		Game.curCampaign.readCampaignFile();
        
        this.areaPath = null;
        this.removeChild(areaEditor);
        this.areaEditor = null;
        
        if (menuBar != null) {
        	this.updateStatusText("Loaded campaign from " + this.path);
        }
	}
	
	public String getPath() {
		return this.path;
	}
	
	public void updateStatusText(String text) {
		menuBar.updateStatusText(text);
	}
	
	public AreaEditor.Mode getMode() {
		return menuBar.getMode();
	}
	
	public AreaTriggerEditor getTriggerEditor() { return areaTriggerEditor; }
	public TransitionEditor getTransitionEditor() { return transitionEditor; }
	
	public void reloadEncounters() {
		if (areaEditor != null) areaEditor.reloadEncounters();
	}
	
	public void reloadTriggersAndTransitions() {
		if (areaEditor != null) areaEditor.reloadTriggersAndTransitions();
	}
	
	public String getCampaignPath() { return path; }
	
	public void exit() {
		this.isRunning = false;
	}
	
	public void mainLoop() {
		update();
		isRunning = true;
		
		while (isRunning) {
			Game.textureLoader.update();
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			gui.update();
			
            Display.update(false);
            GL11.glGetError();
            Display.sync(60);
            Display.processMessages();
            
			if (Display.isCloseRequested()) {
				CloseEditorPopup popup = new CloseEditorPopup(this);
				popup.openPopupCentered();
			}
		}
		
		gui.destroy();
	}
	
	public void checkSaveArea() {
		if (this.areaPath == null || this.areaEditor == null) return;
		
		this.areaEditor.saveArea();
	}
	
	@Override public void newComplete() {
		this.update();
	}

	@Override public void copyComplete() { }

	@Override public void deleteComplete() {
		String areaPath = menuBar.getSelectedArea();
		
		if (areaPath.equals(this.areaPath)) {
			this.removeChild(areaEditor);
			this.areaPath = null;
		}
		
		this.update();
	}
	
	public void checkOpenArea(String newAreaPath) {
		if (newAreaPath == null) return;
		
		if (newAreaPath.equals(areaPath)) return;
		
		if (areaPath == null) openSelectedArea();
		else {
			OpenAreaPopup popup = new OpenAreaPopup(this, newAreaPath);
			popup.openPopupCentered();
		}
	}
	
	public void openSelectedArea() {
		String newAreaPath = menuBar.getSelectedArea();
		if (newAreaPath == null) return;
		
		if (newAreaPath.equals(areaPath)) return;
		
		if (areaEditor != null) {
			Game.curCampaign.getTileset(this.areaEditor.getArea().getTileset()).freeTiles();
		}
		Game.curCampaign.removeArea(areaPath);
		this.removeChild(areaEditor);
		areaPath = newAreaPath;
		Area area = Game.curCampaign.getArea(areaPath);
		AreaUtil.setMatrix(area.getExplored(), true);
		
		// remove automatically placed creatures from encounters
		Encounter.removeCreaturesFromArea(area);
		
		// force spot all traps so we can see them in the editor
		for (Entity e : area.getEntities()) {
			if (e.getType() == Entity.Type.TRAP) ((Trap)e).setSpotted(true);
		}
		
		areaEditor = new AreaEditor(area, newAreaPath, new Point(0, 20));
		this.add(areaEditor);
		this.moveChild(this.getChildIndex(areaEditor), 0);
		
		this.update();
		
		this.updateStatusText("Area " + areaPath + " opened successfully.");
	}
	
	public SimpleChangableListModel<Script> getScripts(ScriptEditor.Type type) {
		switch (type) {
		case Conversation: return conversationScripts;
		case Item: return itemScripts;
		case Quest: return questScripts;
		case Trigger: return triggerScripts;
		case AI: return aiScripts;
		}
		
		return null;
	}
	
	public SimpleChangableListModel<Script> getAIScripts() { return aiScripts; }
	public SimpleChangableListModel<Script> getConversationScripts() { return conversationScripts; }
	public SimpleChangableListModel<Script> getItemScripts() { return itemScripts; }
	public SimpleChangableListModel<Script> getQuestScripts() { return questScripts; }
	public SimpleChangableListModel<Script> getTriggerScripts() { return triggerScripts; }
	
	public SimpleChangableListModel<Merchant> getMerchantsModel() { return merchantsModel; }
	public SimpleChangableListModel<WorldMapLocation> getLocationsModel() { return locationsModel; }
	public SimpleChangableListModel<Area> getAreasModel() { return areasModel; }
	public SimpleChangableListModel<ItemList> getItemListsModel() { return itemListsModel; }
	public SimpleChangableListModel<Item> getItemsModel() { return itemsModel; }
	public SimpleChangableListModel<Creature> getCreaturesModel() { return creaturesModel; }
	public SimpleChangableListModel<Encounter> getEncountersModel() { return encountersModel; }
	public SimpleChangableListModel<AreaTransition> getTransitionsModel() { return transitionsModel; }
	public SimpleChangableListModel<AreaTrigger> getTriggersModel() { return triggersModel; }
	
	public void moveToFront(Widget child) {
		if (child != null) {
			int fromIndex = this.getChildIndex(child);
			int numChildren = this.getNumChildren();
			if (fromIndex < numChildren - 1) {
				this.moveChild(fromIndex, numChildren - 1);
			}
		}
	}
	
	@Override public void update() {
		this.menuBar.setMenuHiddenState();
		
		if (this.path == null) return;
		
		Game.entityManager.clearEntities();
		
		merchantsModel.clear();
		File merchantsDir = new File(Game.campaignEditor.getPath() + "/merchants");
		List<File> merchantFiles = DirectoryListing.getFiles(merchantsDir);
		for (File file : merchantFiles) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			merchantsModel.addElement(Game.curCampaign.getMerchant(ref));
		}
		
		locationsModel.clear();
		for (WorldMapLocation location : Game.curCampaign.worldMapLocations) {
			locationsModel.addElement(location);
		}
		
		areasModel.clear();
		File areaDir = new File(Game.campaignEditor.getPath() + "/areas");
		List<File> areaFiles = DirectoryListing.getFiles(areaDir);
		for (File file : areaFiles) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			areasModel.addElement(Game.curCampaign.getArea(ref));
		}
		if (areaEditor != null) areaEditor.update();
		
		creaturesModel.clear();
		File creDir = new File(Game.campaignEditor.getPath() + "/creatures");
		for (File file : DirectoryListing.getFiles(creDir)) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			creaturesModel.addElement(Game.entityManager.getCreature(ref));
		}
		
		encountersModel.clear();
		File encDir = new File(Game.campaignEditor.getPath() + "/encounters");
		for (File file : DirectoryListing.getFiles(encDir)) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			encountersModel.addElement(Game.curCampaign.getEncounter(ref));
		}
		
		transitionsModel.clear();
		File tranDir = new File(Game.campaignEditor.getPath() + "/transitions");
		for (File file : DirectoryListing.getFiles(tranDir)) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			AreaTransition transition = Game.curCampaign.getAreaTransition(ref);
			transition.activate();
			
			transitionsModel.addElement(transition);
		}
		
		triggersModel.clear();
		File trigDir = new File(Game.campaignEditor.getPath() + "/triggers");
		for (File file : DirectoryListing.getFiles(trigDir)) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			triggersModel.addElement(Game.curCampaign.getTrigger(ref));
		}
		
		itemsModel.clear();
		File itemDir = new File(Game.campaignEditor.getPath() + "/items");
		for (File file : DirectoryListing.getFiles(itemDir)) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			itemsModel.addElement(Game.entityManager.getItem(ref));
		}
		
		itemListsModel.clear();
		Game.ruleset.readItemLists();
		
		Collection<ItemList> listsCollection = Game.ruleset.getAllItemLists();
		for (ItemList list : listsCollection) {
			itemListsModel.addElement(list);
		}
		
		if (areaEditor != null) {
			areaEditor.reloadEncounters();
			areaEditor.reloadTriggersAndTransitions();
		}
		
		updateScripts();
	}
	
	public void updateScripts() {
		File scriptDir = new File(Game.campaignEditor.getPath() + "/scripts");
		
		aiScripts.clear();
		File aiScriptDir = new File(Game.campaignEditor.getPath() + "/scripts/ai");
		for (File file : DirectoryListing.getFiles(aiScriptDir)) {
			String ref = FileUtil.getRelativePath(scriptDir, file);
			aiScripts.addElement(new Script(ref.substring(0, ref.length() - 3)));
		}
		
		conversationScripts.clear();
		File convoScriptDir = new File(Game.campaignEditor.getPath() + "/scripts/conversations");
		for (File file : DirectoryListing.getFiles(convoScriptDir)) {
			String ref = FileUtil.getRelativePath(scriptDir, file);
			conversationScripts.addElement(new Script(ref.substring(0, ref.length() - 3)));
		}
		
		itemScripts.clear();
		File itemScriptDir = new File(Game.campaignEditor.getPath() + "/scripts/items");
		for (File file : DirectoryListing.getFiles(itemScriptDir)) {
			String ref = FileUtil.getRelativePath(scriptDir, file);
			itemScripts.addElement(new Script(ref.substring(0, ref.length() - 3)));
		}
		
		questScripts.clear();
		File questScriptDir = new File(Game.campaignEditor.getPath() + "/scripts/quests");
		for (File file : DirectoryListing.getFiles(questScriptDir)) {
			String ref = FileUtil.getRelativePath(scriptDir, file);
			questScripts.addElement(new Script(ref.substring(0, ref.length() - 3)));
		}
		
		triggerScripts.clear();
		File triggerScriptDir = new File(Game.campaignEditor.getPath() + "/scripts/triggers");
		for (File file : DirectoryListing.getFiles(triggerScriptDir)) {
			String ref = FileUtil.getRelativePath(scriptDir, file);
			triggerScripts.addElement(new Script(ref.substring(0, ref.length() - 3)));
		}
	}
	
	// don't change child ordering based on keyboard focus
	@Override public void keyboardFocusChildChanged(Widget child) {
		if (child == menuBar || child == areaEditor) return;
		
		super.keyboardFocusChildChanged(child);
	}
	
	public class OpenAreaPopup extends PopupWindow {
		public OpenAreaPopup(Widget parent, String areaName) {
			super(parent);
			this.setTheme("");
			this.setCloseOnClickedOutside(false);
			
			Label title = new Label("Open " + areaName + ".");
			
			Label warning = new Label("Save your work in " + CampaignEditor.this.areaPath + "?");
			
			Button yes = new Button("Yes");
			yes.addCallback(new Runnable() {
				@Override public void run() {
					if (areaEditor != null) areaEditor.saveArea();
					openSelectedArea();
					closePopup();
				}
			});
			
			Button no = new Button("No");
			no.addCallback(new Runnable() {
				@Override public void run() {
					openSelectedArea();
					closePopup();
				}
			});
			
			Button cancel = new Button("Cancel");
			cancel.addCallback(new Runnable() {
				@Override public void run() {
					closePopup();
				}
			});
			
			DialogLayout content = new DialogLayout();
			content.setTheme("/filepopup");
			
			Group topH = content.createSequentialGroup();
			Group topV = content.createParallelGroup();
			
			topH.addGap(50);
			topH.addWidget(title);
			topV.addWidget(title);
			topH.addGap(50);
			
			Group mainH = content.createParallelGroup(topH);
			Group mainV = content.createSequentialGroup(topV);
			
			mainV.addGap(10);
			
			mainH.addWidget(warning);
			mainV.addWidget(warning);
			
			mainH.addGroup(content.createSequentialGroup(yes, no, cancel));
			mainV.addGroup(content.createParallelGroup(yes, no, cancel));
			
			content.setHorizontalGroup(mainH);
			content.setVerticalGroup(mainV);
			
			this.add(content);
		}
	}
}
