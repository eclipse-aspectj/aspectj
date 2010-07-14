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

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * 
 */
public class ReflectionFactory { // XXX lease, pool
	public static final String OLD_AJC = "bridge.tools.impl.OldAjc";
	public static final String ECLIPSE = "org.aspectj.ajdt.ajc.AjdtCommand";

	private static final Object[] NONE = new Object[0];

	/**
	 * Produce a compiler as an ICommand.
	 * 
	 * @param cname the fully-qualified class name of the command to create by reflection (assuming a public no-argument
	 *        constructor).
	 * @return ICommand compiler or null
	 */
	public static ICommand makeCommand(String cname, IMessageHandler errorSink) {
		return (ICommand) make(ICommand.class, cname, NONE, errorSink);
	}

	/**
	 * Make an object of type c by reflectively loading the class cname and creating an instance using args (if any), signalling
	 * errors (if any) to any errorSink.
	 */
	private static Object make(Class<?> c, String cname, Object[] args, IMessageHandler errorSink) {
		final boolean makeErrors = (null != errorSink);
		Object result = null;
		try {
			final Class<?> cfn = Class.forName(cname);
			String error = null;
			if (args == NONE) {
				result = cfn.newInstance();
			} else {
				Class<?>[] types = getTypes(args);
				Constructor<?> constructor = cfn.getConstructor(types);
				if (null != constructor) {
					result = constructor.newInstance(args);
				} else {
					if (makeErrors) {
						error = "no constructor for " + c + " using " + Arrays.asList(types);
					}
				}
			}
			if (null != result) {
				if (!c.isAssignableFrom(result.getClass())) {
					if (makeErrors) {
						error = "expecting type " + c + " got " + result.getClass();
					}
					result = null;
				}
			}
			if (null != error) {
				IMessage mssg = new Message(error, IMessage.FAIL, null, null);
				errorSink.handleMessage(mssg);
			}
		} catch (Throwable t) {
			if (makeErrors) {
				String mssg = "ReflectionFactory unable to load " + cname + " as " + c.getName();
				IMessage m = new Message(mssg, IMessage.FAIL, t, null);
				errorSink.handleMessage(m);
			}
		}
		return result;
	}

	/**
	 * @return Class[] with types of args or matching null elements
	 */
	private static Class<?>[] getTypes(Object[] args) {
		if ((null == args) || (0 < args.length)) {
			return new Class[0];
		} else {
			Class<?>[] result = new Class[args.length];
			for (int i = 0; i < result.length; i++) {
				if (null != args[i]) {
					result[i] = args[i].getClass();
				}
			}
			return result;
		}
	}

	private ReflectionFactory() {
	}
}
