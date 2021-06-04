/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Wes Isberg     initial implementation
 * ******************************************************************/

import org.aspectj.testing.Tester;

/**
 * @testcase PR#49784 aspect declares interface method (public abstract)
 */
public class InterfaceMethodDeclarationFull {

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
    abstract public int I.getInt();
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
