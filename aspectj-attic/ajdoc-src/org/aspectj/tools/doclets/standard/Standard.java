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

import org.aspectj.tools.ajdoc.Quietable;

import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.DocletAbortException;
import com.sun.tools.doclets.standard.AllClassesFrameWriter;
import com.sun.tools.doclets.standard.FrameOutputWriter;
import com.sun.tools.doclets.standard.HelpWriter;
import com.sun.tools.doclets.standard.PackageIndexFrameWriter;
import com.sun.tools.doclets.standard.PackageIndexWriter;
import com.sun.tools.doclets.standard.PackageListWriter;
import com.sun.tools.doclets.standard.PackagesFileWriter;
import com.sun.tools.doclets.standard.SerializedFormWriter;
import com.sun.tools.doclets.standard.StylesheetWriter;

import java.io.IOException;

/**
 * Main doclet for ajdoc.  It defines a number of
 * passes to use in generating the documentation.
 *
 * @author Jeff Palm
 */
public class Standard extends AbstractStandard {
    private static Standard SINGLETON; // todo: prefer early/final?
    public static final Standard getSingleton() {
        if (null == SINGLETON) {
            SINGLETON = new Standard();
        }
        return SINGLETON;
    }
    private Standard() {}

    public static boolean start(RootDoc root) throws IOException {
        return start(getSingleton(), root);
    }

    public ConfigurationStandard getConfiguration() {
        return (ConfigurationStandard)configuration();
    }

    public static void quiet() {
        if (configuration().root instanceof Quietable) {
            ((Quietable)configuration().root).quiet();
        }
    }
    public static void speak() {
        if (configuration().root instanceof Quietable) {
            ((Quietable)configuration().root).speak();
        }
    }

    public static class ClassUseMapperPass extends Pass {
        protected boolean cond() {
            return cs.classuse;
        }
        protected void gen() throws DocletAbortException {
            ClassUseMapper.generate(root, std.classtree);
        }
        public String title() { return "class use mapper"; }
    }

    public static class TreeWriterPass extends Pass {
        protected boolean cond() {
            return cs.createtree;
        }
        protected void gen() throws DocletAbortException {
            TreeWriter.generate(std.classtree);
        }
        public String title() { return "tree writer"; }
    }

    public static class SplitIndexWriterPass extends Pass {
        protected boolean cond() {
            return cs.createindex && cs.splitindex;
        }
        protected void gen() throws DocletAbortException {
            SplitIndexWriter.generate(std.indexBuilder(root, false));
        }
        public String title() { return "split index"; }
    }

    public static class SingleIndexWriterPass extends Pass {
        protected boolean cond() {
            return cs.createindex && !cs.splitindex;
        }
        protected void gen() throws DocletAbortException {
            SingleIndexWriter.generate(std.indexBuilder(root, false));
        }
        public String title() { return "single index"; }
    }

    public static class DeprecatedListWriterPass extends Pass {
        protected boolean cond() {
            return !cs.nodeprecatedlist && !cs.nodeprecated;
        }
        protected void gen() throws DocletAbortException {
            DeprecatedListWriter.generate(root);
        }
        public String title() { return "deprecated list"; }
    }

    public static class AllClassesFrameWriterPass extends Pass {
        protected void gen() throws DocletAbortException {
            AllClassesFrameWriter.generate(std.indexBuilder(root, true));
        }
        public String title() { return "all classes frame"; }
    }

    public static class FrameOutputWriterPass extends Pass {
        protected void gen() throws DocletAbortException {
            FrameOutputWriter.generate();
        }
        public String title() { return "output frame"; }
    }

    public static class PackagesFileWriterPass extends Pass {
        protected void gen() throws DocletAbortException {
            PackagesFileWriter.generate();
        }
        public String title() { return "packages files"; }
    }

    public static class PackageIndexWriterPass extends Pass {
        protected boolean cond(ConfigurationStandard cs) {
            return cs.createoverview;
        }
        protected void gen() throws DocletAbortException {
            PackageIndexWriter.generate(root);
        }
        public String title() { return "package index"; }
    }
    
    public static class PackageIndexFrameWriterPass extends Pass {
        protected boolean cond() {
            return cs.packages.length > 1;
        }
        protected void gen() throws DocletAbortException {
            PackageIndexFrameWriter.generate();
        }
        public String title() { return "package index frame"; }
    }
    
    protected Class[] preGenerationClasses() {
        return new Class[] {
            ClassUseMapperPass.class,
            TreeWriterPass.class,
            SplitIndexWriterPass.class,
            SingleIndexWriterPass.class,
            DeprecatedListWriterPass.class,
            AllClassesFrameWriterPass.class,
            FrameOutputWriterPass.class,
            PackagesFileWriterPass.class,
            PackageIndexWriterPass.class,
            PackageIndexFrameWriterPass.class,
        };
    }

    public static class SerializedFormWriterPass extends Pass {
        protected void gen() throws DocletAbortException {
            SerializedFormWriter.generate(root);
        }
        public String title() { return "serialized form"; }
    }

    public static class PackageListWriterPass extends Pass {
        protected void gen() throws DocletAbortException {
            PackageListWriter.generate(root);
        }
        public String title() { return "package list"; }
    }

    public static class HelpWriterPass extends Pass {
        protected boolean cond() {
            return cs.helpfile.length() == 0 &&
                !cs.nohelp;
        }
        protected void gen() throws DocletAbortException {
            HelpWriter.generate();
        }
        public String title() { return "help"; }
    }

    public static class StylesheetWriterPass extends Pass {
        protected boolean cond() {
            return cs.stylesheetfile.length() == 0;
        }
        protected void gen() throws DocletAbortException {
            StylesheetWriter.generate();
        }
        public String title() { return "style sheet"; }
    }

    
    protected Class[] postGenerationClasses() {
        return new Class[] {
            SerializedFormWriterPass.class,
            PackageListWriterPass.class,
            HelpWriterPass.class,
            StylesheetWriterPass.class,
        };
    }

    public static class NoPublicClassesToDocumentCheck extends Check {
        protected boolean cond() {
            return root.classes().length == 0;
        }
        protected String message() {
            return "doclet.No_Public_Classes_To_Document";
        }
    }
    public static class NoNonDeprecatedClassToDocumentCheck extends Check {
        protected boolean cond() {
            return cs.topFile.length() == 0;
        }
        protected String message() {
            return "doclet.No_Non_Deprecated_Classes_To_Document";
        }
    }

    protected Class[] checkClasses() {
        return new Class[] {
            NoPublicClassesToDocumentCheck.class,
            NoNonDeprecatedClassToDocumentCheck.class,
        };
    }
} 
        

