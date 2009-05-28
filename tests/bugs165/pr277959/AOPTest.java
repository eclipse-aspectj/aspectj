/*
 * AOPTest
 * Copyright (c) 2001-2006 MessageOne Inc.
 */
package example;

public class AOPTest {
    public static void doSomething() {}

    public static void cleanup() {
        try {
            doSomething();
            doSomething();
        } catch(Exception ex) {
            // ignore
        }
    }
    public static void cleanup2() {
        try {

            doSomething();
        } catch(Exception ex) {
            // ignore
        }
    }


    public static void main(String[] args) throws Throwable {
        AOPTest.cleanup();
    }
}

