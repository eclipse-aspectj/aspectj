/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC)
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     removed unused globals.
 * ******************************************************************/
 
package org.aspectj.testing.harness.bridge;

import java.io.File;

import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 */
public class Globals {
    /** name/key of the System property to set to define library dir */
    public static final String LIBDIR_NAME = "harness.libdir";

    /** name/key of the System property to set to define J2SE_HOME dir */
    public static final String J2SE14_RTJAR_NAME = "j2se14.rtjar";

    /** assumed relative location of a library with required jars */
    public static final String LIBDIR = getSystemProperty(LIBDIR_NAME, "../lib/test");

    /** Path to J2SE_HOME */
    public static final File J2SE14_RTJAR; // XXX used only by 1.0 compiler tests - deprecate?

    /** array of parameter types for main(String[]) */
    public static final Class[] MAIN_PARM_TYPES = new Class[] {String[].class};    
    public static final String S_testingclient_jar  = LIBDIR + "/testing-client.jar";
    public static final String S_aspectjrt_jar      = LIBDIR + "/aspectjrt.jar";
    public static final File F_testingclient_jar    = new File(S_testingclient_jar);
    public static final File F_aspectjrt_jar        = new File(S_aspectjrt_jar);
    public static final boolean globalsValid;
    
    static {
        File j2seJar = null;
        String path = getSystemProperty(J2SE14_RTJAR_NAME, "c:/home/apps/jdk14");
        File j2seHome = new File(path);
        if (j2seHome.exists() && j2seHome.isDirectory()) {
            File rtjar = new File(j2seHome.getAbsolutePath() + "/jre/lib/rt.jar");
            if (rtjar.canRead() && rtjar.isFile()) {
                j2seJar = rtjar;
            }
        }
        J2SE14_RTJAR = j2seJar;
    	globalsValid = 
    		(FileUtil.canReadFile(F_testingclient_jar)
    		  && FileUtil.canReadFile(F_aspectjrt_jar)
              && FileUtil.canReadFile(J2SE14_RTJAR)
            );
    }

	/**
	 * 
	 * @return null if not found, or 
     *          String with class path for compiler to load J2SE 1.4 classes from.
	 */
	public static String get14Bootclasspath() {
        return null;
	}

	/**
	 * Get System property completely safely.
	 * @param propertyName the String name of the property to get
	 * @param defaultValue the String default value to return value is null or empty
	 * @return String value or defaultValue if not available.
	 */
	private static String getSystemProperty(
		String propertyName,
		String defaultValue) {
        String result = defaultValue;
        try {
            String value = System.getProperty(propertyName);
            if (!LangUtil.isEmpty(value)) {
                result = value;
            }
        } catch (Throwable t) {}
        return result;      
	}

}
