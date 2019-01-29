/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

/**
 * Abstract class for the different declare annotation
 * declaration AST node types.
 * 
 * Has everything a DeclareDeclaration has plus:
 *   	a PatternNode called 'pattern'
 *      a SimpleName called 'annotationName'
 *      
 * Unsupported for JLS2.    
 */
public abstract class DeclareAnnotationDeclaration extends DeclareDeclaration {
	
	abstract ChildPropertyDescriptor internalPatternNodeProperty();
	abstract ChildPropertyDescriptor internalAnnotationNameProperty();
	
	/**
	 * Creates and returns a structural property descriptor for the
	 * "annotationName" property declared on the given concrete node type.
	 * 
	 * @return the property descriptor
	 */
	static final ChildPropertyDescriptor internalAnnotationNamePropertyFactory(Class nodeClass) {
		return new ChildPropertyDescriptor(nodeClass, "annotationName", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	}
	
	/**
	 * Creates and returns a structural property descriptor for the
	 * "pattern" property declared on the given concrete node type.
	 * 
	 * @return the property descriptor
	 */
	static final ChildPropertyDescriptor internalPatternNodePropertyFactory(Class nodeClass) {
		return new ChildPropertyDescriptor(nodeClass, "pattern", PatternNode.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	}
	
	PatternNode pattern;
	SimpleName name = null;

	DeclareAnnotationDeclaration(AST ast) {
		super(ast);
		unsupportedIn2();
	}
	
	public PatternNode getPatternNode(){
		return pattern;
	}
	
	public void setPatternNode(PatternNode pattern) {
		if (pattern == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.pattern;
		preReplaceChild(oldChild, pattern, internalPatternNodeProperty());
		this.pattern = pattern;
		postReplaceChild(oldChild, pattern, internalPatternNodeProperty());
	}
	
	/**
	 * Returns the name of the annotation type member declared in this declaration.
	 * 
	 * @return the name node
	 */ 
	public SimpleName getAnnotationName() {
		if (this.name == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.name == null) {
					preLazyInit();
					this.name = new SimpleName(this.ast);
					postLazyInit(this.name, internalAnnotationNameProperty());
				}
			}
		}
		return this.name;
	}
	
	/**
	 * Sets the name of the annotation type member declared in this declaration to the
	 * given name.
	 * 
	 * @param name the new member name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setAnnotationName(SimpleName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.name;
		preReplaceChild(oldChild, name, internalAnnotationNameProperty());
		this.name = name;
		postReplaceChild(oldChild, name, internalAnnotationNameProperty());
	}

}
