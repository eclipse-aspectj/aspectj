import pack.SuperClass;
import org.aspectj.testing.Tester; 

/** @testcase PR#585 PUREJAVA subclass unable to access protected static methods using type-qualified references */
public class SubClass extends SuperClass {
    private static int i;
    static {
        while (i<6) { 
            Tester.expectEvent(label() + SuperClass.SUPERCLASS);
        }
        i = 0;
    }
    static void register(Object o) {
        Tester.event(""+o);
    }

    public static String label() { return "label() " + i++; }
    public static void main(String[] args) {
        Object o = protectedStaticObject;
        register(""+protectedStatic(label() + o));
        register(""+SuperClass.protectedStatic(label() + o));
        register(""+pack.SuperClass.protectedStatic(label() + o));
        new SubClass().run();
        Tester.checkAllEvents();
    }
    public void run() {
        Object o = protectedObject;
        register(label() + protectedObject);
        register(""+protectedMethod(label()+o));
        register(""+this.protectedMethod(label()+o));
    }
}
