/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 
package org.aspectj.ajdt.internal.compiler.lookup;

//import java.io.IOException;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.weaver.ResolvedType;

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
		ResolvedType onType,
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
			IProgramElement sourceIPE = AsmManager.getDefault().getHierarchy()
				.findElementForSourceLine(munger.getSourceLocation());
			String sourceHandle = AsmManager.getDefault().getHandleProvider()
				.createHandleIdentifier(sourceIPE);
			
			IProgramElement targetIPE = AsmManager.getDefault().getHierarchy()
				.findElementForSourceLine(onType.getSourceLocation());
			String targetHandle = AsmManager.getDefault().getHandleProvider()
				.createHandleIdentifier(targetIPE);
				
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
