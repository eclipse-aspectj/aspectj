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

	public Map getFileMap() {
        return fileMap;
    }


	public void setFileMap(HashMap fileMap) {
        this.fileMap = fileMap;
    }


	public boolean isValid() {
        return root != null && fileMap != null;
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
		// !!! this searches each file for a class
		for (Iterator it = packageNode.getChildren().iterator(); it.hasNext(); ) {
			ProgramElementNode fileNode = (ProgramElementNode)it.next();
			for (Iterator j = fileNode.getChildren().iterator(); j.hasNext(); ) {
				ProgramElementNode classNode = (ProgramElementNode)j.next();	
				if (classNode instanceof ProgramElementNode && className.equals(classNode.getName())) {
					return (ProgramElementNode)classNode;
				}
			}
		}
		return null;
	}


	/**
	 * @param		sourceFilePath	modified to '/' delimited path for consistency
	 * @return		a new structure node for the file if it was not found in the model
	 */
	public StructureNode findRootNodeForSourceFile(String sourceFilePath) {
       	if (!isValid() || sourceFilePath == null) {  
            return StructureModel.NO_STRUCTURE;
        } else {
            String correctedPath = sourceFilePath.replace('\\', '/');
            StructureNode node = (StructureNode)getFileMap().get(correctedPath);//findFileNode(filePath, model);
            if (node != null) {
                return node;
            } else {
                return createFileStructureNode(sourceFilePath);
            }
        }
    }

	/**
	 * Never returns null 
	 * 
	 * @param		sourceFilePath	modified to '/' delimited path for consistency
	 * @param 		lineNumber		if 0 or 1 the corresponding file node will be returned
	 * @return		a new structure node for the file if it was not found in the model
	 */
	public StructureNode findNodeForSourceLine(String sourceFilePath, int lineNumber) {
		String correctedPath = sourceFilePath.replace('\\', '/');
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
		return node != null 
			&& node.getSourceLocation() != null
			&& node.getSourceLocation().getSourceFile().getAbsolutePath().equals(sourceFilePath)
			&& ((node.getSourceLocation().getLine() <= lineNumber
				&& node.getSourceLocation().getEndLine() >= lineNumber)
			    ||
				(lineNumber <= 1
				 && node instanceof ProgramElementNode 
				 && ((ProgramElementNode)node).getProgramElementKind().isSourceFileKind())	
			);
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

