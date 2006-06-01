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


package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.problem.AjProblemReporter;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.aspectj.org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Constants;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;

/**
 * An inter-type method declaration.
 *
 * @author Jim Hugunin
 */
public class InterTypeMethodDeclaration extends InterTypeDeclaration {
	public InterTypeMethodDeclaration(CompilationResult result, TypeReference onType) {
		super(result, onType);
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		if (ignoreFurtherInvestigation)
			return;
		if (!Modifier.isAbstract(declaredModifiers)) {
			parser.parse(this, unit);
		}
	}
	
	protected char[] getPrefix() {
		return (NameMangler.ITD_PREFIX + "interMethod$").toCharArray();
	}
	
	public boolean isFinal() {
		return  (declaredModifiers & AccFinal) != 0;		
	}

	public void analyseCode(
		ClassScope currentScope,
		InitializationFlowContext flowContext,
		FlowInfo flowInfo)
	{
		if (Modifier.isAbstract(declaredModifiers)) return;
		
		super.analyseCode(currentScope, flowContext, flowInfo);
	}
	
	public void resolve(ClassScope upperScope) {
		if (munger == null) ignoreFurtherInvestigation = true;
		if (ignoreFurtherInvestigation) return;
		
		if (!Modifier.isStatic(declaredModifiers)) {
			this.arguments = AstUtil.insert(
				AstUtil.makeFinalArgument("ajc$this_".toCharArray(), onTypeBinding),
				this.arguments);
			binding.parameters  = AstUtil.insert(onTypeBinding, binding.parameters);
		}
			
		super.resolve(upperScope);
	}
	
	
	public void resolveStatements() {
		checkAndSetModifiersForMethod();
        if ((modifiers & AccSemicolonBody) != 0) {
            if ((declaredModifiers & AccAbstract) == 0)
                scope.problemReporter().methodNeedBody(this);
        } else {
            // the method HAS a body --> abstract native modifiers are forbiden
            if (((declaredModifiers & AccAbstract) != 0))
                scope.problemReporter().methodNeedingNoBody(this);
        }        

        // XXX AMC we need to do this, but I'm not 100% comfortable as I don't
        // know why the return type is wrong in this case. Also, we don't seem to need
        // to do it for args...
        if (munger.getSignature().getReturnType().isRawType()) {
        	if (!binding.returnType.isRawType()) {
				EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);
        		binding.returnType = world.makeTypeBinding(munger.getSignature().getReturnType()); 
        	}
        }
        
        // check @Override annotation - based on MethodDeclaration.resolveStatements() @Override processing
		checkOverride: {
			if (this.binding == null) break checkOverride;
			if (this.scope.compilerOptions().sourceLevel < JDK1_5) break checkOverride;
			boolean hasOverrideAnnotation = (this.binding.tagBits & TagBits.AnnotationOverride) != 0;
			
			// Need to verify
			if (hasOverrideAnnotation) {
				
				// Work out the real method binding that we can use for comparison
				EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);
				MethodBinding realthing = world.makeMethodBinding(munger.getSignature(),munger.getTypeVariableAliases());
				
				boolean reportError = true;				
				// Go up the hierarchy, looking for something we override
				ReferenceBinding supertype = onTypeBinding.superclass();
				while (supertype!=null && reportError) {
					MethodBinding[] possibles = supertype.getMethods(declaredSelector);
					for (int i = 0; i < possibles.length; i++) {
						MethodBinding mb = possibles[i];

						boolean couldBeMatch = true;
						if (mb.parameters.length!=realthing.parameters.length) couldBeMatch=false;
						else {
							for (int j = 0; j < mb.parameters.length && couldBeMatch; j++) {
								if (!mb.parameters[j].equals(realthing.parameters[j])) couldBeMatch=false;
							}
						}
						// return types compatible? (allow for covariance)
						if (couldBeMatch && !returnType.resolvedType.isCompatibleWith(mb.returnType)) couldBeMatch=false;
						if (couldBeMatch) reportError = false;
					}
					supertype = supertype.superclass(); // superclass of object is null
				}
				// If we couldn't find something we override, report the error
				if (reportError) ((AjProblemReporter)this.scope.problemReporter()).itdMethodMustOverride(this,realthing);			
			}
		}
        
		if (!Modifier.isAbstract(declaredModifiers)) super.resolveStatements();
		if (Modifier.isStatic(declaredModifiers)) {
			// Check the target for ITD is not an interface
			if (onTypeBinding.isInterface()) {
				scope.problemReporter().signalError(sourceStart, sourceEnd,
					"methods in interfaces cannot be declared static");
			}
		}
	}
	
	

	public EclipseTypeMunger build(ClassScope classScope) {
		EclipseFactory factory = EclipseFactory.fromScopeLookupEnvironment(classScope);
		
		resolveOnType(classScope);
		if (ignoreFurtherInvestigation) return null;

		binding = classScope.referenceContext.binding.resolveTypesFor(binding);
		if (binding == null) {
			// if binding is null, we failed to find a type used in the method params, this error
			// has already been reported.
			this.ignoreFurtherInvestigation = true;
			//return null;
			throw new AbortCompilationUnit(compilationResult,null);
		}

		if (isTargetAnnotation(classScope,"method")) return null; // Error message output in isTargetAnnotation
		if (isTargetEnum(classScope,"method")) return null; // Error message output in isTargetEnum
		
		if (interTypeScope==null) return null; // We encountered a problem building the scope, don't continue - error already reported
		
		// This signature represents what we want consumers of the targetted type to 'see'
		// must use the factory method to build it since there may be typevariables from the binding
		// referred to in the parameters/returntype
		ResolvedMember sig = factory.makeResolvedMemberForITD(binding,onTypeBinding,interTypeScope.getRecoveryAliases());
		sig.resetName(new String(declaredSelector));
		int resetModifiers = declaredModifiers;
		if (binding.isVarargs())  resetModifiers = resetModifiers | Constants.ACC_VARARGS;
		sig.resetModifiers(resetModifiers); 
		NewMethodTypeMunger myMunger = new NewMethodTypeMunger(sig, null, typeVariableAliases);
		setMunger(myMunger);
		ResolvedType aspectType = factory.fromEclipse(classScope.referenceContext.binding);
		ResolvedMember me =
			myMunger.getInterMethodBody(aspectType);
		this.selector = binding.selector = me.getName().toCharArray();
		return new EclipseTypeMunger(factory, myMunger, aspectType, this);
	}
	
	
	private AjAttribute makeAttribute() {
		return new AjAttribute.TypeMunger(munger);
	}
	
	
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation) {
			//System.err.println("no code for " + this);
			return;
		}
		
		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute()));
		
		if (!Modifier.isAbstract(declaredModifiers)) {
			super.generateCode(classScope, classFile); // this makes the interMethodBody
		}
		
		// annotations on the ITD declaration get put on this method 
		generateDispatchMethod(classScope, classFile);
	}
	
	public void generateDispatchMethod(ClassScope classScope, ClassFile classFile) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);
		
		UnresolvedType aspectType = world.fromBinding(classScope.referenceContext.binding);
		ResolvedMember signature = munger.getSignature();
		
		ResolvedMember dispatchMember = 
			AjcMemberMaker.interMethodDispatcher(signature, aspectType);
		MethodBinding dispatchBinding = world.makeMethodBinding(dispatchMember,munger.getTypeVariableAliases(),munger.getSignature().getDeclaringType());
		MethodBinding introducedMethod = 
			world.makeMethodBinding(AjcMemberMaker.interMethod(signature, aspectType, onTypeBinding.isInterface()),munger.getTypeVariableAliases());
		
		classFile.generateMethodInfoHeader(dispatchBinding);
		int methodAttributeOffset = classFile.contentsOffset;
		
		
		// Watch out!  We are passing in 'binding' here (instead of dispatchBinding) so that
		// the dispatch binding attributes will include the annotations from the 'binding'.
		// There is a chance that something else on the binding (e.g. throws clause) might
		// damage the attributes generated for the dispatch binding.
		int attributeNumber = classFile.generateMethodInfoAttribute(binding,
				false,
				makeEffectiveSignatureAttribute(signature, Shadow.MethodCall, false));
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);
		
		codeStream.initializeMaxLocals(dispatchBinding);
		
		MethodBinding methodBinding = introducedMethod;
		TypeBinding[] parameters = methodBinding.parameters;
		int length = parameters.length;
		int resolvedPosition;
		if (methodBinding.isStatic())
			resolvedPosition = 0;
		else {
			codeStream.aload_0();
			resolvedPosition = 1;
		}
		for (int i = 0; i < length; i++) {
			codeStream.load(parameters[i], resolvedPosition);
			if ((parameters[i] == DoubleBinding) || (parameters[i] == LongBinding))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
//		TypeBinding type;
		if (methodBinding.isStatic())
			codeStream.invokestatic(methodBinding);
		else {
			if (methodBinding.declaringClass.isInterface()){
				codeStream.invokeinterface(methodBinding);
			} else {
				codeStream.invokevirtual(methodBinding);
			}
		}
		AstUtil.generateReturn(dispatchBinding.returnType, codeStream);

		classFile.completeCodeAttribute(codeAttributeOffset);
		attributeNumber++;
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}

	
	protected Shadow.Kind getShadowKindForBody() {
		return Shadow.MethodExecution;
	}
	
	// XXX this code is copied from MethodScope, with a few adjustments for ITDs...
	private void checkAndSetModifiersForMethod() {
		
		// for reported problems, we want the user to see the declared selector
		char[] realSelector = this.selector;
		this.selector = declaredSelector;
		
		final ReferenceBinding declaringClass = this.binding.declaringClass;
		if ((declaredModifiers & AccAlternateModifierProblem) != 0)
			scope.problemReporter().duplicateModifierForMethod(onTypeBinding, this);

		// after this point, tests on the 16 bits reserved.
		int realModifiers = declaredModifiers & AccJustFlag;

		// check for abnormal modifiers
		int unexpectedModifiers = ~(AccPublic | AccPrivate | AccProtected
			| AccAbstract | AccStatic | AccFinal | AccSynchronized | AccNative | AccStrictfp);
		if ((realModifiers & unexpectedModifiers) != 0) {
			scope.problemReporter().illegalModifierForMethod(this);
			declaredModifiers &= ~AccJustFlag | ~unexpectedModifiers;
		}

		// check for incompatible modifiers in the visibility bits, isolate the visibility bits
		int accessorBits = realModifiers & (AccPublic | AccProtected | AccPrivate);
		if ((accessorBits & (accessorBits - 1)) != 0) {
			scope.problemReporter().illegalVisibilityModifierCombinationForMethod(onTypeBinding, this);

			// need to keep the less restrictive so disable Protected/Private as necessary
			if ((accessorBits & AccPublic) != 0) {
				if ((accessorBits & AccProtected) != 0)
					declaredModifiers &= ~AccProtected;
				if ((accessorBits & AccPrivate) != 0)
					declaredModifiers &= ~AccPrivate;
			} else if ((accessorBits & AccProtected) != 0 && (accessorBits & AccPrivate) != 0) {
				declaredModifiers &= ~AccPrivate;
			}
		}

		// check for modifiers incompatible with abstract modifier
		if ((declaredModifiers & AccAbstract) != 0) {
			int incompatibleWithAbstract = AccStatic | AccFinal | AccSynchronized | AccNative | AccStrictfp;
			if ((declaredModifiers & incompatibleWithAbstract) != 0)
				scope.problemReporter().illegalAbstractModifierCombinationForMethod(onTypeBinding, this);
			if (!onTypeBinding.isAbstract())
				scope.problemReporter().abstractMethodInAbstractClass((SourceTypeBinding) onTypeBinding, this);
		}

		/* DISABLED for backward compatibility with javac (if enabled should also mark private methods as final)
		// methods from a final class are final : 8.4.3.3 
		if (methodBinding.declaringClass.isFinal())
			modifiers |= AccFinal;
		*/
		// native methods cannot also be tagged as strictfp
		if ((declaredModifiers & AccNative) != 0 && (declaredModifiers & AccStrictfp) != 0)
			scope.problemReporter().nativeMethodsCannotBeStrictfp(onTypeBinding, this);

		// static members are only authorized in a static member or top level type
		if (((realModifiers & AccStatic) != 0) && declaringClass.isNestedType() && !declaringClass.isStatic())
			scope.problemReporter().unexpectedStaticModifierForMethod(onTypeBinding, this);

		// restore the true selector now that any problems have been reported
		this.selector = realSelector;		
	}
}
