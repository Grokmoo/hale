package net.sf.hale.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class GrepUtil {
	public static List<Entry> findStringInTextFiles(String toFind, String directory) {
		List<Entry> entries = new LinkedList<Entry>();
		
		try {
			for (File file : DirectoryListing.getFiles(new File(directory))) {
				Scanner scanner = new Scanner(file);
				int lineNumber = 1;
				
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					
					if (line.contains(toFind)) {
						entries.add(new Entry(lineNumber, FileUtil.getRelativePath(directory, file.getPath()), line));
					}
					
					lineNumber++;
				}
				
				scanner.close();
			}
		} catch (Exception e) {
			Logger.appendToErrorLog("Error finding string " + toFind + " in directory " + directory, e);
		}
		
		return entries;
	}
	
	public static class Entry {
		public Entry(int lineNumber, String filePath, String line) {
			this.lineNumber = lineNumber;
			this.filePath = filePath;
			this.line = line;
		}
		
		public int getLineNumber() { return lineNumber; }
		public String getPath() { return filePath; }
		public String getLine() { return line; }
		
		private final int lineNumber;
		private final String filePath;
		private final String line;
	}
}
