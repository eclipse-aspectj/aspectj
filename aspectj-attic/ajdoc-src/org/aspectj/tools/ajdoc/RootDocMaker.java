/* -*- Mode: JDE; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the debugger and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 */
package org.aspectj.tools.ajdoc;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

import java.util.List;

/**
 * Interface specifying types that
 * can produce RootDocs.
 *
 * @author Jeff Palm
 */
public interface RootDocMaker {

    /**
     * Returns a RootDoc using the passed in parameters.
     * Throw a CannotMakeRootDocException is something
     * goes awry.
     *
     * @param sourcepath           sourcepath to use.
     * @param classpath            classpath to use.
     * @param bootclasspath        bootclasspath to use.
     * @param extdirs              extdirs to use.
     * @param flags                flags to use.
     * @param encoding             encoding to use.
     * @param locale               locale to use.
     * @param source               source to use.
     * @param filenamesAndPackages filenamesAndPackages to use.
     * @param options              options to use.
     * @param err                  ErrPrinter to use.
     * @param programName          program name to use.
     * @param filter               filter to use.
     * @return                     a RootDoc.
     */
    public RootDoc makeRootDoc(String sourcepath,
                               String classpath,
                               String bootclasspath,
                               String extdirs,
                               long flags,
                               String encoding,
                               String locale,
                               String source,
                               List filenamesAndPackages,
                               List options,
                               DocErrorReporter err,
                               String programName,
                               AccessChecker filter)
    throws CannotMakeRootDocException;
                               
}
