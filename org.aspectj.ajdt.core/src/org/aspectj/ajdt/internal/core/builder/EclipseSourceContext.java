/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseSourceLocation;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ISourceContext;
import org.eclipse.jdt.internal.compiler.CompilationResult;



public class EclipseSourceContext implements ISourceContext {
	
	CompilationResult result;

	public EclipseSourceContext(CompilationResult result) {
		this.result = result;
	}
	

	public ISourceLocation makeSourceLocation(IHasPosition position) {
		return new EclipseSourceLocation(result, position.getStart(), position.getEnd());
	}

}
