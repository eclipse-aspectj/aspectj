/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 *     Helen Hawkins  Converted to new interface (bug 148190)
 * ******************************************************************/

package org.aspectj.ajde.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.BuildConfigNode;
import org.aspectj.ajdt.ajc.ConfigParser;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;

/**
 * Used for reading and writing build configuration (".lst") files.
 *
 * @author Mik Kersten
 */
class LstBuildConfigFileUpdater {

	/**
	 * Adds an entry to a build configuration file.
	 */
	public void updateBuildConfigFile(String buildConfigFile, String update, boolean addToConfiguration) {
		List<String> fileContents = readConfigFile(buildConfigFile);
		if (addToConfiguration) {
			fileContents.add(update);
		} else {
			fileContents.remove(update);
		}
		writeConfigFile(buildConfigFile, fileContents);
	}

	/**
	 * Adds an entry to multiple build configuration files.
	 */
	public void updateBuildConfigFiles(List buildConfigFiles, List<String> filesToUpdate, boolean addToConfiguration) {
		for (Object buildConfigFile : buildConfigFiles) {
			List<String> fileContents = readConfigFile((String) buildConfigFile);
			if (addToConfiguration) {
				for (String s : filesToUpdate) {
					fileContents.add(s);
				}
			} else {
				for (String s : filesToUpdate) {
					if (fileContents.contains(s)) {
						fileContents.remove(s);
					}
				}
			}
			writeConfigFile((String) buildConfigFile, fileContents);
		}
	}

	/**
	 * Checks if an entry exists within a build configuration file.
	 */
	public boolean exists(String entry, String configFile) {
		return exists(entry, configFile, "");
	}

	public boolean exists(String entry, String configFile, String rootPath) {
		for (String s : readConfigFile(configFile)) {
			if ((entry).equals(rootPath + "/" + s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads the entries of a configuration file.
	 */
	public List<String> readConfigFile(String filePath) {
		try {
			File configFile = new File(filePath);
			if (!configFile.exists()) {
				Message msg = new Message("Config file: " + filePath + " does not exist.  Update failed.", IMessage.WARNING, null,
						null);
				Ajde.getDefault().getMessageHandler().handleMessage(msg);
			}
			List<String> fileContents = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			String line = reader.readLine();
			while (line != null) {
				fileContents.add(line.replace('\\', '/'));
				line = reader.readLine();
			}
			reader.close();
			return fileContents;
		} catch (IOException ioe) {
			Message msg = new Message("Could not update build config file.", IMessage.ERROR, ioe, null);
			Ajde.getDefault().getMessageHandler().handleMessage(msg);
		}
		return null;
	}

	public void writeConfigFile(String filePath, List<BuildConfigNode> files, List<BuildConfigNode> importedNodes) {
		// Set contentsSet = new TreeSet(fileContents);
		String fileContentsString = "";
		// List filesToWrite = null;
		Set<String> includedFiles = new HashSet<>();
		for (BuildConfigNode node : importedNodes) {
			fileContentsString += '@' + node.getResourcePath() + "\n";
			String parentPath = new File(filePath).getParent();
			String importedFilePath = parentPath + File.separator + node.getResourcePath();
			includedFiles.addAll(getIncludedFiles(importedFilePath, parentPath));
		}

		for (BuildConfigNode node : files) {
			if (node.getName().endsWith(".lst") && !node.getResourcePath().startsWith("..")) {
				fileContentsString += '@';
				fileContentsString += node.getResourcePath() + "\n";
			} else {
				if (!includedFiles.contains(node.getResourcePath())) {
					fileContentsString += node.getResourcePath() + "\n";
				}
			}
		}
		writeFile(fileContentsString, filePath);
	}

	private List<String> getIncludedFiles(String path, String rootPath) {
		try {
			ConfigParser configParser = new ConfigParser();
			configParser.parseConfigFile(new File(path));
			List<File> files = configParser.getFiles();
			List<String> relativeFiles = new ArrayList<>();
			for (File file : files) {
				relativeFiles.add(relativizePath(file.getPath(), rootPath));
			}
			return relativeFiles;
		} catch (ConfigParser.ParseException pe) {
			return new ArrayList<>();
		}
	}

	// private synchronized List getUniqueFileList(List list, Set set) {
	// List uniqueList = new ArrayList();
	// for (Iterator it = list.iterator(); it.hasNext(); ) {
	// BuildConfigNode node = (BuildConfigNode)it.next();
	// String file1 = node.getResourcePath();
	// if (set.contains(file1) && !uniqueList.contains(file1)) {
	// uniqueList.add(file1);
	// }
	// }
	// return uniqueList;
	// }

	public String relativizePath(String path, String rootPath) {
		path = path.replace('\\', '/');
		rootPath = rootPath.replace('\\', '/');
		int pathIndex = path.indexOf(rootPath);
		if (pathIndex > -1) {
			return path.substring(pathIndex + rootPath.length() + 1);
		} else {
			return path;
		}
	}

	/**
	 * Sorts and does not write duplicates.
	 *
	 * @param fileContents full paths representing file entries
	 */
	public void writeConfigFile(String filePath, List<String> fileContents) {
		Set<String> contentsSet = new TreeSet<>(fileContents);
		StringBuffer fileContentsSB = new StringBuffer();
		for (String s : contentsSet) {
			fileContentsSB.append(s.toString());
			fileContentsSB.append("\n");
		}
		writeFile(fileContentsSB.toString(), filePath);
	}

	private void writeFile(String contents, String filePath) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath, false);
			fos.write(contents.getBytes());
		} catch (IOException ioe) {
			Message msg = new Message("Could not update build config file: " + filePath, IMessage.ERROR, ioe, null);
			Ajde.getDefault().getMessageHandler().handleMessage(msg);
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException ioe) {
				}
		}
	}
}
