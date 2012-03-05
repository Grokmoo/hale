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

package net.sf.hale.util;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.sf.hale.resource.ResourceManager;

/**
 * A class for parsing text data from a file.  Creates a mapping of String keys (the first token
 * on each line) to String values (the rest of the tokens on a given line).
 * 
 * @author Jared Stephen
 *
 */

public class FileKeyMap {
	private final Map<String, LinkedList<LineKeyList>> fileMap;
	private final String filePath;
	private boolean resourceIsParsable = true;
	
	/**
	 * Creates a new FileKeyMap and parses the specified File.
	 * 
	 * @param file the File to parse
	 */
	
	public FileKeyMap(File file) {
		fileMap = new LinkedHashMap<String, LinkedList<LineKeyList>>();
		filePath = "{" + file.getPath() + "}";
		
		try {
			parseResource(new Scanner(file), file.getPath());
		} catch (Exception e) {
			Logger.appendToErrorLog("Error parsing file " + file, e);
			resourceIsParsable = false;
		}
	}
	
	/**
	 * Creates a new FileKeyMap and parses the resource located at the
	 * specified resource location.
	 * 
	 * @param resource the resource location of the resource to parse
	 */
	
	public FileKeyMap(String resource) {
		fileMap = new LinkedHashMap<String, LinkedList<LineKeyList>>();
		filePath = "{" + resource + " in package " + ResourceManager.getPackageIDOfResource(resource) + "}";
		
		try {
			parseResource(ResourceManager.getScanner(resource), resource);
		} catch (Exception e) {
			Logger.appendToErrorLog("Error parsing resource " + resource, e);
			resourceIsParsable = false;
		}
	}
	
	/**
	 * Creates a FileKeyMap and parses the resource at the specified input stream
	 * @param input the input resource to parse
	 */
	
	public FileKeyMap(InputStream input) {
		fileMap = new LinkedHashMap<String, LinkedList<LineKeyList>>();
		filePath = "{ InputStream }";
		
		try {
			parseResource(new Scanner(input), "InputStream");
		} catch (Exception e) {
			Logger.appendToErrorLog("Error parsing input stream resource.", e);
			resourceIsParsable = false;
		}
	}
	
	/**
	 * Returns true if the resource for this FileKeyMap exists and has been
	 * parsed, false otherwise
	 * @return whether the resource specified for this FileKeyMap is parseable
	 */
	
	public boolean resourceIsParsable() {
		return resourceIsParsable;
	}
	
	/**
	 * Returns the resource or file path for this FileKeyMap
	 * @return the resource of file path for this FileKeyMap
	 */
	
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Returns true if this FileKeyMap contains at least one line with the
	 * specified key.
	 * 
	 * @param key the key (first token) of the line to search for
	 * @return true if and only if this FileKeyMap contains at least one
	 * line with the specified key
	 */
	
	public boolean has(String key) {
		return fileMap.containsKey(key);
	}
	
	/**
	 * Returns a List of all lines with the specified key as their
	 * first token.  All lines returned via this function are considered
	 * used and will not be reported in {@link #checkUnusedKeys()}.
	 * 
	 * @param key the first token key of the lines to return
	 * @return the List of all lines with the specified key
	 */
	
	public List<LineKeyList> get(String key) {
		List<LineKeyList> lines = fileMap.get(key);
		if (lines != null) {
			fileMap.remove(key);
			return lines;
		}
		else {
			return new LinkedList<LineKeyList>();
		}
	}
	
	/**
	 * Returns the last line found in the parsed resource with the specified key.
	 * This line will be considered used and not reported in {@link #checkUnusedKeys()}.
	 * Returns null if no line is found with the specified key.
	 * 
	 * @param key the first token (key) of the line to return
	 * @return the last line found with the specified key
	 */
	
	public LineKeyList getLast(String key) {
		LinkedList<LineKeyList> allLinesWithKey = fileMap.get(key);
		if (allLinesWithKey != null) {
			LineKeyList last = allLinesWithKey.getLast();
			allLinesWithKey.removeLast();
			return last;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the second token from the last line found with the specified
	 * first token (key).  If no such key is found, returns the specified
	 * default value.  The line from which the token is found will be 
	 * considered used and not reported in {@link #checkUnusedKeys()}.
	 * However, a warning will be generated if there are more than 2 tokens
	 * on the line used.
	 * 
	 * @param key the first token (key) of the line to search for
	 * @param defaultValue the value returned if no lines matching the key
	 * are found.
	 * @return the second token from the last line matching the specified key,
	 * or the default value if no lines matching the key are found
	 */

	public String getValue(String key, String defaultValue) {
		LinkedList<LineKeyList> allLinesWithKey = fileMap.get(key);
		if (allLinesWithKey != null) {
			LineKeyList last = allLinesWithKey.getLast();
			allLinesWithKey.removeLast();
			
			if (last.size() > 1) {
				Logger.appendToWarningLog("Warning: unused tokens in " + filePath +
						" on line " + last.getLineNumber());
			}
			
			if (last.size() > 0) {
				return last.next();
			}
		}
		
		return defaultValue;
	}
	
	/**
	 * Returns the second token from the last line found with the specified
	 * first token (key).  If no such key is found, returns the specified
	 * default value.  The line from which the token is found will be 
	 * considered used and not reported in {@link #checkUnusedKeys()}.
	 * However, a warning will be generated if there are more than 2 tokens
	 * on the line used.
	 * 
	 * @param key the first token (key) of the line to search for
	 * @param defaultValue the value returned if no lines matching the key
	 * are found.
	 * @return the boolean value of the second token from the last line matching
	 * the specified key, or the default value if no lines matching the key are found
	 */
	
	public boolean getValue(String key, boolean defaultValue) {
		LinkedList<LineKeyList> allLinesWithKey = fileMap.get(key);
		if (allLinesWithKey != null) {
			LineKeyList last = allLinesWithKey.getLast();
			allLinesWithKey.removeLast();
			
			if (last.size() > 1) {
				Logger.appendToWarningLog("Warning: unused tokens in " + filePath +
						" on line " + last.getLineNumber());
			}
			
			if (last.size() > 0) {
				return last.nextBoolean();
			}
		}
		
		return defaultValue;
	}
	
	/**
	 * Returns the second token from the last line found with the specified
	 * first token (key).  If no such key is found, returns the specified
	 * default value.  The line from which the token is found will be 
	 * considered used and not reported in {@link #checkUnusedKeys()}.
	 * However, a warning will be generated if there are more than 2 tokens
	 * on the line used.
	 * 
	 * @param key the first token (key) of the line to search for
	 * @param defaultValue the value returned if no lines matching the key
	 * are found.
	 * @return the integer value of the second token from the last line matching
	 * the specified key, or the default value if no lines matching the key are found
	 */
	
	public int getValue(String key, int defaultValue) {
		LinkedList<LineKeyList> allLinesWithKey = fileMap.get(key);
		if (allLinesWithKey != null) {
			LineKeyList last = allLinesWithKey.getLast();
			allLinesWithKey.removeLast();
			
			if (last.size() > 1) {
				Logger.appendToWarningLog("Warning: unused tokens in " + filePath +
						" on line " + last.getLineNumber());
			}
			
			if (last.size() > 0) {
				return last.nextInt();
			}
		}
		
		return defaultValue;
	}
	
	/**
	 * Returns the second token from the last line found with the specified
	 * first token (key).  If no such key is found, returns the specified
	 * default value.  The line from which the token is found will be 
	 * considered used and not reported in {@link #checkUnusedKeys()}.
	 * However, a warning will be generated if there are more than 2 tokens
	 * on the line used.
	 * 
	 * @param key the first token (key) of the line to search for
	 * @param defaultValue the value returned if no lines matching the key
	 * are found.
	 * @return the long integer value of the second token from the last line matching
	 * the specified key, or the default value if no lines matching the key are found
	 */
	
	public long getValue(String key, long defaultValue) {
		LinkedList<LineKeyList> allLinesWithKey = fileMap.get(key);
		if (allLinesWithKey != null) {
			LineKeyList last = allLinesWithKey.getLast();
			allLinesWithKey.removeLast();
			
			if (last.size() > 1) {
				Logger.appendToWarningLog("Warning: unused tokens in " + filePath +
						" on line " + last.getLineNumber());
			}
			
			if (last.size() > 0) {
				return last.nextLong();
			}
		}
		
		return defaultValue;
	}
	
	/**
	 * Checks this FileKeyMap for unused keys.  Any lines that have not been returned with
	 * one of the get functions will generate a logged warning.
	 */
	
	public void checkUnusedKeys() {
		for (String key : fileMap.keySet()) {
			for (LineKeyList list : fileMap.get(key)) {
				Logger.appendToWarningLog("Warning: line " + list.getLineNumber() + " unused in " + filePath);
			}
		}
	}
	
	private void parseResource(Scanner fileScanner, String resource) {
		int currentLineNumber = 0;
		
		try {
			LineParser lineParser;
			
			while (fileScanner.hasNextLine()) {
				lineParser = new LineParser(fileScanner.nextLine());
				currentLineNumber++;
				
				if (!lineParser.hasNext()) continue;
				
				String key = lineParser.next().toLowerCase();
				
				LinkedList<LineKeyList> linesWithKey;
				if (!fileMap.containsKey(key)) {
					linesWithKey = new LinkedList<LineKeyList>();
					fileMap.put(key, linesWithKey);
				} else {
					linesWithKey = fileMap.get(key);
				}
				
				LineKeyList currentLineKeys = new LineKeyList(filePath, currentLineNumber);
				while (lineParser.hasNext()) {
					currentLineKeys.add(lineParser.next());
				}
				
				linesWithKey.add(currentLineKeys);
				currentLineKeys.setIterator();
			}
			
			fileScanner.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error reading resource " + resource + " on line number " + currentLineNumber, e);
		}
	}
}
