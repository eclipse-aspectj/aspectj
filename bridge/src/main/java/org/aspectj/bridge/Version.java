/* ********************************************************************
 * Copyright (c) 1998-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Xerox/PARC     initial implementation
 * *******************************************************************/

package org.aspectj.bridge;

import java.io.IOException;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/** release-specific version information */
public class Version {
    
    // generated from build/lib/BridgeVersion.java

    /** default version value for development version */
    public static final String DEVELOPMENT = "DEVELOPMENT";
    // VersionUptodate.java depends on this value

    /** default time value for development version */
    public static final long NOTIME = 0L;
    
    public static final String UNREPLACED_TEXT = "${project.version}";
    public static final String UNREPLACED_TIME_TEXT = "${version.time_text}";
    
    /** set by build script */
    private static String text;// = "DEVELOPMENT";
    // VersionUptodate.java scans for "static final String text = "
    
    /** 
      * Time text set by build script using SIMPLE_DATE_FORMAT.
      * (if DEVELOPMENT version, invalid)
      */
    private static String time_text;// = "Tuesday Jan 15, 2019 at 00:53:54 GMT";
    
    /** 
      * time in seconds-since-... format, used by programmatic clients.
      * (if DEVELOPMENT version, NOTIME)
      */
    private static long time = -1; // -1 == uninitialized
    
	/** format used by build script to set time_text */
    public static final String SIMPLE_DATE_FORMAT = "EEEE MMM d, yyyy 'at' HH:mm:ss z";
    
    static {
    	try {
	    	URL resource = Version.class.getResource("version.properties");
	    	Properties p = new Properties();
			p.load(resource.openStream());
			text = p.getProperty("version.text","");
			if (text.equals(UNREPLACED_TEXT)) {
				text="DEVELOPMENT";
			}
			time_text = p.getProperty("version.time_text","");
			if (time_text.equals(UNREPLACED_TIME_TEXT)) {
				time_text="";
			}
		} catch (IOException e) {
			text="DEVELOPMENT";
			time_text = "";
		}    	
    }
    
    public static long getTime() {
    	if (time==-1) {
    		long foundTime = NOTIME;
    	    // if not DEVELOPMENT version, read time text using format used to set time 
            try {
                SimpleDateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
                ParsePosition pos = new ParsePosition(0);
                Date date = format.parse(time_text, pos);
                if (date!=null) foundTime = date.getTime();
            } catch (Throwable t) {            
            }
            time = foundTime;
    	}
    	return time;
    }

    /**
     * Test whether the version is as specified by any first argument.
     * Emit text to System.err on failure
     * @param args String[] with first argument equal to Version.text
     * @see Version#text
     */
    public static void main(String[] args) {
        if ((null != args) && (0 < args.length)) {
            if (!Version.text.equals(args[0])) {
                System.err.println("version expected: \"" 
                                + args[0] 
                                + "\" actual=\"" 
                                + Version.text 
                                + "\"");
            }
        }
    }

	public static String getTimeText() {
		return time_text;
	}

	public static String getText() {
		return text;
	}
}
    




