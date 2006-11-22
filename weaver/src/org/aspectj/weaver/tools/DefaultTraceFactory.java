/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class DefaultTraceFactory extends TraceFactory {

	public final static String ENABLED_PROPERTY = "org.aspectj.tracing.enabled";
	public final static String FILE_PROPERTY = "org.aspectj.tracing.file";

    private boolean tracingEnabled = getBoolean(ENABLED_PROPERTY,false);
    private PrintStream print;

    public DefaultTraceFactory() {
		String filename = System.getProperty(FILE_PROPERTY);
		if (filename != null) {
			File file = new File(filename);
			try {
				print = new PrintStream(new FileOutputStream(file));
			}
			catch (IOException ex) {
	    		if (debug) ex.printStackTrace();
			}
		}
	}
    
    public boolean isEnabled() {
		return tracingEnabled;
	}
    
    public Trace getTrace (Class clazz) {
    	DefaultTrace trace = new DefaultTrace(clazz);
    	trace.setTraceEnabled(tracingEnabled);
    	if (print != null) trace.setPrintStream(print);
    	return trace;
    }

}
