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
import org.aspectj.ajdoc.IntroducedDoc;
import org.aspectj.ajdoc.IntroducedSuperDoc;
import org.aspectj.ajdoc.IntroductionDoc;
import org.aspectj.ajdoc.OfClauseDoc;
import org.aspectj.ajdoc.OfEachObjectDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.tools.doclets.ClassTree;
import com.sun.tools.doclets.DirectoryManager;
import com.sun.tools.doclets.DocletAbortException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ClassWriter extends com.sun.tools.doclets.standard.ClassWriter {

    /**
     * The MethodSubWriter that prints out the methods
     * of <code>classdoc</code>.
     */
    protected MethodSubWriter ourMethodSubWriter;
    
    /**
     * The ConstructorSubWriter that prints out the constructors
     * of <code>classdoc</code>.
     */
    protected ConstructorSubWriter ourConstrSubWriter;
    
    /**
     * The FieldSubWriter that prints out the fields
     * of <code>classdoc</code>.
     */
    protected FieldSubWriter ourFieldSubWriter;
    
    /**
     * The ClassSubWriter that prints out the classs
     * of <code>classdoc</code>.
     */
    protected ClassSubWriter ourInnerSubWriter;
    
    /**
     * The PointcutSubWriter that prints out the pointcuts
     * of <code>classdoc</code>.
     */
    protected PointcutSubWriter pointcutSubWriter = null;
    
    /**
     * The SuperIntroductionSubWriter that prints out the superintroductions
     * of <code>classdoc</code>.
     */
    protected SuperIntroductionSubWriter superIntroductionSubWriter = null;
    
    /**
     * The FieldIntroductionSubWriter that prints out the fieldintroductions
     * of <code>classdoc</code>.
     */
    protected FieldIntroductionSubWriter fieldIntroductionSubWriter = null;
    
    /**
     * The ConstructorIntroductionSubWriter that prints out the constructorintroductions
     * of <code>classdoc</code>.
     */
    protected ConstructorIntroductionSubWriter constrIntroductionSubWriter = null;
    
    /**
     * The MethodIntroductionSubWriter that prints out the methodintroductions
     * of <code>classdoc</code>.
     */
    protected MethodIntroductionSubWriter methodIntroductionSubWriter = null;
    
    /**
     * The AdviceSubWriter that prints out the advices
     * of <code>classdoc</code>.
     */
    protected AdviceSubWriter adviceSubWriter = null;
    

    /**
     * Construct a ClassWriter from the passed in arguments.  This
     * will instantiate the subwriters to be used.
     *
     * @param path      the path directory of the html file to generate.
     * @param filename  the html file to generate.
     * @param classdoc  the ClassDoc for which this file will
     *                  be generated.
     * @param prev      the ClassDoc preceding <code>classdoc</code>
     *                  in order of generation.
     * @param next      the ClassDoc following <code>classdoc</code>
     *                  in order of generation.
     * @param classtree the ClassTree to use.
     * @param nopackage whether this <code>classdoc</code>'s package
     *                  is specified to be documented.
     */
    public ClassWriter(String path,
                       String filename,
                       ClassDoc classdoc,
                       ClassDoc prev,
                       ClassDoc next,
                       ClassTree classtree,
                       boolean nopackage)
        throws IOException, DocletAbortException {

        super(path, filename,  classdoc, prev,
              next, classtree, nopackage);

        // Construct the subwriters just for ClassDocs
        // We want our superclass to delegate to our subwriters delegate,
        // but we want to call our subwriters.  So we set our superclasses
        // subwriters to our subwriters delegates.
        ourMethodSubWriter = new MethodSubWriter(this, classdoc);
        methodSubWriter = (com.sun.tools.doclets.standard.MethodSubWriter)
            ourMethodSubWriter.del();
        constrSubWriter = (com.sun.tools.doclets.standard.ConstructorSubWriter)
            (ourConstrSubWriter = new ConstructorSubWriter(this, classdoc)).del();
        fieldSubWriter = (com.sun.tools.doclets.standard.FieldSubWriter)
            (ourFieldSubWriter = new FieldSubWriter(this, classdoc)).del();
        innerSubWriter = (com.sun.tools.doclets.standard.ClassSubWriter)
            (ourInnerSubWriter = new ClassSubWriter(this, classdoc)).del();

        if (classdoc instanceof org.aspectj.ajdoc.ClassDoc) {
            pointcutSubWriter =
                new PointcutSubWriter(this, (org.aspectj.ajdoc.ClassDoc)classdoc);
        }

        // If we've been passed an AspectDoc, create the AspectJ-specfic
        // subwriters
        if (classdoc instanceof AspectDoc) {
            AspectDoc ad = (AspectDoc)classdoc;
            superIntroductionSubWriter  = new SuperIntroductionSubWriter(this, ad);
            fieldIntroductionSubWriter  = new FieldIntroductionSubWriter(this, ad);
            constrIntroductionSubWriter = new ConstructorIntroductionSubWriter(this, ad);
            methodIntroductionSubWriter = new MethodIntroductionSubWriter(this, ad);
            adviceSubWriter             = new AdviceSubWriter(this, ad);
        }
    }

    public static void generate(ClassDoc classdoc,
                                ClassDoc prev, 
                                ClassDoc next,
                                ClassTree classtree, 
                                boolean nopackage)
        throws DocletAbortException {
        ClassWriter cw = null;
        String path = DirectoryManager.getDirectoryPath
            (classdoc.containingPackage());
        String filename = classdoc.name() + ".html";
        try {
            (cw = new ClassWriter(path, filename, classdoc, 
                                  prev, next, classtree, nopackage)).
                generateClassFile();
        } catch (IOException e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered", e+"", filename);
            throw new DocletAbortException();
        } finally {
            if (cw != null) cw.close();
        }
    }

    /**
     * Prints the header of the class -- which is one
     * of <code>class</code>, <code>interface</code> or
     * <code>aspect</code> with the name <code>title</code>.
     *
     * @param title the name of the class.
     */
    public void printHeader(String title) {
        int ispace = title.indexOf(' ');
        if (ispace != -1) {
            title = Statics.type(classdoc) + title.substring(ispace);
        }
        super.printHeader(title);
    }

    /*
     * This is set up to intercept calls to print out
     * the correct type of classdoc before we automatically
     * print 'class' or 'interface'.
     */

    private boolean h2warn = false;

    /**
     * If we've started to print a h2 heading, we're
     * printing the name of the class so get ready to
     * to intercept the call.
     */
    public void h2() {
        h2warn = true;
        super.h2();
    }

    /**
     * After printing the class declaration with the h2 heading
     * turn off the h2 warning.
     */
    public void h2End() {
        h2warn = false;
        super.h2End();
    }

    /**
     * This is where we intercept the call to print so
     * we can print the correct type.
     */
    public void print(String str) {
        if (h2warn) {
            int ispace = str.indexOf(' ');
            if (ispace != -1 && str.charAt(0) == 'C') {
                str = Statics.type(classdoc) + str.substring(ispace);
            }
        }
        super.print(str);
    }

    /**
     * Print the members summary for our AspectJ subwriters.
     */
    protected void printAspectJSummary() {
        printMembersSummary(superIntroductionSubWriter);
        printMembersSummary(fieldIntroductionSubWriter);
        printMembersSummary(constrIntroductionSubWriter);
        printMembersSummary(methodIntroductionSubWriter);
        printMembersSummary(pointcutSubWriter);
        printMembersSummary(adviceSubWriter);
    }

    /**
     * Formats the output correctly for a member summary.
     *
     * @param mw the AbstractSubWriter to use.
     */
    protected final void printMembersSummary(AbstractSubWriter mw) {
        if (mw != null) {
            println();
            println("<!-- === " + getText(mw.navKey()) + " SUMMARY === -->");
            println();
            mw.printMembersSummary();
        }
    }

    /**
     * Print the members detail for our AspectJ subwriters.
     */
    protected void printAspectJDetail() {
        printMembersDetail(superIntroductionSubWriter);
        printMembersDetail(fieldIntroductionSubWriter);
        printMembersDetail(constrIntroductionSubWriter);
        printMembersDetail(methodIntroductionSubWriter);
        printMembersDetail(pointcutSubWriter);
        printMembersDetail(adviceSubWriter);
    }

    /**
     * Formats the output correctly for a member detail.
     *
     * @param mw the AbstractSubWriter to use.
     */
    protected final void printMembersDetail(AbstractSubWriter mw) {
        if (mw != null) {
            println("<!-- ===" + getText(mw.navKey()) + " DETAIL === -->");
            println();
            mw.printMembers();
            println();
        }
    }

    //TODO: Just need to make sure 'aspect; shows up and
    //TODO  not 'class'
    protected void printClassDescription() {
        boolean isInterface = classdoc.isInterface();
        boolean isAspect = classdoc instanceof AspectDoc;
        dl();
        dt();

        print(classdoc.modifiers() + " ");  

        if (!isInterface) {
            print(isAspect ? "aspect " : "class ");
        }
        bold(classdoc.name());

        if (!isInterface) {
            ClassDoc superclass = classdoc.superclass();
            if (superclass != null) {
                dt();
                print("extends ");
                printClassLink(superclass);
                printIntroducedSuper(superclass);
            }
        }

        ClassDoc[] implIntfacs = classdoc.interfaces();
        if (implIntfacs != null && implIntfacs.length > 0) {
            dt();
            print(isInterface? "extends " : "implements ");
            for (int i = 0; i < implIntfacs.length; i++) {
                if (i > 0) print(", ");
                printClassLink(implIntfacs[i]);
                printIntroducedSuper(implIntfacs[i]);
            }
        }
        if (isAspect) {
            AspectDoc ad = (AspectDoc)classdoc;
            OfClauseDoc ofClause = ad.ofClause();
            if (ofClause != null) {
                dt();
                if (ofClause.kind() == OfClauseDoc.Kind.EACH_CFLOW) {
                    print("percflow(..)");
                } else if (ofClause.kind() == OfClauseDoc.Kind.EACH_JVM) {
                    print("issingleton()");
                } else if (ofClause.kind() == OfClauseDoc.Kind.EACH_OBJECT) {
                    print("pertarget(");
                    printClassLinks(((OfEachObjectDoc)ofClause).instances());
                    print(")");
                }
            }
            AspectDoc[] dominatees = ad.dominatees();
            if (dominatees != null && dominatees.length > 0) {
                dt();
                print("dominates ");
                printClassLinks(dominatees);
            }
            AspectDoc[] dominators = ad.dominators();
            if (dominators != null && dominators.length > 0) {
                dt();
                print("dominated by ");
                printClassLinks(dominators);
            }
        }
        dlEnd();
    }

    /**
     * Prints a list of class links separated by commas.
     *
     * @param cds array of ClassDoc to be printed.
     */
    protected void printClassLinks(ClassDoc[] cds) {
        if (cds == null || cds.length < 1) return;
        for (int i = 0; i < cds.length; i++) {
            if (i > 0) print(", ");
            if (cds[i] != null) {
                printClassLink(cds[i]);
            }
        }
    }

    /**
     * Prints information about <code>classdoc</code>'s type introduction
     * if <code>cd</code>'s type was introduced onto <code>classdoc</code>.
     *
     * @param cd the ClassDoc being printed.
     */
    protected void printIntroducedSuper(ClassDoc cd) {
        IntroducedSuperDoc[] intros =
            ((org.aspectj.ajdoc.ClassDoc)classdoc).introducers();
        if (null != intros) {
            for (int i = 0; i < intros.length; i++) {
                IntroducedSuperDoc intro = intros[i];
                org.aspectj.ajdoc.Type[] types = intro.types();
                for (int j = 0; j < types.length; j++) {
                    if (types[j].equals(cd)) {
                        print(' ');
                        printText("doclet.by_parens",
                                  getClassLink
                                  (intro.containingClass(),
                                   superIntroductionSubWriter.link(intro),
                                   "introduced"),
                                  getClassLink(intro.containingClass()));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Print the navSummaryLink for all the AspectJ subwriters.
     */
    protected void navAspectJSummaryLinks() {
        navSummaryLink(superIntroductionSubWriter);
        navSummaryLink(fieldIntroductionSubWriter);
        navSummaryLink(constrIntroductionSubWriter);
        navSummaryLink(methodIntroductionSubWriter);
        navSummaryLink(pointcutSubWriter);
        navSummaryLink(adviceSubWriter);
    }

    /**
     * Prints the navSummaryLink correctly.
     *
     * @param mw AbstractSubWriter to invoke.
     */
    protected final void navSummaryLink(AbstractSubWriter mw) {
        if (mw != null) {
            mw.navSummaryLink();
            _navGap();
        }
    }

    /**
     * Print the navDetailLink for all the AspectJ subwriters.
     */
    protected void navAspectJDetailLinks() {
        navDetailLink(superIntroductionSubWriter);
        navDetailLink(fieldIntroductionSubWriter);
        navDetailLink(constrIntroductionSubWriter);
        navDetailLink(methodIntroductionSubWriter);
        navDetailLink(pointcutSubWriter);
        navDetailLink(adviceSubWriter);
    }

    /**
     * Prints the navDetailLink correctly.
     *
     * @param mw AbstractSubWriter to invoke.
     */
    protected final void navDetailLink(AbstractSubWriter mw) {
        if (mw != null) {
            mw.navDetailLink();
            _navGap();
        }
    }

    /*
     * A hack... I'll explain later, if you need to change
     * this mail jeffrey_palm@hotmail.com.
     */

    protected final void _navGap() { super.navGap(); }

    protected int navstate = 0;
    protected void navGap() {
        _navGap();
        if (navstate == 1) {
            navAspectJSummaryLinks();
            navstate++;
        } else if (navstate == 3) {
            navAspectJDetailLinks();
            navstate++;
        }
    }

    protected void printEnclosingClassInfo() {
        super.printEnclosingClassInfo();
        printAdvisorInfo();
        printAdviseeInfo();
    }

    protected void printAdviseeInfo() {
        if (!(classdoc instanceof AspectDoc)) return;
        AspectDoc ad = (AspectDoc)classdoc;
        Set set = new TreeSet();
        AdviceDoc[] as = ad.advice();
        if (as != null) {
            for (int i = 0; i < as.length; i++) {
                ExecutableMemberDoc[] crosscuts = as[i].crosscuts();
                if (null != crosscuts) {
                    for (int j = 0; j < crosscuts.length; j++) {
                        if (null != crosscuts[j]) {
                            set.add(crosscuts[j].containingClass());
                        }
                    }
                }
            }
        }
        IntroductionDoc[] is = ad.introductions();
        if (null != is) {
            for (int i = 0 ; i < is.length; i++) {
                ClassDoc[] targets = is[i].targets();
                for (int j = 0; j < targets.length; j++) {
                    set.add(targets[j]);
                }
            }
            printInfo(set, "doclet.All_Advisees");
        }
    }

    protected void printAdvisorInfo() {
        Set set = new TreeSet();
        set.addAll(advisors(classdoc.fields()));
        set.addAll(advisors(classdoc.methods()));
        set.addAll(advisors(classdoc.constructors()));
        printInfo(set, "doclet.All_Advisors");
    }

    protected void printInfo(Collection classdocs, String str) {
        if ((null != classdocs) && (classdocs.size() > 0)) {
            printInfoHeader();
            boldText(str);
            dd();
            for (Iterator i = classdocs.iterator(); i.hasNext();) {
                printClassLink((ClassDoc)i.next());
                if (i.hasNext()) print(", ");
            }
            ddEnd();
            dlEnd();
        }
    }

    protected final Collection advisors(final MemberDoc[] ms) {
        List list = new ArrayList();
        if (null != ms) {
            for (int i = 0 ; i < ms.length; i++) {
                IntroducedDoc id =
                    ((org.aspectj.ajdoc.MemberDoc)ms[i]).introduced();
                if (id != null) list.add(id.containingClass());
                if (ms[i] instanceof org.aspectj.ajdoc.ExecutableMemberDoc) {
                    AdviceDoc[] as =
                        ((org.aspectj.ajdoc.ExecutableMemberDoc)ms[i]).advice();
                    for (int j = 0; j < as.length; j++) {
                        list.add(as[j].containingClass());
                    }
                }
            }
        }
        return list;
    }
}


