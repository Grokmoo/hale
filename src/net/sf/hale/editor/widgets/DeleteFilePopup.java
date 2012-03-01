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

package net.sf.hale.editor.widgets;

import java.io.File;

import net.sf.hale.Game;
import net.sf.hale.editor.reference.Referenceable;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * A generic popup used for deleting a wide variety of file types
 * @author Jared Stephen
 *
 */

public class DeleteFilePopup extends PopupWindow implements Runnable {
	private final DialogLayout content;
	
	private final String path;
	private final Referenceable ref;
	
	private PopupCallback callback;
	
	/**
	 * Creates a new popup for deleting the file at the specified path.  If the
	 * specified referenceable has references remaining, will not allow deleting it
	 * @param parent the parent Widget to open this popup for
	 * @param path the file path for the file to delete
	 * @param referenceable the referenceable object being deleted
	 */
	
	public DeleteFilePopup(Widget parent, String path, Referenceable referenceable) {
		super(parent);
		
		this.setTheme("");
		this.setCloseOnClickedOutside(false);
		
		content = new DialogLayout();
		content.setTheme("/filepopup");
		this.add(content);
		
		this.path = path;
		this.ref = referenceable;
		
		// one reference is always the self reference
		int numRefs = ref.getReferenceList().getReferences().size() - 1;
		
		Label titleLabel = new Label();
		
		Label messageLabel = new Label();
		
		Button noButton = new Button("No");
		noButton.addCallback(new Runnable() {
			@Override public void run() {
				DeleteFilePopup.this.closePopup();
			}
		});
		
		Button yesButton = new Button("Yes");
		yesButton.addCallback(this);
		
		if (numRefs > 0) {
			titleLabel.setText("Cannot delete " + ref.getReferenceType() + " " + path + ".");
			messageLabel.setText("The resource has " + numRefs + " active references.");
			
			yesButton.setVisible(false);
			noButton.setText("OK");
		} else {
			titleLabel.setText("Delete " + ref.getReferenceType() + " " + path + "?");
		}
		
		Group mainH = content.createParallelGroup(titleLabel, messageLabel);
		Group mainV = content.createSequentialGroup(titleLabel, messageLabel);
		
		mainV.addGap(30);
		
		mainH.addGroup(content.createSequentialGroup(yesButton, noButton));
		mainV.addGroup(content.createParallelGroup(yesButton, noButton));
		
		content.setHorizontalGroup(mainH);
		content.setVerticalGroup(mainV);
	}
	
	public void setCallback(PopupCallback callback) {
		this.callback = callback;
	}
	
	@Override public void run() {
		try {
			File file = new File(path);
			file.delete();
		} catch (Exception e) {
			Logger.appendToErrorLog("Error deleting file " + path, e);
		}
		
		this.closePopup();
		Game.campaignEditor.updateStatusText(ref.getReferenceType() + " file " + path + " deleted.");
		
		String resource = FileUtil.getRelativePath(Game.campaignEditor.getCampaignPath(), path);
		ResourceManager.removeCampaignResource(resource);
		
		if (callback != null) callback.deleteComplete();
	}
}
