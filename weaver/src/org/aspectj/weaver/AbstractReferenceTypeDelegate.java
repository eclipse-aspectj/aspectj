/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - June 2005 - separated out from ResolvedTypeX
 * ******************************************************************/
package org.aspectj.weaver;

public abstract class AbstractReferenceTypeDelegate implements ReferenceTypeDelegate {
	protected boolean exposedToWeaver;
	protected ReferenceType resolvedTypeX;

    public AbstractReferenceTypeDelegate(ReferenceType resolvedTypeX, boolean exposedToWeaver) {
        this.resolvedTypeX = resolvedTypeX;
        this.exposedToWeaver = exposedToWeaver;
    }
        
	public final boolean isClass() {
    	return !isAspect() && !isInterface();
    }

	/**
	 * Designed to be overriden by EclipseType to disable collection of shadow mungers
	 * during pre-weave compilation phase
	 */
	public boolean doesNotExposeShadowMungers() {
		return false;
	}

	public boolean isExposedToWeaver() {
		return exposedToWeaver;
	}

	public ReferenceType getResolvedTypeX() {
		return resolvedTypeX;
	}

}