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

package net.sf.hale.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.editor.reference.MerchantReferenceList;
import net.sf.hale.editor.reference.ReferenceList;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.LineKeyList;
import net.sf.hale.util.Logger;
import net.sf.hale.util.SimpleJSONObject;

public class Merchant implements Referenceable, Saveable {
	private final LootList baseItems;
	private String name;
	private int buyValuePercentage;
	private int sellValuePercentage;
	private boolean usesSpeechSkill;
	private boolean confirmOnExit;
	private int currentBuyPercentage;
	private int currentSellPercentage;
	private int respawnHours;
	
	private final String id;
	private int lastRespawnRounds;
	private ItemList currentItems;
	
	@Override public Object save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		data.put("id", id);
		data.put("lastRespawnRound", lastRespawnRounds);
		
		data.put("currentItems", currentItems.save());
		
		return data;
	}
	
	public void load(SimpleJSONObject data) {
		this.lastRespawnRounds = data.get("lastRespawnRound", 0);
		
		this.currentItems = new ItemList("merchant");
		this.currentItems.load(data.getArray("currentItems"));
	}
	
	public Merchant(String id) {
		this.id = id;
		
		this.baseItems = new LootList();
		
		this.confirmOnExit = false;
		this.usesSpeechSkill = true;
		this.buyValuePercentage = 100;
		this.sellValuePercentage = 100;
		this.respawnHours = 0;
		
		readMerchantFile(id);
		
		this.currentBuyPercentage = buyValuePercentage;
		this.currentSellPercentage = sellValuePercentage;
	}
	
	public ItemList getCurrentItems() {
		return currentItems;
	}
	
	public int getRespawnHours() {
		return respawnHours;
	}
	
	public void setRespawnHours(int respawn) {
		this.respawnHours = respawn;
	}
	
	public LootList getBaseItems() { return baseItems; }
	
	public void setUsesSpeechSkill(boolean usesSpeechSkill) { this.usesSpeechSkill = usesSpeechSkill; }
	public boolean usesSpeechSkill() { return usesSpeechSkill; }
	
	public void setConfirmOnExit(boolean confirmOnExit) { this.confirmOnExit = confirmOnExit; }
	public boolean confirmOnExit() { return confirmOnExit; }

	private boolean checkRespawn() {
		if (currentItems == null) return true;
		
		if (respawnHours == 0) return false;
		
		int currentRound = Game.curCampaign.getDate().getTotalRoundsElapsed();
		int elapsedRounds = currentRound - this.lastRespawnRounds;
		
		return elapsedRounds >= respawnHours * Game.curCampaign.getDate().ROUNDS_PER_HOUR;
	}
	
	/**
	 * Gets the current list of items for this merchant, respawning if the respawn time has passed
	 * @return the current list of items for this merchant
	 */
	
	public ItemList updateCurrentItems() {
		if (checkRespawn()) {
			this.lastRespawnRounds = Game.curCampaign.getDate().getTotalRoundsElapsed();
			this.currentItems = baseItems.generate();
		}
		
		return currentItems;
	}
	
	@Override
	public String getID() { return id; }
	public String getName() { return name; }
	
	public void setName(String name) { this.name = name; }
	
	public void setPartySpeech(int partySpeech) {
		if (usesSpeechSkill) {
			double gapExponent = -1.0 * ((double)partySpeech) / ((double)Game.ruleset.getValue("BuySellGapSpeechExpFactor"));
			double gapPercentage = Math.exp(gapExponent);
			
			double base = (double)(sellValuePercentage - buyValuePercentage) / 2.0;
			
			int modifier = (int)Math.round(base * (1.0 - gapPercentage));
			
			
			this.currentBuyPercentage = buyValuePercentage + modifier;
			this.currentSellPercentage = sellValuePercentage - modifier;
		}
	}
	
	public void setBuyValuePercentage(int buyValuePercentage) { this.buyValuePercentage = buyValuePercentage; }
	public void setSellValuePercentage(int sellValuePercentage) { this.sellValuePercentage = sellValuePercentage; }
	
	public int getBuyValuePercentage() { return buyValuePercentage; }
	public int getSellValuePercentage() { return sellValuePercentage; }
	
	public int getCurrentBuyPercentage() { return currentBuyPercentage; }
	
	public int getCurrentSellPercentage() { return currentSellPercentage; }
	
	public void sellItem(Item item, Creature creature) {
		sellItem(item, creature, 1);
	}
	
	public void buyItem(Item item, Creature creature) {
		buyItem(item, creature, 1);
	}
	
	public void sellItem(Item item, Creature creature, int quantity) {
		int cost = Currency.getPlayerBuyCost(item, quantity, currentSellPercentage).getValue();
		
		if (Game.curCampaign.getPartyCurrency().getValue() < cost) return;
		
		Item soldItem = new Item(item);
		
		Game.curCampaign.getPartyCurrency().addCP(-cost);
		creature.getInventory().addItem(soldItem, quantity);
		
		currentItems.removeItem(item, quantity);
		
		Game.mainViewer.updateInterface();
	}
	
	public void buyItem(Item item, Creature creature, int quantity) {
		if (item.isQuestItem()) return;
		
		int cost = Currency.getPlayerSellCost(item, quantity, currentBuyPercentage).getValue();
		
		Game.curCampaign.getPartyCurrency().addCP(cost);
		creature.getInventory().getUnequippedItems().removeItem(item, quantity);
		currentItems.addItem(item, quantity);
		
		
		Game.mainViewer.updateInterface();
	}
	
	private void readMerchantFile(String ref) {
		FileKeyMap keyMap = new FileKeyMap("merchants/" + ref + ResourceType.Text.getExtension());
		
		if (keyMap.has("name")) name = keyMap.getLast("name").next();
		if (keyMap.has("sellvaluepercentage")) sellValuePercentage = keyMap.getLast("sellvaluepercentage").nextInt();
		if (keyMap.has("buyvaluepercentage")) buyValuePercentage = keyMap.getLast("buyvaluepercentage").nextInt();
		if (keyMap.has("usesspeechskill")) usesSpeechSkill = keyMap.getLast("usesspeechskill").nextBoolean();
		if (keyMap.has("confirmonexit")) confirmOnExit = keyMap.getLast("confirmonexit").nextBoolean();
		if (keyMap.has("respawnhours")) respawnHours = keyMap.getLast("respawnhours").nextInt();
		
		List<LootList.Entry> lootEntries = new ArrayList<LootList.Entry>();
		
		for (LineKeyList line : keyMap.get("additems")) {
			String itemListID = line.next();
			int probability = line.nextInt();
			LootList.ProbabilityMode mode = LootList.ProbabilityMode.valueOf(line.next().toUpperCase());
			
			lootEntries.add(new LootList.Entry(itemListID, probability, mode));
		}
		baseItems.addAll(lootEntries);
		
		keyMap.checkUnusedKeys();
	}
	
	public void saveToFile() {
		File fout = new File("campaigns/" + Game.curCampaign.getID() + "/merchants/" + id + ".txt");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			
			out.write("name \"" + name + "\"");
			out.newLine();
			
			out.write("usesSpeechSkill " + this.usesSpeechSkill);
			out.newLine();
			
			out.write("confirmOnExit " + this.confirmOnExit);
			out.newLine();
			
			out.write("buyValuePercentage " + this.buyValuePercentage);
			out.newLine();
			
			out.write("sellValuePercentage " + this.sellValuePercentage);
			out.newLine();
			
			out.write("respawnhours " + this.respawnHours);
			out.newLine();
			
			for (int i = 0; i < baseItems.size(); i++) {
				LootList.Entry entry = baseItems.getEntry(i);
				
				out.write("additems \"" + entry.itemListID + "\" " + entry.probability + " " + entry.mode.toString());
				out.newLine();
			}
			
			out.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error saving merchant " + id, e);
		}
	}
	
	@Override public String toString() {
		return id; 
	}
	
	@Override public String getReferenceType() {
		return "Merchant";
	}
	
	@Override public ReferenceList getReferenceList() {
		return new MerchantReferenceList(this);
	}
}
