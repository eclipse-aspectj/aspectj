/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
