package test;
import java.lang.reflect.Constructor;
import org.aspectj.testing.Tester;

/**
 * @author Ron Bodkin
 * @author Jim Hugunin
 */
public class NoSoftener {
    public static void main(String[] args) {
        Throwable wrapped = null;
        try {
            new NoSoftener().foo(Integer.class);
        } catch (org.aspectj.lang.SoftException se) {
            wrapped = se.getWrappedThrowable();
        }
        Tester.checkNonNull(wrapped, "exception thrown");
        Tester.check(wrapped instanceof NoSuchMethodException,
                "must be NoSuchMethodException");
        
    }
    
    public void foo(Class clazz) {
        Class[] keyArgType = {};
        Constructor ctor = clazz.getConstructor(keyArgType);
    }
}