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

import org.aspectj.compiler.base.CompilerErrors;
import org.aspectj.compiler.base.ExitRequestException;
import org.aspectj.compiler.base.InternalCompilerError;

import com.sun.javadoc.RootDoc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Front-end for ajdoc.
 *
 */ 
public class Ajdoc extends AjdocCompiler {

    private final static int VERBOSE  = 1;
    private final static int WARNINGS = 4;
   
    private DocletProxy docletProxy;
    private RootDocMaker rootDocMaker;          
    private String source = null;
    private String extdirs = null;
    private String locale = null;
    private String encoding = null;
    private String sourcepath = null;
    private String classpath = null;
    private String bootclasspath = null;
    private int verbosity = WARNINGS;
    private List filenamesAndPackages = new ArrayList();
    private List options = new ArrayList();

    public Ajdoc(String programName) {
        super(programName);
    }

    public Ajdoc() {
        this("ajdoc");
    }

    /**
     * Programmatic entry into this compiler that
     * uses the error printer to catch internal errors.
     *
     * @param args Command line arguments.
     * @return     <code>0</code> for a successful document.
     */
    public int execute(String[] args) {
        try {
            return doc(args) && err.getNumErrors() == 0 ? 0 : 1;
        } catch (ExitRequestException exit) {
            return exit.getValue();
        } catch (CompilerErrors err) { 
            return err.errors;   // report error already printed by ajc
        } catch (InternalCompilerError error) { // cf ajc.Main
            handleInternalError(error.uncaughtThrowable);
            error.showWhere(new PrintWriter(System.err));
            return -2;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            err.internalError("internal_msg", e);
            return 1;
        }
    }

    /** copied from ajc.Main */
    public void handleInternalError(Throwable uncaughtThrowable) {
        System.err.println("An internal error occured in Ajdoc invocation of AspectJ-"
                           +getCompiler().getVersion());
        System.err.println(uncaughtThrowable.toString());
        uncaughtThrowable.printStackTrace(System.err);
        System.err.println();
    }

    /**
     * Programmatic entry into this compiler that
     * doesn't use the error printer to catch internal errors.
     *
     * @param args Command line arguments.
     * @return     <code>true</code> for a succesful run.
     */
    public boolean doc(String[] args) {

        long start = System.currentTimeMillis();

        if (args == null) {
            err.error("internal_error", "Arguments cannot be null");
            return false;
        }

        try {
            args = expandAndCreateDoclet(args);
        } catch (FileNotFoundException e) {
            err.error("file_not_found_exception", e.getMessage());
            return false;
        } catch (IOException e) {
            err.error("io_exception", e.getMessage());
            return false;
        }
        
        for (int i = 0; i < args.length;) {
            String arg = args[i++];
            if (arg.equals("-overview")) {
                set(args, i++);
            } else if (arg.equals("-public")) {
                set(arg);
                if (filter != null) {
                    err.error("argument_already_seen", arg);
                } else {
                    setFilter(AccessChecker.PUBLIC, arg);
                }
            } else if (arg.equals("-protected")) {
                set(arg);
                if (filter != null) {
                    err.error("argument_already_seen", arg);
                } else {
                    setFilter(AccessChecker.PROTECTED, arg);
                }
            } else if (arg.equals("-package")) {
                set(arg);
                if (filter != null) {
                    err.error("argument_already_seen", arg);
                } else {
                    setFilter(AccessChecker.PACKAGE, arg);
                }
            } else if (arg.equals("-private")) {
                set(arg);
                if (filter != null) {
                    err.error("argument_already_seen", arg);
                } else {
                    setFilter(AccessChecker.PRIVATE, arg); 
                }
            } else if (arg.equals("-help")) {
                usage(0);
            } else if (arg.equals("-sourcepath")) {
                if (sourcepath != null) {
                    usage("argument_already_seen", arg);
                }
                sourcepath = set(args, i++);
            }else if (arg.equals("-classpath")) {
                if (classpath != null) {
                    usage("argument_already_seen", arg);
                }
                classpath = set(args, i++);
            }else if (arg.equals("-bootclasspath")) {
                if (bootclasspath != null) {
                    usage("argument_already_seen", arg);
                }
                bootclasspath = set(args, i++); 
            }else if (arg.equals("-source")) {
                if (source != null) {
                    usage("argument_already_seen", arg);
                }
                source = set(args, i++);
            } else if (arg.equals("-extdirs")) {
                if (extdirs != null) {
                    usage("argument_already_seen", arg);
                }
                extdirs = set(args, i++);
            } else if (arg.equals("-verbose")) {
                set(arg);
                verbosity |= VERBOSE;
            } else if (arg.equals("-locale")) {
                if (locale != null) {
                    usage("argument_already_seen", arg);
                }
                set(args, i++);
            } else if (arg.equals("-encoding")) {
                if (encoding != null) {
                    usage("argument_already_seen", arg);
                }
                encoding = set(args, i++);
            } else if (arg.equals("-compiler")) { 
                err.warning("usage_help", "-compiler option ignored");
            } else if (arg.equals("-debug")) { 
                err.warning("usage_help", "-debug option disabled");
            } else if (arg.startsWith("-J")) { // todo unsupported?
                if (arg.length() == 2) continue;
                String rest = arg.substring(2);
                int ieq = rest.indexOf('=');
                String key, val;
                if (ieq != -1) {
                    key = rest.substring(0, ieq);
                    val = rest.substring(ieq+1);
                } else {
                    key = rest;
                    val = "";
                }
                System.setProperty(key, val);
            } else if (arg.startsWith("-")) {
                int optionLength = docletProxy.optionLength(arg);
                if (optionLength < 0) {
                    exit();
                } else if (optionLength == 0) {
                    usage("invalid_flag", arg);
                } else if (optionLength > args.length) {
                    usage("requires_argument", arg, optionLength+"");
                } else {
                    List iargs = new ArrayList();
                    iargs.add(arg);
                    for (int j = 0; j < optionLength-1; j++) {
                        iargs.add(args[i++]);
                    }
                    set((String[])iargs.toArray(new String[iargs.size()]));
                }
            } else {
                filenamesAndPackages.add(arg);
            }
        }
        if (locale == null) {
            locale = "";
        }
        if (sourcepath == null) {
            sourcepath = ".";
        }
        try {
            if (!docletProxy.validOptions(options, err)) {
                exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            err.internalError("internal_msg", e);
            return false;
        }
        if (filenamesAndPackages.size() < 1) {
            usage("No_packages_or_classes_specified");
            return false;
        }
        RootDoc rootDoc = null;
        boolean good = true;
        try {
            rootDoc = makeRootDoc(sourcepath,
                                  classpath,
                                  bootclasspath,
                                  extdirs,
                                  verbosity,
                                  encoding,
                                  locale,
                                  source,
                                  filenamesAndPackages,
                                  options,
                                  err,
                                  programName,
                                  getFilter());
        } catch (CannotMakeRootDocException e) {
            err.error("cant_create_root_doc_ex", "AjdocCompiler", e.getMessage());
            exit(1);
            good = false;
        }
        good &= rootDoc != null;
        if (good) {
            err.notice("generating_docs");
            try {
                good &= docletProxy.start(rootDoc);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                err.internalError("internal_msg", e);
                return false;
            }
        }
        if ((verbosity & VERBOSE) != 0) {
            err.notice("done_in", Long.toString(System.currentTimeMillis()-start));
        }
        return good;
    }
    

    private void usage(String key, String s0, String s1) {
        err.error(key, s0, s1);
        usage(1);
    }

    private void usage(String key, String s0) {
        usage(key,s0,"");
    }

    private void usage(String key) {
        usage(key,"");
    }

    private void usage() {
        err.notice("usage_help", programName);
        if (docletProxy != null) { docletProxy.optionLength("-help"); }
    }
    
    private void usage(int exit) {
        usage();
        exit(exit);
    }

    protected String[] expandAndCreateDoclet(String[] args) throws IOException {
        List list = new ArrayList();
        docletProxy = DocletProxy.DEFAULT;
        for (int i = 0; i < args.length;) {
            String arg = args[i++];
            if (arg == null || arg.length() < 1) continue;
            if (arg.charAt(0) == '@') {
                expandAtFile(arg.substring(1), list);
            } else if (arg.equals("-argfile")) {
                if (check(args, i)) {
                    expandAtFile(args[i++], list);
                }
            } else if (arg.equals("-doclet")) {
                err.warning("usage_help", "-doclet option ignored");
            } else if (arg.equals("-docletpath")) {
                err.warning("usage_help", "-docletpath option ignored");
            } else if (arg.equals("-standard")) { 
                docletProxy = DocletProxy.STANDARD;
            } else {
                list.add(arg);
            }
        }
        return (String[])list.toArray(new String[list.size()]);
    }

    private static boolean check(String[] args, int i) {
        return i >= 0 && i < args.length;
    }

    private String set(String[] args, int i) {
        String arg = null;
        if (check(args, i)) {
            arg = args[i];
            set(args[i-1], arg);
        } else {
            err.internalError("internal_error",
                              new ArrayIndexOutOfBoundsException(i));
        }
        return arg;
    }

    private void set(String opt) {
        set(new String[]{opt});
    }

    private void set(String opt, String arg) {
        set(new String[]{opt, arg});
    }

    private void set(String[] opts) {
        options.add(opts);
    }

    protected void internalCompile(List filenames) {
        super.internalCompile(filenames);
    }

    private final void exit() {
        exit(0);
    }
    
    private void exit(int exit) {
        throw new ExitRequestException(exit);
    }

    private static String classname(Object o) {
        return o != null ? o.getClass().getName() : "null";
    }
}
