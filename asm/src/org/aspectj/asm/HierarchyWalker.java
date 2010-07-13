/* *******************************************************************
 * Copyright (c) 2003,2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 *     Andy Clement
 * ******************************************************************/

package org.aspectj.asm;

/**
 * @author Mik Kersten
 * @author Andy Clement
 */
public abstract class HierarchyWalker {

	public HierarchyWalker() {
	}

	protected void preProcess(IProgramElement node) {
	}

	protected void postProcess(IProgramElement node) {
	} 

	public IProgramElement process(IProgramElement node) {
		preProcess(node);
		node.walk(this);
		postProcess(node);
		return node;
	}
}
