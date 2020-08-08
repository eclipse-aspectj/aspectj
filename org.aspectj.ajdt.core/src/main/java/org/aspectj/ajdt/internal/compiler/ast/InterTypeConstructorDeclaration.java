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
import org.aspectj.ajdt.internal.compiler.lookup.EclipseSourceLocation;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeScope;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Constants;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;

/**
 * An inter-type constructor declaration.
 * 
 * This will generate two implementation methods in the aspect, the main one for the body of the constructor, and an additional
 * <code>preMethod</code> for the code that runs before the super constructor is called.
 * 
 * @author Jim Hugunin
 */
public class InterTypeConstructorDeclaration extends InterTypeDeclaration {
	private static final String SUPPRESSAJWARNINGS = "Lorg/aspectj/lang/annotation/SuppressAjWarnings;";
	private static final String NOEXPLICITCONSTRUCTORCALL = "noExplicitConstructorCall";
	private MethodDeclaration preMethod;
	private ExplicitConstructorCall explicitConstructorCall = null;

	public InterTypeConstructorDeclaration(CompilationResult result, TypeReference onType) {
		super(result, onType);
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		if (ignoreFurtherInvestigation)
			return;
		parser.parse(this, unit);
	}

	protected char[] getPrefix() {
		return (NameMangler.ITD_PREFIX + "interConstructor$").toCharArray();
	}

	public void resolve(ClassScope upperScope) {
		if (munger == null || binding == null)
			ignoreFurtherInvestigation = true;
		if (ignoreFurtherInvestigation)
			return;

		explicitConstructorCall = null;
		if (statements != null && statements.length > 0 && statements[0] instanceof ExplicitConstructorCall) {
			explicitConstructorCall = (ExplicitConstructorCall) statements[0];
			statements = AstUtil.remove(0, statements);
		}

		preMethod = makePreMethod(upperScope, explicitConstructorCall);

		binding.parameters = AstUtil.insert(onTypeBinding, binding.parameters);
		this.arguments = AstUtil.insert(AstUtil.makeFinalArgument("ajc$this_".toCharArray(), onTypeBinding), this.arguments);

		super.resolve(upperScope);

		// after annotations have been resolved...
		if (explicitConstructorCall == null) {
			raiseNoFieldInitializersWarning();
		}
	}

	/**
	 * Warning added in response to PR 62606 - if an ITD constructor does not make an explicit constructor call then field
	 * initializers in the target class will not be executed leading to unexpected behaviour.
	 */
	private void raiseNoFieldInitializersWarning() {
		if (suppressingNoExplicitConstructorCall())
			return;
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);
		ISourceLocation location = new EclipseSourceLocation(scope.problemReporter().referenceContext.compilationResult(),
				sourceStart(), sourceEnd());
		world.getWorld().getLint().noExplicitConstructorCall.signal(null, location);
	}

	/**
	 * true iff constructor has @SuppressAjWarnings or @SuppressAjWarnings("xyz,noExplicitConstructorCall,def,...")
	 * 
	 * @return
	 */
	private boolean suppressingNoExplicitConstructorCall() {
		if (this.annotations == null)
			return false;
		for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation annotation : this.annotations) {
			if (new String(annotation.resolvedType.signature()).equals(SUPPRESSAJWARNINGS)) {
				if (annotation instanceof MarkerAnnotation) {
					return true;
				} else if (annotation instanceof SingleMemberAnnotation) {
					SingleMemberAnnotation sma = (SingleMemberAnnotation) annotation;
					if (sma.memberValue instanceof ArrayInitializer) {
						ArrayInitializer memberValue = (ArrayInitializer) sma.memberValue;
						for (int j = 0; j < memberValue.expressions.length; j++) {
							if (memberValue.expressions[j] instanceof StringLiteral) {
								StringLiteral val = (StringLiteral) memberValue.expressions[j];
								if (new String(val.source()).equals(NOEXPLICITCONSTRUCTORCALL))
									return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private MethodDeclaration makePreMethod(ClassScope scope, ExplicitConstructorCall explicitConstructorCall) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(scope);

		UnresolvedType aspectTypeX = world.fromBinding(binding.declaringClass);
		UnresolvedType targetTypeX = world.fromBinding(onTypeBinding);

		ArrayBinding objectArrayBinding = scope.createArrayType(scope.getJavaLangObject(), 1);

		MethodDeclaration pre = new MethodDeclaration(compilationResult);
		pre.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
		pre.returnType = AstUtil.makeTypeReference(objectArrayBinding);
		pre.selector = NameMangler.postIntroducedConstructor(aspectTypeX, targetTypeX).toCharArray();

		pre.arguments = AstUtil.copyArguments(this.arguments);

		// XXX should do exceptions

		pre.scope = new MethodScope(scope, pre, true);
		// ??? do we need to do anything with scope???

		// Use the factory to build a semi-correct resolvedmember - then patch it up with
		// reset calls. This is SAFE
		ResolvedMemberImpl preIntroducedConstructorRM = world.makeResolvedMember(binding);
		preIntroducedConstructorRM.resetName(NameMangler.preIntroducedConstructor(aspectTypeX, targetTypeX));
		preIntroducedConstructorRM.resetModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
		preIntroducedConstructorRM.resetReturnTypeToObjectArray();

		pre.binding = world.makeMethodBinding(preIntroducedConstructorRM);

		pre.bindArguments();
		pre.bindThrownExceptions();

		if (explicitConstructorCall == null) {
			pre.statements = new Statement[] {};
		} else {
			pre.statements = new Statement[] { explicitConstructorCall };
		}

		InterTypeScope newParent = new InterTypeScope(scope, onTypeBinding);
		pre.scope.parent = newParent;

		pre.resolveStatements(); // newParent);

		int nParams = pre.arguments.length;
		MethodBinding explicitConstructor = null;
		if (explicitConstructorCall != null) {
			explicitConstructor = explicitConstructorCall.binding;
			// If it is null then we are going to report something else is wrong with this code!
			if (explicitConstructor != null && explicitConstructor.alwaysNeedsAccessMethod()) {
				explicitConstructor = explicitConstructor.getAccessMethod(true);
			}
		}

		int nExprs;
		if (explicitConstructor == null)
			nExprs = 0;
		else
			nExprs = explicitConstructor.parameters.length;

		ArrayInitializer init = new ArrayInitializer();
		init.expressions = new Expression[nExprs + nParams];
		int index = 0;
		for (int i = 0; i < nExprs; i++) {
			if (i >= (explicitConstructorCall.arguments == null ? 0 : explicitConstructorCall.arguments.length)) {
				init.expressions[index++] = new NullLiteral(0, 0);
				continue;
			}

			Expression arg = explicitConstructorCall.arguments[i];
			ResolvedMember conversionMethod = AjcMemberMaker.toObjectConversionMethod(world
					.fromBinding(explicitConstructorCall.binding.parameters[i]));
			if (conversionMethod != null) {
				arg = new KnownMessageSend(world.makeMethodBindingForCall(conversionMethod), new CastExpression(new NullLiteral(0,
						0), AstUtil.makeTypeReference(world.makeTypeBinding(AjcMemberMaker.CONVERSIONS_TYPE))),
						new Expression[] { arg });
			}
			init.expressions[index++] = arg;
		}

		for (int i = 0; i < nParams; i++) {
			LocalVariableBinding binding = pre.arguments[i].binding;
			Expression arg = AstUtil.makeResolvedLocalVariableReference(binding);
			ResolvedMember conversionMethod = AjcMemberMaker.toObjectConversionMethod(world.fromBinding(binding.type));
			if (conversionMethod != null) {
				arg = new KnownMessageSend(world.makeMethodBindingForCall(conversionMethod), new CastExpression(new NullLiteral(0,
						0), AstUtil.makeTypeReference(world.makeTypeBinding(AjcMemberMaker.CONVERSIONS_TYPE))),
						new Expression[] { arg });
			}
			init.expressions[index++] = arg;
		}

		init.binding = objectArrayBinding;

		ArrayAllocationExpression newArray = new ArrayAllocationExpression();
		newArray.initializer = init;
		newArray.type = AstUtil.makeTypeReference(scope.getJavaLangObject());
		newArray.dimensions = new Expression[1];
		newArray.constant = Constant.NotAConstant;

		pre.statements = new Statement[] { new ReturnStatement(newArray, 0, 0), };
		return pre;
	}

	public EclipseTypeMunger build(ClassScope classScope) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);

		resolveOnType(classScope);
		if (ignoreFurtherInvestigation)
			return null;

		binding = classScope.referenceContext.binding.resolveTypesFor(binding);

		if (isTargetAnnotation(classScope, "constructor"))
			return null; // Error message output in isTargetAnnotation
		if (isTargetEnum(classScope, "constructor"))
			return null; // Error message output in isTargetEnum

		if (onTypeBinding.isInterface()) {
			classScope.problemReporter().signalError(sourceStart, sourceEnd, "can't define constructors on interfaces");
			ignoreFurtherInvestigation = true;
			return null;
		}

		if (onTypeBinding.isNestedType()) {
			classScope.problemReporter().signalError(sourceStart, sourceEnd,
					"can't define constructors on nested types (compiler limitation)");
			ignoreFurtherInvestigation = true;
			return null;
		}

		ResolvedType declaringTypeX = world.fromEclipse(onTypeBinding);
		ResolvedType aspectType = world.fromEclipse(classScope.referenceContext.binding);

		if (interTypeScope == null)
			return null; // We encountered a problem building the scope, don't continue - error already reported

		// This signature represents what we want consumers of the targetted type to 'see'
		ResolvedMemberImpl signature = world.makeResolvedMemberForITD(binding, onTypeBinding, interTypeScope.getRecoveryAliases());
		signature.resetKind(Member.CONSTRUCTOR);
		signature.resetName("<init>");
		int resetModifiers = declaredModifiers;
		if (binding.isVarargs())
			resetModifiers = resetModifiers | Constants.ACC_VARARGS;
		signature.resetModifiers(resetModifiers);

		ResolvedMember syntheticInterMember = AjcMemberMaker.interConstructor(declaringTypeX, signature, aspectType);

		NewConstructorTypeMunger myMunger = new NewConstructorTypeMunger(signature, syntheticInterMember, null, null,
				typeVariableAliases);
		setMunger(myMunger);
		myMunger.check(world.getWorld());

		this.selector = binding.selector = NameMangler.postIntroducedConstructor(world.fromBinding(binding.declaringClass),
				declaringTypeX).toCharArray();

		return new EclipseTypeMunger(world, myMunger, aspectType, this);
	}

	private AjAttribute makeAttribute(EclipseFactory world) {
		if (explicitConstructorCall != null && (explicitConstructorCall.binding != null)
				&& !(explicitConstructorCall.binding instanceof ProblemMethodBinding)) {
			MethodBinding explicitConstructor = explicitConstructorCall.binding;
			if (explicitConstructor.alwaysNeedsAccessMethod()) {
				explicitConstructor = explicitConstructor.getAccessMethod(true);
			}
			if (explicitConstructor instanceof ParameterizedMethodBinding) {
				explicitConstructor = explicitConstructor.original();
			}
			((NewConstructorTypeMunger) munger).setExplicitConstructor(world.makeResolvedMember(explicitConstructor));
		} else {
			((NewConstructorTypeMunger) munger).setExplicitConstructor(new ResolvedMemberImpl(Member.CONSTRUCTOR, world
					.fromBinding(onTypeBinding.superclass()), 0, UnresolvedType.VOID, "<init>", UnresolvedType.NONE));
		}
		return new AjAttribute.TypeMunger(munger);
	}

	public void generateCode(ClassScope classScope, ClassFile classFile) {
		if (ignoreFurtherInvestigation)
			return;
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);
		classFile.extraAttributes.add(new EclipseAttributeAdapter(makeAttribute(world)));
		super.generateCode(classScope, classFile);
		// classFile.codeStream.generateAttributes &= ~ClassFileConstants.ATTR_VARS;
		preMethod.generateCode(classScope, classFile);
	}

	protected Shadow.Kind getShadowKindForBody() {
		return Shadow.ConstructorExecution;
	}

}
