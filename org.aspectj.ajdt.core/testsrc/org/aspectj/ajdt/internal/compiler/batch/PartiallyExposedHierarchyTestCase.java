/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.batch;

import java.io.File;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

/**
 * If you need to rebuild the components for this test, I'm afraid you will have
 * to run build.cmd in the testdata/partialHierarchy directory which calls ajc and
 * does some jar manipulation.
 */
public class PartiallyExposedHierarchyTestCase extends AjcTestCase {

	public static final String PROJECT_DIR = "partialHierarchy";

	private File baseDir;
	
	protected void setUp() throws Exception {
		super.setUp();
		baseDir = new File("../org.aspectj.ajdt.core/testdata",PROJECT_DIR);
	}
	
	/**
	 * This test verifies that AspectJ behaves correctly when parts of an object
	 * hierarchy are exposed to it for weaving.  See pr49657 for all the details.
	 */
	public void testPartiallyExposedHierarchy () {
		Message warning = new Message(11,"no interface constructor-execution join point");
		Message error   = new Message(15, "type sample.Base must be accessible for weaving interface inter type declaration from aspect sample.Trace");
		CompilationResult result = ajc(baseDir,
				new String[]{"-classpath","fullBase.jar",
							 "-injars","base.jar",
							 "sample"+File.separator+"Trace.aj"});
//		System.err.println(result.getWarningMessages());
//		System.err.println(result.getErrorMessages());
//		System.err.println(result.getStandardOutput());
		MessageSpec spec = new MessageSpec(null,newMessageList(warning),newMessageList(error));
		assertMessages(result,spec);
	}
	

}
