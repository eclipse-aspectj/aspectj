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

import org.aspectj.ajdoc.IntroducedDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.VisibleMemberMap;

public class FieldSubWriter extends AbstractSubWriter {

    public static class Del extends com.sun.tools.doclets.standard.FieldSubWriter {
        protected FieldSubWriter mw;
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
            if (writer instanceof ClassWriter) {
                ((ClassWriter)writer).printAspectJDetail();
            }
            mw.printMembers();
        }
        protected void navSummaryLink() {
            mw.navSummaryLink();
        }
        protected void navDetailLink() {
            if (writer instanceof ClassWriter) {
                ((ClassWriter)writer).navstate++;
            }
            mw.navDetailLink();
        }
        public void setDelegator(FieldSubWriter mw) { this.mw = mw; }
        public void printSummaryMember(ClassDoc cd, ProgramElementDoc member) {
            mw.printSummaryMember(cd, member);
        }
    }
    
    protected Class delegateClass() {
        return Del.class;
    }

    public FieldSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         ClassDoc classdoc)
    {
        super(writer, classdoc);
    }
    
    public FieldSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

      public int getMemberKind() {
          //XXX hack!!!
          return VisibleMemberMap.FIELDS;
      }

    protected String propertyName() { return "Field"; }

    public void printIntroducedSummaryLink(ClassDoc cd, 
                                           ProgramElementDoc member) {
        writer.printClassLink(cd, member.name(), member.name(), false);
    }

    public void printCrosscuts(ClassDoc cd, ProgramElementDoc member) {
        org.aspectj.ajdoc.FieldDoc field = (org.aspectj.ajdoc.FieldDoc)member;
        IntroducedDoc intro = field.introduced();
        if (intro != null) {
            writer.dt();
            writer.boldText("doclet.Introduced_from");
            writer.dd();
            writer.printClassLink(intro.containingClass(),
                                  Statics.where(intro.member()));
        }
    }

    public void printSummaryCrosscuts(ClassDoc cd, ProgramElementDoc member) {}

    public boolean hasCrosscuts(ClassDoc classDoc, ProgramElementDoc member) {
        return true || ((org.aspectj.ajdoc.MemberDoc)member).introduced() != null;
    }
}  
    
    
