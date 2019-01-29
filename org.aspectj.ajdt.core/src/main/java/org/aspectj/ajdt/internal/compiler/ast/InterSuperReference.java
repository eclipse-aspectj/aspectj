/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Used to represent super references inside of inter-type declarations.  Special mechanism
 * needed for handling in an interface context.
 * 
 * @author Jim Hugunin
 */
public class InterSuperReference extends SuperReference {
	public InterSuperReference(SuperReference template, TypeBinding myType) {
		super(template.sourceStart, template.sourceEnd);
		this.resolvedType = myType;
		this.constant=Constant.NotAConstant;
	}

	public TypeBinding resolveType(BlockScope scope) {
		return resolvedType;
	}

}
