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
 * Java compilation unit AST node type. This is the type of the root of an AST.
 *
 * Range 0: first character through last character of the source file.
 *
 * <pre>
 * CompilationUnit:
 *    [ PackageDeclaration ]
 *    { ImportDeclaration }
 *    { TypeDeclaration | <b>;</b> }
 * </pre>
 * 
 * @since 2.0
 */
public class CompilationUnit extends ASTNode {

	/**
	 * The package declaration, or <code>null</code> if none; initially
	 * <code>null</code>.
	 */
	private PackageDeclaration optionalPackageDeclaration = null;
	
	/**
	 * The list of import declarations in textual order order; 
	 * initially none (elementType: <code>ImportDeclaration</code>).
	 */
	private ASTNode.NodeList imports =
		new ASTNode.NodeList(false, ImportDeclaration.class);
	
	/**
	 * The list of type declarations in textual order order; 
	 * initially none (elementType: <code>TypeDeclaration</code>)
	 */
	private ASTNode.NodeList types =
		new ASTNode.NodeList(false, TypeDeclaration.class);
	
	/**
	 * Line end table. If <code>lineEndTable[i] == p</code> then the
	 * line number <code>i+1</code> ends at character position 
	 * <code>p</code>. Except for the last line, the positions are that
	 * of the last character of the line delimiter. 
	 * For example, the source string <code>A\nB\nC</code> has
	 * line end table {1, 3} (if \n is one character).
	 */
	private int[] lineEndTable = new int[0];

	/**
	 * Canonical empty list of messages.
	 */
	private static final Message[] EMPTY_MESSAGES = new Message[0];

	/**
	 * Messages reported by the compiler during parsing or name resolution;
	 * defaults to the empty list.
	 */
	private Message[] messages = EMPTY_MESSAGES;
	 
	/**
	 * Sets the line end table for this compilation unit.
	 * If <code>lineEndTable[i] == p</code> then line number <code>i+1</code> 
	 * ends at character position <code>p</code>. Except for the last line, the 
	 * positions are that of (the last character of) the line delimiter.
	 * For example, the source string <code>A\nB\nC</code> has
	 * line end table {1, 3, 4}.
	 * 
	 * @param lineEndtable the line end table
	 */
	void setLineEndTable(int[] lineEndTable) {
		if (lineEndTable == null) {
			throw new NullPointerException();
		}
		modifying();
		this.lineEndTable = lineEndTable;
	}

	/**
	 * Creates a new AST node for a compilation owned by the given AST.
	 * The compilation unit initially has no package declaration, no
	 * import declarations, and no type declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	CompilationUnit(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return COMPILATION_UNIT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		CompilationUnit result = new CompilationUnit(target);
		// n.b do not copy line number table or messages
		result.setPackage(
			(PackageDeclaration) ASTNode.copySubtree(target, getPackage()));
		result.imports().addAll(ASTNode.copySubtrees(target, imports()));
		result.types().addAll(ASTNode.copySubtrees(target, types()));
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
			acceptChild(visitor, getPackage());
			acceptChildren(visitor, imports);
			acceptChildren(visitor, types);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the node for the package declaration of this compilation 
	 * unit, or <code>null</code> if this compilation unit is in the 
	 * default package.
	 * 
	 * @return the package declaration node, or <code>null</code> if none
	 */ 
	public PackageDeclaration getPackage() {
		return optionalPackageDeclaration;
	}
	
	/**
	 * Sets or clears the package declaration of this compilation unit 
	 * node to the given package declaration node.
	 * 
	 * @param pkgDecl the new package declaration node, or 
	 *   <code>null</code> if this compilation unit does not have a package
	 *   declaration (that is in the default package)
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setPackage(PackageDeclaration pkgDecl) {
		replaceChild(this.optionalPackageDeclaration, pkgDecl, false);
		this.optionalPackageDeclaration = pkgDecl;
	}

	/**
	 * Returns the live list of nodes for the import declaration of this 
	 * compilation unit, in order of appearance.
	 * 
	 * @return the live list of import declaration nodes
	 *    (elementType: <code>ImportDeclaration</code>)
	 */ 
	public List imports() {
		return imports;
	}
	
	/**
	 * Returns the live list of nodes for the top-level type declaration of this 
	 * compilation unit, in order of appearance.
	 * 
	 * @return the live list of top-level type declaration
	 *    nodes (elementType: <code>TypeDeclaration</code>)
	 */ 
	public List types() {
		return types;
	}

	/**
	 * Finds the corresponding AST node in the given compilation unit from 
	 * which the given binding originated. Returns <code>null</code> if the
	 * binding does not correspond to any node in this compilation unit.
	 * <p>
	 * The following table indicates the expected node type for the various
	 * different kinds of bindings:
	 * <ul>
	 * <li></li>
	 * <li>package - a <code>PackageDeclaration</code></li>
	 * <li>class or interface - a <code>TypeDeclaration</code> or a
	 *    <code>ClassInstanceCreation</code> (for anonymous classes) </li>
	 * <li>primitive type - none</li>
	 * <li>array type - none</li>
	 * <li>field - a <code>VariableDeclarationFragment</code> in a 
	 *    <code>FieldDeclaration</code> </li>
	 * <li>local variable - a <code>SingleVariableDeclaration</code>, or
	 *    a <code>VariableDeclarationFragment</code> in a 
	 *    <code>VariableDeclarationStatement</code> or 
	 *    <code>VariableDeclarationExpression</code></li>
	 * <li>method - a <code>MethodDeclaration</code> </li>
	 * <li>constructor - a <code>MethodDeclaration</code> </li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @param binding the binding
	 * @return the corresponding node where the bindings is declared, 
	 *    or <code>null</code> if none
	 */
	public ASTNode findDeclaringNode(IBinding binding) {
		return getAST().getBindingResolver().findDeclaringNode(binding);
	}
	
	/**
	 * Returns the line number corresponding to the given source character
	 * position in the original source string. The initial line of the 
	 * compilation unit is numbered 1, and each line extends through the
	 * last character of the end-of-line delimiter. The very last line extends
	 * through the end of the source string and has no line delimiter.
	 * For example, the source string <code>class A\n{\n}</code> has 3 lines
	 * corresponding to inclusive character ranges [0,8], [8,9], and [10,10].
	 * Returns 1 for a character position that does not correspond to any
	 * source line, or if no line number information is available for this
	 * compilation unit.
	 * 
	 * @param position a 0-based character position, possibly
	 *   negative or out of range
	 * @return the 1-based line number, or <code>1</code> if the character
	 *    position does not correspond to a source line in the original
	 *    source file or if line number information is not known for this
	 *    compilation unit
	 * @see AST#parseCompilationUnit
	 */
	public int lineNumber(int position) {
		int length = lineEndTable.length;
		if (length == 0) {
			// no line number info
			return 1;
		}
		int low = 0;
		if (position <= lineEndTable[low]) {
			// position illegal or before the first line delimiter
			return 1;
		}
		// assert position > lineEndTable[low+1]  && low == 0
		int hi = length - 1;
		if (position > lineEndTable[hi]) {
			// position beyond the last line separator
			if (position >= getStartPosition() + getLength()) {
				// this is beyond the end of the source length
				return 1;
			} else {
				return length + 1;
			}
		}
		// assert lineEndTable[low]  < position <= lineEndTable[hi]
		// && low == 0 && hi == length - 1 && low < hi
		
		// binary search line end table
		while (true) {
			// invariant lineEndTable[low] < position <= lineEndTable[hi]
			// && 0 <= low < hi <= length - 1
			// reducing measure hi - low
			if (low + 1 == hi) {
				// assert lineEndTable[low] < position <= lineEndTable[low+1]
				// position is on line low+1 (line number is low+2)
				return low + 2;
			}
			// assert hi - low >= 2, so average is truly in between
			int mid = (low + hi) / 2;
			// assert 0 <= low < mid < hi <= length - 1
			if (position <= lineEndTable[mid]) {
				// assert lineEndTable[low] < position <= lineEndTable[mid]
				// && 0 <= low < mid < hi <= length - 1
				hi = mid;
			} else {
				// position > lineEndTable[mid]
				// assert lineEndTable[mid] < position <= lineEndTable[hi]
				// && 0 <= low < mid < hi <= length - 1
				low = mid;
			}
			// in both cases, invariant reachieved with reduced measure
		}
	}

	/**
	 * Returns the list of messages reported by the compiler during the parsing 
	 * or the type checking of this compilation unit. This list might be a subset of 
	 * errors detected and reported by a Java compiler.
	 * 
	 * @return the list of messages, possibly empty
	 * @see AST#parseCompilationUnit
	 */
	public Message[] getMessages() {
		return messages;
	}

	/**
	 * Sets the array of messages reported by the compiler during the parsing or
	 * name resolution of this compilation unit.
	 * 
	 * @param messages the list of messages
	 */
	void setMessages(Message[] messages) {
		if (messages == null) {
			throw new IllegalArgumentException();
		}
		this.messages = messages;
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("CompilationUnit"); //$NON-NLS-1$
		// include the type names
		buffer.append("["); //$NON-NLS-1$
		for (Iterator it = types().iterator(); it.hasNext(); ) {
			TypeDeclaration d = (TypeDeclaration) it.next();
			buffer.append(d.getName().getIdentifier());
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
		int size = BASE_NODE_SIZE + 4 * 4;
		if (lineEndTable != null) {
			size += HEADERS + 4 * lineEndTable.length;
		}
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (optionalPackageDeclaration == null ? 0 : getPackage().treeSize())
			+ imports.listSize()
			+ types.listSize();
	}
}

