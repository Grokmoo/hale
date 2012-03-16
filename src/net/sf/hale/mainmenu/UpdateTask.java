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

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import net.sf.hale.util.Logger;

/**
 * A thread that is run to download and extract an update from the server
 * @author Jared
 *
 */

public class UpdateTask extends Thread {
	private final CheckForUpdatesTask.UpdateInfo updateInfo;
	
	private int bytesDownloaded;
	
	private volatile String error;
	private volatile boolean canceled;
	
	private volatile String taskText;
	
	private volatile float progress;
	
	private volatile boolean done;
	
	/**
	 * Creates a new UpdateTask which will download and apply the update
	 * from the specified file
	 * @param updateInfo the information about the file to be downloaded
	 */
	
	public UpdateTask(CheckForUpdatesTask.UpdateInfo updateInfo) {
		this.updateInfo = updateInfo;
		
		updateProgress();
	}
	
	/**
	 * Returns a string description of any error this task has encountered, or null if
	 * no error has occurred
	 * @return a description of any error that has occurred
	 */
	
	public String getError() {
		return error;
	}
	
	/**
	 * Returns true if and only if this update task has completed
	 * @return true if this update task has completed successfully
	 */
	
	public boolean isDone() {
		return done;
	}
	
	/**
	 * Causes this task to immediately cancel anything it is currently doing
	 */
	
	public void cancel() {
		canceled = true;
	}
	
	/**
	 * Gets the current amount of progress this update task has made, as a fraction between 0.0 and 1.0
	 * @return the amount of progress
	 */
	
	public float getProgress() {
		return progress;
	}
	
	public String getTaskText() {
		return taskText;
	}
	
	private void updateProgress() {
		taskText = "Downloading (" + (bytesDownloaded / 1024) + " KB / " + (updateInfo.fileSize / 1024) + " KB)";
		progress = ((float)bytesDownloaded) / ((float)updateInfo.fileSize);
	}
	
	@Override public void run() {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(updateInfo.URL).openStream());
			fout = new FileOutputStream(updateInfo.targetName);
			
			byte[] buffer = new byte[2048];
			int count;
			while ( (count = in.read(buffer, 0, 2048)) != -1) {
				if (canceled) break;
				
				// write the bytes to a file
				fout.write(buffer, 0, count);
				
				// update the progress description
				bytesDownloaded += count;
				
				updateProgress();
			}
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error downloading update", e);
			error = "Error retrieving file from server";
		}
		
		try {
			if (in != null)
				in.close();

			if (fout != null)
				fout.close();
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error closing stream while updating", e);
		}
		
		done = true;
	}
}
