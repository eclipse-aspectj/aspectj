/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.bcel.UnwovenClassFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class EclipseUnwovenClassFile extends UnwovenClassFile {

	private DeferredWriteFile file = null;
	private IMessageHandler handler = null;

	public EclipseUnwovenClassFile(IFile file, IMessageHandler handler) {
		super(null, null);
		this.file = new DeferredWriteFile(file);		
		this.handler = handler;
	}

	public String getFilename() {
		return getFile().getLocation().toString();
	}


	public IFile getFile() {
		return file;
	}
	
	public void clear() {
//		javaClass = null;
	}
		
	public byte[] getBytes() {
		bytes = file.getUnwovenBytes();
		return bytes;
	}
	
	public void deleteRealFile() throws IOException {
		try {
			file.deleteRealFile();
		} catch (CoreException ce) {
			throw new RuntimeException("unimplemented");
		}
	}

	public UnwovenClassFile makeInnerClassFile(
		String innerName,
		byte[] bytes) {
		throw new RuntimeException("unimplemented");
	}

	public void writeWovenBytes(byte[] bytes, List childClasses) throws IOException {
		if (!childClasses.isEmpty()) {
			writeChildClasses(childClasses);
		}
		try {
			file.writeWovenBytes(bytes);
			handler.handleMessage(new Message("wrote class file: " + file.getLocation().toFile().getAbsolutePath(), IMessage.INFO, null, null));
		} catch (CoreException ce) {
			//XXX more difficult than this
			throw new IOException(ce.toString());
		}
	}
	
	private void writeChildClasses(List childClasses) throws IOException {
		//??? we only really need to delete writtenChildClasses whose
		//??? names aren't in childClasses; however, it's unclear
		//??? how much that will affect performance
		//XXXdeleteAllChildClasses();

		//XXXchildClasses.removeAll(writtenChildClasses);
		
		for (Iterator iter = childClasses.iterator(); iter.hasNext();) {
			ChildClass childClass = (ChildClass) iter.next();
			writeChildClassFile(childClass.name, childClass.bytes);
			
		}
		
		writtenChildClasses = childClasses;
		
	}
	
	private void writeChildClassFile(String innerName, byte[] bytes) throws IOException {
		try {
			file.writeInnerWovenBytes(innerName, bytes);
			//handler.handleMessage(new Message("wrote class file: " + file.getLocation().toFile().getAbsolutePath(), IMessage.INFO, null, null));
		} catch (CoreException ce) {
			//XXX more difficult than this
			throw new IOException(ce.toString());
		}			
	}


}
