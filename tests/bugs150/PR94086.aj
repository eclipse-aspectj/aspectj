public aspect PR94086 {
    private static final SimpleLogger sl
        = new SimpleLogger();

    pointcut PC() :
        (execution(* Test.a(..)) && if (sl.isEnabled()))
        || (execution(* Test.b(..)) && if (sl.isEnabled()))
        || (execution(* Test.c(..)) && if (sl.isEnabled()))
        || (execution(* Test.d(..)) && if (sl.isEnabled()))
        || (execution(* Test.e(..)) && if (sl.isEnabled()))
        || (execution(* Test.f(..)) && if (sl.isEnabled()))
        || (execution(* Test.g(..)) && if (sl.isEnabled()))
        || (execution(* Test.h(..)) && if (sl.isEnabled()))
        || (execution(* Test.i(..)) && if (sl.isEnabled()))
        || (execution(* Test.j(..)) && if (sl.isEnabled()))
        ;

    before() : PC() {
        sl.log("Before");
    }

    after() : PC() {
        sl.log("After");
    }
}

class Test {
        public void a() {}
        public void b() {}
        public void c() {}
        public void d() {}
        public void e() {}
        public void f() {}
        public void g() {}
        public void h() {}
        public void i() {}
        public void j() {}
        public void k() {}
        public void l() {}
        public void m() {}
        public void n() {}
        public void o() {}
        public void p() {}
}


class SimpleLogger {
    private boolean enabled;

    public SimpleLogger() {
        enabled = false;
    }

    public void disable() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void log(String str) {
        if (enabled) {
            System.out.println("> Log: " + str);
        }

    }

}
