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
package org.aspectj.tools.doclets.standard;

import org.aspectj.tools.ajdoc.Access;

import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.DocletAbortException;

import java.io.IOException;
import java.util.List;

public class DeprecatedListWriter
    extends com.sun.tools.doclets.standard.DeprecatedListWriter
{

    protected DeprecatedAPIListBuilder builder;
    
    public DeprecatedListWriter(String filename,
                                DeprecatedAPIListBuilder builder)
        throws IOException {
        super(filename);
        this.builder = builder;
    }

    public static void generate(RootDoc root) throws DocletAbortException {
        String filename = "deprecated-list.html";
        DeprecatedListWriter dw = null;
        try {
            (dw = new DeprecatedListWriter(filename,
                                           new DeprecatedAPIListBuilder(root))).
                generateDeprecatedListFile();
        } catch (IOException e) {
            Standard.configuration().
                standardmessage.error("doclet.exception_encountered",
                                      e+"", filename);
            throw new DocletAbortException();
        } finally {
            if (dw != null) dw.close();
        }
    }

    protected void generateDeprecatedListFile() throws IOException {
        generateDeprecatedListFile(builder);
    }

    protected void printDeprecatedFooter() {
        printRestOfDeprecatedListFile();
        super.printDeprecatedFooter();
    }

    protected void printRestOfDeprecatedListFile() {
        deprecatedListFile(new AdviceSubWriter(this),
                           builder.getDeprecatedAdivce());
        deprecatedListFile(new PointcutSubWriter(this),
                           builder.getDeprecatedPointcuts());
        deprecatedListFile(new FieldIntroductionSubWriter(this),
                           builder.getDeprecatedFieldIntroductions());
        deprecatedListFile(new MethodIntroductionSubWriter(this),
                           builder.getDeprecatedMethodIntroductions());
        deprecatedListFile(new ConstructorIntroductionSubWriter(this),
                           builder.getDeprecatedConstructorIntroductions());
        deprecatedListFile(new SuperIntroductionSubWriter(this),
                           builder.getDeprecatedSuperIntroductions());
        
                           
    }

    protected final void deprecatedListFile(AbstractSubWriter mw,
                                      List list) {
        Access.printDeprecatedAPI(mw, list,
                                  "doclet.Deprecated_" +
                                  mw.keyName() + "s");
    }
}
