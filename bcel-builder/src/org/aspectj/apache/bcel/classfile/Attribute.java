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
import java.io.Serializable;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisParamAnnos;

/**
 * Abstract super class for <em>Attribute</em> objects. Currently the <em>ConstantValue</em>, <em>SourceFile</em>, <em>Code</em>,
 * <em>Exceptiontable</em>, <em>LineNumberTable</em>, <em>LocalVariableTable</em>, <em>InnerClasses</em> and <em>Synthetic</em>
 * attributes are supported. The <em>Unknown</em> attribute stands for non-standard-attributes.
 * 
 * @version $Id: Attribute.java,v 1.9 2009/12/09 18:01:31 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see ConstantValue
 * @see SourceFile
 * @see Code
 * @see Unknown
 * @see ExceptionTable
 * @see LineNumberTable
 * @see LocalVariableTable
 * @see InnerClasses
 * @see Synthetic
 * @see Deprecated
 * @see Signature
 */
public abstract class Attribute implements Cloneable, Node, Serializable {
	public final static Attribute[] NoAttributes = new Attribute[0];

	protected byte tag; // Tag to distinguish subclasses
	protected int nameIndex;
	protected int length;
	protected ConstantPool cpool;

	protected Attribute(byte tag, int nameIndex, int length, ConstantPool cpool) {
		this.tag = tag;
		this.nameIndex = nameIndex;
		this.length = length;
		this.cpool = cpool;
	}

	public void dump(DataOutputStream file) throws IOException {
		file.writeShort(nameIndex);
		file.writeInt(length);
	}

	// OPTIMIZE how about just reading them in and storing them until we need to decode what they really are?
	public static final Attribute readAttribute(DataInputStream file, ConstantPool cpool) throws IOException {
		byte tag = Constants.ATTR_UNKNOWN;
		int idx = file.readUnsignedShort();
		String name = cpool.getConstantUtf8(idx).getValue();
		int len = file.readInt();

		// Compare strings to find known attribute
		for (byte i = 0; i < Constants.KNOWN_ATTRIBUTES; i++) {
			if (name.equals(Constants.ATTRIBUTE_NAMES[i])) {
				tag = i;
				break;
			}
		}
		switch (tag) {
		case Constants.ATTR_UNKNOWN:
			return new Unknown(idx, len, file, cpool);
		case Constants.ATTR_CONSTANT_VALUE:
			return new ConstantValue(idx, len, file, cpool);
		case Constants.ATTR_SOURCE_FILE:
			return new SourceFile(idx, len, file, cpool);
		case Constants.ATTR_CODE:
			return new Code(idx, len, file, cpool);
		case Constants.ATTR_EXCEPTIONS:
			return new ExceptionTable(idx, len, file, cpool);
		case Constants.ATTR_LINE_NUMBER_TABLE:
			return new LineNumberTable(idx, len, file, cpool);
		case Constants.ATTR_LOCAL_VARIABLE_TABLE:
			return new LocalVariableTable(idx, len, file, cpool);
		case Constants.ATTR_INNER_CLASSES:
			return new InnerClasses(idx, len, file, cpool);
		case Constants.ATTR_SYNTHETIC:
			return new Synthetic(idx, len, file, cpool);
		case Constants.ATTR_DEPRECATED:
			return new Deprecated(idx, len, file, cpool);
		case Constants.ATTR_SIGNATURE:
			return new Signature(idx, len, file, cpool);
		case Constants.ATTR_STACK_MAP:
			return new StackMap(idx, len, file, cpool);
		case Constants.ATTR_RUNTIME_VISIBLE_ANNOTATIONS:
			return new RuntimeVisAnnos(idx, len, file, cpool);
		case Constants.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS:
			return new RuntimeInvisAnnos(idx, len, file, cpool);
		case Constants.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
			return new RuntimeVisParamAnnos(idx, len, file, cpool);
		case Constants.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
			return new RuntimeInvisParamAnnos(idx, len, file, cpool);
		case Constants.ATTR_ANNOTATION_DEFAULT:
			return new AnnotationDefault(idx, len, file, cpool);
		case Constants.ATTR_LOCAL_VARIABLE_TYPE_TABLE:
			return new LocalVariableTypeTable(idx, len, file, cpool);
		case Constants.ATTR_ENCLOSING_METHOD:
			return new EnclosingMethod(idx, len, file, cpool);
		case Constants.ATTR_BOOTSTRAPMETHODS:
			return new BootstrapMethods(idx,len,file,cpool);
		default:
			throw new IllegalStateException();
		}
	}

	public String getName() {
		return cpool.getConstantUtf8(nameIndex).getValue();
	}

	public final int getLength() {
		return length;
	}

	public final int getNameIndex() {
		return nameIndex;
	}

	public final byte getTag() {
		return tag;
	}

	public final ConstantPool getConstantPool() {
		return cpool;
	}

	@Override
	public String toString() {
		return Constants.ATTRIBUTE_NAMES[tag];
	}

	public abstract void accept(ClassVisitor v);

}
