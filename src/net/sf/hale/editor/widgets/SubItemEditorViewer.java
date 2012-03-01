package net.sf.hale.editor.widgets;

import net.sf.hale.entity.Item;

import de.matthiasmann.twl.Widget;

public class SubItemEditorViewer extends Widget {
	private SubItemEditor activeEditor;
	
	public SubItemEditorViewer() {
		super();
		this.setTheme("");
	}
	
	public void setActiveEditor(SubItemEditor editor) {
		this.activeEditor = editor;
		
		this.removeAllChildren();
		
		if (activeEditor != null) {
			activeEditor.setSize(this.getWidth(), this.getHeight());
			super.add(activeEditor);
		}
	}
	
	@Override public int getMinHeight() {
		return 280;
	}
	
	@Override public int getMinWidth() {
		return 420;
	}
	
	@Override public int getPreferredWidth() {
		return Short.MAX_VALUE;
	}
	
	@Override public int getPreferredHeight() {
		return Short.MAX_VALUE;
	}
	
	public void getPropertiesFromItem(Item item) {
		if (activeEditor != null) {
			activeEditor.getPropertiesFromItem(item);
		}
	}
	
	@Override public void add(Widget widget) {
		throw new UnsupportedOperationException("Use setActiveEditor instead.");
	}
	
	@Override public boolean setSize(int width, int height) {
		if (activeEditor != null) {
			activeEditor.setSize(width, height);
		}
		
		return super.setSize(width, height);
	}
}
