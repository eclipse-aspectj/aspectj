package test;

import org.aspectj.testing.Tester;
import java.util.*;


public class TypePat {
    public static void main(String[] args) {
        Inner o = new Inner();
        o.m();
        
        Tester.checkAndClearEvents(new String[] {
            "A.before1: TypePat.Inner.m()",
                "InnerA.before: TypePat.Inner.m()",
                "A.before2: C.foo()",
                "TypePat.Inner.m",
                });


        Map m = new HashMap();
        m.put("a", "b");

        for (Iterator i = m.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            e.getKey();
        }

        Tester.checkAndClearEvents(new String[] {
            "A.before3: Map.Entry.getKey()"
                });



        Runnable r = new Runnable() {
                public void run() {
                    C.foo();
                    Tester.event("TypePat.Runnable.run");
                }
            };

        r.run();
        Tester.checkAndClearEvents(new String[] {
            "A.before2: C.foo()",
                "TypePat.Runnable.run",
                });
            //Tester.printEvents();
    }
    static class Inner {
        public void m() {
            C.foo();
            Tester.event("TypePat.Inner.m");
        }
    }

    static aspect InnerA {
        before(): call(* Inner.*(..)) {
            Tester.event("InnerA.before: " + thisJoinPoint.getSignature().toShortString());
        }
    }
}

class C {
    static void foo() {
    }
}


aspect A {
    before(): call(* TypePat.*.*(..)) && within(TypePat) && !within(TypePat.*) {
        Tester.event("A.before1: " + thisJoinPoint.getSignature().toShortString());
    }

    pointcut checkCall(): call(* *(..)) && !call(* Tester.*(..));

    before(): checkCall() && within(TypePat.*) && !within(*.InnerA) {
        Tester.event("A.before2: " + thisJoinPoint.getSignature().toShortString());
    }

    before(): checkCall() && target(Map.Entry) {
        Tester.event("A.before3: " + thisJoinPoint.getSignature().toShortString());
    }
}
