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

import java.util.EventListener;

/**
 * View renderers get notified of structure view update events and should
 * update the display of the structure view accordingly.
 *
 * @author Mik Kersten
 */
public interface StructureViewRenderer extends EventListener {

	/**
	 * Implementors should updated the display of the corresponding
	 * file structure view.
	 */
	void updateView(StructureView structureView);

    /**
     * Highlights and selects the given node as active.  What "active"
     * means depends on the renderer: a typical activation should cause
     * the corresponding node's sourceline to be highlighted in the
     * active editor.
     */
	void setActiveNode(IStructureViewNode node);

    /**
     * Same behavior as <CODE>setActiveNode(StructureViewNode)</CODE> but
     * highlights a particular line within the span of the node.
     *
     * @param	lineOffset	number of lines after the begin and before the
     * 						end line of the corresponding <CODE>StructureNode</CODE>.
     */
	void setActiveNode(IStructureViewNode activeNode, int lineOffset);
}
