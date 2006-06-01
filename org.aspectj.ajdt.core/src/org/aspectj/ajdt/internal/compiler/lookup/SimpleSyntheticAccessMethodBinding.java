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


package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;

public class SimpleSyntheticAccessMethodBinding extends SyntheticMethodBinding {
//	public SimpleSyntheticAccessMethodBinding(MethodBinding method) {
//		super(method);
//		this.declaringClass = method.declaringClass;
//		this.selector = method.selector;
//		this.modifiers = method.modifiers;
//		this.parameters = method.parameters;
//		this.returnType = method.returnType;
//	}
	
	public SimpleSyntheticAccessMethodBinding(MethodBinding binding) {
		super(binding);
	}
}
