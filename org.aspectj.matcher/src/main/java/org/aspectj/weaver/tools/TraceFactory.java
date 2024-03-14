/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.tools;

public abstract class TraceFactory {

	public final static String DEBUG_PROPERTY = "org.aspectj.tracing.debug";
	public final static String FACTORY_PROPERTY = "org.aspectj.tracing.factory";
	public final static String DEFAULT_FACTORY_NAME = "default";

    protected static boolean debug = getBoolean(DEBUG_PROPERTY,false);
    private static final TraceFactory instance;

    public Trace getTrace(Class clazz) {
        return instance.getTrace(clazz);
    }

    public static TraceFactory getTraceFactory() {
        return instance;
    }

    protected static boolean getBoolean(String name, boolean def) {
		String defaultValue = String.valueOf(def);
		String value = System.getProperty(name,defaultValue);
		return Boolean.parseBoolean(value);
	}

    static {
        instance = createInstance();
        if (debug) System.err.println("TraceFactory.instance=" + instance);
    }

    private static TraceFactory createInstance() {
        /*
         * Allow user to override default behaviour or specify their own factory
         */
        String factoryName = System.getProperty(FACTORY_PROPERTY);
        if (factoryName != null) try {
            if (factoryName.equals(DEFAULT_FACTORY_NAME)) {
                return new DefaultTraceFactory();
            } else {
                Class<?> factoryClass = Class.forName(factoryName);
                return (TraceFactory)factoryClass.getDeclaredConstructor().newInstance();
            }
        } catch (Throwable th) {
            if (debug) th.printStackTrace();
        }

        /*
         * Try to load external trace infrastructure using supplied factories
         */
        try {
            Class<?> factoryClass = Class.forName("org.aspectj.weaver.tools.Jdk14TraceFactory");
            return (TraceFactory)factoryClass.getDeclaredConstructor().newInstance();
        } catch (Throwable th) {
            if (debug) th.printStackTrace();
        }

        /*
         * Use default trace
         */
        return new DefaultTraceFactory();
    }

}
