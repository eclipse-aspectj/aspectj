/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde.ui;

import org.aspectj.asm.AdviceAssociation;
import org.aspectj.asm.InheritanceAssociation;
import org.aspectj.asm.IntroductionAssociation;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.Relation;

/**
 * Uses the factory pattern.
 * 
 * @author Mik Kersten
 */
public abstract class AbstractIconRegistry {
	
	protected final String RESOURCE_PATH = "org/aspectj/ajde/resources/";
	protected final String STRUCTURE_PATH = RESOURCE_PATH + "structure/";

	protected final AbstractIcon PROJECT = createIcon(STRUCTURE_PATH + "project.gif");
	protected final AbstractIcon PACKAGE = createIcon(STRUCTURE_PATH + "package.gif");
	protected final AbstractIcon ASPECT = createIcon(STRUCTURE_PATH + "aspect.gif");
	protected final AbstractIcon INITIALIZER = createIcon(STRUCTURE_PATH + "code.gif");
	protected final AbstractIcon INTRODUCTION = createIcon(STRUCTURE_PATH + "introduction.gif");
	protected final AbstractIcon CONSTRUCTOR = createIcon(STRUCTURE_PATH + "method.gif");
	protected final AbstractIcon POINTCUT = createIcon(STRUCTURE_PATH + "pointcut.gif");
	protected final AbstractIcon ADVICE = createIcon(STRUCTURE_PATH + "advice.gif");
	protected final AbstractIcon DECLARE_PARENTS = createIcon(STRUCTURE_PATH + "declareParents.gif");
	protected final AbstractIcon DECLARE_WARNING = createIcon(STRUCTURE_PATH + "declareWarning.gif");
	protected final AbstractIcon DECLARE_ERROR = createIcon(STRUCTURE_PATH + "declareError.gif");
	protected final AbstractIcon DECLARE_SOFT = createIcon(STRUCTURE_PATH + "declareSoft.gif");
	protected final AbstractIcon CODE = createIcon(STRUCTURE_PATH + "code.gif");
	protected final AbstractIcon ERROR = createIcon(STRUCTURE_PATH + "error.gif");
	
	protected final AbstractIcon FILE = createIcon(STRUCTURE_PATH + "file.gif");
	protected final AbstractIcon FILE_JAVA = createIcon(STRUCTURE_PATH + "file-java.gif");
	protected final AbstractIcon FILE_ASPECTJ = createIcon(STRUCTURE_PATH + "file-aspectj.gif");
	protected final AbstractIcon FILE_LST = createIcon(STRUCTURE_PATH + "file-lst.gif");
	
	protected final AbstractIcon METHOD = createIcon(STRUCTURE_PATH + "method.gif");
	protected final AbstractIcon FIELD = createIcon(STRUCTURE_PATH + "field.gif");
	protected final AbstractIcon CLASS = createIcon(STRUCTURE_PATH + "class.gif");
	protected final AbstractIcon INTERFACE = createIcon(STRUCTURE_PATH + "interface.gif");

    protected final AbstractIcon RELATION_ADVICE_FORWARD = createIcon(STRUCTURE_PATH + "adviceForward.gif");
    protected final AbstractIcon RELATION_ADVICE_BACK = createIcon(STRUCTURE_PATH + "adviceBack.gif");
    protected final AbstractIcon RELATION_INHERITANCE_FORWARD = createIcon(STRUCTURE_PATH + "inheritanceForward.gif");
    protected final AbstractIcon RELATION_INHERITANCE_BACK = createIcon(STRUCTURE_PATH + "inheritanceBack.gif");
    protected final AbstractIcon RELATION_REFERENCE_FORWARD = createIcon(STRUCTURE_PATH + "referenceForward.gif");
    protected final AbstractIcon RELATION_REFERENCE_BACK = createIcon(STRUCTURE_PATH + "referenceBack.gif");

	public AbstractIcon getRelationIcon(Relation relation) {
		if (relation.getAssociationName().equals(AdviceAssociation.NAME)) {
			return RELATION_ADVICE_FORWARD;
		} else if (relation.getAssociationName().equals(IntroductionAssociation.NAME)) {
			return RELATION_ADVICE_FORWARD;
		} else if (relation.getAssociationName().equals(InheritanceAssociation.NAME)) {
			return RELATION_INHERITANCE_FORWARD;
		} else {
			return RELATION_REFERENCE_FORWARD;
		}
	}

	/**
	 * @return	null if the kind could not be resolved
	 */
	protected AbstractIcon getStructureIcon(ProgramElementNode.Kind kind, ProgramElementNode.Accessibility accessibility) {
		return getStructureIcon(kind, ProgramElementNode.Accessibility.PUBLIC);
	}	
	
	/**
	 * Assumes "public" visibility for the icon.
	 * 
	 * @return	null if the kind could not be resolved
	 */
	public AbstractIcon getStructureIcon(ProgramElementNode.Kind kind) { 
		if (kind == ProgramElementNode.Kind.PROJECT) {
			return PROJECT;
		} else if (kind == ProgramElementNode.Kind.PACKAGE) {
			return PACKAGE;
		} else if (kind == ProgramElementNode.Kind.FILE) {
			return FILE;
		} else if (kind == ProgramElementNode.Kind.FILE_JAVA) {
			return FILE_JAVA;
		} else if (kind == ProgramElementNode.Kind.FILE_ASPECTJ) {
			return FILE_ASPECTJ;
		} else if (kind == ProgramElementNode.Kind.FILE_LST) {
			return FILE_LST;
		} else if (kind == ProgramElementNode.Kind.CLASS) {
			return CLASS;
		} else if (kind == ProgramElementNode.Kind.INTERFACE) {
			return INTERFACE;
		} else if (kind == ProgramElementNode.Kind.ASPECT) {
			return ASPECT;
		} else if (kind == ProgramElementNode.Kind.INITIALIZER) {
			return INITIALIZER;
		} else if (kind == ProgramElementNode.Kind.INTER_TYPE_CONSTRUCTOR) {
			return INTRODUCTION;
		} else if (kind == ProgramElementNode.Kind.INTER_TYPE_FIELD) {
			return INTRODUCTION;
		} else if (kind == ProgramElementNode.Kind.INTER_TYPE_METHOD) {
			return INTRODUCTION;
		} else if (kind == ProgramElementNode.Kind.CONSTRUCTOR) {
			return CONSTRUCTOR;
		} else if (kind == ProgramElementNode.Kind.METHOD) {
			return METHOD;
		} else if (kind == ProgramElementNode.Kind.FIELD) {
			return FIELD;
		} else if (kind == ProgramElementNode.Kind.POINTCUT) {
			return POINTCUT;
		} else if (kind == ProgramElementNode.Kind.ADVICE) {
			return ADVICE;
		} else if (kind == ProgramElementNode.Kind.DECLARE_PARENTS) {
			return DECLARE_PARENTS;
		} else if (kind == ProgramElementNode.Kind.DECLARE_WARNING) {
			return DECLARE_WARNING;
		} else if (kind == ProgramElementNode.Kind.DECLARE_ERROR) {
			return DECLARE_ERROR;
		} else if (kind == ProgramElementNode.Kind.DECLARE_SOFT) {
			return DECLARE_SOFT;
		} else if (kind == ProgramElementNode.Kind.DECLARE_PRECEDENCE) {
			return DECLARE_SOFT;
		} else if (kind == ProgramElementNode.Kind.CODE) {
			return CODE;
		} else if (kind == ProgramElementNode.Kind.ERROR) {
			return ERROR;
		} else {
			System.err.println("AJDE Message: unresolved icon kind " + kind);
			return null;
		}
	}
 
 	/**
 	 * Implement to create platform-specific icons.
 	 */ 
	protected abstract AbstractIcon createIcon(String path);
}



