aspect Aspect {
  int A.B.foo() {
     class C {
        int bar() { return A.this.x;}
     }
     return new C().bar();
  }
}
class A {
  int x = 1;
  class B { }
  B b = new B();
}

public class MissingAccessor2 {

    public static void main(String[] args) {
        A a = new A();
        System.out.println(a.b.foo());
    }
}
