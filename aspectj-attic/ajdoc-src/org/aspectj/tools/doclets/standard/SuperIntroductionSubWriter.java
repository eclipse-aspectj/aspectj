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
import org.aspectj.ajdoc.IntroducedSuperDoc;
import org.aspectj.ajdoc.IntroductionDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SuperIntroductionSubWriter extends AbstractSubWriter {

    protected Class delegateClass() {
        return null; //XXX ????
    }

    public SuperIntroductionSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         AspectDoc ad)
    {
        super(writer, ad);
    }
    
    public SuperIntroductionSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

    protected final String keyName() { return "Super_Introduction"; }

    public void printInheritedSummaryAnchor(ClassDoc cd) {}
    public void printInheritedSummaryLabel(ClassDoc cd) {}
    protected void printInheritedSummaryLink(ClassDoc cd, ProgramElementDoc ped) {}

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        IntroducedSuperDoc intro = (IntroducedSuperDoc)member;
        writer.codeEnd();
        writer.printText("doclet.declare_parents");
        print(' ');
        Type[] targets = intro.targets();
        for (int i = 0; i < targets.length; i++) {
            if (i > 0) print(", ");
            printTypeLink(targets[i]);
        }
        print(' ');
        String str = intro.isImplements() ? "implements" : "extends";
        writer.printClassLink(cd, link(intro), str, false);
        print(' ');
        Type[] types = intro.types();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) print(", ");
            printTypeLink(types[i]);
        }

    }

    protected static String link(IntroducedSuperDoc intro) {
        String str = intro.isImplements() ? "+implements" : "+extends";
        Type[] types = intro.types();
        str += "%";
        for (int i = 0; i < types.length; i++) str += types[i].qualifiedTypeName();
        str += "%";
        ClassDoc[] targets = intro.targets();
        for (int i = 0; i < targets.length; i++) str += targets[i].qualifiedTypeName();
        return str;
    }

    protected void printSummaryType(ProgramElementDoc member) {}

    protected void printBodyHtmlEnd(ClassDoc cd) {}

    protected void nonfinalPrintMember(ProgramElementDoc member) {
        IntroducedSuperDoc intro = (IntroducedSuperDoc)member;
        writer.anchor(link(intro));
        String head = intro.isImplements() ? "+implements " : "+extends ";
        Type[] types = intro.types();
        for (int i = 0; i < types.length; i++) {
            head += (i > 0 ? ", " : "") + types[i].typeName();
        }
        printHead(head);
        printFullComment(intro);
    }

    protected void printDeprecatedLink(ProgramElementDoc member) {
        //TODO: ???
    }

    protected List getMembers(ClassDoc cd) {
        if (!(cd instanceof AspectDoc)) return Collections.EMPTY_LIST;
        IntroductionDoc[] introductions = ((AspectDoc)cd).introductions();
        List list = new ArrayList();
        if (introductions == null) return list;
        for (int i = 0; i < introductions.length; i++) {
            IntroductionDoc intro = introductions[i];
            if (intro instanceof IntroducedSuperDoc) {
                list.add(intro);
            }
        }
        return list;
    }

    public void printCrosscuts(ClassDoc cd, ProgramElementDoc member) {
        org.aspectj.ajdoc.IntroducedSuperDoc intro =
            (org.aspectj.ajdoc.IntroducedSuperDoc)member;
        ClassDoc[] targets = intro.targets();
        if (targets.length > 0) {
            writer.dt();
            writer.boldText("doclet.Introduced_on");
            writer.dd();
            writer.code();
            Set targetSet = new HashSet();
            for (int i = 0; i < targets.length; i++) {
                targetSet.add(targets[i]);
            }
            List targetList = new ArrayList(targetSet);
            Collections.sort(targetList);
            for (Iterator i = targetList.iterator(); i.hasNext();) {
                ClassDoc target = (ClassDoc)i.next();
                writer.printClassLink(target);
                if (i.hasNext()) writer.print(", ");
                
            }
            writer.codeEnd();
        }
    }

    public void printSummaryCrosscuts(ClassDoc cd,
                                      ProgramElementDoc member) {
        Set set = new HashSet();
        org.aspectj.ajdoc.IntroducedSuperDoc intro =
            (org.aspectj.ajdoc.IntroducedSuperDoc)member;
        ClassDoc[] targets = intro.targets();
        for (int i = 0; i < targets.length; i++) {
            set.add(targets[i]);
        }
        if (targets.length > 0) {
            writer.boldText("doclet.Advises");
            List list = new ArrayList(set);
            Collections.sort(list);
            for (Iterator i = list.iterator(); i.hasNext();) {
                print(' ');
                ClassDoc target = (ClassDoc)i.next();
                writer.printClassLink(target);
                if (i.hasNext()) print(",");
            }
        }
    }

    public boolean hasCrosscuts(ClassDoc cd, ProgramElementDoc member) {
        return true;
    }
}
