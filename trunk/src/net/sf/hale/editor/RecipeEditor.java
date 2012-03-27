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
import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.Recipe;
import net.sf.hale.editor.reference.ReferenceComboBox;
import net.sf.hale.editor.reference.ReferenceListBox;
import net.sf.hale.editor.widgets.CopyFilePopup;
import net.sf.hale.editor.widgets.DeleteFilePopup;
import net.sf.hale.editor.widgets.PopupCallback;
import net.sf.hale.entity.Item;
import net.sf.hale.rules.ItemList;
import net.sf.hale.rules.ItemQuality;
import net.sf.hale.rules.Skill;
import net.sf.hale.util.DirectoryListing;
import net.sf.hale.widgets.ExpandableScrollPane;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class RecipeEditor extends EditorWindow implements Updateable, PopupCallback, ItemListEntryPane.Callback {
	
	private Recipe selectedRecipe;
	
	private final DialogLayout content;
	
	private final Label recipesLabel;
	private final ListBox<Recipe> recipesBox;
	private final SimpleChangableListModel<Recipe> recipesModel;
	
	private final Button newRecipe;
	private final Button deleteRecipe;
	private final Button copyRecipe;
	
	private final DialogLayout selectedRecipeContent;
	
	private final Button saveRecipe;
	
	private final Label selectedRecipeNameLabel;
	private final EditField selectedRecipeNameField;
	
	private final Label selectedRecipeDescriptionLabel;
	private final EditField selectedRecipeDescriptionField;
	
	private final Label selectedRecipeSkillLabel;
	private final ComboBox<String> selectedRecipeSkillBox;
	private final SimpleChangableListModel<String> selectedRecipeSkillModel;
	
	private final Label selectedRecipeRanksLabel;
	private final ValueAdjusterInt selectedRecipeRanksAdjuster;
	
	private final Label selectedRecipeResultLabel;
	private final ComboBox<Item> selectedRecipeResultBox;
	private final ValueAdjusterInt selectedRecipeResultQuantityAdjuster;
	private final ToggleButton resultIsIngredientButton;
	
	private final Label selectedRecipeIngredientsLabel;
	private final ItemListBox selectedRecipePotentialIngredientsBox;
	
	private final Label anyItemOfTypeLabel;
	private final ComboBox<String> anyItemOfTypeBox;
	private final SimpleChangableListModel<String> anyItemOfTypeModel;
	
	private final ScrollPane selectedRecipeIngredientsPane;
	private final DialogLayout selectedRecipeIngredientsContent;
	private final Button selectedRecipeAddIngredientButton;
	
	private final ToggleButton selectedRecipeHasQualityButton;
	
	private final Label selectedRecipeQualityLabel;
	private final Label selectedRecipeSkillModifiersLabel;
	private final Label selectedRecipeEnchantmentLabel;
	private final List<Label> selectedRecipeModifierLabels;
	private final List<SkillModifierAdjuster> selectedRecipeModifierAdjusters;
	private final List<EnchantmentField> selectedRecipeEnchantmentFields;
	
	public RecipeEditor(Updateable updateable) {
		super("Recipe Editor");
		
		content = new DialogLayout();
		content.setTheme("/editorlayout");
		this.add(content);
		
		recipesLabel = new Label("Select a recipe:");
		
		recipesBox = new ReferenceListBox<Recipe>();
		recipesModel = new SimpleChangableListModel<Recipe>();
		recipesBox.setModel(recipesModel);
		recipesBox.setTheme("listboxnoexpand");
		recipesBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(CallbackReason reason) {
				saveSelected();
				
				if (recipesBox.getSelected() == -1) selectedRecipe = null;
				else selectedRecipe = recipesModel.getEntry(recipesBox.getSelected());
				
				updateSelectedRecipe();
			}
		});
		
		newRecipe = new Button("New");
		newRecipe.addCallback(new Runnable() {
			@Override public void run() {
				NewRecipePopup popup = new NewRecipePopup(RecipeEditor.this);
				popup.setCallback(RecipeEditor.this);
				popup.openPopupCentered();
			}
		});
		
		copyRecipe = new Button("Copy");
		copyRecipe.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedRecipe == null) return;
				
				CopyFilePopup popup = new CopyFilePopup(RecipeEditor.this, "recipes", selectedRecipe.getID());
				popup.setCallback(RecipeEditor.this);
				popup.openPopupCentered();
			}
		});
		
		deleteRecipe = new Button("Delete");
		deleteRecipe.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedRecipe == null) return;
				
				DeleteFilePopup popup = new DeleteFilePopup(RecipeEditor.this,
						Game.campaignEditor.getPath() + "/recipes/" + selectedRecipe.getID() + ".txt", selectedRecipe);
				popup.setCallback(RecipeEditor.this);
				popup.openPopupCentered();
			}
		});
		
		selectedRecipeContent = new DialogLayout();
		selectedRecipeContent.setTheme("/editorlayout");
		
		saveRecipe = new Button("Save");
		saveRecipe.addCallback(new Runnable() {
			@Override public void run() {
				saveSelected();
			}
		});
		
		selectedRecipeNameLabel = new Label("Name");
		
		selectedRecipeNameField = new EditField();
		selectedRecipeNameField.addCallback(new EditField.Callback() {
			@Override public void callback(int arg0) {
				selectedRecipe.setName(selectedRecipeNameField.getText());
			}
		});
		
		selectedRecipeDescriptionLabel = new Label("Description");
		
		selectedRecipeDescriptionField = new EditField();
		selectedRecipeDescriptionField.addCallback(new EditField.Callback() {
			@Override public void callback(int key) {
				selectedRecipe.setDescription(selectedRecipeDescriptionField.getText());
			}
		});
		
		selectedRecipeSkillLabel = new Label("Skill:");
		
		selectedRecipeSkillModel = new SimpleChangableListModel<String>();
		for (Skill skill : Game.ruleset.getAllSkills()) {
			if (skill.isCraftSkill()) selectedRecipeSkillModel.addElement(skill.getID());
		}
		
		selectedRecipeSkillBox = new ComboBox<String>(selectedRecipeSkillModel);
		selectedRecipeSkillBox.setTheme("mediumcombobox");
		selectedRecipeSkillBox.addCallback(new Runnable() {
			@Override public void run() {
				int index = selectedRecipeSkillBox.getSelected();
				if (index == -1) return;
				
				String skillID = selectedRecipeSkillModel.getEntry(index);
				selectedRecipe.setSkill(skillID);
			}
		});
		
		selectedRecipeRanksLabel = new Label("Ranks required");
		
		selectedRecipeRanksAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		selectedRecipeRanksAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				selectedRecipe.setSkillRequirement(selectedRecipeRanksAdjuster.getValue());
			}
		});
		
		selectedRecipeResultLabel = new Label("Result: ");
		
		selectedRecipeResultBox = new ReferenceComboBox<Item>(Game.campaignEditor.getItemsModel());
		selectedRecipeResultBox.setTheme("mediumcombobox");
		selectedRecipeResultBox.addCallback(new Runnable() {
			@Override public void run() {
				if (selectedRecipeResultBox.getSelected() != -1) {
					String result = Game.campaignEditor.getItemsModel().getEntry(selectedRecipeResultBox.getSelected()).getID();
					selectedRecipe.setResult(result);
				}
			}
		});
		
		selectedRecipeResultQuantityAdjuster = new ValueAdjusterInt(new SimpleIntegerModel(1, 999, 1));
		selectedRecipeResultQuantityAdjuster.getModel().addCallback(new Runnable() {
			@Override
			public void run() {
				if (selectedRecipeResultBox.getSelected() == -1) return;
				
				selectedRecipe.setResultQuantity(selectedRecipeResultQuantityAdjuster.getValue());
			}
		});
		
		resultIsIngredientButton = new ToggleButton("Result is Ingredient");
		resultIsIngredientButton.addCallback(new Runnable() {
			@Override public void run() {
				selectedRecipe.setResultIsIngredient(resultIsIngredientButton.isActive());
				
				selectedRecipeResultBox.setVisible(!selectedRecipe.resultIsIngredient());
				selectedRecipeResultQuantityAdjuster.setVisible(!selectedRecipe.resultIsIngredient());
			}
		});
		
		selectedRecipePotentialIngredientsBox = new ItemListBox(Game.campaignEditor.getItemsModel(), "All Items");
		selectedRecipePotentialIngredientsBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
			@Override public void callback(ListBox.CallbackReason reason) {
				int index = selectedRecipePotentialIngredientsBox.getSelected();
				if (index != -1) {
					selectedRecipeAddIngredientButton.setEnabled(true);
				}
				
				switch (reason) {
				case MOUSE_DOUBLE_CLICK:
					addSelectedIngredient();
				}
			}
		});
		
		selectedRecipeIngredientsLabel = new Label("Ingredients");
		
		anyItemOfTypeLabel = new Label("Any Item of Type:");
		
		anyItemOfTypeModel = new SimpleChangableListModel<String>();
		anyItemOfTypeModel.addElement("<None>");
		for (Item.ItemType type : Item.ItemType.values()) {
			anyItemOfTypeModel.addElement(type.toString());
		}
		anyItemOfTypeBox = new ComboBox<String>(anyItemOfTypeModel);
		anyItemOfTypeBox.addCallback(new Runnable() {
			@Override
			public void run() {
				int index = anyItemOfTypeBox.getSelected();
				if (index == 0) selectedRecipe.setAnyItemOfTypeIngredient(null);
				else {
					String selected = anyItemOfTypeModel.getEntry(index);
					Item.ItemType type = Item.ItemType.valueOf(selected);
					selectedRecipe.setAnyItemOfTypeIngredient(type);
				}
			}
		});
		
		selectedRecipeIngredientsContent = new DialogLayout();
		selectedRecipeIngredientsContent.setTheme("/editorlayout");
		selectedRecipeIngredientsContent.setHorizontalGroup(selectedRecipeIngredientsContent.createParallelGroup());
		selectedRecipeIngredientsContent.setVerticalGroup(selectedRecipeIngredientsContent.createSequentialGroup());
		
		selectedRecipeIngredientsPane = new ExpandableScrollPane(selectedRecipeIngredientsContent);
		selectedRecipeIngredientsPane.setTheme("scrollpane");
		
		selectedRecipeAddIngredientButton = new Button("Add");
		selectedRecipeAddIngredientButton.addCallback(new Runnable() {
			@Override public void run() {
				addSelectedIngredient();
			}
		});
		
		selectedRecipeHasQualityButton = new ToggleButton("Result has quality");
		selectedRecipeHasQualityButton.addCallback(new Runnable() {
			@Override public void run() {
				selectedRecipe.setResultHasQuality(selectedRecipeHasQualityButton.isActive());
			}
		});
		
		selectedRecipeQualityLabel = new Label("Quality");
		selectedRecipeQualityLabel.setTheme("/titlelabel");
		
		selectedRecipeSkillModifiersLabel = new Label("Skill Requirement");
		selectedRecipeSkillModifiersLabel.setTheme("/titlelabel");
		
		selectedRecipeEnchantmentLabel = new Label("Enchantment");
		selectedRecipeEnchantmentLabel.setTheme("/titlelabel");
		
		selectedRecipeModifierLabels = new ArrayList<Label>();
		selectedRecipeModifierAdjusters = new ArrayList<SkillModifierAdjuster>();
		selectedRecipeEnchantmentFields = new ArrayList<EnchantmentField>();
		
		Group rightH = selectedRecipeContent.createParallelGroup();
		Group rightV = selectedRecipeContent.createSequentialGroup();
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup(selectedRecipeNameLabel, selectedRecipeNameField));
		rightV.addGroup(selectedRecipeContent.createParallelGroup(selectedRecipeNameLabel, selectedRecipeNameField));
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup(selectedRecipeDescriptionLabel, selectedRecipeDescriptionField));
		rightV.addGroup(selectedRecipeContent.createParallelGroup(selectedRecipeDescriptionLabel, selectedRecipeDescriptionField));
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup(selectedRecipeSkillLabel, selectedRecipeSkillBox,
				selectedRecipeRanksLabel, selectedRecipeRanksAdjuster));
		rightV.addGroup(selectedRecipeContent.createParallelGroup(selectedRecipeSkillLabel, selectedRecipeSkillBox,
				selectedRecipeRanksLabel, selectedRecipeRanksAdjuster));
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup(selectedRecipeResultLabel,
				selectedRecipeResultBox, selectedRecipeResultQuantityAdjuster));
		rightV.addGroup(selectedRecipeContent.createParallelGroup(selectedRecipeResultLabel,
				selectedRecipeResultBox, selectedRecipeResultQuantityAdjuster));
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup(resultIsIngredientButton, selectedRecipeHasQualityButton));
		rightV.addGroup(selectedRecipeContent.createParallelGroup(resultIsIngredientButton, selectedRecipeHasQualityButton));
		
		Group centerLeftH = selectedRecipeContent.createParallelGroup(selectedRecipePotentialIngredientsBox,
				selectedRecipeAddIngredientButton);
		Group centerLeftV = selectedRecipeContent.createSequentialGroup(selectedRecipePotentialIngredientsBox,
				selectedRecipeAddIngredientButton);
		
		Group centerRightH = selectedRecipeContent.createParallelGroup(selectedRecipeIngredientsLabel);
		Group centerRightV = selectedRecipeContent.createSequentialGroup(selectedRecipeIngredientsLabel);
		
		centerRightH.addGroup(selectedRecipeContent.createSequentialGroup(anyItemOfTypeLabel, anyItemOfTypeBox));
		centerRightV.addGroup(selectedRecipeContent.createParallelGroup(anyItemOfTypeLabel, anyItemOfTypeBox));
		
		centerRightH.addWidget(selectedRecipeIngredientsPane);
		centerRightV.addWidget(selectedRecipeIngredientsPane);
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup(centerLeftH, centerRightH));
		rightV.addGroup(selectedRecipeContent.createParallelGroup(centerLeftV, centerRightV));
		
		Group rowLabelsH = selectedRecipeContent.createSequentialGroup(selectedRecipeQualityLabel);
		rowLabelsH.addGap(40);
		rowLabelsH.addWidget(selectedRecipeSkillModifiersLabel);
		rowLabelsH.addGap(40);
		rowLabelsH.addWidget(selectedRecipeEnchantmentLabel);
		
		Group rowLabelsV = selectedRecipeContent.createParallelGroup(selectedRecipeQualityLabel,
				selectedRecipeSkillModifiersLabel, selectedRecipeEnchantmentLabel);
		
		rightH.addGroup(rowLabelsH);
		rightV.addGroup(rowLabelsV);
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup());
		rightV.addGroup(selectedRecipeContent.createParallelGroup());
		
		Group labelsH = selectedRecipeContent.createParallelGroup();
		Group labelsV = selectedRecipeContent.createSequentialGroup();
		
		Group adjustersH = selectedRecipeContent.createParallelGroup();
		Group adjustersV = selectedRecipeContent.createSequentialGroup();
		
		Group fieldsH = selectedRecipeContent.createParallelGroup();
		Group fieldsV = selectedRecipeContent.createSequentialGroup();
		
		for (int i = 0; i < Game.ruleset.getNumItemQualities(); i++) {
			ItemQuality quality = Game.ruleset.getItemQuality(i);
			
			Label label = new Label(quality.getName());
			
			SkillModifierAdjuster adjuster = new SkillModifierAdjuster(i);
			
			EnchantmentField field = new EnchantmentField(i);
			
			selectedRecipeModifierLabels.add(label);
			selectedRecipeModifierAdjusters.add(adjuster);
			selectedRecipeEnchantmentFields.add(field);
			
			labelsH.addWidget(label);
			labelsV.addWidget(label);
			labelsV.addGap(11);
			
			adjustersH.addWidget(adjuster);
			adjustersV.addWidget(adjuster);
			
			fieldsH.addWidget(field);
			fieldsV.addWidget(field);
		}
		
		rightH.addGroup(selectedRecipeContent.createSequentialGroup(labelsH, adjustersH, fieldsH));
		rightV.addGroup(selectedRecipeContent.createParallelGroup(labelsV, adjustersV, fieldsV));
		
		selectedRecipeContent.setHorizontalGroup(rightH);
		selectedRecipeContent.setVerticalGroup(rightV);
		
		Group leftH = content.createParallelGroup(recipesLabel, recipesBox);
		Group leftV = content.createSequentialGroup(recipesLabel, recipesBox);
		
		leftH.addGroup(content.createSequentialGroup(saveRecipe, newRecipe, deleteRecipe, copyRecipe));
		leftV.addGroup(content.createParallelGroup(saveRecipe, newRecipe, deleteRecipe, copyRecipe));
		
		Group mainH = content.createSequentialGroup(leftH);
		Group mainV = content.createParallelGroup(leftV);
		
		mainH.addWidget(selectedRecipeContent);
		mainV.addWidget(selectedRecipeContent);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
		
	}
	
	public void addSelectedIngredient() {
		int index = selectedRecipePotentialIngredientsBox.getSelected();
		if (index != -1) {
			String item = Game.campaignEditor.getItemsModel().getEntry(index).getID();
			selectedRecipe.addIngredient(item);
			updateSelectedIngredient();
		}
	}
	
	@Override public void saveSelected() {
		if (selectedRecipe == null) return;
		
		selectedRecipe.saveToFile();
		Game.campaignEditor.updateStatusText("Recipe " + selectedRecipe.getID() + " saved.");
	}
	
	@Override public void update() {
		populateRecipesList();
	}
	
	@Override public void entryEquippedOffHand(int index) { }
	@Override public void entryEquipped(int index) { }
	
	@Override public void entryRemoved(int index) {
		selectedRecipe.removeIngredient(index);
		updateSelectedIngredient();
	}
	
	public void updateSelectedIngredient() {
		selectedRecipeIngredientsContent.removeAllChildren();
		
		Group mainH = selectedRecipeIngredientsContent.createParallelGroup();
		Group mainV = selectedRecipeIngredientsContent.createSequentialGroup();
		
		ItemList ingredients = selectedRecipe.getIngredients();
		for (int i = 0; i < ingredients.size(); i++) {
			ItemListEntryPane pane = new ItemListEntryPane(ingredients, i, null, false);
			pane.addCallback(this);
			
			mainH.addWidget(pane);
			mainV.addWidget(pane);
		}
		
		selectedRecipeIngredientsContent.setHorizontalGroup(mainH);
		selectedRecipeIngredientsContent.setVerticalGroup(mainV);
	}
	
	public void updateSelectedRecipe() {
		selectedRecipeContent.setVisible(selectedRecipe != null);
		
		if (selectedRecipe == null) return;
		
		if (selectedRecipe.getName() != null)
			selectedRecipeNameField.setText(selectedRecipe.getName());
		else
			selectedRecipeNameField.setText("");
		
		if (selectedRecipe.getDescription() != null)
			selectedRecipeDescriptionField.setText(selectedRecipe.getDescription());
		else
			selectedRecipeDescriptionField.setText("");
		
		String skillID = selectedRecipe.getSkill();
		if (skillID != null) {
			int index = selectedRecipeSkillModel.findElement(skillID);
			selectedRecipeSkillBox.setSelected(index);
		} else {
			selectedRecipeSkillBox.setSelected(-1);
		}
		
		selectedRecipeHasQualityButton.setActive(selectedRecipe.resultHasQuality());
		
		int ranks = selectedRecipe.getSkillRequirement();
		selectedRecipeRanksAdjuster.setValue(ranks);
		
		if (selectedRecipe.getAnyItemOfTypeIngredient() == null) {
			anyItemOfTypeBox.setSelected(0);
		} else {
			String typeString = selectedRecipe.getAnyItemOfTypeIngredient().toString();
			anyItemOfTypeBox.setSelected(anyItemOfTypeModel.findElement(typeString));
		}
		
		String result = selectedRecipe.getResult();
		selectedRecipeResultBox.setSelected(-1);
		if (result != null) {
			for (int i = 0; i < Game.campaignEditor.getItemsModel().getNumEntries(); i++) {
				Item item = Game.campaignEditor.getItemsModel().getEntry(i);
				if (item.getID().equals(result)) {
					selectedRecipeResultBox.setSelected(i);
					break;
				}
			}
		}
		
		selectedRecipeResultQuantityAdjuster.setValue(selectedRecipe.getResultQuantity());
		
		resultIsIngredientButton.setActive(selectedRecipe.resultIsIngredient());
		selectedRecipeResultBox.setVisible(!selectedRecipe.resultIsIngredient());
		selectedRecipeResultQuantityAdjuster.setVisible(!selectedRecipe.resultIsIngredient());
		
		selectedRecipeAddIngredientButton.setEnabled(false);
		
		updateSelectedIngredient();
		
		for (int i = 0; i < selectedRecipe.getNumQualityModifiers(); i++) {
			selectedRecipeModifierAdjusters.get(i).setValue(selectedRecipe.getQualityModifier(i));
			selectedRecipeEnchantmentFields.get(i).setText(selectedRecipe.getEnchantment(i));
		}
		
		this.selectedRecipePotentialIngredientsBox.resetView();
	}
	
	public void populateRecipesList() {
		recipesModel.clear();
		
		File dir = new File(Game.campaignEditor.getPath() + "/recipes");
		
		List<File> files = DirectoryListing.getFiles(dir);
		for (File file : files) {
			String ref = file.getName().substring(0, file.getName().length() - 4);
			
			recipesModel.addElement(Game.curCampaign.getRecipe(ref));
		}
		
		updateSelectedRecipe();
	}
	
	@Override public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) populateRecipesList();
	}
	
	@Override public void deleteComplete() {
		this.selectedRecipe = null;
		
		update();
	}
	
	@Override public void copyComplete() {
		update();
	}
	
	@Override public void newComplete() {
		update();
	}
	
	private class EnchantmentField extends EditField implements EditField.Callback {
		private final int index;
		
		public EnchantmentField(int index) {
			super();
			
			this.index = index;
			this.setTheme("/editfield");
			this.setSize(230, 15);
			this.addCallback(this);
		}

		@Override public void callback(int arg0) {
			if (selectedRecipe == null) return;
			
			selectedRecipe.setEnchantment(index, EnchantmentField.this.getText());
		}
	}
	
	private class SkillModifierAdjuster extends ValueAdjusterInt implements Runnable {
		private final int index;
		
		public SkillModifierAdjuster(int index) {
			super(new SimpleIntegerModel(0, 999, 0));
			
			this.setSize(70, 20);
			this.setTheme("/valueadjuster");
			this.index = index;
			this.getModel().addCallback(this);
		}
		
		@Override
		public void run() {
			if (selectedRecipe == null) return;
			
			selectedRecipe.setQualityModifier(index, SkillModifierAdjuster.this.getValue());
		}
	}
}
