import org.aspectj.testing.Tester;

import java.lang.reflect.Modifier;

public class InterfaceAndInner {
    public static void main(String[] args) {
        Object o = new InterfaceAndInnerHelper.C();
        Tester.check(o instanceof InterfaceAndInnerHelper.C,
                    "! o instanceof InterfaceAndInnerHelper.C" );
        Class c = o.getClass();
        Tester.check(Modifier.isStatic(c.getModifiers()),
                    "! Modifier.isStatic(c.getModifiers())" );
        Tester.check(Modifier.isPublic(c.getModifiers()),
                    "! Modifier.isPublic(c.getModifiers())" );
        
    }
}
