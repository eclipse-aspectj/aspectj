/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.bridge;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.aspectj.util.LangUtil;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 */
public class VersionTest extends TestCase {

	private static final String ME = "org.aspectj.bridge.VersionTest";

	/** @param args ignored */
	public static void main(String[] args) {
		TestRunner.main(new String[] { ME });
	}

	/**
	 * Constructor for MessageTest.
	 * 
	 * @param name
	 */
	public VersionTest(String name) {
		super(name);
	}

	public void testVersion() {
		if (LangUtil.is11VMOrGreater()) {
			return;
		}
		if (Version.getTimeText().equals("")) {
			return; // dev build, we can only test this on the build server.
		}
		Date date = new Date(Version.getTime());
		SimpleDateFormat format = new SimpleDateFormat(Version.SIMPLE_DATE_FORMAT, Locale.getDefault());
		format.setTimeZone(TimeZone.getTimeZone("PST"));
		String timeString = format.format(date);
		assertEquals(Version.getTimeText(), timeString);
	}
}
