/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.ajdt.internal.compiler.lookup.AjTypeConstants;
import org.aspectj.weaver.*;
//import org.aspectj.weaver.bcel.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.core.util.CharArrayOps;

/**
 * Represents before, after and around advice in an aspect.
 * Will generate a method corresponding to the body of the advice with an
 * attribute including additional information.
 * 
 * @author Jim Hugunin
 */
public class AdviceDeclaration extends MethodDeclaration {
	public PointcutDesignator pointcutDesignator;
	int baseArgumentCount;
	
	public Argument extraArgument;
	
	public AdviceKind kind;
	private int extraArgumentFlags = 0;
	
	public MethodBinding proceedMethodBinding;
	
	public boolean proceedInInners;
	public ResolvedMember[] proceedCallSignatures;
	public boolean[] formalsUnchangedToProceed;
	public TypeX[] declaredExceptions;
	
	
	public AdviceDeclaration(CompilationResult result, AdviceKind kind) {
		super(result);
		this.returnType = TypeReference.baseTypeReference(T_void, 0);
		this.kind = kind;
	}

	
	protected int generateInfoAttributes(ClassFile classFile) {
		List l = new ArrayList(1);
		l.add(new EclipseAttributeAdapter(makeAttribute()));

		return classFile.generateMethodInfoAttribute(binding, l);
	}
	
	public AjAttribute makeAttribute() {
		if (kind == AdviceKind.Around) {
			return new AjAttribute.AdviceAttribute(kind, pointcutDesignator.getPointcut(), 
					extraArgumentFlags, sourceStart, sourceEnd, null,
					proceedInInners, proceedCallSignatures, formalsUnchangedToProceed, 
					declaredExceptions);
		} else {
			return new AjAttribute.AdviceAttribute(kind, pointcutDesignator.getPointcut(), 
					extraArgumentFlags, sourceStart, sourceEnd, null);
		}
	}

	public void resolveStatements(ClassScope upperScope) {
		if (binding == null || ignoreFurtherInvestigation) return;
		
		modifiers = binding.modifiers = checkAndSetModifiers(modifiers, upperScope);
		
		if (kind == AdviceKind.AfterThrowing && extraArgument != null) {
			TypeBinding argTb = extraArgument.binding.type;
			TypeBinding expectedTb = upperScope.getJavaLangThrowable();
			if (!upperScope.areTypesCompatible(argTb, expectedTb)) {
				scope.problemReporter().typeMismatchError(argTb, expectedTb, extraArgument);
				ignoreFurtherInvestigation = true;
				return;
			}
		}
		
		
		pointcutDesignator.finishResolveTypes(this, this.binding, 
			baseArgumentCount, upperScope.referenceContext.binding);
		
		
		if (kind == AdviceKind.Around && binding != null) {
			//XXX set these correctly
			proceedInInners = false;
			proceedCallSignatures = new ResolvedMember[0];
			formalsUnchangedToProceed = new boolean[baseArgumentCount];
			declaredExceptions = new TypeX[0];
			
			
			ReferenceBinding[] exceptions = 
				new ReferenceBinding[] { upperScope.getJavaLangThrowable() };
			proceedMethodBinding = new MethodBinding(Modifier.STATIC,
				"proceed".toCharArray(), binding.returnType,
				resize(baseArgumentCount+1, binding.parameters),
				exceptions, binding.declaringClass);
			proceedMethodBinding.selector =
				CharArrayOps.concat(selector, proceedMethodBinding.selector);
		}
		
		super.resolveStatements(upperScope);
		if (binding != null) determineExtraArgumentFlags();
	}


	public int getDeclaredParameterCount() {
		// this only works before code generation
		return this.arguments.length - 3 - ((extraArgument == null) ? 0 : 1);
		//Advice.countOnes(extraArgumentFlags);
	}

	public void generateProceedMethod(ClassScope classScope, ClassFile classFile) {
		MethodBinding binding = (MethodBinding)proceedMethodBinding;
		
		classFile.generateMethodInfoHeader(binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(binding, AstUtil.getAjSyntheticAttribute());
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);
		
		// push the closure
		int nargs = binding.parameters.length;
		int closureIndex = 0;
		for (int i=0; i < nargs-1; i++) {
			closureIndex += AstUtil.slotsNeeded(binding.parameters[i]);
		}
		
		
		codeStream.loadObject(closureIndex);
		
		// build the Object[]

		codeStream.generateInlinedValue(nargs-1);
		codeStream.anewarrayJavaLangObject();
		
		int index = 0;
		for (int i=0; i < nargs-1; i++) {
			TypeBinding type = binding.parameters[i];
			codeStream.dup();
			codeStream.generateInlinedValue(i);
			codeStream.load(type, index);
			index += AstUtil.slotsNeeded(type);
			if (type.isBaseType()) {
				codeStream.invokestatic(AjTypeConstants.getConversionMethodToObject(classScope, type));
			}
			
			codeStream.aastore();
		}
		
		// call run
		ReferenceBinding closureType = (ReferenceBinding)binding.parameters[nargs-1];
		MethodBinding runMethod = closureType.getMethods("run".toCharArray())[0];
		codeStream.invokevirtual(runMethod);

		TypeBinding returnType = binding.returnType;
		if (returnType.isBaseType()) {
			codeStream.invokestatic(AjTypeConstants.getConversionMethodFromObject(classScope, returnType));
		} else {
			codeStream.checkcast(returnType);
		}
		AstUtil.generateReturn(returnType, codeStream);
		
		classFile.completeCodeAttribute(codeAttributeOffset);
		attributeNumber++;
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}



	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) return;
		
		super.generateCode(classScope, classFile);
		if (proceedMethodBinding != null) {
			generateProceedMethod(classScope, classFile);
		}
	}


	private void determineExtraArgumentFlags() {
		if (extraArgument != null) extraArgumentFlags |= Advice.ExtraArgument;
		
		ThisJoinPointVisitor tjp = new ThisJoinPointVisitor(this);
		extraArgumentFlags |= tjp.removeUnusedExtraArguments();
	}
	
	private static TypeBinding[] resize(int newSize, TypeBinding[] bindings) {
		int len = bindings.length;
		TypeBinding[] ret = new TypeBinding[newSize];
		System.arraycopy(bindings, 0, ret, 0, Math.min(newSize, len));
		return ret;
	}


	
	public void postParse(TypeDeclaration typeDec) {
		this.selector =
			NameMangler.adviceName(EclipseWorld.fromBinding(typeDec.binding), kind, sourceStart).toCharArray();
		if (arguments != null) {
			baseArgumentCount = arguments.length;
		}
		
		if (kind == AdviceKind.Around) {
			extraArgument = makeFinalArgument("ajc_aroundClosure",
					AjTypeConstants.getAroundClosureType());
		}
		
		int addedArguments = 3;
		if (extraArgument != null) {
			addedArguments += 1;
		}
		
		arguments = extendArgumentsLength(arguments, addedArguments);
		
		int index = baseArgumentCount;
		if (extraArgument != null) {
			arguments[index++] = extraArgument;
		}
		
		arguments[index++] = makeFinalArgument("thisJoinPointStaticPart", AjTypeConstants.getJoinPointStaticPartType());
		arguments[index++] = makeFinalArgument("thisJoinPoint", AjTypeConstants.getJoinPointType());
		arguments[index++] = makeFinalArgument("thisEnclosingJoinPointStaticPart", AjTypeConstants.getJoinPointStaticPartType());
		
		if (pointcutDesignator.isError()) {
			this.ignoreFurtherInvestigation = true;
		}
		pointcutDesignator.postParse(typeDec, this);
	}

	private int checkAndSetModifiers(int modifiers, ClassScope scope) {
		if (modifiers == 0) return Modifier.PUBLIC;
		else if (modifiers == Modifier.STRICT) return Modifier.PUBLIC | Modifier.STRICT;
		else {
			tagAsHavingErrors();
			scope.problemReporter().signalError(declarationSourceStart, sourceStart-1, "illegal modifier on advice, only strictfp is allowed");
			return Modifier.PUBLIC;
		}
	}

	
	public static Argument[]  addTjpArguments(Argument[] arguments) {
		int index = arguments.length;
		arguments = extendArgumentsLength(arguments, 3);
		
		arguments[index++] = makeFinalArgument("thisJoinPointStaticPart", AjTypeConstants.getJoinPointStaticPartType());
		arguments[index++] = makeFinalArgument("thisJoinPoint", AjTypeConstants.getJoinPointType());
		arguments[index++] = makeFinalArgument("thisEnclosingJoinPointStaticPart", AjTypeConstants.getJoinPointStaticPartType());
	
		return arguments;
	}
	
	

	private static Argument makeFinalArgument(String name, TypeReference typeRef) {
		long pos = 0; //XXX encode start and end location
		return new Argument(name.toCharArray(), pos, typeRef, Modifier.FINAL);
	}


	private static Argument[] extendArgumentsLength(Argument[] args, int addedArguments) {
		if (args == null) {
			return new Argument[addedArguments];
		}
		int len = args.length;
		Argument[] ret = new Argument[len + addedArguments];
		System.arraycopy(args, 0, ret, 0, len);
		return ret;
	}

	
	public String toString(int tab) {
		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}

		if (kind == AdviceKind.Around) {
			s += returnTypeToString(0);
		}

		s += new String(selector) + "("; //$NON-NLS-1$
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				s += arguments[i].toString(0);
				if (i != (arguments.length - 1))
					s = s + ", "; //$NON-NLS-1$
			};
		};
		s += ")"; //$NON-NLS-1$
		
		if (extraArgument != null) {
			s += "(" + extraArgument.toString(0) + ")";
		}
		
		
		
		if (thrownExceptions != null) {
			s += " throws "; //$NON-NLS-1$
			for (int i = 0; i < thrownExceptions.length; i++) {
				s += thrownExceptions[i].toString(0);
				if (i != (thrownExceptions.length - 1))
					s = s + ", "; //$NON-NLS-1$
			};
		};
		
		s += ": ";
		if (pointcutDesignator != null) {
			s += pointcutDesignator.toString(0);
		}

		s += toStringStatements(tab + 1);
		return s;
	}
}
