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

import org.aspectj.asm.IProgramElement;

/**
 * Uses "typesafe enum" pattern.
 */
public class HandleProviderDelimiter {

	// taken from JavaElement
	public static final HandleProviderDelimiter JAVAPROJECT = new HandleProviderDelimiter('=');
	public static final HandleProviderDelimiter PACKAGEFRAGMENT = new HandleProviderDelimiter('<');
	public static final HandleProviderDelimiter FIELD = new HandleProviderDelimiter('^');
	public static final HandleProviderDelimiter METHOD = new HandleProviderDelimiter('~');
	public static final HandleProviderDelimiter INITIALIZER = new HandleProviderDelimiter('|');
	public static final HandleProviderDelimiter COMPILATIONUNIT = new HandleProviderDelimiter('{');
	public static final HandleProviderDelimiter CLASSFILE = new HandleProviderDelimiter('(');
	public static final HandleProviderDelimiter TYPE = new HandleProviderDelimiter('[');
	public static final HandleProviderDelimiter IMPORTDECLARATION = new HandleProviderDelimiter('#');
	public static final HandleProviderDelimiter COUNT = new HandleProviderDelimiter('!');
	public static final HandleProviderDelimiter ESCAPE = new HandleProviderDelimiter('\\');
	public static final HandleProviderDelimiter PACKAGEDECLARATION = new HandleProviderDelimiter('%');
	public static final HandleProviderDelimiter PACKAGEFRAGMENTROOT = new HandleProviderDelimiter('/');
	// these below are not currently used because no iprogramelement.kind
	// equivalent
	public static final HandleProviderDelimiter LOCALVARIABLE = new HandleProviderDelimiter('@');
	public static final HandleProviderDelimiter TYPE_PARAMETER = new HandleProviderDelimiter(']');

	// AspectJ specific ones
	public static final HandleProviderDelimiter ASPECT_CU = new HandleProviderDelimiter('*');
	public static final HandleProviderDelimiter ADVICE = new HandleProviderDelimiter('&');
	public static final HandleProviderDelimiter ASPECT_TYPE = new HandleProviderDelimiter('\'');
	public static final HandleProviderDelimiter CODEELEMENT = new HandleProviderDelimiter('?');
	public static final HandleProviderDelimiter ITD_FIELD = new HandleProviderDelimiter(',');
	public static final HandleProviderDelimiter ITD = new HandleProviderDelimiter(')');
	public static final HandleProviderDelimiter DECLARE = new HandleProviderDelimiter('`');
	public static final HandleProviderDelimiter POINTCUT = new HandleProviderDelimiter('"');

	public static final HandleProviderDelimiter PHANTOM = new HandleProviderDelimiter(';');

	private static char empty = ' ';
	private final char delim;

	private HandleProviderDelimiter(char delim) {
		this.delim = delim;
	}

	/**
	 * Returns the delimiter for the HandleProviderDelimiter, for example ASPECT returns '*' and METHOD returns '~'
	 */
	public char getDelimiter() {
		return delim;
	}

	/**
	 * Returns the delimiter for the given IProgramElement for example if the IProgramElement is an aspect returns '*' and if the
	 * IProgramElement is a method returns '~'
	 */
	public static char getDelimiter(IProgramElement ipe) {
		IProgramElement.Kind kind = ipe.getKind();
		if (kind.equals(IProgramElement.Kind.PROJECT)) {
			return JAVAPROJECT.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.PACKAGE)) {
			return PACKAGEFRAGMENT.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.FILE_JAVA)) {
			if (ipe.getName().endsWith(".aj")) {
				return ASPECT_CU.getDelimiter();
			} else {
				return COMPILATIONUNIT.getDelimiter();
			}
		} else if (kind.equals(IProgramElement.Kind.FILE_ASPECTJ)) {
			return ASPECT_CU.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.IMPORT_REFERENCE)) {
			return IMPORTDECLARATION.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.PACKAGE_DECLARATION)) {
			return PACKAGEDECLARATION.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.CLASS) || kind.equals(IProgramElement.Kind.INTERFACE)
				|| kind.equals(IProgramElement.Kind.ENUM) || kind.equals(IProgramElement.Kind.ANNOTATION)) {
			return TYPE.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.ASPECT)) {
			if (ipe.isAnnotationStyleDeclaration()) {
				return TYPE.getDelimiter();
			} else {
				return ASPECT_TYPE.getDelimiter();
			}
		} else if (kind.equals(IProgramElement.Kind.INITIALIZER)) {
			return INITIALIZER.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.INTER_TYPE_FIELD)) {
			return ITD_FIELD.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.INTER_TYPE_METHOD) || kind.equals(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR)
				|| kind.equals(IProgramElement.Kind.INTER_TYPE_PARENT)) {
			return ITD.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.CONSTRUCTOR) || kind.equals(IProgramElement.Kind.METHOD)) {
			return METHOD.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.FIELD) || kind.equals(IProgramElement.Kind.ENUM_VALUE)) {
			return FIELD.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.POINTCUT)) {
			if (ipe.isAnnotationStyleDeclaration()) {
				return METHOD.getDelimiter();
			} else {
				return POINTCUT.getDelimiter();
			}
		} else if (kind.equals(IProgramElement.Kind.ADVICE)) {
			if (ipe.isAnnotationStyleDeclaration()) {
				return METHOD.getDelimiter();
			} else {
				return ADVICE.getDelimiter();
			}
		} else if (kind.equals(IProgramElement.Kind.DECLARE_PARENTS) || kind.equals(IProgramElement.Kind.DECLARE_WARNING)
				|| kind.equals(IProgramElement.Kind.DECLARE_ERROR) || kind.equals(IProgramElement.Kind.DECLARE_SOFT)
				|| kind.equals(IProgramElement.Kind.DECLARE_PRECEDENCE)
				|| kind.equals(IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR)
				|| kind.equals(IProgramElement.Kind.DECLARE_ANNOTATION_AT_FIELD)
				|| kind.equals(IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD)
				|| kind.equals(IProgramElement.Kind.DECLARE_ANNOTATION_AT_TYPE)) {
			return DECLARE.getDelimiter();
		} else if (kind.equals(IProgramElement.Kind.CODE)) {
			return CODEELEMENT.getDelimiter();
		} else if (kind == IProgramElement.Kind.FILE) {
			if (ipe.getName().endsWith(".class")) {
				return CLASSFILE.getDelimiter();
			} else if (ipe.getName().endsWith(".aj")) {
				return ASPECT_CU.getDelimiter();
			} else if (ipe.getName().endsWith(".java")) {
				return COMPILATIONUNIT.getDelimiter();
			} else {
				return empty;
			}
		}
		return empty;
	}

}
