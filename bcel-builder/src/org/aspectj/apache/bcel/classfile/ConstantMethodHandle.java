/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * This class is derived from the abstract <A HREF="org.aspectj.apache.bcel.classfile.Constant.html">Constant</A> class and
 * represents a reference to the name and signature of a field or method.
 * 
 * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.8
 * 
 * @author Andy Clement
 * @see Constant
 */
public final class ConstantMethodHandle extends Constant {
	private byte referenceKind;
	private int referenceIndex; 

	ConstantMethodHandle(DataInputStream file) throws IOException {
		this(file.readByte(), file.readUnsignedShort());
	}

	public ConstantMethodHandle(byte referenceKind, int referenceIndex) {
		super(Constants.CONSTANT_MethodHandle);
		this.referenceKind = referenceKind;
		this.referenceIndex = referenceIndex;
	}

	@Override
	public final void dump(DataOutputStream file) throws IOException {
		file.writeByte(tag);
		file.writeByte(referenceKind);
		file.writeShort(referenceIndex);
	}

	public final byte getReferenceKind() {
		return referenceKind;
	}

//	public final String getName(ConstantPool cp) {
//		return cp.constantToString(getNameIndex(), Constants.CONSTANT_Utf8);
//	}
//
//	public final int getSignatureIndex() {
//		return referenceIndex;
//	}
//	
//	public final String getSignature(ConstantPool cp) {
//		return cp.constantToString(getSignatureIndex(), Constants.CONSTANT_Utf8);
//	}

	@Override
	public final String toString() {
		return super.toString() + "(referenceKind=" + referenceKind + ",referenceIndex=" + referenceIndex + ")";
	}

	@Override
	public String getValue() {
		return toString();
	}

	@Override
	public void accept(ClassVisitor v) {
		v.visitConstantMethodHandle(this);
	}

}
