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
 * Represents a source type in a compilation unit, either as a top-level type or a member type.
 * The corresponding syntactic units are ClassDeclaration (JLS2 8.1) and InterfaceDeclaration (JLS2 9.1).
 * <p>
 * Allowable child types for a type are <code>IDOMType</code>, <code>IDOMField</code>, 
 * <code>IDOMMethod</code>, and <code>IDOMInitializer</code>.
 * Children are listed in the order in which they appear in the source. The parent of a type
 * is a type (in the case of a member type) or a compilation unit (in the case of a top-level type).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IDOMType extends IDOMMember {
/**
 * Adds the given interface name to the names of interfaces that this type implements or extends
 * (the name will be added after the existing interface names). This is a convenience method.
 *
 * For classes, this represents the interfaces that this class implements.
 * For interfaces, this represents the interfaces that this interface extends.
 * The name may or may not be fully qualified.
 *
 * @param interfaceName the syntax for an interface name is defined by
 *  Interfaces in ClassDeclaration (JLS2 8.1). Type names must be specified as they would
 *  appear in source code. For example: "Cloneable", "java.io.Serializable".
 *
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void addSuperInterface(String interfaceName) throws IllegalArgumentException;
/**
 * The <code>IDOMType</code> refinement of this <code>IDOMNode</code>
 * method returns the name of this type. The name of a class is defined by 
 * ClassDeclaration (JLS2 8.1); the name of an interface is defined by
 * InterfaceDeclaration (JLS2 9.1).
 */
public String getName();
/**
 * Returns the name of this type's superclass. The syntax for a superclass name
 * is specified by Super in ClassDeclaration (JLS2 8.1). Type names must be
 * specified as they would appear in source code. For example: 
 * <code>"Object"</code>, or <code>"java.io.File"</code>.
 *
 * @return the superclass name, or <code>null</code> if this type represents
 *   an interface or if no superclass has been assigned to this class
 */
public String getSuperclass();
/**
 * Returns the names of interfaces that this type implements or extends,
 * in the order in which they are listed in the source, or an empty array
 * if no superinterfaces are present. The syntax for interface names is
 * defined by Interfaces in ClassDeclaration (JLS2 8.1). Type names appear
 * as they would in source code. For example: <code>"Cloneable"</code>,
 * or <code>"java.io.Serializable"</code>.
 * <p>
 * For classes, this method returns the interfaces that this class implements.
 * For interfaces, this method returns the interfaces that this interface extends.
 * </p>
 * 
 * @return the list of interface names
 */
public String[] getSuperInterfaces();
/**
 * Returns whether this type is a class.
 *
 * @return <code>true</code> for classes, and <code>false</code> for interfaces
 */
public boolean isClass();
/**
 * Sets whether this type is a class or an interface. If this type is
 * a class, and is changed to an interface, this type's superclass
 * becomes <code>null</code>. When a class becomes an interface or an
 * interface becomes a class, superinterfaces remain (as part of an
 * <code>implements</code> clause for classes, or an <code>extends</code>
 * clause for interfaces).
 *
 * @param b <code>true</code> for classes, and <code>false</code> for interfaces
 */
public void setClass(boolean b);
/**
 * The <code>IDOMType</code> refinement of this <code>IDOMNode</code>
 * method sets the name of this type. The name of a class is defined by 
 * ClassDeclaration (JLS2 8.1); the name of an interface is defined by
 * InterfaceDeclaration (JLS2 9.1).
 *
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void setName(String name) throws IllegalArgumentException;
/**
 * Sets the name of this type's superclass. Has no effect if this type
 * represents an interface. A <code>null</code> name indicates that no 
 * superclass name (extends clause) should appear in the source code.
 * The syntax for a superclass name is specified by Super in ClassDeclaration
 * (JLS2 8.1). Type names must be specified as they would appear in source code.
 * For example: <code>"Object"</code>, or <code>"java.io.File"</code>.
 *
 * @param superclassName the superclass name, or <code>null</code> if this type
 *   should have to no explicitly specified superclass
 */
public void setSuperclass(String superclassName);
/**
 * Sets the names of interfaces that this type implements or extends,
 * in the order in which they are to be listed in the source. An empty array
 * parameter indicates that no superinterfaces are present. The syntax for
 * interface names is defined by Interfaces in ClassDeclaration (JLS2 8.1).
 * Type names appear as they would in source code. For example: 
 * <code>"Cloneable"</code>, or <code>"java.io.Serializable"</code>.
 * <p>
 * For classes, this method sets the interfaces that this class implements.
 * For interfaces, this method sets the interfaces that this interface extends.
 * </p>
 * 
 * @param interfaceNames the list of interface names
 */
public void setSuperInterfaces(String[] interfaceNames);
}
