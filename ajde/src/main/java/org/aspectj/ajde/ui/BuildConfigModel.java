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
 * ******************************************************************/

package org.aspectj.ajde.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TODO: we have schitzophrenia between BuildConfigNode(s) and IProgramElement(s), fix.
 *
 * @author Mik Kersten
 */
public class BuildConfigModel {

	private BuildConfigNode root = null;

	private String sourceFile;

	public BuildConfigModel(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	/**
	 * @param path java.io.File.separator delimited path
	 * @return corresponding node if the path is found, the root otherwise
	 */
	public BuildConfigNode getNodeForPath(String path) {
		BuildConfigNode upPathMatch = searchUpPaths(path);
		if (upPathMatch != null && upPathMatch != root) {
			return upPathMatch;
		} else {
			StringTokenizer st = new StringTokenizer(path, "/");
			return getNodeForPathHelper(st, root);
		}
	}

	private BuildConfigNode searchUpPaths(String path) {
		for (BuildConfigNode node : root.getChildren()) {
			if (node.getName().equals(path))
				return node;
		}
		return null;
	}

	private BuildConfigNode getNodeForPathHelper(StringTokenizer st, BuildConfigNode node) {
		BuildConfigNode parent = node;
		while (st.hasMoreElements()) {
			String pathItem = st.nextToken();
			for (BuildConfigNode element : node.getChildren()) {
				node = element;
				String childName = node.getName();
				if (childName.equals(pathItem)) {
					return getNodeForPathHelper(st, node);
				}
			}
		}
		return parent;
	}

	public List<BuildConfigNode> getActiveNodes(BuildConfigNode.Kind kind) {
		List<BuildConfigNode> nodes = new ArrayList<>();
		getActiveNodesHelper(root, kind, nodes);
		return nodes;
	}

	private void getActiveNodesHelper(BuildConfigNode node, BuildConfigNode.Kind kind, List<BuildConfigNode> nodes) {
		for (BuildConfigNode currNode : node.getChildren()) {
			if (currNode.getBuildConfigNodeKind().equals(kind) && currNode.isActive()) {
				nodes.add(currNode);
			}
			getActiveNodesHelper(currNode, kind, nodes);
		}
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public BuildConfigNode getRoot() {
		return root;
	}

	public void setRoot(BuildConfigNode node) {
		root = node;
	}

	public BuildConfigNode findNodeForSourceLine(String sourceFilePath, int lineNumber) {
		BuildConfigNode node = findNodeForSourceLineHelper(root, sourceFilePath, lineNumber);
		return node;
	}

	private BuildConfigNode findNodeForSourceLineHelper(BuildConfigNode node, String sourceFilePath, int lineNumber) {
		if (matches(node, sourceFilePath, lineNumber) && !hasMoreSpecificChild(node, sourceFilePath, lineNumber)) {
			return node;
		}

		if (node != null && node.getChildren() != null) {
			for (Object element : node.getChildren()) {
				BuildConfigNode foundNode = findNodeForSourceLineHelper((BuildConfigNode) element, sourceFilePath, lineNumber);
				if (foundNode != null)
					return foundNode;
			}
		}
		return null;
	}

	private boolean matches(BuildConfigNode node, String sourceFilePath, int lineNumber) {
		try {
			return node != null
					&& node.getSourceLocation() != null
					&& node.getSourceLocation().getSourceFile().getCanonicalPath().equals(sourceFilePath)
					&& ((node.getSourceLocation().getLine() <= lineNumber && node.getSourceLocation().getEndLine() >= lineNumber) || (lineNumber <= 1));
		} catch (IOException ioe) {
			return false;
		}
	}

	private boolean hasMoreSpecificChild(BuildConfigNode node, String sourceFilePath, int lineNumber) {
		for (BuildConfigNode child : node.getChildren()) {
			if (matches(child, sourceFilePath, lineNumber)) {
				return true;
			}
		}
		return false;
	}

}
