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
package org.aspectj.tools.ajdoc.rootmakers;

import com.sun.javadoc.RootDoc;
import com.sun.javadoc.DocErrorReporter;
import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.tools.ajdoc.AccessChecker;
import org.aspectj.tools.ajdoc.RootDocMaker;
import org.aspectj.tools.ajdoc.CannotMakeRootDocException;

/**
 * Makes a RootDoc using javadoc from SDK 1.3 as an example.
 *
 * @author Jeff Palm
 */
public class Javadoc13 extends Javadoc implements RootDocMaker {

    public Javadoc13() {}    
    public RootDoc makeRootDoc(String sourcepath,
                               String classpath,
                               String bootclasspath,
                               String extdirs,
                               long flags,
                               String encoding,
                               String locale,
                               String source,
                               List classesAndPackages,
                               List options,
                               DocErrorReporter messager,
                               String programName,
                               AccessChecker filter)
        throws CannotMakeRootDocException {
        // todo: options ignored in 13?
        Class envClass = type("com.sun.tools.javadoc.Env");
        if (envClass == null) return null;
        Method create = method("create", new Class[]{String.class,
                                                     String.class,
                                                     String.class,
                                                     String.class,
                                                     int.class,
                                                     String.class},
                               envClass);
        if (create == null) return null;
        Object env = invoke(create, null, new Object[]{sourcepath,
                                                       classpath,
                                                       bootclasspath,
                                                       extdirs,
                                                       new Integer((int)flags),
                                                       encoding});
        if (env == null) return null;
        

        return null;
    }

}
