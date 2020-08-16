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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.BuildConfigModel;
import org.aspectj.ajde.ui.BuildConfigNode;
import org.aspectj.ajdt.ajc.ConfigParser;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.FileUtil;

/**
 * @author Mik Kersten
 */
public class LstBuildConfigManager implements BuildConfigManager {


	private List<String> allBuildConfigFiles;
	private List<BuildConfigListener> listeners = new ArrayList<>();
	private LstBuildConfigFileUpdater fileUpdater = new LstBuildConfigFileUpdater();
	protected String currConfigFilePath = null;

	private static final FilenameFilter SOURCE_FILE_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return FileUtil.hasSourceSuffix(name) || name.endsWith(".lst");
		}
	};

	private static final FileFilter DIR_FILTER = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};

	@Override
	public BuildConfigModel buildModel(String configFilePath) {
		File configFile = new File(configFilePath);
		String rootPath = configFile.getParent();
		String configFileName = configFile.getName();
		BuildConfigModel model = new BuildConfigModel(configFilePath);
		List<File> configFiles = new ArrayList<>();
		List<File> importedFiles = new ArrayList<>();
		List<String> badEntries = null;
		try {
			LstBuildConfigFileParser configParser = new LstBuildConfigFileParser(configFilePath);
			configParser.parseConfigFile(new File(configFilePath));
			configFiles = configParser.getFiles();
			importedFiles = configParser.getImportedFiles();
			badEntries = configParser.getProblemEntries();
		} catch (ConfigParser.ParseException pe) {
			// String filePath = "<unknown>";
			// if (pe.getFile() != null) filePath = pe.getFile().getAbsolutePath();
			IMessage message = new Message(pe.getMessage(), IMessage.ERROR, pe, new SourceLocation(pe.getFile(), pe.getLine(), 1));
			Ajde.getDefault().getMessageHandler().handleMessage(message);
		}

		List<String> relativePaths = relativizeFilePaths(configFiles, rootPath);
		BuildConfigNode root = new BuildConfigNode(configFileName, BuildConfigNode.Kind.FILE_LST, rootPath);
		buildDirTree(root, rootPath, importedFiles, configFileName);
		model.setRoot(root);
		addFilesToDirTree(model, relativePaths, badEntries);

		pruneEmptyDirs(root);
		sortModel(model.getRoot(), ALPHABETICAL_COMPARATOR);
		// addImportedFilesToDirTree(model, importedFiles);

		addProblemEntries(root, badEntries);
		return model;
	}

	private void addProblemEntries(BuildConfigNode root, List<String> badEntries) {
		for (String string : badEntries) {
			root.addChild(new BuildConfigNode(string.toString(), BuildConfigNode.Kind.ERROR, null));
		}
	}

	@Override
	public void writeModel(BuildConfigModel model) {
		// final List paths = new ArrayList();
		// StructureWalker walker = new StructureWalker() {
		// protected void postProcess(StructureNode node) {
		// BuildConfigNode configNode = (BuildConfigNode)node;
		// if (configNode.isActive() && configNode.isValidResource()) {
		// paths.add(configNode.getResourcePath());
		// }
		// }
		// };
		// model.getRoot().walk(walker);

		List<BuildConfigNode> activeSourceFiles = model.getActiveNodes(BuildConfigNode.Kind.FILE_ASPECTJ);
		activeSourceFiles.addAll(model.getActiveNodes(BuildConfigNode.Kind.FILE_JAVA));
		List<BuildConfigNode> activeImportedFiles = model.getActiveNodes(BuildConfigNode.Kind.FILE_LST);
		fileUpdater.writeConfigFile(model.getSourceFile(), activeSourceFiles, activeImportedFiles);
	}

	@Override
	public void writePaths(String configFilePath, List<String> files) {
		fileUpdater.writeConfigFile(configFilePath, files);
	}

	@Override
	public void addFilesToConfig(String configFilePath, List paths) {

	}

	@Override
	public void removeFilesFromConfig(String configFilePath, List files) {

	}

	private List<String> relativizeFilePaths(List<File> configFiles, String rootPath) {
		List<String> relativePathsList = new ArrayList<>();
		for (File file : configFiles) {
			relativePathsList.add(fileUpdater.relativizePath(file.getPath(), rootPath));
		}
		return relativePathsList;
	}

	// private String relativizePath(String path, String rootPath) {
	// path = path.replace('\\', '/');
	// rootPath = rootPath.replace('\\', '/');
	// int pathIndex = path.indexOf(rootPath);
	// if (pathIndex > -1) {
	// return path.substring(pathIndex + rootPath.length() + 1);
	// } else {
	// return path;
	// }
	// }

	private void buildDirTree(BuildConfigNode node, String rootPath, List importedFiles, String configFileName) {
		File[] dirs = new File(node.getResourcePath()).listFiles(DIR_FILTER);
		if (dirs == null)
			return;
		for (File dir2 : dirs) {
			BuildConfigNode dir = new BuildConfigNode(dir2.getName(), BuildConfigNode.Kind.DIRECTORY, dir2.getPath());
			File[] files = dir2.listFiles(SOURCE_FILE_FILTER);
			for (File file2 : files) {
				if (file2 != null) {// && !files[j].getName().endsWith(".lst")) {
					String filePath = fileUpdater.relativizePath(file2.getPath(), rootPath);
					BuildConfigNode.Kind kind = BuildConfigNode.Kind.FILE_JAVA;
					if (!file2.getName().endsWith(".lst")) {
						// kind = BuildConfigNode.Kind.FILE_LST;
						BuildConfigNode file = new BuildConfigNode(file2.getName(), kind, filePath);
						file.setActive(false);
						dir.addChild(file);
					}
				}
			}
			node.addChild(dir);
			// boolean foundMatch = false;
			for (Object file : importedFiles) {
				File importedFile = (File) file;
				if (importedFile.getParentFile().getAbsolutePath().equals(dir2.getAbsolutePath())) {
					// foundMatch = true;
					BuildConfigNode importedFileNode = new BuildConfigNode(importedFile.getName(), BuildConfigNode.Kind.FILE_LST,
							fileUpdater.relativizePath(importedFile.getPath(), rootPath));
					importedFileNode.setActive(true);
					// dir.getChildren().clear();
					boolean found = false;
					for (BuildConfigNode buildConfigNode : dir.getChildren()) {
						if (buildConfigNode.getName().equals(importedFile.getName())) {
							found = true;
						}
					}
					if (!found)
						dir.addChild(importedFileNode);
				}

			}
			// if (!foundMatch)
			buildDirTree(dir, rootPath, importedFiles, configFileName);
		}

		if (node.getName().endsWith(".lst")) {
			File[] files = new File(rootPath).listFiles(SOURCE_FILE_FILTER);
			if (files == null)
				return;
			for (File file2 : files) {
				if (file2 != null && !file2.getName().equals(configFileName)) {// && !files[i].getName().endsWith(".lst")) {
					BuildConfigNode.Kind kind = BuildConfigNode.Kind.FILE_JAVA;
					if (file2.getName().endsWith(".lst")) {
						kind = BuildConfigNode.Kind.FILE_LST;
					}
					BuildConfigNode file = new BuildConfigNode(file2.getName(), kind, file2.getName());
					file.setActive(false);
					node.addChild(file);
				}
			}
		}
	}

	private void addFilesToDirTree(BuildConfigModel model, List configFiles, List badEntries) {
		for (Object configFile : configFiles) {
			String path = (String) configFile;
			if (path.startsWith("..")) {
				File file = new File(path);
				BuildConfigNode node = new BuildConfigNode(file.getName(), BuildConfigNode.Kind.FILE_JAVA, path);
				BuildConfigNode upPath = model.getNodeForPath(file.getParentFile().getPath());
				if (upPath == model.getRoot()) {
					upPath = new BuildConfigNode(file.getParentFile().getPath(), BuildConfigNode.Kind.DIRECTORY, file
							.getParentFile().getAbsolutePath());
					model.getRoot().addChild(upPath);
				}
				node.setActive(true);
				upPath.addChild(node);
			} else if (!(new File(path).isAbsolute())) {
				// String name = new File(path).getName();
				BuildConfigNode existingNode = model.getNodeForPath(path);
				existingNode.setActive(true);
			} else {
				badEntries.add("Use relative paths only, omitting: " + path);
			}
		}
	}

	private boolean pruneEmptyDirs(BuildConfigNode node) {
		List<BuildConfigNode> nodesToRemove = new ArrayList<>();
		for (BuildConfigNode currNode : node.getChildren()) {
			boolean hasValidChildren = pruneEmptyDirs(currNode);
			if (!currNode.isValidResource() && !hasValidChildren) {
				nodesToRemove.add(currNode);
			}
		}

		for (BuildConfigNode currNode : nodesToRemove) {
			node.removeChild(currNode);
		}
		return node.getChildren().size() > 0;
	}

	@Override
	public String getActiveConfigFile() {
		return currConfigFilePath;
	}

	@Override
	public void setActiveConfigFile(String currConfigFilePath) {
		if (currConfigFilePath == null)
			return;
		this.currConfigFilePath = currConfigFilePath;
		notifyConfigChanged();
	}

	@Override
	public void addListener(BuildConfigListener configurationListener) {
		listeners.add(configurationListener);
	}

	@Override
	public void removeListener(BuildConfigListener configurationListener) {
		listeners.remove(configurationListener);
	}

	private void notifyConfigChanged() {
		for (Object element : listeners) {
			((BuildConfigListener) element).currConfigChanged(currConfigFilePath);
		}
	}

	// private void notifyConfigsListUpdated() {
	// for (Iterator it = listeners.iterator(); it.hasNext(); ) {
	// ((BuildConfigListener)it.next()).configsListUpdated(configFiles);
	// }
	// }
	//
	private void sortModel(BuildConfigNode node, Comparator<BuildConfigNode> comparator) {
		if (node == null || node.getChildren() == null)
			return;
		node.getChildren().sort(comparator);
		for (BuildConfigNode nextNode : node.getChildren()) {
			if (nextNode != null)
				sortModel(nextNode, comparator);
		}
	}

	private static final Comparator<BuildConfigNode> ALPHABETICAL_COMPARATOR = new Comparator<BuildConfigNode>() {
		@Override
		public int compare(BuildConfigNode n1, BuildConfigNode n2) {
			return n1.getName().compareTo(n2.getName());
		}
	};

	@Override
	public List<String> getAllBuildConfigFiles() {
		if (allBuildConfigFiles == null) {
			allBuildConfigFiles = new ArrayList<>();
			if (getActiveConfigFile() != null) {
				allBuildConfigFiles.add(getActiveConfigFile());
			}
		}
		return allBuildConfigFiles;
	}

}
