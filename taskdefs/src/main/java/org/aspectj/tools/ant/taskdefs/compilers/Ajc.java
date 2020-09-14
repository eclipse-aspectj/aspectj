/* *******************************************************************
 * Copyright (c) 2000-2001 Xerox Corporation. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs.compilers;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;

/**
 * Ajc uses this as the CompilerAdapter.
 *
 * This task was developed by the <a href="http://aspectj.org">AspectJ Project</a>
 *
 * @author <a href="mailto:palm@parc.xerox.com">Jeffrey Palm</a>
 * @see    org.aspectj.tools.ant.taskdefs.Ajc2
 */
public class Ajc extends DefaultCompilerAdapter {

    /** The value of a compiler success. */
    public final static int AJC_COMPILER_SUCCESS = 0;

    /** The name of the compiler's main class. */
    private final static String MAIN_CLASS_NAME = "org.aspectj.tools.ajc.Main";
    
    /**
     * List of arguments allowed only by javac and <b>not</b> ajc.
     */    
    final static List<String> javacOnlyFlags
        = finalList(new String[] { "-g:none", "-g:lines",
        "-g:vars", "-g:source", "-nowarn"});
    final static List<String> javacOnlyArgs  
        = finalList(new String[] { "-sourcepath",
        "-encoding", "-target" });

    private static List<String> finalList(String[] args) {
        List<String> result = new ArrayList<>(Arrays.asList(args));
        return Collections.unmodifiableList(result);
    }

    /**
     * Checks the command line for arguments allowed only in AJC and
     * disallowed by AJC and then calls the <code>compile()</code> method.
     *
     * @return true if a good compile, false otherwise.
     * @throws org.apache.tools.ant.BuildException
     */
    @Override
	public boolean execute() throws BuildException {
        attributes.log("Using AJC", Project.MSG_VERBOSE);
        return compile(addAjcOptions(setupJavacCommand()));
    }

    /**
     * Invokes the AJC compiler using reflection.
     *
     * @param cline the command line to pass the compiler
     * @return      true for a good compile (0), false otherwise
     * @throws      org.apache.tools.ant.BuildException
     */
    private boolean compile(Commandline cline) throws BuildException {
        PrintStream err = System.err;
        PrintStream out = System.out;
        try {
            Class main = Class.forName(MAIN_CLASS_NAME);
            if (main == null) {
                throw new ClassNotFoundException(MAIN_CLASS_NAME);
            }
            PrintStream logstr =
                new PrintStream(new LogOutputStream(attributes,
                                                    Project.MSG_WARN));
            System.setOut(logstr);
            System.setErr(logstr);
            return (Integer) main.getMethod
					("compile", new Class[]{String[].class}).invoke
					(main.newInstance(), new Object[]{
							removeUnsupported(cline, logstr)
					}) == AJC_COMPILER_SUCCESS;
        } catch (Exception e) {
            if (e instanceof BuildException) {
                throw (BuildException)e;
            } else {
                throw new BuildException("Error starting AJC compiler",
                                         e, location);
            }
        } finally {
            System.setErr(err);
            System.setOut(out);
        }
    }

    
    /**
     * Removes unsupported arguments from <code>cline</code>
     * issuing warnings for each using <code>log</code>.
     *
     * @param cline the <code>org.apache.tools.ant.types.Commandline</code> from
     *              which the argument is removed.
     * @return      a new <code>java.lang.String</code> array containing all the
     *              supported arguments found in <code>cline</code>.
     * @throws      org.apache.tools.ant.BuildException
     */
    private String[] removeUnsupported(Commandline cline, PrintStream log) {
        if (null == log) log = System.err;
        String[] args = cline.getCommandline();
        List argsList = new ArrayList();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (javacOnlyFlags.contains(arg)) {
              log.println("ignored by ajc " + arg);
            } else if (javacOnlyArgs.contains(arg)) {
              i++;
              if (i < args.length) {
                arg = arg + " " + args[i];
              }
              log.println("ignored by ajc " + arg);
            } else {
                argsList.add(args[i]);
            }
        }
        return (String[])argsList.toArray(new String[0]);
    }
    
    /**
     * Adds arguments that setupJavacCommand() doesn't pick up.
     *
     * @param cline <code>org.apache.tools.ant.types.Commandline</code> to
     *              which arguments are added.
     * @throws      org.apache.tools.ant.BuildException
     * @see         AjcCompiler#ajcOnlyArgs
     * @see         org.apache.tools.ant.taskdefs.compilers#DefaultCompilerAdapter.setupJavacCommand()
     */
    private Commandline addAjcOptions(Commandline cline) throws BuildException {
        Javac javac = getJavac();
                               
        org.aspectj.tools.ant.taskdefs.Ajc2 ajc = null;

        try {
            ajc = (org.aspectj.tools.ant.taskdefs.Ajc2)javac;
        } catch (ClassCastException cce) {
            throw new BuildException(cce+"");
        }
        
        if (ajc.getThreads() != null) {
            cline.createArgument().setValue("-threads");
            cline.createArgument().setValue(ajc.getThreads() + "");
        }
        if (ajc.getNocomments()) {
            cline.createArgument().setValue("-nocomments");
        }
        if (ajc.getNosymbols()) {
            cline.createArgument().setValue("-nosymbols");
        }
        if (ajc.getPreprocess()) {
            cline.createArgument().setValue("-preprocess");
        }
        if (ajc.getWorkingdir() != null) {
            cline.createArgument().setValue("-workingdir");
            cline.createArgument().setFile(ajc.getWorkingdir());
        }

        return cline;
    }

    /**
     * Logs the compilation parameters, adds the files to compile and logs the 
     * &quot;niceSourceList&quot;
     */
    @Override
	protected void logAndAddFilesToCompile(Commandline cmd) {

        // Same behavior as DefaultCompilerAdapter.logAndAddFilesToCompile
        attributes.log("Compilation args: " + cmd.toString(), Project.MSG_VERBOSE);
        StringBuffer niceSourceList = new StringBuffer("File");
        if (compileList.length != 1) {
            niceSourceList.append("s");
        }
        niceSourceList.append(" to be compiled:");
        niceSourceList.append(lSep);

		for (File file : compileList) {

			// DefaultCompilerAdapter only expects .java files but we must deal
			// with .lst files also
			if (file == null) continue;

			String arg = file.getAbsolutePath();
			String rest = "";
			String name = file.getName();

			// For .java files take the default behavior and add that
			// file to the command line
			if (name.endsWith(".java")) {
				cmd.createArgument().setValue(arg);
			}
			niceSourceList.append("   " + arg + rest + lSep);
		}
        attributes.log(niceSourceList.toString(), Project.MSG_VERBOSE);
    }    
}
