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
 *     Alexandre Vasseur    perClause support for @AJ aspects
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.INVOKEINTERFACE;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.MULTIANEWARRAY;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.util.ClassLoaderReference;
import org.aspectj.apache.bcel.util.ClassLoaderRepository;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.NonCachingClassLoaderRepository;
import org.aspectj.apache.bcel.util.Repository;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationOnTypeMunger;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Checker;
import org.aspectj.weaver.ICrossReferenceHandler;
import org.aspectj.weaver.IWeavingSupport;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.NewParentTypeMunger;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.model.AsmRelationshipProvider;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

public class BcelWorld extends World implements Repository {

	private final ClassPathManager classPath;
	protected Repository delegate;
	private BcelWeakClassLoaderReference loaderRef;
	private final BcelWeavingSupport bcelWeavingSupport = new BcelWeavingSupport();

	private static Trace trace = TraceFactory.getTraceFactory().getTrace(BcelWorld.class);

	public BcelWorld() {
		this("");
	}

	public BcelWorld(String cp) {
		this(makeDefaultClasspath(cp), IMessageHandler.THROW, null);
	}

	public IRelationship.Kind determineRelKind(ShadowMunger munger) {
		AdviceKind ak = ((Advice) munger).getKind();
		if (ak.getKey() == AdviceKind.Before.getKey())
			return IRelationship.Kind.ADVICE_BEFORE;
		else if (ak.getKey() == AdviceKind.After.getKey())
			return IRelationship.Kind.ADVICE_AFTER;
		else if (ak.getKey() == AdviceKind.AfterThrowing.getKey())
			return IRelationship.Kind.ADVICE_AFTERTHROWING;
		else if (ak.getKey() == AdviceKind.AfterReturning.getKey())
			return IRelationship.Kind.ADVICE_AFTERRETURNING;
		else if (ak.getKey() == AdviceKind.Around.getKey())
			return IRelationship.Kind.ADVICE_AROUND;
		else if (ak.getKey() == AdviceKind.CflowEntry.getKey() || ak.getKey() == AdviceKind.CflowBelowEntry.getKey()
				|| ak.getKey() == AdviceKind.InterInitializer.getKey() || ak.getKey() == AdviceKind.PerCflowEntry.getKey()
				|| ak.getKey() == AdviceKind.PerCflowBelowEntry.getKey() || ak.getKey() == AdviceKind.PerThisEntry.getKey()
				|| ak.getKey() == AdviceKind.PerTargetEntry.getKey() || ak.getKey() == AdviceKind.Softener.getKey()
				|| ak.getKey() == AdviceKind.PerTypeWithinEntry.getKey()) {
			// System.err.println("Dont want a message about this: "+ak);
			return null;
		}
		throw new RuntimeException("Shadow.determineRelKind: What the hell is it? " + ak);
	}

	public void reportMatch(ShadowMunger munger, Shadow shadow) {
		if (getCrossReferenceHandler() != null) {
			getCrossReferenceHandler().addCrossReference(munger.getSourceLocation(), // What is being applied
					shadow.getSourceLocation(), // Where is it being applied
					determineRelKind(munger).getName(), // What kind of advice?
					((Advice) munger).hasDynamicTests() // Is a runtime test being stuffed in the code?
					);
		}

		// TAG: WeavingMessage
		if (!getMessageHandler().isIgnoring(IMessage.WEAVEINFO)) {
			reportWeavingMessage(munger, shadow);
		}

		if (getModel() != null) {
			// System.err.println("munger: " + munger + " on " + this);
			AsmRelationshipProvider.adviceMunger(getModelAsAsmManager(), shadow, munger);
		}
	}

	/*
	 * Report a message about the advice weave that has occurred. Some messing about to make it pretty ! This code is just asking
	 * for an NPE to occur ...
	 */
	private void reportWeavingMessage(ShadowMunger munger, Shadow shadow) {
		Advice advice = (Advice) munger;
		AdviceKind aKind = advice.getKind();
		// Only report on interesting advice kinds ...
		if (aKind == null || advice.getConcreteAspect() == null) {
			// We suspect someone is programmatically driving the weaver
			// (e.g. IdWeaveTestCase in the weaver testcases)
			return;
		}
		if (!(aKind.equals(AdviceKind.Before) || aKind.equals(AdviceKind.After) || aKind.equals(AdviceKind.AfterReturning)
				|| aKind.equals(AdviceKind.AfterThrowing) || aKind.equals(AdviceKind.Around) || aKind.equals(AdviceKind.Softener)))
			return;

		// synchronized blocks are implemented with multiple monitor_exit instructions in the bytecode
		// (one for normal exit from the method, one for abnormal exit), we only want to tell the user
		// once we have advised the end of the sync block, even though under the covers we will have
		// woven both exit points
		if (shadow.getKind() == Shadow.SynchronizationUnlock) {
			if (advice.lastReportedMonitorExitJoinpointLocation == null) {
				// this is the first time through, let's continue...
				advice.lastReportedMonitorExitJoinpointLocation = shadow.getSourceLocation();
			} else {
				if (areTheSame(shadow.getSourceLocation(), advice.lastReportedMonitorExitJoinpointLocation)) {
					// Don't report it again!
					advice.lastReportedMonitorExitJoinpointLocation = null;
					return;
				}
				// hmmm, this means some kind of nesting is going on, urgh
				advice.lastReportedMonitorExitJoinpointLocation = shadow.getSourceLocation();
			}
		}

		String description = advice.getKind().toString();
		String advisedType = shadow.getEnclosingType().getName();
		String advisingType = advice.getConcreteAspect().getName();
		Message msg = null;
		if (advice.getKind().equals(AdviceKind.Softener)) {
			msg = WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_SOFTENS, new String[] { advisedType,
					beautifyLocation(shadow.getSourceLocation()), advisingType, beautifyLocation(munger.getSourceLocation()) },
					advisedType, advisingType);
		} else {
			boolean runtimeTest = advice.hasDynamicTests();
			String joinPointDescription = shadow.toString();
			msg = WeaveMessage.constructWeavingMessage(WeaveMessage.WEAVEMESSAGE_ADVISES, new String[] { joinPointDescription,
					advisedType, beautifyLocation(shadow.getSourceLocation()), description, advisingType,
					beautifyLocation(munger.getSourceLocation()), (runtimeTest ? " [with runtime test]" : "") }, advisedType,
					advisingType);
			// Boolean.toString(runtimeTest)});
		}
		getMessageHandler().handleMessage(msg);
	}

	private boolean areTheSame(ISourceLocation locA, ISourceLocation locB) {
		if (locA == null)
			return locB == null;
		if (locB == null)
			return false;
		if (locA.getLine() != locB.getLine())
			return false;
		File fA = locA.getSourceFile();
		File fB = locA.getSourceFile();
		if (fA == null)
			return fB == null;
		if (fB == null)
			return false;
		return fA.getName().equals(fB.getName());
	}

	/*
	 * Ensure we report a nice source location - particular in the case where the source info is missing (binary weave).
	 */
	private String beautifyLocation(ISourceLocation isl) {
		StringBuffer nice = new StringBuffer();
		if (isl == null || isl.getSourceFile() == null || isl.getSourceFile().getName().indexOf("no debug info available") != -1) {
			nice.append("no debug info available");
		} else {
			// can't use File.getName() as this fails when a Linux box encounters a path created on Windows and vice-versa
			int takeFrom = isl.getSourceFile().getPath().lastIndexOf('/');
			if (takeFrom == -1) {
				takeFrom = isl.getSourceFile().getPath().lastIndexOf('\\');
			}
			int binary = isl.getSourceFile().getPath().lastIndexOf('!');
			if (binary != -1 && binary < takeFrom) {
				// we have been woven by a binary aspect
				String pathToBinaryLoc = isl.getSourceFile().getPath().substring(0, binary + 1);
				if (pathToBinaryLoc.indexOf(".jar") != -1) {
					// only want to add the extra info if we're from a jar file
					int lastSlash = pathToBinaryLoc.lastIndexOf('/');
					if (lastSlash == -1) {
						lastSlash = pathToBinaryLoc.lastIndexOf('\\');
					}
					nice.append(pathToBinaryLoc.substring(lastSlash + 1));
				}
			}
			nice.append(isl.getSourceFile().getPath().substring(takeFrom + 1));
			if (isl.getLine() != 0)
				nice.append(":").append(isl.getLine());
			// if it's a binary file then also want to give the file name
			if (isl.getSourceFileName() != null)
				nice.append("(from " + isl.getSourceFileName() + ")");
		}
		return nice.toString();
	}

	private static List makeDefaultClasspath(String cp) {
		List classPath = new ArrayList();
		classPath.addAll(getPathEntries(cp));
		classPath.addAll(getPathEntries(ClassPath.getClassPath()));
		return classPath;

	}

	private static List getPathEntries(String s) {
		List ret = new ArrayList();
		StringTokenizer tok = new StringTokenizer(s, File.pathSeparator);

		while (tok.hasMoreTokens())
			ret.add(tok.nextToken());

		return ret;
	}

	public BcelWorld(List classPath, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		// this.aspectPath = new ClassPathManager(aspectPath, handler);
		this.classPath = new ClassPathManager(classPath, handler);
		setMessageHandler(handler);
		setCrossReferenceHandler(xrefHandler);
		// Tell BCEL to use us for resolving any classes
		delegate = this;
	}

	public BcelWorld(ClassPathManager cpm, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		classPath = cpm;
		setMessageHandler(handler);
		setCrossReferenceHandler(xrefHandler);
		// Tell BCEL to use us for resolving any classes
		delegate = this;
	}

	/**
	 * Build a World from a ClassLoader, for LTW support
	 * 
	 * @param loader
	 * @param handler
	 * @param xrefHandler
	 */
	public BcelWorld(ClassLoader loader, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		classPath = null;
		loaderRef = new BcelWeakClassLoaderReference(loader);
		setMessageHandler(handler);
		setCrossReferenceHandler(xrefHandler);
		// Tell BCEL to use us for resolving any classes
		// delegate = getClassLoaderRepositoryFor(loader);
	}

	public void ensureRepositorySetup() {
		if (delegate == null) {
			delegate = getClassLoaderRepositoryFor(loaderRef);
		}
	}

	public Repository getClassLoaderRepositoryFor(ClassLoaderReference loader) {
		if (bcelRepositoryCaching) {
			return new ClassLoaderRepository(loader);
		} else {
			return new NonCachingClassLoaderRepository(loader);
		}
	}

	public void addPath(String name) {
		classPath.addPath(name, this.getMessageHandler());
	}

	// ---- various interactions with bcel

	public static Type makeBcelType(UnresolvedType type) {
		return Type.getType(type.getErasureSignature());
	}

	static Type[] makeBcelTypes(UnresolvedType[] types) {
		Type[] ret = new Type[types.length];
		for (int i = 0, len = types.length; i < len; i++) {
			ret[i] = makeBcelType(types[i]);
		}
		return ret;
	}

	static String[] makeBcelTypesAsClassNames(UnresolvedType[] types) {
		String[] ret = new String[types.length];
		for (int i = 0, len = types.length; i < len; i++) {
			ret[i] = types[i].getName();
		}
		return ret;
	}

	public static UnresolvedType fromBcel(Type t) {
		return UnresolvedType.forSignature(t.getSignature());
	}

	static UnresolvedType[] fromBcel(Type[] ts) {
		UnresolvedType[] ret = new UnresolvedType[ts.length];
		for (int i = 0, len = ts.length; i < len; i++) {
			ret[i] = fromBcel(ts[i]);
		}
		return ret;
	}

	public ResolvedType resolve(Type t) {
		return resolve(fromBcel(t));
	}

	protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
		String name = ty.getName();
		ensureAdvancedConfigurationProcessed();
		JavaClass jc = lookupJavaClass(classPath, name);
		if (jc == null) {
			return null;
		} else {
			return buildBcelDelegate(ty, jc, false);
		}
	}

	public BcelObjectType buildBcelDelegate(ReferenceType resolvedTypeX, JavaClass jc, boolean exposedToWeaver) {
		BcelObjectType ret = new BcelObjectType(resolvedTypeX, jc, exposedToWeaver);
		return ret;
	}

	private JavaClass lookupJavaClass(ClassPathManager classPath, String name) {
		if (classPath == null) {
			try {
				ensureRepositorySetup();
				JavaClass jc = delegate.loadClass(name);
				if (trace.isTraceEnabled())
					trace.event("lookupJavaClass", this, new Object[] { name, jc });
				return jc;
			} catch (ClassNotFoundException e) {
				if (trace.isTraceEnabled())
					trace.error("Unable to find class '" + name + "' in repository", e);
				return null;
			}
		}

		try {
			ClassPathManager.ClassFile file = classPath.find(UnresolvedType.forName(name));
			if (file == null)
				return null;

			ClassParser parser = new ClassParser(file.getInputStream(), file.getPath());

			JavaClass jc = parser.parse();
			file.close();
			return jc;
		} catch (IOException ioe) {
			return null;
		}
	}

	public BcelObjectType addSourceObjectType(JavaClass jc) {
		BcelObjectType ret = null;
		String signature = UnresolvedType.forName(jc.getClassName()).getSignature();

		Object fromTheMap = typeMap.get(signature);

		if (fromTheMap != null && !(fromTheMap instanceof ReferenceType)) {
			// what on earth is it then? See pr 112243
			StringBuffer exceptionText = new StringBuffer();
			exceptionText.append("Found invalid (not a ReferenceType) entry in the type map. ");
			exceptionText.append("Signature=[" + signature + "] Found=[" + fromTheMap + "] Class=[" + fromTheMap.getClass() + "]");
			throw new BCException(exceptionText.toString());
		}

		ReferenceType nameTypeX = (ReferenceType) fromTheMap;

		if (nameTypeX == null) {
			if (jc.isGeneric() && isInJava5Mode()) {

				nameTypeX = ReferenceType.fromTypeX(UnresolvedType.forRawTypeName(jc.getClassName()), this);
				ret = buildBcelDelegate(nameTypeX, jc, true);
				ReferenceType genericRefType = new ReferenceType(UnresolvedType.forGenericTypeSignature(signature, ret
						.getDeclaredGenericSignature()), this);
				nameTypeX.setDelegate(ret);
				genericRefType.setDelegate(ret);
				nameTypeX.setGenericType(genericRefType);
				typeMap.put(signature, nameTypeX);
			} else {
				nameTypeX = new ReferenceType(signature, this);
				ret = buildBcelDelegate(nameTypeX, jc, true);
				typeMap.put(signature, nameTypeX);
			}
		} else {
			ret = buildBcelDelegate(nameTypeX, jc, true);
		}
		return ret;
	}

	void deleteSourceObjectType(UnresolvedType ty) {
		typeMap.remove(ty.getSignature());
	}

	public static Member makeFieldJoinPointSignature(LazyClassGen cg, FieldInstruction fi) {
		ConstantPool cpg = cg.getConstantPool();
		return MemberImpl.field(fi.getClassName(cpg),
				(fi.opcode == Constants.GETSTATIC || fi.opcode == Constants.PUTSTATIC) ? Modifier.STATIC : 0, fi.getName(cpg), fi
						.getSignature(cpg));
	}

	public Member makeJoinPointSignatureFromMethod(LazyMethodGen mg, MemberKind kind) {
		Member ret = mg.getMemberView();
		if (ret == null) {
			int mods = mg.getAccessFlags();
			if (mg.getEnclosingClass().isInterface()) {
				mods |= Modifier.INTERFACE;
			}
			return new ResolvedMemberImpl(kind, UnresolvedType.forName(mg.getClassName()), mods, fromBcel(mg.getReturnType()), mg
					.getName(), fromBcel(mg.getArgumentTypes()));
		} else {
			return ret;
		}

	}

	public Member makeJoinPointSignatureForMonitorEnter(LazyClassGen cg, InstructionHandle h) {
		return MemberImpl.monitorEnter();
	}

	public Member makeJoinPointSignatureForMonitorExit(LazyClassGen cg, InstructionHandle h) {
		return MemberImpl.monitorExit();
	}

	public Member makeJoinPointSignatureForArrayConstruction(LazyClassGen cg, InstructionHandle handle) {
		Instruction i = handle.getInstruction();
		ConstantPool cpg = cg.getConstantPool();
		Member retval = null;

		if (i.opcode == Constants.ANEWARRAY) {
			// ANEWARRAY arrayInstruction = (ANEWARRAY)i;
			Type ot = i.getType(cpg);
			UnresolvedType ut = fromBcel(ot);
			ut = UnresolvedType.makeArray(ut, 1);
			retval = MemberImpl.method(ut, Modifier.PUBLIC, ResolvedType.VOID, "<init>", new ResolvedType[] { ResolvedType.INT });
		} else if (i instanceof MULTIANEWARRAY) {
			MULTIANEWARRAY arrayInstruction = (MULTIANEWARRAY) i;
			UnresolvedType ut = null;
			short dimensions = arrayInstruction.getDimensions();
			ObjectType ot = arrayInstruction.getLoadClassType(cpg);
			if (ot != null) {
				ut = fromBcel(ot);
				ut = UnresolvedType.makeArray(ut, dimensions);
			} else {
				Type t = arrayInstruction.getType(cpg);
				ut = fromBcel(t);
			}
			ResolvedType[] parms = new ResolvedType[dimensions];
			for (int ii = 0; ii < dimensions; ii++)
				parms[ii] = ResolvedType.INT;
			retval = MemberImpl.method(ut, Modifier.PUBLIC, ResolvedType.VOID, "<init>", parms);

		} else if (i.opcode == Constants.NEWARRAY) {
			// NEWARRAY arrayInstruction = (NEWARRAY)i;
			Type ot = i.getType();
			UnresolvedType ut = fromBcel(ot);
			retval = MemberImpl.method(ut, Modifier.PUBLIC, ResolvedType.VOID, "<init>", new ResolvedType[] { ResolvedType.INT });
		} else {
			throw new BCException("Cannot create array construction signature for this non-array instruction:" + i);
		}
		return retval;
	}

	public Member makeJoinPointSignatureForMethodInvocation(LazyClassGen cg, InvokeInstruction ii) {
		ConstantPool cpg = cg.getConstantPool();
		String name = ii.getName(cpg);
		String declaring = ii.getClassName(cpg);
		UnresolvedType declaringType = null;

		String signature = ii.getSignature(cpg);

		int modifier = (ii instanceof INVOKEINTERFACE) ? Modifier.INTERFACE
				: (ii.opcode == Constants.INVOKESTATIC) ? Modifier.STATIC : (ii.opcode == Constants.INVOKESPECIAL && !name
						.equals("<init>")) ? Modifier.PRIVATE : 0;

		// in Java 1.4 and after, static method call of super class within
		// subclass method appears
		// as declared by the subclass in the bytecode - but they are not
		// see #104212
		if (ii.opcode == Constants.INVOKESTATIC) {
			ResolvedType appearsDeclaredBy = resolve(declaring);
			// look for the method there
			for (Iterator iterator = appearsDeclaredBy.getMethods(); iterator.hasNext();) {
				ResolvedMember method = (ResolvedMember) iterator.next();
				if (method.isStatic()) {
					if (name.equals(method.getName()) && signature.equals(method.getSignature())) {
						// we found it
						declaringType = method.getDeclaringType();
						break;
					}
				}

			}
		}

		if (declaringType == null) {
			if (declaring.charAt(0) == '[')
				declaringType = UnresolvedType.forSignature(declaring);
			else
				declaringType = UnresolvedType.forName(declaring);
		}
		return MemberImpl.method(declaringType, modifier, name, signature);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("BcelWorld(");
		// buf.append(shadowMungerMap);
		buf.append(")");
		return buf.toString();
	}

	/**
	 * Retrieve a bcel delegate for an aspect - this will return NULL if the delegate is an EclipseSourceType and not a
	 * BcelObjectType - this happens quite often when incrementally compiling.
	 */
	public static BcelObjectType getBcelObjectType(ResolvedType concreteAspect) {
		ReferenceTypeDelegate rtDelegate = ((ReferenceType) concreteAspect).getDelegate();
		if (rtDelegate instanceof BcelObjectType) {
			return (BcelObjectType) rtDelegate;
		} else {
			return null;
		}
	}

	public void tidyUp() {
		// At end of compile, close any open files so deletion of those archives
		// is possible
		classPath.closeArchives();
		typeMap.report();
		ResolvedType.resetPrimitives();
	}

	// / The repository interface methods

	public JavaClass findClass(String className) {
		return lookupJavaClass(classPath, className);
	}

	public JavaClass loadClass(String className) throws ClassNotFoundException {
		return lookupJavaClass(classPath, className);
	}

	public void storeClass(JavaClass clazz) {
		// doesn't need to do anything
	}

	public void removeClass(JavaClass clazz) {
		throw new RuntimeException("Not implemented");
	}

	public JavaClass loadClass(Class clazz) throws ClassNotFoundException {
		throw new RuntimeException("Not implemented");
	}

	public void clear() {
		delegate.clear();
		// throw new RuntimeException("Not implemented");
	}

	/**
	 * The aim of this method is to make sure a particular type is 'ok'. Some operations on the delegate for a type modify it and
	 * this method is intended to undo that... see pr85132
	 */
	public void validateType(UnresolvedType type) {
		ResolvedType result = typeMap.get(type.getSignature());
		if (result == null)
			return; // We haven't heard of it yet
		if (!result.isExposedToWeaver())
			return; // cant need resetting
		ReferenceType rt = (ReferenceType) result;
		rt.ensureConsistent();
		// rt.getDelegate().ensureDelegateConsistent();
		// If we want to rebuild it 'from scratch' then:
		// ClassParser cp = new ClassParser(new
		// ByteArrayInputStream(newbytes),new String(cs));
		// try {
		// rt.setDelegate(makeBcelObjectType(rt,cp.parse(),true));
		// } catch (ClassFormatException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * Apply a single declare parents - return true if we change the type
	 */
	private boolean applyDeclareParents(DeclareParents p, ResolvedType onType) {
		boolean didSomething = false;
		List newParents = p.findMatchingNewParents(onType, true);
		if (!newParents.isEmpty()) {
			didSomething = true;
			BcelObjectType classType = BcelWorld.getBcelObjectType(onType);
			// System.err.println("need to do declare parents for: " + onType);
			for (Iterator j = newParents.iterator(); j.hasNext();) {
				ResolvedType newParent = (ResolvedType) j.next();

				// We set it here so that the imminent matching for ITDs can
				// succeed - we
				// still haven't done the necessary changes to the class file
				// itself
				// (like transform super calls) - that is done in
				// BcelTypeMunger.mungeNewParent()
				// classType.addParent(newParent);
				onType.addParent(newParent);
				ResolvedTypeMunger newParentMunger = new NewParentTypeMunger(newParent);
				newParentMunger.setSourceLocation(p.getSourceLocation());
				onType.addInterTypeMunger(new BcelTypeMunger(newParentMunger, getCrosscuttingMembersSet()
						.findAspectDeclaringParents(p)));
			}
		}
		return didSomething;
	}

	/**
	 * Apply a declare @type - return true if we change the type
	 */
	private boolean applyDeclareAtType(DeclareAnnotation decA, ResolvedType onType, boolean reportProblems) {
		boolean didSomething = false;
		if (decA.matches(onType)) {

			if (onType.hasAnnotation(decA.getAnnotationX().getType())) {
				// already has it
				return false;
			}

			AnnotationAJ annoX = decA.getAnnotationX();

			// check the annotation is suitable for the target
			boolean isOK = checkTargetOK(decA, onType, annoX);

			if (isOK) {
				didSomething = true;
				ResolvedTypeMunger newAnnotationTM = new AnnotationOnTypeMunger(annoX);
				newAnnotationTM.setSourceLocation(decA.getSourceLocation());
				onType.addInterTypeMunger(new BcelTypeMunger(newAnnotationTM, decA.getAspect().resolve(this)));
				decA.copyAnnotationTo(onType);
			}
		}
		return didSomething;
	}

	/**
	 * Checks for an @target() on the annotation and if found ensures it allows the annotation to be attached to the target type
	 * that matched.
	 */
	private boolean checkTargetOK(DeclareAnnotation decA, ResolvedType onType, AnnotationAJ annoX) {
		if (annoX.specifiesTarget()) {
			if ((onType.isAnnotation() && !annoX.allowedOnAnnotationType()) || (!annoX.allowedOnRegularType())) {
				return false;
			}
		}
		return true;
	}

	// Hmmm - very similar to the code in BcelWeaver.weaveParentTypeMungers -
	// this code
	// doesn't need to produce errors/warnings though as it won't really be
	// weaving.
	protected void weaveInterTypeDeclarations(ResolvedType onType) {

		List declareParentsList = getCrosscuttingMembersSet().getDeclareParents();
		if (onType.isRawType())
			onType = onType.getGenericType();
		onType.clearInterTypeMungers();

		List decpToRepeat = new ArrayList();

		boolean aParentChangeOccurred = false;
		boolean anAnnotationChangeOccurred = false;
		// First pass - apply all decp mungers
		for (Iterator i = declareParentsList.iterator(); i.hasNext();) {
			DeclareParents decp = (DeclareParents) i.next();
			boolean typeChanged = applyDeclareParents(decp, onType);
			if (typeChanged) {
				aParentChangeOccurred = true;
			} else { // Perhaps it would have matched if a 'dec @type' had
				// modified the type
				if (!decp.getChild().isStarAnnotation())
					decpToRepeat.add(decp);
			}
		}

		// Still first pass - apply all dec @type mungers
		for (Iterator i = getCrosscuttingMembersSet().getDeclareAnnotationOnTypes().iterator(); i.hasNext();) {
			DeclareAnnotation decA = (DeclareAnnotation) i.next();
			boolean typeChanged = applyDeclareAtType(decA, onType, true);
			if (typeChanged) {
				anAnnotationChangeOccurred = true;
			}
		}

		while ((aParentChangeOccurred || anAnnotationChangeOccurred) && !decpToRepeat.isEmpty()) {
			anAnnotationChangeOccurred = aParentChangeOccurred = false;
			List decpToRepeatNextTime = new ArrayList();
			for (Iterator iter = decpToRepeat.iterator(); iter.hasNext();) {
				DeclareParents decp = (DeclareParents) iter.next();
				boolean typeChanged = applyDeclareParents(decp, onType);
				if (typeChanged) {
					aParentChangeOccurred = true;
				} else {
					decpToRepeatNextTime.add(decp);
				}
			}

			for (Iterator iter = getCrosscuttingMembersSet().getDeclareAnnotationOnTypes().iterator(); iter.hasNext();) {
				DeclareAnnotation decA = (DeclareAnnotation) iter.next();
				boolean typeChanged = applyDeclareAtType(decA, onType, false);
				if (typeChanged) {
					anAnnotationChangeOccurred = true;
				}
			}
			decpToRepeat = decpToRepeatNextTime;
		}
	}

	public IWeavingSupport getWeavingSupport() {
		return bcelWeavingSupport;
	}

	public void reportCheckerMatch(Checker checker, Shadow shadow) {
		IMessage iMessage = new Message(checker.getMessage(), shadow.toString(), checker.isError() ? IMessage.ERROR
				: IMessage.WARNING, shadow.getSourceLocation(), null, new ISourceLocation[] { checker.getSourceLocation() }, true,
				0, -1, -1);

		getMessageHandler().handleMessage(iMessage);

		if (getCrossReferenceHandler() != null) {
			getCrossReferenceHandler()
					.addCrossReference(
							checker.getSourceLocation(),
							shadow.getSourceLocation(),
							(checker.isError() ? IRelationship.Kind.DECLARE_ERROR.getName() : IRelationship.Kind.DECLARE_WARNING
									.getName()), false);

		}

		if (getModel() != null) {
			AsmRelationshipProvider.checkerMunger(getModelAsAsmManager(), shadow, checker);
		}

	}

	public AsmManager getModelAsAsmManager() {
		return (AsmManager) getModel(); // For now... always an AsmManager in a bcel environment
	}

}