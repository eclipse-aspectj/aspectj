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
import java.util.List;
import java.util.zip.*;

//XXX we believe this is now unneeded
public class UnwovenZipClassFile extends UnwovenClassFile {
	private ZipOutputStream zipOutputStream;
	
	public UnwovenZipClassFile(ZipOutputStream zipOutputStream, String filename, byte[] bytes) {
		super(filename, bytes);
		this.zipOutputStream = zipOutputStream;
	}
	

	public void writeWovenBytes(byte[] bytes, List childClasses) throws IOException {
		//??? we rewrite this every time
		if (!childClasses.isEmpty()) {
			throw new RuntimeException("unimplemented");
		}
		
		ZipEntry newEntry = new ZipEntry(filename);  //??? get compression scheme right
		
		zipOutputStream.putNextEntry(newEntry);
		zipOutputStream.write(bytes);
		zipOutputStream.closeEntry();
		
		writtenBytes = bytes;
	}

}
