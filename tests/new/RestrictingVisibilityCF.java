import org.aspectj.testing.Tester;

/** @testcase PR#536 expecting compile failures with subclass narrowing scope of superclass methods or accessing private superclass variables */
public class RestrictingVisibilityCF {
    public static void main(String[] args) {
        Tester.check(false, "compile should fail");
    }
}

class Parent {
    public int publicAccess;
    protected int protectedAccess;
    int defaultAccess;
    private int privateAccess;

    public void publicAccess() {}
    protected void protectedAccess() {}
    void defaultAccess() {}
    private void privateAccess() {}
    void drivePrivateAccess() {
        privateAccess();
    }
}

class InValidChild extends Parent {
    /** @testcase subclass private implementation of public method */
    private void publicAccess() { }           // errLine 27
    /** @testcase subclass private implementation of method with default access */
    private void defaultAccess() { }          // errLine 29
    /** @testcase subclass private implementation of protected method */
    private void protectedAccess() { }        // errLine 31

    // todo: sep package, attempt package acces
    int defaultAccessSub = defaultAccess;
}

class InValidChild2 extends Parent {
    /** @testcase subclass private implementation of method with default access */
    private void defaultAccess() { }        // errLine 39 
    /** @testcase subclass protected implementation of public method */
    protected void publicAccess() { }         // errLine 41 
}

class InValidChild3 extends Parent {
    /** @testcase subclass default implementation of method with protected access */
    void protectedAccess() { }        // errLine 46 
    /** @testcase subclass default implementation of public method */
    void publicAccess() { }                   // errLine 48 
}

class InValidChild4 extends Parent {
    /** @testcase private access members unavailable in subclass */
    private int foo = new Parent().privateAccess; // errLine 53
}

//  /** todo: separate package test */
//  class Invalid {
//      /** @testcase default access members unavailable from separate package */
//      private int bar = new Parent().defaultAccess;
//      /** @testcase protected access members unavailable from separate package */
//      private int foo = new Parent().protectedAccess;
//  }
