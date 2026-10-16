/* *******************************************************************
 * Copyright (c) 2005-2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.weaver;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

/**
 * Lightweight subclass of DataInputStream that knows what version of the weaver was used to construct the data in it. The input
 * stream has a constant pool reader attached which enables it to decode constant pool references found within the data being read.
 *
 * @author Andy Clement
 */
public class VersionedDataInputStream extends DataInputStream {

	private static final int MAX_ARRAY_LENGTH = 0xFFFF;

	private WeaverVersionInfo version = new WeaverVersionInfo();// assume we are the latest unless something tells us otherwise...

	private ConstantPoolReader constantPoolReader;

	public VersionedDataInputStream(ByteArrayInputStream bais, ConstantPoolReader constantPoolReader) {
		super(bais);
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

	/**
	 * Checks a count that has just been read from the stream and is about to be
	 * used to size an array. Every element occupies at least one byte, so a count
	 * that is negative, crazy large, or bigger than the number of bytes
	 * remaining cannot be genuine and is rejected before anything is allocated.
	 * This relies on available() being exact, which is guaranteed now the constructor
	 * takes a ByteArrayInputStream.
	 */
	public int checkArrayLength(int length) throws IOException {
		if (length < 0 || length > MAX_ARRAY_LENGTH || length > available()) {
			throw new BCException("Invalid array length in attribute data: " + length);
		}
		return length;
	}
}
