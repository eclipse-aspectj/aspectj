/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * An adapter class for interating through the parse tree.
 */

public class AbstractSyntaxTreeVisitorAdapter implements IAbstractSyntaxTreeVisitor {

	public void acceptProblem(IProblem problem) {}
	public void endVisit(
		AllocationExpression allocationExpression,
		BlockScope scope) {
	}
	public void endVisit(AND_AND_Expression and_and_Expression, BlockScope scope) {
	}
	public void endVisit(
		AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
		BlockScope scope) {
	}
	public void endVisit(Argument argument, BlockScope scope) {
	}
	public void endVisit(
		ArrayAllocationExpression arrayAllocationExpression,
		BlockScope scope) {
	}
	public void endVisit(ArrayInitializer arrayInitializer, BlockScope scope) {
	}
	public void endVisit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		BlockScope scope) {
	}
	public void endVisit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		ClassScope scope) {
	}
	public void endVisit(ArrayReference arrayReference, BlockScope scope) {
	}
	public void endVisit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
	}
	public void endVisit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
	}
	public void endVisit(Assignment assignment, BlockScope scope) {
	}
	public void endVisit(AssertStatement assertStatement, BlockScope scope) {
	}
	public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
	}
	public void endVisit(Block block, BlockScope scope) {
	}
	public void endVisit(Break breakStatement, BlockScope scope) {
	}
	public void endVisit(Case caseStatement, BlockScope scope) {
	}
	public void endVisit(CastExpression castExpression, BlockScope scope) {
	}
	public void endVisit(CharLiteral charLiteral, BlockScope scope) {
	}
	public void endVisit(ClassLiteralAccess classLiteral, BlockScope scope) {
	}
	public void endVisit(Clinit clinit, ClassScope scope) {
	}
	public void endVisit(
		CompilationUnitDeclaration compilationUnitDeclaration,
		CompilationUnitScope scope) {
	}
	public void endVisit(CompoundAssignment compoundAssignment, BlockScope scope) {
	}
	public void endVisit(
		ConditionalExpression conditionalExpression,
		BlockScope scope) {
	}
	public void endVisit(
		ConstructorDeclaration constructorDeclaration,
		ClassScope scope) {
	}
	public void endVisit(Continue continueStatement, BlockScope scope) {
	}
	public void endVisit(DefaultCase defaultCaseStatement, BlockScope scope) {
	}
	public void endVisit(DoStatement doStatement, BlockScope scope) {
	}
	public void endVisit(DoubleLiteral doubleLiteral, BlockScope scope) {
	}
	public void endVisit(EqualExpression equalExpression, BlockScope scope) {
	}
	public void endVisit(
		ExplicitConstructorCall explicitConstructor,
		BlockScope scope) {
	}
	public void endVisit(
		ExtendedStringLiteral extendedStringLiteral,
		BlockScope scope) {
	}
	public void endVisit(FalseLiteral falseLiteral, BlockScope scope) {
	}
	public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
	}
	public void endVisit(FieldReference fieldReference, BlockScope scope) {
	}
	public void endVisit(FloatLiteral floatLiteral, BlockScope scope) {
	}
	public void endVisit(EmptyStatement emptyStatement, BlockScope scope) {
	}
	public void endVisit(ForStatement forStatement, BlockScope scope) {
	}
	public void endVisit(IfStatement ifStatement, BlockScope scope) {
	}
	public void endVisit(ImportReference importRef, CompilationUnitScope scope) {
	}
	public void endVisit(Initializer initializer, MethodScope scope) {
	}
	public void endVisit(
		InstanceOfExpression instanceOfExpression,
		BlockScope scope) {
	}
	public void endVisit(IntLiteral intLiteral, BlockScope scope) {
	}
	public void endVisit(LabeledStatement labeledStatement, BlockScope scope) {
	}
	public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
	}
	public void endVisit(LongLiteral longLiteral, BlockScope scope) {
	}
	public void endVisit(
		MemberTypeDeclaration memberTypeDeclaration,
		ClassScope scope) {
	}
	public void endVisit(MessageSend messageSend, BlockScope scope) {
	}
	public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
	}
	public void endVisit(NullLiteral nullLiteral, BlockScope scope) {
	}
	public void endVisit(OR_OR_Expression or_or_Expression, BlockScope scope) {
	}
	public void endVisit(PostfixExpression postfixExpression, BlockScope scope) {
	}
	public void endVisit(PrefixExpression prefixExpression, BlockScope scope) {
	}
	public void endVisit(
		QualifiedAllocationExpression qualifiedAllocationExpression,
		BlockScope scope) {
	}
	public void endVisit(
		QualifiedNameReference qualifiedNameReference,
		BlockScope scope) {
	}
	public void endVisit(
		QualifiedSuperReference qualifiedSuperReference,
		BlockScope scope) {
	}
	public void endVisit(
		QualifiedThisReference qualifiedThisReference,
		BlockScope scope) {
	}
	public void endVisit(
		QualifiedTypeReference qualifiedTypeReference,
		BlockScope scope) {
	}
	public void endVisit(
		QualifiedTypeReference qualifiedTypeReference,
		ClassScope scope) {
	}
	public void endVisit(ReturnStatement returnStatement, BlockScope scope) {
	}
	public void endVisit(
		SingleNameReference singleNameReference,
		BlockScope scope) {
	}
	public void endVisit(
		SingleTypeReference singleTypeReference,
		BlockScope scope) {
	}
	public void endVisit(
		SingleTypeReference singleTypeReference,
		ClassScope scope) {
	}
	public void endVisit(StringLiteral stringLiteral, BlockScope scope) {
	}
	public void endVisit(SuperReference superReference, BlockScope scope) {
	}
	public void endVisit(SwitchStatement switchStatement, BlockScope scope) {
	}
	public void endVisit(
		SynchronizedStatement synchronizedStatement,
		BlockScope scope) {
	}
	public void endVisit(ThisReference thisReference, BlockScope scope) {
	}
	public void endVisit(ThrowStatement throwStatement, BlockScope scope) {
	}
	public void endVisit(TrueLiteral trueLiteral, BlockScope scope) {
	}
	public void endVisit(TryStatement tryStatement, BlockScope scope) {
	}
	public void endVisit(
		TypeDeclaration typeDeclaration,
		CompilationUnitScope scope) {
	}
	public void endVisit(UnaryExpression unaryExpression, BlockScope scope) {
	}
	public void endVisit(WhileStatement whileStatement, BlockScope scope) {
	}
	public boolean visit(
		AllocationExpression allocationExpression,
		BlockScope scope) {
		return true;
	}
	public boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		return true;
	}
	public boolean visit(
		AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
		BlockScope scope) {
		return true;
	}
	public boolean visit(Argument argument, BlockScope scope) {
		return true;
	}
	public boolean visit(
		ArrayAllocationExpression arrayAllocationExpression,
		BlockScope scope) {
		return true;
	}
	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
		return true;
	}
	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		ClassScope scope) {
		return true;
	}
	public boolean visit(ArrayReference arrayReference, BlockScope scope) {
		return true;
	}
	public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		return true;
	}
	public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		return true;
	}
	public boolean visit(Assignment assignment, BlockScope scope) {
		return true;
	}
	public boolean visit(AssertStatement assertStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
		return true;
	}
	public boolean visit(Block block, BlockScope scope) {
		return true;
	}
	public boolean visit(Break breakStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(Case caseStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(CastExpression castExpression, BlockScope scope) {
		return true;
	}
	public boolean visit(CharLiteral charLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(ClassLiteralAccess classLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(Clinit clinit, ClassScope scope) {
		return true;
	}
	public boolean visit(
		CompilationUnitDeclaration compilationUnitDeclaration,
		CompilationUnitScope scope) {
		return true;
	}
	public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
		return true;
	}
	public boolean visit(
		ConditionalExpression conditionalExpression,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		ConstructorDeclaration constructorDeclaration,
		ClassScope scope) {
		return true;
	}
	public boolean visit(Continue continueStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(DefaultCase defaultCaseStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(DoStatement doStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(DoubleLiteral doubleLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(EqualExpression equalExpression, BlockScope scope) {
		return true;
	}
	public boolean visit(EmptyStatement emptyStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(
		ExplicitConstructorCall explicitConstructor,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		ExtendedStringLiteral extendedStringLiteral,
		BlockScope scope) {
		return true;
	}
	public boolean visit(FalseLiteral falseLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		return true;
	}
	public boolean visit(FieldReference fieldReference, BlockScope scope) {
		return true;
	}
	public boolean visit(FloatLiteral floatLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(ForStatement forStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(IfStatement ifStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
		return true;
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		return true;
	}
	public boolean visit(
		InstanceOfExpression instanceOfExpression,
		BlockScope scope) {
		return true;
	}
	public boolean visit(IntLiteral intLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(LabeledStatement labeledStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
		return true;
	}
	public boolean visit(LongLiteral longLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(
		MemberTypeDeclaration memberTypeDeclaration,
		ClassScope scope) {
		return true;
	}
	public boolean visit(MessageSend messageSend, BlockScope scope) {
		return true;
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		return true;
	}
	public boolean visit(NullLiteral nullLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		return true;
	}
	public boolean visit(PostfixExpression postfixExpression, BlockScope scope) {
		return true;
	}
	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
		return true;
	}
	public boolean visit(
		QualifiedAllocationExpression qualifiedAllocationExpression,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		QualifiedNameReference qualifiedNameReference,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		QualifiedSuperReference qualifiedSuperReference,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		QualifiedThisReference qualifiedThisReference,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		QualifiedTypeReference qualifiedTypeReference,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		QualifiedTypeReference qualifiedTypeReference,
		ClassScope scope) {
		return true;
	}
	public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(
		SingleNameReference singleNameReference,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		SingleTypeReference singleTypeReference,
		BlockScope scope) {
		return true;
	}
	public boolean visit(
		SingleTypeReference singleTypeReference,
		ClassScope scope) {
		return true;
	}
	public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(SuperReference superReference, BlockScope scope) {
		return true;
	}
	public boolean visit(SwitchStatement switchStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(
		SynchronizedStatement synchronizedStatement,
		BlockScope scope) {
		return true;
	}
	public boolean visit(ThisReference thisReference, BlockScope scope) {
		return true;
	}
	public boolean visit(ThrowStatement throwStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(TrueLiteral trueLiteral, BlockScope scope) {
		return true;
	}
	public boolean visit(TryStatement tryStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(
		TypeDeclaration typeDeclaration,
		CompilationUnitScope scope) {
		return true;
	}
	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
		return true;
	}
	public boolean visit(WhileStatement whileStatement, BlockScope scope) {
		return true;
	}
	public boolean visit(
		LocalTypeDeclaration localTypeDeclaration,
		BlockScope scope) {
		return true;
	}
	public void endVisit(
		LocalTypeDeclaration localTypeDeclaration,
		BlockScope scope) {
	}
}
