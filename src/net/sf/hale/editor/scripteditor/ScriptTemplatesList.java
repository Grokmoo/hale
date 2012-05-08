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

package net.sf.hale.editor.scripteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.hale.resource.ResourceType;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;

/**
 * Parses and contains the list of script templates and common functions
 * for easy access while coding scripts
 * @author Jared Stephen
 *
 */

public class ScriptTemplatesList {
	
	private Map<String, Map<String, Insertable>> insertables;
	private Map<String, Map<String, Template>> templates;
	
	/**
	 * Creates a ScriptTemplateList from the scriptTemplates json file
	 * at the specified resource ID
	 * @param resourceID the resource containing the json file to parse.  This should
	 * not contain the .json extension
	 */
	
	public ScriptTemplatesList(String resourceID) {
		// initialize template and insertable storage
		insertables = new HashMap<String, Map<String, Insertable>>();
		templates = new HashMap<String, Map<String, Template>>();
		
		for (ScriptEditor.Type type : ScriptEditor.Type.values()) {
			insertables.put(type.toString(), new HashMap<String, Insertable>());
			templates.put(type.toString(), new HashMap<String, Template>());
		}
		
		SimpleJSONParser parser = new SimpleJSONParser(resourceID, ResourceType.JSON);
		parser.setWarnOnMissingKeys(true);
		
		SimpleJSONObject allInsertables = parser.getObject("insertable");
		for (String key : allInsertables.keySet()) {
			Insertable insertable = new Insertable();
			
			SimpleJSONObject insertableObject = allInsertables.getObject(key);
			
			// determine the content list
			SimpleJSONArray contentArray = insertableObject.getArray("content");
			for (SimpleJSONArrayEntry entry : contentArray) {
				insertable.content.add(entry.getString());
			}
			
			// add the insertable for all specified types
			SimpleJSONArray typesArray = insertableObject.getArray("types");
			for (SimpleJSONArrayEntry entry : typesArray) {
				String typeString = entry.getString();
				insertables.get(typeString).put(key, insertable);
			}
		}
		
		SimpleJSONObject allTemplates = parser.getObject("templates");
		for (String key : allTemplates.keySet()) {
			Template template = new Template();
			
			SimpleJSONObject templateObject = allTemplates.getObject(key);
			
			// determine the content list
			if (templateObject.containsKey("content")) {
				SimpleJSONArray contentArray = templateObject.getArray("content");
				for (SimpleJSONArrayEntry entry : contentArray) {
					template.content.add(entry.getString());
				}
			}
			
			// determine the function
			template.function = templateObject.get("function", null);
			
			// determine the arguments list
			SimpleJSONArray argumentArray = templateObject.getArray("arguments");
			for (SimpleJSONArrayEntry entry : argumentArray) {
				template.arguments.add(entry.getString());
			}
			
			// add the template for all specified types
			SimpleJSONArray typesArray = templateObject.getArray("types");
			for (SimpleJSONArrayEntry entry : typesArray) {
				String typeString = entry.getString();
				templates.get(typeString).put(key, template);
			}
		}
		
		parser.warnOnUnusedKeys();
	}
	
	/**
	 * Returns a list model of all function templates for the specified script type
	 * @param type the script type
	 * @return the list model
	 */
	
	public List<String> getTemplates(ScriptEditor.Type type) {
		Map<String, Template> templatesOfType = templates.get(type.toString());
		
		List<String> list = new ArrayList<String>();
		for (String key : templatesOfType.keySet()) {
			list.add(key);
		}
		Collections.sort(list);
		
		return list;
	}
	
	/**
	 * Returns a list model of all insertable functions for the specified script type
	 * @param type the script type
	 * @return the list model
	 */
	
	public List<String> getInsertables(ScriptEditor.Type type) {
		Map<String, Insertable> insertablesOfType = insertables.get(type.toString());
		
		List<String> list = new ArrayList<String>();
		for (String key : insertablesOfType.keySet()) {
			list.add(key);
		}
		Collections.sort(list);
		
		return list;
	}
	
	/**
	 * Returns the full length string for the specified insertable
	 * @param type the Script type that the insertable is referenced under
	 * @param insertableID the ID string for the insertable
	 * @return the full string for the insertable
	 */
	
	public String getInsertableString(ScriptEditor.Type type, String insertableID) {
		Insertable insertable = insertables.get(type.toString()).get(insertableID);
		
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iter = insertable.content.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
			
			if (iter.hasNext()) sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns the full length string for the specified template
	 * @param type the script type for the template
	 * @param templateID the ID string for the template
	 * @return the full string for the template
	 */
	
	public String getTemplateString(ScriptEditor.Type type, String templateID) {
		Template template = templates.get(type.toString()).get(templateID);
		
		StringBuilder sb = new StringBuilder();
		sb.append("function ");
		sb.append(template.function);
		
		sb.append("(");
		Iterator<String> iter = template.arguments.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
			if (iter.hasNext()) sb.append(", ");
		}
		sb.append(") {\n");
		
		iter = template.content.iterator();
		while (iter.hasNext()) {
			sb.append("    ");
			sb.append(iter.next());
			if (iter.hasNext()) sb.append("\n");
		}
		
		sb.append("\n}\n");
		
		return sb.toString();
	}
	
	private class Insertable {
		private List<String> content;
		
		private Insertable() {
			content = new ArrayList<String>();
		}
	}
	
	private class Template {
		private String function;
		private List<String> arguments;
		private List<String> content;
		
		private Template() {
			content = new ArrayList<String>();
			arguments = new ArrayList<String>();
		}
	}
}
