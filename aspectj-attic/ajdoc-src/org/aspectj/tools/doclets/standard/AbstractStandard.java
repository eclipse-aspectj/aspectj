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

import org.aspectj.ajdoc.AspectDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.ClassTree;
import com.sun.tools.doclets.DocletAbortException;
import com.sun.tools.doclets.HtmlDocWriter;
import com.sun.tools.doclets.IndexBuilder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * An abstract allowing one to customize the writers
 * used in ajdoc.  Subclasses should define the three
 * generate methods to specify the documentation made.
 *
 * @see #preGenerationClasses()
 * @see #postGenerationClasses()
 * @see #checkClasses()
 * @author Jeff Palm
 */
public abstract class AbstractStandard 
    extends com.sun.tools.doclets.standard.Standard {

    // todo wes removed restriction, but implemented Standard as singleton via proxy
    private static int refCount = 0;
    {
        if (refCount > 0) {
            System.err.println("Warning: " + refCount + " AbstractStandard already ");
        }
        refCount++;
    }

    /**
     * The ClassTree that is available to subclasses and it gaurateed
     * to be created before pre-generating classes.
     */
    protected ClassTree classtree;
    
    protected static boolean start(AbstractStandard as,
                                   RootDoc root) throws IOException {
        try {
            as.getConfiguration().setOptions(root);
            as.startGeneration(root);
        } catch (DocletAbortException exc) {
            return false;
        }
        return true;
    }

    /**
     * Returns the types of {@link Pass}es that will
     * run before generating classes.
     *
     * @return an array of Class, where each entry
     *         is a subclass of {@link Pass}, ordered
     *         to run directly before generating the
     *         classes.
     */
    protected abstract Class[] preGenerationClasses();

    /**
     * Returns the types of {@link Pass}es that will
     * run after generating classes.
     *
     * @return an array of Class, where each entry
     *         is a subclass of {@link Pass}, ordered
     *         to run directly after generating the
     *         classes.
     */
    protected abstract Class[] postGenerationClasses();

    /**
     * Returns the types of {@link CheckPass}es that will
     * run to check the classes.
     *
     * @return an array of Class, where each entry
     *         is a subclass of {@link CheckPass}, ordered
     *         to run in order to check the classes passed
     *         into the class generation phase.
     */
    protected abstract Class[] checkClasses();

    /**
     * Return the configuration used by a subclass.  This
     * allows the subclass to specify it's own kind.
     *
     * @return a customized configuration.
     */
    public abstract ConfigurationStandard getConfiguration();

    protected ConfigurationStandard makeConfiguration() {
        return new ConfigurationStandard();
    }


    /**
     * Returns the configuration, and ensures that
     * HtmlDocWriter.configuration is of the type used by
     * this class.
     *
     * @return the current instanceof ConfigurationStandard being
     *         used and creates one if needed.  This will <b>not</b>
     *         be null.
     */
    // todo these are the heinous globals that impose one process per classloader
    public static com.sun.tools.doclets.standard.ConfigurationStandard
        configuration() {
        if (HtmlDocWriter.configuration == null ||
            !(HtmlDocWriter.configuration instanceof ConfigurationStandard)) {
            HtmlDocWriter.configuration = new ConfigurationStandard();
            //TODO: change to makeConfiguration()
        }
        return (ConfigurationStandard)HtmlDocWriter.configuration;
    }

    /**
     * Creates and returns an IndexBuilder that includes aspects.
     *
     * @param root        RootDoc to pass the new IndexBuilder.
     * @param classesOnly <code>true</code> if only classes
     *                    should be included.
     * @return            an IndexBuilder that includes aspects.
     */
    protected IndexBuilder indexBuilder(RootDoc root, boolean classesOnly) {
        class MyIndexBuilder extends IndexBuilder {
            public MyIndexBuilder(RootDoc r, boolean n) {
                super(r, n);
            }
            public MyIndexBuilder(RootDoc r, boolean n, boolean b) {
                super(r, n, b);
            }
            protected void putMembersInIndexMap(ClassDoc classdoc) {
                super.putMembersInIndexMap(classdoc);
                if (classdoc instanceof org.aspectj.ajdoc.ClassDoc) {
                    org.aspectj.ajdoc.ClassDoc cd =
                        (org.aspectj.ajdoc.ClassDoc)classdoc;
                    adjustIndexMap(cd.pointcuts());
                    if (cd instanceof AspectDoc) {
                        adjustIndexMap(((AspectDoc)cd).advice());
                    }
                }
            }
        }
        return new MyIndexBuilder(root, configuration().nodeprecated, classesOnly);
    }


    /**
     * Does the work in generating the documentation.
     * First, call all the passes return from {@link #generateCheckPasses}
     * them perform some copying.  Second build the classtree, run the
     * pre-classgeneration passes, generate the packages, generate the
     * classes, then call all the postGenerationClasses.
     *
     * @param root the root of the documentation.
     */
    protected void startGeneration(RootDoc root) throws DocletAbortException {

        if (!generateCheckPasses(getConfiguration(), root)) return;

        performCopy(getConfiguration().destdirname,
                    getConfiguration().helpfile);
        performCopy(getConfiguration().destdirname,
                    getConfiguration().stylesheetfile);

        classtree = new ClassTree(root, getConfiguration().nodeprecated);

        generatePrePasses(getConfiguration(), root);

        generatePackageCycle(getConfiguration().packages,
                             getConfiguration().createtree,
                             getConfiguration().nodeprecated);
         generateClassFiles(root, classtree);
        generatePostPasses(getConfiguration(), root);
    }

    /**
     * A class representing a single pass in the generation cycles.  It
     * does some of the dirty work for you.
     */
    public static abstract class Pass {

        /** The root available to this pass. */
        protected RootDoc root;

        /** The configuration available to this pass. */
        protected ConfigurationStandard cs;

        /** The doclet available to this pass. */
        protected AbstractStandard std;
        
        public Pass() {}

        /**
         * Returns the title of the pass for logging.
         *
         * @return the unique title of this pass.  This can
         *         be <code>null</code> to disable display.
         */
        public abstract String title();

        /**
         * Do the generation work.  All instance variables
         * are guaranteed to be set.
         */
        protected abstract void gen() throws DocletAbortException;

        /**
         * Do the actual generation if {@link #cond} returns
         * <code>true</code>.  Do some other logging, too.
         *
         * @param std  the AbstractStandard to use.
         * @param cs   the ConfigurationStandard to use.
         * @param root the RootDoc to use.
         */
        public final void generate(AbstractStandard std,
                                   ConfigurationStandard cs,
                                   RootDoc root)
            throws DocletAbortException {
            this.std = std;
            this.cs = cs;
            this.root = root;
            if (cond()) {
                String title = title();
                long start = System.currentTimeMillis();
                if (cs.log && title != null) {
                    cs.standardmessage.notice("doclet.pass_msg", title);
                }
                gen();
                if (cs.log && title != null) {
                    long stop = System.currentTimeMillis();
                    cs.standardmessage.notice("doclet.done_msg",
                                              title, (stop-start)+"");
                }
            }
        }

        /**
         * Returns whether the generation should proceed.  Override
         * this method for conditional passes.
         *
         * @return <code>true</code> is this pass shoulud proceed.
         */
        protected boolean cond() {
            return true;
        }
    }

    /**
     * A convenience class for doing checks.
     */
    public abstract static class Check extends Pass {

        /**
         * Returns the error message if check fails.
         *
         * @return error message if check fails.
         */
        protected abstract String message();

        /**
         * Returns whether check has failed or not.
         *
         * @return <code>true</code> is check fails.
         */
        protected abstract boolean cond();

        /**
         * Prints message, because we've failed and throws
         * a DocletAbortException to notify the doclet
         * that we've failed.
         */
        protected void gen() throws DocletAbortException {
            cs.standardmessage.error(message());
            throw new DocletAbortException();
        }

        /**
         * Returns null, because we don't want to be displayed.
         *
         * @return <code>null</code>.
         */
        public String title() { return null; }
    }

    /**
     * Generates the passes to run before generating the classes.
     */
    private final void generatePrePasses(ConfigurationStandard cs,
                                         RootDoc root)
        throws DocletAbortException {
        generatePasses(cs, root, preGenerationClasses());
    }

    /**
     * Generates the passes to run after generating the classes.
     */
    private final void generatePostPasses(ConfigurationStandard cs,
                                          RootDoc root)
        throws DocletAbortException {
        generatePasses(cs, root, postGenerationClasses());
    }

    /**
     * Generates the passes that run before doing anything.  These
     * passes check that it's OK to do anything.
     */
    private final boolean generateCheckPasses(ConfigurationStandard cs,
                                              RootDoc root)
        throws DocletAbortException {
        try {
            generatePasses(cs, root, checkClasses());
        } catch (DocletAbortException e) {
            return false;
        }
        return true;
    }

    /**
     * Generates passes from <code>classes</code>.  For each
     * class found in <code>classes</code> a constructor taking zero
     * or one-argument is called.  Then the generate method is
     * called on that Pass passing it <code>this</code>, the
     * configuration, and root.
     *
     * @param cs      configuration to use.
     * @param root    root we're documenting.
     * @param classes list of subtypes of {@link Pass} that
     *                will be run.
     */
    private final void generatePasses(ConfigurationStandard cs,
                                      RootDoc root,
                                      Class[] classes)
        throws DocletAbortException {
        if (classes == null) return;
        nextClass:
        for (int i = 0; i < classes.length; i++) {
            try {
                Constructor[] ctrs = classes[i].getConstructors();
            nextCtr:
                for (int j = 0; j < ctrs.length; j++) {
                    Pass pass = null;
                    if (ctrs[j].getParameterTypes().length == 0) {
                        pass = (Pass)ctrs[j].newInstance(new Object[]{});
                    } else if (ctrs[j].getParameterTypes().length == 1) {
                        pass = (Pass)ctrs[j].newInstance(new Object[]{this});
                    }
                    if (pass != null) {
                        pass.generate(this,cs,root);
                        continue nextClass;
                    }
                }
                throw new Exception("Can't create pass for class " + classes[i]);
            } catch (Exception e) {
                e.printStackTrace();
                Standard.configuration().standardmessage.
                    error("doclet.exception", e+"");
                throw new DocletAbortException();
            }
        }
    }


    /**
     * Generates the packages.
     */
    protected void generatePackageCycle(PackageDoc[] pkgs,
                                        boolean createtree,
                                        boolean nodeprecated)
        throws DocletAbortException {
        Arrays.sort(pkgs);
        for (int i = 0; i < pkgs.length; i++) {
            PackageDoc prev = i == 0 ? null : pkgs[i-1];
            PackageDoc curr = pkgs[i];
            PackageDoc next = i == pkgs.length-1 ? null : pkgs[i+1];
            generatePackages(prev, curr, next,
                             createtree, nodeprecated);
        }
    }

    /**
     * Generates a package doc for the three PackageDocs passed.
     */
    protected void generatePackages(PackageDoc prev,
                                    PackageDoc curr,
                                    PackageDoc next,
                                    boolean createtree,
                                    boolean nodeprecated)
        throws DocletAbortException {
        PackageWriter.generate(curr, prev, next);
        if (createtree) {
            PackageTreeWriter.generate(curr, prev,
                                       next, nodeprecated);
        }
        PackageFrameWriter.generate(curr);
    }

    /**
     * Generates all the classes.
     */
    protected void generateClassCycle(ClassDoc[] cs,
                                      ClassTree classtree,
                                      boolean nopackage)
        throws DocletAbortException {
        Arrays.sort(cs);
        for(int i = 0; i < cs.length; i++) {
            if (configuration().nodeprecated && 
                cs[i].tags("deprecated").length > 0) {
                continue;
            }
            ClassDoc prev = i == 0 ? null : cs[i-1];
            ClassDoc curr = cs[i];
            ClassDoc next = i == cs.length-1 ? null : cs[i+1];
            generateClasses(prev, curr, next,
                            classtree, nopackage);
        }
    }

    /**
     * Generates class docs for the three ClassDocs passed.
     */
    protected void generateClasses(ClassDoc  prev,
                                   ClassDoc  curr,
                                   ClassDoc  next,
                                   ClassTree classtree,
                                   boolean nopackage)
        throws DocletAbortException {
        ClassWriter.generate(curr, prev, next,
                             classtree, nopackage);
    }

    /**
     * Returns the delegation to {@link #configuration()}.
     */
    public static int optionLength(String option) {
        return configuration().optionLength(option);
    }

    /**
     * Returns the delegation to {@link #configuration()}.
     */
    public static boolean validOptions(String options[][], 
                                       DocErrorReporter reporter) 
        throws IOException {
        return configuration().validOptions(options, reporter);
    }
} 
        

