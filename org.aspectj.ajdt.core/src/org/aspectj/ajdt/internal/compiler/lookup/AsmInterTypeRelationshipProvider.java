/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 
package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.*;

import org.aspectj.asm.*;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.weaver.*;

/**
 * @author Mik Kersten
 */
public class AsmInterTypeRelationshipProvider {

	public static final String INTER_TYPE_DECLARES = "declares on";
	public static final String INTER_TYPE_DECLARED_BY = "aspect declarations";

	public static void addRelationship(
		ResolvedTypeX onType,
		EclipseTypeMunger munger) {
			
		IProgramElement.Kind kind = IProgramElement.Kind.ERROR;
		if (munger.getMunger().getKind() == ResolvedTypeMunger.Field) {
			kind = IProgramElement.Kind.INTER_TYPE_FIELD;
		} else if (munger.getMunger().getKind() == ResolvedTypeMunger.Constructor) {
			kind = IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR;
		} else if (munger.getMunger().getKind() == ResolvedTypeMunger.Method) {
			kind = IProgramElement.Kind.INTER_TYPE_METHOD;
		} else if (munger.getMunger().getKind() == ResolvedTypeMunger.Parent) {
			kind = IProgramElement.Kind.INTER_TYPE_PARENT;
		}  	 
	
		if (munger.getSourceLocation() != null
			&& munger.getSourceLocation() != null) {
			String sourceHandle = 
				munger.getSourceLocation().getSourceFile().getAbsolutePath() + IProgramElement.ID_DELIM
				+ munger.getSourceLocation().getLine() + IProgramElement.ID_DELIM
				+ munger.getSourceLocation().getColumn();
				
			String targetHandle = 
				onType.getSourceLocation().getSourceFile().getAbsolutePath() + IProgramElement.ID_DELIM
				+ onType.getSourceLocation().getLine() + IProgramElement.ID_DELIM
				+ onType.getSourceLocation().getColumn();
	
			IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
			if (sourceHandle != null && targetHandle != null) {
				IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.ADVICE, INTER_TYPE_DECLARES);
				foreward.getTargets().add(targetHandle);
				
				IRelationship back = mapper.get(targetHandle, IRelationship.Kind.ADVICE, INTER_TYPE_DECLARED_BY);
				back.getTargets().add(sourceHandle);
			}
		}
	}

}
