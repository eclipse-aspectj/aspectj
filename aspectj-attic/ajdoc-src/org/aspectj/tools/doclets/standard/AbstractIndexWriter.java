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

import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.ajdoc.IntroducedDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.tools.doclets.IndexBuilder;

import java.io.IOException;

public class AbstractIndexWriter
    extends com.sun.tools.doclets.standard.AbstractIndexWriter
{

    protected AbstractIndexWriter(String path,
                                  String filename, 
                                  String relativePath,
                                  IndexBuilder indexbuilder) 
                                  throws IOException {
        super(path, filename, relativePath, indexbuilder);
    }

    protected AbstractIndexWriter(String filename,
                                  IndexBuilder indexbuilder) 
                                  throws IOException {
        super(filename, indexbuilder);
    }

    protected void printClassInfo(ClassDoc cd) {
        if (cd instanceof AspectDoc) {
            print("aspect ");
            printPreQualifiedClassLink(cd);
            print('.');
        } else {
            super.printClassInfo(cd);
        }
    }

    protected void printMemberDesc(MemberDoc member) {
        String classdesc =  Statics.type(member.containingClass()) + " " +
            getPreQualifiedClassLink(member.containingClass());
        if (member instanceof org.aspectj.ajdoc.MemberDoc) {
            org.aspectj.ajdoc.MemberDoc md = (org.aspectj.ajdoc.MemberDoc)member;
            if (md.isAdvice()) {
                printText("doclet.Advice_in", classdesc);
            } else if (md.isPointcut()) {
                printText("doclet.Pointcut_in", classdesc);
            }
        } else {
            super.printMemberDesc(member);
        }
        if (member instanceof org.aspectj.ajdoc.MemberDoc) {
            IntroducedDoc intro =
                ((org.aspectj.ajdoc.MemberDoc)member).introduced();
            if (intro != null) {
                print(' ');
                printText("doclet.introduced_by",
                          Statics.type(intro.containingClass()) + " " +
                          getPreQualifiedClassLink(intro.containingClass()));
            }
        }
    }
}
