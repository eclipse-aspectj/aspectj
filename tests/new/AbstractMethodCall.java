

abstract class AbstractClass {
  public abstract void abstractMethod ();
}

public class AbstractMethodCall extends AbstractClass {
    /** @testcase PR591 PUREJAVA compiler error expected when directly calling unimplemented abstract method using super */
    public void abstractMethodCall () {
        super.abstractMethod (); // expecting compiler error: cannot access directly
    }
    public void abstractMethod() {}
    public static void main(String[] args) {
        new AbstractMethodCall().abstractMethodCall();
    }
}


