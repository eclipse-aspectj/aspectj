/**
 * Inspired by examples in https://openjdk.java.net/jeps/420
 */
public class SwitchPatternPreview2OK {
  public static void main(String[] args) {
//    constantLabelMustAppearBeforePattern(-1);
//    constantLabelMustAppearBeforePattern(0);
//    constantLabelMustAppearBeforePattern(42);
//    constantLabelMustAppearBeforePattern(-99);
//    constantLabelMustAppearBeforePattern(Integer.valueOf(123));
//    constantLabelMustAppearBeforePattern(null);

    constantLabelMustAppearBeforePatternInteger(-1);
    constantLabelMustAppearBeforePatternInteger(0);
    constantLabelMustAppearBeforePatternInteger(42);
    constantLabelMustAppearBeforePatternInteger(-99);
    constantLabelMustAppearBeforePatternInteger(Integer.valueOf(123));
    constantLabelMustAppearBeforePatternInteger(null);

//    System.out.println(testGenericSealedExhaustive(new B<Integer>()));
  }

  /**
   * According to an example from JEP 420, this should work, but it does not, neither with Javac nor ECJ.
   *
   * See:
   *   https://openjdk.java.net/jeps/420#1b--Dominance-of-pattern-labels
   *   https://bugs.openjdk.java.net/browse/JDK-8273326
   *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=579355
   *
   * TODO: reactivate when implemented or move to preview 3 with Java 19, Eclipse 4.24.
   */
  /*
  static String constantLabelMustAppearBeforePattern(Object o) {
    switch (o) {
      case null -> System.out.println("value unavailable: " + i);
      case -1, 1 -> System.out.println("special case:" + o);
      case Integer i && i > 0 -> System.out.println("positive integer: " + o);
      case Integer i -> System.out.println("other integer: " + o);
      default -> System.out.println("non-integer: " + o);
    }
    return i == null ? "null" : i.toString();
  }
  */

  static String constantLabelMustAppearBeforePatternInteger(Integer i) {
    switch (i) {
      case null -> System.out.println("value unavailable: " + i);
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      case Integer value && value > 0 -> System.out.println("positive integer: " + i);
      default -> System.out.println("other integer: " + i);
    }
    return i == null ? "null" : i.toString();
  }

  static void nullCanAppearAfterConstantLabel(Integer i) {
    switch (i) {
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      case null -> System.out.println("value unavailable: " + i);
      case Integer value && value > 0 -> System.out.println("positive integer: " + i);
      default -> System.out.println("other integer: " + i);
    }
  }

  static void defaultCanAppearBeforePattern(Integer i) {
    switch (i) {
      case null -> System.out.println("value unavailable: " + i);
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      default -> System.out.println("other integer: " + i);
      case Integer value && value > 0 -> System.out.println("positive integer: " + i);
    }
  }

  static void defaultCanAppearBeforeNull(Integer i) {
    switch (i) {
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      default -> System.out.println("other integer: " + i);
      case null -> System.out.println("value unavailable: " + i);
      case Integer value && value > 0 -> System.out.println("positive integer: " + i);
    }
  }

  static void defaultCanAppearBeforeConstantLabel(Integer i) {
    switch (i) {
      case null -> System.out.println("value unavailable: " + i);
      default -> System.out.println("other integer: " + i);
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      case Integer value && value > 0 -> System.out.println("positive integer: " + i);
    }
  }

  /**
   * According to an example from JEP 420, this should work, and it does with Javac, but not with ECJ.
   *
   * See:
   *   https://openjdk.java.net/jeps/420#2--Exhaustiveness-of-switch-expressions-and-statements
   *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=579360
   *
   * TODO: reactivate when implemented or move to preview 3 with Java 19, Eclipse 4.24.
   */
  /*
  sealed interface I<T> permits A, B {}
  final static class A<X> implements I<String> {}
  final static class B<Y> implements I<Y> {}

  static int testGenericSealedExhaustive(I<Integer> i) {
    return switch (i) {
      // Exhaustive as no A case possible!
      case B<Integer> bi -> 42;
    };
  }
  */
}
