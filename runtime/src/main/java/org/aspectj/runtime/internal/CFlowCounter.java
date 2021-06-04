/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 *
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement     initial implementation
 * ******************************************************************/


package org.aspectj.runtime.internal;

import org.aspectj.runtime.internal.cflowstack.ThreadCounter;
import org.aspectj.runtime.internal.cflowstack.ThreadStackFactory;
import org.aspectj.runtime.internal.cflowstack.ThreadStackFactoryImpl;
import org.aspectj.runtime.internal.cflowstack.ThreadStackFactoryImpl11;


public class CFlowCounter {

	private static ThreadStackFactory tsFactory;
	private ThreadCounter flowHeightHandler;

	static {
		selectFactoryForVMVersion();
	}

	public CFlowCounter() {
		flowHeightHandler = tsFactory.getNewThreadCounter();
	}

    public void inc() {
    	flowHeightHandler.inc();
    }

    public void dec() {
    	flowHeightHandler.dec();
    	if (!flowHeightHandler.isNotZero()) {
    		flowHeightHandler.removeThreadCounter();
    	}
    }

    public boolean isValid() {
    	return flowHeightHandler.isNotZero();
    }


	private static ThreadStackFactory getThreadLocalStackFactory()      { return new ThreadStackFactoryImpl(); }
	private static ThreadStackFactory getThreadLocalStackFactoryFor11() { return new ThreadStackFactoryImpl11(); }

	private static void selectFactoryForVMVersion() {
		String override = getSystemPropertyWithoutSecurityException("aspectj.runtime.cflowstack.usethreadlocal","unspecified");
		boolean useThreadLocalImplementation = false;
		if (override.equals("unspecified")) {
			String v = System.getProperty("java.class.version","0.0");
			// Java 1.2 is version 46.0 and above
			useThreadLocalImplementation = (v.compareTo("46.0") >= 0);
		} else {
			useThreadLocalImplementation = override.equals("yes") || override.equals("true");
		}
		// System.err.println("Trying to use thread local implementation? "+useThreadLocalImplementation);
		if (useThreadLocalImplementation) {
			tsFactory = getThreadLocalStackFactory();
		} else {
			tsFactory = getThreadLocalStackFactoryFor11();
		}
	}


	private static String getSystemPropertyWithoutSecurityException (String aPropertyName, String aDefaultValue) {
		try {
			return System.getProperty(aPropertyName, aDefaultValue);
		}
		catch (SecurityException ex) {
			return aDefaultValue;
		}
	}

	//  For debug ...
	public static String getThreadStackFactoryClassName() {
		return tsFactory.getClass().getName();
	}

}
