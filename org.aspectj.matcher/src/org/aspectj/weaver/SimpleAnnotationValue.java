/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

public class SimpleAnnotationValue extends AnnotationValue {

	public SimpleAnnotationValue(int kind) {
		super(kind);
	}

	public SimpleAnnotationValue(int kind, Object value) {
		super(kind);
		switch (kind) {
		case AnnotationValue.PRIMITIVE_BYTE:
			theByte = ((Byte) value).byteValue();
			break;
		case AnnotationValue.PRIMITIVE_CHAR:
			theChar = ((Character) value).charValue();
			break;
		case AnnotationValue.PRIMITIVE_INT:
			theInt = ((Integer) value).intValue();
			break;
		case AnnotationValue.STRING:
			theString = (String) value;
			break;
		case AnnotationValue.PRIMITIVE_DOUBLE:
			theDouble = ((Double) value).doubleValue();
			break;
		case AnnotationValue.PRIMITIVE_FLOAT:
			theFloat = ((Float) value).floatValue();
			break;
		case AnnotationValue.PRIMITIVE_LONG:
			theLong = ((Long) value).longValue();
			break;
		case AnnotationValue.PRIMITIVE_SHORT:
			theShort = ((Short) value).shortValue();
			break;
		case AnnotationValue.PRIMITIVE_BOOLEAN:
			theBoolean = ((Boolean) value).booleanValue();
			break;
		default:
			throw new BCException("Not implemented for this kind: " + whatKindIsThis(kind));
		}
	}

	private byte theByte;
	private char theChar;
	private int theInt;
	private String theString;
	private double theDouble;
	private float theFloat;
	private long theLong;
	private short theShort;
	private boolean theBoolean;

	public void setValueString(String s) {
		theString = s;
	}

	public void setValueByte(byte b) {
		theByte = b;
	}

	public void setValueChar(char c) {
		theChar = c;
	}

	public void setValueInt(int i) {
		theInt = i;
	}

	public String stringify() {
		switch (valueKind) {
		case 'B': // byte
			return Byte.toString(theByte);
		case 'C': // char
			return new Character(theChar).toString();
		case 'D': // double
			return Double.toString(theDouble);
		case 'F': // float
			return Float.toString(theFloat);
		case 'I': // int
			return Integer.toString(theInt);
		case 'J': // long
			return Long.toString(theLong);
		case 'S': // short
			return Short.toString(theShort);
		case 'Z': // boolean
			return new Boolean(theBoolean).toString();
		case 's': // String
			return theString;
		default:
			throw new BCException("Do not understand this kind: " + valueKind);
		}
	}

	public String toString() {
		return stringify();
	}

}
