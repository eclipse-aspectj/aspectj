/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.Iterator;
import java.util.List;

/**
 * Method declaration AST node type. A method declaration
 * is the union of a method declaration and a constructor declaration.
 *
 * <pre>
 * MethodDeclaration:
 *    [ Javadoc ] { Modifier } ( Type | <b>void</b> ) Identifier <b>(</b>
 *        [ FormalParameter 
 * 		     { <b>,</b> FormalParameter } ] <b>)</b> {<b>[</b> <b>]</b> }
 *        [ <b>throws</b> TypeName { <b>,</b> TypeName } ] ( Block | <b>;</b> )
 * ConstructorDeclaration:
 *    [ Javadoc ] { Modifier } Identifier <b>(</b>
 * 		  [ FormalParameter
 * 			 { <b>,</b> FormalParameter } ] <b>)</b>
 *        [<b>throws</b> TypeName { <b>,</b> TypeName } ] MethodBody
 * </pre>
 * Normal form:
 * <pre>
 * MethodDeclaration:
 *    [ Javadoc ] { Modifier } ( Type | <b>void</b> ) Identifier
 *        <b>(</b> [ FormalParamter { <b>,</b> FormalParameter } ] <b>)</b>
 *        [ <b>throws</b> TypeName { <b>,</b> TypeName } ]
 *        ( Block | <b>;</b> )
 * ConstructorDeclaration:
 *    [ Javadoc ] { Modifier } Identifier
 *        <b>(</b> [ FormalParameter { <b>,</b> FormalParameter } ] <b>)</b>
 *        [ <b>throws</b> TypeName { <b>,</b> TypeName } ]
 *        Block
 * </pre> 
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier keyword (if modifiers), or the
 * first character of the return type (method, no modifiers), or the
 * first character of the identifier (constructor, no modifiers).
 * The source range extends through the last character of the ";" token (if
 * no body), or the last character of the block (if body).
 * </p>
 *
 * @since 2.0 
 */
public class MethodDeclaration extends BodyDeclaration {
	
	/**
	 * Mask containing all legal modifiers for this construct.
	 */
	private static final int LEGAL_MODIFIERS = 
		Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
		| Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED
		| Modifier.NATIVE | Modifier.ABSTRACT | Modifier.STRICTFP;
		
	/**
	 * <code>true</code> for a constructor, <code>false</code> for a method.
	 * Defaults to method.
	 */
	private boolean isConstructor = false;
	
	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none.
	 */
	private int modifiers = Modifier.NONE;
	
	/**
	 * The method name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName methodName = null;
	
	/**
	 * The parameter declarations 
	 * (element type: <code>SingleVariableDeclaration</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList parameters =
		new ASTNode.NodeList(true, SingleVariableDeclaration.class);;
	
	/**
	 * The return type; lazily initialized; defaults to void. Note that this 
	 * field is not used for constructor declarations.
	 */
	private Type returnType = null;
	
	/**
	 * The list of thrown exception names (element type: <code>Name</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList thrownExceptions =
		new ASTNode.NodeList(false, Name.class);

	/**
	 * The method body, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Block optionalBody = null;
	
	/**
	 * Creates a new AST node for a method declaration owned 
	 * by the given AST. By default, the declaration is for a method of an
	 * unspecified, but legal, name; no modifiers; no javadoc; no parameters; 
	 * void return type; no thrown exceptions; and no body (as opposed to an
	 * empty body).
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	MethodDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return METHOD_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		MethodDeclaration result = new MethodDeclaration(target);
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target,(ASTNode) getJavadoc()));
		result.setModifiers(getModifiers());
		result.setConstructor(isConstructor());
		result.setReturnType(
			(Type) ASTNode.copySubtree(target, getReturnType()));
		result.setName((SimpleName) getName().clone(target));
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		result.thrownExceptions().addAll(
			ASTNode.copySubtrees(target, thrownExceptions()));
		result.setBody(
			(Block) ASTNode.copySubtree(target, getBody()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getJavadoc());
			acceptChild(visitor, getReturnType());
			acceptChild(visitor, getName());
			acceptChildren(visitor, parameters);
			acceptChildren(visitor, thrownExceptions);
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns whether this declaration declares a constructor or a method.
	 * 
	 * @return <code>true</code> if this is a constructor declaration,
	 *    and <code>false</code> if this is a method declaration
	 */ 
	public boolean isConstructor() {
		return isConstructor;
	}
	
	/**
	 * Sets whether this declaration declares a constructor or a method.
	 * 
	 * @param isConstructor <code>true</code> for a constructor declaration,
	 *    and <code>false</code> for a method declaration
	 */ 
	public void setConstructor(boolean isConstructor) {
		modifying();
		this.isConstructor = isConstructor;
	}

	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * Note that deprecated is not included.
	 * </p>
	 * 
	 * @return the bit-wise or of Modifier constants
	 * @see Modifier
	 */ 
	public int getModifiers() {
		return modifiers;
	}
	
	/**
	 * Sets the modifiers explicitly specified on this declaration.
	 * <p>
	 * The following modifiers are valid for methods: public, private, protected,
	 * static, final, synchronized, native, abstract, and strictfp.
	 * For constructors, only public, private, and protected are meaningful.
	 * </p>
	 * 
	 * @param modifiers the bit-wise or of Modifier constants
	 * @see Modifier
	 * @exception IllegalArgumentException if the modifiers are illegal
	 */ 
	public void setModifiers(int modifiers) {
		if ((modifiers & ~LEGAL_MODIFIERS) != 0) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.modifiers = modifiers;
	}

//	public List<TypeParameter> typeParameters(); // JSR-014

	/**
	 * Returns the name of the method declared in this method declaration.
	 * For a constructor declaration, this should be the same as the name 
	 * of the class.
	 * 
	 * @return the method name node
	 */ 
	public SimpleName getName() {
		if (methodName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return methodName;
	}
	
	/**
	 * Sets the name of the method declared in this method declaration to the
	 * given name. For a constructor declaration, this should be the same as 
	 * the name of the class.
	 * 
	 * @param methodName the new method name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName methodName) {
		if (methodName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.methodName, methodName, false);
		this.methodName = methodName;
	}

	/**
	 * Returns the live ordered list of method parameter declarations for this
	 * method declaration.
	 * 
	 * @return the live list of method parameter declarations
	 *    (element type: <code>SingleVariableDeclaration</code>)
	 */ 
	public List parameters() {
		return parameters;
	}
	
	/**
	 * Returns the live ordered list of thrown exception names in this method 
	 * declaration.
	 * 
	 * @return the live list of exception names
	 *    (element type: <code>Name</code>)
	 */ 
	public List thrownExceptions() {
		return thrownExceptions;
	}
	
	/**
	 * Returns the return type of the method declared in this method 
	 * declaration. This is one of the few places where the void type 
	 * is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons).
	 * </p>
	 * 
	 * @return the return type, possibly the void primitive type
	 */ 
	public Type getReturnType() {
		if (returnType == null) {
			// lazy initialize - use setter to ensure parent link set too
			setReturnType(getAST().newPrimitiveType(PrimitiveType.VOID));
		}
		return returnType;
	}

	/**
	 * Sets the return type of the method declared in this method declaration
	 * to the given type. This is one of the few places where the void type is
	 * meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons).
	 * </p>
	 * 
	 * @param type the new return type, possibly the void primitive type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setReturnType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		replaceChild((ASTNode) this.returnType, (ASTNode) type, false);
		this.returnType = type;
	}

	/**
	 * Returns the body of this method declaration, or <code>null</code> if 
	 * this method has <b>no</b> body.
	 * <p>
	 * Note that there is a subtle difference between having no body and having
	 * an empty body ("{}").
	 * </p>
	 * 
	 * @return the method body, or <code>null</code> if this method has no
	 *    body
	 */ 
	public Block getBody() {
		return optionalBody;
	}

	/**
	 * Sets or clears the body of this method declaration.
	 * <p>
	 * Note that there is a subtle difference between having no body 
	 * (as in <code>"void foo();"</code>) and having an empty body (as in
	 * "void foo() {}"). Abstract methods, and methods declared in interfaces,
	 * have no body. Non-abstract methods, and all constructors, have a body.
	 * </p>
	 * 
	 * @param body the block node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		// a MethodDeclaration may occur in a Block - must check cycles
		replaceChild(this.optionalBody, body, true);
		this.optionalBody = body;
	}

	/**
	 * Resolves and returns the binding for the method or constructor declared
	 * in this method or constructor declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IMethodBinding resolveBinding() {
		return getAST().getBindingResolver().resolveMethod(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("MethodDeclaration[");//$NON-NLS-1$
		buffer.append(isConstructor() ? "constructor " : "method ");//$NON-NLS-2$//$NON-NLS-1$
		buffer.append(getName().getIdentifier());
		buffer.append("(");//$NON-NLS-1$
		for (Iterator it = parameters().iterator(); it.hasNext(); ) {
			SingleVariableDeclaration d = (SingleVariableDeclaration) it.next();
			d.getType().appendPrintString(buffer);
			if (it.hasNext()) {
				buffer.append(";");//$NON-NLS-1$
			}
		}
		buffer.append(")");//$NON-NLS-1$
		if (!isConstructor()) {
			buffer.append(" returns ");//$NON-NLS-1$
			getReturnType().appendPrintString(buffer);
		}
		buffer.append("]");//$NON-NLS-1$
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 7 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (getJavadoc() == null ? 0 : getJavadoc().treeSize())
			+ (methodName == null ? 0 : getName().treeSize())
			+ (returnType == null ? 0 : getReturnType().treeSize())
			+ parameters.listSize()
			+ thrownExceptions.listSize()
			+ (optionalBody == null ? 0 : getBody().treeSize());
	}
}

