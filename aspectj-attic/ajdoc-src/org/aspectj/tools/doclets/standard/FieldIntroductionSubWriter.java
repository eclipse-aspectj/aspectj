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
import org.aspectj.ajdoc.IntroductionDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.ProgramElementDoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FieldIntroductionSubWriter extends FieldSubWriter {

    public FieldIntroductionSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         AspectDoc ad)
    {
        super(writer, ad);
    }
    
    public FieldIntroductionSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

    protected final String keyName() { return "Field_Introduction"; }

    public void printInheritedSummaryAnchor(ClassDoc cd) {}
    public void printInheritedSummaryLabel(ClassDoc cd) {}
    protected void printInheritedSummaryLink(ClassDoc cd, ProgramElementDoc ped) {}

    protected List getMembers(ClassDoc cd) {
        if (!(cd instanceof AspectDoc)) return super.getMembers(cd);
        IntroductionDoc[] introductions = ((AspectDoc)cd).introductions();
        List list = new ArrayList();
        if (introductions == null) return list;
        for (int i = 0; i < introductions.length; i++) {
            IntroductionDoc id = introductions[i];
            if (!(id instanceof IntroducedDoc)) continue;
            MemberDoc member = ((IntroducedDoc)id).member();
            if (member.isField()) {
                //FieldDec field = (FieldDec)member;
                //TODO: field.bindSignatures(((ClassDec)cd).getTypeScope());
                list.add(member); //field);
            }
        }
        return list;
    }
    
    /** print links back to aspect in target class docs? 
     * or forward from aspects to target class members? */
    public void printCrosscuts(ClassDoc cd, ProgramElementDoc member) {
        org.aspectj.ajdoc.FieldDoc field = (org.aspectj.ajdoc.FieldDoc)member;
        IntroducedDoc intro = field.introduced();
        //String name = field.name();
        ClassDoc[] targets = intro.targets();
        if (targets.length > 0) {
            writer.dt(); // define term
            writer.boldText("doclet.Introduced_on");
            writer.dd();
            writer.code();
            for (int i = 0; i < targets.length; i++) {
                if (i > 0) writer.print(", ");
                ClassDoc target = targets[i];
                writer.printClassLink(target,
                                      "fields_introduced_from_class_" +
                                      cd.qualifiedName(),
                                      target.name());
                     
            }
            writer.codeEnd();
            writer.ddEnd();      // XXX added for balance
        }
    }

    public void printSummaryCrosscuts(ClassDoc cd,
                                      ProgramElementDoc member) {
        Set set = new HashSet();
        org.aspectj.ajdoc.MemberDoc md = (org.aspectj.ajdoc.MemberDoc)member;
        IntroducedDoc intro = md.introduced();
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
                writer.printClassLink(target,
                                      "fields_introduced_from_class_"
                                      + cd.qualifiedName(),
                                      target.name());
                if (i.hasNext()) print(",");
            }
        }
    }

    public boolean hasCrosscuts(ClassDoc classDoc, ProgramElementDoc member) {
        return true;
    }
}
