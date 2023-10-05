import java.util.List;
import java.util.Locale;

/**
 * Inspired by examples in https://openjdk.org/jeps/427
 */
public class SwitchPatternPreview4OK {
  public static void main(String[] args) {

    System.out.println(formatterPatternSwitch(null));
    System.out.println(formatterPatternSwitch(123));
    System.out.println(formatterPatternSwitch(999L));
    System.out.println(formatterPatternSwitch(12.34));
    System.out.println(formatterPatternSwitch("foo"));
    System.out.println(formatterPatternSwitch(List.of(123, "foo", 999L, 12.34)));

    System.out.println(testCircle(new Rectangle()));
    System.out.println(testCircle(new Circle(5)));
    System.out.println(testCircle(new Circle(6)));

    System.out.println(testSealedCoverage(new A()));
    System.out.println(testSealedCoverage(new B()));
    System.out.println(testSealedCoverage(new C(5)));

//    constantLabelMustAppearBeforePattern(-1);
//    constantLabelMustAppearBeforePattern(0);
//    constantLabelMustAppearBeforePattern(42);
//    constantLabelMustAppearBeforePattern(-99);
//    constantLabelMustAppearBeforePattern(Integer.valueOf(123));
//    constantLabelMustAppearBeforePattern(null);

    // TODO: Activate when https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1466 is fixed.
    /*
    constantLabelMustAppearBeforePatternInteger(-1);
    constantLabelMustAppearBeforePatternInteger(0);
    constantLabelMustAppearBeforePatternInteger(42);
    constantLabelMustAppearBeforePatternInteger(-99);
    constantLabelMustAppearBeforePatternInteger(Integer.valueOf(123));
    constantLabelMustAppearBeforePatternInteger(null);
    */

    System.out.println(testGenericSealedExhaustive(new E<Integer>()));
  }

  static String formatterPatternSwitch(Object o) {
    return switch (o) {
      case null      -> "null";
      case Integer i -> String.format("int %d", i);
      case Long l    -> String.format("long %d", l);
      case Double d  -> String.format(Locale.ENGLISH, "double %f", d);
      case String s  -> String.format("String %s", s);
      default        -> o.toString();
    };
  }

  static class Shape {}
  static class Rectangle extends Shape {}
  static class Circle extends Shape {
    private final double radius;
    public Circle(double radius) { this.radius = radius; }
    double calculateArea() { return Math.PI * radius * radius; }
  }

  static String testCircle(Shape s) {
    return switch (s) {
      case Circle c when (c.calculateArea() > 100) -> "Large circle";
      case Circle c -> "Small circle";
      default -> "Non-circle";
    };
  }

  sealed interface S permits A, B, C {}
  final static class A implements S {}
  final static class B implements S {}
  static record C(int i) implements S {}  // Implicitly final

  static String testSealedCoverage(S s) {
    return switch (s) {
      case A a -> "Sealed sub-class A";
      case B b -> "Sealed sub-class B";
      case C c -> "Sealed sub-record C";
    };
  }

  /**
   * According to an example from JEP 420, this should work, but it does not, neither with Javac nor ECJ.
   *
   * See:
   *   https://openjdk.java.net/jeps/420#1b--Dominance-of-pattern-labels
   *   https://bugs.openjdk.java.net/browse/JDK-8273326
   *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=579355
   *
   * TODO: reactivate when implemented or move to preview 5 with Java 21, Eclipse 4.28.
   */
/*
  static String constantLabelMustAppearBeforePattern(Object o) {
    switch (o) {
      case null -> System.out.println("value unavailable: " + o);
      case -1, 1 -> System.out.println("special case:" + o);
      case Integer value when value > 0 -> System.out.println("positive integer: " + o);
      case Integer i -> System.out.println("other integer: " + o);
      default -> System.out.println("non-integer: " + o);
    }
    return o == null ? "null" : o.toString();
  }
*/

  /**
   * This used to work in preview 4 (Java 20), but fails during runtime with
   *   java.lang.IndexOutOfBoundsException: Index 4 out of bounds for length 4
   * in ECJ 3.36.0-SNAPSHOT with Java 21.
   * See:
   *   https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1466.
   *
   * TODO: Activate when https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1466 is fixed.
   */
  static String constantLabelMustAppearBeforePatternInteger(Integer i) {
    switch (i) {
      case null -> System.out.println("value unavailable: " + i);
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      case Integer value when value > 0 -> System.out.println("positive integer: " + i);
      default -> System.out.println("other integer: " + i);
    }
    return i == null ? "null" : i.toString();
  }

  static void nullCanAppearAfterConstantLabel(Integer i) {
    switch (i) {
      case -1, 1 -> System.out.println("absolute value 1: " + i);
      case null -> System.out.println("value unavailable: " + i);
      case Integer value when value > 0 -> System.out.println("positive integer: " + i);
      default -> System.out.println("other integer: " + i);
    }
  }

  /**
   * According to an example from JEP 420, this should work with preview 2 (Java 18), and it does with Javac,
   * but not with ECJ for Java 18, 19 and 20.
   *
   * See:
   *   https://openjdk.java.net/jeps/420#2--Exhaustiveness-of-switch-expressions-and-statements
   *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=579360
   *   https://github.com/eclipse-jdt/eclipse.jdt.core/issues/587
   *
   * TODO: reactivate when implemented or move to preview 5 with Java 21.
   */
  sealed interface I<T> permits D, E {}
  final static class D<X> implements I<String> {}
  final static class E<Y> implements I<Y> {}

  static int testGenericSealedExhaustive(I<Integer> i) {
    return switch (i) {
      // Exhaustive as no D case possible!
      case E<Integer> bi -> 42;
    };
  }
}
