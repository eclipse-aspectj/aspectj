package org.aspectj.weaver.test;

/**
 * @author hilsdale
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class TestSwitchy {


    public int i = 3;
    
    public static final int j = 4;
    
    
    public static void main(String[] args) {
        switch (args.length) {
            case 0: System.err.println("hi");
            case 1: System.err.println("bye"); break;
            case 2: System.err.println("two");
            default: System.err.println("ning");
        }
        System.err.println("done");
    }
    
    
    abstract int goo();
    
    void nimbo() {}   
}
