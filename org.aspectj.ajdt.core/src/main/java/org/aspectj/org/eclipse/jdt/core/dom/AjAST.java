/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

public class AjAST extends AST {

	/**
	 * Creates a new AspectJ abstract syntax tree
     * (AST) following the specified set of API rules.
     *
 	 * @param level the API level; one of the LEVEL constants
     * @since 3.0
	 */
	private AjAST(int level,boolean previewEnabled) {
		super(level,previewEnabled);
	}

	/**
	 * Creates a new AspectJ abstract syntax tree
     * (AST) following the specified set of API rules.
     * <p>
     * Clients should use this method specifing {@link #JLS3} as the
     * AST level in all cases, even when dealing with JDK 1.3 or 1.4..
     * </p>
     *
 	 * @param level the API level; one of the LEVEL constants
	 * @return new AST instance following the specified set of API rules.
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the API level is not one of the LEVEL constants</li>
	 * </ul>
     * @since 3.0
	 */
	public static AjAST newAjAST(int level,boolean previewEnabled) {
		if ((level != AST.JLS2)
			&& (level != AST.JLS3)) {
			throw new IllegalArgumentException();
		}
		return new AjAST(level,previewEnabled);
	}

	/**
	 * Internal method.
	 * <p>
	 * This method converts the given internal compiler AST for the given source string
	 * into a compilation unit. This method is not intended to be called by clients.
	 * </p>
	 *
 	 * @param level the API level; one of the LEVEL constants
	 * @param compilationUnitDeclaration an internal AST node for a compilation unit declaration
	 * @param source the string of the Java compilation unit
	 * @param options compiler options
	 * @param workingCopy the working copy that the AST is created from
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *     or <code>null</code> if none
	 * @param isResolved whether the given compilation unit declaration is resolved
	 * @return the compilation unit node
	 */
	public static CompilationUnit convertCompilationUnit(
		int level,
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration compilationUnitDeclaration,
		char[] source,
		Map options,
		boolean isResolved,
		org.aspectj.org.eclipse.jdt.internal.core.CompilationUnit workingCopy,
		IProgressMonitor monitor) {

		ASTConverter converter =
			// AspectJ extension - use the factory
			ASTConverter.getASTConverter(options,isResolved,monitor);
		// create a new AjAst - difference between this method in AjAST and AST
		AjAST ast = AjAST.newAjAST(level,false);
		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
		BindingResolver resolver = null;
		if (isResolved) {
			resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope, workingCopy.owner, new DefaultBindingResolver.BindingTables(), false,true);
			ast.setFlag(AST.RESOLVED_BINDINGS);
		} else {
			resolver = new BindingResolver();
		}
		ast.setBindingResolver(resolver);
		converter.setAST(ast);

		CompilationUnit unit = converter.convert(compilationUnitDeclaration, source);
		unit.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
		unit.setTypeRoot(workingCopy);
		ast.setDefaultNodeFlag(savedDefaultNodeFlag);
		return unit;
	}

	/**
	 * Creates an unparented aspect declaration node owned by this AST.
	 * The name of the aspect is an unspecified, but legal, name;
	 * no modifiers; no doc comment; no superclass or superinterfaces;
	 * an empty body; a null perclause; and is not privileged
	 * <p>
	 * To set the perclause, use this method and then call
	 * <code>AspectDeclaration.setPerClause(ASTNode)</code>.
	 * </p>
	 * <p>
	 * To create a privileged aspect, use this method and then call
	 * <code>AspectDeclaration.setPrivileged(true)</code>.
	 * </p>
	 *
	 * @return a new unparented aspect declaration node
	 */
	public AspectDeclaration newAspectDeclaration() {
		AspectDeclaration result = new AspectDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented ajtype declaration node owned by this AST.
	 * The name of the class is an unspecified, but legal, name;
	 * no modifiers; no doc comment; no superclass or superinterfaces;
	 * and an empty body.
	 * <p>
	 * To create an aspect, use this method and then call
	 * <code>AjTypeDeclaration.setAspect(true)</code>.
	 * </p>
	 *
	 * @return a new unparented ajtype declaration node
	 */
	public AjTypeDeclaration newAjTypeDeclaration() {
		AjTypeDeclaration result = new AjTypeDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented after advice declaration node owned by this AST.
	 * By default, the declaration is for an after advice with no pointcut;
	 * no doc comment; and no body (as opposed to an empty body).
	 *
	 * @return a new unparented after advice declaration node
	 */
	public AfterAdviceDeclaration newAfterAdviceDeclaration() {
		AfterAdviceDeclaration result = new AfterAdviceDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented after returning advice declaration node owned
	 * by this AST. By default, the declaration is for an after returning
	 * advice with no pointcut; no doc comment; no return value and no
	 * body (as opposed to an empty body).
	 *
	 * @return a new unparented after returning advice declaration node
	 */
	public AfterReturningAdviceDeclaration newAfterReturningAdviceDeclaration() {
		AfterReturningAdviceDeclaration result = new AfterReturningAdviceDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented after throwing advice declaration node owned
	 * by this AST. By default, the declaration is for an after throwing
	 * advice with no pointcut; no doc comment; no throwing value and no
	 * body (as opposed to an empty body).
	 *
	 * @return a new unparented after throwing advice declaration node
	 */
	public AfterThrowingAdviceDeclaration newAfterThrowingAdviceDeclaration() {
		AfterThrowingAdviceDeclaration result = new AfterThrowingAdviceDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented before advice declaration node owned by this AST.
	 * By default, the declaration is for a before advice with no pointcut;
	 * no doc comment; and no body (as opposed to an empty body).
	 *
	 * @return a new unparented before advice declaration node
	 */
	public BeforeAdviceDeclaration newBeforeAdviceDeclaration() {
		BeforeAdviceDeclaration result = new BeforeAdviceDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented around advice declaration node owned by this AST.
	 * By default, the declaration is for an around advice with no pointcut;
	 * no doc comment; no return type; and no body (as opposed to an empty body).
	 *
	 * @return a new unparented around advice declaration node
	 */
	public AroundAdviceDeclaration newAroundAdviceDeclaration() {
		AroundAdviceDeclaration result = new AroundAdviceDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare at constructor declaration node owned by this AST.
	 * By default, the declaration is for a declare annotation with no doc comment;
	 * no pattern node; no annotation name; and no declare kind.
	 *
	 * @return a new unparented declare at constructor declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 */
	public DeclareAtConstructorDeclaration newDeclareAtConstructorDeclaration() {
		DeclareAtConstructorDeclaration result = new DeclareAtConstructorDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare at field declaration node owned by this AST.
	 * By default, the declaration is for a declare annotation with no doc comment;
	 * no pattern node; no annotation name; and no declare kind.
	 *
	 * @return a new unparented declare at field declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 */
	public DeclareAtFieldDeclaration newDeclareAtFieldDeclaration() {
		DeclareAtFieldDeclaration result = new DeclareAtFieldDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare at method declaration node owned by this AST.
	 * By default, the declaration is for a declare annotation with no doc comment;
	 * no pattern node; no annotation name; and no declare kind.
	 *
	 * @return a new unparented declare at method declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 */
	public DeclareAtMethodDeclaration newDeclareAtMethodDeclaration() {
		DeclareAtMethodDeclaration result = new DeclareAtMethodDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare at type declaration node owned by this AST.
	 * By default, the declaration is for a declare annotation with no doc comment;
	 * no pattern node; no annotation name; and no declare kind.
	 *
	 * @return a new unparented declare at type declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 */
	public DeclareAtTypeDeclaration newDeclareAtTypeDeclaration() {
		DeclareAtTypeDeclaration result = new DeclareAtTypeDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare error declaration node owned by this AST.
	 * By default, the declaration is for a declare error with no doc comment;
	 * no pointcut; and no message.
	 *
	 * @return a new unparented declare error declaration node
	 */
	public DeclareErrorDeclaration newDeclareErrorDeclaration() {
		DeclareErrorDeclaration result = new DeclareErrorDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare parents declaration node owned by this AST.
	 * By default, the declaration is for a declare parents which is implements;
	 * with no doc comment; no child type pattern; and no parent type pattern
	 * <p>
	 * To create an extends declare parents, use this method and then call
	 * <code>DeclareParentsDeclaration.setExtends(true)</code>.
	 * </p>
	 *
	 * @return a new unparented declare parents declaration node
	 */
	public DeclareParentsDeclaration newDeclareParentsDeclaration() {
		DeclareParentsDeclaration result = new DeclareParentsDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare precedence declaration node owned by this AST.
	 * By default, the declaration is for a declare precedence with no doc comment;
	 * and no type pattern list.
	 *
	 * @return a new unparented declare precedence declaration node
	 */
	public DeclarePrecedenceDeclaration newDeclarePrecedenceDeclaration() {
		DeclarePrecedenceDeclaration result = new DeclarePrecedenceDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare soft declaration node owned by this AST.
	 * By default, the declaration is for a declare soft with no doc comment;
	 * no pointcut; and no type pattern.
	 *
	 * @return a new unparented declare soft declaration node
	 */
	public DeclareSoftDeclaration newDeclareSoftDeclaration() {
		DeclareSoftDeclaration result = new DeclareSoftDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented declare warning declaration node owned by this AST.
	 * By default, the declaration is for a declare warning with no doc comment;
	 * no pointcut; and no message.
	 *
	 * @return a new unparented declare warning declaration node
	 */
	public DeclareWarningDeclaration newDeclareWarningDeclaration() {
		DeclareWarningDeclaration result = new DeclareWarningDeclaration(this);
		return result;
	}

	/**
	 * Creates a new unparented intertype field declaration node owned by this
	 * AST. By default, there are no modifiers, no doc comment, and the base
	 * type is unspecified (but legal).
	 *
	 * @return a new unparented intertype field declaration node
	 */
	public InterTypeFieldDeclaration newInterTypeFieldDeclaration() {
		InterTypeFieldDeclaration result = new InterTypeFieldDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented intertype method declaration node owned by
	 * this AST. By default, the declaration is for a method of an
	 * unspecified, but legal, name; no modifiers; no doc comment; no
	 * parameters; return type void; no extra array dimensions; no
	 * thrown exceptions; and no body (as opposed to an empty body).
	 *
	 * @return a new unparented inter type method declaration node
	 */
	public InterTypeMethodDeclaration newInterTypeMethodDeclaration() {
		InterTypeMethodDeclaration result = new InterTypeMethodDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented pointcut declaration node owned by this AST.
	 * By default, the declaration is for a pointcut of an unspecified, but
	 * legal, name; no modifiers; no doc comment; and no pointcut designator
	 *
	 * @return a new unparented pointcut declaration node
	 */
	public PointcutDeclaration newPointcutDeclaration() {
		PointcutDeclaration result = new PointcutDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented AndPointcut node owned by this AST.
	 * By default, the declaration is for an and pointcut with no left
	 * or right pointcut designators
	 *
	 * @return a new unparented AndPointcut node
	 */
	public AndPointcut newAndPointcut() {
		AndPointcut result = new AndPointcut(this);
		return result;
	}

	/**
	 * Creates an unparented CflowPointcut node owned by this AST.
	 * By default, the declaration is for a cflow pointcut with no body
	 * pointcut designator
	 *
	 * @return a new unparented CflowPointcut node
	 */
	public CflowPointcut newCflowPointcut() {
		CflowPointcut result = new CflowPointcut(this);
		return result;
	}

	/**
	 * Creates an unparented NotPointcut node owned by this AST.
	 * By default, the declaration is for a not pointcut with no body
	 * pointcut designator
	 *
	 * @return a new unparented NotPointcut node
	 */
	public NotPointcut newNotPointcut() {
		NotPointcut result = new NotPointcut(this);
		return result;
	}

	/**
	 * Creates an unparented OrPointcut node owned by this AST.
	 * By default, the declaration is for an or pointcut with no left
	 * or right pointcut designators
	 *
	 * @return a new unparented OrPointcut node
	 */
	public OrPointcut newOrPointcut() {
		OrPointcut result = new OrPointcut(this);
		return result;
	}

	/**
	 * Creates an unparented PerCflow node owned by this AST.
	 * By default, the declaration is for a percflow with no body
	 * pointcut designator
	 *
	 * @return a new unparented percflow node
	 */
	public PerCflow newPerCflow() {
		PerCflow result = new PerCflow(this);
		return result;
	}

	/**
	 * Creates an unparented perobject node owned by this AST.
	 * By default, the declaration is for a perobject with no body
	 * pointcut designator
	 *
	 * @return a new unparented perobject node
	 */
	public PerObject newPerObject() {
		PerObject result = new PerObject(this);
		return result;
	}

	/**
	 * Creates an unparented pertypewithin node owned by this AST.
	 * By default, the declaration is for a pertypewithin
	 *
	 * @return a new unparented pertypewithin node
	 */
	public PerTypeWithin newPerTypeWithin() {
		PerTypeWithin result = new PerTypeWithin(this);
		return result;
	}

	/**
	 * Creates an unparented reference pointcut node owned by this AST.
	 * By default, the declaration is for a reference pointcut with no
	 * name
	 *
	 * @return a new unparented reference pointcut node
	 */
	public ReferencePointcut newReferencePointcut() {
		ReferencePointcut result = new ReferencePointcut(this);
		return result;
	}

	/**
	 * Creates an unparented default pointcut node owned by this AST.
	 * By default, the declaration is for a default pointcut with an
	 * empty detail string.
	 * <p>
	 * To edit the detail string, use this method and then call
	 * <code>DefaultPointcut.setDetail("newString")</code>.
	 * </p>
	 *
	 * @return a new unparented default pointcut node
	 */
	public DefaultPointcut newDefaultPointcut() {
		DefaultPointcut result = new DefaultPointcut(this,"");
		return result;
	}

	/**
	 * Creates an unparented default type pattern node owned by this AST.
	 * By default, the declaration is for a default type pattern with an
	 * empty detail string.
	 * <p>
	 * To edit the detail string, use this method and then call
	 * <code>DefaultTypePattern.setDetail("newString")</code>.
	 * </p>
	 *
	 * @return a new unparented default type pattern node
	 */
	public DefaultTypePattern newDefaultTypePattern() {
		DefaultTypePattern result = new DefaultTypePattern(this,"");
		return result;
	}

	/**
	 * Creates an unparented default signature pattern node owned by this AST.
	 * By default, the declaration is for a default signature pattern with an
	 * empty detail string.
	 * <p>
	 * To edit the detail string, use this method and then call
	 * <code>SignaturePattern.setDetail("newString")</code>.
	 * </p>
	 *
	 * @return a new unparented default signature pattern node
	 */
	public SignaturePattern newSignaturePattern() {
		SignaturePattern result = new SignaturePattern(this,"");
		return result;
	}
}
