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
//package com.sun.tools.doclets.standard;
package org.aspectj.tools.ajdoc;

import org.aspectj.tools.doclets.standard.AbstractSubWriter;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.standard.ClassUseWriter;
import com.sun.tools.doclets.standard.ExecutableMemberSubWriter;
import com.sun.tools.doclets.standard.SubWriterHolderWriter;

import java.util.List;
import java.util.SortedSet;

/**
 * Allows for access to protected and package-protected (OK
 * sometimes private) members in classes in the package
 * com.sun.tools.doclets...
 *
 * @author Jeff Palm
 */
public class Access {

    public static void printSummaryType(AbstractSubWriter mw,
                                        ProgramElementDoc member) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printSummaryType",
                    new Class[]{ProgramElementDoc.class},
                    new Object[]{member});
    }
    public static void printSummaryLink(AbstractSubWriter mw,
                                        ClassDoc cd,
                                        ProgramElementDoc member) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printSummaryLink",
                    new Class[]{ClassDoc.class,
                                ProgramElementDoc.class},
                    new Object[]{cd, member});
    }
    public static void printInheritedSummaryLink(AbstractSubWriter mw,
                                                 ClassDoc cd,
                                                 ProgramElementDoc member) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printInheritedSummaryLink",
                    new Class[]{ClassDoc.class,
                                ProgramElementDoc.class},
                    new Object[]{cd, member});
    }
    public static void printHeader(AbstractSubWriter mw,
                                   ClassDoc cd) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printHeader",
                    new Class[]{ClassDoc.class},
                    new Object[]{cd});
    }
    public static void printBodyHtmlEnd(AbstractSubWriter mw,
                                        ClassDoc cd) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printBodyHtmlEnd",
                    new Class[]{ClassDoc.class},
                    new Object[]{cd});
    }
    public static void printMember(AbstractSubWriter mw,
                                   ProgramElementDoc elem) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printMember",
                    new Class[]{ProgramElementDoc.class},
                    new Object[]{elem});
    }
    public static void printDeprecatedLink(AbstractSubWriter mw,
                                           ProgramElementDoc member) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printDeprecatedLink",
                    new Class[]{ProgramElementDoc.class},
                    new Object[]{member});
    }
    public static void printNavSummaryLink(AbstractSubWriter mw,
                                           ClassDoc cd,
                                           boolean link) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printNavSummaryLink",
                    new Class[]{ClassDoc.class,
                                boolean.class},
                    new Object[]{cd, new Boolean(link)});
    }
    public static void printNavDetailLink(AbstractSubWriter mw,
                                          boolean link) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printNavDetailLink",
                    new Class[]{boolean.class},
                    new Object[]{new Boolean(link)});
    }
    public static void printTags(AbstractSubWriter mw,
                                 ProgramElementDoc member) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printTags",
                    new Class[]{ProgramElementDoc.class},
                    new Object[]{member});
    }
    public static void printDeprecatedAPI(AbstractSubWriter mw,
                                   List deprmembers,
                                   String headingKey) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw.del(),
                    "printDeprecatedAPI",
                    new Class[]{List.class, String.class},
                    new Object[]{deprmembers, headingKey});
    }
    public static void printParameters(ExecutableMemberSubWriter mw,
                                       ExecutableMemberDoc member) {
        Util.invoke(com.sun.tools.doclets.standard.ExecutableMemberSubWriter.class,
                    mw,
                    "printParameters",
                    new Class[]{ExecutableMemberDoc.class},
                    new Object[]{member});
    }
    public static void printUseInfo(AbstractSubWriter mw,
                                    Object mems,
                                    String heading) {
        printUseInfo(mw.del(), mems, heading);
    }
    public static void printUseInfo
        (com.sun.tools.doclets.standard.AbstractSubWriter mw,
         Object mems,
         String heading) {
        if (mw != null) {
            Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                        mw,
                        "printUseInfo",
                        new Class[]{Object.class, String.class},
                        new Object[]{mems, heading});
        }
    }
    public static void printCommentDef(AbstractSubWriter mw, Doc doc) {
        Util.invoke(SubWriterHolderWriter.class,
                    writer(mw),
                    "printCommentDef",
                    new Class[]{Doc.class},
                    new Object[]{doc});
    }
    public static SubWriterHolderWriter writer(AbstractSubWriter mw) {
        return (SubWriterHolderWriter)
            Util.access(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                        mw, "writer");
    }
    public static SortedSet pkgSet(ClassUseWriter writer) {
        return (SortedSet)Util.access(ClassUseWriter.class,
                                      writer,
                                      "pkgSet");
    }
    public static ClassDoc classdoc(ClassUseWriter writer) {
        return (ClassDoc)Util.access(ClassUseWriter.class,
                                     writer,
                                     "classdoc");
    }
    public static void print(com.sun.tools.doclets.standard.AbstractSubWriter mw,
                             String str) {
        Util.invoke(com.sun.tools.doclets.standard.AbstractSubWriter.class,
                    mw,
                    "print",
                    new Class[]{String.class},
                    new Object[]{str});
    }
}
