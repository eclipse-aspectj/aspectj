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
 * Field declaration node type.
 * <p>
 * This kind of node collects several variable declaration fragments
 * (<code>VariableDeclarationFragment</code>) into a single body declaration
 * (<code>BodyDeclaration</code>), all sharing the same modifiers and base type.
 * </p>
 * <pre>
 * FieldDeclaration:
 *    [Javadoc] { Modifier } Type VariableDeclarationFragment
 *         { <b>,</b> VariableDeclarationFragment } <b>;</b>
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source range begins with the first
 * character of the "/**" comment delimiter. When there is no Javadoc comment,
 * the source range begins with the first character of the initial modifier or
 * type. The source range extends through the last character of the final ";".
 * </p>
 * 
 * @since 2.0
 */
public class FieldDeclaration extends BodyDeclaration {
	
	/**
	 * Mask containing all legal modifiers for this construct.
	 */
	private static final int LEGAL_MODIFIERS = 
		Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
		| Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE
		| Modifier.TRANSIENT;

	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none.
	 */
	private int modifiers = Modifier.NONE;
		
	/**
	 * The base type; lazily initialized; defaults to an unspecified,
	 * legal type.
	 */
	private Type baseType = null;

	/**
	 * The list of variable declaration fragments (element type: 
	 * <code VariableDeclarationFragment</code>).  Defaults to an empty list.
	 */
	private ASTNode.NodeList variableDeclarationFragments = 
		new ASTNode.NodeList(true,  VariableDeclarationFragment.class);

	/**
	 * Creates a new unparented field declaration statement node owned 
	 * by the given AST.  By default, the field declaration has: no modifiers,
	 * an unspecified (but legal) type, and an empty list of variable 
	 * declaration fragments (which is syntactically illegal).
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	FieldDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return FIELD_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		FieldDeclaration result = new FieldDeclaration(target);
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target,(ASTNode) getJavadoc()));
		result.setModifiers(getModifiers());
		result.setType((Type) getType().clone(target));
		result.fragments().addAll(
			ASTNode.copySubtrees(target, fragments()));
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
			acceptChild(visitor, getType());
			acceptChildren(visitor, variableDeclarationFragments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * The following modifiers are valid for fields: public, private, protected,
	 * static, final, volatile, and transient.
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
	 * The following modifiers are valid for fields: public, private, protected,
	 * static, final, volatile, and transient.
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
	 * Returns the base type declared in this field declaration.
	 * <p>
	 * N.B. The individual child variable declaration fragments may specify 
	 * additional array dimensions. So the type of the variable are not 
	 * necessarily exactly this type.
	 * </p>
	 * 
	 * @return the base type
	 */ 
	public Type getType() {
		if (baseType == null) {
			// lazy initialize - use setter to ensure parent link set too
			setType(getAST().newPrimitiveType(PrimitiveType.INT));
		}
		return baseType;
	}

	/**
	 * Sets the base type declared in this field declaration to the given type.
	 * 
	 * @param type the new base type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.baseType, type, false);
		this.baseType = type;
	}

	/**
	 * Returns the live list of variable declaration fragments in this field 
	 * declaration. Adding and removing nodes from this list affects this node
	 * dynamically. All nodes in this list must be 
	 * <code>VariableDeclarationFragment</code>s; attempts to add any other 
	 * type of node will trigger an exception.
	 * 
	 * @return the live list of variable declaration fragments in this 
	 *    statement (element type: <code>VariableDeclarationFragment</code>)
	 */ 
	public List fragments() {
		return variableDeclarationFragments;
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("FieldDeclaration["); //$NON-NLS-1$
		buffer.append("field "); //$NON-NLS-1$
		getType().appendPrintString(buffer);
		buffer.append(" "); //$NON-NLS-1$
		for (Iterator it = fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment d = (VariableDeclarationFragment) it.next();
			d.getName().appendPrintString(buffer);
			if (it.hasNext()) {
				buffer.append(","); //$NON-NLS-1$
			}
		}
		buffer.append("]"); //$NON-NLS-1$
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (getJavadoc() == null ? 0 : getJavadoc().treeSize())
			+ (baseType == null ? 0 : getType().treeSize())
			+ variableDeclarationFragments.listSize();
	}
}
