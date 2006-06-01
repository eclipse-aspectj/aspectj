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

import java.io.*;
import java.util.*;

/**
 * FIXME regen with an Eclipse 2.1 the testdata/bin with this new package
 * same for all classes in that package
 * and update tests then (pointcuts etc)
 *
 * @version 	1.0
 * @author
 */
public class DynamicHelloWorld implements Serializable {

    public static void main(String[] args) {
    	try {
			new DynamicHelloWorld().doit("hello", Collections.EMPTY_LIST);
		} catch (UnsupportedOperationException t) {
			System.out.println("expected and caught: " + t);
			return;
		}
		throw new RuntimeException("should have caught exception");
    }
    
    String doit(String s, List l) {
    	l.add(s);   // this will throw an exception
    	return l.toString();
    }
}
