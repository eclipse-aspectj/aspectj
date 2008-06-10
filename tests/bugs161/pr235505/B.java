privileged abstract aspect A<T> {
  public void foo(T t) { System.out.println(t); }
  before(T t): execution(* *(..)) && args(t) && !within(A+) { foo(t); }
}

aspect X extends A<String> {}

public class B {
  public static void main(String []argv) {
    new B().run("Hello World");
  }

  public void run(String s) {
    System.out.println(s);
  }
}
