import org.aspectj.testing.Tester;

import java.lang.reflect.*;

public class IntroducedModifiers {
    public static void main(String[] args) throws Exception {
        Field f = C.class.getField("cf");
        Tester.check(Modifier.isVolatile(f.getModifiers()), "volatile");
        Tester.check(Modifier.isTransient(f.getModifiers()), "transient");

        Method m = C.class.getMethod("m", new Class[0]);
        Tester.check(Modifier.isSynchronized(m.getModifiers()), "synchronized");
        Tester.check(Modifier.isStrict(m.getModifiers()), "strictfp");

        f = C.class.getField("scf");
        Tester.check(Modifier.isVolatile(f.getModifiers()), "volatile");
        Tester.check(Modifier.isTransient(f.getModifiers()), "transient");
        Tester.check(Modifier.isStatic(f.getModifiers()), "static");

        //XXX this name depends on implementation details for field intro on interfaces
        try {
            f = C.class.getField("ajc$interField$A$I$iField");
        } catch (NoSuchFieldException e) {
            f = C.class.getField("iField");
        }
        Tester.check(Modifier.isVolatile(f.getModifiers()), "volatile");
        Tester.check(Modifier.isTransient(f.getModifiers()), "transient");

        m = C.class.getMethod("im", new Class[0]);
        Tester.check(Modifier.isSynchronized(m.getModifiers()), "synchronized");
        Tester.check(Modifier.isStrict(m.getModifiers()), "strictfp");
    }
}


interface I {
}

class C implements I {
}

aspect A {
    public transient volatile int C.cf = 0;
    public synchronized strictfp int C.m() { return 0; }

    public transient volatile static int C.scf = 0;

    public synchronized strictfp int I.im() { return 0; }
    public transient volatile int I.iField = 1;
}
