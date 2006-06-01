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

import java.util.*;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.*;
//import org.aspectj.asm.internal.*;

/**
 * @author	Mik Kersten
 */
public class StructureSearchManager {

	/**
	 * @param		pattern		case-sensitive substring of node name
	 * 
	 * @return 	null if a corresponding node was not found
	 */
	public List findMatches(
		String pattern, 
		IProgramElement.Kind kind) {
		
		List matches = new ArrayList();
		IHierarchy model = Ajde.getDefault().getStructureModelManager().getHierarchy();
		if (model.equals(IHierarchy.NO_STRUCTURE)) {
			return null;
		} else {
			return findMatchesHelper((IProgramElement)model.getRoot(), pattern, kind, matches);
		}
	}					
	
	
	private List findMatchesHelper(
		IProgramElement node, 
		String pattern, 
		IProgramElement.Kind kind,
		List matches) {
			
		if (node != null && node.getName().indexOf(pattern) != -1) {
			if (kind == null || node.getKind().equals(kind)) {
				matches.add(node);	
			} 
		}
		if (node != null && node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				IProgramElement nextNode = (IProgramElement)it.next();
				if (nextNode instanceof IProgramElement) {
					findMatchesHelper(
							(IProgramElement)nextNode, 
							pattern, 
							kind,
							matches);
				}
			}
		}
		 
		return matches;		
	}
}
