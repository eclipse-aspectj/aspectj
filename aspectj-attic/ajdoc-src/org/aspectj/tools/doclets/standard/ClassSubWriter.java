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
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.VisibleMemberMap;

public class ClassSubWriter extends AbstractSubWriter {

    public static class Del extends com.sun.tools.doclets.standard.ClassSubWriter {
        protected ClassSubWriter mw;
        public Del(com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
                   ClassDoc classdoc)
        {
            super(writer, classdoc);
        }
        public Del(com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
        {
            super(writer);
        }
        public void printMembersSummary() {
            mw.printMembersSummary();
            mw.printIntroducedMembersSummary();
            if (writer instanceof ClassWriter) {
                ((ClassWriter)writer).printAspectJSummary();
            }
        }
        public void printMembers() {
            mw.printMembers();
        }
        protected void navSummaryLink() {
            mw.navSummaryLink();
            if (writer instanceof ClassWriter) {
                ((ClassWriter)writer).navstate++;
            }
        }
        protected void navDetailLink() {
            mw.navDetailLink();
        }
        public void setDelegator(ClassSubWriter mw) { this.mw = mw; }
    }

    protected Class delegateClass() {
        return Del.class;
    }

    public ClassSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         ClassDoc classdoc)
    {
        super(writer, classdoc);
    }
    
    public ClassSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }
    
    public int getMemberKind() {
        //XXX hack!!!
        return VisibleMemberMap.INNERCLASSES;
    }
    
    protected void printSummaryType(ProgramElementDoc member) {
        ClassDoc cd = (ClassDoc)member;
        printModifierAndType(cd, null);
    }

    protected void printModifierAndType(ProgramElementDoc member,
                                        Type type) {
        writer.printTypeSummaryHeader();
        printModifier(member);
        if (type == null) {
            print(member instanceof AspectDoc ?
                  "aspect" : member.isClass() ?
                  "class" :
                  "interface");
        } else {
            printTypeLink(type); 
        }
        writer.printTypeSummaryFooter();
    }
}  
    
    
