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
import com.sun.tools.doclets.ClassTree;
import com.sun.tools.doclets.DocletAbortException;

import java.io.IOException;
import java.util.List;

public class TreeWriter
    extends com.sun.tools.doclets.standard.TreeWriter
{
    protected class Del extends AbstractTreeWriter {
        public Del(String s, ClassTree c)
            throws IOException, DocletAbortException {
            super(s, c);
        }
        public void print(String s) {
            TreeWriter.this.print(s);
        }
    }
    final protected Del del;
    {
        Standard.quiet();
        Del d = null;
        try {
            d = new Del(filename, classtree);
        } catch (Exception e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered", 
                      e+"", filename);
        } finally {
            del = d;
            Standard.speak();
        }
    }
    protected void generateLevelInfo(ClassDoc parent, List list) {
        del.generateLevelInfo(parent, list);
    }
    protected void generateTree(List list, String heading) {
        del.generateTree(list, heading);
    }

    public TreeWriter(String filename, ClassTree classtree)  
        throws IOException, DocletAbortException {
        super(filename, classtree);
    }

    public static void generate(ClassTree classtree) 
                                throws DocletAbortException {
        TreeWriter tw = null;
        String filename = "overview-tree.html";
        try {
            (tw = new TreeWriter(filename, classtree)).
                generateTreeFile();
        } catch (IOException e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered",
                      e+"", filename);
            throw new DocletAbortException();
        } finally {
            if (tw != null) tw.close();
        }
    }
}
