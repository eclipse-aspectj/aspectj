/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * AspectDeclaration DOM AST node.
 * 
 * Has everything an AjTypeDeclaration has plus: an ASTNode called 'perClause' a boolean called 'privileged'
 * 
 * @author ajh02
 * 
 */
public class AspectDeclaration extends AjTypeDeclaration {

	public static final ChildPropertyDescriptor PERCLAUSE_PROPERTY = new ChildPropertyDescriptor(AspectDeclaration.class,
			"perClause", ASTNode.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	public static final SimplePropertyDescriptor PRIVILEGED_PROPERTY = new SimplePropertyDescriptor(AspectDeclaration.class,
			"privileged", boolean.class, MANDATORY); //$NON-NLS-1$

	protected static List aspectPROPERTY_DESCRIPTORS_2_0;
	protected static List aspectPROPERTY_DESCRIPTORS_3_0;

	static {
		List temporary = new ArrayList();
		createPropertyList(AspectDeclaration.class, temporary);
		temporary.addAll(ajPROPERTY_DESCRIPTORS_2_0);
		addProperty(PERCLAUSE_PROPERTY, temporary);
		addProperty(PRIVILEGED_PROPERTY, temporary);
		aspectPROPERTY_DESCRIPTORS_2_0 = reapPropertyList(temporary);

		temporary.clear();
		createPropertyList(AspectDeclaration.class, temporary);
		temporary.addAll(ajPROPERTY_DESCRIPTORS_3_0);
		addProperty(PERCLAUSE_PROPERTY, temporary);
		addProperty(PRIVILEGED_PROPERTY, temporary);
		aspectPROPERTY_DESCRIPTORS_3_0 = reapPropertyList(temporary);
	}

	protected ASTNode perClause = null; // stays null if the aspect is an _implicit_ persingleton()
	/**
	 * <code>true</code> for a privileged aspect, <code>false</code> otherwise. Defaults to not privileged.
	 */
	private boolean isPrivileged = false;

	AspectDeclaration(AST ast) {
		super(ast);
	}

	AspectDeclaration(AST ast, ASTNode perClause) {
		this(ast);
		this.perClause = perClause;
		setAspect(true);
	}

	AspectDeclaration(AST ast, ASTNode perClause, boolean isPrivileged) {
		this(ast, perClause);
		this.isPrivileged = isPrivileged;
	}

	/*
	 * (omit javadoc for this method) Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		AspectDeclaration result = new AspectDeclaration(target, perClause);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc((Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			result.internalSetModifiers(getModifiers());
			result.setSuperclass((Name) ASTNode.copySubtree(target, getSuperclass()));
			result.superInterfaces().addAll(ASTNode.copySubtrees(target, superInterfaces()));
		}
		result.setInterface(isInterface());
		result.setAspect(isAspect());
		result.setPrivileged(isPrivileged());
		result.setName((SimpleName) getName().clone(target));
		if (this.ast.apiLevel >= AST.JLS3) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.typeParameters().addAll(ASTNode.copySubtrees(target, typeParameters()));
			result.setSuperclassType((Type) ASTNode.copySubtree(target, getSuperclassType()));
			result.superInterfaceTypes().addAll(ASTNode.copySubtrees(target, superInterfaceTypes()));
		}
		result.bodyDeclarations().addAll(ASTNode.copySubtrees(target, bodyDeclarations()));
		result.setPerClause(getPerClause().clone(target));
		return result;
	}

	/*
	 * (omit javadoc for this method) Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
				acceptChild(visitor, getJavadoc());
				acceptChild(visitor, getName());
				acceptChild(visitor, getSuperclass());
				acceptChildren(visitor, this.superInterfaceNames);
				acceptChild(visitor, this.perClause);
				acceptChildren(visitor, this.bodyDeclarations);
			}
			if (this.ast.apiLevel >= AST.JLS3) {
				acceptChild(visitor, getJavadoc());
				acceptChildren(visitor, this.modifiers);
				acceptChild(visitor, getName());
				acceptChildren(visitor, this.typeParameters);
				acceptChild(visitor, getSuperclassType());
				acceptChildren(visitor, this.superInterfaceTypes);
				acceptChild(visitor, this.perClause);
				acceptChildren(visitor, this.bodyDeclarations);
			}
		}
		visitor.endVisit(this);
	}

	/*
	 * (omit javadoc for this method) Method declared on ASTNode and AjTypeDeclaration.
	 */
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == PRIVILEGED_PROPERTY) {
			if (get) {
				return isPrivileged();
			} else {
				setPrivileged(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}

	/*
	 * (omit javadoc for this method) Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == PERCLAUSE_PROPERTY) {
			if (get) {
				return getPerClause();
			} else {
				setPerClause(child);
				return null;
			}
		}
		return super.internalGetSetChildProperty(property, get, child);
	}

	/**
	 * Returns a list of structural property descriptors for this node type. Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the <code>AST.JLS&ast;</code> constants
	 * 
	 * @return a list of property descriptors (element type: {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return aspectPROPERTY_DESCRIPTORS_2_0;
		} else {
			return aspectPROPERTY_DESCRIPTORS_3_0;
		}
	}

	public ASTNode getPerClause() {
		return perClause;
	}

	public void setPerClause(ASTNode perClause) {
		if (perClause == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.perClause;
		preReplaceChild(oldChild, perClause, PERCLAUSE_PROPERTY);
		this.perClause = perClause;
		postReplaceChild(oldChild, perClause, PERCLAUSE_PROPERTY);
	}

	/**
	 * Returns whether this aspect is a privileged one.
	 * 
	 * @return <code>true</code> if this is a privileged aspect declaration, and <code>false</code> otherwise.
	 */
	public boolean isPrivileged() {
		return this.isPrivileged;
	}

	/**
	 * Sets whether this aspect is a privileged one
	 * 
	 * @param isPrivileged <code>true</code> if this is a privileged aspect declaration, and <code>false</code> otherwise.
	 */
	public void setPrivileged(boolean isPrivileged) {
		preValueChange(PRIVILEGED_PROPERTY);
		this.isPrivileged = isPrivileged;
		postValueChange(PRIVILEGED_PROPERTY);
	}

	public List getAdvice() {
		// ajh02: method added
		List bd = bodyDeclarations();
		List advice = new ArrayList();
		for (Object decl : bd) {
			if (decl instanceof AdviceDeclaration) {
				advice.add(decl);
			}
		}
		return advice;
	}

	// public PointcutDeclaration[] getPointcuts() {
	// // ajh02: method added, currently returning none :-/
	// List bd = bodyDeclarations();
	// // ajh02: 0 bodyDeclarations :-/
	// int pointcutCount = 0;
	// for (Iterator it = bd.listIterator(); it.hasNext(); ) {
	// if (it.next() instanceof PointcutDeclaration) {
	// pointcutCount++;
	// }
	// }
	// PointcutDeclaration[] pointcuts = new PointcutDeclaration[pointcutCount];
	// int next = 0;
	// for (Iterator it = bd.listIterator(); it.hasNext(); ) {
	// Object decl = it.next();
	// if (decl instanceof PointcutDeclaration) {
	// pointcuts[next++] = (PointcutDeclaration) decl;
	// }
	// }
	// return pointcuts;
	// }

}