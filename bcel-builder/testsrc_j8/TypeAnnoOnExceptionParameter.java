public class TypeAnnoOnExceptionParameter {
  class Throwable2 extends Throwable {}
  class Throwable3 extends Throwable {}
  public void m() {
    try {
      foo();
    } catch (@Anno Throwable3 | @Anno(99) Throwable2 t) {
    }
  }
  public void foo() throws Throwable2,Throwable3 {}
}
