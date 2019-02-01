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

 
package org.aspectj.internal.tools.build;

/** logging stub XXX replace */
public class Messager {
    public Messager() {
    }
    public boolean log(String s) {
        System.out.println(s);
        return true;
    }
    
    public boolean error(String s) {
        System.out.println(s);
        return true;
    }
    
    public boolean logException(String context, Throwable thrown) {
        System.err.println(context);
        thrown.printStackTrace(System.err);
        return true;
    }
}







