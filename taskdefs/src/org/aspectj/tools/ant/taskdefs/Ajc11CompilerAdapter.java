/* *******************************************************************
 * Copyright (c) 2001-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.tools.ant.taskdefs;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.aspectj.bridge.*;
import org.aspectj.util.LangUtil;


/**
 * EXPERIMENTAL - This adapts the AspectJ compiler as if it were Javac.
 * It just passes the arguments through to the compiler.
 * Limitations:
 * <ol>
 * <li>Users still must specify all source files to compile.</li>
 * <li>No support for non-Javac options</li>
 * <li>Javac srcdir are treated as sourceroots unless -DajcAdapterSkipRoots=...</li>
 * </ol>
 * 
 * @author Wes Isberg <a href="mailto:isberg@aspectj.org">isberg@aspectj.org</a>
 * @since AspectJ 1.1, Ant 1.3
 */
public class Ajc11CompilerAdapter extends DefaultCompilerAdapter {
    /*
     * Not sure how best to implement our functionality -- not documented.
     * Desired functionality:
     * - ability to run in-line or forked 
     *   - switch based only on fork="[true|false]" 
     *     (so classpath required to fork must be calculated)
     *   - able to set memory available when forking
     * - handles .lst files on command-line by prefixing with @
     *   - and/or handles compiler option -XargPrefix=...
     * - this means all AspectJ-specific options (injars/outjars...)
     *   are in the .lst file?
     * 
     * Implementation options:
     * - straight CompilerAdapter
     * - override DefaultCompilerAdapter methods for template processes
     * - create a new FacadeTaskHelper, supply to DefaultCompilerAdaper
     */
    public static final String SKIP_ROOTS_NAME = "ajcAdapterSkipRoots";

    // ------------------------------ setup
    /**
     * Adds the command line arguments specifc to the current implementation.
     */
    protected void addCurrentCompilerArgs(Commandline cmd) {
        cmd.addArguments(getJavac().getCurrentCompilerArgs());
    }

    // ------------------------------ run
    /**
     * Run the compilation.
     *
     * @exception BuildException if the compilation has problems.
     */
    public boolean execute() throws BuildException {
        Commandline cmd = setupModernJavacCommand();

        try {
            String[] args = cmd.getArguments();
            int result = runCompiler(args);
            return (0 == result);
        } catch (BuildException e) {
            throw e;
        } catch (AbortException x) {
            if (AbortException.ABORT.equals(x)) { // no message, just return
                return false;
            } else {
                Throwable t = x.getThrown();
                if (null == t) {
                    t = x;
                }
                throw new BuildException("Thrown: ", t, location);
            }
        } catch (Throwable x) {
            throw new BuildException("Thrown: ", x, location);
        }
    }
    
    // run? handle forking?
    private int runCompiler(String[] args) {
        int result = -1;
        IMessageHolder sink = new MessageHandler();
        ICommand compiler = ReflectionFactory.makeCommand(ReflectionFactory.ECLIPSE, sink);
        if ((null == compiler) || sink.hasAnyMessage(IMessage.FAIL, true)) {
            throwBuildException("loading compiler", sink);
        } else {
            args = filterArgs(args);
            if (!compiler.runCommand(args, sink)) {
                System.err.println("runCompiler args: " + Arrays.asList(args));
                throwBuildException("running compiler", sink);
            } else {
                result = 0;
            }
        }
        return  result;
    }

	/**
	 * Method throwBuildException.
	 * @param string
	 * @param sink
	 */
	private void throwBuildException(String string, IMessageHolder sink) { // XXX nicer
        if ((null != sink) && (0 < sink.numMessages(null, true))) {
            MessageUtil.print(System.err, sink, null);
        }
        throw new BuildException(string + ": " + sink, location);
	}
    
    /** Convert javac argument list to a form acceptable by ajc */
    protected String[] filterArgs(String[] args) {
        LinkedList argList = new LinkedList();
        argList.addAll(LangUtil.arrayAsList(args));
        ArrayList roots = new ArrayList();
        for (ListIterator iter = argList.listIterator(); iter.hasNext();) {
			String arg = (String) iter.next();
            if ("-sourcepath".equals(arg)) { // similar to -sourceroots?
                iter.remove();
                roots.add(iter.next()); // need next after remove?
                iter.remove();
            } else if ("-0".equals(arg)) { // unsupported
                System.err.println("warning: ignoring -0 argument");
                iter.remove();
            } else if ("-bootclasspath".equals(arg)) { // ajc fakes
            } else if ("-extdirs".equals(arg)) { // ajc fakes
            } else if ("-target".equals(arg)) { // -1.4 or -1.3
                iter.remove();
                String vers = (String) iter.next();
                if ("1.3".equals(vers)) {
                    iter.set("-1.3");
                } else if ("1.4".equals(vers)) {
                    iter.set("-1.4");
                } else { // huh?
                    String s = "expecting 1.3 or 1.4 at " + vers + " in " + argList;
                    throwBuildException(s, null);                    
                }
            }
		}
        
        if (0 < roots.size()) {
            String skipRoots = null;
            try {
                skipRoots = System.getProperty(SKIP_ROOTS_NAME);
            } catch (Throwable t) {
                // ignore SecurityException, etc.
            }
            
            if (null == skipRoots) {
                StringBuffer sb = new StringBuffer();
                boolean first = true;
                for (Iterator iter = roots.iterator(); iter.hasNext();) {
                    if (!first) {
                        sb.append(File.pathSeparator);
                    } else {
                        first = false;
                    }
                    sb.append((String) iter.next());
                }
                argList.add(0, "-sourceroots");
                argList.add(1, sb.toString());
            }
        }

        return (String[]) argList.toArray(new String[0]);
    }
}
