/* ********************************************************************
 * Copyright (c) 1998-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Xerox/PARC     initial implementation
 * *******************************************************************/

package org.aspectj.bridge;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/** release-specific version information */
public class Version {
    
    /** default version value for development version */
    public static final String DEVELOPMENT = "DEVELOPMENT";

    /** default time value for development version */
    public static final long NOTIME = 0L;
    
    /** set by build script */
    public static final String text = DEVELOPMENT;
    
    /** 
      * Time text set by build script using SIMPLE_DATE_FORMAT.
      * (if DEVELOPMENT version, invalid)
      */
    public static final String time_text = "12/18/02 at 03:20:03 PST";

    /** 
      * time in seconds-since-... format, used by programmatic clients.
      * (if DEVELOPMENT version, NOTIME)
      */
    public static final long time;
    
	/** format used by build script to set time_text */
    public static final String SIMPLE_DATE_FORMAT = "MM/dd/yy 'at' hh:mm:ss z";
    
    // if not DEVELOPMENT version, read time text using format used to set time 
    static {
        long foundTime = NOTIME;
        if (!DEVELOPMENT.equals(text)) {
	        try {
	            SimpleDateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
	            ParsePosition pos = new ParsePosition(0);
	            Date date = format.parse(time_text, pos);
	            foundTime = date.getTime();
	        } catch (Throwable t) {            
	        }
        }
        time = foundTime;
    }
}
    




