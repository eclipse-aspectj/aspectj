
package pack;

public class SuperClass {
    public static final String SUPERCLASS = "SuperClass";
    private static String superClass() { return SUPERCLASS; }

    /** @testcase PR#585 subclass access to protected static field */
    protected static Object protectedStaticObject = superClass();
    /** @testcase PR#585 subclass access to protected static method */
    protected static Object protectedStatic(String s) { return s; }

    /** @testcase PR#585 subclass access to protected field */
    protected Object protectedObject = superClass();
    /** @testcase PR#585 subclass access to protected method */
    protected Object protectedMethod(String s) { return s; }

    public String toString() { return superClass(); }
}
