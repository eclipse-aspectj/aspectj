/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

import org.aspectj.testing.Tester;

/**
 * @testcase PR#49784 aspect declares interface method (abstract)
 */
public class InterfaceMethodDeclarationAbstract {

    public static void main(String[] args) {
        Tester.expectEvent("before-execution");
        Tester.expectEvent("before-call");
        I i = new C();
        Tester.check(1 == i.getInt(), "1 == i.getInt()");
        Tester.checkAllEvents();
    }
}

interface I {}

aspect A {
    abstract int I.getInt();  // Error expected: Needs to be public
    before() : execution(int getInt()) && target(I) {
        Tester.event("before-execution");
    }
    before() : call(int getInt()) && target(I) {
        Tester.event("before-call");
    }
}
class C implements I {
    public int getInt() { return 1; }
}
