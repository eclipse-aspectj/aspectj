/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

// todo: non-distribution license?

package org.aspectj.testing.compare;

// currently in aspectj-external-lib/regexp
import org.apache.regexp.RE;

import java.util.Vector;


/** Factory for our Regexp. */
public class RegexpFactory {
    public static Regexp makeRegexp() {
        return new RegExpDelegate();
    }
}

/** Implement Regexp by delegating to org.apache.regexp.RE */
final class RegExpDelegate implements Regexp {
    String pattern;
    RE regexp;
    public RegExpDelegate() { } 
    public Vector getGroups(String arg) { 
        String label = "getGroups(\"" + arg + "\") ";
        D.log(label);
        Vector result = null;
        if ((null != arg) && (matches(arg))) {
            int size = regexp.getParenCount();
            D.log(label + " size " + size);
            result = new Vector(size);
            for (int i = 0; i < size; i++) {
                Object item = regexp.getParen(i);
                result.addElement(item);
                D.log(label + i + ": " + item);
            }
        }
        return result;
    }
    public boolean matches(String arg) {
        return ((null != regexp) && regexp.match(arg));
    }
    public void setPattern(String pattern) throws Exception {
        this.pattern = pattern;
        regexp = new RE(this.pattern);
        D.log("RE: " + regexp + " pattern: /" + pattern + "/");
    }
    public String getPattern()  {
        return pattern;
    }
}

