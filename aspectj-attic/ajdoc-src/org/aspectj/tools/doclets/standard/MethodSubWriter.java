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

import org.aspectj.ajdoc.AdviceDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.VisibleMemberMap;

public class MethodSubWriter extends ExecutableMemberSubWriter {

    public static class Del
        extends com.sun.tools.doclets.standard.MethodSubWriter {
        protected MethodSubWriter mw;
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
        }
        public void printMembers() {
            mw.printMembers();
        }
        protected void navSummaryLink() {
            mw.navSummaryLink();
        }
        protected void navDetailLink() {
            mw.navDetailLink();
        }
        public void setDelegator(MethodSubWriter mw) { this.mw = mw; }
        public void printSummaryMember(ClassDoc cd, ProgramElementDoc member) {
            mw.printSummaryMember(cd, member);
        }
    }

    protected Class delegateClass() { return Del.class; }

    public MethodSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         ClassDoc classdoc)
    {
        super(writer, classdoc);
    }

    public MethodSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

    public int getMemberKind() {
        //XXX hack!!!
        return VisibleMemberMap.METHODS; 
    }

    //XXX
    //hacks
    protected void printSummaryType(ProgramElementDoc member) {
        if (member instanceof MethodDoc) {
            //TODO: Put in Access...
            MethodDoc meth = (MethodDoc)member;
            printModifierAndType(meth, meth.returnType());
        } else if (member instanceof AdviceDoc) {
            AdviceDoc advice = (AdviceDoc)member;
            printModifierAndType(advice, advice.returnType());
        }
    }
    protected void printSignature(ExecutableMemberDoc member) {
        writer.displayLength = 0;
        writer.pre();
        printModifiers(member);
        //printReturnType((MethodDoc)member);
        bold(member.name());
        //  	printParameters(member);
        //  	printExceptions(member);
        writer.preEnd();
    }
    //end-hacks

    protected String propertyName() { return "Method"; }
}


