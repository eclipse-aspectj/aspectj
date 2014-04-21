public class Code {
  public static void main(String[]argv) {
  }

  public void foo(UID x) {
      bar((x instanceof UID ? E.one : E.two));
  }

  public static void bar(FM fm) { }
}

aspect X {
  void around(): execution(* foo(..)) {
  }
}

class E {
    static BBB one;
    static CCC two;
    class BBB extends FM<String> {}
    class CCC extends FM<Long> {}
}
class FM<T> {}

class UID {}
