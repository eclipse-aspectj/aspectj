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
 * Type declaration AST node type. A type declaration
 * is the union of a class declaration and an interface declaration.
 *
 * <pre>
 * TypeDeclaration:
 * 		ClassDeclaration
 * 		InterfaceDeclaration
 * ClassDeclaration:
 *      [ Javadoc ] { Modifier } <b>class</b> Identifier
 *			[ <b>extends</b> Type]
 *			[ <b>implements</b> Type { <b>,</b> Type } ]
 *			<b>{</b> { ClassBodyDeclaration | <b>;</b> } <b>}</b>
 * InterfaceDeclaration:
 *      [ Javadoc ] { Modifier } <b>interface</b> Identifier
 *			[ <b>extends</b> Type]
 * 			<b>{</b> { InterfaceBodyDeclaration | <b>;</b> } <b>}</b>
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier keyword (if modifiers), or the
 * first character of the "class" or "interface": keyword (if no modifiers).
 * The source range extends through the last character of the ";" token (if
 * no body), or the last character of the "}" token following the body
 * declarations.
 * </p>
 * 
 * @since 2.0
 */
public class TypeDeclaration extends BodyDeclaration {
	
	/**
	 * Mask containing all legal modifiers for this construct.
	 */
	private static final int LEGAL_MODIFIERS = 
		Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
		| Modifier.STATIC | Modifier.FINAL | Modifier.ABSTRACT
		| Modifier.STRICTFP;
		
	/**
	 * <code>true</code> for an interface, <code>false</code> for a class.
	 * Defaults to class.
	 */
	private boolean isInterface = false;
	
	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none.
	 */
	private int modifiers = Modifier.NONE;
	
	/**
	 * The type name; lazily initialized; defaults to a unspecified,
	 * legal Java class identifier.
	 */
	private SimpleName typeName = null;

	/**
	 * The optional superclass name; <code>null</code> if none.
	 * Defaults to none. Note that this field is not used for
	 * interface declarations.
	 */
	private Name optionalSuperclassName = null;

	/**
	 * The superinterface names (element type: <code>Name</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList superInterfaceNames =
		new ASTNode.NodeList(false, Name.class);

	/**
	 * The body declarations (element type: <code>BodyDeclaration</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList bodyDeclarations = 
		new ASTNode.NodeList(true, BodyDeclaration.class);

	/**
	 * Creates a new AST node for a type declaration owned by the given 
	 * AST. By default, the type declaration is for a class of an
	 * unspecified, but legal, name; no modifiers; no javadoc; 
	 * no superclass or superinterfaces; and an empty list of body
	 * declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TypeDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TYPE_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TypeDeclaration result = new TypeDeclaration(target);
		result.setModifiers(getModifiers());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target,(ASTNode) getJavadoc()));
		result.setInterface(isInterface());
		result.setName((SimpleName) getName().clone(target));
		result.setSuperclass(
			(Name) ASTNode.copySubtree(target,(ASTNode) getSuperclass()));
		result.superInterfaces().addAll(
			ASTNode.copySubtrees(target, superInterfaces()));
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
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
			acceptChild(visitor, getName());
			acceptChild(visitor, getSuperclass());
			acceptChildren(visitor, superInterfaceNames);
			acceptChildren(visitor, bodyDeclarations);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns whether this type declaration declares a class or an 
	 * interface.
	 * 
	 * @return <code>true</code> if this is an interface declaration,
	 *    and <code>false</code> if this is a class declaration
	 */ 
	public boolean isInterface() {
		return isInterface;
	}
	
	/**
	 * Sets whether this type declaration declares a class or an 
	 * interface.
	 * 
	 * @param isInterface <code>true</code> if this is an interface
	 *    declaration, and <code>false</code> if this is a class
	 * 	  declaration
	 */ 
	public void setInterface(boolean isInterface) {
		modifying();
		this.isInterface = isInterface;
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
	 * The following modifiers are valid for types: public, private, protected,
	 * static, final, abstract, and strictfp.
	 * </p>
	 * <p>
	 * Only a subset of modifiers are legal in any given situation.
	 * Note that deprecated is not included.
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

	/**
	 * Returns the name of the type declared in this type declaration.
	 * 
	 * @return the type name node
	 */ 
	public SimpleName getName() {
		if (typeName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return typeName;
	}
		
	/**
	 * Sets the name of the type declared in this type declaration to the
	 * given name.
	 * 
	 * @param typeName the new type name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.typeName, typeName, false);
		this.typeName = typeName;
	}

//	JSR-014 feature
//	public List<TypeParameter> typeParameters() {
//		throw RuntimeException("not implemented yet");
//	}

	/**
	 * Returns the name of the superclass declared in this type
	 * declaration, or <code>null</code> if there is none.
	 * <p>
	 * Note that this child is not relevant for interface declarations
	 * (although it does still figure in subtree equality comparisons).
	 * </p>
	 * 
	 * @return the superclass name node, or <code>null</code> if 
	 *    there is none
	 */ 
	public Name getSuperclass() {
		return optionalSuperclassName;
	}
	
	/**
	 * Sets or clears the name of the superclass declared in this type
	 * declaration.
	 * <p>
	 * Note that this child is not relevant for interface declarations
	 * (although it does still figure in subtree equality comparisons).
	 * </p>
	 * 
	 * @param superclassName the superclass name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setSuperclass(Name superclassName) {
		replaceChild(
			(ASTNode) this.optionalSuperclassName,
			(ASTNode) superclassName, false);
		this.optionalSuperclassName = superclassName;
	}

	/**
	 * Returns the live ordered list of names of superinterfaces of this type 
	 * declaration. For a class declaration, these are the names of the
	 * interfaces that this class implements; for an interface declaration,
	 * these are the names of the interfaces that this interface extends.
	 * 
	 * @return the live list of interface names
	 *    (element type: <code>Name</code>)
	 */ 
	public List superInterfaces() {
		return superInterfaceNames;
	}
	
	/**
	 * Returns the live ordered list of body declarations of this type 
	 * declaration. For a class declaration, these are the
	 * initializer, field, method, constructor, and member type
	 * declarations; for an interface declaration, these are 
	 * the constant, method, and member type declarations.
	 * 
	 * @return the live list of body declarations
	 *    (element type: <code>BodyDeclaration</code>)
	 */ 
	public List bodyDeclarations() {
		return bodyDeclarations;
	}
	
	/**
	 * Returns the ordered list of field declarations of this type 
	 * declaration. For a class declaration, these are the
	 * field declarations; for an interface declaration, these are
	 * the constant declarations.
	 * <p>
	 * This convenience method returns this node's body declarations
	 * with non-fields filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of field declarations
	 */ 
	public FieldDeclaration[] getFields() {
		List bd = bodyDeclarations();
		int fieldCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof FieldDeclaration) {
				fieldCount++;
			}
		}
		FieldDeclaration[] fields = new FieldDeclaration[fieldCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof FieldDeclaration) {
				fields[next++] = (FieldDeclaration) decl;
			}
		}
		return fields;
	}

	/**
	 * Returns the ordered list of method declarations of this type 
	 * declaration.
	 * <p>
	 * This convenience method returns this node's body declarations
	 * with non-methods filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of method (and constructor) 
	 *    declarations
	 */ 
	public MethodDeclaration[] getMethods() {
		List bd = bodyDeclarations();
		int methodCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof MethodDeclaration) {
				methodCount++;
			}
		}
		MethodDeclaration[] methods = new MethodDeclaration[methodCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof MethodDeclaration) {
				methods[next++] = (MethodDeclaration) decl;
			}
		}
		return methods;
	}

	/**
	 * Returns the ordered list of member type declarations of this type 
	 * declaration.
	 * <p>
	 * This convenience method returns this node's body declarations
	 * with non-types filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of member type declarations
	 */ 
	public TypeDeclaration[] getTypes() {
		List bd = bodyDeclarations();
		int typeCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof TypeDeclaration) {
				typeCount++;
			}
		}
		TypeDeclaration[] memberTypes = new TypeDeclaration[typeCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof TypeDeclaration) {
				memberTypes[next++] = (TypeDeclaration) decl;
			}
		}
		return memberTypes;
	}

	/**
	 * Returns whether this type declaration is a package member (that is,
	 * a top-level type).
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a compilation unit node.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a compilation unit node, and <code>false</code> otherwise
	 */ 
	public boolean isPackageMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof CompilationUnit);
	}

	/**
	 * Returns whether this type declaration is a type member.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration node or an anonymous 
	 * class declaration.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration node or a class instance creation node, and 
	 *   <code>false</code> otherwise
	 */ 
	public boolean isMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof TypeDeclaration)
			|| (parent instanceof AnonymousClassDeclaration);
	}

	/**
	 * Returns whether this type declaration is a local type.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration statement node.
	 * </p>
	 * 
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration statement node, and <code>false</code> otherwise
	 */ 
	public boolean isLocalTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof TypeDeclarationStatement);
	}
	
	/**
	 * Resolves and returns the binding for the class or interface declared in
	 * this type declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public ITypeBinding resolveBinding() {
		return getAST().getBindingResolver().resolveType(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("TypeDeclaration[");//$NON-NLS-1$
		buffer.append(isInterface() ? "interface " : "class ");//$NON-NLS-2$//$NON-NLS-1$
		buffer.append(getName().getIdentifier());
		buffer.append(" ");//$NON-NLS-1$
		for (Iterator it = bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			d.appendDebugString(buffer);
			if (it.hasNext()) {
				buffer.append(";");//$NON-NLS-1$
			}
		}
		buffer.append("]");//$NON-NLS-1$
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 6 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (getJavadoc() == null ? 0 : getJavadoc().treeSize())
			+ (typeName == null ? 0 : getName().treeSize())
			+ (optionalSuperclassName == null ? 0 : getSuperclass().treeSize())
			+ superInterfaceNames.listSize()
			+ bodyDeclarations.listSize();
	}
}

