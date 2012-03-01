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

package net.sf.hale.mainmenu;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.hale.Game;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.FileKeyMap;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * The popup window for selecting the current campaign
 * @author Jared Stephen
 *
 */

public class CampaignPopup extends PopupWindow {
	private CampaignSelector selected;
	
	private Content content;
	private MainMenu mainMenu;
	
	/**
	 * Create a new CampaignPopup with the specified MainMenu as the parent Widget
	 * @param mainMenu the parent Widget
	 */
	
	public CampaignPopup(MainMenu mainMenu) {
		super(mainMenu);
		this.mainMenu = mainMenu;
		
		content = new Content();
		add(content);
	}
	
	private class Content extends Widget {
		private Label title;
		private ScrollPane selectorPane;
		private DialogLayout paneContent;
		private Button accept, cancel;
		
		private HTMLTextAreaModel textAreaModel;
		private ScrollPane textPane;
		
		private int acceptCancelGap;
		
		private Content() {
			title = new Label();
			title.setTheme("titlelabel");
			add(title);
			
			textAreaModel = new HTMLTextAreaModel();
			TextArea textArea = new TextArea(textAreaModel);
	        textPane = new ScrollPane(textArea);
	        textPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
	        textPane.setCanAcceptKeyboardFocus(false);
	        textPane.setTheme("descriptionpane");
	        add(textPane);
			
			paneContent = new DialogLayout();
			paneContent.setTheme("content");
			selectorPane = new ScrollPane(paneContent);
			selectorPane.setTheme("selectorpane");
			add(selectorPane);
			
			accept = new Button();
			accept.setTheme("acceptbutton");
			accept.addCallback(new Runnable() {
				@Override public void run() {
					mainMenu.loadCampaign(selected.id);
					mainMenu.update();
					CampaignPopup.this.closePopup();
				}
			});
			accept.setEnabled(false);
			add(accept);
			
			cancel = new Button();
			cancel.setTheme("cancelbutton");
			cancel.addCallback(new Runnable() {
				@Override public void run() {
					CampaignPopup.this.closePopup();
				}
			});
			add(cancel);
			
			DialogLayout.Group mainH = paneContent.createParallelGroup();
			DialogLayout.Group mainV = paneContent.createSequentialGroup();
			
			// add available campaigns to pane Content
			Map<String, CampaignDescriptor> campaigns = CampaignPopup.getAvailableCampaigns();
			for (String id : campaigns.keySet()) {
				CampaignDescriptor descriptor = campaigns.get(id);
				
				CampaignSelector selector = new CampaignSelector(id, descriptor.name, descriptor.description);
				mainH.addWidget(selector);
				mainV.addWidget(selector);
				
				// if we found the current campaign in the list of selections,
				// select that campaign
				if (Game.curCampaign != null && id.equals(Game.curCampaign.getID()) ) {
					selected = selector;
					textAreaModel.setHtml(selected.description);
					selected.setActive(true);
					accept.setEnabled(true);
				}
			}
			
			paneContent.setHorizontalGroup(mainH);
			paneContent.setVerticalGroup(mainV);
		}
		
		@Override protected void applyTheme(ThemeInfo themeInfo) {
			super.applyTheme(themeInfo);
			
			acceptCancelGap = themeInfo.getParameter("acceptCancelGap", 0);
		}
		
		@Override protected void layout() {
			super.layout();
			
			int centerX = getInnerX() + getWidth() / 2;
			
			title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
			accept.setSize(accept.getPreferredWidth(), accept.getPreferredHeight());
			cancel.setSize(cancel.getPreferredWidth(), cancel.getPreferredHeight());
			
			title.setPosition(centerX - title.getWidth() / 2, getInnerY());
			
			accept.setPosition(centerX - acceptCancelGap - accept.getWidth(),
					getInnerBottom() - accept.getHeight());
			
			cancel.setPosition(centerX + acceptCancelGap, getInnerBottom() - cancel.getHeight());
			
			int paneBottom = Math.min(accept.getY(), cancel.getY());
			
			selectorPane.setPosition(getInnerX(), title.getBottom());
			selectorPane.setSize(selectorPane.getPreferredWidth(), paneBottom - title.getBottom());
			
			textPane.setPosition(selectorPane.getRight(), title.getBottom());
			textPane.setSize(getInnerRight() - textPane.getX(), paneBottom - title.getBottom());
		}
	}
	
	private class CampaignSelector extends ToggleButton implements Runnable {
		private String id;
		private String description;
		
		private CampaignSelector(String id, String name, String description) {
			super(name);
			
			this.id = id;
			this.description = description;
			
			addCallback(this);
		}
		
		@Override public void run() {
			if (selected != null) {
				selected.setActive(false);
			}
			
			setActive(true);
			selected = this;
			
			CampaignPopup.this.content.textAreaModel.setHtml(description);
			CampaignPopup.this.content.textPane.invalidateLayout();
			
			CampaignPopup.this.content.accept.setEnabled(true);
		}
		
		@Override public int getPreferredWidth() {
			return CampaignPopup.this.content.selectorPane.getInnerWidth();
		}
	}
	
	/**
	 * Returns a Map of all available (loadable) campaigns.  The keySet is the set of available
	 * campaign IDs.  Each key is mapped to the campaign description file contents.
	 * @return the Map of all available campaigns
	 */
	
	public static Map<String, CampaignDescriptor> getAvailableCampaigns() {
		Map<String, CampaignDescriptor> campaigns = new LinkedHashMap<String, CampaignDescriptor>();
		
		File campaignDir = new File("campaigns");
		String[] fileList = campaignDir.list();
		
		try {
			// first, add all extracted directories
			for (String fileName : fileList) {
				File f = new File("campaigns/" + fileName);

				if (!f.isDirectory() || f.getName().startsWith(".")) continue;

				CampaignDescriptor descriptor = new CampaignDescriptor();
				descriptor.description = FileUtil.readFileAsString(f.getPath() + "/description" +
						ResourceType.HTML.getExtension());
				
				FileKeyMap fileMap = new FileKeyMap(new File(f.getPath() + "/campaign.txt"));
				descriptor.name = fileMap.getValue("name", fileName);
				
				campaigns.put( fileName, descriptor );
			}

			// now, look for zip archives.
			for (String fileName : fileList) {
				File f = new File("campaigns/" + fileName);

				if (!f.isFile() || !f.getName().endsWith(ResourceType.Zip.getExtension())) continue;

				String id = ResourceManager.getResourceID(f.getName(), ResourceType.Zip);

				if (campaigns.containsKey(id)) continue;

				CampaignDescriptor descriptor = new CampaignDescriptor();
				
				ZipFile file = new ZipFile(f);
				ZipEntry entry = file.getEntry("description" + ResourceType.HTML.getExtension());

				descriptor.description = ResourceManager.getResourceAsString(file.getInputStream(entry));
				
				FileKeyMap fileMap = new FileKeyMap( file.getInputStream(file.getEntry("campaign.txt")) );
				descriptor.name = fileMap.getValue("name", id);
				
				campaigns.put(id,  descriptor);
			}
		} catch (Exception e) {
			Logger.appendToErrorLog("Error generating list of campaigns.", e);
		}
		
		return campaigns;
	}
	
	/**
	 * A class containing the campaign description and name
	 */
	
	private static class CampaignDescriptor {
		private String name;
		private String description;
	}
}
