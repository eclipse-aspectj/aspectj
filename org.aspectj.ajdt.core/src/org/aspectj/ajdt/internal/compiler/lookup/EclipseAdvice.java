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


package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.Pointcut;


public class EclipseAdvice extends Advice {
	public EclipseAdvice(AjAttribute.AdviceAttribute attribute, Pointcut pointcut, Member signature) {
			
		super(attribute, pointcut, signature);
	}



	public void implementOn(Shadow shadow) {
		throw new RuntimeException("unimplemented");
	}

	public void specializeOn(Shadow shadow) {
		throw new RuntimeException("unimplemented");
	}

	public int compareTo(Object other) {
		throw new RuntimeException("unimplemented");
	}

}
