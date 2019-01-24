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
	public final static String DEFAULT_FACTORY_NAME = "default";
	
    protected static boolean debug = getBoolean(DEBUG_PROPERTY,false); 
    private static TraceFactory instance;
    
    public Trace getTrace (Class clazz) {
    	return instance.getTrace(clazz);
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
		
		/*
		 * Allow user to override default behaviour or specify their own factory 
		 */
		String factoryName = System.getProperty(FACTORY_PROPERTY);
		if (factoryName != null) try {
			if (factoryName.equals(DEFAULT_FACTORY_NAME)) {
				instance = new DefaultTraceFactory();
			}
			else {
	    		Class factoryClass = Class.forName(factoryName);
	    		instance = (TraceFactory)factoryClass.newInstance();
			}
		}
    	catch (Throwable th) {
    		if (debug) th.printStackTrace();
    	}
    	
		/*
		 * Try to load external trace infrastructure using supplied factories
		 */
    	if (instance == null) try {
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

    	/*
		 * Use default trace 
		 */
    	if (instance == null) {
    	    instance = new DefaultTraceFactory();
    	}
    	
    	if (debug) System.err.println("TraceFactory.instance=" + instance);
    }

}
