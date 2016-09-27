package net.sf.hale.view;

import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import net.sf.hale.Game;

/**
 * The sub window showing the contents of the message log in expanded form from the always present message box
 * @author Jared
 *
 */

public class MessagesWindow extends GameSubWindow {
	private final TextArea textArea;
	private final ScrollPane scrollPane;
	
	public MessagesWindow() {
		this.setTitle("Messages");
		
		textArea = new TextArea();
		scrollPane = new ScrollPane(textArea);
		scrollPane.setTheme("messagespane");
		scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		add(scrollPane);
	}
	
	@Override protected void layout() {
		super.layout();
		
		scrollPane.setSize(getInnerWidth(), getInnerHeight());
		scrollPane.setPosition(getInnerX(), getInnerY());
	}
	
	public void updateContent() {
		textArea.setModel(Game.mainViewer.getMessageBoxModel());
	}
}
