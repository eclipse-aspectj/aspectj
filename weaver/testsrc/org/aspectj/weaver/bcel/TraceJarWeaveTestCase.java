/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.IOException;

public class TraceJarWeaveTestCase extends WeaveTestCase {
	{
		regenerate = true;
	}

	public TraceJarWeaveTestCase(String name) {
		super(name);
	}
	
	
	public void testTraceJar() throws IOException {
		world = new BcelWorld(getTraceJar());
		BcelWeaver weaver = new BcelWeaver(world);
		weaver.addLibraryAspect("MyTrace");
		
		UnwovenClassFile classFile = makeUnwovenClassFile(classDir, "DynamicHelloWorld", outDir);
        
        weaver.addClassFile(classFile);
        weaver.prepareForWeave();
		
		weaveTestInner(weaver, classFile, "DynamicHelloWorld", "TraceJarHello");
	}
}
