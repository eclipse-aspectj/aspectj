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

import com.sun.javadoc.MemberDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.tools.doclets.DocletAbortException;
import com.sun.tools.doclets.IndexBuilder;

import java.io.IOException;

public class SingleIndexWriter
    extends com.sun.tools.doclets.standard.SingleIndexWriter
{

    protected class Del extends AbstractIndexWriter {
        public Del(String s, IndexBuilder i) throws IOException {
            super(s, i);
        }
        public void print(String s) {
            SingleIndexWriter.this.print(s);
        }
    }
    final protected Del del;
    {
        Standard.quiet();
        Del d = null;
        try {
            d = new Del(filename, indexbuilder);
        } catch (Exception e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered", 
                      e+"", filename);
        } finally {
            del = d;
            Standard.speak();
        }
    }
    protected void printMemberDesc(MemberDoc member) {
        del.printMemberDesc(member);
    }
    protected void printClassInfo(ClassDoc cd) {
        del.printClassInfo(cd);
    }

    public SingleIndexWriter(String filename, 
                             IndexBuilder indexbuilder) throws IOException {
        super(filename, indexbuilder);
    }

    public static void generate(IndexBuilder indexbuilder) 
                                throws DocletAbortException {
        SingleIndexWriter sw = null;
        String filename = "index-all.html";
        try {
            (sw = new SingleIndexWriter(filename, indexbuilder)).
                generateIndexFile();
        } catch (IOException e) {
            Standard.configuration().standardmessage.error
                ("doclet.exception_encountered",
                 e+"", filename);
            throw new DocletAbortException();
        } finally {
            if (sw != null) sw.close();
        }
    }
}
