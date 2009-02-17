/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.PartialOrder;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;

/**
 * For every shadow munger, nothing can be done with it until it is concretized. Then...
 * 
 * (Then we call fast match.)
 * 
 * For every shadow munger, for every shadow, first match is called, then (if match returned true) the shadow munger is specialized
 * for the shadow, which may modify state. Then implement is called.
 */
public abstract class ShadowMunger implements PartialOrder.PartialComparable, IHasPosition {

	protected Pointcut pointcut;

	// these three fields hold the source location of this munger
	protected int start, end;
	protected ISourceContext sourceContext;
	private ISourceLocation sourceLocation;
	private ISourceLocation binarySourceLocation;
	private File binaryFile;
	public String handle = null;
	private ResolvedType declaringType; // the type that declared this munger.

	public ShadowMunger(Pointcut pointcut, int start, int end, ISourceContext sourceContext) {
		this.pointcut = pointcut;
		this.start = start;
		this.end = end;
		this.sourceContext = sourceContext;
	}

	public abstract ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause);

	public abstract void specializeOn(Shadow shadow);

	/**
	 * Implement this munger at the specified shadow, returning a boolean to indicate success.
	 * 
	 * @param shadow the shadow where this munger should be applied
	 * @return true if the munger was successful
	 */
	public abstract boolean implementOn(Shadow shadow);

	/**
	 * All overriding methods should call super
	 */
	public boolean match(Shadow shadow, World world) {
		if (world.isXmlConfigured() && world.isAspectIncluded(declaringType)) {
			TypePattern scoped = world.getAspectScope(declaringType);
			if (scoped != null) {
				boolean b = scoped.matches(shadow.getEnclosingType().resolve(world), TypePattern.STATIC).alwaysTrue();
				if (!b) {
					return false;
				}
			}
		}
		return pointcut.match(shadow).maybeTrue();
	}

	public abstract ShadowMunger parameterizeWith(ResolvedType declaringType, Map typeVariableMap);

	public int fallbackCompareTo(Object other) {
		return toString().compareTo(toString());
	}

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

	public ISourceLocation getSourceLocation() {
		if (sourceLocation == null) {
			if (sourceContext != null) {
				sourceLocation = sourceContext.makeSourceLocation(this);
			}
		}
		if (isBinary()) {
			if (binarySourceLocation == null) {
				binarySourceLocation = getBinarySourceLocation(sourceLocation);
			}
			return binarySourceLocation;
		}
		return sourceLocation;
	}

	// ---- fields

	public static final ShadowMunger[] NONE = new ShadowMunger[0];

	public Pointcut getPointcut() {
		return pointcut;
	}

	// pointcut may be updated during rewriting...
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * Invoked when the shadow munger of a resolved type are processed.
	 * 
	 * @param aType
	 */
	public void setDeclaringType(ResolvedType aType) {
		declaringType = aType;
	}

	public ResolvedType getDeclaringType() {
		return declaringType;
	}

	/**
	 * @return a Collection of ResolvedType for all checked exceptions that might be thrown by this munger
	 */
	public abstract Collection getThrownExceptions();

	/**
	 * Does the munger has to check that its exception are accepted by the shadow ? ATAJ: It s not the case for @AJ around advice
	 * f.e. that can throw Throwable, even if the advised method does not throw any exceptions.
	 * 
	 * @return true if munger has to check that its exceptions can be throwned based on the shadow
	 */
	public abstract boolean mustCheckExceptions();

	/**
	 * Returns the binarySourceLocation for the given sourcelocation. This isn't cached because it's used when faulting in the
	 * binary nodes and is called with ISourceLocations for all advice, pointcuts and deows contained within the
	 * resolvedDeclaringAspect.
	 */
	public ISourceLocation getBinarySourceLocation(ISourceLocation sl) {
		if (sl == null)
			return null;
		String sourceFileName = null;
		if (getDeclaringType() instanceof ReferenceType) {
			String s = ((ReferenceType) getDeclaringType()).getDelegate().getSourcefilename();
			int i = s.lastIndexOf('/');
			if (i != -1) {
				sourceFileName = s.substring(i + 1);
			} else {
				sourceFileName = s;
			}
		}
		ISourceLocation sLoc = new SourceLocation(getBinaryFile(), sl.getLine(), sl.getEndLine(),
				((sl.getColumn() == 0) ? ISourceLocation.NO_COLUMN : sl.getColumn()), sl.getContext(), sourceFileName);
		return sLoc;
	}

	/**
	 * Returns the File with pathname to the class file, for example either C:\temp
	 * \ajcSandbox\workspace\ajcTest16957.tmp\simple.jar!pkg\BinaryAspect.class if the class file is in a jar file, or
	 * C:\temp\ajcSandbox\workspace\ajcTest16957.tmp!pkg\BinaryAspect.class if the class file is in a directory
	 */
	private File getBinaryFile() {
		if (binaryFile == null) {
			String s = getDeclaringType().getBinaryPath();
			File f = getDeclaringType().getSourceLocation().getSourceFile();
			// Replace the source file suffix with .class
			int i = f.getPath().lastIndexOf('.');
			String path = null;
			if (i != -1) {
				path = f.getPath().substring(0, i) + ".class";
			} else {
				path = f.getPath() + ".class";
			}
			binaryFile = new File(s + "!" + path);
		}
		return binaryFile;
	}

	/**
	 * Returns whether or not this shadow munger came from a binary aspect - keep a record of whether or not we've checked if we're
	 * binary otherwise we keep caluclating the same thing many times
	 */
	public boolean isBinary() {
		if (!checkedIsBinary) {
			ResolvedType rt = getDeclaringType();
			if (rt != null) {
				isBinary = ((rt.getBinaryPath() == null) ? false : true);
			}
			checkedIsBinary = true;
		}
		return isBinary;
	}

	private boolean isBinary;
	private boolean checkedIsBinary;

}
