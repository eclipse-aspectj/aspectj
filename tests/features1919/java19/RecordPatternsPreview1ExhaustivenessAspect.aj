public aspect RecordPatternsPreview1ExhaustivenessAspect {
  static Pair<I> p2 = new Pair<>(new C(), new D());

  public static void main(String[] args) {
    doSomething(p2);
  }

  public static void doSomething(Pair<I> pair) {
    System.out.println(pair.toString().replaceAll("@[0-9a-f]+", "@000"));
  }

  before(Pair<I> pair) : execution(* doSomething(Pair)) && args(pair) {
    switch (pair) {
      case Pair<I>(I i, C c) -> System.out.println("x");
      case Pair<I>(I i, D d) -> System.out.println("y");
      // Fixed since Java 21, see features1921/java21/RecordPatternsPreview1ExhaustivenessAspect.aj.
      default -> System.out.println("z");
    }

    switch (pair) {
      case Pair<I>(C c, I i) -> System.out.println("a");
      case Pair<I>(D d, C c) -> System.out.println("b");
      case Pair<I>(D d1, D d2) -> System.out.println("c");
      // Fixed since Java 21, see features1921/java21/RecordPatternsPreview1ExhaustivenessAspect.aj.
      default -> System.out.println("d");
    }
  }
}

sealed interface I permits C, D { }
final class C implements I { }
final class D implements I { }
record Pair<T>(T x, T y) { }
