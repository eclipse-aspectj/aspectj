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

/**
 * Static or instance initializer AST node type.
 *
 * <pre>
 * Initializer:
 *     [ <b>static</b> ] Block
 * </pre>
 * 
 * @since 2.0
 */
public class Initializer extends BodyDeclaration {
	
	/**
	 * Mask containing all legal modifiers for this construct.
	 */
	private static final int LEGAL_MODIFIERS = Modifier.STATIC;
	
	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none.
	 */
	private int modifiers = 0;

	/**
	 * The initializer body; lazily initialized; defaults to an empty block.
	 */
	private Block body = null;

	/**
	 * Creates a new AST node for an initializer declaration owned by the given 
	 * AST. By default, the initializer has no modifiers and an empty block.
	 * The javadoc comment is not used for initializers.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Initializer(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return INITIALIZER;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		Initializer result = new Initializer(target);
		result.setModifiers(getModifiers());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target,(ASTNode) getJavadoc()));
		result.setBody((Block) getBody().clone(target));
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
			acceptChild(visitor, getJavadoc());
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * Note that static is the only meaningful modifier for an initializer.
	 * </p>
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 */ 
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Sets the modifiers explicitly specified on this declaration.
	 * <p>
	 * Note that static is the only meaningful modifier for an initializer.
	 * </p>
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
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

	/**
	 * Returns the body of this initializer declaration.
	 * 
	 * @return the initializer body
	 */ 
	public Block getBody() {
		if (body == null) {
			// lazy initialize - use setter to ensure parent link set too
			setBody(new Block(getAST()));
		}
		return body;
	}
	
	/**
	 * Sets the body of this initializer declaration.
	 * 
	 * @param body the block node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.body, body, true);
		this.body = body;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (getJavadoc() == null ? 0 : getJavadoc().treeSize())
			+ (body == null ? 0 : getBody().treeSize());
	}
}

