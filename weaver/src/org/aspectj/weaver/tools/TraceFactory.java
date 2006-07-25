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

public class TraceFactory {
    
    public static TraceFactory instance = new TraceFactory(); 
    
    public Trace getTrace (Class clazz) {
    	return new DefaultTrace(clazz);
    }
    
    public static TraceFactory getTraceFactory () {
    	return instance;
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
//    		th.printStackTrace();
    	}
//    	System.out.println("TraceFactory.<clinit>() instance=" + instance);
    }

}
