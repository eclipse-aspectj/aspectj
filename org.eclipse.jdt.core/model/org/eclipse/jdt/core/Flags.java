/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.internal.compiler.env.IConstants;

/**
 * Utility class for decoding modifier flags in Java elements.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 * @see IMember#getFlags
 */
public final class Flags {

	/**
	 * Public access flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccPublic = IConstants.AccPublic;
	/**
	 * Private access flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccPrivate = IConstants.AccPrivate;
	/**
	 * Protected access flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccProtected = IConstants.AccProtected;
	/**
	 * Static access flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccStatic = IConstants.AccStatic;
	/**
	 * Final access flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccFinal = IConstants.AccFinal;
	/**
	 * Synchronized access flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccSynchronized = IConstants.AccSynchronized;
	/**
	 * Volatile property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccVolatile = IConstants.AccVolatile;
	/**
	 * Transient property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccTransient = IConstants.AccTransient;
	/**
	 * Native property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccNative = IConstants.AccNative;
	/**
	 * Interface property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccInterface = IConstants.AccInterface;
	/**
	 * Abstract property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccAbstract = IConstants.AccAbstract;
	/**
	 * Strictfp property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccStrictfp = IConstants.AccStrictfp;
	/**
	 * Super property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccSuper = IConstants.AccSuper;
	/**
	 * Synthetic property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccSynthetic = IConstants.AccSynthetic;
	/**
	 * Deprecated property flag. See The Java Virtual Machine Specification for more details.
	 * @since 2.0
	 */
	public static final int AccDeprecated = IConstants.AccDeprecated;
	
	/**
	 * Not instantiable.
	 */
	private Flags() {
	}
	/**
	 * Returns whether the given integer includes the <code>abstract</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>abstract</code> modifier is included
	 */
	public static boolean isAbstract(int flags) {
		return (flags & AccAbstract) != 0;
	}
	/**
	 * Returns whether the given integer includes the indication that the 
	 * element is deprecated (<code>@deprecated</code> tag in Javadoc comment).
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the element is marked as deprecated
	 */
	public static boolean isDeprecated(int flags) {
		return (flags & AccDeprecated) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>final</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>final</code> modifier is included
	 */
	public static boolean isFinal(int flags) {
		return (flags & AccFinal) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>interface</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>interface</code> modifier is included
	 * @since 2.0
	 */
	public static boolean isInterface(int flags) {
		return (flags & AccInterface) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>native</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>native</code> modifier is included
	 */
	public static boolean isNative(int flags) {
		return (flags & AccNative) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>private</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>private</code> modifier is included
	 */
	public static boolean isPrivate(int flags) {
		return (flags & AccPrivate) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>protected</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>protected</code> modifier is included
	 */
	public static boolean isProtected(int flags) {
		return (flags & AccProtected) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>public</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>public</code> modifier is included
	 */
	public static boolean isPublic(int flags) {
		return (flags & AccPublic) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>static</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>static</code> modifier is included
	 */
	public static boolean isStatic(int flags) {
		return (flags & AccStatic) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>strictfp</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>strictfp</code> modifier is included
	 */
	public static boolean isStrictfp(int flags) {
		return (flags & AccStrictfp) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>synchronized</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>synchronized</code> modifier is included
	 */
	public static boolean isSynchronized(int flags) {
		return (flags & AccSynchronized) != 0;
	}
	/**
	 * Returns whether the given integer includes the indication that the 
	 * element is synthetic.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the element is marked synthetic
	 */
	public static boolean isSynthetic(int flags) {
		return (flags & AccSynthetic) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>transient</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>transient</code> modifier is included
	 */
	public static boolean isTransient(int flags) {
		return (flags & AccTransient) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>volatile</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>volatile</code> modifier is included
	 */
	public static boolean isVolatile(int flags) {
		return (flags & AccVolatile) != 0;
	}
	/**
	 * Returns a standard string describing the given modifier flags.
	 * Only modifier flags are included in the output; the deprecated and
	 * synthetic flags are ignored if set.
	 * <p>
	 * The flags are output in the following order:
	 * <pre>
	 *   <code>public</code> <code>protected</code> <code>private</code> 
	 *   <code>static</code> 
	 *   <code>abstract</code> <code>final</code> <code>native</code> <code>synchronized</code> <code>transient</code> <code>volatile</code> <code>strictfp</code>
	 * </pre>
	 * This is a compromise between the orders specified in sections 8.1.1,
	 * 8.3.1, 8.4.3, 8.8.3, 9.1.1, and 9.3 of <em>The Java Language 
	 * Specification, Second Edition</em> (JLS2).
	 * </p> 
	 * <p>
	 * Examples results:
	 * <pre>
	 *	  <code>"public static final"</code>
	 *	  <code>"private native"</code>
	 * </pre>
	 * </p>
	 *
	 * @param flags the flags
	 * @return the standard string representation of the given flags
	 */
	public static String toString(int flags) {
		StringBuffer sb = new StringBuffer();

		if (isPublic(flags))
			sb.append("public "); //$NON-NLS-1$
		if (isProtected(flags))
			sb.append("protected "); //$NON-NLS-1$
		if (isPrivate(flags))
			sb.append("private "); //$NON-NLS-1$
		if (isStatic(flags))
			sb.append("static "); //$NON-NLS-1$
		if (isAbstract(flags))
			sb.append("abstract "); //$NON-NLS-1$
		if (isFinal(flags))
			sb.append("final "); //$NON-NLS-1$
		if (isNative(flags))
			sb.append("native "); //$NON-NLS-1$
		if (isSynchronized(flags))
			sb.append("synchronized "); //$NON-NLS-1$
		if (isTransient(flags))
			sb.append("transient "); //$NON-NLS-1$
		if (isVolatile(flags))
			sb.append("volatile "); //$NON-NLS-1$
		if (isStrictfp(flags))
			sb.append("strictfp "); //$NON-NLS-1$

		int len = sb.length();
		if (len == 0)
			return ""; //$NON-NLS-1$
		sb.setLength(len - 1);
		return sb.toString();
	}
}