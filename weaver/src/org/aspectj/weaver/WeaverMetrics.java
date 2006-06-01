/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement         initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.util.FuzzyBoolean;


/**
 * Records stats about the weaver.  Information like 'how many types are dismissed during fast match' that
 * may be useful for trying to tune pointcuts.  Not publicised.
 */
public class WeaverMetrics {
    
	// Level 1 of matching is at the type level, which types can be dismissed?
	public static int fastMatchOnTypeAttempted = 0;
	public static int fastMatchOnTypeTrue = 0;
	public static int fastMatchOnTypeFalse = 0;

	// Level 2 of matching is fast matching on the shadows in the remaining types
	public static int fastMatchOnShadowsAttempted = 0;
	public static int fastMatchOnShadowsTrue = 0;
	public static int fastMatchOnShadowsFalse = 0;
	
    // Level 3 of matching is slow matching on the shadows (more shadows than were fast matched on!)
	public static int matchTrue = 0;
	public static int matchAttempted = 0;


	
	public static void reset() {

		fastMatchOnShadowsAttempted = 0;
		fastMatchOnShadowsTrue = 0;
		fastMatchOnShadowsFalse = 0;

		fastMatchOnTypeAttempted = 0;
		fastMatchOnTypeTrue = 0;
		fastMatchOnTypeFalse = 0;
	
		matchTrue = 0;
		matchAttempted = 0;
	}	

	
	public static void dumpInfo() {
		System.err.println("Match summary:");
		int fastMatchOnTypeMaybe = (fastMatchOnTypeAttempted-fastMatchOnTypeTrue-fastMatchOnTypeFalse);
		System.err.print("At the type level, we attempted #"+fastMatchOnTypeAttempted+" fast matches:");
		System.err.println("   YES/NO/MAYBE = "+fastMatchOnTypeTrue+"/"+fastMatchOnTypeFalse+"/"+fastMatchOnTypeMaybe);
		int fastMatchMaybe = (fastMatchOnShadowsAttempted-fastMatchOnShadowsFalse-fastMatchOnShadowsTrue);
		System.err.print("Within those #"+(fastMatchOnTypeTrue+fastMatchOnTypeMaybe)+" possible types, ");
		System.err.print("we fast matched on #"+fastMatchOnShadowsAttempted+" shadows:");
		System.err.println("   YES/NO/MAYBE = "+fastMatchOnShadowsTrue+"/"+fastMatchOnShadowsFalse+"/"+fastMatchMaybe);
		System.err.println("Shadow (non-fast) matches attempted #"+matchAttempted+" of which "+matchTrue+" successful");
	}
	

	public static void recordFastMatchTypeResult(FuzzyBoolean fb) {
		fastMatchOnTypeAttempted++;
		if (fb.alwaysTrue()) fastMatchOnTypeTrue++;
		if (fb.alwaysFalse()) fastMatchOnTypeFalse++;
	}
	
	public static void recordFastMatchResult(FuzzyBoolean fb) {
		fastMatchOnShadowsAttempted++;
		if (fb.alwaysTrue())  fastMatchOnShadowsTrue++;
		if (fb.alwaysFalse()) fastMatchOnShadowsFalse++;
	}
	
	public static void recordMatchResult(boolean b) {
		matchAttempted++;
		if (b) matchTrue++;
	}

}
