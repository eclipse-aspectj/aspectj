public class RecordPatternsPreview1ExhaustivenessError {
  static Pair<I> p2 = new Pair<>(new C(), new D());

  public static void main(String[] args) {
    exhaustiveSwitch();
  }

  static void exhaustiveSwitch() {
    switch (p2) {
      case Pair<I>(C fst, D snd) -> System.out.println("a");
      case Pair<I>(D fst, C snd) -> System.out.println("b");
      case Pair<I>(I fst, C snd) -> System.out.println("c");
    }
  }
}

class A { }
class B extends A { }
sealed interface I permits C, D { }
final class C implements I { }
final class D implements I { }
record Pair<T>(T x, T y) { }
