class C implements II {}

class D {
  public static void m() {
    C c = new C();
    E1 e1 = new E1();
    E2 e2 = new E2();
    c.foo(e1,e2.getClass());
  }
}

class E1 implements I1 {}
class E2 implements I2 {}
