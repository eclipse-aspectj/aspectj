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

public abstract class AnnotationValue {

	protected int valueKind;

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

	public abstract String stringify();

	public AnnotationValue(int kind) {
		valueKind = kind;
	}

	public static String whatKindIsThis(int kind) {
		switch (kind) {
		case PRIMITIVE_BYTE: // byte
			return "byte";
		case PRIMITIVE_CHAR: // char
			return "char";
		case PRIMITIVE_DOUBLE: // double
			return "double";
		case PRIMITIVE_FLOAT: // float
			return "float";
		case PRIMITIVE_INT: // int
			return "int";
		case PRIMITIVE_LONG: // long
			return "long";
		case PRIMITIVE_SHORT: // short
			return "short";
		case PRIMITIVE_BOOLEAN: // boolean
			return "boolean";
		case 's': // String
			return "string";
		case 'e': // Enum constant
			return "enum";
		case 'c': // Class
			return "class";
		case '@': // Annotation
			return "annotation";
		case '[': // Array
			return "array";
		default:
			throw new RuntimeException("Dont know what this is : " + kind);
		}
	}
}
