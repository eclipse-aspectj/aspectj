// thisResultObject for primitives
// I think this is a bad test.  Is there a way to do this?  -eh

import org.aspectj.testing.Tester;

public class Driver {
    public static void test() {
        C1 c1 = new C1();

        c1.getInteger();
        c1.getDouble();
        c1.getVoid();
        c1.getString();
        c1.getBoolean();
    }

    public static void main(String[] args) { test(); }
}

class C1 {
    int getInteger() {
        return 1;
    }

    double getDouble() {
        return 3.14;
    }

    void getVoid() {
    }

    String getString() {
        return "Hello World";
    }

    boolean getBoolean() {
        return true;
    }
}

aspect A1 {
    // don't advise the finalize reception, or weird interactions with GC can happen
     after() returning (Object result): 
            target(C1) && call(* *()) && !call(void finalize()) {
        if (result == null) {
            Tester.checkEqual(thisJoinPoint.getSignature().getName(), 
                               "getVoid", 
                               "void method");
        } 
        else {
            String resultClassName = result.getClass().getName();

            Tester.checkEqual("java.lang." + 
                                 thisJoinPoint.getSignature().getName().substring(3),
                               resultClassName, 
                               "result object type");
        }
    }
}

