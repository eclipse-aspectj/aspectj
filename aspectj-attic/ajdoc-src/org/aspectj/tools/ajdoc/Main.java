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

/**
 * Entry point for ajdoc.
 *
 * @author Jeff Palm
 */
public class Main {
    /** 
     * value returned from execute(..) 
     * when the JDK tools are not supported 
     */
    public static final int PLATFORM_ERROR = 3;

    /**
     * Call {@link #execute} and exit with
     * its exit code.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        System.exit(execute(args));
    }

    /**
     * Programmatic entry without calling <code>System.exit</code>,
     * returning the result of {@link Ajdoc#execute(String[])}.
     *
     * @param args Command line arguments.
     * @return PLATFORM_ERROR if platformErrorMessage() is not null
     *         or the result of calling {@link Ajdoc#execute(String[])}.
     * @throw Error if bad platform - look at message
     */
    public static int execute(String[] args) {
        int result = 0;
        String platformError = platformErrorMessage();
        if (null != platformError) {
            result = PLATFORM_ERROR;
            System.err.println(platformError);
        } else {
            Ajdoc me  = new Ajdoc();
            result = me.execute(args);
        }
        return result;
    }

    /**
     * Generate version error message if we cannot run in this VM
     * or using this class path.
     * @return null if no error or String describing error otherwise
     */
    public static String platformErrorMessage() {
        // todo: stolen from ajc.Main
        boolean failed = false;
        final String[] versions = new String[] 
        { "java.lang.reflect.Proxy"   // 1.3: failed if class not found
          // permit users to run in 1.4 iff using 1.3 tools.jar
          //, "java.lang.CharSequence"  // 1.4: failed if class found
        };
        for (int i = 0; i < versions.length; i++) {
            try {
                Class.forName(versions[i]);
                failed = (i == 1);
            } catch (ClassNotFoundException cnfe) {
                failed = (i == 0);
            } catch (Error err) {
                failed = (i == 0);
            }
            if (failed) {
                String version = "(unknown version)";
                try { version = System.getProperty("java.version"); }
                catch (Throwable t) { } // ignore
                return "Ajdoc requires J2SE 1.3; not java " + version;
            }
        }
        // now looking for tools.jar
        try {
            Class.forName("com.sun.javadoc.RootDoc"); // may be version error
            Class.forName("com.sun.javadoc.Type"); // not in 1.4 
        } catch (ClassNotFoundException cnfe) {
            // System.err.println(cnfe.getMessage());
            // cnfe.printStackTrace(System.err); // XXX
            return "Requires tools.jar from J2SE 1.3 (not 1.2 or 1.4) be on the class path";
        } catch (Error err) { // probably wrong version of the class
            // System.err.println(err.getMessage());
            // err.printStackTrace(System.err); // XXX
            return "Requires tools.jar from J2SE 1.3 (not 1.2 or 1.4) be on the class path";
        }
        return null;
    }
}
