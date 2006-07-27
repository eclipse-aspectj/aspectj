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

import org.aspectj.util.LangUtil;

public abstract class TraceFactory {
    
	public final static String DEBUG_PROPERTY = "org.aspectj.tracing.debug";
	public final static String FACTORY_PROPERTY = "org.aspectj.tracing.factory";
	
    private static boolean debug = getBoolean(DEBUG_PROPERTY,false); 
    private static TraceFactory instance = new DefaultTraceFactory();
    
    public Trace getTrace (Class clazz) {
    	return instance.getTrace(clazz);
    }

	public boolean isEnabled() {
		return true;
	}
    
    public static TraceFactory getTraceFactory () {
    	return instance;
    }
    
    protected static boolean getBoolean(String name, boolean def) {
		String defaultValue = String.valueOf(def);
		String value = System.getProperty(name,defaultValue);
		return Boolean.valueOf(value).booleanValue();
	}

	static {
    	try {
			if (LangUtil.is15VMOrGreater()) {
	    		Class factoryClass = Class.forName("org.aspectj.weaver.tools.Jdk14TraceFactory");
	    		instance = (TraceFactory)factoryClass.newInstance();
			} else {
	    		Class factoryClass = Class.forName("org.aspectj.weaver.tools.CommonsTraceFactory");
	    		instance = (TraceFactory)factoryClass.newInstance();
			}
    	}
    	catch (Throwable th) {
    		if (debug) th.printStackTrace();
    	}
    	
    	if (debug) System.out.println("TraceFactory.instance=" + instance);
    }

}
