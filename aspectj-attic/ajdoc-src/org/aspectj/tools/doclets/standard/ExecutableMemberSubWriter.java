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
import org.aspectj.ajdoc.IntroducedDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ProgramElementDoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class ExecutableMemberSubWriter extends AbstractSubWriter {
    public ExecutableMemberSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer, 
         ClassDoc classdoc)
    {
        super(writer, classdoc);
    }
    
    public ExecutableMemberSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

    protected String where(ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        return emd.name() + emd.signature();
    }

    protected String label(ExecutableMemberDoc emd) {
        return emd.qualifiedName() + emd.flatSignature();
    }

    public void printIntroducedSummaryLink(ClassDoc cd, 
                                           ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
	writer.printClassLink(cd, where(emd), emd.name(), false);
    }

    public void printCrosscuts(ClassDoc cd, ProgramElementDoc member) {
        org.aspectj.ajdoc.ExecutableMemberDoc emd =
            (org.aspectj.ajdoc.ExecutableMemberDoc)member;
        
        IntroducedDoc intro = emd.introduced();
        if (intro != null) {
            writer.dt();
            writer.boldText("doclet.Introduced_from");
            writer.dd();
            org.aspectj.ajdoc.MemberDoc otherMember =
                (org.aspectj.ajdoc.MemberDoc)intro.member();
            String name = where(otherMember);
            ClassDoc containing = intro.containingClass();
            writer.printClassLink(containing, name,
                                  containing.typeName(), false);
        }
        
        AdviceDoc[] advice = emd.advice();
        if (advice.length > 0) {
            writer.boldText("doclet.Crosscut_by");
            Set set = new HashSet();
            for (int i = 0; i < advice.length; i++) {
                set.add(advice[i]);
            }
            List list = new ArrayList(set);
            Collections.sort(list);
            for (Iterator i = list.iterator(); i.hasNext();) {
                writer.dd();
                writer.code();
                AdviceDoc ad = (AdviceDoc)i.next();
                writer.printClassLink(ad.containingClass(),
                                      AdviceSubWriter.getWhere
                                      (ad.containingClass(), ad),
                                      label(ad));
                print(" in ");
                writer.printClassLink(ad.containingClass());
                writer.codeEnd();
                print('.');
            }
        }
    }
    
    public void printSummaryCrosscuts(ClassDoc cd,
                                      ProgramElementDoc member) {
        Set cds = new HashSet();
        org.aspectj.ajdoc.ExecutableMemberDoc emd =
            (org.aspectj.ajdoc.ExecutableMemberDoc)member;
        AdviceDoc[] advice = emd.advice();
        for (int i = 0; i < advice.length; i++) {
            cds.add(advice[i].containingClass());
        }        
        if (cds.size() > 0) {
            writer.boldText("doclet.Advised_by");
            List list = new ArrayList(cds);
            Collections.sort(list);
            for (Iterator i = list.iterator(); i.hasNext();) {
                print(' ');
                ClassDoc cdoc = (ClassDoc)i.next();
                writer.printClassLink(cdoc, "advice_detail", cdoc.name());
                if (i.hasNext()) print(",");
            }
        }
    }

    public boolean hasCrosscuts(ClassDoc classDoc,
                                ProgramElementDoc member) {
        org.aspectj.ajdoc.ExecutableMemberDoc emd =
            (org.aspectj.ajdoc.ExecutableMemberDoc)member;
        return emd.introduced() != null || emd.advice().length > 0;
    }
}  
    
    
