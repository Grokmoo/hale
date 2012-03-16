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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;

/**
 * A thread that checks for available updates
 * @author Jared
 *
 */

public class CheckForUpdatesTask extends Thread {
	private volatile String error;
	
	private volatile boolean canceled;
	
	private volatile boolean hasFoundUpdates;
	
	private InputStream in;
	
	private UpdateInfo updateInfo;
	
	/**
	 * Returns a string description of any error this task has encountered, or null if
	 * no error has occurred
	 * @return a description of any error that has occurred
	 */
	
	public String getError() {
		return error;
	}
	
	/**
	 * Gets the information pertaining to the update to be downloaded
	 * from the server
	 * @return
	 */
	
	public UpdateInfo getUpdateInfo() {
		return updateInfo;
	}
	
	/**
	 * Returns true if and only if this task has found an available update
	 * and has launched a new task controlling that
	 * @return whether this task has found any updates
	 */
	
	public boolean hasFoundUpdates() {
		return hasFoundUpdates;
	}
	
	/**
	 * Causes this task to immediately cancel anything it is currently doing
	 */
	
	public void cancel() {
		canceled = true;
	}
	
	private void closeStream() {
		try {
			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			Logger.appendToErrorLog("Error closing update connection", e);
		}
	}
	
	@Override public void run() {
		in = null;
		try {
			in = new URL("http://www.halegame.com/version.txt").openStream();
			
			// check if the user has canceled
			if (canceled) {
				closeStream();
				return;
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String serverVersion = reader.readLine();
			String downloadFile = reader.readLine();
			String targetName = reader.readLine();
			int fileSize = Integer.parseInt(reader.readLine());
			String md5Hash = reader.readLine();
			
			updateInfo = new UpdateInfo(serverVersion, downloadFile, targetName, fileSize, md5Hash);
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error checking for updates", e);
			error = "Error connecting to update server!";
			closeStream();
			return;
		}
		
		// check if the user has canceled
		if (canceled) {
			closeStream();
			return;
		}
		
		String localVersion = null;
		try {
			localVersion = FileUtil.readFileAsString("docs/version.txt");
			
		} catch (IOException e) {
			Logger.appendToErrorLog("Error checking for updates", e);
			error = "Error getting local version!";
			closeStream();
			return;
		}
		
		// check if the user has canceled
		if (canceled) {
			closeStream();
			return;
		}
		
		if (localVersion.equals("svn")) {
			error = "You must update using \"svn update\".";
		} else if (localVersion.equals(updateInfo.version)) {
			error = "Hale is up to date.";
		} else {
			hasFoundUpdates = true;
		}
		
		closeStream();
	}
	
	/**
	 * A class containing information pertaining to an update
	 * @author Jared
	 *
	 */

	public static class UpdateInfo {
		public final String version;
		public final String URL;
		public final String targetName;
		public final int fileSize;
		public final String md5Sum;
		
		private UpdateInfo(String version, String URL, String targetName, int fileSize, String md5Sum) {
			this.version = version;
			this.URL = URL;
			this.targetName = targetName;
			this.fileSize = fileSize;
			this.md5Sum = md5Sum;
		}
	}
}
