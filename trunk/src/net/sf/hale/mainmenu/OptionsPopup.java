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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.DisplayMode;

import net.sf.hale.Config;
import net.sf.hale.Game;
import net.sf.hale.util.Logger;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;

/**
 * The menu Popup responsible for setting game options, such
 * as resolution and tooltip delay.
 * @author Jared Stephen
 *
 */

public class OptionsPopup extends PopupWindow {
	private MainMenu mainMenu;
	private Content content;
	
	public OptionsPopup(MainMenu mainMenu) {
		super(mainMenu);
		this.mainMenu = mainMenu;
		
		content = new Content();
		add(content);
		
		setCloseOnClickedOutside(false);
		setCloseOnEscape(true);
	}
	
	/**
	 * Saves the config specified by the arguments to config.txt
	 * 
	 * @param resX ResolutionX
	 * @param resY ResolutionY
	 * @param edResX EditorResolutionX
	 * @param edResY EditorResolutionY
	 * @param fullscreen true for fullscreen mode, false for windowed mode
	 * @param tooltipDelay Delay until tooltips appear in milliseconds
	 * @param combatDelay combat delay in milliseconds
	 */
	
	private void writeConfigToFile(int resX, int resY, int edResX, int edResY,
			boolean fullscreen, int tooltipDelay, int combatDelay, Map<String, String> keyBindings) {
		File fout = new File("config.json");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			
			out.write("{"); out.newLine();
			
			writeSimpleKey(out, "Resolution", "[ " + resX + ", " + resY + " ]", 1);
			writeSimpleKey(out, "Fullscreen", Boolean.toString(fullscreen), 1);
			writeSimpleKey(out, "EditorResolution", "[ " + edResX + ", " + edResY + " ]", 1);
			writeSimpleKey(out, "ShowFPS", Boolean.toString(Game.config.showFPS()), 1);
			writeSimpleKey(out, "CapFPS", Boolean.toString(Game.config.capFPS()), 1);
			writeSimpleKey(out, "TooltipDelay", Integer.toString(tooltipDelay), 1);
			writeSimpleKey(out, "CombatDelay", Integer.toString(combatDelay), 1);
			writeSimpleKey(out, "ScriptConsoleEnabled", Boolean.toString(Game.config.isScriptConsoleEnabled()), 1);
			writeSimpleKey(out, "DebugMode", Boolean.toString(Game.config.isDebugModeEnabled()), 1);
			writeSimpleKey(out, "WarningMode", Boolean.toString(Game.config.isWarningModeEnabled()), 1);
			writeSimpleKey(out, "CheckForUpdatesInterval", Long.toString(Game.config.getCheckForUpdatesInterval()), 1);
			if (Game.config.randSeedSet()) {
				writeSimpleKey(out, "RandSeed", Long.toString(Game.config.getRandSeed()), 1);
			}
			
			out.write("  \"Keybindings\" : {"); out.newLine();
			
			// write out keybindings
			for (String actionName : keyBindings.keySet()) {
				String keyName = keyBindings.get(actionName);
				
				writeSimpleKey(out, actionName, "\"" + keyName + "\"", 2);
			}
			
			out.write("  }"); out.newLine();
			
			out.write("}"); out.newLine();
			
			out.close();
		} catch (Exception e) {
			Logger.appendToErrorLog("Error writing config file.", e);
		}
	}
	
	private void writeSimpleKey(BufferedWriter out, String key, String value, int indent) throws IOException {
		for (int i = 0; i < indent; i++) {
			out.write("  ");
		}
		
		out.write("\"");
		out.write(key);
		out.write("\" : ");
		out.write(value);
		out.write(",");
		out.newLine();
	}
	
	private class Content extends DialogLayout {
		private Label title, keybindingsTitle, editorTitle;
		private Button accept, cancel;
		
		private final ToggleButton fullscreen;
		private final ComboBox<String> modesBox;
		private final SimpleChangableListModel<String> modesModel;
		private final Scrollbar tooltipDelay, combatSpeed;
		private final ComboBox<String> editorModesBox;
		private final Label modesTitle, tooltipTitle, editorModesTitle;
		private final Label combatSpeedTitle;
		private final ScrollPane keyBindingsPane;
		private final KeyBindingsContent keyBindingsContent;
		
		private Group mainH, mainV;
		
		private Content() {
			mainH = createParallelGroup();
			mainV = createSequentialGroup();
			
			title = new Label();
			title.setTheme("titlelabel");
			addHorizontalWidgets(title);
			
			modesTitle = new Label();
			modesTitle.setTheme("modeslabel");
			
			modesModel = new SimpleChangableListModel<String>();
			modesBox = new ComboBox<String>(modesModel);
			modesBox.setTheme("modesbox");
			modesBox.addCallback(new Runnable() {
				@Override public void run() {
					setAcceptEnabled();
				}
			});
			
			fullscreen = new ToggleButton();
			fullscreen.setTheme("fullscreentoggle");
			
			addHorizontalWidgets(modesTitle, modesBox, fullscreen);
			
			tooltipTitle = new Label();
			tooltipTitle.setTheme("tooltiplabel");
			
			tooltipDelay = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
			tooltipDelay.setModel(new SimpleIntegerModel(1, 20, 4));
			tooltipDelay.setPageSize(1);
			tooltipDelay.setTheme("tooltipbar");
			addHorizontalWidgets(tooltipTitle, tooltipDelay);
			
			combatSpeedTitle = new Label();
			combatSpeedTitle.setTheme("combatspeedlabel");
			
			combatSpeed = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
			combatSpeed.setModel(new SimpleIntegerModel(1, 5, 3));
			combatSpeed.setPageSize(1);
			combatSpeed.setTheme("combatspeedbar");
			addHorizontalWidgets(combatSpeedTitle, combatSpeed);
			
			mainV.addGap(DialogLayout.LARGE_GAP);
			
			keybindingsTitle = new Label();
			keybindingsTitle.setTheme("keybindingslabel");
			addHorizontalWidgets(keybindingsTitle);
			
			keyBindingsContent = new KeyBindingsContent();
			keyBindingsPane = new ScrollPane(keyBindingsContent);
			keyBindingsPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
			keyBindingsPane.setTheme("keybindingspane");
			addHorizontalWidgets(keyBindingsPane);
			
			mainV.addGap(DialogLayout.LARGE_GAP);
			
			editorTitle = new Label();
			editorTitle.setTheme("editortitlelabel");
			addHorizontalWidgets(editorTitle);
			
			editorModesTitle = new Label();
			editorModesTitle.setTheme("editormodeslabel");
			
			editorModesBox = new ComboBox<String>(modesModel);
			editorModesBox.addCallback(new Runnable() {
				@Override public void run() {
					setAcceptEnabled();
				}
			});
			editorModesBox.setTheme("editormodesbox");
			addHorizontalWidgets(editorModesTitle, editorModesBox);
			
			mainV.addGap(DialogLayout.LARGE_GAP);
			
			accept = new Button();
			accept.setTheme("acceptbutton");
			accept.addCallback(new Runnable() {
				@Override public void run() {
					applySettings();
					OptionsPopup.this.closePopup();
				}
			});
			
			cancel = new Button();
			cancel.setTheme("cancelbutton");
			cancel.addCallback(new Runnable() {
				@Override public void run() {
					OptionsPopup.this.closePopup();
				}
			});
			
			addHorizontalWidgets(accept, cancel);
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
			
			// initialize all content
			fullscreen.setActive(Game.config.getFullscreen());
			tooltipDelay.setValue(Game.config.getTooltipDelay() / 100);
			combatSpeed.setValue(Game.config.getCombatDelay() / 50);
			
			modesModel.clear();
			for (DisplayMode mode : Game.allDisplayModes) {
				modesModel.addElement(mode.getWidth() + " x " + mode.getHeight());
			}
			
			int index = Config.getMatchingDisplayMode(Game.config.getResolutionX(), Game.config.getResolutionY());
			modesBox.setSelected(index);
			
			int editorIndex = Config.getMatchingDisplayMode(Game.config.getEditorResolutionX(),
					Game.config.getEditorResolutionY());
			editorModesBox.setSelected(editorIndex);
			
			setAcceptEnabled();
		}
		
		private void setAcceptEnabled() {
			accept.setEnabled(modesBox.getSelected() != -1 && editorModesBox.getSelected() != -1);
		}
		
		private void addHorizontalWidgets(Widget... widgets) {
			switch (widgets.length) {
			case 0: break;
			case 1:
				mainH.addWidget(widgets[0]);
				mainV.addWidget(widgets[0]);
				break;
			default:
				Group gH = createSequentialGroup(widgets);
				Group gV = createParallelGroup(widgets);
				mainH.addGroup(gH);
				mainV.addGroup(gV);
			}
		}
		
		private void applySettings() {
			// get the set of key bindings that is currently being shown in the UI
			Map<String, String> keyBindings = new LinkedHashMap<String, String>();
			
			for (KeyBindWidget widget : keyBindingsContent.widgets) {
				if (widget.keyBinding.getText() != null) {
					keyBindings.put(widget.actionName, widget.keyBinding.getText());
				} else {
					keyBindings.put(widget.actionName, "");
				}
			}
			
			
			DisplayMode mode = Game.allDisplayModes.get(this.modesBox.getSelected());
			boolean fullscreen = this.fullscreen.isActive();
			
			// get tooltip delay in milliseconds
			int tooltipDelay = this.tooltipDelay.getValue() * 100;
			
			DisplayMode edMode = Game.allDisplayModes.get(this.editorModesBox.getSelected());
			
			// get combat speed in milliseconds
			int combatSpeed = this.combatSpeed.getValue() * 50;
			
			writeConfigToFile(mode.getWidth(), mode.getHeight(), edMode.getWidth(),
					edMode.getHeight(), fullscreen, tooltipDelay, combatSpeed, keyBindings);
			
			if (mode.getWidth() == Game.config.getResolutionX() && mode.getHeight() == Game.config.getResolutionY() &&
					fullscreen == Game.config.getFullscreen()) {
				
				Game.config = new Config();
				return;
			}
			
			Game.config = new Config();
			mainMenu.restartMenu();
		}
	}
	
	private class KeyBindingsContent extends DialogLayout {
		private List<KeyBindWidget> widgets;
		
		private KeyBindingsContent() {
			widgets = new ArrayList<KeyBindWidget>();
			
			Group mainH = createParallelGroup();
			Group mainV = createSequentialGroup();
			
			List<String> actions = Game.config.getKeyActionNames();
			Collections.sort(actions);
			
			for (String actionName : actions) {
				KeyBindWidget widget = new KeyBindWidget(actionName);
				mainH.addWidget(widget);
				mainV.addWidget(widget);
				
				widgets.add(widget);
			}
			
			setHorizontalGroup(mainH);
			setVerticalGroup(mainV);
		}
	}
	
	private class KeyBindWidget extends Widget implements KeyBindPopup.Callback {
		private Label actionLabel;
		private Button keyBinding;
		
		private String actionName;
		
		private KeyBindWidget(String actionName) {
			this.actionName = actionName;
			
			actionLabel = new Label(actionName);
			actionLabel.setTheme("actionlabel");
			add(actionLabel);
			
			int keyCode = Game.config.getKeyForAction(actionName);
			String keyChar = Event.getKeyNameForCode(keyCode);
			
			keyBinding = new Button(keyChar);
			keyBinding.addCallback(new Runnable() {
				@Override public void run() {
					showKeyBindPopup();
				}
			});
			keyBinding.setTheme("keybindingbutton");
			add(keyBinding);
		}
		
		private void showKeyBindPopup() {
			KeyBindPopup popup = new KeyBindPopup(KeyBindWidget.this, KeyBindWidget.this);
			popup.openPopupCentered();
		}
		
		@Override public int getPreferredInnerHeight() {
			return Math.max(actionLabel.getPreferredHeight(), keyBinding.getPreferredHeight());
		}
		
		@Override protected void layout() {
			actionLabel.setSize(actionLabel.getPreferredWidth(), actionLabel.getPreferredHeight());
			keyBinding.setSize(keyBinding.getPreferredWidth(), keyBinding.getPreferredHeight());
			
			actionLabel.setPosition(getInnerX(), getInnerY() + getInnerHeight() / 2 - actionLabel.getHeight() / 2);
			keyBinding.setPosition(actionLabel.getRight(), getInnerY() + getInnerHeight() / 2 - keyBinding.getHeight() / 2);
		}
		
		@Override public String getActionName() {
			return actionName;
		}

		@Override public void keyBound(int keyCode) {
			String keyChar = Event.getKeyNameForCode(keyCode);
			
			// bind the key for this widget
			keyBinding.setText(keyChar);
			
			// check for conflicts with the key
			for (KeyBindWidget widget : content.keyBindingsContent.widgets) {
				if (widget == this) continue;
				
				if ( keyChar.equals(widget.keyBinding.getText()) ) {
					widget.keyBinding.setText("");
				}
			}
		}
	}
}
