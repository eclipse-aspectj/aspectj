package org.aspectj.apache.bcel.classfile;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

/**
 * Abstract superclass for classes to represent the different constant types in the constant pool of a class file. The classes keep
 * closely to the JVM specification.
 * 
 * @version $Id: Constant.java,v 1.5 2009/09/10 15:35:04 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class Constant implements Cloneable, Node {

	protected byte tag;

	Constant(byte tag) {
		this.tag = tag;
	}

	public final byte getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return Constants.CONSTANT_NAMES[tag] + "[" + tag + "]";
	}

	public abstract void accept(ClassVisitor v);

	public abstract void dump(DataOutputStream dataOutputStream) throws IOException;

	public abstract Object getValue();

	public Constant copy() {
		try {
			return (Constant) super.clone();
		} catch (CloneNotSupportedException e) {
		}

		return null;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	static final Constant readConstant(DataInputStream dis) throws IOException, ClassFormatException {
		byte b = dis.readByte();
		switch (b) {
		case Constants.CONSTANT_Class:
			return new ConstantClass(dis);
		case Constants.CONSTANT_NameAndType:
			return new ConstantNameAndType(dis);
		case Constants.CONSTANT_Utf8:
			return new ConstantUtf8(dis);
		case Constants.CONSTANT_Fieldref:
			return new ConstantFieldref(dis);
		case Constants.CONSTANT_Methodref:
			return new ConstantMethodref(dis);
		case Constants.CONSTANT_InterfaceMethodref:
			return new ConstantInterfaceMethodref(dis);
		case Constants.CONSTANT_String:
			return new ConstantString(dis);
		case Constants.CONSTANT_Integer:
			return new ConstantInteger(dis);
		case Constants.CONSTANT_Float:
			return new ConstantFloat(dis);
		case Constants.CONSTANT_Long:
			return new ConstantLong(dis);
		case Constants.CONSTANT_Double:
			return new ConstantDouble(dis);
		case Constants.CONSTANT_MethodHandle:
			return new ConstantMethodHandle(dis);
		case Constants.CONSTANT_MethodType:
			return new ConstantMethodType(dis);
		case Constants.CONSTANT_InvokeDynamic:
			return new ConstantInvokeDynamic(dis);
		case Constants.CONSTANT_Module:
			return new ConstantModule(dis);
		case Constants.CONSTANT_Package:
			return new ConstantPackage(dis);
		case Constants.CONSTANT_Dynamic:
			return new ConstantDynamic(dis);
		default:
			throw new ClassFormatException("Invalid byte tag in constant pool: " + b);
		}
	}

}
