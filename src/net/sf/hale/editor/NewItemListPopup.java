package net.sf.hale.editor;

import net.sf.hale.Game;
import net.sf.hale.editor.widgets.NewFilePopup;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.rules.ItemList;

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class NewItemListPopup extends NewFilePopup {
	private final EditField idField;
	
	public NewItemListPopup(Widget parent) {
		super(parent, "Create a new Item List", 50);
		
		idField = new EditField();
		
		this.addWidgetsAsGroup(new Label("ID"), idField);
		this.addAcceptAndCancel();
	}

	@Override public String newFileAccept() {
		String name = idField.getText();
		
		if (name == null || name.length() == 0) {
			setError("Please enter a name.");
			return null;
		}
		
		for (int i = 0; i < Game.campaignEditor.getItemListsModel().getNumEntries(); i++) {
			ItemList itemList = Game.campaignEditor.getItemListsModel().getEntry(i);
			if (itemList.getID().equals(name)) {
				setError("An item list of this ID already exists.");
				return null;
			}
		}
		
		ItemList itemList = new ItemList(name);
		
		Game.campaignEditor.getItemListsModel().addElement(itemList);
		
		itemList.saveToDisk();
		
		Game.campaignEditor.updateStatusText("Item List " + name + " created.");
		
		return "itemLists/" + name + ResourceType.Text.getExtension();
	}
}
