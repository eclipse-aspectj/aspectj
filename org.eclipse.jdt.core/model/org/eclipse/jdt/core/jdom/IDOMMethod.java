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
package org.eclipse.jdt.core.jdom;

/**
 * Represents a method declaration.
 * The corresponding syntactic units are MethodDeclaration (JLS2 8.4),
 * ConstructorDeclaration (JLS2 8.8), and AbstractMethodDeclaration (JLS2 9.4).
 * A method has no children and its parent is a type.
 * Local classes are considered to be part of the body of a method, not a child.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IDOMMethod extends IDOMMember {
/**
 * Adds the given exception to the end of the list of exceptions this method
 * is declared to throw.
 * The syntax for an exception type name is defined by Method Throws (JLS2 8.4.4).
 * Type names must be specified as they would appear in source code. For 
 * example: <code>"IOException"</code> or <code>"java.io.IOException"</code>.
 * This is a convenience method for <code>setExceptions</code>.
 *
 * @param exceptionType the exception type
 * @exception IllegalArgumentException if <code>null</code> is specified
 * @see #setExceptions
 */
public void addException(String exceptionType) throws IllegalArgumentException;
/**
 * Adds the given parameter to the end of the parameter list. 
 * This is a convenience method for <code>setParameters</code>.
 * The syntax for parameter names is defined by Formal Parameters (JLS2 8.4.1).
 * The syntax for type names is defined by Formal Parameters (JLS2 8.4.1). 
 * Type names must be specified as they would appear in source code. For
 * example: <code>"File"</code>, <code>"java.io.File"</code>, or 
 * <code>"int[]"</code>.
 * 
 * @param type the type name
 * @param name the parameter name
 * @exception IllegalArgumentException if <code>null</code> is specified for
 *   either the type or the name
 * @see #setParameters
 */
public void addParameter(String type, String name) throws IllegalArgumentException;
/**
 * Returns the body of this method. The method body includes all code following
 * the method declaration, including the enclosing braces. 
 *
 * @return the body, or <code>null</code> if the method has no body (for
 *   example, for an abstract or native method)
 */
public String getBody();
/**
 * Returns the names of the exception types this method throws
 * in the order in which they are declared in the source, or an empty array
 * if this method declares no exception types.
 * The syntax for an exception type name is defined by Method Throws (JLS2 8.4.4).
 * Type names appear as they would in source code. For example: 
 * <code>"IOException"</code> or <code>"java.io.IOException"</code>.
 *
 * @return the list of exception types
 */
public String[] getExceptions();
/**
 * The <code>IDOMMethod</code> refinement of this <code>IDOMNode</code>
 * method returns the name of this method. Returns <code>null</code> for
 * constructors. The syntax for a method  name is defined by Identifier
 * of MethodDeclarator (JLS2 8.4).
 */
public String getName();
/**
 * Returns the names of parameters in this method in the order they are declared,
 * or <code>null</code> if no parameters are declared.
 * The syntax for parameter names is defined by Formal Parameters (JLS2 8.4.1).
 * 
 * @return the list of parameter names, or <code>null</code> if no parameters
 *  are declared
 */
public String[] getParameterNames();
/**
 * Returns the type names for the parameters of this method in the order they are declared,
 * or <code>null</code> if no parameters are declared.
 * The syntax for type names is defined by Formal Parameters (JLS2 8.4.1). 
 * Type names must be specified as they would appear in source code. For
 * example: <code>"File"</code>, <code>"java.io.File"</code>, or 
 * <code>"int[]"</code>.
 * 
 * @return the list of parameter types, or <code>null</code> if no parameters
 *  are declared
 */
public String[] getParameterTypes();
/**
 * Returns the return type name, or <code>null</code>. 
 * Returns <code>null</code> for constructors.
 * The syntax for return type name corresponds to ReturnType in 
 * MethodDeclaration (JLS2 8.4). Names are returned as they appear in the source
 * code; for example: <code>"File"</code>, <code>"java.io.File"</code>,
 * <code>"int[]"</code>, or <code>"void"</code>.
 *
 * @return the return type
 */
public String getReturnType();
/**
 * Returns whether this method is a constructor.
 *
 * @return <code>true</code> for constructors, and <code>false</code> for methods
 */
public boolean isConstructor();
/**
 * Sets the body of this method. The method body includes all code following
 * the method declaration, including the enclosing braces. No formatting or
 * syntax checking is performed on the body.
 *
 * @return the body, or <code>null</code> indicating the method has no body (for
 *   example, for an abstract or native method)
 */
public void setBody(String body);
/**
 * Sets whether this method represents a constructor.
 *
 * @param b <code>true</code> for constructors, and <code>false</code> for methods
 */
public void setConstructor(boolean b);
/**
 * Sets the names of the exception types this method throws,
 * in the order in which they are declared in the source. An empty array
 * indicates this method declares no exception types.
 * The syntax for an exception type name is defined by Method Throws (JLS2 8.4.4).
 * Type names must be specified as they would appear in source code. For 
 * example: <code>"IOException"</code> or <code>"java.io.IOException"</code>.
 *
 * @param exceptionTypes the list of exception types
 */
public void setExceptions(String[] exceptionTypes);
/**
 * The <code>IDOMMethod</code> refinement of this <code>IDOMNode</code>
 * method sets the name of this method. The syntax for a method 
 * name is defined by Identifer of MethodDeclarator (JLS2 8.4).
 * <p>
 * The name of a constructor is always <code>null</code> and thus it
 * must not be set.
 * </p>
 *
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void setName(String name) throws IllegalArgumentException;
/**
 * Sets the types and names of parameters in this method in the order they are
 * to be declared. If both <code>types</code> and <code>names</code> are <code>null</code> 
 * this indicates that this method has no parameters.
 * The syntax for parameter names is defined by Formal Parameters (JLS2 8.4.1).
 * The syntax for type names is defined by Formal Parameters (JLS2 8.4.1). 
 * Type names must be specified as they would appear in source code. For
 * example: <code>"File"</code>, <code>"java.io.File"</code>, or 
 * <code>"int[]"</code>.
 * 
 * @param types the list of type names
 * @param names the list of parameter name
 * @exception IllegalArgumentException if the number of types and names do not 
 *   match, or if either argument is <code>null</code>
 */
public void setParameters(String[] types, String[] names) throws IllegalArgumentException;
/**
 * Sets the return type name. This has no effect on constructors.
 * The syntax for return type name corresponds to ReturnType in 
 * MethodDeclaration (JLS2 8.4). Type names are specified as they appear in the 
 * source code; for example: <code>"File"</code>, <code>"java.io.File"</code>,
 * <code>"int[]"</code>, or <code>"void"</code>.
 *
 * @param type the return type
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void setReturnType(String type) throws IllegalArgumentException;
}
