/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
package org.aspectj.tools.ajdoc;

/**
 * @author Mik Kersten
 */
public class Util {

    public static boolean isExecutingOnJava5() {
        String version = System.getProperty("java.class.version","44.0");
        return version.equals("49.0");
    }
    
}
