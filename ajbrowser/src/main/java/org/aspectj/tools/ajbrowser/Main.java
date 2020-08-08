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

package org.aspectj.tools.ajbrowser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Run ajbrowser if 0+ .lst file arguments, and ajc otherwise.
 */
public class Main {

	/**
	 * Run ajbrowser if args contains only .lst files and ajc otherwise.
	 * 
	 * @param args the String[] of args to interpret
	 */
	public static void main(String[] args) {
		if (!compilerMain(args)) {
			BrowserManager.getDefault().init(args, true);
		}
	}

	/**
	 * Invoke the compiler if there are arguments and some are not .lst files.
	 * 
	 * @return false if compiler was not invoked and the browser main should be
	 */
	static boolean compilerMain(String[] args) {
		if ((null == args) || (0 == args.length)) {
			return false;
		}
		int numConfigFiles = 0;
		for (String arg : args) {
			if ((null != arg) && arg.endsWith(".lst")) {
				numConfigFiles++;
			}
		}
		if (numConfigFiles != args.length) {
			try {
				Class<?> ajc = Class.forName("org.aspectj.tools.ajc.Main");
				Method main = ajc.getMethod("main", new Class[] { String[].class });
				main.invoke(null, new Object[] { args });
				return true;
			} catch (ClassNotFoundException e) {
				report(e);
			} catch (NoSuchMethodException e) {
				report(e);
			} catch (IllegalAccessException e) {
				report(e);
			} catch (InvocationTargetException e) {
				report(e.getTargetException());
			}
		}
		return false;
	}

	private static void report(Throwable t) {
		t.printStackTrace(System.err);
	}
}
