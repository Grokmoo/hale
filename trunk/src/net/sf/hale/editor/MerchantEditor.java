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

import net.sf.hale.Game;
import net.sf.hale.Updateable;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.rules.LootList;
import net.sf.hale.rules.Merchant;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class MerchantEditor extends EditorWindow implements Updateable, PopupCallback {
	private Merchant selectedMerchant;
	
	private final Updateable parent;
	private final DialogLayout content;
	
	private final Label merchantsLabel;
	private final ListBox<Merchant> merchantsBox;
	
	private final Button saveMerchant, newMerchant, deleteMerchant, copyMerchant;
	
	private final DialogLayout merchantContent;
	
	private final Label selectedMerchantNameLabel;
	private final EditField selectedMerchantNameField;
	
	private final ToggleButton selectedMerchantUseSpeech;
	private final ToggleButton selectedMerchantConfirmExit;
	
	private final Label selectedMerchantBuyPercentageLabel;
	private final ValueAdjusterInt selectedMerchantBuyPercentageAdjuster;
	
	private final Label selectedMerchantSellPercentageLabel;
	private final ValueAdjusterInt selectedMerchantSellPercentageAdjuster;
	
	private final Label selectedMerchantRespawnLabel;
	private final ValueAdjusterInt selectedMerchantRespawnAdjuster;
	private final Label selectedMerchantRespawnHours;
	
	private final LootEditor baseItemsEditor;
	
	public MerchantEditor(Updateable parent) {
		super("Merchant Editor");
		
		this.parent = parent;
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		this.add(content);
		
		merchantsLabel = new Label("Select a merchant:");
		
		merchantsBox = new ReferenceListBox<Merchant>(Game.campaignEditor.getMerchantsModel());
		merchantsBox.setTheme("listboxnoexpand");
		merchantsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(ListBox.CallbackReason reason) {
				saveSelected();
				
				if (merchantsBox.getSelected() == -1) selectedMerchant = null;
				else selectedMerchant = merchantsBox.getModel().getEntry(merchantsBox.getSelected());
				
				updateSelectedMerchant();
			}
		});
		
		saveMerchant = new Button("Save");
		saveMerchant.addCallback(new Runnable() {
			@Override public void run() {
				saveSelected();
			}
		});
		
		newMerchant = new Button("New");
		newMerchant.addCallback(new Runnable() {
			@Override public void run() {
				NewMerchantPopup popup = new NewMerchantPopup(MerchantEditor.this);
				popup.setCallback(MerchantEditor.this);
				popup.openPopupCentered();
			}
		});
		
		copyMerchant = new Button("Copy");
		copyMerchant.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedMerchant == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(MerchantEditor.this, "merchants", selectedMerchant.getID());
				popup.setCallback(MerchantEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteMerchant = new Button("Delete");
		deleteMerchant.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedMerchant == null) return;
				
				DeleteFilePopup popup = new DeleteFilePopup(MerchantEditor.this,
						Game.campaignEditor.getPath() + "/merchants/" + selectedMerchant.getID() + ".txt", selectedMerchant);
				popup.setCallback(MerchantEditor.this);
				popup.openPopupCentered();
			}
		});
		
		merchantContent = new DialogLayout();
		merchantContent.setTheme("/editorlayout");
		
		selectedMerchantNameLabel = new Label("Name");
		
		selectedMerchantNameField = new EditField();
		selectedMerchantNameField.addCallback(new EditField.Callback() {
			@Override public void callback(int arg0) {
				selectedMerchant.setName(selectedMerchantNameField.getText());
			}
			
		});
		
		selectedMerchantUseSpeech = new ToggleButton("Use Speech Skill");
		selectedMerchantUseSpeech.addCallback(new Runnable() {
			@Override
			public void run() {
				selectedMerchant.setUsesSpeechSkill(selectedMerchantUseSpeech.isActive());
			}
		});
		
		selectedMerchantConfirmExit = new ToggleButton("Confirm on Exit");
		selectedMerchantConfirmExit.addCallback(new Runnable() {
			@Override public void run() {
				selectedMerchant.setConfirmOnExit(selectedMerchantConfirmExit.isActive());
			}
		});
		
		selectedMerchantBuyPercentageLabel = new Label("Buy Value Percentage");
		
		selectedMerchantBuyPercentageAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 50));
		selectedMerchantBuyPercentageAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedMerchant.setBuyValuePercentage(selectedMerchantBuyPercentageAdjuster.getValue());
			}
		});
		
		selectedMerchantSellPercentageLabel = new Label("Sell Value Percentage");
		
		selectedMerchantSellPercentageAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 300, 150));
		selectedMerchantSellPercentageAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedMerchant.setSellValuePercentage(selectedMerchantSellPercentageAdjuster.getValue());
			}
		});
		
		selectedMerchantRespawnLabel = new Label("Respawn every");
		selectedMerchantRespawnHours = new Label("hours (set to 0 for spawn only once)");
		selectedMerchantRespawnAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(0, 999, 0));
		selectedMerchantRespawnAdjuster.getModel().addCallback(new Runnable() {
			@Override public void run() {
				selectedMerchant.setRespawnHours(selectedMerchantRespawnAdjuster.getValue());
			}
		});
		
		baseItemsEditor = new LootEditor();
		baseItemsEditor.setDefaultProbability(100);
		baseItemsEditor.setDefaultMode(LootList.ProbabilityMode.PER_ITEM);
		
		Group rightH = merchantContent.createParallelGroup();
		Group rightV = merchantContent.createSequentialGroup();
		
		rightH.addGroup(merchantContent.createSequentialGroup(selectedMerchantNameLabel, selectedMerchantNameField));
		rightV.addGroup(merchantContent.createParallelGroup(selectedMerchantNameLabel, selectedMerchantNameField));
		
		rightH.addWidget(selectedMerchantUseSpeech);
		rightV.addWidget(selectedMerchantUseSpeech);
		
		rightH.addWidget(selectedMerchantConfirmExit);
		rightV.addWidget(selectedMerchantConfirmExit);
		
		rightH.addGroup(merchantContent.createSequentialGroup(selectedMerchantBuyPercentageLabel, selectedMerchantBuyPercentageAdjuster));
		rightV.addGroup(merchantContent.createParallelGroup(selectedMerchantBuyPercentageLabel, selectedMerchantBuyPercentageAdjuster));
		
		rightH.addGroup(merchantContent.createSequentialGroup(selectedMerchantSellPercentageLabel, selectedMerchantSellPercentageAdjuster));
		rightV.addGroup(merchantContent.createParallelGroup(selectedMerchantSellPercentageLabel, selectedMerchantSellPercentageAdjuster));
		
		rightH.addGroup(merchantContent.createSequentialGroup(selectedMerchantRespawnLabel, selectedMerchantRespawnAdjuster,
				selectedMerchantRespawnHours));
		rightV.addGroup(merchantContent.createParallelGroup(selectedMerchantRespawnLabel, selectedMerchantRespawnAdjuster,
				selectedMerchantRespawnHours));
		
		rightH.addWidget(baseItemsEditor);
		rightV.addWidget(baseItemsEditor);
		
		merchantContent.setHorizontalGroup(rightH);
		merchantContent.setVerticalGroup(rightV);
		
		Group leftH = content.createParallelGroup(merchantsLabel, merchantsBox);
		Group leftV = content.createSequentialGroup(merchantsLabel, merchantsBox);
		
		leftH.addGroup(content.createSequentialGroup(saveMerchant, newMerchant, deleteMerchant, copyMerchant));
		leftV.addGroup(content.createParallelGroup(saveMerchant, newMerchant, deleteMerchant, copyMerchant));
		
		Group mainH = content.createSequentialGroup(leftH);
		Group mainV = content.createParallelGroup(leftV);
		
		mainH.addWidget(merchantContent);
		mainV.addWidget(merchantContent);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) updateSelectedMerchant();
	}
	
	@Override public void saveSelected() {
		if (selectedMerchant != null) {
			selectedMerchant.saveToFile();
			Game.campaignEditor.updateStatusText("Merchant " + selectedMerchant.getName() + " saved.");
		}
	}
	
	@Override public void newComplete() {
		this.update();
	}

	@Override public void copyComplete() {
		this.update();
	}

	@Override public void deleteComplete() {
		this.selectedMerchant = null;
		
		this.update();
	}
	
	@Override public void update() {
		parent.update();
		updateSelectedMerchant();
	}
	
	public void updateSelectedMerchant() {
		merchantContent.setVisible(selectedMerchant != null);
		
		if (selectedMerchant == null) return;
		
		selectedMerchantNameField.setText(selectedMerchant.getName());
		
		selectedMerchantUseSpeech.setActive(selectedMerchant.usesSpeechSkill());
		selectedMerchantConfirmExit.setActive(selectedMerchant.confirmOnExit());
		
		selectedMerchantBuyPercentageAdjuster.setValue(selectedMerchant.getBuyValuePercentage());
		selectedMerchantSellPercentageAdjuster.setValue(selectedMerchant.getSellValuePercentage());
		
		selectedMerchantRespawnAdjuster.setValue(selectedMerchant.getRespawnHours());
		
		baseItemsEditor.setLoot(selectedMerchant.getBaseItems());
	}
}
