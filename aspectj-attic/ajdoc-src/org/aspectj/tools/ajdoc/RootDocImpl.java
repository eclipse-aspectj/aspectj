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

import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.base.ast.World;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This is responsible for constituting the world
 * of specified[classes|packages] and all classes.
 * It ensures that any classes compiled are included (if appropriate)
 * but does not ensure that linked classes are.
 */
public class RootDocImpl
    extends DocImpl
    implements org.aspectj.ajdoc.RootDoc,
               Quietable {

    /** The collection of packages specified to be documented. */
    private final Set specifiedPackages;

    /** The collection of types specified to be documented. */
    private final Set specifiedClasses;

    /** The collection of packages visible in this world. */
    private final Set packages = new HashSet();

    /** The collection of classes visible in this world. */
    private final Set classes = new HashSet();

    /** The documentation options. */
    private final String[][] options;

    /** The World delegate. */
    private final World world;

    /** Determines whether items are included */
    private final AccessChecker filter;

    public RootDocImpl(World world, String[][] options,
                       Collection pkgnames, Collection classnames,
                       AccessChecker filter) {
        this.world = world;
        this.options = options;
        this.filter = (null != filter ? filter : AccessChecker.PUBLIC);
        Set set = createSpecifiedPackages(pkgnames);
        specifiedPackages = set; // modifiable to prune empty packages
        set = createSpecifiedClasses(classnames);
        specifiedClasses = Collections.unmodifiableSet(set);
        // adds all world classes and packages for classes and packages
        // addWorldTypes();  // todo re-enable as needed
        // make sure specified are added - should duplicate world
        // but should come after since packages are removed if empty
        addSpecifiedPackages();
        addSpecifiedClasses();
        setupDominatesRelations();
        ensureWorldInclusion();
    }
    /* ------------------------------------------------------------
     * Implementation of RootDoc
     * ------------------------------------------------------------
     */

    /**
     * Returns the classes visible in this world.
     *
     * @return an array of ClassDoc representing the visible
     *         classes in this world.
     */
    public ClassDoc[] classes() {
        return (ClassDoc[])classes.toArray
            (new org.aspectj.ajdoc.ClassDoc[classes.size()]);
    }
    
    /**
     * Returns a type visible in this world
     * for the name <code>className</code>.  If there is
     * no visible package, this method will return
     * <code>null</code>.
     *
     * @return an instance of ClassDoc in this world
     *         that corresponds to <code>className</code>.
     *         <code>null</code> is returned if there exists
     *         no such visible type named <code>className</code>.
     */
    public ClassDoc classNamed(String className) {
        ClassDoc[] docs = classes();
        for (int i = 0; i < docs.length; i++) {
            ClassDoc doc = docs[i];
            if (doc.name().equals(className)) {
                return doc;
            }
        }
        return null;
    }

    /**
     * Returns a package visible in this world
     * for the name <code>packageName</code>.  If there is
     * no visible package, this method will return
     * <code>null</code>.
     *
     * @return an instance of PackageDoc in this world
     *         that corresponds to <code>packageName</code>.
     *         <code>null</code> is returned if there exists
     *         no such visible package named <code>packageName</code>.
     */
    public PackageDoc packageNamed(String packageName) {
        for (Iterator i = packages.iterator(); i.hasNext();) {
            PackageDoc doc = (PackageDoc)i.next();
            if (doc.name().equals(packageName)) {
                return doc;
            }
        }
        return null;
    }

    /**
     * Returns the underlying world.
     *
     * @return an instance of World representing all
     *         the CompilationUnits.
     */
    public World world() {
        return world;
    }

    /**
     * Returns the documentation options.
     *
     * @return the documentation options.
     */
    public String[][] options() {
        return options;
    }

    /**
     * Returns the types specified to be documented.
     *
     * @return an array of ClassDoc representing the
     *         specified types.
     */
    public ClassDoc[] specifiedClasses() {
        return (ClassDoc[])specifiedClasses.toArray
            (new org.aspectj.ajdoc.ClassDoc[specifiedClasses.size()]);
    }

    /**
     * Returns the packages specified to be documented.
     *
     * @return an array of PackageDoc representing the
     *         specified packages.
     */
    public PackageDoc[] specifiedPackages() {
        return (PackageDoc[])specifiedPackages.toArray
            (new org.aspectj.ajdoc.PackageDoc[specifiedPackages.size()]);
    }


    /* ------------------------------------------------------------
     * Implementation of Quietable
     * ------------------------------------------------------------
     */

    /** <code>true</code> when notices should be printed. */
    private boolean notice = true;

    /** Supresses output notices. */
    public void quiet() { notice = false; }

    /** Allows output notices. */
    public void speak() { notice = true; }


    /* ------------------------------------------------------------
     * Implementation of DocErrReporter
     * ------------------------------------------------------------
     */

    /**
     * Prints the error message <code>msg</code> using
     * the current error handler.
     *
     * @param msg the error message.
     */
    public void printError(String msg) {
        err().printError(msg);
    }

    /**
     * Prints the notice message <code>msg</code> using
     * the current error handler.
     *
     * @param msg the notice message.
     */
    public void printNotice(String msg) {
        if (notice) err().printNotice(msg);
    }

    /**
     * Prints the warning message <code>msg</code> using
     * the current error handler.
     *
     * @param msg the warning message.
     */
    public void printWarning(String msg) {
        err().printWarning(msg);
    }


    /* ------------------------------------------------------------
     * Implementation of Doc
     * ------------------------------------------------------------
     */

    /**
     * Returns <code>null</code>.
     *
     * @return <code>null</code>.
     */
    public String name() {
        return "who knows???";
    }


    /* ------------------------------------------------------------
     * Helper methods
     * ------------------------------------------------------------
     */
    
    /**
     * Creates only PackageDocs that were included on the command
     * line, even if they are empty.  Should be used only for 
     * specifiedPackages.
     */
    private HashSet createSpecifiedPackages(Collection pkgnames) {
        HashSet result = new HashSet();
        for (Iterator i = pkgnames.iterator(); i.hasNext();) {
            String pkgname = (String)i.next();
            PackageDocImpl pkgdoc = PackageDocImpl.getPackageDoc(pkgname);
            pkgdoc.setIncluded(true);
            result.add(pkgdoc);
        }
        return result;
    }
    private void addWorldTypes() {
        for (Iterator i = world.getTypes().iterator(); i.hasNext();) {
            TypeDec td = (TypeDec)i.next();
            ClassDocImpl cd = ClassDocImpl.getInstance(td);
            addClass(cd);
            cd.setIncluded(filter.canAccess(td));
        }
    }

    /**
     * Creates only ClassDocs that were included on the command
     * line, and then only if they pass the filter.  
     * Should be used only for specifiedClasses.
     * todo: createClasses uses to use all classes if no names 
     */
    private HashSet createSpecifiedClasses(Collection classnames) {
        HashSet result = new HashSet();
        if (classnames != null) {
            for (Iterator i = classnames.iterator(); i.hasNext();) {
                String classname = (String)i.next();
                for (Iterator j = world.getTypes().iterator(); j.hasNext();) {
                    TypeDec td = (TypeDec)j.next();
                    if (filter.canAccess(td)) {
                        ClassDoc cd = ClassDocImpl.getInstance(td);
                        if (cd.qualifiedName().equals(classname)) {
                            result.add(cd);
                            // add inner classes since not specified explicitly
                            ClassDoc[] inners = cd.innerClasses(); // no cycles, right?
                            if (null != inners) {
                                for (int l = 0; l < inners.length; l++) {
                                    result.add(inners[l]); 
                                }
                            }
                            break;
                        }
                    }
                }
                // todo: warn if class specified but not in world? 
            }
        }
        return result;
    }

    private void addSpecifiedClasses() {
        for (Iterator i = new ArrayList(specifiedClasses).iterator(); i.hasNext();) {
            ClassDoc cd = (ClassDoc)i.next();
            addClass(cd);
        }
    }

    private void addSpecifiedPackages() {
        for (Iterator i = new ArrayList(specifiedPackages).iterator(); i.hasNext();) {
            PackageDoc pd = (PackageDoc)i.next();
            ClassDoc[] allClasses = pd.allClasses();
            if (allClasses.length == 0) {
                specifiedPackages.remove(pd);
            } else {
                for (int j = 0; j < allClasses.length; j++) {
                    addClass(allClasses[j]);
                }
            }
        }
    }

    /** 
     * If filter accepts this ClassDoc,
     * Add it and and inner classes to classes
     * and add package to packages.
     */
    private void addClass(ClassDoc cd) {
        if (null == cd) return;
        ClassDocImpl impl = (ClassDocImpl) cd;
        if (filter.canAccess(impl.typeDec()) 
            && (!classes.contains(impl))) {
            impl.setIncluded(true);
            classes.add(impl);
            packages.add(impl.containingPackage());
            ClassDoc[] inners = impl.innerClasses();
            for (int i = 0; i < inners.length; i++) {
                addClass(inners[i]);
            }
        } // todo: flag classes not added?
    }

    /** Read all classes to find any dominates relations */
    private void setupDominatesRelations() {
        // Find just the aspects
        List aspects = new ArrayList();
        ClassDoc[] classes = classes();
        for (int i = 0; i < classes.length; i++) {
            ClassDocImpl cd = (ClassDocImpl)classes[i];
            if (cd.isAspect()) {
                aspects.add(cd);
            }
        }

        // Iterate over the aspects, if
        for (Iterator i = aspects.iterator(); i.hasNext();) {
            AspectDocImpl aspect1 = (AspectDocImpl)i.next();
            for (Iterator j = aspects.iterator(); j.hasNext();) {
                AspectDocImpl aspect2 = (AspectDocImpl)j.next();
                if (aspect1.dominates(aspect2)) {
                    aspect1.addDominatee(aspect2);
                    aspect2.addDominator(aspect1);
                }
            }
        }
    }

    /**
     * Ensure compiled classes are included if they pass the filter
     * and excluded otherwise.
     * todo: The set of types available includes the world plus reachable
     * types from there; I would like to exclude the reachable ones,
     * but do not know how.
     */
    private void ensureWorldInclusion() {
        for (Iterator i = world.getTypes().iterator(); i.hasNext();) {
            TypeDec td = (TypeDec)i.next();
            ClassDocImpl cd = ClassDocImpl.getInstance(td);
            boolean isIncluded = cd.isIncluded();
            // todo: update to consider enclosing class privileges
            boolean shouldInclude = filter.canAccess(td);
            if (shouldInclude != isIncluded) {
                cd.setIncluded(shouldInclude);
            }
        }
    }
}
