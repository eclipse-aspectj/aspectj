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
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;
import java.io.Serializable;
import java.io.File;

public class SourceLine implements Serializable {
    public int line;
    public String filename;
    //boolean hasBody;

    public SourceLine(String filename, int line) {
        this.line = line;
        this.filename = filename;
    }

    public String getDirectory() {
        return new File(filename).getParent();
    }

    public int hashCode() {
        return filename.hashCode() ^ line;
    }

    public boolean equals(Object other) {
        if (!(other instanceof SourceLine)) return false;

        SourceLine otherLine = (SourceLine)other;

        return otherLine.line == line && otherLine.filename.equals(filename);
    }

    public String toString() {
        return filename + "::" + line;
    }

    /**
     * @return  true     when the method has a corresponding signature in the source code
     * @return  false    otherwise
     */
    //public boolean hasBody() { return hasBody; }
}
