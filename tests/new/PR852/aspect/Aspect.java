package aspect;

import target.SuperClass;
import target.SubClass;

/** @testcase PR#852 declaring method on superclass and subclass */
public aspect Aspect {
    void SuperClass.test() { System.out.println("SuperClass ok");}
    void SubClass.test() { System.out.println("SubClass ok");}
    //public void SuperClass+.callTest() { test(); }
    public static void main (String[] args) {
        new SuperClass().test();
        new SubClass().test();
    }
}
