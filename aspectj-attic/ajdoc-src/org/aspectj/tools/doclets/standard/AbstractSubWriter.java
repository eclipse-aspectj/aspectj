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
import org.aspectj.tools.ajdoc.Access;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ProgramElementDoc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class AbstractSubWriter
    extends com.sun.tools.doclets.standard.AbstractSubWriter
    implements AbstractSubWriterAJ
{

    public AbstractSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         ClassDoc classdoc)
    {
        super(writer, classdoc);
    }

    public AbstractSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

    public final void printSummaryMember(ClassDoc cd, ProgramElementDoc member) {
        writer.printSummaryLinkType(this, member);
        printSummaryLink(cd, member);
        printSummaryLinkComment(member);
    }
    
    protected final void printCrosscuts(ProgramElementDoc member) {
        if (hasCrosscuts(classdoc, member)) {
            writer.dl();
            writer.dd();
            printCrosscuts(classdoc, member);
            writer.ddEnd();
            writer.dlEnd();
        }
    }
    
    protected /*abstract*/ Class delegateClass() { throw new RuntimeException(""); }
    /*final*/ private com.sun.tools.doclets.standard.AbstractSubWriter del;
    { setDelegator(); }
    protected final void setDelegator() {
        com.sun.tools.doclets.standard.AbstractSubWriter mw = null;
        try {
            List list = new ArrayList();
            if (writer != null) list.add(writer);
            if (classdoc != null) list.add(classdoc);
            setDelegator(mw = findDelegate
                         (list.toArray
                          (new Object[list.size()])));
        } finally {
            del = mw;
        }
    }
    public final com.sun.tools.doclets.standard.AbstractSubWriter del() {
        return del;
    }
    private final void setDelegator(Object o) {
        if (o == null) return;
        try {
            Method[] ms = o.getClass().getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getName().equals("setDelegator")) {
                    ms[i].invoke(o, new Object[]{this});
                }
            }
        } catch (Exception e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered", e+"");
        }
    }
    private final com.sun.tools.doclets.standard.AbstractSubWriter
        findDelegate(Object[] params) {
        if (delegateClass() == null) {
            return this;
        }
        try {
            Constructor[] cs = delegateClass().getConstructors();
            for (int i = 0; i < cs.length; i++) {
                if (cs[i].getParameterTypes().length == params.length) {
                    return (com.sun.tools.doclets.standard.AbstractSubWriter)
                        cs[i].newInstance(params);
                }
            }
        } catch (Exception e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered", e+"");
        }
        return null;
    }

    public int getMemberKind() {
        return -1;
    }
    public void printSummaryLabel(ClassDoc cd) {
        if (keyName() != null) {
            summaryLabel(keyName());
        } else {
            del.printSummaryLabel(cd);
        }
    }
    public void printSummaryAnchor(ClassDoc cd) {
        if (keyName() != null) {
            summaryAnchor(keyName());
        } else {
            del.printSummaryAnchor(cd);
        }
    }
    public void printInheritedSummaryAnchor(ClassDoc cd) {
        del.printInheritedSummaryAnchor(cd);
    }
    protected void printSummaryType(ProgramElementDoc member) {
        Access.printSummaryType(this, member);
    }
    protected void printSummaryLink(ClassDoc cd,
                                    ProgramElementDoc member) {
        Access.printSummaryLink(this, cd, member);
    }
    protected void printInheritedSummaryLink(ClassDoc cd, 
                                             ProgramElementDoc member) {
        Access.printInheritedSummaryLink(this, cd, member);
    }
    protected void printHeader(ClassDoc cd) {
        if (keyName() != null) {
            header(keyName());
        } else {
            Access.printHeader(this, cd);
        }
    }
    protected void printBodyHtmlEnd(ClassDoc cd) {
        Access.printBodyHtmlEnd(this, cd);
    }
    protected final void printMember(ProgramElementDoc elem) {
        nonfinalPrintMember(elem);
        printCrosscuts(elem);
    }
    protected void nonfinalPrintMember(ProgramElementDoc elem) {
        Access.printMember(this, elem);
    }
    protected void printDeprecatedLink(ProgramElementDoc member) {
        Access.printDeprecatedLink(this, member);
    }
    protected void printNavSummaryLink(ClassDoc cd, boolean link) {
        if (keyName() != null) {
            navSummaryLink(cd, keyName(), link);
        } else {
            Access.printNavSummaryLink(this, cd, link);
        }
    }
    protected void printNavDetailLink(boolean link) {
        if (keyName() != null) {
            navDetailLink(keyName(), link);
        } else {
            Access.printNavDetailLink(this, link);
        }
    }

    protected /*abstract*/ String propertyName() { return keyName(); }
    protected /*abstract*/ String keyName() { return null; }
    protected final String lowercase() {
        String str = propertyName();
        return str == null || str.length() == 0 ? "" :
            Character.toLowerCase(str.charAt(0)) +
            str.substring(1);
    }
    public void printInheritedSummaryLabel(ClassDoc cd) {
        summaryLabel("Inherited_From", cd);
    }
    public void printIntroducedSummaryLabel(ClassDoc cd) {
        summaryLabel("Introduced_From", cd);
    }
    public void printIntroducedSummaryAnchor(ClassDoc cd) {
        summaryAnchor("introduced_from_class", cd);
    }
    protected final void summaryLabel(String keyName) {
        writer.boldText("doclet." + keyName + "_Summary");
    }
    protected final void summaryAnchor(String keyName) {
        writer.anchor(keyName.toLowerCase() + "_summary");
    }
    protected final void summaryLabel(String type, ClassDoc cd) {
        writer.bold();
        writer.printText("doclet." + propertyName() + "s_" + type,
                         Statics.type(cd),
                         writer.getPreQualifiedClassLink(cd));
        writer.boldEnd();
    }
    protected final void summaryAnchor(String type, ClassDoc cd) {
        writer.anchor(lowercase() + "s_" + type + "_" + cd.qualifiedName());
    }
    protected final String navKey(String keyName) {
        return "doclet.nav" + keyName;
    }
    protected final String navKey() {
        return navKey(keyName());
    }
    protected final void navLink(ClassDoc cd, String keyName,
                                 boolean link, String kind) {
        if (link) {
            writer.printHyperLink
                ("",
                 cd == null ?
                 keyName.toLowerCase() + "_" + kind :
                 keyName.toLowerCase() + "s_inherited_from_class_" +
                 cd.qualifiedName(),
                 writer.getText(navKey(keyName)));
        } else {
            writer.printText(navKey(keyName));
        }
    }
    protected final void navSummaryLink(ClassDoc cd, String keyName, boolean link) {
        navLink(cd, keyName, link, "summary");
    }
    protected final void navDetailLink(String keyName, boolean link) {
        navLink(null, keyName, link, "detail");
    }
    protected final void header(String keyName) {
        writer.anchor(keyName.toLowerCase() + "_detail");
        writer.printTableHeadingBackground
            (writer.getText("doclet." + keyName + "_Detail"));
    }
    
    public void printCrosscuts(ClassDoc cd, ProgramElementDoc member) {}
    public void printSummaryCrosscuts(ClassDoc cd, ProgramElementDoc member) {}
    public boolean hasCrosscuts(ClassDoc cd,ProgramElementDoc member) {
        return false;
    }

    protected void printHead(String name) {
        writer.h3();
        writer.print(name);
        writer.h3End();
    }

    protected List nointros(List members) {
        List list = new ArrayList();
        for (Iterator i = members.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof org.aspectj.ajdoc.MemberDoc) {
                IntroducedDoc intro =
                    ((org.aspectj.ajdoc.MemberDoc)o).introduced();
                if (intro == null || intro.containingClass().equals(classdoc)) {
                    list.add(o);
                }
            } else {
                list.add(o);
            }
        }
        return list;
    }

    public void printMembersSummary() {
        nointros = true;
        super.printMembersSummary();
        nointros = false;
    }

    protected final void printSummaryLinkComment(ProgramElementDoc member) {
        writer.codeEnd();
        writer.println();
        writer.br();
        Access.printCommentDef(this, member);
        boolean newline = member.tags("deprecated").length +
            member.firstSentenceTags().length > 0;
        if (classdoc != null || true) { // ?? always
            if (hasCrosscuts(classdoc, member)) {
                if (newline) {
                    writer.br();
                    writer.printNbsps();
                }
                printSummaryCrosscuts(classdoc, member);
            }
        }
        writer.summaryRowEnd();
        writer.trEnd();        
    }

    public void printIntroducedSummaryHeader(ClassDoc cd) {
            printIntroducedSummaryAnchor(cd);
            writer.tableIndexSummary();
            writer.tableInheritedHeaderStart("#EEEEFF");
            printIntroducedSummaryLabel(cd);
            writer.tableInheritedHeaderEnd();
            writer.trBgcolorStyle("white", "TableRowColor");
            writer.summaryRow(0);
            writer.code();
    }
    
    public void printIntroducedSummaryFooter(ClassDoc cd) {
            writer.codeEnd();
            writer.summaryRowEnd();
            writer.trEnd(); 
            writer.tableEnd();
            writer.space();
    }
    
    public void printIntroducedSummaryMember(ClassDoc cd,
                                             ProgramElementDoc member) {
        printIntroducedSummaryLink(cd, member);
    }
    
    public void printIntroducedSummaryLink(ClassDoc cd,
                                           ProgramElementDoc member) {}
    public void printIntroducedMembersSummary() {
        Map typesToMembers = new HashMap();
        for (Iterator i = members(classdoc).iterator(); i.hasNext();) {
            Object o = i.next();
            if (!(o instanceof org.aspectj.ajdoc.MemberDoc)) continue;
            org.aspectj.ajdoc.MemberDoc md = (org.aspectj.ajdoc.MemberDoc)o;
            if (md.introduced() != null) {
                ClassDoc cd = md.introduced().containingClass();
                List members = (List)typesToMembers.get(cd);
                if (members == null) members = new ArrayList();
                members.add(md);
                typesToMembers.put(cd, members);
            }
        }
        for (Iterator i = typesToMembers.keySet().iterator(); i.hasNext();) {
            ClassDoc cd = (ClassDoc)i.next();
            printIntroducedSummaryHeader(cd);
            List members = (List)typesToMembers.get(cd);
            Collections.sort(members);
            for (Iterator j = members.iterator(); j.hasNext();) {
                printIntroducedSummaryMember(cd, (ProgramElementDoc)j.next());
                if (j.hasNext()) print(", ");
            }
            printIntroducedSummaryFooter(cd);
        }
    }


    private boolean nointros = false;
    public void printMembers() {
        nointros = true;
        super.printMembers();
        nointros = false;
    }

    public void navSummaryLink() {
        nointros = true;
        super.navSummaryLink();
        nointros = false;
    }
        
    protected void navDetailLink() {
        printNavDetailLink(members(classdoc).size() > 0 ? true : false);
    }
    
    public final List members(ClassDoc cd) {
        return nointros ? nointros(getMembers(cd)) : getMembers(cd);
    }
    
    protected List getMembers(ClassDoc cd) {
        return super.members(cd);
    }
}  
    
    
