/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
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

 
package org.aspectj.internal.tools.build;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/** 
 * Open struct for specifying builds for both modules and products.
 * Separated from bulder to permit this to build many modules
 * concurrently.
 * Static state has much of the Ant build properties (move?)
 */
public class BuildSpec {
    public static final String baseDir_NAME = "aspectj.modules.dir";
    public static final String stagingDir_NAME = "aj.staging.dir";
    public static final String jarDir_NAME = "aj.jar.dir";
    public static final String tempDir_NAME = "aj.temp.dir";
    public static final String distDir_NAME = "aj.dist.dir";
    
    /** name of a system property for reading the build version */
    public static final String SYSTEM_BUILD_VERSION_KEY = "aspectj.build.version";

    /** value of the build.version if build version not defined */
    public static final String BUILD_VERSION_DEFAULT = "DEVELOPMENT";

    /** name of a filter property for the normal build version */
    public static final String BUILD_VERSION_NAME = "build.version";

    /** name of a filter property for the company */
    public static final String COMPANY_NAME = "company.name";

    /** default value of of a filter property for the company */
    public static final String COMPANY_NAME_DEFAULT = "aspectj.org";

    /** name of a filter property for the base build version (no alpha, etc.) */
    public static final String BUILD_VERSION_BASE_NAME = "build.version.base";

    /** copyright property name */
    public static final String COPYRIGHT_NAME = "copyright.allRights.from1998";

    /** overall copyright */
    public static final String COPYRIGHT =
       "Copyright (c) 1998-2001 Xerox Corporation, "
       + "2002 Palo Alto Research Center, Incorporated. All rights reserved.";

    /** name of a filter property for the long build version (alpha) */
    public static final String BUILD_VERSION_LONG_NAME = "build.version.long";

    /** name of a filter property for the short build version (alpha -> a, etc.) */
    public static final String BUILD_VERSION_SHORT_NAME = "build.version.short";

    /** name of a filter property for the build time */
    public static final String BUILD_TIME_NAME = "build.time";

    /** name of a filter property for the build date */
    public static final String BUILD_DATE_NAME = "build.date";
    
    /** lazily and manually generate properties */
    public static Properties getFilterProperties(long time, String longVersion) {
        if (time < 1) {
            time = System.currentTimeMillis();
        }  
        if ((null == longVersion) || (0 == longVersion.length())) {
            longVersion = System.getProperty(
                        BuildSpec.SYSTEM_BUILD_VERSION_KEY, 
                        BuildSpec.BUILD_VERSION_DEFAULT);
        }
        Properties filterProps = new Properties();

        // build time and date  XXX set in build script?
        String timeString = time+"L";
        // XXX wrong date format - use Version.java template format?
        String date = new SimpleDateFormat("MMMM d, yyyy").format(new Date());
        filterProps.setProperty(BUILD_TIME_NAME, timeString);
        filterProps.setProperty(BUILD_DATE_NAME, date);

        // build version, short build version, and base build version 
        // 1.1alpha1,     1.1a1,               and 1.1
        String key = BuildSpec.BUILD_VERSION_NAME;
        String value = longVersion;
        value = value.trim();
        filterProps.setProperty(key, value);
        
        key = BuildSpec.BUILD_VERSION_LONG_NAME;
        filterProps.setProperty(key, value);

        if (!BuildSpec.BUILD_VERSION_DEFAULT.equals(value)) {
            value = Util.shortVersion(value);
        }
        key = BuildSpec.BUILD_VERSION_SHORT_NAME;
        filterProps.setProperty(key, value);

        key = BuildSpec.BUILD_VERSION_BASE_NAME;
        if (!BuildSpec.BUILD_VERSION_DEFAULT.equals(value)) {
            int MAX = value.length();
            for (int i = 0; i < MAX; i++) {
                char c = value.charAt(i);
                if ((c != '.') && ((c < '0') || (c > '9'))) {
                    value = value.substring(0,i);
                    break;
                }
            }
        }
        filterProps.setProperty(key, value);

        // company name, copyright XXX fix company name
        key = BuildSpec.COMPANY_NAME;
        value = System.getProperty(key, BuildSpec.COMPANY_NAME_DEFAULT);
        filterProps.setProperty(key, value);
        filterProps.setProperty(BuildSpec.COPYRIGHT_NAME, BuildSpec.COPYRIGHT);
        
        return filterProps;
    }
    
    // shared
    public File baseDir;
    public File moduleDir;
    public File jarDir;
    public File tempDir;
    public File stagingDir;
    public String buildConfig;   
    public String version;
    public boolean rebuild;
    public boolean trimTesting;
    public boolean assembleAll;
    public boolean failonerror;
    public boolean verbose;

    // building products
    public File productDir;
    public boolean createInstaller;
    public File distDir;
    
    // building modules
    public String module;
    
    public String toString() { // XXX better
        if (null != productDir) {
            return "product " + productDir.getName();
        } else {
            return "module " + moduleDir.getName();
        }
    }
}

