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
import java.util.ArrayList;
import java.util.List;

public final class DirectoryListing {
	public static List<File> getFiles(File startingDir) {
		List<File> result = new ArrayList<File>();
		
		File[] subFiles = startingDir.listFiles();
		
		if (subFiles == null) return result;
		
		for (int i = 0; i < subFiles.length; i++) {
			if (subFiles[i].getName().equals(".svn")) continue;
			
			if (subFiles[i].isFile()) {
				result.add(subFiles[i]);
			} else {
				result.addAll( getFiles(subFiles[i]) );
			}
		}
		
		return result;
	}
	
	public static List<File> getFilesAndDirectories(File startingDir) {
		List<File> result = new ArrayList<File>();
		
		File[] subFiles = startingDir.listFiles();
		
		if (subFiles == null) return result;
		
		for (int i = 0; i < subFiles.length; i++) {
			if (subFiles[i].getName().equals(".svn")) continue;
			
			result.add(subFiles[i]);
			if (subFiles[i].isDirectory()) {
				result.addAll( getFilesAndDirectories(subFiles[i]) );
			}
		}
		
		return result;
	}
} 
