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

import org.aspectj.tools.ajdoc.Access;
import org.aspectj.tools.ajdoc.Util;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.DirectoryManager;
import com.sun.tools.doclets.DocletAbortException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Provides support for aspects.
 *
 * @author Jeff Palm
 */
public class ClassUseWriter
    extends com.sun.tools.doclets.standard.ClassUseWriter
{

    /**
     * The target ClassDoc.
     */
    protected final ClassDoc classdoc;

    /**
     * Maps PackageDocs to advice arguments.
     */
    protected final Map pkgToAdviceArgs;

    /**
     * Maps PackageDocs to advice return types.
     */
    protected final Map pkgToAdviceReturn;
    
    /**
     * Maps PackageDocs to pointcut arguments.
     */
    protected final Map pkgToPointcutArgs;

    /**
     * Maps PackageDocs to pointcut return types.
     */
    protected final Map pkgToPointcutReturn;

    /**
     * Maps PackageDocs to field introductions.
     */
    protected final Map pkgToFieldIntroductions;

    /**
     * Maps PackageDocs to class introductions.
     */
    protected final Map pkgToClassIntroductions;
    
    /**
     * Maps PackageDocs to interface introductions.
     */
    protected final Map pkgToInterfaceIntroductions;

    /**
     * Maps PackageDocs to class advisors.
     */
    protected final Map pkgToClassAdvisors;

    /**
     * Maps PackageDocs to aspects that dominate
     * aspects in that package.
     */
    protected final Map pkgToAspectDominatees;

    /**
     * Maps PackageDocs to aspects that are dominated
     * by aspects in that package.
     */
    protected final Map pkgToAspectDominators;

    /**
     * The MethodSubWriter to use.
     */
    protected final MethodSubWriter methodSubWriter
        = new MethodSubWriter(this);
    
    /**
     * The ConstructorSubWriter to use.
     */
    protected final ConstructorSubWriter constrSubWriter
        = new ConstructorSubWriter(this);
    
    /**
     * The FieldSubWriter to use.
     */
    protected final FieldSubWriter fieldSubWriter
        = new FieldSubWriter(this);
    
    /**
     * The ClassSubWriter to use.
     */
    protected final ClassSubWriter classSubWriter
        = new ClassSubWriter(this);
        
    /**
     * The PointcutSubWriter to use.
     */
    protected final PointcutSubWriter pointcutSubWriter
        = new PointcutSubWriter(this);
    
    /**
     * The SuperIntroductionSubWriter to use.
     */
    protected final SuperIntroductionSubWriter superIntroductionSubWriter
        = new SuperIntroductionSubWriter(this);
    
    /**
     * The FieldIntroductionSubWriter to use.
     */
    protected final FieldIntroductionSubWriter fieldIntroductionSubWriter
        = new FieldIntroductionSubWriter(this);
    
    /**
     * The ConstructorIntroductionSubWriter to use.
     */
    protected final ConstructorIntroductionSubWriter constrIntroductionSubWriter
        = new ConstructorIntroductionSubWriter(this);
    
    /**
     * The MethodIntroductionSubWriter to use.
     */
    protected final MethodIntroductionSubWriter methodIntroductionSubWriter
        = new MethodIntroductionSubWriter(this);
    
    /**
     * The AdviceSubWriter to use.
     */
    protected final AdviceSubWriter adviceSubWriter
        = new AdviceSubWriter(this);
    


    public ClassUseWriter(ClassUseMapper mapper,
                          String path, 
                          String filename,
                          String relpath,
                          ClassDoc classdoc) 
        throws IOException, DocletAbortException {

        super(mapper.mapper(classdoc), path,
              filename, relpath, classdoc);

        mapper.restore(classdoc);

        this.classdoc = Access.classdoc(this);

        this.pkgToAdviceReturn =
            _pkgDivide(mapper.classToAdviceReturn);
        this.pkgToAdviceArgs =
            _pkgDivide(mapper.classToAdviceArgs);
        this.pkgToPointcutReturn =
            _pkgDivide(mapper.classToPointcutReturn);
        this.pkgToPointcutArgs =
            _pkgDivide(mapper.classToPointcutArgs);
        this.pkgToFieldIntroductions =
            _pkgDivide(mapper.classToFieldIntroductions);
        this.pkgToClassIntroductions =
            _pkgDivide(mapper.classToClassIntroductions);
        this.pkgToInterfaceIntroductions =
            _pkgDivide(mapper.classToInterfaceIntroductions);
        this.pkgToClassAdvisors =
            _pkgDivide(mapper.classToAdvisors);
        this.pkgToAspectDominatees =
            _pkgDivide(mapper.classToDominatees);
        this.pkgToAspectDominators =
            _pkgDivide(mapper.classToDominators);
    }

    protected com.sun.tools.doclets.standard.ClassUseWriter
        writer;
    private Map _pkgDivide(Map classMap) {
        return (Map)Util.invoke
            (com.sun.tools.doclets.standard.ClassUseWriter.class,
             this, "pkgDivide",
             new Class[]{java.util.Map.class},
             new Object[]{classMap});
    }
	
    public static void generate(ClassUseMapper mapper, 
                                ClassDoc classdoc) 
        throws DocletAbortException {
        ClassUseWriter cw = null;
        String path = DirectoryManager.getDirectoryPath(classdoc.
                                                        containingPackage());
        if (path.length() > 0) {
            path += File.separator;
        }
        path += "class-use";
        String filename = classdoc.name() + ".html";
        String pkgname = classdoc.containingPackage().name();
        pkgname += (pkgname.length() > 0 ? "." : "") + "class-use";
        String relpath = DirectoryManager.getRelativePath(pkgname); 
        try {
            (cw = new ClassUseWriter(mapper, path, filename, 
                                     relpath, classdoc)).
                generateClassUseFile();
        } catch (IOException e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered", e+"", filename);
            throw new DocletAbortException();
        } finally {
            if (cw != null) cw.close();
        }
    }

    protected void generateClassUse(PackageDoc pkg) throws IOException {
        super.generateClassUse(pkg);
        String classlink = getClassLink(classdoc);
        String pkglink = getPackageLink(pkg);

        printUseInfo(adviceSubWriter, pkgToAdviceReturn,
                     pkg, "AdviceReturn", classlink, pkglink);
        printUseInfo(adviceSubWriter, pkgToAdviceArgs,
                     pkg, "AdviceArgs", classlink, pkglink);
        printUseInfo(pointcutSubWriter, pkgToPointcutReturn,
                     pkg, "PointcutReturn", classlink, pkglink);
        printUseInfo(pointcutSubWriter, pkgToPointcutArgs,
                     pkg, "PointcutArgs", classlink, pkglink);
        printUseInfo(fieldIntroductionSubWriter, pkgToFieldIntroductions,
                     pkg, "FieldIntroductions", classlink, pkglink);
        printUseInfo(superIntroductionSubWriter, pkgToClassIntroductions,
                     pkg, "ClassIntroductions", classlink, pkglink);
        printUseInfo(superIntroductionSubWriter, pkgToInterfaceIntroductions,
                     pkg, "InterfaceIntroductions", classlink, pkglink);
        printUseInfo(classSubWriter, pkgToClassAdvisors,
                     pkg, "ClassAdvisors", classlink, pkglink);

        printUseInfo(classSubWriter, pkgToAspectDominatees,
                     pkg, "AspectDominatees", classlink, pkglink);
        printUseInfo(classSubWriter, pkgToAspectDominators,
                     pkg, "AspectDominators", classlink, pkglink);
    }

    protected final void printUseInfo(AbstractSubWriter mw, Map map,
                                      PackageDoc pkg, String kind,
                                      String classlink, String pkglink) {
        Access.printUseInfo(mw, map.get(pkg),
                            getText("doclet.ClassUse_" + kind,
                                    classlink,pkglink));
    }
}
