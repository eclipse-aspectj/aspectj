privileged public aspect A {
    public final static String B.s = C.f.toString();
}

class B { }

class C {
 public static final C f = new C();
}

