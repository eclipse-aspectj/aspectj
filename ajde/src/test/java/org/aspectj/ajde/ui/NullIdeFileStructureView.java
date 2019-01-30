/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
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


/**
 * @author Mik Kersten
 */
public class NullIdeFileStructureView extends FileStructureView {
	
	private String sourceFilePath = null;
	
	public NullIdeFileStructureView(StructureViewProperties viewProperties) {
		super(viewProperties);	
	} 
	public String getSourceFile() {
		return sourceFilePath;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFilePath = sourceFile;
	}

	public void setRootNode(IStructureViewNode rootNode) {
		super.setRootNode(rootNode);
		notifyViewUpdated();
	}
}
