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
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.util.FuzzyBoolean;
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

	public static final ShadowMunger[] NONE = new ShadowMunger[0];

	private static int VERSION_1 = 1; // ShadowMunger version for serialization

	protected static final int ShadowMungerAdvice = 1;
	protected static final int ShadowMungerDeow = 2;

	public String handle = null;

	private int shadowMungerKind;

	protected int start, end;
	protected ISourceContext sourceContext;
	private ISourceLocation sourceLocation;
	private ISourceLocation binarySourceLocation;
	private File binaryFile;
	private ResolvedType declaringType;
	private boolean isBinary;
	private boolean checkedIsBinary;

	protected Pointcut pointcut;

	protected ShadowMunger() {
	}

	public ShadowMunger(Pointcut pointcut, int start, int end, ISourceContext sourceContext, int shadowMungerKind) {
		this.shadowMungerKind = shadowMungerKind;
		this.pointcut = pointcut;
		this.start = start;
		this.end = end;
		this.sourceContext = sourceContext;
	}

	/**
	 * All overriding methods should call super
	 */
	public boolean match(Shadow shadow, World world) {
		if (world.isXmlConfigured() && world.isAspectIncluded(declaringType)) {
			TypePattern scoped = world.getAspectScope(declaringType);
			if (scoped != null) {
				// Check the 'cached' exclusion map
				Set<ResolvedType> excludedTypes = world.getExclusionMap().get(declaringType);
				ResolvedType type = shadow.getEnclosingType().resolve(world);
				if (excludedTypes != null && excludedTypes.contains(type)) {
					return false;
				}
				boolean b = scoped.matches(type, TypePattern.STATIC).alwaysTrue();
				if (!b) {
					if (!world.getMessageHandler().isIgnoring(IMessage.INFO)) {
						world.getMessageHandler().handleMessage(
								MessageUtil.info("Type '" + type.getName() + "' not woven by aspect '" + declaringType.getName()
										+ "' due to scope exclusion in XML definition"));
					}
					if (excludedTypes == null) {
						excludedTypes = new HashSet<>();
						excludedTypes.add(type);
						world.getExclusionMap().put(declaringType, excludedTypes);
					} else {
						excludedTypes.add(type);
					}
					return false;
				}
			}
		}
		if (world.areInfoMessagesEnabled() && world.isTimingEnabled()) {
			long starttime = System.nanoTime();
			FuzzyBoolean isMatch = pointcut.match(shadow);
			long endtime = System.nanoTime();
			world.record(pointcut, endtime - starttime);
			return isMatch.maybeTrue();
		} else {
			FuzzyBoolean isMatch = pointcut.match(shadow);
			return isMatch.maybeTrue();
		}
	}

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

	public abstract ResolvedType getConcreteAspect();

	/**
	 * Returns the binarySourceLocation for the given sourcelocation. This isn't cached because it's used when faulting in the
	 * binary nodes and is called with ISourceLocations for all advice, pointcuts and deows contained within the
	 * resolvedDeclaringAspect.
	 */
	public ISourceLocation getBinarySourceLocation(ISourceLocation sl) {
		if (sl == null) {
			return null;
		}
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
	 * Returns the File with pathname to the class file, for example either:<br>
	 * C:\temp \ajcSandbox\workspace\ajcTest16957.tmp\simple.jar!pkg\BinaryAspect.class if the class file is in a jar file, or <br>
	 * C:\temp\ajcSandbox\workspace\ajcTest16957.tmp!pkg\BinaryAspect.class if the class file is in a directory
	 */
	private File getBinaryFile() {
		if (binaryFile == null) {
			String binaryPath = getDeclaringType().getBinaryPath();
			if (binaryPath == null) {
				// Looks like an aspect that has been picked up from the classpath (likely an abstract one
				// being extended). As it didn't come in via inpath or aspectpath the binarypath has not
				// yet been constructed.

				// We can't discover where the file came from now, that info has been lost. So just
				// use "classpath" for now - until we discover we need to get this right.

				binaryPath = "classpath";
				getDeclaringType().setBinaryPath(binaryPath);
				// ReferenceTypeDelegate delegate = ((ReferenceType) getDeclaringType()).getDelegate();
				// if (delegate instanceof BcelObjectType) {
				// grab javaclass... but it doesnt know the originating file
				// }
			}
			if (!binaryPath.contains("!")) {
				File f = getDeclaringType().getSourceLocation().getSourceFile();
				// Replace the source file suffix with .class
				int i = f.getPath().lastIndexOf('.');
				String path = null;
				if (i != -1) {
					path = f.getPath().substring(0, i) + ".class";
				} else {
					path = f.getPath() + ".class";
				}
				binaryFile = new File(binaryPath + "!" + path);
			} else {
				binaryFile = new File(binaryPath);
			}
		}
		return binaryFile;
	}

	/**
	 * Returns whether or not this shadow munger came from a binary aspect - keep a record of whether or not we've checked if we're
	 * binary otherwise we keep calculating the same thing many times
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

	public abstract ShadowMunger concretize(ResolvedType fromType, World world, PerClause clause);

	public abstract void specializeOn(Shadow shadow);

	/**
	 * Implement this munger at the specified shadow, returning a boolean to indicate success.
	 * 
	 * @param shadow the shadow where this munger should be applied
	 * @return true if the implement was successful
	 */
	public abstract boolean implementOn(Shadow shadow);

	public abstract ShadowMunger parameterizeWith(ResolvedType declaringType, Map<String, UnresolvedType> typeVariableMap);

	/**
	 * @return a Collection of ResolvedTypes for all checked exceptions that might be thrown by this munger
	 */
	public abstract Collection<ResolvedType> getThrownExceptions();

	/**
	 * Does the munger have to check that its exception are accepted by the shadow ? It is not the case for annotation style around
	 * advice, for example: that can throw Throwable, even if the advised method does not throw any exceptions.
	 * 
	 * @return true if munger has to check that its exceptions can be thrown based on the shadow
	 */
	public abstract boolean mustCheckExceptions();

	public void write(CompressingDataOutputStream stream) throws IOException {
		stream.writeInt(VERSION_1);
		stream.writeInt(shadowMungerKind); // determines real subclass
		stream.writeInt(start);
		stream.writeInt(end);
		PersistenceSupport.write(stream, sourceContext);
		PersistenceSupport.write(stream, sourceLocation);
		PersistenceSupport.write(stream, binarySourceLocation);
		PersistenceSupport.write(stream, binaryFile);
		declaringType.write(stream);
		stream.writeBoolean(isBinary);
		stream.writeBoolean(checkedIsBinary);
		pointcut.write(stream);
	}

	//
	// public static ShadowMunger read(VersionedDataInputStream stream, World world) throws IOException {
	// stream.readInt();
	// int kind = stream.readInt();
	// ShadowMunger newShadowMunger = null;
	// switch (kind) {
	// case ShadowMungerAdvice:
	// // world.getWeavingSupport().createAdviceMunger(attribute, pointcut, signature)
	// case ShadowMungerDeow:
	// newShadowMunger = Checker.read(stream, world);
	// default:
	// throw new IllegalStateException("Unexpected type of shadow munger found on deserialization: " + kind);
	// }
	// newShadowMunger.binaryFile = null;
	// }
	
	public boolean bindsProceedingJoinPoint() {
		return false;
	}
	
	public boolean isAroundAdvice() {
		return false;
	}

}
