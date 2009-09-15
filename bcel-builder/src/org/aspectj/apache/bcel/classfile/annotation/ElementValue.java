/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation {date}
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.classfile.ConstantPool;

public abstract class ElementValue {

	public static final int STRING = 's';
	public static final int ENUM_CONSTANT = 'e';
	public static final int CLASS = 'c';
	public static final int ANNOTATION = '@';
	public static final int ARRAY = '[';

	public static final int PRIMITIVE_INT = 'I';
	public static final int PRIMITIVE_BYTE = 'B';
	public static final int PRIMITIVE_CHAR = 'C';
	public static final int PRIMITIVE_DOUBLE = 'D';
	public static final int PRIMITIVE_FLOAT = 'F';
	public static final int PRIMITIVE_LONG = 'J';
	public static final int PRIMITIVE_SHORT = 'S';
	public static final int PRIMITIVE_BOOLEAN = 'Z';

	protected int type;
	protected ConstantPool cpool;

	protected ElementValue(int type, ConstantPool cpool) {
		this.type = type;
		this.cpool = cpool;
	}

	public int getElementValueType() {
		return type;
	}

	public abstract String stringifyValue();

	public abstract void dump(DataOutputStream dos) throws IOException;

	public static ElementValue readElementValue(DataInputStream dis, ConstantPool cpGen) throws IOException {
		int type = dis.readUnsignedByte();
		switch (type) {
		case 'B': // byte
			return new SimpleElementValue(PRIMITIVE_BYTE, dis.readUnsignedShort(), cpGen);
		case 'C': // char
			return new SimpleElementValue(PRIMITIVE_CHAR, dis.readUnsignedShort(), cpGen);
		case 'D': // double
			return new SimpleElementValue(PRIMITIVE_DOUBLE, dis.readUnsignedShort(), cpGen);
		case 'F': // float
			return new SimpleElementValue(PRIMITIVE_FLOAT, dis.readUnsignedShort(), cpGen);
		case 'I': // int
			return new SimpleElementValue(PRIMITIVE_INT, dis.readUnsignedShort(), cpGen);
		case 'J': // long
			return new SimpleElementValue(PRIMITIVE_LONG, dis.readUnsignedShort(), cpGen);
		case 'S': // short
			return new SimpleElementValue(PRIMITIVE_SHORT, dis.readUnsignedShort(), cpGen);
		case 'Z': // boolean
			return new SimpleElementValue(PRIMITIVE_BOOLEAN, dis.readUnsignedShort(), cpGen);
		case 's': // String
			return new SimpleElementValue(STRING, dis.readUnsignedShort(), cpGen);

		case 'e': // Enum constant
			return new EnumElementValue(dis.readUnsignedShort(), dis.readUnsignedShort(), cpGen);

		case 'c': // Class
			return new ClassElementValue(dis.readUnsignedShort(), cpGen);

			// FIXME should this be true here? or should it be the value for the containing annotation?
		case '@': // Annotation
			return new AnnotationElementValue(ANNOTATION, AnnotationGen.read(dis, cpGen, true), cpGen);

		case '[': // Array
			int numArrayVals = dis.readUnsignedShort();
			ElementValue[] evalues = new ElementValue[numArrayVals];
			for (int j = 0; j < numArrayVals; j++) {
				evalues[j] = ElementValue.readElementValue(dis, cpGen);
			}
			return new ArrayElementValue(ARRAY, evalues, cpGen);

		default:
			throw new RuntimeException("Unexpected element value kind in annotation: " + type);
		}
	}

	protected ConstantPool getConstantPool() {
		return cpool;
	}

	/**
	 * Creates an (modifiable) ElementValueGen copy of an (immutable) ElementValue - constant pool is assumed correct.
	 */
	public static ElementValue copy(ElementValue value, ConstantPool cpool, boolean copyPoolEntries) {
		switch (value.getElementValueType()) {
		case 'B': // byte
		case 'C': // char
		case 'D': // double
		case 'F': // float
		case 'I': // int
		case 'J': // long
		case 'S': // short
		case 'Z': // boolean
		case 's': // String
			return new SimpleElementValue((SimpleElementValue) value, cpool, copyPoolEntries);

		case 'e': // Enum constant
			return new EnumElementValue((EnumElementValue) value, cpool, copyPoolEntries);

		case '@': // Annotation
			return new AnnotationElementValue((AnnotationElementValue) value, cpool, copyPoolEntries);

		case '[': // Array
			return new ArrayElementValue((ArrayElementValue) value, cpool, copyPoolEntries);

		case 'c': // Class
			return new ClassElementValue((ClassElementValue) value, cpool, copyPoolEntries);

		default:
			throw new RuntimeException("Not implemented yet! (" + value.getElementValueType() + ")");
		}
	}
}
