/* *******************************************************************
 * Copyright (c) 2005 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement           initial implementation
 * ******************************************************************/

package org.aspectj.weaver;	

import java.io.DataInputStream;
import java.io.InputStream;

import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

/**
 * Lightweight subclass of DataInputStream that knows what version of the weaver was used to construct the data in it.
 */
public class VersionedDataInputStream extends DataInputStream {
	private WeaverVersionInfo version = new WeaverVersionInfo();// assume we are the latest unless something tells us otherwise...
	public VersionedDataInputStream(InputStream is) { super(is); }
	
	public int getMajorVersion() { return version.getMajorVersion(); }
	public int getMinorVersion() { return version.getMinorVersion(); }
	public long getBuildstamp() { return version.getBuildstamp(); }
	
	public void setVersion(WeaverVersionInfo version) { this.version = version; }
}