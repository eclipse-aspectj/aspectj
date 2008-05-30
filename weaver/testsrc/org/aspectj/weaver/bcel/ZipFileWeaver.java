/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;


//XXX delete very soon
public class ZipFileWeaver {
	File inFile;
	public ZipFileWeaver(File inFile) {
		super();
		this.inFile = inFile;
	}

	public void weave(BcelWeaver weaver, File outFile) throws IOException {
		int count = 0;
		long startTime = System.currentTimeMillis();
		weaver.addJarFile(inFile, new File("."),false);
		weaver.weave(outFile);
		long stopTime = System.currentTimeMillis();
		
		
		System.out.println("handled " + count + " entries, in " + 
				(stopTime-startTime)/1000. + " seconds");
	}
}
