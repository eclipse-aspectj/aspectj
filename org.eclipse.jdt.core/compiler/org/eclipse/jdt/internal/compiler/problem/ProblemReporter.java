/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.impl.*;

public class ProblemReporter extends ProblemHandler implements ProblemReasons {
	
	public ReferenceContext referenceContext;
public ProblemReporter(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
	super(policy, options, problemFactory);
}
public void abortDueToInternalError(String errorMessage) {
	this.handle(
		IProblem.Unclassified,
		new String[] {errorMessage},
		Error | Abort,
		0,
		0);
}
public void abortDueToInternalError(String errorMessage, AstNode location) {
	this.handle(
		IProblem.Unclassified,
		new String[] {errorMessage},
		Error | Abort,
		location.sourceStart,
		location.sourceEnd);
}
public void abstractMethodCannotBeOverridden(SourceTypeBinding type, MethodBinding concreteMethod) {

	this.handle(
		// %1 must be abstract since it cannot override the inherited package-private abstract method %2
		IProblem.AbstractMethodCannotBeOverridden,
		new String[] {new String(type.sourceName()), new String(concreteMethod.readableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodInAbstractClass(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.AbstractMethodInAbstractClass,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void abstractMethodMustBeImplemented(SourceTypeBinding type, MethodBinding abstractMethod) {
	this.handle(
		// Must implement the inherited abstract method %1
		// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
		IProblem.AbstractMethodMustBeImplemented,
		new String[] {
			new String(
				CharOperation.concat(
					abstractMethod.declaringClass.readableName(),
					abstractMethod.readableName(),
					'.'))},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodNeedingNoBody(AbstractMethodDeclaration method) {
	this.handle(
		IProblem.BodyForAbstractMethod,
		new String[0],
		method.sourceStart,
		method.sourceEnd,
		method,
		method.compilationResult());
}
public void alreadyDefinedLabel(char[] labelName, AstNode location) {
	this.handle(
		IProblem.DuplicateLabel,
		new String[] {new String(labelName)},
		location.sourceStart,
		location.sourceEnd);
}
public void anonymousClassCannotExtendFinalClass(Expression expression, TypeBinding type) {
	this.handle(
		IProblem.AnonymousClassCannotExtendFinalClass,
		new String[] {new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void argumentTypeCannotBeVoid(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	this.handle(
		IProblem.ArgumentTypeCannotBeVoid,
		new String[] {new String(methodDecl.selector), new String(arg.name)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void argumentTypeCannotBeVoidArray(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	this.handle(
		IProblem.ArgumentTypeCannotBeVoidArray,
		new String[] {new String(methodDecl.selector), new String(arg.name)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void argumentTypeProblem(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ArgumentTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ArgumentTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ArgumentTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ArgumentTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ArgumentTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(methodDecl.selector), arg.name(), new String(expectedType.readableName())},
		arg.type.sourceStart,
		arg.type.sourceEnd);
}
public void arrayConstantsOnlyInArrayInitializers(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.ArrayConstantsOnlyInArrayInitializers,
		new String[0],
		sourceStart,
		sourceEnd);
}
public void attemptToReturnNonVoidExpression(ReturnStatement returnStatement, TypeBinding expectedType) {
	this.handle(
		IProblem.VoidMethodReturnsValue,
		new String[] {new String(expectedType.readableName())},
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}
public void attemptToReturnVoidValue(ReturnStatement returnStatement) {
	this.handle(
		IProblem.MethodReturnsVoid,
		new String[] {},
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}
public void bytecodeExceeds64KLimit(AbstractMethodDeclaration location) {
	this.handle(
		IProblem.BytecodeExceeds64KLimit,
		new String[] {new String(location.selector)},
		Error | Abort,
		location.sourceStart,
		location.sourceEnd);
}
public void bytecodeExceeds64KLimit(TypeDeclaration location) {
	this.handle(
		IProblem.BytecodeExceeds64KLimitForClinit,
		new String[0],
		Error | Abort,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotAllocateVoidArray(Expression expression) {
	this.handle(
		IProblem.CannotAllocateVoidArray,
		new String[] {},
		expression.sourceStart,
		expression.sourceEnd);
}
public void cannotAssignToFinalField(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.FinalFieldAssignment,
		new String[] {
			(field.declaringClass == null ? "array" : new String(field.declaringClass.readableName())), //$NON-NLS-1$
			new String(field.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void cannotAssignToFinalOuterLocal(LocalVariableBinding local, AstNode location) {
	this.handle(
		IProblem.FinalOuterLocalAssignment,
		new String[] {new String(local.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void cannotDeclareLocalInterface(char[] interfaceName, int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.CannotDefineInterfaceInLocalType,
		new String[] {new String(interfaceName)},
		sourceStart,
		sourceEnd);
}
public void cannotDefineDimensionsAndInitializer(ArrayAllocationExpression expresssion) {
	this.handle(
		IProblem.CannotDefineDimensionExpressionsWithInit,
		new String[0],
		expresssion.sourceStart,
		expresssion.sourceEnd);
}
public void cannotDireclyInvokeAbstractMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		IProblem.DirectInvocationOfAbstractMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void cannotImportPackage(ImportReference importRef) {
	this.handle(
		IProblem.CannotImportPackage,
		new String[] {CharOperation.toString(importRef.tokens)},
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void cannotInstantiate(TypeReference typeRef, TypeBinding type) {
	this.handle(
		IProblem.InvalidClassInstantiation,
		new String[] {new String(type.readableName())},
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
public void cannotReferToNonFinalOuterLocal(LocalVariableBinding local, AstNode location) {
	this.handle(
		IProblem.OuterLocalMustBeFinal,
		new String[] {new String(local.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void cannotReturnInInitializer(AstNode location) {
	this.handle(
		IProblem.CannotReturnInInitializer,
		new String[0],
		location.sourceStart,
		location.sourceEnd);
}
public void cannotThrowNull(ThrowStatement statement) {
	this.handle(
		IProblem.CannotThrowNull,
		new String[0],
		statement.sourceStart,
		statement.sourceEnd);
}
public void cannotThrowType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, TypeReference exceptionType, TypeBinding expectedType) {
	this.handle(
		IProblem.CannotThrowType,
		new String[] {new String(expectedType.readableName())},
		exceptionType.sourceStart,
		exceptionType.sourceEnd);
}
public void cannotUseSuperInJavaLangObject(AstNode reference) {
	this.handle(
		IProblem.ObjectHasNoSuperclass,
		new String[0],
		reference.sourceStart,
		reference.sourceEnd);
}
public void cannotUseSuperInCodeSnippet(int start, int end) {
	this.handle(
		IProblem.CannotUseSuperInCodeSnippet,
		new String[0],
		Error | Abort,
		start,
		end);
}
public void caseExpressionMustBeConstant(Expression expression) {
	this.handle(
		IProblem.NonConstantExpression,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void classExtendFinalClass(SourceTypeBinding type, TypeReference superclass, TypeBinding expectedType) {
	this.handle(
		IProblem.ClassExtendFinalClass,
		new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
		superclass.sourceStart,
		superclass.sourceEnd);
}
public void codeSnippetMissingClass(String missing, int start, int end) {
	this.handle(
		IProblem.CodeSnippetMissingClass,
		new String[]{ missing },
		Error | Abort,
		start,
		end);
}
public void codeSnippetMissingMethod(String className, String missingMethod, String argumentTypes, int start, int end) {
	this.handle(
		IProblem.CodeSnippetMissingMethod,
		new String[]{ className, missingMethod, argumentTypes },
		Error | Abort,
		start,
		end);
}
/*
 * Given the current configuration, answers which category the problem
 * falls into:
 *		Error | Warning | Ignore
 */
public int computeSeverity(int problemId){

	// severity can have been preset on the problem
//	if ((problem.severity & Fatal) != 0){
//		return Error;
//	}

	// if not then check whether it is a configurable problem
	int errorThreshold = options.errorThreshold;
	int warningThreshold = options.warningThreshold;
	
	switch(problemId){

		case IProblem.UnreachableCatch :
		case IProblem.CodeCannotBeReached :
			if ((errorThreshold & CompilerOptions.UnreachableCode) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnreachableCode) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.MaskedCatch : 
			if ((errorThreshold & CompilerOptions.MaskedCatchBlock) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.MaskedCatchBlock) != 0){
				return Warning;
			}
			return Ignore;
			
/*
		case Never Used  :
			if ((errorThreshold & ParsingOptionalError) != 0){
				return Error;
			}
			if ((warningThreshold & ParsingOptionalError) != 0){
				return Warning;
			}
			return Ignore;
*/
		case IProblem.ImportNotFound :
		case IProblem.ImportNotVisible :
		case IProblem.ImportAmbiguous :
		case IProblem.ImportInternalNameProvided :
		case IProblem.ImportInheritedNameHidesEnclosingName :
		case IProblem.DuplicateImport :
		case IProblem.ConflictingImport :
		case IProblem.CannotImportPackage :
			if ((errorThreshold & CompilerOptions.ImportProblem) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.ImportProblem) != 0){
				return Warning;
			}
			return Ignore;
			
		case IProblem.UnusedImport :
			// if import problem are disabled, then ignore
			if ((errorThreshold & CompilerOptions.ImportProblem) == 0 
				&& (warningThreshold & CompilerOptions.ImportProblem) == 0){
				return Ignore;
			}
			if ((errorThreshold & CompilerOptions.UnusedImport) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnusedImport) != 0){
				return Warning;
			}
			return Ignore;
			
/*		
		case UnnecessaryEnclosingInstanceSpecification :
			if ((errorThreshold & UnnecessaryEnclosingInstance) != 0){
				return Error;
			}
			if ((warningThreshold & UnnecessaryEnclosingInstance) != 0){
				return Warning;
			}
			return Ignore;
*/		
		case IProblem.MethodButWithConstructorName :
			if ((errorThreshold & CompilerOptions.MethodWithConstructorName) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.MethodWithConstructorName) != 0){
				return Warning;
			}
			return Ignore;
		
		case IProblem.OverridingNonVisibleMethod :
			if ((errorThreshold & CompilerOptions.OverriddenPackageDefaultMethod) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.OverriddenPackageDefaultMethod) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.OverridingDeprecatedMethod :				
		case IProblem.UsingDeprecatedType :				
		case IProblem.UsingDeprecatedMethod :
		case IProblem.UsingDeprecatedConstructor :
		case IProblem.UsingDeprecatedField :
			if ((errorThreshold & CompilerOptions.UsingDeprecatedAPI) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UsingDeprecatedAPI) != 0){
				return Warning;
			}
			return Ignore;
		
		case IProblem.LocalVariableIsNeverUsed :
			if ((errorThreshold & CompilerOptions.UnusedLocalVariable) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnusedLocalVariable) != 0){
				return Warning;
			}
			return Ignore;
		
		case IProblem.ArgumentIsNeverUsed :
			if ((errorThreshold & CompilerOptions.UnusedArgument) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnusedArgument) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.NoImplicitStringConversionForCharArrayExpression :
			if ((errorThreshold & CompilerOptions.NoImplicitStringConversion) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.NoImplicitStringConversion) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.NeedToEmulateFieldReadAccess :
		case IProblem.NeedToEmulateFieldWriteAccess :
		case IProblem.NeedToEmulateMethodAccess :
		case IProblem.NeedToEmulateConstructorAccess :			
			if ((errorThreshold & CompilerOptions.AccessEmulation) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.AccessEmulation) != 0){
				return Warning;
			}
			return Ignore;
		case IProblem.NonExternalizedStringLiteral :
			if ((errorThreshold & CompilerOptions.NonExternalizedString) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.NonExternalizedString) != 0){
				return Warning;
			}
			return Ignore;
		case IProblem.UseAssertAsAnIdentifier :
			if ((errorThreshold & CompilerOptions.AssertUsedAsAnIdentifier) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.AssertUsedAsAnIdentifier) != 0){
				return Warning;
			}
			return Ignore;		
		default:
			return Error;
	}
}
public void conditionalArgumentsIncompatibleTypes(ConditionalExpression expression, TypeBinding trueType, TypeBinding falseType) {
	this.handle(
		IProblem.IncompatibleTypesInConditionalOperator,
		new String[] {new String(trueType.readableName()), new String(falseType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void conflictingImport(ImportReference importRef) {
	this.handle(
		IProblem.ConflictingImport,
		new String[] {CharOperation.toString(importRef.tokens)},
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void constantOutOfFormat(NumberLiteral lit) {
	// the literal is not in a correct format
	// this code is called on IntLiteral and LongLiteral 
	// example 000811 ...the 8 is uncorrect.

	if ((lit instanceof LongLiteral) || (lit instanceof IntLiteral)) {
		char[] source = lit.source();
		try {
			final String Radix;
			final int radix;
			if ((source[1] == 'x') || (source[1] == 'X')) {
				radix = 16;
				Radix = "Hexa"; //$NON-NLS-1$
			} else {
				radix = 8;
				Radix = "Octal"; //$NON-NLS-1$
			}
			//look for the first digit that is incorrect
			int place = -1;
			label : for (int i = radix == 8 ? 1 : 2; i < source.length; i++) {
				if (Character.digit(source[i], radix) == -1) {
					place = i;
					break label;
				}
			}

			this.handle(
				IProblem.NumericValueOutOfRange,
				new String[] {Radix + " " + new String(source) + " (digit " + new String(new char[] {source[place]}) + ")"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				lit.sourceStart,
				lit.sourceEnd);
			return;
		} catch (IndexOutOfBoundsException ex) {}
	
		// just in case .... use a predefined error..
		// we should never come here...(except if the code changes !)
		this.constantOutOfRange(lit);
	}
}
public void constantOutOfRange(Literal lit) {
	// lit is some how out of range of it declared type
	// example 9999999999999999999999999999999999999999999999999999999999999999999

	this.handle(
		IProblem.NumericValueOutOfRange,
		new String[] {new String(lit.source())},
		lit.sourceStart,
		lit.sourceEnd);
}
public void deprecatedField(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.UsingDeprecatedField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void deprecatedMethod(MethodBinding method, AstNode location) {
	if (method.isConstructor())
		this.handle(
			IProblem.UsingDeprecatedConstructor,
			new String[] {new String(method.declaringClass.readableName()), parametersAsString(method)},
			location.sourceStart,
			location.sourceEnd);
	else
		this.handle(
			IProblem.UsingDeprecatedMethod,
			new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
			location.sourceStart,
			location.sourceEnd);
}
public void deprecatedType(TypeBinding type, AstNode location) {
	if (location == null) return; // 1G828DN - no type ref for synthetic arguments
	this.handle(
		IProblem.UsingDeprecatedType,
		new String[] {new String(type.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void duplicateCase(Case statement, Constant constant) {
	this.handle(
		IProblem.DuplicateCase,
		new String[] {String.valueOf(constant.intValue())},
		statement.sourceStart,
		statement.sourceEnd);
}
public void duplicateDefaultCase(DefaultCase statement) {
	this.handle(
		IProblem.DuplicateDefaultCase,
		new String[0],
		statement.sourceStart,
		statement.sourceEnd);
}
public void duplicateFieldInType(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.DuplicateField,
		new String[] {new String(type.sourceName()), fieldDecl.name()},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void duplicateImport(ImportReference importRef) {
	this.handle(
		IProblem.DuplicateImport,
		new String[] {CharOperation.toString(importRef.tokens)},
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void duplicateInitializationOfBlankFinalField(FieldBinding field, Reference reference) {
	this.handle(
		IProblem.DuplicateBlankFinalFieldInitialization,
		new String[] {new String(field.readableName())},
		reference.sourceStart,
		reference.sourceEnd);
}
public void duplicateInitializationOfFinalLocal(LocalVariableBinding local, NameReference reference) {
	this.handle(
		IProblem.DuplicateFinalLocalInitialization,
		new String[] {new String(local.readableName())},
		reference.sourceStart,
		reference.sourceEnd);
}
public void duplicateMethodInType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.DuplicateMethod,
		new String[] {new String(methodDecl.selector), new String(type.sourceName())},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void duplicateModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
/* to highlight modifiers use:
	this.handle(
		new Problem(
			DuplicateModifierForField,
			new String[] {fieldDecl.name()},
			fieldDecl.modifiers.sourceStart,
			fieldDecl.modifiers.sourceEnd));
*/

	this.handle(
		IProblem.DuplicateModifierForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void duplicateModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.DuplicateModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void duplicateModifierForType(SourceTypeBinding type) {
	this.handle(
		IProblem.DuplicateModifierForType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void duplicateModifierForVariable(LocalDeclaration localDecl, boolean complainForArgument) {
	this.handle(
		complainForArgument
			?  IProblem.DuplicateModifierForArgument 
			: IProblem.DuplicateModifierForVariable,
		new String[] {localDecl.name()},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void duplicateNestedType(TypeDeclaration typeDecl) {
	this.handle(
		IProblem.DuplicateNestedType,
		new String[] {new String(typeDecl.name)},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void duplicateSuperinterface(SourceTypeBinding type, TypeDeclaration typeDecl, ReferenceBinding superType) {
	this.handle(
		IProblem.DuplicateSuperInterface,
		new String[] {
			new String(superType.readableName()),
			new String(type.sourceName())},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void duplicateTypes(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		IProblem.DuplicateTypes,
		new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)},
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void errorNoMethodFor(MessageSend messageSend, TypeBinding recType, TypeBinding[] params) {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "); //$NON-NLS-1$
		buffer.append(new String(params[i].readableName()));
	}

	this.handle(
		recType.isArrayType() ? IProblem.NoMessageSendOnArrayType : IProblem.NoMessageSendOnBaseType,
		new String[] {new String(recType.readableName()), new String(messageSend.selector), buffer.toString()},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void errorThisSuperInStatic(AstNode reference) {
	this.handle(
		IProblem.ThisInStaticContext,
		new String[] {reference.isSuper() ? "super" : "this"}, //$NON-NLS-2$ //$NON-NLS-1$
		reference.sourceStart,
		reference.sourceEnd);
}
public void exceptionTypeProblem(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, TypeReference exceptionType, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ExceptionTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ExceptionTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ExceptionTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ExceptionTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ExceptionTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(methodDecl.selector), new String(expectedType.readableName())},
		exceptionType.sourceStart,
		exceptionType.sourceEnd);
}
public void fieldsOrThisBeforeConstructorInvocation(ThisReference reference) {
	this.handle(
		IProblem.ThisSuperDuringConstructorInvocation,
		new String[0],
		reference.sourceStart,
		reference.sourceEnd);
}
public void fieldTypeProblem(SourceTypeBinding type, FieldDeclaration fieldDecl, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.FieldTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.FieldTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.FieldTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.FieldTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.FieldTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {fieldDecl.name(), new String(type.sourceName()), new String(expectedType.readableName())},
		fieldDecl.type.sourceStart,
		fieldDecl.type.sourceEnd);
}
public void finalMethodCannotBeOverridden(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		// Cannot override the final method from %1
		// 8.4.3.3 - Final methods cannot be overridden or hidden.
		IProblem.FinalMethodCannotBeOverridden,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void forwardReference(Reference reference, int indexInQualification, TypeBinding type) {
	this.handle(
		IProblem.ReferenceToForwardField,
		new String[] {},
		reference.sourceStart,
		reference.sourceEnd);
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments

private void handle(
	int problemId, 
	String[] problemArguments,
	int problemStartPosition, 
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
			problemStartPosition,
			problemEndPosition,
			referenceContext, 
			referenceContext == null ? null : referenceContext.compilationResult()); 
	referenceContext = null;
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments

private void handle(
	int problemId, 
	String[] problemArguments,
	int severity,
	int problemStartPosition, 
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
			severity,
			problemStartPosition,
			problemEndPosition,
			referenceContext, 
			referenceContext == null ? null : referenceContext.compilationResult()); 
	referenceContext = null;
}
// use this private API when the compilation unit result cannot be found through the
// reference context. 

private void handle(
	int problemId, 
	String[] problemArguments,
	int problemStartPosition, 
	int problemEndPosition,
	CompilationResult unitResult){

	this.handle(
			problemId,
			problemArguments,
			problemStartPosition,
			problemEndPosition,
			referenceContext, 
			unitResult); 
	referenceContext = null;
}
public void hidingEnclosingType(TypeDeclaration typeDecl) {
	this.handle(
		IProblem.HidingEnclosingType,
		new String[] {new String(typeDecl.name)},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void hierarchyCircularity(SourceTypeBinding sourceType, ReferenceBinding superType, TypeReference reference) {
	int start = 0;
	int end = 0;
	String typeName = ""; //$NON-NLS-1$

	if (reference == null) {	// can only happen when java.lang.Object is busted
		start = sourceType.sourceStart();
		end = sourceType.sourceEnd();
		typeName = new String(superType.readableName());
	} else {
		start = reference.sourceStart;
		end = reference.sourceEnd;
		typeName = CharOperation.toString(reference.getTypeName());
	}

	if (sourceType == superType)
		this.handle(
			IProblem.HierarchyCircularitySelfReference,
			new String[] {new String(sourceType.sourceName()), typeName},
			start,
			end);
	else
		this.handle(
			IProblem.HierarchyCircularity,
			new String[] {new String(sourceType.sourceName()), typeName},
			start,
			end);
}
public void hierarchyHasProblems(SourceTypeBinding type) {
	this.handle(
		IProblem.HierarchyHasProblems,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalAbstractModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalAbstractModifierCombinationForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierCombinationFinalAbstractForClass(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalModifierCombinationFinalAbstractForClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierCombinationFinalVolatileForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.IllegalModifierCombinationFinalVolatileForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}

public void illegalModifierForClass(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalModifierForClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.IllegalModifierForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForInterface(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalModifierForInterface,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForInterfaceField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.IllegalModifierForInterfaceField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForInterfaceMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalModifierForInterfaceMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForLocalClass(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalModifierForLocalClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberClass(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalModifierForMemberClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberInterface(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalModifierForMemberInterface,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForVariable(LocalDeclaration localDecl, boolean complainAsArgument) {
	this.handle(
		complainAsArgument
			? IProblem.IllegalModifierForArgument
			: IProblem.IllegalModifierForVariable,
		new String[] {localDecl.name()},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void illegalPrimitiveOrArrayTypeForEnclosingInstance(TypeBinding enclosingType, AstNode location) {
	this.handle(
		IProblem.IllegalPrimitiveOrArrayTypeForEnclosingInstance,
		new String[] {new String(enclosingType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void illegalStaticModifierForMemberType(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalStaticModifierForMemberType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVisibilityModifierCombinationForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForField,
		new String[] {new String(fieldDecl.name())},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalVisibilityModifierCombinationForMemberType(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForMemberType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVisibilityModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalVisibilityModifierForInterfaceMemberType(SourceTypeBinding type) {
	this.handle(
		IProblem.IllegalVisibilityModifierForInterfaceMemberType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVoidExpression(AstNode location) {
	this.handle(
		IProblem.InvalidVoidExpression,
		new String[] {},
		location.sourceStart,
		location.sourceEnd);
}
public void importProblem(ImportReference importRef, Binding expectedImport) {
	int problemId = expectedImport.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ImportNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ImportNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ImportAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ImportInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ImportInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	String argument;
	if(expectedImport instanceof ProblemReferenceBinding) {
		argument = CharOperation.toString(((ProblemReferenceBinding)expectedImport).compoundName);
	} else {
		argument = CharOperation.toString(importRef.tokens);
	}
	this.handle(id, new String[] {argument}, importRef.sourceStart, importRef.sourceEnd);
}
public void incompatibleExceptionInThrowsClause(SourceTypeBinding type, MethodBinding currentMethod, MethodBinding inheritedMethod, ReferenceBinding exceptionType) {
	if (type == currentMethod.declaringClass)
		this.handle(
			// Exception %1 is not compatible with throws clause in %2
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			IProblem.IncompatibleExceptionInThrowsClause,
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.readableName(),
						inheritedMethod.readableName(),
						'.'))},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	else	
		this.handle(
			// Exception %1 in throws clause of %2 is not compatible with %3
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			IProblem.IncompatibleExceptionInInheritedMethodThrowsClause,
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						currentMethod.declaringClass.sourceName(),
						currentMethod.readableName(),
						'.')),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.readableName(),
						inheritedMethod.readableName(),
						'.'))},
			type.sourceStart(),
			type.sourceEnd());
}
public void incompatibleReturnType(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	StringBuffer methodSignature = new StringBuffer();
	methodSignature
		.append(inheritedMethod.declaringClass.readableName())
		.append('.')
		.append(inheritedMethod.readableName());

	this.handle(
		// Return type is incompatible with %1
		// 9.4.2 - The return type from the method is incompatible with the declaration.
		IProblem.IncompatibleReturnType,
		new String[] {methodSignature.toString()},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void incorrectEnclosingInstanceReference(
	QualifiedThisReference reference, 
	TypeBinding qualificationType) {
		
	this.handle(
		IProblem.IncorrectEnclosingInstanceReference, 
		new String[] { new String(qualificationType.readableName())}, 
		reference.sourceStart, 
		reference.sourceEnd); 
}
public void incorrectLocationForEmptyDimension(ArrayAllocationExpression expression, int index) {
	this.handle(
		IProblem.IllegalDimension,
		new String[0],
		expression.dimensions[index + 1].sourceStart,
		expression.dimensions[index + 1].sourceEnd);
}
public void incorrectSwitchType(Expression expression, TypeBinding testType) {
	this.handle(
		IProblem.IncorrectSwitchType,
		new String[] {new String(testType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void inheritedMethodReducesVisibility(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	StringBuffer concreteSignature = new StringBuffer();
	concreteSignature
		.append(concreteMethod.declaringClass.readableName())
		.append('.')
		.append(concreteMethod.readableName());
	this.handle(
		// The inherited method %1 cannot hide the public abstract method in %2
		IProblem.InheritedMethodReducesVisibility,
		new String[] {
			new String(concreteSignature.toString()),
			new String(abstractMethods[0].declaringClass.readableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void inheritedMethodsHaveIncompatibleReturnTypes(SourceTypeBinding type, MethodBinding[] inheritedMethods, int length) {
	StringBuffer methodSignatures = new StringBuffer();
	for (int i = length; --i >= 0;) {
		methodSignatures
			.append(inheritedMethods[i].declaringClass.readableName())
			.append('.')
			.append(inheritedMethods[i].readableName());
		if (i != 0)
			methodSignatures.append(", "); //$NON-NLS-1$
	}

	this.handle(
		// Return type is incompatible with %1
		// 9.4.2 - The return type from the method is incompatible with the declaration.
		IProblem.IncompatibleReturnType,
		new String[] {methodSignatures.toString()},
		type.sourceStart(),
		type.sourceEnd());
}
public void initializerMustCompleteNormally(FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.InitializerMustCompleteNormally,
		new String[0],
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void innerTypesCannotDeclareStaticInitializers(ReferenceBinding innerType, AstNode location) {
	this.handle(
		IProblem.CannotDefineStaticInitializerInLocalType,
		new String[] {new String(innerType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void interfaceCannotHaveConstructors(ConstructorDeclaration constructor) {
	this.handle(
		IProblem.InterfaceCannotHaveConstructors,
		new String[0],
		constructor.sourceStart,
		constructor.sourceEnd,
		constructor,
		constructor.compilationResult());
}
public void interfaceCannotHaveInitializers(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.InterfaceCannotHaveInitializers,
		new String[] {new String(type.sourceName())},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void invalidBreak(AstNode location) {
	this.handle(
		IProblem.InvalidBreak,
		new String[0],
		location.sourceStart,
		location.sourceEnd);
}
public void invalidConstructor(Statement statement, MethodBinding targetConstructor) {

	boolean insideDefaultConstructor = 
		(referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)referenceContext).isDefaultConstructor();
	boolean insideImplicitConstructorCall =
		(statement instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) statement).accessMode == ExplicitConstructorCall.ImplicitSuper);

	int flag = IProblem.UndefinedConstructor; //default...
	switch (targetConstructor.problemId()) {
		case NotFound :
			if (insideDefaultConstructor){
				flag = IProblem.UndefinedConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				flag = IProblem.UndefinedConstructorInImplicitConstructorCall;
			} else {
				flag = IProblem.UndefinedConstructor;
			}
			break;
		case NotVisible :
			if (insideDefaultConstructor){
				flag = IProblem.NotVisibleConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				flag = IProblem.NotVisibleConstructorInImplicitConstructorCall;
			} else {
				flag = IProblem.NotVisibleConstructor;
			}
			break;
		case Ambiguous :
			if (insideDefaultConstructor){
				flag = IProblem.AmbiguousConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				flag = IProblem.AmbiguousConstructorInImplicitConstructorCall;
			} else {
				flag = IProblem.AmbiguousConstructor;
			}
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	
	this.handle(
		flag,
		new String[] {new String(targetConstructor.declaringClass.readableName()), parametersAsString(targetConstructor)},
		statement.sourceStart,
		statement.sourceEnd);
}
public void invalidContinue(AstNode location) {
	this.handle(
		IProblem.InvalidContinue,
		new String[0],
		location.sourceStart,
		location.sourceEnd);
}
public void invalidEnclosingType(Expression expression, TypeBinding type, TypeBinding enclosingType) {

	int flag = IProblem.UndefinedType; // default
	switch (type.problemId()) {
		case NotFound : // 1
			flag = IProblem.UndefinedType;
			break;
		case NotVisible : // 2
			flag = IProblem.NotVisibleType;
			break;
		case Ambiguous : // 3
			flag = IProblem.AmbiguousType;
			break;
		case InternalNameProvided :
			flag = IProblem.InternalTypeNameProvided;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(enclosingType.readableName()) + "." + new String(type.readableName())}, //$NON-NLS-1$
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidExpressionAsStatement(Expression expression){
	this.handle(
		IProblem.InvalidExpressionAsStatement,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidField(FieldReference fieldRef, TypeBinding searchedType) {
	int severity = Error;
	int flag = IProblem.UndefinedField;
	FieldBinding field = fieldRef.binding;
	switch (field.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			flag = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(field.readableName())},
		severity,
		fieldRef.sourceStart,
		fieldRef.sourceEnd);
}
public void invalidField(NameReference nameRef, FieldBinding field) {
	int flag = IProblem.UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedField;
			break;
		case NotVisible :
			flag = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	this.handle(
		flag,
		new String[] {new String(field.readableName())},
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void invalidField(QualifiedNameReference nameRef, FieldBinding field, int index, TypeBinding searchedType) {
	//the resolution of the index-th field of qname failed
	//qname.otherBindings[index] is the binding that has produced the error

	//The different targetted errors should be :
	//UndefinedField
	//NotVisibleField
	//AmbiguousField

	if (searchedType.isBaseType()) {
		this.handle(
			IProblem.NoFieldOnBaseType,
			new String[] {
				new String(searchedType.readableName()),
				CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index)),
				new String(nameRef.tokens[index])},
			nameRef.sourceStart,
			nameRef.sourceEnd);
		return;
	}

	int flag = IProblem.UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			flag = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	this.handle(
		flag, 
		new String[] {CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index + 1))},
		nameRef.sourceStart, 
		nameRef.sourceEnd); 
}
public void invalidMethod(MessageSend messageSend, MethodBinding method) {
	// CODE should be UPDATED according to error coding in the different method binding errors
	// The different targetted errors should be :
	// 	UndefinedMethod
	//	NotVisibleMethod
	//	AmbiguousMethod
	//  InheritedNameHidesEnclosingName
	//	InstanceMethodDuringConstructorInvocation
	// StaticMethodRequested

	int flag = IProblem.UndefinedMethod; //default...
	switch (method.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedMethod;
			break;
		case NotVisible :
			flag = IProblem.NotVisibleMethod;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousMethod;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedMethodHidesEnclosingName;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceMethodDuringConstructorInvocation;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.StaticMethodRequested;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	if (flag == IProblem.UndefinedMethod) {
		ProblemMethodBinding problemMethod = (ProblemMethodBinding) method;
		if (problemMethod.closestMatch != null) {
				this.handle(
					IProblem.ParameterMismatch,
					new String[] {
						new String(problemMethod.closestMatch.declaringClass.readableName()),
						new String(problemMethod.closestMatch.selector),
						parametersAsString(problemMethod.closestMatch),
						parametersAsString(method)},
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
				return;
		}
	}

	this.handle(
		flag,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector), parametersAsString(method)},
		(int) (messageSend.nameSourcePosition >>> 32),
		(int) messageSend.nameSourcePosition);
}
public void invalidNullToSynchronize(Expression expression) {
	this.handle(
		IProblem.InvalidNullToSynchronized,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(BinaryExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IProblem.InvalidOperator,
		new String[] {
			expression.operatorToString(),
			new String(leftType.readableName()) + ", " + new String(rightType.readableName())}, //$NON-NLS-1$
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(CompoundAssignment assign, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IProblem.InvalidOperator,
		new String[] {
			assign.operatorToString(),
			new String(leftType.readableName()) + ", " + new String(rightType.readableName())}, //$NON-NLS-1$
		assign.sourceStart,
		assign.sourceEnd);
}
public void invalidOperator(UnaryExpression expression, TypeBinding type) {
	this.handle(
		IProblem.InvalidOperator,
		new String[] {expression.operatorToString(), new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidSuperclass(SourceTypeBinding type, TypeReference superclassRef, ReferenceBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.SuperclassNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.SuperclassNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.SuperclassAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.SuperclassInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.SuperclassInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
		superclassRef.sourceStart,
		superclassRef.sourceEnd);
}
public void invalidSuperinterface(SourceTypeBinding type, TypeReference superinterfaceRef, ReferenceBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.InterfaceNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.InterfaceNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.InterfaceAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.InterfaceInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.InterfaceInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
		this.handle(
			id,
			new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
			superinterfaceRef.sourceStart,
			superinterfaceRef.sourceEnd);
}
public void invalidType(AstNode location, TypeBinding type) {
	int flag = IProblem.UndefinedType; // default
	switch (type.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedType;
			break;
		case NotVisible :
			flag = IProblem.NotVisibleType;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousType;
			break;
		case InternalNameProvided :
			flag = IProblem.InternalTypeNameProvided;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedTypeHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(type.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void invalidTypeReference(Expression expression) {
	this.handle(
		IProblem.InvalidTypeExpression,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidTypeToSynchronize(Expression expression, TypeBinding type) {
	this.handle(
		IProblem.InvalidTypeToSynchronized,
		new String[] {new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidUnaryExpression(Expression expression) {
	this.handle(
		IProblem.InvalidUnaryExpression,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void isClassPathCorrect(char[][] wellKnownTypeName, CompilationUnitDeclaration compUnitDecl) {
	referenceContext = compUnitDecl;
	this.handle(
		IProblem.IsClassPathCorrect,
		new String[] {CharOperation.toString(wellKnownTypeName)}, 
		AbortCompilation | Error,
		compUnitDecl == null ? 0 : compUnitDecl.sourceStart,
		compUnitDecl == null ? 1 : compUnitDecl.sourceEnd);
}
public void maskedExceptionHandler(ReferenceBinding exceptionType, AstNode location) {
	this.handle(
		IProblem.MaskedCatch,
		new String[0],
		location.sourceStart,
		location.sourceEnd);
}
public void methodNeedingAbstractModifier(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.MethodRequiresBody,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void methodNeedingNoBody(MethodDeclaration methodDecl) {
	this.handle(
		((methodDecl.modifiers & CompilerModifiers.AccNative) != 0) ? IProblem.BodyForNativeMethod : IProblem.BodyForAbstractMethod,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void methodWithConstructorName(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.MethodButWithConstructorName,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void missingEnclosingInstanceSpecification(ReferenceBinding enclosingType, AstNode location) {
	boolean insideConstructorCall =
		(location instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper);

	this.handle(
		insideConstructorCall
			? IProblem.MissingEnclosingInstanceForConstructorCall
			: IProblem.MissingEnclosingInstance,
		new String[] {new String(enclosingType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void missingReturnType(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.MissingReturnType,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void mustDefineDimensionsOrInitializer(ArrayAllocationExpression expression) {
	this.handle(
		IProblem.MustDefineEitherDimensionExpressionsOrInitializer,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void mustSpecifyPackage(CompilationUnitDeclaration compUnitDecl) {
	this.handle(
		IProblem.MustSpecifyPackage,
		new String[] {new String(compUnitDecl.getFileName())},
		compUnitDecl.sourceStart,
		compUnitDecl.sourceStart + 1);	
}
public void mustUseAStaticMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		IProblem.StaticMethodRequested,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void nativeMethodsCannotBeStrictfp(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.NativeMethodsCannotBeStrictfp,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void needImplementation() {
	this.abortDueToInternalError(Util.bind("abort.missingCode")); //$NON-NLS-1$
}
public void needToEmulateFieldReadAccess(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.NeedToEmulateFieldReadAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void needToEmulateFieldWriteAccess(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.NeedToEmulateFieldWriteAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void needToEmulateMethodAccess(
	MethodBinding method, 
	AstNode location) {

	if (method.isConstructor())
		this.handle(
			IProblem.NeedToEmulateConstructorAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				parametersAsString(method)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
	else
		this.handle(
			IProblem.NeedToEmulateMethodAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				new String(method.selector), 
				parametersAsString(method)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
}
public void nestedClassCannotDeclareInterface(TypeDeclaration typeDecl) {
	this.handle(
		IProblem.CannotDefineInterfaceInLocalType,
		new String[] {new String(typeDecl.name)},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void noMoreAvailableSpaceForArgument(LocalVariableBinding local, AstNode location) {
	this.handle(
		IProblem.TooManyArgumentSlots,
		new String[]{ new String(local.name) },
		Abort | Error,
		location.sourceStart,
		location.sourceEnd);
}
public void noMoreAvailableSpaceForLocal(LocalVariableBinding local, AstNode location) {
	this.handle(
		IProblem.TooManyLocalVariableSlots,
		new String[]{ new String(local.name) },
		Abort | Error,
		location.sourceStart,
		location.sourceEnd);
}
public void notCompatibleTypesError(EqualExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IProblem.IncompatibleTypesInEqualityOperator,
		new String[] {new String(leftType.readableName()), new String(rightType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void notCompatibleTypesError(InstanceOfExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IProblem.IncompatibleTypesInConditionalOperator,
		new String[] {new String(leftType.readableName()), new String(rightType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void operatorOnlyValidOnNumericType(CompoundAssignment  assignment, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IProblem.TypeMismatch,
		new String[] {new String(leftType.readableName()), new String(rightType.readableName())},
		assignment.sourceStart,
		assignment.sourceEnd);
}
public void overridesDeprecatedMethod(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.OverridingDeprecatedMethod,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void overridesPackageDefaultMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.OverridingNonVisibleMethod,
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.readableName(),
						localMethod.readableName(),
						'.')),
			new String(inheritedMethod.declaringClass.readableName())},
		localMethod.sourceStart(),
		localMethod.sourceEnd());
}
public void packageCollidesWithType(CompilationUnitDeclaration compUnitDecl) {
	this.handle(
		IProblem.PackageCollidesWithType,
		new String[] {CharOperation.toString(compUnitDecl.currentPackage.tokens)},
		compUnitDecl.currentPackage.sourceStart,
		compUnitDecl.currentPackage.sourceEnd);
}
public void packageIsNotExpectedPackage(CompilationUnitDeclaration compUnitDecl) {
	this.handle(
		IProblem.PackageIsNotExpectedPackage,
		new String[] {CharOperation.toString(compUnitDecl.compilationResult.compilationUnit.getPackageName())},
		compUnitDecl.currentPackage == null ? 0 : compUnitDecl.currentPackage.sourceStart,
		compUnitDecl.currentPackage == null ? 0 : compUnitDecl.currentPackage.sourceEnd);
}
private String parametersAsString(MethodBinding method) {
	TypeBinding[] params = method.parameters;
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "); //$NON-NLS-1$
		buffer.append(new String(params[i].readableName()));
	}
	return buffer.toString();
}
public void parseError(
	int startPosition, 
	int endPosition, 
	char[] currentTokenSource, 
	String errorTokenName, 
	String[] possibleTokens) {
	
	if (possibleTokens.length == 0) { //no suggestion available
		if (isKeyword(currentTokenSource)) {
			this.handle(
				IProblem.ParsingErrorOnKeywordNoSuggestion,
				new String[] {new String(currentTokenSource)},
				// this is the current -invalid- token position
				startPosition,
				endPosition);
			return;
		} else {
			this.handle(
				IProblem.ParsingErrorNoSuggestion,
				new String[] {errorTokenName},
				// this is the current -invalid- token position
				startPosition,
				endPosition);
			return;
		}
	}

	//build a list of probable right tokens
	StringBuffer list = new StringBuffer(20);
	for (int i = 0, max = possibleTokens.length; i < max; i++) {
		if (i > 0)
			list.append(", "); //$NON-NLS-1$
		list.append('"');
		list.append(possibleTokens[i]);
		list.append('"');
	}

	if (isKeyword(currentTokenSource)) {
		this.handle(
			IProblem.ParsingErrorOnKeyword,
			new String[] {new String(currentTokenSource), list.toString()},
			// this is the current -invalid- token position
			startPosition,
			endPosition);
		return;
	}
	//extract the literal when it's a literal  
	if ((errorTokenName.equals("IntegerLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("LongLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("FloatingPointLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("DoubleLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("StringLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("CharacterLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("Identifier"))) { //$NON-NLS-1$
			errorTokenName = new String(currentTokenSource);
	}

	this.handle(
		IProblem.ParsingError,
		new String[] {errorTokenName, list.toString()},
		// this is the current -invalid- token position
		startPosition,
		endPosition);
}
public void publicClassMustMatchFileName(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		IProblem.PublicClassMustMatchFileName,
		new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)},
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
/*
 * Flag all constructors involved in a cycle, we know we have a cycle.
 */
public void recursiveConstructorInvocation(TypeDeclaration typeDeclaration) {

	// propagate the reference count, negative counts means leading to a super constructor invocation (directly or indirectly)
	boolean hasChanged;
	AbstractMethodDeclaration[] methods = typeDeclaration.methods;
	int max = methods.length;
	do {
		hasChanged = false;
		for(int i = 0; i < max; i++){
			if (methods[i].isConstructor()){
				ConstructorDeclaration constructor = (ConstructorDeclaration) methods[i];
				if (constructor.referenceCount > 0){
					ConstructorDeclaration targetConstructor = constructor.constructorCall == null
						? null
						: (ConstructorDeclaration)(typeDeclaration.declarationOf(constructor.constructorCall.binding));
					if ((targetConstructor == null) || (targetConstructor.referenceCount < 0)){
						hasChanged = true;
						constructor.referenceCount = -1;
					}	
				}
			}
		}
	} while (hasChanged);

	// all remaining constructors with a positive count are still involved in a cycle
	for(int i = 0; i < max; i++){
		if (methods[i].isConstructor()){
			ConstructorDeclaration constructor = (ConstructorDeclaration) methods[i];
			if (constructor.referenceCount > 0){
				this.referenceContext = constructor;
				this.handle(
					IProblem.RecursiveConstructorInvocation,
					new String[] {
						new String(constructor.constructorCall.binding.declaringClass.readableName()), 
						parametersAsString(constructor.constructorCall.binding)
					},
					constructor.constructorCall.sourceStart,
					constructor.constructorCall.sourceEnd);
			}
		}
	}
}
public void redefineArgument(Argument arg) {
	this.handle(
		IProblem.RedefinedArgument,
		new String[] {new String(arg.name)},
		arg.sourceStart,
		arg.sourceEnd);
}
public void redefineLocal(LocalDeclaration localDecl) {
	this.handle(
		IProblem.RedefinedLocal,
		new String[] {new String(localDecl.name)},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void referenceMustBeArrayTypeAt(TypeBinding arrayType, ArrayReference arrayRef) {
	this.handle(
		IProblem.ArrayReferenceRequired,
		new String[] {new String(arrayType.readableName())},
		arrayRef.sourceStart,
		arrayRef.sourceEnd);
}
public void returnTypeCannotBeVoidArray(SourceTypeBinding type, MethodDeclaration methodDecl) {
	this.handle(
		IProblem.ReturnTypeCannotBeVoidArray,
		new String[] {new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void returnTypeProblem(SourceTypeBinding type, MethodDeclaration methodDecl, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ReturnTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ReturnTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ReturnTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ReturnTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ReturnTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(methodDecl.selector), new String(expectedType.readableName())},
		methodDecl.returnType.sourceStart,
		methodDecl.returnType.sourceEnd);
}
public void scannerError(Parser parser, String errorTokenName) {
	Scanner scanner = parser.scanner;

	int flag = IProblem.ParsingErrorNoSuggestion;
	int startPos = scanner.startPosition;

	//special treatment for recognized errors....
	if (errorTokenName.equals(Scanner.END_OF_SOURCE))
		flag = IProblem.EndOfSource;
	else
		if (errorTokenName.equals(Scanner.INVALID_HEXA))
			flag = IProblem.InvalidHexa;
		else
			if (errorTokenName.equals(Scanner.INVALID_OCTAL))
				flag = IProblem.InvalidOctal;
			else
				if (errorTokenName.equals(Scanner.INVALID_CHARACTER_CONSTANT))
					flag = IProblem.InvalidCharacterConstant;
				else
					if (errorTokenName.equals(Scanner.INVALID_ESCAPE))
						flag = IProblem.InvalidEscape;
					else
						if (errorTokenName.equals(Scanner.INVALID_UNICODE_ESCAPE)){
							flag = IProblem.InvalidUnicodeEscape;
							// better locate the error message
							char[] source = scanner.source;
							int checkPos = scanner.currentPosition - 1;
							if (checkPos >= source.length) checkPos = source.length - 1;
							while (checkPos >= startPos){
								if (source[checkPos] == '\\') break;
								checkPos --;
							}
							startPos = checkPos;
						} else
							if (errorTokenName.equals(Scanner.INVALID_FLOAT))
								flag = IProblem.InvalidFloat;
							else
								if (errorTokenName.equals(Scanner.UNTERMINATED_STRING))
									flag = IProblem.UnterminatedString;
								else
									if (errorTokenName.equals(Scanner.UNTERMINATED_COMMENT))
										flag = IProblem.UnterminatedComment;
									else
										if (errorTokenName.equals(Scanner.INVALID_CHAR_IN_STRING))
											flag = IProblem.UnterminatedString;

	this.handle(
		flag, 
		flag == IProblem.ParsingErrorNoSuggestion 
			? new String[] {errorTokenName}
			: new String[0],
		// this is the current -invalid- token position
		startPos, 
		scanner.currentPosition - 1,
		parser.compilationUnit.compilationResult);
}
public void shouldReturn(TypeBinding returnType, AstNode location) {
	this.handle(
		IProblem.ShouldReturnValue,
		new String[] { new String (returnType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void signalNoImplicitStringConversionForCharArrayExpression(Expression expression) {
	this.handle(
		IProblem.NoImplicitStringConversionForCharArrayExpression,
		new String[] {},
		expression.sourceStart,
		expression.sourceEnd);
}
public void staticAndInstanceConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	if (currentMethod.isStatic())
		this.handle(
			// This static method cannot hide the instance method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature a static (non-abstract) method cannot hide an instance method.
			IProblem.CannotHideAnInstanceMethodWithAStaticMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	else
		this.handle(
			// This instance method cannot override the static method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature an instance (non-abstract) method cannot override a static method.
			IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
}
public void staticFieldAccessToNonStaticVariable(FieldReference fieldRef, FieldBinding field) {
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		new String[] {new String(field.readableName())},
		fieldRef.sourceStart,
		fieldRef.sourceEnd); 
}
public void staticFieldAccessToNonStaticVariable(QualifiedNameReference nameRef, FieldBinding field){
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		new String[] { new String(field.readableName())},
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void staticFieldAccessToNonStaticVariable(SingleNameReference nameRef, FieldBinding field) {
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		new String[] {new String(field.readableName())},
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void staticInheritedMethodConflicts(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	this.handle(
		// The static method %1 conflicts with the abstract method in %2
		// 8.4.6.4 - If a class inherits more than one method with the same signature it is an error for one to be static (non-abstract) and the other abstract.
		IProblem.StaticInheritedMethodConflicts,
		new String[] {
			new String(concreteMethod.readableName()),
			new String(abstractMethods[0].declaringClass.readableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void stringConstantIsExceedingUtf8Limit(AstNode location) {
	this.handle(
		IProblem.StringConstantIsExceedingUtf8Limit,
		new String[0],
		location.sourceStart,
		location.sourceEnd);
}
public void superclassMustBeAClass(SourceTypeBinding type, TypeReference superclassRef, ReferenceBinding superType) {
	this.handle(
		IProblem.SuperclassMustBeAClass,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		superclassRef.sourceStart,
		superclassRef.sourceEnd);
}
public void superinterfaceMustBeAnInterface(SourceTypeBinding type, TypeDeclaration typeDecl, ReferenceBinding superType) {
	this.handle(
		IProblem.SuperInterfaceMustBeAnInterface,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void typeCastError(CastExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IProblem.IllegalCast,
		new String[] {new String(rightType.readableName()), new String(leftType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void typeCollidesWithPackage(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		IProblem.TypeCollidesWithPackage,
		new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)},
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void typeMismatchError(TypeBinding resultType, TypeBinding expectedType, AstNode location) {
	this.handle(
		IProblem.TypeMismatch,
		new String[] {new String(resultType.readableName()), new String(expectedType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void typeMismatchErrorActualTypeExpectedType(Expression expression, TypeBinding constantType, TypeBinding expectedType) {
	this.handle(
		IProblem.TypeMismatch,
		new String[] {new String(constantType.readableName()), new String(expectedType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void undefinedLabel(BranchStatement statement) {
	this.handle(
		IProblem.UndefinedLabel,
		new String[] {new String(statement.label)},
		statement.sourceStart,
		statement.sourceEnd);
}
public void unexpectedStaticModifierForField(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.UnexpectedStaticModifierForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void unexpectedStaticModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.UnexpectedStaticModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void unhandledException(TypeBinding exceptionType, AstNode location) {
	boolean insideDefaultConstructor = 
		(referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)referenceContext).isDefaultConstructor();
	boolean insideImplicitConstructorCall =
		(location instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper);

	this.handle(
		insideDefaultConstructor
			? IProblem.UnhandledExceptionInDefaultConstructor
			: (insideImplicitConstructorCall 
					? IProblem.UndefinedConstructorInImplicitConstructorCall
					: IProblem.UnhandledException),
		new String[] {new String(exceptionType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void uninitializedBlankFinalField(FieldBinding binding, AstNode location) {
	this.handle(
		IProblem.UninitializedBlankFinalField,
		new String[] {new String(binding.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void uninitializedLocalVariable(LocalVariableBinding binding, AstNode location) {
	this.handle(
		IProblem.UninitializedLocalVariable,
		new String[] {new String(binding.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void unmatchedBracket(int position, ReferenceContext context, CompilationResult compilationResult) {

	this.handle(
		IProblem.UnmatchedBracket, 
		new String[] {},
		position, 
		position,
		context,
		compilationResult);
}
public void unnecessaryEnclosingInstanceSpecification(Expression expression, ReferenceBinding targetType) {
	this.handle(
		IProblem.IllegalEnclosingInstanceSpecification,
		new String[]{ new String(targetType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void unreachableCode(Statement statement) {
	this.handle(
		IProblem.CodeCannotBeReached,
		new String[0],
		statement.sourceStart,
		statement.sourceEnd);
}
public void unreachableExceptionHandler(ReferenceBinding exceptionType, AstNode location) {
	this.handle(
		IProblem.UnreachableCatch,
		new String[0],
		location.sourceStart,
		location.sourceEnd);
}
public void unresolvableReference(NameReference nameRef, Binding binding) {
	int severity = Error;
/* also need to check that the searchedType is the receiver type
	if (binding instanceof ProblemBinding) {
		ProblemBinding problem = (ProblemBinding) binding;
		if (problem.searchType != null && problem.searchType.isHierarchyInconsistent())
			severity = SecondaryError;
	}
*/
	this.handle(
		IProblem.UndefinedName,
		new String[] {new String(binding.readableName())},
		severity,
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void unusedArgument(LocalDeclaration localDecl) {
	this.handle(
		IProblem.ArgumentIsNeverUsed,
		new String[] {localDecl.name()},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void unusedImport(ImportReference importRef) {
	this.handle(
		IProblem.UnusedImport,
		new String[] { CharOperation.toString(importRef.tokens) },
		importRef.sourceStart,
		importRef.sourceEnd); 
}
public void unusedLocalVariable(LocalDeclaration localDecl) {
	this.handle(
		IProblem.LocalVariableIsNeverUsed,
		new String[] {localDecl.name()},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}

public void useAssertAsAnIdentifier(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.UseAssertAsAnIdentifier,
		new String[0],
		sourceStart,
		sourceEnd);	
}

public void variableTypeCannotBeVoid(AbstractVariableDeclaration varDecl) {
	this.handle(
		IProblem.VariableTypeCannotBeVoid,
		new String[] {new String(varDecl.name)},
		varDecl.sourceStart,
		varDecl.sourceEnd);
}
public void variableTypeCannotBeVoidArray(AbstractVariableDeclaration varDecl) {
	this.handle(
		IProblem.VariableTypeCannotBeVoidArray,
		new String[] {new String(varDecl.name)},
		varDecl.sourceStart,
		varDecl.sourceEnd);
}
public void visibilityConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		//	Cannot reduce the visibility of the inherited method from %1
		// 8.4.6.3 - The access modifier of an hiding method must provide at least as much access as the hidden method.
		// 8.4.6.3 - The access modifier of an overiding method must provide at least as much access as the overriden method.
		IProblem.MethodReducesVisibility,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void wrongSequenceOfExceptionTypesError(TryStatement statement, int under, int upper) {
	//the two catch block under and upper are in an incorrect order.
	//under should be define BEFORE upper in the source

	//notice that the compiler could arrange automatically the
	//correct order - and the only error would be on cycle ....
	//on this one again , java is compiler-driven instead of being
	//user-driven .....

	TypeReference typeRef = statement.catchArguments[under].type;
	this.handle(
		IProblem.UnreachableCatch,
		new String[0],
		typeRef.sourceStart,
		typeRef.sourceEnd);
}

public void nonExternalizedStringLiteral(AstNode location) {
	this.handle(
		IProblem.NonExternalizedStringLiteral,
		new String[] {},
		location.sourceStart,
		location.sourceEnd);
}

public void noMoreAvailableSpaceInConstantPool(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyConstantsInConstantPool,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		Abort | Error,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}

private boolean isKeyword(char[] tokenSource) {
	/*
	 * This code is heavily grammar dependant
	 */

	if (tokenSource == null) {
		return false;
	}
	try {
		Scanner scanner = new Scanner();
		scanner.setSource(tokenSource);
		int token = scanner.getNextToken();
		char[] currentKeyword;
		try {
			currentKeyword = scanner.getCurrentIdentifierSource();
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
		int nextToken= scanner.getNextToken();
		if (nextToken == ITerminalSymbols.TokenNameEOF
			&& scanner.startPosition == scanner.source.length) { // to handle case where we had an ArrayIndexOutOfBoundsException 
															     // while reading the last token
			switch(token) {
				case Scanner.TokenNameERROR:
					if (CharOperation.equals("goto".toCharArray(), currentKeyword) ||CharOperation.equals("const".toCharArray(), currentKeyword)) { //$NON-NLS-1$ //$NON-NLS-2$
						return true;
					} else {
						return false;
					}
				case Scanner.TokenNameabstract:
				case Scanner.TokenNameassert:
				case Scanner.TokenNamebyte:
				case Scanner.TokenNamebreak:
				case Scanner.TokenNameboolean:
				case Scanner.TokenNamecase:
				case Scanner.TokenNamechar:
				case Scanner.TokenNamecatch:
				case Scanner.TokenNameclass:
				case Scanner.TokenNamecontinue:
				case Scanner.TokenNamedo:
				case Scanner.TokenNamedouble:
				case Scanner.TokenNamedefault:
				case Scanner.TokenNameelse:
				case Scanner.TokenNameextends:
				case Scanner.TokenNamefor:
				case Scanner.TokenNamefinal:
				case Scanner.TokenNamefloat:
				case Scanner.TokenNamefalse:
				case Scanner.TokenNamefinally:
				case Scanner.TokenNameif:
				case Scanner.TokenNameint:
				case Scanner.TokenNameimport:
				case Scanner.TokenNameinterface:
				case Scanner.TokenNameimplements:
				case Scanner.TokenNameinstanceof:
				case Scanner.TokenNamelong:
				case Scanner.TokenNamenew:
				case Scanner.TokenNamenull:
				case Scanner.TokenNamenative:
				case Scanner.TokenNamepublic:
				case Scanner.TokenNamepackage:
				case Scanner.TokenNameprivate:
				case Scanner.TokenNameprotected:
				case Scanner.TokenNamereturn:
				case Scanner.TokenNameshort:
				case Scanner.TokenNamesuper:
				case Scanner.TokenNamestatic:
				case Scanner.TokenNameswitch:
				case Scanner.TokenNamestrictfp:
				case Scanner.TokenNamesynchronized:
				case Scanner.TokenNametry:
				case Scanner.TokenNamethis:
				case Scanner.TokenNametrue:
				case Scanner.TokenNamethrow:
				case Scanner.TokenNamethrows:
				case Scanner.TokenNametransient:
				case Scanner.TokenNamevoid:
				case Scanner.TokenNamevolatile:
				case Scanner.TokenNamewhile:
					return true;
				default: 
					return false;
			}
		} else {
			return false;
		}
	}
	catch (InvalidInputException e) {
		return false;
	}
	
}

/**
 * Signals an error with a string message for those errors that we don't know about
 * 
 * This backdoor weakens NLS guarantees, but it makes life much easier for extensions.
 */
public void signalError(int start, int end, String msg) {
	CompilationResult unitResult = referenceContext.compilationResult();
	IProblem problem = 
		new DefaultProblem(unitResult.getFileName(), msg,
						IProblem.ParsingError,  //??? would like IProblem.Unknown
		                new String[0], ProblemSeverities.Error,
		                start, end,
		                           start >= 0
				? searchLineNumber(unitResult.lineSeparatorPositions, start)
				: 0);
	record(problem, unitResult, referenceContext);
	
	
}

}
