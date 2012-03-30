/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.swingeditor;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.sf.hale.Area;
import net.sf.hale.Encounter;
import net.sf.hale.Game;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Trap;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.DirectoryListing;

/**
 * The menu bar for the swing campaign Editor
 * @author Jared
 *
 */

public class EditorMenuBar extends JMenuBar {
	private SwingEditor frame;
	
	private JMenu areasMenu;
	private JMenu openAreasMenu;
	private JMenu editorsMenu;
	
	private JTextField logItem;
	
	/**
	 * Creates a new menu bar
	 * @param frame the parent frame
	 */
	
	public EditorMenuBar(SwingEditor frame) {
		this.frame = frame;
		
		// create campaign menu
		JMenu campaignMenu = new JMenu("Campaign");
		campaignMenu.setMnemonic(KeyEvent.VK_C);
		add(campaignMenu);
		
		JMenuItem newItem = new JMenuItem(new NewAction());
		newItem.setEnabled(false);
		newItem.setMnemonic(KeyEvent.VK_N);
		campaignMenu.add(newItem);
		
		JMenuItem saveItem = new JMenuItem(new SaveAction());
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		campaignMenu.add(saveItem);
		
		// add open menu and a menu item for each campaign directory
		JMenu openMenu = new JMenu("Open");
		openMenu.setMnemonic(KeyEvent.VK_O);
		campaignMenu.add(openMenu);
		
		File campaignDir = new File("campaigns");
		String[] fileList = campaignDir.list();
		for (int i = 0; i < fileList.length; i++) {
			File f = new File("campaigns/" + fileList[i]);
			if (f.isDirectory() && !f.getName().equals(".svn")) {
				JMenuItem openItem = new JMenuItem(new OpenAction(fileList[i]));
				openMenu.add(openItem);
			}
		}

		JMenuItem extractItem = new JMenuItem("Extract Zip", KeyEvent.VK_E);
		extractItem.setEnabled(false);
		campaignMenu.add(extractItem);
		
		JMenuItem compressItem = new JMenuItem("Compress to Zip", KeyEvent.VK_C);
		compressItem.setEnabled(false);
		campaignMenu.add(compressItem);
		
		JMenuItem exitItem = new JMenuItem(new ExitAction());
		exitItem.setMnemonic(KeyEvent.VK_X);
		campaignMenu.add(exitItem);
		
		// create areas menu, it won't be populated until a campaign is loaded
		areasMenu = new JMenu("Areas");
		areasMenu.setEnabled(false);
		areasMenu.setMnemonic(KeyEvent.VK_A);
		add(areasMenu);
		
		JMenuItem createAreaItem = new JMenuItem(new CreateAreaAction());
		createAreaItem.setMnemonic(KeyEvent.VK_C);
		createAreaItem.setEnabled(false);
		areasMenu.add(createAreaItem);
		
		openAreasMenu = new JMenu("Open");
		openAreasMenu.setMnemonic(KeyEvent.VK_O);
		areasMenu.add(openAreasMenu);
		
		// create editors menu
		editorsMenu = new JMenu("Windows");
		editorsMenu.setMnemonic(KeyEvent.VK_E);
		editorsMenu.setEnabled(false);
		add(editorsMenu);
		
		JMenuItem createEditorItem = new JMenuItem(new CreateEditorAction());
		createEditorItem.setMnemonic(KeyEvent.VK_N);
		editorsMenu.add(createEditorItem);
		
		JMenuItem closeEditorsItem = new JMenuItem(new CloseEditorsAction());
		closeEditorsItem.setMnemonic(KeyEvent.VK_C);
		editorsMenu.add(closeEditorsItem);
		
		JMenuItem showLogViewerItem = new JMenuItem(new ShowLogViewerAction());
		showLogViewerItem.setMnemonic(KeyEvent.VK_L);
		editorsMenu.add(showLogViewerItem);
		
		logItem = new JTextField();
		logItem.setEditable(false);
		logItem.setHorizontalAlignment(JTextField.RIGHT);
		logItem.setBorder(null);
		add(logItem);
	}
	
	/**
	 * Sets the text that is displayed in the upper right hand corner of the screen (the most recent log entry)
	 * @param text the log text to display
	 */
	
	public void setLogText(String text) {
		logItem.setText(text);
	}
	
	/**
	 * This method is called after a campaign is loaded / unloaded, to
	 * update the status of the menu bars
	 */
	
	public void updateCampaign() {
		openAreasMenu.removeAll();
		
		if (Game.curCampaign == null) {
			areasMenu.setEnabled(false);
			editorsMenu.setEnabled(false);
		} else {
			areasMenu.setEnabled(true);
			editorsMenu.setEnabled(true);
			
			// populate the list of areas
			File areaDir = new File("campaigns/" + Game.curCampaign.getID() + "/areas");
			List<File> areaFiles = DirectoryListing.getFiles(areaDir);
			for (File file : areaFiles) {
				String ref = file.getName().substring(0, file.getName().length() - 4);
				
				JMenuItem item = new JMenuItem(new OpenAreaAction(ref));
				openAreasMenu.add(item);
			}
		}
	}
	
	private class ShowLogViewerAction extends AbstractAction {
		private ShowLogViewerAction() { super("Show Log Viewer"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			EditorManager.showLogViewer();
		}
	}
	
	private class CreateEditorAction extends AbstractAction {
		private CreateEditorAction() { super("Create New Editor"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			EditorManager.createNewEditor();
		}
	}
	
	private class CloseEditorsAction extends AbstractAction {
		private CloseEditorsAction() { super("Close All Editors"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			EditorManager.closeAllEditors();
		}
	}
	
	private class CreateAreaAction extends AbstractAction {
		private CreateAreaAction() { super("Create"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			
		}
	}
	
	private class OpenAreaAction extends AbstractAction {
		private String areaID;
		
		private OpenAreaAction(String areaID) {
			super(areaID);
			this.areaID = areaID;
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			Area area = Game.curCampaign.getArea(areaID);
			AreaUtil.setMatrix(area.getExplored(), true);
			
			// remove automatically placed creatures from encounters
			Encounter.removeCreaturesFromArea(area);
			
			// force spot all traps so we can see them in the editor
			for (Entity entity : area.getEntities()) {
				if (entity.getType() == Entity.Type.TRAP) ((Trap)entity).setSpotted(true);
			}
			
			AreaRenderer viewer = new AreaRenderer(area);
			frame.setAreaViewer(viewer);
			
			EditorManager.addLogEntry("Opened area: " + area.getID());
		}
	}
	
	private class NewAction extends AbstractAction {
		private NewAction() { super("New"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			
		}
	}
	
	private class SaveAction extends AbstractAction {
		private SaveAction() {
			super("Save");
			setEnabled(false);
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			
		}
	}
	
	private class OpenAction extends AbstractAction {
		private String campaignID;
		
		private OpenAction(String id) {
			super(id);
			this.campaignID = id;
		}
		
		@Override public void actionPerformed(ActionEvent e) {
			CampaignLoader loader = new CampaignLoader(EditorMenuBar.this, campaignID);
			loader.execute();
			
			EditorManager.addLogEntry("Opened campaign: " + campaignID);
		}
	}
	
	private class ExitAction extends AbstractAction {
		private ExitAction() { super("Exit"); }
		
		@Override public void actionPerformed(ActionEvent e) {
			WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
		}
	}
}