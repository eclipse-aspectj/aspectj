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
import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.tools.ajdoc.Access;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class AdviceSubWriter extends ExecutableMemberSubWriter {

    protected Class delegateClass() {
        return MethodSubWriter.class;
    }

    public AdviceSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         AspectDoc aspectdoc)
    {
        super(writer, aspectdoc);
    }

    public AdviceSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

    protected final String keyName() { return "Advice"; }
   
    protected String where(ProgramElementDoc member) {
        return getWhere(classdoc, member);
    }
    
    public static String getWhere(ClassDoc cd, ProgramElementDoc member) {
        return getName(cd, (AdviceDoc)member).replace(' ','_').replace('#','-');
    }

    protected void printSummaryType(ProgramElementDoc member) {
        AdviceDoc advice = (AdviceDoc)member;
        printModifiers(advice);
    }

    protected void printReturnTag(Tag[] returnsTag) {
        if (returnsTag.length > 0) {
            writer.dt();
            writer.boldText("doclet.Returns");
            writer.dd();
            writer.printInlineComment(returnsTag[0]);
        }
    }

    protected void printTagsInfoHeader() {
        writer.dd();
        writer.dl();
    }

    protected void printTagsInfoFooter() {
        writer.dlEnd();
        writer.ddEnd();
    }

    protected void printSignature(ExecutableMemberDoc member) {
        AdviceDoc advice = (AdviceDoc)member;
        writer.displayLength = 0;
        writer.pre();
        printReturnType(advice);
        bold(advice.name());
        printParameters(advice);
        if (advice.isThrowing()) {
            writer.print(" throwing ");
            printExtraType(advice.extraType());
        }
        if (advice.isReturning()) {
            writer.print(" returning ");
            printExtraType(advice.extraType());
        }
        writer.preEnd();
    }

    protected void printExtraType(Type type) {
        print(' ');
        print('(');
        if (type != null) {
            printTypeLink(type);
        }
        print(')');
    }

    public static String getName(ClassDoc cd, AdviceDoc advice) {
        String name = advice.name();
        int num = 1;
        for (Iterator i = staticMembers(cd).iterator(); i.hasNext();) {
            AdviceDoc ad = (AdviceDoc)i.next();
            if (ad.equals(advice)) {
                break;
            }
            if (ad.name().equals(name)) {
                num++;
            }
        }
        return name + " #" + num;
    }

    protected String name(ProgramElementDoc member) {
        return getName(classdoc, (AdviceDoc)member);
    }

    protected void printParameters(ExecutableMemberDoc member) {
        //AdviceDoc advice = (AdviceDoc)member;
        Access.printParameters
            ((com.sun.tools.doclets.standard.ExecutableMemberSubWriter)
             ((AbstractSubWriter)del()).del(),
             member);
    }

    protected void printReturnType(AdviceDoc advice) {
        Type type = advice.returnType();
        if (type != null) {
            printTypeLink(type);
            print(' ');
        }
    }

    public void nonfinalPrintMember(ProgramElementDoc elem) {
        AdviceDoc advice = (AdviceDoc)elem;
        writer.anchor(where(advice));
        printHead(advice);
        printSignature(advice);
        printFullComment(advice);
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        String name = emd.name();
        writer.bold();
        writer.printClassLink(cd, where(emd), name, false);
        writer.boldEnd();
        writer.displayLength = name.length();
        printParameters(emd);
    }

    protected void printHead(MemberDoc member) {
        printHead(name(member));
    }

    protected static List staticMembers(ClassDoc classdoc) {
        if (!(classdoc instanceof AspectDoc)) return Collections.EMPTY_LIST;
        AdviceDoc[] advice = ((AspectDoc)classdoc).advice();
        return advice == null ? Collections.EMPTY_LIST : Util.asList(advice);
    }
    
    protected List getMembers(ClassDoc classdoc) {
        return staticMembers(classdoc);
    }

    public void printCrosscuts(ClassDoc cd, ProgramElementDoc member) {
        if (!(cd instanceof AspectDoc)) return;
        //AspectDoc ad = (AspectDoc)cd;
        AdviceDoc advice = (AdviceDoc)member;
        ExecutableMemberDoc[] crosscuts = advice.crosscuts();
        if (null != crosscuts && crosscuts.length > 0) {
            writer.dt();
            writer.boldText("doclet.Crosscuts");
            Set set = new HashSet();
            for (int i = 0; i < crosscuts.length; i++) {
                set.add(crosscuts[i]);
            }
            List list = new ArrayList(set);
            Collections.sort(list);
            for (Iterator i = list.iterator(); i.hasNext();) {
                ExecutableMemberDoc emd = (ExecutableMemberDoc)i.next();
                if (null != emd) {
                    writer.dd();
                    writer.code();
                    String where = emd instanceof AdviceDoc
                        ?          where(emd)
                        :    super.where(emd);
                    writer.printClassLink(emd.containingClass(),
                                          where, label(emd));
                    print(" in ");
                    writer.printClassLink(emd.containingClass());
                    writer.codeEnd();
                    print('.');
                }
            }
        }
    }

    public void printSummaryCrosscuts(ClassDoc cd,
                                         ProgramElementDoc member) {

        class CCs extends TreeMap {
            void add(ExecutableMemberDoc cc) {
                if (null != cc) { 
                    Object o = get(cc.containingClass());
                    if (o == null) {
                        o = cc;
                    } else if (o instanceof ExecutableMemberDoc) {
                        o = new Integer(2);
                    } else {
                        o = new Integer(((Integer)o).intValue()+1);
                    }
                    put(cc.containingClass(), o);
                }
            }
        }
        
        ExecutableMemberDoc[] crosscuts = ((AdviceDoc)member).crosscuts();
        if (crosscuts.length > 0) {
            writer.boldText("doclet.Advises");
            CCs ccs = new CCs();
            for (int i = 0; i < crosscuts.length; i++) {
                ccs.add(crosscuts[i]);
            }
            for (Iterator i = ccs.keySet().iterator(); i.hasNext();) {
                print(' ');
                ClassDoc target = (ClassDoc)i.next();
                Object o = ccs.get(target);
                String link;
                String name = target.name();
                if (o instanceof AdviceDoc) {
                    link = where((AdviceDoc)o);
                } else if (o instanceof ExecutableMemberDoc) {
                    link = super.where((ExecutableMemberDoc)o);
                } else {
                    link = "method_detail";
                    name += "(" + o + ")";
                }
                writer.printClassLink(target, link, name);
                if (i.hasNext()) print(",");
            }
        }
    }

    public boolean hasCrosscuts(ClassDoc classDoc, ProgramElementDoc member) {
        return true;
    }
}
