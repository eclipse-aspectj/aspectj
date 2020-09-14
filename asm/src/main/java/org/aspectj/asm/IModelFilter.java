/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.asm;

/**
 * When dumping the model out (for debugging/testing), various parts of it can be passed through this filter. Currently it is used
 * to ensure the source locations we dump out are independent of sandbox directory.
 * 
 * @author Andy Clement
 */
public interface IModelFilter {

	/**
	 * Called when about to dump out an absolute file location, enabling it to be altered (eg.
	 * c:/temp/ajcsSandbox/foo/ajctemp.12323/&lt;BLAH&gt; could become TEST_SANDBOX/&lt;BLAH&gt;
	 */
	String processFilelocation(String loc);

	/**
	 * When the relationship map is dumped, lines are prefixed with a handle ID. Return true if you want these, false if you do not.
	 */
	boolean wantsHandleIds();
}
