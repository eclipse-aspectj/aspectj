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

import java.util.Map;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * Umbrella owner and abstract syntax tree node factory.
 * An <code>AST</code> instance serves as the common owner of any number of
 * AST nodes, and as the factory for creating new AST nodes owned by that 
 * instance.
 * <p>
 * Abstract syntax trees may be hand constructed by clients, using the
 * <code>new<it>TYPE</it></code> factory methods to create new nodes, and the
 * various <code>set<it>CHILD</it></code> methods 
 * (see <code>ASTNode</code> and its subclasses) to connect them together.
 * </p>
 * <p>
 * Each AST node belongs to a unique AST instance, called the owning AST.
 * The children of an AST node always have the same owner as their parent node.
 * If a node from one AST is to be added to a different AST, the subtree must
 * be cloned first to ensures that the added nodes have the correct owning AST.
 * </p>
 * <p>
 * The static method <code>parseCompilationUnit</code> parses a string
 * containing a Java compilation unit and returns the abstract syntax tree
 * for it. The resulting nodes carry source ranges relating the node back to
 * the original source characters. Optional name and type resolution can also
 * be requested at the time the AST is created.
 * </p>
 * <p>
 * Clients may create instances of this class, which is not intended to be
 * subclassed.
 * </p>
 * 
 * @see #parseCompilationUnit
 * @see ASTNode
 * @since 2.0
 */
public final class AST {
	
	/**
	 * Internal modification count; initially 0; increases monotonically
	 * <b>by one or more</b> as the AST is successively modified.
	 */
	private long modCount = 0;
	
	/**
	 * Java Scanner used to validate preconditions for the creation of specific nodes
	 * like CharacterLiteral, NumberLiteral, StringLiteral or SimpleName.
	 */
	Scanner scanner;

	/**
	 * Creates a new, empty abstract syntax tree using default options.
	 * 
	 * @see JavaCore#getDefaultOptions
	 */
	public AST() {
		this(JavaCore.getDefaultOptions());
	}

	/**
	 * Creates a new, empty abstract syntax tree using the given options.
	 * <p>
	 * Following option keys are significant:
	 * <ul>
	 * <li><code>"org.eclipse.jdt.core.compiler.source"</code> - 
	 *    indicates source compatibility mode (as per <code>JavaCore</code>);
	 *    <code>"1.3"</code> means the source code is as per JDK 1.3;
	 *    <code>"1.4"</code> means the source code is as per JDK 1.4
	 *    (<code>assert</code> is a keyword);
	 *    additional legal values may be added later. </li>
	 * </ul>
	 * Options other than the above are ignored.
	 * </p>
	 * 
	 * @param options the table of options (key type: <code>String</code>;
	 *    value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions
	 */
	public AST(Map options) {
		Object value = options.get("org.eclipse.jdt.core.compiler.source"); //$NON-NLS-1$
		if ("1.3".equals(value)) { //$NON-NLS-1$
			// use a 1.3 scanner - treats assert as an identifier
			this.scanner = new Scanner();
		} else {
			// use a 1.4 scanner - treats assert as an keyword
			this.scanner = new Scanner(false, false, false, true);
		}
	}
		
	/**
	 * Returns the modification count for this AST. The modification count
	 * is a non-negative value that increases (by 1 or perhaps by more) as
	 * this AST or its nodes are changed. The initial value is unspecified.
	 * <p>
	 * The following things count as modifying an AST:
	 * <ul>
	 * <li>creating a new node owned by this AST,</li>
	 * <li>adding a child to a node owned by this AST,</li>
	 * <li>removing a child from a node owned by this AST,</li>
	 * <li>setting a non-node attribute of a node owned by this AST.</li>
	 * </ul>
	 * </p>
	 * Operations which do not entail creating or modifying existing nodes
	 * do not increase the modification count.
	 * <p>
	 * N.B. This method may be called several times in the course
	 * of a single client operation. The only promise is that the modification
	 * count increases monotonically as the AST or its nodes change; there is 
	 * no promise that a modifying operation increases the count by exactly 1.
	 * </p>
	 * 
	 * @return the current value (non-negative) of the modification counter of
	 *    this AST
	 */
	public long modificationCount() {
		return modCount;
	}
	
	/**
	 * Indicates that this AST is about to be modified.
	 * <p>
	 * The following things count as modifying an AST:
	 * <ul>
	 * <li>creating a new node owned by this AST,</li>
	 * <li>adding a child to a node owned by this AST</li>
	 * <li>removing a child from a node owned by this AST</li>
	 * <li>setting a non-node attribute of a node owned by this AST</li>.
	 * </ul>
	 * </p>
	 * <p>
	 * N.B. This method may be called several times in the course
	 * of a single client operation.
	 * </p> 
	 */
	void modifying() {
		// increase the modification count
		modCount++;	
	}

	
	/**
	 * Parses the given string as a Java compilation unit and creates and 
	 * returns a corresponding abstract syntax tree.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST). If a syntax error is detected while
	 * parsing, the relevant node(s) of the tree will be flagged as 
	 * <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If <code>resolveBindings</code> is <code>true</code>, the various names
	 * and types appearing in the compilation unit can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings 
	 * draw connections between the different parts of a program, and 
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a 
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the 
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Note that bindings can only be resolved while the AST remains
	 * in its original unmodified state. Once the AST is modified, all 
	 * <code>resolveBinding</code> methods return <code>null</code>.
	 * If <code>resolveBindings</code> is <code>false</code>, the analysis 
	 * does not go beyond parsing and building the tree, and all 
	 * <code>resolveBinding</code> methods return <code>null</code> from the 
	 * outset.
	 * </p>
	 * 
	 * @param unit the Java model compilation unit whose source code is to be parsed
	 * @param resolveBindings <code>true</code> if bindings are wanted, 
	 *   and <code>false</code> if bindings are not of interest
	 * @return the compilation unit node
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 */
	public static CompilationUnit parseCompilationUnit(
			ICompilationUnit unit,
			boolean resolveBindings) {
				
		if (unit == null) {
			throw new IllegalArgumentException();
		}
		
		char[] source = null;
		try {
			source = unit.getSource().toCharArray();
		} catch(JavaModelException e) {
			// no source, then we cannot build anything
			throw new IllegalArgumentException();
		}

		if (resolveBindings) {
			try {
				CompilationUnitDeclaration compilationUnitDeclaration = CompilationUnitResolver.resolve(
					unit,
					new AbstractSyntaxTreeVisitorAdapter());
				ASTConverter converter = new ASTConverter(true);
				AST ast = new AST();
				BindingResolver resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
				ast.setBindingResolver(resolver);
				converter.setAST(ast);
			
				CompilationUnit cu = converter.convert(compilationUnitDeclaration, source);
				cu.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
				resolver.storeModificationCount(ast.modificationCount());
				return cu;
			} catch(JavaModelException e) {
				/* if a JavaModelException is thrown trying to retrieve the name environment
				 * then we simply do a parsing without creating bindings.
				 * Therefore all binding resolution will return null.
				 */
				return parseCompilationUnit(source);			
			}
		} else {
			return parseCompilationUnit(source);
		}
	}

	/**
	 * Parses the given string as the hypothetical contents of the named
	 * compilation unit and creates and returns a corresponding abstract syntax tree.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST). If a syntax error is detected while
	 * parsing, the relevant node(s) of the tree will be flagged as 
	 * <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If the given project is not <code>null</code>, the various names
	 * and types appearing in the compilation unit can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings 
	 * draw connections between the different parts of a program, and 
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a 
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the 
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Note that bindings can only be resolved while the AST remains
	 * in its original unmodified state. Once the AST is modified, all 
	 * <code>resolveBinding</code> methods return <code>null</code>.
	 * If the given project is <code>null</code>, the analysis 
	 * does not go beyond parsing and building the tree, and all 
	 * <code>resolveBinding</code> methods return <code>null</code> from the 
	 * outset.
	 * </p>
	 * <p>
	 * The name of the compilation unit must be supplied for resolving bindings.
	 * This name should include the ".java" suffix and match the name of the main
	 * (public) class or interface declared in the source. For example, if the source
	 * declares a public class named "Foo", the name of the compilation should be
	 * "Foo.java". For the purposes of resolving bindings, types declared in the
	 * source string hide types by the same name available through the classpath
	 * of the given project.
	 * </p>
	 * 
	 * @param source the string to be parsed as a Java compilation unit
	 * @param unitName the name of the compilation unit that would contain the source
	 *    string, or <code>null</code> if <code>javaProject</code> is also <code>null</code>
	 * @param project the Java project used to resolve names, or 
	 *    <code>null</code> if bindings are not resolved
	 * @return the compilation unit node
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 */
	public static CompilationUnit parseCompilationUnit(
		char[] source,
		String unitName,
		IJavaProject project) {
			
		if (source == null) {
			throw new IllegalArgumentException();
		}
		if (unitName == null && project != null) {
			throw new IllegalArgumentException();
		}
		if (project == null) {
			// this just reuces to the other simplest case
			return parseCompilationUnit(source);
		}
	
		try {
			CompilationUnitDeclaration compilationUnitDeclaration =
				CompilationUnitResolver.resolve(
					source,
					unitName,
					project,
					new AbstractSyntaxTreeVisitorAdapter());
			ASTConverter converter = new ASTConverter(true);
			AST ast = new AST();
			BindingResolver resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
			ast.setBindingResolver(resolver);
			converter.setAST(ast);
		
			CompilationUnit cu = converter.convert(compilationUnitDeclaration, source);
			cu.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
			resolver.storeModificationCount(ast.modificationCount());
			return cu;
		} catch(JavaModelException e) {
			/* if a JavaModelException is thrown trying to retrieve the name environment
			 * then we simply do a parsing without creating bindings.
			 * Therefore all binding resolution will return null.
			 */
			return parseCompilationUnit(source);			
		}
	}
	  	
	/**
	 * Parses the given string as a Java compilation unit and creates and 
	 * returns a corresponding abstract syntax tree.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST). If a syntax error is detected while
	 * parsing, the relevant node(s) of the tree will be flagged as 
	 * <code>MALFORMED</code>.
	 * </p>
	 * 
	 * @param source the string to be parsed as a Java compilation unit
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 */
	public static CompilationUnit parseCompilationUnit(char[] source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilationUnitDeclaration compilationUnitDeclaration = 
			CompilationUnitResolver.parse(source);

		ASTConverter converter = new ASTConverter(false);
		AST ast = new AST();
		ast.setBindingResolver(new BindingResolver());
		converter.setAST(ast);
				
		CompilationUnit cu = converter.convert(compilationUnitDeclaration, source);
		
		// line end table should be extracted from scanner
		cu.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
		return cu;
	}

	/**
	 * The binding resolver for this AST. Initially a binding resolver that
	 * does not resolve names at all.
	 */
	private BindingResolver resolver = new BindingResolver();
	
	/**
	 * Returns the binding resolver for this AST.
	 * 
	 * @return the binding resolver for this AST
	 */
	BindingResolver getBindingResolver() {
		return resolver;
	}

	/** 
	 * Returns the type binding for a "well known" type.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * <p>
	 * The following type names are supported:
	 * <ul>
	 * <li><code>"boolean"</code></li>
	 * <li><code>"char"</code></li>
	 * <li><code>"byte"</code></li>
	 * <li><code>"short"</code></li>
	 * <li><code>"int"</code></li>
	 * <li><code>"long"</code></li>
	 * <li><code>"float"</code></li>
	 * <li><code>"double"</code></li>
	 * <li><code>"void"</code></li>
	 * <li><code>"java.lang.Object"</code></li>
	 * <li><code>"java.lang.String"</code></li>
	 * <li><code>"java.lang.StringBuffer"</code></li>
	 * <li><code>"java.lang.Throwable"</code></li>
	 * <li><code>"java.lang.Exception"</code></li>
	 * <li><code>"java.lang.RuntimeException"</code></li>
	 * <li><code>"java.lang.Error"</code></li>
	 * <li><code>"java.lang.Class"</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @param name the name of a well known type
	 * @return the corresponding type binding, or <code>null</code> if the 
	 *   named type is not considered well known or if no binding can be found
	 *   for it
	 */
	public ITypeBinding resolveWellKnownType(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		return getBindingResolver().resolveWellKnownType(name);
	}
		
	/**
	 * Sets the binding resolver for this AST.
	 * 
	 * @param resolver the new binding resolver for this AST
	 */
	void setBindingResolver(BindingResolver resolver) {
		if (resolver == null) {
			throw new IllegalArgumentException();
		}
		this.resolver = resolver;
	}

	//=============================== NAMES ===========================
	/**
	 * Creates and returns a new unparented simple name node for the given
	 * identifier. The identifier should be a legal Java identifier, but not
	 * a keyword, boolean literal ("true", "false") or null literal ("null").
	 * 
	 * @param identifier the identifier
	 * @return a new unparented simple name node
	 * @exception IllegalArgumentException if the identifier is invalid
	 */
	public SimpleName newSimpleName(String identifier) {
		if (identifier == null) {
			throw new IllegalArgumentException();
		}
		SimpleName result = new SimpleName(this);
		result.setIdentifier(identifier);
		return result;
	}
	
	/**
	 * Creates and returns a new unparented qualified name node for the given 
	 * qualifier and simple name child node.
	 * 
	 * @param qualifier the qualifier name node
	 * @param name the simple name being qualified
	 * @return a new unparented qualified name node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public QualifiedName newQualifiedName(
		Name qualifier,
		SimpleName name) {
		QualifiedName result = new QualifiedName(this);
		result.setQualifier(qualifier);
		result.setName(name);
		return result;
		
	}
	
	/**
	 * Creates and returns a new unparented name node for the given name 
	 * segments. Returns a simple name if there is only one name segment, and
	 * a qualified name if there are multiple name segments. Each of the name
	 * segments should be legal Java identifiers (this constraint may or may 
	 * not be enforced), and there must be at least one name segment.
	 * 
	 * @param identifiers a list of 1 or more name segments, each of which
	 *    is a legal Java identifier
	 * @return a new unparented name node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the identifier is invalid</li>
	 * <li>the list of identifiers is empty</li>
	 * </ul>
	 */
	public Name newName(String[] identifiers) {
		int count = identifiers.length;
		if (count == 0) {
			throw new IllegalArgumentException();
		}
		Name result = newSimpleName(identifiers[0]);
		for (int i = 1; i < count; i++) {
			SimpleName name = newSimpleName(identifiers[i]);
			result = newQualifiedName(result, name);
		}
		return result;
	}

	//=============================== TYPES ===========================
	/**
	 * Creates and returns a new unparented simple type node with the given
	 * type name.
	 * <p>
	 * This method can be used to convert a name (<code>Name</code>) into a
	 * type (<code>Type</code>) by wrapping it.
	 * </p>
	 * 
	 * @param typeName the name of the class or interface
	 * @return a new unparented simple type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public SimpleType newSimpleType(Name typeName) {
		SimpleType result = new SimpleType(this);
		result.setName(typeName);
		return result;
	}

	/**
	 * Creates and returns a new unparented array type node with the given
	 * component type, which may be another array type.
	 * 
	 * @param componentType the component type (possibly another array type)
	 * @return a new unparented array type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public ArrayType newArrayType(Type componentType) {
		ArrayType result = new ArrayType(this);
		result.setComponentType(componentType);
		return result;
	}

	/**
	 * Creates and returns a new unparented array type node with the given
	 * element type and number of dimensions. 
	 * <p>
	 * Note that if the element type passed in is an array type, the
	 * element type of the result will not be the same as what was passed in.
	 * </p>
	 * 
	 * @param elementType the element type (never an array type)
	 * @param dimensions the number of dimensions, a positive number
	 * @return a new unparented array type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public ArrayType newArrayType(Type elementType, int dimensions) {
		if (elementType == null || elementType.isArrayType()) {
			throw new IllegalArgumentException();
		}
		if (dimensions < 1 || dimensions > 1000) {
			// we would blow our stacks anyway with a 1000-D array
			throw new IllegalArgumentException();
		}
		ArrayType result = new ArrayType(this);
		result.setComponentType(elementType);
		for (int i = 2; i <= dimensions; i++) {
			result = newArrayType(result);
		}
		return result;
		
	}

	/**
	 * Creates and returns a new unparented primitive type node with the given
	 * type code.
	 * 
	 * @param typeCode one of the primitive type code constants declared in 
	 *    <code>PrimitiveType</code>
	 * @return a new unparented primitive type node
	 * @exception IllegalArgumentException if the primitive type code is invalid
	 */
	public PrimitiveType newPrimitiveType(PrimitiveType.Code typeCode) {
		PrimitiveType result = new PrimitiveType(this);
		result.setPrimitiveTypeCode(typeCode);
		return result;
	}

	//=============================== DECLARATIONS ===========================
	/**
	 * Creates an unparented compilation unit node owned by this AST.
	 * The compilation unit initially has no package declaration, no
	 * import declarations, and no type declarations.
	 * <p>
	 * Note that the new compilation unit is <b>not</b> automatically
	 * made the root node of this AST. This must be done explicitly
	 * by calling <code>setRoot</code>.
	 * </p>
	 * 
	 * @return the new unparented compilation unit node
	 */
	public CompilationUnit newCompilationUnit() {
		return new CompilationUnit(this);
	}
	
	/**
	 * Creates an unparented package declaration node owned by this AST.
	 * The package declaration initially declares a package with an
	 * unspecified name.
	 * 
	 * @return the new unparented package declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public PackageDeclaration newPackageDeclaration() {
		PackageDeclaration result = new PackageDeclaration(this);
		return result;
	}
	
	/**
	 * Creates an unparented import declaration node owned by this AST.
	 * The import declaration initially contains a single-type import
	 * of a type with an unspecified name.
	 * 
	 * @return the new unparented import declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public ImportDeclaration newImportDeclaration() {
		ImportDeclaration result = new ImportDeclaration(this);
		return result;
	}
	
	/**
	 * Creates an unparented class declaration node owned by this AST.
	 * The name of the class is an unspecified, but legal, name; 
	 * no modifiers; no Javadoc comment; no superclass or superinterfaces; 
	 * and an empty class body.
	 * <p>
	 * To create an interface, use this method and then call
	 * <code>TypeDeclaration.setInterface(true)</code> and
	 * </p>
	 * 
	 * @return a new unparented type declaration node
	 */
	public TypeDeclaration newTypeDeclaration() {
		TypeDeclaration result = new TypeDeclaration(this);
		result.setInterface(false);
		return result;
	}
	
	/**
	 * Creates an unparented method declaration node owned by this AST.
	 * By default, the declaration is for a method of an unspecified, but 
	 * legal, name; no modifiers; no Javadoc comment; no parameters; return
	 * type void; no thrown exceptions; and no body (as opposed to an empty
	 * body).
	 * <p>
	 * To create a constructor, use this method and then call
	 * <code>MethodDeclaration.setConstructor(true)</code> and
	 * <code>MethodDeclaration.setName(className)</code>.
	 * </p>
	 * 
	 * @return a new unparented method declaration node
	 */
	public MethodDeclaration newMethodDeclaration() {
		MethodDeclaration result = new MethodDeclaration(this);
		result.setConstructor(false);
		return result;
	}
	
	/**
	 * Creates an unparented single variable declaration node owned by this AST.
	 * By default, the declaration is for a variable with an unspecified, but 
	 * legal, name and type; no modifiers; and no initializer.
	 * 
	 * @return a new unparented single variable declaration node
	 */
	public SingleVariableDeclaration newSingleVariableDeclaration() {
		SingleVariableDeclaration result = new SingleVariableDeclaration(this);
		return result;
	}
	
	/**
	 * Creates an unparented variable declaration fragment node owned by this 
	 * AST. By default, the fragment is for a variable with an unspecified, but 
	 * legal, name; no extra array dimensions; and no initializer.
	 * 
	 * @return a new unparented variable declaration fragment node
	 */
	public VariableDeclarationFragment newVariableDeclarationFragment() {
		VariableDeclarationFragment result = new VariableDeclarationFragment(this);
		return result;
	}
	
	/**
	 * Creates an unparented initializer node owned by this AST, with an 
	 * empty block. By default, the initializer has no modifiers and 
	 * an empty block.
	 * 
	 * @return a new unparented initializer node
	 */
	public Initializer newInitializer() {
		Initializer result = new Initializer(this);
		return result;
	}

	/**
	 * Creates and returns a new Javadoc comment node.
	 * Initially the new node has an unspecified, but legal, Javadoc comment.
	 * 
	 * @return a new unparented Javadoc comment node
	 */
	public Javadoc newJavadoc() {
		Javadoc result = new Javadoc(this);
		return result;
	}
	
	//=============================== STATEMENTS ===========================
	/**
	 * Creates a new unparented local variable declaration statement node 
	 * owned by this AST, for the given variable declaration fragment. 
	 * By default, there are no modifiers and the base type is unspecified
	 * (but legal).
	 * <p>
	 * This method can be used to convert a variable declaration fragment
	 * (<code>VariableDeclarationFragment</code>) into a statement
	 * (<code>Statement</code>) by wrapping it. Additional variable
	 * declaration fragments can be added afterwards.
	 * </p>
	 * 
	 * @param fragment the variable declaration fragment
	 * @return a new unparented variable declaration statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public VariableDeclarationStatement
			newVariableDeclarationStatement(VariableDeclarationFragment fragment) {
		if (fragment == null) {
			throw new IllegalArgumentException();
		}
		VariableDeclarationStatement result =
			new VariableDeclarationStatement(this);
		result.fragments().add(fragment);
		return result;
	}
	
	/**
	 * Creates a new unparented local type declaration statement node 
	 * owned by this AST, for the given type declaration.
	 * <p>
	 * This method can be used to convert a type declaration
	 * (<code>TypeDeclaration</code>) into a statement
	 * (<code>Statement</code>) by wrapping it.
	 * </p>
	 * 
	 * @param decl the type declaration
	 * @return a new unparented local type declaration statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public TypeDeclarationStatement 
			newTypeDeclarationStatement(TypeDeclaration decl) {
		TypeDeclarationStatement result = new TypeDeclarationStatement(this);
		result.setTypeDeclaration(decl);
		return result;
	}
	
	/**
	 * Creates an unparented block node owned by this AST, for an empty list 
	 * of statements.
	 * 
	 * @return a new unparented, empty block node
	 */
	public Block newBlock() {
		return new Block(this);
	}
	
	/**
	 * Creates an unparented continue statement node owned by this AST.
	 * The continue statement has no label.
	 * 
	 * @return a new unparented continue statement node
	 */
	public ContinueStatement newContinueStatement() {
		return new ContinueStatement(this);
	}
	
	/**
	 * Creates an unparented break statement node owned by this AST.
	 * The break statement has no label.
	 * 
	 * @return a new unparented break statement node
	 */
	public BreakStatement newBreakStatement() {
		return new BreakStatement(this);
	}
	
	/**
	 * Creates a new unparented expression statement node owned by this AST,
	 * for the given expression.
	 * <p>
	 * This method can be used to convert an expression 
	 * (<code>Expression</code>) into a statement (<code>Type</code>) 
	 * by wrapping it. Note, however, that the result is only legal for 
	 * limited expression types, including method invocations, assignments,
	 * and increment/decrement operations.
	 * </p>
	 * 
	 * @param expression the expression
	 * @return a new unparented statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public ExpressionStatement newExpressionStatement(Expression expression) {
		ExpressionStatement result = new ExpressionStatement(this);
		result.setExpression(expression);
		return result;
	}
	
	/**
	 * Creates a new unparented if statement node owned by this AST.
	 * By default, the expression is unspecified (but legal), 
	 * the then statement is an empty block, and there is no else statement.
	 * 
	 * @return a new unparented if statement node
	 */
	public IfStatement newIfStatement() {
		return new IfStatement(this);
	}

	/**
	 * Creates a new unparented while statement node owned by this AST.
	 * By default, the expression is unspecified (but legal), and
	 * the body statement is an empty block.
	 * 
	 * @return a new unparented while statement node
	 */
	public WhileStatement newWhileStatement() {
		return new WhileStatement(this);
	}

	/**
	 * Creates a new unparented do statement node owned by this AST.
	 * By default, the expression is unspecified (but legal), and
	 * the body statement is an empty block.
	 * 
	 * @return a new unparented do statement node
	 */
	public DoStatement newDoStatement() {
		return new DoStatement(this);
	}

	/**
	 * Creates a new unparented try statement node owned by this AST.
	 * By default, the try statement has an empty block, no catch
	 * clauses, and no finally block.
	 * 
	 * @return a new unparented try statement node
	 */
	public TryStatement newTryStatement() {
		return new TryStatement(this);
	}

	/**
	 * Creates a new unparented catch clause node owned by this AST.
	 * By default, the catch clause declares an unspecified, but legal, 
	 * exception declaration and has an empty block.
	 * 
	 * @return a new unparented catch clause node
	 */
	public CatchClause newCatchClause() {
		return new CatchClause(this);
	}

	/**
	 * Creates a new unparented return statement node owned by this AST.
	 * By default, the return statement has no expression.
	 * 
	 * @return a new unparented return statement node
	 */
	public ReturnStatement newReturnStatement() {
		return new ReturnStatement(this);
	}

	/**
	 * Creates a new unparented throw statement node owned by this AST.
	 * By default, the expression is unspecified, but legal.
	 * 
	 * @return a new unparented throw statement node
	 */
	public ThrowStatement newThrowStatement() {
		return new ThrowStatement(this);
	}

	/**
	 * Creates a new unparented assert statement node owned by this AST.
	 * By default, the first expression is unspecified, but legal, and has no
	 * message expression.
	 * 
	 * @return a new unparented assert statement node
	 */
	public AssertStatement newAssertStatement() {
		return new AssertStatement(this);
	}

	/**
	 * Creates a new unparented empty statement node owned by this AST.
	 * 
	 * @return a new unparented empty statement node
	 */
	public EmptyStatement newEmptyStatement() {
		return new EmptyStatement(this);
	}

	/**
	 * Creates a new unparented labeled statement node owned by this AST.
	 * By default, the label and statement are both unspecified, but legal.
	 * 
	 * @return a new unparented labeled statement node
	 */
	public LabeledStatement newLabeledStatement() {
		return new LabeledStatement(this);
	}

	/**
	 * Creates a new unparented switch statement node owned by this AST.
	 * By default, the expression is unspecified, but legal, and there are
	 * no statements or switch cases.
	 * 
	 * @return a new unparented labeled statement node
	 */
	public SwitchStatement newSwitchStatement() {
		return new SwitchStatement(this);
	}

	/**
	 * Creates a new unparented switch case statement node owned by 
	 * this AST. By default, the expression is unspecified, but legal.
	 * 
	 * @return a new unparented switch case node
	 */
	public SwitchCase newSwitchCase() {
		return new SwitchCase(this);
	}

	/**
	 * Creates a new unparented synchronized statement node owned by this AST.
	 * By default, the expression is unspecified, but legal, and the body is
	 * an empty block.
	 * 
	 * @return a new unparented synchronized statement node
	 */
	public SynchronizedStatement newSynchronizedStatement() {
		return new SynchronizedStatement(this);
	}

	/**
	 * Creates a new unparented for statement node owned by this AST.
	 * By default, there are no initializers, no condition expression, 
	 * no updaters, and the body is an empty block.
	 * 
	 * @return a new unparented throw statement node
	 */
	public ForStatement newForStatement() {
		return new ForStatement(this);
	}

	//=============================== EXPRESSIONS ===========================
	/**
	 * Creates and returns a new unparented string literal node for 
	 * the empty string literal.
	 * 
	 * @return a new unparented string literal node
	 */
	public StringLiteral newStringLiteral() {
		return new StringLiteral(this);
	}
	

	/**
	 * Creates and returns a new unparented character literal node.
	 * Initially the node has an unspecified character literal.
	 * 
	 * @return a new unparented character literal node
	 */
	public CharacterLiteral newCharacterLiteral() {
		return new CharacterLiteral(this);
	}

	/**
	 * Creates and returns a new unparented number literal node.
	 * 
	 * @param literal the token for the numeric literal as it would 
	 *    appear in Java source code
	 * @return a new unparented number literal node
	 */
	public NumberLiteral newNumberLiteral(String literal) {
		if (literal == null) {
			throw new IllegalArgumentException();
		}
		NumberLiteral result = new NumberLiteral(this);
		result.setToken(literal);
		return result;
	}
	
	/**
	 * Creates and returns a new unparented number literal node.
	 * Initially the number literal token is <code>"0"</code>.
	 * 
	 * @return a new unparented number literal node
	 */
	public NumberLiteral newNumberLiteral() {
		NumberLiteral result = new NumberLiteral(this);
		return result;
	}
	
	/**
	 * Creates and returns a new unparented null literal node.
	 * 
	 * @return a new unparented null literal node
	 */
	public NullLiteral newNullLiteral() {
		return new NullLiteral(this);
	}
	
	/**
	 * Creates and returns a new unparented boolean literal node.
	 * <p>
	 * For example, the assignment expression <code>foo = true</code>
	 * is generated by the following snippet:
	 * <code>
	 * <pre>
	 * Assignment e= ast.newAssignment();
	 * e.setLeftHandSide(ast.newSimpleName("foo"));
	 * e.setRightHandSide(ast.newBooleanLiteral(true));
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param value the boolean value
	 * @return a new unparented boolean literal node
	 */
	public BooleanLiteral newBooleanLiteral(boolean value) {
		BooleanLiteral result = new BooleanLiteral(this);
		result.setBooleanValue(value);
		return result;
	}
	
	/**
	 * Creates and returns a new unparented assignment expression node 
	 * owned by this AST. By default, the assignment operator is "=" and
	 * the left and right hand side expressions are unspecified, but 
	 * legal, names.
	 * 
	 * @return a new unparented assignment expression node
	 */
	public Assignment newAssignment() {
		Assignment result = new Assignment(this);
		return result;
	}
	
	/**
	 * Creates an unparented method invocation expression node owned by this 
	 * AST. By default, the name of the method is unspecified (but legal) 
	 * there is no receiver expression, and the list of arguments is empty.
	 * 
	 * @return a new unparented method invocation expression node
	 */
	public MethodInvocation newMethodInvocation() {
		MethodInvocation result = new MethodInvocation(this);
		return result;
	}
	
	/**
	 * Creates an unparented "super" method invocation expression node owned by 
	 * this AST. By default, the name of the method is unspecified (but legal) 
	 * there is no qualifier, and the list of arguments is empty.
	 * 
	 * @return a new unparented  "super" method invocation 
	 *    expression node
	 */
	public SuperMethodInvocation newSuperMethodInvocation() {
		SuperMethodInvocation result = new SuperMethodInvocation(this);
		return result;
	}
	
	/**
	 * Creates an unparented alternate constructor ("this(...);") invocation 
	 * statement node owned by this AST. By default, the list of arguments
	 * is empty.
	 * <p>
	 * Note that this type of node is a Statement, whereas a regular
	 * method invocation is an Expression. The only valid use of these 
	 * statements are as the first statement of a constructor body.
	 * </p>
	 * 
	 * @return a new unparented alternate constructor invocation statement node
	 */
	public ConstructorInvocation newConstructorInvocation() {
		ConstructorInvocation result = new ConstructorInvocation(this);
		return result;
	}
	
	/**
	 * Creates an unparented alternate super constructor ("super(...);") 
	 * invocation statement node owned by this AST. By default, there is no
	 * qualifier and the list of arguments is empty.
	 * <p>
	 * Note that this type of node is a Statement, whereas a regular
	 * super method invocation is an Expression. The only valid use of these 
	 * statements are as the first statement of a constructor body.
	 * </p>
	 * 
	 * @return a new unparented super constructor invocation statement node
	 */
	public SuperConstructorInvocation newSuperConstructorInvocation() {
		SuperConstructorInvocation result =
			new SuperConstructorInvocation(this);
		return result;
	}
		
	/**
	 * Creates a new unparented local variable declaration expression node 
	 * owned by this AST, for the given variable declaration fragment. By 
	 * default, there are no modifiers and the base type is unspecified
	 * (but legal).
	 * <p>
	 * This method can be used to convert a variable declaration fragment
	 * (<code>VariableDeclarationFragment</code>) into an expression
	 * (<code>Expression</code>) by wrapping it. Additional variable
	 * declaration fragments can be added afterwards.
	 * </p>
	 * 
	 * @param fragment the first variable declaration fragment
	 * @return a new unparented variable declaration expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public VariableDeclarationExpression
			newVariableDeclarationExpression(VariableDeclarationFragment fragment) {
		if (fragment == null) {
			throw new IllegalArgumentException();
		}
		VariableDeclarationExpression result =
			new VariableDeclarationExpression(this);
		result.fragments().add(fragment);
		return result;
	}
	
	/**
	 * Creates a new unparented field declaration node owned by this AST, 
	 * for the given variable declaration fragment. By default, there are no
	 * modifiers, no javadoc comment, and the base type is unspecified 
	 * (but legal).
	 * <p>
	 * This method can be used to wrap a variable declaration fragment
	 * (<code>VariableDeclarationFragment</code>) into a field declaration
	 * suitable for inclusion in the body of a type declaration
	 * (<code>FieldDeclaration</code> implements <code>BodyDeclaration</code>).
	 * Additional variable declaration fragments can be added afterwards.
	 * </p>
	 * 
	 * @param fragment the variable declaration fragment
	 * @return a new unparented field declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public FieldDeclaration newFieldDeclaration(VariableDeclarationFragment fragment) {
		if (fragment == null) {
			throw new IllegalArgumentException();
		}
		FieldDeclaration result = new FieldDeclaration(this);
		result.fragments().add(fragment);
		return result;
	}
	
	/**
	 * Creates and returns a new unparented "this" expression node 
	 * owned by this AST. By default, there is no qualifier.
	 * 
	 * @return a new unparented "this" expression node
	 */
	public ThisExpression newThisExpression() {
		ThisExpression result = new ThisExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented field access expression node 
	 * owned by this AST. By default, the expression and field are both
	 * unspecified, but legal, names.
	 * 
	 * @return a new unparented field access expression node
	 */
	public FieldAccess newFieldAccess() {
		FieldAccess result = new FieldAccess(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented super field access expression node 
	 * owned by this AST. By default, the expression and field are both
	 * unspecified, but legal, names.
	 * 
	 * @return a new unparented super field access expression node
	 */
	public SuperFieldAccess newSuperFieldAccess() {
		SuperFieldAccess result = new SuperFieldAccess(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented type literal expression node 
	 * owned by this AST. By default, the type is unspecified (but legal).
	 * 
	 * @return a new unparented type literal node
	 */
	public TypeLiteral newTypeLiteral() {
		TypeLiteral result = new TypeLiteral(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented cast expression node 
	 * owned by this AST. By default, the type and expression are unspecified
	 * (but legal).
	 * 
	 * @return a new unparented cast expression node
	 */
	public CastExpression newCastExpression() {
		CastExpression result = new CastExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented parenthesized expression node 
	 * owned by this AST. By default, the expression is unspecified (but legal).
	 * 
	 * @return a new unparented parenthesized expression node
	 */
	public ParenthesizedExpression newParenthesizedExpression() {
		ParenthesizedExpression result = new ParenthesizedExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented infix expression node 
	 * owned by this AST. By default, the operator and left and right
	 * operand are unspecified (but legal), and there are no extended
	 * operands.
	 * 
	 * @return a new unparented infix expression node
	 */
	public InfixExpression newInfixExpression() {
		InfixExpression result = new InfixExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented instanceof expression node 
	 * owned by this AST. By default, the operator and left and right
	 * operand are unspecified (but legal).
	 * 
	 * @return a new unparented instanceof expression node
	 */
	public InstanceofExpression newInstanceofExpression() {
		InstanceofExpression result = new InstanceofExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented postfix expression node 
	 * owned by this AST. By default, the operator and operand are 
	 * unspecified (but legal).
	 * 
	 * @return a new unparented postfix expression node
	 */
	public PostfixExpression newPostfixExpression() {
		PostfixExpression result = new PostfixExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented prefix expression node 
	 * owned by this AST. By default, the operator and operand are 
	 * unspecified (but legal).
	 * 
	 * @return a new unparented prefix expression node
	 */
	public PrefixExpression newPrefixExpression() {
		PrefixExpression result = new PrefixExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented array access expression node 
	 * owned by this AST. By default, the array and index expression are 
	 * both unspecified (but legal).
	 * 
	 * @return a new unparented array access expression node
	 */
	public ArrayAccess newArrayAccess() {
		ArrayAccess result = new ArrayAccess(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented array creation expression node 
	 * owned by this AST. By default, the array type is an unspecified
	 * 1-dimensional array, the list of dimensions is empty, and there is no
	 * array initializer.
	 * <p>
	 * Examples:
	 * <code>
	 * <pre>
	 * 	// new String[len]
	 * ArrayCreation ac1 = ast.newArrayCreation();
	 * ac1.setType(
	 *    ast.newArrayType(
	 *       ast.newSimpleType(ast.newSimpleName("String"))));
	 * ac1.dimensions().add(ast.newSimpleName("len"));

	 * 	// new double[7][24][]
	 * ArrayCreation ac2 = ast.newArrayCreation();
	 * ac2.setType(
	 *    ast.newArrayType(
	 *       ast.newPrimitiveType(PrimitiveType.DOUBLE), 3));
	 * ac2.dimensions().add(ast.newNumberLiteral("7"));
	 * ac2.dimensions().add(ast.newNumberLiteral("24"));
	 *
	 * // new int[] {1, 2}
	 * ArrayCreation ac3 = ast.newArrayCreation();
	 * ac3.setType(
	 *    ast.newArrayType(
	 *       ast.newPrimitiveType(PrimitiveType.INT)));
	 * ArrayInitializer ai = ast.newArrayInitializer();
	 * ac3.setInitializer(ai);
	 * ai.expressions().add(ast.newNumberLiteral("1"));
	 * ai.expressions().add(ast.newNumberLiteral("2"));
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @return a new unparented array creation expression node
	 */
	public ArrayCreation newArrayCreation() {
		ArrayCreation result = new ArrayCreation(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented class instance creation 
	 * ("new") expression node owned by this AST. By default, 
	 * there is no qualifying expression, an unspecified (but legal) type name,
	 * an empty list of arguments, and does not declare an anonymous
	 * class declaration.
	 * 
	 * @return a new unparented class instance creation expression node
	 */
	public ClassInstanceCreation newClassInstanceCreation() {
		ClassInstanceCreation result = new ClassInstanceCreation(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented anonymous class declaration
	 * node owned by this AST. By default, the body declaration list is empty.
	 * 
	 * @return a new unparented anonymous class declaration node
	 */
	public AnonymousClassDeclaration newAnonymousClassDeclaration() {
		AnonymousClassDeclaration result = new AnonymousClassDeclaration(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented array initializer node 
	 * owned by this AST. By default, the initializer has no expressions.
	 * 
	 * @return a new unparented array initializer node
	 */
	public ArrayInitializer newArrayInitializer() {
		ArrayInitializer result = new ArrayInitializer(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented conditional expression node 
	 * owned by this AST. By default, the condition and both expressions
	 * are unspecified (but legal).
	 * 
	 * @return a new unparented array conditional expression node
	 */
	public ConditionalExpression newConditionalExpression() {
		ConditionalExpression result = new ConditionalExpression(this);
		return result;
	}
}

