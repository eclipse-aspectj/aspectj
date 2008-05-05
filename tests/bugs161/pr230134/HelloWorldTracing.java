/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Webster           initial implementation      
 *******************************************************************************/

package demo.hello.tracing;

import org.aspectj.lib.tracing.*;

public aspect HelloWorldTracing extends SimpleTracing {

	protected pointcut tracingScope () :
		within(hello.*);

	/**
	 * Template method that allows choice of destination for output
	 * 
	 * @param s message to be traced
	 */
	protected void println (String s) {
		System.out.println(s);
	}
	
}

