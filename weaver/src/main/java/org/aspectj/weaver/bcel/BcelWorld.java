/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.aspectj.asm.internal.CharOperation;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationOnTypeMunger;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.BytecodeWorld;
import org.aspectj.weaver.ClassPathManager;
import org.aspectj.weaver.Clazz;
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
import org.aspectj.weaver.TypeDelegateResolver;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareParents;

public class BcelWorld extends BytecodeWorld/* implements Repository*/ {

	private final ClassPathManager classPath;
	protected Repository delegate;
	private BcelWeakClassLoaderReference loaderRef;
	private final BcelWeavingSupport bcelWeavingSupport = new BcelWeavingSupport();

	public BcelWorld() {
		this("");
	}

	public BcelWorld(String cp) {
		this(makeDefaultClasspath(cp), IMessageHandler.THROW, null);
	}

	private static List<String> makeDefaultClasspath(String cp) {
		List<String> classPath = new ArrayList<>();
		classPath.addAll(getPathEntries(cp));
		classPath.addAll(getPathEntries(ClassPath.getClassPath()));
		return classPath;

	}

	private static List<String> getPathEntries(String s) {
		List<String> ret = new ArrayList<>();
		StringTokenizer tok = new StringTokenizer(s, File.pathSeparator);
		while (tok.hasMoreTokens()) {
			ret.add(tok.nextToken());
		}
		return ret;
	}

	public BcelWorld(List classPath, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		// this.aspectPath = new ClassPathManager(aspectPath, handler);
		this.classPath = new ClassPathManager(classPath, handler);
		setMessageHandler(handler);
		setCrossReferenceHandler(xrefHandler);
		delegate = new BcelWorldRepository();
	}

	public BcelWorld(ClassPathManager cpm, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		classPath = cpm;
		setMessageHandler(handler);
		setCrossReferenceHandler(xrefHandler);
		delegate = new BcelWorldRepository();
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

	public static Type[] makeBcelTypes(String[] types) {
		if (types == null || types.length==0 ) {
			return null;
		}
		Type[] ret = new Type[types.length];
		for (int i=0, len=types.length; i<len; i++) {
			ret[i] = makeBcelType(types[i]);
		}
		return ret;
	}

	public static Type makeBcelType(String type) {
		return Type.getType(type);
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

	@Override
	protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
		String name = ty.getName();
		ensureAdvancedConfigurationProcessed();
		Clazz jc = lookupJavaClass(classPath, name);
		if (jc == null) {
			// Anyone else to ask?
			if (typeDelegateResolvers != null) {
				for (TypeDelegateResolver tdr : typeDelegateResolvers) {
					ReferenceTypeDelegate delegate = tdr.getDelegate(ty);
					if (delegate != null) {
						return delegate;
					}
				}
			}
			return null;
		} else {
			return buildBcelDelegate(ty, jc, false, false);
		}
	}

	public BcelObjectType buildBcelDelegate(ReferenceType type, Clazz jc, boolean artificial, boolean exposedToWeaver) {
		BcelObjectType ret = new BcelObjectType(type, jc, artificial, exposedToWeaver);
		return ret;
	}

	public BcelObjectType addSourceObjectType(Clazz jc, boolean artificial) {
		return addSourceObjectType(jc.getClassName(), jc, artificial);
	}

	public BcelObjectType addSourceObjectType(String classname, Clazz jc, boolean artificial) {
		BcelObjectType ret = null;
		if (!jc.getClassName().equals(classname)) { // TODO look at the callers to this, can they really provide diff values?
			throw new RuntimeException(jc.getClassName() + "!=" + classname);
		}
		String signature = UnresolvedType.forName(jc.getClassName()).getSignature();

		ResolvedType resolvedTypeFromTypeMap = typeMap.get(signature);

		if (resolvedTypeFromTypeMap != null && !(resolvedTypeFromTypeMap instanceof ReferenceType)) {
			// what on earth is it then? See pr 112243
			StringBuilder exceptionText = new StringBuilder();
			exceptionText.append("Found invalid (not a ReferenceType) entry in the type map. ");
			exceptionText.append("Signature=[" + signature + "] Found=[" + resolvedTypeFromTypeMap + "] Class=[" + resolvedTypeFromTypeMap.getClass() + "]");
			throw new BCException(exceptionText.toString());
		}

		ReferenceType referenceTypeFromTypeMap = (ReferenceType) resolvedTypeFromTypeMap;

		if (referenceTypeFromTypeMap == null) {
			if (jc.isGeneric() && isInJava5Mode()) {
				ReferenceType rawType = ReferenceType.fromTypeX(UnresolvedType.forRawTypeName(jc.getClassName()), this);
				ret = buildBcelDelegate(rawType, jc, artificial, true);
				ReferenceType genericRefType = new ReferenceType(UnresolvedType.forGenericTypeSignature(signature,
						ret.getDeclaredGenericSignature()), this);
				rawType.setDelegate(ret);
				genericRefType.setDelegate(ret);
				rawType.setGenericType(genericRefType);
				typeMap.put(signature, rawType);
			} else {
				referenceTypeFromTypeMap = new ReferenceType(signature, this);
				ret = buildBcelDelegate(referenceTypeFromTypeMap, jc, artificial, true);
				typeMap.put(signature, referenceTypeFromTypeMap);
			}
		} else {
			ret = buildBcelDelegate(referenceTypeFromTypeMap, jc, artificial, true);
		}
		return ret;
	}

	public BcelObjectType addSourceObjectType(String classname, byte[] bytes, boolean artificial) {
		BcelObjectType retval = null;
		String signature = UnresolvedType.forName(classname).getSignature();
		ResolvedType resolvedTypeFromTypeMap = typeMap.get(signature);

		if (resolvedTypeFromTypeMap != null && !(resolvedTypeFromTypeMap instanceof ReferenceType)) {
			// what on earth is it then? See pr 112243
			StringBuilder exceptionText = new StringBuilder();
			exceptionText.append("Found invalid (not a ReferenceType) entry in the type map. ");
			exceptionText.append("Signature=[" + signature + "] Found=[" + resolvedTypeFromTypeMap + "] Class=[" + resolvedTypeFromTypeMap.getClass() + "]");
			throw new BCException(exceptionText.toString());
		}

		ReferenceType referenceTypeFromTypeMap = (ReferenceType) resolvedTypeFromTypeMap;

		if (referenceTypeFromTypeMap == null) {
			Clazz jc = makeClazz(classname, bytes);
//			JavaClass jc = Utility.makeJavaClass(classname, bytes);
			if (jc.isGeneric() && isInJava5Mode()) {
				referenceTypeFromTypeMap = ReferenceType.fromTypeX(UnresolvedType.forRawTypeName(jc.getClassName()), this);
				retval = buildBcelDelegate(referenceTypeFromTypeMap, jc, artificial, true);
				ReferenceType genericRefType = new ReferenceType(UnresolvedType.forGenericTypeSignature(signature,
						retval.getDeclaredGenericSignature()), this);
				referenceTypeFromTypeMap.setDelegate(retval);
				genericRefType.setDelegate(retval);
				referenceTypeFromTypeMap.setGenericType(genericRefType);
				typeMap.put(signature, referenceTypeFromTypeMap);
			} else {
				referenceTypeFromTypeMap = new ReferenceType(signature, this);
				retval = buildBcelDelegate(referenceTypeFromTypeMap, jc, artificial, true);
				typeMap.put(signature, referenceTypeFromTypeMap);
			}
		} else {
			ReferenceTypeDelegate existingDelegate = referenceTypeFromTypeMap.getDelegate();
			if (!(existingDelegate instanceof BcelObjectType)) {
				throw new IllegalStateException("For " + classname + " should be BcelObjectType, but is " + existingDelegate.getClass());
			}
			retval = (BcelObjectType) existingDelegate;
			// Note1: If the type is already exposed to the weaver (retval.isExposedToWeaver()) then this is likely
			// to be a hotswap reweave so build a new delegate, don't accidentally use the old data.
			// Note2: Also seen when LTW and another agent precedes the AspectJ agent.  Earlier in LTW
			// a type is resolved (and ends up in the typemap but not exposed to the weaver at that time)
			// then later LTW actually is attempted on this type. We end up here with different
			// bytes to the current delegate if the earlier agent has modified them. See PR488216
//			if (retval.isArtificial() || retval.isExposedToWeaver()) {
			retval = buildBcelDelegate(referenceTypeFromTypeMap, makeClazz(classname,bytes)/*Utility.makeJavaClass(classname, bytes)*/, artificial, true);
//			}
		}
		return retval;
	}

	private Clazz makeClazz(String classname, byte[] bytes) {
		return BcelClazz.asBcelClazz(Utility.makeJavaClass(classname, bytes));
	}

	public static Member makeFieldJoinPointSignature(LazyClassGen cg, FieldInstruction fi) {
		ConstantPool cpg = cg.getConstantPool();
		return MemberImpl.field(fi.getClassName(cpg),
				(fi.opcode == Constants.GETSTATIC || fi.opcode == Constants.PUTSTATIC) ? Modifier.STATIC : 0, fi.getName(cpg),
				fi.getSignature(cpg));
	}

	public Member makeJoinPointSignatureFromMethod(LazyMethodGen mg, MemberKind kind) {
		Member ret = mg.getMemberView();
		if (ret == null) {
			int mods = mg.getAccessFlags();
			if (mg.getEnclosingClass().isInterface()) {
				mods |= Modifier.INTERFACE;
			}
			return new ResolvedMemberImpl(kind, UnresolvedType.forName(mg.getClassName()), mods, fromBcel(mg.getReturnType()),
					mg.getName(), fromBcel(mg.getArgumentTypes()));
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
			retval = MemberImpl.method(ut, Modifier.PUBLIC, UnresolvedType.VOID, "<init>", new ResolvedType[] { INT });
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
			for (int ii = 0; ii < dimensions; ii++) {
				parms[ii] = INT;
			}
			retval = MemberImpl.method(ut, Modifier.PUBLIC, UnresolvedType.VOID, "<init>", parms);

		} else if (i.opcode == Constants.NEWARRAY) {
			// NEWARRAY arrayInstruction = (NEWARRAY)i;
			Type ot = i.getType();
			UnresolvedType ut = fromBcel(ot);
			retval = MemberImpl.method(ut, Modifier.PUBLIC, UnresolvedType.VOID, "<init>", new ResolvedType[] { INT });
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

		// 307147
		if (name.startsWith("ajc$privMethod$")) {
			// The invoke is on a privileged accessor. These may be created for different
			// kinds of target, not necessarily just private methods. In bug 307147 it is
			// for a private method. This code is identifying the particular case in 307147
			try {
				declaringType = UnresolvedType.forName(declaring);
				String typeNameAsFoundInAccessorName = declaringType.getName().replace('.', '_');
				int indexInAccessorName = name.lastIndexOf(typeNameAsFoundInAccessorName);
				if (indexInAccessorName != -1) {
					String methodName = name.substring(indexInAccessorName+typeNameAsFoundInAccessorName.length()+1);
					ResolvedType resolvedDeclaringType = declaringType.resolve(this);
					ResolvedMember[] methods = resolvedDeclaringType.getDeclaredMethods();
					for (ResolvedMember method: methods) {
						if (method.getName().equals(methodName) && method.getSignature().equals(signature) && Modifier.isPrivate(method.getModifiers())) {
							return method;
						}
					}
				}
			} catch (Exception e) {
				// Remove this once confident above code isn't having unexpected side effects
				// Added 1.8.7
				e.printStackTrace();
			}
		}

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
			for (Iterator<ResolvedMember> iterator = appearsDeclaredBy.getMethods(true, true); iterator.hasNext();) {
				ResolvedMember method = iterator.next();
				if (Modifier.isStatic(method.getModifiers())) {
					if (name.equals(method.getName()) && signature.equals(method.getSignature())) {
						// we found it
						declaringType = method.getDeclaringType();
						break;
					}
				}
			}
		}

		if (declaringType == null) {
			if (declaring.charAt(0) == '[') {
				declaringType = UnresolvedType.forSignature(declaring);
			} else {
				declaringType = UnresolvedType.forName(declaring);
			}
		}
		return MemberImpl.method(declaringType, modifier, name, signature);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("BcelWorld(");
		// buf.append(shadowMungerMap);
		buf.append(")");
		return buf.toString();
	}

	public ReferenceTypeDelegate getReferenceTypeDelegateIfBytecodey(ResolvedType concreteAspect) {
		if (concreteAspect == null) {
			return null;
		}
		if (!(concreteAspect instanceof ReferenceType)) { // Might be Missing
			return null;
		}
		ReferenceTypeDelegate rtDelegate = ((ReferenceType) concreteAspect).getDelegate();
		if (rtDelegate instanceof BcelObjectType) {
			return (BcelObjectType) rtDelegate;
		} else {
			return null;
		}
	}

	public static BcelObjectType getBcelObjectType(ResolvedType concreteAspect) {
		if (concreteAspect == null) {
			return null;
		}
		if (!(concreteAspect instanceof ReferenceType)) { // Might be Missing
			return null;
		}
		ReferenceTypeDelegate rtDelegate = ((ReferenceType) concreteAspect).getDelegate();
		if (rtDelegate instanceof BcelObjectType) {
			return (BcelObjectType) rtDelegate;
		} else {
			return null;
		}
	}
	
//	/**
//	 * Retrieve a bcel delegate for an aspect - this will return NULL if the delegate is an EclipseSourceType and not a
//	 * BcelObjectType - this happens quite often when incrementally compiling.
//	 */
//	public static BcelObjectType getBcelObjectType(ResolvedType concreteAspect) {
//		if (concreteAspect == null) {
//			return null;
//		}
//		if (!(concreteAspect instanceof ReferenceType)) { // Might be Missing
//			return null;
//		}
//		ReferenceTypeDelegate rtDelegate = ((ReferenceType) concreteAspect).getDelegate();
//		if (rtDelegate instanceof BcelObjectType) {
//			return (BcelObjectType) rtDelegate;
//		} else {
//			return null;
//		}
//	}

	public void tidyUp() {
		// At end of compile, close any open files so deletion of those archives
		// is possible
		classPath.closeArchives();
		typeMap.report();
		typeMap.demote(true);
		// ResolvedType.resetPrimitives();
	}

	class BcelWorldRepository implements Repository {

		@Override
		public JavaClass findClass(String className) {
			return lookupJavaClass(classPath, className).getJavaClass();
		}
	
		@Override
		public JavaClass loadClass(String className) throws ClassNotFoundException {
			return lookupJavaClass(classPath, className).getJavaClass();
		}
	
		@Override
		public void storeClass(JavaClass clazz) {
			// doesn't need to do anything
		}
	
		@Override
		public void removeClass(JavaClass clazz) {
			throw new RuntimeException("Not implemented");
		}
	
		@Override
		public JavaClass loadClass(Class clazz) throws ClassNotFoundException {
			throw new RuntimeException("Not implemented");
		}
	
		@Override
		public void clear() {
			delegate.clear();
			// throw new RuntimeException("Not implemented");
		}
	}

	/**
	 * The aim of this method is to make sure a particular type is 'ok'. Some operations on the delegate for a type modify it and
	 * this method is intended to undo that... see pr85132
	 */
	@Override
	public void validateType(UnresolvedType type) {
		ResolvedType result = typeMap.get(type.getSignature());
		if (result == null) {
			return; // We haven't heard of it yet
		}
		if (!result.isExposedToWeaver()) {
			return; // cant need resetting
		}
		result.ensureConsistent();
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
		List<ResolvedType> newParents = p.findMatchingNewParents(onType, true);
		if (!newParents.isEmpty()) {
			didSomething = true;
			BcelObjectType classType = BcelWorld.getBcelObjectType(onType);
			// System.err.println("need to do declare parents for: " + onType);
			for (ResolvedType newParent : newParents) {
				// We set it here so that the imminent matching for ITDs can
				// succeed - we still haven't done the necessary changes to the class file
				// itself (like transform super calls) - that is done in
				// BcelTypeMunger.mungeNewParent()
				// classType.addParent(newParent);
				onType.addParent(newParent);
				ResolvedTypeMunger newParentMunger = new NewParentTypeMunger(newParent, p.getDeclaringType());
				newParentMunger.setSourceLocation(p.getSourceLocation());
				onType.addInterTypeMunger(new BcelTypeMunger(newParentMunger, getCrosscuttingMembersSet()
						.findAspectDeclaringParents(p)), false);
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

			if (onType.hasAnnotation(decA.getAnnotation().getType())) {
				// already has it
				return false;
			}

			AnnotationAJ annoX = decA.getAnnotation();

			// check the annotation is suitable for the target
			boolean isOK = checkTargetOK(decA, onType, annoX);

			if (isOK) {
				didSomething = true;
				ResolvedTypeMunger newAnnotationTM = new AnnotationOnTypeMunger(annoX);
				newAnnotationTM.setSourceLocation(decA.getSourceLocation());
				onType.addInterTypeMunger(new BcelTypeMunger(newAnnotationTM, decA.getAspect().resolve(this)), false);
				decA.copyAnnotationTo(onType);
			}
		}
		return didSomething;
	}

	/**
	 * Apply the specified declare @field construct to any matching fields in the specified type.
	 * @param deca the declare annotation targeting fields
	 * @param type the type to check for members matching the declare annotation
	 * @return true if something matched and the type was modified
	 */
	private boolean applyDeclareAtField(DeclareAnnotation deca, ResolvedType type) {
		boolean changedType = false;
		ResolvedMember[] fields = type.getDeclaredFields();
		for (ResolvedMember field: fields) {
			if (deca.matches(field, this)) {
				AnnotationAJ anno = deca.getAnnotation();
				if (!field.hasAnnotation(anno.getType())) {
					field.addAnnotation(anno);
					changedType=true;
				}
			}
		}
		return changedType;
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


	@Override
	public IWeavingSupport getWeavingSupport() {
		return bcelWeavingSupport;
	}


	// Hmmm - very similar to the code in BcelWeaver.weaveParentTypeMungers -
	// this code
	// doesn't need to produce errors/warnings though as it won't really be
	// weaving.
	protected void weaveInterTypeDeclarations(ResolvedType onType) {

		List<DeclareParents> declareParentsList = getCrosscuttingMembersSet().getDeclareParents();
		if (onType.isRawType()) {
			onType = onType.getGenericType();
		}
		onType.clearInterTypeMungers();

		List<DeclareParents> decpToRepeat = new ArrayList<>();

		boolean aParentChangeOccurred = false;
		boolean anAnnotationChangeOccurred = false;
		// First pass - apply all decp mungers
		for (DeclareParents decp : declareParentsList) {
			boolean typeChanged = applyDeclareParents(decp, onType);
			if (typeChanged) {
				aParentChangeOccurred = true;
			} else { // Perhaps it would have matched if a 'dec @type' had
				// modified the type
				if (!decp.getChild().isStarAnnotation()) {
					decpToRepeat.add(decp);
				}
			}
		}

		// Still first pass - apply all dec @type mungers
		for (DeclareAnnotation decA : getCrosscuttingMembersSet().getDeclareAnnotationOnTypes()) {
			boolean typeChanged = applyDeclareAtType(decA, onType, true);
			if (typeChanged) {
				anAnnotationChangeOccurred = true;
			}
		}

		// apply declare @field
		for (DeclareAnnotation deca: getCrosscuttingMembersSet().getDeclareAnnotationOnFields()) {
			if (applyDeclareAtField(deca,onType)) {
				anAnnotationChangeOccurred = true;
			}
		}

		while ((aParentChangeOccurred || anAnnotationChangeOccurred) && !decpToRepeat.isEmpty()) {
			anAnnotationChangeOccurred = aParentChangeOccurred = false;
			List<DeclareParents> decpToRepeatNextTime = new ArrayList<>();
			for (DeclareParents decp: decpToRepeat) {
				if (applyDeclareParents(decp, onType)) {
					aParentChangeOccurred = true;
				} else {
					decpToRepeatNextTime.add(decp);
				}
			}

			for (DeclareAnnotation deca: getCrosscuttingMembersSet().getDeclareAnnotationOnTypes()) {
				if (applyDeclareAtType(deca, onType, false)) {
					anAnnotationChangeOccurred = true;
				}
			}

			for (DeclareAnnotation deca: getCrosscuttingMembersSet().getDeclareAnnotationOnFields()) {
				if (applyDeclareAtField(deca, onType)) {
					anAnnotationChangeOccurred = true;
				}
			}
			decpToRepeat = decpToRepeatNextTime;
		}

	}
	
	@Override
	public boolean hasUnsatisfiedDependency(ResolvedType aspectType) {
		String aspectName = aspectType.getName();

		if (aspectType.hasAnnotations()) {
			AnnotationAJ[] annos = aspectType.getAnnotations();
			for (AnnotationAJ anno: annos) {
				if (anno.getTypeName().equals("org.aspectj.lang.annotation.RequiredTypes")) {
					String values = anno.getStringFormOfValue("value"); // Example: "[A,org.foo.Bar]"
					if (values != null && values.length() > 2) {
						values = values.substring(1,values.length()-1);
						StringTokenizer tokenizer = new StringTokenizer(values,",");
						boolean anythingMissing = false;
						while (tokenizer.hasMoreElements()) {
							String requiredTypeName = tokenizer.nextToken();
							ResolvedType rt = resolve(UnresolvedType.forName(requiredTypeName));
							if (rt.isMissing()) {
								if (!getMessageHandler().isIgnoring(IMessage.INFO)) {
									getMessageHandler().handleMessage(
											MessageUtil.info("deactivating aspect '" + aspectName + "' as it requires type '"
													+ requiredTypeName + "' which cannot be found on the classpath"));
								}
								anythingMissing = true;
								if (aspectRequiredTypes == null) {
									aspectRequiredTypes = new HashMap<>();
								}
								// Record that it has an invalid type reference
								aspectRequiredTypes.put(aspectName,requiredTypeName);
							}
						}
						if (anythingMissing) {
							return true;
						}
						else {
							return false;
						}
					}
					else {
						// no value specified for annotation
						return false;
					}
				}
			}
		}
		if (aspectRequiredTypes == null) {
			// no aspects require anything, so there can be no unsatisfied dependencies
			return false;
		}
		if (!aspectRequiredTypesProcessed.contains(aspectName)) {
			String requiredTypeName = aspectRequiredTypes.get(aspectName);
			if (requiredTypeName==null) {
				aspectRequiredTypesProcessed.add(aspectName);
				return false;
			} else {
				ResolvedType rt = resolve(UnresolvedType.forName(requiredTypeName));
				if (!rt.isMissing()) {
					aspectRequiredTypesProcessed.add(aspectName);
					aspectRequiredTypes.remove(aspectName);
					return false;
				} else {
					if (!getMessageHandler().isIgnoring(IMessage.INFO)) {
						getMessageHandler().handleMessage(
								MessageUtil.info("deactivating aspect '" + aspectName + "' as it requires type '"
										+ requiredTypeName + "' which cannot be found on the classpath"));
					}
					aspectRequiredTypesProcessed.add(aspectName);
					return true;
				}
			}
		}
		return aspectRequiredTypes.containsKey(aspectName);
	}

	private List<String> aspectRequiredTypesProcessed = new ArrayList<>();
	private Map<String, String> aspectRequiredTypes = null;

	public void addAspectRequires(String aspectClassName, String requiredType) {
		if (aspectRequiredTypes == null) {
			aspectRequiredTypes = new HashMap<>();
		}
		aspectRequiredTypes.put(aspectClassName,requiredType);
	}

	@Override
	public TypeMap getTypeMap() {
		return typeMap;
	}

	@Override
	public boolean isLoadtimeWeaving() {
		return false;
	}

	@Override
	public void classWriteEvent(char[][] compoundName) {
		typeMap.classWriteEvent(new String(CharOperation.concatWith(compoundName, '.')));
	}

	/**
	 * Force demote a type.
	 */
	public void demote(ResolvedType type) {
		typeMap.demote(type);
	}

	protected BcelClazz lookupJavaClass(ClassPathManager classPath, String name) {
		if (classPath == null) {
			try {
				ensureRepositorySetup();
				JavaClass jc = delegate.loadClass(name);
				if (trace.isTraceEnabled()) {
					trace.event("lookupJavaClass", this, new Object[] { name, jc });
				}
				return BcelClazz.asBcelClazz(jc);
			} catch (ClassNotFoundException e) {
				if (trace.isTraceEnabled()) {
					trace.error("Unable to find class '" + name + "' in repository", e);
				}
				return null;
			}
		}
		ClassPathManager.ClassFile file = null;
		try {
			file = classPath.find(UnresolvedType.forName(name));
			if (file == null) {
				return null;
			}
			ClassParser parser = new ClassParser(file.getInputStream(), file.getPath());
			JavaClass jc = parser.parse();
			return BcelClazz.asBcelClazz(jc);
		} catch (IOException ioe) {
			if (trace.isTraceEnabled()) {
				trace.error("IOException whilst processing class",ioe);
			}
			return null;
		} finally {
			if (file != null) {
				file.close();
			}
		}
	}

	public void storeClass(JavaClass unwovenJavaClass) {
		delegate.storeClass(unwovenJavaClass);
	}

	public void storeClass(Clazz unwovenJavaClass) {
		delegate.storeClass(((BcelClazz)unwovenJavaClass).getJavaClass());
	}
	
}
