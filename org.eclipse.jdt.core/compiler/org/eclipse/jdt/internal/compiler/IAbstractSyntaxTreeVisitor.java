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
 * A visitor interface for interating through the parse tree.
 */
public interface IAbstractSyntaxTreeVisitor {
	void acceptProblem(IProblem problem);
	void endVisit(AllocationExpression allocationExpression, BlockScope scope);
	void endVisit(AND_AND_Expression and_and_Expression, BlockScope scope);
	void endVisit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration, BlockScope scope);
	void endVisit(Argument argument, BlockScope scope);
	void endVisit(ArrayAllocationExpression arrayAllocationExpression, BlockScope scope);
	void endVisit(ArrayInitializer arrayInitializer, BlockScope scope);
	void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope);
	void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope);
	void endVisit(ArrayReference arrayReference, BlockScope scope);
	void endVisit(ArrayTypeReference arrayTypeReference, BlockScope scope);
	void endVisit(ArrayTypeReference arrayTypeReference, ClassScope scope);
	void endVisit(AssertStatement assertStatement, BlockScope scope);
	void endVisit(Assignment assignment, BlockScope scope);
	void endVisit(BinaryExpression binaryExpression, BlockScope scope);
	void endVisit(Block block, BlockScope scope);
	void endVisit(Break breakStatement, BlockScope scope);
	void endVisit(Case caseStatement, BlockScope scope);
	void endVisit(CastExpression castExpression, BlockScope scope);
	void endVisit(CharLiteral charLiteral, BlockScope scope);
	void endVisit(ClassLiteralAccess classLiteral, BlockScope scope);
	void endVisit(Clinit clinit, ClassScope scope);
	void endVisit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope);
	void endVisit(CompoundAssignment compoundAssignment, BlockScope scope);
	void endVisit(ConditionalExpression conditionalExpression, BlockScope scope);
	void endVisit(ConstructorDeclaration constructorDeclaration, ClassScope scope);
	void endVisit(Continue continueStatement, BlockScope scope);
	void endVisit(DefaultCase defaultCaseStatement, BlockScope scope);
	void endVisit(DoStatement doStatement, BlockScope scope);
	void endVisit(DoubleLiteral doubleLiteral, BlockScope scope);
	void endVisit(EqualExpression equalExpression, BlockScope scope);
	void endVisit(EmptyStatement statement, BlockScope scope);
	void endVisit(ExplicitConstructorCall explicitConstructor, BlockScope scope);
	void endVisit(ExtendedStringLiteral extendedStringLiteral, BlockScope scope);
	void endVisit(FalseLiteral falseLiteral, BlockScope scope);
	void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope);
	void endVisit(FieldReference fieldReference, BlockScope scope);
	void endVisit(FloatLiteral floatLiteral, BlockScope scope);
	void endVisit(ForStatement forStatement, BlockScope scope);
	void endVisit(IfStatement ifStatement, BlockScope scope);
	void endVisit(ImportReference importRef, CompilationUnitScope scope);
	void endVisit(Initializer initializer, MethodScope scope);
	void endVisit(InstanceOfExpression instanceOfExpression, BlockScope scope);
	void endVisit(IntLiteral intLiteral, BlockScope scope);
	void endVisit(LabeledStatement labeledStatement, BlockScope scope);
	void endVisit(LocalDeclaration localDeclaration, BlockScope scope);
	void endVisit(LocalTypeDeclaration localTypeDeclaration, BlockScope scope);
	void endVisit(LongLiteral longLiteral, BlockScope scope);
	void endVisit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope);
	void endVisit(MessageSend messageSend, BlockScope scope);
	void endVisit(MethodDeclaration methodDeclaration, ClassScope scope);
	void endVisit(NullLiteral nullLiteral, BlockScope scope);
	void endVisit(OR_OR_Expression or_or_Expression, BlockScope scope);
	void endVisit(PostfixExpression postfixExpression, BlockScope scope);
	void endVisit(PrefixExpression prefixExpression, BlockScope scope);
	void endVisit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope);
	void endVisit(QualifiedNameReference qualifiedNameReference, BlockScope scope);
	void endVisit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope);
	void endVisit(QualifiedThisReference qualifiedThisReference, BlockScope scope);
	void endVisit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope);
	void endVisit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope);
	void endVisit(ReturnStatement returnStatement, BlockScope scope);
	void endVisit(SingleNameReference singleNameReference, BlockScope scope);
	void endVisit(SingleTypeReference singleTypeReference, BlockScope scope);
	void endVisit(SingleTypeReference singleTypeReference, ClassScope scope);
	void endVisit(StringLiteral stringLiteral, BlockScope scope);
	void endVisit(SuperReference superReference, BlockScope scope);
	void endVisit(SwitchStatement switchStatement, BlockScope scope);
	void endVisit(SynchronizedStatement synchronizedStatement, BlockScope scope);
	void endVisit(ThisReference thisReference, BlockScope scope);
	void endVisit(ThrowStatement throwStatement, BlockScope scope);
	void endVisit(TrueLiteral trueLiteral, BlockScope scope);
	void endVisit(TryStatement tryStatement, BlockScope scope);
	void endVisit(TypeDeclaration typeDeclaration, CompilationUnitScope scope);
	void endVisit(UnaryExpression unaryExpression, BlockScope scope);
	void endVisit(WhileStatement whileStatement, BlockScope scope);
	boolean visit(AllocationExpression allocationExpression, BlockScope scope);
	boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope);
	boolean visit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration, BlockScope scope);
	boolean visit(Argument argument, BlockScope scope);
	boolean visit(ArrayAllocationExpression arrayAllocationExpression, BlockScope scope);
	boolean visit(ArrayInitializer arrayInitializer, BlockScope scope);
	boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope);
	boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope);
	boolean visit(ArrayReference arrayReference, BlockScope scope);
	boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope);
	boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope);
	boolean visit(AssertStatement assertStatement, BlockScope scope);
	boolean visit(Assignment assignment, BlockScope scope);
	boolean visit(BinaryExpression binaryExpression, BlockScope scope);
	boolean visit(Block block, BlockScope scope);
	boolean visit(Break breakStatement, BlockScope scope);
	boolean visit(Case caseStatement, BlockScope scope);
	boolean visit(CastExpression castExpression, BlockScope scope);
	boolean visit(CharLiteral charLiteral, BlockScope scope);
	boolean visit(ClassLiteralAccess classLiteral, BlockScope scope);
	boolean visit(Clinit clinit, ClassScope scope);
	boolean visit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope);
	boolean visit(CompoundAssignment compoundAssignment, BlockScope scope);
	boolean visit(ConditionalExpression conditionalExpression, BlockScope scope);
	boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope);
	boolean visit(Continue continueStatement, BlockScope scope);
	boolean visit(DefaultCase defaultCaseStatement, BlockScope scope);
	boolean visit(DoStatement doStatement, BlockScope scope);
	boolean visit(DoubleLiteral doubleLiteral, BlockScope scope);
	boolean visit(EqualExpression equalExpression, BlockScope scope);
	boolean visit(EmptyStatement statement, BlockScope scope);
	boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope);
	boolean visit(ExtendedStringLiteral extendedStringLiteral, BlockScope scope);
	boolean visit(FalseLiteral falseLiteral, BlockScope scope);
	boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope);
	boolean visit(FieldReference fieldReference, BlockScope scope);
	boolean visit(FloatLiteral floatLiteral, BlockScope scope);
	boolean visit(ForStatement forStatement, BlockScope scope);
	boolean visit(IfStatement ifStatement, BlockScope scope);
	boolean visit(ImportReference importRef, CompilationUnitScope scope);
	boolean visit(Initializer initializer, MethodScope scope);
	boolean visit(InstanceOfExpression instanceOfExpression, BlockScope scope);
	boolean visit(IntLiteral intLiteral, BlockScope scope);
	boolean visit(LabeledStatement labeledStatement, BlockScope scope);
	boolean visit(LocalDeclaration localDeclaration, BlockScope scope);
	boolean visit(LocalTypeDeclaration localTypeDeclaration, BlockScope scope);
	boolean visit(LongLiteral longLiteral, BlockScope scope);
	boolean visit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope);
	boolean visit(MessageSend messageSend, BlockScope scope);
	boolean visit(MethodDeclaration methodDeclaration, ClassScope scope);
	boolean visit(NullLiteral nullLiteral, BlockScope scope);
	boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope);
	boolean visit(PostfixExpression postfixExpression, BlockScope scope);
	boolean visit(PrefixExpression prefixExpression, BlockScope scope);
	boolean visit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope);
	boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope scope);
	boolean visit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope);
	boolean visit(QualifiedThisReference qualifiedThisReference, BlockScope scope);
	boolean visit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope);
	boolean visit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope);
	boolean visit(ReturnStatement returnStatement, BlockScope scope);
	boolean visit(SingleNameReference singleNameReference, BlockScope scope);
	boolean visit(SingleTypeReference singleTypeReference, BlockScope scope);
	boolean visit(SingleTypeReference singleTypeReference, ClassScope scope);
	boolean visit(StringLiteral stringLiteral, BlockScope scope);
	boolean visit(SuperReference superReference, BlockScope scope);
	boolean visit(SwitchStatement switchStatement, BlockScope scope);
	boolean visit(SynchronizedStatement synchronizedStatement, BlockScope scope);
	boolean visit(ThisReference thisReference, BlockScope scope);
	boolean visit(ThrowStatement throwStatement, BlockScope scope);
	boolean visit(TrueLiteral trueLiteral, BlockScope scope);
	boolean visit(TryStatement tryStatement, BlockScope scope);
	boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope);
	boolean visit(UnaryExpression unaryExpression, BlockScope scope);
	boolean visit(WhileStatement whileStatement, BlockScope scope);
}
