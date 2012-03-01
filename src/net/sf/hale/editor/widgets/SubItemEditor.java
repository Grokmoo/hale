package net.sf.hale.editor.widgets;

import net.sf.hale.entity.Item;

import de.matthiasmann.twl.Widget;

public abstract class SubItemEditor extends Widget {
	public abstract void getPropertiesFromItem(Item item);
}
