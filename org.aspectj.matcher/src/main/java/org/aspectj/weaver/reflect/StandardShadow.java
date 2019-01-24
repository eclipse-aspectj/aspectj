/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.tools.MatchingContext;

/**
 * @author colyer
 * 
 */
public class StandardShadow extends Shadow {

	private final World world;
	private final ResolvedType enclosingType;
	private final ResolvedMember enclosingMember;
	private final MatchingContext matchContext;
	private Var thisVar = null;
	private Var targetVar = null;
	private Var[] argsVars = null;
	private Var atThisVar = null;
	private Var atTargetVar = null;
	private Map atArgsVars = new HashMap();
	private Map withinAnnotationVar = new HashMap();
	private Map withinCodeAnnotationVar = new HashMap();
	private Map annotationVar = new HashMap();
	private AnnotationFinder annotationFinder;

	public static Shadow makeExecutionShadow(World inWorld, java.lang.reflect.Member forMethod, MatchingContext withContext) {
		Kind kind = (forMethod instanceof Method) ? Shadow.MethodExecution : Shadow.ConstructorExecution;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(forMethod, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new StandardShadow(inWorld, kind, signature, null, enclosingType, null, withContext);
	}

	public static Shadow makeExecutionShadow(World inWorld, ResolvedMember forMethod, MatchingContext withContext) {
		Kind kind = forMethod.getName().equals("<init>") ? Shadow.ConstructorExecution : Shadow.MethodExecution;
		// Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(forMethod, inWorld);
		// ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new StandardShadow(inWorld, kind, forMethod, null, (ResolvedType) forMethod.getDeclaringType(), null, withContext);
	}

	public static Shadow makeAdviceExecutionShadow(World inWorld, java.lang.reflect.Method forMethod, MatchingContext withContext) {
		Kind kind = Shadow.AdviceExecution;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedAdviceMember(forMethod, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new StandardShadow(inWorld, kind, signature, null, enclosingType, null, withContext);
	}

	public static Shadow makeCallShadow(World inWorld, ResolvedMember aMember, ResolvedMember withinCode,
			MatchingContext withContext) {
		Shadow enclosingShadow = makeExecutionShadow(inWorld, withinCode, withContext);
		// Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(aMember, inWorld);
		// ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(withinCode, inWorld);
		// ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = !aMember.getName().equals("<init>") ? Shadow.MethodCall : Shadow.ConstructorCall;
		return new StandardShadow(inWorld, kind, aMember, enclosingShadow, (ResolvedType) withinCode.getDeclaringType(),
				withinCode, withContext);
	}

	public static Shadow makeCallShadow(World inWorld, java.lang.reflect.Member aMember, Class thisClass,
			MatchingContext withContext) {
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, thisClass, withContext);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(aMember, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(thisClass, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = aMember instanceof Method ? Shadow.MethodCall : Shadow.ConstructorCall;
		return new StandardShadow(inWorld, kind, signature, enclosingShadow, enclosingType, enclosingMember, withContext);
	}

	public static Shadow makeStaticInitializationShadow(World inWorld, Class forType, MatchingContext withContext) {
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(forType, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.StaticInitialization;
		return new StandardShadow(inWorld, kind, signature, null, enclosingType, null, withContext);
	}

	public static Shadow makeStaticInitializationShadow(World inWorld, ResolvedType forType, MatchingContext withContext) {
		ResolvedMember[] members = forType.getDeclaredMethods();
		int clinit = -1;
		for (int i = 0; i < members.length && clinit == -1; i++) {
			if (members[i].getName().equals("<clinit>")) {
				clinit = i;
			}
		}
		// Member signature = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(forType, inWorld);
		Kind kind = Shadow.StaticInitialization;
		if (clinit == -1) {
			Member clinitMember = new ResolvedMemberImpl(org.aspectj.weaver.Member.STATIC_INITIALIZATION, forType, Modifier.STATIC,
					UnresolvedType.VOID, "<clinit>", new UnresolvedType[0], new UnresolvedType[0]);
			return new StandardShadow(inWorld, kind, clinitMember, null, forType, null, withContext);
		} else {
			return new StandardShadow(inWorld, kind, members[clinit], null, forType, null, withContext);
		}
	}

	public static Shadow makePreInitializationShadow(World inWorld, Constructor forConstructor, MatchingContext withContext) {
		Kind kind = Shadow.PreInitialization;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(forConstructor, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new StandardShadow(inWorld, kind, signature, null, enclosingType, null, withContext);
	}

	public static Shadow makeInitializationShadow(World inWorld, Constructor forConstructor, MatchingContext withContext) {
		Kind kind = Shadow.Initialization;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(forConstructor, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new StandardShadow(inWorld, kind, signature, null, enclosingType, null, withContext);
	}

	public static Shadow makeHandlerShadow(World inWorld, Class exceptionType, Class withinType, MatchingContext withContext) {
		Kind kind = Shadow.ExceptionHandler;
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, withinType, withContext);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createHandlerMember(exceptionType, withinType, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(withinType, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		return new StandardShadow(inWorld, kind, signature, enclosingShadow, enclosingType, enclosingMember, withContext);
	}

	public static Shadow makeHandlerShadow(World inWorld, Class exceptionType, java.lang.reflect.Member withinCode,
			MatchingContext withContext) {
		Kind kind = Shadow.ExceptionHandler;
		Shadow enclosingShadow = makeExecutionShadow(inWorld, withinCode, withContext);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createHandlerMember(exceptionType,
				withinCode.getDeclaringClass(), inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(withinCode, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		return new StandardShadow(inWorld, kind, signature, enclosingShadow, enclosingType, enclosingMember, withContext);
	}

	public static Shadow makeFieldGetShadow(World inWorld, Field forField, Class callerType, MatchingContext withContext) {
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, callerType, withContext);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(callerType, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldGet;
		return new StandardShadow(inWorld, kind, signature, enclosingShadow, enclosingType, enclosingMember, withContext);
	}

	public static Shadow makeFieldGetShadow(World inWorld, Field forField, java.lang.reflect.Member inMember,
			MatchingContext withContext) {
		Shadow enclosingShadow = makeExecutionShadow(inWorld, inMember, withContext);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(inMember, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldGet;
		return new StandardShadow(inWorld, kind, signature, enclosingShadow, enclosingType, enclosingMember, withContext);
	}

	public static Shadow makeFieldSetShadow(World inWorld, Field forField, Class callerType, MatchingContext withContext) {
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, callerType, withContext);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(callerType, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldSet;
		return new StandardShadow(inWorld, kind, signature, enclosingShadow, enclosingType, enclosingMember, withContext);
	}

	public static Shadow makeFieldSetShadow(World inWorld, Field forField, java.lang.reflect.Member inMember,
			MatchingContext withContext) {
		Shadow enclosingShadow = makeExecutionShadow(inWorld, inMember, withContext);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(inMember, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldSet;
		return new StandardShadow(inWorld, kind, signature, enclosingShadow, enclosingType, enclosingMember, withContext);
	}

	public StandardShadow(World world, Kind kind, Member signature, Shadow enclosingShadow, ResolvedType enclosingType,
			ResolvedMember enclosingMember, MatchingContext withContext) {
		super(kind, signature, enclosingShadow);
		this.world = world;
		this.enclosingType = enclosingType;
		this.enclosingMember = enclosingMember;
		this.matchContext = withContext;
		if (world instanceof IReflectionWorld) {
			this.annotationFinder = ((IReflectionWorld) world).getAnnotationFinder();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getIWorld()
	 */
	public World getIWorld() {
		return world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getThisVar()
	 */
	public Var getThisVar() {
		if (thisVar == null && hasThis()) {
			thisVar = ReflectionVar.createThisVar(getThisType().resolve(world), this.annotationFinder);
		}
		return thisVar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getTargetVar()
	 */
	public Var getTargetVar() {
		if (targetVar == null && hasTarget()) {
			targetVar = ReflectionVar.createTargetVar(getThisType().resolve(world), this.annotationFinder);
		}
		return targetVar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getEnclosingType()
	 */
	public UnresolvedType getEnclosingType() {
		return this.enclosingType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getArgVar(int)
	 */
	public Var getArgVar(int i) {
		if (argsVars == null) {
			this.argsVars = new Var[this.getArgCount()];
			for (int j = 0; j < this.argsVars.length; j++) {
				this.argsVars[j] = ReflectionVar.createArgsVar(getArgType(j).resolve(world), j, this.annotationFinder);
			}
		}
		if (i < argsVars.length) {
			return argsVars[i];
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getThisJoinPointVar()
	 */
	public Var getThisJoinPointVar() {
		// TODO Auto-generated method stub
		return null;
	}

	public Var getThisJoinPointStaticPartVar() {
		return null;
	}

	public Var getThisEnclosingJoinPointStaticPartVar() {
		return null;
	}

	public Var getThisAspectInstanceVar(ResolvedType aspectType) {
		return null;
	}

	public Var getKindedAnnotationVar(UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (annotationVar.get(annType) == null) {
			Var v = ReflectionVar.createAtAnnotationVar(annType, this.annotationFinder);
			annotationVar.put(annType, v);
		}
		return (Var) annotationVar.get(annType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getWithinAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getWithinAnnotationVar(UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (withinAnnotationVar.get(annType) == null) {
			Var v = ReflectionVar.createWithinAnnotationVar(annType, this.annotationFinder);
			withinAnnotationVar.put(annType, v);
		}
		return (Var) withinAnnotationVar.get(annType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getWithinCodeAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getWithinCodeAnnotationVar(UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (withinCodeAnnotationVar.get(annType) == null) {
			Var v = ReflectionVar.createWithinCodeAnnotationVar(annType, this.annotationFinder);
			withinCodeAnnotationVar.put(annType, v);
		}
		return (Var) withinCodeAnnotationVar.get(annType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getThisAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getThisAnnotationVar(UnresolvedType forAnnotationType) {
		if (atThisVar == null) {
			atThisVar = ReflectionVar.createThisAnnotationVar(forAnnotationType.resolve(world), this.annotationFinder);
		}
		return atThisVar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getTargetAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getTargetAnnotationVar(UnresolvedType forAnnotationType) {
		if (atTargetVar == null) {
			atTargetVar = ReflectionVar.createTargetAnnotationVar(forAnnotationType.resolve(world), this.annotationFinder);
		}
		return atTargetVar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getArgAnnotationVar(int, org.aspectj.weaver.UnresolvedType)
	 */
	public Var getArgAnnotationVar(int i, UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (atArgsVars.get(annType) == null) {
			Var[] vars = new Var[getArgCount()];
			atArgsVars.put(annType, vars);
		}
		Var[] vars = (Var[]) atArgsVars.get(annType);
		if (i > (vars.length - 1))
			return null;
		if (vars[i] == null) {
			vars[i] = ReflectionVar.createArgsAnnotationVar(annType, i, this.annotationFinder);
		}
		return vars[i];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getEnclosingCodeSignature()
	 */
	public Member getEnclosingCodeSignature() {
		// XXX this code is copied from BcelShadow with one minor change...
		if (getKind().isEnclosingKind()) {
			return getSignature();
		} else if (getKind() == Shadow.PreInitialization) {
			// PreInit doesn't enclose code but its signature
			// is correctly the signature of the ctor.
			return getSignature();
		} else if (enclosingShadow == null) {
			return this.enclosingMember;
		} else {
			return enclosingShadow.getSignature();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.Shadow#getSourceLocation()
	 */
	public ISourceLocation getSourceLocation() {
		return null;
	}

	public MatchingContext getMatchingContext() {
		return this.matchContext;
	}
}
