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
package org.eclipse.jdt.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents either a source type in a compilation unit (either a top-level
 * type or a member type) or a binary type in a class file.
 * <p>
 * If a binary type cannot be parsed, its structure remains unknown.
 * Use <code>IJavaElement.isStructureKnown</code> to determine whether this
 * is the case.
 * </p>
 * <p>
 * The children are of type <code>IMember</code>, which includes <code>IField</code>,
 * <code>IMethod</code>, <code>IInitializer</code> and <code>IType</code>.
 * The children are listed in the order in which they appear in the source or class file.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IType extends IMember, IParent {
	/**
	 * Do code completion inside a code snippet in the context of the current type.
	 * 
	 * If the type can access to his source code and the insertion position is valid,
	 * then completion is performed against source. Otherwise the completion is performed
	 * against type structure and given locals variables.
	 * 
	 * @param snippet the code snippet
	 * @param insertion the position with in source where the snippet
	 * is inserted. This position must not be in comments.
	 * A possible value is -1, if the position is not known.
	 * @param position the position with in snippet where the user 
	 * is performing code assist.
	 * @param localVariableTypesNames an array (possibly empty) of fully qualified 
	 * type names of local variables visible at the current scope
	 * @param localVariableNames an array (possibly empty) of local variable names 
	 * that are visible at the current scope
	 * @param localVariableModifiers an array (possible empty) of modifiers for 
	 * local variables
	 * @param isStatic whether the current scope is in a static context
	 * @param requestor the completion requestor
	 * @since 2.0
	 */
	void codeComplete(
		char[] snippet,
		int insertion,
		int position,
		char[][] localVariableTypeNames,
		char[][] localVariableNames,
		int[] localVariableModifiers,
		boolean isStatic,
		ICompletionRequestor requestor)
		throws JavaModelException;

	/**
	 * Creates and returns a field in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the element will be inserted
	 * as the last field declaration in this type.</p>
	 *
	 * <p>It is possible that a field with the same name already exists in this type.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the field is created with the new contents</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 * </ul></p>
	 *
	 * @param contents the given contents
	 * @param sibling the given sibling
	 * @param force a flag in case the same name already exists in this type
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as a field declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * <li> There was a naming collision with an existing field (NAME_COLLISION)
	 * </ul>
	 * @return a field in this type with the given contents
	 */
	IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor)
		throws JavaModelException;
		
	/**
	 * Creates and returns a static initializer in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the new initializer is positioned
	 * after the last existing initializer declaration, or as the first member
	 * in the type if there are no initializers.</p>
	 *
	 * @param contents the given contents
	 * @param sibling the given sibling
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This element does not exist
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as an initializer declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * </ul>
	 * @return a static initializer in this type with the given contents
	 */
	IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor)
		throws JavaModelException;
		
	/**
	 * Creates and returns a method or constructor in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the element will be appended
	 * to this type.
	 *
	 * <p>It is possible that a method with the same signature already exists in this type.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the method is created with the new contents</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 * </ul></p>
	 *
	 * @param contents the given contents
	 * @param sibling the given sibling
	 * @param force a flag in case the same name already exists in this type
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as a method or constructor
	 *		declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * <li> There was a naming collision with an existing method (NAME_COLLISION)
	 * </ul>
	 * @return a method or constructor in this type with the given contents
	 */
	IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor)
		throws JavaModelException;
		
	/**
	 * Creates and returns a type in this type with the
	 * given contents.
	 * <p>
	 * Optionally, the new type can be positioned before the specified
	 * sibling. If no sibling is specified, the type will be appended
	 * to this type.
	 *
	 * <p>It is possible that a type with the same name already exists in this type.
	 * The value of the <code>force</code> parameter effects the resolution of
	 * such a conflict:<ul>
	 * <li> <code>true</code> - in this case the type is created with the new contents</li>
	 * <li> <code>false</code> - in this case a <code>JavaModelException</code> is thrown</li>
	 * </ul></p>
	 *
	 * @param contents the given contents
	 * @param sibling the given sibling
	 * @param force a flag in case the same name already exists in this type
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this type (INVALID_SIBLING)
	 * <li> The contents could not be recognized as a type declaration (INVALID_CONTENTS)
	 * <li> This type is read-only (binary) (READ_ONLY)
	 * <li> There was a naming collision with an existing field (NAME_COLLISION)
	 * </ul>
	 * @return a type in this type with the given contents
	 */
	IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor)
		throws JavaModelException;
		
	/** 
	 * Finds the methods in this type that correspond to
	 * the given method.
	 * A method m1 corresponds to another method m2 if:
	 * <ul>
	 * <li>m1 has the same element name as m2.
	 * <li>m1 has the same number of arguments as m2 and
	 *     the simple names of the argument types must be equals.
	 * <li>m1 exists.
	 * </ul>
	 * @param method the given method
	 * @return the found method or <code>null</code> if no such methods can be found.
	 * 
	 * @since 2.0 
	 */
	IMethod[] findMethods(IMethod method);
	
	/**
	 * Returns the simple name of this type, unqualified by package or enclosing type.
	 * This is a handle-only method.
	 * 
	 * @return the simple name of this type
	 */
	String getElementName();
	
	/**
	 * Returns the field with the specified name
	 * in this type (for example, <code>"bar"</code>).
	 * This is a handle-only method.  The field may or may not exist.
	 * 
	 * @param name the given name
	 * @return the field with the specified name in this type
	 */
	IField getField(String name);
	
	/**
	 * Returns the fields declared by this type.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source, otherwise, the results are
	 * in no particular order.  For binary types, this includes synthetic fields.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the fields declared by this type
	 */
	IField[] getFields() throws JavaModelException;
	
	/**
	 * Returns the fully qualified name of this type, 
	 * including qualification for any containing types and packages.
	 * This is the name of the package, followed by <code>'.'</code>,
	 * followed by the type-qualified name.
	 * This is a handle-only method.
	 *
	 * @see IType#getTypeQualifiedName()
	 * @return the fully qualified name of this type
	 */
	String getFullyQualifiedName();
	
	/**
	 * Returns the fully qualified name of this type, 
	 * including qualification for any containing types and packages.
	 * This is the name of the package, followed by <code>'.'</code>,
	 * followed by the type-qualified name using the <code>enclosingTypeSeparator</code>.
	 * 
	 * For example:
	 * <ul>
	 * <li>the fully qualified name of a class B defined as a member of a class A in a compilation unit A.java 
	 *     in a package x.y using the '.' separator is "x.y.A.B"</li>
	 * <li>the fully qualified name of a class B defined as a member of a class A in a compilation unit A.java 
	 *     in a package x.y using the '$' separator is "x.y.A$B"</li>
	 * <li>the fully qualified name of a binary type whose class file is x/y/A$B.class
	 *     using the '.' separator is "x.y.A.B"</li>
	 * <li>the fully qualified name of a binary type whose class file is x/y/A$B.class
	 *     using the '$' separator is "x.y.A$B"</li>
	 * <li>the fully qualified name of an anonymous binary type whose class file is x/y/A$1.class
	 *     using the '.' separator is "x.y.A$1"</li>
	 * </ul>
	 * 
	 * This is a handle-only method.
	 *
	 * @param enclosingTypeSeparator the given enclosing type separator
	 * @return the fully qualified name of this type, including qualification for any containing types and packages
	 * @see IType#getTypeQualifiedName(char)
	 * @since 2.0
	 */
	String getFullyQualifiedName(char enclosingTypeSeparator);
	
	/**
	 * Returns the initializer with the specified position relative to
	 * the order they are defined in the source.
	 * Numbering starts at 1 (i.e. the first occurrence is occurrence 1, not occurrence 0).
	 * This is a handle-only method.  The initializer may or may not be present.
	 * 
	 * @param occurrenceCount the specified position
	 * @return the initializer with the specified position relative to the order they are defined in the source
	 */
	IInitializer getInitializer(int occurrenceCount);
	
	/**
	 * Returns the initializers declared by this type.
	 * For binary types this is an empty collection.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the initializers declared by this type
	 */
	IInitializer[] getInitializers() throws JavaModelException;
	
	/**
	 * Returns the method with the specified name and parameter types
	 * in this type (for example, <code>"foo", {"I", "QString;"}</code>). To get the
	 * handle for a constructor, the name specified must be the simple
	 * name of the enclosing type.
	 * This is a handle-only method.  The method may or may not be present.
	 * 
	 * @param name the given name
	 * @param parameterTypeSignatures the given parameter types
	 * @return the method with the specified name and parameter types in this type
	 */
	IMethod getMethod(String name, String[] parameterTypeSignatures);
	
	/**
	 * Returns the methods and constructors declared by this type.
	 * For binary types, this may include the special <code>&lt;clinit&gt</code>; method 
	 * and synthetic methods.
	 * If this is a source type, the results are listed in the order
	 * in which they appear in the source, otherwise, the results are
	 * in no particular order.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the methods and constructors declared by this type
	 */
	IMethod[] getMethods() throws JavaModelException;
	
	/**
	 * Returns the package fragment in which this element is defined.
	 * This is a handle-only method.
	 * 
	 * @return the package fragment in which this element is defined
	 */
	IPackageFragment getPackageFragment();
	
	/**
	 * Returns the name of this type's superclass, or <code>null</code>
	 * for source types that do not specify a superclass.
	 * For interfaces, the superclass name is always <code>"java.lang.Object"</code>.
	 * For source types, the name as declared is returned, for binary types,
	 * the resolved, qualified name is returned.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the name of this type's superclass, or <code>null</code> for source types that do not specify a superclass
	 */
	String getSuperclassName() throws JavaModelException;
	
	/**
	 * Returns the names of interfaces that this type implements or extends,
	 * in the order in which they are listed in the source.
	 * For classes, this gives the interfaces that this class implements.
	 * For interfaces, this gives the interfaces that this interface extends.
	 * An empty collection is returned if this type does not implement or
	 * extend any interfaces. For source types, simples name are returned,
	 * for binary types, qualified names are returned.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return  the names of interfaces that this type implements or extends, in the order in which they are listed in the source, 
	 * an empty collection if none
	 */
	String[] getSuperInterfaceNames() throws JavaModelException;
	
	/**
	 * Returns the member type declared in this type with the given simple name.
	 * This is a handle-only method. The type may or may not exist.
	 * 
	 * @param the given simple name
	 * @return the member type declared in this type with the given simple name
	 */
	IType getType(String name);
	
	/**
	 * Returns the type-qualified name of this type, 
	 * including qualification for any enclosing types,
	 * but not including package qualification.
	 * For source types, this consists of the simple names of
	 * any enclosing types, separated by <code>"$"</code>, followed by the simple name of this type.
	 * For binary types, this is the name of the class file without the ".class" suffix.
	 * This is a handle-only method.
	 * 
	 * @return the type-qualified name of this type
	 */
	String getTypeQualifiedName();
	
	/**
	 * Returns the type-qualified name of this type, 
	 * including qualification for any enclosing types,
	 * but not including package qualification.
	 * This consists of the simple names of any enclosing types, 
	 * separated by the <code>enclosingTypeSeparator</code>, 
	 * followed by the simple name of this type.
	 * 
	 * For example:
	 * <ul>
	 * <li>the type qualified name of a class B defined as a member of a class A
	 *     using the '.' separator is "A.B"</li>
	 * <li>the type qualified name of a class B defined as a member of a class A
	 *     using the '$' separator is "A$B"</li>
	 * <li>the type qualified name of a binary type whose class file is A$B.class
	 *     using the '.' separator is "A.B"</li>
	 * <li>the type qualified name of a binary type whose class file is A$B.class
	 *     using the '$' separator is "A$B"</li>
	 * <li>the type qualified name of an anonymous binary type whose class file is A$1.class
	 *     using the '.' separator is "A$1"</li>
	 * </ul>
	 *
	 * This is a handle-only method.
	 * 
	 * @param enclosingTypeSeparator the specified enclosing type separator
	 * @return the type-qualified name of this type
	 * @since 2.0
	 */
	String getTypeQualifiedName(char enclosingTypeSeparator);
	
	/**
	 * Returns the immediate member types declared by this type.
	 * The results are listed in the order in which they appear in the source or class file.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the immediate member types declared by this type
	 */
	IType[] getTypes() throws JavaModelException;
	
	/**
	 * Returns whether this type represents an anonymous type.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return true if this type represents an anonymous type, false otherwise
	 * @since 2.0
	 */
	boolean isAnonymous() throws JavaModelException;

	/**
	 * Returns whether this type represents a class.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return true if this type represents a class, false otherwise
	 */
	boolean isClass() throws JavaModelException;
	
	/**
	 * Returns whether this type represents an interface.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return true if this type represents an interface, false otherwise
	 */
	boolean isInterface() throws JavaModelException;
	
	/**
	 * Returns whether this type represents a local type.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return true if this type represents a local type, false otherwise
	 * @since 2.0
	 */
	boolean isLocal() throws JavaModelException;

	/**
	 * Returns whether this type represents a member type.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return true if this type represents a member type, false otherwise
	 * @since 2.0
	 */
	boolean isMember() throws JavaModelException;

	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type and all of its supertypes.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing this type and all of its supertypes
	 */
	ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws JavaModelException;
	
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type and all of its supertypes, considering types in the given 
	 * working copies. In other words, the list of working copies will take 
	 * precedence over their original compilation units in the workspace.
	 * <p>
	 * Note that passing an empty working copy will be as if the original compilation
	 * unit had been deleted.
	 *
	 * @param workingCopies the working copies that take precedence over their original compilation units
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing this type and all of its supertypes
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @since 2.0
	 */
	ITypeHierarchy newSupertypeHierarchy(IWorkingCopy[] workingCopies, IProgressMonitor monitor)
		throws JavaModelException;
		
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace
	 */
	ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException;
	
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace, 
	 * considering types in the given working copies. In other words, the list of working 
	 * copies that will take precedence over their original compilation units in the workspace.
	 * <p>
	 * Note that passing an empty working copy will be as if the original compilation
	 * unit had been deleted.
	 *
	 * @param workingCopies the working copies that take precedence over their original compilation units
	 * @param monitor the given progress monitor
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes in the workspace
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @since 2.0
	 */
	ITypeHierarchy newTypeHierarchy(IWorkingCopy[] workingCopies, IProgressMonitor monitor) throws JavaModelException;
	
	/**
	 * Creates and returns a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes 
	 * in the context of the given project.
	 *
	 * @param project the given project
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return a type hierarchy for this type containing
	 * this type, all of its supertypes, and all its subtypes 
	 * in the context of the given project
	 */
	ITypeHierarchy newTypeHierarchy(IJavaProject project, IProgressMonitor monitor) throws JavaModelException;
	
	/**
	 * Resolves the given type name within the context of this type (depending on the type hierarchy 
	 * and its imports). Multiple answers might be found in case there are ambiguous matches.
	 *
	 * Each matching type name is decomposed as an array of two strings, the first denoting the package
	 * name (dot-separated) and the second being the type name.
	 * Returns <code>null</code> if unable to find any matching type.
	 *
	 * For example, resolution of <code>"Object"</code> would typically return
	 * <code>{{"java.lang", "Object"}}</code>.
	 * 
	 * @param typeName the given type name
	 * @exception JavaModelException if code resolve could not be performed. 
	 * @return the resolved type names or <code>null</code> if unable to find any matching type
	 */
	String[][] resolveType(String typeName) throws JavaModelException;
}
