import org.aspectj.testing.*;

/** @testcase PUREJAVA PR#739 final local variables can be accessed from inner class */
public class LocalsFromInnerCP {
    public static void main (String[] args) {
        // static init
        LocalsFromInnerCP.registerAll();
        LocalsFromInnerCP me = new LocalsFromInnerCP();
        me = new LocalsFromInnerCP(1);
        me.f();
        LocalsFromInnerCP.sf();
        me.f(1);
        LocalsFromInnerCP.sf(1);
        me.m();
        me.m(1);
        me.new m().f();
        me.new m().f(1);
        Tester.check(globali == 11, "globali: " + globali);
        Tester.checkAllEvents();
    } 
    
    void f() {
        final int i = 1;
        new Runnable() {
                public void run() { signal("LocalsFromInnerCP.f().Runnable.run()", i); } 
            }.run();
    }

    static void sf() {
        final int i = 1;
        new Runnable() {
                public void run() { signal("LocalsFromInnerCP.sf().Runnable.run()", i); } 
            }.run();
    }

    void f(final int i) {
        new Runnable() {
                public void run() { signal("LocalsFromInnerCP.f(int).Runnable.run()", i); } 
            }.run();
    }

    static void sf(final int i) {
        new Runnable() {
                public void run() { signal("LocalsFromInnerCP.sf(int).Runnable.run()", i); } 
            }.run();
    }

    static {
        final int i = 1;
        new Runnable() {
                public void run() { signal("LocalsFromInnerCP.<clinit>.Runnable.run()", i); } 
            }.run();
    }

    void m(final int i) {
        class m {
            void f() {
                new Runnable() {
                        public void run() { signal("LocalsFromInnerCP.m(int).m.f().Runnable.run()", i); } 
                    }.run();
            }
        }
        new m().f();
    }

    void m() {
        final int i = 1;
        class m {
            void f() {
                new Runnable() {
                        public void run() { signal("LocalsFromInnerCP.m().m.f().Runnable.run()", i); } 
                    }.run();
            }
        }
        new m().f();
    }

    class m {
        void f() {
            final int i = 1;
            new Runnable() {
                    public void run() { signal("LocalsFromInnerCP.m.f().Runnable.run()", i); } 
                }.run();
        }
 
        void f(final int i) {
            new Runnable() {
                    public void run() { signal("LocalsFromInnerCP.m.f(int).Runnable.run()", i); } 
                }.run();
        }
    }

    LocalsFromInnerCP() {
        final int i = 1;
        new Runnable() {
                public void run() { signal("LocalsFromInnerCP.LocalsFromInnerCP().Runnable.run()", i); } 
            }.run();
    }

    LocalsFromInnerCP(final int i) {
        new Runnable() {
                public void run() { signal("LocalsFromInnerCP.LocalsFromInnerCP(int).Runnable.run()", i); } 
            }.run();
    }
    //----------------------------------------------------------
    // sed -n '/"/p' pureJava/LocalsFromInnerCP.java 
    //   | sed 's|.*"\(.*\)".*|    register("\1");|' 
    // >> pureJava/LocalsFromInnerCP.java
    static void registerAll() {
        register("LocalsFromInnerCP.f().Runnable.run()");
        register("LocalsFromInnerCP.sf().Runnable.run()");
        register("LocalsFromInnerCP.f(int).Runnable.run()");
        register("LocalsFromInnerCP.sf(int).Runnable.run()");
        register("LocalsFromInnerCP.<clinit>.Runnable.run()");
        register("LocalsFromInnerCP.m(int).m.f().Runnable.run()");
        register("LocalsFromInnerCP.m().m.f().Runnable.run()");
        register("LocalsFromInnerCP.m.f().Runnable.run()");
        register("LocalsFromInnerCP.m.f(int).Runnable.run()");
        register("LocalsFromInnerCP.LocalsFromInnerCP().Runnable.run()");
        register("LocalsFromInnerCP.LocalsFromInnerCP(int).Runnable.run()");
    }
    static void register(String s) {
        Tester.expectEvent(s);
    }
    static int globali;
    static void signal(String s, int i) {
        Tester.event(s);
        Tester.check(i == 1, "1 != i=" + i + " for " + s);
        globali++;
    }
}
