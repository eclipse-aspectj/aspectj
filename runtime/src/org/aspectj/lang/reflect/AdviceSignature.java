/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.lang.reflect;
import java.lang.reflect.Method;

public interface AdviceSignature extends CodeSignature {
    Class getReturnType();      /* name is consistent with reflection API   */
                                /* before and after always return Void.TYPE */
                                /* (some around also return Void.Type)      */
	Method getAdvice();
}
