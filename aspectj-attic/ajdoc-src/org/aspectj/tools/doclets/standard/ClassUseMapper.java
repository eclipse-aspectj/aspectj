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
import org.aspectj.ajdoc.PointcutDoc;
import org.aspectj.tools.ajdoc.Util;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.ClassTree;
import com.sun.tools.doclets.DocletAbortException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides support for aspects.
 *
 * @author Jeff Palm
 */
public class ClassUseMapper {


    /**
     * Maps a ClassDoc to advice that return its type.
     */
    public final Map classToAdviceReturn = new HashMap();
    
    /**
     * Maps a ClassDoc to advice that have its type
     * are arguments.
     */
    public final Map classToAdviceArgs = new HashMap();
    
    /**
     * Maps a ClassDoc to pointcuts that return its type.
     */
    public final Map classToPointcutReturn = new HashMap();
    
    /**
     * Maps a ClassDoc to pointcuts that have its type
     * as arguments.
     */
    public final Map classToPointcutArgs = new HashMap();
    
    /**
     * Maps a ClassDoc to field introductions that
     * are its type.
     */
    public final Map classToFieldIntroductions = new HashMap();

    /**
     * Maps a ClassDoc to class introductions that
     * are its type.
     */
    public final Map classToClassIntroductions = new HashMap();
    
    /**
     * Maps a ClassDoc to interface introductions that
     * are its type.
     */
    public final Map classToInterfaceIntroductions = new HashMap();
    
    /**
     * Maps a ClassDoc to aspects that advise it.
     */
    public final Map classToAdvisors = new HashMap();
    
    /**
     * Maps a ClassDoc to aspects that it dominates.
     */
    public final Map classToDominatees = new HashMap();
    
    /**
     * Maps a ClassDoc to aspects that dominate it..
     */
    public final Map classToDominators = new HashMap();
    

    public static void generate(RootDoc root, ClassTree classtree)
        throws DocletAbortException {
        try {
            
            ClassUseMapper mapper = new ClassUseMapper(root, classtree);

            ClassDoc[] classes = root.classes();
            for (int i = 0; i < classes.length; i++) {
                ClassUseWriter.generate(mapper, classes[i]);
            }
            PackageDoc[] pkgs = Standard.configuration().packages;
            for (int i = 0; i < pkgs.length; i++) {
                com.sun.tools.doclets.standard.PackageUseWriter.
                    generate(mapper.mapper, pkgs[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Standard.configuration().standardmessage.
                error("doclet.exception", e+"",
                      "creating class use tree");
            throw new DocletAbortException();
        }
    }

    protected final com.sun.tools.doclets.standard.ClassUseMapper mapper;
    
    public ClassUseMapper(RootDoc root, ClassTree classtree)
        throws Exception {
        Constructor constr =
            com.sun.tools.doclets.standard.ClassUseMapper.class.
            getDeclaredConstructor(new Class[] {
                com.sun.javadoc.RootDoc.class,
                com.sun.tools.doclets.ClassTree.class,
            });
        constr.setAccessible(true);
        mapper = (com.sun.tools.doclets.standard.ClassUseMapper)constr.
            newInstance(new Object[]{root, classtree});

        classToPackageSave = new HashMap();
        for (Iterator i = mapper.classToPackage.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            classToPackageSave.put(key, new HashSet((Collection)
                                                    mapper.classToPackage.
                                                    get(key)));
        }
        
        finish(root, classtree);
    }

    protected Object saved;
    protected final Map classToPackageSave;
    protected final com.sun.tools.doclets.standard.ClassUseMapper mapper
        (ClassDoc classdoc)
    {
        Object noaspects = classToPackageSave.get(classdoc);
        saved = mapper.classToPackage.get(classdoc);
        mapper.classToPackage.put(classdoc, noaspects);
        return mapper;
    }

    protected void restore(ClassDoc classdoc) {
        mapper.classToPackage.put(classdoc, saved);
    }
   
    protected void finish(RootDoc root, ClassTree classtree) {
       
        ClassDoc[] classes = root.classes();
        for (int i = 0; i < classes.length; i++) {
            ClassDoc cd = classes[i];
            if (cd instanceof org.aspectj.ajdoc.ClassDoc) {
                org.aspectj.ajdoc.ClassDoc acd = (org.aspectj.ajdoc.ClassDoc)cd;
                PointcutDoc[] pcs = acd.pointcuts();
                for (int j = 0; j < pcs.length; j++) {
                    PointcutDoc pd = pcs[j];
                    mapExecutable(pd);
                    Type result = pd.resultType();
                    if (result != null) {
                        ClassDoc tcd = result.asClassDoc();
                        if (tcd != null) {
                            add(classToPointcutReturn, tcd, pd);
                        }
                    }
                }
            }
            
            if (cd instanceof AspectDoc) {
                AspectDoc ad = (AspectDoc)cd;
                AdviceDoc[] adocs = ad.advice();
                for (int j = 0; j < adocs.length; j++) {
                    AdviceDoc adoc = adocs[j];
                    mapExecutable(adoc);
                    Type result = adoc.returnType();
                    if (result != null) {
                        ClassDoc tcd = result.asClassDoc();
                        if (tcd != null) {
                            add(classToAdviceReturn, tcd, adoc);
                        }
                    }
                    ExecutableMemberDoc[] emds = adoc.crosscuts();
                    if (null != emds) {
                        for (int k = 0; k < emds.length; k++) {
                            ExecutableMemberDoc emd = emds[k];
                            if (null != emd) {
                                ClassDoc tcd = emd.containingClass();
                                ClassDoc fcd = adoc.containingClass();
                                //TODO: This probably sucks!!!
                                if (!refList(classToAdvisors, tcd).contains(fcd)) {
                                    add(classToAdvisors, tcd, fcd);
                                }
                            }
                        }
                    }
                }

                IntroductionDoc[] ids = ad.introductions();
                for (int j = 0; j < ids.length; j++) {
                    IntroductionDoc id = ids[j];
                    if (id instanceof IntroducedDoc) {
                        IntroducedDoc idd = (IntroducedDoc)id;
                        MemberDoc mem = idd.member();
                        if (mem.isField()) {
                            FieldDoc fd = (FieldDoc)mem;
                            ClassDoc tcd = fd.type().asClassDoc();
                            add(classToFieldIntroductions, tcd, fd);
                        }
                    } else if (id instanceof IntroducedSuperDoc) {
                        IntroducedSuperDoc idd = (IntroducedSuperDoc)id;
                        boolean isImplements = idd.isImplements();
                        Type[] types = idd.types();
                        for (int k = 0; k < types.length; k++) {
                            ClassDoc tcd = types[k].asClassDoc();
                            add(isImplements ?
                                classToInterfaceIntroductions :
                                classToClassIntroductions, tcd,  idd);
                        }
                    }
                }
                AspectDoc[] dominatees = ad.dominatees();
                for (int j = 0; j < dominatees.length; j++) {
                    add(classToDominatees, ad, dominatees[j]);
                }
                AspectDoc[] dominators = ad.dominators();
                for (int j = 0; j < dominators.length; j++) {
                    add(classToDominators, ad, dominators[j]);
                }
            }
        }
    }
    
    protected void mapExecutable(ExecutableMemberDoc em) {
        Parameter[] params = em.parameters();
        List classargs = new ArrayList();
        Map argsmap = ((org.aspectj.ajdoc.MemberDoc)em).isAdvice() ?
            classToAdviceArgs : classToPointcutArgs ;
        for (int i = 0; i < params.length; i++) {
            ClassDoc pcd = params[i].type().asClassDoc();
            if (pcd != null && !classargs.contains(pcd)) {
                add(argsmap, pcd, em);
                classargs.add(pcd);
            }
        }
    }
    
    protected List refList(Map map, ClassDoc cd) {
        return (List)Util.invoke(mapper, "refList",
                                 new Class[]{java.util.Map.class,
                                             com.sun.javadoc.ClassDoc.class},
                                 new Object[]{map, cd});
    }

    protected void add(Map map, ClassDoc cd, ProgramElementDoc ref) {
        Util.invoke(mapper, "add",
                    new Class[]{java.util.Map.class,
                                com.sun.javadoc.ClassDoc.class,
                                com.sun.javadoc.ProgramElementDoc.class},
                    new Object[]{map, cd, ref});
    }
}
