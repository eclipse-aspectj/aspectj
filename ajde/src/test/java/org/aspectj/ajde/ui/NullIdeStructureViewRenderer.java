/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.ajde.ui;


/**
 * @author Mik Kersten
 */
public class NullIdeStructureViewRenderer implements StructureViewRenderer {

	private boolean hasBeenNotified = false;

    public void updateView(StructureView structureView) {
    	hasBeenNotified = true;
    }

    public void setActiveNode(IStructureViewNode node) {
    	// ignored
    }

	public void setActiveNode(IStructureViewNode node, int lineOffset) {
		// ignored
	}

	public boolean getHasBeenNotified() {
		return hasBeenNotified;
	}

	public void setHasBeenNotified(boolean hasBeenNotified) {
		this.hasBeenNotified = hasBeenNotified;
	}
}

