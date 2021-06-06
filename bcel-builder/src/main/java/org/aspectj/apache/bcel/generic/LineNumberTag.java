/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC         initial implementation
 *   Andy Clement   pushed down into bcel module
 * ******************************************************************/


package org.aspectj.apache.bcel.generic;

/**
 * we don't actually target instructions, but instructions target us.
 */
public class LineNumberTag extends Tag {

    private final int lineNumber;

    public LineNumberTag(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
    	return lineNumber;
    }

    public String toString() {
        return "line " + lineNumber;
    }

    public boolean equals(Object other) {
        if (!(other instanceof LineNumberTag)) return false;
        return lineNumber == ((LineNumberTag)other).lineNumber;
    }

    public int hashCode() {
        return lineNumber;
    }
}
