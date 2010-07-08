/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 * Andy Clement (SpringSource)
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A variation of a DataOutputStream that is linked to a constant pool writer. The linked constant pool can be used to compress
 * objects into to simple index references into the constant pool. The corresponding decompression is done in the
 * VersionedDataInputStream.
 * 
 * @author Andy Clement
 */
public class CompressingDataOutputStream extends DataOutputStream {

	private ConstantPoolWriter constantPoolWriter;
	public boolean compressionEnabled = true;

	public CompressingDataOutputStream(ByteArrayOutputStream baos, ConstantPoolWriter constantPoolWriter) {
		super(baos);
		this.constantPoolWriter = constantPoolWriter;
	}

	public CompressingDataOutputStream(FileOutputStream fos) {
		super(fos);
	}

	public boolean canCompress() {
		return constantPoolWriter != null && compressionEnabled;
	}

	/**
	 * @param signature of the form 'La/b/c/d;'
	 * @return the constant pool index
	 */
	public int compressSignature(String signature) {
		if (constantPoolWriter == null) {
			throw new IllegalStateException();
		}
		return constantPoolWriter.writeUtf8(signature);
	}

	/**
	 * @param filepath a file system path 'c:\a\b\c.txt' or '/a/b/c.txt'
	 * @return the constant pool index
	 */
	public int compressFilepath(String filepath) {
		if (constantPoolWriter == null) {
			throw new IllegalStateException();
		}
		return constantPoolWriter.writeUtf8(filepath);
	}

	/**
	 * @param name a simple name (for example a method or field name)
	 * @return the constant pool index
	 */
	public int compressName(String name) {
		if (constantPoolWriter == null) {
			throw new IllegalStateException();
		}
		return constantPoolWriter.writeUtf8(name);
	}

	/**
	 * @param name a simple name (for example a method or field name)
	 */
	public void writeCompressedName(String name) throws IOException {
		writeShort(compressName(name));
	}

	/**
	 * @param signature of the form 'La/b/c/d;'
	 */
	public void writeCompressedSignature(String signature) throws IOException {
		writeShort(compressSignature(signature));
	}

	/**
	 * @param path a file system path 'c:\a\b\c.txt' or '/a/b/c.txt'
	 */
	public void writeCompressedPath(String path) throws IOException {
		writeShort(compressFilepath(path));
	}

}