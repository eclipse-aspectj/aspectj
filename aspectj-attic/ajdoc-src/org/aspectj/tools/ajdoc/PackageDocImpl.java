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
package org.aspectj.tools.ajdoc;

import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.ajdoc.PackageDoc;
import org.aspectj.compiler.base.ast.Type;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.crosscuts.AspectJCompiler;

import com.sun.javadoc.ClassDoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a package in the aspectj-world.
 * A package is a passive container of classes given to it.
 * It does not enforce visibility rules. However, you can
 * purge packages without classes from the static list of packages.
 * @author Jeff Palm
 */
public class PackageDocImpl
    extends DocImpl
    implements org.aspectj.ajdoc.PackageDoc {

    public final static String UNNAMED_PACKAGE = ""; //unnamed-package";

    private static AjdocCompiler ajdoc = Ajdoc.instance();
    private static AspectJCompiler ajc = ajdoc;
    public AspectJCompiler ajc() { return ajc; }
    private Set allClasses = new HashSet();
    private Comment comment;
    private String name;

    /**
     * Only want to create these within the static access.
     *
     * @param name name of the new package.
     */
    private PackageDocImpl(String name) {
        this.name = name;
        findDocumentation();
    }

    /**
     * Adds a class to this package.
     *
     * @param classDoc The new class.
     */
    public void addClass(ClassDoc classDoc) {
        allClasses.add(classDoc);
    }
    
    /**
     * Attempts to find a class with name <code>className</code>
     * in this package.
     *
     * @param className name of the class to find.
     */
    public ClassDoc findClass(String className) {
        Type type = ajc.getTypeManager().
            findType(name(), className);
        if (type != null) {
            return ClassDocImpl.getInstance(type.getTypeDec());
        }
        for (Iterator i = allClasses.iterator(); i.hasNext();) {
            ClassDoc cd = (ClassDoc)i.next();
            if (className.equals(cd.name())) {
                return cd; //todo wes was null?
            }
        }
        return null;
    }

    /**
     * Returns all the classes in this package.
     *
     * @return an array of ClassDoc representing all
     *         the classes in this package.
     */
    public ClassDoc[] allClasses() {
        return (ClassDoc[])allClasses.toArray(new ClassDoc[allClasses.size()]);
    }

    /**
     * Returns all the aspects in this package.
     *
     * @return an array of AspectDoc representing all
     *         the aspects in this package.
     */
    public AspectDoc[] aspects() {
        List list = new ArrayList();
        for (Iterator i = allClasses.iterator(); i.hasNext();) {
            org.aspectj.ajdoc.ClassDoc doc = (org.aspectj.ajdoc.ClassDoc)i.next();
            if (doc.isAspect()) list.add(doc);
        }
        return (AspectDoc[])list.toArray
            (new AspectDoc[list.size()]);  
    }

    /**
     * Returns all the ordinary classes in this package.
     *
     * @return an array of ClassDoc representing all
     *         the ordinary classes in this package.
     */
    public ClassDoc[] ordinaryClasses() {
        List list = new ArrayList();
        for (Iterator i = allClasses.iterator(); i.hasNext();) {
            ClassDoc doc = (ClassDoc)i.next();
            if (doc.isOrdinaryClass()) list.add(doc);
        }
        return (ClassDoc[])list.toArray
            (new ClassDoc[list.size()]);
    }
    
    /**
     * Returns all the exceptions in this package.
     *
     * @return an array of ClassDoc representing all
     *         the exceptions in this package.
     */
    public ClassDoc[] exceptions() {
        List list = new ArrayList();
        for (Iterator i = allClasses.iterator(); i.hasNext();) {
            ClassDoc doc = (ClassDoc)i.next();
            if (doc.isException()) list.add(doc);
        }
        return (ClassDoc[])list.toArray
            (new ClassDoc[list.size()]);
    }

    /**
     * Returns all the errors in this package.
     *
     * @return an array of ClassDoc representing all
     *         the errors in this package.
     */    
    public ClassDoc[] errors() {
        List list = new ArrayList();
        for (Iterator i = allClasses.iterator(); i.hasNext();) {
            ClassDoc doc = (ClassDoc)i.next();
            if (doc.isError()) list.add(doc);
        }
        return (ClassDoc[])list.toArray
            (new ClassDoc[list.size()]);
    }

    /**
     * Returns all the interfaces in this package.
     *
     * @return an array of ClassDoc representing all
     *         the interfaces in this package.
     */  
    public ClassDoc[] interfaces() {
        List list = new ArrayList();
        for (Iterator i = allClasses.iterator(); i.hasNext();) {
            ClassDoc doc = (ClassDoc)i.next();
            if (doc.isInterface()) list.add(doc);
        }
        return (ClassDoc[])list.toArray
            (new ClassDoc[list.size()]);
    }

    /**
     * Returns the name if included -- null otherwise.
     *
     * @return the name if included -- null otherwise.
     */
    public String name() {
        return isIncluded() ? name : null;
    }

    /**
     * Compare based on <code>name()</code>.
     *
     * @param  other other Object.
     * @return       <code>true</code> if the other Object is a
     *               PackageDocImpl and has the same name.
     */
    public boolean equals(Object other) {
        return other instanceof PackageDocImpl && other != null
            ?  name().equals(((PackageDocImpl)other).name())
            :  super.equals(other);
    }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    public String toString() {
        return name();
    }

    private void findDocumentation() {
        if (ajdoc == null) return;
        String filename = (name.equals("")
                           ? name
                           : name.replace('.',File.separatorChar)
                           + File.separatorChar) + "package.html";
        File html = ajdoc.findFile(filename, false);
        if (html == null) return;
        String rawCommentText = Util.documentation(html, ajdoc.err());

        //TODO: should be done in aspect from Aspects.java
        //FormalComment comment = new FormalComment(rawCommentText);
        setComment(new Comment(this, rawCommentText));
    }


    /* ------------------------------------------------------------
     * Factory stuff
     * ------------------------------------------------------------
     */

    private static Map namesToPackages = new HashMap();

    /**
     * Returns the collection of known packages.
     *
     * @return a Collection representing the known packages.
     */
    public static Collection packages() {
        return namesToPackages.values();
    }

    /**
     * Inits the world and AspectJCompiler with these
     * packages.
     *
     * @param world current World.
     * @param ajc   current compiler.
     */
    public static void init(AspectJCompiler ajc) {
        PackageDocImpl.ajc = ajc;
        if (ajc instanceof AjdocCompiler) {
            PackageDocImpl.ajdoc = (AjdocCompiler)ajc;
        }
    }

    /**
     * Returns a package for a TypeDec.
     *
     * @param typeDec TypeDec whose package is desired.
     * @return        a PackageDocImpl for a given TypeDec.
     */
    public static PackageDocImpl getPackageDoc(TypeDec typeDec) {
        return getPackageDoc(typeDec.getPackageName());
    }
    
    /**
     * Returns a package for a name.
     *
     * @param packageName package name for which to look.
     * @return            a PackageDocImpl for a given package name.
     */
    public static PackageDocImpl getPackageDoc(String packageName) {
        return addPackageDoc(packageName);
    }

    /**
     * Adds a package with name <code>name</code> if it
     * already doesn't exist, and returns the new package
     * (if own with the same name didn't exist) or the
     * existing package.
     *
     * @param name name of the new package.
     * @return     current package mapping to
     *             <code>name</code>.
     */
    public static PackageDocImpl addPackageDoc(String name) {
        if (name == null) name = UNNAMED_PACKAGE;
        PackageDocImpl packageDoc = (PackageDocImpl)namesToPackages.get(name);
        if (packageDoc == null) {
            packageDoc = new PackageDocImpl(name);
            addPackageDoc(packageDoc);
        }
        return packageDoc;
    }

    /**
     * Adds the PackageDoc if one doesn't already exist
     * with the same name, and returns the existing or created
     * package with the same name as the one passed in .
     *
     * @param packageDoc PackageDoc we want to add.
     * @return           package in the world with the same
     *                   name as <code>packageDoc</code>.
     */
    public static PackageDocImpl addPackageDoc(PackageDoc packageDoc) {
        if (packageDoc == null) return null;
        return (PackageDocImpl)namesToPackages.put(packageDoc.name(),
                                                   packageDoc);
    }

    /** 
     * Sets the include flag to false for empty packages
     * todo: remove instead?
     */
    public static void excludeEmptyPackages() {
        for (Iterator i = packages().iterator(); i.hasNext();) {
            PackageDocImpl pkg = (PackageDocImpl)i.next();
            if (pkg.allClasses().length < 1) {
                pkg.setIncluded(false);
            }
        }
    }
}
