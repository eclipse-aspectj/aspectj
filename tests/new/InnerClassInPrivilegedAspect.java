import org.aspectj.testing.Tester; 

/** @testcase PR#555 inner classes of privileged aspects cannot see target class private members */
public class InnerClassInPrivilegedAspect {
    public static void main(String[] args) {
        Util.start();
        Target.main(args);
        new Target().toString();
        Util.end();
    }
}

class Util {
    public static void event(String s) {
        //System.out.println(s);
        Tester.event(s);
    }
    public static void start() {
        // failing
        Tester.expectEvent("before runner1");
        Tester.expectEvent("before intro1");
        Tester.expectEvent("new1");
        Tester.expectEvent("around runner1");
        Tester.expectEvent("around intro1");
        Tester.expectEvent("before instance1");
        Tester.expectEvent("around instance1");
        Tester.expectEvent("pcd if()1");
        Tester.expectEvent("before run1");
        Tester.expectEvent("around run1");

        // passing
        Tester.expectEvent("before pcd if()1");
        Tester.expectEvent("before1");
        Tester.expectEvent("around1");
    }
    public static void end() {
        Tester.checkAllEvents();
    }
}

class Target {
	private Target(String foo) {}
    private static int privateStaticInt = 1;
    private int privateInt = 1;
    public static void main(String args[]) { }
}

interface Test {
    public boolean t();
}
privileged aspect PrivilegedAspectBefore {
    pointcut p() : execution(static void Target.main(..));

    static class IfTest {
        public boolean t(){ Util.event("pcd if()"+Target.privateStaticInt); return true;}
    }

    pointcut q() : p() 
        && if(new IfTest().t());
                
   
    before() : q() { Util.event("before pcd if()" + Target.privateStaticInt); }

    /** @testcase privileged access to target private static variables in introduced member (of anonymous class type)*/
    static Runnable Target.runner = new Runnable() { 
            public void run() { Util.event("before runner"+Target.privateStaticInt); }
        };
    before() : p() { Target.runner.run(); }

    before() : p() {
        /** @testcase privileged access to target private static variables in before advice (ok) */
        Util.event("before" +Target.privateStaticInt);
    }
    before() : p() {
        /** @testcase privileged access to target private static variables from inner class inside before advice */
        Runnable inner = new Runnable() {
                public void run() {
                    Util.event("before run" +Target.privateStaticInt);
                }
            };
        inner.run();
    }

    /** @testcase privileged access to target private static variables from inner class inside introduced method */
    before() : p() { Target.runbefore(); }
    static void Target.runbefore() {
        Runnable inner = new Runnable() {
                public void run() {
                    Util.event("before intro" +Target.privateStaticInt);
                }
            };
        inner.run();
    }    
}
/** differs from PrivilegedAspectBefore only in using around advice */
privileged aspect PrivilegedAspectAround {
    pointcut p() : execution(static void Target.main(..));

    /** @testcase privileged access to target private static variables from inner class in  introduced constructor */
    Target.new() {
    	    this("hi");
            int i = privateStaticInt;
            Runnable p = new Runnable() { 
                    public void run() { Util.event("new"+Target.privateStaticInt); }
                };
            p.run();
        }
    /** @testcase privileged access to target private static variables in introduced member (of anonymous class type)*/
    static Runnable Target.runner2 = new Runnable() { 
            public void run() { Util.event("around runner"+Target.privateStaticInt); }
        };
    Object around() : p() { Target.runner2.run(); return proceed();}
    Object around() : p() {
        /** @testcase privileged access to target private static variables in before advice (ok) */
        Util.event("around" +Target.privateStaticInt);
        return proceed();
    }
    Object around() : p() {
        /** @testcase privileged access to target private static variables from inner class inside around advice */
        Runnable inner = new Runnable() {
                public void run() {
                    Util.event("around run" +Target.privateStaticInt);
                }
            };
        inner.run();
        return proceed();
    }

    Object around() : p() { Target.runaround(); return proceed(); }
    /** @testcase privileged access to target private static variables from inner class inside introduced method */
    static void Target.runaround() {
        Runnable inner = new Runnable() {
                public void run() {
                    Util.event("around intro" +Target.privateStaticInt);
                }
            };
        inner.run();
    }
} 

privileged aspect PrivilegedAspectAroundNonStatic {
    /** @testcase privileged access to target private variables from inner class inside introduced method */
    before(Target t) : call(String Object.toString()) && target(t){ t.runbefore2(); }
    void Target.runbefore2() {
        Runnable inner = new Runnable() {
                public void run() {
                    Util.event("before instance" +privateInt);
                }
            };
        inner.run();
    }   
    /** @testcase privileged access to target private variables from inner class inside introduced method */
    Object around(Target t) : call(String Object.toString()) && target(t){ t.runbefore3(); return proceed(t); }
    void Target.runbefore3() {
        Runnable inner = new Runnable() {
                public void run() {
                    Util.event("around instance" +privateInt);
                }
            };
        inner.run();
    }
}
