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

import junit.framework.TestCase;

public class Test extends TestCase {
    public static void main(String[] args) {
        foo()
        .
        foo();
    }
    public static Test foo() {
        new Exception().printStackTrace();
        return new Test();
    }
    public void testNothingForAntJUnit() {}
}
