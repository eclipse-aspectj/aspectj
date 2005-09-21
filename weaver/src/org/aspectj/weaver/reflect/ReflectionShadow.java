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
import java.util.HashMap;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Var;

/**
 * @author colyer
 *
 */
public class ReflectionShadow extends Shadow {

	private World world;
	private ResolvedType enclosingType;
	private ResolvedMember enclosingMember;
	private Var thisVar = null;
	private Var targetVar = null;
	private Var[] argsVars = null;
	private Var atThisVar = null;
	private Var atTargetVar = null;
	private Map atArgsVars = new HashMap();
	private Map withinAnnotationVar = new HashMap();
	private Map withinCodeAnnotationVar = new HashMap();
	private Map annotationVar = new HashMap();
	
	public static Shadow makeExecutionShadow(World inWorld, java.lang.reflect.Member forMethod) {
		Kind kind = (forMethod instanceof Method) ? Shadow.MethodExecution : Shadow.ConstructorExecution;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(forMethod, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new ReflectionShadow(inWorld,kind,signature,null,enclosingType,null);
	}
	
	public static Shadow makeAdviceExecutionShadow(World inWorld, java.lang.reflect.Method forMethod) {
		Kind kind = Shadow.AdviceExecution;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedAdviceMember(forMethod, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new ReflectionShadow(inWorld,kind,signature,null,enclosingType,null);
	}
	
	public static Shadow makeCallShadow(World inWorld, java.lang.reflect.Member aMember, java.lang.reflect.Member withinCode) {
		Shadow enclosingShadow = makeExecutionShadow(inWorld,withinCode);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(aMember, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(withinCode, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = aMember instanceof Method ? Shadow.MethodCall : Shadow.ConstructorCall;
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);
	}

	public static Shadow makeCallShadow(World inWorld, java.lang.reflect.Member aMember, Class thisClass) {
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, thisClass);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(aMember, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(thisClass, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = aMember instanceof Method ? Shadow.MethodCall : Shadow.ConstructorCall;
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);
	}

	public static Shadow makeStaticInitializationShadow(World inWorld, Class forType) {
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(forType, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.StaticInitialization;
		return new ReflectionShadow(inWorld,kind,signature,null,enclosingType,null);
	}
	
	public static Shadow makePreInitializationShadow(World inWorld, Constructor forConstructor) {
		Kind kind =  Shadow.PreInitialization;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(forConstructor, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new ReflectionShadow(inWorld,kind,signature,null,enclosingType,null);
	}
	
	public static Shadow makeInitializationShadow(World inWorld, Constructor forConstructor) {
		Kind kind =  Shadow.Initialization;
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(forConstructor, inWorld);
		ResolvedType enclosingType = signature.getDeclaringType().resolve(inWorld);
		return new ReflectionShadow(inWorld,kind,signature,null,enclosingType,null);
	}
	
	public static Shadow makeHandlerShadow(World inWorld, Class exceptionType, Class withinType) {
		Kind kind = Shadow.ExceptionHandler;
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, withinType);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createHandlerMember(exceptionType, withinType, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(withinType, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);	
	}
	
	public static Shadow makeHandlerShadow(World inWorld, Class exceptionType, java.lang.reflect.Member withinCode) {
		Kind kind = Shadow.ExceptionHandler;
		Shadow enclosingShadow = makeExecutionShadow(inWorld, withinCode);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createHandlerMember(exceptionType, withinCode.getDeclaringClass(), inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(withinCode, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);	
	}
	
	public static Shadow makeFieldGetShadow(World inWorld, Field forField, Class callerType) {
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, callerType);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(callerType, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldGet;
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);
	}
	
	public static Shadow makeFieldGetShadow(World inWorld, Field forField, java.lang.reflect.Member inMember) {
		Shadow enclosingShadow = makeExecutionShadow(inWorld,inMember);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(inMember, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldGet;
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);
	}

	public static Shadow makeFieldSetShadow(World inWorld, Field forField, Class callerType) {
		Shadow enclosingShadow = makeStaticInitializationShadow(inWorld, callerType);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createStaticInitMember(callerType, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldSet;
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);
	}

	public static Shadow makeFieldSetShadow(World inWorld, Field forField, java.lang.reflect.Member inMember) {
		Shadow enclosingShadow = makeExecutionShadow(inWorld,inMember);
		Member signature = ReflectionBasedReferenceTypeDelegateFactory.createResolvedField(forField, inWorld);
		ResolvedMember enclosingMember = ReflectionBasedReferenceTypeDelegateFactory.createResolvedMember(inMember, inWorld);
		ResolvedType enclosingType = enclosingMember.getDeclaringType().resolve(inWorld);
		Kind kind = Shadow.FieldSet;
		return new ReflectionShadow(inWorld,kind,signature,enclosingShadow,enclosingType,enclosingMember);
	}

	public ReflectionShadow(World world, Kind kind, Member signature, Shadow enclosingShadow, ResolvedType enclosingType, ResolvedMember enclosingMember) {
		super(kind,signature,enclosingShadow);
		this.world = world;
		this.enclosingType = enclosingType;
		this.enclosingMember = enclosingMember;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getIWorld()
	 */
	public World getIWorld() {
		return world;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getThisVar()
	 */
	public Var getThisVar() {
		if (thisVar == null && hasThis()) {
			thisVar = ReflectionVar.createThisVar(getThisType().resolve(world));
		}
		return thisVar;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getTargetVar()
	 */
	public Var getTargetVar() {
		if (targetVar == null && hasTarget()) {
			targetVar = ReflectionVar.createTargetVar(getThisType().resolve(world));
		}
		return targetVar;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getEnclosingType()
	 */
	public UnresolvedType getEnclosingType() {
		return this.enclosingType;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getArgVar(int)
	 */
	public Var getArgVar(int i) {
		if (argsVars == null) {
			this.argsVars = new Var[this.getArgCount()];
			for (int j = 0; j < this.argsVars.length; j++) {
				this.argsVars[j] = ReflectionVar.createArgsVar(getArgType(j).resolve(world), j);
			}
		}
		if (i < argsVars.length) {
			return argsVars[i];
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getThisJoinPointVar()
	 */
	public Var getThisJoinPointVar() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getThisJoinPointStaticPartVar()
	 */
	public Var getThisJoinPointStaticPartVar() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getThisEnclosingJoinPointStaticPartVar()
	 */
	public Var getThisEnclosingJoinPointStaticPartVar() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getKindedAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getKindedAnnotationVar(UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (annotationVar.get(annType) == null) {
			Var v = ReflectionVar.createAtAnnotationVar(annType);
			annotationVar.put(annType,v);
		}
		return (Var) annotationVar.get(annType);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getWithinAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getWithinAnnotationVar(UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (withinAnnotationVar.get(annType) == null) {
			Var v = ReflectionVar.createWithinAnnotationVar(annType);
			withinAnnotationVar.put(annType,v);
		}
		return (Var) withinAnnotationVar.get(annType);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getWithinCodeAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getWithinCodeAnnotationVar(UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (withinCodeAnnotationVar.get(annType) == null) {
			Var v = ReflectionVar.createWithinCodeAnnotationVar(annType);
			withinCodeAnnotationVar.put(annType,v);
		}
		return (Var) withinCodeAnnotationVar.get(annType);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getThisAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getThisAnnotationVar(UnresolvedType forAnnotationType) {
		if (atThisVar == null) {
			atThisVar = ReflectionVar.createThisAnnotationVar(forAnnotationType.resolve(world));
		}
		return atThisVar;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getTargetAnnotationVar(org.aspectj.weaver.UnresolvedType)
	 */
	public Var getTargetAnnotationVar(UnresolvedType forAnnotationType) {
		if (atTargetVar == null) {
			atTargetVar = ReflectionVar.createTargetAnnotationVar(forAnnotationType.resolve(world));
		}
		return atTargetVar;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getArgAnnotationVar(int, org.aspectj.weaver.UnresolvedType)
	 */
	public Var getArgAnnotationVar(int i, UnresolvedType forAnnotationType) {
		ResolvedType annType = forAnnotationType.resolve(world);
		if (atArgsVars.get(annType) == null) {
			Var[] vars = new Var[getArgCount()];
			atArgsVars.put(annType,vars);
		}
		Var[] vars = (Var[]) atArgsVars.get(annType);
		if (i > (vars.length - 1) ) return null;
		if (vars[i] == null) {
			vars[i] = ReflectionVar.createArgsAnnotationVar(annType, i);
		}
		return vars[i];
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.Shadow#getSourceLocation()
	 */
	public ISourceLocation getSourceLocation() {
		return null;
	}

}
