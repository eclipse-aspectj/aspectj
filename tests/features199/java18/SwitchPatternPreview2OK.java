import java.util.List;

/**
 * Inspired by examples in https://openjdk.java.net/jeps/420
 */
public class SwitchPatternPreview2OK {
  public static void main(String[] args) {
    constantLabelMustAppearBeforePattern(-1);
    constantLabelMustAppearBeforePattern(0);
    constantLabelMustAppearBeforePattern(42);
    constantLabelMustAppearBeforePattern(-99);
    constantLabelMustAppearBeforePattern(Integer.valueOf(123));
    constantLabelMustAppearBeforePattern(null);

    System.out.println(testGenericSealedExhaustive(new B<Integer>()));
  }

  static String constantLabelMustAppearBeforePattern(Object o) {
    switch (o) {
      case -1, 1 -> System.out.println("special case:" + o);
      case Integer i && i > 0 -> System.out.println("positive integer: " + o);
      case Integer i -> System.out.println("other integer: " + o);
      default -> System.out.println("non-integer: " + o);
    }
    return o.toString();
  }

  sealed interface I<T> permits A, B {}
  final static class A<X> implements I<String> {}
  final static class B<Y> implements I<Y> {}

  static int testGenericSealedExhaustive(I<Integer> i) {
    return switch (i) {
      // Exhaustive as no A case possible!
      case B<Integer> bi -> 42;
    };
  }
}
