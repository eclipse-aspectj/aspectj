/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.aspectj.bridge.SourceLocation;

/**
 * @author Mik Kersten
 */
public class StructureModel implements Serializable {
	
    protected  StructureNode root = null;
    protected String configFile = null;
    private Map fileMap = null;
    public static final ProgramElementNode NO_STRUCTURE = new ProgramElementNode("<build to view structure>", ProgramElementNode.Kind.ERROR, null);

    public StructureNode getRoot() {
        return root;
    }

    public void setRoot(StructureNode root) {
        this.root = root;
    }

	private Map getFileMap() {
        return fileMap;
    }

	public void addToFileMap( Object key, Object value ){
		fileMap.put( key, value );
	}
	
	public Object findInFileMap( Object key ) {
		return fileMap.get(key);
	}

	public void setFileMap(HashMap fileMap) {
        this.fileMap = fileMap;
    }

	public Set getFileMapEntrySet() {
		return fileMap.entrySet();
	}
	
	public boolean isValid() {
        return root != null && fileMap != null;
    }

	/** 
	 * Returns the first match
	 * 
	 * @param parent
	 * @param kind		not null
	 * @param decErrLabel
	 * @return null if not found
	 */
	public ProgramElementNode findNode(ProgramElementNode parent, ProgramElementNode.Kind kind, String name) {
		for (Iterator it = parent.getChildren().iterator(); it.hasNext(); ) {
			ProgramElementNode node = (ProgramElementNode)it.next();
			if (node.getProgramElementKind().equals(kind) 
				&& name.equals(node.getName())) {
				return node;
			} else {
				ProgramElementNode childSearch = findNode(node, kind, name);
				if (childSearch != null) return childSearch;
			}
		}
		return null;
	}

	/**
	 * @param packageName	if null default package is searched
	 * @param className 	can't be null
	 */ 
	public ProgramElementNode findNodeForClass(String packageName, String className) {
		StructureNode packageNode = null;
		if (packageName == null) {
			packageNode = root;
		} else {
			for (Iterator it = root.getChildren().iterator(); it.hasNext(); ) {
				StructureNode node = (StructureNode)it.next();
				if (packageName.equals(node.getName())) {
					packageNode = node;
				} 
			}
			if (packageNode == null) return null;
		}
		
		// this searches each file for a class
		for (Iterator it = packageNode.getChildren().iterator(); it.hasNext(); ) {
			ProgramElementNode fileNode = (ProgramElementNode)it.next();
			ProgramElementNode ret = findClassInNodes(fileNode.getChildren(), className);
			if (ret != null) return ret;
		}
		
		return null;
	}
	
	private ProgramElementNode findClassInNodes(Collection nodes, String name) {
		String baseName;
		String innerName;
		int dollar = name.indexOf('$');
		if (dollar == -1) {
			baseName = name;
			innerName = null;
		} else {
			baseName = name.substring(0, dollar);
			innerName = name.substring(dollar+1);
		}
		
		
		for (Iterator j = nodes.iterator(); j.hasNext(); ) {
			ProgramElementNode classNode = (ProgramElementNode)j.next();
//			System.err.println("checking: " + classNode + " for " + baseName);	
//			System.err.println("children: " + classNode.getChildren());
			if (baseName.equals(classNode.getName())) {
				if (innerName == null) return classNode;
				else return findClassInNodes(classNode.getChildren(), innerName);
			} else if (name.equals(classNode.getName())) {
				return classNode;
			}
		}
		return null;
	}


	/**
	 * @param		sourceFilePath	modified to '/' delimited path for consistency
	 * @return		a new structure node for the file if it was not found in the model
	 */
	public StructureNode findRootNodeForSourceFile(String sourceFile) {
       	try {
	       	if (!isValid() || sourceFile == null) {   
	            return StructureModel.NO_STRUCTURE;
	        } else {
	            String correctedPath = new File(sourceFile).getCanonicalPath();//.replace('\\', '/');
	            //StructureNode node = (StructureNode)getFileMap().get(correctedPath);//findFileNode(filePath, model);
				StructureNode node = (StructureNode)findInFileMap(correctedPath);//findFileNode(filePath, model);
	            if (node != null) {
	                return node;
	            } else {
	                return createFileStructureNode(correctedPath);
	            }
	        }
		} catch (Exception e) {
			return StructureModel.NO_STRUCTURE;
		}
    }

	/**
	 * Never returns null 
	 * 
	 * @param		sourceFilePath	canonicalized path for consistency
	 * @param 		lineNumber		if 0 or 1 the corresponding file node will be returned
	 * @return		a new structure node for the file if it was not found in the model
	 */
	public StructureNode findNodeForSourceLine(String sourceFilePath, int lineNumber) {
		String correctedPath = sourceFilePath;//.replace('\\', '/');
		StructureNode node = findNodeForSourceLineHelper(root, correctedPath, lineNumber);
		if (node != null) {
			return node;	
		} else {
			return createFileStructureNode(sourceFilePath);
		}
	}

	private StructureNode createFileStructureNode(String sourceFilePath) {
		String fileName = new File(sourceFilePath).getName();
		ProgramElementNode fileNode = new ProgramElementNode(fileName, ProgramElementNode.Kind.FILE_JAVA, null);
		fileNode.setSourceLocation(new SourceLocation(new File(sourceFilePath), 1, 1));
		fileNode.addChild(NO_STRUCTURE);
		return fileNode;
	}


	private StructureNode findNodeForSourceLineHelper(StructureNode node, String sourceFilePath, int lineNumber) {
		if (matches(node, sourceFilePath, lineNumber) 
			&& !hasMoreSpecificChild(node, sourceFilePath, lineNumber)) {
			return node;	
		} 
		
		if (node != null && node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				StructureNode foundNode = findNodeForSourceLineHelper(
					(StructureNode)it.next(), 
					sourceFilePath, 
					lineNumber); 		
				if (foundNode != null) return foundNode;
			}
		}
		
		return null;		
	}

	private boolean matches(StructureNode node, String sourceFilePath, int lineNumber) {
		try {			
//			if (node != null && node.getSourceLocation() != null)
//				System.err.println("====\n1: " + 
//					sourceFilePath + "\n2: " +
//					node.getSourceLocation().getSourceFile().getCanonicalPath().equals(sourceFilePath)
//				);	
			
			return node != null 
				&& node.getSourceLocation() != null
				&& node.getSourceLocation().getSourceFile().getCanonicalPath().equals(sourceFilePath)
				&& ((node.getSourceLocation().getLine() <= lineNumber
					&& node.getSourceLocation().getEndLine() >= lineNumber)
				    ||
					(lineNumber <= 1
					 && node instanceof ProgramElementNode 
					 && ((ProgramElementNode)node).getProgramElementKind().isSourceFileKind())	
				);
		} catch (IOException ioe) { 
			return false;
		} 
	}
	
	private boolean hasMoreSpecificChild(StructureNode node, String sourceFilePath, int lineNumber) {
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			ProgramElementNode child = (ProgramElementNode)it.next();
			if (matches(child, sourceFilePath, lineNumber)) return true;
		}
		return false;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

}

