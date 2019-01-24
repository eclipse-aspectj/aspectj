package org.aspectj.apache.bcel.generic;

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

import org.aspectj.apache.bcel.classfile.ConstantPool;

/**
 * Super class for the GET/PUTxxx family of instructions.
 * 
 * @version $Id: FieldInstruction.java,v 1.7 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class FieldInstruction extends FieldOrMethod {

	public FieldInstruction(short opcode, int index) {
		super(opcode, index);
	}

	public String toString(ConstantPool cp) {
		return org.aspectj.apache.bcel.Constants.OPCODE_NAMES[opcode] + " "
				+ cp.constantToString(index, org.aspectj.apache.bcel.Constants.CONSTANT_Fieldref);
	}

	/**
	 * @return size of field (1 or 2)
	 */
	protected int getFieldSize(ConstantPool cpg) {
		return Type.getTypeSize(getSignature(cpg));
	}

	public Type getType(ConstantPool cpg) {
		return getFieldType(cpg);
	}

	public Type getFieldType(ConstantPool cpg) {
		return Type.getType(getSignature(cpg));
	}

	public String getFieldName(ConstantPool cpg) {
		return getName(cpg);
	}

	public int produceStack(ConstantPool cpg) {
		if (!isStackProducer()) {
			return 0;
		}

		return getFieldSize(cpg); // SAME FOR GETFIELD/GETSTATIC
	}

	public int consumeStack(ConstantPool cpg) {
		if (!isStackConsumer()) {
			return 0;
		}
		if (opcode == GETFIELD) {
			return 1;
		}
		return getFieldSize(cpg) + (opcode == PUTFIELD ? 1 : 0);
	}
}
