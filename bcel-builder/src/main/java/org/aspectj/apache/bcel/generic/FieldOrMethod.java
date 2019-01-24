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
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantCP;
import org.aspectj.apache.bcel.classfile.ConstantNameAndType;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;

/**
 * Super class for InvokeInstruction and FieldInstruction, since they have some methods in common!
 * 
 * @version $Id: FieldOrMethod.java,v 1.8 2009/10/05 17:35:36 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class FieldOrMethod extends InstructionCP {

	protected String signature;
	protected String name;
	private String classname;

	protected FieldOrMethod(short opcode, int index) {
		super(opcode, index);
	}

	/**
	 * @return signature of referenced method/field.
	 */
	public String getSignature(ConstantPool cp) {
		if (signature == null) {
			Constant c = cp.getConstant(index);
			ConstantCP cmr = (ConstantCP) c;
			ConstantNameAndType cnat = (ConstantNameAndType) cp.getConstant(cmr.getNameAndTypeIndex());
			signature = ((ConstantUtf8) cp.getConstant(cnat.getSignatureIndex())).getValue();
		}
		return signature;
	}

	/**
	 * @return name of referenced method/field.
	 */
	public String getName(ConstantPool cp) {
		if (name == null) {
			ConstantCP cmr = (ConstantCP) cp.getConstant(index);
			ConstantNameAndType cnat = (ConstantNameAndType) cp.getConstant(cmr.getNameAndTypeIndex());
			name = ((ConstantUtf8) cp.getConstant(cnat.getNameIndex())).getValue();
		}
		return name;
	}

	/**
	 * @return name of the referenced class/interface
	 */
	public String getClassName(ConstantPool cp) {
		if (classname == null) {
			ConstantCP cmr = (ConstantCP) cp.getConstant(index);
			String str = cp.getConstantString(cmr.getClassIndex(), CONSTANT_Class);
			if (str.charAt(0) == '[') {
				classname = str;
			} else {
				classname = str.replace('/', '.');
			}
		}
		return classname;
	}

	/**
	 * @return type of the referenced class/interface
	 */
	public ObjectType getClassType(ConstantPool cpg) {
		return new ObjectType(getClassName(cpg));
	}

	/**
	 * @return type of the referenced class/interface
	 */
	@Override
	public ObjectType getLoadClassType(ConstantPool cpg) {
		return getClassType(cpg);
	}
}
