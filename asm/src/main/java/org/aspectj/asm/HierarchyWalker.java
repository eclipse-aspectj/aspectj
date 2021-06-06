/* *******************************************************************
 * Copyright (c) 2003,2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
