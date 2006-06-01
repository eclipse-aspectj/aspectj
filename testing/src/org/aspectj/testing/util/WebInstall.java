/* *******************************************************************
 * Copyright (c) 1999-2000 Xerox Corporation. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.testing.util;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/** 
 * Install programmatically using http URL.
 * (Very strange that java tool classpath does not accept http URL's.)
 *
 * Example:
 * <p><code>java -classpath aj-testing.jar org.aspectj.testing.util.WebInstall
 *    http://aspectj.org/download/distribution/aspectj10-tools.jar -text
 *    install.properties</code>
 *
 * <p>You can omit the <code>-text install.properties</code> if there is
 * a file called "install.properties" in the current directory.
 *
 * <p>The properties file must define the properties 
 * <code>output.dir</code> and <code>context.javaPath</code>
 * in properties- and platform specifie ways.  
 * For property values, a backslash must be escaped with another backslash,
 * and directory separators should be valid. E.g., on Windows:
 * <pre>output.dir=c:\\output\\dir
 * context.javaPath=c:\\apps\\jdk1.3.1</pre>
 *
 * For an installer to complete programmatically,
 * the output directory must be empty of colliding files.
 * This will fail with a stack trace if anything goes wrong, except for
 * simple input errors.  
 *
 * <p>You may also use this as a driver for the known installers
 * by specifying the following options (-baseurl must be first):<pre>
 *         -baseurl           {baseurl}
 *         -version           {version}
 *         -context.javaPath  {path to JDK}    (properties form)
 *         -output.dir        {path to outDir} (properties form, including trailing /)
 *         -outdir            {path to outDir} (actual form) </pre>
 * such that URL=
 * <code>{baseurl}<packagePrefix>{version}<packageSuffix>.jar</code>
 * and paths to context.javaPath and output.dir are specified in 
 * properties-compliant format
 *
 * @see ant script test-product.xml for example of installing from files
 *      which can be driven from the command-line.
 */
public class WebInstall {
    private static final String EOL = "\n"; // todo where is this defined?
    public static final String SYNTAX 
        = "java WebInstall url {args}" + EOL
        + "  url  - to installer"  + EOL
        + "  args - normally -text install.properties"  + EOL
        + "         (if n/a, use install.properties)"  + EOL;

    /** default arguments assume file <code>install.properties</code> 
     * is in current directory */
    private static final String[] ARGS = new String[] 
    { "-text", "install.properties" };

    /** @param args the String[] <code>{ "<url>" {, "-text", "<propsPath>" }</code> */
    public static void main(String[] args) throws Exception {
        if ((null != args) && (args.length > 0) 
            && ("-baseurl".equals(args[0]))) {
            driver(args);
        } else {
            try {
                new WebInstall().install(args);
            } catch (Throwable t) {
                System.err.println("Error installing args ");
                for (int i = 0; i < args.length; i++) {
                    System.err.println(" " + i + ": " + args[i]);
                } 
                t.printStackTrace(System.err);
            }
        }
    }

    /** known .jar packages {(prefix, suffix}...} */
    protected static String[] packages = new String[] 
    { "aspectj-tools-", ""
    , "aspectj-docs-", ""
    , "ajde-forteModule-", ""
    , "ajde-jbuilderOpenTool-", ""
    };

    /**
     * Drive install of all jar-based installers.
     * @param args the String[] containing<pre>
     *         -baseurl           {baseurl}
     *         -version           {version}
     *         -context.javaPath  {path to JDK}    (properties form)
     *         -output.dir        {path to outDir} (properties form, including trailing /)
     *         -outdir            {path to outDir} (actual form) </pre>
     * such that URL=
     * <code>{baseurl}<packagePrefix>{version}<packageSuffix>.jar</code>
     * and paths to context.javaPath and output.dir are specified in 
     * properties-compliant format
     */
    protected static void driver(String[] args) throws Exception {
        String baseurl = null;
        String version = null;
        String outputDir = null;
        File outdir = null;
        String jdk = null;
        for (int i = 0; i < args.length; i++) {
            if ("-baseurl".equals(args[i])) {
                baseurl = args[++i];
            } else if ("-version".equals(args[i])) {
                version = args[++i];
            } else if ("-context.javaPath".equals(args[i])) {
                jdk = args[++i];
            } else if ("-output.dir".equals(args[i])) {
                outputDir=args[++i];
            } else if ("-outdir".equals(args[i])) {
                outdir = new File(args[++i]).getCanonicalFile();
                if (!outdir.isDirectory()) {
                    outdir.mkdir();
                }
            }
        }
        final File props = File.createTempFile("WebInstall", null);
        final String[] ARGS = new String [] {null, "-text", props.getCanonicalPath()};
        for (int i = 0; i < packages.length; i++) {
            String name = packages[i++] + version + packages[i];
            File outDir = new File(outdir, name);
            FileWriter fw = null;
            try {
                if (!outDir.isDirectory()) {
                    outDir.mkdir();
                }
                fw = new FileWriter(props);
                fw.write("output.dir=" + outputDir + name + "\n");
                fw.write("context.javaPath=" + jdk + "\n");
                fw.close(); 
                fw = null;
                ARGS[0] = baseurl + name + ".jar";
                main(ARGS);
            } finally {
                try { if (null != fw) fw.close(); } 
                catch (java.io.IOException e) {} // ignore
            }
        }
        if (props.exists()) props.delete();
    } // driver

    private static boolean printError(String err) {
        if (null != err) System.err.println(err);
        System.err.println(SYNTAX);
        return (null != err);
    }

    /**
     * Create a classloader using the first argument (presumed to be URL for classloader),
     * construct the installer, and invoke it using remaining arguments (or default args).
     */
    protected void install(String[] args) throws Exception {
        if ((null == args) || (args.length < 1) 
            || (null == args[0]) || (1 > args[0].length())) {
            if (printError("expecting installer URL")) return;
        }
        URL[] urls = new URL[] { new URL(args[0]) };
        //System.err.println("before: " + render(args));
        args = getArgs(args);
        //System.err.println("after: " + render(args));
        URLClassLoader cl = new URLClassLoader(urls);
        Class c = cl.loadClass("$installer$.org.aspectj.Main"); // todo: dependency on class name
        Method ms = c.getMethod("main", new Class[]{String[].class});
        ms.invoke(null, new Object[] { args });
    }
    public static final String render(String[] args) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < args.length; i++) {
            if (0 < i) sb.append(", ");
            sb.append("" + args[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /** @return args less args[0] or default args if less than 3 arguments */

    protected String[] getArgs(String[] args) {
        if ((null == args) || (args.length < 3)) {
            return ARGS;
        } else {
            String[] result = new String[args.length-1];
            System.arraycopy(args, 1, result, 0, result.length);
            return result;
        }
    }

}
