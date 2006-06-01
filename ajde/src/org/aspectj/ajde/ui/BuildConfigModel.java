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
import java.util.*;

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
	 * @param	path	java.io.File.separator delimited path
	 * @return corresponding node if the path is found, the root otherwise
	 */
	public BuildConfigNode getNodeForPath(String path) {
		BuildConfigNode upPathMatch = searchUpPaths(path);
		if (upPathMatch != null && upPathMatch != root) {
			return upPathMatch;
		} else {
			StringTokenizer st = new StringTokenizer(path, "/");
			BuildConfigNode node = (BuildConfigNode)root;
			return getNodeForPathHelper(st, node);
		}
	}

	private BuildConfigNode searchUpPaths(String path) {
		for (Iterator it = root.getChildren().iterator(); it.hasNext(); ) {
			BuildConfigNode node = (BuildConfigNode)it.next();
			if (node.getName().equals(path)) return node;	
		}
		return null;
	}

	private BuildConfigNode getNodeForPathHelper(StringTokenizer st, BuildConfigNode node) {
		BuildConfigNode parent = node;
		while (st.hasMoreElements()) {
			String pathItem = (String)st.nextElement();
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				node = (BuildConfigNode)it.next();
				String childName = node.getName();
				if (childName.equals(pathItem)) {
					return getNodeForPathHelper(st, node);
				} 
			}
		}
		return parent;	
	}
	
	public List getActiveNodes(BuildConfigNode.Kind kind) {
		List nodes = new ArrayList();
		getActiveNodesHelper((BuildConfigNode)getRoot(), kind, nodes);
		return nodes;
	}

	private void getActiveNodesHelper(BuildConfigNode node, BuildConfigNode.Kind kind, List nodes) {
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			BuildConfigNode currNode = (BuildConfigNode)it.next();
			if (currNode.getBuildConfigNodeKind().equals(kind)
				&& currNode.isActive()) {
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
		if (matches(node, sourceFilePath, lineNumber) 
			&& !hasMoreSpecificChild(node, sourceFilePath, lineNumber)) {
			return node;	
		} 
		
		if (node != null && node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				BuildConfigNode foundNode = findNodeForSourceLineHelper(
					(BuildConfigNode)it.next(), 
					sourceFilePath, 
					lineNumber); 		
				if (foundNode != null) return foundNode;
			}
		}
		return null;		
	}

	private boolean matches(BuildConfigNode node, String sourceFilePath, int lineNumber) {
		try {			
			return node != null 
				&& node.getSourceLocation() != null
				&& node.getSourceLocation().getSourceFile().getCanonicalPath().equals(sourceFilePath)
				&& ((node.getSourceLocation().getLine() <= lineNumber
					&& node.getSourceLocation().getEndLine() >= lineNumber)
					||
					(lineNumber <= 1
					 && node instanceof BuildConfigNode)	
				);
		} catch (IOException ioe) { 
			return false;
		} 
	}
	
	private boolean hasMoreSpecificChild(BuildConfigNode node, String sourceFilePath, int lineNumber) {
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			BuildConfigNode child = (BuildConfigNode)it.next();
			if (matches(child, sourceFilePath, lineNumber)) return true;
		}
		return false;
	}

}


