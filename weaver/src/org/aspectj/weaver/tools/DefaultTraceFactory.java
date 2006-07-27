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

public class DefaultTraceFactory extends TraceFactory {

	public final static String ENABLED_PROPERTY = "org.aspectj.tracing.enabled";

    private boolean tracingEnabled = getBoolean(ENABLED_PROPERTY,false);

    public boolean isEnabled() {
		return tracingEnabled;
	}
    
    public Trace getTrace (Class clazz) {
    	DefaultTrace trace = new DefaultTrace(clazz);
    	trace.setTraceEnabled(tracingEnabled);
    	return trace;
    }

}
