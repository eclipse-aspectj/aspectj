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

//import java.io.IOException;

import org.aspectj.asm.*;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.weaver.*;

/**
 * !!! is this class still being used?
 * 
 * @author Mik Kersten
 */
public class AsmInterTypeRelationshipProvider {

    protected static AsmInterTypeRelationshipProvider INSTANCE = new AsmInterTypeRelationshipProvider();
    
	public static final String INTER_TYPE_DECLARES = "declared on";
	public static final String INTER_TYPE_DECLARED_BY = "aspect declarations";

	public void addRelationship(
		ResolvedTypeX onType,
		EclipseTypeMunger munger) {
			
//		IProgramElement.Kind kind = IProgramElement.Kind.ERROR;
//		if (munger.getMunger().getKind() == ResolvedTypeMunger.Field) {
//			kind = IProgramElement.Kind.INTER_TYPE_FIELD;
//		} else if (munger.getMunger().getKind() == ResolvedTypeMunger.Constructor) {
//			kind = IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR;
//		} else if (munger.getMunger().getKind() == ResolvedTypeMunger.Method) {
//			kind = IProgramElement.Kind.INTER_TYPE_METHOD;
//		} else if (munger.getMunger().getKind() == ResolvedTypeMunger.Parent) {
//			kind = IProgramElement.Kind.INTER_TYPE_PARENT;
//		}  	 
	
		if (munger.getSourceLocation() != null
			&& munger.getSourceLocation() != null) {
			String sourceHandle = ProgramElement.createHandleIdentifier(
				munger.getSourceLocation().getSourceFile(),
				munger.getSourceLocation().getLine(),
				munger.getSourceLocation().getColumn(),
				munger.getSourceLocation().getOffset());
				
			String targetHandle = ProgramElement.createHandleIdentifier(
				onType.getSourceLocation().getSourceFile(),
				onType.getSourceLocation().getLine(),
				onType.getSourceLocation().getColumn(),
				onType.getSourceLocation().getOffset());
				
			IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
			if (sourceHandle != null && targetHandle != null) {
				IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES,false,true);
				foreward.addTarget(targetHandle);
				
				IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY,false,true);
				back.addTarget(sourceHandle);
			}
		}
	}
	
    public static AsmInterTypeRelationshipProvider getDefault() {
        return INSTANCE;
    }
    
    /**
     * Reset the instance of this class, intended for extensibility.
     * This enables a subclass to become used as the default instance.
     */
    public static void setDefault(AsmInterTypeRelationshipProvider instance) {
        INSTANCE = instance;
    }
}
