/* *******************************************************************
 * Copyright (c) 2005-2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 * Andy Clement (IBM, SpringSource)
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

/**
 * Lightweight subclass of DataInputStream that knows what version of the weaver was used to construct the data in it. The input
 * stream has a constant pool reader attached which enables it to decode constant pool references found within the data being read.
 * 
 * @author Andy Clement
 */
public class VersionedDataInputStream extends DataInputStream {

	private WeaverVersionInfo version = new WeaverVersionInfo();// assume we are the latest unless something tells us otherwise...

	private ConstantPoolReader constantPoolReader;

	public VersionedDataInputStream(InputStream is, ConstantPoolReader constantPoolReader) {
		super(is);
		this.constantPoolReader = constantPoolReader;
	}

	public int getMajorVersion() {
		return version.getMajorVersion();
	}

	public int getMinorVersion() {
		return version.getMinorVersion();
	}

	public long getBuildstamp() {
		return version.getBuildstamp();
	}

	public void setVersion(WeaverVersionInfo version) {
		this.version = version;
	}

	public String readUtf8(int cpIndex) {
		if (constantPoolReader == null) {
			throw new IllegalStateException();
		}
		if (cpIndex < 0) {
			throw new IllegalStateException(cpIndex + "");
		}
		return constantPoolReader.readUtf8(cpIndex);
	}

	public boolean canDecompress() {
		return constantPoolReader != null;
	}

	public boolean isAtLeast169() {
		return getMajorVersion() >= AjAttribute.WeaverVersionInfo.WEAVER_VERSION_AJ169;
	}

	public String readPath() throws IOException {
		return readUtf8(readShort());
	}

	public String readSignature() throws IOException {
		return readUtf8(readShort());
	}

	public UnresolvedType readSignatureAsUnresolvedType() throws IOException {
		return UnresolvedType.forSignature(readUtf8(readShort()));
	}

	public String toString() {
		return "VersionedDataInputStream: version=" + version + " constantPoolReader?" + (constantPoolReader != null);
	}
}