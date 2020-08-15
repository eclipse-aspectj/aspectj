/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.tools.ajc;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.org.eclipse.jdt.core.JavaCore;
import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AbstractBooleanTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.AfterAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AfterReturningAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AfterThrowingAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.AjTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AndTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.AnyTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.AnyWithAnnotationTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.AroundAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AspectDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.Assignment;
import org.aspectj.org.eclipse.jdt.core.dom.BeforeAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.BindingTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.Block;
import org.aspectj.org.eclipse.jdt.core.dom.BlockComment;
import org.aspectj.org.eclipse.jdt.core.dom.BodyDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtConstructorDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtFieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtMethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareErrorDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareParentsDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclarePrecedenceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareSoftDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareWarningDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.EnumDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.ExactTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.ExpressionStatement;
import org.aspectj.org.eclipse.jdt.core.dom.FieldAccess;
import org.aspectj.org.eclipse.jdt.core.dom.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.IdentifierTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.InfixExpression;
import org.aspectj.org.eclipse.jdt.core.dom.Initializer;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.MethodInvocation;
import org.aspectj.org.eclipse.jdt.core.dom.NotTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.NumberLiteral;
import org.aspectj.org.eclipse.jdt.core.dom.OrTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.PerCflow;
import org.aspectj.org.eclipse.jdt.core.dom.PerObject;
import org.aspectj.org.eclipse.jdt.core.dom.PerTypeWithin;
import org.aspectj.org.eclipse.jdt.core.dom.PointcutDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.PrimitiveType;
import org.aspectj.org.eclipse.jdt.core.dom.QualifiedName;
import org.aspectj.org.eclipse.jdt.core.dom.ReferencePointcut;
import org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern;
import org.aspectj.org.eclipse.jdt.core.dom.SimpleName;
import org.aspectj.org.eclipse.jdt.core.dom.StringLiteral;
import org.aspectj.org.eclipse.jdt.core.dom.TypeCategoryTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.VariableDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.aspectj.org.eclipse.jdt.core.dom.WildTypePattern;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.TestCase;

public class ASTVisitorTest extends TestCase {
	
	public void testEnum_pr211201() {
		check("public enum BasicEnum {"+
	        "A = 1, B = 2, C = 3;"+
	        "public int getNumberOfEnumerators(){"+
	        "       return 3;"+
	        "}"+
	        "}","(compilationUnit(enum(simpleName)(simpleName)(simpleName)(simpleName)(method(primitiveType)(simpleName)(block(numberLiteral)))))");
		
	}
	
    // from bug 110465 - will currently break because of casts
	public void testAspectWithITD() {
		check("aspect A{ public void B.x(){} }",
			  "(compilationUnit(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
	}
	
	public void testAspectWithCommentThenITD() {
		check("aspect A{ /** */ public void B.x(){} }",
			  "(compilationUnit(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
	}
	
	public void testAspectWithCommentThenPointcut() {
		check("aspect A{ /** */ pointcut x(); }","(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testAPrivilegedAspect() {
		check("privileged aspect AnAspect{}","(compilationUnit(privileged(aspect(simpleName))))");
	}
	// original tests
	public void testAnInterface() {
		check("interface AnInterface{}","(compilationUnit(interface(simpleName)))");
	}
	public void testAnAspect() {
		check("aspect AnAspect{}","(compilationUnit(aspect(simpleName)))");
	}
	public void testPointcutInClass() {
		check("class A {pointcut a();}",
			"(compilationUnit(class(simpleName)(pointcut(simpleName))))");
	}
	public void testPointcutInAspect() {
		check("aspect A {pointcut a();}","(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testAroundAdvice() {
		check("aspect A {pointcut a();void around():a(){}}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(aroundAdvice(primitiveType)(referencePointcut(simpleName))(block))))");
	}
	public void testAroundAdviceWithProceed() {
		// ajh02: currently proceed calls are just normal method calls
		// could add a special AST node for them if anyone would like
		check("aspect A {pointcut a();void around():a(){proceed();}}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(aroundAdvice(primitiveType)(referencePointcut(simpleName))(block(expressionStatement(methodInvocation(simpleName)))))))");
	}
	public void testBeforeAdvice() {
		check("aspect A {pointcut a();before():a(){}}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(beforeAdvice(referencePointcut(simpleName))(block))))");
	}
	public void testAfterAdvice() {
		check("aspect A {pointcut a();after():a(){}}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterAdvice(referencePointcut(simpleName))(block))))");
	}
	public void testAfterThrowingAdviceWithNoArgument() {
		check("aspect A {pointcut a();after()throwing:a(){} }",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterThrowingAdvice(referencePointcut(simpleName))(block))))");
	}
	public void testAfterThrowingAdvice() {
		check("aspect A {pointcut a();after()throwing(Exception e):a(){} }",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterThrowingAdvice(referencePointcut(simpleName))(simpleName)(simpleName)(block))))");
	}
	public void testAfterReturningAdviceWithNoArgument() {
		check("aspect A {pointcut a();after()returning:a(){}}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterReturningAdvice(referencePointcut(simpleName))(block))))");
	}
	public void testAfterReturningAdvice() {
		check("aspect A {pointcut a();after()returning(Object o):a(){}}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterReturningAdvice(referencePointcut(simpleName))(simpleName)(simpleName)(block))))");
	}
	public void testMethodWithStatements() {
		check("class A {void a(){System.out.println(\"a\");}}",
				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
	}
	public void testAdviceWithStatements() {
		check("aspect A {pointcut a();before():a(){System.out.println(\"a\");}}",
		"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(beforeAdvice(referencePointcut(simpleName))(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
	}
	public void testPointcutInAPointcut() {
		check("aspect A {pointcut a();pointcut b();pointcut c(): a() && b();}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(pointcut(simpleName))(pointcut(simpleName)(referencePointcut(simpleName))(referencePointcut(simpleName)))))");
	}
	
	public void testCallPointcut(){
		check("aspect A {pointcut a(): call(* *.*(..));}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testExecutionPointcut(){
		check("aspect A {pointcut a(): execution(* *.*(..));}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testGetPointcut(){
		check("aspect A {pointcut a(): get(* *.*);}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testSetPointcut(){
		check("aspect A {pointcut a(): set(* *.*);}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testHandlerPointcut(){
		check("aspect A {pointcut a(): handler(Exception+);}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testStaticInitializationPointcut(){
		check("aspect A {pointcut a(): staticinitialization(Object+);}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testInitializationPointcut(){
		check("aspect A {pointcut a(): initialization(public Object+.new());}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testPreInitializationPointcut(){
		check("aspect A {pointcut a(): preinitialization(public Object+.new());}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	public void testAdviceExecutionPointcut(){
		check("aspect A {pointcut a(): adviceexecution();}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
	
	public void testPointcutWithoutArguments(){
		check("aspect A {pointcut a(): adviceexecution();}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
	}
		
	public void testPointcutWithOnePrimitiveArgument(){
		check("aspect A {pointcut a(int a): adviceexecution();}",
		"(compilationUnit(aspect(simpleName)(pointcut(simpleName)(primitiveType)(simpleName))))");
	}

	public void testPointcutWithTwoPrimitiveArguments(){
		check("aspect A {pointcut a(int a, double b): adviceexecution();}",
		"(compilationUnit(aspect(simpleName)(pointcut" +
		"(simpleName)(primitiveType)(simpleName)(primitiveType)" +
		"(simpleName))))");
	}

	public void testPointcutWithOneTypedArgument(){
		check("aspect A {pointcut a(A a): adviceexecution();}",
		"(compilationUnit(aspect(simpleName)(pointcut" +
		"(simpleName)(simpleName)" +
		"(simpleName))))");
	}

	public void testPointcutWithTwoTypedArgument(){
		check("aspect A {pointcut a(A a, B b): adviceexecution();}",
		"(compilationUnit(aspect(simpleName)(pointcut" +
		"(simpleName)(simpleName)" +
		"(simpleName)(simpleName)" +
		"(simpleName))))");
	}

	public void testFieldITD(){
		check("class A {}aspect B {int A.a;}",
				"(compilationUnit(class(simpleName))(aspect(simpleName)(fieldITD(primitiveType)(simpleName))))");
	}
	public void testMethodITD(){
		check("class A {}aspect B {void A.a(){}}",
				"(compilationUnit(class(simpleName))(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
	}
	public void testConstructorITD(){
		check("class A {}aspect B {A.new(){}}",
				"(compilationUnit(class(simpleName))(aspect(simpleName)(constructorITD(primitiveType)(simpleName)(block))))");
	}
	
	public void testInitializedField(){
		check("class A{int a = 1;}",
				"(compilationUnit(class(simpleName)(field(primitiveType)(simpleName)(numberLiteral))))");
	}
	public void testMethodITDWithStatements(){
		check("class A {}aspect B {void A.a(){System.out.println(\"a\");}}",
				"(compilationUnit(class(simpleName))(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
	}
	public void testConstructorITDWithStatements(){
		check("class A {}aspect B {A.new(){System.out.println(\"a\");}}",
				"(compilationUnit(class(simpleName))(aspect(simpleName)(constructorITD(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
	}
	public void testInitializedFieldITD(){
		check("class A {}aspect B {int A.a = 1;}",
				"(compilationUnit(class(simpleName))(aspect(simpleName)(fieldITD(primitiveType)(simpleName)(numberLiteral))))");
	}
	
	public void testMethodBeingCalled(){
		check("class A {void a(){}void b(){a();}}",
				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block))(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName)))))))");
	}
	public void testFieldBeingCalled(){
		check("class A {int a;void b(){int c = a;a = c;}}",
				"(compilationUnit(class(simpleName)(field(primitiveType)(simpleName))(method(primitiveType)(simpleName)(block(variableDeclarationStatement(primitiveType)(simpleName)(simpleName))(expressionStatement(assignment(simpleName)(simpleName)))))))");
	}
	public void testConstructorBeingCalled(){
		check("class A {A(){}void b(){A();}}",
				"(compilationUnit(class(simpleName)(constructor(simpleName)(block))(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName)))))))");
	}
	public void testMethodITDBeingCalled(){
		check("class A {void b(){a();}}aspect B {void A.a(){}}",
				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName))))))(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
	}
	public void testFieldITDBeingCalled(){
		check("class A {void b(){int c = a;a = c;}}aspect B {int A.a;}",
				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(variableDeclarationStatement(primitiveType)(simpleName)(simpleName))(expressionStatement(assignment(simpleName)(simpleName))))))(aspect(simpleName)(fieldITD(primitiveType)(simpleName))))");
	}
	public void testConstructorITDBeingCalled(){
		check("class A {void b(){A();}}aspect B {A.new(){}}",
				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName))))))(aspect(simpleName)(constructorITD(primitiveType)(simpleName)(block))))");
	}
	
	/*
	 * 
	 * START: Test TypePattern nodes introduced in Bugzilla 329268.
	 * 
	 */
	
	public void testDeclareParents() {
		check("class A{}class B{}aspect C {declare parents : A extends B;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents(wildTypePattern)(wildTypePattern))))");
	}

	public void testDeclareParentsAnyTypePattern() {
		check("class A{}class B{}aspect C {declare parents : * extends B;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents(anyTypePattern)(wildTypePattern))))");
	}

	public void testDeclareParentsAndTypePattern() {
		check("class A{}class B{}class D{}class E{}aspect C {declare parents : A && B && D extends E;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents((wildTypePattern)andTypePattern((wildTypePattern)andTypePattern(wildTypePattern)))(wildTypePattern))))");
	}

	public void testDeclareParentsOrTypePattern() {
		check("class A{}class B{}class D{}class E{}aspect C {declare parents : A || B || D extends E;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents((wildTypePattern)orTypePattern((wildTypePattern)orTypePattern(wildTypePattern)))(wildTypePattern))))");
	}

	public void testDeclareParentsAndOrTypePattern() {
		check("class A{}class B{}class D{}class E{}aspect C {declare parents : A && (B || D) extends E;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents((wildTypePattern)andTypePattern((wildTypePattern)orTypePattern(wildTypePattern)))(wildTypePattern))))");
	}

	public void testDeclareParentsOrAndTypePattern() {
		check("class A{}class B{}class D{}class E{}aspect C {declare parents : A || B && D extends E;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents((wildTypePattern)orTypePattern((wildTypePattern)andTypePattern(wildTypePattern)))(wildTypePattern))))");
	}

	public void testDeclareParentsNotTypePattern() {
		check("class A{}class B{}class D{}class E{}aspect C {declare parents : A && !B extends E;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents((wildTypePattern)andTypePattern(notTypePattern(wildTypePattern)))(wildTypePattern))))");
	}

	public void testDeclareParentsTypeCategoryTypePattern() {
		check("class A{}class E{}aspect C {declare parents : A && is(ClassType) extends E;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents((wildTypePattern)andTypePattern(typeCategoryTypePattern))(wildTypePattern))))");
	}

	public void testDeclareParentsTypeCategoryTypePatternNot() {
		check("class A{}class E{}aspect C {declare parents : A && !is(InnerType) extends E;}",
				"(compilationUnit(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents((wildTypePattern)andTypePattern(notTypePattern(typeCategoryTypePattern)))(wildTypePattern))))");
	}

	public void testDeclareParentsAnyWithAnnotationTypePattern() {
		check("class E{}aspect C {declare parents : (@AnnotationT *) extends E;}",
				"(compilationUnit(class(simpleName))(aspect(simpleName)(declareParents(anyWithAnnotationTypePattern)(wildTypePattern))))");
	}
	
	
	/*
	 * 
	 * END: Test TypePattern nodes introduced in Bugzilla 329268.
	 * 
	 */
	
	
	public void testDeclareWarning(){
		check("aspect A {pointcut a();declare warning: a(): \"warning\";}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(declareWarning(referencePointcut(simpleName))(stringLiteral))))");
	}
	public void testDeclareError(){
		check("aspect A {pointcut a();declare error: a(): \"error\";}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(declareError(referencePointcut(simpleName))(stringLiteral))))");
	}
	public void testDeclareSoft(){
		check("aspect A {pointcut a();declare soft: Exception+: a();}",
				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(declareSoft(referencePointcut(simpleName))(wildTypePattern))))");
	}
	public void testDeclarePrecedence(){
		check("aspect A{}aspect B{declare precedence: B,A;}",
				"(compilationUnit(aspect(simpleName))(aspect(simpleName)(declarePrecedence(wildTypePattern)(wildTypePattern))))");
	}
	public void testDeclareAnnotationType(){
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @type: C : @MyAnnotation;}",
				"(compilationUnit(simpleName)(class(simpleName))(aspect(simpleName)(declareAtType(wildTypePattern)(simpleName))))");
	}
	public void testDeclareAnnotationMethod(){
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @method:public * C.*(..) : @MyAnnotation;}",
				"(compilationUnit(simpleName)(class(simpleName))(aspect(simpleName)(declareAtMethod(signaturePattern)(simpleName))))");
	}
	public void testDeclareAnnotationField(){
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @field: * C+.* : @MyAnnotation;}",
				"(compilationUnit(simpleName)(class(simpleName))(aspect(simpleName)(declareAtField(signaturePattern)(simpleName))))");
	}
	public void testDeclareAnnotationConstructor(){
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @constructor: C+.new(..) : @MyAnnotation;}",
				"(compilationUnit(simpleName)(class(simpleName))(aspect(simpleName)(declareAtConstructor(signaturePattern)(simpleName))))");
	}
	public void testPerThis(){
		check("aspect A perthis(a()) {pointcut a();}",
				"(compilationUnit(aspect(simpleName)(perObject(referencePointcut(simpleName)))(pointcut(simpleName)))))");
	}
	public void testPerTarget(){
		check("aspect A pertarget(a()) {pointcut a();}",
				"(compilationUnit(aspect(simpleName)(perObject(referencePointcut(simpleName)))(pointcut(simpleName)))))");
	}
	public void testPerCFlow(){
		check("aspect A percflow(a()) {pointcut a();}",
				"(compilationUnit(aspect(simpleName)(perCflow(referencePointcut(simpleName)))(pointcut(simpleName)))))");
	}
	public void testPerCFlowBelow(){
		check("aspect A percflowbelow(a()) {pointcut a();}",
				"(compilationUnit(aspect(simpleName)(perCflow(referencePointcut(simpleName)))(pointcut(simpleName)))))");
	}
	
	private void check(String source, String expectedOutput){
		ASTParser parser = ASTParser.newParser(AST.JLS3);//JLS2); // ajh02: need to use 2 for returnType - in 3 it has "returnType2"
		Map<String, String> options = new HashMap<>();
		options.put(CompilerOptions.OPTION_Source, "1.5");
		parser.setCompilerOptions(options);//JavaCore.getOptions());
		parser.setSource(source.toCharArray());
		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
		TestVisitor visitor = new TestVisitor();
		cu2.accept(visitor);
		String result = visitor.toString();
		System.err.println("actual:\n" + result);
		assertTrue("Expected:\n"+ expectedOutput + "====Actual:\n" + result,
				expectedOutput.equals(result));
	}
	
	private void checkJLS3(String source, String expectedOutput) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		Map<String,String> options = new HashMap<>();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
		TestVisitor visitor = new TestVisitor();
		cu2.accept(visitor);
		String result = visitor.toString();
		System.err.println("actual:\n" + result);
		assertTrue("Expected:\n"+ expectedOutput + "====Actual:\n" + result,
				expectedOutput.equals(result));
		
	}
	
	/** @deprecated using deprecated code */
	@Deprecated
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;
	
	
	/**
	 * @deprecated (not really - just suppressing the warnings
	 * that come from testing Javadoc.getComment())
	 *
	 */
	@Deprecated
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	protected CompilationUnit createAST(char[] source) {
		ASTParser parser= ASTParser.newParser(AST_INTERNAL_JLS2);
		parser.setSource(source);
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}
}

class TestVisitor extends AjASTVisitor {
	
	StringBuffer b = new StringBuffer();
	boolean visitTheKids = true;
	
	boolean visitDocTags;
	
	TestVisitor() {
		this(false);
	}
	
	@Override
	public String toString(){
		return b.toString();
	}
	
	TestVisitor(boolean visitDocTags) {
		super(visitDocTags);
		this.visitDocTags = visitDocTags;
	}
	
	public boolean isVisitingChildren() {
		return visitTheKids;
	}

	public void setVisitingChildren(boolean visitChildren) {
		visitTheKids = visitChildren;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		if (((AjTypeDeclaration)node).isAspect()) {
			if (((AspectDeclaration) node).isPrivileged()){
				b.append("(privileged");
			}
			b.append("(aspect"); //$NON-NLS-1$
//			if (((AspectDeclaration)node).getPerClause() != null){
//				b.append("{" + ((AspectDeclaration)node).getPerClause() + "}");
//			}
		} else if (node.isInterface()){
			b.append("(interface"); // $NON-NLS-1$
		} else {
			b.append("(class"); //$NON-NLS-1$
		}
		return isVisitingChildren();
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		if (((AjTypeDeclaration)node).isAspect()) 
			if (((AspectDeclaration) node).isPrivileged() 
					|| ((AspectDeclaration)node).getPerClause() != null) {
				b.append(")");				
			}
		b.append(")"); //$NON-NLS-1$
	}
	
	@Override
	public boolean visit(EnumDeclaration node) {
		b.append("(enum");
		return isVisitingChildren();
	}

	@Override
	public void endVisit(EnumDeclaration node) {
		b.append(")");
	}
	
	@Override
	public boolean visit(PointcutDeclaration node) {
		b.append("(pointcut"); //$NON-NLS-1$
		return isVisitingChildren();
	}	
	@Override
	public void endVisit(PointcutDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(ReferencePointcut node) {
		b.append("(referencePointcut"); //$NON-NLS-1$
		return isVisitingChildren();
	}	
	@Override
	public void endVisit(ReferencePointcut node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(BeforeAdviceDeclaration node) {
		b.append("(beforeAdvice"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public boolean visit(AroundAdviceDeclaration node) {
		b.append("(aroundAdvice"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public boolean visit(AfterAdviceDeclaration node) {
		b.append("(afterAdvice"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public boolean visit(AfterThrowingAdviceDeclaration node) {
		b.append("(afterThrowingAdvice"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public boolean visit(AfterReturningAdviceDeclaration node) {
		b.append("(afterReturningAdvice"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	
	@Override
	public void endVisit(BeforeAdviceDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(AroundAdviceDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(AfterAdviceDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(AfterThrowingAdviceDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(AfterReturningAdviceDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
		
	@Override
	public boolean visit(MethodDeclaration node) {
		if (node instanceof InterTypeMethodDeclaration) return visit((InterTypeMethodDeclaration)node);
		if (node.isConstructor()){
			b.append("(constructor");
		} else {
			b.append("(method"); //$NON-NLS-1$
		}
		return isVisitingChildren();
	}
	@Override
	public void endVisit(MethodDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(InterTypeFieldDeclaration node) {
		b.append("(fieldITD"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(InterTypeFieldDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(InterTypeMethodDeclaration node) {
		if (node.isConstructor()){
			b.append("(constructorITD");
		} else {
			b.append("(methodITD"); //$NON-NLS-1$
		}
		return isVisitingChildren();
	}
	@Override
	public void endVisit(InterTypeMethodDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(MethodInvocation node) {
		b.append("(methodInvocation"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(MethodInvocation node) {
		b.append(")"); //$NON-NLS-1$
	}
	public boolean visit(BodyDeclaration node) {
		b.append("(methodInvocation"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	public void endVisit(BodyDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(FieldDeclaration node) {
		b.append("(field"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(FieldDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(FieldAccess node) {
		b.append("(fieldAccess"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(FieldAccess node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(Assignment node) {
		b.append("(assignment"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(Assignment node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(Block node) {
		b.append("(block"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(Block node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(CompilationUnit node) {
		b.append("(compilationUnit"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(CompilationUnit node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(ExpressionStatement node) {
		b.append("(expressionStatement"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(ExpressionStatement node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(InfixExpression node) {
		b.append("(infixExpression"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(InfixExpression node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(Initializer node) {
		b.append("(initializer"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(Initializer node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(NumberLiteral node) {
		b.append("(numberLiteral"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(NumberLiteral node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(PrimitiveType node) {
		b.append("(primitiveType"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(PrimitiveType node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(QualifiedName node) {
		b.append("(qualifiedName"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(QualifiedName node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(SimpleName node) {
		b.append("(simpleName"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(SimpleName node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(StringLiteral node) {
		b.append("(stringLiteral"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(StringLiteral node) {
		b.append(")"); //$NON-NLS-1$
	}
	public boolean visit(VariableDeclaration node) {
		b.append("(variableDeclaration"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public boolean visit(BlockComment bc) {
		b.append("(blockcomment");
		return isVisitingChildren();
	}
	@Override
	public void endVisit(BlockComment bc) {
		b.append(")");
	}
	public void endVisit(VariableDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		b.append("(variableDeclarationStatement"); //$NON-NLS-1$
		return isVisitingChildren();
	}
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		b.append(")"); //$NON-NLS-1$
	}

	@Override
	public boolean visit(DeclareAtTypeDeclaration node) {
		b.append("(declareAtType");
		return isVisitingChildren();
	}
	@Override
	public boolean visit(DeclareAtMethodDeclaration node) {
		b.append("(declareAtMethod");
		return isVisitingChildren();
	}
	@Override
	public boolean visit(DeclareAtConstructorDeclaration node) {
		b.append("(declareAtConstructor");
		return isVisitingChildren();
	}
	@Override
	public boolean visit(DeclareAtFieldDeclaration node) {
		b.append("(declareAtField");
		return isVisitingChildren();
	}
	
	@Override
	public boolean visit(DeclareErrorDeclaration node) {
		b.append("(declareError");
		return isVisitingChildren();
	}
	
	@Override
	public boolean visit(DeclareParentsDeclaration node) {
		b.append("(declareParents");
		return isVisitingChildren();
	}
	
	@Override
	public boolean visit(DeclarePrecedenceDeclaration node) {
		b.append("(declarePrecedence");
		return isVisitingChildren();
	}
	
	@Override
	public boolean visit(DeclareSoftDeclaration node) {
		b.append("(declareSoft");
		return isVisitingChildren();
	}
	
	@Override
	public boolean visit(DeclareWarningDeclaration node) {
		b.append("(declareWarning");
		return isVisitingChildren();
	}
	@Override
	public void endVisit(DeclareErrorDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclareParentsDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclarePrecedenceDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclareAtFieldDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclareAtMethodDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclareAtTypeDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclareAtConstructorDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclareSoftDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(DeclareWarningDeclaration node) {
		b.append(")"); //$NON-NLS-1$
	}
	
	@Override
	public boolean visit(AbstractBooleanTypePattern node) {
		b.append("(");
		node.getLeft().accept(this);
		if (node instanceof AndTypePattern) {
			b.append("andTypePattern");
		} else if (node instanceof OrTypePattern) {
			b.append("orTypePattern");
		}
		node.getRight().accept(this);
		b.append(")");
		
		// Don't visit the children, as that is done above in order (left node first, boolean operator next, right node last
		return false;
	}


	@Override
	public boolean visit(AnyTypePattern node) {
		b.append("(anyTypePattern");
		return isVisitingChildren();
	}


	@Override
	public void endVisit(AnyTypePattern node) {
		b.append(")"); 
	}


	@Override
	public boolean visit(AnyWithAnnotationTypePattern node) {
		b.append("(anyWithAnnotationTypePattern");
		return isVisitingChildren();
	}


	@Override
	public void endVisit(AnyWithAnnotationTypePattern node) {
		b.append(")"); 
	}

	@Override
	public boolean visit(IdentifierTypePattern node) {
		if (node instanceof WildTypePattern) {
			b.append("(wildTypePattern");
		} else if (node instanceof ExactTypePattern) {
			b.append("(exactTypePattern");
		} else if (node instanceof BindingTypePattern) {
			b.append("(bindingTypePattern");
		}
		return isVisitingChildren();
	}


	@Override
	public void endVisit(IdentifierTypePattern node) {
		b.append(")"); 
	}


	@Override
	public boolean visit(NotTypePattern node) {
		b.append("(notTypePattern");
		return isVisitingChildren();
	}


	@Override
	public void endVisit(NotTypePattern node) {
		b.append(")"); 
	}


	@Override
	public boolean visit(TypeCategoryTypePattern node) {
		b.append("(typeCategoryTypePattern");
		return isVisitingChildren();
	}


	@Override
	public void endVisit(TypeCategoryTypePattern node) {
		b.append(")"); 
	}
	
	
	// End of TypePattern additions for Bugzilla 329268

	@Override
	public boolean visit(SignaturePattern node) {
		b.append("(signaturePattern");
		return isVisitingChildren();		
	}
	@Override
	public void endVisit(SignaturePattern node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public boolean visit(PerObject node) {
		b.append("(perObject");
		return isVisitingChildren();		
	}
	@Override
	public boolean visit(PerCflow node) {
		b.append("(perCflow");
		return isVisitingChildren();		
	}
	@Override
	public boolean visit(PerTypeWithin node) {
		b.append("(perTypeWithin");
		return isVisitingChildren();		
	}
	@Override
	public void endVisit(PerObject node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(PerCflow node) {
		b.append(")"); //$NON-NLS-1$
	}
	@Override
	public void endVisit(PerTypeWithin node) {
		b.append(")"); //$NON-NLS-1$
	}
}
