/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2017 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.aspectj.apache.bcel.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

/**
 * Indicates all the packages of a module that are exported or opened by the module attribute.
 * http://cr.openjdk.java.net/~mr/jigsaw/spec/java-se-9-jvms-diffs.pdf 4.7.26
 * 
 * @author Andy Clement
 */
public final class ModulePackages extends Attribute {

	private static int[] NO_PACKAGES = new int[0];
	private int[] packageIndices;

	public ModulePackages(ModulePackages c) {
		this(c.getNameIndex(), c.getLength(), c.getPackageIndices(), c.getConstantPool());
	}

	public ModulePackages(int nameIndex, int length, int[] packageIndices, ConstantPool cp) {
		super(Constants.ATTR_MODULE_PACKAGES, nameIndex, length, cp);
		setPackageIndices(packageIndices);
	}

	ModulePackages(int nameIndex, int length, DataInputStream stream, ConstantPool cp) throws IOException {
		this(nameIndex, length, (int[]) null, cp);
		int packageIndicesCount = stream.readUnsignedShort();
		packageIndices = new int[packageIndicesCount];
		for (int i = 0; i < packageIndicesCount; i++) {
			packageIndices[i] = stream.readUnsignedShort();
		}
	}

	@Override
	public void accept(ClassVisitor v) {
		v.visitModulePackages(this);
	}

	@Override
	public final void dump(DataOutputStream stream) throws IOException {
		super.dump(stream);
		stream.writeShort(packageIndices.length);
		for (int packageIndex : packageIndices) {
			stream.writeShort(packageIndex);
		}
	}

	public final int[] getPackageIndices() {
		return packageIndices;
	}

	public final void setPackageIndices(int[] packageIndices) {
		if (packageIndices == null) {
			this.packageIndices = NO_PACKAGES;
		} else {
			this.packageIndices = packageIndices;
		}
	}

	@Override
	public final String toString() {
		StringBuffer buf = new StringBuffer();
		for (int packageIndex : packageIndices) {
			buf.append(cpool.getPackageName(packageIndex) + "\n");
		}
		return buf.toString();
	}

}
