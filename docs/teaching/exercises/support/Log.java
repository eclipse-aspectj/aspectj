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


package support;

public class Log {
    static StringBuffer data = new StringBuffer();

    public static void traceObject(Object o) {
        throw new UnsupportedOperationException();
    }

    public static void log(String s) {
        data.append(s);
        data.append(';');
    }

    public static void logClassName(Class _class) {
        String name = _class.getName();
        int dot = name.lastIndexOf('.');
        if (dot == -1) {
            log(name);
        } else {
            log(name.substring(dot+1, name.length()));
        }
    }

    public static String getString() {
        return data.toString();
    }

    public static void clear() {
        data.setLength(0);
    }
}
