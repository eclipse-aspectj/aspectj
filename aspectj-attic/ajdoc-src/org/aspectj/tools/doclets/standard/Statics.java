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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.tools.doclets.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A splattering of misc. functionality.
 *
 * @author Jeff Palm
 */
public class Statics {

    /**
     * Returns a aspectj-world type String of <code>cd</code>.
     *
     * @return either aspect interface or class depending
     *         on the type of <code>cd</code>.
     */
    public static String type(ClassDoc cd) {
        return cd instanceof AspectDoc
            ? "aspect" : cd.isInterface()
            ? "interface" : "class";
    }

    /**
     * Returns the link target for <code>member</code>.
     *
     * @param member the ProgramElementDoc in question.
     * @return       the link target for <code>member</code>.
     */
    public static String where(ProgramElementDoc member) {
        return member.name();
    }

    /**
     * Returns the link label for <code>member</code>.
     *
     * @param member the ProgramElementDoc in question.
     * @return       the link label for <code>member</code>.
     */
    public static String label(ProgramElementDoc member) {
        return member.name();
    }

    /**
     * Returns the link target for <code>member</code> from
     * <code>cd</code>.
     *
     * @param cd     the class from which we're linking.
     * @param member the ProgramElementDoc in question.
     * @return       the link target for <code>member</code>.
     */
    public static String where(ClassDoc cd, ProgramElementDoc member) {
        if (member instanceof AdviceDoc) {
            return name(cd, (AdviceDoc)member).replace(' ','_').replace('#','-');
        }
        return member.name();
    }

    /**
     * Returns the link label for <code>member</code> from
     * <code>cd</code>.
     *
     * @param cd     the class from which we're linking.
     * @param member the ProgramElementDoc in question.
     * @return       the link target for <code>member</code>.
     */
    public static String label(ClassDoc cd, ProgramElementDoc member) {
        return name(cd, member);
    }

    /**
     * Returns the name for <code>member</code> from
     * <code>cd</code>.  This is here because we don't
     * want really print the name of advice.
     *
     * @param cd     the class from which we're printing.
     * @param member the ProgramElementDoc in question.
     * @return       the name for <code>member</code>.
     */
    public static String name(ClassDoc cd, ProgramElementDoc member) {
        if (member instanceof AdviceDoc) {
            return name(cd, (AdviceDoc)member);
        }
        return member.name();
    }

    /**
     * Returns the String that should be printed for
     * an advice's name.
     *
     * @param cd     the ClassDoc from where we're printing.
     * @param advice the member in question.
     * @return       correct printing name for <code>advice</code>.
     */
    public static String name(ClassDoc cd, AdviceDoc advice) {
        String name = advice.name();
        int num = 1;
        for (Iterator i = advice(cd).iterator(); i.hasNext();) {
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

    /**
     * Returns the advice contained in <code>classdoc</code>.
     *
     * @param  cd ClassDoc in question.
     * @return    a List with the {@link AdviceDoc}s
     *            contained in <code>cd</code>.
     */
    public static List advice(ClassDoc classdoc) {
        if (!(classdoc instanceof AspectDoc)) return Collections.EMPTY_LIST;
        AdviceDoc[] advice = ((AspectDoc)classdoc).advice();
        return advice == null ? Collections.EMPTY_LIST : Util.asList(advice);
    }
    
    /**
     * Returns an array of classes only.
     *
     * @param arr source array from where the ClassDocs in
     *            the result will come.
     * @return an array of ClassDoc containing only
     *         classes, <b>no aspects</b>.
     */
    public static ClassDoc[] classes(ClassDoc[] arr) {
        List list = new ArrayList();
        for (int i = 0; i < arr.length; i++) {
            if (!(arr[i] instanceof AspectDoc)) {
                list.add(arr[i]);
            }
        }
        return (ClassDoc[])list.toArray(new ClassDoc[list.size()]);
    }

    /**
     * Returns a list of the classes found in the
     * passed in list of types.
     *
     * @param types List of ClassDocs.
     * @return      a List containing those ClassDocs in
     *              <code>types</code> that <i>are not</i> aspects.
     * @see         #types(List,boolean)
     */
    public static List classes(List types) {
        return types(types, false);
    }

    /**
     * Returns a list of the classes found in the
     * passed in list of types.
     *
     * @param types List of ClassDocs.
     * @return      a List containing those ClassDocs in
     *              <code>types</code> that <i>are</i> aspects.
     * @see         #types(List,boolean)
     */
    public static List aspects(List types) {
        return types(types, true);
    }

    /**
     * Returns a list of ClassDocs taken from <code>types</code>
     * that are aspects iff <code>wantAspects</code>.
     *
     * @param types       source List of ClassDocs.
     * @param wantAspects ClassDocs <i>c<i>in the resulting List will
     *                    conform to the test:
     *                    <code>c instanceof AspectDoc) == wantAspects<code>.
     * @return            a List of ClassDocs all who conform to the test:
     *                    <code>c instanceof AspectDoc) == wantAspects<code>.
     */
    public static List types(List types, boolean wantAspects) {
        List list = new ArrayList();
        for (Iterator i = types.iterator(); i.hasNext();) {
            ClassDoc cd = (ClassDoc)i.next();
            if ((cd instanceof AspectDoc) == wantAspects) {
                list.add(cd);
            }
        }
        return list;
    }

    /**
     * Returns a prequalified class link to <code>cd</code> using
     * the link target <code>where</code>.
     *
     * @param writer base writer to use.
     * @param cd     class to where we're linking.
     * @param where  link target.
     * @return       prequalified class link using
     *               <code>cd</code> and <code>where</code>.
     */
    public static String getPreQualifiedClassLink
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer, 
         ClassDoc cd,
         String where) {
        return writer.getPkgName(cd) + writer.getClassLink(cd, where, cd.name());
    }

    /**
     * Returns a prequalified class link to <code>cd</code> using
     * the link target <code>where</code> returned by
     * calling <code>getPreQualifiedClassLink</code>.
     *
     * @param writer base writer to use.
     * @param cd     class to where we're linking.
     * @param where  link target.
     * @see          #getPreQualifiedClassLink
     */
    public static void printPreQualifiedClassLink
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer, 
         ClassDoc cd,
         String where) {
        writer.print(getPreQualifiedClassLink(writer, cd, where));
    }
}
