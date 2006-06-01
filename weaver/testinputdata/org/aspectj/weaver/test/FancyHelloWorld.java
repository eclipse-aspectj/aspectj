/* Copyright (c) 2002 Contributors.
 * 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 */
package org.aspectj.weaver.test;

import java.io.PrintStream;

/**
 * @version 	1.0
 * @author
 */
public abstract class FancyHelloWorld {
    public static void main(String[] args) {
    	PrintStream out = System.out;
    	try {
    		out.println("bye");
    	} catch (Exception e) {
    		out.println(e);
    	} finally {
    		out.println("finally");
    	}
    }
    
    public static String getName() {
    	int x = 0;
    	x += "name".hashCode();
    	return "name" + x;
    }
}
