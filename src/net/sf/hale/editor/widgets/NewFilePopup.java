package net.sf.hale.editor.widgets;

import net.sf.hale.resource.ResourceManager;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

public abstract class NewFilePopup extends PopupWindow {
	private final DialogLayout content;
	
	private final Group mainH;
	private final Group mainV;
	
	private final Button accept, cancel;
	private final Label errorLabel;
	
	private PopupCallback callback;
	
	public NewFilePopup(Widget parent, String title, int addedWidth) {
		super(parent);
		
		this.setTheme("");
		this.setCloseOnClickedOutside(false);
		
		content = new DialogLayout();
		content.setTheme("/filepopup");
		this.add(content);
		
		Label titleLabel = new Label(title);
		titleLabel.setTheme("/titlelabel");
		
		accept = new Button("Create");
		accept.addCallback(new Runnable() {
			@Override public void run() {
				String newResource = newFileAccept();
				if (newResource != null) {
					ResourceManager.addCampaignResource(newResource);
					NewFilePopup.this.closePopup();
					if (callback != null) {
						callback.newComplete();
					}
				}
			}
		});
		
		cancel = new Button("Cancel");
		cancel.addCallback(new Runnable() {
			@Override public void run() {
				NewFilePopup.this.closePopup();
			}
		});
		
		errorLabel = new Label(" ");
		errorLabel.setTheme("/labelred");
		
		Group row1H = content.createSequentialGroup();
		row1H.addGap(addedWidth);
		row1H.addWidget(titleLabel);
		row1H.addGap(addedWidth);
		Group row1V = content.createParallelGroup(titleLabel);
		
		mainH = content.createParallelGroup(row1H);
		mainV = content.createSequentialGroup(row1V);
		
		mainV.addGap(10);
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	public void setCallback(PopupCallback callback) {
		this.callback = callback;
	}
	
	public void addGap(int gap) {
		mainV.addGap(gap);
	}
	
	public void addWidget(Widget widget) {
		mainH.addWidget(widget);
		mainV.addWidget(widget);
	}
	
	public void addWidgetsAsGroup(Widget... widgets) {
		mainH.addGroup(content.createSequentialGroup(widgets));
		mainV.addGroup(content.createParallelGroup(widgets));
	}
	
	protected void addAcceptAndCancel() {
		mainV.addGap(10);
		
		mainH.addGroup(content.createSequentialGroup(accept, cancel));
		mainV.addGroup(content.createParallelGroup(accept, cancel));
		
		mainH.addWidget(errorLabel);
		mainV.addWidget(errorLabel);
	}
	
	public void setError(String text) {
		errorLabel.setText(text);
	}
	
	public abstract String newFileAccept();
}
