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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.ClassTree;
import com.sun.tools.doclets.DocletAbortException;

import java.io.IOException;
import java.util.List;

public class AbstractTreeWriter 
    extends com.sun.tools.doclets.standard.AbstractTreeWriter
{

    protected boolean seenAspect = false;
    protected boolean aspectMode = false;
    
    protected AbstractTreeWriter(String filename, ClassTree classtree) 
        throws IOException, DocletAbortException {
        super(filename, classtree);
    }
    
    protected AbstractTreeWriter(String path, String filename, 
                                 ClassTree classtree, PackageDoc pkg) 
        throws IOException, DocletAbortException {
        super(path, filename, classtree, pkg);
    }
   
    protected void generateLevelInfo(ClassDoc parent, List list) {
        if (list.size() > 0) {
            ul();
            for (int i = 0; i < list.size(); i++) {
                ClassDoc local = (ClassDoc)list.get(i);
                boolean isAspect = local instanceof org.aspectj.ajdoc.AspectDoc;
                if (aspectMode) {
                    if (!local.qualifiedTypeName().equals("java.lang.Object")
                        && !isAspect) {
                        continue;
                    }
                } else if (isAspect) {
                    continue;
                }
                printPartialInfo(local);
                printExtendsImplements(parent, local);
                generateLevelInfo(local, classtree.subs(local));
            }
            ulEnd();
        }
    }

    protected void printExtendsImplements(ClassDoc parent, ClassDoc cd) {
        super.printExtendsImplements(parent, cd);
        if (cd instanceof AspectDoc) {
            printDominationInfo(((AspectDoc)cd).dominatees(), "dominates");
            printDominationInfo(((AspectDoc)cd).dominators(), "dominated by");
        }
    }

    protected void printDominationInfo(AspectDoc[] aspects,
                                       String whosOnTop) {
        if (aspects != null && aspects.length > 0) {
            print(" (" + whosOnTop + " ");
            for (int i = 0; i < aspects.length; i++) {
                if (i > 0) print(", ");
                printPreQualifiedClassLink(aspects[i]);
            }
            print(")");
        }
    }


    protected void generateTree(List list, String heading) {
        super.generateTree(list, heading);
        if (heading.equals("doclet.Class_Hierarchy")) {
            aspectMode = true;
            generateTree(list, "doclet.Aspect_Hierarchy");
            aspectMode = false;
        }
    }           
}
