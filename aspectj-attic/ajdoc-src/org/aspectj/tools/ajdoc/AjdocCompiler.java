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

import org.aspectj.compiler.base.AbstractCompilerPass;
import org.aspectj.compiler.base.ErrorHandler;
import org.aspectj.compiler.base.ast.CompilationUnit;
import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.Decs;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.base.ast.World;
import org.aspectj.compiler.crosscuts.AspectJCompiler;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Extension of the AspectJCompiler to provide
 * functionality for creating documentation.
 *
 * @author Jeff Palm
 */
public class AjdocCompiler extends AspectJCompiler implements RootDocMaker {

    /** The name of the program. */
    protected final String programName;

    /** The error printer we use. */
    protected final ErrPrinter err;

    /**
     * Construct a new ajdoc compile with the
     * error handler <code>errorHandler</code> and
     * name <code>programName</code>
     *
     * @param errorHandler the error handler.
     * @param programName  the name of the program.
     */
    public AjdocCompiler(ErrorHandler errorHandler, String programName) {
        super(errorHandler);
        getOptions().preprocess = true;
        getOptions().nocomments = true;
        (errorHandler = err =
         new ErrPrinter(this.programName = programName)).
            setCompiler(this);
    }

    /**
     * Construct a new ajdoc compile with the
     * name <code>programName</code>.
     *
     * @param programName the name of the program.
     */
    public AjdocCompiler(String programName) {
        this(null, programName);
    }

    /**
     * Returns the ErrPrinter currently used.
     *
     * @return the ErrPrinter currently used.
     */
    public ErrPrinter err() {
        return err;
    }

    /** The packages found on the command line. */
    private Set pkgnames = new HashSet();

    /** The classes found on the command line and from files. */
    private Set classnames = new HashSet();

    /** The source files on the command line. */
    private Set files = new HashSet();

    /** The list of source files to compile. */
    protected final List srcfiles = new ArrayList();

    /** The list of filenames that came from user-specified source files. */
    protected List srcSrcfilenames = new ArrayList();

    /** The list of filenames that came from user-specified packages. */
    protected List pkgSrcfilenames = new ArrayList();

    /** The list of filenames that came from user-specified classes. */
    protected List clsSrcfilenames = new ArrayList();

    /** The source path with which to search. */
    protected final List sourcepaths = new ArrayList();
    {
        sourcepaths.add(new File("."));
    }
    /** The list of filenames that came from user-specified classes. */
    protected AccessChecker filter;

    /**
     * Create the RootDoc.
     */
    public RootDoc makeRootDoc(String sourcepath,
                               String classpath,
                               String bootclasspath,
                               String extdirs,
                               long flags,
                               String encoding,
                               String locale,
                               String source,
                               List filenamesAndPackages,
                               List options,
                               DocErrorReporter err,
                               String programName,
                               AccessChecker filter)
        throws CannotMakeRootDocException {
        if ((null != filter) && (this.filter != filter)) {
            this.filter = filter;
        }
        if (null == this.filter) {
            this.filter = AccessChecker.PROTECTED;
        }
        if (classpath != null) {
            getOptions().classpath = classpath;
        }
        if (bootclasspath != null) {
            getOptions().bootclasspath = bootclasspath;
        }
        if (extdirs != null) {
            getOptions().extdirs = extdirs;
        }
        if (source != null) {
            getOptions().source = source;
        }
        resolveSourcePath(sourcepath);
        resolveFilesAndPackages(filenamesAndPackages);

        Collections.sort(pkgSrcfilenames);
        Collections.sort(clsSrcfilenames);
        Collections.sort(srcSrcfilenames);

        srcfiles.addAll(pkgSrcfilenames);
        srcfiles.addAll(clsSrcfilenames);
        srcfiles.addAll(srcSrcfilenames);

        err().notice("starting_internal_compile");

        for (Iterator i = options.iterator(); i.hasNext();) {
            String[] opts = (String[])i.next();
            if (opts.length == 1) {
                if (opts[0].equals("-verbose")) {
                    getOptions().verbose = true;
                }
            } else if (opts.length == 2) {
                if (opts[0].equals("-classpath")) {
                    getOptions().classpath = opts[1];
                } else if (opts[1].equals("-bootclasspath")) {
                    getOptions().bootclasspath = opts[1];
                } else if (opts[1].equals("-extdirs")) {
                    getOptions().extdirs = opts[1];
                }
            }
        }

        // Compile the srcfiles - have to add passes first
        addPasses();
        internalCompile(srcfiles);      

        // This is the world with which we create the root
        World world = getWorld();

        // Add all the classes found in the source files
        // to the list of specified classnames
        for (Iterator i = world.getCompilationUnits().iterator();
             i.hasNext();) {
            Decs decs = ((CompilationUnit)i.next()).getDecs();
            for (int j = 0, N = decs.size(); j < N; j++) {
                Dec dec = decs.get(j);
                if (dec instanceof TypeDec) {
                    classnames.add(((TypeDec)dec).getFullName());
                }
            }
        }

        // Initialize and return the root created
        // from the our world
        err().notice("creating_root");
        RootDoc result = init(this, (String[][])options.toArray
                    (new String[options.size()][]));
                    
        // do another pass at RootDoc after constructed 
        com.sun.javadoc.ClassDoc[] cds = result.classes();
        for (int i = 0; i < cds.length; i++) {
            if (cds[i] instanceof ClassDocImpl) {
                ClassDocImpl cd = (ClassDocImpl) cds[i];
                cd.postProcess();
            }
        }
        return result;
    }
    

    private static AjdocCompiler instance;
    { instance = this; }

    public static AjdocCompiler instance() {
        return instance;
    }

    /**
     * The entry point to initialize a world created
     * from an AspectJCompiler.
     *
     * @param ajc     the compiler.
     * @param options the ajdoc options.
     * @return        a RootDocImpl representing the
     *                documentation tree.
     */
    public static RootDocImpl init(AspectJCompiler ajc, String[][] options) {

        if (ajc == null) return null; //TODO: make empty

        World world = ajc.getWorld();

        Collection classnames = null;
        Collection pkgnames = null;
        if (ajc instanceof AjdocCompiler) {
            AjdocCompiler ajdoc = (AjdocCompiler)ajc;
            pkgnames = ajdoc.pkgnames;
            classnames = ajdoc.classnames;
        }

        PackageDocImpl.init(ajc);

        AccessChecker filter = AccessChecker.PUBLIC;
        if (ajc instanceof AjdocCompiler) {
            filter = ((AjdocCompiler) ajc).getFilter();
        }
        RootDocImpl root = new RootDocImpl(world,
                                           options,
                                           pkgnames,
                                           classnames,
                                           filter);
        return root;
    }

    /** set filter associated with this compiler */
    protected void setFilter(AccessChecker filter, String arg) {
        this.filter = filter;
    }

    /** get filter associated with this compiler */
    public final AccessChecker getFilter() {
        return filter;
    }

    protected final void expandAtFile(String filename, 
                                      List args) throws IOException {
        BufferedReader in  = new BufferedReader(new FileReader(filename));
        String dirfile = new File(filename).getParent();
        File basedir = new File(null == dirfile ? "." : dirfile ) ;
        String line;
        while ((line = in.readLine()) != null) {
            if (line == null || line.length() < 1) continue;
            line = line.trim();
            if (line.startsWith("//")) continue;
            if (line.startsWith("#")) continue;
            if (line.startsWith("@")) {
                line = line.substring(1);
                File newfile = new File(line);
                newfile = newfile.isAbsolute() ?
                    newfile : new File(basedir, line);
                expandAtFile(newfile.getPath(), args);
            } else {
                File newfile = new File(line);
                newfile = newfile.isAbsolute() ?
                    newfile : new File(basedir, line);
                if (newfile.exists()) {
                    boolean result = maybeAdd(newfile, args);
                    if (!result) {
                        // we only support valid filenames, not options
                        cantResolve(newfile); 
                    }
                } else {
                    boolean addedFile = false;
                    FileFilter filter = null;
                    String name = newfile.getName().trim();
                    if (name.equals("*.java")) {
                        filter = new FileFilter() {
                                    public boolean accept(File f) {
                                        return f != null &&
                                            f.getName().endsWith(".java");
                                    }
                                };
                    } else if (name.equals("*.aj")) {
                        filter = new FileFilter() {
                                    public boolean accept(File f) {
                                        return f != null &&
                                            f.getName().endsWith(".java");
                                    }
                                };
                    } else if (name.equals("*")) {
                        filter = new FileFilter() {
                                    public boolean accept(File f) {
                                        return f != null &&
                                            (f.getName().endsWith(".java")
                                            || f.getName().endsWith(".aj"));
                                    }
                                };
                    } 
                    if (null != filter) {
                        File parentDir = newfile.getParentFile();
                        File[] javafiles = parentDir.listFiles(filter);
                        if (javafiles != null) {
                            for (int i = 0; i < javafiles.length; i++) {
                                if (maybeAdd(javafiles[i], args)) {
                                    if (!addedFile) addedFile = true;
                                } else {
                                    cantResolve(javafiles[i]);
                                }
                            }
                        }
                    }
                    if (!addedFile) {
                        if (isValidPkg(line)) {
                            args.add(line);
                        } else {
                            cantResolve(newfile);
                        }
                    }
                }
            }
        }
        in.close();
    }

    protected final void cantResolve(File f) {
        err().error("cant_resolve_file", f.getAbsolutePath());
    }

    private void resolveSourcePath(String sourcepath) {
        if (sourcepath != null) {
            sourcepaths.remove(0);
            for (StringTokenizer t = new StringTokenizer(sourcepath,
                                                         File.pathSeparator);
                 t.hasMoreTokens();) {
                File path = new File(t.nextToken().trim());
                if (path.exists() && path.isDirectory()) {
                    sourcepaths.add(path);
                }
            }
            // TODO: don't want this, I think ????
            //sourcepaths.add(new File("."));
        }
    }

    private void resolveFilesAndPackages(List filenamesAndPackages) {
        Collection pkgnamesFromCmd = new HashSet();
        for (Iterator i = filenamesAndPackages.iterator(); i.hasNext();) {
            String str = (String)i.next();
            File file = new File(str);
            if (/*file.isAbsolute() &&*/ maybeAdd(file, srcSrcfilenames)) {
                addFile(file);
                continue;
            } else {
                for (Iterator j = sourcepaths.iterator(); j.hasNext();) {
                    File sourcepath = (File)j.next();
                    file = new File(sourcepath, str);
                    if (maybeAdd(file, srcSrcfilenames)) {
                        addFile(file);
                        continue;
                    }
                }
            }
            pkgnamesFromCmd.add(str);
        }
        for (Iterator i = pkgnamesFromCmd.iterator(); i.hasNext();) {
            resolvePackageOrClass((String)i.next());
        }
    }

    private void resolvePackageOrClass(String pkgOrClassName) {
        boolean recurse;
        String pkgOrClass =
            (recurse = (pkgOrClassName.endsWith(".*"))) ?
            pkgOrClassName.substring(0, pkgOrClassName.length()-2) :
            pkgOrClassName;
        for (Iterator i = sourcepaths.iterator(); i.hasNext();) {
            File sourcepath = (File)i.next();
            File possiblePkg = new File(sourcepath,
                                        pkgOrClass.replace
                                        ('.', File.separatorChar));
            if (possiblePkg.exists() && possiblePkg.isDirectory()) {
                if (recurse) {
                    File[] dirs = possiblePkg.listFiles
                        (new FileFilter() {
                                public boolean accept(File f) {
                                    return f != null && f.isDirectory();
                                }
                            });
                    for (int j = 0; j < dirs.length; j++) {
                        String pkgname = pkgOrClass + '.' + dirs[j].getName();
                        resolvePackageOrClass(pkgname + ".*");
                    }
                }
                File[] javafiles = possiblePkg.listFiles
                    (new FileFilter() {
                            public boolean accept(File f) {
                                return f != null && !f.isDirectory();
                            }
                        });
                if (javafiles.length > 0) {
                    pkgnames.add(pkgOrClass);
                }
                boolean addedPkg = false;
                for (int j = 0; j < javafiles.length; j++) {
                    if (maybeAdd(javafiles[j], pkgSrcfilenames) && !addedPkg) {
                        addPkg(pkgOrClass, javafiles[j]);
                        addedPkg = true;
                    }
                }
                break;
            } else {
                String pkgname = "";
                String classname = pkgOrClass;
                int ilastdot = pkgOrClass.lastIndexOf('.');
                if (ilastdot != -1) {
                    pkgname =  pkgOrClass.substring(0, ilastdot).
                        replace('.', File.separatorChar) + File.separatorChar;
                    classname = pkgOrClass.substring(ilastdot+1);
                }
                File file = new File(sourcepath,
                                     pkgname + classname + ".java");
                if (maybeAdd(file, clsSrcfilenames)) {
                    addClass(pkgOrClass, file);
                    break;
                }
            }
        }
    }

    protected final File findFile(String filename, boolean isDir) {
        for (Iterator i = sourcepaths.iterator(); i.hasNext();) {
            File sourcepath = (File)i.next();
            File file = new File(sourcepath, filename);
            if (file.exists() && !(isDir ^ file.isDirectory())) {
                return file;
            }
        }
        return null;
    }

    protected static boolean maybeAddPkg(String pkgname,
                                         Collection pkgnames) {
        if (isValidPkg(pkgname)) {
            pkgnames.add(pkgname);
            return true;
        }
        return false;
    }

    protected final Map filesToClassnames = new HashMap();
    protected final void addClass(String classname, File file) {
        if (!(maybeAddClass(classname))) {
            err().error("invalid_class_name", classname);
        } else {
            filesToClassnames.put(file.getAbsoluteFile(), classname);
        }
    }

    protected final boolean maybeAddClass(String classname) {
        return maybeAddClass(classname, classnames);
    }

    protected static boolean maybeAddClass(String classname,
                                           Collection classnames) {
        if (isValidClass(classname)) {
            classnames.add(classname);
            return true;
        }
        return false;
    }

    protected final static boolean isValidClass(String classname) {
        return isValidPkg(classname);
    }

    protected final Map filesToPkgnames = new HashMap();
    protected final void addPkg(String pkgname, File file) {
        if (!maybeAddPkg(pkgname)) {
            err().error("invalid_package_name", pkgname);
        } else {
            filesToPkgnames.put(file.getAbsoluteFile(), pkgname);
       }
    }

    protected final boolean maybeAddPkg(String pkgname) {
        return maybeAddPkg(pkgname, pkgnames);
    }

    protected final Map filesToFilenames = new HashMap();
    protected final void addFile(File file) {
        files.add(file);
        filesToFilenames.put(file.getAbsoluteFile(), file.getAbsolutePath());
    }

    protected static boolean maybeAdd(File file, Collection files) {
        if (isValidJavaFile(file)) {
            files.add(file.getAbsolutePath());
            return true;
        }
        return false;
    }

    protected final static boolean isValidJavaFile(File file) {
        return file != null && file.exists() && !file.isDirectory()
            && (file.getName().endsWith(".java")
                || file.getName().endsWith(".aj")) ;
    }

    protected final static boolean isValidPkg(String pkgname) {
        if (pkgname == null) {
            return false;
        }
        if (pkgname.length() < 1) {
            return true;
        }
        if (!Character.isJavaIdentifierStart(pkgname.charAt(0))) {
            return false;
        }
        for (int i = 1; i < pkgname.length(); i++) {
            char c = pkgname.charAt(i);
            if (c == '.' && i == pkgname.length()-1) {
                return false;
            }
            if (!(c == '.' || Character.isJavaIdentifierPart(c))) {
                return false;
            }
        }
        return true;
    }

    protected void loading(CompilationUnit cu) {
        File srcfile = cu.getSourceFile().getAbsoluteFile();
        String pkgname, classname, filename;
        if ((pkgname = (String)filesToPkgnames.get(srcfile))!= null) {
            AjdocCompiler.this.err().notice
                ("Loading_source_files_for_package", pkgname);
        } else if ((classname = (String)filesToClassnames.get(srcfile)) != null) {
            AjdocCompiler.this.err().notice
                ("Loading_source_file_for_class", classname);
        } else if ((filename = (String)filesToFilenames.get(srcfile)) != null) {
            AjdocCompiler.this.err().notice
                ("Loading_source_file", filename);
        }
    }

    protected AbstractCompilerPass createParserPass() {
        return new PrintingParserPass(this);
    }
    
    protected static class PrintingParserPass extends AspectJCompiler.ParserPass {
        public PrintingParserPass(AjdocCompiler jc) { super(jc); }
        public void transform(CompilationUnit cu) {
            ((AjdocCompiler)getCompiler()).loading(cu);
            super.transform(cu);
        }
    }

    protected void addPasses() {
        passes = new ArrayList();
        addPreSymbolPasses();
    }
}
