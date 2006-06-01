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

public class MultiArgHelloWorld {

    public static void main(String[] args) {
    	foo("Hello", "World");
    }
    
    static void foo(Object s, Object t) {
    	System.out.println(s + " " + t);
    }
}
