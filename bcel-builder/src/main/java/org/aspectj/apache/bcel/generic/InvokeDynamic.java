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

package org.aspectj.apache.bcel.generic;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantInvokeDynamic;
import org.aspectj.apache.bcel.classfile.ConstantNameAndType;
import org.aspectj.apache.bcel.classfile.ConstantPool;

/**
 * INVOKEDYNAMIC
 * 
 * @author Andy Clement
 */
public final class InvokeDynamic extends InvokeInstruction {

	public InvokeDynamic(int index, int zeroes) {
		super(Constants.INVOKEDYNAMIC, index);
	}

	public void dump(DataOutputStream out) throws IOException {
		out.writeByte(opcode);
		out.writeShort(index);
		out.writeShort(0);
	}
	
	public String toString(ConstantPool cp) {
		return super.toString(cp) + " " + index;
	}

	public boolean equals(Object other) {
		if (!(other instanceof InvokeDynamic)) {
			return false;
		}
		InvokeDynamic o = (InvokeDynamic) other;
		return o.opcode == opcode && o.index == index;
	}

	public int hashCode() {
		return opcode * 37 + index;
	}
	
	public Type getReturnType(ConstantPool cp) {
		return Type.getReturnType(getSignature(cp));
	}

	public Type[] getArgumentTypes(ConstantPool cp) {
		return Type.getArgumentTypes(getSignature(cp));
	}
	
	public String getSignature(ConstantPool cp) {
		if (signature == null) {
			ConstantInvokeDynamic cid = (ConstantInvokeDynamic)cp.getConstant(index);
			ConstantNameAndType cnat = (ConstantNameAndType) cp.getConstant(cid.getNameAndTypeIndex());
			signature = cp.getConstantUtf8(cnat.getSignatureIndex()).getValue();
		}
		return signature;
	}
	
	@Override
	public String getName(ConstantPool cp) {
		if (name == null) {
			ConstantInvokeDynamic cid = (ConstantInvokeDynamic) cp.getConstant(index);
			ConstantNameAndType cnat = (ConstantNameAndType) cp.getConstant(cid.getNameAndTypeIndex());
			name = cp.getConstantUtf8(cnat.getNameIndex()).getValue();
		}
		return name;
	}
	
	public String getClassName(ConstantPool cp) {
		throw new IllegalStateException("there is no classname for invokedynamic");
	}

}
