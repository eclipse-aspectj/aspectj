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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.DocletAbortException;

import java.io.IOException;

public abstract class SubWriterHolderWriter
    extends com.sun.tools.doclets.standard.SubWriterHolderWriter
{
    
    public SubWriterHolderWriter(String filename) throws IOException {
        super(filename);
    }


    public SubWriterHolderWriter(String path, String filename, String relpath) 
                                 throws IOException, DocletAbortException {
        super(path, filename, relpath);
    }

    public void printSummaryMember(AbstractSubWriter mw, ClassDoc cd, 
                                   ProgramElementDoc member) {
        super.printSummaryMember(mw, cd, member);
        if (mw instanceof AbstractSubWriterAJ) {
            AbstractSubWriterAJ aj = (AbstractSubWriterAJ)mw;
            if (aj.hasCrosscuts(cd, member)) {
                aj.printSummaryCrosscuts(cd, member);
            }
        }
    }

    public String getPreQualifiedClassLink(ClassDoc cd, String where) {
        return getPkgName(cd) + getClassLink(cd, where, cd.name());
    }
    
    public void printPreQualifiedClassLink(ClassDoc cd, String where) {
        print(getPreQualifiedClassLink(cd, where));
    }
}




