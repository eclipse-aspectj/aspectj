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

package org.aspectj.ajde.ui;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;

/**
 * @author Mik Kersten
 */
public class StructureSearchManager {

	/**
	 * @param pattern case-sensitive substring of node name
	 *
	 * @return null if a corresponding node was not found
	 */
	public List<IProgramElement> findMatches(String pattern, IProgramElement.Kind kind) {

		List<IProgramElement> matches = new ArrayList<>();
		IHierarchy model = AsmManager.lastActiveStructureModel.getHierarchy();
		if (model.getRoot().equals(IHierarchy.NO_STRUCTURE)) {
			return null;
		} else {
			return findMatchesHelper(model.getRoot(), pattern, kind, matches);
		}
	}

	private List<IProgramElement> findMatchesHelper(IProgramElement node, String pattern, IProgramElement.Kind kind, List<IProgramElement> matches) {

		if (node != null && node.getName().contains(pattern)) {
			if (kind == null || node.getKind().equals(kind)) {
				matches.add(node);
			}
		}
		if (node != null && node.getChildren() != null) {
			for (IProgramElement nextNode : node.getChildren()) {
				if (nextNode != null) {
					findMatchesHelper(nextNode, pattern, kind, matches);
				}
			}
		}

		return matches;
	}
}
