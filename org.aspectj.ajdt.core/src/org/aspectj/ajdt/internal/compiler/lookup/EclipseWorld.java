/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AstUtil;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.ajdt.internal.core.builder.AsmBuilder;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * This holds unique ResolvedTypeXs for each type known to the compiler
 * or looked up on the class path.  For types which are compiled from 
 * source code, it will hold an EclipseObjectType.  Types that 
 * come from bytecode will be delegated to buildManager.bcelWorld.
 * 
 * @author Jim Hugunin
 */
public class EclipseWorld extends World {
	public static boolean DEBUG = false;
	
	public AjBuildManager buildManager;
	private LookupEnvironment lookupEnvironment;
	
	private Map/*TypeX, TypeBinding*/ typexToBinding = new HashMap();
	//XXX currently unused
	private Map/*TypeBinding, ResolvedTypeX*/ bindingToResolvedTypeX = new HashMap();
	
	public static EclipseWorld forLookupEnvironment(LookupEnvironment env) {
		AjLookupEnvironment aenv = (AjLookupEnvironment)env;
		return aenv.world;
	}
	
	public static EclipseWorld fromScopeLookupEnvironment(Scope scope) {
		return forLookupEnvironment(AstUtil.getCompilationUnitScope(scope).environment);
	}
	
	
	public EclipseWorld(LookupEnvironment lookupEnvironment, IMessageHandler handler) {
		this.lookupEnvironment = lookupEnvironment;
		setMessageHandler(handler);
	}
	
    public Advice concreteAdvice(
       	AjAttribute.AdviceAttribute attribute,
    	Pointcut pointcut,
        Member signature)
    {
        return new EclipseAdvice(attribute, pointcut, signature);
    }

    public ConcreteTypeMunger concreteTypeMunger(
		ResolvedTypeMunger munger, ResolvedTypeX aspectType)
	{
		return null;
	}

	protected ResolvedTypeX resolveObjectType(TypeX typeX) {
		TypeBinding binding = makeTypeBinding(typeX);
		
//		System.err.println("resolvedObjectType: " + typeX + 
//						" found " + 
//						(binding == null ? "null" : binding.getClass().getName()));
		
		if (!(binding instanceof SourceTypeBinding)) {
			//System.err.println("missing: " + binding);
			return ResolvedTypeX.MISSING;
		}
		
		if (binding instanceof BinaryTypeBinding) {
			//System.err.println("binary: " + typeX);
			return new EclipseBinaryType(
					buildManager.bcelWorld.resolve(typeX), 
					this,
					(BinaryTypeBinding)binding);
		}
		
		return new EclipseSourceType(typeX.getSignature(), this, 
							(SourceTypeBinding)binding);
	}
			


	public ResolvedTypeX fromEclipse(ReferenceBinding binding) {
		if (binding == null) return ResolvedTypeX.MISSING;
		//??? this seems terribly inefficient
		//System.err.println("resolving: " + binding.getClass() + ", name = " + getName(binding));
		ResolvedTypeX ret = resolve(fromBinding(binding));
		//System.err.println("      got: " + ret);
		return ret;
	}	
	
	public ResolvedTypeX[] fromEclipse(ReferenceBinding[] bindings) {
		if (bindings == null) {
			return ResolvedTypeX.NONE;
		}
		int len = bindings.length;
		ResolvedTypeX[] ret = new ResolvedTypeX[len];
		for (int i=0; i < len; i++) {
			ret[i] = fromEclipse(bindings[i]);
		}
		return ret;
	}	
	
	
	private static String getName(TypeBinding binding) {
		String packageName = new String(binding.qualifiedPackageName());
		String className = new String(binding.qualifiedSourceName()).replace('.', '$');
		if (packageName.length() > 0) {
			className = packageName + "." + className;
		}
		//XXX doesn't handle arrays correctly (or primitives?)
		return new String(className);
	}



	//??? going back and forth between strings and bindings is a waste of cycles
	public static TypeX fromBinding(TypeBinding binding) {
		if (binding instanceof HelperInterfaceBinding) {
			return ((HelperInterfaceBinding) binding).getTypeX();
		}
		if (binding.qualifiedSourceName() == null) {
			return ResolvedTypeX.MISSING;
		}
		return TypeX.forName(getName(binding));
	}

	public static TypeX[] fromBindings(TypeBinding[] bindings) {
		int len = bindings.length;
		TypeX[] ret = new TypeX[len];
		for (int i=0; i<len; i++) {
			ret[i] = fromBinding(bindings[i]);
		}
		return ret;
	}

	public static AstNode astForLocation(IHasPosition location) {
		return new EmptyStatement(location.getStart(), location.getEnd());
	}

	public Collection getTypeMungers() {
		return crosscuttingMembersSet.getTypeMungers();
	}
	
	public static ResolvedMember makeResolvedMember(MethodBinding binding) {
		ResolvedMember ret =  new ResolvedMember(
			binding.isConstructor() ? Member.CONSTRUCTOR : Member.METHOD,
			fromBinding(binding.declaringClass),
			binding.modifiers,
			fromBinding(binding.returnType),
			new String(binding.selector),
			fromBindings(binding.parameters));
		ret.setCheckedExceptions(fromBindings(binding.thrownExceptions));
		return ret;
	}

	public static ResolvedMember makeResolvedMember(FieldBinding binding) {
		return new ResolvedMember(
			Member.FIELD,
			fromBinding(binding.declaringClass),
			binding.modifiers,
			fromBinding(binding.type),
			new String(binding.name),
			TypeX.NONE);
	}
	
	public TypeBinding makeTypeBinding(TypeX typeX) {
		TypeBinding ret = (TypeBinding)typexToBinding.get(typeX);
		if (ret == null) {
			ret = makeTypeBinding1(typeX);
			typexToBinding.put(typeX, ret);
		}
		return ret;
	}
	
	private TypeBinding makeTypeBinding1(TypeX typeX) {
		if (typeX.isPrimitive()) {
			if (typeX == ResolvedTypeX.BOOLEAN) return BaseTypes.BooleanBinding;
			if (typeX == ResolvedTypeX.BYTE) return BaseTypes.ByteBinding;
			if (typeX == ResolvedTypeX.CHAR) return BaseTypes.CharBinding;
			if (typeX == ResolvedTypeX.DOUBLE) return BaseTypes.DoubleBinding;
			if (typeX == ResolvedTypeX.FLOAT) return BaseTypes.FloatBinding;
			if (typeX == ResolvedTypeX.INT) return BaseTypes.IntBinding;
			if (typeX == ResolvedTypeX.LONG) return BaseTypes.LongBinding;
			if (typeX == ResolvedTypeX.SHORT) return BaseTypes.ShortBinding;
			if (typeX == ResolvedTypeX.VOID) return BaseTypes.VoidBinding;
			throw new RuntimeException("weird primitive type " + typeX);
		} else if (typeX.isArray()) {
			int dim = 0;
			while (typeX.isArray()) {
				dim++;
				typeX = typeX.getComponentType();
			}
			return lookupEnvironment.createArrayType(makeTypeBinding(typeX), dim);
		} else {
			String n = typeX.getName();
			char[][] name = CharOperation.splitOn('.', n.toCharArray());
			return lookupEnvironment.getType(name);
		}
	}
	
	
	
	public TypeBinding[] makeTypeBindings(TypeX[] types) {
		int len = types.length;
		TypeBinding[] ret = new TypeBinding[len];
		
		for (int i = 0; i < len; i++) {
			ret[i] = makeTypeBinding(types[i]);
		}
		return ret;
	}
	
	// just like the code above except it returns an array of ReferenceBindings
	private ReferenceBinding[] makeReferenceBindings(TypeX[] types) {
		int len = types.length;
		ReferenceBinding[] ret = new ReferenceBinding[len];
		
		for (int i = 0; i < len; i++) {
			ret[i] = (ReferenceBinding)makeTypeBinding(types[i]);
		}
		return ret;
	}

	
	public FieldBinding makeFieldBinding(ResolvedMember member) {
		return new FieldBinding(member.getName().toCharArray(),
				makeTypeBinding(member.getReturnType()),
				member.getModifiers(),
				(ReferenceBinding)makeTypeBinding(member.getDeclaringType()),
				Constant.NotAConstant);
	}


	public MethodBinding makeMethodBinding(ResolvedMember member) {
		return new MethodBinding(member.getModifiers(),
				member.getName().toCharArray(),
				makeTypeBinding(member.getReturnType()),
				makeTypeBindings(member.getParameterTypes()),
				makeReferenceBindings(member.getExceptions()),
				(ReferenceBinding)makeTypeBinding(member.getDeclaringType()));
	}


	
	public MethodBinding makeMethodBindingForCall(Member member) {
		return new MethodBinding(member.getCallsiteModifiers(),
				member.getName().toCharArray(),
				makeTypeBinding(member.getReturnType()),
				makeTypeBindings(member.getParameterTypes()),
				new ReferenceBinding[0],
				(ReferenceBinding)makeTypeBinding(member.getDeclaringType()));
	}

	public void finishedCompilationUnit(CompilationUnitDeclaration unit) {
		if (buildManager.doGenerateModel()) {
			AsmBuilder.build(unit, buildManager.getStructureModel());
		}
	}


	public void addTypeBinding(TypeBinding binding) {
		typexToBinding.put(fromBinding(binding), binding);
	}


	public Shadow makeShadow(AstNode location, ReferenceContext context) {
		return EclipseShadow.makeShadow(this, location, context);
	}
	
	public Shadow makeShadow(ReferenceContext context) {
		return EclipseShadow.makeShadow(this, (AstNode) context, context);
	}
}
