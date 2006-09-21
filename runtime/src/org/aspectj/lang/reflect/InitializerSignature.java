/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2006 Contributors.
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
import java.lang.reflect.Constructor;

/**
 * Signature for static and instance initializers.
 * Static initializers have no parameters or exceptions, 
 * so empty arrays are returned from the CodeSignature methods.
 */
public interface InitializerSignature extends CodeSignature { 
    /**
     * @return Constructor associated with this initializer,
     * or null in the case of interface initializers and
     * static initializers.
     */
    Constructor getInitializer();
}
