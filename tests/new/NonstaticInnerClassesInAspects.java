import org.aspectj.testing.Tester; 
public class NonstaticInnerClassesInAspects {
    public static void main(String[] args) {
        new NonstaticInnerClassesInAspects().realMain(args);
    }
    public void realMain(String[] args) {
        new C().c();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("A$Inner-before-c");
        Tester.expectEvent("A$Inner$InnerInner-before-c");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-before-c");
        Tester.expectEvent("A$Inner-after-c");
        Tester.expectEvent("A$Inner$InnerInner-after-c");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-after-c");
        Tester.expectEvent("A$Inner-around-c");
        Tester.expectEvent("A$Inner$InnerInner-around-c");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-around-c");
        Tester.expectEvent("A$Inner-before-d");
        Tester.expectEvent("A$Inner$InnerInner-before-d");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-before-d");
        Tester.expectEvent("A$Inner-after-d");
        Tester.expectEvent("A$Inner$InnerInner-after-d");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-after-d");
        Tester.expectEvent("A$Inner-around-d");
        Tester.expectEvent("A$Inner$InnerInner-around-d");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-around-d");
        Tester.expectEvent("A$Inner-before-x");
        Tester.expectEvent("A$Inner$InnerInner-before-x");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-before-x");
        Tester.expectEvent("A$Inner-after-x");
        Tester.expectEvent("A$Inner$InnerInner-after-x");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-after-x");
        Tester.expectEvent("A$Inner-around-x");
        Tester.expectEvent("A$Inner$InnerInner-around-x");
        Tester.expectEvent("A$Inner$InnerInner$InnerInnerInner-around-x");
    }
}

class C {
    public void c() { d(); }
    public void d() {      } 
}

aspect A {

    pointcut c(): target(C) && call(void c());
    pointcut d(): call(void C.d());
    pointcut x(): target(C) && execution(void c());

    after()      : c() { new Inner().i("after-c");  }
    after()      : d() { new Inner().i("after-d");  }
    after()      : x() { new Inner().i("after-x");  }

    before()     : c() { new Inner().i("before-c"); }
    before()     : d() { new Inner().i("before-d"); }
    before()     : x() { new Inner().i("before-x"); }

    void around(): c() { new Inner().i("around-c"); proceed(); }
    void around(): d() { new Inner().i("around-d"); proceed(); }
    void around(): x() { new Inner().i("around-x"); proceed(); }

    class Inner {
        void i(String s) {
            a(s,this);
            new InnerInner().i(s);
        }
        class InnerInner {
            void i(String s) {
                a(s,this);
                new InnerInnerInner().i(s);
            }
            class InnerInnerInner {
                void i(String s) {
                    a(s,this);
                }
            }
        }
    }

    public static void a(String s, Object o) {
        Tester.event(o.getClass().getName() + "-" + s);
    }
}
