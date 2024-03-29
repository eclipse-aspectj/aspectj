public class RecordPatternsPreview1ExhaustivenessOK1 {
  static Pair<A> p1 = new Pair<>(new A(), new B());
  static Pair<I> p2 = new Pair<>(new C(), new D());

  public static void main(String[] args) {
    exhaustiveSwitch();
  }

  static void exhaustiveSwitch() {
    switch (p2) {
      case Pair<I>(I i, C c) -> System.out.println("x");
      case Pair<I>(I i, D d) -> System.out.println("y");
      // Redundant default clause no longer necessary after fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455.
      // Old version with default clause see features1919/java19/RecordPatternsPreview1ExhaustivenessOK1.java.
      // default -> System.out.println("z");
    }

    switch (p2) {
      case Pair<I>(C c, I i) -> System.out.println("a");
      case Pair<I>(D d, C c) -> System.out.println("b");
      case Pair<I>(D d1, D d2) -> System.out.println("c");
      // Redundant default clause no longer necessary after fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/455.
      // Old version with default clause see features1919/java19/RecordPatternsPreview1ExhaustivenessOK1.java.
      // default -> System.out.println("d");
    }
  }
}

class A { }
class B extends A { }
sealed interface I permits C, D { }
final class C implements I { }
final class D implements I { }
record Pair<T>(T x, T y) { }
