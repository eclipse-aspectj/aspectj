/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;
import java.io.*;

import junit.framework.TestCase;

public class UtilityTestCase extends TestCase {

    //ALEX: don't know why / who but failures stacktrace are hidden if not overrided there
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
            System.err.println("FAIL " + getName());
            t.printStackTrace();
            throw  t;
        }
    }

    public UtilityTestCase(String name) {
        super(name);
    }

    public void disassembleTest(String name) throws IOException {
        BcelWorld world = new BcelWorld("../weaver/bin");
        world.addPath(WeaveTestCase.classDir);

        LazyClassGen clazz = new LazyClassGen(BcelWorld.getBcelObjectType(world.resolve(name)));
        clazz.print();
        System.out.println();
    }


    public void testHelloWorld() throws IOException {
        disassembleTest("Test");
    }
    public void testFancyHelloWorld() throws IOException {
        disassembleTest("FancyHelloWorld");
    }
//    public void testSwitchy() throws IOException {
//        disassembleTest("TestSwitchy");
//    }
    
    public static void main(String[] args) throws IOException {
    	BcelWorld world = new BcelWorld();
        LazyClassGen clazz = new LazyClassGen(BcelWorld.getBcelObjectType(world.resolve(args[0])));
        clazz.print();
    } 
}

