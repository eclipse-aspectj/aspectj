/* *******************************************************************
 * Copyright (c) 2008 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * 
 * @author Andy Clement
 */
public class Utils {

	/**
	 * Check if the annotations contain a SuppressAjWarnings annotation and if that annotation specifies that the given lint message
	 * (identified by its key) should be ignored.
	 * 
	 */
	public static boolean isSuppressing(AnnotationAJ[] anns, String lintkey) {
		if (anns == null) {
			return false;
		}
		// Go through the annotation types on the advice
		for (AnnotationAJ ann : anns) {
			if (UnresolvedType.SUPPRESS_AJ_WARNINGS.getSignature().equals(ann.getTypeSignature())) {
				// Two possibilities:
				// 1. there are no values specified (i.e. @SuppressAjWarnings)
				// 2. there are values specified (i.e. @SuppressAjWarnings("A") or @SuppressAjWarnings({"A","B"})
				String value = ann.getStringFormOfValue("value");
				// Slightly lazy, just doing a string indexof
				if (value == null || value.contains(lintkey)) {
					return true;
				}
			}
		}
		return false;
	}

}
