import org.aspectj.testing.Tester;

import java.lang.reflect.*;

public class Simple {
    public static void main(String[] args) {
	new Simple().fromInstanceMethod("a1", 1);
        //Tester.printEvents();
        Tester.checkAndClearEvents(new String[] {
            "this(simple)",
                "call(C.*)",
                "args(..,a1)",
                "C.toStaticMethod(a1)",

                "this(simple)",
                "call(SubC.*)",
                "args(..,a1)",
                "SubC.toStaticMethod(a1)",

                "this(simple)&&target(c)",
                "target(c)",
                "this(simple)",
                "call(C.*)",
                "args(..,a1)",
                "C.toInstanceMethod(a1)",

                "this(simple)&&target(subc)",
                "target(subc)",
                "this(simple)",
                "call(C.*)",
                "call(C.*)&&target(SubC)",
                "args(..,a1)",
                "SubC.toInstanceMethod(a1)",

                "this(simple)&&target(subc)",
                "target(subc)",
                "this(simple)",
                "call(C.*)",
                "call(SubC.*)",
                "call(SubC.*)&&target(C)",
                "call(C.*)&&target(SubC)",
                "args(..,a1)",
                "SubC.toInstanceMethod(a1)",

				//??? these events are not expected because advice on call join points
				// isn't run when the calls are made reflectively -- Change from 1.0 to 1.1
                //"call(C.*)",
                //"call(C.*)&&target(SubC)",
                //"args(..,a1)",
                "SubC.toInstanceMethod(a1)",
                });
	Simple.fromStaticMethod("a2");
        //Tester.printEvents();
        Tester.checkAndClearEvents(new String[] {        
            "call(C.*)",
                "args(..,a2)",
                "C.toStaticMethod(a2)",

                "call(SubC.*)",
                "args(..,a2)",
                "SubC.toStaticMethod(a2)",

                "target(c)",
                "call(C.*)",
                "args(..,a2)",
                "C.toInstanceMethod(a2)",

                "target(subc)",
                "call(C.*)",
                "call(C.*)&&target(SubC)",
                "args(..,a2)",
                "SubC.toInstanceMethod(a2)",

                "target(subc)",
                "call(C.*)",
                "call(C.*)&&target(SubC)",
                "SubC.toInstanceMethod(2)",
                });
    }

    public static void fromStaticMethod(String s) {
	C.toStaticMethod(s);
	SubC.toStaticMethod(s);
	C c = new C();
	c.toInstanceMethod(0, s);
	c = new SubC();
	c.toInstanceMethod(0, s);
        c.toInstanceMethod(0, new Integer(2));
    }

    public void fromInstanceMethod(String s, int v) {
	C.toStaticMethod(s);
	SubC.toStaticMethod(s);
	C c = new C();
	c.toInstanceMethod(v, s);
	c = new SubC();
	c.toInstanceMethod(v, s);
        SubC subc = new SubC();
	subc.toInstanceMethod(v, s);
        doReflectiveCall(subc, v, s);
    }

    void doReflectiveCall(C c, int v, String s) {
        try {
            Method m =
                C.class.getMethod("toInstanceMethod", 
                                  new Class[] {Integer.TYPE, Object.class});
            m.invoke(c, new Object[] {new Integer(v), s});
        } catch (Exception e) {
            Tester.event("unexpected exception: " + e);
        }
    }

    public String toString() { return "simple"; }
}


class C {
    public static void toStaticMethod(Object s) {
        Tester.event("C.toStaticMethod("+s+")");
    }

    public void toInstanceMethod(int value, Object str) {
        Tester.event("C.toInstanceMethod("+str+")");
    }
    public String toString() { return "c"; }
}

class SubC extends C {
    public static void toStaticMethod(Object s) {
        Tester.event("SubC.toStaticMethod("+s+")");
    }

    public void toInstanceMethod(int x, Object str) {
        Tester.event("SubC.toInstanceMethod("+str+")");
    }
    public String toString() { return "subc"; }
}    

aspect A {
    before(C c, Simple s): this(s) && target(c) {
        Tester.event("this("+s+")&&target("+c+")");
    }

    before(C c): target(c) && within(Simple) {
        Tester.event("target("+c+")");
    }

    before(Simple s): this(s) && call(* C+.*(..))  {
        Tester.event("this("+s+")");
    }

    before(): call(* C.*(..)) {
        Tester.event("call(C.*)");
    }

    before(): call(* SubC.*(..)) {
        Tester.event("call(SubC.*)");
    }

    before(): call(* SubC.*(..)) && target(C) {
        Tester.event("call(SubC.*)&&target(C)");
    }

    before(): call(* C.*(..)) && target(SubC) {
        Tester.event("call(C.*)&&target(SubC)");
    }

    before(String s): args(.., s) && call(* C+.*(..)) {
        //System.out.println(thisJoinPoint);
        Tester.event("args(..,"+s+")");
    }
}
