/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.asm.internal;

public class NameConvertor {

	private static final char BOOLEAN = 'Z';
	private static final char BYTE = 'B';
	private static final char CHAR = 'C';
	private static final char DOUBLE = 'D';
	private static final char FLOAT = 'F';
	private static final char INT = 'I';
	private static final char LONG = 'J';
	private static final char SHORT = 'S';
	private static final char ARRAY = '[';
	private static final char RESOLVED = 'L';
	private static final char UNRESOLVED = 'Q';

	public static final char PARAMETERIZED = 'P';

	private static final char[] BOOLEAN_NAME = new char[] { 'b', 'o', 'o', 'l', 'e', 'a', 'n' };
	private static final char[] BYTE_NAME = new char[] { 'b', 'y', 't', 'e' };
	private static final char[] CHAR_NAME = new char[] { 'c', 'h', 'a', 'r' };
	private static final char[] DOUBLE_NAME = new char[] { 'd', 'o', 'u', 'b', 'l', 'e' };
	private static final char[] FLOAT_NAME = new char[] { 'f', 'l', 'o', 'a', 't' };
	private static final char[] INT_NAME = new char[] { 'i', 'n', 't' };
	private static final char[] LONG_NAME = new char[] { 'l', 'o', 'n', 'g' };
	private static final char[] SHORT_NAME = new char[] { 's', 'h', 'o', 'r', 't' };

	private static final char[] SQUARE_BRACKETS = new char[] { '[', ']' };
	private static final char[] GREATER_THAN = new char[] { '>' };
	private static final char[] LESS_THAN = new char[] { '<' };
	private static final char[] COMMA = new char[] { ',' };
	private static final char[] BACKSLASH_LESSTHAN = new char[] { '\\', '<' };
	private static final char[] SEMICOLON = new char[] { ';' };

	/**
	 * Creates a readable name from the given char array, for example, given 'I' returns 'int'. Moreover, given
	 * 'Ljava/lang/String;&lt;Ljava/lang/String;&gt;' returns 'java.lang.String&lt;java.lang.String&gt;'
	 */
	public static char[] convertFromSignature(char[] c) {
		int lt = CharOperation.indexOf('<', c);
		int sc = CharOperation.indexOf(';', c);
		int gt = CharOperation.indexOf('>', c);

		int smallest = 0;
		if (lt == -1 && sc == -1 && gt == -1) {
			// we have something like 'Ljava/lang/String' or 'I'
			return getFullyQualifiedTypeName(c);
		} else if (lt != -1 && (sc == -1 || lt <= sc) && (gt == -1 || lt <= gt)) {
			// we have something like 'Ljava/lang/String<I'
			smallest = lt;
		} else if (sc != -1 && (lt == -1 || sc <= lt) && (gt == -1 || sc <= gt)) {
			// we have something like 'Ljava/lang/String;I'
			smallest = sc;
		} else {
			// we have something like '>;'
			smallest = gt;
		}
		char[] first = CharOperation.subarray(c, 0, smallest);
		char[] second = CharOperation.subarray(c, smallest + 1, c.length);
		if (smallest == 0 && first.length == 0 && c[0] == '>') {
			// c = {'>',';'} therefore we just want to return '>' to
			// close the generic signature
			return GREATER_THAN;
		} else if (first.length == 1 && second.length == 0) {
			return first;
		} else if (second.length == 0 || (second.length == 1 && second[0] == ';')) {
			// we've reached the end of the array, therefore only care about
			// the first part
			return convertFromSignature(first);
		} else if (smallest == lt) {
			// if c = 'Ljava/lang/String;<I' then first = 'Ljava/Lang/String;' and
			// second = 'I'. Want to end up with 'Ljava.lang.String<I' and so add
			// the '<' back.
			char[] inclLT = CharOperation.concat(convertFromSignature(first), LESS_THAN);
			return CharOperation.concat(inclLT, convertFromSignature(second));
		} else if (smallest == gt) {
			char[] inclLT = CharOperation.concat(convertFromSignature(first), GREATER_THAN);
			return CharOperation.concat(inclLT, convertFromSignature(second));
		} else if (second.length != 2) {
			// if c = 'Ljava/lang/Sting;LMyClass' then first = 'Ljava/lang/String'
			// and second = 'LMyClass'. Want to end up with 'java.lang.String,MyClass
			// so want to add a ','. However, only want to do this if we're in the
			// middle of a '<...>'
			char[] inclComma = CharOperation.concat(convertFromSignature(first), COMMA);
			return CharOperation.concat(inclComma, convertFromSignature(second));
		}
		return CharOperation.concat(convertFromSignature(first), convertFromSignature(second));
	}

	/**
	 * Given a char array, returns the type name for this. For example 'I' returns 'int', 'Ljava/lang/String' returns
	 * 'java.lang.String' and '[Ljava/lang/String' returns 'java.lang.String[]'
	 * 
	 * NOTE: Doesn't go any deaper so given 'Ljava/lang/String;<Ljava/lang/String;>' it would return
	 * 'java.lang.String;<Ljava.lang.String;>', however, only called with something like 'Ljava/lang/String'
	 */
	private static char[] getFullyQualifiedTypeName(char[] c) {
		if (c.length == 0) {
			return c;
		}
		if (c[0] == BOOLEAN) {
			return BOOLEAN_NAME;
		} else if (c[0] == BYTE) {
			return BYTE_NAME;
		} else if (c[0] == CHAR) {
			return CHAR_NAME;
		} else if (c[0] == DOUBLE) {
			return DOUBLE_NAME;
		} else if (c[0] == FLOAT) {
			return FLOAT_NAME;
		} else if (c[0] == INT) {
			return INT_NAME;
		} else if (c[0] == LONG) {
			return LONG_NAME;
		} else if (c[0] == SHORT) {
			return SHORT_NAME;
		} else if (c[0] == ARRAY) {
			return CharOperation.concat(getFullyQualifiedTypeName(CharOperation.subarray(c, 1, c.length)), SQUARE_BRACKETS);
		} else {
			char[] type = CharOperation.subarray(c, 1, c.length);
			CharOperation.replace(type, '/', '.');
			return type;
		}
	}

	// public static char[] createShortName(char[] c) {
	// return createShortName(c, false);
	// }

	/**
	 * Given 'Ppkg/MyGenericClass&lt;Ljava/lang/String;Ljava/lang/Integer;&gt;;' will return 'QMyGenericClass&lt;QString;QInteger;&gt;;'
	 */
	public static char[] createShortName(char[] c, boolean haveFullyQualifiedAtLeastOneThing, boolean needsFullyQualifiedFirstEntry) {
		if (c[0] == '[') {
			char[] ret = CharOperation.concat(
					new char[] { '\\', '[' },
					createShortName(CharOperation.subarray(c, 1, c.length), haveFullyQualifiedAtLeastOneThing,
							needsFullyQualifiedFirstEntry));
			return ret;
		} else if (c[0] == '+') {
			char[] ret = CharOperation.concat(
					new char[] { '+' },
					createShortName(CharOperation.subarray(c, 1, c.length), haveFullyQualifiedAtLeastOneThing,
							needsFullyQualifiedFirstEntry));
			return ret;
		} else if (c[0] == '*') {
			return c; // c is *>;
		}
		int lt = CharOperation.indexOf('<', c);
		int sc = CharOperation.indexOf(';', c);
		int gt = CharOperation.indexOf('>', c);

		int smallest = 0;
		if (lt == -1 && sc == -1 && gt == -1) {
			// we have something like 'Ljava/lang/String' or 'I'
			if (!needsFullyQualifiedFirstEntry) {
				return getTypeName(c, true);
			} else {
				return getTypeName(c, haveFullyQualifiedAtLeastOneThing);
			}
		} else if (lt != -1 && (sc == -1 || lt <= sc) && (gt == -1 || lt <= gt)) {
			// we have something like 'Ljava/lang/String<I'
			smallest = lt;
		} else if (sc != -1 && (lt == -1 || sc <= lt) && (gt == -1 || sc <= gt)) {
			// we have something like 'Ljava/lang/String;I'
			smallest = sc;
		} else {
			// we have something like '>;'
			smallest = gt;
		}
		char[] first = CharOperation.subarray(c, 0, smallest);
		char[] second = CharOperation.subarray(c, smallest + 1, c.length);
		if (smallest == 0 && first.length == 0 && c[0] == '>') {
			// c = {'>',';'} therefore we just want to return c to
			// close the generic signature
			return c;
		} else if (first.length == 1 && second.length == 0) {
			return first;
		} else if (second.length == 0 || (second.length == 1 && second[0] == ';')) {
			// we've reached the end of the array, therefore only care about
			// the first part
			return CharOperation.concat(createShortName(first, haveFullyQualifiedAtLeastOneThing, needsFullyQualifiedFirstEntry),
					new char[] { ';' });
		} else if (smallest == lt) {
			// if c = 'Ljava/lang/String;<I' then first = 'Ljava/Lang/String;' and
			// second = 'I'. Want to end up with 'LString<I' and so add
			// the '<' back.
			char[] inclLT = CharOperation.concat(createShortName(first, haveFullyQualifiedAtLeastOneThing, true),
					BACKSLASH_LESSTHAN);
			return CharOperation.concat(inclLT, createShortName(second, true, false));
		} else if (smallest == gt) {
			char[] inclLT = CharOperation.concat(
					createShortName(first, haveFullyQualifiedAtLeastOneThing, needsFullyQualifiedFirstEntry), GREATER_THAN);
			return CharOperation.concat(inclLT, createShortName(second, true, false));
		} else {
			// if c = 'Ljava/lang/Sting;LMyClass;' then first = 'Ljava/lang/String'
			// and second = 'LMyClass;'. Want to end up with 'QString;QMyClass;
			// so add the ';' back
			char[] firstTypeParam = CharOperation.concat(createShortName(first, haveFullyQualifiedAtLeastOneThing, false),
					SEMICOLON);
			return CharOperation.concat(firstTypeParam, createShortName(second, true, false));
		}
	}

	// public static char[] getTypeName(char[] name) {
	// return getTypeName(name, false);
	// }

	/**
	 * Convert a typename into its handle form. There are various cases to consider here - many are discussed in pr249216. The flag
	 * allreadyFQd indicates if we've already included a fq'd name in what we are creating - if we have then further references
	 * should not be fq'd and can be the short name (so java.util.Set becomes just Set).
	 * 
	 */

	/**
	 * Given 'Qjava/lang/String;' returns 'QString;'
	 */
	public static char[] getTypeName(char[] name, boolean haveFullyQualifiedAtLeastOneThing) {
		if (!haveFullyQualifiedAtLeastOneThing) {
			if (name[0] == RESOLVED || name[0] == PARAMETERIZED) {
				char[] sub = CharOperation.subarray(name, 1, name.length);
				CharOperation.replace(sub, '/', '.');
				return CharOperation.concat(new char[] { UNRESOLVED }, sub);
			} else {
				char[] sub = CharOperation.subarray(name, 1, name.length);
				CharOperation.replace(sub, '/', '.');
				return CharOperation.concat(new char[] { name[0] }, sub);
			}
		} else {
			int i = CharOperation.lastIndexOf('/', name);
			if (i != -1) {
				if (name[0] == RESOLVED || name[0] == PARAMETERIZED) {
					return CharOperation.concat(new char[] { UNRESOLVED }, CharOperation.subarray(name, i + 1, name.length));
				} else {
					return CharOperation.concat(new char[] { name[0] }, CharOperation.subarray(name, i + 1, name.length));
				}
			}
		}
		return name;
	}

}
