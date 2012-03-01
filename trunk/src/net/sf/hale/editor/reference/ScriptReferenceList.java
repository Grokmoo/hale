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

package net.sf.hale.editor.reference;

import java.util.LinkedList;
import java.util.List;

import net.sf.hale.AreaTrigger;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Item;
import net.sf.hale.util.GrepUtil;

/**
 * A {@link ReferenceList} for a Script object.
 * 
 * @author Jared Stephen
 *
 */

public class ScriptReferenceList implements ReferenceList {
	private final Script script;
	
	/**
	 * Create a new ReferenceList with the specified script
	 * 
	 * @param script the script to create this list with
	 */
	
	public ScriptReferenceList(Script script) {
		this.script = script;
	}
	
	/**
	 * Returns a list of Strings representing all the references to the Script
	 * specified by this ReferenceList.
	 * 
	 * @return the list of references to the Script specified at the creation
	 * of this ReferenceList in the current campaign.
	 */
	
	@Override public List<String> getReferences() {
		List<String> references = new LinkedList<String>();
		
		references.add("Script: " + script.getID());
		
		// look for references in trigger scripts
		for (int i = 0; i < Game.campaignEditor.getTriggersModel().getNumEntries(); i++) {
			AreaTrigger trigger = Game.campaignEditor.getTriggersModel().getEntry(i);
			if (script.getID().equals(trigger.getScriptFile())) {
				references.add("Script for AreaTrigger: " + trigger.getID());
			}
		}
		
		// look for references in creature conversation scripts
		for (int i = 0; i < Game.campaignEditor.getCreaturesModel().getNumEntries(); i++) {
			Creature creature = Game.campaignEditor.getCreaturesModel().getEntry(i);
			
			if (script.equals(creature.getConversationScript())) {
				references.add("Conversation Script for Creature: " + creature.getID());
			}
		}
		
		// look for references in item scripts
		for (int i = 0; i < Game.campaignEditor.getItemsModel().getNumEntries(); i++) {
			Item item = Game.campaignEditor.getItemsModel().getEntry(i);
			
			if (script.equals(item.getConversationScript())) {
				references.add("Conversation Script for Item: " + item.getID());
			}
			
			if (!item.hasScript()) continue;
			
			if (script.equals(item.getScript().getScriptLocation())) {
				references.add("Script for Item: " + item.getID());
			}
		}
		
		// look for references in the contents of other scripts (calling the script as an external script)
		List<GrepUtil.Entry> entries = GrepUtil.findStringInTextFiles("\"" + script.getID() + "\"",
				Game.campaignEditor.getPath() + "/scripts");
		
		for (GrepUtil.Entry entry : entries) {
			if (entry.getLine().contains("runExternalScript") || entry.getLine().contains("startConversation")) {
				references.add("Script: " + entry.getPath() + " on line " + entry.getLineNumber());
			}
		}
		
		return references;
	}
}
