/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * Modifier flags. The numeric values of these flags match the ones for class
 * files as described in the Java Virtual Machine Specification.
 * <p>
 * <pre>
 * Modifier:
 *    <b>public</b>
 *    <b>protected</b>
 *    <b>private</b>
 *    <b>static</b>
 *    <b>abstract</b>
 *    <b>final</b>
 *    <b>native</b>
 *    <b>synchronized</b>
 *    <b>transient</b>
 *    <b>volatile</b>
 *    <b>strictfp</b>
 * </pre>
 * </p>
 * <p>
 * This class provides constants and static methods only; it is not intended
 * to be instantiated or subclassed.
 * </p>
 * 
 * @since 2.0
 */
public final class Modifier {

	/**
	 * Modifier constant (bit mask, value 0) indicating no modifiers.
	 */
	public static int NONE = 0x0000;

	/**
	 * "public" modifier constant (bit mask).
	 * Applicable to types, methods, constructors, and fields.
	 */
	public static int PUBLIC = 0x0001;

	/**
	 * "private" modifier constant (bit mask).
	 * Applicable to types, methods, constructors, and fields.
	 */
	public static int PRIVATE = 0x0002;

	/**
	 * "protected" modifier constant (bit mask).
	 * Applicable to types, methods, constructors, and fields.
	 */
	public static int PROTECTED = 0x0004;

	/**
	 * "static" modifier constant (bit mask).
	 * Applicable to types, methods, fields, and initializers.
	 */
	public static int STATIC = 0x0008;

	/**
	 * "final" modifier constant (bit mask).
	 * Applicable to types, methods, fields, and variables.
	 */
	public static int FINAL = 0x0010;

	/**
	 * "synchronized" modifier constant (bit mask).
	 * Applicable only to methods.
	 */
	public static int SYNCHRONIZED = 0x0020;

	/**
	 * "volatile" modifier constant (bit mask).
	 * Applicable only to fields.
	 */
	public static int VOLATILE = 0x0040;

	/**
	 * "transient" modifier constant (bit mask).
	 * Applicable only to fields.
	 */
	public static int TRANSIENT = 0x0080;

	/**
	 * "native" modifier constant (bit mask).
	 * Applicable only to methods.
	 */
	public static int NATIVE = 0x0100;

	/**
	 * "abstract" modifier constant (bit mask).
	 * Applicable to types and methods.
	 */
	public static int ABSTRACT = 0x0400;

	/**
	 * "strictfp" modifier constant (bit mask).
	 * Applicable to types and methods.
	 */
	public static int STRICTFP = 0x0800;

	/**
	 * Returns whether the given flags includes the "public" modifier.
	 * Applicable to types, methods, constructors, and fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>PUBLIC</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isPublic(int flags) {
		return (flags & PUBLIC) != 0;
	}

	/**
	 * Returns whether the given flags includes the "private" modifier.
	 * Applicable to types, methods, constructors, and fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>PRIVATE</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isPrivate(int flags) {
		return (flags & PRIVATE) != 0;
	}

	/**
	 * Returns whether the given flags includes the "protected" modifier.
	 * Applicable to types, methods, constructors, and fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>PROTECTED</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isProtected(int flags) {
		return (flags & PROTECTED) != 0;
	}

	/**
	 * Returns whether the given flags includes the "static" modifier.
	 * Applicable to types, methods, fields, and initializers.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>STATIC</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isStatic(int flags) {
		return (flags & STATIC) != 0;
	}

	/**
	 * Returns whether the given flags includes the "final" modifier.
	 * Applicable to types, methods, fields, and variables.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>FINAL</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isFinal(int flags) {
		return (flags & FINAL) != 0;
	}

	/**
	 * Returns whether the given flags includes the "synchronized" modifier.
	 * Applicable only to methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>SYNCHRONIZED</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isSynchronized(int flags) {
		return (flags & SYNCHRONIZED) != 0;
	}

	/**
	 * Returns whether the given flags includes the "volatile" modifier.
	 * Applicable only to fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>VOLATILE</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isVolatile(int flags) {
		return (flags & VOLATILE) != 0;
	}

	/**
	 * Returns whether the given flags includes the "transient" modifier.
	 * Applicable only to fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>TRANSIENT</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isTransient(int flags) {
		return (flags & TRANSIENT) != 0;
	}

	/**
	 * Returns whether the given flags includes the "native" modifier.
	 * Applicable only to methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>NATIVE</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isNative(int flags) {
		return (flags & NATIVE) != 0;
	}

	/**
	 * Returns whether the given flags includes the "abstract" modifier.
	 * Applicable to types and methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>ABSTRACT</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isAbstract(int flags) {
		return (flags & ABSTRACT) != 0;
	}

	/**
	 * Returns whether the given flags includes the "strictfp" modifier.
	 * Applicable to types and methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>STRICTFP</code> bit is
	 *   set, and <code>false</code> otherwise
	 */
	public static boolean isStrictfp(int flags) {
		return (flags & STRICTFP) != 0;
	}
	
	/*
	 * Block instantiation.
	 */
	private Modifier() {
	}
	
}