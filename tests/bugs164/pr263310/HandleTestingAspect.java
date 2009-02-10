package p;

import java.util.ArrayList;

public aspect HandleTestingAspect {

        static class InnerClass {
                int x;
                {  }

                static aspect InnerInnerAspect {
                        int x;
                        static { } // 13
                }

                public void doNothing() {}
        }

        before() : call(* *.doNothing()) {

        }

        before(int x, long y) : execution(* *.foo(int,long)) && args(x,y) {
                InnerClass u = new InnerClass() {
                        public void doNothing() { 
                                doNothing();
                        }
                };
                u.doNothing();
        }

        interface X { }


        public void doNothing() {}

        // testing ITDs
        int X.X = 6;
//    int X.itd() { return 1;}
//    X.new() { }
//    int X.itd(int x) { return 1;}
//    X.new(int x) { }


        declare parents : HandleTestingClass extends InnerClass;
        declare parents : HandleTestingClass implements X;
        declare soft : Exception : execution(void HandleTestingClass.foo1(int,long));
        declare error : call(void HandleTestingClass.foo1(int,long)) : "";
        declare warning : call(void HandleTestingClass.foo2(int,long)) : "";

        pointcut ypc(int y) : call(* *.yCall(int)) && args(y);
        pointcut zpc(int z) : call(* *.zCall(int)) && args(z);

        // should not have a count
        before(int y) : ypc(y) { }
        after(int y) : ypc(y) { }
        after(int y) throwing(Exception e) : ypc(y) { }
        after(int y) returning(int z) : ypc(y) { }
    int around(int y) : ypc(y) { return 1; }

        // should have a count
        before(int y) : zpc(y) { }
        after(int y) : zpc(y) { }
        after(int y) throwing(Exception e) : zpc(y) { }
        after(int y) returning(int z) : zpc(y) { }
    int around(int y) : ypc(y) { return 1; }

    // should have a count of 3
    Object around(int y) : ypc(y) { return null; }


    after() returning(java.util.List z) : call(* *.zCall(int)) { }
}

class HandleTestingClass {}